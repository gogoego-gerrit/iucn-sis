package org.iucn.sis.server.utils.scripts;

import java.io.File;
import java.util.ArrayList;

import org.iucn.sis.server.simple.SISContainerApp;
import org.iucn.sis.shared.data.assessments.AssessmentData;
import org.iucn.sis.shared.data.assessments.CanonicalNames;
import org.iucn.sis.shared.structures.SISCategoryAndCriteria;
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
import com.solertium.db.DBSessionFactory;
import com.solertium.db.ExecutionContext;
import com.solertium.db.Row;
import com.solertium.db.SystemExecutionContext;
import com.solertium.db.query.QConstraint;
import com.solertium.db.query.SelectQuery;
import com.solertium.vfs.VFS;

/**
 * Adds RedListEvaluators and RlsTaxNote to the assessment
 */
public class PublishedAssessmentEvaluatorFixer extends BasePublishedAssessmentModder {
	public static class PublishedAssessmentEvaluatorFixerResource extends Resource {

		public PublishedAssessmentEvaluatorFixerResource() {
		}

		public PublishedAssessmentEvaluatorFixerResource(final Context context, final Request request,
				final Response response) {
			super(context, request, response);
			getVariants().add(new Variant(MediaType.TEXT_HTML));
		}

		@Override
		public Representation represent(final Variant variant) {
			if (!BasePublishedAssessmentModder.running) {
				new Thread(new PublishedAssessmentEvaluatorFixer(SISContainerApp.getStaticVFS())).run();
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

	public PublishedAssessmentEvaluatorFixer(File vfsRoot) {
		super(vfsRoot);
	}

	public PublishedAssessmentEvaluatorFixer(VFS vfs) {
		super(vfs);
	}

	protected void fixEvaluators(final AssessmentData data) {
		SelectQuery select = new SelectQuery();
		select.select("RedList_NEW", "RlsRecNo");
		select.select("RedList_NEW", "RlsEvalName");
		select.select("RedList_NEW", "RlsCitation");
		select.select("RedList_NEW", "Source");
		select.select("RedList_NEW", "RlsTaxNote");
		select.constrain(new CanonicalColumnName("RedList_NEW", "RlsRecNo"), QConstraint.CT_EQUALS, data
				.getAssessmentID());

		try {
			boolean doWriteBack = false;

			Row.Loader loader = new Row.Loader();
			ec.doQuery(select, loader);

			String shouldBe = loader.getRow().get("RlsEvalName").getString(Column.NEVER_NULL);

			if (!data.getEvaluators().equalsIgnoreCase(shouldBe)) {
				ArrayList evals = new ArrayList();
				evals.add(shouldBe);

				data.getDataMap().put(CanonicalNames.RedListEvaluators, evals);
				doWriteBack = true;
			}

			shouldBe = loader.getRow().get("RlsCitation").getString(Column.NEVER_NULL);
			{
				ArrayList dataList = (ArrayList) data.getDataMap().get(CanonicalNames.RedListAssessmentAuthors);
				if (dataList == null) {
					dataList = new ArrayList();
					dataList.add(shouldBe);
					data.getDataMap().put(CanonicalNames.RedListAssessmentAuthors, dataList);
					doWriteBack = true;
				} else if (!dataList.get(0).equals(shouldBe)) {
					dataList.set(0, shouldBe);
					doWriteBack = true;
				}
			}

			shouldBe = loader.getRow().get("Source").getString(Column.NEVER_NULL);
			{
				ArrayList dataList = (ArrayList) data.getDataMap().get(CanonicalNames.RedListSource);
				if (dataList == null) {
					dataList = new ArrayList();
					dataList.add(shouldBe);
					data.getDataMap().put(CanonicalNames.RedListSource, dataList);
					doWriteBack = true;
				} else if (!dataList.get(0).equals(shouldBe)) {
					dataList.add(shouldBe);
					doWriteBack = true;
				}
			}

			if (loader.getRow().get("RlsTaxNote").getString(Column.EMPTY_IS_NULL) != null) {
				shouldBe = loader.getRow().get("RlsTaxNote").getString(Column.EMPTY_IS_NULL);
				if (data.getDataMap().containsKey(CanonicalNames.RedListCriteria)) {
					ArrayList critData = (ArrayList) data.getDataMap().get(CanonicalNames.RedListCriteria);

					String rlsHistText = (String) critData.get(SISCategoryAndCriteria.RLHISTORY_TEXT_INDEX);

					if (rlsHistText != null && !rlsHistText.equalsIgnoreCase(shouldBe)) {
						critData.set(SISCategoryAndCriteria.RLHISTORY_TEXT_INDEX, shouldBe);
						doWriteBack = true;
					}
				}
			}

			if (data.getFirstDataPiece(CanonicalNames.RedListAssessmentDate, "").equals("")) {
				data.setDateAssessed(data.getDateFinalized());
				doWriteBack = true;
			}

			if (doWriteBack)
				writeBackPublishedAssessment(data);
		} catch (Exception e) {
			e.printStackTrace();
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
