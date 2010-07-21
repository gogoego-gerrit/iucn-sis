package org.iucn.sis.server.crossport.export;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.naming.NamingException;

import org.iucn.sis.client.referenceui.ReferenceUI;
import org.iucn.sis.server.taxa.TaxonomyDocUtils;
import org.iucn.sis.server.utils.FormattedDate;
import org.iucn.sis.server.utils.ServerPaths;
import org.iucn.sis.shared.data.assessments.AssessmentData;
import org.iucn.sis.shared.data.assessments.CanonicalNames;
import org.iucn.sis.shared.data.assessments.OccurrenceMigratorUtils;
import org.iucn.sis.shared.structures.SISCategoryAndCriteria;
import org.iucn.sis.shared.taxonomyTree.CommonNameData;
import org.iucn.sis.shared.taxonomyTree.SynonymData;
import org.iucn.sis.shared.taxonomyTree.TaxonNode;
import org.iucn.sis.shared.taxonomyTree.TaxonNodeFactory;
import org.iucn.sis.shared.xml.XMLUtils;
import org.restlet.Uniform;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.solertium.db.CanonicalColumnName;
import com.solertium.db.Column;
import com.solertium.db.DBException;
import com.solertium.db.DBSessionFactory;
import com.solertium.db.ExecutionContext;
import com.solertium.db.Row;
import com.solertium.db.SystemExecutionContext;
import com.solertium.db.query.QConstraint;
import com.solertium.db.query.SelectQuery;
import com.solertium.util.NodeCollection;
import com.solertium.vfs.ConflictException;
import com.solertium.vfs.NotFoundException;
import com.solertium.vfs.VFS;

public class BirdLifeAccessImport implements Runnable {

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

			assessment.setHistorical(getProbablyBooleanData(assessmentRow.get("isHistorical").getString()));

			assessment.setDateAssessed(assessmentRow.get("RedListAssessmentDate").getString(Column.NEVER_NULL));
			assessment.setDateFinalized(assessmentRow.get("dateFinalized").getString(Column.NEVER_NULL));

			HashMap<String, Object> data = assessment.getDataMap();

