package org.iucn.sis.server.extensions.recentasms;

import org.iucn.sis.server.api.restlets.ServiceRestlet;
import org.iucn.sis.server.api.utils.DocumentUtils;
import org.iucn.sis.server.api.utils.ServerPaths;
import org.restlet.Context;
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

import com.solertium.util.NodeCollection;
import com.solertium.vfs.VFSPath;

public class RecentAssessmentsRestlet extends ServiceRestlet {

	public RecentAssessmentsRestlet(String vfsroot, Context context) {
		super(vfsroot, context);
	}

	@Override
	public void definePaths() {
		paths.add("/recentAssessments/{username}");
		paths.add("/recentAssessments/{username}/{status}/{id}");
	}

	public void doDelete(Request request, Response response) {
		String username = (String) request.getAttributes().get("username");
		String status = (String) request.getAttributes().get("status");
		String id = (String) request.getAttributes().get("id");

		if (vfs.exists(new VFSPath("/users/" + username))) {
			try {
				String url = "/users/" + username + "/recentlyViewed.xml";
				String contents;

				if (vfs.exists(new VFSPath(url))) {
					Document doc = DocumentUtils.getVFSFileAsDocument(url, vfs);
					NodeCollection nodes = new NodeCollection(doc.getElementsByTagName("assessment"));
					String xml = "<recent>";
					for (Node node : nodes) {
						Element el = (Element) node;
						if (el.getAttribute("status").equals(status) && el.getTextContent().equals(id)) {
							// removing assessment from recent
						} else {
							xml += "<assessment status=\"" + el.getAttribute("status") + "\">" + el.getTextContent()
									+ "</assessment>";
						}
					}
					xml += "</recent>";
					DocumentUtils.writeVFSFile("/users/" + username + "/recentlyViewed.xml", vfs, xml);
					response.setStatus(Status.SUCCESS_OK);
				} else {
					response.setStatus(Status.SUCCESS_OK);
					return;
				}

			} catch (Exception e) {
				e.printStackTrace();
				response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			}
		} else {
			response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
		}
	}

	private void doGet(Request request, Response response) {
		String username = (String) request.getAttributes().get("username");
		
		if (vfs.exists(new VFSPath("/users/" + username))) {
			try {
				String recent = DocumentUtils.getVFSFileAsString(
						"/users/" + username + "/recentlyViewed.xml", vfs);
				
				if( recent != null && !recent.trim().equals("") ) {
					Document doc = DocumentUtils.createDocumentFromString(recent);

					if (doc != null) {
						NodeList resourceList = doc.getElementsByTagName("assessment");

//						for (int i = 0; i < resourceList.getLength(); i++)
//						{
//							Element e = (Element) resourceList.item(i);
//							String id = e.getTextContent();
//							if (e.getAttribute("status").equals("published_status")) {
//								if (!vfs.exists(new VFSPath(ServerPaths.getPublishedAssessmentURL(id))))
//									e.getParentNode().removeChild(e);
//							} else if (e.getAttribute("status").equals("draft_status")) {
//								String uri = ServerPaths.getDraftAssessmentURL(id);
//								
//								if (!vfs.exists(new VFSPath(uri)))
//									e.getParentNode().removeChild(e);
//							}
//						}
						
						response.setEntity(new DomRepresentation(MediaType.TEXT_XML, doc));
						response.setStatus(Status.SUCCESS_OK);
					} else {
						response.setEntity(recent, MediaType.TEXT_XML);
						response.setStatus(Status.SUCCESS_OK);
					}
				} else
					response.setStatus(Status.SUCCESS_NO_CONTENT);
				
			} catch (Exception e) {
				e.printStackTrace();
				response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			}
		} else {
			response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
		}
	}

	private void doPost(Request request, Response response) {
		String username = (String) request.getAttributes().get("username");
		String xml;

		try {
			xml = request.getEntity().getText();
		} catch (Exception e) {
			response.setStatus(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY);
			return;
		}

		try {
			if (vfs.exists("/users/" + username)) {
				DocumentUtils.writeVFSFile("/users/" + username + "/recentlyViewed.xml", vfs, xml);
//				Writer writer = vfs.getWriter("/users/" + username + "/recentlyViewed.xml");
//				writer.write(xml);
//				writer.close();

				response.setEntity("<html><head></head><body>Recently viewed "
						+ "assessments were saved just fine.</body></html>", MediaType.TEXT_HTML);
				response.setStatus(Status.SUCCESS_OK);
			} else {
				response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			}
		} catch (Exception e) {
			response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
		}

	}

	@Override
	public void performService(Request request, Response response) {
		if (request.getMethod().equals(Method.GET))
			doGet(request, response);
		else if (request.getMethod().equals(Method.POST))
			doPost(request, response);
		else if (request.getMethod().equals(Method.DELETE))
			doDelete(request, response);
	}

}
