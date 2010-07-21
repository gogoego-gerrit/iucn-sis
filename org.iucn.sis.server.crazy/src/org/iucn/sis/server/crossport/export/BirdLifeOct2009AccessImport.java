package org.iucn.sis.server.crossport.export;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.naming.NamingException;

import org.iucn.sis.client.referenceui.ReferenceUI;
import org.iucn.sis.server.io.AssessmentIO;
import org.iucn.sis.server.io.TaxaIO;
import org.iucn.sis.server.taxa.TaxonomyDocUtils;
import org.iucn.sis.server.utils.FormattedDate;
import org.iucn.sis.shared.BaseAssessment;
import org.iucn.sis.shared.data.assessments.AssessmentData;
import org.iucn.sis.shared.data.assessments.CanonicalNames;
import org.iucn.sis.shared.data.assessments.OccurrenceMigratorUtils;
import org.iucn.sis.shared.structures.FormattingStripper;
import org.iucn.sis.shared.structures.SISCategoryAndCriteria;
import org.iucn.sis.shared.taxonomyTree.TaxonNode;
import org.iucn.sis.shared.xml.XMLUtils;
import org.restlet.Context;
import org.restlet.data.CharacterSet;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.representation.StringRepresentation;

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

public class BirdLifeOct2009AccessImport implements Runnable {

	private class AccessExportAssessment {
		AssessmentData assessment;
		Row assessmentRow;

		public AccessExportAssessment(Row assessmentRow) {
			assessment = new AssessmentData();
			this.assessmentRow = assessmentRow;

			try {
				buildData();
			} catch (DBException e) {
				e.printStackTrace();
				System.out.println("ERROR fetching data for assessment " + assessment.getAssessmentID());
			}
		}

		private void addReferences(AssessmentData assessment) throws DBException {
			SelectQuery select = new SelectQuery();
			select.select("assessment_reference", "*");
			select.constrain(new CanonicalColumnName("assessment_reference", "asm_id"), QConstraint.CT_EQUALS,
					assessment.getAssessmentID());

			Row.Set rowLoader = new Row.Set();
			ec.doQuery(select, rowLoader);

			if (rowLoader == null || rowLoader.getSet() == null)
				return;

			for (Row curRow : rowLoader.getSet()) {
				String fieldName = curRow.get("field").getString();
				String refID = curRow.get("ref_id").getString();

				if (!assessment.getDataMap().containsKey(fieldName))
					assessment.getDataMap().put(fieldName, wrapInArray(new String[] { "" }, false));

				SelectQuery refQuery = new SelectQuery();

				refQuery.select("reference", "*");
				refQuery.constrain(new CanonicalColumnName("reference", "id"), QConstraint.CT_EQUALS, refID);

				Row.Loader refLoader = new Row.Loader();
				ec.doQuery(refQuery, refLoader);

				Row refRow = refLoader.getRow();

				String type = refRow.get("type").getString();

				HashMap<String, String> map = new HashMap<String, String>();

				for (int i = 2; i < refRow.size(); i++) {
					String colName = refRow.get(i).getLocalName();

					if (colName.equalsIgnoreCase("isbn_issn"))
						continue;

					map.put(colName, refRow.get(i).getString(Column.NEVER_NULL));

					if (colName.toLowerCase().startsWith("bib") || colName.toLowerCase().startsWith("External")
							|| colName.toLowerCase().startsWith("citation"))
						refRow.get(i).setTransient(true);
					else
						refRow.get(i).setTransient(false);
				}

				ReferenceUI ref = new ReferenceUI(map, refRow.getMD5Hash(), type);

				assessment.addReference(ref, fieldName);
				// JUST TRIED ADDING REFERENCES ... GO NUTS!!!
			}
		}

