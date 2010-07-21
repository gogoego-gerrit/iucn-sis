package org.iucn.sis.server.utils.scripts;

import java.util.concurrent.atomic.AtomicBoolean;

import org.iucn.sis.server.simple.SISContainerApp;
import org.iucn.sis.server.utils.DocumentUtils;
import org.iucn.sis.server.utils.ServerPaths;
import org.iucn.sis.shared.data.assessments.AssessmentData;
import org.iucn.sis.shared.data.assessments.AssessmentParser;
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

public class GAADateFixer implements Runnable {
	public static class GAADateFixerResource extends Resource {

		public GAADateFixerResource() {
		}

		public GAADateFixerResource(final Context context, final Request request, final Response response) {
			super(context, request, response);
			getVariants().add(new Variant(MediaType.TEXT_HTML));
		}

		@Override
		public Representation represent(final Variant variant) {
			StringBuilder sb = new StringBuilder();

			if (!GAADateFixer.running.get()) {
				new Thread(new GAADateFixer()).start();

				sb.append("<html><head></head><body style='font-family:Verdana; font-size:x-small'>");
				sb.append("Started a new GAA date fixer!");
				sb.append("</body></html>");
			} else {
				sb.append("<html><head></head><body style='font-family:Verdana; font-size:x-small'>");
				sb.append("A GAADateFixer is already running!");
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
			registerDatasource("gaa", "jdbc:access:////usr/data/gaaDateChanges.mdb",
					"com.hxtt.sql.access.AccessDriver", "", "");
			switchToDBSession("gaa");

			startRepair();
		} catch (Exception e) {
			e.printStackTrace();
		}

		running.getAndSet(false);
	}

	private void startRepair() throws DBException {
		SelectQuery sel = new SelectQuery();
		sel.select("___GAA ASSESSMENT DATE CHANGES", "*");

		Row.Set set = new Row.Set();
		ec.doQuery(sel, set);

		int changedCount = 0;

		if (set.getSet() != null) {
			for (Row curRow : set.getSet()) {
				int ass_id = curRow.get("id").getInteger(Column.NEVER_NULL).intValue();

				String uri = ServerPaths.getPublishedAssessmentURL(ass_id + "");
				NativeDocument ndoc = SISContainerApp.newNativeDocument(null);
				ndoc.parse(DocumentUtils.getVFSFileAsString(uri, vfs));
				AssessmentParser p = new AssessmentParser();
				p.parse(ndoc);

				AssessmentData data = p.getAssessment();
				data.setDateAssessed("2008-01-01");

				DocumentUtils.writeVFSFile(uri, vfs, data.toXML());
				changedCount++;
			}
		}

		System.out.println("Changed " + changedCount + " dates.");
	}

	private void switchToDBSession(String sessionName) throws Exception {
		ec = new SystemExecutionContext(sessionName);
		ec.setExecutionLevel(ExecutionContext.READ_WRITE);
	}
}
