package org.iucn.sis.viewmaster;

import static org.gogoego.util.db.fluent.Statics.configure;

import org.gogoego.util.db.fluent.Connection;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

	public void start(BundleContext context) throws Exception {
		
		final Connection c = configure(
				"sistest_clean_20110601",
				"**URL**",
				"**USERNAME**",
				"**PASSWORD**");

		final Connection l = configure(
				"sis_lookups",
				"**URL**",
				"**USERNAME**",
				"**PASSWORD**");
		
		final String targetSchema = "vw_published";
		final String targetUser = "iucn_published";
		
		UniverseBuilder universe = new UniverseBuilder();
		universe.build(c, l, targetSchema, targetUser);

		/*
		 * The old school method
		 */
		SingleTableViewBuilder byTable = new SingleTableViewBuilder();
		byTable.build(c, targetSchema, targetUser);
		
		/*
		 * Each column is its own view
		 */
		SingleColumnViewBuilder byColumn = new SingleColumnViewBuilder();
		byColumn.build(c, targetSchema, targetUser);
		
		/*
		 * Re-creates the old school way based on the views.
		 * FIXME: not done yet
		 */
		AggregatedTableViewBuilder aggregated = new AggregatedTableViewBuilder();
		//aggregated.build(c);
		
		/*
		 * Build up the taxonomy views
		 */
		TaxonomyViewBuilder taxonomy = new TaxonomyViewBuilder();
		taxonomy.build(c, targetSchema, targetUser);
		
		/*
		 * Quickie additional views
		 */
		AdditionalViewBuilder additional = new AdditionalViewBuilder();
		additional.addFile("additional.sql");
		additional.addFile("attachments.sql");
		additional.build(c, targetSchema, targetUser);
		
		if (!"public".equals(targetSchema)) {
			VWFilterViewBuilder filter = new VWFilterViewBuilder();
			filter.build(c, targetSchema, targetUser);
		}
	}

	public void stop(BundleContext context) throws Exception {
	}

}