		private void buildClassificationScheme(String schemeName, String assessmentID, HashMap<String, Object> data)
				throws DBException {
			SelectQuery select = new SelectQuery();
			select.select(schemeName, "*");
			select.constrain(new CanonicalColumnName(schemeName, "asm_id"), QConstraint.CT_EQUALS, assessmentID);

			Row.Set rowLoader = new Row.Set();
			ec.doQuery(select, rowLoader);

			if (rowLoader == null || rowLoader.getSet() == null)
				return;

			HashMap<String, ArrayList<String>> selected = new HashMap<String, ArrayList<String>>();
			for (Row curRow : rowLoader.getSet()) {
				ArrayList<String> cur = new ArrayList<String>();
				String id = curRow.get(1).getString();

				if (selected.containsKey(id)) {
					if (schemeName.equals(CanonicalNames.Threats)) {
						String severity_index = curRow.get("severity_index").getString(Column.NEVER_NULL);

						try {
							if (Integer.valueOf(severity_index) != 0
									&& Integer.valueOf(severity_index) < Integer.valueOf(selected.get(id).get(2))) {
								selected.get(id).set(2, severity_index);
								System.out.println("Replaced a severity for asm " + assessmentID + " and threat " + id);
							}
						} catch (Exception ignored) {
						}
					}
					// if( !Arrays.deepEquals(cur.toArray(),
					// selected.get(id).toArray() ));
					// System.out.println("FOUND A DUPLICATE with disparate data "
					// + schemeName +
					// " with id " + id + " FOR " + assessmentID );
				} else {
					// Skip 0, cause that's asm_id, and 1's the id. The rest is
					// data.
					for (int i = 2; i < curRow.size(); i++)
						cur.add(curRow.get(i).getString(Column.NEVER_NULL));

					if (schemeName.equals(CanonicalNames.Threats))
						buildStresses(assessmentID, cur, id);

					selected.put(id, cur);
				}
			}

			data.put(schemeName, selected);
		}

		private void buildData() throws DBException {
			assessment.setAssessmentID(assessmentRow.get("id").getString());
			assessment.setType(AssessmentData.PUBLISHED_ASSESSMENT_STATUS);

			assessment.setHistorical(false);
			assessment.setDone(true);

			assessment.setDateAssessed(assessmentRow.get("RedListAssessmentDate").getString(Column.NEVER_NULL));
			assessment.setDateFinalized(assessmentRow.get("dateFinalized").getString(Column.NEVER_NULL));

			HashMap<String, Object> data = assessment.getDataMap();

			data.put(CanonicalNames.RedListConsistencyCheck, wrapInArray(new String[] { "2",
					FormattedDate.impl.getDate(), "1", "", "" }, false));
			data.put(CanonicalNames.RedListEvaluated, wrapInArray(new String[] { "2",
					FormattedDate.impl.getDate(), "1", "", "" }, false));

			buildFromAssessmentTable(data);
			buildField(CanonicalNames.RedListCriteria, assessment.getAssessmentID(), data);
			buildField(CanonicalNames.RedListReasonsForChange, assessment.getAssessmentID(), data);
			buildField(CanonicalNames.InPlaceSpeciesManagement, assessment.getAssessmentID(), data);
			buildField(CanonicalNames.InPlaceResearch, assessment.getAssessmentID(), data);
			buildField(CanonicalNames.InPlaceLandWaterProtection, assessment.getAssessmentID(), data);
			buildField(CanonicalNames.InPlaceEducation, assessment.getAssessmentID(), data);
			buildField(CanonicalNames.RegionInformation, assessment.getAssessmentID(), data);

			buildClassificationScheme(CanonicalNames.ConservationActions, assessment.getAssessmentID(), data);
			buildClassificationScheme(CanonicalNames.CountryOccurrence, assessment.getAssessmentID(), data);
			buildClassificationScheme(CanonicalNames.FAOOccurrence, assessment.getAssessmentID(), data);
			buildClassificationScheme(CanonicalNames.GeneralHabitats, assessment.getAssessmentID(), data);
			buildClassificationScheme(CanonicalNames.Research, assessment.getAssessmentID(), data);
			buildClassificationScheme(CanonicalNames.Threats, assessment.getAssessmentID(), data);

			ArrayList<String> critData = ((ArrayList<String>) data.get(CanonicalNames.RedListCriteria));

			String critString = critData.get(SISCategoryAndCriteria.MANUAL_CRITERIA_INDEX).replaceAll("[^iv],[^iv]",
					";");
			String version = critData.get(SISCategoryAndCriteria.CRIT_VERSION_INDEX);

			// if( version.equals("3.1") )
			// {
			// try {
			// CriteriaParser3_1 parser = new CriteriaParser3_1();
			// parser.parseCriteriaString(critString);
			// System.out.println("Conversion of " + critString + " to " +
			// parser.createCriteriaString() );
			// }
			// catch (Exception e) {}
			// }

			addReferences(assessment);

			OccurrenceMigratorUtils.migrateOccurrenceData(assessment);
		}

