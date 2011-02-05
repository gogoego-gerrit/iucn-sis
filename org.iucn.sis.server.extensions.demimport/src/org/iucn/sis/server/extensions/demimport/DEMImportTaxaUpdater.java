package org.iucn.sis.server.extensions.demimport;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.iucn.sis.client.referenceui.ReferenceUI;
import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.server.api.io.TaxonIO;
import org.iucn.sis.server.api.utils.DocumentUtils;
import org.iucn.sis.server.api.utils.WordUtils;
import org.iucn.sis.shared.BaseAssessment;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.Taxon;
import org.iucn.sis.shared.api.models.User;
import org.iucn.sis.shared.api.models.WorkingSet;
import org.iucn.sis.shared.conversions.AssessmentConverter;
import org.iucn.sis.shared.conversions.TaxonConverter;
import org.iucn.sis.shared.data.assessments.AssessmentData;
import org.iucn.sis.shared.data.assessments.CanonicalNames;
import org.iucn.sis.shared.data.assessments.Note;
import org.iucn.sis.shared.data.assessments.OccurrenceMigratorUtils;
import org.iucn.sis.shared.data.assessments.Region;
import org.iucn.sis.shared.structures.FormattingStripper;
import org.iucn.sis.shared.structures.SISCategoryAndCriteria;
import org.iucn.sis.shared.structures.SISLivelihoods;
import org.iucn.sis.shared.structures.SISThreatStructure;
import org.iucn.sis.shared.structures.UseTrade;
import org.iucn.sis.shared.taxonomyTree.CommonNameData;
import org.iucn.sis.shared.taxonomyTree.CommonNameFactory;
import org.iucn.sis.shared.taxonomyTree.SynonymData;
import org.iucn.sis.shared.taxonomyTree.TaxonNode;
import org.iucn.sis.shared.taxonomyTree.TaxonNodeFactory;
import org.iucn.sis.shared.taxonomyTree.TaxonomyTree;
import org.iucn.sis.shared.taxonomyTree.TaxonomyTree.Kingdom;
import org.iucn.sis.shared.xml.XMLUtils;
import org.restlet.Context;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.CharacterSet;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.representation.StringRepresentation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.solertium.db.CanonicalColumnName;
import com.solertium.db.Column;
import com.solertium.db.DBException;
import com.solertium.db.DBSessionFactory;
import com.solertium.db.ExecutionContext;
import com.solertium.db.Row;
import com.solertium.db.SystemExecutionContext;
import com.solertium.db.query.QConstraint;
import com.solertium.db.query.QRelationConstraint;
import com.solertium.db.query.SelectQuery;
import com.solertium.lwxml.factory.NativeDocumentFactory;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.util.ElementCollection;
import com.solertium.util.SysDebugger;

/**
 * This will perform an import of DEM data, creating taxa as it goes if required
 * and adding assessments and their data as required. This process speaks with a
 * running SIS server to achieve creation of taxa and assessments, so please
 * ensure a server is running.
 * 
 * @author adam.schwartz
 */
public class DEMImportTaxaUpdater implements Runnable {

	private ExecutionContext ec;
	private TaxonomyTree tree;

	private static final String CANNED_NOTE = "Unparseable data encountered during DEM import: ";

	private ArrayList<TaxonNode> nodes;

	private LinkedHashMap<Long, TaxonNode> assessedNodesBySpc_id;
	private LinkedHashMap<String, Long> spcNameToIDMap;
	private LinkedHashMap<Long, AssessmentData> assessments;

	private String fileName;
	private String user;
	private User userObj;
	private String secret;

	private int newNodeID = -1;

	private static AtomicBoolean running = new AtomicBoolean(false);
	private static AtomicBoolean failed = new AtomicBoolean(false);
	private static StringBuilder statusMessage = new StringBuilder();

	private StringBuilder log = new StringBuilder();

	private AssessmentConverter assessmentConverter;
	
	
	public static final int COUNTRY_CODING_OCCURRENCE_TYPE = 1;
	public static final int SUBCOUNTRY_CODING_OCCURRENCE_TYPE = 2;
	public static final int FAO_CODING_OCCURRENCE_TYPE = 3;
	public static final int LME_CODING_OCCURRENCE_TYPE = 4;

	public static String getStatusMessage() {
		return statusMessage.toString();
	}

	public static boolean isFailure() {
		return failed.get();
	}

	public static boolean isRunning() {
		return running.get();
	}

	// private HttpClient httpClient = null;

	private String uriPrefix = "riap://application";
	private File source;
//	private Uniform uniform;
	private Context context;

	public DEMImportTaxaUpdater(File source, Context context, String fileName, String user, String secret) {
		this.source = source;
		this.context = context;
		this.fileName = fileName;
		this.user = user;
		this.userObj = SIS.get().getUserIO().getUserFromUsername(user);
		this.secret = secret;

		spcNameToIDMap = new LinkedHashMap<String, Long>();
		assessedNodesBySpc_id = new LinkedHashMap<Long, TaxonNode>();
		assessments = new LinkedHashMap<Long, AssessmentData>();
		nodes = new ArrayList<TaxonNode>();

		// httpClient = new HttpClient();
	}

