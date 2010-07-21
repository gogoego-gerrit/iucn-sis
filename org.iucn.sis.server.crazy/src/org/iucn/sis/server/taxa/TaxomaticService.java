package org.iucn.sis.server.taxa;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.iucn.sis.server.baserestlets.ServiceRestlet;
import org.iucn.sis.server.io.AssessmentIO;
import org.iucn.sis.server.io.TaxaIO;
import org.iucn.sis.server.locking.FileLocker;
import org.iucn.sis.server.simple.SISContainerApp;
import org.iucn.sis.server.utils.DocumentUtils;
import org.iucn.sis.server.utils.ServerPaths;
import org.iucn.sis.server.utils.XMLUtils;
import org.iucn.sis.server.utils.logging.DBTrashBuffer;
import org.iucn.sis.server.utils.logging.EventLogger;
import org.iucn.sis.shared.BaseAssessment;
import org.iucn.sis.shared.data.assessments.AssessmentData;
import org.iucn.sis.shared.data.assessments.CanonicalNames;
import org.iucn.sis.shared.structures.SISCategoryAndCriteria;
import org.iucn.sis.shared.taxonomyTree.SynonymData;
import org.iucn.sis.shared.taxonomyTree.TaxonNode;
import org.iucn.sis.shared.taxonomyTree.TaxonNodeFactory;
import org.restlet.Context;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.xml.DomRepresentation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.solertium.db.CanonicalColumnName;
import com.solertium.db.DBException;
import com.solertium.db.ExecutionContext;
import com.solertium.db.SystemExecutionContext;
import com.solertium.db.query.DeleteQuery;
import com.solertium.db.query.QComparisonConstraint;
import com.solertium.db.query.QConstraint;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.util.Mutex;
import com.solertium.util.NodeCollection;
import com.solertium.util.SysDebugger;
import com.solertium.vfs.ConflictException;
import com.solertium.vfs.NotFoundException;
import com.solertium.vfs.VFS;
import com.solertium.vfs.VFSPath;
import com.solertium.vfs.VFSPathToken;
import com.solertium.vfs.provider.VersionedFileVFS;

public class TaxomaticService extends ServiceRestlet {
	private static final boolean UPDATE_RL_HISTORY_TEXT = true;

	private AtomicLong currentHighestTaxonID = new AtomicLong(100000);
	private AtomicBoolean writing = new AtomicBoolean(false);

	private Mutex lock = new Mutex();

	private DBTrashBuffer trashBuffer;

	public TaxomaticService(String vfsroot, Context context) {
		super(vfsroot, context);
		init();
	}

	public TaxomaticService(VFS vfs, Context context) {
		super(vfs, context);
		init();
	}

	public void definePaths() {
		paths.add("/taxomatic/{operation}");
	}

	private void deleteNode(String id, Request request, final Response response) {
		String url = null;
		try {
			url = ServerPaths.getURLForTaxa(id);

			SysDebugger.getInstance().println("Looking for taxon " + id + " in path " + url);
			System.out.println(url);
			if (!vfs.exists(url))
				throw new Exception("No Taxon!");

			TaxonNode node = TaxonNodeFactory.createNode(DocumentUtils.getVFSFileAsString(
					ServerPaths.getURLForTaxa(id), vfs), null, false);

			String kingdom = node.getFootprint()[0];
			String username = request.getChallengeResponse().getIdentifier();
			String parent = node.getParentId();
			String log = "<taxon user=\"" + username + "\" date=\"" + new Date().toString() + "\" parent=\"" + parent
					+ "\" node=\"" + node.getId() + "\" display=\"" + node.getFullName() + "\">" + id + "</taxon>";

			// remove published assessments
			final Request req = new Request(Method.DELETE, "riap://host/assessmentsByTaxon/" + 
					node.getId());
			req.setChallengeResponse(new ChallengeResponse(request.getChallengeResponse().getScheme(), 
					request.getChallengeResponse().getCredentials()));
			Response resp = getContext().getClientDispatcher().handle(req);
			if (!(resp.getStatus()).isSuccess()) {
				System.out.println("Unable to delete assessments for taxon " + node.getFullName());
			}
			
			trashBuffer.addEvent(DocumentUtils.createDocumentFromString(log));
			trashBuffer.flush();

			vfs.copy(url, url.replace("/browse", "/trash"));
			vfs.delete(url);

			ExecutionContext ec = new SystemExecutionContext("default");
			ec.setExecutionLevel(ExecutionContext.ADMIN);

			System.out.println("REMOVAL ID....." + id);
			final DeleteQuery remove = new DeleteQuery();
			remove.setTable("taxonKeys");
			remove.constrain(new QComparisonConstraint(new CanonicalColumnName("taxonKeys", "NODE_ID"),
					QConstraint.CT_EQUALS, id));
			try {
				System.out.println(remove.getSQL(ec.getDBSession()));
				ec.doUpdate(remove);
				System.out.println("NODE " + id + " removed from search database");
			} catch (DBException dbx) {
			}

			DeleteQuery dsql = new DeleteQuery();
			dsql.setTable("commonNames");
			dsql.constrain(new CanonicalColumnName("commonNames", "NODE_ID"), QConstraint.CT_EQUALS, id);
			try {
				ec.doUpdate(dsql);
			} catch (final DBException e) {
				e.printStackTrace();
			}

			// vfs.move(url, url.replace("/browse", "/trash"));

			TaxonomyDocUtils.removeTaxonFromHierarchy(node.getId(), kingdom, node.generateFullName());
			//
		} catch (Exception e) {
			SysDebugger.getInstance().println("Could not find Taxon " + id);
			e.printStackTrace();
			response.setStatus(Status.SUCCESS_OK);
		}
	}

