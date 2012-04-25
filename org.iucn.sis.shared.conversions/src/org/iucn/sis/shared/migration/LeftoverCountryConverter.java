package org.iucn.sis.shared.migration;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.naming.NamingException;

import org.iucn.sis.server.api.persistance.AssessmentCriteria;
import org.iucn.sis.server.api.persistance.FieldDAO;
import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.Field;
import org.iucn.sis.shared.api.models.Reference;
import org.iucn.sis.shared.api.models.fields.ProxyField;
import org.iucn.sis.shared.conversions.Converter;
import org.iucn.sis.shared.helpers.CanonicalNames;

import com.solertium.db.DBException;
import com.solertium.db.DBSession;
import com.solertium.db.ExecutionContext;
import com.solertium.db.Row;
import com.solertium.db.SystemExecutionContext;
import com.solertium.db.query.SelectQuery;
import com.solertium.util.CSVTokenizer;

public class LeftoverCountryConverter extends Converter {
	
	private static final int TAXON = 0;
	private static final int ASM = 1;
	private static final int TRANSLATION = 3;
	
	public static void main(String[] args) throws IOException {
		final Map<String, String> translations = new HashMap<String, String>();
		translations.put("CS", "OLD-01");
		translations.put("TVL-OO", "OLD-02");
		translations.put("REU-OO", "OLD-03");
		translations.put("FRA-CI", "OLD-04");
		translations.put("CPP-OO", "CPP-EC,TVL-NW,CPP-NC,CPP-WC");
		translations.put("YUG-MN", "ME");
		
		final BufferedReader reader = new BufferedReader(new InputStreamReader(
			LeftoverCountryConverter.class.getResourceAsStream("Countries869.txt")
		));
		
		final File file = new File("/Users/carlscott/Projects/iucn-sis/org.iucn.sis.shared.conversions/src/org/iucn/sis/shared/migration/Countries869.csv");
		final BufferedWriter writer = new BufferedWriter(new PrintWriter(
			new FileWriter(file)
		));
		
		writer.write("taxonid,assessmentid,countryid,translations\n");
		
		List<String> lines = new ArrayList<String>();
		
		String line = null;
		while ((line = reader.readLine()) != null) {
			String numbers = line.substring(0, line.indexOf('.'));
			String[] split = numbers.split("/");
			String taxonID = split[0];
			String asmID = split[split.length-1];
			String countryID;
			
			String countryArea = line.substring(line.indexOf("CountryOccurrenceLookup to match"));
			StringBuilder builder = new StringBuilder();
			boolean write = false;
			for (char c : countryArea.toCharArray()) {
				if (c == '<')
					break;
				
				if (!write && c == ':')
					write = true;
				else if (write)
					builder.append(c);
			}
			
			countryID = builder.toString().trim();
			String translationIDs = translations.get(countryID);
			
			if (translationIDs == null)
				throw new RuntimeException("No translations found for " + countryID);
			
			lines.add(taxonID + "," + asmID + "," + countryID + ",\"" + translationIDs + "\"\n");
		}
		
		Collections.sort(lines);
		
		for (String l : lines)
			writer.write(l);
		
		reader.close();
		writer.close();
	}
	
	protected ExecutionContext SIS1;
	protected ExecutionContext SIS2;
	
	protected Map<String, Row.Set> lookups;
	
	public LeftoverCountryConverter() throws NamingException {
		this("sis_lookups", "sis1_lookups");
	}
	
	public LeftoverCountryConverter(String dbSessionName, String sis1DBS) throws NamingException {
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
	
	@Override
	protected void run() throws Exception {
		final BufferedReader reader = new BufferedReader(new InputStreamReader(
			LeftoverCountryConverter.class.getResourceAsStream("Countries869.csv")
		));
		
		String line = reader.readLine();
		
		int count = 0;
		
		printf("Starting...");
		
		while ((line = reader.readLine()) != null) {
			CSVTokenizer tokenizer = new CSVTokenizer(line);
			tokenizer.setNullOnEnd(true);
			
			Map<Integer, String> data = new HashMap<Integer, String>();
			String token = null;
			int i = 0;
			while ((token = tokenizer.nextToken()) != null)
				data.put(i++, token);
			
			process(data);
			
			if (++count % 50 == 0)
				printf("%s...", count);
		}
		
		printf("Complete.");
	}
	
	private void process(Map<Integer, String> data) throws DBException {
		Assessment assessment = getAssessment(data);
		if (assessment == null) {
			printf("Failed to find unique assessment: %s", data);
			return;
		}
		
		Field parent = assessment.getField(CanonicalNames.CountryOccurrence);
		
		List<Field> toUpdate = new ArrayList<Field>();
		for (Field field : parent.getFields()) {
			if (field.getPrimitiveField(parent.getName()+"Lookup") == null)
				toUpdate.add(field);
		}
		
		if (toUpdate.isEmpty())
			printf("No errors found for row, fixed already? %s", data);
		else {
			for (int i = 0; i < toUpdate.size(); i++) {
				Field field = toUpdate.get(i);
				
				String[] translations = data.get(TRANSLATION+i).split(",");
				if (translations.length == 1) {
					ProxyField proxy = new ProxyField(field);
					proxy.setForeignKeyPrimitiveField(parent.getName()+"Lookup", 
						getIndex(CanonicalNames.CountryOccurrence, parent.getName()+"Lookup", parent.getName()+"Lookup", translations[0]), 
						parent.getName()+"Lookup");
					
					session.update(field);
				}
				else {
					for (String translation : translations) {
						Field newField = field.deepCopy(false);
						newField.setAssessment(null);
						newField.setParent(parent);
						
						if (field.getReference() != null && !field.getReference().isEmpty())
							newField.setReference(new HashSet<Reference>(field.getReference()));
						
						ProxyField proxy = new ProxyField(field);
						proxy.setForeignKeyPrimitiveField(parent.getName()+"Lookup", 
							getIndex(CanonicalNames.CountryOccurrence, parent.getName()+"Lookup", parent.getName()+"Lookup", translation), 
							parent.getName()+"Lookup");
						
						parent.getFields().add(newField);
						
						session.save(newField);
					}
					
					try {
						FieldDAO.deleteAndDissociate(field, session);
					} catch (PersistentException e) {
						printf("Unable to delete subfield %s", field.getId());
					}
				}
			}
		}
	}
	
	private Assessment getAssessment(Map<Integer, String> data) {
		AssessmentCriteria criteria = new AssessmentCriteria(session);
		criteria.createTaxonCriteria().id.eq(Integer.valueOf(data.get(TAXON)));
		criteria.internalId.eq(data.get(ASM));
		
		try {
			return criteria.uniqueAssessment();
		} catch (Exception e) {
			return null;
		}
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

}
