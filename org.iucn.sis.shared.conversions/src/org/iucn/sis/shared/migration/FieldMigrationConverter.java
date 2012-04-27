package org.iucn.sis.shared.migration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.naming.NamingException;

import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.server.api.persistance.AssessmentCriteria;
import org.iucn.sis.server.api.utils.FilenameStriper;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.conversions.FileListing;
import org.iucn.sis.shared.conversions.GenericConverter;
import org.iucn.sis.shared.conversions.VFSInfo;
import org.iucn.sis.shared.conversions.AssessmentConverter.ConversionMode;
import org.iucn.sis.shared.helpers.AssessmentData;
import org.iucn.sis.shared.helpers.AssessmentParser;
import org.iucn.sis.shared.helpers.CanonicalNames;

import com.solertium.db.CString;
import com.solertium.db.CanonicalColumnName;
import com.solertium.db.DBException;
import com.solertium.db.DBSession;
import com.solertium.db.ExecutionContext;
import com.solertium.db.Row;
import com.solertium.db.SystemExecutionContext;
import com.solertium.db.query.QConstraint;
import com.solertium.db.query.QRelationConstraint;
import com.solertium.db.query.SelectQuery;
import com.solertium.lwxml.factory.NativeDocumentFactory;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.util.TrivialExceptionHandler;
import com.solertium.vfs.VFS;

public abstract class FieldMigrationConverter extends GenericConverter<VFSInfo> {
	
	protected final String[] FIELDS;
	
	protected ExecutionContext SIS1;
	protected ExecutionContext SIS2;
	
	protected Map<String, Row.Set> lookups;
	
	protected ConversionMode mode = ConversionMode.ALL;
	protected ExecutionContext ec;
	
	public FieldMigrationConverter(String dbSessionName, String sis1DBS, String... fields) throws NamingException {
		super();
		setClearSessionAfterTransaction(true);
		
		FIELDS = fields;
		
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
	
	protected abstract Row getPrototype();
	
	protected abstract void correct(String fieldName) throws DBException;
	
	protected abstract void process(String fieldName, AssessmentData assessData, Object rawData) throws DBException;
	
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
			for (String fieldName : FIELDS)
				correct(fieldName);
		}
		else if ("create".equals(runMode)) {
			rebuildTables();
			
			if (ConversionMode.DRAFT.equals(mode))
				convertAllDrafts(data.getOldVFS(), data.getNewVFS());
			else if (ConversionMode.PUBLISHED.equals(mode))
				convertAllPublished(data.getOldVFS(), data.getNewVFS());
			else {
				convertAllDrafts(data.getOldVFS(), data.getNewVFS());
				convertAllPublished(data.getOldVFS(), data.getNewVFS());
			}
		}
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
	
	protected Assessment getAssessment(String internal_id) {
		AssessmentCriteria criteria = new AssessmentCriteria(session);
		criteria.internalId.eq(internal_id);
		
		try {
			return criteria.uniqueAssessment();
		} catch (Exception e) {
			return null;
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
				Object rawData = assessData.getDataMap().get(name);
				if (rawData != null) {
					process(name, assessData, rawData);
				}
			}
			
		} catch (Throwable e) {
			print("Failed on file " + file.getPath());
			e.printStackTrace();
			throw new Exception(e);
		}
	}
	
	protected boolean isBird(String id) {
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
	
	protected String getTableName(String fieldName) {
		return "Mig_" + fieldName;
	}
	
	protected Integer toInt(String value) {
		if (value == null || "".equals(value) || "0".equals(value))
			return null;
		
		return Integer.valueOf(value);
	}
	
	protected String toStr(String value) {
		if (value == null || "".equals(value))
			return null;
		
		return value;
	}
	
	protected Boolean toBool(String curPrimitive) {
		Boolean value;
		if (curPrimitive == null || "".equals(curPrimitive) || "false".equals(curPrimitive.toLowerCase()))
			value = null;
		else
			value = Boolean.valueOf(curPrimitive);
		
		return value;
	}
	
	protected Date toDate(String curPrimitive) {
		if (curPrimitive == null || "".equals(curPrimitive))
			return null;
		
		String format = "yyyy-MM-dd";
		String formatWithTime = "yyyy-MM-dd HH:mm:ss";
		String formatSlashes = "yyyy/MM/dd";
		String formatYear = "yyyy";
		SimpleDateFormat formatter = new SimpleDateFormat(format);
		SimpleDateFormat formatterWithTime = new SimpleDateFormat(formatWithTime);
		SimpleDateFormat formatterWithSlashes = new SimpleDateFormat(formatSlashes);
		SimpleDateFormat formatterYear = new SimpleDateFormat(formatYear);
		
		Date date = null;
		try {
			date = formatter.parse(curPrimitive);
		} catch (ParseException e) {
			try {
				date = formatterWithTime.parse(curPrimitive);
			} catch (ParseException e1) {
				try {
					date = formatterWithSlashes.parse(curPrimitive);
				} catch (ParseException e2) {
					try {
						//Strip out non-year characters, first.
						if( curPrimitive.replaceAll("\\D", "").matches("\\d{4}"))
							curPrimitive = curPrimitive.replaceAll("\\D", "");
						
						date = formatterYear.parse(curPrimitive);
					} catch (ParseException e3) {
						printf("Unable to parse date: %s", curPrimitive);
						
						date = null;
					}
				}
			}
		}
		return date;
	}
	
	protected CString getTextColumn(String name) {
		CString col = new CString(name, null);
		col.setScale(4000); //>255
		return col;
	}
	
	protected final Integer getIndex(String canonicalName, String libraryTable, String name, String value) throws DBException {
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
	
	protected boolean canOverride() {
		return "true".equals(parameters.getFirstValue("override"));
	}
	
	protected void constrainQueryToWorkingSet(SelectQuery query, String name) {
		query.join("assessment", new QRelationConstraint(
			new CanonicalColumnName("assessment", "internal_id"),
			new CanonicalColumnName(getTableName(name), "internal_id") 
		));
		query.join("working_set_taxon", new QRelationConstraint(
			new CanonicalColumnName("working_set_taxon", "taxonid"),
			new CanonicalColumnName("assessment", "taxonid")
		));
		query.constrain(QConstraint.CG_AND, new CanonicalColumnName("assessment", "assessment_typeid"), 
			QConstraint.CT_EQUALS, 2);
		query.constrain(new CanonicalColumnName("working_set_taxon", "working_setid"), 
			QConstraint.CT_EQUALS, Integer.valueOf(parameters.getFirstValue("working_set")));
	}
	
	protected SelectQuery newSelectQuery() {
		return new LinkedSelectQuery();
	}
	
	private static class LinkedSelectQuery extends SelectQuery {
		
		public LinkedSelectQuery() {
			super();
			explicitJoins = new LinkedHashMap<String, QConstraint>();
		}
		
	}
	
}
