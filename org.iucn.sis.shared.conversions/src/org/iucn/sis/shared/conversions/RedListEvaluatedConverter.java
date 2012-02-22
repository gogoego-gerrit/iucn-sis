package org.iucn.sis.shared.conversions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.naming.NamingException;

import org.hibernate.criterion.Restrictions;
import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.server.api.persistance.AssessmentCriteria;
import org.iucn.sis.server.api.persistance.FieldCriteria;
import org.iucn.sis.server.api.utils.FilenameStriper;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.AssessmentChange;
import org.iucn.sis.shared.api.models.Field;
import org.iucn.sis.shared.api.models.fields.ProxyField;
import org.iucn.sis.shared.conversions.AssessmentConverter.ConversionMode;
import org.iucn.sis.shared.helpers.AssessmentData;
import org.iucn.sis.shared.helpers.AssessmentParser;
import org.iucn.sis.shared.helpers.CanonicalNames;

import com.solertium.db.CBoolean;
import com.solertium.db.CDateTime;
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

public class RedListEvaluatedConverter extends GenericConverter<VFSInfo> {
		
	private static final String[] FIELDS = new String[] {
		CanonicalNames.RedListEvaluated
	};
	
	private ExecutionContext SIS1;
	private ExecutionContext SIS2;
	
	@SuppressWarnings("unused")
	private Map<String, Row.Set> lookups;
	
	private ConversionMode mode = ConversionMode.ALL;
	private ExecutionContext ec;
	
	public RedListEvaluatedConverter() throws NamingException {
		this("sis_lookups", "sis1_lookups");
	}

	public RedListEvaluatedConverter(String dbSessionName, String sis1DBS) throws NamingException {
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
					String taxonid = parameters.getFirstValue("taxon", "9");
					query.constrain(new CanonicalColumnName(getTableName(name), "taxon_id"), 
							QConstraint.CT_EQUALS, parameters.getFirstValue("taxon_id", taxonid));
				}
				
				ec.doQuery(query, new RowProcessor() {
					public void process(Row row) {
						String internal_id = row.get("internal_id").toString();
						
						update(name, internal_id, row);
						
							
						if (count.incrementAndGet() % 500 == 0)
							printf("%s...", count.get());
					}
				});
				
				printf("%s...", count.get());
			}
		}
	}
	
	private Assessment getAssessment(String internal_id) {
		AssessmentCriteria criteria = new AssessmentCriteria(session);
		criteria.internalId.eq(internal_id);
		
		try {
			return criteria.uniqueAssessment();
		} catch (Exception e) {
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	private void update(String name, String internal_id, Row row) {
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
		
		if (field != null && !"true".equals(parameters.getFirstValue("ignoreChanges", "false"))) {
			List<AssessmentChange> changes = session.createCriteria(AssessmentChange.class)
				.add(Restrictions.eq("assessment", field.getAssessment()))
				.add(Restrictions.eq("fieldName", name))
				.list();
			if (!changes.isEmpty()) {
				String username = changes.get(0).getEdit().getUser().getDisplayableName();
				printf("Not updating field for T%sA%s; has updates from %s", 
					field.getAssessment().getTaxon().getId(), field.getAssessment().getId(), username);
				return;
			}
		}
		
		if (field == null) {
			Assessment assessment = getAssessment(internal_id);
			if (assessment == null) {
				printf("No unique assessment found for %s", internal_id);
				return;
			}
			field = new Field(CanonicalNames.RedListEvaluated, assessment);
			assessment.getField().add(field);
		}
		
		ProxyField proxy = new ProxyField(field);
		proxy.setBooleanPrimitiveField("isEvaluated", "Y".equals(row.get("isEvaluated").getString()), Boolean.FALSE);
		proxy.setDatePrimitiveField("date", row.get("date").getDate());
		proxy.setForeignKeyPrimitiveField("status", row.get("status").getInteger(), name + "_statusLookup");
		proxy.setTextPrimitiveField("reasons", row.get("reasons").getString());
		proxy.setTextPrimitiveField("improvementsNeeded", row.get("improvementsNeeded").getString());
		
		if (field.hasData()) {
			if (field.getId() == 0)
				session.save(field);
			else
				session.update(field);
		}
		if ("true".equals(parameters.getFirstValue("test"))) {
			printf("Has data? %s -- %s", field.hasData(), field.toXML());
		}
		
		commitAndStartTransaction();
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
		prototype.add(new CString("type", null));
		prototype.add(getTextColumn("array"));
		prototype.add(new CBoolean("isEvaluated", false));
		prototype.add(new CDateTime("date", null));
		prototype.add(new CInteger("status", -1));
		prototype.add(getTextColumn("reasons"));
		prototype.add(getTextColumn("improvementsNeeded"));
		
		return prototype;
	}
	
	private CString getTextColumn(String name) {
		CString col = new CString(name, null);
		col.setScale(4000); //>255
		return col;
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
	
	@SuppressWarnings("unchecked")
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
				List rawData = (List<String>) assessData.getDataMap().get(name);
				if (rawData != null) {
					Row row = getPrototype();
					row.get("internal_id").setObject(assessData.getAssessmentID());
					row.get("taxon_id").setObject(assessData.getSpeciesID());
					row.get("type").setObject(assessData.isPublished() ? "published" : "draft");
					
					ArrayList<String> data = new ArrayList<String>(rawData);
					data.ensureCapacity(5);
					
					row.get("array").setObject(toStr(data.toString()));
					row.get("isEvaluated").setObject(toBool(get(data, 0)));
					row.get("date").setObject(toDate(get(data, 1)));
					row.get("status").setObject(toInt(get(data, 2)));
					row.get("reasons").setObject(toStr(get(data, 3)));
					row.get("improvementsNeeded").setObject(toStr(get(data, 4)));
					
					InsertQuery query = new InsertQuery(getTableName(name), row);
					
					print(query.getSQL(ec.getDBSession()));
					
					ec.doUpdate(query);
					
				}
			}
			
		} catch (Throwable e) {
			print("Failed on file " + file.getPath());
			e.printStackTrace();
			throw new Exception(e);
		}
	}
	
	private String get(List<String> data, int index) {
		String value;
		try {
			value = data.get(index);
		} catch (Exception e) {
			value = null;
		}
		return value;
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
	
	private Boolean toBool(String curPrimitive) {
		Boolean value;
		if (curPrimitive == null || "".equals(curPrimitive) || "false".equals(curPrimitive.toLowerCase()))
			value = null;
		else
			value = Boolean.valueOf(curPrimitive);
		
		return value;
	}
	
	private Date toDate(String curPrimitive) {
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

}
