package org.iucn.sis.server.restlets.workingsets;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.hibernate.Session;
import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.server.api.io.AssessmentIO;
import org.iucn.sis.server.api.io.TaxomaticIO;
import org.iucn.sis.server.api.io.TaxonIO;
import org.iucn.sis.server.api.io.UserIO;
import org.iucn.sis.server.api.io.WorkingSetIO;
import org.iucn.sis.server.api.io.AssessmentIO.AssessmentIOWriteResult;
import org.iucn.sis.server.api.locking.LockException;
import org.iucn.sis.server.api.locking.LockType;
import org.iucn.sis.server.api.locking.LockRepository.LockInfo;
import org.iucn.sis.server.api.restlets.BaseServiceRestlet;
import org.iucn.sis.server.api.utils.DocumentUtils;
import org.iucn.sis.server.api.utils.TaxomaticException;
import org.iucn.sis.server.utils.WorkingSetExporter;
import org.iucn.sis.shared.api.debug.Debug;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.CommonName;
import org.iucn.sis.shared.api.models.Synonym;
import org.iucn.sis.shared.api.models.Taxon;
import org.iucn.sis.shared.api.models.TaxonLevel;
import org.iucn.sis.shared.api.models.User;
import org.iucn.sis.shared.api.models.WorkingSet;
import org.restlet.Context;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.fileupload.RestletFileUpload;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.OutputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ResourceException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.solertium.lwxml.factory.NativeDocumentFactory;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.util.TrivialExceptionHandler;
import com.solertium.vfs.VFS;
import com.solertium.vfs.VFSPath;
import com.solertium.vfs.VFSPathToken;

public class WorkingSetExportImportRestlet extends BaseServiceRestlet {
	
	private final VFS vfs;

	public WorkingSetExportImportRestlet(Context context) {
		super(context);
		vfs = SIS.get().getVFS();
	}
	
	@Override
	public void definePaths() {
		paths.add("/workingSetExporter/private/{username}/{workingsetID}");
		paths.add("/workingSetImporter/{username}");
		paths.add("/workingSetExporter/public/{username}/{workingsetID}");
		paths.add("/workingSetExporter/downloads/{file}");
	}
	