	private void addTaxaDetails() throws DBException {
		for (Iterator<Long> iter = assessedNodesBySpc_id.keySet().iterator(); iter.hasNext();) {
			Long curSpcID = iter.next();
			TaxonNode curNode = assessedNodesBySpc_id.get(curSpcID);
			boolean changed = false;

			// DO COMMON NAMES
			SelectQuery select = new SelectQuery();
			select.select("common_names", "*");
			select.constrain(new CanonicalColumnName("common_names", "Sp_code"), QConstraint.CT_EQUALS, curSpcID);

			Row.Set rowSet = new Row.Set();
			ec.doQuery(select, rowSet);

			for (Iterator<Row> rows = rowSet.getSet().iterator(); rows.hasNext();) {
				Row curRow = rows.next();
				String name = curRow.get("Common_name").getString(Column.NEVER_NULL);
				boolean primary = curRow.get("Primary").getInteger(Column.NEVER_NULL) == 1;
				String language = curRow.get("Language").getString(Column.NEVER_NULL);
				String isoCode = curRow.get("ISO_LANG").getString(Column.NEVER_NULL);

				CommonNameData commonName = CommonNameFactory.createCommonName(name, language, isoCode, primary);
				commonName.setValidated(false, CommonNameData.ADDED);

				boolean alreadyExists = false;

				for (CommonNameData cn : curNode.getCommonNames()) {
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
					
					if (primary)
						curNode.addCommonNameAsPrimary(commonName);
					else
						curNode.addCommonName(commonName);

					changed = true;
				}
			}

			// DO SYNONYMS
			select = new SelectQuery();
			select.select("Synonyms", "*");
			select.constrain(new CanonicalColumnName("Synonyms", "Sp_code"), QConstraint.CT_EQUALS, curSpcID);

			rowSet = new Row.Set();
			ec.doQuery(select, rowSet);

			for (Iterator<Row> rows = rowSet.getSet().iterator(); rows.hasNext();) {
				Row curRow = rows.next();
				String name = curRow.get("Species_name").getString(Column.NEVER_NULL);
				String notes = curRow.get("SynonymNotes").getString(Column.NEVER_NULL);

				SynonymData curSyn;
				int synlevel = TaxonNode.SPECIES;
				String[] brokenName = name.split("\\s");
				String genusName = "";
				String spcName = "";
				int infraType = -1;
				String infraName = "";
				String subpopName = "";

				genusName = brokenName[0];
				synlevel = TaxonNode.GENUS;

				if (brokenName.length > 1) {
					spcName = brokenName[1];
					synlevel = TaxonNode.SPECIES;
				}
				if (brokenName.length > 2) {
					if (brokenName[2].matches("^ssp\\.?$") || brokenName[2].matches("^var\\.?$")) {
						if (brokenName[2].matches("^ssp\\.?$"))
							infraType = TaxonNode.INFRARANK_TYPE_SUBSPECIES;
						else
							infraType = TaxonNode.INFRARANK_TYPE_VARIETY;

						infraName = brokenName[3];
						for (int i = 4; i < brokenName.length; i++)
							infraName += " " + brokenName[i];

						synlevel = TaxonNode.INFRARANK;
					} else if (brokenName.length > 3
							&& (brokenName[3].matches("^\\.?$") || brokenName[3].matches("^var\\.?$"))) {
						spcName += " " + brokenName[2];
						if (brokenName[3].matches("^ssp\\.?$"))
							infraType = TaxonNode.INFRARANK_TYPE_SUBSPECIES;
						else
							infraType = TaxonNode.INFRARANK_TYPE_VARIETY;

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
							synlevel = TaxonNode.SUBPOPULATION;
						} else
							spcName += " " + whatsLeft;
					}
				}
				String authority = curRow.get("Syn_Authority").getString(Column.NEVER_NULL);

				curSyn = new SynonymData(genusName, spcName, infraName, infraType, subpopName, synlevel, "");
				curSyn.setAuthority(authority, synlevel);
				curSyn.setStatus(SynonymData.ADDED);
				curSyn.setNotes(notes);

				boolean alreadyExists = false;

				for (SynonymData syn : curNode.getSynonyms()) 
					alreadyExists = curSyn.equals(syn);

				if (!alreadyExists) {
					curNode.addSynonym(curSyn);
					changed = true;
				}
			}

			if (changed && curNode.getId() != newNodeID)
				curNode.setStatus(curNode.getStatus() + "changed");
		}
	}

	private void buildAssessments() throws Exception {
		log.append("<tr><td align=\"center\" colspan=\"9\">" + "<b><u>New Draft Assessments</u></b></td></tr>");

		// FOR EACH NEW SPECIES...
		for (Iterator<Long> iter = assessedNodesBySpc_id.keySet().iterator(); iter.hasNext();) {
			Long curDEMid = iter.next();
			TaxonNode curNode = assessedNodesBySpc_id.get(curDEMid);

			logNode(curNode);

			AssessmentData curAssessment = new AssessmentData();
			curAssessment.setAssessmentID("" + curNode.getId());
			curAssessment.setSpeciesID("" + curNode.getId());
			curAssessment.setSpeciesName(curNode.getFullName());
			curAssessment.setType(BaseAssessment.DRAFT_ASSESSMENT_STATUS);

			HashMap<String, Object> data = new HashMap<String, Object>();

			systematicsTableImport(curDEMid, curAssessment, data);
			
			List<Assessment> drafts = SIS.get().getAssessmentIO().readDraftAssessmentsForTaxon(
					Integer.valueOf(""+curNode.getId()));
			boolean newIsGlobal = curAssessment.isGlobal();
			for( Assessment cur : drafts ) {
				if( newIsGlobal && cur.isGlobal() ) {
					statusMessage.append("A global draft assessment for the species " + curNode.getFullName() + 
							" already exists. Remove the species from the DEM, or delete the assessment in SIS" +
					" so the import can succeed.");
					failed.set(true);
					return;
				} else if( !newIsGlobal ) {
					List<String> curAssessmentRegions = curAssessment.getRegionIDs();
					for( Integer curRegion : cur.getRegionIDs() )
						if( curAssessmentRegions.contains(curRegion.toString()) ) {
							statusMessage.append("A regional draft assessment for the species " + curNode.getFullName() + 
									" with the region specified in the DEM already exists. Remove the species" +
							" from the DEM, or delete the assessment in SIS so the import can succeed.");
							failed.set(true);
							return;
						}

				}
			}
			
			distributionTableImport(curDEMid, curAssessment, data);
			populationTableImport(curDEMid, curAssessment, data);
			habitatTableImport(curDEMid, curAssessment, data);
			lifeHistoryTableImport(curDEMid, curAssessment, data);
			threatTableImport(curDEMid, curAssessment, data);
			countryTableImport(curDEMid, curAssessment, data);
			redListingTableImport(curDEMid, curAssessment, data);
			landCoverTableImport(curDEMid, curAssessment, data);
			utilisationTableImport(curDEMid, curAssessment, data);
			growthFormTableImport(curDEMid, curAssessment, data);
			conservationMeasuresTableImport(curDEMid, curAssessment, data);
			ecosystemServicesTableImport(curDEMid, curAssessment, data);
			riversTableImport(curDEMid, curAssessment, data);
			lakesTableImport(curDEMid, curAssessment, data);
			faoMarineTableImport(curDEMid, curAssessment, data);
			lmeTableImport(curDEMid, curAssessment, data);
			useTradeImport(curDEMid, curAssessment, data);
			livelihoodsTableImport(curDEMid, curAssessment, data);

			curAssessment.addData(data);

			referencesImport(curDEMid, curAssessment, data);

			OccurrenceMigratorUtils.migrateOccurrenceData(curAssessment);

			assessments.put(new Long(curNode.getId()), curAssessment);
		}
	}

	public void buildTree() throws Exception {
		tree = new TaxonomyTree();

		SelectQuery select = new SelectQuery();
		select.select("Systematics", "*");

		Row.Set rows = new Row.Set();

		ec.doQuery(select, rows);

		List<Row> rowList = rows.getSet();

		System.out.println("There are " + rowList.size() + " entries in this DEM.");

		for (Iterator<Row> iter = rowList.listIterator(); iter.hasNext();) {
			Row curCol = iter.next();

			ArrayList<String> footprint = new ArrayList<String>();

			TaxonNode kingdomN = null;
			TaxonNode phylumN = null;
			TaxonNode classN = null;
			TaxonNode orderN = null;
			TaxonNode familyN = null;
			TaxonNode genusN = null;
			TaxonNode speciesN = null;
			TaxonNode rankN = null;
			TaxonNode sspN = null;

			String curKingdom = curCol.get("Kingdom").getString().trim().toUpperCase();
			String curPhylum = curCol.get("Phylum").getString().trim().toUpperCase();
			String curClass = curCol.get("Class").getString().trim().toUpperCase();
			String curOrder = curCol.get("Order").getString().trim().toUpperCase();
			String curFamily = curCol.get("Family").getString().trim().toUpperCase();
			String curGenus = curCol.get("Genus").getString().trim();
			String curSpecies = curCol.get("Species").getString().trim();
			String curInfratype = curCol.get("Rank").getString(Column.NATURAL_NULL);
			String curInfraEpithet = curCol.get("Rank_epithet").getString(Column.NATURAL_NULL);
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

			if (curInfratype != null && curInfraEpithet == null) {
				failed.set(true);
				statusMessage.append("Your DEM contains an infrarank with no epithet specified."
						+ " Please change your data, and try the import again.<br>");
				return;
			}

			if (curInfratype == null && curInfraEpithet != null) {
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
					kingdomN = TaxonNodeFactory.createNode((long) newNodeID, curKingdom, TaxonNode.KINGDOM, "", "",
							false, curStat, "", "");
					kingdomN.setFullName(kingdomN.getName());

					kingdomN.setFootprint(new String[0]);
				}

				tree.addNode(curKingdom, kingdomN);
			}
			footprint.add(kingdomN.getName());

			nodes.add(kingdomN);

			phylumN = tree.getNode(TaxonNode.PHYLUM, curKingdom, curPhylum);
			if (phylumN == null) {
				phylumN = fetchNode(curKingdom, curPhylum);

				if (phylumN == null) {
					phylumN = TaxonNodeFactory.createNode((long) newNodeID, curPhylum, TaxonNode.PHYLUM, "",
							curKingdom, false, curStat, "", "");
					phylumN.setFullName(phylumN.getName());

					phylumN.setFootprint(footprint.toArray(new String[TaxonNode.PHYLUM]));
				}

				kingdomN.setAsParent(phylumN);
				tree.addNode(curKingdom, phylumN);
			}
			updateFootprint(footprint, phylumN);

			nodes.add(phylumN);

			classN = tree.getNode(TaxonNode.CLASS, curKingdom, curClass);
			if (classN == null) {
				classN = fetchNode(curKingdom, curClass);

				if (classN == null) {
					classN = TaxonNodeFactory.createNode((long) newNodeID, curClass, TaxonNode.CLASS, "", curPhylum,
							false, curStat, "", "");
					classN.setFullName(classN.getName());

					classN.setFootprint(footprint.toArray(new String[TaxonNode.CLASS]));
				}

				phylumN.setAsParent(classN);
				tree.addNode(curKingdom, classN);
			}
			updateFootprint(footprint, classN);
			nodes.add(classN);

			orderN = tree.getNode(TaxonNode.ORDER, curKingdom, curOrder);
			if (orderN == null) {
				orderN = fetchNode(curKingdom, curOrder);

				if (orderN == null) {
					orderN = TaxonNodeFactory.createNode((long) newNodeID, curOrder, TaxonNode.ORDER, "", curClass,
							false, curStat, "", "");

					orderN.setFullName(orderN.getName());

					orderN.setFootprint(footprint.toArray(new String[TaxonNode.ORDER]));
				}

				classN.setAsParent(orderN);
				tree.addNode(curKingdom, orderN);
			}
			updateFootprint(footprint, orderN);
			nodes.add(orderN);

			familyN = tree.getNode(TaxonNode.FAMILY, curKingdom, curFamily);
			if (familyN == null) {
				familyN = fetchNode(curKingdom, curFamily);

				if (familyN == null) {
					familyN = TaxonNodeFactory.createNode((long) newNodeID, curFamily, TaxonNode.FAMILY, "", curOrder,
							false, curStat, "", "");
					familyN.setFullName(familyN.getName());

					familyN.setFootprint(footprint.toArray(new String[TaxonNode.FAMILY]));
				}

				orderN.setAsParent(familyN);
				tree.addNode(curKingdom, familyN);
			}
			updateFootprint(footprint, familyN);
			nodes.add(familyN);

			genusN = tree.getNode(TaxonNode.GENUS, curKingdom, curGenus);
			if (genusN == null) {
				genusN = fetchNode(curKingdom, curGenus);

				if (genusN == null) {
					genusN = TaxonNodeFactory.createNode((long) newNodeID, curGenus, TaxonNode.GENUS, "", curFamily,
							false, curStat, "", "");
					genusN.setFullName(genusN.getName());

					genusN.setFootprint(footprint.toArray(new String[TaxonNode.GENUS]));
				}

				familyN.setAsParent(genusN);
				tree.addNode(curKingdom, genusN);
			}
			updateFootprint(footprint, genusN);
			nodes.add(genusN);

			speciesN = tree.getNode(TaxonNode.SPECIES, curKingdom, curGenus + " " + curSpecies);
			if (speciesN == null) {
				speciesN = fetchNode(curKingdom, curGenus + curSpecies);

				if (speciesN == null) {
					speciesN = TaxonNodeFactory.createNode((long) newNodeID, curSpecies, TaxonNode.SPECIES, "",
							curGenus, false, curStat, "", "");

					speciesN.setFullName(curGenus + " " + speciesN.getName());
					spcNameToIDMap.put(speciesN.getFullName(), new Long(specID));

					speciesN.setFootprint(footprint.toArray(new String[TaxonNode.SPECIES]));
				}

				speciesN.setFullName(curGenus + " " + speciesN.getName());
				speciesN.setTaxonomicAuthority(spcTaxonomicAuthority);

				genusN.setAsParent(speciesN);
				tree.addNode(curKingdom, speciesN);
			}
			if (curInfratype == null && curSSP == null) {
				assessedNodesBySpc_id.put(new Long(specID), speciesN);
				speciesN.setHybrid(hybrid);
			}

			updateFootprint(footprint, speciesN);
			nodes.add(speciesN);

			if (curInfratype != null) {
				rankN = tree.getNode(TaxonNode.INFRARANK, curKingdom, curGenus + " " + curSpecies + " " + curInfratype
						+ " " + curInfraEpithet);
				if (rankN == null) {
					rankN = fetchNode(curKingdom, curGenus + curSpecies + curInfratype + curInfraEpithet);

					if (rankN == null) {
						rankN = TaxonNodeFactory.createNode((long) newNodeID, curInfraEpithet, TaxonNode.INFRARANK, "",
								curSpecies, false, curStat, "", "");

						rankN.setFootprint(footprint.toArray(new String[TaxonNode.INFRARANK]));

						if (curInfratype.trim().matches("^ssp\\.?$"))
							rankN.setInfraType(TaxonNode.INFRARANK_TYPE_SUBSPECIES);
						else if (curInfratype.trim().matches("^var\\.?$")
								&& rankN.getFootprint()[0].equalsIgnoreCase("PLANTAE"))
							rankN.setInfraType(TaxonNode.INFRARANK_TYPE_VARIETY);
						else {
							failed.set(true);
							statusMessage.append("Your DEM contains an infrarank with an invalid rank."
									+ " The valid ranks are \"ssp.\" and \"var.\", and a variety MUST be"
									+ " in the kingdom PLANTAE. Please change your data, and"
									+ " try the import again.<br>");
							return;
						}

						rankN.setFullName(rankN.generateFullName());
						spcNameToIDMap.put(rankN.getFullName(), new Long(specID));
					}

					rankN.setFullName(rankN.generateFullName());

					rankN.setTaxonomicAuthority(infraTaxonomicAuthority);

					speciesN.setAsParent(rankN);
					tree.addNode(curKingdom, rankN);
				}

				if (curSSP == null) {
					assessedNodesBySpc_id.put(new Long(specID), rankN);
					rankN.setHybrid(hybrid);
				}

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
						sspN = fetchNode(curKingdom, curGenus + curSpecies + curSSP);
					else
						sspN = fetchNode(curKingdom, curGenus + curSpecies + curInfratype + curInfraEpithet + curSSP);

					if (sspN == null) {
						sspN = TaxonNodeFactory.createNode((long) newNodeID, curSSP, TaxonNode.SUBPOPULATION, "",
								curSpecies, false, curStat, "", "");
						sspN.setTaxonomicAuthority(spcTaxonomicAuthority);

						if (curInfratype != null) {
							if (curInfratype.trim().matches("^ssp\\.?$"))
								rankN.setInfraType(TaxonNode.INFRARANK_TYPE_SUBSPECIES);
							else if (curInfratype.trim().matches("^var\\.?$")
									&& rankN.getFootprint()[0].equalsIgnoreCase("PLANTAE"))
								rankN.setInfraType(TaxonNode.INFRARANK_TYPE_VARIETY);
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
						sspN.setFootprint(footprint.toArray(new String[0]));
						sspN.setFullName(sspN.generateFullName());

						spcNameToIDMap.put(sspN.getFullName(), new Long(specID));
					}

					sspN.setFullName(sspN.generateFullName());
					speciesN.setAsParent(sspN);

					tree.addNode(curKingdom, sspN);
				}

				assessedNodesBySpc_id.put(new Long(specID), sspN);
				sspN.setHybrid(hybrid);
				nodes.add(sspN);
			}
		}
	}

	private void conservationMeasuresTableImport(Long curDEMid, AssessmentData curAssessment,
			HashMap<String, Object> data) throws DBException {
		SelectQuery select = new SelectQuery();
		select.select("conservation_measures", "*");
		select.constrain(new CanonicalColumnName("conservation_measures", "Sp_code"), QConstraint.CT_EQUALS, curDEMid);

		Row.Set rowLoader = new Row.Set();
		ec.doQuery(select, rowLoader);

		if (rowLoader == null)
			return;

		HashMap<String, ArrayList<String>> consSelected = new HashMap<String, ArrayList<String>>();
		HashMap<String, ArrayList<String>> researchSelected = new HashMap<String, ArrayList<String>>();
		for (Row curRow : rowLoader.getSet()) {
			int curCode = curRow.get("Measure_code").getInteger();
			int score = curRow.get("cm_timing").getInteger(Column.NEVER_NULL);

			try {
				switchToDBSession("demConversion");

				if (score == 1) // Do in-place conversion
				{
					try {
						doResearchInPlace(data, curCode);
					} catch (DBException ignored) {
					}
				}// End if in-place needed

				if (score == 2) // Do research needed conversion
				{
					try {
						doResearchNeeded(researchSelected, curCode);
					} catch (DBException ignored) {
					}
				}

				if (score == 2) // Do conservation needed conversion
				{
					try {
						doConservationNeeded(consSelected, curCode);
					} catch (DBException ignored) {
					}
				}

				data.put(CanonicalNames.ConservationActions, consSelected);
				data.put(CanonicalNames.Research, researchSelected);

				try {
					switchToDBSession("dem");
				} catch (Exception e) {
					e.printStackTrace();
					SysDebugger
							.getInstance()
							.println(
									"Error switching back to DEM after building Conservation actions. Import will assuredly fail.");
				}
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("Error checking against conversion table.");
			}
		}
	}

	private void countryTableImport(Long curDEMid, AssessmentData curAssessment, HashMap<String, Object> data)
			throws Exception {
		SelectQuery select = new SelectQuery();
		select.select("coding_occurence", "*");
		select.constrain(new CanonicalColumnName("coding_occurence", "Sp_code"), QConstraint.CT_EQUALS, curDEMid);
		select.constrain(new CanonicalColumnName("coding_occurence", "co_type"), QConstraint.CT_EQUALS,
				COUNTRY_CODING_OCCURRENCE_TYPE);

		Row.Set rowLoader = new Row.Set();
		ec.doQuery(select, rowLoader);

		if (rowLoader == null)
			return;

		HashMap<String, ArrayList<String>> selected = new HashMap<String, ArrayList<String>>();
		for (Iterator<Row> iter = rowLoader.getSet().listIterator(); iter.hasNext();) {
			Row curRow = iter.next();

			String country_number = curRow.get("obj_id").getString(Column.NEVER_NULL);

			ArrayList<String> dataList = new ArrayList<String>();
			fetchCodingOccurrenceData(curRow, dataList);

			selected.put(fetchCountryIsoCode(country_number), dataList);
		}
		switchToDBSession("dem");

		select = new SelectQuery();
		select.select("coding_occurence", "*");
		select.constrain(new CanonicalColumnName("coding_occurence", "Sp_code"), QConstraint.CT_EQUALS, curDEMid);
		select.constrain(new CanonicalColumnName("coding_occurence", "co_type"), QConstraint.CT_EQUALS,
				SUBCOUNTRY_CODING_OCCURRENCE_TYPE);

		rowLoader = new Row.Set();
		ec.doQuery(select, rowLoader);
		for (Iterator<Row> iter = rowLoader.getSet().listIterator(); iter.hasNext();) {
			Row curRow = iter.next();

			String subcountry_number = curRow.get("obj_id").getString(Column.NEVER_NULL);

			ArrayList<String> dataList = new ArrayList<String>();
			fetchCodingOccurrenceData(curRow, dataList);

			selected.put(fetchSubcountryIsoCode(subcountry_number), dataList);
		}

		switchToDBSession("dem");
		data.put(CanonicalNames.CountryOccurrence, selected);
	}

	private ArrayList<String> createDataArray(String data, boolean cleanXML) {
		ArrayList<String> dataList = new ArrayList<String>();
		dataList.add(cleanXML ? XMLUtils.clean(data) : data);

		return dataList;
	}

	private ArrayList<String> createDataArray(String data1, String data2) {
		ArrayList<String> dataList = new ArrayList<String>();
		dataList.add(XMLUtils.clean(data1));
		dataList.add(XMLUtils.clean(data2));

		return dataList;
	}

	private ArrayList<String> createDataArray(String data1, String data2, String data3) {
		ArrayList<String> dataList = new ArrayList<String>();
		dataList.add(XMLUtils.clean(data1));
		dataList.add(XMLUtils.clean(data2));
		dataList.add(XMLUtils.clean(data3));

		return dataList;
	}

	private ArrayList<String> createDataArray(String data1, String data2, String data3, String data4) {
		ArrayList<String> dataList = new ArrayList<String>();
		dataList.add(XMLUtils.clean(data1));
		dataList.add(XMLUtils.clean(data2));
		dataList.add(XMLUtils.clean(data3));
		dataList.add(XMLUtils.clean(data4));

		return dataList;
	}

	private ArrayList<String> createDataArray(String[] data) {
		ArrayList<String> dataList = new ArrayList<String>();

		for (int i = 0; i < data.length; i++)
			dataList.add(XMLUtils.clean(data[i]));

		return dataList;
	}

	private void distributionTableImport(Long curDEMid, AssessmentData curAssessment, HashMap<String, Object> data)
			throws DBException {
		SelectQuery select = new SelectQuery();
		select.select("Distribution", "*");
		select.constrain(new CanonicalColumnName("Distribution", "Sp_code"), QConstraint.CT_EQUALS, curDEMid);

		Row.Loader rowLoader = new Row.Loader();
		ec.doQuery(select, rowLoader);
		Row row = rowLoader.getRow();

		if (row == null)
			return;

		// Biogeographic Realm
		ArrayList<String> dataList = new ArrayList<String>();
		String biogeographicRealms = "";
		if (row.get("Afrotropical").getInteger(Column.NEVER_NULL) == 1)
			biogeographicRealms += "1,";
		if (row.get("Antarctic").getInteger(Column.NEVER_NULL) == 1)
			biogeographicRealms += "2,";
		if (row.get("Australasian").getInteger(Column.NEVER_NULL) == 1)
			biogeographicRealms += "3,";
		if (row.get("Indomalayan").getInteger(Column.NEVER_NULL) == 1)
			biogeographicRealms += "4,";
		if (row.get("Nearctic").getInteger(Column.NEVER_NULL) == 1)
			biogeographicRealms += "5,";
		if (row.get("Neotropical").getInteger(Column.NEVER_NULL) == 1)
			biogeographicRealms += "6,";
		if (row.get("Oceanian").getInteger(Column.NEVER_NULL) == 1)
			biogeographicRealms += "7,";
		if (row.get("Palearctic").getInteger(Column.NEVER_NULL) == 1)
			biogeographicRealms += "8,";
		if (biogeographicRealms.endsWith(","))
			biogeographicRealms = biogeographicRealms.substring(0, biogeographicRealms.length() - 1);
		dataList.add(biogeographicRealms);
		data.put(CanonicalNames.BiogeographicRealm, dataList);

		// System
		dataList = new ArrayList<String>();
		String systems = "";
		if (row.get("Terrestrial").getInteger(Column.NEVER_NULL) == 1)
			systems += "1,";
		if (row.get("Freshwater").getInteger(Column.NEVER_NULL) == 1)
			systems += "2,";
		if (row.get("Marine").getInteger(Column.NEVER_NULL) == 1)
			systems += "3,";
		if (systems.endsWith(","))
			systems = systems.substring(0, systems.length() - 1);
		else
			systems = "0";
		dataList.add(systems);
		data.put(CanonicalNames.System, dataList);

		// Movement Patterns
		dataList = new ArrayList<String>();
		String movementPatterns = "";
		if (row.get("Nomadic").getInteger(Column.NEVER_NULL) == 1)
			movementPatterns += "1,";
		if (row.get("Congregatory").getInteger(Column.NEVER_NULL) == 1)
			movementPatterns += "2,";
		if (row.get("Migratory").getInteger(Column.NEVER_NULL) == 1)
			movementPatterns += "3,";
		if (row.get("Altitudinally_migrant").getInteger(Column.NEVER_NULL) == 1)
			movementPatterns += "4,";
		if (movementPatterns.endsWith(","))
			movementPatterns = movementPatterns.substring(0, movementPatterns.length() - 1);
		else
			movementPatterns = "0";

		dataList.add(movementPatterns);
		data.put(CanonicalNames.MovementPatterns, dataList);

		// Map Status
		dataList = new ArrayList<String>();
		String mapStatus = row.get("Map_status").getString(Column.EMPTY_IS_NULL);
		String mapStatusSelected = "0";
		if (mapStatus == null || mapStatus.matches("\\s"))
			mapStatusSelected = "0";
		else if (mapStatus.equalsIgnoreCase("done"))
			mapStatusSelected = "1";
		else if (mapStatus.equalsIgnoreCase("missing"))
			mapStatusSelected = "2";
		else if (mapStatus.equalsIgnoreCase("incomplete"))
			mapStatusSelected = "3";
		else if (mapStatus.equalsIgnoreCase("not possible"))
			mapStatusSelected = "4";
		else {
			try {
				String message = "Unparseable map status value from DEM import: " + mapStatus;

				sendNote(curAssessment.getType(), message, curAssessment.getAssessmentID(), CanonicalNames.MapStatus);
			} catch (Exception e) {
				System.out.println("Unable to attach note: stack trace follows.");
				e.printStackTrace();
			}
		}

		dataList.add(mapStatusSelected);
		data.put(CanonicalNames.MapStatus, dataList);

		data.put(CanonicalNames.ElevationLower, createDataArray(row.get("lower_elev").getString(Column.NEVER_NULL),
				true));

		data.put(CanonicalNames.ElevationUpper, createDataArray(row.get("upper_elev").getString(Column.NEVER_NULL),
				true));

		data.put(CanonicalNames.DepthUpper, createDataArray(row.get("upper_depth").getString(Column.NEVER_NULL), true));

		data.put(CanonicalNames.DepthLower, createDataArray(row.get("lower_depth").getString(Column.NEVER_NULL), true));

		// DepthZone
		dataList = new ArrayList<String>();
		String depthZone = "";
		if (row.get("Shallow_photic").getInteger(Column.NEVER_NULL) == 1)
			depthZone += "1,";
		if (row.get("Photic").getInteger(Column.NEVER_NULL) == 1)
			depthZone += "2,";
		if (row.get("Bathyl").getInteger(Column.NEVER_NULL) == 1)
			depthZone += "3,";
		if (row.get("Abyssal").getInteger(Column.NEVER_NULL) == 1)
			depthZone += "4,";
		if (row.get("Hadal").getInteger(Column.NEVER_NULL) == 1)
			depthZone += "5,";
		if (depthZone.endsWith(","))
			depthZone = depthZone.substring(0, depthZone.length() - 1);
		else
			depthZone = "0";
		dataList.add(depthZone);
		data.put(CanonicalNames.DepthZone, dataList);

		dataList = new ArrayList<String>();
		dataList.add(row.get("AOO").getString(Column.NEVER_NULL));
		dataList.add("");
		data.put(CanonicalNames.AOO, dataList);

		dataList = new ArrayList<String>();
		dataList.add(row.get("EOO").getString(Column.NEVER_NULL));
		dataList.add("0");
		data.put(CanonicalNames.EOO, dataList);
	}

	private void doConservationNeeded(HashMap<String, ArrayList<String>> consSelected, int curCode) throws DBException {
		String notes;
		// DO Straight conversion
		SelectQuery conversionSelect = new SelectQuery();
		conversionSelect.select("CONS ACTIONS MAPPING OLD TO NEW", "*");
		conversionSelect.constrain(new CanonicalColumnName("CONS ACTIONS MAPPING OLD TO NEW", "oldActionID"),
				QConstraint.CT_EQUALS, curCode);

		Row.Loader conversionRow = new Row.Loader();

		ec.doQuery(conversionSelect, conversionRow);

		if (conversionRow.getRow() != null) {
			curCode = conversionRow.getRow().get("newActionID").getInteger();
			notes = conversionRow.getRow().get("Comment").getString(Column.NEVER_NULL);

			if (!consSelected.containsKey("" + curCode))
				consSelected.put("" + curCode, createDataArray(notes, true));
			else if (!notes.equals("")) {
				String temp = consSelected.get("" + curCode).get(0);
				temp += " --- \n" + XMLUtils.clean(notes);
				consSelected.put("" + curCode, createDataArray(temp, true));
			}
		}
	}

	private void doResearchInPlace(HashMap<String, Object> data, int curCode) throws DBException {
		String notes;
		SelectQuery conversionSelect = new SelectQuery();
		conversionSelect.select("OldConsActions to new Actions IN Place", "*");
		conversionSelect.constrain(new CanonicalColumnName("OldConsActions to new Actions IN Place", "oldaction"),
				QConstraint.CT_EQUALS, curCode);

		Row.Loader conversionRow = new Row.Loader();

		ec.doQuery(conversionSelect, conversionRow);

		if (conversionRow.getRow() != null) {
			curCode = conversionRow.getRow().get("newaction").getInteger();
			notes = conversionRow.getRow().get("comment").getString(Column.NEVER_NULL);

			if (curCode > 0 && curCode < 3) {
				ArrayList<String> dataList;

				if (data.containsKey(CanonicalNames.InPlaceResearch))
					dataList = (ArrayList<String>) data.get(CanonicalNames.InPlaceResearch);
				else
					dataList = createDataArray("0", "", "0", "");

				if (curCode == 1)
					curCode--;

				dataList.remove(curCode);
				dataList.add(curCode, "1");
				dataList.remove(curCode + 1);
				dataList.add(curCode + 1, notes);

				data.put(CanonicalNames.InPlaceResearch, dataList);
			} else if (curCode >= 3 && curCode < 9) {
				curCode -= 3;

				ArrayList<String> dataList;

				if (data.containsKey(CanonicalNames.InPlaceLandWaterProtection))
					dataList = (ArrayList<String>) data.get(CanonicalNames.InPlaceLandWaterProtection);
				else
					dataList = createDataArray(new String[] { "0", "0", "", "0", "", "", "0", "" });

				if (curCode == 5)
					curCode++;

				dataList.remove(curCode);
				dataList.add(curCode, "1");
				if (curCode < 4) {
					dataList.remove(curCode + 2);
					dataList.add(curCode + 2, notes);
				} else {
					dataList.remove(curCode + 1);
					dataList.add(curCode + 1, notes);
				}

				data.put(CanonicalNames.InPlaceLandWaterProtection, dataList);
			} else if (curCode >= 9 && curCode < 12) {
				curCode -= 9;

				ArrayList<String> dataList;

				if (data.containsKey(CanonicalNames.InPlaceSpeciesManagement))
					dataList = (ArrayList<String>) data.get(CanonicalNames.InPlaceSpeciesManagement);
				else
					dataList = createDataArray(new String[] { "0", "", "0", "", "0", "" });

				curCode = curCode * 2;

				dataList.remove(curCode);
				dataList.add(curCode, "1");
				dataList.remove(curCode + 1);
				dataList.add(curCode + 1, notes);

				data.put(CanonicalNames.InPlaceSpeciesManagement, dataList);
			} else if (curCode >= 12 && curCode < 15) {
				curCode -= 12;

				ArrayList<String> dataList;

				if (data.containsKey(CanonicalNames.InPlaceEducation))
					dataList = (ArrayList<String>) data.get(CanonicalNames.InPlaceEducation);
				else
					dataList = createDataArray(new String[] { "0", "", "0", "", "0", "" });

				curCode = curCode * 2;

				dataList.remove(curCode);
				dataList.add(curCode, "1");
				dataList.remove(curCode + 1);
				dataList.add(curCode + 1, notes);

				data.put(CanonicalNames.InPlaceEducation, dataList);
			}
		}
	}

	private void doResearchNeeded(HashMap<String, ArrayList<String>> researchSelected, int curCode) throws DBException {
		String notes;
		SelectQuery conversionSelect = new SelectQuery();
		conversionSelect.select("OLD CONS ACT to RESEARCH MAPPING", "*");
		conversionSelect.constrain(new CanonicalColumnName("OLD CONS ACT to RESEARCH MAPPING", "old ActionAFID"),
				QConstraint.CT_EQUALS, curCode);

		Row.Loader conversionRow = new Row.Loader();

		ec.doQuery(conversionSelect, conversionRow);

		if (conversionRow.getRow() != null) {
			curCode = conversionRow.getRow().get("newResearchAFID").getInteger();
			notes = conversionRow.getRow().get("comments").getString(Column.NEVER_NULL);

			if (!researchSelected.containsKey("" + curCode))
				researchSelected.put("" + curCode, createDataArray(notes, true));
			else if (!notes.equals("")) {
				String temp = researchSelected.get("" + curCode).get(0);
				temp += " --- \n" + XMLUtils.clean(notes);
				researchSelected.put("" + curCode, createDataArray(temp, true));
			}
		}
	}

	private void ecosystemServicesTableImport(Long curDEMid, AssessmentData curAssessment, HashMap<String, Object> data)
			throws DBException {
		SelectQuery select = new SelectQuery();
		select.select("ecosystem_services", "*");
		select.constrain(new CanonicalColumnName("ecosystem_services", "Sp_code"), QConstraint.CT_EQUALS, curDEMid);

		Row.Set rowLoader = new Row.Set();
		ec.doQuery(select, rowLoader);

		if (rowLoader == null)
			return;

		HashMap<String, ArrayList<String>> selected = new HashMap<String, ArrayList<String>>();
		for (Iterator<Row> iter = rowLoader.getSet().listIterator(); iter.hasNext();) {
			Row curRow = iter.next();

			int curCode = 1;
			int curRank;
			String curScale;

			data.put(CanonicalNames.EcosystemServicesInsufficientInfo, createDataArray(""
					+ (curRow.get("info_available").getInteger(Column.NEVER_NULL) == 1), true));
			data.put(CanonicalNames.EcosystemServicesProvidesNone, createDataArray(""
					+ (curRow.get("no_services").getInteger(Column.NEVER_NULL) == 1), true));

			for (int i = 4; i < 30; i += 2) {
				curRank = curRow.get(i).getInteger(Column.NEVER_NULL);
				curScale = curRow.get(i + 1).getString(Column.NEVER_NULL);
				if (curScale.equals(""))
					curScale = "0";

				selected.put("" + curCode, createDataArray("" + curRank, "" + curScale));
				curCode++;
			}

			if (curRow.get("specify_other1").getString() != null) {
				curRank = curRow.get("Other_rank1").getInteger(Column.NEVER_NULL);
				curScale = curRow.get("Other_scale1").getString(Column.NEVER_NULL);
				if (curScale.equals(""))
					curScale = "0";

				String content = curRow.get("specify_other1").getString();
				selected.put("" + curCode, createDataArray("" + curRank, "" + curScale));
				curCode++;

				sendNote(curAssessment.getType(), "Other 1 specified as: " + content, curAssessment.getAssessmentID(),
						CanonicalNames.EcosystemServices);
			}
			if (curRow.get("specify_other2").getString() != null) {
				curRank = curRow.get("Other_rank2").getInteger(Column.NEVER_NULL);
				curScale = curRow.get("Other_scale2").getString(Column.NEVER_NULL);
				if (curScale.equals(""))
					curScale = "0";

				String content = curRow.get("specify_other2").getString();
				selected.put("" + curCode, createDataArray("" + curRank, "" + curScale));
				curCode++;

				sendNote(curAssessment.getType(), "Other 2 specified as: " + content, curAssessment.getAssessmentID(),
						CanonicalNames.EcosystemServices);
			}
		}

		data.put(CanonicalNames.EcosystemServices, selected);
	}

	private void exportAssessments() throws Exception {
		assessmentConverter = new AssessmentConverter("sis_lookups");
		
		WorkingSet ws = new WorkingSet();
		ws.setCreatedDate(new Date());
		ws.setCreator(user);
		ws.setDescription("Import on " + ws.getCreatedDate());
		ws.setCreator(user);
		ws.setName(user + ws.getCreatedDate());

		Set<Taxon> speciesIDs = new HashSet<Taxon>();
		for (Iterator<AssessmentData> iter = assessments.values().iterator(); iter.hasNext();) {
			AssessmentData assessment = iter.next();
			assessment.setAssessmentID("0");
			Assessment newAss = assessmentConverter.assessmentDataToAssessment(assessment);
			
			if (newAss == null)
				System.out.println("Error creating draft assessment for  " + assessment.getSpeciesName());
			else {
				Taxon t = SIS.get().getTaxonIO().getTaxon(session, Integer.valueOf(assessment.getSpeciesID()));
				newAss.setTaxon(t);
				t.getAssessments().add(newAss);
				speciesIDs.add(t);
				
				SIS.get().getAssessmentIO().saveNewAssessment(newAss, userObj);
			}
		}

		ws.setTaxon(speciesIDs);
		if (!SIS.get().getWorkingSetIO().saveWorkingSet(ws, userObj) )
			System.out.println("Error putting new working set!");
	}

	private void exportNodes() throws Exception {
		log.append("<tr><td align=\"center\" colspan=\"9\">" + "<b><u>Newly Created Taxa</u></b></td></tr>");

		for (Object curEntry : tree.getKingdoms().values()) {
			Kingdom curKingdom = (Kingdom) curEntry;

			for (HashMap curLevel : curKingdom.getLevels()) {
				for (Iterator<TaxonNode> iter = curLevel.values().iterator(); iter.hasNext();) {
					TaxonNode cur = iter.next();

					if (cur.getId() == newNodeID) {
						logNode(cur);
						Taxon newTaxon = TaxonConverter.convertTaxonNode(cur, new Date());
						if( SIS.get().getTaxonIO().writeTaxon(newTaxon, userObj) )
							cur.setId(newTaxon.getId());
						else {
							failed.set(true);
							statusMessage.append("Failed to save taxon, " + cur.getFullName()
									+ ".<br>");
							statusMessage.append("Please forward this message: <br>");
							statusMessage.append("Failed TaxaIO call ");
							statusMessage.append("with XML <br>" + newTaxon.toXML() + "<br>");
							return;
						}
					} else if (cur.getStatus().contains("changed")) {
						cur.setStatus(cur.getStatus().replace("changed", ""));
						Taxon newTaxon = TaxonConverter.convertTaxonNode(cur, new Date());
						Taxon existingTaxon = SIS.get().getTaxonIO().getTaxon(session, newTaxon.getId());
						existingTaxon.getCommonNames().addAll(newTaxon.getCommonNames());
						existingTaxon.getSynonyms().addAll(newTaxon.getSynonyms());
						
						if( !SIS.get().getTaxonIO().writeTaxon(existingTaxon, userObj) ) {
							failed.set(true);
							statusMessage.append("Failed to save taxon, " + cur.getFullName()
									+ ".<br>");
							statusMessage.append("Please forward this message: <br>");
							statusMessage.append("Failed TaxaIO call ");
							statusMessage.append("with XML <br>" + existingTaxon.toXML() + "<br>");
							return;
						}
					}
				}
			}
		}
	}

	private void faoMarineTableImport(Long curDEMid, AssessmentData curAssessment, HashMap<String, Object> data)
			throws Exception {
		SelectQuery select = new SelectQuery();
		select.select("coding_occurence", "*");
		select.constrain(new CanonicalColumnName("coding_occurence", "Sp_code"), QConstraint.CT_EQUALS, curDEMid);
		select.constrain(new CanonicalColumnName("coding_occurence", "co_type"), QConstraint.CT_EQUALS,
				FAO_CODING_OCCURRENCE_TYPE);

		Row.Set rowLoader = new Row.Set();
		ec.doQuery(select, rowLoader);

		if (rowLoader == null)
			return;

		HashMap<String, ArrayList<String>> selected = new HashMap<String, ArrayList<String>>();
		for (Iterator<Row> iter = rowLoader.getSet().listIterator(); iter.hasNext();) {
			Row curRow = iter.next();

			String curCode = curRow.get("obj_id").getString();

			ArrayList<String> dataList = new ArrayList<String>();
			fetchCodingOccurrenceData(curRow, dataList);

			selected.put(curCode, dataList);
		}

		data.put(CanonicalNames.FAOOccurrence, selected);
	}

	private void fetchCodingOccurrenceData(Row codingRow, ArrayList<String> dataList) throws Exception {
		String p_code = "0";
		String o_code = "0";
		String m_code = "0";

		if (codingRow != null) {
			p_code = codingRow.get("p_code").getString(Column.NEVER_NULL);
			o_code = codingRow.get("o_code").getString(Column.NEVER_NULL);
			m_code = codingRow.get("m_code").getString(Column.NEVER_NULL);

			if (p_code.equals("9"))
				p_code = "6";
			if (o_code.equals("9"))
				o_code = "5";
		}

		dataList.add(p_code);
		dataList.add(new Boolean(m_code.equals("1")).toString());
		dataList.add(o_code);
	}

	private String fetchCountryIsoCode(String country_number) throws Exception {
		switchToDBSession("demSource");

		SelectQuery select = new SelectQuery();
		select.select("countries_list_all", "Country_code");
		select.select("countries_list_all", "Country_Number");
		select.constrain(new CanonicalColumnName("countries_list_all", "Country_Number"), QConstraint.CT_EQUALS,
				country_number);

		Row.Loader code = new Row.Loader();
		ec.doQuery(select, code);

		return code.getRow().get("Country_code").getString().trim();
	}

	private TaxonNode fetchNode(String kingdomName, String fullName) throws Exception {
		Taxon t = SIS.get().getTaxonIO().readTaxonByName(kingdomName, fullName);
		if( t != null )
			return TaxonNodeFactory.createNode((long)t.getId(), t.getName(), t.getLevel(),
					t.getParentId()+"", t.getParentName(), false, "", "", "");
		else
			return null;
	}

	private String fetchSubcountryIsoCode(String subcountry_number) throws Exception {
		switchToDBSession("demSource");

		SelectQuery select = new SelectQuery();
		select.select("subcountry_list_all", "BruLevel4Code");
		select.select("subcountry_list_all", "subcountry_number");
		select.constrain(new CanonicalColumnName("subcountry_list_all", "subcountry_number"), QConstraint.CT_EQUALS,
				subcountry_number);

		Row.Loader code = new Row.Loader();
		ec.doQuery(select, code);

		return code.getRow().get("BruLevel4Code").getString().trim();
	}

	private void growthFormTableImport(Long curDEMid, AssessmentData curAssessment, HashMap<String, Object> data)
			throws DBException {
		SelectQuery select = new SelectQuery();
		select.select("growth_form_table", "*");
		select.constrain(new CanonicalColumnName("growth_form_table", "Sp_code"), QConstraint.CT_EQUALS, curDEMid);

		Row.Set rowLoader = new Row.Set();
		ec.doQuery(select, rowLoader);

		if (rowLoader == null)
			return;

		HashMap<String, ArrayList<String>> selected = new HashMap<String, ArrayList<String>>();
		for (Iterator<Row> iter = rowLoader.getSet().listIterator(); iter.hasNext();) {
			Row curRow = iter.next();

			String curCode = curRow.get("Growthform_code").getString();
			selected.put(curCode, new ArrayList<String>());
		}

		data.put(CanonicalNames.PlantGrowthForms, selected);
	}

	private void habitatTableImport(Long curDEMid, AssessmentData curAssessment, HashMap<String, Object> data)
			throws DBException {
		SelectQuery select = new SelectQuery();
		select.select("General_habitat", "*");
		select.constrain(new CanonicalColumnName("General_habitat", "Sp_code"), QConstraint.CT_EQUALS, curDEMid);

		Row.Set rowLoader = new Row.Set();
		ec.doQuery(select, rowLoader);

		if (rowLoader == null)
			return;

		HashMap<String, ArrayList<String>> selected = new HashMap<String, ArrayList<String>>();
		for (Row curRow : rowLoader.getSet()) {
			String curCode = curRow.get("Gh_N").getString();
			int majorImportance = curRow.get("major_importance").getInteger(Column.NEVER_NULL);
			int curTiming = curRow.get("Score").getInteger(Column.NEVER_NULL);

			if (curTiming == 9)
				curTiming = 3; // 3 is index of "possible" answer

			if (majorImportance == 1)
				majorImportance = 0;
			else if (majorImportance > 0) // 3 is index of "No", 2 of "Yes"
				majorImportance--;

			ArrayList<String> habitatData = new ArrayList<String>();
			habitatData.add("" + curTiming);
			habitatData.add("" + majorImportance);

			selected.put(curCode, habitatData);
		}

		data.put(CanonicalNames.GeneralHabitats, selected);
	}

	private boolean isValidRangeFormat(String text) {
		boolean ret = text.equals("")
				|| text.matches("(\\s)*(\\d)*(\\.)?(\\d)+(\\s)*((-)((\\s)*(\\d)*(\\.)?(\\d)+))?")
				|| text.matches("(\\s)*(\\d)*(\\.)?(\\d)+(\\s)*(-)(\\s)*(\\d)*(\\.)?(\\d)+(\\s)*"
						+ "(,)(\\s)*(\\d)*(\\.)?(\\d)+(\\s)*((-)((\\s)*(\\d)*(\\.)?(\\d)+))?");

		// System.out.println("Checking against text " + text +
		// " : result is " + ret );

		return ret;
	}

	private void lakesTableImport(Long curDEMid, AssessmentData curAssessment, HashMap<String, Object> data)
			throws DBException {
		SelectQuery select = new SelectQuery();
		select.select("lakes_table", "*");
		select.constrain(new CanonicalColumnName("lakes_table", "Sp_code"), QConstraint.CT_EQUALS, curDEMid);

		Row.Set rowLoader = new Row.Set();
		ec.doQuery(select, rowLoader);

		if (rowLoader == null)
			return;

		HashMap<String, ArrayList<String>> selected = new HashMap<String, ArrayList<String>>();
		for (Iterator<Row> iter = rowLoader.getSet().listIterator(); iter.hasNext();) {
			Row curRow = iter.next();

			String curCode = curRow.get("lake_number").getString();

			ArrayList<String> dataList = new ArrayList<String>();
			dataList.add("0");
			dataList.add("false");
			dataList.add("0");

			selected.put(curCode, dataList);
		}

		data.put(CanonicalNames.Lakes, selected);
	}

	private void landCoverTableImport(Long curDEMid, AssessmentData curAssessment, HashMap<String, Object> data)
			throws DBException {
		SelectQuery select = new SelectQuery();
		select.select("Land_cover", "*");
		select.constrain(new CanonicalColumnName("Land_cover", "Sp_code"), QConstraint.CT_EQUALS, curDEMid);

		Row.Set rowLoader = new Row.Set();
		ec.doQuery(select, rowLoader);

		if (rowLoader == null)
			return;

		HashMap<String, ArrayList<String>> selected = new HashMap<String, ArrayList<String>>();
		for (Iterator<Row> iter = rowLoader.getSet().listIterator(); iter.hasNext();) {
			Row curRow = iter.next();

			String curCode = curRow.get("Lc_N").getString();
			int score = curRow.get("Score").getInteger(Column.NEVER_NULL);

			if (score == 9)
				score = 3; // 3 is index of possible answer

			ArrayList<String> landCoverData = new ArrayList<String>();
			landCoverData.add("" + score);

			selected.put(curCode, landCoverData);
		}

		data.put(CanonicalNames.LandCover, selected);
	}

	private void lifeHistoryTableImport(Long curDEMid, AssessmentData curAssessment, HashMap<String, Object> data)
			throws DBException {
		SelectQuery select = new SelectQuery();
		select.select("life_history", "*");
		select.constrain(new CanonicalColumnName("life_history", "Sp_code"), QConstraint.CT_EQUALS, curDEMid);

		Row.Loader rowLoader = new Row.Loader();

		try {
			ec.doQuery(select, rowLoader);
		} catch (Exception e) {
			// NO LIFE HISTORY RECORDS FOR THIS BABY
			return;
		}

		Row row = rowLoader.getRow();

		if (row == null)
			return;

		// AGE MATURITY UNITS - FOR BOTH FEMALE AND MALE
		String units = row.get("age_maturity_units").getString(Column.NEVER_NULL);
		if (units.equals(""))
			units = "0";
		else if (units.equalsIgnoreCase("days"))
			units = "1";
		else if (units.equalsIgnoreCase("weeks"))
			units = "2";
		else if (units.equalsIgnoreCase("months"))
			units = "3";
		else if (units.equalsIgnoreCase("years"))
			units = "4";
		else {
			String message = "Unparseable units value from DEM import. Imported value: " + units;

			try {
				sendNote(curAssessment.getType(), message, curAssessment.getAssessmentID(),
						CanonicalNames.FemaleMaturityAge);
				sendNote(curAssessment.getType(), message, curAssessment.getAssessmentID(),
						CanonicalNames.MaleMaturityAge);

				units = "";
			} catch (Exception e) {
				System.out.println("Unable to attach note: stack trace follows.");
				e.printStackTrace();
				units = "";
			}
		}

		data.put(CanonicalNames.FemaleMaturityAge, createDataArray(row.get("f_age_maturity").getString(
				Column.NEVER_NULL), units));

		data.put(CanonicalNames.MaleMaturityAge, createDataArray(
				row.get("m_age_maturity").getString(Column.NEVER_NULL), units));

		// FEMALE MATURITY SIZE
		data.put(CanonicalNames.FemaleMaturitySize, createDataArray(row.get("f_size_maturity").getString(
				Column.NEVER_NULL), true));

		// MALE MATURITY SIZE
		data.put(CanonicalNames.MaleMaturitySize, createDataArray(row.get("m_size_maturity").getString(
				Column.NEVER_NULL), true));

		// MAX SIZE
		data.put(CanonicalNames.MaxSize, createDataArray(row.get("max_size").getString(Column.NEVER_NULL), true));

		// Birth SIZE
		data.put(CanonicalNames.BirthSize, createDataArray(row.get("birth_size").getString(Column.NEVER_NULL), true));

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
			sendNote(curAssessment.getType(), CANNED_NOTE + generations + " with justification: " + genJust, curAssessment.getAssessmentID(),
					CanonicalNames.GenerationLength);
		else
			data.put(CanonicalNames.GenerationLength, createDataArray(mod_generations, genJust));

		// REPRODUCTIVE PERIODICITY
		data.put(CanonicalNames.ReproduictivePeriodicity, createDataArray(row.get("reproductive_periodicity")
				.getString(Column.NEVER_NULL), true));

		// LITTER SIZE
		data.put(CanonicalNames.AvgAnnualFecundity, createDataArray(
				row.get("litter_size").getString(Column.NEVER_NULL), true));

		// POPULATION INCREASE RATE
		data.put(CanonicalNames.PopulationIncreaseRate, createDataArray(row.get("rate_pop_increase").getString(
				Column.NEVER_NULL), true));

		// MORTALITY
		data.put(CanonicalNames.NaturalMortality, createDataArray(row.get("mortality").getString(Column.NEVER_NULL),
				true));

		// EGG LAYING
		data.put(CanonicalNames.EggLaying, createDataArray(
				row.get("egg_laying").getInteger(Column.NEVER_NULL) == 1 ? "true" : "false", true));

		// LARVAL
		data.put(CanonicalNames.FreeLivingLarvae, createDataArray(
				row.get("larval").getInteger(Column.NEVER_NULL) == 1 ? "true" : "false", true));

		// LIVE YOUNG
		data.put(CanonicalNames.LiveBirth, createDataArray(
				row.get("live_young").getInteger(Column.NEVER_NULL) == 1 ? "true" : "false", true));

		// PARTHENOGENSIS
		data.put(CanonicalNames.Parthenogenesis, createDataArray(row.get("parthenogenesis").getInteger(
				Column.NEVER_NULL) == 1 ? "true" : "false", true));

		// WATER BREEDING
		data.put(CanonicalNames.WaterBreeding, createDataArray(
				row.get("water_breeding").getInteger(Column.NEVER_NULL) == 1 ? "true" : "false", true));

		// LONGEVITY
		units = row.get("longevity_units").getString(Column.NEVER_NULL);
		if (units.equals(""))
			units = "0";
		else if (units.equalsIgnoreCase("days"))
			units = "1";
		else if (units.equalsIgnoreCase("months"))
			units = "2";
		else if (units.equalsIgnoreCase("years"))
			units = "3";
		else {
			String message = "Unparseable units value from DEM import. Imported value: " + units;

			try {
				sendNote(curAssessment.getType(), message, curAssessment.getAssessmentID(), CanonicalNames.Longevity);
				units = "";
			} catch (Exception e) {
				System.out.println("Unable to attach note: stack trace follows.");
				e.printStackTrace();
				units = "";
			}
		}
		data.put(CanonicalNames.Longevity, createDataArray(row.get("longevity").getString(Column.NEVER_NULL), units));

		// REPRODUCTIVE AGE
		units = row.get("ave_reproductive_age_units").getString(Column.NEVER_NULL);
		if (units.equals(""))
			units = "0";
		else if (units.equalsIgnoreCase("days"))
			units = "1";
		else if (units.equalsIgnoreCase("months"))
			units = "2";
		else if (units.equalsIgnoreCase("years"))
			units = "3";
		else {
			String message = "Unparseable units value from DEM import. Imported value: " + units;

			try {
				sendNote(curAssessment.getType(), message, curAssessment.getAssessmentID(),
						CanonicalNames.AvgReproductiveAge);
				units = "";
			} catch (Exception e) {
				System.out.println("Unable to attach note: stack trace follows.");
				e.printStackTrace();
				units = "";
			}
		}
		data.put(CanonicalNames.AvgReproductiveAge, createDataArray(row.get("ave_reproductive_age").getString(
				Column.NEVER_NULL), units));

		// GESTATION
		units = row.get("gestation_units").getString(Column.NEVER_NULL);
		if (units.equals(""))
			units = "0";
		else if (units.equalsIgnoreCase("days"))
			units = "1";
		else if (units.equalsIgnoreCase("months"))
			units = "2";
		else if (units.equalsIgnoreCase("years"))
			units = "3";
		else {
			String message = "Unparseable units value from DEM import on. Imported value: " + units;

			try {
				sendNote(curAssessment.getType(), message, curAssessment.getAssessmentID(),
						CanonicalNames.GestationTime);
				units = "";
			} catch (Exception e) {
				System.out.println("Unable to attach note: stack trace follows.");
				e.printStackTrace();
				units = "";
			}
		}
		data.put(CanonicalNames.GestationTime,
				createDataArray(row.get("gestation").getString(Column.NEVER_NULL), units));
	}

	private void livelihoodsTableImport(Long curDEMid, AssessmentData curAssessment, HashMap<String, Object> data)
			throws DBException {
		SelectQuery select = new SelectQuery();
		select.select("Livelihoods", "*");
		select.constrain(new CanonicalColumnName("Livelihoods", "Sp_code"), QConstraint.CT_EQUALS, curDEMid);

		Row.Set rowLoader = new Row.Set();
		ec.doQuery(select, rowLoader);

		if (rowLoader == null)
			return;

		ArrayList<String> selected = new ArrayList<String>();

		int numSelected = 0;
		for (Iterator<Row> iter = rowLoader.getSet().listIterator(); iter.hasNext();) {
			Row curRow = iter.next();

			ArrayList<String> curLivelihood = SISLivelihoods.generateDefaultDataList();

			boolean noInformation = curRow.get("data_available").getInteger(Column.NEVER_NULL) == 1;
			selected.add("" + noInformation);

			int count = 0;
			Integer scale = curRow.get("Assess_type_ID").getInteger(Column.NEVER_NULL);
			curLivelihood.set(count++, scale.toString());

			String regionName = curRow.get("Assess_name").getString(Column.NEVER_NULL);
			curLivelihood.set(count++, regionName);
			curLivelihood.set(count++, curRow.get("Assess_date").getString(Column.NEVER_NULL));
			curLivelihood.set(count++, curRow.get("p_product").getString(Column.NEVER_NULL));
			curLivelihood.set(count++, curRow.get("p_Single_harvest_amount").getString(Column.NEVER_NULL));
			curLivelihood.set(count++, curRow.get("P_Single_harvest_amount_unit").getString(Column.NEVER_NULL));
			curLivelihood.set(count++, curRow.get("p_Multi_harvest_amount").getString(Column.NEVER_NULL));
			curLivelihood.set(count++, curRow.get("p_Multi_harvest_amount_unit").getString(Column.NEVER_NULL));
			curLivelihood.set(count++, curRow.get("p_harvest_percent").getString(Column.NEVER_NULL));
			curLivelihood.set(count++, curRow.get("p_Multi_amount").getString(Column.NEVER_NULL));

			curLivelihood.set(count++, curRow.get("p_human_reliance").getString(Column.NEVER_NULL));
			curLivelihood.set(count++, curRow.get("p_harvest_gender").getString(Column.NEVER_NULL));
			curLivelihood.set(count++, curRow.get("p_harvest_socioeconomic").getString(Column.NEVER_NULL));
			curLivelihood.set(count++, curRow.get("p_other_harvest_socioeconomic").getString(Column.NEVER_NULL));

			curLivelihood.set(count++, curRow.get("p_involve_percent").getString(Column.NEVER_NULL));
			curLivelihood.set(count++, curRow.get("p_household_consumption_percent").getString(Column.NEVER_NULL));
			curLivelihood.set(count++, curRow.get("p_household_income_percent").getString(Column.NEVER_NULL));
			curLivelihood.set(count++, curRow.get("p_cash_income").getString(Column.NEVER_NULL));

			selected.addAll(curLivelihood);
			numSelected++;
		}

		if (selected.size() == 0) {
			selected.add("false");
			selected.add("0");
		} else
			selected.add(1, "" + numSelected);

		data.put(CanonicalNames.Livelihoods, selected);
	}

	private void lmeTableImport(Long curDEMid, AssessmentData curAssessment, HashMap<String, Object> data)
			throws Exception {
		SelectQuery select = new SelectQuery();
		select.select("coding_occurence", "*");
		select.constrain(new CanonicalColumnName("coding_occurence", "Sp_code"), QConstraint.CT_EQUALS, curDEMid);
		select.constrain(new CanonicalColumnName("coding_occurence", "co_type"), QConstraint.CT_EQUALS,
				LME_CODING_OCCURRENCE_TYPE);

		Row.Set rowLoader = new Row.Set();
		ec.doQuery(select, rowLoader);

		if (rowLoader == null)
			return;

		HashMap<String, ArrayList<String>> selected = new HashMap<String, ArrayList<String>>();
		for (Iterator<Row> iter = rowLoader.getSet().listIterator(); iter.hasNext();) {
			Row curRow = iter.next();

			String curCode = curRow.get("obj_id").getString();

			ArrayList<String> dataList = new ArrayList<String>();

			fetchCodingOccurrenceData(curRow, dataList);

			selected.put(curCode, dataList);
		}

		data.put(CanonicalNames.LargeMarineEcosystems, selected);
	}

	private void logAsNewCell(String string) {
		log.append("<td>" + string + "</td>");
	}

	private void logNode(TaxonNode cur) {
		log.append("<tr>");
		logAsNewCell(cur.getLevelString());
		logAsNewCell(cur.getFullName());
		for (int j = 0; j < TaxonNode.INFRARANK; j++)
			logAsNewCell(j < cur.getFootprint().length ? cur.getFootprint()[j] : "&nbsp");
		log.append("</tr>");
	}

	private void populationTableImport(Long curDEMid, AssessmentData curAssessment, HashMap<String, Object> data)
			throws DBException {
		SelectQuery select = new SelectQuery();
		select.select("Population", "*");
		select.constrain(new CanonicalColumnName("Population", "Sp_code"), QConstraint.CT_EQUALS, curDEMid);

		Row.Loader rowLoader = new Row.Loader();
		ec.doQuery(select, rowLoader);
		Row row = rowLoader.getRow();

		if (row == null)
			return;

		String max = row.get("Max_population").getString(Column.NEVER_NULL);
		String min = row.get("Min_population").getString(Column.NEVER_NULL);

		max = max.replaceAll(",", "").replaceAll("\\s", "");
		min = min.replaceAll(",", "").replaceAll("\\s", "");

		// IF EITHER ARE "", WE DON'T NEED A COMMA - WE'LL TREAT IT AS A BEST
		// GUESS
		String minMax = min + (min.equals("") || max.equals("") ? "" : "-") + max;

		if (!(isValidRangeFormat(minMax))) {
			String message = "Unparseable population values from DEM import on. Minimum value from DEM: " + min
					+ " --- maximum value from DEM: " + max;

			sendNote(curAssessment.getType(), message, curAssessment.getAssessmentID(), CanonicalNames.PopulationSize);
		} else {
			ArrayList<String> dataList = new ArrayList<String>();
			dataList.add(minMax);
			data.put(CanonicalNames.PopulationSize, dataList);
		}
	}

	@SuppressWarnings(value = "unchecked")
	private void redListingTableImport(Long curDEMid, AssessmentData curAssessment, HashMap<String, Object> data)
			throws DBException {
		SelectQuery select = new SelectQuery();
		select = new SelectQuery();
		select.select("red_listing", "*");
		select.constrain(new CanonicalColumnName("red_listing", "Sp_code"), QConstraint.CT_EQUALS, curDEMid);

		Row.Set rowSet = new Row.Set();
		ec.doQuery(select, rowSet);

		if (rowSet == null)
			return;

		for (Iterator<Row> rows = rowSet.getSet().iterator(); rows.hasNext();) {
			Row curRow = rows.next();
			// Criteria and Category
			List<String> dataList = SISCategoryAndCriteria.generateDefaultDataList();

			String cat = curRow.get("rl_category").getString(Column.EMPTY_IS_NULL);
			String crit = curRow.get("rl_criteria").getString(Column.EMPTY_IS_NULL);

			if (cat != null || crit != null) {
				dataList.remove(SISCategoryAndCriteria.IS_MANUAL_INDEX);
				dataList.add(SISCategoryAndCriteria.IS_MANUAL_INDEX, "true");
			}

			if (cat != null) {
				dataList.remove(SISCategoryAndCriteria.MANUAL_CATEGORY_INDEX);
				dataList.add(SISCategoryAndCriteria.MANUAL_CATEGORY_INDEX, cat);
			}

			if (crit != null) {
				dataList.remove(SISCategoryAndCriteria.MANUAL_CRITERIA_INDEX);
				dataList.add(SISCategoryAndCriteria.MANUAL_CRITERIA_INDEX, crit);
			}

			dataList.remove(SISCategoryAndCriteria.DATE_LAST_SEEN_INDEX);
			dataList.add(SISCategoryAndCriteria.DATE_LAST_SEEN_INDEX, XMLUtils.clean(curRow.get("last_seen").getString(
					Column.NEVER_NULL)));

			dataList.remove(SISCategoryAndCriteria.POSSIBLY_EXTINCT_CANDIDATE_INDEX);
			dataList.add(SISCategoryAndCriteria.POSSIBLY_EXTINCT_CANDIDATE_INDEX, ""
					+ (curRow.get("Poss_extinct_Cand").getInteger() == 1));

			dataList.remove(SISCategoryAndCriteria.POSSIBLY_EXTINCT_INDEX);
			dataList.add(SISCategoryAndCriteria.POSSIBLY_EXTINCT_INDEX, ""
					+ (curRow.get("Poss_extinct").getInteger() == 1));

			data.put(CanonicalNames.RedListCriteria, dataList);

			// Rationale
			data.put(CanonicalNames.RedListRationale, createDataArray(XMLUtils.clean(
					curRow.get("rl_rationale").getString(Column.NEVER_NULL)).replaceAll("\n", "<br>").replaceAll("\r",
					""), false));

			// CHANGE REASON
			dataList = new ArrayList<String>();
			int changeReason = 0;
			String genuineChangeReason = "0";
			String nonGenuineChangeReason = "0";
			String nonGenuineOtherText = "";
			String noChangeReason = "0";

			if (curRow.get("genuine_change").getInteger() == 1) {
				changeReason = 1;

				if (curRow.get("genuine_recent").getInteger() == 1)
					genuineChangeReason = "1";
				else if (curRow.get("genuine_sincefirst").getInteger() == 1)
					genuineChangeReason = "2";
			} else if (curRow.get("nongenuine_change").getInteger() == 1) {
				changeReason = 2;

				nonGenuineChangeReason = "";

				if (curRow.get("knowledge_new").getInteger() == 1)
					nonGenuineChangeReason += "1,";
				if (curRow.get("Knowledge_criteria").getInteger() == 1)
					nonGenuineChangeReason += "2,";
				if (curRow.get("Knowledge_correction").getInteger() == 1)
					nonGenuineChangeReason += "3,";
				if (curRow.get("Taxonomy").getInteger() == 1)
					nonGenuineChangeReason += "4,";
				if (curRow.get("Knowledge_criteria").getInteger() == 1)
					nonGenuineChangeReason += "5,";
				if (curRow.get("Other").getInteger() == 1)
					nonGenuineChangeReason += "6,";

				if (nonGenuineChangeReason.equals(""))
					nonGenuineChangeReason = "0";
				else
					nonGenuineChangeReason = nonGenuineChangeReason.substring(0, nonGenuineChangeReason.length());
			} else if (curRow.get("no_change").getInteger() == 1) {
				changeReason = 3;

				if (curRow.get("same").getInteger() == 1)
					noChangeReason = "1";
				else if (curRow.get("criteria_change").getInteger() == 1)
					noChangeReason = "2";
			}

			dataList.add("" + changeReason);
			dataList.add(genuineChangeReason);
			dataList.add(nonGenuineChangeReason);
			dataList.add(nonGenuineOtherText);
			dataList.add(noChangeReason);

			data.put(CanonicalNames.RedListReasonsForChange, dataList);

			// Population trend
			dataList = new ArrayList<String>();

			String populationTrend = curRow.get("rl_trend").getString(Column.NEVER_NULL);
			if (populationTrend.equals(""))
				dataList.add("0");
			else if (populationTrend.equalsIgnoreCase("Increasing"))
				dataList.add("1");
			else if (populationTrend.equalsIgnoreCase("Decreasing"))
				dataList.add("2");
			else if (populationTrend.equalsIgnoreCase("Stable"))
				dataList.add("3");
			else if (populationTrend.equalsIgnoreCase("Unknown"))
				dataList.add("4");
			else {
				String message = CANNED_NOTE + populationTrend;

				sendNote(curAssessment.getType(), message, curAssessment.getAssessmentID(),
						CanonicalNames.PopulationTrend);
				
				dataList.add("0");
			}
			data.put(CanonicalNames.PopulationTrend, dataList);

			// Date assessed
			data.put(CanonicalNames.RedListAssessmentDate, createDataArray(curRow.get("assess_date").getString(
					Column.NEVER_NULL), true));

			// Red List Notes
			data.put(CanonicalNames.RedListNotes, createDataArray(curRow.get("Notes").getString(Column.NEVER_NULL),
					true));

			// Assessors
			data.put(CanonicalNames.RedListAssessors, createDataArray(curRow.get("Assessors").getString(
					Column.NEVER_NULL), true));

			// Evaluators
			data.put(CanonicalNames.RedListEvaluators, createDataArray(curRow.get("Evaluator").getString(
					Column.NEVER_NULL), true));

			// Locations
			dataList = new ArrayList<String>();
			String locations = curRow.get("Number_locations").getString(Column.NEVER_NULL);
			if (!isValidRangeFormat(locations)) {
				sendNote(curAssessment.getType(), CANNED_NOTE + locations, curAssessment.getAssessmentID(),
						CanonicalNames.LocationsNumber);
			} else {
				dataList.add(locations);
				dataList.add("");
				data.put(CanonicalNames.LocationsNumber, dataList);
			}

//			Generation length - DO NOT USE GEN_LENGTH FROM THIS TABLE!!!!!!!!!!!!!!!!!!!!!!
			//Per e-mail from Jim Ragle, June 9th, 2009

			// Mature individuals
			dataList = new ArrayList();
			String matureIndividuals = curRow.get("Number_mat_ind").getString(Column.NEVER_NULL);
			if (!isValidRangeFormat(matureIndividuals)) {
				sendNote(curAssessment.getType(), CANNED_NOTE + matureIndividuals, curAssessment.getAssessmentID(),
						CanonicalNames.MaleMaturitySize);
				sendNote(curAssessment.getType(), CANNED_NOTE + matureIndividuals, curAssessment.getAssessmentID(),
						CanonicalNames.FemaleMaturitySize);
			} else {
				dataList.add(matureIndividuals);
				data.put(CanonicalNames.FemaleMaturitySize, dataList);

				dataList = new ArrayList<String>();
				dataList.add(matureIndividuals);
				data.put(CanonicalNames.MaleMaturitySize, dataList);
			}

			// Decline Past
			String past_decline = curRow.get("past_decline").getString(Column.NEVER_NULL);
			data.put(CanonicalNames.OldDEMPastDecline, createDataArray(past_decline, true));

			// Period Decline Past
			String period_past_decline = curRow.get("period_past_decline").getString(Column.NEVER_NULL);
			data.put(CanonicalNames.OldDEMPeriodPastDecline, createDataArray(period_past_decline, true));

			// Decline future
			String future_decline = curRow.get("future_decline").getString(Column.NEVER_NULL);
			data.put(CanonicalNames.OldDEMFutureDecline, createDataArray(future_decline, true));

			// Decline future
			String period_future_decline = curRow.get("period_future_decline").getString(Column.NEVER_NULL);
			data.put(CanonicalNames.OldDEMPeriodFutureDecline, createDataArray(period_future_decline, true));

			// Severe fragmentation
			dataList = new ArrayList();
			dataList.add("" + curRow.get("severely_frag").getInteger(Column.NEVER_NULL));
			dataList.add("");
			data.put(CanonicalNames.SevereFragmentation, dataList);
		}
	}

	private void referencesImport(Long curDEMid, AssessmentData curAssessment, HashMap<String, Object> data) {
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
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			Element el = doc.createElement("references");
			doc.appendChild(el);
			ElementalReferenceRowProcessor errp = new ElementalReferenceRowProcessor(doc, el);
			ec.doQuery(select, errp);
			for (Element refEl : new ElementCollection(el.getElementsByTagName("reference"))) {
				StringWriter refStringWriter = new StringWriter();
				Transformer t = TransformerFactory.newInstance().newTransformer();
				t.setOutputProperty("omit-xml-declaration", "yes");
				t.transform(new DOMSource(refEl), new StreamResult(refStringWriter));

				String result = refStringWriter.toString();
				// result =
				// result.replaceAll(
				// "<\\?xml\\s*(version=.*)?\\s*(encoding=.*)?\\?>",
				// "");
				result = result.substring(result.indexOf('>', 0) + 1, result.indexOf("</reference>"));
				// result = result.replaceAll("</reference>", "");

				System.out.println("Insert reference based on: [" + result + "]");

				String xml = DocumentUtils.serializeNodeToString(refEl);
				NativeDocument ndoc = NativeDocumentFactory.newNativeDocument();
				ndoc.parse(xml);
				ReferenceUI ref = new ReferenceUI(ndoc.getDocumentElement());
				curAssessment.addReference(ref, "Global");
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	private void registerDatasource(String sessionName, String URL, String driver, String username, String pass)
			throws Exception {
		DBSessionFactory.registerDataSource(sessionName, URL, driver, username, pass);
	}

	/*
	 * UTILITY FUNCTIONS
	 */

	private void riversTableImport(Long curDEMid, AssessmentData curAssessment, HashMap<String, Object> data)
			throws DBException {
		SelectQuery select = new SelectQuery();
		select.select("rivers_table", "*");
		select.constrain(new CanonicalColumnName("rivers_table", "Sp_code"), QConstraint.CT_EQUALS, curDEMid);

		Row.Set rowLoader = new Row.Set();
		ec.doQuery(select, rowLoader);

		if (rowLoader == null)
			return;

		HashMap<String, ArrayList<String>> selected = new HashMap<String, ArrayList<String>>();
		for (Iterator<Row> iter = rowLoader.getSet().listIterator(); iter.hasNext();) {
			Row curRow = iter.next();

			String curCode = curRow.get("river_number").getString();

			ArrayList<String> dataList = new ArrayList<String>();
			dataList.add("0");
			dataList.add("false");
			dataList.add("0");

			selected.put(curCode, dataList);
		}

		data.put(CanonicalNames.Rivers, selected);
	}

	public void run() {
		running.set(true);
		failed.set(false);
		statusMessage = new StringBuilder();
		try {

			registerDatasource("dem", "jdbc:access:///" + source.getAbsolutePath(), "com.hxtt.sql.access.AccessDriver", "", "");

			try {
				Document structDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
						getClass().getResourceAsStream("refstruct.xml"));
				new SystemExecutionContext("dem").setStructure(structDoc);
			} catch (Exception ugly) {
				ugly.printStackTrace();
				statusMessage.append("Internal system failure: could not read DEM.<br>");
				statusMessage.append("Please report the following message:<br>");
				statusMessage.append(DocumentUtils.getStackTraceAsString(ugly).replaceAll("\n", "<br>"));
				failed.set(true);
			}
			registerDatasource("demConversion", "jdbc:access:////usr/data/demMigration.mdb",
					"com.hxtt.sql.access.AccessDriver", "", "");
			registerDatasource("demSource", "jdbc:access:////usr/data/demSource.mdb",
					"com.hxtt.sql.access.AccessDriver", "", "");

			switchToDBSession("dem");

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
							if( f instanceof SQLException )
								System.out.println("--- SQL Exception - Next Exception ---\n" + DocumentUtils.getStackTraceAsString(((SQLException)f).getNextException()));
							
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
								System.out.println("--- SQL Exception - Next Exception ---\n" + DocumentUtils.getStackTraceAsString(((SQLException)f).getNextException()));
							
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
								System.out.println("--- SQL Exception - Next Exception ---\n" + DocumentUtils.getStackTraceAsString(((SQLException)f).getNextException()));
							
							f = f.getCause();
						}
					}
					failed.set(true);
				}
			} else
				statusMessage.append("(Did not save new taxa in SIS due to a prior failure.)<br>");

