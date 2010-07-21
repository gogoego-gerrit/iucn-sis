package org.iucn.sis.server.utils.scripts;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.iucn.sis.server.simple.SISContainerApp;
import org.iucn.sis.shared.data.assessments.AssessmentData;
import org.iucn.sis.shared.data.assessments.CanonicalNames;
import org.iucn.sis.shared.data.assessments.OccurrenceMigratorUtils;
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
import com.solertium.db.DBException;
import com.solertium.db.DBSessionFactory;
import com.solertium.db.ExecutionContext;
import com.solertium.db.Row;
import com.solertium.db.SystemExecutionContext;
import com.solertium.db.query.QConstraint;
import com.solertium.db.query.SelectQuery;
import com.solertium.vfs.VFS;

public class PublishedAssessmentDataVerifier extends BasePublishedAssessmentModder {
	public static class PublishedAssessmentDataVerifierResource extends Resource {

		public PublishedAssessmentDataVerifierResource() {
		}

		public PublishedAssessmentDataVerifierResource(final Context context, final Request request,
				final Response response) {
			super(context, request, response);
			getVariants().add(new Variant(MediaType.TEXT_HTML));
		}

		@Override
		public Representation represent(final Variant variant) {
			if (!BasePublishedAssessmentModder.running) {
				new Thread(new PublishedAssessmentDataVerifier(SISContainerApp.getStaticVFS())).run();
				System.out.println("Started a new published data verifier!");
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

	public PublishedAssessmentDataVerifier(File vfsRoot) {
		super(vfsRoot);
	}

	public PublishedAssessmentDataVerifier(VFS vfs) {
		super(vfs);
	}

	@SuppressWarnings(value = "unchecked")
	private boolean buildAquaticFAO(String spcRecID, AssessmentData curAssessment) throws Exception {
		boolean changed = false;

		SelectQuery select = new SelectQuery();
		select.select("DistribAquatic", "*");
		select.constrain(new CanonicalColumnName("DistribAquatic", "DaqSpcRecID"), QConstraint.CT_EQUALS, spcRecID);

		Row.Set rowLoader = new Row.Set();
		ec.doQuery(select, rowLoader);

		HashMap<String, ArrayList<String>> faos = (HashMap<String, ArrayList<String>>) curAssessment.getDataMap().get(
				CanonicalNames.FAOOccurrence);

		if (faos == null) {
			faos = new HashMap<String, ArrayList<String>>();
			curAssessment.getDataMap().put(CanonicalNames.FAOOccurrence, faos);
		}

		for (Iterator<Row> iter = rowLoader.getSet().listIterator(); iter.hasNext();) {
			Row curRow = iter.next();

			String curCode = curRow.get("AquRecID").getString();

			ArrayList<String> dataList = new ArrayList<String>();

			String p_code = curRow.get("p_code").getString() == null ? "1" : curRow.get("p_code").getString();
			String m_code = curRow.get("m_code").getString() == null ? "0" : curRow.get("m_code").getString();
			String o_code = curRow.get("o_code").getString() == null ? "0" : curRow.get("o_code").getString();

			if (p_code.trim().equals("9"))
				p_code = "6";
			if (m_code.trim().equals("9"))
				m_code = "5";

			dataList.add(p_code);
			dataList.add(new Boolean(m_code.equals("1")).toString());
			dataList.add(o_code);

			if (!faos.containsKey(curCode) && (p_code.trim().equals("9") || m_code.trim().equals("9"))) {
				if (p_code.trim().equals("9"))
					p_code = "6";
				if (m_code.trim().equals("9"))
					m_code = "5";

				dataList.add(p_code);
				dataList.add(new Boolean(m_code.equals("1")).toString());
				dataList.add(o_code);

				System.out.println("Found missing fao " + curCode + " in " + curAssessment.getAssessmentID());
				faos.put(curCode, dataList);
				changed = true;
			}
		}

		curAssessment.getDataMap().put(CanonicalNames.FAOOccurrence, faos);
		return changed;
	}

	@SuppressWarnings(value = "unchecked")
	private boolean buildCountries(String spcRecID, AssessmentData curAssessment) throws Exception {
		boolean changed = false;

		SelectQuery select = new SelectQuery();
		select.select("DistribCountry", "*");
		select.constrain(new CanonicalColumnName("DistribCountry", "DctSpcRecID"), QConstraint.CT_EQUALS, spcRecID);

		Row.Set rowLoader = new Row.Set();
		ec.doQuery(select, rowLoader);

		HashMap<String, ArrayList<String>> countries = (HashMap<String, ArrayList<String>>) curAssessment.getDataMap()
				.get(CanonicalNames.CountryOccurrence);

		if (countries == null) {
			countries = new HashMap<String, ArrayList<String>>();
			curAssessment.getDataMap().put(CanonicalNames.CountryOccurrence, countries);
		}

		for (Iterator<Row> iter = rowLoader.getSet().listIterator(); iter.hasNext();) {
			Row curRow = iter.next();

			ArrayList<String> dataList = new ArrayList<String>();

			String p_code = curRow.get("p_code").getString() == null ? "1" : curRow.get("p_code").getString();
			String m_code = curRow.get("m_code").getString() == null ? "0" : curRow.get("m_code").getString();
			String o_code = curRow.get("o_code").getString() == null ? "0" : curRow.get("o_code").getString();
			String note = curRow.get("DctNotes").getString(Column.EMPTY_IS_NULL);
			String isoCode = curRow.get("CtyISO2").getString();

			if (!countries.containsKey(isoCode) && (p_code.trim().equals("9") || m_code.trim().equals("9"))) {
				if (p_code.trim().equals("9"))
					p_code = "6";
				if (m_code.trim().equals("9"))
					m_code = "5";

				dataList.add(p_code);
				dataList.add(new Boolean(m_code.equals("1")).toString());
				dataList.add(o_code);

				System.out.println("Found missing country " + isoCode + " in " + curAssessment.getAssessmentID());
				countries.put(isoCode, dataList);
				changed = true;
			}
		}

		select = new SelectQuery();
		select.select("DistribSubcountry", "*");
		select.constrain(new CanonicalColumnName("DistribSubcountry", "DbruSpcRecID"), QConstraint.CT_EQUALS, spcRecID);

		rowLoader = new Row.Set();
		ec.doQuery(select, rowLoader);

		for (Iterator<Row> iter = rowLoader.getSet().listIterator(); iter.hasNext();) {
			Row curRow = iter.next();

			ArrayList<String> dataList = new ArrayList<String>();

			String p_code = curRow.get("p_code").getString() == null ? "1" : curRow.get("p_code").getString();
			String m_code = curRow.get("m_code").getString() == null ? "0" : curRow.get("m_code").getString();
			String o_code = curRow.get("o_code").getString() == null ? "0" : curRow.get("o_code").getString();

			select = new SelectQuery();
			select.select("Subcountry BRU Authority File", "*");
			select.constrain(new CanonicalColumnName("Subcountry BRU Authority File", "BruRecID"),
					QConstraint.CT_EQUALS, curRow.get("DbruRecID"));

			Row.Loader loader = new Row.Loader();
			ec.doQuery(select, loader);

			String subCountryCode = loader.getRow().get("BruLevel4Code").getString();

			if (!countries.containsKey(subCountryCode) && (p_code.trim().equals("9") || m_code.trim().equals("9"))) {
				if (p_code.trim().equals("9"))
					p_code = "6";
				if (m_code.trim().equals("9"))
					m_code = "5";

				dataList.add(p_code);
				dataList.add(new Boolean(m_code.equals("1")).toString());
				dataList.add(o_code);

				System.out.println("Found missing subcountry " + subCountryCode + " in "
						+ curAssessment.getAssessmentID());
				countries.put(subCountryCode, dataList);
				changed = true;
			}
		}

		return changed;
	}

	private boolean checkExtinctData(AssessmentData data) {
		String curvalue = null;

		if (data.getDataMap().containsKey(CanonicalNames.RedListCriteria))
			curvalue = (String) ((ArrayList) data.getDataMap().get(CanonicalNames.RedListCriteria))
					.get(SISCategoryAndCriteria.POSSIBLY_EXTINCT_INDEX);

		if (curvalue == null || curvalue.equalsIgnoreCase("true")) {
			return false;
		} else if (curvalue.trim().equalsIgnoreCase("n")) {
			((ArrayList) data.getDataMap().get(CanonicalNames.RedListCriteria)).set(
					SISCategoryAndCriteria.POSSIBLY_EXTINCT_INDEX, "false");
			return true;
		} else if (curvalue.trim().equalsIgnoreCase("y")) {
			((ArrayList) data.getDataMap().get(CanonicalNames.RedListCriteria)).set(
					SISCategoryAndCriteria.POSSIBLY_EXTINCT_INDEX, "true");
			return true;
		} else if (curvalue.trim().equalsIgnoreCase("false")) {
			SelectQuery select = new SelectQuery();
			select.select("RedList_NEW", "RlsSpcRecID");
			select.select("RedList_NEW", "RlsPossiblyExtinct");
			select.constrain(new CanonicalColumnName("RedList_NEW", "RlsSpcRecID"), QConstraint.CT_EQUALS, data
					.getAssessmentID());

			Row.Loader rowLoader = new Row.Loader();

			try {
				ec.doQuery(select, rowLoader);
			} catch (DBException e) {
				return false;
			}

			if (rowLoader.getRow() != null) {
				String value = rowLoader.getRow().get("RlsPossiblyExtinct").getString();

				if (value != null) {
					if (value.trim().equalsIgnoreCase("Y")) {
						System.out.println("Found a possibly extinct that should be Y in " + data.getAssessmentID());
						((ArrayList) data.getDataMap().get(CanonicalNames.RedListCriteria)).set(
								SISCategoryAndCriteria.POSSIBLY_EXTINCT_INDEX, "true");
						return true;
					}
				}
			}

			return false;
		} else {
			System.out.println("WTF? PossiblyExtinct for assessment " + data.getAssessmentID() + " == " + curvalue);
			return false;
		}
	}

	private boolean checkOccurrenceData(AssessmentData data) {
		boolean changed = false;

		// Make sure all of the Countries are there...
		try {
			// if (Integer.valueOf(data.getAssessmentID()).intValue() < 200000)
			// {
			if (buildCountries(data.getSpeciesID(), data)) {
				changed = true;
				OccurrenceMigratorUtils.operateOn(data, CanonicalNames.CountryOccurrence);
			}
			if (buildAquaticFAO(data.getSpeciesID(), data)) {
				changed = true;
				OccurrenceMigratorUtils.operateOn(data, CanonicalNames.FAOOccurrence);
			}
			// }
		} catch (Exception e) {
			System.out.println("Error checking on countries for assessment " + data.getAssessmentID());
			e.printStackTrace();
		}

		return changed;
	}

	private boolean checkVersion(AssessmentData data) {
		String curvalue = null;

		if (data.getDataMap().containsKey(CanonicalNames.RedListCriteria))
			curvalue = (String) ((ArrayList) data.getDataMap().get(CanonicalNames.RedListCriteria))
					.get(SISCategoryAndCriteria.CRIT_VERSION_INDEX);

		if (curvalue.equals("3.1")) {
			((ArrayList) data.getDataMap().get(CanonicalNames.RedListCriteria)).set(
					SISCategoryAndCriteria.CRIT_VERSION_INDEX, "0");
			return true;
		} else if (curvalue.equals("2.3")) {
			((ArrayList) data.getDataMap().get(CanonicalNames.RedListCriteria)).set(
					SISCategoryAndCriteria.CRIT_VERSION_INDEX, "1");
			return true;
		} else if (curvalue.equals("")) {
			((ArrayList) data.getDataMap().get(CanonicalNames.RedListCriteria)).set(
					SISCategoryAndCriteria.CRIT_VERSION_INDEX, "2");
			return true;
		}

		return false;

	}

	private void registerDatasource(String sessionName, String URL, String driver, String username, String pass)
			throws Exception {
		DBSessionFactory.registerDataSource(sessionName, URL, driver, username, pass);
	}

	protected boolean repairExisting(final AssessmentData data) {
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

		return doWriteBack;
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
		boolean changed = false;

		// changed = checkOccurrenceData(data);
		// changed = repairExisting(data);

		// changed = checkExtinctData(data);
		//
		// if (checkVersion(data))
		// changed = true;

		// if (changed)
		// writeBackPublishedAssessment(data);
	}

	@Override
	protected void workOnMostRecent(AssessmentData data) {
		boolean changed = false;

		changed = checkOccurrenceData(data);
		// changed = repairExisting(data);

		// if (checkExtinctData(data))
		// changed = true;
		// if (checkVersion(data))
		// changed = true;
		//
		if (changed)
			writeBackPublishedAssessment(data);
	}
}
