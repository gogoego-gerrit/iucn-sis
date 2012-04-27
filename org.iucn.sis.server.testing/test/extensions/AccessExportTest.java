package extensions;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Properties;

import javax.naming.NamingException;

import org.hibernate.Session;
import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.server.extensions.export.access.exported.AccessCopyViaJackcess;
import org.iucn.sis.server.extensions.export.access.exported.AccessExporter;
import org.iucn.sis.server.extensions.export.access.exported.AccessExporterViaJackcess;
import org.iucn.sis.server.extensions.export.access.exported.AccessViewBuilder;
import org.iucn.sis.shared.api.models.WorkingSet;
import org.junit.Test;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;

import com.solertium.db.DBSession;
import com.solertium.db.DBSessionFactory;
import com.solertium.db.ExecutionContext;
import com.solertium.db.SystemExecutionContext;
import com.solertium.util.restlet.CookieUtility;

import core.BasicHibernateTest;

public class AccessExportTest extends BasicHibernateTest {
	
	@Test
	public void testGenerate() throws Exception {
		Session session = openSession();
		WorkingSet ws = (WorkingSet) session.load(WorkingSet.class, 16509077);
		AccessViewBuilder builder = new AccessViewBuilder(ws, SIS.get().getExecutionContext(), "access_test");
		builder.setFailOnUpdateError(true);
		builder.destroy();
		builder.build();
		closeSession(session);
	}
	
	@Test
	public void run() throws ResourceException {
		Integer workingSetID = 10912542;
		
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
		
		ExecutionContext source;
		try {
			DBSessionFactory.registerDataSource(name, sourceProperties);
			DBSession sourceDB = DBSessionFactory.getDBSession(name);
			sourceDB.setSchema("access");
			sourceDB.setAllowedTableTypes("TABLE", "VIEW");
			
			source = new SystemExecutionContext(sourceDB);
			source.setAPILevel(ExecutionContext.SQL_ALLOWED);
			source.setExecutionLevel(ExecutionContext.ADMIN);
		} catch (NamingException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
		
		String folder = createTempFolder(name);
		AccessExporter exporter = new AccessExporter(source, workingSetID, folder, name + ".mdb");
		//exporter.setOutputStream(writer, "<br/>");
		try {
			//exporter.setTarget(createH2Target(name));
			//exporter.setTarget(createPostgresTestTarget(name));
			exporter.setTarget(createAccessTarget(folder, name));
		} catch (NamingException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		} catch (IOException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
		
		exporter.run();
		
	}
	
	@Test
	public void runJackessCopy() throws ResourceException, IOException {
		Integer workingSetID = 16509077;
		
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
		
		String folder = createTempFolder(name);
		createAccessTarget(folder, name);
		
		AccessCopyViaJackcess exporter = new AccessCopyViaJackcess(SIS.get().getExecutionContext(), 
				workingSetID, name, sourceProperties, folder, name + ".mdb");
		exporter.setTarget(new SystemExecutionContext());
		//exporter.setOutputStream(writer, "<br/>");
		
		exporter.run();
	}
	
	@Test
	public void runJackcess() throws ResourceException, IOException {
		Integer workingSetID = 10912542;
		
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
		
		ExecutionContext source;
		try {
			DBSessionFactory.registerDataSource(name, sourceProperties);
			DBSession sourceDB = DBSessionFactory.getDBSession(name);
			sourceDB.setSchema("access");
			sourceDB.setAllowedTableTypes("TABLE", "VIEW");
			
			source = new SystemExecutionContext(sourceDB);
			source.setAPILevel(ExecutionContext.SQL_ALLOWED);
			source.setExecutionLevel(ExecutionContext.ADMIN);
		} catch (NamingException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
		
		String folder = createTempFolder(name);
		createAccessTarget(folder, name);
		
		AccessExporterViaJackcess exporter = new AccessExporterViaJackcess(source, workingSetID, folder, name + ".mdb");
		exporter.setTarget(new SystemExecutionContext());
		//exporter.setOutputStream(writer, "<br/>");
		
		exporter.run();
		
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
		
		file.setReadable(true, false);
		file.setWritable(true, false);
		file.setExecutable(true, false);
		
		String name = source + "_target";
		
		Properties properties = new Properties();
		properties.setProperty("dbsession." + name + ".uri", "jdbc:odbc:Driver={Microsoft Access Driver (*.mdb)};DBQ="+file.getAbsolutePath());
		properties.setProperty("dbsession." + name + ".driver", "sun.jdbc.odbc.JdbcOdbcDriver");
		properties.setProperty("dbsession." + name + ".uri", "jdbc:access:////"+file.getAbsolutePath());
		properties.setProperty("dbsession." + name + ".driver", "com.hxtt.sql.access.AccessDriver");
		properties.setProperty("dbsession." + name + ".user", "");
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
