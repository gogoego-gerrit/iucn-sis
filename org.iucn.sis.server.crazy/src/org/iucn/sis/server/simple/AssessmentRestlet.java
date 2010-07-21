package org.iucn.sis.server.simple;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.iucn.sis.server.baserestlets.ServiceRestlet;
import org.iucn.sis.server.filters.AssessmentFilterHelper;
import org.iucn.sis.server.io.AssessmentIO;
import org.iucn.sis.server.io.TaxaIO;
import org.iucn.sis.server.io.AssessmentIO.AssessmentIOWriteResult;
import org.iucn.sis.server.locking.FileLocker;
import org.iucn.sis.server.locking.LockType;
import org.iucn.sis.server.locking.LockRepository.Lock;
import org.iucn.sis.server.utils.DocumentUtils;
import org.iucn.sis.server.utils.IDFactory;
import org.iucn.sis.server.utils.MostRecentFlagger;
import org.iucn.sis.server.utils.ServerPaths;
import org.iucn.sis.server.utils.logging.DBAssessmentBuffer;
import org.iucn.sis.server.utils.logging.DBTrashBuffer;
import org.iucn.sis.server.utils.logging.EventLogger;
import org.iucn.sis.shared.BaseAssessment;
import org.iucn.sis.shared.data.assessments.AssessmentData;
import org.iucn.sis.shared.data.assessments.AssessmentFilter;
import org.iucn.sis.shared.data.assessments.AssessmentParser;
import org.iucn.sis.shared.taxonomyTree.TaxonNode;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.xml.DomRepresentation;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.solertium.db.DBException;
import com.solertium.lwxml.factory.NativeDocumentFactory;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.NativeNodeList;
import com.solertium.util.NodeCollection;
import com.solertium.util.SysDebugger;
import com.solertium.vfs.NotFoundException;
import com.solertium.vfs.VFS;
import com.solertium.vfs.VFSPath;
import com.solertium.vfs.VFSPathToken;
import com.solertium.vfs.utils.VFSUtils;
import com.solertium.vfs.utils.VFSUtils.VFSPathParseException;

public class AssessmentRestlet extends ServiceRestlet {
	
	private DBAssessmentBuffer buffer;
	private IDFactory pubIDFactory;
	private DBTrashBuffer trashBuffer;

	public AssessmentRestlet(String vfsroot, Context context) {
		super(vfsroot, context);
		pubIDFactory = IDFactory.getIDFactory(vfs, "/browse/assessments/assessmentCount");
		
		try {
			buffer = new DBAssessmentBuffer();
			trashBuffer = new DBTrashBuffer();
			EventLogger.impl.addBuffer(buffer);
			EventLogger.impl.addBuffer(trashBuffer);
		} catch (DBException e) {
		}
	}

	/**
	 * @param assessment
	 * @param username
	 * @param speciesID
	 * @param displayName
	 * @return
	 */
	private AssessmentIOWriteResult assignIDAndSave(AssessmentData assessment, String username) throws RegionConflictException {
		if( assessment.getAssessmentID().equals("new")) {
			if( assessment.getType().equals(BaseAssessment.DRAFT_ASSESSMENT_STATUS) )
				assessment.setAssessmentID(getNextDraftID(vfs, assessment.getSpeciesID()));
			else if( assessment.getType().equals(BaseAssessment.PUBLISHED_ASSESSMENT_STATUS) )
				assessment.setAssessmentID(pubIDFactory.getNextIDAsString());
			else //TODO: Figure out if we're going to need multiple user assessments per species.
				assessment.setAssessmentID(assessment.getSpeciesID());
		}
		
		return saveAssessment(assessment, username);
	}

	@Override
	public void definePaths() {
		paths.add("/assessments");
		paths.add("/assessments/{type}");
		paths.add("/assessments/{type}/{id}");
		
		paths.add("/assessmentsByTaxon/{taxonID}");
		paths.add("/assessmentsByTaxon/{taxonID}/{type}");
	}

