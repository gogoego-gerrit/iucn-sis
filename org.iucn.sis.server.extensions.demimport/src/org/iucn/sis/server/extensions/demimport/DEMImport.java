package org.iucn.sis.server.extensions.demimport;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.naming.NamingException;

import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.server.api.fields.FieldSchemaGenerator;
import org.iucn.sis.server.api.io.AssessmentIO;
import org.iucn.sis.server.api.io.IsoLanguageIO;
import org.iucn.sis.server.api.io.RegionIO;
import org.iucn.sis.server.api.io.TaxonIO;
import org.iucn.sis.server.api.io.UserIO;
import org.iucn.sis.server.api.io.WorkingSetIO;
import org.iucn.sis.server.api.io.AssessmentIO.AssessmentIOWriteResult;
import org.iucn.sis.server.api.persistance.RegionCriteria;
import org.iucn.sis.server.api.utils.DocumentUtils;
import org.iucn.sis.server.api.utils.FormattedDate;
import org.iucn.sis.server.api.utils.WordUtils;
import org.iucn.sis.server.api.utils.XMLUtils;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.AssessmentType;
import org.iucn.sis.shared.api.models.CommonName;
import org.iucn.sis.shared.api.models.Edit;
import org.iucn.sis.shared.api.models.Field;
import org.iucn.sis.shared.api.models.Infratype;
import org.iucn.sis.shared.api.models.Notes;
import org.iucn.sis.shared.api.models.Region;
import org.iucn.sis.shared.api.models.Relationship;
import org.iucn.sis.shared.api.models.Synonym;
import org.iucn.sis.shared.api.models.Taxon;
import org.iucn.sis.shared.api.models.TaxonLevel;
import org.iucn.sis.shared.api.models.User;
import org.iucn.sis.shared.api.models.WorkingSet;
import org.iucn.sis.shared.api.models.fields.LivelihoodsField;
import org.iucn.sis.shared.api.models.fields.ProxyField;
import org.iucn.sis.shared.api.models.fields.RedListCreditedUserField;
import org.iucn.sis.shared.api.models.fields.RedListCriteriaField;
import org.iucn.sis.shared.api.models.fields.ThreatsSubfield;
import org.iucn.sis.shared.api.models.fields.UseTradeField;
import org.iucn.sis.shared.api.models.primitivefields.BooleanRangePrimitiveField;
import org.iucn.sis.shared.api.models.primitivefields.BooleanUnknownPrimitiveField;
import org.iucn.sis.shared.api.models.primitivefields.ForeignKeyPrimitiveField;
import org.iucn.sis.shared.api.models.primitivefields.StringPrimitiveField;
import org.iucn.sis.shared.api.utils.CanonicalNames;
import org.iucn.sis.shared.api.utils.FormattingStripper;
import org.iucn.sis.shared.helpers.TaxonNode;
import org.iucn.sis.shared.helpers.TaxonomyTree;
import org.iucn.sis.shared.helpers.TaxonomyTree.Kingdom;
import org.restlet.util.Couple;
import org.restlet.util.Triple;

import com.solertium.db.CanonicalColumnName;
import com.solertium.db.Column;
import com.solertium.db.DBException;
import com.solertium.db.DBSessionFactory;
import com.solertium.db.ExecutionContext;
import com.solertium.db.Row;
import com.solertium.db.RowProcessor;
import com.solertium.db.SystemExecutionContext;
import com.solertium.db.query.QConstraint;
import com.solertium.db.query.QRelationConstraint;
import com.solertium.db.query.SelectQuery;
import com.solertium.util.DynamicWriter;
import com.solertium.util.Replacer;
import com.solertium.util.TrivialExceptionHandler;

/**
 * This will perform an import of DEM data, creating taxa as it goes if required
 * and adding assessments and their data as required. This process speaks with a
 * running SIS server to achieve creation of taxa and assessments, so please
 * ensure a server is running.
 * 
 * @author adam.schwartz
 */
public class DEMImport extends DynamicWriter implements Runnable {
	
	private static final String CANNED_NOTE = "Unparseable data encountered during DEM import: ";
	
	private static final int STATE_CHANGED = 1001;
	private static final int STATE_TO_IMPORT = 1002;
	
	public static final int COUNTRY_CODING_OCCURRENCE_TYPE = 1;
	public static final int SUBCOUNTRY_CODING_OCCURRENCE_TYPE = 2;
	public static final int FAO_CODING_OCCURRENCE_TYPE = 3;
	public static final int LME_CODING_OCCURRENCE_TYPE = 4;
	
	private static AtomicBoolean running = new AtomicBoolean(false);
	private static AtomicBoolean failed = new AtomicBoolean(false);
	private static StringBuilder statusMessage = new StringBuilder();
	
	public static String getStatusMessage() {
		return statusMessage.toString();
	}

	public static boolean isFailure() {
		return failed.get();
	}

	public static boolean isRunning() {
		return running.get();
	}
	
	private final String user;
	private final String demSessionName;
	private final Session session;
	
	private final User userO;
	private final IsoLanguageIO isoLanguageIO;
	private final TaxonIO taxonIO;
	private final RegionIO regionIO;
	private final AssessmentIO assessmentIO;
	private final WorkingSetIO workingSetIO;
	private final FieldSchemaGenerator generator;
	private final StringBuilder log;
	
	private final Map<String, Row.Set> lookups;
	
	private final LinkedHashMap<Long, Taxon> assessedNodesBySpc_id;
	private final LinkedHashMap<String, Long> spcNameToIDMap;
	private final Collection<Taxon> nodes;
	private final Map<String, Map<String, String>> library;
	
	private final List<Assessment> successfulAssessments;
	private final List<Assessment> failedAssessments;
	
	private final ExecutionContext lec;

	private ExecutionContext ec, demConversion, demSource;
	
	private TaxonomyTree tree;

	private boolean allowCreateUpperLevelTaxa;
	private boolean allowAssessments;

	public DEMImport(String user, String demSessionName, Session session) throws NamingException {
		this.user = user;
		this.userO = new UserIO(session).getUserFromUsername(user);
		this.demSessionName = demSessionName;
		this.session = session;
		
		this.isoLanguageIO = new IsoLanguageIO(session);
		this.taxonIO = new TaxonIO(session);
		this.regionIO = new RegionIO(session);
		this.assessmentIO = new AssessmentIO(session);
		this.workingSetIO = new WorkingSetIO(session);
		this.generator = new FieldSchemaGenerator();
		
		lookups = new HashMap<String, Row.Set>();
		library = new HashMap<String, Map<String,String>>();
		lec = SIS.get().getLookupDatabase();

		spcNameToIDMap = new LinkedHashMap<String, Long>();
		assessedNodesBySpc_id = new LinkedHashMap<Long, Taxon>();
		nodes = new HashSet<Taxon>();
		log = new StringBuilder();
		
		successfulAssessments = new ArrayList<Assessment>();
		failedAssessments = new ArrayList<Assessment>();
		
		allowCreateUpperLevelTaxa = true;
		allowAssessments = true;
	}
	
	public void setAllowAssessments(boolean allowAssessments) {
		this.allowAssessments = allowAssessments;
	}
	
	public void setAllowCreateUpperLevelTaxa(boolean allowCreateUpperLevelTaxa) {
		this.allowCreateUpperLevelTaxa = allowCreateUpperLevelTaxa;
	}
	
	public void run() {
		running.set(true);
		failed.set(false);
		statusMessage = new StringBuilder();
		
		Date start = Calendar.getInstance().getTime();
		printf("! -- Starting %s conversion at %s", getClass().getSimpleName(), start.toString());
		
		try {

			//registerDatasource("dem", "jdbc:access:///" + source.getAbsolutePath(), "com.hxtt.sql.access.AccessDriver", "", "");

			/*try {
				Document structDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
						DEMImport.class.getResourceAsStream("refstruct.xml"));
				new SystemExecutionContext("dem").setStructure(structDoc);
			} catch (Exception ugly) {
				ugly.printStackTrace();
				statusMessage.append("Internal system failure: could not read DEM.<br>");
				statusMessage.append("Please report the following message:<br>");
				statusMessage.append(DocumentUtils.getStackTraceAsString(ugly).replaceAll("\n", "<br>"));
				failed.set(true);
			}*/
			
			/*registerDatasource("demConversion", "jdbc:access:////usr/data/demMigration.mdb",
					"com.hxtt.sql.access.AccessDriver", "", "");
			registerDatasource("demSource", "jdbc:access:////usr/data/demSource.mdb",
					"com.hxtt.sql.access.AccessDriver", "", "");*/

			ec = new SystemExecutionContext(demSessionName);
			ec.setExecutionLevel(ExecutionContext.ADMIN);
			ec.setAPILevel(ExecutionContext.SQL_ALLOWED);
			
			demConversion = new SystemExecutionContext("demConversion");
			demConversion.setExecutionLevel(ExecutionContext.ADMIN);
			demConversion.setAPILevel(ExecutionContext.SQL_ALLOWED);
			
			demSource = new SystemExecutionContext("demSource");
			demSource.setExecutionLevel(ExecutionContext.ADMIN);
			demSource.setAPILevel(ExecutionContext.SQL_ALLOWED);

			log.append("<table border=\"1\"><tr>" + "<th>Level</th>" + "<th>Friendly Name</th>" + "<th>Kingdom</th>"
					+ "<th>Phylum</th>" + "<th>Class</th>" + "<th>Order</th>" + "<th>Family</th>" + "<th>Genus</th>"
					+ "<th>Species</th>" + "<th>Infrarank</th></tr>");

			if (!failed.get()) {
				try {
					buildTree();
				} catch (Exception e) {
					statusMessage.append("Failed to convert taxa to the SIS data format.<br>");
					statusMessage.append("Please report the following message:<br>");
					statusMessage.append(DocumentUtils.getStackTraceAsString(e).replaceAll("\n", "<br>"));
					failed.set(true);
					if( e instanceof DBException ) {
						Throwable f = e.getCause();
						
						while( f != null ) {
							if( f instanceof SQLException && ((SQLException)f).getNextException() != null )
								print("--- SQL Exception - Next Exception ---\n" + DocumentUtils.getStackTraceAsString(((SQLException)f).getNextException()));
							
							f = f.getCause();
						}
					}
					failed.set(true);
				}
			} else
				statusMessage.append("(Did not convert taxa to the SIS data format due to a prior failure.)<br>");

			if (!failed.get()) {
				try {
					addTaxaDetails();
				} catch (Exception e) {
					statusMessage.append("Failed adding taxa details.<br>");
					statusMessage.append("Please report the following message:<br>");
					statusMessage.append(DocumentUtils.getStackTraceAsString(e).replaceAll("\n", "<br>"));
					failed.set(true);
					if( e instanceof DBException ) {
						Throwable f = e.getCause();
						
						while( f != null ) {
							if( f instanceof SQLException )
								print("--- SQL Exception - Next Exception ---\n" + DocumentUtils.getStackTraceAsString(((SQLException)f).getNextException()));
							
							f = f.getCause();
						}
					}
					failed.set(true);
				}
			}

			if (!failed.get()) {
				try {
					exportNodes();
				} catch (Exception e) {
					statusMessage.append("Failed saving new taxa in SIS.<br>");
					statusMessage.append("Please report the following message:<br>");
					statusMessage.append(DocumentUtils.getStackTraceAsString(e).replaceAll("\n", "<br>"));
					failed.set(true);
					if( e instanceof DBException ) {
						Throwable f = e.getCause();
						
						while( f != null ) {
							if( f instanceof SQLException )
								print("--- SQL Exception - Next Exception ---\n" + DocumentUtils.getStackTraceAsString(((SQLException)f).getNextException()));
							
							f = f.getCause();
						}
					}
					failed.set(true);
				}
			} else
				statusMessage.append("(Did not save new taxa in SIS due to a prior failure.)<br>");

			
			if (!allowAssessments)
				print("Assessment import has been disabled.  No assessments will be imported.");
			else {
				if (!failed.get()) {
					try {
						buildAssessments();
					} catch (Exception e) {
						statusMessage.append("Failed converting assessments to SIS format.<br>");
						statusMessage.append("Please report the following message:<br>");
						statusMessage.append(DocumentUtils.getStackTraceAsString(e).replaceAll("\n", "<br>"));
						failed.set(true);
						if( e instanceof DBException ) {
							Throwable f = e.getCause();
							
							while( f != null ) {
								if( f instanceof SQLException )
									print("--- SQL Exception - Next Exception ---\n" + DocumentUtils.getStackTraceAsString(((SQLException)f).getNextException()));
								
								f = f.getCause();
							}
						}
						failed.set(true);
					}
				} else
					statusMessage
							.append("(Did not attempt conversion of assessments to SIS format due to a prior failure.)<br>");
	
				
				if (!failed.get()) {
					try {
						exportAssessments();
						statusMessage.append("Import successful.");
					} catch (Exception e) {
						statusMessage.append("Failed saving assessments in SIS.<br>");
						statusMessage.append("Please report the following message:<br>");
						statusMessage.append(DocumentUtils.getStackTraceAsString(e).replaceAll("\n", "<br>"));
						failed.set(true);
						if( e instanceof DBException ) {
							Throwable f = e.getCause();
							
							while( f != null ) {
								if( f instanceof SQLException )
									print("--- SQL Exception - Next Exception ---\n" + DocumentUtils.getStackTraceAsString(((SQLException)f).getNextException()));
								
								f = f.getCause();
							}
						}
						failed.set(true);
					}
				} else
					statusMessage.append("(Did not save assessments in SIS due to a prior failure.)<br>");
			}
		} catch (Throwable ex) {
			ex.printStackTrace();
			statusMessage.append("Internal system failure setting up DEMImport.<br>");
			statusMessage.append("Please report the following message:<br>");
			statusMessage.append(DocumentUtils.getStackTraceAsString(ex).replaceAll("\n", "<br>"));
			failed.set(true);
		}
		
		log.append("</table>");
		
		running.set(false);
		
		DBSessionFactory.unregisterDataSource(demSessionName);
		
		DEMImportInformation info = new DEMImportInformation(
			new Date(), !failed.get(), statusMessage.toString(), "", user, log.toString()
		);
		
		Date end = Calendar.getInstance().getTime();
		
		long millis = end.getTime() - start.getTime();
		long secs = millis / 1000;
		long mins = secs / 60;
		
		printf("! -- Finished %s import in %s mins, %s seconds at %s", getClass().getSimpleName(), mins, secs, end.toString());
		print("Results:");
		print(info.toString());
		
		close();
		
		DEMImportInformation.addToQueue(info, SIS.get().getVFS());
	}

