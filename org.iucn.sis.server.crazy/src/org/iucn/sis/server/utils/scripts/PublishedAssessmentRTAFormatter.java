package org.iucn.sis.server.utils.scripts;

import java.io.File;
import java.util.ArrayList;

import org.iucn.sis.server.simple.SISContainerApp;
import org.iucn.sis.shared.data.assessments.AssessmentData;
import org.iucn.sis.shared.data.assessments.CanonicalNames;
import org.iucn.sis.shared.structures.FormattingStripper;
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

/**
 * Adds RedListEvaluators and RlsTaxNote to the assessment
 */
public class PublishedAssessmentRTAFormatter extends BasePublishedAssessmentModder {
	public static class PublishedAssessmentRTAFormatterResource extends Resource {

		public PublishedAssessmentRTAFormatterResource() {
		}

		public PublishedAssessmentRTAFormatterResource(final Context context, final Request request,
				final Response response) {
			super(context, request, response);
			getVariants().add(new Variant(MediaType.TEXT_HTML));
		}

		@Override
		public Representation represent(final Variant variant) {
			if (!BasePublishedAssessmentModder.running) {
				new Thread(new PublishedAssessmentRTAFormatter(SISContainerApp.getStaticVFS())).run();
				System.out.println("Started a new evaluator fixer!");
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

	public PublishedAssessmentRTAFormatter(File vfsRoot) {
		super(vfsRoot);
	}

	public PublishedAssessmentRTAFormatter(VFS vfs) {
		super(vfs);
	}

	protected void fixEvaluators(final AssessmentData data) {
		boolean doWriteBack = false;

		String[] todo = new String[] { CanonicalNames.ConservationActionsDocumentation,
				CanonicalNames.HabitatDocumentation, CanonicalNames.PopulationDocumentation,
				CanonicalNames.RangeDocumentation, CanonicalNames.ThreatsDocumentation,
				CanonicalNames.UseTradeDocumentation, CanonicalNames.RedListRationale };

		for (String cur : todo) {
			if (data.getDataMap().containsKey(cur)) {
				ArrayList<String> text = (ArrayList<String>) data.getDataMap().get(cur);
				String repaired = FormattingStripper.stripText(text.get(0).trim()).trim();
				if (!repaired.trim().equals(text.get(0).trim())) {
					((ArrayList<String>) data.getDataMap().get(cur)).set(0, repaired);
					doWriteBack = true;
				}
			}
		}

		if (doWriteBack) {
			System.out.println("Fixed formatting for RTAs in " + data.getAssessmentID());
			writeBackPublishedAssessment(data);
		}
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
		fixEvaluators(data);
	}

	@Override
	protected void workOnMostRecent(AssessmentData data) {
		fixEvaluators(data);
	}
}