	/**
	 * For demotions, a species is being demoted (Demoted1) to infrarank. The
	 * user selects a species for which the species will be the new parent of
	 * Demoted1. The children of Demoted1. The subpopulations of Demoted1 is
	 * moved to the status of an infrarank subpopulation. If the species had
	 * infrarank, the species can not be demoted until the infrarank has already
	 * been moved out.
	 * 
	 * when demoting a -something to a sub-something, the sub-something gets a
	 * synonym pointing to the demoted -something
	 * 
	 * @param documentElement
	 * @param request
	 * @param response
	 */
	private void demoteNode(Element documentElement, Request request, final Response response) {
		ArrayList<TaxonNode> nodesToWrite = new ArrayList<TaxonNode>();
		ArrayList<TaxonNode> nodesWithDocChanges = new ArrayList<TaxonNode>();
		ArrayList<AssessmentData> assessmentsChanged = new ArrayList<AssessmentData>();

		Element demoted = (Element) documentElement.getElementsByTagName("demoted").item(0);
		String id = demoted.getAttribute("id");
		String newParentID = demoted.getTextContent();
		HashMap<Long, String> idsToOldFullNames = new HashMap<Long, String>();

		// CHECK TO MAKE SURE PRECONDITIONS ARE VALID
		if (!id.equalsIgnoreCase("") && vfs.exists(ServerPaths.getURLForTaxa(id)) && !newParentID.equalsIgnoreCase("")
				&& vfs.exists(ServerPaths.getURLForTaxa(newParentID))) {

			TaxonNode node = TaxonNodeFactory.createNode(DocumentUtils.getVFSFileAsString(
					ServerPaths.getURLForTaxa(id), vfs), null, false);
			// TODO: DECIDE IF NEEDED
			// if (!node.isDeprecatedStatus())

			TaxonNode parentNode = TaxonNodeFactory.createNode(DocumentUtils.getVFSFileAsString(ServerPaths
					.getURLForTaxa(newParentID), vfs), null, false);
			if (node.getLevel() == TaxonNode.SPECIES && parentNode.getLevel() == TaxonNode.SPECIES) {

				// SET A synonym TO THE OLD NODE LEVEL
				HashMap<String, String> authorities = new HashMap<String, String>();
				authorities.put(TaxonNode.SPECIES + "", node.getTaxonomicAuthority());
				SynonymData synonym = TaxonNodeFactory.synonymizeNode(node);
				node.addSynonym(synonym);

				// Get old full name
				String oldFullName = node.generateFullName();
				String formattedOldName = generateRLTextFormattedName(node);
				idsToOldFullNames.put(new Long(node.getId()), oldFullName);

				// TODO: NEED TO GET LEVEL SEND IN WITH NODE, MAYBE DEMOTING
				// SOMETHING ELSE
				node.setLevel(TaxonNode.INFRARANK);
				node.setInfraType(TaxonNode.INFRARANK_TYPE_SUBSPECIES);

				// SET NEW INFORMATION
				String[] newFootprint = (parentNode.getFootprintCSV() + "," + parentNode.getName()).split(",");
				node.setFootprint(newFootprint);
				node.setParentId(parentNode.getId() + "");
				node.setParentName(parentNode.getFullName());

				// ADD NODE TO THE CHANGING NAMES
				node.setFullName(getNewFullName(node));
				nodesToWrite.add(node);
				nodesWithDocChanges.add(node);

				// Update published assessment's RL History Text, if needed
				updateAssessmentsOnNameChange(assessmentsChanged, node, oldFullName, formattedOldName, node
						.getFullName());

				boolean success = true;
				String[] children;
				String unsplit = TaxonomyDocUtils.getChildrenTaxaByID(id);
				if (unsplit.contains(","))
					children = unsplit.split(",");
				else if (unsplit != null && !unsplit.equals(""))
					children = new String[] { unsplit };
				else
					children = new String[0];

				for (int i = 0; i < children.length && success; i++) {
					if (!children[i].equalsIgnoreCase("")) {
						String url = ServerPaths.getURLForTaxa(children[i]);
						if (vfs.exists(url)) {
							TaxonNode child = TaxonNodeFactory.createNode(DocumentUtils.getVFSFileAsString(url, vfs),
									null, false);
							String oldName = child.generateFullName();
							String oldFormattedName = generateRLTextFormattedName(child);
							idsToOldFullNames.put(new Long(child.getId()), oldName);
							child.setLevel(TaxonNode.INFRARANK_SUBPOPULATION);
							String[] footprint = (node.getFootprintCSV() + "," + node.getName()).split(",");
							child.setFootprint(footprint);
							child.setFullName(getNewFullName(child));
							nodesToWrite.add(child);
							nodesWithDocChanges.add(child);

							updateAssessmentsOnNameChange(assessmentsChanged, child, oldName, oldFormattedName, child
									.getFullName());
						} else {
							success = false;
						}
					}
				}

				if (success) {
					String xml = TaxaIO.writeNodesAndDocument(nodesToWrite, nodesWithDocChanges, idsToOldFullNames,
							vfs, false);

					if (assessmentsChanged.size() > 0)
						AssessmentIO.writeAssessments(assessmentsChanged, request.getChallengeResponse()
								.getIdentifier(), vfs, true);

					if (xml == null) {
						response.setStatus(Status.CLIENT_ERROR_LOCKED);
					} else {
						TaxomaticOperationTracker tracker = new TaxomaticOperationTracker((VersionedFileVFS) vfs);
						tracker.recordLastUpdatedWithNodes(nodesToWrite, nodesWithDocChanges, request
								.getChallengeResponse().getIdentifier(), "demote");

						response.setEntity(xml, MediaType.TEXT_XML);
						response.setStatus(Status.SUCCESS_OK);
					}
				} else {
					response.setStatus(Status.SERVER_ERROR_INTERNAL);
				}

			} else {
				response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			}

		} else {
			response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
		}
	}

	private long doAddNewTaxon(Element newTaxon) throws IOException, ConflictException, NotFoundException {

		TaxonNode taxon = TaxonNodeFactory.createNode(DocumentUtils.serializeNodeToString(newTaxon), null, false);

		String parentID = taxon.getParentId();
		String parentName = taxon.getParentName();
		String name = taxon.getName();
		String fullName = taxon.generateFullName();
		long id = getNewTaxonID();
		newTaxon.setAttribute("id", "" + id);
		taxon.setId(id);

		// Check to see if this node somehow already exists, if someone didn't
		// do their
		// homework before calling this function.
		String kingdom = taxon.getFootprint().length > 0 ? taxon.getFootprint()[0] : fullName;
		String extantID = TaxonomyDocUtils.getIDByName(kingdom, fullName);
		
		if (extantID == null || extantID.equals("")) {
			// WRITE taxon to fs
			TaxaIO.writeNode(taxon, vfs);

			// ADD TAXON TO HIERARCHY THEN WRITE BACK
			TaxonomyDocUtils
					.addTaxonToHierarchy(id, name, fullName, parentID, parentName, vfs, kingdom);
			return id;
		} else {
			System.out.println("Pre-screening for taxon " + fullName + " in kingdom " + kingdom
					+ " found a collision. ID is " + extantID);
			return -1;
		}
	}

	private long findHighestID(VFSPathToken[] tokens, VFSPath root, long currentHighest) throws NotFoundException {
		long localHighest = currentHighest;
		for (int i = 0; i < tokens.length; i++) {
			VFSPathToken curToken = tokens[i];
			if (vfs.isCollection(root.child(curToken))) {
				VFSPath newRoot = root.child(curToken);
				localHighest = findHighestID(vfs.list(newRoot), newRoot, localHighest);
			} else {
				if (curToken.toString().endsWith(".xml")) {
					try {
						long thisOne = Long.valueOf(curToken.toString().replaceAll(".xml", ""));
						localHighest = Math.max(localHighest, thisOne);
					} catch (Exception e) {
						e.printStackTrace();
						System.out.println("Error parsing ID out of token: " + curToken.toString());
					}
				}
			}
		}

		return localHighest;
	}

	private String generateRLTextFormattedName(TaxonNode taxon) {
		String fullName = "<i>";
		boolean isPlant = taxon.getFootprint().length >= 1 ? taxon.getFootprint()[0].equalsIgnoreCase("plantae")
				: false;

		for (int i = 5; i < (taxon.getLevel() >= TaxonNode.SUBPOPULATION ? taxon.getLevel() - 1 : taxon.getLevel()); i++)
			fullName += taxon.getFootprint()[i] + " ";

		if (taxon.getInfrarankType() == TaxonNode.INFRARANK_TYPE_SUBSPECIES)
			fullName += isPlant ? "subsp." : "ssp. ";
		if (taxon.getInfrarankType() == TaxonNode.INFRARANK_TYPE_VARIETY)
			fullName += "var. ";

		if (taxon.getLevel() == TaxonNode.SUBPOPULATION || taxon.getLevel() == TaxonNode.INFRARANK_SUBPOPULATION)
			fullName += "</i>" + taxon.getName().replace("ssp.", "").replace("var.", "").trim();
		else
			fullName += taxon.getName().replace("ssp.", "").replace("var.", "").trim() + "</i>";

		if (isPlant) {
			fullName = fullName.replace("subsp.", "</i> subsp. <i>");
			fullName = fullName.replace("var.", "</i> var. <i>");
		}

		return fullName;
	}

