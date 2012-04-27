package org.iucn.sis.server.extensions.export.access.exported;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.Properties;

import javax.naming.NamingException;

import org.hibernate.Session;
import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.server.api.restlets.TransactionResource;
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
import com.solertium.util.TrivialExceptionHandler;
import com.solertium.util.restlet.CookieUtility;

@SuppressWarnings("deprecation")
public class GenericExportResource extends TransactionResource {
	
	private final String schema;
	private final String workingSet;

	public GenericExportResource(Context context, Request request, Response response) {
		super(context, request, response);
		schema = (String)request.getAttributes().get("source");
		workingSet = (String)request.getAttributes().get("working-set");
		
		getVariants().add(new Variant(MediaType.TEXT_XML));
	}

	public Representation represent(Variant variant, Session session) throws ResourceException {
		if (schema == null || workingSet == null)
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
		
		final Integer workingSetID;
		try {
			workingSetID = Integer.valueOf(workingSet);
		} catch (Exception e) {
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Please supply a valid working set ID");
		}
		
		return copyViaJackcess(workingSetID);
	}
	
	public Representation copyViaJackcess(final Integer workingSetID) throws ResourceException {
		String name = "export_"+Calendar.getInstance().getTimeInMillis()+"_"+CookieUtility.newUniqueID();
		if (DBSessionFactory.isRegistered(name))
			DBSessionFactory.unregisterDataSource(name);
		
		Properties properties = SIS.get().getSettings(null);
		Properties sourceProperties = new Properties();
		for (Object key : properties.keySet()) {
			String keyName = (String)key;
			if (keyName.startsWith("dbsession.sis.")) {
				String last = keyName.split("\\.")[2];
				String newName = "dbsession." + name + "." + last;
				sourceProperties.setProperty(newName, properties.getProperty(keyName));
			}
		}
		
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
		
		final PrintWriter writer;
		try {
			writer = new PrintWriter(new OutputStreamWriter(new PipedOutputStream(inputStream)), true);
		} catch (IOException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
		
		String folder = createTempFolder(name);
		try {
			createAccessTarget(folder, name);
		} catch (IOException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
		
		final AccessCopyViaJackcess exporter;
		try {
			exporter = new AccessCopyViaJackcess(SIS.get().getExecutionContext(), 
				workingSetID, name, sourceProperties, folder, name + ".mdb");
		} catch (IOException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Could not create target database.", e);
		}		
		exporter.setTarget(new SystemExecutionContext());
		exporter.setOutputStream(writer, "<br/>");
		
		new Thread(exporter).start();
		
		return representation;
	}
	
	/**
	 * @deprecated use copyViaJackcess instead.
	 */
	@SuppressWarnings("unused")
	public Representation exportViaJackcess(final Integer workingSetID) throws ResourceException {
		String name = "export_"+Calendar.getInstance().getTimeInMillis()+"_"+CookieUtility.newUniqueID();
		if (DBSessionFactory.isRegistered(name))
			DBSessionFactory.unregisterDataSource(name);
		
		Properties properties = SIS.get().getSettings(getContext());
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
			sourceDB.setAllowedTableTypes("TABLE", "VIEW");
			
			source = new SystemExecutionContext(sourceDB);
			source.setAPILevel(ExecutionContext.SQL_ALLOWED);
			source.setExecutionLevel(ExecutionContext.ADMIN);
		} catch (NamingException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
		
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
		
		final PrintWriter writer;
		try {
			writer = new PrintWriter(new OutputStreamWriter(new PipedOutputStream(inputStream)), true);
		} catch (IOException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
		
		final String folder = createTempFolder(name);
		final String target;
		try {
			//exporter.setTarget(createH2Target(name));
			//exporter.setTarget(createPostgresTestTarget(name));
			target = createAccessTarget(folder, name);
		} catch (IOException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
		
		/*final AccessExporter exporter = new AccessExporter(source, workingSetID, folder, name + ".mdb");
		exporter.setOutputStream(writer, "<br/>");
		try {
			exporter.setTarget(target);
		} catch (NamingException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}*/
		
		final AccessExporterViaJackcess exporter;
		try {
			exporter = new AccessExporterViaJackcess(source, workingSetID, folder, name + ".mdb");
		} catch (IOException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Could not create target database.", e);
		}
		exporter.setOutputStream(writer, "<br/>");
		exporter.setTarget(new SystemExecutionContext());
		
		new Thread(exporter).start();
		
		return representation;
	}
	
	public static String createAccessTarget(String folderName, String source) throws IOException, ResourceException {
		final File folder = new File(folderName);
		final File file = new File(folder, source + ".mdb");
		final InputStream is = AccessExporter.getTemplate();
		final OutputStream os = new BufferedOutputStream(
			new FileOutputStream(file));
		
		final byte[] buf = new byte[65536];
		int i = 0;
		while ((i = is.read(buf)) != -1)
			os.write(buf, 0, i);
		
		is.close();
		os.close();
		
		String name = source + "_target";
		
		Properties properties = new Properties();
		boolean useJdbcObdc = false;
		try {
			Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
			useJdbcObdc = true;
		} catch (Exception e) {
			TrivialExceptionHandler.ignore(new Object(), e);
		}
		/* This only works on certain Java installations :( */
		if (useJdbcObdc) {
			properties.setProperty("dbsession." + name + ".uri", "jdbc:odbc:Driver={Microsoft Access Driver (*.mdb)};DBQ="+file.getAbsolutePath());
			properties.setProperty("dbsession." + name + ".driver", "sun.jdbc.odbc.JdbcOdbcDriver");
		}
		else {
			properties.setProperty("dbsession." + name + ".uri", "jdbc:access:////"+file.getAbsolutePath());
			properties.setProperty("dbsession." + name + ".driver", "com.hxtt.sql.access.AccessDriver");
		}
		properties.setProperty("dbsession." + name + ".user", "");
		properties.setProperty("dbsession." + name + ".password", "");
		
		try {
			DBSessionFactory.registerDataSource(name, properties);
		} catch (NamingException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
		
		return name;
	}
	
	@SuppressWarnings("unused")
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
	
	@SuppressWarnings("unused")
	private String createPostgresTestTarget(String source) throws ResourceException {
		String location = "localhost:5432";
		String name = source + "_target";
		
		Properties properties = new Properties();
		properties.setProperty("dbsession." + name + ".uri", "jdbc:postgresql://" + location + "/sis_target");
		properties.setProperty("dbsession." + name + ".driver", "org.postgresql.Driver");
		properties.setProperty("dbsession." + name + ".user", "sa");
		properties.setProperty("dbsession." + name + ".password", "");
		
		try {
			DBSessionFactory.registerDataSource(name, properties);
		} catch (NamingException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
		
		return name;
	}
	
	private String createTempFolder(String fileName) throws ResourceException {
		File tmp;
		try {
			tmp = File.createTempFile("toDelete", "tmp");
		} catch (IOException e) {
			throw new ResourceException(Status.SERVER_ERROR_INSUFFICIENT_STORAGE, e);
		}
		
		File folder = tmp.getParentFile();
		File tmpFolder = new File(folder, fileName);
		tmpFolder.mkdirs();
		
		tmp.delete();
		
		return tmpFolder.getAbsolutePath();
	}
}
