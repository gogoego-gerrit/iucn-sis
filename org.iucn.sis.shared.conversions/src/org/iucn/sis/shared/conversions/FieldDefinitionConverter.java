package org.iucn.sis.shared.conversions;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.gogoego.api.utils.DocumentUtils;
import org.iucn.sis.client.displays.ClassificationScheme;
import org.iucn.sis.client.displays.Display;
import org.iucn.sis.client.displays.Field;
import org.iucn.sis.shared.DisplayData;
import org.iucn.sis.shared.DisplayDataProcessor;
import org.iucn.sis.shared.TreeData;
import org.iucn.sis.shared.TreeDataRow;
import org.iucn.sis.shared.data.assessments.CanonicalNames;
import org.iucn.sis.shared.data.assessments.FieldParser;
import org.iucn.sis.shared.structures.SISCategoryAndCriteria;
import org.iucn.sis.shared.structures.SISClassificationSchemeStructure;
import org.iucn.sis.shared.structures.SISLivelihoods;
import org.iucn.sis.shared.structures.SISMultiSelect;
import org.iucn.sis.shared.structures.SISOneToMany;
import org.iucn.sis.shared.structures.SISRelatedStructures;
import org.iucn.sis.shared.structures.SISSelect;
import org.iucn.sis.shared.structures.SISStructureCollection;
import org.iucn.sis.shared.structures.SISThreatStructure;
import org.iucn.sis.shared.structures.Structure;
import org.iucn.sis.shared.structures.UseTrade;

import com.solertium.db.DBException;
import com.solertium.lwxml.factory.NativeDocumentFactory;
import com.solertium.lwxml.java.JavaNativeDocumentFactory;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.vfs.VFS;
import com.solertium.vfs.VFSFactory;

public class FieldDefinitionConverter {

	private static FieldParser fieldParser;
	private static HashMap<String, Display> lookupTables = new HashMap<String, Display>();
	private static VFS vfs;
	private static Map<String, String> typeLookup = new HashMap<String, String>();
	private static Map<String, String> quantityLookup = new HashMap<String, String>();
	
	private static StringBuilder everythingElse = new StringBuilder();
	private static StringBuilder creates = new StringBuilder();
	private static StringBuilder inserts = new StringBuilder();
	
	private static String standardInsertTypes = "(name, data_type, number_allowed)";
	
