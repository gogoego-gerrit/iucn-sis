package org.iucn.sis.viewmaster;

import static org.gogoego.util.db.fluent.Statics.configure;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.gogoego.util.db.DBSessionFactory;
import org.gogoego.util.db.fluent.Connection;
import org.gogoego.util.db.postgresql.PostgreSQLDBSessionFactory;
import org.gogoego.util.getout.GetOut;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {
	
	public static void main(String[] args) throws Exception {
		PostgreSQLDBSessionFactory.getInstance().register();
		new Activator().start(null);
	}

	public void start(BundleContext context) throws Exception {
		final File file = new File("/var/sis/viewmaster/db.properties");
		if (!file.exists())
			throw new Exception("Database properties file missing from " + file.getAbsolutePath() + ". Please specify properties.");
		
		final Properties properties = new Properties();
		properties.load(new FileReader(file));
		
		final Connection c = configureConnection("application", properties);
		final Connection l = configureConnection("lookups", properties);
		
		final String user = properties.getProperty("viewmaster.generate.user", "public");
		
		List<SchemaUser> list = new ArrayList<SchemaUser>();
		for (String schema : properties.getProperty("viewmaster.generate.schema").split(","))
			list.add(new SchemaUser(schema.trim(), user));
		
		for (SchemaUser current : list) {
			String targetSchema = current.targetSchema, targetUser = current.targetUser;
			
			UniverseBuilder universe = new UniverseBuilder();
			universe.build(c, l, targetSchema, targetUser);
	
			//The old school method
			SingleTableViewBuilder byTable = new SingleTableViewBuilder();
			//byTable.build(c, targetSchema, targetUser);
			
			// Each column is its own view
			SingleColumnViewBuilder byColumn = new SingleColumnViewBuilder();
			byColumn.build(c, targetSchema, targetUser);
			
			// Re-creates the old school way based on the views.
			// FIXME: not done yet
			AggregatedTableViewBuilder aggregated = new AggregatedTableViewBuilder();
			//aggregated.build(c);
			
			// Build up the taxonomy views
			TaxonomyViewBuilder taxonomy = new TaxonomyViewBuilder();
			taxonomy.build(c, targetSchema, targetUser);
			
			// Quickie additional views
			AdditionalViewBuilder additional = new AdditionalViewBuilder();
			additional.addFile("additional.sql");
			additional.addFile("attachments.sql");
			additional.build(c, targetSchema, targetUser);
			
			if (!"public".equals(targetSchema)) {
				VWFilterViewBuilder filter = new VWFilterViewBuilder();
				filter.build(c, targetSchema, targetUser);
			}
		}
		
		IntegrityViewBuilder integrity = new IntegrityViewBuilder();
		integrity.build(c);
		
		GetOut.log("Closing connections.");
		
		DBSessionFactory.unregisterDataSource("application");
		DBSessionFactory.unregisterDataSource("lookups");
	}
	
	private Connection configureConnection(String name, Properties properties) {
		return configure(name, 
			properties.getProperty("viewmaster." + name + ".url"), 
			properties.getProperty("viewmaster." + name + ".user"), 
			properties.getProperty("viewmaster." + name + ".password")
		);
	}

	public void stop(BundleContext context) throws Exception {
	}
	
	private static class SchemaUser {
		
		public String targetSchema, targetUser;
		
		public SchemaUser(String targetSchema, String targetUser) {
			this.targetSchema = targetSchema;
			this.targetUser = targetUser;
		}
		
	}

}
