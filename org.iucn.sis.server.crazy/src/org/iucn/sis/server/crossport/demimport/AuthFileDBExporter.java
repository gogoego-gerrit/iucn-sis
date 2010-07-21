package org.iucn.sis.server.crossport.demimport;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;

import org.iucn.sis.server.utils.DocumentUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.solertium.db.BackgroundExecutionContext;
import com.solertium.db.CanonicalColumnName;
import com.solertium.db.DBException;
import com.solertium.db.DBSession;
import com.solertium.db.DBSessionFactory;
import com.solertium.db.ExecutionContext;
import com.solertium.db.Row;
import com.solertium.db.query.QConstraint;
import com.solertium.db.query.SelectQuery;
import com.solertium.util.MD5Hash;
import com.solertium.util.SysDebugger;
import com.solertium.vfs.NotFoundException;
import com.solertium.vfs.VFS;
import com.solertium.vfs.VFSFactory;

public class AuthFileDBExporter {

	public static void main(String args[]) throws Exception {
		AuthFileDBExporter me = new AuthFileDBExporter();
		me.buildAuthFiles();
	}

	private DBSession dbsess;

	private ExecutionContext ec = new BackgroundExecutionContext(DEMImport.class.getName());

	private HashMap<String, Element> elements;

	private VFS vfs;

	private void attachAllToDoc(Document document) {
		for (Iterator<Element> iter = elements.values().iterator(); iter.hasNext();) {
			Element curElement = iter.next();

			int depth = Integer.parseInt(curElement.getAttribute("depth"));

			if (depth == 1 || depth == 0)
				document.getDocumentElement().appendChild(curElement);
			else {
				String levelID = curElement.getAttribute("id");

				String parentLevel = null;

				if (levelID.startsWith("101"))
					parentLevel = "100";
				else
					parentLevel = levelID.substring(0, levelID.lastIndexOf('.'));

				elements.get(parentLevel).appendChild(curElement);
			}
		}

		for (Iterator<Element> iter = elements.values().iterator(); iter.hasNext();) {
			Element curElement = iter.next();

			if (curElement.getElementsByTagName("child").getLength() > 0)
				curElement.setAttribute("codeable", "false");
			else
				curElement.setAttribute("codeable", "true");
		}
	}

	// private void findTheMissingCountry() throws Exception
	// {
	// switchToDBSession("rldb");
	//		
	// SelectQuery rldbSelect = new SelectQuery();
	// rldbSelect.select("Country Authority File", "*");
	// rldbSelect.select("Country Authority File", "CtyRecID", "ASC");
	//		
	// Row.Set rldbRowLoader = new Row.Set();
	// ec.doQuery(rldbSelect, rldbRowLoader);
	//		
	//		
	// switchToDBSession("dem");
	//		
	// SelectQuery demSelect = new SelectQuery();
	// demSelect.select("countries_list_all", "*");
	// demSelect.select("countries_list_all", "Country_Number", "ASC");
	//		
	// Row.Set demRowLoader = new Row.Set();
	// ec.doQuery(demSelect, demRowLoader );
	//		
	// Iterator<Row> demIter = demRowLoader.getSet().listIterator();
	// Iterator<Row> rldbIter = rldbRowLoader.getSet().listIterator();
	//		
	// boolean found = false;
	//		
	// while( demIter.hasNext() && !found )
	// {
	// Row curDEMRow = demIter.next();
	// Row curRLDBRow = rldbIter.next();
	//			
	// int DEMcountryNumber = curDEMRow.get("Country_Number").getInteger();
	// String DEMcountryName = curDEMRow.get("Country").getString().trim();
	//			
	// int rldbCountryNumber = curRLDBRow.get("CtyRecID").getInteger();
	// String rldbCountryName =
	// curRLDBRow.get("CtyIUCNName").getString().trim();
	//			
	// if( DEMcountryNumber != rldbCountryNumber || !DEMcountryName.equals(
	// rldbCountryName ))
	// {
	// SysDebugger.getInstance().println("--- Found a mismatch ---");
	// SysDebugger.getInstance().println("DEM record: " + DEMcountryNumber +
	// " : " + DEMcountryName );
	// SysDebugger.getInstance().println("RLDB record: " + rldbCountryNumber +
	// " : " + rldbCountryName );
	//				
	// // found = true;
	// }
	// }
	// }