	public static void main(String args[]) {
		NativeDocumentFactory.setDefaultInstance(new JavaNativeDocumentFactory(null));
	
		typeLookup.put("range", "range_primitive_field");
		typeLookup.put("boolean", "boolean_primitive_field");
		typeLookup.put("booleanRange", "boolean_range_primitive_field");
		typeLookup.put("booleanUnknown", "boolean_unknown_primitive_field");
		typeLookup.put("qualifier", "fk_primitive_field");
		typeLookup.put("singleSelect", "fk_primitive_field");
		typeLookup.put("multipleSelect", "fk_list_primitive_field");
		typeLookup.put("date", "date_primitive_field");
		typeLookup.put("number", "float_primitive_field");
		typeLookup.put("optionsList", "fk_list_primitive_field");
		typeLookup.put("richText", "text_primitive_field");
		typeLookup.put("narrative", "text_primitive_field");
		
		quantityLookup.put("optionsList", "?");
		quantityLookup.put("multipleSelect", "?");
		quantityLookup.put("livelihoods", "*");
		quantityLookup.put("treeStructure", "*");
		quantityLookup.put("useTrade", "*");
		
		VFS ivfs = null;
		if( ivfs == null ) {
			try {
				ivfs = VFSFactory.getVFS(new File("/var/sisLocal/test/sis/vfs"));
			} catch (Exception nf) {
				nf.printStackTrace();
				throw new RuntimeException("The provided HDFS URI is invalid.");
			} 
		}
		
		migrateFieldDefinitionsToDB(ivfs);
		
//		System.out.println(everythingElse.toString()); 
		try {
			FileWriter writer = new FileWriter(new File("allSQL.sql"));
			writer.write(everythingElse.toString());
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			FileWriter writer = new FileWriter(new File("creates.sql"));
			writer.write(creates.toString());
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			FileWriter writer = new FileWriter(new File("inserts.sql"));
			writer.write(inserts.toString());
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void logCommand(String command) {
		if( command.startsWith("CREATE TABLE") )
			creates.append(command);
		else
			inserts.append(command);
		
		everythingElse.append(command);
	}
	
	/**
	 * Reads in the Field Definition XML documents and converts to a new format
	 * that works with the new data model.
	 */
	public static void migrateFieldDefinitionsToDB(VFS fs) {
		vfs = fs;
		fieldParser = new FieldParser();
		lookupTables = new HashMap<String, Display>();

		for( String curField : CanonicalNames.allCanonicalNames ) {
			try {
				createLookupTable(curField);
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("Couldn't build field " + curField);
			}
		}
	}
	
	private static String getFieldURL(String fieldName) {
		return "/browse/docs/fields/" + fieldName + ".xml";
	}
	
	private static void createLookupTable(String fieldName) throws Exception {
		if (lookupTables.containsKey(fieldName))
			return;

		String url = getFieldURL(fieldName);

		if (!vfs.exists(url)) {
//			System.out.println("Could not find field definition for " + url);
			return;
		}

		NativeDocument fdoc = NativeDocumentFactory.newNativeDocument();
		fdoc.parse(DocumentUtils.getVFSFileAsString(url, vfs));

		Display f = fieldParser.parseField(fdoc);
		DisplayData fd = fieldParser.parseFieldData(fdoc);
		
		parseLookupFromDisplay(f.getCanonicalName(), f);
		
		logCommand("CREATE TABLE " + fd.getCanonicalName() + " (id integer auto_increment primary key, " +
				"name varchar(255), data_type varchar(255), number_allowed varchar(255));\n");
		
		String canonicalName = fd.getCanonicalName();
		if( canonicalName.equals(CanonicalNames.RedListSource) )
			System.out.println("At least found rlSoruce...");
		
		if( fd instanceof TreeData ) {
			canonicalName = canonicalName + "Subfield";
			logCommand("INSERT INTO " + fd.getCanonicalName() + " " + standardInsertTypes + " VALUES ('" + 
					canonicalName + "', 'field', '*');\n");
			logCommand("CREATE TABLE " + canonicalName + " (id integer auto_increment primary key, " +
				"name varchar(255), data_type varchar(255), number_allowed varchar(255));\n");
			logCommand("INSERT INTO " + canonicalName + " " + standardInsertTypes + " VALUES ('" + 
					fd.getCanonicalName() + "Lookup', 'fk_primitive_field', '1');\n");
		}
		
		for( Map<String, String> defTotype : getPrimitiveTypes(fd) ) {
			String type = null;
			String definition = null;
			for( Entry<String, String> cur : defTotype.entrySet() ) {
				type = cur.getKey();
				definition = cur.getValue();
			}
			
			String numAllowed = quantityLookup.get(type);
			if( numAllowed == null )
				numAllowed = "?";
			
			if( type != null && !type.equalsIgnoreCase("relatedStructure") && !type.equalsIgnoreCase("related") ) {
				if( type.equals("threat") ) {
					insert(canonicalName, "timing", "fk_primitive_field", numAllowed);
					insert(canonicalName, "scope", "fk_primitive_field", numAllowed);
					insert(canonicalName, "severity", "fk_primitive_field", numAllowed);
					insert(canonicalName, "score", "string_primitive_field", numAllowed);
				} else if( type.equals("regionalInformation") ) {
					insert(canonicalName, "regions", "fk_list_primitive_field", "?");
					insert(canonicalName, "endemic", "boolean_primitive_field", numAllowed);
				} else if( type.equals("regionalQuestions") ) {
					insert(canonicalName, "answers", "text_primitive_field", numAllowed);
				} else if( type.equals("categoryAndCriteria") ) {
					logCommand("INSERT INTO " + canonicalName + " " + standardInsertTypes + " VALUES (" + 
							"'isManual', 'boolean_primitive_field', '" + numAllowed + "');\n");
					logCommand("INSERT INTO " + canonicalName + " " + standardInsertTypes + " VALUES (" + 
							"'critVersion', 'fk_primitive_field', '" + numAllowed + "');\n");
					logCommand("INSERT INTO " + canonicalName + " " + standardInsertTypes + " VALUES (" + 
							"'manualCategory', 'string_primitive_field', '" + numAllowed + "');\n");
					logCommand("INSERT INTO " + canonicalName + " " + standardInsertTypes + " VALUES (" + 
							"'manualCriteria', 'string_primitive_field', '" + numAllowed + "');\n");
					logCommand("INSERT INTO " + canonicalName + " " + standardInsertTypes + " VALUES (" + 
							"'autoCategory', 'string_primitive_field', '" + numAllowed + "');\n");
					logCommand("INSERT INTO " + canonicalName + " " + standardInsertTypes + " VALUES (" + 
							"'autoCriteria', 'string_primitive_field', '" + numAllowed + "');\n");
					logCommand("INSERT INTO " + canonicalName + " " + standardInsertTypes + " VALUES (" + 
							"'rlHistoryText', 'string_primitive_field', '" + numAllowed + "');\n");
					logCommand("INSERT INTO " + canonicalName + " " + standardInsertTypes + " VALUES (" + 
							"'possiblyExtinct', 'boolean_primitive_field', '" + numAllowed + "');\n");
					logCommand("INSERT INTO " + canonicalName + " " + standardInsertTypes + " VALUES (" + 
							"'possiblyExtinctCandidate', 'boolean_primitive_field', '" + numAllowed + "');\n");
					logCommand("INSERT INTO " + canonicalName + " " + standardInsertTypes + " VALUES (" + 
							"'dateLastSeen', 'date_primitive_field', '" + numAllowed + "');\n");
					logCommand("INSERT INTO " + canonicalName + " " + standardInsertTypes + " VALUES (" + 
							"'categoryText', 'string_primitive_field', '" + numAllowed + "');\n");
					logCommand("INSERT INTO " + canonicalName + " " + standardInsertTypes + " VALUES (" + 
							"'dataDeficientReason', 'string_primitive_field', '" + numAllowed + "');\n");
					
				} else if( type.equals("useTrade") ) {
					//type is field, name is UseTradeSubfield
					//create UseTradeSubfield with rows ...
					logCommand("INSERT INTO " + canonicalName + " " + standardInsertTypes + " VALUES (" + 
							"'UseTradeSubfield', 'field', '" + numAllowed + "');\n");
					logCommand("CREATE TABLE UseTradeSubfield (id integer auto_increment primary key, " +
							"name varchar(255), data_type varchar(255), number_allowed varchar(255));\n");
					logCommand("INSERT INTO UseTradeSubfield " + standardInsertTypes + " VALUES (" + 
						"'purpose', 'fk_primitive_field', '?');\n");
					logCommand("INSERT INTO UseTradeSubfield " + standardInsertTypes + " VALUES (" + 
						"'source', 'fk_primitive_field', '?');\n");
					logCommand("INSERT INTO UseTradeSubfield " + standardInsertTypes + " VALUES (" + 
						"'formRemoved', 'fk_primitive_field', '?');\n");
					logCommand("INSERT INTO UseTradeSubfield " + standardInsertTypes + " VALUES (" + 
						"'subsistence', 'boolean_primitive_field', '?');\n");
					logCommand("INSERT INTO UseTradeSubfield " + standardInsertTypes + " VALUES (" + 
						"'national', 'boolean_primitive_field', '?');\n");
					logCommand("INSERT INTO UseTradeSubfield " + standardInsertTypes + " VALUES (" + 
						"'international', 'boolean_primitive_field', '?');\n");
					logCommand("INSERT INTO UseTradeSubfield " + standardInsertTypes + " VALUES (" + 
						"'harvestLevel', 'text_primitive_field', '?');\n");
					logCommand("INSERT INTO UseTradeSubfield " + standardInsertTypes + " VALUES (" + 
						"'units', 'fk_primitive_field', '?');\n");
					logCommand("INSERT INTO UseTradeSubfield " + standardInsertTypes + " VALUES (" + 
						"'possibleThreat', 'boolean_primitive_field', '?');\n");
					logCommand("INSERT INTO UseTradeSubfield " + standardInsertTypes + " VALUES (" + 
						"'justification', 'text_primitive_field', '?');\n");
				} else if( type.equals("livelihoods") ) {
					//type is field, name is LivelihoodsSubfield
					//created LivelihoodsSubfield with rows ...
					logCommand("INSERT INTO " + canonicalName + " " + standardInsertTypes + " VALUES ('" + 
							"LivelihoodsSubfield', 'field', '" + numAllowed + "');\n");
					logCommand("CREATE TABLE LivelihoodsSubfield (id integer auto_increment primary key, " +
						"name varchar(255), data_type varchar(255), number_allowed varchar(255));\n");
					logCommand("INSERT INTO LivelihoodsSubfield " + standardInsertTypes + " VALUES (" + 
						"'scale', 'fk_primitive_field', '?');\n");
					logCommand("INSERT INTO LivelihoodsSubfield " + standardInsertTypes + " VALUES (" + 
						"'nameOfLocality', 'text_primitive_field', '?');\n");
					logCommand("INSERT INTO LivelihoodsSubfield " + standardInsertTypes + " VALUES (" + 
						"'date', 'date_primitive_field', '?');\n");
					logCommand("INSERT INTO LivelihoodsSubfield " + standardInsertTypes + " VALUES (" + 
						"'productDescription', 'text_primitive_field', '?');\n");
					logCommand("INSERT INTO LivelihoodsSubfield " + standardInsertTypes + " VALUES (" + 
						"'annualHarvest', 'text_primitive_field', '?');\n");
					logCommand("INSERT INTO LivelihoodsSubfield " + standardInsertTypes + " VALUES (" + 
						"'unitsAnnualHarvest', 'fk_primitive_field', '?');\n");
					logCommand("INSERT INTO LivelihoodsSubfield " + standardInsertTypes + " VALUES (" + 
						"'annualMultiSpeciesHarvest', 'text_primitive_field', '?');\n");
					logCommand("INSERT INTO LivelihoodsSubfield " + standardInsertTypes + " VALUES (" + 
						"'unitsAnnualMultiSpeciesHarvest', 'fk_primitive_field', '?');\n");
					logCommand("INSERT INTO LivelihoodsSubfield " + standardInsertTypes + " VALUES (" + 
						"'percentInHarvest', 'float_primitive_field', '?');\n");
					logCommand("INSERT INTO LivelihoodsSubfield " + standardInsertTypes + " VALUES (" + 
						"'amountInHarvest', 'text_primitive_field', '?');\n");
					logCommand("INSERT INTO LivelihoodsSubfield " + standardInsertTypes + " VALUES (" + 
						"'humanReliance', 'fk_primitive_field', '?');\n");
					logCommand("INSERT INTO LivelihoodsSubfield " + standardInsertTypes + " VALUES (" + 
						"'genderAge', 'fk_primitive_field', '?');\n");
					logCommand("INSERT INTO LivelihoodsSubfield " + standardInsertTypes + " VALUES (" + 
						"'socioEconomic', 'fk_primitive_field', '?');\n");
					logCommand("INSERT INTO LivelihoodsSubfield " + standardInsertTypes + " VALUES (" + 
						"'other', 'text_primitive_field', '?');\n");
					logCommand("INSERT INTO LivelihoodsSubfield " + standardInsertTypes + " VALUES (" + 
						"'totalPopBenefit', 'fk_primitive_field', '?');\n");
					logCommand("INSERT INTO LivelihoodsSubfield " + standardInsertTypes + " VALUES (" + 
						"'householdConsumption', 'fk_primitive_field', '?');\n");
					logCommand("INSERT INTO LivelihoodsSubfield " + standardInsertTypes + " VALUES (" + 
						"'householdIncome', 'fk_primitive_field', '?');\n");
					logCommand("INSERT INTO LivelihoodsSubfield " + standardInsertTypes + " VALUES (" + 
						"'annualCashIncome', 'float_primitive_field', '?');\n");
					
				} else if( type.equals("treeStructure") ) {
					//type is field, name is Stresses
					//also add Table for Stresses, containing a row for StressesSubfield
					//create StressesSubfield with row
					//  0 stress primitive_type_fk *
					insert(canonicalName, "StressesSubfield", "field", numAllowed);
					logCommand("CREATE TABLE StressesSubfield (id integer auto_increment primary key, " +
							"name varchar(255), data_type varchar(255), number_allowed varchar(255));\n");
					insert("StressesSubfield", "stress", "fk_primitive_field", numAllowed);
				} else if( type.equals("oneToMany") ) {
					//...just elide this
				} else if( type.equals("empty") ) {
					//...just elide this
				} else {
					String dbType = typeLookup.get(type);
					if( dbType == null )
						dbType = "string_primitive_field";
					
					if( type != null && type.equalsIgnoreCase("optionsList") ) {
						insert(canonicalName, "text", "string_primitive_field", "?");
					}
					
					if( type != null && type.equalsIgnoreCase("qualifier") ) {
						buildIndexedLookup(canonicalName + "_" + definition + "Lookup", "index",
								"description", new String[] { "Observed", "Projected", "Inferred" }, 1);
					}
					
					insert(canonicalName, definition, dbType, numAllowed);
				}
			}
		}
//		logCommand("#----------------------------------------\n");
		
		//Arrays.toString(getPrimitiveTypes(fd).toArray())
//		parseLookupFromDisplay(fieldName, f);
		lookupTables.put(fieldName, f);
	}
	
	private static List<Map<String, String>> getPrimitiveTypes(DisplayData fd) {
		List<Map<String, String>> guts = new ArrayList<Map<String,String>>();
		if( fd.getData() instanceof List ) {
			List list = (List)fd.getData();
			for( Object data : list ) {
				if( data instanceof DisplayData )
					guts.addAll(getPrimitiveTypes((DisplayData)data));
				else if( data instanceof List ) {
					for( Object data2 : (List)data )
						if( data2 instanceof DisplayData )
							guts.addAll(getPrimitiveTypes((DisplayData)data2));
				}
			}
		} else if( fd.getData() instanceof DisplayData ) {
			guts.addAll(0, getPrimitiveTypes((DisplayData)fd.getData()));
		} else if( fd instanceof TreeData && ((TreeData)fd).getDefaultStructure() != null ) {
			guts.addAll(0, getPrimitiveTypes(((TreeData)fd).getDefaultStructure()));
		}
		
//		guts.add(0, fd.getDataTypes());
		return guts;
	}
	
	private static void insert(String canonicalName, String name, String type, String numAllowed) {
		logCommand("INSERT INTO " + canonicalName + " " + standardInsertTypes + " VALUES ('" + 
				name + "', '" + type + "', '" + numAllowed + "');\n");
	}
	
	private static void buildIndexedLookup(String tableName, String indexName, String descName, Object[] options,
			int indexOffset) throws DBException {
		
		logCommand("CREATE TABLE " + tableName + "(id integer auto_increment primary key, " +
							"name varchar(255), label varchar(255));\n");
	
		for (int i = 0; i < options.length; i++) {
			logCommand("INSERT INTO " + tableName + " (name, label) VALUES ('" + 
					i + "', '" + options[i] + "');\n");
		}
	}
	
	
	private static void parseLookupFromDisplay(String fieldName, Display f) {
		try {
			if (f instanceof ClassificationScheme) {
//				Row r = new Row();
//				r.get("id").setKey(true);
//				r.add(new CInteger("id", 0));
//				r.add(new CString("orderCode", "sampleID"));
//				r.add(new CString("parentID", "sampleID"));
//				r.add(new CInteger("level", 10));
//				r.add(new CBoolean("codeable", false));
//				r.add(new CString("ref", "1.1.1.1"));
//				r.add(new CString("description", "sample description"));
//
//				ec.createTable("_scheme_lookup_" + fieldName, r);
//
//				// Generate classification scheme lookup table
				ClassificationScheme scheme = (ClassificationScheme) f;
//				r = new Row();
//				r.add(new CString("id", curCode));
//				r.add(new CString("parentID", parentID));
//				r.add(new CInteger("level", level));
//				r.add(new CBoolean("codeable", Boolean.parseBoolean(curRow.getCodeable())));
//				r.add(new CString("ref", curLevelID));
//				r.add(new CString("description", curDesc));
				logCommand("CREATE TABLE " + (scheme.getCanonicalName().equals("") ? "Stresses" : scheme.getCanonicalName()) + "Lookup (id integer auto_increment primary key, " +
					"code varchar(63), parentID varchar(63), level integer, codeable tinyint, ref varchar(63), " +
					"description varchar(255));\n");
				for (Object obj : scheme.getTreeData().getTreeRoots())
					insertClassSchemeLookupRow(fieldName, "(root)", scheme, (TreeDataRow) obj, 0);
				// Generate classification scheme's default structure lookup
				// table
				Structure defStructure = scheme.generateDefaultStructure();
				buildStructureLookup(fieldName, defStructure);
			} else {
				// Generate a field's lookup table
				Field field = (Field) f;
				for (Object obj : field.getStructures()) {
					Structure struct = (Structure) obj;
					buildStructureLookup(fieldName, struct);
				}
			}
		} catch (DBException e) {
			//Table probably already existed.
		}
	}
	
//	private static void buildIndexedLookup(String tableName, String indexName, String descName, Object[] options,
//			int indexOffset) throws DBException {
//		Row prototype = new Row();
//		prototype.add(new CInteger(indexName, Integer.valueOf(0)));
//		prototype.add(new CString(descName, "sample description"));
//		ec.createTable(tableName, prototype);
//
//		for (int i = 0; i < options.length; i++) {
//			Row r = new Row();
//			r.add(new CInteger(indexName, Integer.valueOf(i + indexOffset)));
//			r.add(new CString(descName, options[i].toString()));
//
//			InsertQuery iq = new InsertQuery(tableName, r);
//			ec.doUpdate(iq);
//		}
//	}

	private static void buildStructureLookup(String fieldName, Structure struct) throws DBException {
		if (struct instanceof SISClassificationSchemeStructure) {
			SISClassificationSchemeStructure s = (SISClassificationSchemeStructure) struct;
			parseLookupFromDisplay("Stresses", s.getScheme());
		} else if (struct instanceof SISOneToMany ) {
			SISOneToMany s = (SISOneToMany) struct;
			Structure newOne = DisplayDataProcessor.processDisplayStructure(s.getDefaultStructureData());
			buildStructureLookup(fieldName, newOne);
		} else if (struct instanceof SISStructureCollection) {
			SISStructureCollection s = (SISStructureCollection) struct;

			for (int i = 0; i < s.getStructures().size(); i++) {
				Structure cur = (Structure) s.getStructures().get(i);

				if (cur.getDescription() != null && !cur.getDescription().equals("")) {
					try {
						buildStructureLookup(fieldName, cur);
					} catch (DBException duplicateDescriptionProbably) {
						buildStructureLookup(fieldName, cur);
					}
				} else
					buildStructureLookup(fieldName, cur);
			}
		} else if (struct instanceof SISRelatedStructures) {
			SISRelatedStructures s = (SISRelatedStructures) struct;

			buildStructureLookup(fieldName, s.getDominantStructure());

			for (int i = 0; i < s.getDependantStructures().size(); i++) {
				Structure cur = (Structure) s.getDependantStructures().get(i);

				if (cur.getDescription() != null && !cur.getDescription().equals("")) {
					String cleanDesc = cur.getDescription().replaceAll("\\s", "").replaceAll("\\W", "");
					try {
						if ("_lookup_".length() + fieldName.length() + cleanDesc.length() < 64)
							buildStructureLookup(fieldName, cur);
						else
							buildStructureLookup(fieldName, cur);
					} catch (DBException duplicateDescriptionProbably) {
						buildStructureLookup(fieldName, cur);
					}
				} else
					buildStructureLookup(fieldName, cur);
			}
		} else if (struct instanceof SISMultiSelect || struct instanceof SISSelect) {
			String tableName = fieldName + "_" + struct.getId() + "Lookup";
			String indexName = "index";
			String descName = "description";

			ArrayList options = (ArrayList) struct.getConstructionData();

			if( options.get(0) instanceof ArrayList )
				buildIndexedLookup(tableName, indexName, descName, ((ArrayList)options.get(0)).toArray(), 1);
			else
				buildIndexedLookup(tableName, indexName, descName, options.toArray(), 1);
		} else if (struct instanceof SISThreatStructure) {
			String[] timing = new String[] { "Past, Unlikely to Return", "Ongoing", "Future", "Unknown",
			"Past, Likely to Return" };
			String[] scope = new String[] { "Whole (>90%)", "Majority (50-90%)", "Minority (<50%)", "Unknown" };
			String[] severity = new String[] { "Very Rapid Declines", "Rapid Declines",
					"Slow, Significant Declines", "Causing/Could cause fluctuations", "Negligible declines",
					"No decline", "Unknown" };

			buildIndexedLookup("Threats_timingLookup", "index", "description", timing, 1);
			buildIndexedLookup("Threats_scopeLookup", "index", "description", scope, 1);
			buildIndexedLookup("Threats_severityLookup", "index", "description", severity, 1);
		} else if (struct instanceof UseTrade) {
			UseTrade useTrade = (UseTrade) struct;

			buildIndexedLookup("UseTradeDetails_purposeLookup", "index", "description", useTrade.getPurposeOptions(), 1);
			buildIndexedLookup("UseTradeDetails_sourceLookup", "index", "description", useTrade.getSourceOptions(), 1);
			buildIndexedLookup("UseTradeDetails_formRemovedLookup", "index", "description", useTrade.getFormRemovedOptions(), 1);
			buildIndexedLookup("UseTradeDetails_unitsLookup", "index", "description", useTrade.getUnitsOptions(), 0);
		} else if (struct instanceof SISLivelihoods) {
			SISLivelihoods l = (SISLivelihoods) struct;

			buildIndexedLookup("Livelihoods_scaleLookup", "index", "description", l.getScaleOptions(), 1);
			buildIndexedLookup("Livelihoods_unitsLookup", "index", "description", l.getUnitsOptions(), 1);
			buildIndexedLookup("Livelihoods_humanRelianceLookup", "index", "description", l.getHumanRelianceOptions(), 1);
			buildIndexedLookup("Livelihoods_genderAgeLookup", "index", "description", l.getByGenderAgeOptions(), 1);
			buildIndexedLookup("Livelihoods_socioEconLookup", "index", "description", l.getBySocioEconOptions(), 1);
			buildIndexedLookup("Livelihoods_percentPopBenefitLookup", "index", "description", l.getPercentPopulationBenefitingOptions(), 1);
			buildIndexedLookup("Livelihoods_percentConsumeLookup", "index", "description", l.getPercentConsumptionOptions(), 1);
			buildIndexedLookup("Livelihoods_percentIncomeLookup", "index", "description", l.getPercentIncomeOptions(), 1);
		} else if (struct instanceof SISCategoryAndCriteria) {
			buildIndexedLookup("RedListCriteria_critVersionLookup", "index", "crit_version", new String[] { "3.1", "2.3",
			"Earlier Version" }, 0);
		}
	}
	
	private static void insertClassSchemeLookupRow(String fieldName, String parentID, ClassificationScheme scheme,
			TreeDataRow curRow, int level) throws DBException {
		String curCode = (String) curRow.getDisplayId();
		String curDesc = (String) scheme.getCodeToDesc().get(curCode);
		String curLevelID = (String) scheme.getCodeToLevelID().get(curCode);

		// If it's not a region in CountryOccurrence, do your stuff
		if (!(fieldName.equals(CanonicalNames.CountryOccurrence) && curCode.length() > 10)) {
//			r = new Row();
//			r.add(new CString("id", curCode));
//			r.add(new CString("parentID", parentID));
//			r.add(new CInteger("level", level));
//			r.add(new CBoolean("codeable", Boolean.parseBoolean(curRow.getCodeable())));
//			r.add(new CString("ref", curLevelID));
//			r.add(new CString("description", curDesc));

			logCommand("INSERT INTO " + fieldName + "Lookup (code, parentID, level, codeable, " +
					"ref, description) VALUES('" + curCode +
					"', '" + parentID + "', " + level + ", " + Boolean.parseBoolean(curRow.getCodeable()) +
					", '" + curLevelID + "', '" + curDesc.replaceAll("'", "''") + "');\n");

			for (Object obj : curRow.getChildren())
				insertClassSchemeLookupRow(fieldName, curCode, scheme, (TreeDataRow) obj, level + 1);
		} else
			for (Object obj : curRow.getChildren())
				insertClassSchemeLookupRow(fieldName, "(root)", scheme, (TreeDataRow) obj, level);
	}
}
