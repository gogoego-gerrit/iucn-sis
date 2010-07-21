package org.iucn.sis.server.utils.scripts;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

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

public class PublishedAssessmentStressFixer extends BasePublishedAssessmentModder {
	public static class PublishedAssessmentStressFixerResource extends Resource {
		public PublishedAssessmentStressFixerResource() {
		}

		public PublishedAssessmentStressFixerResource(final Context context, final Request request,
				final Response response) {
			super(context, request, response);
			getVariants().add(new Variant(MediaType.TEXT_HTML));
		}

		@Override
		public Representation represent(final Variant variant) {
			if (!BasePublishedAssessmentModder.running) {
				new Thread(new PublishedAssessmentStressFixer(SISContainerApp.getStaticVFS())).run();
				System.out.println("Started a new stress fixer!");
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

	public PublishedAssessmentStressFixer(File vfsRoot) {
		super(vfsRoot);
	}

	public PublishedAssessmentStressFixer(VFS vfs) {
		super(vfs);
	}

	protected boolean addStresses(AssessmentData data) {
		boolean changed = false;

		HashMap threats = (HashMap) data.getDataMap().get(CanonicalNames.Threats);

		if (threats == null)
			return false;

		SelectQuery select = new SelectQuery();
		select.select("STRESS THREAT DATA", "Sp_code");
		select.select("STRESS THREAT DATA", "Threat_code", "ASC");
		select.select("STRESS THREAT DATA", "Stress");

		select.constrain(new CanonicalColumnName("STRESS THREAT DATA", "Sp_code"), QConstraint.CT_EQUALS, data
				.getSpeciesID());
		// select.constrain( QConstraint.CG_AND,
		// new CanonicalColumnName("STRESS THREAT DATA", "Threat_code"),
		// QConstraint.CT_EQUALS, curCode.toString() );

		try {
			Row.Set stressLoader = new Row.Set();
			ec.doQuery(select, stressLoader);
			HashMap<String, ArrayList<String>> stresses = new HashMap<String, ArrayList<String>>();

			// Build HashMap
			for (Row curRow : stressLoader.getSet()) {
				String key = curRow.get("Threat_code").getString();
				String value = curRow.get("Stress").getString();

				if (stresses.get(key) == null)
					stresses.put(key, new ArrayList<String>());

				stresses.get(key).add(value);
			}

			for (String curCode : stresses.keySet()) {
				ArrayList dataArray = (ArrayList) threats.get(curCode);

				// Magic index, where Stress data starts
				String stressCount = dataArray.get(5).toString();

				if (!stressCount.equals(stresses.get(curCode).size() + "")) {
					dataArray.set(5, stresses.get(curCode).size() + "");

					for (String theStress : stresses.get(curCode))
						dataArray.add(theStress);

					changed = true;

					results.append("Changed threat " + curCode + " in assessment " + data.getAssessmentID() + ".");
					results.append("<br/>-------------------------------" + "<br/>");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error changing stresses for assessment " + data.getAssessmentID());
		}

		return changed;
		// return false;
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

	protected boolean verifyThreatCount(AssessmentData data) {
		try {
			SelectQuery select = new SelectQuery();
			select.select("threat_table_NEW", "*");
			select.constrain(new CanonicalColumnName("threat_table_NEW", "Sp_code"), QConstraint.CT_EQUALS, data
					.getSpeciesID());

			Row.Set rowLoader = new Row.Set();
			ec.doQuery(select, rowLoader);

			HashMap<String, ArrayList<String>> selected = new HashMap<String, ArrayList<String>>();
			for (Iterator<Row> iter = rowLoader.getSet().listIterator(); iter.hasNext();) {
				Row curRow = iter.next();

				String curCode = curRow.get("Threat_code").getString(Column.NATURAL_NULL);

				if (curCode == null || curCode.equals("0")) {
					// System.out.println("Threat code of 0 found! Skipping.");
					continue;
				}

				int curTiming = 0;

				String timing = curRow.get("threat_timing").getString(Column.NEVER_NULL);
				if (timing.equalsIgnoreCase("past"))
					curTiming = 1;
				else if (timing.equalsIgnoreCase("ongoing"))
					curTiming = 2;
				else if (timing.equalsIgnoreCase("future"))
					curTiming = 3;
				else if (timing.equalsIgnoreCase("unknown"))
					curTiming = 4;

				String notes = curRow.get("Threat_note").getString(Column.EMPTY_IS_NULL);

				ArrayList<String> dataArray = new ArrayList<String>();
				dataArray.add("" + (curTiming));
				dataArray.add("0");
				dataArray.add("0");
				dataArray.add("");
				dataArray.add("");

				select = new SelectQuery();
				select.select("STRESS THREAT DATA", "*");
				select.constrain(new CanonicalColumnName("STRESS THREAT DATA", "Sp_code"), QConstraint.CT_EQUALS, data
						.getSpeciesID());
				select.constrain(new CanonicalColumnName("STRESS THREAT DATA", "Threat_code"), QConstraint.CT_EQUALS,
						curCode);

				try {
					Row.Set stressLoader = new Row.Set();
					ec.doQuery(select, stressLoader);

					dataArray.add(stressLoader.getSet().size() + "");

					// if( stressLoader.getSet().size() > 0 )
					// changed = true;

					for (Iterator<Row> stressIter = stressLoader.getSet().listIterator(); stressIter.hasNext();) {
						Row curStress = stressIter.next();
						dataArray.add(curStress.get("Stress").getString());
					}
				} catch (Exception e) {
					System.out.println("Error building stresses.");
					e.printStackTrace();
					dataArray.add("0");
				}

				selected.put(curCode, dataArray);
			}

			HashMap threats = (HashMap) data.getDataMap().get(CanonicalNames.Threats);

			if (threats == null)
				threats = new HashMap();

			if (threats.size() != selected.size()) {
				results.append("Latest assessment, " + data.getAssessmentID() + " has " + threats.size()
						+ " threats but should have " + selected.size());
				results.append("<br/>-------------------------------" + "<br/>");

				return false;
			} else
				return true;
		} catch (Exception e) {
			System.out.println("Error building threats.");
			e.printStackTrace();

			return false;
		}
	}

	@Override
	protected void workOnHistorical(AssessmentData data) {
		// Do nothing.
	}

	@Override
	protected void workOnMostRecent(AssessmentData data) {
		if (verifyThreatCount(data))
			if (addStresses(data))
				writeBackPublishedAssessment(data);
	}
}