			data.put(CanonicalNames.RedListConsistencyCheck, wrapInArray(new String[] { "2",
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
			data.put(CanonicalNames.RedListNotes, wrapInArray(new String[] { assessmentRow.get("RedListNotes")
					.getString(Column.NEVER_NULL) }, false));
			data.put(CanonicalNames.RedListRationale, wrapInArray(new String[] { assessmentRow.get("RedListRationale")
					.getString(Column.NEVER_NULL) }, true));
			data.put(CanonicalNames.System, wrapAndFixMultiSelectData(assessmentRow.get("System").getString(
					Column.NEVER_NULL)));
			data.put(CanonicalNames.HabitatDocumentation, wrapInArray(new String[] { assessmentRow.get(
					"HabitatDocumentation").getString(Column.NEVER_NULL) }, true));
			data.put(CanonicalNames.PopulationDocumentation, wrapInArray(new String[] { assessmentRow.get(
					"PopulationDocumentation").getString(Column.NEVER_NULL) }, true));
			data.put(CanonicalNames.ConservationActionsDocumentation, wrapInArray(new String[] { assessmentRow.get(
					"ConservationActionsDocumentation").getString(Column.NEVER_NULL) }, true));
			data.put(CanonicalNames.RangeDocumentation, wrapInArray(new String[] { assessmentRow.get(
					"RangeDocumentation").getString(Column.NEVER_NULL) }, true));
			data.put(CanonicalNames.ThreatsDocumentation, wrapInArray(new String[] { assessmentRow.get(
					"ThreatsDocumentation").getString(Column.NEVER_NULL) }, true));
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

		private boolean getProbablyBooleanData(String value) {
			if (value == null)
				return false;

			return Boolean.parseBoolean(value.replace("'", ""));
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

	private int orderIDStart = 500000;
	private int familyIDStart = 400000;
	private int generaIDStart = 300000;

	private int speciesIDStart = 200000;

	private final String avesID = "100032";
	private static AtomicBoolean running = new AtomicBoolean(false);

	private static final String DS = "accessexport";

	public static boolean isRunning() {
		return running.get();
	}

	private ExecutionContext ec = null;
	private int taxonCount = 0;
	private String uriPrefix = "riap://host";

	private Uniform uniform;
	private static AtomicBoolean failed = new AtomicBoolean(false);

	private static StringBuilder statusMessage = new StringBuilder();

	private StringBuilder log = new StringBuilder();

	private HashMap<String, TaxonNode> nodes;

	private int newNodeID = -1;

	private int nodesBuilt = 0;

	private VFS vfs;

	public BirdLifeAccessImport(VFS vfs, Uniform uniform) {
		this.vfs = vfs;
		this.uniform = uniform;
	}

	private void addTaxonDetails(String exportID, TaxonNode taxon) {
		SelectQuery select = new SelectQuery();
		select.select("common_name", "*");
		select.constrain(new CanonicalColumnName("common_name", "tax_id"), QConstraint.CT_EQUALS, exportID);

		try {
			Row.Set rows = new Row.Set();
			ec.doQuery(select, rows);

			if (rows.getSet() == null)
				return;

			List<Row> rowList = rows.getSet();

			for (Row curRow : rowList) {
				String name = curRow.get("common_name").getString();
				String iso = curRow.get("iso_language").getString();
				boolean primary = curRow.get("principal").getString(Column.NEVER_NULL).equalsIgnoreCase("Y");
				boolean validated = curRow.get("validated").getString(Column.NEVER_NULL).equalsIgnoreCase("Y");

				CommonNameData c = new CommonNameData(name, "", iso, validated, primary);

				taxon.addCommonName(c);
			}
		} catch (DBException ignored) {
		}

		select = new SelectQuery();
		select.select("synonyms", "*");
		select.constrain(new CanonicalColumnName("synonyms", "tax_id"), QConstraint.CT_EQUALS, exportID);

		try {
			Row.Set rows = new Row.Set();
			ec.doQuery(select, rows);

			if (rows.getSet() == null)
				return;

			List<Row> rowList = rows.getSet();

			for (Row curRow : rowList) {
				String name = curRow.get("name").getString();
				String notes = curRow.get("notes").getString();
				String status = curRow.get("status").getString();

				SynonymData s = new SynonymData(name, TaxonNode.SPECIES, null, status, notes, "");
				// SynonymData s = new SynonymData("", name, "",
				// TaxonNode.INFRARANK_TYPE_NA,
				// TaxonNode.SPECIES, null, status, notes, "");

				taxon.addSynonym(s);
			}
		} catch (DBException ignored) {
		}
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
	private boolean buildAssessmentsAndExportAll(String exportID, TaxonNode taxon) throws DBException, IOException {
		ArrayList<AssessmentData> assessments = generateAssessments(taxon, exportID);

		for (AssessmentData curAssessment : assessments)
			taxon.addAssessment(curAssessment.getAssessmentID());

		boolean success = exportTaxonAsNew(taxon);

		if (!success)
			return false;

		for (AssessmentData curAssessment : assessments) {
			curAssessment.setSpeciesID(taxon.getId() + "");
			curAssessment.setSpeciesName(taxon.getFullName());

			String xml = curAssessment.toXML();

		}

		return true;
	}

	private void buildFamilies() throws DBException, IOException {
		SelectQuery select = new SelectQuery();
		select.select("taxonomy", "*");
		select.constrain(new CanonicalColumnName("taxonomy", "level"), QConstraint.CT_EQUALS, "Family");

		Row.Set rows = new Row.Set();
		ec.doQuery(select, rows);

		List<Row> rowList = rows.getSet();

		System.out.println("There are " + rowList.size() + " families in this DEM.");

		for (Iterator<Row> iter = rowList.listIterator(); iter.hasNext();) {
			Row curCol = iter.next();

			String exportID = curCol.get("id").getString();

			String name = curCol.get("name").getString();

			if (name.equals("-"))
				name = "BirdLifePlaceHolderFamily";

			String status = curCol.get("status").getString();
			boolean hybrid = curCol.get("hybrid").getString(Column.NEVER_NULL).equalsIgnoreCase("Y");
			String taxonomicAuthority = curCol.get("taxonomic_authority").getString();
			String parentID = curCol.get("parent_tax_id").getString();

			TaxonNode parent = nodes.get(parentID);

			TaxonNode curFamily = TaxonNodeFactory.createNode((long) newNodeID, name, TaxonNode.FAMILY, parent.getId()
					+ "", parent.getFullName(), hybrid, status, "Import", FormattedDate.impl.getDate());

			curFamily.setTaxonomicAuthority(taxonomicAuthority);

			ArrayList<String> footprint = new ArrayList<String>();
			for (String curF : parent.getFootprint())
				footprint.add(curF);
			footprint.add(parent.getName());

			curFamily.setFootprint(footprint.toArray(new String[footprint.size()]));
			curFamily.setFullName(curFamily.getName());

			addTaxonDetails(exportID, curFamily);

			exportTaxonAsNew(curFamily);

			nodes.put(exportID, curFamily);
		}
	}

	private void buildGenera() throws DBException, IOException {
		SelectQuery select = new SelectQuery();
		select.select("taxonomy", "*");
		select.constrain(new CanonicalColumnName("taxonomy", "level"), QConstraint.CT_EQUALS, "Genus");

		Row.Set rows = new Row.Set();
		ec.doQuery(select, rows);

		List<Row> rowList = rows.getSet();

		System.out.println("There are " + rowList.size() + " genera in this DEM.");

		for (Iterator<Row> iter = rowList.listIterator(); iter.hasNext();) {
			Row curCol = iter.next();

			String exportID = curCol.get("id").getString();

			String name = curCol.get("name").getString();

			if (name.equals("-"))
				name = "BirdLifePlaceHolderGenus";

			String status = curCol.get("status").getString();
			boolean hybrid = curCol.get("hybrid").getString(Column.NEVER_NULL).equalsIgnoreCase("Y");
			String taxonomicAuthority = curCol.get("taxonomic_authority").getString();
			String parentID = curCol.get("parent_tax_id").getString();

			TaxonNode parent = nodes.get(parentID);

			TaxonNode curGenus = TaxonNodeFactory.createNode((long) newNodeID, name, TaxonNode.GENUS, parent.getId()
					+ "", parent.getFullName(), hybrid, status, "Import", FormattedDate.impl.getDate());

			curGenus.setTaxonomicAuthority(taxonomicAuthority);

			ArrayList<String> footprint = new ArrayList<String>();
			for (String curF : parent.getFootprint())
				footprint.add(curF);
			footprint.add(parent.getName());

			curGenus.setFootprint(footprint.toArray(new String[footprint.size()]));
			curGenus.setFullName(curGenus.getName());

			addTaxonDetails(exportID, curGenus);

			exportTaxonAsNew(curGenus);

			nodes.put(exportID, curGenus);
		}
	}

	private void buildOrders() throws DBException, IOException {
		SelectQuery select = new SelectQuery();
		select.select("taxonomy", "*");
		select.constrain(new CanonicalColumnName("taxonomy", "level"), QConstraint.CT_EQUALS, "Order");

		Row.Set rows = new Row.Set();
		ec.doQuery(select, rows);

		List<Row> rowList = rows.getSet();

		System.out.println("There are " + rowList.size() + " orders in this DEM.");

		for (Iterator<Row> iter = rowList.listIterator(); iter.hasNext();) {
			Row curCol = iter.next();

			String exportID = curCol.get("id").getString();

			String name = curCol.get("name").getString();

			if (name.equals("-"))
				name = "BirdLifePlaceHolderOrder";

			String status = curCol.get("status").getString();
			boolean hybrid = curCol.get("hybrid").getString(Column.NEVER_NULL).equalsIgnoreCase("Y");
			String taxonomicAuthority = curCol.get("taxonomic_authority").getString();

			TaxonNode curOrder = TaxonNodeFactory.createNode((long) newNodeID, name, TaxonNode.ORDER, avesID, "AVES",
					hybrid, status, "Import", FormattedDate.impl.getDate());

			curOrder.setTaxonomicAuthority(taxonomicAuthority);
			curOrder.setFootprint(new String[] { "ANIMALIA", "CHORDATA", "AVES" });
			curOrder.setFullName(curOrder.getName());

			addTaxonDetails(exportID, curOrder);

			exportTaxonAsNew(curOrder);

			nodes.put(exportID, curOrder);
		}
	}

	private void buildSpecies() throws DBException, IOException {
		SelectQuery select = new SelectQuery();
		select.select("taxonomy", "*");
		select.constrain(new CanonicalColumnName("taxonomy", "level"), QConstraint.CT_EQUALS, "Species");

		Row.Set rows = new Row.Set();
		ec.doQuery(select, rows);

		List<Row> rowList = rows.getSet();

		System.out.println("There are " + rowList.size() + " species in this DEM.");

		for (Iterator<Row> iter = rowList.listIterator(); iter.hasNext();) {
			Row curCol = iter.next();

			String exportID = curCol.get("id").getString();
			String name = curCol.get("name").getString();

			if (name.equals("-"))
				name = "BirdLifePlaceHolderSpecies";

			String status = curCol.get("status").getString();

			if (status.equalsIgnoreCase("N"))
				continue;

			boolean hybrid = curCol.get("hybrid").getString(Column.NEVER_NULL).equalsIgnoreCase("Y");
			String taxonomicAuthority = curCol.get("taxonomic_authority").getString();
			String parentID = curCol.get("parent_tax_id").getString();

			TaxonNode parent = nodes.get(parentID);

			TaxonNode curSpecies = TaxonNodeFactory.createNode((long) newNodeID, name, TaxonNode.SPECIES, parent
					.getId()
					+ "", parent.getFullName(), hybrid, status, "Import", FormattedDate.impl.getDate());

			curSpecies.setTaxonomicAuthority(taxonomicAuthority);

			ArrayList<String> footprint = new ArrayList<String>();
			for (String curF : parent.getFootprint())
				footprint.add(curF);
			footprint.add(parent.getName());

			curSpecies.setFootprint(footprint.toArray(new String[footprint.size()]));
			curSpecies.setFullName(curSpecies.generateFullName());

			addTaxonDetails(exportID, curSpecies);

			if (buildAssessmentsAndExportAll(exportID, curSpecies))
				nodes.put(exportID, curSpecies);
		}
	}

	public void buildTaxa() throws DBException, IOException {
		nodes = new HashMap<String, TaxonNode>();

		removeAvesChildren();

		buildOrders();
		buildFamilies();
		buildGenera();
		buildSpecies();
	}

	/**
	 * Copy boilerplate DB to working DB, attach to working DB. Like DEMImport,
	 * this relies on the presence of some magic files in /usr/data.
	 */
	private void createAndConnect() throws IOException, NamingException {
		final String src = "/usr/data/birdsExport.mdb";

		DBSessionFactory.unregisterDataSource(DS);
		System.out.println("Connecting to working database");
		DBSessionFactory.registerDataSource(DS, "jdbc:access:///" + src, "com.hxtt.sql.access.AccessDriver", "", "");

		ec = new SystemExecutionContext(DS);
		ec.setExecutionLevel(ExecutionContext.ADMIN);
		ec.setAPILevel(ExecutionContext.SQL_ALLOWED);
	}

	private void deleteTaxon(Element curTaxon, Element parent) throws ConflictException, NotFoundException {
		for (Node curChild : new NodeCollection(curTaxon.getChildNodes()))
			if (curChild.getNodeType() == Node.ELEMENT_NODE)
				deleteTaxon((Element) curChild, curTaxon);

		// Delete myself
		String id = curTaxon.getNodeName().replace("node", "").trim();

		if (vfs.exists(ServerPaths.getURLForTaxa(id)))
			vfs.delete(ServerPaths.getURLForTaxa(id));
		else
			System.out.println("Error removing taxon at " + ServerPaths.getURLForTaxa(id));

		parent.removeChild(curTaxon);
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
	 * Exports the taxon as a new taxon, and sets the ID to the newly assigned
	 * one from the server.
	 * 
	 * @param taxon
	 *            to be saved
	 * @throws IOException
	 */
	private boolean exportTaxonAsNew(TaxonNode taxon) throws IOException {
		String curXML = TaxonNodeFactory.nodeToDetailedXML(taxon);
		return false;
//		Response response = uniform.put(uriPrefix + "/taxomatic/new", new StringRepresentation(curXML,
//				MediaType.TEXT_XML, null, CharacterSet.UTF_8));
//
//		if (response.getStatus().isSuccess()) {
//			try {
//				long newID = Long.parseLong(response.getEntity().getText());
//				taxon.setId(newID);
//
//				nodesBuilt++;
//				if (nodesBuilt % 500 == 0 || (nodesBuilt >= 13458 && nodesBuilt <= 13468))
//					System.out.println("Built " + nodesBuilt + " of 13468.");
//
//				return true;
//			} catch (Exception e) {
//				System.out.println("Failed to save new taxon to the system: " + curXML);
//				failed.set(true);
//				statusMessage.append("Failed to save a new taxon to the system.<br>");
//				statusMessage.append("Please forward this message: <br>");
//				statusMessage.append("Failed riap call to " + uriPrefix + "/taxomatic/new<br>");
//				statusMessage.append("with XML " + XMLUtils.clean(curXML) + "<br>");
//				statusMessage.append("Server returned entity: " + response.getEntity().getText() + "<br>");
//				return false;
//			}
//		} else
//			return false;
		// else
		// {
		// System.out.println("THE RESPONSE WAS A FAILURE DUDE.");
		// System.exit(0);
		// }
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
	private ArrayList<AssessmentData> generateAssessments(TaxonNode taxon, String exportID) throws DBException {
		SelectQuery select = new SelectQuery();
		select.select("assessment", "*");
		select.constrain(new CanonicalColumnName("assessment", "tax_id"), QConstraint.CT_EQUALS, exportID);

		Row.Set rows = new Row.Set();
		ec.doQuery(select, rows);

		List<Row> rowList = rows.getSet();
		ArrayList<AssessmentData> assessments = null;

		if (rowList != null) {
			assessments = new ArrayList<AssessmentData>();

			for (Row curRow : rowList) {
				AccessExportAssessment ass = new AccessExportAssessment(curRow);
				assessments.add(ass.getAssessment());
			}
		}

		return assessments;
	}

	private void removeAvesChildren() throws ConflictException, NotFoundException {
		TaxonomyDocUtils.toggleWriteBack(false);

		Document taxonomyDoc = TaxonomyDocUtils.getTaxonomyDocByID();
		Element aves = (Element) taxonomyDoc.getElementsByTagName("node" + avesID).item(0);
		for (Node curChild : new NodeCollection(aves.getChildNodes()))
			if (curChild.getNodeType() == Node.ELEMENT_NODE)
				deleteTaxon((Element) curChild, aves);

		Document taxonomyByNameDoc = TaxonomyDocUtils.getTaxonomyDocByName();
		aves = (Element) taxonomyByNameDoc.getElementsByTagName("AVES").item(0);
		for (Node curChild : new NodeCollection(aves.getChildNodes()))
			if (curChild.getNodeType() == Node.ELEMENT_NODE)
				aves.removeChild(curChild);

		TaxonomyDocUtils.toggleWriteBack(true);
		TaxonomyDocUtils.doWriteBack(0);
	}

	public void run() {
		if (!running.compareAndSet(false, true))
			return;
		try {
			doImport();
		} catch (final Throwable t) {
			t.printStackTrace();
		} finally {
			DBSessionFactory.unregisterDataSource(DS);
			running.set(false);
		}
		System.out.println("Exported a total of " + nodesBuilt + " taxa.");
	}
}