	/**
	 * Gets the last taxomatic operation if it is the correct user, or returns
	 * bad request if the user is not able to do an undo.
	 * 
	 * @param request
	 * @param response
	 */
	public void getLastTaxomaticOperation(Request request, Response response) {
		String username = request.getChallengeResponse().getIdentifier();
		TaxomaticOperationTracker tracker = new TaxomaticOperationTracker(((VersionedFileVFS) vfs));
		String lastUser = tracker.getLastTaxomaticUser();
		if (username.equalsIgnoreCase(lastUser) || username.equalsIgnoreCase("admin")) {
			Collection<String> filenames = tracker.getLastTaxomaticTaxaNames();
			StringBuilder files = new StringBuilder();
			for (String file : filenames) {
				files.append(file + ", ");
			}
			if (filenames.size() > 0) {
				files.replace(files.length() - 2, files.length(), "");
			} else
				files.append("none (no taxa was affected)");
			String lastOperation = tracker.getLastTaxomaticOperation();
			response.setStatus(Status.SUCCESS_OK);
			response.setEntity("A " + lastOperation
					+ " was the last taxomatic operation that was performed, which affected taxa " + files.toString()
					+ ".", MediaType.TEXT_PLAIN);
		} else {
			response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
		}

	}

	private String getNewFullName(TaxonNode node) {
		return node.generateFullName();
	}

	private synchronized long getNewTaxonID() throws IOException, ConflictException, NotFoundException {
		currentHighestTaxonID.getAndIncrement();
		DocumentUtils.writeVFSFile("/browse/taxonomy/taxonCount", vfs, ""+currentHighestTaxonID.get());
		return currentHighestTaxonID.get();
	}

	private void init() {

		try {
			trashBuffer = new DBTrashBuffer();
			EventLogger.impl.addBuffer(trashBuffer);
		} catch (DBException e) {
		}

		try {
			if (!SISContainerApp.amIOnline()) {
				readInTaxonCount();

				new Thread() {
					public void run() {
						try {
							Date start = new Date();
							VFSPath root = new VFSPath("/browse/nodes");
							VFSPathToken[] tokens = vfs.list(root);

							long highestID = findHighestID(tokens, root, 0);
							currentHighestTaxonID.getAndSet(highestID);

							Date end = new Date();
							System.out.println("Starting on taxon count of " + currentHighestTaxonID + ". Took: "
									+ (end.getTime() - start.getTime()) + "ms.");
						} catch (Exception e) {
							System.out.println("Failed trying to FIND the highest taxon ID on the file system!");
						}
					}
				}.start();
			} else {
				readInTaxonCount();
				System.out.println("Starting on taxon count of " + currentHighestTaxonID);
			}
		} catch (Exception generalFailure) {
			generalFailure.printStackTrace();
			readInTaxonCount();
			System.out.println("Starting on taxon count of " + currentHighestTaxonID);
		}

		DocumentUtils.unversion("/browse/taxonomy/taxonCount", vfs);
		SysDebugger.getInstance().println("Starting on taxon count of " + currentHighestTaxonID);
	}

	/**
	 * For merge the taxomatic is designed so that you select one or more
	 * species ( say Old1 and Old2) to merge into a species (New1, which has
	 * already been previously created). Old1 and Old 2 will then receive a
	 * synonym to New1 and Old1 and Old2 will both be marked as deprecated. None
	 * of the information that was associated with Old1 or Old2 are moved into
	 * New1. New1 will receive deprecated synonyms to both Old1 and Old2.
	 * 
	 * @param documentElement
	 * @param request
	 * @param response
	 */
	private void mergeNodes(Element documentElement, Request request, final Response response) {

		String mergedIDs = "";
		String mainID = "";
		boolean deprecated = false;
		try {
			mergedIDs = documentElement.getElementsByTagName("merged").item(0).getTextContent();
			mainID = documentElement.getElementsByTagName("main").item(0).getTextContent();
		} catch (Exception e) {
			e.printStackTrace();
			response.setStatus(Status.CLIENT_ERROR_EXPECTATION_FAILED);
		}
		SysDebugger.getInstance().println("This is mergedIDs " + mergedIDs);
		SysDebugger.getInstance().println("This is mainID " + mainID);
		
		HashMap<String, TaxonNode> nodes = new HashMap<String, TaxonNode>();
		String [] ids = (mergedIDs + "," + mainID).split(",");
		List<TaxonNode> taxa = TaxaIO.readNodes(ids, false, vfs);
		for( TaxonNode cur : taxa )
			nodes.put(cur.getId()+"", cur);
		
		HashMap<Long, String> idsToOldFullNames = new HashMap<Long, String>();
		ArrayList<TaxonNode> nodeList = new ArrayList<TaxonNode>();
		ArrayList<TaxonNode> nodesWithDocChanges = new ArrayList<TaxonNode>();

		String[] merged = mergedIDs.split(",");
		TaxonNode main = nodes.get(mainID);

		// if (!main.isDeprecatedStatus())
		// {
		nodeList.add(main);
		// Don't add the main to doc changes, because it doesn't need to be
		// moved

		try {
			for (int i = 0; i < merged.length && !deprecated; i++) {
				TaxonNode mergedNode = nodes.get(merged[i]);
				mergedNode.setStatus(TaxonNode.STATUS_SYNONYM);

				SynonymData synonym = TaxonNodeFactory.synonymizeNode(mergedNode);
				synonym.setStatus("MERGE");
				if (mergedNode.getTaxonomicAuthority() != null) {
					synonym.setAuthority(mergedNode.getTaxonomicAuthority(), mergedNode.getLevel());
				}
				main.addSynonym(synonym);

				synonym = TaxonNodeFactory.synonymizeNode(main);
				synonym.setStatus("MERGE");
				if (mergedNode.getTaxonomicAuthority() != null) {
					synonym.setAuthority(mergedNode.getTaxonomicAuthority(), mergedNode.getLevel());
				}
				mergedNode.addSynonym(synonym);

				// TODO: Make this invoke moveNode(...) instead of
				// moveChildren(...)
				// for each child of mergedNode
				// pass it into TaxonomaticHelper.moveNode();
				ArrayList<TaxonNode> children = TaxomaticHelper.moveChildren(mergedNode, main, vfs, true);

				if (children == null)
					return;

				for (TaxonNode child : children) {
					String oldFullName = child.getFullName();
					String newFullName = getNewFullName(child);
					idsToOldFullNames.put(new Long(child.getId()), oldFullName);
					child.setFullName(newFullName);

					// DOES THIS IN TaxomaticHelper.moveChildren(...) NOW! CAN
					// DELETE THIS AFTER TESTING
					// // CREATE SYNONYM FOR NODE IF THE NAMES AREN'T EQUAL
					// if (!oldFullName.equals(newFullName)) {
					// synonym = new SynonymData(oldFullName, child.getLevel(),
					// child.getId() + "");
					// if (child.getTaxonomicAuthority() != null) {
					// synonym.setAuthority(child.getTaxonomicAuthority(),
					// child.getLevel());
					// }
					// child.addSynonym(synonym);
					// }
				}
				nodeList.addAll(children);
				nodesWithDocChanges.addAll(children);
				nodeList.add(mergedNode);
				nodesWithDocChanges.add(mergedNode);
			}

			if (!deprecated) {
				String xml = TaxaIO.writeNodesAndDocument(nodeList, nodesWithDocChanges, idsToOldFullNames, vfs, false);
				if (xml == null) {
					response.setStatus(Status.CLIENT_ERROR_LOCKED);
				} else {
					TaxomaticOperationTracker tracker = new TaxomaticOperationTracker((VersionedFileVFS) vfs);
					tracker.recordLastUpdatedWithNodes(nodeList, nodesWithDocChanges, request.getChallengeResponse()
							.getIdentifier(), "merge");
					response.setEntity(xml, MediaType.TEXT_XML);
					response.setStatus(Status.SUCCESS_OK);
				}
			} else {
				SysDebugger.getInstance().println("A node is deprecated. Bailing.");
				response.setStatus(Status.CLIENT_ERROR_PRECONDITION_FAILED);
			}
		} catch (NullPointerException notHappenIfDocumentSentByClientOK) {
			notHappenIfDocumentSentByClientOK.printStackTrace();
			response.setStatus(Status.CLIENT_ERROR_EXPECTATION_FAILED);
		}

	}

