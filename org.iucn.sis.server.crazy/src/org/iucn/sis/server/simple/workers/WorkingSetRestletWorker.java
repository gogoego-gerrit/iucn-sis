package org.iucn.sis.server.simple.workers;

import org.iucn.sis.server.locking.FileLocker;
import org.iucn.sis.server.utils.DocumentUtils;
import org.iucn.sis.server.utils.FilenameStriper;
import org.iucn.sis.server.utils.FormattedDate;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.solertium.vfs.VFS;

public class WorkingSetRestletWorker {
	
	private static final int MAXTRIESTOWRITE = 5;
	
	private final VFS vfs;
	
	public WorkingSetRestletWorker(VFS vfs) {
		this.vfs = vfs;
	}
	
	/**
	 * Allows a user to subscribe to a public working set.
	 * 
	 * @param request
	 * @param response
	 */
	public Document subscribeToPublicWorkingSet(String id, String username) throws ResourceException {
		final String url = getWorkingSetFileURL(username);
		if (!vfs.exists(url))
			throw new ResourceException(Status.CLIENT_ERROR_FORBIDDEN);

		boolean locked = FileLocker.impl.aquireWithRetry(url, MAXTRIESTOWRITE);
		if (!locked)
			throw new ResourceException(Status.SERVER_ERROR_SERVICE_UNAVAILABLE);
		
		try {
			Document oldWorkingSet = DocumentUtils.getVFSFileAsDocument(url, vfs);
			Element publicWorkingSets = (Element) oldWorkingSet.getDocumentElement().getElementsByTagName(
					"public").item(0);
			NodeList nodes = publicWorkingSets.getElementsByTagName("workingSet");

			boolean found = false;
			for (int i = 0; i < nodes.getLength() && !found; i++) {
				String tempID = ((Element) nodes.item(i)).getAttribute("id");
				if (id.trim().equalsIgnoreCase(tempID.trim())) {
					found = true;
				}
			}

			if (!found) {

				Element publicDoc = DocumentUtils.getVFSFileAsDocument(getPublicWorkingSetURL(id), vfs)
						.getDocumentElement();
				String creator = publicDoc.getAttribute("creator");
				String name = publicDoc.getAttribute("name");
				String date = FormattedDate.impl.getDate();

				Element newPublic = DocumentUtils.createElementWithText(oldWorkingSet, "workingSet", "");
				newPublic.setAttribute("creator", creator);
				newPublic.setAttribute("dateAdded", date);
				newPublic.setAttribute("id", id);
				newPublic.setAttribute("name", name);

				publicWorkingSets.appendChild(newPublic);
				DocumentUtils.writeVFSFile(url, vfs, oldWorkingSet);

				//FileLocker.impl.releaseLock(url);
				
				return publicDoc.getOwnerDocument();
			}
			// ALREADY THERE
			else {
				throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
			}
		} catch (ResourceException e) {
			throw e;
		} catch (Exception e) {
			throw new ResourceException(Status.CLIENT_ERROR_EXPECTATION_FAILED, e);
		} finally {
			FileLocker.impl.releaseLock(url);
		}
	}
	
	public String getPublicWorkingSetFolderURL() {
		return "/workingsets";
	}

	public String getPublicWorkingSetIDURL() {
		return getPublicWorkingSetFolderURL() + "/workingSetID.txt";
	}

	public String getPublicWorkingSetURL(String id) {
		return (getPublicWorkingSetFolderURL() + "/" + FilenameStriper.getIDAsStripedPath(id) + ".xml");
	}
	
	public String getWorkingSetFileURL(String username) {
		return "/users/" + username + "/workingSet.xml";
	}

}
