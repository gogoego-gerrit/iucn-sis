package org.iucn.sis.server.crossport.export;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.naming.NamingException;

import org.iucn.sis.client.displays.ClassificationScheme;
import org.iucn.sis.client.displays.Display;
import org.iucn.sis.client.displays.Field;
import org.iucn.sis.server.changes.AsmChanges;
import org.iucn.sis.server.io.AssessmentIO;
import org.iucn.sis.server.io.TaxaIO;
import org.iucn.sis.server.simple.SISContainerApp;
import org.iucn.sis.server.taxa.TaxonomyDocUtils;
import org.iucn.sis.server.utils.DocumentUtils;
import org.iucn.sis.server.utils.ServerPaths;
import org.iucn.sis.shared.BaseAssessment;
import org.iucn.sis.shared.TreeDataRow;
import org.iucn.sis.shared.acl.User;
import org.iucn.sis.shared.data.assessments.AssessmentData;
import org.iucn.sis.shared.data.assessments.CanonicalNames;
import org.iucn.sis.shared.data.assessments.FieldParser;
import org.iucn.sis.shared.structures.SISCategoryAndCriteria;
import org.iucn.sis.shared.structures.SISClassificationSchemeStructure;
import org.iucn.sis.shared.structures.SISLivelihoods;
import org.iucn.sis.shared.structures.SISMultiSelect;
import org.iucn.sis.shared.structures.SISRelatedStructures;
import org.iucn.sis.shared.structures.SISSelect;
import org.iucn.sis.shared.structures.SISStructureCollection;
import org.iucn.sis.shared.structures.SISThreatStructure;
import org.iucn.sis.shared.structures.Structure;
import org.iucn.sis.shared.structures.UseTrade;
import org.iucn.sis.shared.taxonomyTree.TaxonNode;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.solertium.db.CInteger;
import com.solertium.db.CString;
import com.solertium.db.DBException;
import com.solertium.db.ExecutionContext;
import com.solertium.db.Row;
import com.solertium.db.StringLiteral;
import com.solertium.db.SystemExecutionContext;
import com.solertium.db.query.DeleteQuery;
import com.solertium.db.query.ExperimentalSelectQuery;
import com.solertium.db.query.InsertQuery;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.util.ClasspathResources;
import com.solertium.util.ElementCollection;
import com.solertium.util.NodeCollection;
import com.solertium.util.ClasspathResources.ClasspathResourceException;

/**
 * Accepts notifications that data has been changed and fires updates to the
 * backing database. 
 * 
 * @author adam.schwartz
 *
 */
public class DBMirrorManager {

	public static DBMirrorManager impl = new DBMirrorManager();

	private ExecutionContext ec;
	public static final String DS = "assess";

	private Queue<AssessmentData> assessmentQueue;
	private Queue<TaxonNode> taxaQueue;

	private DBMirrorDaemon daemon;
	private Thread daemonThread;
	private final int TAXA_THREAD_COUNT = 2;
	private final int ASSESSMENT_THREAD_COUNT = 4;
	
	private AtomicBoolean exporting;
	