	private void addTaxaDetails() throws DBException {
		for (Iterator<Long> iter = assessedNodesBySpc_id.keySet().iterator(); iter.hasNext();) {
			Long curSpcID = iter.next();
			Taxon curNode = assessedNodesBySpc_id.get(curSpcID);
			
			printf("Processing %s", curNode.getFriendlyName());
			
			boolean changed = false;

			// DO COMMON NAMES
			for (Row curRow : queryDEM("common_names", curSpcID)) {
				String name = curRow.get("Common_name").getString(Column.NEVER_NULL);
				boolean primary = curRow.get("Primary").getInteger(Column.NEVER_NULL) == 1;
				//String language = curRow.get("Language").getString(Column.NEVER_NULL);
				String isoCode = curRow.get("ISO_LANG").getString(Column.NEVER_NULL);

				CommonName commonName = new CommonName();
				commonName.setName(name);
				commonName.setIso(isoLanguageIO.getIsoLanguageByCode(isoCode));
				commonName.setPrincipal(primary);
				commonName.setChangeReason(CommonName.ADDED);
				//CommonNameFactory.createCommonName(name, language, isoCode, primary);
				commonName.setValidated(false);

				boolean alreadyExists = false;

				for (CommonName cn : curNode.getCommonNames()) {
					if( commonName.getName().equalsIgnoreCase(cn.getName()) &&
							commonName.getIsoCode().equalsIgnoreCase(cn.getIsoCode()) ) {
						alreadyExists = true;
						break;
					}
				}

				if (!alreadyExists) {
					if( commonName.getName().toUpperCase().equals(commonName.getName())) {
						commonName.setName(WordUtils.capitalizeFully(commonName.getName()));
					}
					
					commonName.setTaxon(curNode);
					curNode.getCommonNames().add(commonName);

					printf(" - Added common name %s", commonName.getName());
					
					changed = true;
				}
			}
			
			// DO SYNONYMS
			for (Row row : queryDEM("Synonyms", curSpcID)) {
				String name = getString(row, "Species_name");
				String notes = getString(row, "SynonymNotes", null);
				int synlevel = TaxonLevel.SPECIES;
				String[] brokenName = name.split("\\s");
				String genusName = "";
				String spcName = "";
				int infraType = -1;
				String infraName = "";
				String subpopName = "";

				genusName = brokenName[0];
				synlevel = TaxonLevel.GENUS;

				if (brokenName.length > 1) {
					spcName = brokenName[1];
					synlevel = TaxonLevel.SPECIES;
				}
				if (brokenName.length > 2) {
					if (brokenName[2].matches("^ssp\\.?$") || brokenName[2].matches("^var\\.?$") || 
							brokenName[2].matches("^fma\\.?$")) {
						if (brokenName[2].matches("^fma\\.?$"))
							infraType = Infratype.INFRARANK_TYPE_FORMA;
						else if (brokenName[2].matches("^var\\.?$"))
							infraType = Infratype.INFRARANK_TYPE_VARIETY;
						else
							infraType = Infratype.INFRARANK_TYPE_SUBSPECIES;

						infraName = brokenName[3];
						for (int i = 4; i < brokenName.length; i++)
							infraName += " " + brokenName[i];

						synlevel = TaxonLevel.INFRARANK;
					} else if (brokenName.length > 3
							&& (brokenName[3].matches("^ssp\\.?$") || brokenName[3].matches("^var\\.?$") || 
									brokenName[2].matches("^fma\\.?$"))) {
						spcName += " " + brokenName[2];
						if (brokenName[3].matches("^fma\\.?$"))
							infraType = Infratype.INFRARANK_TYPE_FORMA;
						else if (brokenName[3].matches("^var\\.?$"))
							infraType = Infratype.INFRARANK_TYPE_VARIETY;
						else
							infraType = Infratype.INFRARANK_TYPE_SUBSPECIES;

						infraName = brokenName[4];
						for (int i = 5; i < brokenName.length; i++)
							infraName += " " + brokenName[i];
					} else {
						String whatsLeft = brokenName[2];
						for (int i = 3; i < brokenName.length; i++)
							whatsLeft += " " + brokenName[i];

						if (whatsLeft.toLowerCase().contains("stock")
								|| whatsLeft.toLowerCase().contains("subpopulation")) {
							subpopName = whatsLeft;
							synlevel = TaxonLevel.SUBPOPULATION;
						} else
							spcName += " " + whatsLeft;
					}
				}
				String authority = getString(row, ("Syn_Authority"));

				Synonym synonym = new Synonym();
				synonym.setGenusName(genusName);
				synonym.setSpeciesName(spcName);
				synonym.setInfraName(infraName);
				
				if (infraType > -1) {
					synonym.setInfraType(Infratype.getInfratype(infraType).getName());
				}
				synonym.setTaxon_level(TaxonLevel.getTaxonLevel(synlevel));
				synonym.setStockName(subpopName);
				synonym.setStatus(Synonym.ADDED);
				synonym.setAuthority(authority, synlevel);
				
				if (!isBlank(notes)) {
					Notes note = createNote(notes);
					note.setSynonym(synonym);
					synonym.getNotes().add(note);
				}

				boolean alreadyExists = false;

				for (Synonym syn : curNode.getSynonyms()) 
					alreadyExists |= synonym.getFriendlyName().equals(syn.getFriendlyName());

				if (!alreadyExists) {
					synonym.setTaxon(curNode);
					curNode.getSynonyms().add(synonym);
					changed = true;
					
					printf(" - Added synonym %s", synonym.getFriendlyName());
				}
			}

			if (changed && curNode.getState() != STATE_TO_IMPORT)
				curNode.setState(STATE_CHANGED);
		}
	}

	private void buildAssessments() throws Exception {
		initCaches();
		
		log.append("<tr><td align=\"center\" colspan=\"9\">" + "<b><u>New Draft Assessments</u></b></td></tr>");
		
		// FOR EACH NEW SPECIES...
		for (Iterator<Long> iter = assessedNodesBySpc_id.keySet().iterator(); iter.hasNext();) {
			Long curDEMid = iter.next();
			Taxon curNode = assessedNodesBySpc_id.get(curDEMid);

			logNode(curNode);

			Assessment curAssessment = new Assessment();
			curAssessment.setId(0);
			curAssessment.setTaxon(curNode);
			curAssessment.setSchema(SIS.get().getDefaultSchema());
			curAssessment.setType(AssessmentType.DRAFT_ASSESSMENT_TYPE);

			systematicsTableImport(curDEMid, curAssessment);
			
			if (curAssessment.getField().isEmpty())
				continue;
			
			if (!assessmentIO.allowedToCreateNewAssessment(curAssessment)) {
				failedAssessments.add(curAssessment);
			}
			else {
				distributionTableImport(curDEMid, curAssessment);
				populationTableImport(curDEMid, curAssessment);
				habitatTableImport(curDEMid, curAssessment);
				lifeHistoryTableImport(curDEMid, curAssessment);
				threatTableImport(curDEMid, curAssessment);
				countryTableImport(curDEMid, curAssessment);
				redListingTableImport(curDEMid, curAssessment);
				landCoverTableImport(curDEMid, curAssessment);
				utilisationTableImport(curDEMid, curAssessment);
				growthFormTableImport(curDEMid, curAssessment);
				conservationMeasuresTableImport(curDEMid, curAssessment);
				ecosystemServicesTableImport(curDEMid, curAssessment);
				faoMarineTableImport(curDEMid, curAssessment);
				lmeTableImport(curDEMid, curAssessment);
				useTradeImport(curDEMid, curAssessment);
				livelihoodsTableImport(curDEMid, curAssessment);

				//Unused in SIS 2:
//				riversTableImport(curDEMid, curAssessment, data);
//				lakesTableImport(curDEMid, curAssessment, data);
				
				referencesImport(curDEMid, curAssessment);
	
				successfulAssessments.add(curAssessment);
			}
		}
	}
	
	private synchronized void initCaches() throws DBException {
		List<Triple<String, String, String>> occurrenceEntries = new ArrayList<Triple<String,String,String>>();
		occurrenceEntries.add(new Triple<String, String, String>("subcountry_list_all", "subcountry_number", "BruLevel4Code"));
		occurrenceEntries.add(new Triple<String, String, String>("countries_list_all", "Country_Number", "Country_code"));
		occurrenceEntries.add(new Triple<String, String, String>("large_marine_ecosystems_list", "lme_code", "Marine_ref"));
		occurrenceEntries.add(new Triple<String, String, String>("FAO_marine_list", "aquatic_number", "FAO_ID"));
		
		for (final Triple<String, String, String> entry : occurrenceEntries) {
			SelectQuery query = new SelectQuery();
			query.select(entry.getFirst(), entry.getSecond());
			query.select(entry.getFirst(), entry.getThird());
			
			synchronized (query) {
				final Map<String, String> cache = new HashMap<String, String>();
				
				demSource.doQuery(query, new RowProcessor() {
					public void process(Row row) {
						cache.put(row.get(entry.getSecond()).toString(), row.get(entry.getThird()).toString().trim());
					}
				});
				
				library.put(entry.getFirst(), cache);
			}
		}
		
		Map<String, Triple<String, String, String>> demConversions = 
			new HashMap<String, Triple<String,String,String>>();
		demConversions.put("OldConsActions to new Actions IN Place", new Triple<String, String, String>("oldaction", "newaction", "comment"));
		demConversions.put("OLD CONS ACT to RESEARCH MAPPING", new Triple<String, String, String>("old ActionAFID", "newResearchAFID", "comments"));
		demConversions.put("CONS ACTIONS MAPPING OLD TO NEW", new Triple<String, String, String>("oldActionID", "newActionID", "Comment"));
		
		for (Map.Entry<String, Triple<String, String, String>> entry : demConversions.entrySet()) {
			SelectQuery query = new SelectQuery();
			query.select(entry.getKey(), entry.getValue().getFirst());
			query.select(entry.getKey(), entry.getValue().getSecond());
			query.select(entry.getKey(), entry.getValue().getThird());
			
			Row.Set rs = new Row.Set();
			
			demConversion.doQuery(query, rs);
			
			Map<String, String> cache = new HashMap<String, String>();
			for (Row row : rs.getSet())
				cache.put(row.get(entry.getValue().getFirst()).toString(), 
						row.get(entry.getValue().getSecond()) + "_" + row.get(entry.getValue().getThird()));
			
			
			library.put(entry.getKey(), cache);
		}
		
		Map<String, String> offTakeLookup = new HashMap<String, String>();
		offTakeLookup.put("Increasing", "1");
		offTakeLookup.put("Decreasing", "2");
		offTakeLookup.put("Stable", "3");
		offTakeLookup.put("Unknown", "4");
		
		library.put(CanonicalNames.TrendInWildOfftake, offTakeLookup);
		
		Map<String, String> domesticLookup = new HashMap<String, String>();
		domesticLookup.put("Increasing", "1");
		domesticLookup.put("Decreasing", "2");
		domesticLookup.put("Stable", "3");
		domesticLookup.put("Not cultivated", "4");
		domesticLookup.put("Unknown", "5");
		
		library.put(CanonicalNames.TrendInDomesticOfftake, domesticLookup);
		
		SelectQuery selectRegion = new SelectQuery();
		selectRegion.select("region_lookup_table", "*");

		Row.Set rs = new Row.Set();
		
		Map<String, String> regionCache = new HashMap<String, String>();
		for (Row row : rs.getSet())
			regionCache.put(getString(row, "Region_number"), getString(row, "Region_name"));
		
		library.put(CanonicalNames.RegionInformation, regionCache);
	}
	
	private void report(Taxon taxon) {
		printf("Adding taxon #%s %s at level %s to tree", taxon.getId(), taxon.getFriendlyName(), taxon.getLevel());
	}
	
	private Taxon newSimpleNode(String name, String status, int level, Taxon parent) {
		Taxon taxon = new Taxon();
		taxon.setId(0);
		taxon.setName(name);
		taxon.setTaxonLevel(TaxonLevel.getTaxonLevel(level));
		taxon.setStatus(status);
		taxon.setParent(parent);
		taxon.setFriendlyName(name);
		taxon.setState(STATE_TO_IMPORT);
		
		return taxon;
	}