//			if (!failed.get()) {
//				try {
//					buildAssessments();
//				} catch (Exception e) {
//					statusMessage.append("Failed converting assessments to SIS format.<br>");
//					statusMessage.append("Please report the following message:<br>");
//					statusMessage.append(DocumentUtils.getStackTraceAsString(e).replaceAll("\n", "<br>"));
//					failed.set(true);
//					if( e instanceof DBException ) {
//						Throwable f = e.getCause();
//						
//						while( f != null ) {
//							if( f instanceof SQLException )
//								System.out.println("--- SQL Exception - Next Exception ---\n" + DocumentUtils.getStackTraceAsString(((SQLException)f).getNextException()));
//							
//							f = f.getCause();
//						}
//					}
//					failed.set(true);
//				}
//			} else
//				statusMessage
//						.append("(Did not attempt conversion of assessments to SIS format due to a prior failure.)<br>");
//
//			if (!failed.get()) {
//				try {
//					exportAssessments();
//					statusMessage.append("Import successful.");
//				} catch (Exception e) {
//					statusMessage.append("Failed saving assessments in SIS.<br>");
//					statusMessage.append("Please report the following message:<br>");
//					statusMessage.append(DocumentUtils.getStackTraceAsString(e).replaceAll("\n", "<br>"));
//					failed.set(true);
//					if( e instanceof DBException ) {
//						Throwable f = e.getCause();
//						
//						while( f != null ) {
//							if( f instanceof SQLException )
//								System.out.println("--- SQL Exception - Next Exception ---\n" + DocumentUtils.getStackTraceAsString(((SQLException)f).getNextException()));
//							
//							f = f.getCause();
//						}
//					}
//					failed.set(true);
//				}
//			} else
//				statusMessage.append("(Did not save assessments in SIS due to a prior failure.)<br>");

		} catch (Exception ex) {
			ex.printStackTrace();
			statusMessage.append("Internal system failure setting up DEMImport.<br>");
			statusMessage.append("Please report the following message:<br>");
			statusMessage.append(DocumentUtils.getStackTraceAsString(ex).replaceAll("\n", "<br>"));
			failed.set(true);
		}
		DBSessionFactory.unregisterDataSource("dem");
		running.set(false);

		log.append("</table>");

		DEMImportInformation.addToQueue(new DEMImportInformation(new Date(), !failed.get(), statusMessage.toString(),
				fileName, user, log.toString()));
	}

	private void sendNote(String assessmentType, String noteBody, String assessmentID, String canonicalName) {
		Note note = new Note();
		note.setBody(noteBody);
		note.setCanonicalName(canonicalName);
		note.setUser("DEMimport");
		
		String url = uriPrefix + "/notes/" + assessmentType + "/" + assessmentID + "/" + canonicalName;

		try {
			Request request = new Request(Method.POST , url, new StringRepresentation(note.toXML(), MediaType.TEXT_XML, null,
					CharacterSet.UTF_8));
			Response response = context.getClientDispatcher().handle(request);

			if (!response.getStatus().isSuccess())
				System.out.println("Failure response from Notes server.");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error noting unparseable " + canonicalName + " data.");
		}
	}

	private void switchToDBSession(String sessionName) throws Exception {
		ec = new SystemExecutionContext(sessionName);
		ec.setExecutionLevel(ExecutionContext.ADMIN);
		ec.setAPILevel(ExecutionContext.SQL_ALLOWED);
	}

	private void systematicsTableImport(Long curDEMid, AssessmentData curAssessment, HashMap<String, Object> data)
			throws DBException {
		SelectQuery select = new SelectQuery();
		select.select("Systematics", "*");
		select.constrain(new CanonicalColumnName("Systematics", "Sp_code"), QConstraint.CT_EQUALS, curDEMid);

		Row.Loader rowLoader = new Row.Loader();
		ec.doQuery(select, rowLoader);
		Row row = rowLoader.getRow();

		if (rowLoader == null)
			return;

		curAssessment.setDateAdded(row.get("date_added").getString(Column.NEVER_NULL));
		// curAssessment.setDateModified(row.get("date_modified").getString(
		// Column.NEVER_NULL));

		Boolean global = new Boolean(row.get("Assessments").getString(Column.NEVER_NULL).equalsIgnoreCase("global"));
		String regionName = "Global";
		Boolean endemic = new Boolean("false");

		if (!global.booleanValue()) {
			endemic = new Boolean(row.get("Endemic_region").getInteger(Column.NEVER_NULL) == 1);

			int region = row.get("Region").getInteger(Column.NEVER_NULL);

			SelectQuery selectRegion = new SelectQuery();
			selectRegion.select("region_lookup_table", "*");
			selectRegion.constrain(new CanonicalColumnName("region_lookup_table", "Region_number"),
					QConstraint.CT_EQUALS, region);

			Row.Loader regionLoader = new Row.Loader();

			try {
				ec.doQuery(selectRegion, regionLoader);
				regionName = regionLoader.getRow().get("Region_name").getString(Column.NEVER_NULL);
			} catch (DBException e) {
				System.out.println("Error grabbing region name for id " + region);
				e.printStackTrace();
			} catch (NullPointerException e) {
				System.out.println("NPE. Just going to assume it's a global, then.");
				e.printStackTrace();
			}
		}

		if (!regionName.equalsIgnoreCase("Global")) {
			// Check to make sure the region exists and get its proper ID
			Region region = new Region("New", regionName, regionName);
			
			Request request = new Request(Method.PUT , "riap://application/regions", new StringRepresentation("<regions>"
					+ region.toXML() + "</regions>", MediaType.TEXT_XML, null, CharacterSet.UTF_8));
			Response response = context.getClientDispatcher().handle(request);
			

			if (!response.getStatus().isSuccess())
				System.out.println("Error getting Region for " + regionName);

			try {
				String regionID = response.getEntity().getText();
				data.put(CanonicalNames.RegionInformation, createDataArray("true", regionID, endemic.toString()));
			} catch (IOException e) {
				e.printStackTrace();
				failed.set(true);
				statusMessage.append("Could not properly create a Regional assessment for " + "region named "
						+ regionName + ". Please report this error and include "
						+ "the DEM you are attempting to import.");
			}
		} else
			curAssessment.setRegionID(BaseAssessment.GLOBAL_ID);

		data.put(CanonicalNames.ConservationActionsDocumentation, createDataArray(XMLUtils.clean(FormattingStripper
				.stripText(row.get("cons_measures").getString(Column.NEVER_NULL)).replaceAll("\n", "<br>").replaceAll(
						"\r", "")), false));

		data.put(CanonicalNames.ThreatsDocumentation, createDataArray(XMLUtils.clean(FormattingStripper.stripText(
				row.get("threats_info").getString(Column.NEVER_NULL)).replaceAll("\n", "<br>").replaceAll("\r", "")),
				false));

		data
				.put(CanonicalNames.HabitatDocumentation, createDataArray(
						XMLUtils.clean(FormattingStripper.stripText(row.get("habitat").getString(Column.NEVER_NULL))
								.replaceAll("\n", "<br>").replaceAll("\r", "")), false));

		data.put(CanonicalNames.PopulationDocumentation, createDataArray(XMLUtils.clean(FormattingStripper.stripText(
				row.get("population").getString(Column.NEVER_NULL)).replaceAll("\n", "<br>").replaceAll("\r", "")),
				false));

		data.put(CanonicalNames.RangeDocumentation, createDataArray(XMLUtils.clean(FormattingStripper.stripText(
				row.get("range").getString(Column.NEVER_NULL)).replaceAll("\n", "<br>").replaceAll("\r", "")), false));

		data.put(CanonicalNames.TaxonomicNotes, createDataArray(XMLUtils.clean(
				row.get("notes").getString(Column.NEVER_NULL)).replaceAll("\n", "<br>"), false));
	}

	private void threatTableImport(Long curDEMid, AssessmentData curAssessment, HashMap<String, Object> data)
			throws DBException {
		SelectQuery select = new SelectQuery();
		select.select("threat_table", "*");
		select.constrain(new CanonicalColumnName("threat_table", "Sp_code"), QConstraint.CT_EQUALS, curDEMid);

		Row.Set rowLoader = new Row.Set();
		ec.doQuery(select, rowLoader);

		if (rowLoader == null)
			return;

		HashMap<String, ArrayList<String>> selected = new HashMap<String, ArrayList<String>>();
		for (Iterator<Row> iter = rowLoader.getSet().listIterator(); iter.hasNext();) {
			Row curRow = iter.next();

			String curCode = curRow.get("Threat_code").getString();
			int curTiming = curRow.get("threat_timing").getInteger(Column.NEVER_NULL);

			String stress1 = null;
			String stress2 = null;
			String notes = "";

			try {
				switchToDBSession("demConversion");

				SelectQuery conversionSelect = new SelectQuery();
				conversionSelect.select("Threat crosswalking", "*");
				conversionSelect.constrain(new CanonicalColumnName("Threat crosswalking", "oldthreat_id"),
						QConstraint.CT_EQUALS, curCode);

				Row.Loader conversionRow = new Row.Loader();
				ec.doQuery(conversionSelect, conversionRow);

				if (conversionRow.getRow() != null) {
					curCode = conversionRow.getRow().get("newthreat_id").getString();
					stress1 = conversionRow.getRow().get("stress 1").getString();
					stress2 = conversionRow.getRow().get("stress 2").getString();
					notes = XMLUtils.clean(conversionRow.getRow().get("comment").getString(Column.NEVER_NULL));
				}
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("Error checking against conversion table.");
			}

			// If an entry has already been added, just tweak the timings
			if (selected.containsKey(curCode)) {
				ArrayList arr = (ArrayList) selected.get(curCode);
				String extantTiming = ((ArrayList) selected.get(curCode)).get(0).toString();

				if (extantTiming == "" + SISThreatStructure.TIMING_ONGOING_INDEX) {
					// If it's ongoing just leave things alone
				}

				// If it's ever present mark it ongoing
				else if (curTiming == SISThreatStructure.TIMING_ONGOING_INDEX)
					arr.set(0, "" + SISThreatStructure.TIMING_ONGOING_INDEX);

				// If it's definitely not ongoing, look for past and future
				else if (curTiming == SISThreatStructure.TIMING_PAST_UNLIKELY_RETURN_INDEX
						&& extantTiming.equals("" + SISThreatStructure.TIMING_FUTURE_INDEX))
					arr.set(0, "" + SISThreatStructure.TIMING_PAST_LIKELY_RETURN_INDEX);

				else if (curTiming == SISThreatStructure.TIMING_FUTURE_INDEX
						&& extantTiming.equals("" + SISThreatStructure.TIMING_PAST_UNLIKELY_RETURN_INDEX))
					arr.set(0, "" + SISThreatStructure.TIMING_PAST_LIKELY_RETURN_INDEX);

				// No need to put it back in, I don't believe
				// selected.put(curCode, arr);
			} else {
				ArrayList<String> dataArray = new ArrayList<String>();
				dataArray.add("" + curTiming);
				dataArray.add("0");
				dataArray.add("0");
				dataArray.add("");
				dataArray.add(notes);

				if (stress1 != null && !stress1.equals("0") && !stress1.equals("")) {
					if (stress2 != null && !stress2.equals("0") && !stress2.equals("")) {
						dataArray.add("2");
						dataArray.add(stress1);
						dataArray.add(stress2);
					} else {
						dataArray.add("1");
						dataArray.add(stress1);
					}
				} else
					dataArray.add("0");

				selected.put(curCode, dataArray);
			}
		}

		data.put(CanonicalNames.Threats, selected);

		try {
			switchToDBSession("dem");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error switching back to DEM after building Threats. Import will now fail.");
		}
	}

	private void updateFootprint(ArrayList<String> footprint, TaxonNode taxon) {
		// Ensure footprint is on par with what the nodes say, not what the DEM
		// says
		footprint.clear();
		footprint.addAll(Arrays.asList(taxon.getFootprint()));
		footprint.add(taxon.getName());
	}

	/**
	 * Tables used: Source_of_specimens_table purpose_table
	 * removed_from_wild_table
	 */
	@SuppressWarnings(value = "unchecked")
	private void useTradeImport(Long curDEMid, AssessmentData curAssessment, HashMap<String, Object> data)
			throws DBException {
		SelectQuery select = new SelectQuery();
		select.select("Source_of_specimens_table", "*");
		select.constrain(new CanonicalColumnName("Source_of_specimens_table", "Sp_code"), QConstraint.CT_EQUALS,
				curDEMid);

		Row.Set rowLoader = new Row.Set();
		ec.doQuery(select, rowLoader);

		if (rowLoader == null)
			return;

		ArrayList<Integer> sourcesSelected = new ArrayList<Integer>();
		for (Row curRow : rowLoader.getSet())
			sourcesSelected.add(curRow.get("Source_code").getInteger(Column.NEVER_NULL));

		select = new SelectQuery();
		select.select("purpose_table", "*");
		select.constrain(new CanonicalColumnName("purpose_table", "Sp_code"), QConstraint.CT_EQUALS, curDEMid);

		rowLoader = new Row.Set();
		ec.doQuery(select, rowLoader);

		HashMap<Integer, ArrayList<Integer>> purposesSelected = new HashMap<Integer, ArrayList<Integer>>();
		for (Row curRow : rowLoader.getSet()) {
			int purposeCode = curRow.get("Purpose_code").getInteger(Column.NEVER_NULL);
			int useCode = curRow.get("utilisation_code").getInteger(Column.NEVER_NULL);

			ArrayList<Integer> mine = null;

			if (purposesSelected.containsKey(new Integer(purposeCode)))
				mine = purposesSelected.get(new Integer(purposeCode));
			else {
				mine = new ArrayList<Integer>();
				purposesSelected.put(new Integer(purposeCode), mine);
			}

			mine.add(new Integer(useCode));
		}

		select = new SelectQuery();
		select.select("removed_from_wild_table", "*");
		select
				.constrain(new CanonicalColumnName("removed_from_wild_table", "Sp_code"), QConstraint.CT_EQUALS,
						curDEMid);

		rowLoader = new Row.Set();
		ec.doQuery(select, rowLoader);

		ArrayList<Integer> wildSelected = new ArrayList<Integer>();
		for (Row curRow : rowLoader.getSet())
			wildSelected.add(curRow.get("Wild_code").getInteger(Column.NEVER_NULL));

		if (!(sourcesSelected.size() == 0 && purposesSelected.size() == 0 && wildSelected.size() == 0)) {
			if (sourcesSelected.size() == 0)
				sourcesSelected.add(new Integer(0));
			if (purposesSelected.keySet().size() == 0)
				purposesSelected.put(new Integer(0), null);
			if (wildSelected.size() == 0)
				wildSelected.add(new Integer(0));

			ArrayList<String> selected = new ArrayList();

			int count = 0;
			int index1;
			int index2;
			int index3;

			for (Integer curPurpose : purposesSelected.keySet()) {
				index1 = curPurpose.intValue();

				for (int j = 0; j < sourcesSelected.size(); j++) {
					index2 = sourcesSelected.get(j).intValue();

					for (int k = 0; k < wildSelected.size(); k++) {
						index3 = wildSelected.get(k).intValue();
						ArrayList dataList = UseTrade.generateDefaultDataList();
						dataList.remove(2);
						dataList.add(2, "" + index3);
						dataList.remove(1);
						dataList.add(1, "" + index2);
						dataList.remove(0);
						dataList.add(0, "" + index1);

						ArrayList<Integer> ticks = purposesSelected.get(curPurpose);
						if (ticks != null) {
							// Magic number use: 2, as subsistence == 1,
							// national == 2
							// and international == 3, and the tick box offsets
							// in the
							// structure are 3, 4 and 5
							for (Integer purp : ticks) {
								dataList.remove(2 + purp.intValue());
								dataList.add(2 + purp.intValue(), "true");
							}
						}

						selected.addAll(dataList);

						count++;
					}
				}
			}

			selected.add(0, "" + count);
			data.put(CanonicalNames.UseTradeDetails, selected);
		}
	}

	private void utilisationTableImport(Long curDEMid, AssessmentData curAssessment, HashMap<String, Object> data)
			throws DBException {
		SelectQuery select = new SelectQuery();
		select.select("utilisation_general", "*");
		select.constrain(new CanonicalColumnName("utilisation_general", "Sp_code"), QConstraint.CT_EQUALS, curDEMid);

		Row.Set rowLoader = new Row.Set();
		ec.doQuery(select, rowLoader);

		if (rowLoader == null)
			return;

		for (Row curRow : rowLoader.getSet()) {
			String useTradeNarrative = "";

			data.put(CanonicalNames.NotUtilized, createDataArray(""
					+ (curRow.get("Utilised").getInteger(Column.NEVER_NULL) == 1), true));

			String curNarText = XMLUtils.clean(curRow.get("Other_purpose").getString(Column.NEVER_NULL));
			if (!curNarText.equals("")) {
				useTradeNarrative += "--- Other purpose text ---<br>";
				useTradeNarrative += curNarText.replaceAll("\n", "<br>").replaceAll("\r", "");
			}

			curNarText = XMLUtils.clean(curRow.get("Other_wild").getString(Column.NEVER_NULL));
			if (!curNarText.equals("")) {
				useTradeNarrative += "--- Other wild text ---<br>";
				useTradeNarrative += curNarText.replaceAll("\n", "<br>").replaceAll("\r", "");
			}

			curNarText = XMLUtils.clean(curRow.get("Other_source").getString(Column.NEVER_NULL));
			if (!curNarText.equals("")) {
				useTradeNarrative += "--- Other source text ---<br>";
				useTradeNarrative += curNarText.replaceAll("\n", "<br>").replaceAll("\r", "");
			}
			data.put(CanonicalNames.UseTradeDocumentation, createDataArray(useTradeNarrative, false));

			String wildOfftake = curRow.get("Offtake").getString(Column.NEVER_NULL);

			if (wildOfftake.equalsIgnoreCase("Increasing"))
				data.put(CanonicalNames.TrendInWildOfftake, createDataArray("1", true));
			else if (wildOfftake.equalsIgnoreCase("Decreasing"))
				data.put(CanonicalNames.TrendInWildOfftake, createDataArray("2", true));
			else if (wildOfftake.equalsIgnoreCase("Stable"))
				data.put(CanonicalNames.TrendInWildOfftake, createDataArray("3", true));
			else if (wildOfftake.equalsIgnoreCase("Unknown"))
				data.put(CanonicalNames.TrendInWildOfftake, createDataArray("4", true));
			else
				data.put(CanonicalNames.TrendInWildOfftake, createDataArray("0", true));

			String domesticOfftake = curRow.get("Trend").getString(Column.NEVER_NULL);

			if (domesticOfftake.equalsIgnoreCase("Increasing"))
				data.put(CanonicalNames.TrendInDomesticOfftake, createDataArray("1", true));
			else if (domesticOfftake.equalsIgnoreCase("Decreasing"))
				data.put(CanonicalNames.TrendInDomesticOfftake, createDataArray("2", true));
			else if (domesticOfftake.equalsIgnoreCase("Stable"))
				data.put(CanonicalNames.TrendInDomesticOfftake, createDataArray("3", true));
			else if (domesticOfftake.equalsIgnoreCase("Not cultivated"))
				data.put(CanonicalNames.TrendInDomesticOfftake, createDataArray("4", true));
			else if (domesticOfftake.equalsIgnoreCase("Unknown"))
				data.put(CanonicalNames.TrendInDomesticOfftake, createDataArray("5", true));
			else
				data.put(CanonicalNames.TrendInDomesticOfftake, createDataArray("0", true));

		}
	}

}