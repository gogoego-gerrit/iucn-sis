package org.iucn.sis.server.utils.scripts;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import org.iucn.sis.server.simple.SISContainerApp;
import org.iucn.sis.shared.data.assessments.AssessmentData;
import org.iucn.sis.shared.data.assessments.CanonicalNames;
import org.iucn.sis.shared.structures.SISThreatStructure;
import org.iucn.sis.shared.taxonomyTree.TaxonNode;
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

public class DraftAssessGrouperThreatsFixer extends BaseDraftAssessmentModder {

	public static class DraftAssessGrouperThreatsFixerResource extends Resource {

		public DraftAssessGrouperThreatsFixerResource() {
		}

		public DraftAssessGrouperThreatsFixerResource(final Context context, final Request request,
				final Response response) {
			super(context, request, response);
			getVariants().add(new Variant(MediaType.TEXT_HTML));
		}

		@Override
		public Representation represent(final Variant variant) {
			if (!BaseDraftAssessmentModder.running) {
				String wsID = (String) getRequest().getAttributes().get("wsID");
				String wsURL = null;

				if (wsID != null)
					wsURL = "/workingsets/" + wsID + ".xml";

				new Thread(new DraftAssessGrouperThreatsFixer(SISContainerApp.getStaticVFS(), wsURL)).run();
				System.out.println("Started a new Grouper threats fixer!");
			} else
				System.out.println("A draft assessment script is already running!");

			StringBuilder sb = new StringBuilder();
			sb.append("<html><head></head><body style='font-family:Verdana; font-size:x-small'>");
			sb.append("DraftAssessGrouperThreatsFixer is running...");
			sb.append("</body></html>");

			return new StringRepresentation(sb, MediaType.TEXT_HTML);
		}

	}

	private ExecutionContext ec;

	public DraftAssessGrouperThreatsFixer(File vfsRoot, String workingSetURL) {
		super(vfsRoot, workingSetURL, false);
	}

	public DraftAssessGrouperThreatsFixer(VFS vfs, String workingSetURL) {
		super(vfs, workingSetURL, false);
	}

	private String getDEMid(TaxonNode node) {
		SelectQuery select = new SelectQuery();
		select.select("Systematics", "Sp_code");
		select.constrain(new CanonicalColumnName("Systematics", "Genus"), QConstraint.CT_EQUALS,
				node.getFootprint()[TaxonNode.GENUS]);

		if (node.getLevel() == TaxonNode.SPECIES)
			select.constrain(new CanonicalColumnName("Systematics", "Species"), QConstraint.CT_EQUALS, node.getName());
		else if (node.getLevel() == TaxonNode.INFRARANK)
			select.constrain(new CanonicalColumnName("Systematics", "Rank_epithet"), QConstraint.CT_EQUALS, node
					.getName().replace("ssp. ", ""));
		else if (node.getLevel() == TaxonNode.SUBPOPULATION)
			select.constrain(new CanonicalColumnName("Systematics", "Sub_pop"), QConstraint.CT_EQUALS, node.getName());
		else
			select.constrain(new CanonicalColumnName("Systematics", "Species"), QConstraint.CT_EQUALS, node
					.getFootprint()[TaxonNode.SPECIES]);

		try {
			Row.Loader rowLoader = new Row.Loader();
			ec.doQuery(select, rowLoader);

			System.out.println("Matched up " + node.getFullName() + " with "
					+ rowLoader.getRow().get("Sp_code").getString());
			return rowLoader.getRow().get("Sp_code").getString();
		} catch (DBException e) {
			return null;
		}
	}

	protected String getOldThreatID(String newThreatID) {
		String oldID = newThreatID;

		try {
			switchToDBSession("demConversion");

			SelectQuery conversionSelect = new SelectQuery();
			conversionSelect.select("Threat crosswalking", "*");
			conversionSelect.constrain(new CanonicalColumnName("Threat crosswalking", "newthreat_id"),
					QConstraint.CT_EQUALS, newThreatID);

			Row.Loader conversionRow = new Row.Loader();
			ec.doQuery(conversionSelect, conversionRow);

			if (conversionRow.getRow() != null)
				oldID = conversionRow.getRow().get("oldthreat_id").getString();
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			switchToDBSession("dem");
		} catch (Exception e) {
			e.printStackTrace();
		}

		return oldID;
	}