	public void buildTree() throws Exception {
		tree = new TaxonomyTree();

		SelectQuery select = new SelectQuery();
		select.select("Systematics", "*");

		Row.Set rows = new Row.Set();

		ec.doQuery(select, rows);

		List<Row> rowList = rows.getSet();

		print("There are " + rowList.size() + " entries in this DEM.");

		for (Iterator<Row> iter = rowList.listIterator(); iter.hasNext();) {
			Row curCol = iter.next();

			ArrayList<String> footprint = new ArrayList<String>();

			Taxon kingdomN = null;
			Taxon phylumN = null;
			Taxon classN = null;
			Taxon orderN = null;
			Taxon familyN = null;
			Taxon genusN = null;
			Taxon speciesN = null;
			Taxon rankN = null;
			Taxon sspN = null;

			String curKingdom = curCol.get("Kingdom").getString().trim().toUpperCase();
			String curPhylum = curCol.get("Phylum").getString().trim().toUpperCase();
			String curClass = curCol.get("Class").getString().trim().toUpperCase();
			String curOrder = curCol.get("Order").getString().trim().toUpperCase();
			String curFamily = curCol.get("Family").getString().trim().toUpperCase();
			String curGenus = curCol.get("Genus").getString().trim();
			String curSpecies = curCol.get("Species").getString().trim();
			String curInfratype = curCol.get("Rank").toString();
			String curInfraEpithet = curCol.get("Rank_epithet").toString();
			String curSSP = null;
			String curStat = "New";

			String spcTaxonomicAuthority = "";
			String infraTaxonomicAuthority = "";
			long specID = curCol.get("Sp_code").getPrimitiveLong();

			boolean hybrid = curCol.get("Hybrid").getInteger(Column.NEVER_NULL) == 1;

			if (curCol.get("Author_year").getString(Column.NATURAL_NULL) != null)
				spcTaxonomicAuthority = curCol.get("Author_year").getString();
			if (curCol.get("Rank_author").getString(Column.NATURAL_NULL) != null)
				infraTaxonomicAuthority = curCol.get("Rank_author").getString();

			if (!isBlank(curInfratype) && isBlank(curInfraEpithet)) {
				failed.set(true);
				statusMessage.append("Your DEM contains an infrarank with no epithet specified."
						+ " Please change your data, and try the import again.<br>");
				return;
			}

			if (isBlank(curInfratype) && !isBlank(curInfraEpithet)) {
				failed.set(true);
				statusMessage.append("Your DEM contains an infrarank with an epithet, but no"
						+ " rank specified. Please change your data, and try the import again.<br>");
				return;
			}

			if (curCol.get("Rank_author").getString(Column.NATURAL_NULL) != null)
				spcTaxonomicAuthority = curCol.get("Rank_author").getString(Column.NATURAL_NULL);

			curSSP = curCol.get("Sub_pop").getString(Column.NATURAL_NULL);
			if (curSSP != null)
				curSSP = curSSP.trim();

			kingdomN = tree.getNode(TaxonNode.KINGDOM, curKingdom, curKingdom);
			if (kingdomN == null) {
				kingdomN = fetchNode(curKingdom, curKingdom);

				if (kingdomN == null) {
					kingdomN = newSimpleNode(curKingdom, curStat, TaxonLevel.KINGDOM, null);
				}

				kingdomN.getFootprint();
				tree.addNode(curKingdom, kingdomN);
				
				report(kingdomN);
			}
			footprint.add(kingdomN.getName());

			nodes.add(kingdomN);

			phylumN = tree.getNode(TaxonNode.PHYLUM, curKingdom, curPhylum);
			if (phylumN == null) {
				phylumN = fetchNode(curKingdom, curPhylum);

				if (phylumN == null) {
					phylumN = newSimpleNode(curPhylum, curStat, TaxonLevel.PHYLUM, kingdomN);
				}

				kingdomN.getChildren().add(phylumN);
				phylumN.getFootprint();
				tree.addNode(curKingdom, phylumN);
				
				report(phylumN);
			}
			updateFootprint(footprint, phylumN);

			nodes.add(phylumN);

			classN = tree.getNode(TaxonNode.CLASS, curKingdom, curClass);
			if (classN == null) {
				classN = fetchNode(curKingdom, curClass);

				if (classN == null) {
					classN = newSimpleNode(curClass, curStat, TaxonLevel.CLASS, phylumN);
				}

				phylumN.getChildren().add(classN);
				classN.getFootprint();
				tree.addNode(curKingdom, classN);
				
				report(classN);
			}
			updateFootprint(footprint, classN);
			nodes.add(classN);

			orderN = tree.getNode(TaxonNode.ORDER, curKingdom, curOrder);
			if (orderN == null) {
				orderN = fetchNode(curKingdom, curOrder);

				if (orderN == null) {
					orderN = newSimpleNode(curOrder, curStat, TaxonLevel.ORDER, classN);
				}

				classN.getChildren().add(orderN);
				orderN.getFootprint();
				tree.addNode(curKingdom, orderN);
				
				report(orderN);
			}
			updateFootprint(footprint, orderN);
			nodes.add(orderN);

			familyN = tree.getNode(TaxonNode.FAMILY, curKingdom, curFamily);
			if (familyN == null) {
				familyN = fetchNode(curKingdom, curFamily);

				if (familyN == null) {
					familyN = newSimpleNode(curFamily, curStat, TaxonLevel.FAMILY, orderN);
				}

				orderN.getChildren().add(familyN);
				familyN.getFootprint();
				tree.addNode(curKingdom, familyN);
			
				report(familyN);
			}
			updateFootprint(footprint, familyN);
			nodes.add(familyN);

			genusN = tree.getNode(TaxonNode.GENUS, curKingdom, curGenus);
			if (genusN == null) {
				genusN = fetchNode(curKingdom, curGenus);

				if (genusN == null) {
					genusN = newSimpleNode(curGenus, curStat, TaxonLevel.GENUS, familyN);
				}

				familyN.getChildren().add(genusN);
				genusN.getFootprint();
				tree.addNode(curKingdom, genusN);
				
				report(genusN);
			}
			updateFootprint(footprint, genusN);
			nodes.add(genusN);

			speciesN = tree.getNode(TaxonNode.SPECIES, curKingdom, curGenus + " " + curSpecies);
			if (speciesN == null) {
				speciesN = fetchNode(curKingdom, curGenus + " " + curSpecies);

				if (speciesN == null) {
					speciesN = newSimpleNode(curSpecies, curStat, TaxonLevel.SPECIES, genusN);
					speciesN.setFriendlyName(curGenus + " " + curSpecies);
					
					spcNameToIDMap.put(speciesN.getFullName(), new Long(specID));
				}

				speciesN.setFriendlyName(curGenus + " " + speciesN.getName());
				speciesN.setTaxonomicAuthority(spcTaxonomicAuthority);

				genusN.getChildren().add(speciesN);
				speciesN.getFootprint();
				tree.addNode(curKingdom, speciesN);
				
				report(speciesN);
			}
			
			//if (curInfratype == null && curSSP == null) {
				assessedNodesBySpc_id.put(new Long(specID), speciesN);
				speciesN.setHybrid(hybrid);
			//}

			updateFootprint(footprint, speciesN);
			nodes.add(speciesN);

			if (!isBlank(curInfratype)) {
				rankN = tree.getNode(TaxonNode.INFRARANK, curKingdom, curGenus + " " + curSpecies + " " + curInfratype
						+ " " + curInfraEpithet);
				if (rankN == null) {
					rankN = fetchNode(curKingdom, curGenus + " " + curSpecies + " " + curInfratype + " " + curInfraEpithet);

					if (rankN == null) {
						rankN = newSimpleNode(curInfraEpithet, curStat, TaxonLevel.INFRARANK, speciesN);
						

						if (curInfratype.trim().matches("^ssp\\.?$"))
							rankN.setInfratype(Infratype.getInfratype(Infratype.INFRARANK_TYPE_SUBSPECIES));
						else if (curInfratype.trim().matches("^var\\.?$")
								&& rankN.getFootprint()[0].equalsIgnoreCase("PLANTAE"))
							rankN.setInfratype(Infratype.getInfratype(Infratype.INFRARANK_TYPE_VARIETY));
						else {
							failed.set(true);
							statusMessage.append("Your DEM contains an infrarank with an invalid rank of " + curInfratype + "."
									+ " The valid ranks are \"ssp.\" and \"var.\", and a variety MUST be"
									+ " in the kingdom PLANTAE. Please change your data, and"
									+ " try the import again.<br>");
							return;
						}

						rankN.setFriendlyName(rankN.generateFullName());
						
						spcNameToIDMap.put(rankN.getFullName(), new Long(specID));
					}

					rankN.setFriendlyName(rankN.generateFullName());
					rankN.setTaxonomicAuthority(infraTaxonomicAuthority);

					speciesN.getChildren().add(rankN);
					rankN.getFootprint();
					tree.addNode(curKingdom, rankN);
					
					report(rankN);
				}

				//if (curSSP == null) {
					assessedNodesBySpc_id.put(new Long(specID), rankN);
					rankN.setHybrid(hybrid);
				//}

				nodes.add(rankN);
			}

			if (curSSP != null) {
				if (curInfratype == null)
					sspN = tree
							.getNode(TaxonNode.SUBPOPULATION, curKingdom, curGenus + " " + curSpecies + " " + curSSP);
				else
					sspN = tree.getNode(TaxonNode.INFRARANK_SUBPOPULATION, curKingdom, curGenus + " " + curSpecies
							+ " " + curInfratype + " " + " " + curInfraEpithet + " " + curSSP);

				if (sspN == null) {
					if (curInfratype == null)
						sspN = fetchNode(curKingdom, curGenus + " " +  curSpecies + " " +  curSSP);
					else
						sspN = fetchNode(curKingdom, curGenus + " " + curSpecies + " " + curInfratype + " " + curInfraEpithet + " " +curSSP);

					if (sspN == null) {
						sspN = newSimpleNode(curSSP, curStat, TaxonLevel.SUBPOPULATION, speciesN);
						sspN.setTaxonomicAuthority(spcTaxonomicAuthority);

						if (curInfratype != null) {
							if (curInfratype.trim().matches("^ssp\\.?$"))
								rankN.setInfratype(Infratype.getInfratype(Infratype.INFRARANK_TYPE_SUBSPECIES));
							else if (curInfratype.trim().matches("^var\\.?$")
									&& rankN.getFootprint()[0].equalsIgnoreCase("PLANTAE"))
								rankN.setInfratype(Infratype.getInfratype(Infratype.INFRARANK_TYPE_VARIETY));
							else {
								failed.set(true);
								statusMessage.append("Your DEM contains an infrarank with an invalid rank."
										+ " The valid ranks are \"ssp.\" and \"var.\", and a variety MUST be"
										+ " in the kingdom PLANTAE. Please change your data, and"
										+ " try the import again.<br>");
								return;
							}
						}

						if (curInfratype != null)
							updateFootprint(footprint, rankN);
						sspN.setFriendlyName(sspN.generateFullName());

						spcNameToIDMap.put(sspN.getFullName(), new Long(specID));
					}

					sspN.setFriendlyName(sspN.generateFullName());
					speciesN.getChildren().add(sspN);
					sspN.getFootprint();
					tree.addNode(curKingdom, sspN);
					
					report(sspN);
				}

				assessedNodesBySpc_id.put(new Long(specID), sspN);
				sspN.setHybrid(hybrid);
				nodes.add(sspN);
			}
		}
		printf("Found %s nodes in the tree", nodes.size());
		printf("Kingdoms are %s", tree.getKingdoms());
	}

	private void conservationMeasuresTableImport(Long curDEMid, Assessment curAssessment) throws DBException {
		HashMap<Integer, Field> consSelected = new HashMap<Integer, Field>();
		HashMap<Integer, Field> researchSelected = new HashMap<Integer, Field>();
		
		Field conservationAction = new Field(CanonicalNames.ConservationActions, curAssessment);
		Field researchNeeded = new Field(CanonicalNames.Research, curAssessment);
		
		for (Row curRow : queryDEM("conservation_measures", curDEMid)) {
			int curCode = curRow.get("Measure_code").getInteger();
			int score = curRow.get("cm_timing").getInteger(Column.NEVER_NULL);

			try {
				if (score == 1) // Do in-place conversion
				{
					try {
						doResearchInPlace(curAssessment, curCode);
					} catch (DBException ignored) {
					}
				}// End if in-place needed
				else if (score == 2) // Do research needed conversion
				{
					try {
						doActionsParse(researchSelected, "OLD CONS ACT to RESEARCH MAPPING", researchNeeded, curCode);
					} catch (DBException ignored) {
						TrivialExceptionHandler.ignore(this, ignored);
					}
					try {
						doActionsParse(consSelected, "CONS ACTIONS MAPPING OLD TO NEW", conservationAction, curCode);
					} catch (DBException ignored) {
						TrivialExceptionHandler.ignore(this, ignored);
					}
				}
			} catch (Exception e) {
				println("Error checking against conversion table.");
			}
		}
		
		if (!consSelected.isEmpty()) {
			conservationAction.getFields().addAll(consSelected.values());
			
			curAssessment.setField(conservationAction);
		}
		
		if (!researchSelected.isEmpty()) {
			researchNeeded.getFields().addAll(researchSelected.values());
			
			curAssessment.setField(researchNeeded);
		}
	}

	private void parseOccurrence(Field field, long curDEMid, String countryJoinTable, int occurrenceType) throws Exception {
		SelectQuery select = new SelectQuery();
		select.select("coding_occurence", "*");
		select.constrain(new CanonicalColumnName("coding_occurence", "Sp_code"), QConstraint.CT_EQUALS, curDEMid);
		select.constrain(new CanonicalColumnName("coding_occurence", "co_type"), QConstraint.CT_EQUALS,
				occurrenceType);

		for (Row row : queryDEM(select)) {
			String countryISO = searchLibrary(countryJoinTable, getString(row, "obj_id"));
			if (countryISO == null) {
				printf("- Occurrence cache miss for %s in %s", getString(row, "obj_id"), countryJoinTable);
				continue;
			}
			
			Integer index = getIndex(field.getName() + "Lookup", countryISO, null);
			if (index == null) {
				printf("No SIS 2 Country matching '%s'", countryISO);
				continue;
			}
			
			Field subfield = new Field();
			subfield.setName(field.getName() + "Subfield");
			subfield.setParent(field);

			fetchCodingOccurrenceData(row, subfield);
			
			ProxyField proxy = new ProxyField(subfield);
			proxy.setForeignKeyPrimitiveField(field.getName()+"Lookup", index, field.getName()+"Lookup");

			field.getFields().add(subfield);
		}
	}
	
	private void countryTableImport(Long curDEMid, Assessment curAssessment) throws Exception {
		final Field field = new Field(CanonicalNames.CountryOccurrence, curAssessment);
		
		parseOccurrence(field, curDEMid, "countries_list_all", COUNTRY_CODING_OCCURRENCE_TYPE);
		
		parseOccurrence(field, curDEMid, "subcountry_list_all", SUBCOUNTRY_CODING_OCCURRENCE_TYPE);

		if (field.hasData())
			curAssessment.getField().add(field);
	}
	