	/**
	 * Given a document which holds the speciesID and the infranks ids, merges
	 * the infraranks into the species and moves the suppopulations of the
	 * infraranks to be suppopulations of the species. Places a synonym in the
	 * infrarank, and in the suppopulatiosn.
	 * 
	 * 
	 * @param documentElement
	 * @param request
	 * @param response
	 */
	private void mergeUpInfraranks(Element documentElement, Request request, final Response response) {

		String mergedIDs = "";
		String mainID = "";
		try {
			mergedIDs = documentElement.getElementsByTagName("infrarank").item(0).getTextContent();
			mainID = documentElement.getElementsByTagName("species").item(0).getTextContent();
		} catch (Exception e) {
			e.printStackTrace();
			response.setStatus(Status.CLIENT_ERROR_EXPECTATION_FAILED);
		}
		SysDebugger.getInstance().println("This is infrarankIDs  " + mergedIDs);
		SysDebugger.getInstance().println("This is mainID " + mainID);

		HashMap<String, TaxonNode> nodes = new HashMap<String, TaxonNode>();
		String [] ids = (mergedIDs + "," + mainID).split(",");
		List<TaxonNode> taxa = TaxaIO.readNodes(ids, false, vfs);
		for( TaxonNode cur : taxa )
			nodes.put(cur.getId()+"", cur);
		
		HashMap<Long, String> idsToOldFullNames = new HashMap<Long, String>();
		ArrayList<TaxonNode> nodeList = new ArrayList<TaxonNode>();
		ArrayList<TaxonNode> nodesWithDocChanges = new ArrayList<TaxonNode>();

		String[] merged = mergedIDs.split(",");
		TaxonNode speciesNode = nodes.get(mainID);

		nodeList.add(speciesNode);
		// Don't add the main to doc changes, because it doesn't need to be
		// moved

		try {
			for (int i = 0; i < merged.length; i++) {
				TaxonNode infrarankNode = nodes.get(merged[i]);
				infrarankNode.setStatus(TaxonNode.STATUS_SYNONYM);

				// ADD INFRARANK SYNONYM TO SPECIES
				// SynonymData synonym = new
				// SynonymData(infrarankNode.getFullName(),
				// infrarankNode.getLevel(),
				// infrarankNode.getId() + "");
				SynonymData synonym = TaxonNodeFactory.synonymizeNode(infrarankNode);
				synonym.setStatus("MERGE");
				if (infrarankNode.getTaxonomicAuthority() != null) {
					synonym.setAuthority(infrarankNode.getTaxonomicAuthority(), infrarankNode.getLevel());
				}
				speciesNode.addSynonym(synonym);

				// ADD SPECIES SYNONYM TO INFRARANK
				// synonym = new SynonymData(speciesNode.getFullName(),
				// speciesNode.getLevel(), speciesNode.getId() + "");
				synonym = TaxonNodeFactory.synonymizeNode(speciesNode);
				if (infrarankNode.getTaxonomicAuthority() != null) {
					synonym.setAuthority(infrarankNode.getTaxonomicAuthority(), infrarankNode.getLevel());
				}
				synonym.setStatus("MERGE");
				infrarankNode.addSynonym(synonym);

				// GET SUBPOPULATIONS OF THE INFRARANK
				ArrayList<TaxonNode> children = TaxomaticHelper.moveChildrenOfInfrarankToSpeciesSubpopulation(
						infrarankNode, speciesNode, vfs);

				if (children == null)
					return;

				// ADD SYNONYMS TO CHILDREN IF NECESSARY
				for (TaxonNode child : children) {
					String oldFullName = child.getFullName();
					String newFullName = getNewFullName(child);

					// CREATE SYNONYM FOR NODE IF THE NAMES AREN'T EQUAL
					if (!oldFullName.equals(newFullName)) {
						synonym = TaxonNodeFactory.synonymizeNode(child);
						if (child.getTaxonomicAuthority() != null) {
							synonym.setAuthority(child.getTaxonomicAuthority(), child.getLevel());
						}
						child.addSynonym(synonym);
					}

					idsToOldFullNames.put(new Long(child.getId()), oldFullName);
					child.setFullName(newFullName);
				}

				// ADD CHANGED NODES TO THE LIST
				nodeList.addAll(children);
				nodesWithDocChanges.addAll(children);
				nodeList.add(infrarankNode);
				// nodesWithDocChanges.add(infrarankNode);
			}

			String xml = TaxaIO.writeNodesAndDocument(nodeList, nodesWithDocChanges, idsToOldFullNames, vfs, false);
			if (xml == null) {
				response.setStatus(Status.CLIENT_ERROR_LOCKED);
			} else {
				TaxomaticOperationTracker tracker = new TaxomaticOperationTracker((VersionedFileVFS) vfs);
				tracker.recordLastUpdatedWithNodes(nodeList, nodesWithDocChanges, request.getChallengeResponse()
						.getIdentifier(), "merge up infrarank");
				response.setEntity(xml, MediaType.TEXT_XML);
				response.setStatus(Status.SUCCESS_OK);
			}

		} catch (NullPointerException notHappenIfDocumentSentByClientOK) {
			notHappenIfDocumentSentByClientOK.printStackTrace();
			response.setStatus(Status.CLIENT_ERROR_EXPECTATION_FAILED);
		}

	}

