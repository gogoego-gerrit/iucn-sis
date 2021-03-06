package org.iucn.sis.server.simple;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.iucn.sis.server.baserestlets.ServiceRestlet;
import org.iucn.sis.server.filters.AssessmentFilterHelper;
import org.iucn.sis.server.io.AssessmentIO;
import org.iucn.sis.server.io.WorkingSetIO;
import org.iucn.sis.server.locking.FileLocker;
import org.iucn.sis.server.locking.LockType;
import org.iucn.sis.server.locking.LockRepository.Lock;
import org.iucn.sis.server.utils.DocumentUtils;
import org.iucn.sis.server.utils.FileZipper;
import org.iucn.sis.server.utils.ServerPaths;
import org.iucn.sis.server.utils.XMLUtils;
import org.iucn.sis.shared.BaseAssessment;
import org.iucn.sis.shared.data.WorkingSetData;
import org.iucn.sis.shared.data.WorkingSetParser;
import org.iucn.sis.shared.data.assessments.AssessmentData;
import org.iucn.sis.shared.data.assessments.AssessmentFilter;
import org.iucn.sis.shared.data.assessments.AssessmentParser;
import org.iucn.sis.shared.taxonomyTree.SynonymData;
import org.iucn.sis.shared.taxonomyTree.TaxonNode;
import org.iucn.sis.shared.taxonomyTree.TaxonNodeFactory;
import org.restlet.Context;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.fileupload.RestletFileUpload;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.StringRepresentation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.solertium.lwxml.factory.NativeDocumentFactory;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.util.SysDebugger;
import com.solertium.vfs.VFSPath;

public class WorkingSetExportImportRestlet extends ServiceRestlet {

	public WorkingSetExportImportRestlet(String vfsroot, Context context) {
		super(vfsroot, context);
	}

	private Document changePrivateToPublic(Document workingSetDocument) {
		Element modeElement = ((Element) ((Element) workingSetDocument.getDocumentElement()
				.getElementsByTagName("info").item(0)).getElementsByTagName("mode").item(0));
		if (modeElement.getTextContent().equalsIgnoreCase("private")) {
			modeElement.setTextContent("public");
		}

		return workingSetDocument;

	}

