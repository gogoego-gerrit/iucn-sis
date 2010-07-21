package org.iucn.sis.server.simple;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.iucn.sis.server.baserestlets.ServiceRestlet;
import org.iucn.sis.server.utils.DocumentUtils;
import org.iucn.sis.server.utils.FilenameStriper;
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

import com.solertium.util.SysDebugger;

public class BrowseAssessmentsRestlet extends ServiceRestlet {
	// private String fullMasterList = "";

	public static Document createDomDocument() {
		try {
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = builder.newDocument();
			return doc;
		} catch (ParserConfigurationException e) {
		}
		return null;
	}

	public BrowseAssessmentsRestlet(String vfsroot, Context context) {
		super(vfsroot, context);
	}

	@Override
	public void definePaths() {
		paths.add("/files/{fileName}");
		paths.add("/browse/assessments");
		paths.add("/browse/assessments/{assessmentID}");
		paths.add("/browse/assessments/docs/{fileName}");
	}

	private void deleteAssessment(Response response, final String assessmentID) {
		String url = null;
		try {
			String stripedAssID = FilenameStriper.getIDAsStripedPath(assessmentID);
			SysDebugger.getInstance().println("Looking for assessment " + assessmentID + " in path " + stripedAssID);
			if (vfs.exists("/browse/assessments/" + stripedAssID + ".xml"))
				url = "/browse/assessments/" + stripedAssID + ".xml";
			else if (vfs.exists("/browse/assessments/" + stripedAssID))
				url = "/browse/assessments/" + stripedAssID;
			else
				throw new Exception("No assessment!");

			vfs.move(url, url.replace("/browse", "/trash"));

		} catch (Exception e) {
			SysDebugger.getInstance().println("Could not find assessment " + assessmentID);
			e.printStackTrace();
			response.setStatus(Status.SUCCESS_OK);
		}
	}

	private void getAssessment(Response response, String assessmentID) {
		String url = null;

		String[] assesmentIds = assessmentID.split(",");
		Document x = createDomDocument();
		Element rootElement = x.createElement("assessments");

		for (int i = 0; i < assesmentIds.length; i++) {
			try {
				String stripedAssID = FilenameStriper.getIDAsStripedPath(assesmentIds[i]);
				SysDebugger.getInstance().println(
						"Looking for assessment " + assesmentIds[i] + " in path " + stripedAssID);
				if (vfs.exists("/browse/assessments/" + stripedAssID + ".xml"))
					url = "/browse/assessments/" + stripedAssID + ".xml";
				else if (vfs.exists("/browse/assessments/" + stripedAssID))
					url = "/browse/assessments/" + stripedAssID;
				else
					throw new Exception("No assessment!");
				Element appendNode = DocumentUtils.getVFSFileAsDocument(url, vfs).getDocumentElement();
				rootElement.appendChild(x.importNode(appendNode, true));

			} catch (Exception e) {
				SysDebugger.getInstance().println("Could not find assessment " + assessmentID);
				e.printStackTrace();
				response.setStatus(Status.SUCCESS_OK);
			}
		}
		x.appendChild(rootElement);
		Representation doc = new DomRepresentation(MediaType.TEXT_XML, x);

		doc.setMediaType(MediaType.TEXT_XML);
		response.setEntity(doc);
		response.setStatus(Status.SUCCESS_OK);

	}

	private void getAssessmentList(Response response) {
		try {
			String[] files = vfs.list("/browse/assessments/");
			String options = "<files>\r\n";

			for (int i = 0; i < files.length; i++)
				options += "<file>" + files[i] + "</file>\r\n";

			options += "</files>\r\n";

			response.setEntity(options, MediaType.TEXT_XML);

		} catch (Exception e) {
			response.setStatus(Status.SERVER_ERROR_INTERNAL);
		}
	}

	private void getFile(Response response, String file) {
		String url = null;

		SysDebugger.getInstance().println("Request for file " + file);

		try {
			if (vfs.exists("/browse/docs/" + file + ".xml"))
				url = "/browse/docs/" + file + ".xml";
			else if (vfs.exists("/browse/docs/" + file))
				url = "/browse/docs/" + file;

			SysDebugger.getInstance().println(" found at url " + url);

			Representation doc = new DomRepresentation(MediaType.TEXT_XML, DocumentUtils.getVFSFileAsDocument(url, vfs));
			doc.setMediaType(MediaType.TEXT_XML);
			response.setEntity(doc);
			response.setStatus(Status.SUCCESS_OK);
		} catch (Exception e) {
			SysDebugger.getInstance().println("Could not find file " + file);
			response.setStatus(Status.SUCCESS_OK);
		}
	}

	@Override
	public void performService(Request request, Response response) {
		String assessmentID = (String) request.getAttributes().get("assessmentID");
		String file = (String) request.getAttributes().get("fileName");

		if (request.getResourceRef().getPath().startsWith("/files/")) {
			String url = null;

			try {
				url = "/files/" + file;

				Representation doc = new DomRepresentation(MediaType.TEXT_XML, DocumentUtils.getVFSFileAsDocument(url,
						vfs));
				response.setEntity(doc);
				response.setStatus(Status.SUCCESS_OK);
			} catch (Exception e) {
				SysDebugger.getInstance().println("Could not find file " + file);
				response.setStatus(Status.SUCCESS_OK);
			}
		}

		if (assessmentID != null) {
			if (request.getMethod() == Method.DELETE)
				deleteAssessment(response, assessmentID);
			else
				getAssessment(response, assessmentID);
		} else if (file != null)
			getFile(response, file);
		else
			getAssessmentList(response);
	}

}