	/**
	 * Given a document which has the oldNode id and the assessment ids to move,
	 * moves the assessmentID to the new parentID
	 * 
	 * @param documentElement
	 * @param request
	 * @param response
	 */
	private void moveAssessments(final Element documentElement, final Request request, final Response response) {
		final NodeList oldNode = documentElement.getElementsByTagName("oldNode");
		final NodeList nodeToMoveAssessmentsInto = documentElement.getElementsByTagName("nodeToMoveInto");
		final NodeList assessmentNodeList = documentElement.getElementsByTagName("assessmentID");
		final ArrayList<String> aquiredLocks = new ArrayList<String>();
		final HashMap<String, AssessmentData> assessments = new HashMap<String, AssessmentData>();
		boolean failedToAquireLockes = false;

		if (oldNode.getLength() != 1) {
			response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
		} else if (nodeToMoveAssessmentsInto.getLength() != 1) {
			response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
		} else if (assessmentNodeList.getLength() == 0) {
			response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
		} else {
			String oldNodeID = oldNode.item(0).getTextContent();
			String newNodeID = nodeToMoveAssessmentsInto.item(0).getTextContent();
			ArrayList<String> assessmentIDS = new ArrayList<String>();
			StringBuilder ids = new StringBuilder();
			for (Node id : new NodeCollection(assessmentNodeList)) {
				assessmentIDS.add(id.getTextContent());
				ids.append(id.getTextContent() + ",");
			}

			HashMap<String, TaxonNode> nodes = new HashMap<String, TaxonNode>();
			List<TaxonNode> taxa = TaxaIO.readNodes(new String[] { oldNodeID, newNodeID }, false, vfs);
			for( TaxonNode cur : taxa )
				nodes.put(cur.getId()+"", cur);
			
			if (FileLocker.impl.aquireWithRetry(ServerPaths.getURLForTaxa(oldNodeID), 5)) {
				aquiredLocks.add(ServerPaths.getURLForTaxa(oldNodeID));
				if (FileLocker.impl.aquireWithRetry(ServerPaths.getURLForTaxa(newNodeID), 5)) {
					aquiredLocks.add(ServerPaths.getURLForTaxa(newNodeID));
				} else {
					failedToAquireLockes = true;
				}
			} else {
				failedToAquireLockes = true;
			}

			if (!failedToAquireLockes) {
				for (String id : assessmentIDS) {
					AssessmentData assessment = AssessmentIO.readAssessment(vfs, id, BaseAssessment.PUBLISHED_ASSESSMENT_STATUS, null);
					assessment.setSpeciesID(newNodeID);
					assessment.setSpeciesName(nodes.get(newNodeID).generateFullName());
					assessments.put(assessment.getAssessmentID(), assessment);
					nodes.get(oldNodeID).removeAssessment(assessment.getAssessmentID());
					nodes.get(newNodeID).addAssessment(assessment.getAssessmentID());
				}

				for (Entry<String, AssessmentData> entry : assessments.entrySet()) {
					if (FileLocker.impl.aquireWithRetry(ServerPaths.getPublishedAssessmentURL(entry.getKey()), 5))
						aquiredLocks.add(ServerPaths.getPublishedAssessmentURL(entry.getKey()));
					else {
						failedToAquireLockes = true;
						break;
					}
				}

			}

			if (!failedToAquireLockes) {
				List<TaxonNode> nodesToSave = new ArrayList<TaxonNode>();
				nodesToSave.add(nodes.get(newNodeID));
				nodesToSave.add(nodes.get(oldNodeID));
				TaxaIO.writeNodes(nodesToSave, vfs, false);
				AssessmentIO.writeAssessments(new ArrayList<AssessmentData>(assessments.values()), 
						request.getChallengeResponse().getIdentifier(), vfs, false);
				
//				DocumentUtils.writeVFSFile(ServerPaths.getURLForTaxa(newNodeID), vfs, TaxonNodeFactory
//						.nodeToDetailedXML(nodes.get(newNodeID)));
//				DocumentUtils.writeVFSFile(ServerPaths.getURLForTaxa(oldNodeID), vfs, TaxonNodeFactory
//						.nodeToDetailedXML(nodes.get(oldNodeID)));
//
//				for (Entry<String, AssessmentData> entry : assessments.entrySet()) {
//					DocumentUtils.writeVFSFile(ServerPaths.getPublishedAssessmentURL(entry.getKey()), vfs, entry
//							.getValue().toXML());
//				}
				for (String lockURL : aquiredLocks) {
					FileLocker.impl.releaseLock(lockURL);
				}

				response.setStatus(Status.SUCCESS_OK);
			} else {
				response.setStatus(Status.SERVER_ERROR_SERVICE_UNAVAILABLE);
			}
		}
	}

	/**
	 * For the lateral move, (for a species or any other rank) you choose the
	 * new genus (or rank directly higher) where you would like to move the
	 * species to. All of the children of the moved taxa are also moved. Besides
	 * this, the information about the the moved species is unchanged.
	 * 
	 * @param documentElement
	 * @param request
	 * @param response
	 */
	private void moveNodes(Element documentElement, Request request, final Response response) {

		boolean success = true;
		Element parentElement = (Element) documentElement.getElementsByTagName("parent").item(0);
		String parentID = parentElement.getAttribute("id");
		final ArrayList<AssessmentData> assessmentsToWrite = new ArrayList<AssessmentData>();
		final ArrayList<TaxonNode> nodesToWrite = new ArrayList<TaxonNode>();
		final ArrayList<TaxonNode> nodesWithDocChanges = new ArrayList<TaxonNode>();
		HashMap<Long, String> idsToOldFullName = new HashMap<Long, String>();
		final TaxonNode parentNode;

		if (vfs.exists(ServerPaths.getURLForTaxa(parentID))) {
			parentNode = TaxonNodeFactory.createNode(DocumentUtils.getVFSFileAsString(ServerPaths
					.getURLForTaxa(parentID), vfs), null, false);
			NodeList newChildren = documentElement.getElementsByTagName("child");

			for (int i = 0; i < newChildren.getLength() && success; i++) {
				String id = ((Element) newChildren.item(i)).getAttribute("id");
				if (vfs.exists(ServerPaths.getURLForTaxa(id))) {
					TaxonNode childNode = TaxonNodeFactory.createNode(DocumentUtils.getVFSFileAsString(ServerPaths
							.getURLForTaxa(id), vfs), null, false);
					ArrayList<TaxonNode> returnedNodes = TaxomaticHelper.moveNode(parentNode, childNode, vfs, true);

					if (returnedNodes == null) {
						System.out.println("Null returned on moveNode, meaning bad levels. " + "Body of move request: "
								+ DocumentUtils.serializeNodeToString(documentElement));
						response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
						return;
					}

					for (TaxonNode n : returnedNodes) {
						TaxonNode oldNode = TaxaIO.readNode(n.getId()+"", vfs);
						if( oldNode != null ) {
							String oldFullName = oldNode.getFullName();
							String formattedOldFullName = generateRLTextFormattedName(oldNode);

							String newFullName = getNewFullName(n);
							idsToOldFullName.put(new Long(n.getId()), oldFullName);
							n.setFullName(newFullName);

							updateAssessmentsOnNameChange(assessmentsToWrite, n, oldFullName, formattedOldFullName,
									newFullName);
						} else {
							System.out.println("Could not update RL history text for taxon " + n.getId());
						}
					}
					nodesToWrite.addAll(returnedNodes);
					nodesWithDocChanges.addAll(returnedNodes);
				} else {
					success = false;
				}
			}
		} else {
			success = false;
		}

		if (success) {
			// True for failOnDupeFound, as it's possible we're moving it back
			String xml = TaxaIO.writeNodesAndDocument(nodesToWrite, nodesWithDocChanges, idsToOldFullName, vfs, true);
			AssessmentIO.writeAssessments(assessmentsToWrite, request.getChallengeResponse().getIdentifier(), vfs, true);

			if (xml == null) {
				response.setStatus(Status.CLIENT_ERROR_LOCKED);
			} else {
				TaxomaticOperationTracker tracker = new TaxomaticOperationTracker((VersionedFileVFS) vfs);
				tracker.recordLastUpdatedWithNodes(nodesToWrite, nodesWithDocChanges, request.getChallengeResponse()
						.getIdentifier(), "lateral move");
				response.setEntity(xml, MediaType.TEXT_XML);
				response.setStatus(Status.SUCCESS_OK);
			}
		} else {
			response.setStatus(Status.SERVER_ERROR_INTERNAL);
		}

	}