	private void deleteAssessment(Request request, Response response, final String assessmentID,
			final String assessmentType, final String username) {
		try {
			AssessmentData assessment = AssessmentIO.readAssessment(vfs, assessmentID, assessmentType, username);
			boolean deleted = AssessmentIO.trashAssessment(vfs, assessment, username);

			if( deleted ) {
				String log = "<assessment user=\"" + username;
				log += "\" status=\"" + assessmentType.toLowerCase().replace("_status", "");
				log += "\" date=\"" + new Date().toString() + "\" node=\"" + assessment.getSpeciesName() + "\">" 
						+ assessmentID + "</assessment>";

				trashBuffer.addEvent(DocumentUtils.createDocumentFromString(log));
				trashBuffer.flush();

				// remove from recent
				final Request req = new Request(Method.DELETE, "riap://host/recentAssessments/" + username
						+ assessmentType + "/" + assessmentID);
				Response resp = getContext().getClientDispatcher().handle(req);
				if (!(resp.getStatus()).isSuccess()) {
					System.out.println("Unable to delete assessment from recent.");
				} else {
					// node.removeAssessment(pubAssessments.get(i));
					System.out.println("Assessment deleted from recent.");
				}
				
				response.setStatus(Status.SUCCESS_OK);
			} else
				response.setStatus(Status.CLIENT_ERROR_LOCKED);
		} catch (Exception e) {
			SysDebugger.getInstance().println("Could not find assessment " + assessmentID);
			e.printStackTrace();
			response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
		}
	}

	private void deleteAssessmentsForTaxon(String taxonID, String type, String user, Request request, Response response) {
		List<AssessmentData> assessments = AssessmentIO.readAllAssessmentsForTaxon(vfs, taxonID, user);
		for( AssessmentData cur : assessments ) {
			deleteAssessment(request, response, cur.getAssessmentID(), cur.getType(), user);
			
			//If it's locked, force the release and delete again. Taxomatic operations are almighty.
			if( response.getStatus() == Status.CLIENT_ERROR_LOCKED ) {
				Lock lock = FileLocker.impl.getAssessmentPersistentLock(cur.getAssessmentID(), cur.getType());
				FileLocker.impl.persistentEagerRelease(cur.getAssessmentID(), cur.getType(), lock.getUsername());
				deleteAssessment(request, response, cur.getAssessmentID(), cur.getType(), user);
			}
		}
	}
	
	public static synchronized String getNextDraftID(VFS vfs, String speciesID) {
		int nextID = 0;
		VFSPath rootURI;

		try {
			rootURI = VFSUtils.parseVFSPath(ServerPaths.getDraftAssessmentRootURL(speciesID));
		} catch (VFSPathParseException e) {
			e.printStackTrace();
			return null;
		}

		if (vfs.exists(rootURI)) { // If there's already regionals for this guy,
			// get the next
			VFSPathToken[] tokens;
			try {
				tokens = vfs.list(rootURI);
				for (VFSPathToken curToken : tokens) {
					String filename = curToken.toString();
					filename = filename.replaceAll(".xml", "");
					if (!SISContainerApp.amIOnline())
						filename = filename.replaceAll("offline", "");

					System.out.println("Crawling file " + curToken + " for new regional ID.");

					try {
						int value = Integer.valueOf(filename.split("_")[1]);
						nextID = Math.max(value, nextID);
					} catch (NumberFormatException e) {
						SysDebugger.getNamedInstance(SISContainerApp.SEVERE_LOG).println(
								"Annoying file in path " + curToken + " non-conformant "
										+ "to standard region assessment file name pattern.");
					}
				}
			} catch (NotFoundException e) {
				SysDebugger.getNamedInstance(SISContainerApp.SEVERE_LOG).println(
						"Big WTF. " + "List failed on existing path " + rootURI.toString());
				return null;
			}

			nextID++; // Increment it one past the highest found.
		}

		String assessmentID = speciesID + "_" + nextID;
		return assessmentID;
	}