		private void buildField(String fieldName, String assessmentID, HashMap<String, Object> data) {
			SelectQuery select = new SelectQuery();
			select.select(fieldName, "*");
			select.constrain(new CanonicalColumnName(fieldName, "asm_id"), QConstraint.CT_EQUALS, assessmentID);

			try {
				Row.Loader rowLoader = new Row.Loader();
				ec.doQuery(select, rowLoader);

				if (rowLoader == null || rowLoader.getRow() == null)
					return;

				ArrayList<String> arr = new ArrayList<String>();
				for (Column curColumn : rowLoader.getRow().getColumns())
					arr.add(curColumn.getString(Column.NEVER_NULL));

				arr.remove(0); // Kick out the assessmentID column

				data.put(fieldName, arr);
			} catch (DBException didntExist) {
			}
		}

		private void buildFromAssessmentTable(HashMap<String, Object> data) {
			data.put(CanonicalNames.BiogeographicRealm, wrapAndFixMultiSelectData(assessmentRow.get(
					"BiogeographicRealm").getString(Column.NEVER_NULL)));
			data.put(CanonicalNames.MovementPatterns, wrapAndFixMultiSelectData(assessmentRow.get("MovementPatterns")
					.getString(Column.NEVER_NULL)));
			data.put(CanonicalNames.PopulationTrend, wrapInArray(new String[] { assessmentRow.get("PopulationTrend")
					.getString(Column.NEVER_NULL) }, false));
			data.put(CanonicalNames.RedListAssessors, wrapInArray(new String[] { assessmentRow.get("RedListAssessors")
					.getString(Column.NEVER_NULL) }, false));
			data.put(CanonicalNames.RedListCaveat, wrapInArray(new String[] { assessmentRow.get("RedListCaveat")
					.getString(Column.NEVER_NULL) }, false));
			data.put(CanonicalNames.RedListEvaluators, wrapInArray(new String[] { assessmentRow
					.get("RedListEvaluators").getString(Column.NEVER_NULL) }, false));
			data.put(CanonicalNames.RedListAssessmentAuthors, wrapInArray(new String[] { assessmentRow.get(
					"RedListAssessmentAuthors").getString(Column.NEVER_NULL) }, false));
			
			data.put(CanonicalNames.RedListNotes, wrapInArray(new String[] { FormattingStripper.stripText(assessmentRow.get("RedListNotes")
					.getString(Column.NEVER_NULL)) }, false));
			data.put(CanonicalNames.RedListRationale, wrapInArray(new String[] { FormattingStripper.stripText(assessmentRow.get("RedListRationale")
					.getString(Column.NEVER_NULL)) }, true));
			data.put(CanonicalNames.System, wrapAndFixMultiSelectData(assessmentRow.get("System").getString(
					Column.NEVER_NULL)));
			
			data.put(CanonicalNames.HabitatDocumentation, wrapInArray(new String[] { FormattingStripper.stripText(assessmentRow.get(
					"HabitatDocumentation").getString(Column.NEVER_NULL)) }, true));
			data.put(CanonicalNames.PopulationDocumentation, wrapInArray(new String[] { FormattingStripper.stripText(assessmentRow.get(
					"PopulationDocumentation").getString(Column.NEVER_NULL)) }, true));
			data.put(CanonicalNames.ConservationActionsDocumentation, wrapInArray(new String[] { FormattingStripper.stripText(assessmentRow.get(
					"ConservationActionsDocumentation").getString(Column.NEVER_NULL)) }, true));
			data.put(CanonicalNames.RangeDocumentation, wrapInArray(new String[] { FormattingStripper.stripText(assessmentRow.get(
					"RangeDocumentation").getString(Column.NEVER_NULL)) }, true));
			data.put(CanonicalNames.ThreatsDocumentation, wrapInArray(new String[] { FormattingStripper.stripText(assessmentRow.get(
					"ThreatsDocumentation").getString(Column.NEVER_NULL)) }, true));
		}