	private void addTextPrimitiveField(String fieldName, String dataPoint, Assessment assessment, String value) {
		if (isBlank(value))
			return;
		
		Field field = new Field(fieldName, assessment);
		
		ProxyField proxy = new ProxyField(field);
		proxy.setTextPrimitiveField(dataPoint, value);
		
		assessment.getField().add(field);
	}
	
	private void addStringPrimitiveField(String fieldName, String dataPoint, Assessment assessment, String value) {
		if (isBlank(value))
			return;
		
		Field field = new Field(fieldName, assessment);
		
		ProxyField proxy = new ProxyField(field);
		proxy.setStringPrimitiveField(dataPoint, value);
		
		assessment.getField().add(field);
	}
	
	private void addDatePrimitiveField(String fieldName, String dataPoint, Assessment assessment, Date value) {
		if (value == null)
			return;
		
		Field field = new Field(fieldName, assessment);
		ProxyField proxy = new ProxyField(field);
		proxy.setDatePrimitiveField(dataPoint, value);
		
		assessment.getField().add(field);
	}
	
	private void addBooleanRangePrimitiveField(String fieldName, String dataPoint, Assessment assessment, String value) {
		if (isBlank(value))
			return;
		
		String dataValue = value;
		if ("true".equalsIgnoreCase(dataValue) || "Y".equalsIgnoreCase(dataValue))
			dataValue = BooleanRangePrimitiveField.YES;
		else if ("false".equalsIgnoreCase(dataValue) || "N".equalsIgnoreCase(dataValue))
			dataValue = BooleanRangePrimitiveField.NO;
		else if ("2".equals(dataValue))
			dataValue = BooleanRangePrimitiveField.UNKNOWN;
		
		//Only SevereFragmentation (see #908)
		if (CanonicalNames.SevereFragmentation.equals(fieldName) && 
				BooleanRangePrimitiveField.NO.equals(dataValue))
			return;
		
		if (BooleanRangePrimitiveField.UNKNOWN.equals(dataValue) || 
				BooleanRangePrimitiveField.YES.equals(dataValue) || 
				BooleanRangePrimitiveField.NO.equals(dataValue)) {
			Field field = new Field(fieldName, assessment);
			ProxyField proxy = new ProxyField(field);
			proxy.setBooleanRangePrimitiveField(dataPoint, dataValue);
			
			assessment.getField().add(field);
		}
		else
			printf("Boolean Range value for %s.%s could not be translated from given value %s", fieldName, dataPoint, value);
	}
	
	private void addBooleanUnknownPrimitiveField(String fieldName, String dataPoint, Assessment assessment, Integer value) {
		if (value == null)
			return;
		
		Field field = new Field(fieldName, assessment);
		ProxyField proxy = new ProxyField(field);
		proxy.setBooleanUnknownPrimitiveField(dataPoint, value);
		
		assessment.getField().add(field);
	}
	
	private void addBooleanPrimitiveField(String fieldName, String dataPoint, Assessment assessment, Boolean value) {
		if (value == null || !value.booleanValue())
			return;
		
		Field field = new Field(fieldName, assessment);
		ProxyField proxy = new ProxyField(field);
		proxy.setBooleanPrimitiveField(dataPoint, value, Boolean.FALSE);
		
		assessment.getField().add(field);
	}
	
	private void addRangePrimitiveField(String fieldName, String dataPoint, Assessment assessment, String value) {
		if (isBlank(value))
			return;
		
		if (!isValidRangeFormat(value)) {
			/*sendNote(curAssessment.getType(), CANNED_NOTE + locations, curAssessment.getAssessmentID(),
					CanonicalNames.LocationsNumber);*/
		}
		
		Field field = new Field(fieldName, assessment);
		
		ProxyField proxy = new ProxyField(field);
		proxy.setRangePrimitiveField(dataPoint, value);
		
		assessment.getField().add(field);
	}
	
	private Field addFloatPrimitiveField(String fieldName, String dataPoint, Assessment assessment, String value) {
		final Float floatValue;
		try {
			floatValue = Float.valueOf(value);
		} catch (NullPointerException e) {
			return null;
		} catch (NumberFormatException e) {
			return null;
		}
		
		Field field = new Field(fieldName, assessment);
		
		ProxyField proxy = new ProxyField(field);
		proxy.setFloatPrimitiveField(dataPoint, floatValue);
		
		assessment.getField().add(field);
		
		return field;
	}
	
	private Field addFieldForCheckedOptions(String fieldName, String dataPoint, Row row, Assessment assessment, String... options) throws DBException {
		ArrayList<Integer> selection = new ArrayList<Integer>();
		for (String option : options) {
			boolean checked = isChecked(row, option);
			if (checked) {
				Integer value = getIndex(fieldName, dataPoint, option, null);
				if (value != null)
					selection.add(value);
			}
		}
		
		return addFieldForCheckedOptions(fieldName, dataPoint, row, assessment, selection);
	}
	
	private Field addFieldForCheckedOptions(String fieldName, String dataPoint, Row row, Assessment assessment, List<Integer> selection) throws DBException {
		if (!selection.isEmpty()) {
			Field field = new Field(fieldName, assessment);
			
			ProxyField proxy = new ProxyField(field);
			proxy.setForeignKeyListPrimitiveField(dataPoint, selection, fieldName + "_" + dataPoint + "Lookup");
			
			assessment.getField().add(field);
			
			return field;
		}
		else
			return null;
	}
	
	private void addForeignKeyPrimitiveField(String fieldName, String dataPoint, Assessment assessment, Integer value) {
		if (value == null)
			return;
		
		Field field = new Field(fieldName, assessment);
		ProxyField proxy = new ProxyField(field);
		proxy.setForeignKeyPrimitiveField(dataPoint, value, fmtLookupTableName(fieldName, dataPoint));
		
		assessment.getField().add(field);
	}

	private void distributionTableImport(Long curDEMid, Assessment curAssessment) throws DBException {
		List<Row> rows = queryDEM("Distribution", curDEMid);
		if (rows.isEmpty())
			return;
		
		Row row = rows.get(0);

		// Biogeographic Realm
		addFieldForCheckedOptions(CanonicalNames.BiogeographicRealm, "realm", row, curAssessment,
				"Afrotropical", "Antarctic", "Australasian", "Indomalayan", 
				"Nearctic", "Neotropical", "Oceanian", "Palearctic");
		
		addFieldForCheckedOptions(CanonicalNames.System, "value", row, curAssessment,
				"Terrestrial", "Freshwater", "Marine");

		if (isChecked(row, "Congregatory"))
			addForeignKeyPrimitiveField(CanonicalNames.Congregatory, "value", curAssessment, 1);
		
		Integer movementPattern = null;
		if (isChecked(row, "Nomadic"))
			movementPattern = 4;
		else if (isChecked(row, "Migratory"))
			movementPattern = 1;
		else if (isChecked(row, "Altitudinally_migrant"))
			movementPattern = 2;
		
		if (movementPattern != null)
			addForeignKeyPrimitiveField(CanonicalNames.MovementPatterns, "pattern", curAssessment, movementPattern);
		
		// Map Status
		String mapStatus = row.get("Map_status").toString();
		Integer mapStatusSelected = getIndex(CanonicalNames.MapStatus, "status", mapStatus, null);
		if (mapStatusSelected != null) {
			Field field = new Field(CanonicalNames.MapStatus, curAssessment);
			
			ProxyField proxy = new ProxyField(field);
			proxy.setForeignKeyPrimitiveField("status", mapStatusSelected, CanonicalNames.MapStatus + "_statusLookup");
			
			curAssessment.getField().add(field);
		}

		addFloatPrimitiveField(CanonicalNames.ElevationLower, "limit", curAssessment, row.get("lower_elev").toString());
		addFloatPrimitiveField(CanonicalNames.ElevationUpper, "limit", curAssessment, row.get("upper_elev").toString());
		addFloatPrimitiveField(CanonicalNames.DepthUpper, "limit", curAssessment, row.get("upper_depth").toString());
		addFloatPrimitiveField(CanonicalNames.DepthLower, "limit", curAssessment, row.get("lower_depth").toString());
		
		// DepthZone by hand since the options don't map up
		List<Integer> depthZoneSelection = new ArrayList<Integer>();
		int index = 1;
		for (String key : new String[] { "Shallow_photic", "Photic", "Bathyl", "Abyssal", "Hadal" }) {
			if (row.get(key) != null && Integer.valueOf(1).equals(row.get(key).getInteger())) {
				depthZoneSelection.add(index);
			}
			index++;
		}
		addFieldForCheckedOptions(CanonicalNames.DepthZone, "depthZone", row, curAssessment, depthZoneSelection);

		addRangePrimitiveField(CanonicalNames.AOO, "range", curAssessment, row.get("AOO").toString());
		addRangePrimitiveField(CanonicalNames.EOO, "range", curAssessment, row.get("EOO").toString());
	}

	private String searchLibrary(String catalog, String key) {
		Map<String, String> cache = library.get(catalog);
		if (cache == null || key == null)
			return null;
		
		return cache.get(key);
	}
	
	private void addSimpleInPlaceFieldWithNote(int value, String notes, String fieldName, Assessment assessment) {
		Field field = new Field(fieldName, assessment);
		field.addPrimitiveField(new ForeignKeyPrimitiveField(
			"value", field, value, fmtLookupTableName(fieldName, "value")
		));
		if (notes != null)
			field.addPrimitiveField(new StringPrimitiveField("note", field, notes));
		
		assessment.setField(field);
	}
	
	private void doResearchInPlace(Assessment assessment, int oldaction) throws DBException {
		String newData = searchLibrary("OldConsActions to new Actions IN Place", Integer.toString(oldaction));

		if (newData != null) {
			String[] split = newData.split("_");
			//int newAction = Integer.valueOf(split[0]);
			String notes = split[1];
			
			if (oldaction == 52)
				addSimpleInPlaceFieldWithNote(1, notes, CanonicalNames.InPlaceResearchRecoveryPlan, assessment);
			else if (oldaction == 40 || oldaction == 41)
				addSimpleInPlaceFieldWithNote(1, notes, CanonicalNames.InPlaceLandWaterProtectionSitesIdentified, assessment);
			else if (oldaction == 39)
				addSimpleInPlaceFieldWithNote(1, notes, CanonicalNames.InPlaceLandWaterProtectionInPA, assessment);
			else if (oldaction == 50)
				addSimpleInPlaceFieldWithNote(1, notes, CanonicalNames.InPlaceSpeciesManagementHarvestPlan, assessment);
			else if (oldaction == 47 || oldaction == 48)
				addSimpleInPlaceFieldWithNote(1, notes, CanonicalNames.InPlaceSpeciesManagementReintroduced, assessment);
			else if (oldaction == 55 || oldaction == 56 || oldaction == 57)
				addSimpleInPlaceFieldWithNote(1, notes, CanonicalNames.InPlaceSpeciesManagementExSitu, assessment);
			else if (oldaction == 19 || oldaction == 20 || oldaction == 21 || oldaction == 22)
				addSimpleInPlaceFieldWithNote(1, notes, CanonicalNames.InPlaceEducationSubjectToPrograms, assessment);
		}
	}

	private void doActionsParse(HashMap<Integer, Field> dataMap, String libraryTable, Field field, int oldCode) throws DBException {
		String newData = searchLibrary(libraryTable, Integer.toString(oldCode));
		
		if (newData != null) {
			String[] split = newData.split("_");
			String newCode = split[0];
			String notes = split.length > 1 ? split[1] : null;
			
			String numericCode = "";
			for (char c : newCode.toCharArray())
				if (Character.isWhitespace(c))
					break;
				else
					numericCode += c;
			
			Integer index = getIndex(field.getName()+"Lookup", numericCode, null);
			if (index == null) {
				printf("No %sLookup found to match %s (found from %s).", field.getName(), numericCode, newCode);
				return;
			}
			
			Field subfield = dataMap.get(index);
			if (subfield == null) {
				subfield = new Field(field.getName()+"Subfield", null);
				subfield.setParent(field);
				
				ProxyField proxy = new ProxyField(subfield);
				proxy.setForeignKeyPrimitiveField(field.getName()+"Lookup", index, field.getName()+"Lookup");
				
				dataMap.put(index, subfield);
			}
			
			if (!isBlank(notes)) {
				ProxyField proxy = new ProxyField(subfield);
				String existingNote = proxy.getStringPrimitiveField("note");
				if ("".equals(existingNote))
					proxy.setStringPrimitiveField("note", XMLUtils.clean(notes));
				else
					proxy.setStringPrimitiveField("note", existingNote + " --- \n" + XMLUtils.clean(notes));
			}
		}
	}

