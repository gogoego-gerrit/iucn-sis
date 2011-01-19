package org.iucn.sis.server.extensions.recentasms;

import java.io.IOException;
import java.util.List;

import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
import org.iucn.sis.server.api.restlets.BaseServiceRestlet;
import org.iucn.sis.server.api.utils.DocumentUtils;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.Region;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ResourceException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.solertium.util.ElementCollection;
import com.solertium.util.NodeCollection;
import com.solertium.vfs.VFS;
import com.solertium.vfs.VFSPath;

public class RecentAssessmentsRestlet extends BaseServiceRestlet {
	
	private final VFS vfs;
	//TODO: put this in the DB

	public RecentAssessmentsRestlet(Context context) {
		super(context);
		vfs = SIS.get().getVFS();
	}

	@Override
	public void definePaths() {
		paths.add("/recentAssessments/{username}");
		paths.add("/recentAssessments/{username}/{status}/{id}");
	}
	
	@Override
	public void handleDelete(Request request, Response response) throws ResourceException {
		String username = (String) request.getAttributes().get("username");
		String status = (String) request.getAttributes().get("status");
		String id = (String) request.getAttributes().get("id");

		if (vfs.exists(new VFSPath("/users/" + username))) {
			final String url = "/users/" + username + "/recentlyViewed.xml";

			if (vfs.exists(new VFSPath(url))) {
				Document doc;
				try {
					doc = vfs.getDocument(new VFSPath(url));
				} catch (IOException e) {
					throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
				}
				
				NodeCollection nodes = new NodeCollection(doc.getElementsByTagName("assessment"));
				
				StringBuilder xml = new StringBuilder();
				xml.append("<recent>");
				
				for (Node node : nodes) {
					Element el = (Element) node;
					if (!(el.getAttribute("status").equals(status) && el.getTextContent().equals(id))) {
						xml.append("<assessment status=\"" + el.getAttribute("status") + "\">" + el.getTextContent()
								+ "</assessment>");
					}
				}
				
				xml.append("</recent>");
				
				if (DocumentUtils.writeVFSFile(url, vfs, xml.toString()))
					response.setStatus(Status.SUCCESS_OK);
				else
					throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
			} else {
				//FIXME: Maybe NOT_FOUND is more appropriate?
				response.setStatus(Status.SUCCESS_OK);
			}
		} else {
			response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
		}
	}
	
	@Override
	public Representation handleGet(Request request, Response response) throws ResourceException {
		String username = (String) request.getAttributes().get("username");
		
		final VFSPath url = 
			new VFSPath("/users/" + username + "/recentlyViewed.xml");
		
		if (!vfs.exists(url))
			throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);
		
		boolean thin = 
			"true".equals(request.getResourceRef().getQueryAsForm().getFirstValue("thin"));
		
		final Document document;
		try {
			document = vfs.getDocument(url); 
		} catch (IOException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
		if (thin)
			return new DomRepresentation(MediaType.TEXT_XML, document);
		else {
			final StringBuilder builder = new StringBuilder();
			builder.append("<recent>");
			final ElementCollection elements = new ElementCollection(document.getDocumentElement().getElementsByTagName("assessment"));
			for (Element el : elements) {
				String id = el.getTextContent();
				
				final Assessment assessment;
				try {
					assessment = SIS.get().getManager().loadObject(Assessment.class, Integer.valueOf(id));
					//SIS.get().getAssessmentIO().getAssessment(Integer.valueOf(id));
				} catch (PersistentException e) {
					continue;
				}
					
				if (assessment != null && assessment.getState() != Assessment.DELETED) {
					String region;
					if (assessment.isRegional()) {
						List<Integer> regions = assessment.getRegionIDs();
						if (regions.isEmpty())
							region = "(Unspecified Region)";
						else {
							Region r = SIS.get().getRegionIO().getRegion(regions.get(0));
							if (r == null)
								region = "(Invalid Region ID)";
							else if (regions.size() == 1)
								region = r.getName();
							else
								region = r.getName() + " + " + (regions.size() - 1) + " more...";
						}
						if (assessment.isEndemic())
							region += " -- Endemic";
					}
					else
						region = "Global";
					
					builder.append("<row>");
					builder.append("<field name=\"id\">" + id + "</field>");
					builder.append("<field name=\"status\">" + el.getAttribute("status") + "</field>");
					builder.append("<field name=\"species\"><![CDATA[" + assessment.getSpeciesName() + "]]></field>");
					builder.append("<field name=\"region\"><![CDATA[" + region + "]]></field>");
					builder.append("</row>");
				}
			}
			builder.append("</recent>");
			
			return new StringRepresentation(builder.toString(), MediaType.TEXT_XML);
		}
	}
	
	@Override
	public void handlePost(Representation entity, Request request, Response response) throws ResourceException {
		String username = (String) request.getAttributes().get("username");
		
		String xml;
		try {
			xml = request.getEntity().getText();
		} catch (Exception e) {
			throw new ResourceException(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY, e);
		}
		
		final VFSPath url = 
			new VFSPath("/users/" + username + "/recentlyViewed.xml");
		
		if (!vfs.exists(url.getCollection())) {
			try {
				vfs.makeCollections(url);
			} catch (IOException e) {
				throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Directory not found and could not be created.");
			}
		}

		if (DocumentUtils.writeVFSFile("/users/" + username + "/recentlyViewed.xml", vfs, xml)) {
			response.setEntity("<html><head></head><body>Recently viewed "
				+ "assessments were saved just fine.</body></html>", MediaType.TEXT_HTML);
			response.setStatus(Status.SUCCESS_OK);
		}
		else
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Could not save");

	}

}