		private void buildStresses(String assessmentID, ArrayList<String> cur, String id) throws DBException {
			SelectQuery stressSel = new SelectQuery();
			stressSel.select("Stresses", "*");
			stressSel.constrain(new CanonicalColumnName("Stresses", "asm_id"), QConstraint.CT_EQUALS, assessmentID);
			stressSel.constrain(new CanonicalColumnName("Stresses", "threat_id"), QConstraint.CT_EQUALS, id);

			Row.Set stressLoader = new Row.Set();
			ec.doQuery(stressSel, stressLoader);

			if (stressLoader == null || stressLoader.getSet() == null)
				return;

			int size = 0;

			ArrayList<String> stressIDs = new ArrayList<String>();
			for (Row stressRow : stressLoader.getSet()) {
				String stressID = stressRow.get("stress_id").getString();
				if (!stressIDs.contains(stressID)) {
					stressIDs.add(stressID);
					size++;
				}
			}

			if (size > 0) {
				cur.add(size + "");
				cur.addAll(stressIDs);
			}
		}

		public AssessmentData getAssessment() {
			return assessment;
		}

		private ArrayList<String> wrapAndFixMultiSelectData(String multiSelectData) {
			ArrayList<String> arr = new ArrayList<String>();

			if (multiSelectData == null || multiSelectData.equals(""))
				arr.add("");
			else {
				if (multiSelectData.contains(",")) {
					String[] split = multiSelectData.replaceAll("\\s", "").split(",");
					for (String cur : split) {
						int value = Integer.valueOf(cur);
						value++;

						arr.add(value + "");
					}
				} else {
					int value = Integer.valueOf(multiSelectData);
					value++;

					arr.add(value + "");
				}
			}

			return arr;
		}

		private ArrayList<String> wrapInArray(String[] data, boolean cleanEndLines) {
			ArrayList<String> arr = new ArrayList<String>();
			for (String cur : data)
				arr.add(cleanEndLines ? XMLUtils.clean(cur.replaceAll("\\n", "<br>")) : XMLUtils.clean(cur));

			return arr;
		}
	}

	private static AtomicBoolean running = new AtomicBoolean(false);

	private static final String DS = "accessexport";

	public static boolean isRunning() {
		return running.get();
	}

	private ExecutionContext ec = null;
	
	private static AtomicBoolean failed = new AtomicBoolean(false);

	private static StringBuilder statusMessage = new StringBuilder();

	private StringBuilder log = new StringBuilder();

	private HashMap<String, TaxonNode> nodes;

	private int newNodeID = -1;

	private int nodesBuilt = 0;

	private VFS vfs;
	private Context context;
	
	public BirdLifeOct2009AccessImport(VFS vfs, Context context) {
		this.vfs = vfs;
		this.context = context;
	}

	/**
	 * This handles building the taxon's assessments, exporting the taxon (to
	 * obtain and ID), setting the assessment's species information, then
	 * exporting the assessments themselves.
	 * 
	 * @param exportID
	 * @param taxon
	 * 
	 * @throws DBException
	 * @throws IOException
	 */
	private boolean buildAssessmentsAndExportAll(TaxonNode taxon, String exportID) throws DBException, IOException {
		for( String assID : taxon.getAssessments() ) {
			AssessmentData data = AssessmentIO.readAssessment(vfs, assID, BaseAssessment.PUBLISHED_ASSESSMENT_STATUS, "");
			if( data.getDateAssessed().equals("2009/01/01") ) {
				System.out.println("Already assessed taxon " + taxon.getId() + " in 2009.");
				return true;
			}
		}
		
		AssessmentData assessment = generateAssessment(taxon, exportID);
		
		assessment.setSpeciesID(taxon.getId()+"");
		assessment.setSpeciesName(taxon.getFullName());
		assessment.setAssessmentID("new");
	
		String xml = assessment.toXML();

		Request r = new Request(Method.PUT, "riap://component/assessments", new StringRepresentation(xml,
					MediaType.TEXT_XML, null, CharacterSet.UTF_8));
//		Client c = new Client(context, r.getProtocol());
		Response res = context.getServerDispatcher().handle(r);
//		Response res = c.handle(r);
		
		if (!res.getStatus().isSuccess()) {
			System.out.println("Fucking shit. Error putting new published assessment for taxon " + 
					taxon.getFullName() + ". Status was " + res.getStatus().getCode() + " with message " + res.getStatus().getDescription());
			return false;
		} else {
			String newID = res.getEntity().getText();
//			System.out.println("Put new assessment - newID is " + newID);
			return true;
		}
		
	}