	private void ecosystemServicesTableImport(Long curDEMid, Assessment curAssessment) throws DBException {
		SelectQuery select = new SelectQuery();
		select.select("ecosystem_services", "*");
		select.constrain(new CanonicalColumnName("ecosystem_services", "Sp_code"), QConstraint.CT_EQUALS, curDEMid);

		Row.Set rowLoader = new Row.Set();
		ec.doQuery(select, rowLoader);

		if (rowLoader == null)
			return;
		
		Field field = new Field(CanonicalNames.EcosystemServices, curAssessment);
		
		Boolean infoAvailable = null;
		Boolean providesNothing = null;

		for (Row row : queryDEM("ecosystem_services", curDEMid)) {
			if (getString(row, "info_available", null) != null) {
				if (infoAvailable == null)
					infoAvailable = isChecked(row, "info_available");
				else
					infoAvailable |= isChecked(row, "info_available");
			}
			if (getString(row, "no_services", null) != null) {
				if (providesNothing == null)
					providesNothing = isChecked(row, "no_services");
				else
					providesNothing |= isChecked(row, "no_services");
			}
			
			List<Couple<String, String>> selections = new ArrayList<Couple<String,String>>();
			selections.add(new Couple<String, String>("1", "Water_quality"));
			selections.add(new Couple<String, String>("2", "Water_supplies"));
			selections.add(new Couple<String, String>("3", "Flood_control"));
			selections.add(new Couple<String, String>("4", "Climate_regulation"));
			selections.add(new Couple<String, String>("5", "Landscape"));
			selections.add(new Couple<String, String>("6", "Air_quality"));
			selections.add(new Couple<String, String>("7", "Nutrient_cycling"));
			selections.add(new Couple<String, String>("8", "Habitat_maintenance"));
			selections.add(new Couple<String, String>("9", "Critical_habitat"));
			selections.add(new Couple<String, String>("10", "Pollination"));
			selections.add(new Couple<String, String>("11", "Erosion_control"));
			selections.add(new Couple<String, String>("12", "Biocontrol"));
			selections.add(new Couple<String, String>("13", "Shoreline_protection"));
			selections.add(new Couple<String, String>("14", "Other"));
			selections.add(new Couple<String, String>("15", "Other"));

			for (Couple<String, String> entry : selections) {
				Field subfield = new Field(field.getName()+"Subfield", null);
				subfield.setParent(field);

				ProxyField proxy = new ProxyField(subfield);
				
				String rankKey = entry.getSecond()+"_rank", scaleKey = entry.getSecond()+"_scale", otherKey = null;
				if (entry.getFirst().equals("14")) {
					rankKey += "1";
					scaleKey += "1";
					otherKey = "specify_other1";
				}
				else if (entry.getFirst().equals("15")) {
					rankKey += "2";
					scaleKey += "2";
					otherKey = "specify_other2";
				}
				
				String rankImportance = getString(row, entry.getSecond()+"_rank", null);
				String scaleRangeOfBenefit = getString(row, entry.getSecond()+"_scale", null);
				
				if (rankImportance != null) {
					try {
						proxy.setForeignKeyPrimitiveField("importance", Integer.valueOf(rankImportance), fmtLookupTableName(field.getName(), "importance"));
					} catch (Exception e) {
						printf(" - Error saving importance %s", rankImportance);
					}
				}
					
				if (scaleRangeOfBenefit != null) {
					try {
						proxy.setForeignKeyPrimitiveField("rangeOfBenefit", Integer.valueOf(scaleRangeOfBenefit), fmtLookupTableName(field.getName(), "rangeOfBenefit"));
					} catch (Exception e) { 
						printf(" - Error saving range of benefit %s", scaleRangeOfBenefit);
					}
				}
				
				if (subfield.hasData()) {
					if (otherKey != null) {
						String notes = getString(row, otherKey, null);
						if (notes != null) {
							Notes note = createNote(notes);
							note.setField(subfield);
							subfield.getNotes().add(note);
						}
					}
					
					proxy.setForeignKeyPrimitiveField(field.getName()+"Lookup", Integer.valueOf(entry.getFirst()), field.getName()+"Lookup");
					field.getFields().add(subfield);
				}
			}
		}

		if (field.hasData())
			curAssessment.getField().add(field);
		
		if (infoAvailable != null && !infoAvailable.booleanValue())
			addBooleanPrimitiveField(CanonicalNames.EcosystemServicesInsufficientInfo, "isInsufficient", curAssessment, true);
		
		addBooleanPrimitiveField(CanonicalNames.EcosystemServicesProvidesNone, "providesNothing", curAssessment, providesNothing);
	}
	
	private Notes createNote(String value) {
		Edit edit = new Edit("Note created via DEM Import.");
		edit.setUser(userO);
		
		Notes notes = new Notes();
		notes.setEdit(edit);
		edit.getNotes().add(notes);
		
		notes.setValue(value);
		
		return notes;
	}
//
	private void exportAssessments() throws Exception {
		session.beginTransaction();
		
		Map<Integer, Region> regionCache = new HashMap<Integer, Region>();
		
		Set<Region> regions = new HashSet<Region>();
		Set<Taxon> taxa = new HashSet<Taxon>();
		
		if (!successfulAssessments.isEmpty()) {
			printf(" - Saving %s assessments", successfulAssessments.size());
			for (Assessment assessment : successfulAssessments) {
				taxa.add(assessment.getTaxon());
				for (Integer regionID : assessment.getRegionIDs()) {
					Region region;
					if (regionCache.containsKey(regionID))
						region = regionCache.get(regionID);
					else {
						Region r = regionIO.getRegion(regionID);
						regionCache.put(regionID, r);
						region = r;
					}
						
					if (region != null)
						regions.add(region);
				}
				
				AssessmentIOWriteResult result = assessmentIO.saveNewAssessment(assessment, userO);
				if (!result.status.isSuccess())
					println("Error putting draft assessment for " + assessment.getSpeciesName());
			}
		}
		
		if (!failedAssessments.isEmpty()) {
			printf("====== Failed Assessments (%s) ======", failedAssessments.size());
			for (Assessment assessment : failedAssessments) {
				if (assessment.isGlobal())
					printf("A global draft assessment for the species %s" +  
							" already exists. Remove the species from the DEM, or delete the assessment in SIS" +
							" so the import can succeed.", assessment.getSpeciesName());
				else
					printf("A regional draft assessment for the species %s" +
							" with the region specified in the DEM already exists. Remove the species" +
							" from the DEM, or delete the assessment in SIS so the import can.", 
							assessment.getSpeciesName());
			}
			print("============");
		}
		
		WorkingSet ws = new WorkingSet();
		ws.setCreatedDate(Calendar.getInstance().getTime());
		ws.setName("DEM Import for " + userO.getUsername() + " on " + FormattedDate.impl.getDate(ws.getCreatedDate()));
		ws.setDescription(ws.getName());
		ws.getAssessmentTypes().add((AssessmentType)session.get(AssessmentType.class, AssessmentType.DRAFT_ASSESSMENT_STATUS_ID));
		ws.setCreator(userO);
		ws.getUsers().add(userO);
		ws.setIsMostRecentPublished(false);
		ws.setRelationship(Relationship.fromName(Relationship.OR));
		
		ws.setRegion(regions);
		ws.setTaxon(taxa);
		
		boolean saved = workingSetIO.saveWorkingSet(ws, userO, "Working Set Created via DEM Import");
		if (!saved)
			println("Error putting new working set!");
		
		session.getTransaction().commit();
	}

	private void exportNodes() throws Exception {
		log.append("<tr><td align=\"center\" colspan=\"9\">" + "<b><u>Newly Created Taxa</u></b></td></tr>");
		session.beginTransaction();
		for (Kingdom curKingdom : tree.getKingdoms().values()) {
			Taxon kingdom = curKingdom.getTheKingdom();
			if (kingdom.getState() == STATE_TO_IMPORT) {
				if (!allowCreateUpperLevelTaxa)
					throw new Exception("Creating taxa only allowed at the Genus level and below.");
				
				kingdom.setState(Taxon.ACTIVE);
				printf("Saving new kingdom with id %s", kingdom.getId());
				session.save(kingdom);
				logNode(kingdom);
			}

			for (HashMap<String, Taxon> curLevel : curKingdom.getLevels()) {
				for (Taxon cur : curLevel.values()) {
					if (cur.getState() == STATE_TO_IMPORT) // SUBMIT IT
					{	
						if (!allowCreateUpperLevelTaxa && cur.getLevel() < TaxonLevel.GENUS)
							throw new Exception("Creating taxa only allowed at the Genus level and below.");
						
						cur.setState(Taxon.ACTIVE);
						session.save(cur);
						//taxomaticIO.saveNewTaxon(cur, userO);
						logNode(cur);
					} else if (cur.getState() == STATE_CHANGED) {
						cur.setState(Taxon.ACTIVE);
						
						Hibernate.initialize(cur.getEdits());
						taxonIO.writeTaxon(cur, userO, "DEM Import.");
						
						/*if( ) {
							failed.set(true);
							statusMessage.append("Failed to save changes to an existing taxon, " + cur.getFullName()
									+ ".<br>");
							statusMessage.append("Please forward this message: <br>");
							statusMessage.append("Failed TaxaIO call!<br>");
							statusMessage.append("with XML " + XMLUtils.clean(curXML) + "<br>");
							return;
						}*/
					}
				}
			}
		}
		session.getTransaction().commit();
	}
//
	private void faoMarineTableImport(Long curDEMid, Assessment curAssessment) throws Exception {
		Field field = new Field(CanonicalNames.FAOOccurrence, curAssessment);
		
		parseOccurrence(field, curDEMid, "FAO_marine_list", FAO_CODING_OCCURRENCE_TYPE);
		
		if (field.hasData())
			curAssessment.getField().add(field);
	}

	private void fetchCodingOccurrenceData(Row codingRow, Field field) throws Exception {
		String p_code = "0";
		String o_code = "0";
		String m_code = "0";

		if (codingRow != null) {
			p_code = getString(codingRow, "p_code");
			o_code = getString(codingRow, "o_code");
			m_code = getString(codingRow, "m_code");

			if ("9".equals(p_code))
				p_code = "6";
			if ("9".equals(o_code))
				o_code = "5";
		}

		ArrayList<String> dataList = new ArrayList<String>();
		dataList.add(p_code);
		dataList.add(new Boolean(m_code.equals("1")).toString());
		dataList.add(o_code);
		
		modifyOccurrenceEntry(dataList);
		
		ProxyField proxy = new ProxyField(field);
		String presence = dataList.get(0), formerlyBred = dataList.get(1), origin = dataList.get(2);
		if (!"0".equals(presence))
			proxy.setForeignKeyPrimitiveField("presence", Integer.valueOf(presence), fmtLookupTableName(field.getName(), "presence"));
		if (!"0".equals(formerlyBred))
			proxy.setBooleanUnknownPrimitiveField("formerlyBred", Integer.valueOf(formerlyBred));
		if (!"0".equals(origin))
			proxy.setForeignKeyPrimitiveField("origin", Integer.valueOf(origin), fmtLookupTableName(field.getName(), "origin"));
		
		List<Integer> seasonalityValues = null;
		String seasonality = dataList.get(3);
		if (!isBlank(seasonality)) {
			seasonalityValues = new ArrayList<Integer>();
			for (String current : seasonality.split(","))
				seasonalityValues.add(Integer.valueOf(current));
		}
		proxy.setForeignKeyListPrimitiveField("seasonality", seasonalityValues, fmtLookupTableName(field.getName(), "seasonality"));
	}
	
	private void modifyOccurrenceEntry(ArrayList<String> curSelected) {
		curSelected.ensureCapacity(4);

		String presenceCode = curSelected.get(0);
		String passageMigrant = curSelected.get(1);
		String origin = curSelected.get(2);

		String seasonality = "";

		if (!presenceCode.equals("") && !presenceCode.equals("0")) {
			int pCode = Integer.valueOf(presenceCode);
			if (pCode <= 3) {
				curSelected.set(0, "1");

				if (pCode == 1)
					seasonality += "1,";
				else if (pCode == 2)
					seasonality += "2,";
				else if (pCode == 3)
					seasonality += "3,";
			} else if (pCode == 4)
				curSelected.set(0, "2");
			else if (pCode == 5)
				curSelected.set(0, "3");
			else if (pCode == 6)
				curSelected.set(0, "4");
		} else {
			curSelected.set(0, "0");
		}

		//Formerly bred is unseleted
		curSelected.set(1, "0");

		if (passageMigrant.equals("true"))
			seasonality += "4";

		if (!origin.equals("") && !origin.equals("0")) {
			int oCode = Integer.valueOf(origin);

			if (oCode == 1)
				curSelected.set(2, "1");
			else if (oCode == 2)
				curSelected.set(2, "3");
			else if (oCode == 3)
				curSelected.set(2, "2");
			else if (oCode == 4)
				curSelected.set(2, "5");
			else if (oCode == 5)
				curSelected.set(2, "6");
			else if (oCode == 9) // This shouldn't be in there, but somehow a
				// few are...
				curSelected.set(2, "6");
		} else
			curSelected.set(2, "0");

		if (seasonality.endsWith(","))
			seasonality = seasonality.substring(0, seasonality.length() - 1);

		curSelected.add(seasonality);
	}

	private Taxon fetchNode(String kingdomName, String fullName) throws Exception {
		Taxon existing = taxonIO.readTaxonByName(kingdomName, fullName, null, false);
		if (existing != null)
			printf("Found existing taxon %s with id %s", existing.getFullName(), existing.getId());
		return existing;
	}
	
	private void growthFormTableImport(Long curDEMid, Assessment curAssessment) throws DBException {
		SelectQuery select = new SelectQuery();
		select.select("growth_form_table", "*");
		select.constrain(new CanonicalColumnName("growth_form_table", "Sp_code"), QConstraint.CT_EQUALS, curDEMid);

		Row.Set rowLoader = new Row.Set();
		ec.doQuery(select, rowLoader);

		if (rowLoader == null)
			return;
		
		Field field = new Field(CanonicalNames.PlantGrowthForms, curAssessment);
		
		for (Row row : queryDEM("growth_form_table", curDEMid)) {
			String curCode = getString(row, "Growthform_code");
			
			Integer index = getIndex(field.getName()+"Lookup", curCode, null);
			if (index == null)
				continue;
			
			Field subfield = new Field(field.getName()+"Subfield", null);
			subfield.setParent(field);
			
			ProxyField proxy = new ProxyField(subfield);
			proxy.setForeignKeyPrimitiveField(field.getName()+"Lookup", index);
			
			field.getFields().add(subfield);
		}

		if (field.hasData())
			curAssessment.getField().add(field);
	}
	