	public void performService(Request request, Response response) {
		String operation = (String) request.getAttributes().get("operation");
		Method method = request.getMethod();

		boolean acquired = lock.attempt();
		
		try {
			if( !acquired ) {
				response.setStatus(Status.CLIENT_ERROR_LOCKED);
			} else if (operation.equalsIgnoreCase("new") && method.equals(Method.PUT)) {
				long id = putNewTaxon(new DomRepresentation(request.getEntity()).getDocument().getDocumentElement(), request, response);
				if (id > 0) {
					response.setEntity(id + "", MediaType.TEXT_PLAIN);
					response.setStatus(Status.SUCCESS_OK);
				}
			} else if (operation.equalsIgnoreCase("batch") && method.equals(Method.PUT)) {
				String ids = putBatch(request, response);

				// Put batch would have returned null and handled the response
				// if a
				// failure occurred.
				if (ids != null) {
					response.setEntity(ids, MediaType.TEXT_PLAIN);
					response.setStatus(Status.SUCCESS_OK);
				}
			} else if (operation.equalsIgnoreCase("merge") && method.equals(Method.POST)) {
				mergeNodes(new DomRepresentation(request.getEntity()).getDocument().getDocumentElement(), request, response);
			} else if (operation.equalsIgnoreCase("moveAssessments") && method.equals(Method.POST)) {
				moveAssessments(new DomRepresentation(request.getEntity()).getDocument().getDocumentElement(), request, response);
			} else if (operation.equalsIgnoreCase("mergeupinfrarank") && method.equals(Method.POST)) {
				mergeUpInfraranks(new DomRepresentation(request.getEntity()).getDocument().getDocumentElement(), request, response);
			} else if (operation.equalsIgnoreCase("split") && method.equals(Method.POST)) {
				splitNodes(new DomRepresentation(request.getEntity()).getDocument().getDocumentElement(), request, response);
			} else if (operation.equalsIgnoreCase("move") && method.equals(Method.POST)) {
				moveNodes(new DomRepresentation(request.getEntity()).getDocument().getDocumentElement(), request, response);
			} else if (operation.equalsIgnoreCase("promote") && method.equals(Method.POST)) {
				promoteNode(new DomRepresentation(request.getEntity()).getDocument().getDocumentElement(), request, response);
			} else if (operation.equalsIgnoreCase("demote") && method.equals(Method.POST)) {
				demoteNode(new DomRepresentation(request.getEntity()).getDocument().getDocumentElement(), request, response);
			} else if (operation.equalsIgnoreCase("undo") && method.equals(Method.POST)) {
				undoLastTaxomaticOperation(request, response);
			} else if (operation.equalsIgnoreCase("undo") && method.equals(Method.GET)) {
				getLastTaxomaticOperation(request, response);
			} else if (method.equals(Method.PUT)) {
				long id = putNumberedTaxon(operation, new DomRepresentation(request.getEntity()).getDocument().getDocumentElement(),
						request, response);
				response.setEntity(id + "", MediaType.TEXT_PLAIN);
				response.setStatus(Status.SUCCESS_OK);
			} else if (method.equals(Method.DELETE)) {
				String id = operation;
				deleteNode(operation, request, response);
			} else
				response.setStatus(Status.SERVER_ERROR_NOT_IMPLEMENTED);
			
			if( acquired )
				lock.release();
			
		} catch (Exception e) {
			if( acquired )
				lock.release();
			
			e.printStackTrace();
			response.setStatus(Status.SERVER_ERROR_INTERNAL, DocumentUtils.getStackTraceAsString(e));
			response.setEntity("<pre>" + DocumentUtils.getStackTraceAsString(e) + "</pre>", MediaType.TEXT_HTML);
		}
	}

	/**
	 * For promotions an infrank is being moved into a species. The infrarank
	 * will just be promoted to be "brothers" with its previous parent, and all
	 * of the subpopulations of the infrarank are moved to be subpopulations of
	 * the newly promoted species. Besides this, the information about the the
	 * promoted species is unchanged.
	 * 
	 * if you promote a sub-something to a -something, the -something isn't
	 * getting a synonym pointing to the promoted sub-something. Check to make
	 * sure it puts the authority in the synonym.
	 * 
	 * @param documentElement
	 * @param request
	 * @param response
	 */
	private void promoteNode(Element documentElement, Request request, final Response response) {
		ArrayList<TaxonNode> nodesToWrite = new ArrayList<TaxonNode>();
		ArrayList<TaxonNode> nodesWithDocChanges = new ArrayList<TaxonNode>();
		ArrayList<AssessmentData> assessmentsChanged = new ArrayList<AssessmentData>();

		Element promoted = (Element) documentElement.getElementsByTagName("promoted").item(0);
		String id = promoted.getAttribute("id");
		HashMap<Long, String> idsToFullName = new HashMap<Long, String>();

		if (!id.equalsIgnoreCase("") && vfs.exists(ServerPaths.getURLForTaxa(id))) {
			TaxonNode node = TaxonNodeFactory.createNode(DocumentUtils.getVFSFileAsString(
					ServerPaths.getURLForTaxa(id), vfs), null, false);

			if (node.getLevel() == TaxonNode.INFRARANK) {
				// 	TODO: DECIDE IF NEEDEDs
				// && !node.isDeprecatedStatus())
				TaxonNode sisterNode = TaxaIO.readNode(node.getParentId(), vfs);

				// SET A synonym TO THE OLD NODE LEVEL
				HashMap<String, String> authorities = new HashMap<String, String>();
				authorities.put(TaxonNode.INFRARANK + "", node.getTaxonomicAuthority());
				// SynonymData synonym = new SynonymData(node.getFullName(),
				// node.getLevel(), authorities, node.getId()
				// + "");
				SynonymData synonym = TaxonNodeFactory.synonymizeNode(node);
				node.addSynonym(synonym);

				String oldFullName = node.getFullName();
				String oldFormattedName = generateRLTextFormattedName(node);

				// SET THE LEVEL OF THE NODE
				node.setLevel(TaxonNode.SPECIES);
				node.setInfraType(TaxonNode.INFRARANK_TYPE_NA);

				// SET THE NEW FOOTPRINT OF THE NODE
				String[] tmp = new String[sisterNode.getFootprint().length];
				System.arraycopy(sisterNode.getFootprint(), 0, tmp, 0, sisterNode.getFootprint().length);
				node.setFootprint(tmp);

				// SET THE NEW PARENT NAME
				node.setParentName(sisterNode.getParentName());
				node.setParentId(sisterNode.getParentId());

				// SET THE NEED TO WRITE THE NODE BACK
				nodesToWrite.add(node);
				nodesWithDocChanges.add(node);

				idsToFullName.put(new Long(node.getId()), oldFullName);

				// SET THE FULL NAME
				node.setFullName(getNewFullName(node));

				// Update published assessments' RL History text, if needed
				updateAssessmentsOnNameChange(assessmentsChanged, node, oldFullName, oldFormattedName, node
						.getFullName());

				boolean success = true;
				String[] children = TaxonomyDocUtils.getChildrenTaxaByID(id).split(",");
				for (int i = 0; i < children.length && success; i++) {
					if (!children[i].equalsIgnoreCase("")) {
						String url = ServerPaths.getURLForTaxa(children[i]);
						if (vfs.exists(url)) {
							TaxonNode child = TaxonNodeFactory.createNode(DocumentUtils.getVFSFileAsString(url, vfs),
									null, false);
							child.setLevel(TaxonNode.SUBPOPULATION);
							child.setFootprintAtLevel(TaxonNode.SPECIES, node.getName());
							child.setFootprintAtLevel(TaxonNode.INFRARANK, "");

							oldFullName = child.getFullName();
							oldFormattedName = generateRLTextFormattedName(child);

							idsToFullName.put(new Long(child.getId()), oldFullName);
							child.setFullName(getNewFullName(child));
							nodesToWrite.add(child);
							nodesWithDocChanges.add(child);

							updateAssessmentsOnNameChange(assessmentsChanged, child, oldFullName, oldFormattedName,
									child.getFullName());
						} else {
							success = false;
						}
					}
				}

				if (success) {
					String xml = TaxaIO.writeNodesAndDocument(nodesToWrite, nodesWithDocChanges, idsToFullName, vfs,
							false);

					if (assessmentsChanged.size() > 0)
						AssessmentIO.writeAssessments(assessmentsChanged, request.getChallengeResponse()
								.getIdentifier(), vfs, true);

					if (xml == null) {
						response.setStatus(Status.CLIENT_ERROR_LOCKED);
					} else {
						TaxomaticOperationTracker tracker = new TaxomaticOperationTracker((VersionedFileVFS) vfs);
						tracker.recordLastUpdatedWithNodes(nodesToWrite, nodesWithDocChanges, request
								.getChallengeResponse().getIdentifier(), "promote");
						response.setEntity(xml, MediaType.TEXT_XML);
						response.setStatus(Status.SUCCESS_OK);
					}
				} else {
					response.setStatus(Status.SERVER_ERROR_INTERNAL);
				}

			}
			// TODO: DECIDE IF NEEDED
			// else if (node.isDeprecatedStatus())
			// {
			// response.setStatus(Status.CLIENT_ERROR_PRECONDITION_FAILED);
			// }
			else {
				response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			}
		} else {
			response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
		}

	}

