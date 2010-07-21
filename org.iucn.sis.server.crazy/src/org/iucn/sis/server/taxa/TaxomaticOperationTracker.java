package org.iucn.sis.server.taxa;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.iucn.sis.server.io.TaxaIO;
import org.iucn.sis.server.locking.FileLocker;
import org.iucn.sis.server.locking.TaxonLockAquirer;
import org.iucn.sis.server.utils.DocumentUtils;
import org.iucn.sis.server.utils.ServerPaths;
import org.iucn.sis.shared.taxonomyTree.TaxonNode;
import org.iucn.sis.shared.taxonomyTree.TaxonNodeFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.solertium.util.NodeCollection;
import com.solertium.vfs.ConflictException;
import com.solertium.vfs.NotFoundException;
import com.solertium.vfs.VFSPath;
import com.solertium.vfs.VFSRevisionUtils;
import com.solertium.vfs.provider.VersionedFileVFS;
import com.solertium.vfs.utils.VFSUtils;
import com.solertium.vfs.utils.VFSUtils.VFSPathParseException;

/**
 * This class records every taxomatic operation after the operation has occured,
 * this tracker also contains helpful functions for undoing an operation
 * 
 * @author liz.schwartz
 * 
 */
public class TaxomaticOperationTracker {

	protected final VersionedFileVFS vfs;
	protected Map<String, String> lastOperationToTimestamp;
	protected Map<String, String> lastOperationToName;
	protected List<String> idsWithDocChanges;
	protected String user = null;
	protected String operation = null;
	protected boolean loaded = false;

	public TaxomaticOperationTracker(VersionedFileVFS vfs) {
		this.vfs = vfs;
	}

	public String getLastTaxomaticOperation() {
		load();
		return operation;
	}

	public Collection<String> getLastTaxomaticTaxaNames() {
		load();
		return lastOperationToName.values();
	}

	public String getLastTaxomaticUser() {
		load();
		return user;
	}

	/**
	 * 
	 * @param idsOfNodesChange
	 * @param username
	 * @param operation
	 * @param vfs
	 * @return
	 * @throws NotFoundException
	 * @throws VFSPathParseException
	 */
	private String getOperationString(Map<String, String> idsOfNodesChangeToFullName, List<String> docChanges,
			String username, String operation) throws NotFoundException, VFSPathParseException {
		StringBuilder xml = new StringBuilder();
		xml.append("<taxomatic>\r\n");
		xml.append("<operation user=\"" + username + "\" >" + operation + "</operation>\r\n");
		for (Entry<String, String> entry : idsOfNodesChangeToFullName.entrySet()) {
			String id = entry.getKey();
			VFSPath taxonPath = VFSUtils.parseVFSPath(ServerPaths.getURLForTaxa(id));
			String lastModified;
			if (vfs.exists(taxonPath))
				lastModified = vfs.getLastModified(taxonPath) + "";
			else
				lastModified = "DELETED";

			String docChange = "";
			if (docChanges.contains(id)) {
				docChange = "docChange=\"true\" ";
			}
			xml.append("<taxa id=\"" + id + "\" lastModified=\"" + lastModified + "\" " + docChange + ">"
					+ entry.getValue() + "</taxa>\r\n");
		}
		xml.append("</taxomatic>");

		return xml.toString();
	}

