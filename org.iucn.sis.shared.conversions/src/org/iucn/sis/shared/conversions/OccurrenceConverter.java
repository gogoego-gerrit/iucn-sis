package org.iucn.sis.shared.conversions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import javax.naming.NamingException;

import org.hibernate.criterion.Restrictions;
import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.server.api.persistance.AssessmentCriteria;
import org.iucn.sis.server.api.persistance.FieldCriteria;
import org.iucn.sis.server.api.utils.FilenameStriper;
import org.iucn.sis.shared.api.models.AssessmentChange;
import org.iucn.sis.shared.api.models.Field;
import org.iucn.sis.shared.api.models.fields.ProxyField;
import org.iucn.sis.shared.conversions.AssessmentConverter.ConversionMode;
import org.iucn.sis.shared.helpers.AssessmentData;
import org.iucn.sis.shared.helpers.AssessmentParser;
import org.iucn.sis.shared.helpers.CanonicalNames;

import com.solertium.db.CInteger;
import com.solertium.db.CString;
import com.solertium.db.CanonicalColumnName;
import com.solertium.db.DBException;
import com.solertium.db.DBSession;
import com.solertium.db.ExecutionContext;
import com.solertium.db.Row;
import com.solertium.db.RowProcessor;
import com.solertium.db.SystemExecutionContext;
import com.solertium.db.query.InsertQuery;
import com.solertium.db.query.QConstraint;
import com.solertium.db.query.SelectQuery;
import com.solertium.lwxml.factory.NativeDocumentFactory;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.util.TrivialExceptionHandler;
import com.solertium.vfs.VFS;

public class OccurrenceConverter extends GenericConverter<VFSInfo> {
	
	private static final String[] FIELDS = new String[] {
		CanonicalNames.CountryOccurrence, CanonicalNames.FAOOccurrence, CanonicalNames.LargeMarineEcosystems
	};
	
	private ExecutionContext SIS1;
	private ExecutionContext SIS2;
	private Map<String, Row.Set> lookups;
	
	private ConversionMode mode = ConversionMode.ALL;
	private ExecutionContext ec;
	
	public OccurrenceConverter() throws NamingException {
		this("sis_lookups", "sis1_lookups");
	}

	public OccurrenceConverter(String dbSessionName, String sis1DBS) throws NamingException {
		super();
		setClearSessionAfterTransaction(true);
		
		lookups = new HashMap<String, Row.Set>();
		
		SIS2 = new SystemExecutionContext(dbSessionName);
		SIS2.setAPILevel(ExecutionContext.SQL_ALLOWED);
		SIS2.setExecutionLevel(ExecutionContext.ADMIN);
		SIS2.getDBSession().setIdentifierCase(DBSession.CASE_UPPER);
		
		SIS1 = new SystemExecutionContext(sis1DBS);
		SIS1.setAPILevel(ExecutionContext.SQL_ALLOWED);
		SIS1.setExecutionLevel(ExecutionContext.ADMIN);
		SIS1.getDBSession().setIdentifierCase(DBSession.CASE_UPPER);
	}
	
	public void setConversionMode(ConversionMode mode) {
		this.mode = mode;
	}
	
	@Override
	protected void run() throws Exception {
		ec = new SystemExecutionContext(SIS.get().getExecutionContext().getDBSession());
		ec.setAPILevel(ExecutionContext.SQL_ALLOWED);
		ec.setExecutionLevel(ExecutionContext.ADMIN);
		
		String runMode = parameters.getFirstValue("mode", "create");
		
		if ("fix".equals(runMode)) {
			correct();
		}
		else if ("create".equals(runMode)) {
			rebuildTables();
			
			if (ConversionMode.DRAFT.equals(mode))
				convertAllDrafts(data.getOldVFS(), data.getNewVFS());
			else if (ConversionMode.PUBLISHED.equals(mode))
				convertAllPublished(data.getOldVFS(), data.getNewVFS());
			else {
				mode = ConversionMode.DRAFT;
				convertAllDrafts(data.getOldVFS(), data.getNewVFS());
				mode = ConversionMode.PUBLISHED;
				convertAllPublished(data.getOldVFS(), data.getNewVFS());
			}
		}
	}
	