	@Override
	public Representation handleGet(Request request, Response response, Session session) throws ResourceException {
		String file = (String)request.getAttributes().get("file");
		if (file != null) {
			final InputRepresentation dl;
			try {
				dl = new InputRepresentation(new FileInputStream(open(file)));
			} catch (IOException e) {
				throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
			}
			dl.setDownloadable(true);
			dl.setDownloadName(file);
			
			return dl;
		}
		
		String username = (String) request.getAttributes().get("username");
		String workingSetID = (String) request.getAttributes().get("workingsetID");
		
		boolean lockParam = Boolean.valueOf(request.getResourceRef().getQueryAsForm().getFirstValue("lock"))
					.booleanValue();
		
		UserIO userIO = new UserIO(session);

		try {
			return export(userIO.getUserFromUsername(username), Integer.valueOf(workingSetID), lockParam,
					response, request, session);
		} catch (IOException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
	}
	
	@Override
	public void handlePost(Representation entity, Request request, Response response, Session session) throws ResourceException {
		String username = (String) request.getAttributes().get("username");
		if (vfs.exists(new VFSPath(getUserPath(username))))
			postZipFile(username, response, request, session);
		else
			super.handlePost(entity, request, response, session);
	}

	private Representation export(final User user, final Integer workingsetID, boolean lockParam, final Response response,
			final Request request, Session session) throws IOException, ResourceException {

		final PipedInputStream inputStream = new PipedInputStream(); 
		final Representation representation = new OutputRepresentation(MediaType.TEXT_HTML) {
			public void write(OutputStream out) throws IOException {
				byte[] b = new byte[8];
				int read;
				while ((read = inputStream.read(b)) != -1) {
					out.write(b, 0, read);
					out.flush();
				}
			}
		};
		
		PrintWriter writer;
		try {
			writer = new PrintWriter(new OutputStreamWriter(new PipedOutputStream(inputStream)), true);
		} catch (IOException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
		
		String fileName = user.getId() + "_" + workingsetID + "_" + 
			Calendar.getInstance().getTimeInMillis();
		
		WorkingSetExporter exporter = new WorkingSetExporter( 
				workingsetID, user.getUsername(), lockParam, 
				createTempFolder(fileName), fileName);
		exporter.setOutputStream(writer, "<br/>");
		
		new Thread(exporter).start();
		
		return representation;
	}
	
	private File open(String fileName) throws ResourceException {
		File tmp;
		try {
			tmp = File.createTempFile("toDelete", "tmp");
		} catch (IOException e) {
			throw new ResourceException(Status.SERVER_ERROR_INSUFFICIENT_STORAGE, e);
		}
		
		try {
			String folder = fileName.split("\\.")[0];
			File tmpFolder = new File(tmp.getParentFile(), folder);
			File tmpFile = new File(tmpFolder, fileName);
			
			if (!tmpFile.exists())
				throw new ResourceException(Status.CLIENT_ERROR_GONE);
			
			return tmpFile;
		} catch (Exception e) {
			throw new ResourceException(Status.CLIENT_ERROR_GONE, e);
		} finally {
			tmp.delete();
		}
	}
	
	private String createTempFolder(String fileName) throws ResourceException {
		File tmp;
		try {
			tmp = File.createTempFile("toDelete", "tmp");
		} catch (IOException e) {
			throw new ResourceException(Status.SERVER_ERROR_INSUFFICIENT_STORAGE, e);
		}
		
		File folder = tmp.getParentFile();
		File tmpFolder = new File(folder, fileName);
		tmpFolder.mkdirs();
		
		tmp.delete();
		
		return tmpFolder.getAbsolutePath();
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

			if (level.equalsIgnoreCase(TaxonLevel.getDisplayableLevel(TaxonLevel.KINGDOM))) {
				kingdom = fullName;
			} else {
				kingdom = fullFootprint[TaxonLevel.KINGDOM];
			}

			if (level.equalsIgnoreCase(TaxonLevel.getDisplayableLevel(TaxonLevel.SPECIES))) {
				fullName = fullFootprint[TaxonLevel.GENUS] + fullName;
			} else if (level.equalsIgnoreCase(TaxonLevel.getDisplayableLevel(TaxonLevel.INFRARANK))) {
				if (infraType.trim().equals("0"))
					fullName = fullFootprint[TaxonLevel.GENUS] + fullFootprint[TaxonLevel.SPECIES]
							+ " ssp. " + fullName;
				else if (infraType.trim().equals("1"))
					fullName = fullFootprint[TaxonLevel.GENUS] + fullFootprint[TaxonLevel.SPECIES]
							+ " var. " + fullName;
				else {
					fullName = fullFootprint[TaxonLevel.GENUS] + fullFootprint[TaxonLevel.SPECIES]
							+ fullName;
				}
			} else if (level.equalsIgnoreCase(TaxonLevel.getDisplayableLevel(TaxonLevel.INFRARANK))
					|| level.equalsIgnoreCase(TaxonLevel.getDisplayableLevel(TaxonLevel.SUBPOPULATION))) {
				fullName = fullFootprint[TaxonLevel.GENUS] + fullFootprint[TaxonLevel.SPECIES] + fullName;
			} else if (level.equalsIgnoreCase(TaxonLevel.getDisplayableLevel(TaxonLevel.INFRARANK_SUBPOPULATION))) {
				fullName = fullFootprint[TaxonLevel.GENUS] + fullFootprint[TaxonLevel.SPECIES]
						+ fullFootprint[TaxonLevel.INFRARANK] + fullName;
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

	private boolean importDraftAssessment(Assessment assessment, String username, Request request) {
		try {
			// TODO: RIGHT NOW IT JUST REWRITES OVER DRAFT ASSESSMENTS ... NOT
			// NECESSARILY WANTED BEHAVIOR

			Integer assessmentID = assessment.getId();
			String url = "riap://host/assessments";

			Method method = Method.PUT;

			Request newRequest = new Request(method, url, new StringRepresentation(assessment.toXML(),
					MediaType.TEXT_XML));
			if (request.getChallengeResponse() != null)
				newRequest.setChallengeResponse(new ChallengeResponse(request.getChallengeResponse().getScheme(),
						request.getChallengeResponse().getIdentifier(), request.getChallengeResponse().getSecret()));
			else
				newRequest.setChallengeResponse(new ChallengeResponse(ChallengeScheme.HTTP_BASIC, username, ""));

			Response newResponse = getContext().getClientDispatcher().handle(newRequest);

			if (newResponse.getStatus().isSuccess())
				return true;
			else {
				Debug.println("Unsuccessfully PUT draft assessment {0}: {1}" + assessmentID, newResponse.getStatus());
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	private HashMap<Integer, Integer> importTaxa(ArrayList<String> taxaFiles, User user, Session session) {
		HashMap<Integer, Integer> importedIdsToSISIds = new HashMap<Integer, Integer>();

		// BREAK INTO KINGDOM, PHYLUM, CLASS ,ORDER , FAMILY ,GENUS ,
		// SPECIES , INFRARANK , SUBPOPULATION , INFRARANK_SUBPOPULATION

		Map<Integer, Taxon> idToImportedTaxon = new HashMap<Integer, Taxon>();
		ArrayList<Taxon> kingdomList = new ArrayList<Taxon>();
		ArrayList<Taxon> phylumList = new ArrayList<Taxon>();
		ArrayList<Taxon> classList = new ArrayList<Taxon>();
		ArrayList<Taxon> orderList = new ArrayList<Taxon>();
		ArrayList<Taxon> familyList = new ArrayList<Taxon>();
		ArrayList<Taxon> genusList = new ArrayList<Taxon>();
		ArrayList<Taxon> speciesList = new ArrayList<Taxon>();
		ArrayList<Taxon> infrarankList = new ArrayList<Taxon>();
		ArrayList<Taxon> subpopulationList = new ArrayList<Taxon>();
		ArrayList<Taxon> infrarankSubpopulationList = new ArrayList<Taxon>();

		while (taxaFiles.size() > 0) {
			NativeDocument file = NativeDocumentFactory.newNativeDocument();
			file.parse(DocumentUtils.getVFSFileAsString(taxaFiles.remove(0), vfs));
			Taxon taxon = Taxon.fromXML(file);
			idToImportedTaxon.put(taxon.getId(), taxon);

			Integer nodeLevel = taxon.getLevel();
			if (nodeLevel == TaxonLevel.KINGDOM) {
				kingdomList.add(taxon);
			} else if (nodeLevel == TaxonLevel.PHYLUM) {
				phylumList.add(taxon);
			} else if (nodeLevel == TaxonLevel.CLASS) {
				classList.add(taxon);
			} else if (nodeLevel == TaxonLevel.ORDER) {
				orderList.add(taxon);
			} else if (nodeLevel == TaxonLevel.FAMILY) {
				familyList.add(taxon);
			} else if (nodeLevel == TaxonLevel.GENUS) {
				genusList.add(taxon);
			} else if (nodeLevel == TaxonLevel.SPECIES) {
				speciesList.add(taxon);
			} else if (nodeLevel == TaxonLevel.INFRARANK) {
				infrarankList.add(taxon);
			} else if (nodeLevel == TaxonLevel.SUBPOPULATION) {
				subpopulationList.add(taxon);
			} else if (nodeLevel == TaxonLevel.INFRARANK_SUBPOPULATION) {
				infrarankSubpopulationList.add(taxon);
			}
		}

		while (kingdomList.size() > 0) {
			importNodes(kingdomList.remove(0), idToImportedTaxon, importedIdsToSISIds, user, session);

		}
		while (phylumList.size() > 0) {
			importNodes(phylumList.remove(0), idToImportedTaxon, importedIdsToSISIds, user, session);

		}
		while (classList.size() > 0) {
			importNodes(classList.remove(0), idToImportedTaxon, importedIdsToSISIds, user, session);

		}
		while (orderList.size() > 0) {
			importNodes(orderList.remove(0), idToImportedTaxon, importedIdsToSISIds, user, session);

		}
		while (familyList.size() > 0) {
			importNodes(familyList.remove(0), idToImportedTaxon, importedIdsToSISIds, user, session);

		}
		while (genusList.size() > 0) {
			importNodes(genusList.remove(0), idToImportedTaxon, importedIdsToSISIds, user, session);

		}
		while (speciesList.size() > 0) {
			importNodes(speciesList.remove(0), idToImportedTaxon, importedIdsToSISIds, user, session);

		}
		while (infrarankList.size() > 0) {
			importNodes(infrarankList.remove(0), idToImportedTaxon, importedIdsToSISIds, user, session);

		}
		while (subpopulationList.size() > 0) {
			importNodes(subpopulationList.remove(0), idToImportedTaxon, importedIdsToSISIds, user, session);

		}
		while (infrarankSubpopulationList.size() > 0) {
			importNodes(infrarankSubpopulationList.remove(0), idToImportedTaxon, importedIdsToSISIds, user, session);

		}
		return importedIdsToSISIds;
	}

	/**
	 * Adds new synonyms created in the importedTaxon to the sisTaxon
	 * 
	 * @param importedTaxon
	 * @param sisTaxon
	 */
	private void importSynonyms(Taxon importedTaxon, Taxon sisTaxon) {
		for (Synonym syn : importedTaxon.getSynonyms()) {
			if (!sisTaxon.getSynonyms().contains(syn)) {
				syn.setId(0);
				sisTaxon.getSynonyms().add(syn);
			}
		}
	}

	/**
	 * Adds new synonyms created in the importedTaxon to the sisTaxon
	 * 
	 * @param importedTaxon
	 * @param sisTaxon
	 */
	private void importCommonNames(Taxon importedTaxon, Taxon sisTaxon) {
		for (CommonName commonName : importedTaxon.getCommonNames()) {
			if (!sisTaxon.getCommonNames().contains(commonName)) {
				commonName.setId(0);
				sisTaxon.getCommonNames().add(commonName);
			}
		}
	}

	/**
	 * Imports the node into the appropriate location, returns the id of the
	 * document and the new id of the document where it is currently saved on
	 * the system. Returns -1 if there was a failure.
	 * 
	 * @param document
	 * @return
	 */
	private void importNodes(Taxon importedTaxon, final Map<Integer, Taxon> idsToImportedTaxon,
			final HashMap<Integer, Integer> oldToNewIDs, User user, Session session) {
		if (importedTaxon.getLevel() != TaxonLevel.KINGDOM) {
			importedTaxon.setParent(idsToImportedTaxon.get(importedTaxon.getParent().getId()));
		}

		TaxonIO taxonIO = new TaxonIO(session);
		TaxomaticIO taxomaticIO = new TaxomaticIO(session);
		
		Integer importedID = importedTaxon.getId();

		if (!taxonIO.hasDuplicate(importedTaxon)) {
			// THE TAXON IS NEW!
			importedTaxon.setId(0);
			try {
				taxomaticIO.saveNewTaxon(importedTaxon, user);
			} catch (TaxomaticException e) {
				Debug.println(e);
				return;
			}
		} else {
			Taxon sisTaxon = taxonIO.readTaxonByName(importedTaxon.getKingdomName(), 
					importedTaxon.getFriendlyName(), importedTaxon.getTaxonLevel());
			
			importSynonyms(importedTaxon, sisTaxon);
			importCommonNames(importedTaxon, sisTaxon);
			try {
				taxonIO.writeTaxon(sisTaxon, user, "Imported taxon.");
			} catch (TaxomaticException e) {
				Debug.println(e);
				return;
			}
			idsToImportedTaxon.put(importedID, sisTaxon);
		}

		oldToNewIDs.put(importedID, importedTaxon.getId());

	}

	private boolean importPublishedAssessment(Assessment published, Request request, Session session) {
		if (SIS.amIOnline()) {
			return false;
		} else {
			AssessmentIO assessmentIO = new AssessmentIO(session);
			try {

				AssessmentIOWriteResult result = assessmentIO.saveNewAssessment(published,
						getUser(request, session));
				return result.status.isSuccess();
			} catch (Exception e1) {
				e1.printStackTrace();
				return false;
			}
		}
	}

	
	private boolean importWorkingSet(WorkingSet workingSet, String username, Session session) {
		WorkingSetIO workingSetIO = new WorkingSetIO(session);
		UserIO userIO = new UserIO(session);
		
		workingSet.setId(0);
		workingSet.setCreatedDate(new Date());
		workingSet.setCreator(userIO.getUserFromUsername(username));
		workingSet.getUsers().add(workingSet.getCreator());
		return workingSetIO.saveWorkingSet(workingSet, workingSet.getCreator(), "Imported working set.");
	}

	private void postZipFile(String username, Response response, Request request, Session session) throws ResourceException {
		RestletFileUpload fileUploaded = new RestletFileUpload(new DiskFileItemFactory());
		List<FileItem> list;
		try {
			list = fileUploaded.parseRequest(request);
		} catch (Exception e) {
			Debug.println(e);
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, e);
		}
		FileItem file = null;
		for (int i = 0; i < list.size() && file == null; i++) {
			FileItem item = list.get(i);
			if (!item.isFormField()) {
				file = item;
			}
		}

		if (file == null)
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
			
		if (!vfs.exists(new VFSPath(getImportPath(username)))) {
			try {
				vfs.makeCollection(new VFSPath(getImportPath(username)));
			} catch (IOException e) {
				throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
			}
		}
		OutputStream outStream;
		try {
			outStream = vfs.getOutputStream(new VFSPath(getImportURL(username)));
			outStream.write(file.get());
		} catch (IOException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
		try {
			outStream.close();
		} catch (IOException e) {
			TrivialExceptionHandler.ignore(this, e);
		} finally {
			unZipAndImport(getImportURL(username), username, response, request, session);
		}
	}

	@SuppressWarnings("unused")
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

	private void unZipAndImport(String url, String username, Response response, Request request, Session session) {
		UserIO userIO = new UserIO(session);
		AssessmentIO assessmentIO = new AssessmentIO(session);
		
		ArrayList<String> taxaFiles = new ArrayList<String>();
		ArrayList<String> draftFiles = new ArrayList<String>();
		ArrayList<String> publishedFiles = new ArrayList<String>();
		String workingSetFile = "";
		//Document workingSetDocument = null;
		User user = userIO.getUserFromUsername(username);
		ArrayList<Assessment> successfulImports = new ArrayList<Assessment>();

		try {
			ZipInputStream zipInputStream = new ZipInputStream(vfs.getInputStream(new VFSPath(url)));
			ZipEntry entry = zipInputStream.getNextEntry();
			while (entry != null) {

				// UNZIP
				// System.out.println("Unzipping " +
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
			HashMap<Integer, Integer> importedIdsToSISIds = importTaxa(taxaFiles, user, session);

			// IMPORT DRAFT ASSESSMENTS
			while (!draftFiles.isEmpty() && successfulImportThusFar) {
				String currentFile = draftFiles.remove(0);
				Document currentDraftDoc = DocumentUtils.getVFSFileAsDocument(currentFile, vfs);
				NativeDocument ndoc = NativeDocumentFactory.newNativeDocument();
				ndoc.parse(DocumentUtils.serializeNodeToString(currentDraftDoc.getDocumentElement()));

				Assessment assessment = Assessment.fromXML(ndoc);
				Integer assessmentSpeciesID = assessment.getSpeciesID();

				// IF THE CURRENTID IS NOT IN THE LIST, THAT MEANS THAT IT HAS
				// BEEN CHANGED
				if (importedIdsToSISIds.containsKey(assessmentSpeciesID)) {
					Integer newID = importedIdsToSISIds.get(assessmentSpeciesID);
					assessment.getTaxon().setId(newID);

					List<Assessment> compareTo = assessmentIO.readUnpublishedAssessmentsForTaxon(
							assessment.getSpeciesID());

					boolean found = false;
					for (Assessment cur : compareTo) {
						if ((cur.isGlobal() && assessment.isGlobal())
								|| ((cur.getRegionIDs().contains(assessment.getRegionIDs()) && assessment
										.getRegionIDs().size() == cur.getRegionIDs().size()) && cur.isEndemic() == assessment
										.isEndemic())) {
							assessment.setId(cur.getId());
							found = true;
							break;
						}
					}

					if (!found)
						assessment.setId(0);

					if (isImportAllowed(assessment, username, session) && importDraftAssessment(assessment, username, request)) {
						if (!found && SIS.amIOnline())
							SIS.get().getLocker().persistentEagerRelease(assessment.getId(),
									userIO.getUserFromUsername(username));
						successfulImports.add(assessment);
					}

				}
			}

			// IMPORT PUBLISHED ASSESSMENTS
			if (!SIS.amIOnline()) {				
				
				while (!publishedFiles.isEmpty() && successfulImportThusFar) {
					String currentFile = publishedFiles.remove(0);
					NativeDocument ndoc = NativeDocumentFactory.newNativeDocument();
					ndoc.parse(DocumentUtils.getVFSFileAsString(currentFile, vfs));
					Assessment published = Assessment.fromXML(ndoc);
					published.getTaxon().setId(importedIdsToSISIds.get(published.getTaxon().getId()));
					successfulImportThusFar = importPublishedAssessment(published, request, session);
				}
				
			}

			// IMPORT WORKING SET
			if (successfulImportThusFar) {
				
				NativeDocument ndoc = NativeDocumentFactory.newNativeDocument();
				ndoc.parse(DocumentUtils.getVFSFileAsString(workingSetFile, vfs));
				WorkingSet workingSet = WorkingSet.fromXML(ndoc);
				
				// HAVE TO CHANGE THE WORKING SET TO REFLECT THE NEW IDS

				for (Taxon taxon : workingSet.getTaxon()) {
					taxon.setId(importedIdsToSISIds.get(taxon.getId()));					
				}
				
				successfulImportThusFar = importWorkingSet(workingSet, username, session);
			}

			if (!successfulImportThusFar) {
				response.setEntity(new StringRepresentation("A failure occurred during the import.",
						MediaType.TEXT_PLAIN));
				response.setStatus(Status.SERVER_ERROR_INTERNAL);
			} else {
				
				// REMOVE ALL UNNECESSARY FILES IN /imports
				VFSPathToken[] inImports = vfs.list(new VFSPath(getImportPath(username)));
				for (int i = 0; i < inImports.length; i++) {
					String path = getImportPath(username) + "/" + inImports[i];
					if (!(path).equalsIgnoreCase(getImportURL(username)))
						vfs.delete(new VFSPath(path));
				}

				StringBuilder ret = new StringBuilder("<div>");
				if (SIS.amIOnline()) {
					for (Assessment curAss : successfulImports) {
						ret.append("<div>A" + (curAss.isGlobal() ? " global " : " regional "));
						ret.append("draft assessment has been imported for the taxon " + curAss.getSpeciesName()
								+ ".</div>");
					}

					if (successfulImports.size() == 0)
						ret.append("There were no assessments to import. If this seems incorrect, "
								+ "please check with an administrator to ensure you had successfully "
								+ "obtained checkout locks when you first exported this working set.");
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
			for (StackTraceElement el : e.getStackTrace())
				ret.append(el.toString() + "\n");

			response.setEntity(new StringRepresentation(ret.toString(), MediaType.TEXT_PLAIN));
			response.setStatus(Status.SERVER_ERROR_INTERNAL);
		}
	}

	private boolean isImportAllowed(Assessment assessment, String username, Session session) {
		if (!SIS.amIOnline())
			return true;
		else if (assessment.getId() == 0) {
			AssessmentIO assessmentIO = new AssessmentIO(session);
			return assessmentIO.allowedToCreateNewAssessment(assessment);
		}
		else {
			LockInfo lock;
			try {
				lock = SIS.get().getLocker().getAssessmentPersistentLock(assessment.getId());
			} catch (LockException e) {
				return true;
			}
			return lock.getUsername().equalsIgnoreCase(username) && lock.getLockType().equals(LockType.CHECKED_OUT);
		}

	}

}