	@Override
	public void performService(Request request, Response response) {
		try {
		String username = request.getChallengeResponse().getIdentifier();
		
		if (username == null || username.equals("")) {
			response.setStatus(Status.CLIENT_ERROR_FORBIDDEN);
		} else if (request.getMethod().equals(Method.GET)) {
			String id = (String)request.getAttributes().get("id");
			String ret = null;
			
			try {
				if( id == null )
					ret = getAssessments(request, username);
				else
					ret = AssessmentIO.readAssessmentAsString(vfs, id, 
							(String)request.getAttributes().get("type"), username);

				if( ret != null ) {
					response.setEntity(ret, MediaType.TEXT_XML);
					response.setStatus(Status.SUCCESS_OK);
				} else
					response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			} catch (IOException e) {
				e.printStackTrace();
				response.setStatus(Status.SERVER_ERROR_INTERNAL);
			}
		} else if (request.getMethod().equals(Method.PUT)) {
			try {
				putAssessment(request, response, username);
			} catch (Throwable e) {
				e.printStackTrace();
			}
		} else if (request.getMethod().equals(Method.POST)) {
			String action = request.getResourceRef().getQueryAsForm().getFirstValue("action"); 
			if (action == null) {
				postAssessment(request, response, username);
			} else if (action.equalsIgnoreCase("fetch")) {
				try {
					String ret = getAssessments(request, username);
					
					if( ret != null ) {
						response.setEntity(ret, MediaType.TEXT_XML);
						response.setStatus(Status.SUCCESS_OK);
					} else
						response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
				} catch (IOException e) {
					e.printStackTrace();
					response.setStatus(Status.SERVER_ERROR_INTERNAL);
				}
			} else if (action.equalsIgnoreCase("batch")) {
				String ret = batchCreate(request, username);
				
				if( ret != null ) {
					response.setEntity(ret, MediaType.TEXT_XML);
					response.setStatus(Status.SUCCESS_OK);
				} else {
					response.setStatus(Status.SERVER_ERROR_INTERNAL);
				}
			}
		} else if (request.getMethod().equals(Method.DELETE)) {
			if( request.getResourceRef().getPath().startsWith("assessmentsByTaxon") ) {
				String taxonID = (String)request.getAttributes().get("id");
				String type = (String)request.getAttributes().get("type");
				deleteAssessmentsForTaxon(taxonID, type, username, request, response);
			} else {
				String id = (String)request.getAttributes().get("id");
				String type = (String)request.getAttributes().get("type");
				deleteAssessment(request, response, id, type, username);
			}
		} else {
			response.setStatus(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
		}
		} catch (Exception e) {
			e.printStackTrace();
			response.setStatus(Status.SERVER_ERROR_INTERNAL);
		}
	}

	/**
	 * Format of a proper get request:
	 * &lt;uid&gt;(assessmentUID)&lt;/uid&gt;
	 * 
	 * or
	 * 
	 * &lt;taxon&gt;(taxonID)&lt;/taxon&gt;
	 * 
	 * If the type parameter is null for the latter format, all .
	 * 
	 * @param getEntity
	 * @param type
	 * @return
	 */
	private String getAssessments(Request request, String user) throws IOException {
		String type = (String)request.getAttributes().get("type");
		Document doc = new DomRepresentation(request.getEntity()).getDocument();
		StringBuilder ret = new StringBuilder("<assessments>");
		
		NodeCollection uidNodes = new NodeCollection(doc.getElementsByTagName("uid"));
		if( uidNodes.size() > 0 ) {
			Map<String, List<String>> typeToIDs = new HashMap<String, List<String>>();
			for( Node curNode : uidNodes ) {
				String uid = curNode.getTextContent();
				int splitIndex = uid.lastIndexOf("_", uid.lastIndexOf("_")-1);

				if( splitIndex > 0 ) {
					String curType = uid.substring(splitIndex+1);

					List<String> typeList = typeToIDs.get(curType);
					if( typeList == null ) {
						typeList = new ArrayList<String>();
						typeToIDs.put(uid.substring(splitIndex+1), typeList);
					}

					typeList.add(uid.substring(0, splitIndex));
				} else {
					System.out.println("Split index for UID " + uid + " is obviously shite.");
				}
			}

			for( Entry<String, List<String>> curEntry : typeToIDs.entrySet() )
				for( String curID : curEntry.getValue() )
					ret.append(AssessmentIO.readAssessmentAsString(vfs, curID, curEntry.getKey(), user));
		}
		
		NodeCollection taxaNodes = new NodeCollection(doc.getElementsByTagName("taxon"));
		if( taxaNodes.size() > 0 ) {
			for( Node curNode : taxaNodes ) {
				String taxonID = curNode.getTextContent();

				if( type == null )
					for( String cur : AssessmentIO.readAllAssessmentsForTaxonAsStrings(vfs, taxonID, user) )
						ret.append(cur);
				else if( type.equals(BaseAssessment.DRAFT_ASSESSMENT_STATUS ))
					for( String cur : AssessmentIO.readAllDraftAssessmentsAsStrings(vfs, taxonID) )
						ret.append(cur);
				else if( type.equals(BaseAssessment.PUBLISHED_ASSESSMENT_STATUS ))
					for( String cur : AssessmentIO.readPublishedAssessmentsForTaxonAsStrings(vfs, taxonID) )
						ret.append(cur);
			}
		}
		
		ret.append("</assessments>");
		return ret.toString();

	}
	
	private void postAssessment(Request request, Response response, String username) {
		try {
			String entity =request.getEntity().getText();
			
			NativeDocument doc = NativeDocumentFactory.newNativeDocument();
			doc.parse(entity);
			AssessmentParser parser = new AssessmentParser(doc);
			AssessmentData assessment = parser.getAssessment();

			VFSPath assessmentUrl = new VFSPath(ServerPaths.getPathForAssessment(assessment, username));

			if (vfs.exists(assessmentUrl)) {
				Status status = FileLocker.impl.persistentLockAssessment(assessment.getAssessmentID(),
						BaseAssessment.DRAFT_ASSESSMENT_STATUS, LockType.SAVE_LOCK, username);

				if (status.isSuccess()) {
					AssessmentIOWriteResult result = saveAssessment(assessment, username);
					if( result.status.isSuccess() ) {
						response.setEntity(result.newLastModified+"", MediaType.TEXT_PLAIN);
						response.setStatus(status);
					} else {
						response.setStatus(Status.CLIENT_ERROR_EXPECTATION_FAILED);
					}
				} else {
					response.setStatus(status);
				}
			}
		} catch (RegionConflictException e) {
			response.setStatus(Status.CLIENT_ERROR_CONFLICT);
		} catch (Exception e) {
			e.printStackTrace();
			response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
		}
	}

	private String batchCreate(Request request, String username) {
		NativeDocument doc = NativeDocumentFactory.newNativeDocument();
		StringBuffer successfulIDs = new StringBuffer();
		StringBuffer extantIDs = new StringBuffer();
		StringBuffer unsuccessfulIDs = new StringBuffer();

		try {
			String text = request.getEntity().getText();
			doc.parse(text);
			
			AssessmentFilter filter = AssessmentFilter.parseXML(doc.getDocumentElement().getElementsByTagName(
					AssessmentFilter.HEAD_TAG).elementAt(0));
			
			NativeNodeList nodes = doc.getDocumentElement().getElementsByTagName("taxon");
			boolean useTemplate = Boolean.parseBoolean(doc.getDocumentElement().getElementsByTagName(
					"useTemplate").elementAt(0).getTextContent());
			System.out.println("Using template? " + useTemplate);
			
			for (int i = 0; i < nodes.getLength(); i++) {
				TaxonNode taxon = TaxaIO.readNode(nodes.elementAt(i).getTextContent(), vfs);
				AssessmentData curAss = null;
				
				curAss = doCreateAssessmentForBatch(request.getChallengeResponse().getIdentifier(), 
						filter, useTemplate, taxon);
				
				try {
					AssessmentIOWriteResult result = assignIDAndSave(curAss, username);
					if (result.status.isSuccess())
						successfulIDs.append(curAss.getSpeciesName() + ( i == nodes.getLength()-1 ? "" : ", " ));
					else
						unsuccessfulIDs.append(curAss.getSpeciesName() + ( i == nodes.getLength()-1 ? "" : ", " ));
				} catch(RegionConflictException e) {
					extantIDs.append(curAss.getSpeciesName() + ( i == nodes.getLength()-1 ? "" : ", ") );
				}
			}

			StringBuilder ret = new StringBuilder();
			if( unsuccessfulIDs.length() > 0 )
				ret.append("<div>Unable to create an assessment for the following species: " + unsuccessfulIDs + "</div>\r\n");
			if( extantIDs.length() > 0 )	
				ret.append("<div>The following species already have draft assessments with the specific locality: " + extantIDs + "</div>\r\n");
			if( successfulIDs.length() > 0 )
				ret.append("<div>Successfully created an assessment for the following species: " + successfulIDs + "</div>\r\n");

			return ret.toString();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} catch (NullPointerException e) {
			e.printStackTrace();
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} 
	}

	private AssessmentData doCreateAssessmentForBatch(String user, AssessmentFilter filter, boolean useTemplate,
			TaxonNode taxon) {
		AssessmentFilter draftFilter = filter.deepCopy();
		draftFilter.setDraft(false);
		draftFilter.setRecentPublished(true);
		draftFilter.setAllPublished(false);
		
		AssessmentFilterHelper helper = new AssessmentFilterHelper(draftFilter);
		
		AssessmentData curAss = null;
		
		if( useTemplate ) {
			List<AssessmentData> assessments = helper.getAssessments(taxon.getId()+"", vfs);
			if( assessments.size() == 0 ) {
				draftFilter.getRegions().clear();
				draftFilter.getRegions().add(AssessmentData.GLOBAL_ID);
				assessments = helper.getAssessments(taxon.getId()+"", vfs);
			}
			
			if( assessments.size() == 0 ) {
				System.out.println("No template exists for species " + taxon.getFullName());
				curAss = new AssessmentData(); //No template exists...
			} else {
				curAss = assessments.get(0).deepCopy();
			}
		} else
			curAss = new AssessmentData();
		
		curAss.setRegionIDs(filter.getRegions());
		if( !filter.getRegions().contains("-1") )
			curAss.setEndemic(false);
		curAss.setType(AssessmentData.DRAFT_ASSESSMENT_STATUS);
		curAss.setAssessmentID("new");
		curAss.setSpeciesID(taxon.getId()+"");
		curAss.setSpeciesName(taxon.getFullName());
		return curAss;
	}

	private void putAssessment(Request request, Response response, String username) {
		try {
			NativeDocument doc = NativeDocumentFactory.newNativeDocument();
			doc.parse(request.getEntity().getText());
			AssessmentParser parser = new AssessmentParser(doc);
			AssessmentData assessment = parser.getAssessment();

			AssessmentIOWriteResult result = assignIDAndSave(assessment, username);
			if (result.status.isSuccess()) {
				response.setEntity(assessment.getAssessmentID(), MediaType.TEXT_PLAIN);
				response.setStatus(Status.SUCCESS_OK);
			} else {
				response.setStatus(Status.CLIENT_ERROR_EXPECTATION_FAILED);
			}
		} catch (RegionConflictException e) {
			response.setStatus(Status.CLIENT_ERROR_CONFLICT);
		} catch (Exception e) {
			response.setStatus(Status.SERVER_ERROR_INTERNAL);
		}
	}

	private AssessmentIOWriteResult saveAssessment(AssessmentData assessment, String username) throws RegionConflictException {
		if( assessment.getType().equals(BaseAssessment.DRAFT_ASSESSMENT_STATUS) ) {
			List<AssessmentData> compareTo = AssessmentIO.readAllDraftAssessments(vfs, assessment.getSpeciesID());

			for( AssessmentData cur : compareTo ) {
				if( cur.getRegionIDs().containsAll(assessment.getRegionIDs()) || cur.isGlobal() && assessment.isGlobal() ) {
					if( !cur.getAssessmentID().equals(assessment.getAssessmentID() ))
						throw new RegionConflictException();
				}
			}
		}
		
		return AssessmentIO.writeAssessment(assessment, username, vfs, true);
	}
	
	private class RegionConflictException extends Exception {
	}
}
