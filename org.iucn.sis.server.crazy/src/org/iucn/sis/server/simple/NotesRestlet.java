package org.iucn.sis.server.simple;

import java.io.IOException;
import java.io.Writer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.iucn.sis.server.baserestlets.ServiceRestlet;
import org.iucn.sis.server.utils.DocumentUtils;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.Representation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.solertium.util.SysDebugger;
import com.solertium.vfs.BoundsException;
import com.solertium.vfs.NotFoundException;
import com.solertium.vfs.VFSPath;
import com.solertium.vfs.VFSPathToken;

public class NotesRestlet extends ServiceRestlet {

	public static Document createDomDocument() {
		try {
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = builder.newDocument();
			return doc;
		} catch (ParserConfigurationException e) {
		}
		return null;
	}

	public NotesRestlet(String vfsroot, Context context) {
		super(vfsroot, context);
	}

	@Override
	public void definePaths() {
		paths.add("/notes/{type}/{assessmentID}");
		paths.add("/notes/{type}/{assessmentID}/{canonicalName}");
		paths.add("/notes/{type}/{assessmentID}/{canonicalName}/{username}");

	}

	private void doDelete(Request request, Response response, String assessmentID, String type, String canonicalName,
			String username) {

		String url = null;

		if (vfs.exists("/notes/" + type + "/" + assessmentID + "/" + canonicalName + ".xml"))
			url = "/notes/" + type + "/" + assessmentID + "/" + canonicalName + ".xml";
		else if (vfs.exists("/notes/" + type + "/" + assessmentID + "/" + canonicalName))
			url = "/notes/" + type + "/" + assessmentID + "/" + canonicalName;
		else if (vfs.exists("/notes/" + type + "/" + username + "/" + assessmentID + "/" + canonicalName + ".xml"))
			url = "/notes/" + type + "/" + username + "/" + assessmentID + "/" + canonicalName + ".xml";
		else if (vfs.exists("/notes/" + type + "/" + username + "/" + assessmentID + "/" + canonicalName))
			url = "/notes/" + type + "/" + username + "/" + assessmentID + "/" + canonicalName;

		if (url == null) {
			response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
		} else {
			try {

				Document notes = DocumentUtils.getVFSFileAsDocument(url, vfs);
				Document delNote = new DomRepresentation(request.getEntity()).getDocument();

				NodeList notesList = notes.getElementsByTagName("note");

				for (int i = 0; i < notesList.getLength(); i++) {

					if (((Element) notesList.item(i)).getAttribute("id").equals(
							delNote.getDocumentElement().getAttribute("id"))) {
						Node theParent = notesList.item(i).getParentNode();
						theParent.removeChild(notesList.item(i));
					}
				}

				SysDebugger.getInstance().println(
						"remove them all.... " + DocumentUtils.serializeNodeToString(notes.getDocumentElement()));

				Writer writer = vfs.getWriter(url);
				writer.write(DocumentUtils.serializeNodeToString(notes.getDocumentElement()));
				writer.close();

				response.setEntity(DocumentUtils.serializeNodeToString(notes.getDocumentElement()), MediaType.TEXT_XML);
				response.setStatus(Status.SUCCESS_OK);
			} catch (Exception e) {
				e.printStackTrace();
				response.setStatus(Status.SERVER_ERROR_INTERNAL);
			}

		}

	}