	private String fmtLookupTableName(String field, String dataPoint) {
		return field + "_" + dataPoint + "Lookup";
	}
//
	private void habitatTableImport(Long curDEMid, Assessment curAssessment) throws DBException {
		List<Row> rows = queryDEM("General_habitat", curDEMid);
		if (rows.isEmpty())
			return;

		Field field = new Field(CanonicalNames.GeneralHabitats, curAssessment);
		
		for (Row curRow : rows) {
			String fieldName = CanonicalNames.GeneralHabitats + "Subfield";
			String curCode = getString(curRow, "Gh_N", "");
			
			Integer lookup = getIndex(field.getName()+"Lookup", curCode, null);
			if (lookup == null) {
				printf("No lookup to match habitat %s", curCode);
				continue;
			}
			
			Integer majorImportance = curRow.get("major_importance").getInteger();
			Integer suitability = curRow.get("Score").getInteger();

			if (suitability != null && suitability == 9)
				suitability = 3; // 3 is index of "possible" answer

			if (majorImportance != null) {
				if (majorImportance == 1)
					majorImportance = null;
				else if (majorImportance > 0) // 3 is index of "No", 2 of "Yes"
					majorImportance--;
			}
			
			Field subfield = new Field(fieldName, null);
			
			ProxyField proxy = new ProxyField(subfield);
			proxy.setForeignKeyPrimitiveField(CanonicalNames.GeneralHabitats + "Lookup", lookup, CanonicalNames.GeneralHabitats + "Lookup");
			proxy.setForeignKeyPrimitiveField("suitability", suitability, fmtLookupTableName(CanonicalNames.GeneralHabitats, "suitability"));
			proxy.setForeignKeyPrimitiveField("majorImportance", majorImportance, fmtLookupTableName(CanonicalNames.GeneralHabitats, "majorImportance"));

			subfield.setParent(field);
			field.getFields().add(subfield);
		}

		curAssessment.getField().add(field);
	}
//
	private boolean isValidRangeFormat(String text) {
		boolean ret = text.equals("")
				|| text.matches("(\\s)*(\\d)*(\\.)?(\\d)+(\\s)*((-)((\\s)*(\\d)*(\\.)?(\\d)+))?")
				|| text.matches("(\\s)*(\\d)*(\\.)?(\\d)+(\\s)*(-)(\\s)*(\\d)*(\\.)?(\\d)+(\\s)*"
						+ "(,)(\\s)*(\\d)*(\\.)?(\\d)+(\\s)*((-)((\\s)*(\\d)*(\\.)?(\\d)+))?");

		// SysDebugger.getInstance().println("Checking against text " + text +
		// " : result is " + ret );

		return ret;
	}
//
//	private void lakesTableImport(Long curDEMid, AssessmentData curAssessment, HashMap<String, Object> data)
//			throws DBException {
//		SelectQuery select = new SelectQuery();
//		select.select("lakes_table", "*");
//		select.constrain(new CanonicalColumnName("lakes_table", "Sp_code"), QConstraint.CT_EQUALS, curDEMid);
//
//		Row.Set rowLoader = new Row.Set();
//		ec.doQuery(select, rowLoader);
//
//		if (rowLoader == null)
//			return;
//
//		HashMap<String, ArrayList<String>> selected = new HashMap<String, ArrayList<String>>();
//		for (Iterator<Row> iter = rowLoader.getSet().listIterator(); iter.hasNext();) {
//			Row curRow = iter.next();
//
//			String curCode = curRow.get("lake_number").getString();
//
//			ArrayList<String> dataList = new ArrayList<String>();
//			dataList.add("0");
//			dataList.add("false");
//			dataList.add("0");
//
//			selected.put(curCode, dataList);
//		}
//
//		data.put(CanonicalNames.Lakes, selected);
//	}
//
	private void landCoverTableImport(Long curDEMid, Assessment curAssessment) throws DBException {
		Field field = new Field(CanonicalNames.LandCover, curAssessment);
		
		for (Row row : queryDEM("Land_cover", curDEMid)) {
			String curCode = getString(row, "Lc_N", null);
			Integer score = row.get("Score").getInteger();
			if (curCode == null || score == null)
				continue;
			
			Integer index = getIndex(field.getName()+"Lookup", curCode, null);
			if (index == null)
				continue;

			if (score == 9)
				score = 4; // 3 is index of possible answer

			Field subfield = new Field(field.getName()+"Subfield", null);
			subfield.setParent(field);
			
			ProxyField proxy = new ProxyField(subfield);
			proxy.setForeignKeyPrimitiveField(field.getName()+"Lookup", index, field.getName()+"Lookup");
			proxy.setForeignKeyPrimitiveField("suitability", score, fmtLookupTableName(field.getName(), "suitability"));
			
			field.getFields().add(subfield);
		}
		
		if (field.hasData())
			curAssessment.getField().add(field);
	}
	
	private void lifeHistoryTableImport(Long curDEMid, Assessment curAssessment) throws DBException {
		List<Row> rows = queryDEM("life_history", curDEMid);
		if (rows.isEmpty())
			return;
		
		Row row = rows.get(0);
		// AGE MATURITY UNITS - FOR BOTH FEMALE AND MALE
		String maturityUnits = getString(row, "age_maturity_units");
		if (isBlank(maturityUnits))
			maturityUnits = "0";
		else if (maturityUnits.equalsIgnoreCase("days"))
			maturityUnits = "1";
		else if (maturityUnits.equalsIgnoreCase("weeks"))
			maturityUnits = "2";
		else if (maturityUnits.equalsIgnoreCase("months"))
			maturityUnits = "3";
		else if (maturityUnits.equalsIgnoreCase("years"))
			maturityUnits = "4";
		else {
			printf("Unparseable units value from DEM import for Female & Male maturity age. Imported value: %s", maturityUnits);
		}
		
		Map<String, String> maturityAge = new HashMap<String, String>();
		maturityAge.put(CanonicalNames.FemaleMaturityAge, "f_age_maturity");
		maturityAge.put(CanonicalNames.MaleMaturityAge, "m_age_maturity");
		
		for (Map.Entry<String, String> entry : maturityAge.entrySet()) {
			String age = getString(row, entry.getValue());
			if (!isBlank(age)) {
				Field field = new Field(entry.getKey(), curAssessment);
				ProxyField proxy = new ProxyField(field);
				proxy.setStringPrimitiveField("age", age);
				if (!isBlank(maturityUnits))
					proxy.setForeignKeyPrimitiveField("units", Integer.valueOf(maturityUnits), fmtLookupTableName(entry.getKey(), "units"));
				
				curAssessment.setField(field);
			}
		}
		
		List<Triple<String, String, String>> simpleStringFields = new ArrayList<Triple<String,String,String>>();
		simpleStringFields.add(new Triple<String, String, String>(CanonicalNames.AvgAnnualFecundity, "fecundity", "litter_size"));
		simpleStringFields.add(new Triple<String, String, String>(CanonicalNames.NaturalMortality, "value", "mortality"));
		simpleStringFields.add(new Triple<String, String, String>(CanonicalNames.ReproductivePeriodicity, "value", "reproductive_periodicity"));
		simpleStringFields.add(new Triple<String, String, String>(CanonicalNames.FemaleMaturitySize, "size", "f_size_maturity"));
		simpleStringFields.add(new Triple<String, String, String>(CanonicalNames.MaleMaturitySize, "size", "m_size_maturity"));
		simpleStringFields.add(new Triple<String, String, String>(CanonicalNames.MaxSize, "size", "max_size"));
		simpleStringFields.add(new Triple<String, String, String>(CanonicalNames.BirthSize, "size", "birth_size"));
		
		for (Triple<String, String, String> value : simpleStringFields)
			addStringPrimitiveField(value.getFirst(), value.getSecond(), curAssessment, getString(row, value.getThird()));
		
		// GENERATION LENGTH
		String generations = row.get("Gen_len").getString(Column.NEVER_NULL);
		String genJust = XMLUtils.clean(row.get("Gen_Len_Just").getString(Column.NEVER_NULL));
		
		String mod_generations = generations.toLowerCase();
		mod_generations = mod_generations.replaceAll("\\s", "");
		mod_generations = mod_generations.replaceAll("years", "");
		mod_generations = mod_generations.replaceAll("days", "");
		mod_generations = mod_generations.replaceAll("weeks", "");
		mod_generations = mod_generations.replaceAll("months", "");

		String leftovers = generations.replace(mod_generations, "");
		if( !leftovers.replaceAll("\\s", "").equals("") )
			genJust = XMLUtils.clean(leftovers.trim()) + (genJust.equals("") ? "" : " -- " + genJust);
		
		if (!isValidRangeFormat(mod_generations))
			printf(CANNED_NOTE + generations + " with justification: " + genJust);
		else {
			Field field = new Field(CanonicalNames.GenerationLength, curAssessment);
			ProxyField proxy = new ProxyField(field);
			proxy.setRangePrimitiveField("range", mod_generations);
			proxy.setStringPrimitiveField("justification", genJust);
			
			curAssessment.setField(field);
		}
		
		addTextPrimitiveField(CanonicalNames.PopulationIncreaseRate, "narrative", curAssessment, getString(row, "rate_pop_increase"));
		
		List<Triple<String, String, String>> booleans = new ArrayList<Triple<String,String,String>>();
		booleans.add(new Triple<String, String, String>(CanonicalNames.EggLaying, "egg_laying", "layEggs"));
		booleans.add(new Triple<String, String, String>(CanonicalNames.FreeLivingLarvae, "larval", "hasStage"));
		booleans.add(new Triple<String, String, String>(CanonicalNames.LiveBirth, "live_young", "liveBirth"));
		booleans.add(new Triple<String, String, String>(CanonicalNames.Parthenogenesis, "parthenogenesis", "exhibitsParthenogenesis"));
		booleans.add(new Triple<String, String, String>(CanonicalNames.WaterBreeding, "water_breeding", "value"));
		
		for (Triple<String, String, String> value : booleans) {
			String rawValue = getString(row, value.getSecond());
			if (!isBlank(rawValue)) {
				Integer dataValue = isChecked(row, value.getSecond()) ? BooleanUnknownPrimitiveField.YES : BooleanUnknownPrimitiveField.NO;
				addBooleanUnknownPrimitiveField(value.getFirst(), value.getThird(), curAssessment, dataValue);
			}
		}
		
		Map<String, Triple<String, String, String>> fieldAndUnits = new HashMap<String, Triple<String, String, String>>();
		fieldAndUnits.put(CanonicalNames.Longevity, new Triple<String, String, String>("longevity_units", "longevity", "longevity"));
		fieldAndUnits.put(CanonicalNames.AvgReproductiveAge, new Triple<String, String, String>("ave_reproductive_age_units", "ave_reproductive_age", "age"));
		fieldAndUnits.put(CanonicalNames.GestationTime, new Triple<String, String, String>("gestation_units", "gestation", "time"));

		
		for (Map.Entry<String, Triple<String, String, String>> entry : fieldAndUnits.entrySet()) {
		// LONGEVITY
			String units = getString(row, entry.getValue().getFirst());
			if (isBlank(units))
				units = "0";
			else if (units.equalsIgnoreCase("days"))
				units = "1";
			else if (units.equalsIgnoreCase("months"))
				units = "2";
			else if (units.equalsIgnoreCase("years"))
				units = "3";
			else {
				printf("Unparseable units value from DEM import on %s.%s. Imported value: %s", 
						entry.getKey(), entry.getValue().getFirst(), units);
			}
	
			String value = getString(row, entry.getValue().getSecond());
			if (!isBlank(value)) {
				Field field = new Field(entry.getKey(), curAssessment);
				ProxyField proxy = new ProxyField(field);
				proxy.setStringPrimitiveField(entry.getValue().getThird(), value);
				if (!isBlank(units))
					proxy.setForeignKeyPrimitiveField("units", Integer.valueOf(units), fmtLookupTableName(entry.getKey(), "units"));
				
				curAssessment.setField(field);
			}
		}
	}

	private void livelihoodsTableImport(Long curDEMid, Assessment curAssessment) throws DBException {
		List<Row> rows = queryDEM("Livelihoods", curDEMid);
		if (rows.isEmpty())
			return;

		Field field = new Field(CanonicalNames.Livelihoods, curAssessment);
		
		for (Row row : rows) {
			Field subfield = new Field(field.getName() + "Subfield", null);
			subfield.setParent(field);
			field.getFields().add(subfield);
			
			LivelihoodsField proxy = new LivelihoodsField(subfield);
			proxy.setScale(row.get("Assess_type_ID").getInteger());
			proxy.setLocalityName(getString(row, "Assess_name", null));
			try {
				proxy.setDate(row.get("Assess_date").getDate());
			} catch (Exception e) { }
			
			proxy.setProductDescription(getString(row, "p_product", null));
			proxy.setAnnualHarvest(getString(row, "p_Single_harvest_amount", null));
			try {
				proxy.setAnnualHarvestUnits(Integer.valueOf(getString(row, "p_Single_harvest_amount_unit", null)));
			} catch (Exception e) { }
			
			proxy.setMultiSpeciesHarvest(getString(row, "p_Multi_harvest_amount", null));
			try {
				proxy.setMultiSpeciesHarvestUnits(Integer.valueOf(getString(row, "p_Multi_harvest_amount_unit", null)));
			} catch (Exception e) { }
			
			proxy.setPercentInHarvest(getString(row, "p_harvest_percent", null));
			proxy.setAmountInHarvest(getString(row, "p_Multi_amount", null));
			
			try {
				proxy.setHumanReliance(Integer.valueOf(getString(row, "p_human_reliance", null)));
			} catch (Exception e) { }
			
			try {
				proxy.setGenderAge(Integer.valueOf(getString(row, "p_harvest_gender", null)));
			} catch (Exception e) { }
			
			try {
				proxy.setSocioEconomic(Integer.valueOf(getString(row, "p_harvest_socioeconomic", null)));
			} catch (Exception e) { }
			
			proxy.setOther(getString(row, "p_other_harvest_socioeconomic", null));
			
			try {
				proxy.setTotalPopulationBenefit(Integer.valueOf(getString(row, "p_involve_percent", null)));
			} catch (Exception e) { }
			
			try {
				proxy.setHouseholdConsumption(Integer.valueOf(getString(row, "p_household_consumption_percent", null)));
			} catch (Exception e) { }
			
			try {
				proxy.setHouseholdIncome(Integer.valueOf(getString(row, "p_household_income_percent", null)));
			} catch (Exception e) { }
			
			proxy.setAnnualCashIncome(getString(row, "p_cash_income", null));
		}
		
		ProxyField proxy = new ProxyField(field);
		if (!field.getFields().isEmpty())
			proxy.setBooleanPrimitiveField("noInfo", Boolean.TRUE, Boolean.FALSE);

		curAssessment.getField().add(field);
	}