	/**
	 * Return CSV list of IDs
	 * 
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	private String putBatch(Request request, Response response) throws Exception {
		Document newTaxa = null;
		String taxaAsString = request.getEntity().getText();

		newTaxa = DocumentUtils.createDocumentFromString(taxaAsString);

		if (newTaxa == null) {
			Writer writer = vfs.getWriter("/browse/nodes/errors.log");
			writer.write("I was trying to get: \r\n" + taxaAsString);
			writer.close();

			throw new Exception("Error...");
		}

		NodeCollection list = new NodeCollection(newTaxa.getDocumentElement().getChildNodes());

		String ids = "";

		for (Iterator<Node> iter = list.listIterator(); iter.hasNext();) {
			Node curNode = iter.next();

			if (curNode.getNodeType() == Node.ELEMENT_NODE) {
				Element cur = (Element) curNode;
				String curID = cur.getAttribute("id");

				if (curID.equalsIgnoreCase("")) {
					SysDebugger.getInstance().println(
							"This is what I'm working with: " + DocumentUtils.serializeNodeToString(cur));
					throw new Exception("Crap.");
				}
				if (curID.equalsIgnoreCase("-1")) {
					long newID = putNewTaxon(cur, request, response);
					if (newID > 0)
						ids += newID + ",";
					else {
						if (ids.endsWith(","))
							ids = ids.substring(0, ids.length() - 1);

						response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "One of these taxa already exists.");
						response.setEntity(ids, MediaType.TEXT_PLAIN);

						return null;
					}
				} else
					ids += putNumberedTaxon(curID, cur, request, response) + ",";
			}
		}

		if (ids.endsWith(","))
			ids = ids.substring(0, ids.length() - 1);

		return ids;
	}

	private long putNewTaxon(Element newTaxon, Request request, Response response) throws Exception {
		long id = doAddNewTaxon(newTaxon);

		if (id > 0) {
			response.setEntity(id + "", MediaType.TEXT_PLAIN);
			response.setStatus(Status.SUCCESS_OK);
		}

		else {
			response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
		}

		return id;
	}

	private long putNumberedTaxon(String number, Element newTaxon, Request request, Response response) throws Exception {
		TaxonNode taxon = TaxonNodeFactory.createNode(DocumentUtils.serializeNodeToString(newTaxon), null, false);

		String parentID = taxon.getParentId();
		String parentName = taxon.getParentName();
		String name = taxon.getName();
		String fullName = taxon.generateFullName();
		int level = taxon.getLevel();

		// WRITE taxon to fs
//		DocumentUtils.writeVFSFile("/browse/nodes/" + FilenameStriper.getIDAsStripedPath(taxon.getId()) + ".xml", vfs,
//				DocumentUtils.serializeNodeToString(newTaxon));
		TaxaIO.writeNode(taxon, vfs);

		// ADD TAXON TO HIERARCHY THEN WRITE BACK
		try {
			TaxonomyDocUtils.addTaxonToHierarchy(taxon.getId(), name, fullName, parentID, parentName, vfs, taxon
					.getFootprint().length > 0 ? taxon.getFootprint()[0] : name);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error in call: TaxonomyDocUtils.addTaxonToHierarchy( " + taxon.getId() + ", " + name
					+ ", " + fullName + ", " + parentID + ", " + parentName + ", " + vfs + ")");
			System.out.println("Causing taxon: " + DocumentUtils.serializeNodeToString(newTaxon));
		}

		response.setEntity(taxon.getId() + "", MediaType.TEXT_PLAIN);
		response.setStatus(Status.SUCCESS_OK);

		return taxon.getId();
	}

	private void readInTaxonCount() {
		String curID = "0";
		if (vfs.exists("/browse/taxonomy/taxonCount")) {
			curID = DocumentUtils.getVFSFileAsString("/browse/taxonomy/taxonCount", vfs);
		}

		long readHighestTaxonID = Long.parseLong(curID.replaceAll("\\D", ""));
		if (readHighestTaxonID > currentHighestTaxonID.get()) {
			currentHighestTaxonID.getAndSet(readHighestTaxonID);
		}
	}

	/**
	 * For split the taxomatic is designed so that you select the node (Old1)
	 * that you would like to split. You are then prompted to create new taxa
	 * which you would like to split the node into (at least 2, lets say New1,
	 * New2). For each child of Old1, you can choose to place it under either
	 * New1 or in New2. Old1 is deprecated, and has two new Synonyms linking to
	 * New1 and New2. New1 and New2 receive the children that was selected for
	 * them, and they both receive a deprecated synonym to Old1. None of the
	 * assessments are transferred from Old1.
	 * 
	 * @param documentElement
	 * @param request
	 * @param response
	 */
	private void splitNodes(Element documentElement, Request request, final Response response) {
		boolean success = true;
		boolean deprecated = false;
		Element originalNode = (Element) documentElement.getElementsByTagName("current").item(0);
		NodeList newElements = documentElement.getElementsByTagName("parent");

		ArrayList<TaxonNode> nodesToWrite = new ArrayList<TaxonNode>();
		ArrayList<TaxonNode> nodesWithDocChanges = new ArrayList<TaxonNode>();

		HashMap<Long, String> idToOldFullName = new HashMap<Long, String>();
		String oldId = originalNode.getTextContent();
		TaxonNode oldNode = null;
		HashMap<TaxonNode, ArrayList<TaxonNode>> parentNodes = new HashMap<TaxonNode, ArrayList<TaxonNode>>();
		ArrayList<String> idsMentioned = new ArrayList<String>();

		// CHANGE INFORMATION ABOUT ORIGNAL NODE
		if (oldId != null && !oldId.equalsIgnoreCase("") && vfs.exists(ServerPaths.getURLForTaxa(oldId))) {
			oldNode = TaxonNodeFactory.createNode(DocumentUtils.getVFSFileAsString(ServerPaths.getURLForTaxa(oldId),
					vfs), null, false);

			// DECIDE IF NEED TO CHANGE STATUS ON DEPRECATION
			if (oldNode.getLevel() >= TaxonNode.SPECIES) {
				oldNode.setStatus(TaxonNode.STATUS_SYNONYM);
			}

			nodesToWrite.add(oldNode);
		} else {
			System.out.println("Unable to find oldID " + oldId);
			success = false;
		}

		// DO VALIDATION ON SPLIT
		if (success) {

			if (oldNode.getLevel() >= TaxonNode.SPECIES && newElements.getLength() < 2) {
				success = false;
				System.out.println("the level was >= species and the size of newElements is less than required");
			} else {
				// BUILD MAP OF PARENT AND CHILDREN NODES
				int numberOfChildrenAttachedToParents = 0;
				for (int i = 0; i < newElements.getLength() && success; i++) {
					Element element = (Element) newElements.item(i);
					String id = element.getAttribute("id");
					NodeList children = element.getElementsByTagName("child");
					if (id != null && !id.equalsIgnoreCase("") && vfs.exists(ServerPaths.getURLForTaxa(id))) {
						success = idsMentioned.add(id);
						TaxonNode parent = TaxonNodeFactory.createNode(DocumentUtils.getVFSFileAsString(ServerPaths
								.getURLForTaxa(id), vfs), null, false);
						ArrayList<TaxonNode> childrenNodes = new ArrayList<TaxonNode>();
						for (int j = 0; j < children.getLength() && success; j++) {

							String childid = ((Element) children.item(j)).getTextContent();

							if (childid != null && !childid.equalsIgnoreCase("")
									&& vfs.exists(ServerPaths.getURLForTaxa(childid))) {
								success = idsMentioned.add(childid);
								TaxonNode child = TaxonNodeFactory.createNode(DocumentUtils.getVFSFileAsString(
										ServerPaths.getURLForTaxa(childid), vfs), null, false);
								numberOfChildrenAttachedToParents++;
								childrenNodes.add(child);
							} else {
								System.out.println("unable to find childID " + childid);
								success = false;
							}

						}

						parentNodes.put(parent, childrenNodes);

					} else {
						System.out.println("Unable to find parentid " + id);
						success = false;
					}

				}

				if (oldNode.getLevel() >= TaxonNode.SPECIES) {
					int realNumberOfChildren = 0;
					final NativeDocument ndoc = SISContainerApp.newNativeDocument(request.getChallengeResponse());
					ndoc.get("/browse/taxonomy/" + oldNode.getId(), new GenericCallback<String>() {

						public void onFailure(Throwable caught) {
							// TODO Auto-generated method stub

						}

						public void onSuccess(String result) {

						}
					});

					try {
						realNumberOfChildren = ndoc.getDocumentElement().getElementByTagName("options")
								.getElementsByTagName("option").getLength();

					} catch (NullPointerException e) {
						// TODO: handle exception
					}
					success = realNumberOfChildren == numberOfChildrenAttachedToParents;
				}

			}

		}

		// IF PASSED VALIDATION, DO SPLIT BASED ON PARENTNODES
		Iterator<Entry<TaxonNode, ArrayList<TaxonNode>>> iter = parentNodes.entrySet().iterator();
		while (iter.hasNext() && success) {
			Entry<TaxonNode, ArrayList<TaxonNode>> entry = iter.next();
			TaxonNode parent = entry.getKey();
			ArrayList<TaxonNode> children = entry.getValue();

			// CREATE SYNONYM
			SynonymData synonym = TaxonNodeFactory.synonymizeNode(oldNode);
			if (oldNode.getTaxonomicAuthority() != null) {
				synonym.setAuthority(oldNode.getTaxonomicAuthority(), oldNode.getLevel());
			}
			synonym.setStatus(TaxonNode.STATUS_SYNONYM);
			parent.addSynonym(synonym);
			nodesToWrite.add(parent);

			for (TaxonNode child : children) {
				ArrayList<TaxonNode> nodes = TaxomaticHelper.moveNode(parent, child, vfs, true);

				for (TaxonNode n : nodes) {
					String oldFullName = n.getFullName();
					String newFullName = getNewFullName(n);
					idToOldFullName.put(new Long(n.getId()), oldFullName);
					n.setFullName(newFullName);

					// DONE IN TaxomaticHelper.moveNode(...) now. DELETE WHEN
					// TESTED.
					// // CREATE SYNONYM FOR NODE IF THE NAMES AREN'T EQUAL
					// if (!oldFullName.equals(newFullName)) {
					// synonym = new SynonymData(oldFullName, n.getLevel(),
					// n.getId() + "");
					// if (child.getTaxonomicAuthority() != null) {
					// synonym.setAuthority(child.getTaxonomicAuthority(),
					// child.getLevel());
					// }
					// n.addSynonym(synonym);
					// }

				}
				nodesToWrite.addAll(nodes);
				nodesWithDocChanges.addAll(nodes);
			}

		}

		// DO WRITING OF TAXONOMY DOCUMENT
		if (success) {
			String xml = TaxaIO.writeNodesAndDocument(nodesToWrite, nodesWithDocChanges, idToOldFullName, vfs, false);
			if (xml == null) {
				response.setStatus(Status.CLIENT_ERROR_LOCKED);
			} else {
				TaxomaticOperationTracker tracker = new TaxomaticOperationTracker((VersionedFileVFS) vfs);
				tracker.recordLastUpdatedWithNodes(nodesToWrite, nodesWithDocChanges, request.getChallengeResponse()
						.getIdentifier(), "split taxon");
				System.out.println("could have given nodes with doc changes");
				for (TaxonNode node34 : nodesWithDocChanges) {
					System.out.println(TaxonNodeFactory.nodeToDetailedXML(node34));
				}
				response.setEntity(xml, MediaType.TEXT_XML);
				response.setStatus(Status.SUCCESS_OK);
			}
		} else if (deprecated) {
			response.setStatus(Status.CLIENT_ERROR_PRECONDITION_FAILED);
		} else if (newElements.getLength() < 2) {
			response.setStatus(Status.CLIENT_ERROR_EXPECTATION_FAILED);
		} else {
			response.setStatus(Status.SERVER_ERROR_INTERNAL);
		}

	}

