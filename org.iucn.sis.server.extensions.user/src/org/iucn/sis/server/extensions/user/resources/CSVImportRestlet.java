package org.iucn.sis.server.extensions.user.resources;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.List;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.hibernate.Session;
import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.server.api.application.SISDBAuthenticator;
import org.iucn.sis.server.api.io.UserIO;
import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
import org.iucn.sis.server.api.restlets.BaseServiceRestlet;
import org.iucn.sis.server.extensions.user.utils.ImportFromCSV;
import org.iucn.sis.shared.api.debug.Debug;
import org.iucn.sis.shared.api.models.User;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.fileupload.RestletFileUpload;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ResourceException;

public class CSVImportRestlet extends BaseServiceRestlet {

	public CSVImportRestlet(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void definePaths() {
		paths.add("/import/csv");
	}
	
	@Override
	public Representation handleGet(Request request, Response response, Session session) throws ResourceException {
		final StringBuilder html = new StringBuilder();
		html.append("<h3>Please select a spreadsheet or CSV file to upload:</h3>");
		html.append("<br/>");
		html.append("<form enctype=\"multipart/form-data\" method=\"POST\">");
		html.append("<input type=\"file\" name=\"file\" /><br/><br/>");
		html.append("<input type=\"submit\" value=\"Submit (click only once)\"/>");
		html.append("</form>");
		
		return new StringRepresentation(wrapInHTML(html.toString()), MediaType.TEXT_HTML);
	}
	
	@Override
	public void handlePost(Representation entity, Request request, Response response, Session session) throws ResourceException {
		RestletFileUpload fileUpload = new RestletFileUpload(new DiskFileItemFactory());
		
		final List<FileItem> items;
		try {
			items = fileUpload.parseRepresentation(entity);
		} catch (FileUploadException e) {
			Debug.println(e);
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, e);
		}
		
		final UserIO userIO = new UserIO(session);
		
		for (FileItem item : items) {
			if (!item.isFormField()) {
				final StringWriter writer = new StringWriter();
				
				final SISDBAuthenticator authenticator = 
					new SISDBAuthenticator(SIS.get().getExecutionContext());
				
				ImportFromCSV worker = new ImportFromCSV();
				worker.setOutputStream(writer, "<br/>");
				worker.setAddProfileListener(new ImportFromCSV.UserEvent() {
					public boolean addUser(User user) {
						User existing = userIO.getUserFromUsername(user.getUsername());
						if (existing == null || existing.state == User.DELETED) {
							try {
								return userIO.saveUser(user);
							} catch (PersistentException e) {
								return false;
							}
						}
						else
							return false;
					}
				});
				worker.setAddUserListener(new ImportFromCSV.UserEvent() {
					public boolean addUser(User user) {
						User existing = userIO.getUserFromUsername(user.getUsername());
						if (existing == null || existing.state == User.DELETED) {
							String password = user.getPassword();
							if (password == null || "".equals(password))
								password = "changeme";
							
							user.setPassword(authenticator.translatePassword(user.getUsername(), password));
							
							try {
								return userIO.saveUser(user);
							} catch (PersistentException e) {
								Debug.println("Failed to save new user: {0}", e);
								return false;
							}
						} else {
							return false;
						}
					}
				});
				try {
					worker.importUsers(new InputStreamReader(item.getInputStream()));
				} catch (IOException e) {
					throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
				}
				
				response.setStatus(Status.SUCCESS_OK);
				response.setEntity(wrapInHTML(writer.toString()), MediaType.TEXT_HTML);
				
				return;
			}
		}
		
		throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Please supply a CSV file to import.");
	}
	
	private String wrapInHTML(String value) {
		final StringBuilder html = new StringBuilder();
		html.append("<html>");
		html.append("<head><title>Upload</title></head>");
		html.append("<body bgcolor=\"white\">");
		html.append(value);
		html.append("</body>");
		html.append("<html>");
		
		return html.toString();
	}

}