	public void buildAuthFiles() throws Exception {
		elements = new LinkedHashMap<String, Element>();

		registerDatasource("rldb", "jdbc:access:////usr/data/rldbRelationshipFree.mdb",
				"com.hxtt.sql.access.AccessDriver", "", "");
		registerDatasource("dem", "jdbc:access:////usr/data/demSource.mdb", "com.hxtt.sql.access.AccessDriver", "", "");

		initVFS();

		// findTheMissingCountry();

		switchToDBSession("rldb");
		// buildConservationActionsAuthFile();
		// elements.clear();
		// buildHabitatsAuthFile();
		// elements.clear();
		// buildThreatsAuthFile();
		// elements.clear();
		// buildResearchAuthFile();
		// elements.clear();
		// buildStressAuthFile();
		// elements.clear();
		buildCountriesAuthFile();
		elements.clear();
		//		
		// switchToDBSession("dem");
		// buildLMOAuthFile();
		// elements.clear();
		// buildPlantGrowthFile();
		// elements.clear();
		// buildLandCoverFile();
		// elements.clear();
		// buildRiversFile();
		// elements.clear();
		// buildLakesFile();
		// elements.clear();
	}

	private void buildConservationActionsAuthFile() throws DBException {
		Document doc = DocumentUtils.createDocumentFromString("<treeRoot></treeRoot>");

		SelectQuery select = new SelectQuery();
		select.select("ZZ_conservation actions AF_new", "*");

		Row.Set rowLoader = new Row.Set();
		ec.doQuery(select, rowLoader);

		Element lastNode = null;

		for (Iterator<Row> iter = rowLoader.getSet().listIterator(); iter.hasNext();) {
			Row curRow = iter.next();

			String code = curRow.get("Measure_code").getString().trim();
			String levelID = curRow.get("Measure_ref").getString().trim();
			String description = curRow.get("Measure_description").getString().trim();
			int depth = curRow.get("Measure_level").getInteger();

			lastNode = insertRow(doc, lastNode, code, levelID, description, depth);
		}
		attachAllToDoc(doc);

		DocumentUtils.writeVFSFile("/auth/consMeasures.xml", vfs, doc);
	}

