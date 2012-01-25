package org.iucn.sis.viewmaster;

import static org.gogoego.util.db.fluent.Statics.configure;

import java.util.ArrayList;
import java.util.List;

import org.gogoego.util.db.DBSessionFactory;
import org.gogoego.util.db.fluent.Connection;
import org.gogoego.util.getout.GetOut;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {
	
	private static final String USER = "**USER**";
	private static final String PASSWORD = "**PASSWORD**";

	public void start(BundleContext context) throws Exception {
		
		final Connection c = configure(
				"application",
				"**URL**",
				USER,
				PASSWORD);

		final Connection l = configure(
				"lookups",
				"**URL**",
				USER,
				PASSWORD);
		
		List<SchemaUser> list = new ArrayList<SchemaUser>();
		list.add(new SchemaUser("public", "iucn"));
		list.add(new SchemaUser("vw_published", "iucn"));
		
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
		
		AccessViewBuilder access = new AccessViewBuilder();
		access.build(c);
		
		GetOut.log("Closing connections.");
		
		DBSessionFactory.unregisterDataSource("application");
		DBSessionFactory.unregisterDataSource("lookups");
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
