package org.iucn.sis.server.extensions.offline;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintWriter;
import java.util.Properties;

import org.hibernate.Session;
import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.server.api.io.UserIO;
import org.iucn.sis.server.api.restlets.BaseServiceRestlet;
import org.iucn.sis.shared.api.models.User;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.representation.OutputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;

public class OfflineRestlet extends BaseServiceRestlet {
	
	public OfflineRestlet(Context context) {
		super(context);
	}
	
	@Override
	public void definePaths() {
		paths.add("/offline/importToLive/{username}/{workingsetID}");
	}
	
	@Override
	public Representation handleGet(Request request, Response response, Session session) throws ResourceException {

		String username = (String) request.getAttributes().get("username");
		String workingSetID = (String) request.getAttributes().get("workingsetID");
					
		UserIO userIO = new UserIO(session);
	
		try {
			return importToOnline(userIO.getUserFromUsername(username), Integer.valueOf(workingSetID), 
						response, request, session);
		} catch (IOException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
		
	}
		
	private Representation importToOnline(final User user, final Integer workingsetID, final Response response,
			final Request request, Session session) throws IOException, ResourceException {

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
		
		/*
		 * This should point to the Live SIS database, or whatever 
		 * the target database is for importing data into.
		 * 
		 * Declaring properties here to avoid using Context.getCurrent.  
		 * Also, making this easier to test via JUnit since now we can 
		 * pass in properties with strings instead of relying on the 
		 * Restlet Context object, that requires an Application, that 
		 * requires Bootstrap, that requires... you get the idea.
		 */
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
		
		OfflineToOnlineImporter importer = new OfflineToOnlineImporter(workingsetID, properties);
		importer.setOutputStream(writer, "<br/>");
		
		new Thread(importer).start();
		
		return representation;
	}
	
	private boolean isBlank(String value) {
		return value == null || "".equals(value);
	}
	
}