	private void registerDatasource(String sessionName, String URL, String driver, String username, String pass)
			throws Exception {
		DBSessionFactory.registerDataSource(sessionName, URL, driver, username, pass);
	}

	@Override
	public void run() {
		try {
			registerDatasource("dem", "jdbc:access:////usr/data/groupers.mdb", "com.hxtt.sql.access.AccessDriver", "",
					"");
			registerDatasource("demConversion", "jdbc:access:////usr/data/demMigration.mdb",
					"com.hxtt.sql.access.AccessDriver", "", "");

			switchToDBSession("dem");

			super.run();
		} catch (Exception e) {
			e.printStackTrace();
			results.append("Failure to connect to Groupers DEM.");
		}
	}

	private void switchToDBSession(String sessionName) throws Exception {
		ec = new SystemExecutionContext(sessionName);
		ec.setExecutionLevel(ExecutionContext.READ_WRITE);
	}

	@Override
	protected void workOnAssessment(AssessmentData data, TaxonNode node) {
		String DEMid = getDEMid(node);
		boolean writeBackAssessment = false;

		if (DEMid == null) {
			System.out.println("Could not find DEM id for taxon " + node.getFullName());
			return;
		}

		HashMap<String, ArrayList<String>> threats = (HashMap<String, ArrayList<String>>) data.getDataMap().get(
				CanonicalNames.Threats);

		for (Entry<String, ArrayList<String>> entry : threats.entrySet()) {
			String threatCode = getOldThreatID(entry.getKey());
			ArrayList<String> structures = entry.getValue();

			SelectQuery select = new SelectQuery();
			select.select("threat_table", "*");
			select.constrain(new CanonicalColumnName("threat_table", "Sp_code"), QConstraint.CT_EQUALS, DEMid);
			select.constrain(new CanonicalColumnName("threat_table", "Threat_code"), QConstraint.CT_EQUALS, threatCode);

			try {
				Row.Set rowLoader = new Row.Set();
				ec.doQuery(select, rowLoader);

				int extantTiming = -50;

				for (Row curRow : rowLoader.getSet()) {
					int curTiming = curRow.get("threat_timing").getInteger(Column.NEVER_NULL);

					if (extantTiming == -50)
						extantTiming = curTiming;
					else if (extantTiming == +SISThreatStructure.TIMING_ONGOING_INDEX) {
						// If it's ongoing just leave things alone
					}
					// If it's ever present mark it ongoing
					else if (curTiming == SISThreatStructure.TIMING_ONGOING_INDEX)
						extantTiming = SISThreatStructure.TIMING_ONGOING_INDEX;
					// If it's definitely not ongoing, look for past and future
					else if (curTiming == SISThreatStructure.TIMING_PAST_UNLIKELY_RETURN_INDEX
							&& extantTiming == SISThreatStructure.TIMING_FUTURE_INDEX)
						extantTiming = SISThreatStructure.TIMING_PAST_LIKELY_RETURN_INDEX;
					else if (curTiming == SISThreatStructure.TIMING_FUTURE_INDEX
							&& extantTiming == SISThreatStructure.TIMING_PAST_UNLIKELY_RETURN_INDEX)
						extantTiming = SISThreatStructure.TIMING_PAST_LIKELY_RETURN_INDEX;
				}

				// See if the timing in the assessment, for this Threat, is
				// right anyway
				if (extantTiming == -50)
					System.out.println("Threat " + threatCode + " not found for " + DEMid + ", or "
							+ node.getFullName());
				else if (!structures.get(0).equals("" + extantTiming)) {
					System.out.println("Threat " + threatCode + " found for " + DEMid + ", or " + node.getFullName());
					writeBackAssessment = true;
					structures.set(0, "" + extantTiming);
				} else
					System.out.println("Threat " + threatCode + " found for " + DEMid + ", or " + node.getFullName()
							+ " but no writeback needed.");
			} catch (DBException e) {
				e.printStackTrace();
			}
		}// End for each threat...

		if (writeBackAssessment) {
			System.out.println("Writing back assessment " + data.getAssessmentID());
			writeBackDraftAssessment(data);
		}
	}
}