	private void buildCountriesAuthFile() throws DBException {
		Document doc = DocumentUtils.createDocumentFromString("<treeRoot></treeRoot>");

		SelectQuery select = new SelectQuery();
		select.select("Country Authority File", "*");

		Row.Set rowLoader = new Row.Set();
		ec.doQuery(select, rowLoader);

		for (Iterator<Row> iter = rowLoader.getSet().listIterator(); iter.hasNext();) {
			Row curRow = iter.next();

			String region = curRow.get("CtyRegion").getString();
			Element regionElement = elements.get(region);

			if (regionElement == null) {
				Element curElement = doc.createElement("root");

				curElement.setAttribute("code", new MD5Hash(region).toString());
				curElement.setAttribute("depth", "1");
				curElement.setAttribute("id", "0");
				curElement.setAttribute("codeable", "false");

				Element label = doc.createElement("label");
				label.setTextContent(region);

				curElement.appendChild(label);

				elements.put(region, curElement);

				regionElement = curElement;
				doc.getDocumentElement().appendChild(regionElement);
			}

			String code = curRow.get("CtyISO2").getString().trim();
			String levelID = curRow.get("CtyISO2").getString().trim();
			String description = curRow.get("CtyShort").getString().trim();
			int depth = 2;

			Element curElement = doc.createElement("child");

			curElement.setAttribute("code", code);
			curElement.setAttribute("depth", "" + depth);
			curElement.setAttribute("id", levelID);
			curElement.setAttribute("codeable", "true");

			Element label = doc.createElement("label");
			label.setTextContent(description);

			curElement.appendChild(label);

			elements.put(code, curElement);

			regionElement.appendChild(curElement);

			SelectQuery subSelect = new SelectQuery();
			subSelect.select("Subcountry BRU Authority File", "*");
			subSelect.constrain(new CanonicalColumnName("Subcountry BRU Authority File", "CtyISO2"),
					QConstraint.CT_EQUALS, code);

			Row.Set subRowLoader = new Row.Set();
			ec.doQuery(subSelect, subRowLoader);

			for (Iterator<Row> innerIter = subRowLoader.getSet().listIterator(); innerIter.hasNext();) {
				Row curSubRow = innerIter.next();

				String subCode = curSubRow.get("BruLevel4Code").getString();
				String subLevelID = curSubRow.get("BruLevel4Code").getString().trim();
				String subDescription = curSubRow.get("BruLevel4Name").getString().trim();
				int subDepth = 3;

				Element curSubCountry = doc.createElement("child");

				curSubCountry.setAttribute("code", subCode);
				curSubCountry.setAttribute("depth", "" + subDepth);
				curSubCountry.setAttribute("id", subLevelID);
				curSubCountry.setAttribute("codeable", "true");

				Element subLabel = doc.createElement("label");
				subLabel.setTextContent(subDescription);

				curSubCountry.appendChild(subLabel);

				curElement.appendChild(curSubCountry);
			}
		}

		DocumentUtils.writeVFSFile("/auth/countries.xml", vfs, doc);
	}

	private void buildHabitatsAuthFile() throws DBException {
		Document doc = DocumentUtils.createDocumentFromString("<treeRoot></treeRoot>");

		SelectQuery select = new SelectQuery();
		select.select("General_Habitat_Authority_File", "*");
		select.select("General_Habitat_Authority_File", "order", "ASC");

		SysDebugger.getInstance().println(select.getSQL(ec.getDBSession()));

		Row.Set rowLoader = new Row.Set();
		ec.doQuery(select, rowLoader);

		Element lastNode = null;

		for (Iterator<Row> iter = rowLoader.getSet().listIterator(); iter.hasNext();) {
			Row curRow = iter.next();

			String code = curRow.get("Gh_N").getString().trim();
			String levelID = curRow.get("Middle level code").getString().trim();
			String description = curRow.get("Description").getString().trim();
			int depth = curRow.get("GH_level").getInteger();

			lastNode = insertRow(doc, lastNode, code, levelID, description, depth);
		}
		attachAllToDoc(doc);

		DocumentUtils.writeVFSFile("/auth/habitats.xml", vfs, doc);
	}

	private void buildLakesFile() throws DBException {
		Document doc = DocumentUtils.createDocumentFromString("<treeRoot></treeRoot>");

		SelectQuery select = new SelectQuery();
		select.select("Lakes_list", "*");

		Row.Set rowLoader = new Row.Set();
		ec.doQuery(select, rowLoader);

		Element lastNode = null;
		int id = 0;

		for (Iterator<Row> iter = rowLoader.getSet().listIterator(); iter.hasNext();) {
			Row curRow = iter.next();
			id++;

			String code = curRow.get("aquatic_number").getString().trim();
			String levelID = "" + id;
			String description = curRow.get("Aquatic_description").getString();

			int depth = 1;

			lastNode = insertRow(doc, lastNode, code, levelID, description, depth);
		}
		attachAllToDoc(doc);

		DocumentUtils.writeVFSFile("/auth/lakes.xml", vfs, doc);
	}