	protected void load() {
		if (!loaded) {
			try {
				Document operationDoc = vfs.getDocument(VFSUtils.parseVFSPath(ServerPaths
						.getLastTaxomaticOperationPath()));
				lastOperationToName = new HashMap<String, String>();
				lastOperationToTimestamp = new HashMap<String, String>();
				idsWithDocChanges = new ArrayList<String>();

				final Element docElement = operationDoc.getDocumentElement();
				final Element operation = (Element) docElement.getElementsByTagName("operation").item(0);
				final NodeList taxa = docElement.getElementsByTagName("taxa");

				user = operation.getAttribute("user");
				this.operation = operation.getTextContent();

				NodeCollection iter = new NodeCollection(taxa);
				for (Node node : iter) {
					Element element = ((Element) node);
					String id = element.getAttribute("id");
					lastOperationToTimestamp.put(id, element.getAttribute("lastModified"));
					lastOperationToName.put(id, element.getTextContent());
					if (element.hasAttribute("docChange"))
						idsWithDocChanges.add(id);
				}
				loaded = true;
			} catch (VFSPathParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NullPointerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * given a username of the person requesting the undo, tries to undo the
	 * last taxomatic operation, returns boolean of success
	 * 
	 * @param username
	 * @return
	 */
	public boolean performTaxomaticUndo(String username) {
		TaxonLockAquirer lockAquirer = null;
		boolean validated = true;
		boolean success = false;
		final Map<String, String> lastOperation;

		// GATHER OPERTAION, FAST FAIL IF ERROR IN PARSING LAST OPERATION
		// DOCUMENT
		load();
		lastOperation = new HashMap<String, String>(this.lastOperationToTimestamp);
		if (lastOperation == null)
			return false;

		// DO VALIDATION OF USER
		if (!username.equalsIgnoreCase(this.user) && !username.equalsIgnoreCase("admin")) {
			System.out.println("the username is not equal");
			validated = false;
			return false;
		}

		// GATHER LOCKS
		if (validated) {
			lockAquirer = new TaxonLockAquirer(lastOperation.keySet().toArray(new String[lastOperation.size()]));
			lockAquirer.aquireLocks();

			if (!lockAquirer.isSuccess()) {
				System.out.println("the locks were not successfully aquired");
				validated = false;
			} else {
				validated = FileLocker.impl.aquireWithRetry(ServerPaths.getLastTaxomaticOperationPath(), 5);
				System.out.println("aquired lock on taxomaticOperationPath " + validated);
			}

		}

		// DO VALIDATION OF FILES
		if (validated) {

			for (Entry<String, String> entry : lastOperation.entrySet()) {
				String taxaID = entry.getKey();
				String lastModified = entry.getValue();

				if (lastModified.equalsIgnoreCase("DELETE") && vfs.exists(ServerPaths.getURLForTaxa(taxaID))) {

					validated = false;
					System.out.println("validated is false because of lastModified delete");
					break;

				} else {
					try {
						String currentLastModified = vfs.getLastModified(ServerPaths.getURLForTaxa(taxaID)) + "";
						if (!currentLastModified.equals(lastModified)) {
							System.out.println("the currentLastModfied did not equal lastModified");
							validated = false;
							break;
						}
					} catch (NotFoundException e) {
						e.printStackTrace();
						validated = false;
						break;
					}
				}

			}
		}

		// VALIDATION PASSED
		if (validated) {
			System.out.println("trying to do undo");
			success = performUndo(lastOperation.keySet(), idsWithDocChanges);
			System.out.println("finished undo with success " + success);
		}

		// RELEASE LOCKS
		lockAquirer.releaseLocks();
		FileLocker.impl.releaseLock(ServerPaths.getLastTaxomaticOperationPath());

		// RETURN SUCCESSFUL UNDO
		return validated && success;

	}

	/**
	 * After locks have been aquired, and validation has passed, this function
	 * does the guts of the undo.
	 * 
	 * @param ids
	 * @return
	 */
	private boolean performUndo(Set<String> ids, List<String> docChanges) {
		boolean success = true;
		final HashMap<String, TaxonNode> nodesToSave = new HashMap<String, TaxonNode>();
		final HashMap<Long, String> idToOldFullName = new HashMap<Long, String>();

		for (String taxaID : ids) {
			final TaxonNode currentNode = TaxonNodeFactory.createNode(DocumentUtils.getVFSFileAsString(ServerPaths
					.getURLForTaxa(taxaID), vfs), null, false);
			String lastTaxa;
			try {
				lastTaxa = VFSRevisionUtils.getLastUndoString(vfs, VFSUtils.parseVFSPath(ServerPaths
						.getURLForTaxa(taxaID)));
			} catch (VFSPathParseException e) {
				e.printStackTrace();
				return false;
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}

			// IT EXISTS IN UNDO DIRECTORY
			if (lastTaxa != null) {
				final TaxonNode undoNode = TaxonNodeFactory.createNode(lastTaxa, null, false);
				System.out.println("just created undoNode " + undoNode.generateFullName() + " with taxaID " + taxaID);
				nodesToSave.put(taxaID, undoNode);
			}

			// IT DIDN"T EXIST IN UNDO DIRECTORY, MUST HAVE BEEN CREATED LAST
			// OPERATION
			else {
				nodesToSave.put(taxaID, null);
				idToOldFullName.put(new Long(taxaID), currentNode.generateFullName());
			}

		}

		try {
			TaxaIO.writeNodeandDocumentChanges(nodesToSave, idToOldFullName, docChanges, vfs);
			vfs.delete(ServerPaths.getLastTaxomaticOperationPath());
		} catch (NotFoundException e) {
			e.printStackTrace();
		} catch (ConflictException e) {
			e.printStackTrace();
		}

		return success;
	}

	/**
	 * Given a list of ids that have changed, the user who changed the nodes,
	 * and the operation performed, recordes the operation in filesystem.
	 * Returns success of saving file in filesystem based on given information,
	 * if given correct information should never fail unless there is an epic
	 * failure.
	 * 
	 * 
	 * @param idsOfNodesChanged
	 * @param username
	 * @param operation
	 * @return
	 */
	public boolean recordLastUpdated(Map<String, String> idsOfNodesChangedToFullName, List<String> docChanges,
			String username, String operation) {
		boolean success = true;

		final String xml;
		try {
			xml = getOperationString(idsOfNodesChangedToFullName, docChanges, username, operation);
		} catch (NotFoundException e) {
			return false;
		} catch (VFSPathParseException e) {
			return false;
		}

		success = DocumentUtils.writeVFSFile(ServerPaths.getLastTaxomaticOperationPath(), vfs, xml);
		if (!success) {
			try {
				vfs.delete(VFSUtils.parseVFSPath(ServerPaths.getLastTaxomaticOperationPath()));
			} catch (NotFoundException e) {
				System.out.println("Error writing last operation to the lastTaxomaticOperationPath");
				e.printStackTrace();
			} catch (ConflictException e) {
				System.out.println("Error writing last operation to the lastTaxomaticOperationPath");
				e.printStackTrace();
			} catch (VFSPathParseException e) {
				System.out.println("Error writing last operation to the lastTaxomaticOperationPath");
				e.printStackTrace();
			}

		}

		return success;
	}

	public boolean recordLastUpdatedWithNodes(List<TaxonNode> taxaChanged, List<TaxonNode> docChanges, String username,
			String operation) {

		Map<String, String> taxaids = new HashMap<String, String>();
		for (TaxonNode node : taxaChanged) {
			taxaids.put(node.getId() + "", node.getFullName());
		}

		List<String> docChangesString = new ArrayList<String>();
		for (TaxonNode node : docChanges) {
			docChangesString.add(node.getId() + "");
		}

		return recordLastUpdated(taxaids, docChangesString, username, operation);
	}
}
