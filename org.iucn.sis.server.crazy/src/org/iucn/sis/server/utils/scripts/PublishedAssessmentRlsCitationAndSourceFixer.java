package org.iucn.sis.server.utils.scripts;

import java.io.File;
import java.util.ArrayList;

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
public class PublishedAssessmentRlsCitationAndSourceFixer extends BasePublishedAssessmentModder {
	public static class PublishedAssessmentRlsCitationAndSourceFixerResource extends Resource {

		public PublishedAssessmentRlsCitationAndSourceFixerResource() {
		}

		public PublishedAssessmentRlsCitationAndSourceFixerResource(final Context context, final Request request,
				final Response response) {
			super(context, request, response);
			getVariants().add(new Variant(MediaType.TEXT_HTML));
		}

		@Override
		public Representation represent(final Variant variant) {
			if (!BasePublishedAssessmentModder.running) {
				new Thread(new PublishedAssessmentRlsCitationAndSourceFixer(SISContainerApp.getStaticVFS())).run();
				System.out.println("Started a new RlsCitation and source fixer!");
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

	public PublishedAssessmentRlsCitationAndSourceFixer(File vfsRoot) {
		super(vfsRoot);
	}

	public PublishedAssessmentRlsCitationAndSourceFixer(VFS vfs) {
		super(vfs);
	}

	protected void fixData(final AssessmentData data) {
		SelectQuery select = new SelectQuery();
		select.select("RedList_NEW", "RlsSpcRecID");
		select.select("RedList_NEW", "RlsCitation");
		select.select("RedList_NEW", "Source");
		select.constrain(new CanonicalColumnName("RedList_NEW", "RlsRecNo"), QConstraint.CT_EQUALS, data
				.getAssessmentID());

		try {
			boolean doWriteBack = false;

			Row.Loader loader = new Row.Loader();
			ec.doQuery(select, loader);

			if (loader.getRow().get("RlsCitation").getString(Column.EMPTY_IS_NULL) != null) {
				String citation = loader.getRow().get("RlsCitation").getString(Column.EMPTY_IS_NULL);
				if (!data.getDataMap().containsKey(CanonicalNames.RedListAssessmentAuthors)) {
					ArrayList dataList = new ArrayList();
					dataList.add(citation);
					data.getDataMap().put(CanonicalNames.RedListAssessmentAuthors, dataList);
					doWriteBack = true;
				}
			}

			if (loader.getRow().get("Source").getString(Column.EMPTY_IS_NULL) != null) {
				String source = loader.getRow().get("Source").getString(Column.EMPTY_IS_NULL);
				if (!data.getDataMap().containsKey(CanonicalNames.RedListSource)) {
					ArrayList dataList = new ArrayList();
					dataList.add(source);
					data.getDataMap().put(CanonicalNames.RedListSource, dataList);
					doWriteBack = true;
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
		fixData(data);
	}

	@Override
	protected void workOnMostRecent(AssessmentData data) {
		fixData(data);
	}
}