	private void buildSpecies() throws DBException, IOException {
		SelectQuery select = new SelectQuery();
		select.select("assessed", "*");

		Row.Set rows = new Row.Set();
		ec.doQuery(select, rows);

		List<Row> rowList = rows.getSet();

		System.out.println("There are " + rowList.size() + " assessed AVES species.");
		int count = 0;
		
		for (Iterator<Row> iter = rowList.listIterator(); iter.hasNext();) {
			Row curCol = iter.next();

			String exportID = curCol.get("tax_id").getString();
			String genus = curCol.get("genus").getString();
			String species = curCol.get("species").getString();

			String id = TaxonomyDocUtils.getIDByName("ANIMALIA", genus+species);
			if( id == null )
				System.out.println("Could not find " + genus + " " + species);
			else if( !buildAssessmentsAndExportAll(TaxaIO.readNode(id, vfs), exportID) )
				break;
			
			count++;
			if( count % 1000 == 0 || !iter.hasNext() )
				System.out.println("Through " + count + " species.");
		}
	}

	public void buildTaxa() throws DBException, IOException {
		nodes = new HashMap<String, TaxonNode>();

		buildSpecies();
	}

	/**
	 * Copy boilerplate DB to working DB, attach to working DB. Like DEMImport,
	 * this relies on the presence of some magic files in /usr/data.
	 */
	private void createAndConnect() throws IOException, NamingException {
		final String src = "/usr/data/birdsOct2009.mdb";

		DBSessionFactory.unregisterDataSource(DS);
		System.out.println("Connecting to working database");
		DBSessionFactory.registerDataSource(DS, "jdbc:access:///" + src, "com.hxtt.sql.access.AccessDriver", "", "");

		ec = new SystemExecutionContext(DS);
		ec.setExecutionLevel(ExecutionContext.ADMIN);
		ec.setAPILevel(ExecutionContext.SQL_ALLOWED);
	}

	public void doImport() throws IOException, NamingException {
		createAndConnect();

		try {
			buildTaxa();
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println(" ***** Error building taxa!!");
		} catch (DBException e) {
			e.printStackTrace();
			System.out.println(" ***** Error building taxa!!");
		}

	}

	/**
	 * Generates assessments for this taxon, according to its ID in the Access
	 * Export. Returns them as an ArrayList<AssessmentData> WITHOUT AN TAXON
	 * INFORMATION SET. This is important, as after you export the taxa (which
	 * should be the next step) you should set the assessments' taxon data, then
	 * export them.
	 * 
	 * @param taxon
	 *            - the taxon from the AccessExport
	 * @param exportID
	 *            - the ID of the taxon in the AccessExport
	 * @return a list of Assessments sans taxon information ... remember to
	 *         populate it.
	 * @throws DBException
	 */
	private AssessmentData generateAssessment(TaxonNode taxon, String exportID) throws DBException {
		SelectQuery select = new SelectQuery();
		select.select("assessment", "*");
		select.constrain(new CanonicalColumnName("assessment", "tax_id"), QConstraint.CT_EQUALS, exportID);

		Row.Set rows = new Row.Set();
		ec.doQuery(select, rows);

		List<Row> rowList = rows.getSet();
		AssessmentData assessment = null;

		if (rowList != null) {
			if( rowList.size() > 1 )
				System.out.println("ERROR: Species " + taxon.getFullName() + " apparently has two assessments...");
			else {
				AccessExportAssessment ass = new AccessExportAssessment(rowList.get(0));
				assessment = ass.getAssessment();
			}
		}

		return assessment;
	}

	public void run() {
		if (!running.compareAndSet(false, true))
			return;
		try {
//			SISContainerApp.getCrawler().unbindFromVFS();
			doImport();
//			SISContainerApp.getCrawler().bindToVFS();
		} catch (final Throwable t) {
			t.printStackTrace();
		} finally {
			DBSessionFactory.unregisterDataSource(DS);
			running.set(false);
		}
		System.out.println("Exported a total of " + nodesBuilt + " taxa.");
	}
}