	/**
	 * 
	 * @param docElement
	 * @param request
	 * @param response
	 */
	public void undoLastTaxomaticOperation(Request request, Response response) {
		String username = request.getChallengeResponse().getIdentifier();
		TaxomaticOperationTracker tracker = new TaxomaticOperationTracker(((VersionedFileVFS) vfs));
		boolean success = tracker.performTaxomaticUndo(username);
		if (success) {
			response.setStatus(Status.SUCCESS_OK);
		} else {
			response.setStatus(Status.SERVER_ERROR_INTERNAL);
		}
	}

	private void updateAssessmentsOnNameChange(final ArrayList<AssessmentData> assessmentsToWrite, TaxonNode n,
			String oldFullName, String formattedOldFullName, String newFullName) {
		// SET SYNONYM IF NAME CHANGES
		if (!oldFullName.equals(newFullName)) {
			if (UPDATE_RL_HISTORY_TEXT && n.getAssessments() != null) {
				for (String assID : n.getAssessments()) {
					AssessmentData curAss = updateRLTaxonomyHistory(assID, formattedOldFullName);

					if (curAss != null) {
						curAss.setSpeciesName(n.generateFullName());
						assessmentsToWrite.add(curAss);
					}
				}
			}
		}
	}

	/**
	 * 
	 * @param assessmentID
	 * @param request
	 *            TODO
	 */
	private AssessmentData updateRLTaxonomyHistory(String assessmentID, String asOldName) {
		AssessmentData ass = AssessmentIO.readAssessment(vfs, assessmentID, AssessmentData.PUBLISHED_ASSESSMENT_STATUS,
				"");
		ArrayList<String> crit = (ArrayList<String>) ass.getDataMap().get(CanonicalNames.RedListCriteria);

		if (crit.get(SISCategoryAndCriteria.RLHISTORY_TEXT_INDEX).equals("")) {
			SysDebugger.getInstance().println(
					"Updating assessment " + assessmentID + " to read: as <i>" + asOldName + "</i>");
			crit.set((SISCategoryAndCriteria.RLHISTORY_TEXT_INDEX), XMLUtils.clean("as " + asOldName));
			ass.getDataMap().put(CanonicalNames.RedListCriteria, crit);
		}

		return ass;
	}

}
