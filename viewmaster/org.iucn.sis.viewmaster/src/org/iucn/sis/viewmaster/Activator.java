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
		
		UniverseBuilder universe = new UniverseBuilder();
		universe.build(c, l);

		/*
		c.update("DROP FUNCTION IF EXISTS getPrimitiveId(a int, f varchar(255), t varchar(255), n varchar(255)) CASCADE");

		BufferedReader br = new BufferedReader(new InputStreamReader(
			getClass().getResourceAsStream("getPrimitiveId.sql")
		));
		String in = "";
		StringBuilder sql = new StringBuilder();
		while(in != null){
			sql.append(in+"\n");
			in = br.readLine();
		}
		br.close();
		
		c.update(sql.toString());
		*/

		/*
		 * The old school method
		 */
		SingleTableViewBuilder byTable = new SingleTableViewBuilder();
		byTable.build(c);
		
		/*
		 * Each column is its own view
		 */
		SingleColumnViewBuilder byColumn = new SingleColumnViewBuilder();
		byColumn.build(c);
		
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
		taxonomy.build(c);
	}

	public void stop(BundleContext context) throws Exception {
	}

}
