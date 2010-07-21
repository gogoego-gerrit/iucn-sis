package org.iucn.sis.server.utils.scripts;

import java.io.File;
import java.util.ArrayList;

import org.iucn.sis.shared.data.assessments.AssessmentData;
import org.iucn.sis.shared.data.assessments.CanonicalNames;
import org.iucn.sis.shared.structures.SISCategoryAndCriteria;

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
public class PublishedAssessmentTaxNoteFixer extends BasePublishedAssessmentModder {
	private ExecutionContext ec;

	public PublishedAssessmentTaxNoteFixer(File vfsRoot) {
		super(vfsRoot);
	}

	public PublishedAssessmentTaxNoteFixer(VFS vfs) {
		super(vfs);
	}

	protected void fixEvaluators(final AssessmentData data) {
		SelectQuery select = new SelectQuery();
		select.select("RedList_NEW", "RlsSpcRecID");
		select.select("RedList_NEW", "RlsEvalName");
		select.select("RedList_NEW", "RlsTaxNote");
		select.constrain(new CanonicalColumnName("RedList_NEW", "RlsRecNo"), QConstraint.CT_EQUALS, data
				.getAssessmentID());

		try {
			boolean doWriteBack = false;

			Row.Loader loader = new Row.Loader();
			ec.doQuery(select, loader);

			if (loader.getRow().get("RlsTaxNote").getString(Column.EMPTY_IS_NULL) != null) {
				String shouldBe = loader.getRow().get("RlsTaxNote").getString(Column.EMPTY_IS_NULL);
				if (data.getDataMap().containsKey(CanonicalNames.RedListCriteria)) {
					ArrayList critData = (ArrayList) data.getDataMap().get(CanonicalNames.RedListCriteria);

					String rlsHistText = (String) critData.get(SISCategoryAndCriteria.RLHISTORY_TEXT_INDEX);

					if (rlsHistText != null && !rlsHistText.equalsIgnoreCase(shouldBe)) {
						critData.set(SISCategoryAndCriteria.RLHISTORY_TEXT_INDEX, shouldBe);
						doWriteBack = true;
					}
				}
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