	private void doGet(Response response, String assessmentID, String type, String canonicalName, String username) {
		VFSPath url = new VFSPath("/notes/" + type + "/" + assessmentID);

		try {
			if (vfs.exists(url)) {
				VFSPathToken[] list = vfs.list(url);
				StringBuilder xml = new StringBuilder("<noteList>");

				for (VFSPathToken curToken : list) {
					String file = vfs.getString(url.child(curToken));
					file = file.replaceAll("<\\?xml\\s*(version=.*)?\\s*(encoding=.*)?\\?>", "");
					file = file.replaceFirst("<notes>", "<notes id=\"" + curToken.toString().replace(".xml", "")
							+ "\">");

					xml.append(file);
				}

				xml.append("</noteList>");

				response.setEntity(xml.toString(), MediaType.TEXT_XML);
				response.setStatus(Status.SUCCESS_OK);
			} else
				response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
		} catch (BoundsException e) {
			e.printStackTrace();
		} catch (NotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void doPost(Request request, Response response, String assessmentID, String type, String canonicalName,
			String username) {
		String url = null;

		if (vfs.exists("/notes/" + type + "/" + assessmentID + "/" + canonicalName + ".xml"))
			url = "/notes/" + type + "/" + assessmentID + "/" + canonicalName + ".xml";
		else if (vfs.exists("/notes/" + type + "/" + assessmentID + "/" + canonicalName))
			url = "/notes/" + type + "/" + assessmentID + "/" + canonicalName;
		else if (vfs.exists("/notes/" + type + "/" + username + "/" + assessmentID + "/" + canonicalName + ".xml"))
			url = "/notes/" + type + "/" + username + "/" + assessmentID + "/" + canonicalName + ".xml";
		else if (vfs.exists("/notes/" + type + "/" + username + "/" + assessmentID + "/" + canonicalName))
			url = "/notes/" + type + "/" + username + "/" + assessmentID + "/" + canonicalName;

		if (url == null) {
			try {
				String newNote = request.getEntity().getText();
				String notes = "<notes>\r\n" + newNote + "</notes>\r\n";
				if (username == null)
					url = "/notes/" + type + "/" + assessmentID + "/" + canonicalName + ".xml";
				else
					url = "/notes/" + type + "/" + username + "/" + assessmentID + "/" + canonicalName + ".xml";

				Writer writer = vfs.getWriter(url);
				writer.write(notes);
				writer.close();

				response.setEntity(notes, MediaType.TEXT_XML);
				response.setStatus(Status.SUCCESS_OK);
			} catch (Exception e) {
				e.printStackTrace();
				response.setStatus(Status.SERVER_ERROR_INTERNAL);
			}
		} else {
			try {
				Representation rep = new DomRepresentation(MediaType.TEXT_XML, DocumentUtils.getVFSFileAsDocument(url,
						vfs));
				String notes = rep.getText();
				String newNote = request.getEntity().getText();

				notes = notes.replace("</notes>", newNote + "</notes>\r\n");
				SysDebugger.getInstance().println(notes);

				Writer writer = vfs.getWriter(url);
				writer.write(notes);
				writer.close();

				response.setEntity(notes, MediaType.TEXT_XML);
				response.setStatus(Status.SUCCESS_OK);
			} catch (Exception e) {
				e.printStackTrace();
				response.setStatus(Status.SERVER_ERROR_INTERNAL);
			}

		}
	}

	@Override
	public void performService(Request request, Response response) {
		String type = (String) request.getAttributes().get("type");
		String assessmentID = (String) request.getAttributes().get("assessmentID");
		String canonicalName = (String) request.getAttributes().get("canonicalName");
		String user = (String) request.getAttributes().get("username");

		if (type.equals("taxon")) {
			canonicalName = assessmentID;
		}
		if (assessmentID == null)
			response.setStatus(Status.SERVER_ERROR_NOT_IMPLEMENTED);
		else if (request.getMethod().equals(Method.GET))
			doGet(response, assessmentID, type, canonicalName, user);
		else if (request.getMethod().equals(Method.POST) || request.getMethod().equals(Method.PUT)) {
			if (request.getResourceRef().getQueryAsForm().getFirstValue("option") != null
					&& request.getResourceRef().getQueryAsForm().getFirstValue("option").equals("remove")) {
				doDelete(request, response, assessmentID, type, canonicalName, user);
			} else
				doPost(request, response, assessmentID, type, canonicalName, user);
		}

		else
			response.setStatus(Status.SERVER_ERROR_NOT_IMPLEMENTED);
	}
}
