package org.iucn.sis.server.utils.scripts;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import org.iucn.sis.server.simple.SISContainerApp;
import org.iucn.sis.shared.data.assessments.AssessmentData;
import org.iucn.sis.shared.data.assessments.CanonicalNames;
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
public class PublishedAssessmentOccurrenceDataUncertainFixer extends BasePublishedAssessmentModder {
	public static class PublishedAssessmentOccurrenceDataUncertainFixerResource extends Resource {

		public PublishedAssessmentOccurrenceDataUncertainFixerResource() {
		}

		public PublishedAssessmentOccurrenceDataUncertainFixerResource(final Context context, final Request request,
				final Response response) {
			super(context, request, response);
			getVariants().add(new Variant(MediaType.TEXT_HTML));
		}

		@Override
		public Representation represent(final Variant variant) {
			if (!BasePublishedAssessmentModder.running) {
				new Thread(new PublishedAssessmentOccurrenceDataUncertainFixer(SISContainerApp.getStaticVFS())).run();
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

	public PublishedAssessmentOccurrenceDataUncertainFixer(File vfsRoot) {
		super(vfsRoot);
	}

	public PublishedAssessmentOccurrenceDataUncertainFixer(VFS vfs) {
		super(vfs);
	}

	protected void fixEvaluators(final AssessmentData data) {
		HashMap<String, ArrayList<String>> coo = (HashMap<String, ArrayList<String>>) data.getDataMap().get(
				CanonicalNames.CountryOccurrence);
		HashMap<String, ArrayList<String>> fao = (HashMap<String, ArrayList<String>>) data.getDataMap().get(
				CanonicalNames.FAOOccurrence);
		HashMap<String, ArrayList<String>> lme = (HashMap<String, ArrayList<String>>) data.getDataMap().get(
				CanonicalNames.LargeMarineEcosystems);
		boolean doWriteBack = false;

		if (coo != null) {
			for (Entry<String, ArrayList<String>> cur : coo.entrySet()) {
				if (cur.getValue().get(2).equals("9")) {
					cur.getValue().set(2, "6");
					doWriteBack = true;
				}
			}
		}
		if (fao != null) {
			for (Entry<String, ArrayList<String>> cur : fao.entrySet()) {
				if (cur.getValue().get(2).equals("9")) {
					cur.getValue().set(2, "6");
					doWriteBack = true;
				}
			}
		}
		if (lme != null) {
			for (Entry<String, ArrayList<String>> cur : lme.entrySet()) {
				if (cur.getValue().get(2).equals("9")) {
					cur.getValue().set(2, "6");
					doWriteBack = true;
				}
			}
		}

		if (doWriteBack) {
			System.out.println("Found occurrence data to fix for ID " + data.getAssessmentID());
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