	private void lmeTableImport(Long curDEMid, Assessment curAssessment)
			throws Exception {
		Field field = new Field(CanonicalNames.LargeMarineEcosystems, curAssessment);
		
		parseOccurrence(field, curDEMid, "large_marine_ecosystems_list", LME_CODING_OCCURRENCE_TYPE);
		
		if (field.hasData())
			curAssessment.getField().add(field);
	}

	private void logAsNewCell(String string) {
		log.append("<td>" + string + "</td>");
	}

	private void logNode(Taxon cur) {
		log.append("<tr>");
		logAsNewCell(Integer.toString(cur.getLevel()));
		logAsNewCell(cur.getFullName());
		for (int j = 0; j < TaxonLevel.INFRARANK; j++)
			logAsNewCell(j < cur.getFootprint().length ? cur.getFootprint()[j] : "&nbsp");
		log.append("</tr>");
	}
	
	private boolean isChecked(Row row, String key) {
		try {
			return "Y".equals(row.get(key).toString()) || 
				"1".equals(row.get(key).toString());
		} catch (NullPointerException e) {
			return false;
		}
	}
	
	private String getString(Row row, String key) {
		return getString(row, key, "");
	}
	
	private String getString(Row row, String key, String defaultValue) {
		Column column = row.get(key);
		if (column == null || column.isEmpty())
			return defaultValue;
		
		String value = column.toString();
		if (isBlank(value))
			value = defaultValue;
		
		return value;
	}

	private void populationTableImport(Long curDEMid, Assessment curAssessment) throws DBException {
		List<Row> rows = queryDEM("Population", curDEMid);
		if (rows.isEmpty())
			return;
		
		Row row = rows.get(0);
		
		String max = getString(row, "Max_population");
		String min = getString(row, "Min_population");

		max = max.replaceAll(",", "").replaceAll("\\s", "");
		min = min.replaceAll(",", "").replaceAll("\\s", "");

		// IF EITHER ARE "", WE DON'T NEED A COMMA - WE'LL TREAT IT AS A BEST
		// GUESS
		String minMax = min + (min.equals("") || max.equals("") ? "" : "-") + max;

		if (!isValidRangeFormat(minMax)) {
			printf("Unparseable population values from DEM import. Minimum value from DEM: " + min
					+ " --- maximum value from DEM: " + max);
		} else {
			addRangePrimitiveField(CanonicalNames.PopulationSize, "range", curAssessment, minMax);
		}
	}
	
	private boolean isBlank(String value) {
		return value == null || "".equals(value);
	}
	
	private void redListingTableImport(Long curDEMid, Assessment curAssessment) throws DBException {
		List<Row> rows = queryDEM("red_listing", curDEMid);
		if (rows.isEmpty())
			return;
		
		//Pretty sure there's only one row, but...
		for (Row curRow : rows) {
			{
				Field redListCriteria = new Field(CanonicalNames.RedListCriteria, curAssessment);
				RedListCriteriaField proxy = new RedListCriteriaField(redListCriteria);
	
				String cat = curRow.get("rl_category").toString();
				String crit = curRow.get("rl_criteria").toString();
				
				if (!isBlank(cat) || !isBlank(crit)) {
					proxy.setManual(true);
					proxy.setManualCategory(cat);
					proxy.setManualCriteria(crit);
				}
				
				String dateLastSeen = getString(curRow, "last_seen", null);
				if (dateLastSeen != null)
					proxy.setYearLastSeen(dateLastSeen);
				
				/*
				 * PEC in it's old form has essentially been removed in SIS 2
				 */
				/*
				dataList.remove(SISCategoryAndCriteria.POSSIBLY_EXTINCT_CANDIDATE_INDEX);
				dataList.add(SISCategoryAndCriteria.POSSIBLY_EXTINCT_CANDIDATE_INDEX, ""
						+ (curRow.get("Poss_extinct_Cand").getInteger() == 1));*/
	
				proxy.setPossiblyExtinct(Integer.valueOf(1).equals(curRow.get("Poss_extinct").getInteger()));
	
				if (redListCriteria.hasData())
					curAssessment.getField().add(redListCriteria);
			}

			String ratValue = curRow.get("rl_rationale").toString();
			if (!isBlank(ratValue)) {
				ratValue = XMLUtils.clean(ratValue);
				ratValue = Replacer.replace(ratValue, "\n", "<br/>");
				ratValue = Replacer.replace(ratValue, "\r", "");
				Field rationale = new Field(CanonicalNames.RedListRationale, curAssessment);
				
				ProxyField proxy = new ProxyField(rationale);
				proxy.setTextPrimitiveField("value", ratValue);
				
				curAssessment.getField().add(rationale);
				
			}

			// CHANGE REASON
			int changeReason = 0;
			Integer genuineChangeReason = null;
			List<Integer> nonGenuineSelection = null;
			String nonGenuineOtherText = null;
			Integer noChangeReason = null;

			if (isChecked(curRow, "genuine_change")) {
				changeReason = 1;

				if (isChecked(curRow, "genuine_recent"))
					genuineChangeReason = 1;
				else if (isChecked(curRow, "genuine_sincefirst"))
					genuineChangeReason = 2;
			} else if (curRow.get("nongenuine_change").getInteger() == 1) {
				changeReason = 2;
				
				nonGenuineSelection = new ArrayList<Integer>();
				int index = 1;
				for (String key : new String[] {"knowledge_new", "Knowledge_criteria", 
						"Knowledge_correction", "Taxonomy", "Knowledge_criteria", "Other"}) {
					if (isChecked(curRow, key))
						nonGenuineSelection.add(index);
					index++;
				}
			} else if (isChecked(curRow, "no_change")) {
				changeReason = 3;

				if (isChecked(curRow, "same"))
					noChangeReason = 1;
				else if (isChecked(curRow, "criteria_change"))
					noChangeReason = 2;
			}
			
			if (changeReason > 0) {
				Field field = new Field(CanonicalNames.RedListReasonsForChange, curAssessment);
				ProxyField proxy = new ProxyField(field);
				proxy.setForeignKeyPrimitiveField("type", changeReason, fmtLookupTableName(CanonicalNames.RedListReasonsForChange, "type"));
				proxy.setForeignKeyPrimitiveField("timeframe", genuineChangeReason, fmtLookupTableName(CanonicalNames.RedListReasonsForChange, "timeframe"));
				proxy.setForeignKeyListPrimitiveField("changeReasons", nonGenuineSelection, fmtLookupTableName(CanonicalNames.RedListReasonsForChange, "changeReasons"));
				proxy.setStringPrimitiveField("otherReason", nonGenuineOtherText);
				proxy.setForeignKeyPrimitiveField("catCritChanges", noChangeReason, fmtLookupTableName(CanonicalNames.RedListReasonsForChange, "catCritChanges"));
				
				curAssessment.getField().add(field);
			}

			// Population trend
			String populationTrend = curRow.get("rl_trend").toString();
			if (!isBlank(populationTrend)) {
				Integer populationTrendSelected = getIndex(CanonicalNames.PopulationTrend, "value", populationTrend, null);
				if (populationTrendSelected != null) {
					Field field = new Field(CanonicalNames.PopulationTrend, curAssessment);
					
					ProxyField proxy = new ProxyField(field);
					proxy.setForeignKeyPrimitiveField("value", populationTrendSelected, fmtLookupTableName(CanonicalNames.PopulationTrend, "value"));
					
					curAssessment.getField().add(field);
				}
			}
			
			String rlAsmDate = curRow.get("assess_date").toString();
			if (!isBlank(rlAsmDate)) {
				try {
					addDatePrimitiveField(CanonicalNames.RedListAssessmentDate, "value", 
						curAssessment, curRow.get("assess_date").getDate());
				} catch (Exception e) { }
			}

			// Red List Notes
			addTextPrimitiveField(CanonicalNames.RedListNotes, "value", curAssessment, 
				XMLUtils.clean(curRow.get("Notes").toString()));

			Map<String, String> creditedUsers = new HashMap<String, String>();
			creditedUsers.put(CanonicalNames.RedListAssessors, "Assessors");
			creditedUsers.put(CanonicalNames.RedListEvaluators, "Evaluator");
			
			for (Map.Entry<String, String> entry : creditedUsers.entrySet()) {
				String value = curRow.get(entry.getValue()).toString();
				if (!isBlank(value)) {
					Field field = new Field(entry.getKey(), curAssessment);
					RedListCreditedUserField proxy = new RedListCreditedUserField(field);
					proxy.setText(value);
					
					curAssessment.getField().add(field);
				}
			}

			// Locations
			addRangePrimitiveField(CanonicalNames.LocationsNumber, "range", curAssessment, curRow.get("Number_locations").toString());

//			Generation length - DO NOT USE GEN_LENGTH FROM THIS TABLE!!!!!!!!!!!!!!!!!!!!!!
			//Per e-mail from Jim Ragle, June 9th, 2009

			// Mature individuals
			String matureIndividuals = curRow.get("Number_mat_ind").toString();
			if (!isBlank(matureIndividuals)) {
				if (!isValidRangeFormat(matureIndividuals)) {
					printf("Invalid range format: " + matureIndividuals);
					/*sendNote(curAssessment.getType(), CANNED_NOTE + matureIndividuals, curAssessment.getAssessmentID(),
							CanonicalNames.MaleMaturitySize);
					sendNote(curAssessment.getType(), CANNED_NOTE + matureIndividuals, curAssessment.getAssessmentID(),
							CanonicalNames.FemaleMaturitySize);*/
				} else {
					addStringPrimitiveField(CanonicalNames.FemaleMaturitySize, "size", curAssessment, matureIndividuals);
					addStringPrimitiveField(CanonicalNames.MaleMaturitySize, "size", curAssessment, matureIndividuals);
				}
			}

			addStringPrimitiveField(CanonicalNames.OldDEMPastDecline, "value", curAssessment, getString(curRow, "past_decline", null));
			addStringPrimitiveField(CanonicalNames.OldDEMPeriodPastDecline, "value", curAssessment, getString(curRow, "period_past_decline", null));
			addStringPrimitiveField(CanonicalNames.OldDEMFutureDecline, "value", curAssessment, getString(curRow, "future_decline", null));
			addStringPrimitiveField(CanonicalNames.OldDEMPeriodFutureDecline, "value", curAssessment, getString(curRow, "period_future_decline"));
			
			addBooleanRangePrimitiveField(CanonicalNames.SevereFragmentation, "isFragmented", curAssessment, getString(curRow, "severely_frag", null));
		}
	}
	

	private void referencesImport(Long curDEMid, Assessment curAssessment) {
		// query the DEM bibliographic_original_records that go with this assessment
		SelectQuery select = new SelectQuery();
		select = new SelectQuery();
		select.select("bibliographic_original_records", "*");
		select.join("bibliography_link", new QRelationConstraint(new CanonicalColumnName("bibliography_link",
				"Bibliography_number"), new CanonicalColumnName("bibliographic_original_records", "Bib_Code")));
		select.constrain(new CanonicalColumnName("bibliography_link", "Sp_code"), QConstraint.CT_EQUALS, curDEMid);
		try {
			// build a single document containing all the references for this
			// assessment
			ElementalReferenceRowProcessor errp = new ElementalReferenceRowProcessor(session);
			ec.doQuery(select, errp);
			
			curAssessment.getReference().addAll(errp.getReferences());
		} catch (Throwable e) {
			printf("Error: %s", e);
		}
	}

	/*
	 * UTILITY FUNCTIONS
	 */

//	private void riversTableImport(Long curDEMid, AssessmentData curAssessment, HashMap<String, Object> data)
//			throws DBException {
//		SelectQuery select = new SelectQuery();
//		select.select("rivers_table", "*");
//		select.constrain(new CanonicalColumnName("rivers_table", "Sp_code"), QConstraint.CT_EQUALS, curDEMid);
//
//		Row.Set rowLoader = new Row.Set();
//		ec.doQuery(select, rowLoader);
//
//		if (rowLoader == null)
//			return;
//
//		HashMap<String, ArrayList<String>> selected = new HashMap<String, ArrayList<String>>();
//		for (Iterator<Row> iter = rowLoader.getSet().listIterator(); iter.hasNext();) {
//			Row curRow = iter.next();
//
//			String curCode = curRow.get("river_number").getString();
//
//			ArrayList<String> dataList = new ArrayList<String>();
//			dataList.add("0");
//			dataList.add("false");
//			dataList.add("0");
//
//			selected.put(curCode, dataList);
//		}
//
//		data.put(CanonicalNames.Rivers, selected);
//	}

	

//	private void sendNote(String assessmentType, String noteBody, String assessmentID, String canonicalName) {
//		Note note = new Note();
//		note.setBody(noteBody);
//		note.setCanonicalName(canonicalName);
//		note.setUser("DEMimport");
//		note.setDate(DateFormatUtils.ISO_DATE_FORMAT.format(new Date()));
//
//		String url = uriPrefix + "/notes/" + assessmentType + "/" + assessmentID + "/" + canonicalName;
//
//		try {
//			Request request = new Request(Method.POST , url, new StringRepresentation(note.toXML(), MediaType.TEXT_XML, null,
//					CharacterSet.UTF_8));
//			Response response = context.getClientDispatcher().handle(request);
//
//			if (!response.getStatus().isSuccess())
//				SysDebugger.getInstance().println("Failure response from Notes server.");
//		} catch (Exception e) {
//			e.printStackTrace();
//			SysDebugger.getInstance().println("Error noting unparseable " + canonicalName + " data.");
//		}
//	}