	private void buildLandCoverFile() throws DBException {
		Document doc = DocumentUtils.createDocumentFromString("<treeRoot></treeRoot>");

		SelectQuery select = new SelectQuery();
		select.select("land_cover_list", "*");

		Row.Set rowLoader = new Row.Set();
		ec.doQuery(select, rowLoader);

		Element lastNode = null;

		for (Iterator<Row> iter = rowLoader.getSet().listIterator(); iter.hasNext();) {
			Row curRow = iter.next();

			String code = curRow.get("lc_N").getString().trim();
			String levelID = curRow.get("lc_N").getString().trim();
			String description = curRow.get("Description").getString();

			int depth = 1;

			lastNode = insertRow(doc, lastNode, code, levelID, description, depth);
		}
		attachAllToDoc(doc);

		DocumentUtils.writeVFSFile("/auth/landCover.xml", vfs, doc);
	}

	private void buildLMOAuthFile() throws DBException {
		Document doc = DocumentUtils.createDocumentFromString("<treeRoot></treeRoot>");

		SelectQuery select = new SelectQuery();
		select.select("large_marine_ecosystems_list", "*");

		Row.Set rowLoader = new Row.Set();
		ec.doQuery(select, rowLoader);

		Element lastNode = null;

		for (Iterator<Row> iter = rowLoader.getSet().listIterator(); iter.hasNext();) {
			Row curRow = iter.next();

			String code = curRow.get("lme_code").getString().trim();
			String levelID = curRow.get("Marine_ref").getString().trim();
			String description = curRow.get("Marine_description").getString();
			description = description.replaceAll("\\d", "");
			description = description.replaceAll("\\.", "");
			description = description.replaceAll("LME", "");
			description = description.trim();

			int depth = curRow.get("Marine_level").getInteger();

			lastNode = insertRow(doc, lastNode, code, levelID, description, depth);
		}
		attachAllToDoc(doc);

		DocumentUtils.writeVFSFile("/auth/lmo.xml", vfs, doc);
	}

	private void buildPlantGrowthFile() throws DBException {
		Document doc = DocumentUtils.createDocumentFromString("<treeRoot></treeRoot>");

		SelectQuery select = new SelectQuery();
		select.select("Growth_Forms_Authority_File", "*");

		Row.Set rowLoader = new Row.Set();
		ec.doQuery(select, rowLoader);

		Element lastNode = null;

		for (Iterator<Row> iter = rowLoader.getSet().listIterator(); iter.hasNext();) {
			Row curRow = iter.next();

			String code = curRow.get("Growthform_code").getString().trim();
			String levelID = curRow.get("Growthform_abb").getString().trim();
			String description = curRow.get("GFFull").getString();
			String title = curRow.get("GFDefn").getString();

			int depth = 1;

			lastNode = insertRow(doc, lastNode, code, levelID, description, depth);
		}
		attachAllToDoc(doc);

		DocumentUtils.writeVFSFile("/auth/plantGrowth.xml", vfs, doc);
	}

	private void buildResearchAuthFile() throws DBException {
		Document doc = DocumentUtils.createDocumentFromString("<treeRoot></treeRoot>");

		SelectQuery select = new SelectQuery();
		select.select("ZZZ_RESEARCH AF_NEW", "*");

		Row.Set rowLoader = new Row.Set();
		ec.doQuery(select, rowLoader);

		Element lastNode = null;

		for (Iterator<Row> iter = rowLoader.getSet().listIterator(); iter.hasNext();) {
			Row curRow = iter.next();

			String code = curRow.get("Research_code").getString().trim();
			String levelID = curRow.get("Research_ref").getString().trim();
			String description = curRow.get("Research_description").getString().trim();
			int depth = curRow.get("Research_level").getInteger();

			lastNode = insertRow(doc, lastNode, code, levelID, description, depth);
		}
		attachAllToDoc(doc);

		DocumentUtils.writeVFSFile("/auth/research.xml", vfs, doc);
	}

