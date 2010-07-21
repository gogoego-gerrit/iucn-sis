package org.iucn.sis.server.utils.scripts;

import java.io.File;

import org.iucn.sis.server.simple.SISContainerApp;
import org.iucn.sis.shared.data.assessments.AssessmentData;
import org.iucn.sis.shared.data.assessments.OccurrenceMigratorUtils;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.Resource;

import com.solertium.db.DBSessionFactory;
import com.solertium.db.ExecutionContext;
import com.solertium.db.SystemExecutionContext;
import com.solertium.vfs.VFS;

public class PublishedAssessmentOccurrenceMigrator extends BasePublishedAssessmentModder {
	public static class PublishedAssessmentOccurrenceMigratorResource extends Resource {

		public PublishedAssessmentOccurrenceMigratorResource() {
		}

		public PublishedAssessmentOccurrenceMigratorResource(final Context context, final Request request,
				final Response response) {
			super(context, request, response);
			getVariants().add(new Variant(MediaType.TEXT_HTML));
		}

		@Override
		public Representation represent(final Variant variant) {
			if (!BasePublishedAssessmentModder.running) {
				new Thread(new PublishedAssessmentOccurrenceMigrator(SISContainerApp.getStaticVFS())).run();
				System.out.println("Started a new published occurrence migrator!");
			} else
				System.out.println("A published assessment script is already running!");

			StringBuilder sb = new StringBuilder();
			sb.append("<html><head></head><body style='font-family:Verdana; font-size:x-small'>");
			sb.append(BasePublishedAssessmentModder.results.toString());
			sb.append("</body></html>");

			return new StringRepresentation(sb, MediaType.TEXT_HTML);
		}

	}

	private ExecutionContext ec;

	public PublishedAssessmentOccurrenceMigrator(File vfsRoot) {
		super(vfsRoot);
	}

	public PublishedAssessmentOccurrenceMigrator(VFS vfs) {
		super(vfs);
	}

	private void migrateOccurrenceData(AssessmentData data) {
		// Make sure all of the Countries are there...
		if (OccurrenceMigratorUtils.migrateOccurrenceData(data))
			writeBackPublishedAssessment(data);
	}

	private void registerDatasource(String sessionName, String URL, String driver, String username, String pass)
			throws Exception {
		DBSessionFactory.registerDataSource(sessionName, URL, driver, username, pass);
	}

	@Override
	public void run() {
		try {
			registerDatasource("rldb", "jdbc:access:////usr/data/rldbRelationshipFree.mdb",
					"com.hxtt.sql.access.AccessDriver", "", "");
			switchToDBSession("rldb");

			super.run();
		} catch (Exception e) {
			e.printStackTrace();
			results.append("Failure to connect to Red List Database.");
		}
	}

	private void switchToDBSession(String sessionName) throws Exception {
		ec = new SystemExecutionContext(sessionName);
		ec.setExecutionLevel(ExecutionContext.READ_WRITE);
	}

	@Override
	protected void workOnHistorical(AssessmentData data) {
		migrateOccurrenceData(data);
	}

	@Override
	protected void workOnMostRecent(AssessmentData data) {
		migrateOccurrenceData(data);
	}
}