	private void correct() throws DBException {
		for (final String name : FIELDS) {
			synchronized (this) {
				final AtomicInteger count = new AtomicInteger(0);
				
				printf("Running corrections for %s", name);
				
				SelectQuery query = new SelectQuery();
				query.select(getTableName(name), "internal_id", "ASC");
				query.select(getTableName(name), "*");
				if ("true".equals(parameters.getFirstValue("test"))) {
					query.constrain(new CanonicalColumnName(getTableName(name), "taxon_id"), 
							QConstraint.CT_EQUALS, parameters.getFirstValue("taxon_id", "171912"));
				}
				
				final AtomicReference<String> ref = new AtomicReference<String>("");
				final Map<Integer, Row> values = new ConcurrentHashMap<Integer, Row>();
				
				ec.doQuery(query, new RowProcessor() {
					public void process(Row row) {
						String current = ref.get();
						String internal_id = row.get("internal_id").toString();
						Integer index = row.get("index").getInteger();
						
						if ("".equals(current)) {
							ref.set(internal_id);
						}
						else if (!internal_id.equals(current)) {
							update(name, ref.get(), new HashMap<Integer, Row>(values));
							
							values.clear();
							
							ref.set(internal_id);
						}
						
						values.put(index, row);
							
						if (count.incrementAndGet() % 500 == 0)
							printf("%s...", count.get());
					}
				});
				
				update(name, ref.get(), values);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private void update(String name, String internal_id, Map<Integer, Row> data) {
		FieldCriteria criteria = new FieldCriteria(session);
		criteria.name.eq(name);
		
		AssessmentCriteria asmCrit = criteria.createAssessmentCriteria();
		asmCrit.internalId.eq(internal_id);
		
		Field field = null;
		try  {
			field = criteria.uniqueField();
		} catch (Exception e) {
			TrivialExceptionHandler.ignore(this, e);
		}
		
		if (field == null) {
			printf("No unique field found for Internal ID %s", internal_id);
			return;
		}
		
		List<AssessmentChange> changes = session.createCriteria(AssessmentChange.class)
			.add(Restrictions.eq("assessment", field.getAssessment()))
			.add(Restrictions.eq("fieldName", name))
			.list();
		if (!changes.isEmpty()) {
			printf("Not updating field for T%sA%s; has updates", 
				field.getAssessment().getTaxon().getId(), field.getAssessment().getId());
			return;
		}
		
		int count = 0, size = field.getFields().size();
		for (Field subfield : field.getFields()) {
			ProxyField proxy = new ProxyField(subfield);
			Integer value = proxy.getForeignKeyPrimitiveField(name + "Lookup");
			if (value == null) {
				printf("!! No lookup value for this field?!");
				continue;
			}
			
			Row row = data.get(value);
			if (row == null)
				printf("Value %s not found in %s", value, data.keySet());
			else {
				count++;
				
				proxy.setForeignKeyPrimitiveField("presence", row.get("presence").getInteger());
				proxy.setBooleanUnknownPrimitiveField("formerlyBred", row.get("formerlybred").getInteger());
				proxy.setForeignKeyPrimitiveField("origin", row.get("origin").getInteger());
				
				List<Integer> seasonalityValues = null;
				String seasonality = row.get("seasonality").toString();
				if (seasonality != null && !"".equals(seasonality)) {
					seasonalityValues = new ArrayList<Integer>();
					for (String current : seasonality.split(","))
						seasonalityValues.add(Integer.valueOf(current));
				}
				proxy.setForeignKeyListPrimitiveField("seasonality", seasonalityValues);
			}
		}
		
		session.update(field);
		
		commitAndStartTransaction();
		
		if (count != size)
			printf("Only updated %s/%s fields for T%sA%s", count, size, field.getAssessment().getTaxon().getId(), field.getAssessment().getId());
	}
	
	private void rebuildTables() throws DBException {
		for (String name : FIELDS) {
			try {
				ec.dropTable(getTableName(name));
				printf("Dropped existing table for %s", name);
			} catch (DBException e) {
				TrivialExceptionHandler.ignore(this, e);
			}
		}
		
		for (String name : FIELDS) {
			ec.createTable(getTableName(name), getPrototype());
			printf("Created new table for %s", name);
		}
	}
	
	private Row getPrototype() {
		Row prototype = new Row();
		prototype.add(new CString("internal_id", null));
		prototype.add(new CString("taxon_id", null));
		prototype.add(new CString("status", null));
		prototype.add(new CString("code", null));
		prototype.add(new CInteger("index", null));
		prototype.add(new CInteger("presence", 0));
		prototype.add(new CInteger("formerlybred", 0));
		prototype.add(new CInteger("origin", 0));
		prototype.add(new CString("seasonality", null));
		
		return prototype;
	}
	
	public void convertAllPublished(VFS oldVFS, VFS newVFS) throws Exception {
		File cache = new File(data.getOldVFSPath() + "/HEAD/migration/assessments.dat");
		if (cache.exists())
			convertCached(new AssessmentParser(), new AtomicInteger(), cache);
		else
			convertAllFaster("/HEAD/browse/assessments", oldVFS, newVFS);
	}
	
	public void convertAllDrafts(VFS oldVFS, VFS newVFS) throws Exception {
		convertAllFaster("/HEAD/drafts", oldVFS, newVFS);
	}
	
	public void convertAllFaster(String rootURL, VFS oldVFS, VFS newVFS) throws Exception {
		final AssessmentParser parser = new AssessmentParser();
		final AtomicInteger converted = new AtomicInteger(0);
		
		File folder = new File(data.getOldVFSPath() + rootURL);
		
		readFolder(parser, converted, folder);
	}
	
	private void convertCached(AssessmentParser parser, AtomicInteger converted, File cache) throws Exception {
		final BufferedReader reader = new BufferedReader(new FileReader(cache));
		String line = null;
		
		HashSet<String> taxa = new HashSet<String>();
		if (parameters.getFirstValue("subset", null) != null)
			for (String taxon : parameters.getValuesArray("subset"))
				taxa.add(taxon);
		
		boolean subset = !taxa.isEmpty();
		boolean found = false;
		boolean canStop = false;
		
		if (subset)
			printf("Converting the subset: %s", taxa);
		
		while ((line = reader.readLine()) != null) {
			String[] split = line.split(":");
			File file = new File(data.getOldVFSPath() + "/HEAD/browse/assessments/" + 
					FilenameStriper.getIDAsStripedPath(split[1]) + ".xml");
			if (!file.exists()) {
				printf("No assessment found on disk for taxon %s at %s", split[0], file.getPath());
				continue;
			}
			
			if (!subset || (found = taxa.contains(split[0])))
				readFile(parser, converted, file);
			
			if (subset && found)
				canStop = true;
			
			if (subset && !found && canStop)
				break;
		}
	}
	
	private void readFolder(AssessmentParser parser, AtomicInteger converted, File folder) throws Exception {
		for (File file : folder.listFiles()) {
			if (file.isDirectory())
				readFolder(parser, converted, file);
			else if (file.getName().endsWith(".xml"))
				readFile(parser, converted, file);
		}
	}
	
	private boolean isBird(String id) {
		if ("true".equals(parameters.getFirstValue("birds", "false")))
			return false;
		
		Row.Loader rl = new Row.Loader();
		
		try {
			SelectQuery query = new SelectQuery();
			query.select("aves", "id");
			query.constrain(new CanonicalColumnName("aves", "id"), QConstraint.CT_EQUALS, id);
			
			ec.doQuery(query, rl);
		} catch (Exception e) {
			return false;
		}
		
		return rl.getRow() != null;
	}
	
	private void readFile(AssessmentParser parser, AtomicInteger converted, File file) throws Exception {
		try {
			NativeDocument ndoc = NativeDocumentFactory.newNativeDocument();
			ndoc.parse(FileListing.readFileAsString(file));
			
			parser.parse(ndoc);
			AssessmentData assessData = parser.getAssessment();
			
			if (isBird(assessData.getSpeciesID())) {
				printf("Skipping assessment %s with taxon %s, as it is a bird...", 
						assessData.getAssessmentID(), assessData.getSpeciesID());
				return;
			}
			
			for (String name : FIELDS) {
				HashMap<String, ArrayList<String>> map = operateOn(assessData, name);
				if (map != null) {
					for (Map.Entry<String, ArrayList<String>> entry : map.entrySet()) {
						Integer codingOption = getIndex(name, name + "Lookup", name + "Lookup", entry.getKey());
						if (codingOption != null && codingOption.intValue() <= 0)
							codingOption = null;
						
						Row row = getPrototype();
						row.get("internal_id").setObject(assessData.getAssessmentID());
						row.get("taxon_id").setObject(assessData.getSpeciesID());
						row.get("status").setObject(assessData.isPublished() ? "published" : "draft");
						row.get("code").setObject(entry.getKey());
						row.get("index").setObject(codingOption);
						row.get("presence").setObject(toInt(entry.getValue().get(0)));
						row.get("formerlybred").setObject(toInt(entry.getValue().get(1)));
						row.get("origin").setObject(toInt(entry.getValue().get(2)));
						row.get("seasonality").setObject(toStr(entry.getValue().get(3)));
						
						InsertQuery query = new InsertQuery(getTableName(name), row);
						
						print(query.getSQL(ec.getDBSession()));
						
						ec.doUpdate(query);
					}
				}
			}
			
		} catch (Throwable e) {
			print("Failed on file " + file.getPath());
			e.printStackTrace();
			throw new Exception(e);
		}
	}
	
	private String getTableName(String fieldName) {
		return "Mig_" + fieldName;
	}
	
	private Integer toInt(String value) {
		if (value == null || "".equals(value) || "0".equals(value))
			return null;
		
		return Integer.valueOf(value);
	}
	
	private String toStr(String value) {
		if (value == null || "".equals(value))
			return null;
		
		return value;
	}
	
	/**
	 * This will migrate occurrence data from the old format (included passage
	 * migrant as its own check box) to the new format, with three select boxes.
	 * 
	 * @param data
	 *            Assessment to work on
	 * @return true/false, if changes were made to the assessment
	 */	
	@SuppressWarnings("unchecked")
	private HashMap<String, ArrayList<String>> operateOn(AssessmentData data, String canonicalName) {
		HashMap<String, ArrayList<String>> coo = null;
		if (data.getDataMap().containsKey(canonicalName)) {
			coo = (HashMap) data.getDataMap().get(canonicalName);
			for (Entry<String, ArrayList<String>> curSelected : coo.entrySet()) {
				if (curSelected.getValue().size() == 4) { // This is already modded
					//Continue to check origin since it can't hurt...
					String origin = curSelected.getValue().get(2);
					if (origin.equals("9"))
						curSelected.getValue().set(2, "6");
				} else {
					modifyOccurrenceEntry(curSelected);
				}
			}
		}
		return coo;
	}

	private void modifyOccurrenceEntry(Entry<String, ArrayList<String>> curSelected) {
		curSelected.getValue().ensureCapacity(4);

		String presenceCode = curSelected.getValue().get(0);
		String passageMigrant = curSelected.getValue().get(1);
		String origin = curSelected.getValue().get(2);

		String seasonality = "";

		if (!presenceCode.equals("") && !presenceCode.equals("0")) {
			int pCode = Integer.valueOf(presenceCode);
			if (pCode <= 3) {
				curSelected.getValue().set(0, "1");

				if (pCode == 1)
					seasonality += "1,";
				else if (pCode == 2)
					seasonality += "2,";
				else if (pCode == 3)
					seasonality += "3,";
			} else if (pCode == 4)
				curSelected.getValue().set(0, "2");
			else if (pCode == 5)
				curSelected.getValue().set(0, "3");
			else if (pCode == 6)
				curSelected.getValue().set(0, "4");
		} else {
			curSelected.getValue().set(0, "0");
		}

		//Formerly bred is unseleted
		curSelected.getValue().set(1, "0");

		if (passageMigrant.equals("true"))
			seasonality += "4";

		if (!origin.equals("") && !origin.equals("0")) {
			int oCode = Integer.valueOf(origin);

			if (oCode == 1)
				curSelected.getValue().set(2, "1");
			else if (oCode == 2)
				curSelected.getValue().set(2, "3");
			else if (oCode == 3)
				curSelected.getValue().set(2, "2");
			else if (oCode == 4)
				curSelected.getValue().set(2, "5");
			else if (oCode == 5)
				curSelected.getValue().set(2, "6");
			else if (oCode == 9) // This shouldn't be in there, but somehow a
				// few are...
				curSelected.getValue().set(2, "6");
		} else
			curSelected.getValue().set(2, "0");

		if (seasonality.endsWith(","))
			seasonality = seasonality.substring(0, seasonality.length() - 1);

		curSelected.getValue().add(seasonality);
	}
	
	private Integer getIndex(String canonicalName, String libraryTable, String name, String value) throws DBException {
//		String table = canonicalName + "_" + name + "Lookup";
		
		for( Row row : getLookup(libraryTable).getSet() ) {
			if (row.get("code") != null) {
				if (correctCode(value).equalsIgnoreCase(row.get("code").getString()))
					return row.get("id").getInteger();
			} else if( value.equalsIgnoreCase(row.get("label").getString()) || 
					value.equalsIgnoreCase( Integer.toString((Integer.parseInt(
							row.get("name").getString())+1)) ) )
				return row.get("id").getInteger();
		}
		if( !value.equals("0") ) {
			printf("For %s.%s, didn't find a lookup in %s to match: %s", 
				canonicalName, name, libraryTable, value);
			return -1;
		} else
			return 0;
	}
	
	private Row.Set getLookup(String table) throws DBException {
		String fieldName = table;
		if (fieldName.equalsIgnoreCase(CanonicalNames.ReproduictivePeriodicity))
			fieldName = org.iucn.sis.shared.api.utils.CanonicalNames.ReproductivePeriodicity;
		
		if (lookups.containsKey(fieldName))
			return lookups.get(fieldName);
		else {
			SelectQuery query = new SelectQuery();
			query.select(fieldName, "ID", "ASC");
			query.select(fieldName, "*");
			
			Row.Set lookup = new Row.Set();
			
			try {
				SIS2.doQuery(query, lookup);
			} catch (DBException e) {
				SIS1.doQuery(query, lookup);
			}

			lookups.put(fieldName, lookup);

			return lookup;
		}
	}
	
	private String correctCode(String code) {
		if ("NLA-CU".equals(code))
			return "CW";
		
		return code;
	}

}