	public DBMirrorManager() {
		exporting = new AtomicBoolean(false);
		
		try {
			if( SISContainerApp.amIOnline ) {
				ec = new SystemExecutionContext(DS);
				ec.setExecutionLevel(ExecutionContext.ADMIN);
				ec.setAPILevel(ExecutionContext.SQL_ALLOWED);
			
				Document structureDoc = ClasspathResources.getDocument(AccessExport.class, "struct-postgresql.xml");
				try {
					ec.appendStructure(structureDoc, true);
				} catch (Exception e) {
					//Let this fail silently, as it will usually be the case that the structure exists.
					e.printStackTrace();
				}
				
				AsmChanges.startTriggering(ec);	
			}
			
			assessmentQueue = new ConcurrentLinkedQueue<AssessmentData>();
			taxaQueue = new ConcurrentLinkedQueue<TaxonNode>();
			daemon = new DBMirrorDaemon();
			daemonThread = new Thread(daemon);
			daemonThread.start();
		} catch (final DBException e) {
			e.printStackTrace();
			throw new RuntimeException("The database structure could not be set", e);
		} catch (final NamingException e) {
			e.printStackTrace();
			throw new RuntimeException("The database was not found", e);
		} catch (final ClasspathResourceException e) {
			e.printStackTrace();
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}
	
	public void runFullExport() {
		if( !exporting.getAndSet(true) ) {
			try {
				ElementCollection tables = new ElementCollection(ec.analyzeExistingStructure().getElementsByTagName("table"));
				for( Element table : tables ) {
					try {
						ec.dropTable(table.getAttribute("name"));
					} catch (Exception e) {
						//Table didn't exist, most certainly.
					}
				}

				Document structureDoc = ClasspathResources.getDocument(AccessExport.class, "struct-postgresql.xml");
				ec.createStructure(structureDoc);
			} catch (Exception e) {
				//Let this fail silently, as it will usually be the case that the structure exists.
			}

			try {
				AsmChanges.stopTriggering(ec);
			} catch (DBException e) {
				e.printStackTrace();
				System.out.println("Could not turn off triggers. Cowardly refusing to perform " +
						"export with them on...");
				exporting.set(false);
				return;
			}
			
			final Document taxdoc = TaxonomyDocUtils.getTaxonomyDocByID();
			System.out.println("Populating Regions lookup table.");
			Document regionsDoc = DocumentUtils
					.getVFSFileAsDocument("/regions/regions.xml", SISContainerApp.getStaticVFS());
			if (regionsDoc != null) {
				try {
					final DeleteQuery dq = new DeleteQuery("RegionLookup");
					
					ElementCollection col = ElementCollection.childElementsByTagName(regionsDoc.getDocumentElement(), "region");
					for (Element el : col) {
						Row row = ec.getRow("RegionLookup");
						String name = el.getElementsByTagName("name").item(0).getTextContent();
						String desc = el.getElementsByTagName("description").item(0).getTextContent();

						row.get("region_id").setString(el.getAttribute("id"));
						row.get("region_name").setString(ec.formatLiteral(new StringLiteral(name)));
						row.get("region_description").setString(ec.formatLiteral(new StringLiteral(desc)));

						final InsertQuery iq = new InsertQuery("RegionLookup", row);
						ec.doUpdate(iq);
					}
				} catch (DBException e) {
					e.printStackTrace();
					System.out.println("ERROR building RegionLookup table.");
				}
			}
			
			System.out.println("Populating assessment lookup tables.");
			new AssessmentLookupBuilder(Arrays.asList(CanonicalNames.allCanonicalNames));

			final NodeCollection nodes = new NodeCollection(taxdoc.getDocumentElement().getChildNodes());
			List<TaxonNode> taxaList = new ArrayList<TaxonNode>();
			List<AssessmentData> assessments = new ArrayList<AssessmentData>();
			
			Date start = new Date();
			recurseIntoTaxonomy(nodes, taxaList, assessments);
			
			if( taxaList.size() > 0 ) //Don't forget the leftovers
				taxaChanged(taxaList);
			
			Date end = new Date();
			System.out.println("Export of taxa took " + (end.getTime()-start.getTime()) + "ms.");
			
			exporting.set(false);
		}
	}

	private void recurseIntoTaxonomy(final NodeCollection nodes, List<TaxonNode> taxaList, List<AssessmentData> assessments) {
		for (final Node node : nodes) {
			if (!(node instanceof Element))
				continue;
			final Element element = (Element) node;
			final String elementName = element.getNodeName();
			if (!elementName.startsWith("node"))
				continue;

			final String nodeID = elementName.substring(4);
			TaxonNode taxon = TaxaIO.readNode(nodeID, SISContainerApp.getStaticVFS());
			if( taxon != null )
				taxaList.add(taxon);
			
			for( String id : taxon.getAssessments() )
				assessments.add(AssessmentIO.readAssessment(SISContainerApp.getStaticVFS(), id, BaseAssessment.PUBLISHED_ASSESSMENT_STATUS, ""));

			assessments.addAll(AssessmentIO.readAllDraftAssessments(SISContainerApp.getStaticVFS(), taxon.getId()+""));
			
			if( taxaList.size() > 2000 ) {
				taxaChanged(taxaList);
				taxaList.clear();
				System.gc();
			}
			
			if( assessments.size() > 100 ) {
				assessmentsChanged(assessments);
				assessments.clear();
				System.gc();
			}
			
			while( taxaQueue.size() > 4000 || assessmentQueue.size() > 200 ) {
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {}
			}
			
			if( node.hasChildNodes())
				recurseIntoTaxonomy(new NodeCollection(node.getChildNodes()), taxaList, assessments);
		}
	}
	
	public boolean isExporting() {
		return exporting.get();
	}

	public void assessmentDeleted(AssessmentData assessment) {
		assessmentQueue.add(assessment);
		notifyDaemon();
	}

	public void assessmentsDeleted(List<AssessmentData> assessments) {
		assessmentQueue.addAll(assessments);
		notifyDaemon();
	}
	
	public void assessmentChanged(AssessmentData assessment) {
		assessmentQueue.add(assessment);
		notifyDaemon();
	}

	public void assessmentsChanged(List<AssessmentData> assessments) {
		assessmentQueue.addAll(assessments);
		notifyDaemon();
	}

	public void taxonChanged(TaxonNode taxon) {
		taxaQueue.add(taxon);
		notifyDaemon();
	}

	public void taxaChanged(List<TaxonNode> taxa) {
		taxaQueue.addAll(taxa);
		notifyDaemon();
	}

	private void notifyDaemon() {
		if( SISContainerApp.amIOnline ) {
			if( daemonThread.getState() == Thread.State.WAITING )
				daemonThread.interrupt();
		} else {
			assessmentQueue.clear();
			taxaQueue.clear();
		}
	}

	private class DBMirrorDaemon implements Runnable {

		private List<DBMirrorAssessmentProcessor> assessmentConsumers;
		private List<DBMirrorTaxonProcessor> taxaConsumers;
		protected HashMap<String, User> users;

		public DBMirrorDaemon() throws DBException, NamingException, IOException {
			assessmentConsumers = new ArrayList<DBMirrorAssessmentProcessor>();
			taxaConsumers = new ArrayList<DBMirrorTaxonProcessor>();
			
			if( SISContainerApp.amIOnline ) {
				getUserInfo();

				for( int i = 0; i < TAXA_THREAD_COUNT; i++ )
					taxaConsumers.add(new DBMirrorTaxonProcessor(taxaQueue, "thread"+i));

				for( int i = 0; i < ASSESSMENT_THREAD_COUNT; i++ )
					assessmentConsumers.add(new DBMirrorAssessmentProcessor(assessmentQueue, "assessment"+i, users));
			}
		}

		public void run() {
			synchronized (this) {
				while( 1 == 1 ) {
					try {
						if( assessmentQueue.peek() != null )
							awakenAssessmentConsumers();

						if( taxaQueue.peek() != null )
							awakenTaxonConsumers();

						wait();
					} catch (InterruptedException e) {}
				}
			}
		}

		private void awakenAssessmentConsumers() {
			for( DBMirrorAssessmentProcessor cur : assessmentConsumers ) {
				if( cur.getMyThread().getState() == Thread.State.WAITING )
					cur.getMyThread().interrupt();
			}
		}

		private void awakenTaxonConsumers() {
			for( DBMirrorTaxonProcessor cur : taxaConsumers )
				if( cur.getMyThread().getState() == Thread.State.WAITING )
					cur.getMyThread().interrupt();
		}
		
		private void getUserInfo() throws NamingException {
			users = new HashMap<String, User>();
			
			SystemExecutionContext ec2 = new SystemExecutionContext("users");
			ec2.setExecutionLevel(ExecutionContext.ADMIN);
			ec2.setAPILevel(ExecutionContext.SQL_ALLOWED);
			
			final ExperimentalSelectQuery query = new ExperimentalSelectQuery();
			query.select("user", "*");
			query.select("profile", "firstname");
			query.select("profile", "lastname");
			query.select("profile", "initials");
			query.select("profile", "affiliation");
			
			final Row.Set rs = new Row.Set();
			try {
				ec2.doQuery(query, rs);
				
				for( Row curRow : rs.getSet() ) {
					String f = curRow.get("firstname").getString();
					String l = curRow.get("lastname").getString();
					String i = curRow.get("initials").getString();
					String id = curRow.get("id").getString();
					
					User user = new User();
					user.setFirstName(f);
					user.setLastName(l);
					user.setInitials(i);
					
					users.put(id, user);
				}
			} catch (DBException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	private class AssessmentLookupBuilder {
		
		private FieldParser fieldParser;
		private HashMap<String, Display> lookupTables = new HashMap<String, Display>();
		
		public AssessmentLookupBuilder(List<String> fieldNames) {
			fieldParser = new FieldParser();
			lookupTables = new HashMap<String, Display>();

			for( String curField : fieldNames ) {
				try {
					createLookupTable(curField);
				} catch (Exception e) {
					e.printStackTrace();
					System.out.println("Couldn't build field " + curField);
				}
			}
		}
		
		private void createLookupTable(String fieldName) throws Exception {
			if (lookupTables.containsKey(fieldName))
				return;

			String url = ServerPaths.getFieldURL(fieldName);

			if (!SISContainerApp.getStaticVFS().exists(url)) {
				System.out.println("Could not find field definition for " + url);
				return;
			}

			NativeDocument fdoc = SISContainerApp.newNativeDocument(null);
			fdoc.parse(DocumentUtils.getVFSFileAsString(url, SISContainerApp.getStaticVFS()));

			Display f = fieldParser.parseField(fdoc);
			parseLookupFromDisplay(fieldName, f);
			lookupTables.put(fieldName, f);
		}
		private void parseLookupFromDisplay(String fieldName, Display f) throws DBException {
			try {
				if (f instanceof ClassificationScheme) {
					Row r = new Row();
					r.add(new CString("id", "sampleID"));
					r.add(new CString("parentID", "sampleID"));
					r.add(new CInteger("level", 10));
					r.add(new CString("ref", "1.1.1.1"));
					r.add(new CString("description", "sample description"));

					ec.createTable("_scheme_lookup_" + fieldName, r);

					// Generate classification scheme lookup table
					ClassificationScheme scheme = (ClassificationScheme) f;
					for (Object obj : scheme.getTreeData().getTreeRoots())
						insertClassSchemeLookupRow(fieldName, "(root)", scheme, (TreeDataRow) obj, 0);

					// Generate classification scheme's default structure lookup
					// table
					Structure defStructure = scheme.generateDefaultStructure();
					buildStructureLookup(fieldName + "Structure", defStructure);
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
		private void buildIndexedLookup(String tableName, String indexName, String descName, Object[] options,
				int indexOffset) throws DBException {
			Row prototype = new Row();
			prototype.add(new CInteger(indexName, Integer.valueOf(0)));
			prototype.add(new CString(descName, "sample description"));
			ec.createTable(tableName, prototype);

			for (int i = 0; i < options.length; i++) {
				Row r = new Row();
				r.add(new CInteger(indexName, Integer.valueOf(i + indexOffset)));
				r.add(new CString(descName, options[i].toString()));

				InsertQuery iq = new InsertQuery(tableName, r);
				ec.doUpdate(iq);
			}
		}

		private void buildStructureLookup(String fieldName, Structure struct) throws DBException {
			if (struct instanceof SISClassificationSchemeStructure) {
				SISClassificationSchemeStructure s = (SISClassificationSchemeStructure) struct;
				parseLookupFromDisplay("Stresses", s.getScheme());
			}
			if (struct instanceof SISStructureCollection) {
				SISStructureCollection s = (SISStructureCollection) struct;

				for (int i = 0; i < s.getStructures().size(); i++) {
					Structure cur = (Structure) s.getStructures().get(i);

					if (cur.getDescription() != null && !cur.getDescription().equals("")) {
						String cleanDesc = cur.getDescription().replaceAll("\\s", "").replaceAll("\\W", "");
						try {
							if ("_lookup_".length() + fieldName.length() + cleanDesc.length() < 64)
								buildStructureLookup(fieldName + cleanDesc, cur);
							else
								buildStructureLookup(fieldName + i, cur);
						} catch (DBException duplicateDescriptionProbably) {
							buildStructureLookup(fieldName + i, cur);
						}
					} else
						buildStructureLookup(fieldName + i, cur);
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
								buildStructureLookup(fieldName + cleanDesc, cur);
							else
								buildStructureLookup(fieldName + i, cur);
						} catch (DBException duplicateDescriptionProbably) {
							buildStructureLookup(fieldName + i, cur);
						}
					} else
						buildStructureLookup(fieldName + i, cur);
				}
			} else if (struct instanceof SISMultiSelect || struct instanceof SISSelect) {
				String tableName = "_lookup_" + fieldName;
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

				buildIndexedLookup("_lookup_ThreatTiming", "index", "description", timing, 1);
				buildIndexedLookup("_lookup_ThreatScope", "index", "description", scope, 1);
				buildIndexedLookup("_lookup_ThreatSeverity", "index", "description", severity, 1);
			} else if (struct instanceof UseTrade) {
				UseTrade useTrade = (UseTrade) struct;

				buildIndexedLookup("_lookup_UseTradePurpose", "index", "description", useTrade.getPurposeOptions(), 1);
				buildIndexedLookup("_lookup_UseTradeSource", "index", "description", useTrade.getSourceOptions(), 1);
				buildIndexedLookup("_lookup_UseTradeFormRemoved", "index", "description", useTrade.getFormRemovedOptions(), 1);
				buildIndexedLookup("_lookup_UseTradeUnits", "index", "description", useTrade.getUnitsOptions(), 0);
			} else if (struct instanceof SISLivelihoods) {
				SISLivelihoods l = (SISLivelihoods) struct;

				buildIndexedLookup("_lookup_LivelihoodsScale", "index", "description", l.getScaleOptions(), 1);
				buildIndexedLookup("_lookup_LivelihoodsUnits", "index", "description", l.getUnitsOptions(), 1);
				buildIndexedLookup("_lookup_LivelihoodsHumanReliance", "index", "description", l.getHumanRelianceOptions(), 1);
				buildIndexedLookup("_lookup_LivelihoodsGenderAge", "index", "description", l.getByGenderAgeOptions(), 1);
				buildIndexedLookup("_lookup_LivelihoodsSocioEcon", "index", "description", l.getBySocioEconOptions(), 1);
				buildIndexedLookup("_lookup_LivelihoodsPercentPopBenefit", "index", "description", l.getPercentPopulationBenefitingOptions(), 1);
				buildIndexedLookup("_lookup_LivelihoodsPercentConsume", "index", "description", l.getPercentConsumptionOptions(), 1);
				buildIndexedLookup("_lookup_LivelihoodsPercentIncome", "index", "description", l.getPercentIncomeOptions(), 1);
			} else if (struct instanceof SISCategoryAndCriteria) {
				buildIndexedLookup("_lookup_CriteriaVersions", "index", "crit_version", new String[] { "3.1", "2.3",
				"Earlier Version" }, 0);
			}
		}
		
		private void insertClassSchemeLookupRow(String fieldName, String parentID, ClassificationScheme scheme,
				TreeDataRow curRow, int level) throws DBException {
			Row r;
			String curCode = (String) curRow.getDisplayId();
			String curDesc = (String) scheme.getCodeToDesc().get(curCode);
			String curLevelID = (String) scheme.getCodeToLevelID().get(curCode);

			// If it's not a region in CountryOccurrence, do your stuff
			if (!(fieldName.equals(CanonicalNames.CountryOccurrence) && curCode.length() > 10)) {
				r = new Row();
				r.add(new CString("id", curCode));
				r.add(new CString("parentID", parentID));
				r.add(new CInteger("level", level));
				r.add(new CString("ref", curLevelID));
				r.add(new CString("description", curDesc));

				InsertQuery i = new InsertQuery("_scheme_lookup_" + fieldName, r);
				ec.doUpdate(i);

				for (Object obj : curRow.getChildren())
					insertClassSchemeLookupRow(fieldName, curCode, scheme, (TreeDataRow) obj, level + 1);
			} else
				for (Object obj : curRow.getChildren())
					insertClassSchemeLookupRow(fieldName, "(root)", scheme, (TreeDataRow) obj, level);
		}
	}
}

