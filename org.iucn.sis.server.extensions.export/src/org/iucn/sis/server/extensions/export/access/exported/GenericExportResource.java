package org.iucn.sis.server.extensions.export.access.exported;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.Properties;

import javax.naming.NamingException;

import org.gogoego.api.plugins.GoGoEgo;
import org.hibernate.Session;
import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.server.api.io.WorkingSetIO;
import org.iucn.sis.server.api.restlets.TransactionResource;
import org.iucn.sis.shared.api.models.WorkingSet;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.representation.OutputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;

import com.solertium.db.DBSession;
import com.solertium.db.DBSessionFactory;
import com.solertium.db.ExecutionContext;
import com.solertium.db.SystemExecutionContext;
import com.solertium.util.restlet.CookieUtility;

public class GenericExportResource extends TransactionResource {
	
	private final String schema;
	private final String workingSet;

	public GenericExportResource(Context context, Request request, Response response) {
		super(context, request, response);
		schema = (String)request.getAttributes().get("source");
		workingSet = (String)request.getAttributes().get("workingSet");
	}

	@Override
	public Representation represent(Variant variant, Session session) throws ResourceException {
		if (schema == null || workingSet == null)
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
		
		final Integer workingSetID;
		try {
			workingSetID = Integer.valueOf(workingSet);
		} catch (Exception e) {
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Please supply a valid working set ID");
		}
		
		final WorkingSetIO io = new WorkingSetIO(session);
		final WorkingSet workingSet = io.readWorkingSet(workingSetID);
		if (workingSet == null)
			throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "Working set " + workingSet + " was not found");
		
		String name = "export_"+Calendar.getInstance().getTimeInMillis()+"_"+CookieUtility.newUniqueID();
		if (DBSessionFactory.isRegistered(name))
			DBSessionFactory.unregisterDataSource(name);
		
		Properties properties = GoGoEgo.getInitProperties();
		Properties sourceProperties = new Properties();
		for (Object key : properties.keySet()) {
			String keyName = (String)key;
			if (keyName.startsWith("dbsession.sis.")) {
				String last = keyName.split("\\.")[2];
				String newName = "dbsession." + name + "." + last;
				sourceProperties.setProperty(newName, properties.getProperty(keyName));
			}
		}
		
		ExecutionContext source;
		try {
			DBSessionFactory.registerDataSource(name, sourceProperties);
			DBSession sourceDB = DBSessionFactory.getDBSession(name);
			sourceDB.setSchema(schema);
			
			source = new SystemExecutionContext(sourceDB);
			source.setAPILevel(ExecutionContext.SQL_ALLOWED);
			source.setExecutionLevel(ExecutionContext.ADMIN);
		} catch (NamingException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
		
		final PipedInputStream inputStream = new PipedInputStream(); 
		final Representation representation = new OutputRepresentation(MediaType.TEXT_PLAIN) {
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
		 * TODO: determine what the target and template 
		 * should be...
		 */
		
		
		GenericExporter exporter = new GenericExporter(session, source, workingSet);
		exporter.setOutputStream(writer, "\n");
		try {
			exporter.setTarget(createH2Target(name));
		} catch (NamingException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
		
		new Thread(exporter).start();
		
		return representation;
	}
	
	@SuppressWarnings("deprecation")
	private String createH2Target(String source) throws ResourceException {
		String location = SIS.get().getSettings(getContext()).getProperty("org.iucn.sis.server.extensions.export.location", "export");
		String name = source + "_target";
		
		Properties properties = new Properties();
		properties.setProperty("dbsession." + name + ".uri", "jdbc:h2:file://" + location + "/" + name);
		properties.setProperty("dbsession." + name + ".driver", "org.h2.Driver");
		properties.setProperty("dbsession." + name + ".user", "sa");
		properties.setProperty("dbsession." + name + ".password", "");
		
		try {
			DBSessionFactory.registerDataSource(name, properties);
		} catch (NamingException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
		
		return name;
	}
}
