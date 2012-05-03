package org.iucn.sis.server.extensions.export.access.exported;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Properties;

import javax.naming.NamingException;

import org.hibernate.Session;
import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.server.api.io.WorkingSetIO;
import org.iucn.sis.server.api.persistance.SISPersistentManager;
import org.iucn.sis.shared.api.debug.Debug;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.Taxon;
import org.iucn.sis.shared.api.models.WorkingSet;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;

import com.solertium.db.DBException;
import com.solertium.db.DBSession;
import com.solertium.db.DBSessionFactory;
import com.solertium.db.ExecutionContext;
import com.solertium.db.SystemExecutionContext;

public class AccessCopyViaJackcess extends AccessExporterViaJackcess {
	
	private final Properties sourceProperties;
	private final String sessionName;
	
	public AccessCopyViaJackcess(ExecutionContext source, Integer workingSetID,
			String sessionName, Properties sourceProperties, String location, String fileName) throws IOException {
		super(source, workingSetID, location, fileName);
		this.ignorePrefix = "vw_";
		this.sourceProperties = sourceProperties;
		this.sessionName = sessionName;
	}
	
	protected void afterRun() throws DBException {
		final Session session;
		try {
			session = SIS.get().getManager().openSession();
		} catch (Exception e) {
			throw new DBException("Could not open SIS session.");
		}
		
		final WorkingSet workingSet = new WorkingSetIO(session).readWorkingSet(workingSetID);
		
		createTable("taxon");
		
		try {
			final HashSet<Integer> seen = new HashSet<Integer>();
			for (Taxon taxon : workingSet.getTaxon())
				insertTaxa(taxon, seen);
		} finally {
			session.close();
		}
		
		if (location == null) {
			write("Your database has been exported successfully.");
			return;
		};
		
		write("Write complete, zipping results...");
		
		try {
			write("--- Complete ---");
			write("You can now download your working set.");
			write("<a target=\"blank\" href=\"/apps/org.iucn.sis.server.extensions.export/downloads/%s\">Click here to download</a>", zip());
		} catch (Exception e) {
			write("Failed to zip database");
		}
		
		inAfterRun = false;
	}
	
	@Override
	protected void execute() throws Throwable {
		Session session = SISPersistentManager.instance().openSession();
		WorkingSet ws = (WorkingSet) session.load(WorkingSet.class, workingSetID);
		
		AccessViewBuilder builder = new AccessViewBuilder(ws, source);
		builder.setOutputStream(writer, lineBreakRule);
		builder.build();
		
		ExecutionContext source;
		try {
			DBSessionFactory.registerDataSource(sessionName, sourceProperties);
			DBSession sourceDB = DBSessionFactory.getDBSession(sessionName);
			sourceDB.setSchema(builder.getSchema());
			sourceDB.setAllowedTableTypes("TABLE", "VIEW");
			
			source = new SystemExecutionContext(sourceDB);
			source.setAPILevel(ExecutionContext.SQL_ALLOWED);
			source.setExecutionLevel(ExecutionContext.ADMIN);
		} catch (NamingException e) {
			builder.destroy();
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
		
		setSource(source);
		
		try {
			final Collection<String> allTables = source.getDBSession().listTables(source);
			write("Starting process of copying %s tables...", allTables.size());
			for (String table : allTables) {
				write("Copying table %s", table);
				createAndCopyTable(table, getQuery(table), getRowProcessor(table));
			}
			
			afterRun();
		} catch (DBException e) {
			write("Failed to copy assessment data: %s", e.getMessage());
			Debug.println(e);
			return;
		} finally {
			builder.destroy();
		}
	}
	
	@Override
	protected void insertAssessment(Session session, Assessment assessment) throws DBException {
		
	}

}