	private void systematicsTableImport(Long curDEMid, Assessment curAssessment) throws DBException {
		List<Row> rows = queryDEM("Systematics", curDEMid);
		if (rows.isEmpty())
			return;
		
		Row row = rows.get(0);

		try {
			curAssessment.setDateAssessed(new Date(Long.valueOf(row.get("date_added").toString())));
		} catch (NullPointerException e) {
			TrivialExceptionHandler.ignore(this, e);
		} catch (NumberFormatException e) {
			TrivialExceptionHandler.ignore(this, e);
		}
		// curAssessment.setDateModified(row.get("date_modified").getString(
		// Column.NEVER_NULL));

		Boolean global = "global".equals(row.get("Assessments").toString());
		Boolean endemic = Boolean.FALSE;
		
		String regionName = "Global";

		if (!global.booleanValue()) {
			endemic = Integer.valueOf(1).equals(row.get("Endemic_region").getInteger());

			Integer region = row.get("Region").getInteger();
			if (region != null) {
				String foundRegion = searchLibrary(CanonicalNames.RegionInformation, getString(row, "Region"));
				if (foundRegion != null)
					regionName = foundRegion;
			}
		}

		if (!regionName.equalsIgnoreCase("Global")) {
			// Check to make sure the region exists and get its proper ID
			RegionCriteria criteria = new RegionCriteria(session);
			criteria.name.ilike(regionName);
			
			Region region = null;
			try {
				region = criteria.uniqueRegion();
			} catch (Exception e) {
				statusMessage.append("Could not properly create a Regional assessment for " + "region named "
						+ regionName + ". Please report this error and include "
						+ "the DEM you are attempting to import.");
			}
			
			if (region != null) {
				curAssessment.setRegions(Arrays.asList(region), endemic);
				/*try {
					data.put(CanonicalNames.RegionInformation, createDataArray("true", region.getId(), endemic.toString()));
				} catch (IOException e) {
					e.printStackTrace();
					failed.set(true);
					statusMessage.append("Could not properly create a Regional assessment for " + "region named "
							+ regionName + ". Please report this error and include "
							+ "the DEM you are attempting to import.");
				}*/
			}
		} else
			curAssessment.setRegions(Arrays.asList(Region.getGlobalRegion()), endemic);
		
		Map<String, String> docFields = new HashMap<String, String>();
		docFields.put(CanonicalNames.ConservationActionsDocumentation, "cons_measures");
		docFields.put(CanonicalNames.ThreatsDocumentation, "threats_info");
		docFields.put(CanonicalNames.HabitatDocumentation, "habitat");
		docFields.put(CanonicalNames.PopulationDocumentation, "population");
		docFields.put(CanonicalNames.RangeDocumentation, "range");
		docFields.put(CanonicalNames.TaxonomicNotes, "notes");
		
		for (Map.Entry<String, String> entry : docFields.entrySet()) {
			String value = null;
			try {
				value = row.get(entry.getValue()).toString();
			} catch (NullPointerException e) {
				continue;
			}
			
			if (value != null && !"".equals(value)) {
				if (!CanonicalNames.TaxonomicNotes.equals(entry.getKey()))
					value = FormattingStripper.stripText(value);
				value = Replacer.replace(value, "\n", "<br/>");
				value = Replacer.replace(value, "\r", "");
				
				Field field;
				try {
					field = generator.getField(entry.getKey());
				} catch (Exception e) {
					printf("No field found in schema for %s", entry.getKey());
					continue;
				}
				
				String prim = field.getPrimitiveField().iterator().next().getName();
				
				ProxyField proxy = new ProxyField(field);
				proxy.setTextPrimitiveField(prim, value);
				
				field.setAssessment(curAssessment);
				curAssessment.getField().add(field);
			}
			
		}
	}
	
	private Row findThreatID(String code) {
		Row.Loader rl = new Row.Loader();
		try {
			SelectQuery conversionSelect = new SelectQuery();
			conversionSelect.select("Threat crosswalking", "*");
			conversionSelect.constrain(new CanonicalColumnName("Threat crosswalking", "oldthreat_id"),
					QConstraint.CT_EQUALS, code);

			demConversion.doQuery(conversionSelect, rl);
		} catch (Exception e) {
			printf("Error checking against conversion table.");
			return null;
		}
		
		return rl.getRow();
	}

	private void threatTableImport(Long curDEMid, Assessment curAssessment) throws DBException {
		List<Row> rows = queryDEM("threat_table", curDEMid);
		if (rows.isEmpty())
			return;
		
		Field field = new Field(CanonicalNames.Threats, curAssessment);
		
		Map<Integer, Field> data = new HashMap<Integer, Field>();
		
		for (Row row : rows) {
			String curCode = getString(row, "Threat_code");
			Integer curTiming = row.get("threat_timing").getInteger();
			if (curTiming == null)
				curTiming = 0;

			String stress1 = null;
			String stress2 = null;
			String notes = "";
			
			Row conversionRow = findThreatID(curCode);
			if (conversionRow != null) {
				curCode = getString(conversionRow, "newthreat_id", null);
				stress1 = getString(conversionRow, "stress 1", null);
				stress2 = getString(conversionRow, "stress 2", null);
				notes = XMLUtils.clean(getString(conversionRow, "comment"));
			}
			
			Integer index = getIndex(CanonicalNames.Threats + "Lookup", curCode, null);
			if (index == null) {
				printf("No threat found for code %s, skipping...", curCode);
				//TODO: throw failure exception?
				continue;
			}
			
			if (data.containsKey(index)) {
				Field subfield = data.get(index);
				ThreatsSubfield proxy = new ThreatsSubfield(subfield);
				
				Integer existingTiming = proxy.getTiming();
				if (existingTiming != null) {
					if (curTiming == 2) {
						proxy.setTiming(2);
					}
					else if (curTiming == 1 && existingTiming == 3) {
						//Past, unlikely to return, future
						proxy.setTiming(1);
					}
					else if (curTiming == 3 && existingTiming == 1)
						proxy.setTiming(1);
				}
			}
			else {
				Field subfield = new Field();
				subfield.setName(CanonicalNames.Threats + "Subfield");
				subfield.setParent(field);
				field.getFields().add(subfield);
				
				ThreatsSubfield proxy = new ThreatsSubfield(subfield);
				proxy.setThreat(index);
				proxy.setTiming(curTiming);
				
				if (!isBlank(notes)) {
					Edit edit = new Edit();
					edit.setUser(userO);
					edit.setReason("Threat created via DEM Import.");
					
					Notes note = new Notes();
					note.setValue(notes);
					
					note.setEdit(edit);
					edit.getNotes().add(note);
					
					note.setField(subfield);
					subfield.getNotes().add(note);
				}
				
				List<Integer> stresses = new ArrayList<Integer>();
				for (String stress : new String[] { stress1, stress2 }) {
					if (!isBlank(stress) && !"0".equals(stress)) {
						Integer stressIndex = getIndex("StressesLookup", stress, null);
						if (stressIndex != null)
							stresses.add(stressIndex);
					}
				}
				proxy.setStresses(stresses);
				
				data.put(index, subfield);
			}
		}

		curAssessment.getField().add(field);
	}
//
	private void updateFootprint(ArrayList<String> footprint, Taxon taxon) {
		// Ensure footprint is on par with what the nodes say, not what the DEM
		// says
		footprint.clear();
		footprint.addAll(Arrays.asList(taxon.getFootprint()));
		footprint.add(taxon.getName());
	}
//
//	/**
//	 * Tables used: Source_of_specimens_table purpose_table
//	 * removed_from_wild_table
//	 */
//	@SuppressWarnings(value = "unchecked")
	private void useTradeImport(Long curDEMid, Assessment curAssessment) throws DBException {
		List<Integer> sourcesSelected = new ArrayList<Integer>();
		for (Row curRow : queryDEM("Source_of_specimens_table", curDEMid))
			sourcesSelected.add(curRow.get("Source_code").getInteger(Column.NEVER_NULL));

		Map<Integer, Set<Integer>> purposesSelected = new HashMap<Integer, Set<Integer>>();
		for (Row curRow : queryDEM("purpose_table", curDEMid)) {
			Integer purposeCode = curRow.get("Purpose_code").getInteger();
			Integer useCode = curRow.get("utilisation_code").getInteger();

			if (purposeCode == null)
				continue;
			
			Set<Integer> purposeSelections = purposesSelected.get(purposeCode);
			if (purposeSelections == null) {
				purposeSelections = new HashSet<Integer>();
				purposesSelected.put(purposeCode, purposeSelections);
			}
			if (useCode != null)
				purposeSelections.add(useCode);
		}

		List<Integer> wildSelected = new ArrayList<Integer>();
		for (Row curRow : queryDEM("removed_from_wild_table", curDEMid))
			wildSelected.add(curRow.get("Wild_code").getInteger(Column.NEVER_NULL));

		if (!(sourcesSelected.size() == 0 && purposesSelected.size() == 0 && wildSelected.size() == 0)) {
			if (sourcesSelected.size() == 0)
				sourcesSelected.add(new Integer(0));
			if (purposesSelected.keySet().size() == 0)
				purposesSelected.put(new Integer(0), null);
			if (wildSelected.size() == 0)
				wildSelected.add(new Integer(0));
			
			Field field = new Field(CanonicalNames.UseTradeDetails, curAssessment);

			for (Map.Entry<Integer, Set<Integer>> curPurpose : purposesSelected.entrySet()) {
				for (Integer curSource : sourcesSelected) {
					for (Integer formInWild : wildSelected) {
						Field subfield = new Field(field.getName()+"Subfield", null);
						subfield.setParent(field);
						
						UseTradeField proxy = new UseTradeField(subfield);
						proxy.setPurpose(curPurpose.getKey());
						proxy.setSource(curSource);
						proxy.setFormRemoved(formInWild);

						Set<Integer> ticks = curPurpose.getValue();
						if (ticks != null) {
							proxy.setSubsistence(ticks.contains(Integer.valueOf(1)));
							proxy.setNational(ticks.contains(Integer.valueOf(2)));
							proxy.setInternational(ticks.contains(Integer.valueOf(3)));
						}

						field.getFields().add(subfield);
					}
				}
			}

			curAssessment.getField().add(field);
		}
	}

	private void utilisationTableImport(Long curDEMid, Assessment curAssessment) throws DBException {
		List<Row> rows = queryDEM("utilisation_general", curDEMid);
		if (rows.isEmpty())
			return;

		Row row = rows.get(0);
		
		addBooleanPrimitiveField(CanonicalNames.NotUtilized, "isNotUtilized", curAssessment, isChecked(row, "Utilised"));
		
		StringBuilder useTradeNarrative = new StringBuilder();

		String narrativeText = getString(row, "Other_purpose", null);
		if (narrativeText != null) {
			useTradeNarrative.append("--- Other purpose text ---<br/>");
			useTradeNarrative.append(XMLUtils.clean(narrativeText).replaceAll("\n", "<br/>").replaceAll("\r", ""));
			useTradeNarrative.append("<br/>");
		}

		narrativeText = getString(row, "Other_wild", null);
		if (narrativeText != null) {
			useTradeNarrative.append("--- Other wild text ---<br/>");
			useTradeNarrative.append(XMLUtils.clean(narrativeText).replaceAll("\n", "<br/>").replaceAll("\r", ""));
			useTradeNarrative.append("<br/>");
		}
		
		narrativeText = getString(row, "Other_source", null);
		if (narrativeText != null) {
			useTradeNarrative.append("--- Other source text ---<br/>");
			useTradeNarrative.append(XMLUtils.clean(narrativeText).replaceAll("\n", "<br/>").replaceAll("\r", ""));
			useTradeNarrative.append("<br/>");
		}
		
		narrativeText = useTradeNarrative.toString();
		if (!"".equals(narrativeText)) {
			addTextPrimitiveField(CanonicalNames.UseTradeDocumentation, "value", curAssessment, narrativeText);
		}

		String wildOfftake = searchLibrary(CanonicalNames.TrendInWildOfftake, getString(row, "Offtake"));
		if (wildOfftake != null)
			addForeignKeyPrimitiveField(CanonicalNames.TrendInWildOfftake, "value", curAssessment, Integer.valueOf(wildOfftake));
	
		String domesticOfftake = searchLibrary(CanonicalNames.TrendInDomesticOfftake, getString(row, "Trend"));
		if (domesticOfftake != null)
			addForeignKeyPrimitiveField(CanonicalNames.TrendInDomesticOfftake, "value", curAssessment, Integer.valueOf(domesticOfftake));
	}
	
	private List<Row> queryDEM(String table, Long SpcRecID) throws DBException {
		SelectQuery select = new SelectQuery();
		select.select(table, "*");
		select.constrain(new CanonicalColumnName(table, "Sp_code"), QConstraint.CT_EQUALS, SpcRecID);
		
		return queryDEM(select);
	}
	
	private List<Row> queryDEM(SelectQuery query) throws DBException {
		Row.Set rs = new Row.Set();
		
		ec.doQuery(query, rs);

		return rs.getSet();
	}
	
	private String correctCode(String code) {
		if ("NLA-CU".equals(code))
			return "CW";
		
		return code;
	}
	
	private Integer getIndex(String canonicalName, String dataPointName, String value, Integer defaultValue) throws DBException {
		return getIndex(canonicalName + "_" + dataPointName + "Lookup", value, defaultValue);
	}
	
	private Integer getIndex(String libraryTable, String value, Integer defaultValue) throws DBException {
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
		
		return defaultValue;
	}
	
	private Row.Set getLookup(String table) throws DBException {
		String fieldName = table;
		
		if (lookups.containsKey(fieldName))
			return lookups.get(fieldName);
		else {
			SelectQuery query = new SelectQuery();
			query.select(fieldName, "ID", "ASC");
			query.select(fieldName, "*");
			
			Row.Set lookup = new Row.Set();
			
			lec.doQuery(query, lookup);
			
			lookups.put(fieldName, lookup);
			
			return lookup;
		}
	}
	
	public void print(String out) {
		write(out);
	}
	
	public void println(String out) {
		write(out);
	}

	public void printf(String out, Object... args) {
		write(String.format(out, args));
	}
	
}