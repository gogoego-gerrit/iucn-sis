//package org.iucn.sis.server.simple;
//
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;
//
//import org.iucn.sis.server.baserestlets.ServiceRestlet;
//import org.iucn.sis.server.filters.AssessmentFilterHelper;
//import org.iucn.sis.server.io.WorkingSetIO;
//import org.iucn.sis.server.locking.FileLocker;
//import org.iucn.sis.server.utils.DocumentUtils;
//import org.iucn.sis.server.utils.FilenameStriper;
//import org.iucn.sis.server.utils.FormattedDate;
//import org.iucn.sis.server.utils.IDFactory;
//import org.iucn.sis.server.utils.logging.DBWorkingSetBuffer;
//import org.iucn.sis.server.utils.logging.EventLogger;
//import org.iucn.sis.shared.data.WorkingSetData;
//import org.iucn.sis.shared.data.assessments.AssessmentData;
//import org.iucn.sis.shared.data.assessments.AssessmentFilter;
//import org.restlet.Context;
//import org.restlet.data.MediaType;
//import org.restlet.data.Method;
//import org.restlet.data.Request;
//import org.restlet.data.Response;
//import org.restlet.data.Status;
//import org.restlet.ext.xml.DomRepresentation;
//import org.restlet.representation.StringRepresentation;
//import org.w3c.dom.Document;
//import org.w3c.dom.Element;
//import org.w3c.dom.Node;
//import org.w3c.dom.NodeList;
//
//import com.solertium.db.DBException;
//import com.solertium.lwxml.factory.NativeDocumentFactory;
//import com.solertium.lwxml.shared.NativeDocument;
//import com.solertium.util.SysDebugger;
//import com.solertium.vfs.ConflictException;
//import com.solertium.vfs.NotFoundException;
//import com.solertium.vfs.VFS;
//import com.solertium.vfs.VFSPath;
//import com.solertium.vfs.VFSPathToken;
//import com.solertium.vfs.utils.VFSUtils;
//import com.solertium.vfs.utils.VFSUtils.VFSPathParseException;
//
///**
// * The Working Set Restlet, handles calls to get and modify a user's
// * workingSet.xml file. Makes assumptions that an xml file exists in the users
// * working space.
// * 
// * @author liz.schwartz
// * 
// */
//public class WorkingSetRestlet extends ServiceRestlet {
//
//	/**
//	 * Just returns the file name, not the entire path.
//	 * 
//	 * @param rootPath
//	 * @return
//	 */
//	public static List<String> getAllFilesRecursively(String rootPath, VFS vfs) {
//		ArrayList<String> files = new ArrayList<String>();
//		ArrayList<VFSPath> folders = new ArrayList<VFSPath>();
//		VFSPath folder;
//		try {
//			folder = VFSUtils.parseVFSPath(rootPath);
//		} catch (VFSPathParseException e) {
//			return null;
//		}
//		if (vfs.exists(folder))
//			folders.add(folder);
//		boolean cont = true;
//		while (!folders.isEmpty()) {
//			folder = folders.remove(0);
//			try {
//				if (vfs.isCollection(folder)) {
//					VFSPathToken[] tokens = vfs.list(folder);
//					for (VFSPathToken token : tokens) {
//						folders.add(folder.child(token));
//					}
//				} else {
//					String name = folder.getName();
//					files.add(name.substring(0, name.length() - ".xml".length()));
//				}
//			} catch (NotFoundException e) {
//				e.printStackTrace();
//				return null;
//			}
//		}
//		return files;
//
//	}
//
//	protected final int MAXTRIESTOWRITE = 5;
//	protected IDFactory idfactory = null;
//	protected DBWorkingSetBuffer buffer;
//	protected String xml;
//
//	public WorkingSetRestlet(String vfsroot, Context context) {
//		super(vfsroot, context);
//		idfactory = IDFactory.getIDFactory(vfs, getPublicWorkingSetIDURL());
//		try {
//			buffer = new DBWorkingSetBuffer();
//			EventLogger.impl.addBuffer(buffer);
//		} catch (DBException e) {
//		}
//	}
//
//	private boolean addPublicWorkingSetToUserSet(String userName, String id, String creator) {
//		final String url = getWorkingSetFileURL(userName);
//		boolean lock = FileLocker.impl.aquireWithRetry(url, MAXTRIESTOWRITE);
//		if (lock) {
//			Document userWorkingSetDoc = DocumentUtils.getVFSFileAsDocument(url, vfs);
//			Element workingSetElement = userWorkingSetDoc.createElement("workingSet");
//			workingSetElement.setAttribute("id", id);
//			workingSetElement.setAttribute("creator", creator);
//			workingSetElement.setAttribute("dateAdded", FormattedDate.impl.getDate());
//			((Element) userWorkingSetDoc.getDocumentElement().getElementsByTagName("public").item(0))
//			.appendChild(workingSetElement);
//			DocumentUtils.writeVFSFile(url, vfs, userWorkingSetDoc);
//			FileLocker.impl.releaseLock(url);
//		}
//		return lock;
//
//	}
//
//	/**
//	 * Given a WorkingSetData object inside an xml document, creates a new
//	 * working set with the data, generates and returns the new working set id.
//	 * 
//	 * @param request
//	 * @param response
//	 */
//	private void createPrivateWorkingSet(Request request, Response response) {
//
//		final String username = (String) request.getAttributes().get("username");
//		// final String workingSetFileURL = getWorkingSetFileURL(username);
//		// final VFSPath url = VFSUtils.parseVFSPath(workingSetFileURL);
//
//		// if (vfs.exists(url)) {
//		// try {
//		// Document oldWorkingSetDoc =
//		// DocumentUtils.getVFSFileAsDocument(workingSetFileURL, vfs);
//		Document newWorkingSet;
//		try {
//			newWorkingSet = new DomRepresentation(request.getEntity()).getDocument();
//		} catch (IOException e) {
//			response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
//			return;
//		}
//		Element newWorkingSetElement = newWorkingSet.getDocumentElement();
//
//		String id = idfactory.getNextIDAsString();
//
//		newWorkingSetElement.setAttribute("id", id);
//		// DocumentUtils.writeVFSFile(getPrivateWorkingSetURL(username, id),
//		// vfs, DocumentUtils
//		// .serializeNodeToString(newWorkingSetElement));
//		try {
//			writePrivateWorkingSet(newWorkingSetElement, id, username);
//		} catch (IOException e) {
//			e.printStackTrace();
//			response.setStatus(Status.SERVER_ERROR_INTERNAL);
//			return;
//		}
//
//		// newWorkingSetElement = (Element)
//		// oldWorkingSetDoc.importNode(newWorkingSetElement, true);
//		// oldWorkingSetDoc.getDocumentElement().getElementsByTagName("private").
//		// item(0).appendChild(
//		// newWorkingSetElement);
//		//
//		// DocumentUtils.writeVFSFile(url, vfs, oldWorkingSetDoc);
//
//		response.setEntity(id, MediaType.TEXT_PLAIN);
//		response.setStatus(Status.SUCCESS_CREATED);
//
//		// }
//		// // WASN'T IN PROPER XML FORMAT
//		// catch (Exception e) {
//		// response.setStatus(Status.CLIENT_ERROR_EXPECTATION_FAILED);
//		// e.printStackTrace();
//		// }
//		// }
//		//
//		// TRYING TO WRITE USER'S FILE THAT DOESN'T EXIST
//		// else
//		// response.setStatus(Status.CLIENT_ERROR_FORBIDDEN);
//
//	}
//
//	private void createPublicWorkingSet(Request request, Response response) {
//		String username = (String) request.getAttributes().get("username");
//
//		try {
//			Document newWorkingSet = (new DomRepresentation(request.getEntity())).getDocument();
//			Element newWorkingSetElement = newWorkingSet.getDocumentElement();
//			Element info = (Element) newWorkingSet.getDocumentElement().getElementsByTagName("info").item(0);
//
//			String id = idfactory.getNextIDAsString();
//			String creator = newWorkingSetElement.getAttribute("creator");
//			String name = info.getElementsByTagName("name").item(0).getTextContent();
//			newWorkingSetElement.setAttribute("id", id);
//
//			// Get and write commit to the log
//			writeToCommitLog(id, creator, name);
//
//			boolean acquiredLock = FileLocker.impl.aquireWithRetry(getPublicWorkingSetURL(id), MAXTRIESTOWRITE);
//			if (acquiredLock) {
//				// ADD TO THE USER'S SET OF WORKING SETS
//				boolean added = addPublicWorkingSetToUserSet(username, id, creator);
//
//				if (added) {
//					DocumentUtils.writeVFSFile(getPublicWorkingSetURL(id), vfs, newWorkingSet);
//					FileLocker.impl.releaseLock(getPublicWorkingSetURL(id));
//					response.setStatus(Status.SUCCESS_OK);
//					response.setEntity(id, MediaType.TEXT_ALL);
//				} else {
//					FileLocker.impl.releaseLock(getPublicWorkingSetURL(id));
//					response.setEntity(id, MediaType.TEXT_ALL);
//					response.setStatus(Status.SUCCESS_CREATED);
//				}
//			} else {
//				response.setStatus(Status.SERVER_ERROR_SERVICE_UNAVAILABLE);
//			}
//
//		}
//
//		// WASN'T IN PROPER XML FORMAT
//		catch (Exception e) {
//			response.setStatus(Status.CLIENT_ERROR_EXPECTATION_FAILED);
//			e.printStackTrace();
//		}
//	}
//
//	@Override
//	public void definePaths() {
//		paths.add("/workingSet/taxaList/{username}/{id}");
//		paths.add("/workingSet/subscribe/{username}/{id}");
//		paths.add("/workingSet/subscribe/{username}");
//		paths.add("/workingSet/public/{username}");
//		paths.add("/workingSet/public/{username}/{id}");
//		paths.add("/workingSet/private/{username}");
//		paths.add("/workingSet/private/{username}/{id}");
//		paths.add("/workingSet/{username}");
//		paths.add("/workingSet/taxaIDs/{username}/{id}");
//	}
//
//	/**
//	 * DELETES A PRIVATE WORKING SET, OR UNSUBSCRIBES FROM A PUBLIC WORKING SET
//	 * 
//	 * TODO: REMOVE CASES HANDLING PRIVATE IN SAME FILE AS PUBLIC
//	 * 
//	 * @param request
//	 * @param response
//	 * @param mode
//	 *            -- either "public" or "private"
//	 */
//	private void deleteFromWorkingSet(Request request, Response response, String mode) {
//
//		final String username = (String) request.getAttributes().get("username");
//		final String url = getWorkingSetFileURL(username);
//		final String id = (String) request.getAttributes().get("id");
//		final VFSPath workingSetPath;
//		try {
//			workingSetPath = VFSUtils.parseVFSPath(url);
//		} catch (VFSPathParseException e1) {
//			e1.printStackTrace();
//			response.setStatus(Status.SERVER_ERROR_INTERNAL);
//			return;
//		}
//		boolean found = false;
//
//		if (mode.equals(WorkingSetData.PRIVATE)) {
//			String workingSetURL = getPrivateWorkingSetURL(username, id);
//			VFSPath privateWorkingSetPath;
//			try {
//				privateWorkingSetPath = VFSUtils.parseVFSPath(workingSetURL);
//			} catch (VFSPathParseException e) {
//				e.printStackTrace();
//				response.setStatus(Status.SERVER_ERROR_INTERNAL);
//				return;
//			}
//
//			if (vfs.exists(privateWorkingSetPath)) {
//				try {
//					vfs.delete(privateWorkingSetPath);
//				} catch (NotFoundException e) {
//					e.printStackTrace();
//					response.setStatus(Status.SERVER_ERROR_INTERNAL);
//					return;
//				} catch (ConflictException e) {
//					e.printStackTrace();
//					response.setStatus(Status.SERVER_ERROR_INTERNAL);
//					return;
//				}
//				found = true;
//			}
//
//		}
//		if (!found || mode.equalsIgnoreCase(WorkingSetData.PUBLIC)) {
//			boolean locked = FileLocker.impl.aquireWithRetry(url, MAXTRIESTOWRITE);
//
//			if (locked) {
//				if (vfs.exists(workingSetPath)) {
//
//					Document workingSetDoc = DocumentUtils.getVFSFileAsDocument(url, vfs);
//
//					NodeList nodes = ((Element) workingSetDoc.getDocumentElement().getElementsByTagName(mode).item(0))
//					.getElementsByTagName("workingSet");
//					Node nodeToDelete = null;
//					int count = 0;
//
//					while (nodeToDelete == null && count < nodes.getLength()) {
//						Element temp = (Element) nodes.item(count);
//						if (temp.getAttribute("id").equalsIgnoreCase(id))
//							nodeToDelete = nodes.item(count);
//						count++;
//					}
//
//					if (nodeToDelete != null) {
//						((Element) workingSetDoc.getDocumentElement().getElementsByTagName(mode).item(0))
//						.removeChild(nodeToDelete);
//						DocumentUtils.writeVFSFile(url, vfs, workingSetDoc);
//
//					}
//					// NOT IN THE DOCUMENT
//					else {
//						response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
//						return;
//					}
//				}
//
//			}
//
//			// TRYING TO WRITE USER'S FILE THAT DOESN'T EXIST
//			else {
//				response.setStatus(Status.CLIENT_ERROR_FORBIDDEN);
//				return;
//			}
//
//			FileLocker.impl.releaseLock(url);
//
//		}
//
//		response.setStatus(Status.SUCCESS_OK);
//	}
//
//	// /**
//	// * Returns the private working set requested
//	// * @param request
//	// * @param response
//	// */
//	// private void getPrivateWorkingSet(Request request, Response response) {
//	// String url = getPrivateWorkingSetURL((String)
//	// request.getAttributes().get("username"));
//	// String xml = DocumentUtils.getVFSFileAsString(url, vfs);
//	// if (xml != null) {
//	// response.setEntity(xml, MediaType.TEXT_XML);
//	// response.setStatus(Status.SUCCESS_OK);
//	// } else {
//	// response.setStatus(Status.SERVER_ERROR_INTERNAL);
//	// }
//	// }
//
//	/**
//	 * Given a request object with the full working set data, saves over the
//	 * existing data in the working set. The working set must already exist.
//	 * 
//	 * @param request
//	 * @param response
//	 */
//	private void editPrivateWorkingSet(Request request, Response response) {
//
//		String username = (String) request.getAttributes().get("username");
//		Element newWorkingSetElement;
//		try {
//			newWorkingSetElement = (new DomRepresentation(request.getEntity())).getDocument().getDocumentElement();
//		} catch (IOException e) {
//			e.printStackTrace();
//			response.setStatus(Status.SERVER_ERROR_INTERNAL);
//			return;
//		}
//		String id = newWorkingSetElement.getAttribute("id");
//		String privateWorkingSetURL = getPrivateWorkingSetURL(username, id);
//		VFSPath path;
//		try {
//			path = VFSUtils.parseVFSPath(privateWorkingSetURL);
//		} catch (VFSPathParseException e) {
//			e.printStackTrace();
//			response.setStatus(Status.SERVER_ERROR_INTERNAL);
//			return;
//		}
//
//		if (vfs.exists(path)) {
//			try {
//				writePrivateWorkingSet(newWorkingSetElement, id, username);
//			} catch (IOException e) {
//				e.printStackTrace();
//				response.setStatus(Status.SERVER_ERROR_INTERNAL);
//				return;
//			}
//		} else {
//
//			String oldWorkingSetURL = getWorkingSetFileURL(username);
//			Document oldWorkingSet = DocumentUtils.getVFSFileAsDocument(oldWorkingSetURL, vfs);
//			Element privateWorkingSets = (Element) oldWorkingSet.getDocumentElement().getElementsByTagName("private")
//			.item(0);
//			NodeList nodes = privateWorkingSets.getElementsByTagName("workingSet");
//			Node nodeToReplace = null;
//
//			for (int i = 0; i < nodes.getLength() && nodeToReplace == null; i++) {
//				String tempID = ((Element) nodes.item(i)).getAttribute("id");
//				if (id.trim().equalsIgnoreCase(tempID.trim())) {
//					nodeToReplace = nodes.item(i);
//				}
//			}
//
//			if (nodeToReplace != null) {
//				privateWorkingSets.removeChild(nodeToReplace);
//				try {
//					writePrivateWorkingSet(newWorkingSetElement, id, username);
//				} catch (IOException e) {
//					e.printStackTrace();
//					response.setStatus(Status.SERVER_ERROR_INTERNAL);
//					return;
//				}
//				DocumentUtils.writeVFSFile(oldWorkingSetURL, vfs, oldWorkingSet);
//			}
//			// NOT THERE TO POST
//			else {
//				response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
//			}
//		}
//
//	}
//
//	/**
//	 * Posts to a public working set and adds creates taxa
//	 * 
//	 * @param request
//	 * @param response
//	 */
//	private void editPublicWorkingSet(Request request, Response response) {
//
//		String username = (String) request.getAttributes().get("username");
//		try {
//
//			Document doc = (new DomRepresentation(request.getEntity())).getDocument();
//			Element docElement = doc.getDocumentElement();
//			String id = docElement.getAttribute("id");
//			String creator = docElement.getAttribute("creator");
//			String url = getPublicWorkingSetURL(id);
//			Document oldDoc = DocumentUtils.getVFSFileAsDocument(url, vfs);
//			String oldid = oldDoc.getDocumentElement().getAttribute("id");
//			String oldcreator = oldDoc.getDocumentElement().getAttribute("creator");
//
//			if (oldid.equalsIgnoreCase(id) && oldcreator.equalsIgnoreCase(username)
//					&& creator.equalsIgnoreCase(oldcreator)) {
//
//				boolean locked = FileLocker.impl.aquireWithRetry(url, MAXTRIESTOWRITE);
//				if (locked) {
//					DocumentUtils.writeVFSFile(url, vfs, doc);
//					response.setStatus(Status.SUCCESS_OK);
//					FileLocker.impl.releaseLock(url);
//				} else {
//					response.setStatus(Status.SERVER_ERROR_SERVICE_UNAVAILABLE);
//				}
//
//			}
//
//			else {
//
//				response.setStatus(Status.CLIENT_ERROR_FORBIDDEN);
//
//			}
//
//		} catch (Exception e) {
//			e.printStackTrace();
//			response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
//		}
//
//	}
//
//	private List<String> getAllPublicWorkingSets() {
//		List<String> list = getAllFilesRecursively(getPublicWorkingSetFolderURL(), vfs);
//		list.remove("workingSetID");
//		return list;
//	}
//
//	/**
//	 * Gets a public working set and returns it.
//	 * 
//	 * @param request
//	 * @param response
//	 */
//	private void getPrivateWorkingSet(Request request, Response response) {
//		String id = (String) request.getAttributes().get("id");
//		String username = (String) request.getAttributes().get("username");
//
//		Element el = getPrivateWorkingSetElement(username, id);
//		if (el != null) {
//			String payload = DocumentUtils.serializeNodeToString(el);
//			response.setEntity(payload, MediaType.TEXT_XML);
//			response.setStatus(Status.SUCCESS_OK);
//		} else {
//			response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
//		}
//	}
//
//	/**
//	 * Returns a private working set represented by the given id
//	 */
//	public Element getPrivateWorkingSetElement(String username, String id) {
//
//		id = id.trim();
//		String url = getPrivateWorkingSetURL(username, id);
//		VFSPath path;
//		try {
//			path = VFSUtils.parseVFSPath(url);
//		} catch (VFSPathParseException e) {
//			e.printStackTrace();
//			return null;
//		}
//
//		if (vfs.exists(path)) {
//			return DocumentUtils.getVFSFileAsDocument(url, vfs).getDocumentElement();
//		}
//		// TODO: REMOVE WHEN ALL PRIVATE WORKING SETS ARE IN SEPERATE FILES
//		else {
//			path = new VFSPath("/users/" + username + "/workingSet.xml");
//
//			if (vfs.exists(path)) {
//				Document doc = DocumentUtils.getVFSFileAsDocument(path.toString(), vfs);
//				NodeList privateWS = ((Element) doc.getDocumentElement().getElementsByTagName("private").item(0))
//				.getElementsByTagName("workingSet");
//				Element elementToReturn = null;
//
//				SysDebugger.getInstance().println("This is the id I am looking for " + id);
//				for (int i = 0; i < privateWS.getLength() && elementToReturn == null; i++) {
//					Element temp = (Element) privateWS.item(i);
//					SysDebugger.getInstance().println("this is the id of the workingset " + temp.getAttribute("id"));
//					if (temp.getAttribute("id").trim().equalsIgnoreCase(id)) {
//						elementToReturn = temp;
//					}
//				}
//
//				return elementToReturn;
//			} else
//				return null;
//		}
//
//	}
//
//	public String getPrivateWorkingSetFolderURL(String username) {
//		return "/users/" + username + "/workingsets";
//	}
//
//	public String getPrivateWorkingSetURL(String username, String id) {
//		return getPrivateWorkingSetFolderURL(username) + "/" + id + ".xml";
//	}
//
//	/**
//	 * Gets a public working set and returns it.
//	 * 
//	 * @param request
//	 * @param response
//	 */
//	private void getPublicWorkingSet(Request request, Response response) {
//
//		// String url = getPublicWorkingSetURL((String)
//		// request.getAttributes().get("id"));
//		// SysDebugger.getInstance().println("I am in getpublic working set with url "
//		// + url);
//		// Document doc = DocumentUtils.getVFSFileAsDocument(url, vfs);
//
//		Document doc = WorkingSetIO.readPublicWorkingSetAsDocument(vfs, (String) request.getAttributes().get("id"));
//		if (doc != null) {
//			response.setEntity(new DomRepresentation(MediaType.TEXT_XML, doc));
//			response.setStatus(Status.SUCCESS_OK);
//		} else {
//			SysDebugger.getNamedInstance(SISContainerApp.SEVERE_LOG).println(
//					"COULD NOT FIND WORKING SET FOR ID: " + (String) request.getAttributes().get("id"));
//			response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
//		}
//	}
//
//	public String getPublicWorkingSetFolderURL() {
//		return "/workingsets";
//	}
//
//	public String getPublicWorkingSetIDURL() {
//		return getPublicWorkingSetFolderURL() + "/workingSetID.txt";
//	}
//
//	public String getPublicWorkingSetURL(String id) {
//		return (getPublicWorkingSetFolderURL() + "/" + FilenameStriper.getIDAsStripedPath(id) + ".xml");
//	}
//
//	/**
//	 * Gets all possible subscribable working sets, leaving out ones that users
//	 * are curerntly subscribed to.
//	 * 
//	 * @param request
//	 * @param response
//	 */
//	private void getSubscribableWorkingSets(Request request, Response response) {
//
//		String userUrl = getWorkingSetFileURL((String) request.getAttributes().get("username"));
//
//		if (vfs.exists(userUrl)) {
//			final List<String> publicWorkingSets = getAllPublicWorkingSets();
//			Document userDoc = DocumentUtils.getVFSFileAsDocument(userUrl, vfs);
//			NodeList publicWorkingSetsInUser = ((Element) userDoc.getDocumentElement().getElementsByTagName("public")
//					.item(0)).getElementsByTagName("workingSet");
//
//			StringBuilder xml = new StringBuilder("<xml>\r\n");
//			for (int i = 0; i < publicWorkingSetsInUser.getLength(); i++) {
//				publicWorkingSets.remove(((Element) publicWorkingSetsInUser.item(i)).getAttribute("id"));
//			}
//
//			for (String publicWorkingSet : publicWorkingSets) {
//				Document doc = DocumentUtils.getVFSFileAsDocument(getPublicWorkingSetURL(publicWorkingSet), vfs);
//				Element element = doc.getDocumentElement();
//				Element info = (Element) element.getElementsByTagName("info").item(0);
//				xml.append("<workingSet creator=\"" + element.getAttribute("creator") + "\" id=\""
//						+ element.getAttribute("id") + "\" date=\""
//						+ info.getElementsByTagName("date").item(0).getTextContent() + "\" name=\""
//						+ info.getElementsByTagName("name").item(0).getTextContent() + "\"/>");
//			}
//
//			xml.append("</xml>");
//			response.setEntity(xml.toString(), MediaType.TEXT_XML);
//			response.setStatus(Status.SUCCESS_OK);
//		}
//
//		else {
//			response.setStatus(Status.CLIENT_ERROR_FORBIDDEN);
//		}
//	}
//
//	/**
//	 * For each taxa in the working set, returns the entire footprint.
//	 * 
//	 * @param request
//	 * @param response
//	 */
//	private void getTaxaFootprintForWorkingSet(Request request, Response response) {
//		String username = (String) request.getAttributes().get("username");
//		String id = (String) request.getAttributes().get("id");
//
//		Document doc = null;
//
//		try {
//			if (vfs.exists(getPublicWorkingSetURL(id))) {
//				doc = DocumentUtils.getVFSFileAsDocument(getPublicWorkingSetURL(id), vfs);
//			} else {
//				Element privateWS = getPrivateWorkingSetElement(username, id);
//				doc = DocumentUtils.createDocumentFromString(DocumentUtils.serializeElementToString(privateWS));
//			}
//
//			String taxa = getTaxaString(doc);
//			Request newRequest = new Request(Method.GET, "riap://host/browse/footprint/null");
//			newRequest.setEntity(new StringRepresentation(taxa));
//
//			Response newResponse = getContext().getClientDispatcher().handle(newRequest);
//			if (newResponse.getStatus().isSuccess()) {
//				StringBuffer csv = new StringBuffer();
//				Document xmlFootprintDoc = new DomRepresentation(newResponse.getEntity()).getDocument();
//				NodeList species = xmlFootprintDoc.getDocumentElement().getElementsByTagName("species");
//				for (int i = 0; i < species.getLength(); i++) {
//					csv.append(species.item(i).getTextContent() + "\r\n");
//				}
//
//				DocumentUtils.writeVFSFile(getURLToSaveFootprint(username), vfs, csv.toString());
//				SysDebugger.getInstance().println(
//						"this is what I am returning " + request.getResourceRef().getHostIdentifier() + "/raw/"
//						+ getURLToSaveFootprint(username));
//
//				response.setEntity(request.getResourceRef().getHostIdentifier() + "/raw/"
//						+ getURLToSaveFootprint(username), MediaType.TEXT_ALL);
//				response.setStatus(Status.SUCCESS_CREATED);
//			} else {
//				response.setStatus(newResponse.getStatus());
//			}
//
//		} catch (IOException e) {
//			// GAVE US AN ID THAT WASN'T VALID
//			e.printStackTrace();
//			response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
//		} catch (NullPointerException e) {
//			// SOMETHING WAS WRONG IN THE DOCUMENT
//			e.printStackTrace();
//			response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
//		}
//
//	}
//
//
//	/**
//	 * 
//	 * @param request
//	 * @param response
//	 */
//	private void getTaxaFootprintWithAdditionalInfo(Request request, Response response) {
//
//		System.out.println("in get taxa footprint with additional information");
//
//		String username = (String) request.getAttributes().get("username");
//		String id = (String) request.getAttributes().get("id");
//		try {
//			NativeDocument entityDoc = NativeDocumentFactory.newNativeDocument();
//			entityDoc.parse(request.getEntityAsText());
//			AssessmentFilter filter = AssessmentFilter.parseXML(entityDoc);
//			AssessmentFilterHelper helper = new AssessmentFilterHelper(filter);
//
//			Document doc = null;
//
//
//			if (vfs.exists(getPublicWorkingSetURL(id))) {
//				doc = DocumentUtils.getVFSFileAsDocument(getPublicWorkingSetURL(id), vfs);
//			} else {
//				Element privateWS = getPrivateWorkingSetElement(username, id);
//				doc = DocumentUtils.createDocumentFromString(DocumentUtils.serializeElementToString(privateWS));
//			}
//
//			String taxa = getTaxaString(doc);
//			String [] taxaList = taxa.split(",");
//			Request newRequest = new Request(Method.GET, "riap://host/browse/footprint/null");
//			newRequest.setEntity(new StringRepresentation(taxa));
//
//			Response newResponse = getContext().getClientDispatcher().handle(newRequest);
//			if (newResponse.getStatus().isSuccess()) {
//				//TODO: to optimize, can fetch species only after the helper says if it wants to get information on it or not
//				StringBuffer csv = new StringBuffer();
//				Document xmlFootprintDoc = new DomRepresentation(newResponse.getEntity()).getDocument();
//				NodeList species = xmlFootprintDoc.getDocumentElement().getElementsByTagName("species");
//				for (int i = 0; i < species.getLength(); i++) {
//					String taxaID = taxaList[i];
//					List<AssessmentData> asms =helper.getAssessments(taxaID, vfs);
//					if (asms.size() > 0)
//					{
//						System.out.println("got the most recent assessment ");
//						csv.append(species.item(i).getTextContent() + ",");
//						csv.append("\"" + asms.get(0).getProperCriteriaString() + "\"," + asms.get(0).getProperCategoryAbbreviation() + "\n");
//					}
//
//				}
//
//				DocumentUtils.writeVFSFile(getURLToSaveFootprint(username), vfs, csv.toString());
//				SysDebugger.getInstance().println(
//						"this is what I am returning " + request.getResourceRef().getHostIdentifier() + "/raw/"
//						+ getURLToSaveFootprint(username));
//
//				response.setEntity(request.getResourceRef().getHostIdentifier() + "/raw/"
//						+ getURLToSaveFootprint(username), MediaType.TEXT_ALL);
//				response.setStatus(Status.SUCCESS_CREATED);
//			} else {
//				response.setStatus(newResponse.getStatus());
//			}
//
//		} catch (IOException e) {
//			// GAVE US AN ID THAT WASN'T VALID
//			e.printStackTrace();
//			response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
//		} catch (NullPointerException e) {
//			// SOMETHING WAS WRONG IN THE DOCUMENT
//			e.printStackTrace();
//			response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//
//	}
//
//	/**
//	 * Returns all taxa associated with a working set
//	 * 
//	 * @param request
//	 * @param response
//	 */
//	private void getTaxaListForWorkingSet(Request request, Response response) {
//		String username = (String) request.getAttributes().get("username");
//		String id = (String) request.getAttributes().get("id");
//		Document doc = null;
//
//		if (vfs.exists(getPublicWorkingSetURL(id))) {
//			doc = DocumentUtils.getVFSFileAsDocument(getPublicWorkingSetURL(id), vfs);
//		} else {
//			Element privateWS = getPrivateWorkingSetElement(username, id);
//			doc = DocumentUtils.createDocumentFromString(DocumentUtils.serializeNodeToString(privateWS));
//		}
//
//		String taxa = getTaxaString(doc);
//		response.setEntity(taxa, MediaType.TEXT_PLAIN);
//		response.setStatus(Status.SUCCESS_OK);
//
//	}
//
//	/**
//	 * Given a document which is a valid working set, returns the taxa ids in
//	 * the csv form
//	 * 
//	 * @param doc
//	 * @return
//	 */
//	private String getTaxaString(Document doc) throws NullPointerException {
//		StringBuffer taxa = new StringBuffer();
//
//		Element taxaList = (Element) doc.getDocumentElement().getElementsByTagName("taxa").item(0);
//		NodeList list = taxaList.getElementsByTagName("species");
//		for (int i = 0; i < list.getLength(); i++) {
//			taxa.append(list.item(i).getTextContent() + ",");
//		}
//
//		if (taxa.length() > 0)
//			return taxa.substring(0, taxa.length() - 1);
//		else
//			return "";
//	}
//
//	public String getURLToSaveFootprint(String username) {
//		return "/users/" + username + "/reports/footprint.csv";
//	}
//
//	protected String getWorkingSetFileURL(String username) {
//		return "/users/" + username + "/workingSet.xml";
//	}
//
//	/**
//	 * gets all working sets both public and private
//	 * 
//	 * @param request
//	 * @param response
//	 */
//	private void getWorkingSets(Request request, Response response) {
//		String username = (String) request.getAttributes().get("username");
//
//		if (!vfs.exists(new VFSPath("/users/" + username))) {
//			response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
//		} else {
//			try {
//				boolean allExisted = true;
//				String contents;
//				VFSPath workingSetURL = new VFSPath(getWorkingSetFileURL(username));
//				if (vfs.exists(workingSetURL)) {
//					Document workingSets = DocumentUtils.getVFSFileAsDocument(getWorkingSetFileURL(username), vfs);
//					Element documentElement = workingSets.getDocumentElement();
//					Element publicWorkingSetElement = (Element) documentElement.getElementsByTagName("public").item(0);
//
//					NodeList publicNodes = publicWorkingSetElement.getElementsByTagName("workingSet");
//
//					allExisted = true;
//					for (int i = 0; i < publicNodes.getLength(); i++) {
//						Element tempPublic = (Element) publicNodes.item(i);
//						String publicID = tempPublic.getAttribute("id");
//
//						// IF IT EXISTS REPLACE IT WITH INFORMATION, OTHERWISE
//						// DO
//						// NOTHING
//						if (vfs.exists(getPublicWorkingSetURL(publicID))) {
//
//							Element workingSet = DocumentUtils.getVFSFileAsDocument(getPublicWorkingSetURL(publicID),
//									vfs).getDocumentElement();
//							workingSet = (Element) workingSets.importNode(workingSet, true);
//							publicWorkingSetElement.replaceChild(workingSet, tempPublic);
//						} else
//							allExisted = false;
//					}
//
//					// GET PRIVATE WORKING SETS
//					NodeList privateElements = documentElement.getElementsByTagName("private");
//					Element privateElement;
//					if (privateElements.getLength() == 0) {
//						privateElement = workingSets.createElement("private");
//						documentElement.appendChild(privateElement);
//					} else {
//						privateElement = (Element) privateElements.item(0);
//					}
//
//					List<String> privateWorkingSets = getAllFilesRecursively(getPrivateWorkingSetFolderURL(username),
//							vfs);
//					for (String filename : privateWorkingSets) {
//						Document doc = DocumentUtils.getVFSFileAsDocument(getPrivateWorkingSetURL(username, filename),
//								vfs);
//						Element docElement = doc.getDocumentElement();
//						docElement = (Element) doc.removeChild(docElement);
//						privateElement.appendChild(workingSets.adoptNode(docElement.cloneNode(true)));
//
//					}
//					contents = DocumentUtils.serializeNodeToString(workingSets.getDocumentElement());
//
//				} else {
//					contents = "<workingSets>\r\n<public/>\r\n<private/>\r\n</workingSets>";
//					DocumentUtils.writeVFSFile(workingSetURL.toString(), vfs, contents);
//				}
//
//				// IF EVERYTHING GOES ALRIGHT, SEND BACK!
//				response.setEntity(contents, MediaType.TEXT_XML);
//
//				if (allExisted)
//					response.setStatus(Status.SUCCESS_OK);
//				else
//					response.setStatus(Status.SUCCESS_OK);
//
//			} catch (Exception e) {
//				e.printStackTrace();
//				response.setStatus(Status.CLIENT_ERROR_EXPECTATION_FAILED);
//			}
//
//		}
//
//	}
//
//	@Override
//	public void performService(Request request, Response response) {
//		if (request.getMethod().equals(Method.GET)) {
//			if (request.getResourceRef().getPath().startsWith("/workingSet/subscribe/"))
//				getSubscribableWorkingSets(request, response);
//			else if (request.getResourceRef().getPath().startsWith("/workingSet/public/"))
//				getPublicWorkingSet(request, response);
//			else if (request.getResourceRef().getPath().startsWith("/workingSet/private/"))
//				getPrivateWorkingSet(request, response);
//			else if (request.getResourceRef().getPath().startsWith("/workingSet/taxaList/"))
//				getTaxaFootprintForWorkingSet(request, response);
//			else if (request.getResourceRef().getPath().startsWith("/workingSet/taxaIDs/"))
//				getTaxaListForWorkingSet(request, response);
//			else
//				getWorkingSets(request, response);
//		} else if (request.getMethod().equals(Method.DELETE)) {
//			if (request.getResourceRef().getPath().startsWith("/workingSet/private/"))
//				deleteFromWorkingSet(request, response, "private");
//			else if (request.getResourceRef().getPath().startsWith("/workingSet/public/"))
//				deleteFromWorkingSet(request, response, "public");
//		} else if (request.getMethod().equals(Method.PUT)) {
//			if (request.getResourceRef().getPath().startsWith("/workingSet/private/"))
//				createPrivateWorkingSet(request, response);
//			else if (request.getResourceRef().getPath().startsWith("/workingSet/subscribe/"))
//				subscribeToPublicWorkingSet(request, response);
//			else
//				createPublicWorkingSet(request, response);
//		} else if (request.getMethod().equals(Method.POST)) {
//			if (request.getResourceRef().getPath().startsWith("/workingSet/private/")) {
//				try {
//					editPrivateWorkingSet(request, response);
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//
//			}
//			else if (request.getResourceRef().getPath().startsWith("/workingSet/taxaList/"))
//				getTaxaFootprintWithAdditionalInfo(request, response);
//			else
//				editPublicWorkingSet(request, response);
//		} else
//			response.setStatus(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
//	}
//
//	/**
//	 * Allows a user to subscribe to a public working set.
//	 * 
//	 * @param request
//	 * @param response
//	 */
//	private void subscribeToPublicWorkingSet(Request request, Response response) {
//		String url = getWorkingSetFileURL((String) request.getAttributes().get("username"));
//
//		boolean locked = FileLocker.impl.aquireWithRetry(url, MAXTRIESTOWRITE);
//		if (locked) {
//			if (vfs.exists(url)) {
//				try {
//
//					String id = (String) request.getAttributes().get("id");
//					Document oldWorkingSet = DocumentUtils.getVFSFileAsDocument(url, vfs);
//					Element publicWorkingSets = (Element) oldWorkingSet.getDocumentElement().getElementsByTagName(
//					"public").item(0);
//					NodeList nodes = publicWorkingSets.getElementsByTagName("workingSet");
//
//					boolean found = false;
//					for (int i = 0; i < nodes.getLength() && !found; i++) {
//						String tempID = ((Element) nodes.item(i)).getAttribute("id");
//						if (id.trim().equalsIgnoreCase(tempID.trim())) {
//							found = true;
//						}
//					}
//
//					if (!found) {
//
//						Element publicDoc = DocumentUtils.getVFSFileAsDocument(getPublicWorkingSetURL(id), vfs)
//						.getDocumentElement();
//						String creator = publicDoc.getAttribute("creator");
//						String name = publicDoc.getAttribute("name");
//						String date = FormattedDate.impl.getDate();
//
//						Element newPublic = DocumentUtils.createElementWithText(oldWorkingSet, "workingSet", "");
//						newPublic.setAttribute("creator", creator);
//						newPublic.setAttribute("dateAdded", date);
//						newPublic.setAttribute("id", id);
//						newPublic.setAttribute("name", name);
//
//						publicWorkingSets.appendChild(newPublic);
//						DocumentUtils.writeVFSFile(url, vfs, oldWorkingSet);
//
//						response.setEntity(new DomRepresentation(MediaType.TEXT_XML, publicDoc.getOwnerDocument()));
//						response.setStatus(Status.SUCCESS_OK);
//					}
//					// ALREADY THERE
//					else {
//						response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
//					}
//
//				} catch (Exception e) {
//					response.setStatus(Status.CLIENT_ERROR_EXPECTATION_FAILED);
//					e.printStackTrace();
//				}
//			} else {
//				response.setStatus(Status.CLIENT_ERROR_FORBIDDEN);
//			}
//			FileLocker.impl.releaseLock(url);
//		} else {
//			response.setStatus(Status.SERVER_ERROR_SERVICE_UNAVAILABLE);
//		}
//	}
//
//	/**
//	 * TODO: HANDLES ALL WRITING OF ALL PRIVATE WORKING SETS
//	 * 
//	 * @param element
//	 * @param id
//	 * @throws IOException
//	 */
//	private void writePrivateWorkingSet(Element element, String workingSetID, String username) throws IOException {
//		String folder = getPrivateWorkingSetFolderURL(username);
//		VFSPath folderPath = VFSUtils.parseVFSPath(folder);
//		if (!vfs.exists(folderPath)) {
//			vfs.makeCollection(folderPath);
//		}
//
//		if (!DocumentUtils.writeVFSFile(getPrivateWorkingSetURL(username, workingSetID), vfs, DocumentUtils
//				.serializeNodeToString(element))) {
//			throw new IOException("Unable to save working set");
//		}
//	}
//
//	/**
//	 * Does all logging of needed events.
//	 * 
//	 * @param id
//	 * @param creator
//	 * @param name
//	 */
//	private synchronized void writeToCommitLog(String id, String creator, String name) {
//		// Document logDoc =
//		// DocumentUtils.getVFSFileAsDocument(getPublicWorkingSetCommitLogURL(),
//		// vfs);
//		// Element logDocElement = logDoc.getDocumentElement();
//
//		String date = FormattedDate.impl.getDate();
//
//		String xml = "<log>\r\n</log>";
//		String log = "<workingSet creator=\"" + creator + "\" id=\"" + id + "\" date=\"" + date + "\" name=\"" + name
//		+ "\"/>";
//		xml = xml.replace("</log>", log + "\r\n</log>");
//
//		// CREATE NEXT ENTRY
//		// Element newElement = logDoc.createElement("workingSet");
//		// newElement.setAttribute("id", id);
//		// newElement.setAttribute("creator", creator);
//		// newElement.setAttribute("name", name);
//		// newElement.setAttribute("date", date);
//		// logDocElement.appendChild(newElement);
//		buffer.addEvent(DocumentUtils.createDocumentFromString(xml));
//		// buffer.addEvent(logDoc);
//
//		// WRITE TO FILE (ABOUT TO BECOME OBSOLETE)
//		// DocumentUtils.writeVFSFile(getPublicWorkingSetCommitLogURL(), vfs,
//		// logDoc);
//
//	}
//}