	/**
	 * If the new document has published assessments listed, then grab them. If
	 * the existing document has published assessments listed that weren't in
	 * the online version, we can just clobber them since no one is supposed to
	 * be making published assessments in the offline version.
	 * 
	 * @param document
	 * @param existingDocument
	 * @param mergedDocument
	 * @return the merged document
	 */
	private Document checkAssessments(final Document document, final Document existingDocument, Document mergedDocument) {

		try {
			if (!SISContainerApp.amIOnline) {
				NodeList assessmentsNodes = document.getElementsByTagName("assessments");
				if (assessmentsNodes != null && assessmentsNodes.getLength() > 0) {
					Node assessments = assessmentsNodes.item(0);

					if (mergedDocument == null)
						mergedDocument = existingDocument;

					Element el = mergedDocument.createElement("assessments");
					el.setTextContent(assessments.getTextContent());
					mergedDocument.getDocumentElement().appendChild(el);
					return mergedDocument;
				} else {
					System.out.println("Assessments <= 0...");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return mergedDocument;
	}

	/**
	 * Given the document that you want to import, and the existing taxa
	 * document, determines if you need to add common names to the existing taxa
	 * document. Returns null if the existing taxa document matches the
	 * document, returns a new document added common names.
	 * 
	 * @param document
	 * @param existingTaxaDocument
	 * @return
	 */
	private Document checkCommonNames(final Document document, final Document existingTaxaDocument) {

		Element commonNames = null;
		Element existingCommonNames = null;
		NodeList existingCommonNamesList = null;
		NodeList commonNamesList = null;
		Document returnDocument = null;

		// IF THERE IS NOTHING IN THE IMPORTED DOCUMENT, DOSEN'T NEED TO CHANGE
		// ANYTHING
		if (document.getDocumentElement().getElementsByTagName("commonNames") != null
				&& document.getDocumentElement().getElementsByTagName("commonNames").getLength() == 1) {

			commonNames = (Element) document.getDocumentElement().getElementsByTagName("commonNames").item(0);
			commonNamesList = commonNames.getElementsByTagName("commonName");

			// GET COMMON NAMES IN THE EXISTING DOCUMENT
			if (existingTaxaDocument.getDocumentElement().getElementsByTagName("commonNames") != null
					&& existingTaxaDocument.getDocumentElement().getElementsByTagName("commonNames").getLength() == 1) {

				existingCommonNames = (Element) existingTaxaDocument.getDocumentElement().getElementsByTagName(
						"commonNames").item(0);
				existingCommonNamesList = existingCommonNames.getElementsByTagName("commonName");

				// CHECK TO MAKE SURE THAT THE COMMON NAMES ARE IDENTICAL ...
				// IF NOT, NEED TO MERGE THEM
				for (int i = 0; i < commonNamesList.getLength(); i++) {

					Element element = (Element) commonNamesList.item(i);
					String name = element.getAttribute("name");
					// String notes = element.getAttribute("notes");
					// String authority = element.getAttribute("authority");
					// String deprecated = element.getAttribute("deprecated");
					String validated = element.getAttribute("validated");
					String iso = element.getAttribute("iso");
					String language = element.getAttribute("language");
					String primary = element.getAttribute("primary");

					if (iso != null && iso.length() >= 3) {
						String temp = language;
						language = iso;
						iso = temp;
					}

					// SysDebugger.getInstance().println(" new " + name + " " +
					// validated + " " + iso + " " + languague);

					boolean found = false;

					for (int j = 0; j < existingCommonNamesList.getLength() && !found; j++) {
						Element existingElement = (Element) existingCommonNamesList.item(j);
						String existingName = existingElement.getAttribute("name");
						String existingValidated = existingElement.getAttribute("validated");
						String existingiso = existingElement.getAttribute("iso");
						String existinglanguage = existingElement.getAttribute("language");
						String existingPrimary = existingElement.getAttribute("primary");

						if (existingiso != null && existingiso.length() >= 3) {
							String temp = existinglanguage;
							existinglanguage = existingiso;
							existingiso = temp;
						}

						// SysDebugger.getInstance().println(" old " +
						// existingName + " " + existingValidated + " " +
						// existingiso + " " + existinglanguage);
						// IF ALL ARE THE SAME THEN WE HAVE FOUND THEM, IF NOT,
						// NEED TO ADD NEW
						// if (name.equalsIgnoreCase(existingName) &&
						// notes.equalsIgnoreCase(existingNotes) &&
						// authority.equalsIgnoreCase(existingAuthority) &&
						// deprecated.equalsIgnoreCase(existingDeprecated)){
						// found = true;
						// }
						if (name.trim().equalsIgnoreCase(existingName)
								&& validated.trim().equalsIgnoreCase(existingValidated.trim())
								&& iso.trim().equalsIgnoreCase(existingiso.trim())
								&& language.trim().equalsIgnoreCase(existinglanguage.trim())
								&& primary.trim().equalsIgnoreCase(existingPrimary.trim()))
							found = true;

					}

					// IF WE DIDN'T FIND THAT COMMON NAME, NEED TO ADD IT TO THE
					// DOCUMENT
					if (!found) {
						SysDebugger.getInstance().println("DIDN'T FIND IT!");
						if (returnDocument == null) {
							returnDocument = existingTaxaDocument;
						}
						element = (Element) returnDocument.importNode(element, true);
						Element mergedCommonNames = (Element) returnDocument.getDocumentElement().getElementsByTagName(
								"commonNames").item(0);
						mergedCommonNames.appendChild(element);
					}
				}

			}

			// IF NO EXISTING COMMON NAMES IN EXISTING DOCUMENT, NEED TO ADD ALL
			// COMMON NAMES
			// FROM NEW DOCUMENT
			else {
				returnDocument = existingTaxaDocument;
				commonNames = (Element) returnDocument.importNode(commonNames, true);
				returnDocument.getDocumentElement().appendChild(commonNames);
			}
		}
		return returnDocument;
	}

	/**
	 * Given a document, the existing document, and a document that has the
	 * common names merged between (null) if the common names where the same,
	 * compares synonyms in the document to the one in the existingDocument. if
	 * the document has a synonym that doesn't exist, it adds it to the
	 * mergedDocument. If the mergedDocument is null, makes the mergedDocument
	 * the existingDocument.
	 * 
	 * @param document
	 * @param existingDocument
	 * @return
	 */
	private Document checkSynonyms(final Document document, final Document existingDocument, Document mergedDocument) {
		Element synonyms = null;
		Element existingSynonyms = null;
		NodeList existingSynonymsList = null;
		NodeList synonymsList = null;

		// IF THERE IS NOTHING IN THE NEW DOCUMENT, DON'T NEED TO ADD ANYTHING
		if (document.getElementsByTagName("synonyms") != null
				&& document.getElementsByTagName("synonyms").getLength() == 1) {

			synonyms = (Element) document.getElementsByTagName("synonyms").item(0);
			synonymsList = synonyms.getElementsByTagName("synonym");

			// GET Synonyms IN THE EXISTING DOCUMENT
			if (existingDocument.getElementsByTagName("synonyms") != null
					&& existingDocument.getElementsByTagName("synonyms").getLength() == 1) {

				existingSynonyms = (Element) existingDocument.getElementsByTagName("synonyms").item(0);
				existingSynonymsList = existingSynonyms.getElementsByTagName("synonym");

				TaxonNode existingTaxon = TaxonNodeFactory.createNode(DocumentUtils
						.serializeNodeToString(existingDocument.getDocumentElement()), null, false);
				TaxonNode importedTaxon = TaxonNodeFactory.createNode(DocumentUtils.serializeNodeToString(document
						.getDocumentElement()), null, false);

				for (SynonymData curImportedSynonym : importedTaxon.getSynonyms()) {
					boolean found = false;

					for (SynonymData curExistingSynonym : existingTaxon.getSynonyms()) {
						if (curImportedSynonym.equals(curExistingSynonym))
							found = true;
					}

					// IF WE DIDN'T FIND THAT SYNONYM, NEED TO ADD IT TO THE
					// DOCUMENT
					if (!found) {
						if (mergedDocument == null) {
							mergedDocument = existingDocument;
						}
						String synXML = curImportedSynonym.toXML();
						Document newSyn = DocumentUtils.createDocumentFromString(synXML);
						Element element = (Element) mergedDocument.importNode(newSyn.getDocumentElement(), true);
						Element mergedSynonyms = (Element) mergedDocument.getDocumentElement().getElementsByTagName(
								"synonyms").item(0);
						mergedSynonyms.appendChild(element);
					}
				}
			}
			// IF NO EXISTING SYNONYMS IN EXISTING DOCUMENT, NEED TO ADD ALL
			// COMMON NAMES
			// FROM NEW DOCUMENT
			else {
				mergedDocument = existingDocument;
				synonyms = (Element) mergedDocument.importNode(synonyms, true);
				mergedDocument.getDocumentElement().appendChild(synonyms);
			}
		}

		return mergedDocument;
	}

	@Override
	public void definePaths() {
		paths.add("/workingSetExporter/private/{username}/{workingsetID}");
		paths.add("/workingSetImporter/{username}");
		paths.add("/workingSetExporter/public/{username}/{workingsetID}");
	}

	private void export(final String publicOrPrivate, final String username, final String workingsetID,
			boolean lockParam, final Response response, final Request request) throws IOException {

		final StringBuilder lockLog = new StringBuilder();
		final String workingSetXML;
		final HashMap<String, String> locked = new HashMap<String, String>(); 

		if (publicOrPrivate.equalsIgnoreCase("public"))
			workingSetXML = WorkingSetIO.readPublicWorkingSetAsString(vfs, workingsetID);
		else
			workingSetXML = WorkingSetIO.readPrivateWorkingSetAsString(vfs, workingsetID, username);

		NativeDocument ndoc = NativeDocumentFactory.newNativeDocument();
		ndoc.parse(workingSetXML);

		WorkingSetParser parser = new WorkingSetParser();
		WorkingSetData ws = null;

		ws = parser.parseSingleWorkingSet(ndoc.getDocumentElement());

		if (ws == null) {
			System.out.println("ERROR: Error parsing working set: " + workingSetXML);
			response.setStatus(Status.SERVER_ERROR_INTERNAL);
			response.setEntity("Error parsing working set: " + XMLUtils.clean(workingSetXML), MediaType.TEXT_PLAIN);
			return;
		}

		AssessmentFilter filter = ws.getFilter();
		AssessmentFilterHelper helper = new AssessmentFilterHelper(filter);
		
		try {
			// MAKE TEMPORARY FILE REPRESENTING SINGLE WORKINGSET
			String workingSetName = ws.getWorkingSetName();
			workingSetName = workingSetName.replaceAll("\\s", "");

			DocumentUtils.writeVFSFile(getWorkingSetTempPath(workingSetName, username), vfs, true, ws.toXML());

			ArrayList<String> filenames = new ArrayList<String>();
			filenames.add(getWorkingSetTempPath(workingSetName, username));

			for (String taxaID : ws.getSpeciesIDs()) {
				TaxonNode curNode = TaxonNodeFactory.createNode(DocumentUtils.getVFSFileAsString(ServerPaths
						.getURLForTaxa(taxaID), vfs), null, false);
				if (curNode.getAssessments().size() > 0) {
					for (String curAssessmentID : curNode.getAssessments())
						filenames.add(ServerPaths.getPublishedAssessmentURL(curAssessmentID));
				}

				List<AssessmentData> drafts = AssessmentIO.readAllDraftAssessments(vfs, curNode.getId()+"");
				for( AssessmentData data : drafts ) {
					if( helper.allowAssessment(data) ) {
						filenames.add(ServerPaths.getDraftAssessmentURL(data.getAssessmentID()));
						if( SISContainerApp.amIOnline && lockParam && assessmentShouldBeLocked(filter.getRegions(), data.getRegionIDs())) {
							doLockThings(username, lockLog, ws, data.getAssessmentID(), curNode, locked);
						}
					}
				}

				if (vfs.exists(new VFSPath(ServerPaths.getURLForTaxa(taxaID)))) {
					Request getFootprintRequest = new Request(Method.GET, "riap://host/browse/footprintIDs/" + taxaID);
					getFootprintRequest.setChallengeResponse(request.getChallengeResponse());
					Response getFootprintResponse = getContext().getClientDispatcher().handle(getFootprintRequest);

					if (getFootprintResponse.getStatus().isSuccess()) {
						Document fullFootprint = new DomRepresentation(getFootprintResponse.getEntity()).getDocument();
						String[] ids = fullFootprint.getDocumentElement().getTextContent().split(",");
						for (String id : ids) {
							if (!filenames.contains(ServerPaths.getURLForTaxa(id))) {
								filenames.add(ServerPaths.getURLForTaxa(id));
							}
						}
					} else {
						System.out.println("FAILURE FETCHING PARENT IDS FOR TAXA " + taxaID);
						response.setStatus(getFootprintResponse.getStatus());
						response.setEntity("Failure fetching parent ids for taxa " + taxaID, MediaType.TEXT_PLAIN);
						return;
					}
				}

			}

			// CREATING FOLDER IF DOESN'T EXIST
			if (!vfs.exists(new VFSPath(getZippedFolder(username)))) {
				vfs.makeCollections(new VFSPath(getZippedFolder(username)));
			}

			// MAKE A FILE TO REWRITE ...
			if (!vfs.exists(new VFSPath(getZippedPath(workingSetName, username)))) {
				Writer writer = vfs.getWriter(new VFSPath(getZippedPath(workingsetID, username)));
				writer.close();
			}

			// ZIPPING IT!
			for( String cur : filenames )
				if( !vfs.exists(new VFSPath(cur)) )
					System.out.println("Will not be able to export file " + cur + " because it does not exist.");

			FileZipper.zipper(vfs, filenames.toArray(new String[filenames.size()]), getZippedPath(workingsetID,
					username));

			// REMOVE TEMPFILE
			vfs.delete(new VFSPath(getWorkingSetTempPath(workingSetName, username)));

			String exportPath = request.getResourceRef().getHostIdentifier() + "/raw"
				+ getZippedPath(workingsetID, username);
			String entity = exportPath + "\r\n";
			entity += "<div><b>" + locked.size() + "</b> assessments successfully locked.<br/> Please" +
					" note they will be locked until you re-import the working set that " +
					" contains these assessments.</div><br/>";
			if( lockLog.length() > 0 )
				entity += "<div><b><u>Some Assessments Could Not Be Locked!</u></b></div><br/>"
					+ "<div>This means your import will NOT be able to update the online" +
						" version of the following assessments. You may re-export this working set" +
						" once the locks are returned to obtain them for yourself.<br/><br/>" +
						" The follow assessments were not locked:</div>" + lockLog.toString();
			
			response.setEntity(entity, MediaType.TEXT_ALL);
			response.setStatus(Status.SUCCESS_CREATED);

		} catch (Exception e) {
			e.printStackTrace();
			response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
		}
		
		if( !response.getStatus().isSuccess() )
			//Release partially acquired locks...
			for( Entry<String, String> curLocked : locked.entrySet() )
				FileLocker.impl.persistentEagerRelease(curLocked.getKey(), BaseAssessment.DRAFT_ASSESSMENT_STATUS, curLocked.getValue());
	}

	private boolean assessmentShouldBeLocked(List<String> lockParam, List<String> region) {
		return lockParam.containsAll(region);
	}

	private void doLockThings(final String username, final StringBuilder lockLog, WorkingSetData ws,
			String assessmentID, TaxonNode curNode, Map<String, String> locked) {
		if( SISContainerApp.amIOnline ) {
			Status ret = FileLocker.impl.persistentLockAssessment(assessmentID, BaseAssessment.DRAFT_ASSESSMENT_STATUS,
					LockType.CHECKED_OUT, username, ws.getId());

			if( !ret.isSuccess() ) {
				Lock lock = FileLocker.impl.getAssessmentPersistentLock(assessmentID, 
						BaseAssessment.DRAFT_ASSESSMENT_STATUS);
				if (lock.getLockType().equals(LockType.CHECKED_OUT) ) {
					lockLog.append("<div>Global draft assessment for " + curNode.getFullName() + 
							" is already checked out by " + lock.getUsername());
				} else {
					lockLog.append("<div>Global draft assessment for " + curNode.getFullName() + 
							" is locked for saving by " + lock.getUsername()+ ". Save" +
							" locks are granted for a 2 minute period after each save." +
							" Please try again later.");
				}
			} else
				locked.put(assessmentID, username);
		}
	}

	public String getSpeciesID(Document draftDoc) {
		return ((Element) draftDoc.getDocumentElement().getElementsByTagName("basicInformation").item(0))
				.getElementsByTagName("speciesID").item(0).getTextContent();
	}

	private String getImportFUllPath(String username, String entryName) {
		return getImportPath(username) + "/" + entryName;
	}

	private String getImportPath(String username) {
		return getUserPath(username) + "/imports";
	}

	private String getImportURL(String username) {
		return getImportPath(username) + "/lastImported.zip";
	}

	/**
	 * Given a taxon document, returns the kingdom and full name as comma
	 * separated values.
	 * 
	 * @param document
	 * @return kingdom,fullName
	 */
	public String getKingdomFullName(Document document) {
		String kingdom = null;
		String fullName = null;
		String footprint = null;
		String[] fullFootprint = null;
		String level = null;
		String infraType = null;

		try {
			Element node = document.getDocumentElement();
			level = node.getAttribute("level");
			fullName = node.getAttribute("name");
			infraType = node.getAttribute("infrarankType");
			if (node.getElementsByTagName("footprint") != null
					&& node.getElementsByTagName("footprint").getLength() == 1) {
				footprint = node.getElementsByTagName("footprint").item(0).getTextContent();
				fullFootprint = footprint.split(",");
			}

			if (level.equalsIgnoreCase(BrowseTaxonomyRestlet.KINGDOM)) {
				kingdom = fullName;
			} else {
				kingdom = fullFootprint[BrowseTaxonomyRestlet.KINGDOM_INT];
			}

			if (level.equalsIgnoreCase(BrowseTaxonomyRestlet.SPECIES)) {
				fullName = fullFootprint[BrowseTaxonomyRestlet.GENUS_INT] + fullName;
			} else if (level.equalsIgnoreCase(BrowseTaxonomyRestlet.INFRARANK)) {
				if (infraType.trim().equals("0"))
					fullName = fullFootprint[BrowseTaxonomyRestlet.GENUS_INT]
							+ fullFootprint[BrowseTaxonomyRestlet.SPECIES_INT] + " ssp. " + fullName;
				else if (infraType.trim().equals("1"))
					fullName = fullFootprint[BrowseTaxonomyRestlet.GENUS_INT]
							+ fullFootprint[BrowseTaxonomyRestlet.SPECIES_INT] + " var. " + fullName;
				else {
					fullName = fullFootprint[BrowseTaxonomyRestlet.GENUS_INT]
							+ fullFootprint[BrowseTaxonomyRestlet.SPECIES_INT] + fullName;
				}
			} else if (level.equalsIgnoreCase(BrowseTaxonomyRestlet.INFRARANK)
					|| level.equalsIgnoreCase(BrowseTaxonomyRestlet.SUBPOPULATION)) {
				fullName = fullFootprint[BrowseTaxonomyRestlet.GENUS_INT]
						+ fullFootprint[BrowseTaxonomyRestlet.SPECIES_INT] + fullName;
			} else if (level.equalsIgnoreCase(BrowseTaxonomyRestlet.INFRARANK_SUBPOPULATION)) {
				fullName = fullFootprint[BrowseTaxonomyRestlet.GENUS_INT]
						+ fullFootprint[BrowseTaxonomyRestlet.SPECIES_INT]
						+ fullFootprint[BrowseTaxonomyRestlet.INFRARANK_INT] + fullName;
			}

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		return kingdom + "," + fullName.replaceAll("\\s", "");
	}

	private String getUserPath(String username) {
		return "/users/" + username;
	}

	private String getWorkingSetTempPath(String workingSetName, String userName) {
		return getUserPath(userName) + "/" + workingSetName + "Temp.xml";
	}

	private String getZippedFolder(String username) {
		return (getUserPath(username) + "/export");
	}

	private String getZippedPath(String workingSetName, String username) {
		return (getZippedFolder(username) + "/" + workingSetName + ".zip");
	}

	private boolean importDraftAssessment(AssessmentData assessment, String username, Request request) {
		try {
			// TODO: RIGHT NOW IT JUST REWRITES OVER DRAFT ASSESSMENTS ... NOT
			// NECESSARILY WANTED BEHAVIOR
			
			String assessmentID = assessment.getAssessmentID();
			String speciesName = assessment.getSpeciesName();
			String url = "riap://host/assessments";

			Method method = Method.PUT;

			Request newRequest = new Request(method, url, new StringRepresentation(assessment.toXML(), MediaType.TEXT_XML));
			if( request.getChallengeResponse() != null ) 
				newRequest.setChallengeResponse(new ChallengeResponse(request.getChallengeResponse().getScheme(), request
					.getChallengeResponse().getIdentifier(), request.getChallengeResponse().getSecret()));
			else
				newRequest.setChallengeResponse(new ChallengeResponse(ChallengeScheme.HTTP_BASIC, username, ""));
			
			Response newResponse = getContext().getClientDispatcher().handle(newRequest);

			if (newResponse.getStatus().isSuccess())
				return true;
			else {
				System.out.println("Unsuccessfully PUT draft assessment " + assessmentID);
				System.out.println(newResponse.getStatus().getCode());
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	private HashMap<String, String> importNodes(ArrayList<String> taxaFiles) {
		HashMap<String, String> newIds = new HashMap<String, String>();

		// BREAK INTO KINGDOM, PHYLUM, CLASS ,ORDER , FAMILY ,GENUS ,
		// SPECIES , INFRARANK , SUBPOPULATION , INFRARANK_SUBPOPULATION

		ArrayList<Document> kingdomList = new ArrayList<Document>();
		ArrayList<Document> phylumList = new ArrayList<Document>();
		ArrayList<Document> classList = new ArrayList<Document>();
		ArrayList<Document> orderList = new ArrayList<Document>();
		ArrayList<Document> familyList = new ArrayList<Document>();
		ArrayList<Document> genusList = new ArrayList<Document>();
		ArrayList<Document> speciesList = new ArrayList<Document>();
		ArrayList<Document> infrarankList = new ArrayList<Document>();
		ArrayList<Document> subpopulationList = new ArrayList<Document>();
		ArrayList<Document> infrarankSubpopulationList = new ArrayList<Document>();

		while (taxaFiles.size() > 0) {
			Document file = DocumentUtils.getVFSFileAsDocument(taxaFiles.remove(0), vfs);
			String nodeLevel = file.getDocumentElement().getAttribute("level");
			if (nodeLevel.trim().equalsIgnoreCase(BrowseTaxonomyRestlet.KINGDOM)) {
				kingdomList.add(file);
			} else if (nodeLevel.trim().equalsIgnoreCase(BrowseTaxonomyRestlet.PHYLUM)) {
				phylumList.add(file);
			} else if (nodeLevel.trim().equalsIgnoreCase(BrowseTaxonomyRestlet.CLASS)) {
				classList.add(file);
			} else if (nodeLevel.trim().equalsIgnoreCase(BrowseTaxonomyRestlet.ORDER)) {
				orderList.add(file);
			} else if (nodeLevel.trim().equalsIgnoreCase(BrowseTaxonomyRestlet.FAMILY)) {
				familyList.add(file);
			} else if (nodeLevel.trim().equalsIgnoreCase(BrowseTaxonomyRestlet.GENUS)) {
				genusList.add(file);
			} else if (nodeLevel.trim().equalsIgnoreCase(BrowseTaxonomyRestlet.SPECIES)) {
				speciesList.add(file);
			} else if (nodeLevel.trim().equalsIgnoreCase(BrowseTaxonomyRestlet.INFRARANK)) {
				infrarankList.add(file);
			} else if (nodeLevel.trim().equalsIgnoreCase(BrowseTaxonomyRestlet.SUBPOPULATION)) {
				subpopulationList.add(file);
			} else if (nodeLevel.trim().equalsIgnoreCase(BrowseTaxonomyRestlet.INFRARANK_SUBPOPULATION)) {
				infrarankSubpopulationList.add(file);
			}
		}

		while (kingdomList.size() > 0) {
			String[] ids = importNodes(kingdomList.remove(0), BrowseTaxonomyRestlet.KINGDOM, newIds).split(",");
			if (!ids[0].equalsIgnoreCase(ids[1])) {
				newIds.put(ids[0], ids[1]);
			}
		}
		while (phylumList.size() > 0) {
			String[] ids = importNodes(phylumList.remove(0), BrowseTaxonomyRestlet.PHYLUM, newIds).split(",");
			if (!ids[0].equalsIgnoreCase(ids[1])) {
				newIds.put(ids[0], ids[1]);
			}
		}
		while (classList.size() > 0) {
			String[] ids = importNodes(classList.remove(0), BrowseTaxonomyRestlet.CLASS, newIds).split(",");
			if (!ids[0].equalsIgnoreCase(ids[1])) {
				newIds.put(ids[0], ids[1]);
			}
		}
		while (orderList.size() > 0) {
			String[] ids = importNodes(orderList.remove(0), BrowseTaxonomyRestlet.ORDER, newIds).split(",");
			if (!ids[0].equalsIgnoreCase(ids[1])) {
				newIds.put(ids[0], ids[1]);
			}
		}
		while (familyList.size() > 0) {
			String[] ids = importNodes(familyList.remove(0), BrowseTaxonomyRestlet.FAMILY, newIds).split(",");
			if (!ids[0].equalsIgnoreCase(ids[1])) {
				newIds.put(ids[0], ids[1]);
			}
		}
		while (genusList.size() > 0) {
			String[] ids = importNodes(genusList.remove(0), BrowseTaxonomyRestlet.GENUS, newIds).split(",");
			if (!ids[0].equalsIgnoreCase(ids[1])) {
				newIds.put(ids[0], ids[1]);
			}
		}
		while (speciesList.size() > 0) {
			String[] ids = importNodes(speciesList.remove(0), BrowseTaxonomyRestlet.SPECIES, newIds).split(",");
			if (!ids[0].equalsIgnoreCase(ids[1])) {
				newIds.put(ids[0], ids[1]);
			}
		}
		while (infrarankList.size() > 0) {
			String[] ids = importNodes(infrarankList.remove(0), BrowseTaxonomyRestlet.INFRARANK, newIds).split(",");
			if (!ids[0].equalsIgnoreCase(ids[1])) {
				newIds.put(ids[0], ids[1]);
			}
		}
		while (subpopulationList.size() > 0) {
			String[] ids = importNodes(subpopulationList.remove(0), BrowseTaxonomyRestlet.SUBPOPULATION, newIds).split(
					",");
			if (!ids[0].equalsIgnoreCase(ids[1])) {
				newIds.put(ids[0], ids[1]);
			}
		}
		while (infrarankSubpopulationList.size() > 0) {
			String[] ids = importNodes(infrarankSubpopulationList.remove(0),
					BrowseTaxonomyRestlet.INFRARANK_SUBPOPULATION, newIds).split(",");
			if (!ids[0].equalsIgnoreCase(ids[1])) {
				newIds.put(ids[0], ids[1]);
			}
		}
		return newIds;
	}

	/**
	 * Imports the node into the appropriate location, returns the id of the
	 * document and the new id of the document where it is currently saved on
	 * the system. Returns -1 if there was a failure.
	 * 
	 * @param document
	 * @return
	 */
	private String importNodes(Document document, String rank, final HashMap<String, String> oldToNewIDs) {
		String oldID = "";
		String newID = "";
		Document finalDocumentToSave = null;
		try {

			// GET OLD ID
			oldID = document.getDocumentElement().getAttribute("id");
			SysDebugger.getInstance().println("I am in importnodes with id " + oldID);
			String fullNames = getKingdomFullName(document);
			Request newRequest = new Request(Method.GET, "riap://host/browse/taxonName/" + fullNames.substring(0, fullNames.indexOf(",", 0)) + "/"
					+ fullNames.substring(fullNames.indexOf(",", 0), fullNames.length()));
			Response newResponse = getContext().getClientDispatcher().handle(newRequest);

			// IF IT IS SUCCESS THEN ONE ALREADY EXISTS
			if (newResponse.getStatus().isSuccess()) {
				Document existingDocument = (new DomRepresentation(newResponse.getEntity())).getDocument();

				// GET NEW ID
				newID = existingDocument.getDocumentElement().getAttribute("id");
				SysDebugger.getInstance().println("This is my old id " + oldID + " and this is my new id " + newID);

				// CREATE A NEW DOCUMENT IF
				finalDocumentToSave = checkCommonNames(document, existingDocument);

				// GET MERGED SYNONYMS
				finalDocumentToSave = checkSynonyms(document, existingDocument, finalDocumentToSave);

				// GET MERGED SYNONYMS
				finalDocumentToSave = checkAssessments(document, existingDocument, finalDocumentToSave);

				// IF THE FINAL DOCUMENT IS NOT EQUAL TO NULL THAT MEANS THAT WE
				// HAVE TO REPLACE WHAT IS ON THE SERVER
				if (finalDocumentToSave != null) {

					Request newRequest2 = new Request(Method.PUT, "riap://host/browse/nodes/"
							+ finalDocumentToSave.getDocumentElement().getAttribute("id"), new DomRepresentation(
							MediaType.TEXT_XML, finalDocumentToSave));
					Response newResponse2 = getContext().getClientDispatcher().handle(newRequest2);

					if (!newResponse2.getStatus().isSuccess()) {
						SysDebugger.getInstance().println("failed in browse taxonomy restlet");
						return "-1";
					}

				}
				// ELSE THEY WERE THE SAME, OR THE IMPORTED ONE HAD LESS
				// INFORMATION, MEANS WE DON'T HAVE TO DO ANYTHING

			}
			// IF FAILURE IT DOSEN'T PREVIOUSLY EXIST
			else {

				// CHECK TO MAKE SURE THAT PARENT IS OKAY
				if (!rank.equalsIgnoreCase(BrowseTaxonomyRestlet.KINGDOM)) {
					String oldParentID = document.getDocumentElement().getAttribute("parentid");
					if (oldToNewIDs.containsKey(oldParentID)) {
						document.getDocumentElement().setAttribute("parentid", oldToNewIDs.get(oldParentID));
					}
				}

				newRequest = new Request(Method.PUT, "riap://host/taxomatic/new", new DomRepresentation(
						MediaType.TEXT_XML, document));
				newResponse = getContext().getClientDispatcher().handle(newRequest);

				if (!newResponse.getStatus().isSuccess()) {
					SysDebugger.getInstance().println("failed in taxomatic");
					return "-1";
				} else {
					newID = newResponse.getEntity().getText();
					SysDebugger.getInstance().println("This is the new id " + newID);
				}
			}

			return oldID + "," + newID;
		} catch (Exception e) {
			e.printStackTrace();
			return "-1";
		}
	}

	private boolean importPublishedAssessment(Document document, String username, HashMap<String, String> newIds, Request request) {

		if (SISContainerApp.amIOnline) {
			return true;
		} else {
			try {
				Element documentElement = document.getDocumentElement();
				Element basicInfo = (Element) documentElement.getElementsByTagName("basicInformation").item(0);
				String assessmentID = basicInfo.getElementsByTagName("assessmentID").item(0).getTextContent();
				String speciesName = basicInfo.getElementsByTagName("speciesName").item(0).getTextContent();
				String speciesID = basicInfo.getElementsByTagName("speciesID").item(0).getTextContent();
				if( newIds.containsKey(speciesID) )
					basicInfo.getElementsByTagName("speciesID").item(0).setTextContent(newIds.get(speciesID));


				Request newRequest = new Request(Method.PUT, "riap://application/assessments", new DomRepresentation(MediaType.TEXT_XML, document));
				newRequest.setChallengeResponse(new ChallengeResponse(ChallengeScheme.HTTP_BASIC, username, ""));
				Response newResponse = getContext().getClientDispatcher().handle(newRequest);

				if (newResponse.getStatus().isSuccess())
					return true;
				else {
					System.out.println("RIAP failed: " + newResponse.getStatus().getCode());
					return false;
				}
			} catch (Exception e1) {
				e1.printStackTrace();
				return false;
			}
		}
	}

	/**
	 * given a document that represents the working set, and the username of
	 * where the working set should be imported, copies the working set into the
	 * user's set of working sets
	 * 
	 * @param document
	 * @param username
	 * @return true if successful import, false otherwise
	 */
	private boolean importWorkingSet(Document document, String username) {

		try {

			// SEND TO THE WORKING SET RESTLET
			document.getDocumentElement().setAttribute("creator", username);
			document.getDocumentElement().setAttribute("id", "");
			Request newRequest = new Request(Method.PUT, "riap://host/workingSet/public/" + username,
					new DomRepresentation(MediaType.TEXT_XML, document));
			Response newResponse = getContext().getClientDispatcher().handle(newRequest);

			if (newResponse.getStatus().isSuccess()) {
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

	}

	@Override
	public void performService(Request request, Response response) {
		try {
			String username = (String) request.getAttributes().get("username");
			String workingSetID = (String) request.getAttributes().get("workingsetID");
			if (request.getMethod().equals(Method.GET)) {
				boolean lockParam = Boolean.valueOf(request.getResourceRef().getQueryAsForm().getFirstValue("lock")).booleanValue();
					
				if (request.getResourceRef().getPath().startsWith("/workingSetExporter/private/"))
					export("private", username, workingSetID, lockParam, response, request);
				else
					export("public", username, workingSetID, lockParam, response, request);
			} else if (request.getMethod().equals(Method.POST) && vfs.exists(new VFSPath(getUserPath(username))))
				postZipFile(username, response, request);
			else
				response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
		} catch (Exception e) {
			e.printStackTrace();
			response.setStatus(Status.SERVER_ERROR_INTERNAL);
		}
	}

	private void postZipFile(String username, Response response, Request request) {

		RestletFileUpload fileUploaded = new RestletFileUpload(new DiskFileItemFactory());
		try {
			List<FileItem> list = fileUploaded.parseRequest(request);
			FileItem file = null;

			for (int i = 0; i < list.size() && file == null; i++) {
				FileItem item = list.get(i);
				if (!item.isFormField()) {
					file = item;
				}
			}

			if (file == null) {
				System.out.println("Supposed file attachment is NULL...");
				response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			}

			else {
				if (!vfs.exists(new VFSPath(getImportPath(username)))) {
					vfs.makeCollection(new VFSPath(getImportPath(username)));
				}
				OutputStream outStream = vfs.getOutputStream(new VFSPath(getImportURL(username)));
				outStream.write(file.get());
				outStream.close();

				unZipAndImport(getImportURL(username), username, response, request);
			}

		} catch (Exception e) {
			e.printStackTrace();
			response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
		}

	}

	private Document replaceTaxaIDInWorkingSet(Document workingSetDoc, String oldID, String newID) {

		Element taxa = (Element) workingSetDoc.getDocumentElement().getElementsByTagName("taxa").item(0);
		NodeList species = taxa.getElementsByTagName("species");
		Node nodeToReplace = null;

		for (int i = 0; i < species.getLength() && nodeToReplace == null; i++) {
			String tempID = species.item(i).getTextContent();
			if (tempID.trim().equalsIgnoreCase(oldID.trim())) {
				nodeToReplace = species.item(i);
			}
		}

		if (nodeToReplace != null) {
			Node newNode = DocumentUtils.createElementWithText(workingSetDoc, "species", newID);
			(nodeToReplace.getParentNode()).replaceChild(newNode, nodeToReplace);
			return workingSetDoc;
		} else
			return null;
	}

	private void unZipAndImport(String url, String username, Response response, Request request) {

		ArrayList<String> taxaFiles = new ArrayList<String>();
		ArrayList<String> draftFiles = new ArrayList<String>();
		ArrayList<String> publishedFiles = new ArrayList<String>();
		String workingSetFile = "";
		Document workingSetDocument = null;
		
		ArrayList<AssessmentData> successfulImports = new ArrayList<AssessmentData>();
		
		try {
			ZipInputStream zipInputStream = new ZipInputStream(vfs.getInputStream(new VFSPath(url)));
			ZipEntry entry = zipInputStream.getNextEntry();
			while (entry != null) {

				// UNZIP
				// SysDebugger.getInstance().println("Unzipping " +
				// entry.getName());
				if (!vfs.exists(new VFSPath(getImportFUllPath(username, entry.getName())))) {
					Writer writer = vfs
							.getWriter(new VFSPath(getImportFUllPath(username, entry.getName().substring(1))));
					writer.close();
				}

				String filePathToImport = getImportFUllPath(username, entry.getName().substring(1));
				FileOutputStream fout = (FileOutputStream) vfs.getOutputStream(new VFSPath(filePathToImport));

				for (int c = zipInputStream.read(); c != -1; c = zipInputStream.read()) {
					fout.write(c);
				}
				zipInputStream.closeEntry();
				fout.close();

				entry = zipInputStream.getNextEntry();

				// DETERMINE WHICH LIST TO PLACE THE FILE IN

				// IT IS A DRAFT ASSESSMENT
				if (filePathToImport.indexOf("/drafts/") != -1) {
					draftFiles.add(filePathToImport);
				}

				// IT IS A TAXON
				else if (filePathToImport.indexOf("/nodes/") != -1) {
					taxaFiles.add(filePathToImport);
				}

				// IT IS A TAXON
				else if (filePathToImport.indexOf("/browse/assessments/") != -1) {
					publishedFiles.add(filePathToImport);
				}

				// IT IS A WORKING SET
				else {
					workingSetFile = filePathToImport;
				}

			}
			// FINISHED UNZIPPING
			zipInputStream.closeEntry();

			boolean successfulImportThusFar = true;

			// IMPORT TAXA, ADDING THE ID AND THE PREVIOUS ID IN THE TAXANEWID
			// ARRAYLIST VIA COMMA SEPERATED VALUES
			// IF THEY ARE DIFFERENT, ONLY ADD THE SINGLE ID IF THEY ARE THE
			// SAME. SHOULD THROW EXCEPTION IF
			// THERE WAS A PROBLEM AND IMPORTNODES RETURNED -1
			HashMap<String, String> newIds = importNodes(taxaFiles);

			Iterator<String> iter = newIds.keySet().iterator();
			while (iter.hasNext()) {
				String temp = iter.next();
				SysDebugger.getInstance().println("This is in the hashmap " + temp + "," + newIds.get(temp));
			}

			// IMPORT DRAFT ASSESSMENTS
			while (!draftFiles.isEmpty() && successfulImportThusFar) {
				String currentFile = draftFiles.remove(0);
				Document currentDraftDoc = DocumentUtils.getVFSFileAsDocument(currentFile, vfs);
				String assessmentSpeciesID = getSpeciesID(currentDraftDoc);
				
				NativeDocument ndoc = NativeDocumentFactory.newNativeDocument();
				ndoc.parse(DocumentUtils.serializeNodeToString(currentDraftDoc.getDocumentElement()));
				AssessmentParser pa = new AssessmentParser(ndoc);
				AssessmentData assessment = pa.getAssessment();
				
				// IF THE CURRENTID IS NOT IN THE LIST, THAT MEANS THAT IT HAS
				// BEEN CHANGED
				if (newIds.containsKey(assessmentSpeciesID)) {
					String newID = newIds.get(assessmentSpeciesID);
					assessment.setSpeciesID(newID);
					
					List<AssessmentData> compareTo = AssessmentIO.readAllDraftAssessments(vfs, assessment.getSpeciesID());

					boolean found = false;
					for( AssessmentData cur : compareTo )
						if( cur.getRegionIDs().containsAll(assessment.getRegionIDs()) || cur.isGlobal() && assessment.isGlobal() ) {
							assessment.setAssessmentID(cur.getAssessmentID());
							found = true;
						}
					if( !found )
						assessment.setAssessmentID("new");
					
					if( isImportAllowed(assessment, username)) {
						successfulImportThusFar = importDraftAssessment(assessment, username, request);
						if( successfulImportThusFar ) {
							if( SISContainerApp.amIOnline && !assessment.getAssessmentID().equals("new") )
								FileLocker.impl.persistentEagerRelease(assessment.getAssessmentID(), 
										BaseAssessment.DRAFT_ASSESSMENT_STATUS, username);
							successfulImports.add(assessment);
						}
					}
				} else {
					List<AssessmentData> compareTo = AssessmentIO.readAllDraftAssessments(vfs, assessment.getSpeciesID());

					boolean found = false;
					for( AssessmentData cur : compareTo )
						if( cur.getRegionIDs().containsAll(assessment.getRegionIDs()) || cur.isGlobal() && assessment.isGlobal() ) {
							assessment.setAssessmentID(cur.getAssessmentID());
							found = true;
						}
					if( !found )
						assessment.setAssessmentID("new");
					
					if( isImportAllowed(assessment, username)) {
						successfulImportThusFar = importDraftAssessment(assessment, username, request);
						if( successfulImportThusFar ) {
							if( SISContainerApp.amIOnline )
								FileLocker.impl.persistentEagerRelease(assessment.getAssessmentID(), 
										BaseAssessment.DRAFT_ASSESSMENT_STATUS, username);
							successfulImports.add(assessment);
						}
					}
				}
			}

			// IMPORT PUBLISHED ASSESSMENTS
			while (!publishedFiles.isEmpty() && successfulImportThusFar) {
				String currentFile = publishedFiles.remove(0);
				Document currentPubDoc = DocumentUtils.getVFSFileAsDocument(currentFile, vfs);

				// SAVE
				successfulImportThusFar = importPublishedAssessment(currentPubDoc, username, newIds, request);
			}

			// IMPORT WORKING SET
			if (successfulImportThusFar) {

				workingSetDocument = DocumentUtils.getVFSFileAsDocument(workingSetFile, vfs);
				NodeList taxaList = ((Element) workingSetDocument.getDocumentElement().getElementsByTagName("taxa")
						.item(0)).getElementsByTagName("species");

				SysDebugger.getInstance().println(
						"This is the workingsetdocument " + DocumentUtils.getVFSFileAsString(workingSetFile, vfs));

				// HAVE TO CHANGE THE WORKING SET TO REFLECT THE NEW IDS

				for (int i = 0; i < taxaList.getLength(); i++) {
					String tempID = taxaList.item(i).getTextContent();
					SysDebugger.getInstance().println("this is what is in the workingset " + tempID);
					// IF WE NEED TO REPLACE THE ID
					if (newIds.containsKey(tempID)) {
						workingSetDocument = replaceTaxaIDInWorkingSet(workingSetDocument, tempID, newIds.get(tempID));
					}
				}
			}

			if (!successfulImportThusFar) {
				response.setEntity(new StringRepresentation("A failure occurred during the import.", MediaType.TEXT_PLAIN));
				response.setStatus(Status.SERVER_ERROR_INTERNAL);
			} else {
				// CHANGE FROM PUBLIC TO PRIVATE IF NECESSARY
				workingSetDocument = changePrivateToPublic(workingSetDocument);
				successfulImportThusFar = importWorkingSet(workingSetDocument, username);

				// REMOVE ALL UNNECESSARY FILES IN /imports
				String[] inImports = vfs.list(getImportPath(username));
				for (int i = 0; i < inImports.length; i++) {
					String path = getImportPath(username) + "/" + inImports[i];
					if (!(path).equalsIgnoreCase(getImportURL(username)))
						vfs.delete(new VFSPath(path));
				}

				StringBuilder ret = new StringBuilder("<div>");
				if( SISContainerApp.amIOnline ) {
					for( AssessmentData curAss : successfulImports ) {
						ret.append("<div>A" + (curAss.isGlobal() ? " global " : " regional "));
						ret.append("draft assessment has been imported for the taxon " + curAss.getSpeciesName()+".</div>");
					}
					
					if( successfulImports.size() == 0 )
						ret.append("There were no assessments to import. If this seems incorrect, " +
								"please check with an administrator to ensure you had successfully " +
								"obtained checkout locks when you first exported this working set.");
				} else {
					ret.append("All taxa and assessments have been imported into your offline copy.");
				}
				ret.append("</div>");
				
				response.setEntity(new StringRepresentation(ret.toString(), MediaType.TEXT_HTML));
				response.setStatus(Status.SUCCESS_CREATED);
			}

		} catch (Exception e) {
			e.printStackTrace();
			StringBuilder ret = new StringBuilder(e.toString() + "\n");
			for( StackTraceElement el : e.getStackTrace() )
				ret.append(el.toString() + "\n");
				
			response.setEntity(new StringRepresentation(ret.toString(), MediaType.TEXT_PLAIN));
			response.setStatus(Status.SERVER_ERROR_INTERNAL);
		}
	}

	private boolean isImportAllowed(AssessmentData assessment, String username) {
		if( !SISContainerApp.amIOnline )
			return true;
		else {
			Lock lock = FileLocker.impl.getAssessmentPersistentLock(assessment.getAssessmentID(), assessment.getType());
			
			if( lock == null ) {
				if( assessment.isGlobal() ) {
					AssessmentData data = AssessmentIO.readAssessment(vfs, assessment.getAssessmentID(), 
							assessment.getType(), "");
					return data == null; //If it's not locked but it doesn't exist, let it import
				} else {
					List<AssessmentData> assessments = AssessmentIO.readAllDraftAssessments(vfs, assessment.getSpeciesID());
					for( AssessmentData curAss : assessments )
						if( curAss.isRegional() && curAss.getRegionIDs().equals(assessment.getRegionIDs()) )
							return false;
					
					return true;
				}
			} else
				return lock.getUsername().equalsIgnoreCase(username) && lock.getLockType().equals(LockType.CHECKED_OUT);
		}
	}

}
