package org.iucn.sis.server.utils.scripts;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import org.iucn.sis.server.io.TaxaIO;
import org.iucn.sis.server.simple.SISContainerApp;
import org.iucn.sis.server.taxa.TaxomaticHelper;
import org.iucn.sis.server.users.utils.Arrays16Emulation;
import org.iucn.sis.server.utils.DocumentUtils;
import org.iucn.sis.server.utils.ServerPaths;
import org.iucn.sis.shared.taxonomyTree.SynonymData;
import org.iucn.sis.shared.taxonomyTree.TaxonNode;
import org.iucn.sis.shared.taxonomyTree.TaxonNodeFactory;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.Resource;

import com.solertium.db.CanonicalColumnName;
import com.solertium.db.Column;
import com.solertium.db.DBException;
import com.solertium.db.DBSessionFactory;
import com.solertium.db.ExecutionContext;
import com.solertium.db.Row;
import com.solertium.db.SystemExecutionContext;
import com.solertium.db.query.QConstraint;
import com.solertium.db.query.SelectQuery;
import com.solertium.vfs.VFS;

public class GAADataFixer implements Runnable {
	public static class GAADataFixerResource extends Resource {

		public GAADataFixerResource() {
		}

		public GAADataFixerResource(final Context context, final Request request, final Response response) {
			super(context, request, response);
			getVariants().add(new Variant(MediaType.TEXT_HTML));
		}

		@Override
		public Representation represent(final Variant variant) {
			StringBuilder sb = new StringBuilder();

			if (!GAADataFixer.running.get()) {
				new Thread(new GAADataFixer()).start();

				sb.append("<html><head></head><body style='font-family:Verdana; font-size:x-small'>");
				sb.append("Started a new GAA data fixer!");
				sb.append("</body></html>");
			} else {
				sb.append("<html><head></head><body style='font-family:Verdana; font-size:x-small'>");
				sb.append("A GAADataFixer is already running!");
				sb.append("</body></html>");
			}

			return new StringRepresentation(sb, MediaType.TEXT_HTML);
		}

	}

	static AtomicBoolean running = new AtomicBoolean(false);
	private ExecutionContext ec;

	private VFS vfs;

	private void registerDatasource(String sessionName, String URL, String driver, String username, String pass)
			throws Exception {
		DBSessionFactory.registerDataSource(sessionName, URL, driver, username, pass);
	}

	public void run() {
		try {
			running.getAndSet(true);

			vfs = SISContainerApp.getStaticVFS();
			registerDatasource("gaa", "jdbc:access:////usr/data/GAA/gaa.mdb", "com.hxtt.sql.access.AccessDriver", "",
					"");
			switchToDBSession("gaa");

			startRepair();
		} catch (Exception e) {
			e.printStackTrace();
		}

		running.getAndSet(false);
	}

	private void startRepair() throws DBException {
		SelectQuery sel = new SelectQuery();
		sel.select("Systematics", "*");
		sel.constrain(new CanonicalColumnName("Systematics", "SIS_code"), QConstraint.CT_NOT, 0);

		Row.Set set = new Row.Set();
		ec.doQuery(sel, set);

		int changedCount = 0;

		if (set.getSet() != null) {
			for (Row curRow : set.getSet()) {
				int id = curRow.get("SIS_code").getInteger(Column.NEVER_NULL).intValue();
				String authority = curRow.get("Author_year").getString(Column.NEVER_NULL);
				String species = curRow.get("Species").getString(Column.NEVER_NULL);
				String genus = curRow.get("Genus").getString(Column.NEVER_NULL);

				if (id == 0)
					continue;

				String uri = ServerPaths.getURLForTaxa(id + "");

				TaxonNode node = TaxonNodeFactory.createNode(DocumentUtils.getVFSFileAsString(uri, vfs), null, false);

				if (!node.getName().equals(species)) {
					System.out.println("** DISPARATE species names " + node.generateFullName() + " vs. " + genus + " "
							+ species + " for " + node.getId());

					TaxonNode newNode = TaxonNodeFactory.createNode(node.getId(), species, TaxonNode.SPECIES, node
							.getParentId(), node.getParentName(), false, node.getStatus(), "", "");

					newNode.setFootprint(Arrays16Emulation.copyOf(node.getFootprint(), node.getFootprint().length));
					newNode.setFullName(newNode.generateFullName());

					// TEST ME!
					SynonymData newSyn = new SynonymData(node.getFootprint()[TaxonNode.GENUS], node.getName(), "",
							TaxonNode.INFRARANK_TYPE_NA, node.getLevel(), node.getId() + "");
					newSyn.setAuthority(node.getTaxonomicAuthority(), TaxonNode.SPECIES);
					node.addSynonym(newSyn);
					node.setTaxonomicAuthority(authority);

					ArrayList<TaxonNode> changed = TaxomaticHelper.taxonNameChanged(newNode, node, vfs);

					node.setName(species);
					node.setFullName(node.generateFullName());
					changed.add(node);

					TaxaIO.writeNodes(changed, vfs, true);

					changedCount++;
				} else if (node.getTaxonomicAuthority() == null || !node.getTaxonomicAuthority().equals(authority)) {
					node.setTaxonomicAuthority(authority);

					// System.out.println("-- Taxonomic authority changed for "
					// + node.getId());
					DocumentUtils.writeVFSFile(uri, vfs, TaxonNodeFactory.nodeToDetailedXML(node));
				}
			}
		}

		System.out.println("Renamed " + changedCount + " taxa.");
	}

	private void switchToDBSession(String sessionName) throws Exception {
		ec = new SystemExecutionContext(sessionName);
		ec.setExecutionLevel(ExecutionContext.READ_WRITE);
	}
}
