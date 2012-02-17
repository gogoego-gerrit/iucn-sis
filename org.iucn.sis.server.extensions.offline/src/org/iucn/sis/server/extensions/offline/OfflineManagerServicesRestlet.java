package org.iucn.sis.server.extensions.offline;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.Properties;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.server.extensions.offline.OfflineToOnlineImporter.OfflineImportMode;
import org.iucn.sis.server.extensions.offline.manager.Resources;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.fileupload.RestletFileUpload;
import org.restlet.representation.OutputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ResourceException;

import com.solertium.util.Replacer;
import com.solertium.util.TrivialExceptionHandler;

public class OfflineManagerServicesRestlet extends Restlet {

	public OfflineManagerServicesRestlet(Context context) {
		super(context);
	}
	
	public void handle(Request arg0, Response arg1) {
		if (Method.POST.equals(arg0.getMethod()))
			handlePost(arg0, arg1);
		else if (Method.GET.equals(arg0.getMethod()))
			handleGet(arg0, arg1);
		else
			arg1.setStatus(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
	}
	
	public void handleGet(Request arg0, Response arg1) {
		final String service = (String) arg0.getAttributes().get("service");
		
		arg1.setStatus(Status.SUCCESS_OK);
		
		if ("import".equals(service)) {
			Form form = arg0.getResourceRef().getQueryAsForm();
			
			String username = form.getFirstValue("username");
			OfflineImportMode mode = "remove".equals(form.getFirstValue("mode")) ? 
					OfflineImportMode.REMOVE : OfflineImportMode.RESYNC;
			
			try {
				arg1.setEntity(importToOnline(username, mode));
			} catch (ResourceException e) {
				arg1.setEntity(getResultPage(e.getMessage()));
			} catch (Exception e) {
				arg1.setEntity(getResultPage("Error starting sync: " + e.getMessage()));
			}
		}
		else
			arg1.setEntity(getResultPage("Service not found: " + service));
	}
	
	public void handlePost(Request arg0, Response arg1) {
		final String service = (String) arg0.getAttributes().get("service");
		final String message;
		try {
			if ("upload".equals(service))
				message = upload(arg0, arg1);
			else if ("backup".equals(service))
				message = backup(arg0, arg1);
			else if ("restore".equals(service))
				message = restore(arg0, arg1);
			else
				message = "Service not found: " + service;
		} catch (ResourceException e){
			arg1.setStatus(Status.SUCCESS_OK);
			arg1.setEntity(getResultPage("Service failed due to server error: " + e.getMessage()));
			
			return;
		}
		
		arg1.setStatus(Status.SUCCESS_OK);
		arg1.setEntity(getResultPage(message));
	}
	
	private Representation importToOnline(final String username, final OfflineImportMode mode) throws IOException, ResourceException {
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
		
		Properties settings = SIS.get().getSettings(getContext());
		
		for (String key : OfflineSettings.REQUIRED)
			if (isBlank(settings.getProperty(key)))
				throw new ResourceException(Status.SERVER_ERROR_SERVICE_UNAVAILABLE, "No target database has been specified.");
		
		Properties properties = new Properties();
		properties.setProperty("database_dialect", settings.getProperty(OfflineSettings.DIALECT,"org.hibernate.dialect.PostgreSQLDialect"));
		properties.setProperty("dbsession.sis_target.uri", settings.getProperty(OfflineSettings.URL));
		properties.setProperty("dbsession.sis_target.driver", settings.getProperty(OfflineSettings.DRIVER,"org.postgresql.Driver"));
		properties.setProperty("dbsession.sis_target.user", settings.getProperty(OfflineSettings.USER));
		properties.setProperty("dbsession.sis_target.password", settings.getProperty(OfflineSettings.PASSWORD));
		
		OfflineToOnlineImporter importer = new OfflineToOnlineImporter(username, properties);
		importer.setOutputStream(writer, "<br/>");
		importer.setMode(mode);
		
		new Thread(importer).start();
		
		return representation;
	}
	
	private String restore(Request arg0, Response arg1) throws ResourceException {
		final Form form;
		try {
			form = new Form(arg0.getEntity());
		} catch (Exception e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Failed to parse information from form.", e);
		}
		
		String value = form.getFirstValue("database");
		if (value == null)
			return "No database backup specified.";
		
		try {
			OfflineBackupWorker.restore(value);
		} catch (Exception e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Failed to restore backup.", e);
		}
		
		return "Backup restored successfully from " + value;
	}
	
	private String upload(Request arg0, Response arg1) throws ResourceException {
		RestletFileUpload fileUpload = new RestletFileUpload(new DiskFileItemFactory());
		List<FileItem> list;
		try {
			list = fileUpload.parseRequest(arg0);
		} catch (FileUploadException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
		
		boolean found = false;
		for (FileItem item : list) {
			if (!item.isFormField()) {
				//Copy to temp directory, unzip, then move.
				try {
					OfflineBackupWorker.upload(item);
					found = true;
				} catch (Exception e) {
					throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
				}
			}
		}
		
		if (found)
			return "Upload successful.";
		else
			return "No file found to upload.";
	}
	
	private String backup(Request arg0, Response arg1) throws ResourceException {
		String location = OfflineBackupWorker.backup();
		if (location == null)
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Data failed to back up.");
		
		return "Data successfully backed up to " + location;
	}
	
	private Representation getResultPage(String message) {
		final BufferedReader reader = new BufferedReader(new InputStreamReader(Resources.get("result.html")));
		final StringBuilder in = new StringBuilder();
		String line = null;
		
		try {
			while ((line = reader.readLine()) != null)
				in.append(line);
		} catch (Exception e) {
			TrivialExceptionHandler.impossible(this, e);
		}
		
		String value = in.toString();
		value = Replacer.replace(value, "$message", message);
		
		return new StringRepresentation(value, MediaType.TEXT_HTML);
	}
	
	private boolean isBlank(String value) {
		return value == null || "".equals(value);
	}

}