	private void buildRiversFile() throws DBException {
		Document doc = DocumentUtils.createDocumentFromString("<treeRoot></treeRoot>");

		SelectQuery select = new SelectQuery();
		select.select("Rivers_list", "*");

		Row.Set rowLoader = new Row.Set();
		ec.doQuery(select, rowLoader);

		Element lastNode = null;
		int id = 0;

		for (Iterator<Row> iter = rowLoader.getSet().listIterator(); iter.hasNext();) {
			Row curRow = iter.next();
			id++;

			String code = curRow.get("aquatic_number").getString().trim();
			String levelID = "" + id;
			String description = curRow.get("Aquatic_description").getString();

			int depth = 1;

			lastNode = insertRow(doc, lastNode, code, levelID, description, depth);
		}
		attachAllToDoc(doc);

		DocumentUtils.writeVFSFile("/auth/rivers.xml", vfs, doc);
	}

	private void buildStressAuthFile() throws DBException {
		Document doc = DocumentUtils.createDocumentFromString("<treeRoot></treeRoot>");

		SelectQuery select = new SelectQuery();
		select.select("ZZZ_stress_list", "*");

		Row.Set rowLoader = new Row.Set();
		ec.doQuery(select, rowLoader);

		Element lastNode = null;

		for (Iterator<Row> iter = rowLoader.getSet().listIterator(); iter.hasNext();) {
			Row curRow = iter.next();

			String code = curRow.get("stress_code").getString().trim();
			String levelID = curRow.get("stress_ref").getString().trim();
			String description = curRow.get("stress_description").getString().trim();
			int depth = curRow.get("stress_level").getInteger();

			lastNode = insertRow(doc, lastNode, code, levelID, description, depth);
		}
		attachAllToDoc(doc);

		DocumentUtils.writeVFSFile("/auth/stress.xml", vfs, doc);
	}

	private void buildThreatsAuthFile() throws DBException {
		Document doc = DocumentUtils.createDocumentFromString("<treeRoot></treeRoot>");

		SelectQuery select = new SelectQuery();
		select.select("ZZZ_threat_list_new", "*");

		Row.Set rowLoader = new Row.Set();
		ec.doQuery(select, rowLoader);

		Element lastNode = null;

		for (Iterator<Row> iter = rowLoader.getSet().listIterator(); iter.hasNext();) {
			Row curRow = iter.next();

			String code = curRow.get("Threat_code").getString().trim();
			String levelID = curRow.get("Threat_ref").getString().trim();
			String description = curRow.get("Threat_description").getString().trim();
			int depth = curRow.get("Threat_level").getInteger();

			lastNode = insertRow(doc, lastNode, code, levelID, description, depth);
		}
		attachAllToDoc(doc);

		DocumentUtils.writeVFSFile("/auth/threats.xml", vfs, doc);
	}

	private void initVFS() {
		final File spec = new File("/var/sis/peru/sis/vfs");
		try {
			vfs = VFSFactory.getVFS(spec);
		} catch (final NotFoundException nf) {
			throw new RuntimeException("VFS " + spec.getPath() + " could not be opened.");
		}
	}

	private Element insertRow(Document document, Element lastNode, String code, String levelID, String description,
			int depth) {
		return insertRow(document, lastNode, null, code, levelID, description, depth);
	}

	private Element insertRow(Document document, Element lastNode, String title, String code, String levelID,
			String description, int depth) {
		Element curElement;

		if (depth == 1)
			curElement = document.createElement("root");
		else
			curElement = document.createElement("child");

		curElement.setAttribute("code", code);
		curElement.setAttribute("depth", "" + depth);
		curElement.setAttribute("id", levelID);

		Element label = document.createElement("label");
		label.setTextContent(description);

		curElement.appendChild(label);

		elements.put(levelID, curElement);

		return curElement;
	}

	private void registerDatasource(String name, String URL, String driver, String username, String pass)
			throws Exception {
		ec.setExecutionLevel(ExecutionContext.READ_WRITE);
		DBSessionFactory.registerDataSource(name, URL, driver, username, pass);
	}

	private void switchToDBSession(String name) throws Exception {
		dbsess = DBSessionFactory.getDBSession(name);
		ec.setDBSession(dbsess);
	}
}
