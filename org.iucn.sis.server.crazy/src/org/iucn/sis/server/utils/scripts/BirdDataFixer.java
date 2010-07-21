package org.iucn.sis.server.utils.scripts;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import org.iucn.sis.server.simple.SISContainerApp;
import org.iucn.sis.server.utils.DocumentUtils;
import org.iucn.sis.server.utils.ServerPaths;
import org.iucn.sis.shared.data.assessments.AssessmentData;
import org.iucn.sis.shared.data.assessments.AssessmentParser;
import org.iucn.sis.shared.data.assessments.CanonicalNames;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.Resource;

import com.solertium.db.Column;
import com.solertium.db.DBException;
import com.solertium.db.DBSessionFactory;
import com.solertium.db.ExecutionContext;
import com.solertium.db.Row;
import com.solertium.db.SystemExecutionContext;
import com.solertium.db.query.SelectQuery;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.vfs.VFS;

public class BirdDataFixer implements Runnable {
	public static class BirdDataFixerResource extends Resource {

		public BirdDataFixerResource() {
		}

		public BirdDataFixerResource(final Context context, final Request request, final Response response) {
			super(context, request, response);
			getVariants().add(new Variant(MediaType.TEXT_HTML));
		}

		@Override
		public Representation represent(final Variant variant) {
			StringBuilder sb = new StringBuilder();

			if (!BirdDataFixer.running.get()) {
				new Thread(new BirdDataFixer()).start();

				sb.append("<html><head></head><body style='font-family:Verdana; font-size:x-small'>");
				sb.append("Started a new BirdData fixer!");
				sb.append("</body></html>");
			} else {
				sb.append("<html><head></head><body style='font-family:Verdana; font-size:x-small'>");
				sb.append("A BirdData is already running!");
				sb.append("</body></html>");
			}

			return new StringRepresentation(sb, MediaType.TEXT_HTML);
		}

	}

	static AtomicBoolean running = new AtomicBoolean(false);
	private ExecutionContext ec;

	private VFS vfs;

	private AssessmentData fetchAssessment(String uri) {
		String xml = DocumentUtils.getVFSFileAsString(uri, vfs);
		NativeDocument ndoc = SISContainerApp.newNativeDocument(null);
		ndoc.parse(xml);

		AssessmentParser p = new AssessmentParser();
		p.parse(ndoc);

		AssessmentData data = p.getAssessment();
		return data;
	}

	private void registerDatasource(String sessionName, String URL, String driver, String username, String pass)
			throws Exception {
		DBSessionFactory.registerDataSource(sessionName, URL, driver, username, pass);
	}

	public void run() {
		try {
			running.getAndSet(true);
			vfs = SISContainerApp.getStaticVFS();

			registerDatasource("birdTaxaNotes", "jdbc:access:////usr/data/birdTaxNotes.mdb",
					"com.hxtt.sql.access.AccessDriver", "", "");
			switchToDBSession("birdTaxaNotes");

			startRepair();
		} catch (Exception e) {
			e.printStackTrace();
		}

		running.getAndSet(false);
	}

	private void startRepair() throws DBException {
		SelectQuery sel = new SelectQuery();
		sel.select("__FIX MISSING TAX NOTES", "*");

		Row.Set set = new Row.Set();
		ec.doQuery(sel, set);

		if (set.getSet() != null) {
			int changed = 0;

			for (Row curRow : set.getSet()) {
				boolean writeback = false;

				int id = curRow.get("id").getInteger(Column.NEVER_NULL).intValue();
				String taxaNotes = curRow.get("SpcTaxaNotes").getString(Column.NEVER_NULL);
				String uri = ServerPaths.getPublishedAssessmentURL(id + "");

				if (vfs.exists(uri)) {
					AssessmentData assess = fetchAssessment(uri);

					ArrayList<String> wrapper = new ArrayList<String>();
					wrapper.add(taxaNotes);

					assess.getDataMap().put(CanonicalNames.TaxonomicNotes, wrapper);

					DocumentUtils.writeVFSFile(uri, vfs, assess.toXML());
					changed++;
				}
			}

			System.out.println("Done! Changed " + changed + " assessments.");
		}
	}

	private void switchToDBSession(String sessionName) throws Exception {
		ec = new SystemExecutionContext(sessionName);
		ec.setExecutionLevel(ExecutionContext.READ_WRITE);
	}
}
