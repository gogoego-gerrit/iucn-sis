//package org.iucn.sis.server.simple;
//
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.List;
//
//import org.iucn.sis.server.baserestlets.ServiceRestlet;
//import org.iucn.sis.server.filters.AssessmentFilterHelper;
//import org.iucn.sis.server.io.AssessmentIO;
//import org.iucn.sis.server.io.TaxaIO;
//import org.iucn.sis.server.io.AssessmentIO.AssessmentIOWriteResult;
//import org.iucn.sis.server.locking.FileLocker;
//import org.iucn.sis.server.utils.DocumentUtils;
//import org.iucn.sis.server.utils.ServerPaths;
//import org.iucn.sis.server.utils.logging.DBAssessmentBuffer;
//import org.iucn.sis.server.utils.logging.DBTrashBuffer;
//import org.iucn.sis.server.utils.logging.EventLogger;
//import org.iucn.sis.shared.BaseAssessment;
//import org.iucn.sis.shared.data.assessments.AssessmentData;
//import org.iucn.sis.shared.data.assessments.AssessmentFilter;
//import org.iucn.sis.shared.data.assessments.AssessmentParser;
//import org.iucn.sis.shared.data.assessments.Region;
//import org.restlet.Context;
//import org.restlet.data.CharacterSet;
//import org.restlet.data.MediaType;
//import org.restlet.data.Method;
//import org.restlet.data.Request;
//import org.restlet.data.Response;
//import org.restlet.data.Status;
//import org.restlet.representation.Representation;
//import org.restlet.representation.StringRepresentation;
//import org.w3c.dom.Document;
//import org.w3c.dom.Element;
//import org.w3c.dom.NodeList;
//
//import com.solertium.db.DBException;
//import com.solertium.lwxml.factory.NativeDocumentFactory;
//import com.solertium.lwxml.shared.NativeDocument;
//import com.solertium.lwxml.shared.NativeElement;
//import com.solertium.lwxml.shared.NativeNodeList;
//import com.solertium.util.SysDebugger;
//import com.solertium.vfs.ConflictException;
//import com.solertium.vfs.NotFoundException;
//import com.solertium.vfs.VFSMetadata;
//import com.solertium.vfs.VFSPath;
//import com.solertium.vfs.VFSPathToken;
//import com.solertium.vfs.utils.VFSUtils;
//import com.solertium.vfs.utils.VFSUtils.VFSPathParseException;
//
//public class DraftAssessmentRestlet extends ServiceRestlet {
//	public static boolean isRegional(String assessmentID) {
//		if (assessmentID.indexOf("_") > -1) {
//			return true;
//		} else
//			return false;
//	}
//
//	private DBAssessmentBuffer buffer;
//
//	private DBTrashBuffer trashBuffer;
//
//	public DraftAssessmentRestlet(String vfsroot, Context context) {
//		super(vfsroot, context);
//		try {
//			buffer = new DBAssessmentBuffer();
//			trashBuffer = new DBTrashBuffer();
//			EventLogger.impl.addBuffer(buffer);
//			EventLogger.impl.addBuffer(trashBuffer);
//		} catch (DBException e) {
//		}
//	}
//
//	/**
//	 * Called when want to create a batch of draft assessments given a filter,
//	 * a set of taxaIDs and a boolean that says whether to create a blank one,
//	 * or create one from a previously published assessment
//	 * 
//	 * called from within the working set ui for adding assessments to the workingset
//	 * 
//	 * @param request
//	 * @param response
//	 */
//	private void createBatchDraftAssessments(Request request, Response response) {
//
//		Document doc;
//		try {
//			String text = request.getEntityAsText();
//			System.out.println("this is text: \n" + text);
//			doc = DocumentUtils.createDocumentFromString(text);
//		} catch (Exception e) {
//			e.printStackTrace();
//			response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
//			return;
//		}
//		try{
//			String username = request.getChallengeResponse().getIdentifier();
//			Element docElement = doc.getDocumentElement();
//			NodeList asmFilter = docElement.getElementsByTagName(AssessmentFilter.HEAD_TAG);
//			NativeDocument ndoc = NativeDocumentFactory.newNativeDocument();
//			System.out.println(DocumentUtils.serializeDocumentToString(doc));
//			ndoc.parse(DocumentUtils.serializeElementToString((Element)asmFilter.item(0)));
//			AssessmentFilter filter = AssessmentFilter.parseXML(ndoc.getDocumentElement());
//			boolean useTemplate = Boolean.parseBoolean(docElement.getElementsByTagName("useTemplate").item(0).getTextContent());
//			ArrayList<String> listOfTaxa = new ArrayList<String>();
//			NodeList taxa = ((Element)docElement.getElementsByTagName("taxa").item(0)).getElementsByTagName("taxon");
//			List<AssessmentData> assessmentsToSave = new ArrayList<AssessmentData>();
//			List<String> taxaIdsAlreadyExist = new ArrayList<String>();
//			List<AssessmentData> successfullySaved = new ArrayList<AssessmentData>();
//			List<AssessmentData> locked = new ArrayList<AssessmentData>();
//			List<AssessmentData> perms = new ArrayList<AssessmentData>();
//			for (int i = 0; i < taxa.getLength(); i++)
//			{
//				String taxaID = taxa.item(i).getTextContent();
//				listOfTaxa.add(taxaID);
//				AssessmentData data = getTemplateToSaveIfNotAlreadyExist(username, taxaID, filter, useTemplate);
//				if (data != null)
//				{
//					AssessmentIOWriteResult result = getAndSaveNextRegionalID(data, username);
//					if (result.status.isSuccess())
//						successfullySaved.add(data);
//					else if (result.status.equals(Status.CLIENT_ERROR_LOCKED))
//						locked.add(data);
//					else if (result.status.equals(Status.CLIENT_ERROR_FORBIDDEN))
//						perms.add(data);
//				}
//				else
//					taxaIdsAlreadyExist.add(taxaID);
//			}
//
//			StringBuilder result = new StringBuilder("<results>\n");
//
//			result.append("<alreadyExist>\n");
//			for (String taxaID : taxaIdsAlreadyExist)
//				result.append("<taxaID>" + taxaID + "</taxaID>\n");
//			result.append("</alreadyExist>\n");
//
//			result.append("<success>\n");
//			for (AssessmentData taxaID : successfullySaved)
//				result.append("<taxaID>" + taxaID.getSpeciesID() + "</taxaID>\n");
//			result.append("</success>\n");
//
//			result.append("<locked>\n");
//			for (AssessmentData taxaID : locked)
//				result.append("<taxaID>" + taxaID.getSpeciesID() + "</taxaID>\n");
//			result.append("</locked>\n");
//
//			result.append("<badPermission>\n");
//			for (AssessmentData taxaID : perms)
//				result.append("<taxaID>" + taxaID.getSpeciesID() + "</taxaID>\n");
//			result.append("</badPermission>\n");
//
//			result.append("</results>");
//
//			System.out.println("Returning : \n" + result.toString() );
//			response.setStatus(Status.SUCCESS_CREATED);
//			response.setEntity(result.toString(), MediaType.TEXT_XML);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//
//	}
//
//
//	/**
//	 * Returns the assessment data object to save if there does not already exist a draft assessment for the given region and species
//	 * if the assessment already exists, then returns null
//	 */
//	private AssessmentData getTemplateToSaveIfNotAlreadyExist(final String user, final String taxaID, final AssessmentFilter filter, final boolean useTemplate) {
//		AssessmentFilter draftFilter = filter.deepCopy();
//		draftFilter.setDraft(true);
//		draftFilter.setRecentPublished(false);
//
//		AssessmentFilterHelper helper = new AssessmentFilterHelper(draftFilter);
//		List<AssessmentData> possible = helper.getAssessments(taxaID, vfs);
//		if (possible.size() > 0) {
//			System.out.println("the list of possible assessments is greater than 0");
//			return null;
//		}
//
//		draftFilter.setDraft(false);
//		draftFilter.setRecentPublished(true);
//		AssessmentData template = null;
//		if (useTemplate)
//		{
//			List<AssessmentData> assessments = helper.getAssessments(taxaID, vfs);
//			if (assessments.size() == 0) {
//				draftFilter.getRegions().clear();
//				draftFilter.getRegions().add(Region.GLOBAL_REGION_ID);
//				assessments = helper.getAssessments(taxaID, vfs);
//			}
//			if (assessments.size() != 0) {
//				com.solertium.lwxml.shared.utils.ArrayUtils.quicksort(assessments, new MostRecentlyPublishedComparator());
//				template = assessments.get(0).deepCopy();
//
//			}
//		} 
//		if (template == null) {
//			template = new AssessmentData();
//			template.setSpeciesID(taxaID);
//			template.setSpeciesName(TaxaIO.readNode(taxaID, vfs).getFullName());
//		}
//		template.setRegional(true);
//		template.setEndemic(false);		
//		template.setDateAssessed("");
//		System.out.println("Setting region name as " + filter.getRegionIDsCSV());
//		template.setRegionName(filter.getRegionIDsCSV());
//		template.setType(AssessmentData.DRAFT_ASSESSMENT_STATUS);	
//		return template;
//	}
//
//
//
//
//	private void createAssessmentsIfNotExist(Request request, Response response) {
//
//		String username = request.getChallengeResponse().getIdentifier();
//		StringBuffer idsAdded = new StringBuffer();
//		StringBuffer badids = new StringBuffer();
//		try {
//			String text = request.getEntity().getText();
//			NativeDocument doc = NativeDocumentFactory.newNativeDocument();
//			doc.parse(text);
//
//			NativeNodeList nodes = doc.getDocumentElement().getElementsByTagName("assessment");
//
//			for (int i = 0; i < nodes.getLength(); i++) {
//				NativeElement assessmentEl = nodes.elementAt(i);
//				AssessmentData assessment = new AssessmentParser(assessmentEl).getAssessment();
//				VFSPath uri = new VFSPath(ServerPaths.getPathForGlobalDraftAssessment(assessment.getSpeciesID()));
//
//				if (!vfs.exists(uri)) {
//					boolean success = createGlobalAssessment(assessment, username, assessment.getSpeciesID(),
//							assessment.getSpeciesName());
//
//					if (success) {
//						idsAdded.append(assessment.getSpeciesID() + ",");
//					} else
//						badids.append(assessment.getSpeciesID() + ",");
//				}
//			}
//			response.setEntity(createBatchReturn(badids.toString(), idsAdded.toString()), MediaType.TEXT_XML);
//			if (badids.length() == 0)
//				response.setStatus(Status.SUCCESS_CREATED);
//			else
//				response.setStatus(Status.SUCCESS_PARTIAL_CONTENT);
//
//		} catch (Exception e) {
//			response.setEntity(createBatchReturn(badids.toString(), idsAdded.toString()), MediaType.TEXT_XML);
//			response.setStatus(Status.SERVER_ERROR_INTERNAL);
//		}
//
//	}
//
//	private String createBatchReturn(String badIDs, String goodIDs) {
//		if (badIDs.length() > 0)
//			badIDs = badIDs.substring(0, badIDs.length() - 1);
//		if (goodIDs.length() > 0)
//			goodIDs = goodIDs.substring(0, goodIDs.length() - 1);
//
//		String newDocument = "<status>\r\n " + "<success>" + goodIDs + "</success>\r\n" + "<error>" + badIDs
//		+ "</error>\r\n" + "</status>";
//
//		return newDocument;
//	}
//
//	/**
//	 * @param assessment
//	 * @param username
//	 * @param speciesID
//	 * @param displayName
//	 * @return
//	 */
//	private boolean createGlobalAssessment(AssessmentData assessment, String username, String speciesID,
//			String displayName) {
//		String assessmentUrl = ServerPaths.getPathForGlobalDraftAssessment(speciesID);
//
//		boolean lock = FileLocker.impl.aquireLock(assessmentUrl);
//		if (lock) {
//			writeToGlobalCommitLog(username, displayName, speciesID);
//			saveGlobalAssessment(assessment, username, speciesID, displayName);
//			FileLocker.impl.releaseLock(assessmentUrl);
//		}
//
//		return lock;
//	}
//
//	private boolean createRegionalAssessment(AssessmentData assessment, String username) {
//		String assessmentUrl = ServerPaths
//		.getPathForRegionalDraftAssessmentFromRegionalID(assessment.getAssessmentID());
//		if (!assessmentUrl.equals("") && vfs.exists(new VFSPath(assessmentUrl))) {
//			boolean lock = FileLocker.impl.aquireLock(assessmentUrl);
//
//			if (lock) {
//				boolean ret = saveRegionalAssessment(assessment, username);
//				FileLocker.impl.releaseLock(assessmentUrl);
//				return ret;
//			} else
//				return false;
//		} else {
//			return getAndSaveNextRegionalID(assessment, username).status.isSuccess();
//		}
//	}
//
//	@Override
//	public void definePaths() {
//		paths.add("/drafts/regional/{nodeID}");
//		paths.add("/drafts/global/{nodeID}");
//		paths.add("/drafts/regional/{username}/{regionName}/{isEndemic}");
//		paths.add("/drafts/mightNotExist/{nodeID}/{mightNotExist}");
//		paths.add("/drafts/batch");
//		paths.add("/drafts/batch/{username}");
//		paths.add("/drafts/batch/{username}/{nodeID}");
//		paths.add("/drafts/{username}/{nodeID}/{displayName}");
//	}
//
//	private void deleteAssessment(Request request, Response response, final String assessmentID) {
//		String url = null;
//		try {
//			String path = "";
//			if (isRegional(assessmentID))
//				path = ServerPaths.getPathForRegionalDraftAssessmentFromRegionalID(assessmentID);
//			else
//				path = ServerPaths.getPathForGlobalDraftAssessment(assessmentID);
//			SysDebugger.getInstance().println("regional: " + isRegional(assessmentID));
//			SysDebugger.getInstance().println("Looking for assessment " + assessmentID + " in path " + path);
//			SysDebugger.getInstance().println(path);
//			url = path;
//			VFSPath trashURL = VFSUtils.parseVFSPath(url.replace("/drafts", "/trash/drafts"));
//			if (vfs.exists(trashURL))
//				vfs.delete(trashURL);
//
//			vfs.move(VFSUtils.parseVFSPath(url), trashURL);
//
//			String username = (String) request.getAttributes().get("nodeID");
//			String displayName = (String) request.getAttributes().get("displayName");
//			// String assessmentUrl =
//			// ServerPaths.getPublishedAssessmentURL(assessmentID);
//
//			String log = "<assessment user=\"" + username;
//			if (isRegional(assessmentID))
//				log += "\" status=\"" + "draft_regional";
//			else
//				log += "\" status=\"" + "draft";
//
//			log += "\" date=\"" + new Date().toString() + "\" node=\"" + displayName + "\">" + assessmentID
//			+ "</assessment>";
//
//			trashBuffer.addEvent(DocumentUtils.createDocumentFromString(log));
//			trashBuffer.flush();
//
//			// remove from recent
//			final Request req = new Request(Method.DELETE, "riap://host/recentAssessments/" + username
//					+ "/draft_status/" + assessmentID);
//			Response resp = getContext().getServerDispatcher().handle(req);
//			if (!(resp.getStatus()).isSuccess()) {
//				System.out.println("Unable to delete published assessment from recent");
//			} else {
//				// node.removeAssessment(pubAssessments.get(i));
//				System.out.println("Published assessment deleted from recent.");
//			}
//
//		} catch (Exception e) {
//			SysDebugger.getInstance().println("Could not find assessment " + assessmentID);
//			e.printStackTrace();
//			response.setStatus(Status.SUCCESS_OK);
//		}
//	}
//
//	private void getAllAssessments(Request request, Response response, String nodeID) {
//		String url = "";
//		Boolean mightNotExist = Boolean.valueOf((String) request.getAttributes().get("mightNotExist"));
//
//		String[] nodeIDs = nodeID.split(",");
//		StringBuffer xml = new StringBuffer("<assessments>\r\n");
//
//		for (int i = 0; i < nodeIDs.length; i++) {
//			url = ServerPaths.getPathForGlobalDraftAssessment(nodeIDs[i]);
//
//			try {
//				boolean existed = false;
//				if ((vfs.exists(url))) {
//					String assXML = DocumentUtils.getVFSFileAsString(url, vfs);
//					long lastMod = vfs.getLastModified(new VFSPath(url));
//					assXML = assXML.replaceAll("<\\?xml\\s*(version=.*)?\\s*(encoding=.*)?\\?>", "");
//					assXML = assXML.replaceAll("(<dateModified>.*?</dateModified>)|(<dateModified\\s*/>)",
//							"<dateModified>" + lastMod + "</dateModified>");
//					xml.append(assXML);
//					existed = true;
//				}
//
//				String regionalURL = ServerPaths.getPathForRegionalDraftAssessment(nodeIDs[i]);
//				SysDebugger.getInstance().println("This is the regionalURL " + regionalURL);
//				if (vfs.exists(regionalURL)) {
//					existed = true;
//					SysDebugger.getInstance().println("The regionalURL existed ");
//					String[] otherfiles = null;
//
//					try {
//						otherfiles = vfs.list(regionalURL);
//					} catch (NullPointerException e) {
//						//This just means the regionalURL is empty. 
//					}
//
//					if( otherfiles != null ) {
//						for (int j = 0; j < otherfiles.length; j++) {
//							if (!otherfiles[j].startsWith(".")) {
//								SysDebugger.getInstance().println(
//										"I am putting the document in that is in " + regionalURL + otherfiles[j]);
//								String assXML = DocumentUtils.getVFSFileAsString(regionalURL + "/" + otherfiles[j], vfs);
//								long lastMod = vfs.getLastModified(new VFSPath(regionalURL + "/" + otherfiles[j]));
//								assXML = assXML.replaceAll("<\\?xml\\s*(version=.*)?\\s*(encoding=.*)?\\?>", "");
//								assXML = assXML.replaceAll("(<dateModified>.*?</dateModified>)|(<dateModified\\s*/>)",
//										"<dateModified>" + lastMod + "</dateModified>");
//								xml.append(assXML);
//							}
//						}
//					}
//				}
//
//				if (!existed && !mightNotExist) {
//					SysDebugger.getInstance().println("Could not find assessment " + nodeIDs[i]);
//					response
//					.setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Could not find draft assessment " + nodeIDs[i]);
//					return;
//				}
//
//			} catch (Exception e) {
//				SysDebugger.getInstance().println("Could not find assessment " + nodeIDs[i]);
//				e.printStackTrace();
//				response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Could not find draft assessment " + nodeIDs[i]);
//				return;
//			}
//		}
//		xml.append("</assessments>\r\n");
//
//		Representation rep = new StringRepresentation(xml, MediaType.TEXT_XML);
//		rep.setCharacterSet(CharacterSet.UTF_8);
//		response.setEntity(rep);
//		response.setStatus(Status.SUCCESS_OK);
//
//	}
//
//	private synchronized AssessmentIOWriteResult getAndSaveNextRegionalID(AssessmentData assessment, String username) {
//		int nextID = 0;
//
//		VFSPath rootURI;
//
//		try {
//			rootURI = VFSUtils.parseVFSPath(ServerPaths.getPathForRegionalDraftAssessment(assessment.getSpeciesID()));
//		} catch (VFSPathParseException e) {
//			e.printStackTrace();
//			SysDebugger.getNamedInstance(SISContainerApp.SEVERE_LOG).println(
//					"Path invalid " + "to save regional assessment: "
//					+ ServerPaths.getPathForRegionalDraftAssessment(assessment.getSpeciesID())
//					+ ". Assessment's Species ID is most likely not set.");
//			return null;
//		}
//
//		boolean amIOnline = false;
//
//		try {
//			amIOnline = SISContainerApp.amIOnline().booleanValue();
//		} catch (Exception e) {
//			amIOnline = false;
//		}
//
//		VFSPath saveURI;
//		if (vfs.exists(rootURI)) { // If there's already regionals for this guy,
//			// get the next
//			VFSPathToken[] tokens;
//			try {
//				tokens = vfs.list(rootURI);
//				for (VFSPathToken curToken : tokens) {
//					String filename = curToken.toString();
//					filename = filename.replaceAll(".xml", "");
//					if (!amIOnline)
//						filename = filename.replaceAll("offline", "");
//
//					System.out.println("Crawling file " + curToken + " for new regional ID.");
//
//					try {
//						int value = Integer.valueOf(filename.split("_")[1]);
//						nextID = Math.max(value, nextID);
//					} catch (NumberFormatException e) {
//						SysDebugger.getNamedInstance(SISContainerApp.SEVERE_LOG).println(
//								"Annoying file in path " + curToken + " non-conformant "
//								+ "to standard region assessment file name pattern.");
//					}
//				}
//			} catch (NotFoundException e) {
//				SysDebugger.getNamedInstance(SISContainerApp.SEVERE_LOG).println(
//						"Big WTF. " + "List failed on existing path " + rootURI.toString());
//				return null;
//			}
//
//			nextID++; // Increment it one past the highest found.
//		}
//
//		String assessmentID;
//		if (amIOnline)
//			assessmentID = assessment.getSpeciesID() + "_" + nextID;
//		else
//			assessmentID = assessment.getSpeciesID() + "_" + "offline" + nextID;
//
//		saveURI = rootURI.child(new VFSPathToken(assessmentID + ".xml"));
//
//		assessment.setAssessmentID(assessmentID);
//
//		if (!assessment.getRegionID().matches("(\\d+,?)+")) {
//			String regionName = assessment.getRegionID();
//			if (RegionRestlet.nameToID.containsKey(regionName))
//				assessment.setRegionName(RegionRestlet.nameToID.get(regionName));
//			else {
//				assessment.setRegionName("" + RegionRestlet.addNewRegion(regionName, regionName));
//			}
//		}
//
//		//This should never fail as this is used to create a new assessment.
//		return AssessmentIO.writeAssessment(assessment, username, vfs, true);
//	}
//
//	private void getAssessmentsViaIDs(Request request, Response response, String assessmentIDs) {
//		String[] ids = assessmentIDs.split(",");
//		String xml = "<assessments>\r\n";
//		for (int i = 0; i < ids.length; i++) {
//			VFSPath url;
//			if (isRegional(ids[i])) {
//				url = new VFSPath(ServerPaths.getPathForRegionalDraftAssessmentFromRegionalID(ids[i]));
//			} else {
//				url = new VFSPath(ServerPaths.getPathForGlobalDraftAssessment(ids[i]));
//			}
//			if (vfs.exists(url)) {
//				try {
//					String assXML = DocumentUtils.getVFSFileAsString(url.toString(), vfs);
//					long lastMod = vfs.getLastModified(url);
//					assXML = assXML.replaceAll("<\\?xml\\s*(version=.*)?\\s*(encoding=.*)?\\?>", "");
//					assXML = assXML.replaceAll("(<dateModified>.*?</dateModified>)|(<dateModified\\s*/>)",
//							"<dateModified>" + lastMod + "</dateModified>");
//					xml += assXML;
//				} catch (NotFoundException e) {
//					System.out.println("Unable to find requested assessment, even though "
//							+ "it was reported as extant: " + url.toString());
//				}
//			} else {
//				response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
//			}
//		}
//		xml += "</assessments>";
//		Representation rep = new StringRepresentation(xml, MediaType.TEXT_XML);
//		rep.setCharacterSet(CharacterSet.UTF_8);
//		response.setEntity(rep);
//		response.setStatus(Status.SUCCESS_OK);
//	}
//
//	public String getRegionFromDraftAssessmentID(String assessmentID) {
//		String region = "";
//		if (assessmentID.indexOf("_") > -1) {
//			region = assessmentID.substring(assessmentID.indexOf("_") + 1, assessmentID.length());
//		}
//		return region;
//	}
//
//	public String getTaxaIDFromAssessmentID(String assessmentID) {
//
//		String taxaID = assessmentID;
//		if (assessmentID.indexOf("_") > -1) {
//			taxaID = assessmentID.substring(0, assessmentID.indexOf("_"));
//		}
//		return taxaID;
//	}
//
//	@Override
//	public void performService(Request request, Response response) {
//		String nodeID = (String) request.getAttributes().get("nodeID");
//		String username;
//
//		try {
//			username = request.getChallengeResponse().getIdentifier();
//		} catch (NullPointerException e) {
//			// e.printStackTrace();
//			username = (String) request.getAttributes().get("username");
//		}
//
//		if (username == null || username.equals("")) {
//			response.setStatus(Status.CLIENT_ERROR_FORBIDDEN);
//			System.out.println("Draft Restlet call rejected because username is null.");
//		} else if (request.getMethod().equals(Method.GET)) {
//			if (!nodeID.contains("_"))
//				getAllAssessments(request, response, nodeID);
//			else
//				getAssessmentsViaIDs(request, response, nodeID);
//		} else if (request.getMethod().equals(Method.PUT)) {
//			if (request.getResourceRef().getPath().startsWith("/drafts/batch/")) {
//				putAssessments(request, response, username);
//			} else if (request.getResourceRef().getPath().startsWith("/drafts/regional/")) {
//				try {
//					putRegionalAssessment(request, response, username);
//				} catch (Throwable e) {
//					e.printStackTrace();
//				}
//			} else {
//				String speciesID = (String) request.getAttributes().get("nodeID");
//				String displayName = (String) request.getAttributes().get("displayName");
//				putGlobalAssessment(request, response, username, speciesID, displayName);
//			}
//		} else if (request.getMethod().equals(Method.POST)) {
//
//			if (request.getResourceRef().getPath().startsWith("/drafts/batch")) {
//				String user = (String) request.getAttributes().get("username");
//				if (user == null)
//					createBatchDraftAssessments(request, response);
//				else
//					createAssessmentsIfNotExist(request, response);
//			} else if (request.getResourceRef().getPath().startsWith("/drafts/regional/")) {
//				postRegionalAssessment(request, response, username);
//			} else {
//				String speciesID = (String) request.getAttributes().get("nodeID");
//				String speciesName = (String) request.getAttributes().get("displayName");
//				postGlobalAssessment(request, response, username, speciesID, speciesName);
//			}
//		} else if (request.getMethod().equals(Method.DELETE)) {
//			username = (String) request.getAttributes().get("username");
//			deleteAssessment(request, response, username);
//		} else {
//			response.setStatus(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
//		}
//	}
//
//	private void postGlobalAssessment(Request request, Response response, String username, String speciesID,
//			String speciesName) {
//
//		try {
//			NativeDocument ndoc = NativeDocumentFactory.newNativeDocument();
//			String payload = request.getEntity().getText();
//			ndoc.parse(payload);
//			AssessmentData assessment = new AssessmentParser(ndoc).getAssessment();
//
//			AssessmentIOWriteResult result = AssessmentIO.writeAssessment(assessment, username, vfs, true);
//			if (result.status.isSuccess()) {
//				response.setEntity(result.newLastModified + "", MediaType.TEXT_PLAIN);
//				response.setStatus(result.status);
//			} else {
//				response.setStatus(result.status);
//			}
//
//		} catch (Exception e) {
//			e.printStackTrace();
//			response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
//		}
//	}
//
//	private void postRegionalAssessment(Request request, Response response, String username) {
//		try {
//			// Document doc = request.getEntityAsDom().getDocument();
//			// Element docElement = doc.getDocumentElement();
//			// Element basicInfo = (Element)
//			// docElement.getElementsByTagName("basicInformation").item(0);
//			// String speciesID =
//			// basicInfo.getElementsByTagName("speciesID").item(0).getTextContent();
//			// String speciesName =
//			// basicInfo.getElementsByTagName("speciesName").item(0).getTextContent();
//
//			NativeDocument doc = NativeDocumentFactory.newNativeDocument();
//			doc.parse(request.getEntity().getText());
//			AssessmentParser parser = new AssessmentParser(doc);
//			AssessmentData assessment = parser.getAssessment();
//
//			VFSPath assessmentUrl = new VFSPath(ServerPaths.getPathForRegionalDraftAssessmentFromRegionalID(assessment
//					.getAssessmentID()));
//
//			if (vfs.exists(assessmentUrl)) {
//				Status status = FileLocker.impl.lockAssessment(assessment.getAssessmentID(),
//						BaseAssessment.DRAFT_ASSESSMENT_STATUS, FileLocker.SAVE_LOCK, username);
//
//				if (status.isSuccess()) {
//					if (saveRegionalAssessment(assessment, username)) {
//						response.setEntity(vfs.getLastModified(assessmentUrl) + "", MediaType.TEXT_PLAIN);
//						response.setStatus(status);
//					} else {
//						response.setStatus(Status.CLIENT_ERROR_CONFLICT);
//					}
//				} else {
//					response.setStatus(status);
//				}
//			}
//		} catch (Exception e) {
//			response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
//		}
//
//	}
//
//	private void putAssessments(Request request, Response response, String username) {
//		NativeDocument doc = NativeDocumentFactory.newNativeDocument();
//		StringBuffer successfulIDs = new StringBuffer();
//		StringBuffer unsuccessfulIDs = new StringBuffer();
//
//		try {
//			String text = request.getEntity().getText();
//			doc.parse(text);
//			NativeNodeList nodes = doc.getDocumentElement().getElementsByTagName("assessment");
//
//			for (int i = 0; i < nodes.getLength(); i++) {
//				NativeElement assessmentEl = nodes.elementAt(i);
//				AssessmentData curAss = new AssessmentParser(assessmentEl).getAssessment();
//
//				boolean success = createGlobalAssessment(curAss, username, curAss.getSpeciesID(), curAss
//						.getSpeciesName());
//				if (success)
//					successfulIDs.append(curAss.getSpeciesID() + ",");
//				else
//					unsuccessfulIDs.append(curAss.getSpeciesID() + ",");
//			}
//
//			response.setEntity(createBatchReturn(unsuccessfulIDs.toString(), successfulIDs.toString()),
//					MediaType.TEXT_XML);
//			if (unsuccessfulIDs.length() == 0)
//				response.setStatus(Status.SUCCESS_CREATED);
//			else
//				response.setStatus(Status.SUCCESS_PARTIAL_CONTENT);
//		} catch (IOException e) {
//			e.printStackTrace();
//			response.setEntity(createBatchReturn(unsuccessfulIDs.toString(), successfulIDs.toString()),
//					MediaType.TEXT_XML);
//			response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
//		} catch (NullPointerException e) {
//			e.printStackTrace();
//			response.setEntity(createBatchReturn(unsuccessfulIDs.toString(), successfulIDs.toString()),
//					MediaType.TEXT_XML);
//			if (successfulIDs.length() > 0)
//				response.setStatus(Status.SUCCESS_PARTIAL_CONTENT);
//			else
//				response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
//		}
//
//	}
//
//	private void putGlobalAssessment(Request request, Response response, String username, String speciesID,
//			String displayName) {
//
//		Representation assessRep = request.getEntity();
//		try {
//			String assessText = assessRep.getText();
//			NativeDocument ndoc = NativeDocumentFactory.newNativeDocument();
//			ndoc.parse(assessText);
//			AssessmentData assessment = new AssessmentParser(ndoc).getAssessment();
//
//			boolean success = createGlobalAssessment(assessment, username, speciesID, displayName);
//			if (success) {
//				response.setEntity(vfs.getLastModified(new VFSPath(ServerPaths
//						.getPathForGlobalDraftAssessment(speciesID)))
//						+ "", MediaType.TEXT_PLAIN);
//				response.setStatus(Status.SUCCESS_OK);
//			} else {
//				response.setStatus(Status.SERVER_ERROR_INTERNAL);
//			}
//		} catch (Exception e) {
//			response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
//		}
//
//	}
//
//	private void putRegionalAssessment(Request request, Response response, String username) {
//		try {
//			NativeDocument doc = NativeDocumentFactory.newNativeDocument();
//			doc.parse(request.getEntity().getText());
//			AssessmentParser parser = new AssessmentParser(doc);
//			AssessmentData assessment = parser.getAssessment();
//
//			//Check to see if one exists for this exact regionality...
//			List<AssessmentData> others = AssessmentIO.readRegionalDraftAssessmentsForTaxon(vfs, assessment.getSpeciesID());
//			if( others.size() > 0 ) {
//				for( AssessmentData cur : others ) {
//					if( cur.getRegionID().equals(assessment.getRegionID()) && cur.isEndemic() == assessment.isEndemic() ) {
//						assessment.setAssessmentID(cur.getAssessmentID());
//						break; //No need to keep checking.
//					}
//				}
//			}
//
//			boolean success = createRegionalAssessment(assessment, username);
//
//			if (success) {
//				response.setEntity(assessment.getAssessmentID() + "", MediaType.TEXT_PLAIN);
//				response.setStatus(Status.SUCCESS_OK);
//			} else {
//				response.setStatus(Status.CLIENT_ERROR_FORBIDDEN);
//			}
//		} catch (IOException e) {
//			response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
//		} catch (NullPointerException e) {
//			response.setStatus(Status.CLIENT_ERROR_EXPECTATION_FAILED);
//		}
//
//	}
//
//	private boolean saveGlobalAssessment(AssessmentData assessment, String username, String speciesID,
//			String displayName) {
//		String assessmentUrl = ServerPaths.getPathForGlobalDraftAssessment(speciesID);
//		// String logUrl = determineGlobalAssessmentLogUrl( );
//
//		try {
//			writeToGlobalCommitLog(username, displayName, speciesID);
//			// DocumentUtils.writeVFSFile(assessmentUrl, vfs, assessment);
//			AssessmentIO.writeAssessment(assessment, username, vfs, true);
//			VFSMetadata meta = vfs.getMetadata(new VFSPath(assessmentUrl));
//			meta.addArbitraryData("owner", username);
//			vfs.setMetadata(new VFSPath(assessmentUrl), meta);
//
//			return true;
//		} catch (ConflictException e) {
//			e.printStackTrace();
//			return false;
//		}
//	}
//
//	private boolean saveRegionalAssessment(AssessmentData assessment, String username) {
//		// String logUrl = determineRegionalAssessmentLogUrl();
//		String assessmentUrl = ServerPaths
//		.getPathForRegionalDraftAssessmentFromRegionalID(assessment.getAssessmentID());
//
//		try {
//			writeToRegionalCommitLog(username, assessment.getSpeciesName(), assessment.getSpeciesID(), "N/A");
//			// DocumentUtils.writeVFSFile(assessmentUrl, vfs,
//			// assessment.toXML());
//			AssessmentIO.writeAssessment(assessment, username, vfs, true);
//			VFSMetadata meta = vfs.getMetadata(new VFSPath(assessmentUrl));
//			meta.addArbitraryData("owner", username);
//			vfs.setMetadata(new VFSPath(assessmentUrl), meta);
//
//			return true;
//		} catch (ConflictException e) {
//			return false;
//		}
//	}
//
//	private synchronized void writeToGlobalCommitLog(String username, String speciesName, String speciesID) {
//
//		if (SISContainerApp.commitLogging) {
//			String logUrl = ServerPaths.getGlobalDraftAssessmentLogUrl();
//			String log = "<assessment user=\"" + username + "\" status=\"" + "draft" + "\" date=\""
//			+ new Date().toString() + "\" name=\"" + speciesName.replace("%20", " ") + "\">" + speciesID
//			+ "</assessment>";
//
//			// NEW LOGGING CODE
//			// *************************
//			buffer.addEvent(DocumentUtils.createDocumentFromString(log));
//			// **************************
//
//			/*
//			 * String fullLog;
//			 * 
//			 * DocumentUtils.unversion(logUrl,vfs);
//			 * 
//			 * if( !vfs.exists( logUrl ) ) fullLog =
//			 * "<assessments>\r\n</assessments>"; else fullLog =
//			 * DocumentUtils.getVFSFileAsString( logUrl, vfs ); fullLog =
//			 * fullLog.replace("</assessments>", log + "\r\n</assessments>");
//			 * 
//			 * try { Writer logWriter = vfs.getWriter( logUrl );
//			 * logWriter.write( fullLog ); logWriter.close(); } catch (Exception
//			 * e){ e.printStackTrace(); }
//			 */
//		}
//	}
//
//	private synchronized void writeToRegionalCommitLog(String username, String speciesName, String speciesID,
//			String regionName) {
//		if (SISContainerApp.commitLogging) {
//			String logUrl = ServerPaths.getRegionalDraftAssessmentLogUrl();
//			String log = "<assessment user=\"" + username + "\" status=\"" + "draft" + "\" date=\""
//			+ new Date().toString() + "\" name=\"" + speciesName + "\" region=\"" + regionName + "\">"
//			+ speciesID + "</assessment>";
//
//			// NEW LOGGING CODE
//			// *************************
//			buffer.addEvent(DocumentUtils.createDocumentFromString(log));
//			// **************************
//			/*
//			 * 
//			 * String fullLog;
//			 * 
//			 * DocumentUtils.unversion(logUrl,vfs);
//			 * 
//			 * if( !vfs.exists( logUrl ) ) fullLog =
//			 * "<assessments>\r\n</assessments>"; else fullLog =
//			 * DocumentUtils.getVFSFileAsString( logUrl, vfs );
//			 * 
//			 * fullLog = fullLog.replace("</assessments>", log +
//			 * "\r\n</assessments>");
//			 * 
//			 * try { Writer logWriter = vfs.getWriter( logUrl );
//			 * logWriter.write( fullLog ); logWriter.close(); } catch (Exception
//			 * e){ }
//			 */
//		}
//	}
//
//	// public static Document createDomDocument() {
//	// try {
//	// DocumentBuilder builder =
//	// DocumentBuilderFactory.newInstance().newDocumentBuilder();
//	// Document doc = builder.newDocument();
//	// return doc;
//	// } catch (ParserConfigurationException e) {
//	// }
//	// return null;
//	// }
//}
