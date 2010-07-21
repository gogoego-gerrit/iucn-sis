package org.iucn.sis.server.utils.scripts;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.iucn.sis.client.displays.ClassificationScheme;
import org.iucn.sis.server.io.AssessmentIO;
import org.iucn.sis.server.io.TaxaIO;
import org.iucn.sis.server.io.AssessmentIO.AssessmentIOWriteResult;
import org.iucn.sis.server.simple.SISContainerApp;
import org.iucn.sis.server.taxa.TaxonomyDocUtils;
import org.iucn.sis.server.utils.DocumentUtils;
import org.iucn.sis.server.utils.MostRecentFlagger;
import org.iucn.sis.server.utils.ServerPaths;
import org.iucn.sis.shared.BaseAssessment;
import org.iucn.sis.shared.data.assessments.AssessmentData;
import org.iucn.sis.shared.data.assessments.CanonicalNames;
import org.iucn.sis.shared.data.assessments.FieldParser;
import org.iucn.sis.shared.taxonomyTree.TaxonNode;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.solertium.lwxml.factory.NativeDocumentFactory;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.util.portable.PortableAlphanumericComparator;
import com.solertium.vfs.NotFoundException;

public class GMAUpperLevelThreatFixer {

	private static ClassificationScheme threatScheme;
	private static StringBuffer log;
	private static StringBuffer pathsModified;
	private static boolean writeback = false;

	/**
	 * Iterates through species in the class under ANIMALIA -> CHORDATA, as supplied, invoking
	 * the UpperLevelThreatFixer utility class on the assessments.. 
	 * 
	 * @param className
	 * @throws NotFoundException
	 */
	public static void fixUpperLevelThreats(String className, boolean doWriteback) throws NotFoundException {
		try {
			writeback = doWriteback;
			log = new StringBuffer();
			pathsModified = new StringBuffer();

			Document nameDoc = TaxonomyDocUtils.getTaxonomyDocByName();
			Element animalia = (Element)nameDoc.getDocumentElement().getElementsByTagName("ANIMALIA").item(0);
			Element chordata = (Element)animalia.getElementsByTagName("CHORDATA").item(0);

			NativeDocument ndoc = NativeDocumentFactory.newNativeDocument();
			ndoc.parse(DocumentUtils.getVFSFileAsString("/browse/docs/fields/Threats.xml", SISContainerApp.getStaticVFS()));

			FieldParser fp = new FieldParser();
			threatScheme = (ClassificationScheme)fp.parseField(ndoc);

			doFixUpperLevelThreats((Element)chordata.getElementsByTagName(className).item(0), TaxonNode.CLASS, 0);

			//			System.out.println(log.toString());
			FileWriter out = new FileWriter(new File("GMAupperLevelThreatFixer.out"));
			out.write(log.toString());
			out.close();

			FileWriter out2 = new FileWriter(new File("GMAupperLevelThreatFixerPathsModified.out"));
			out2.write(pathsModified.toString());
			out2.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static int doFixUpperLevelThreats(Element el, int level, int total) {
		if( level >= TaxonNode.SPECIES ) {
			processTaxon(el.getAttribute("id"));			

			total++;
			if( total % 1000 == 0 )
				System.out.println("Processed " + total + " species.");
		}

		NodeList children = el.getChildNodes();
		for( int i = 0; i < children.getLength(); i++ ) {
			Node child = children.item(i);
			if( child.getNodeType() != Node.ELEMENT_NODE )
				continue;

			total = doFixUpperLevelThreats((Element)child, level+1, total);
		}

		return total;
	}

	private static void processTaxon(String taxonID) {
		final PortableAlphanumericComparator comparator = new PortableAlphanumericComparator();
		TaxonNode taxon = TaxaIO.readNode(taxonID, SISContainerApp.getStaticVFS());
		if( taxon == null ) {
			System.out.println("Could not find taxon document for ID " + taxonID);
			return;
		}

		boolean foundAny = false;
		ArrayList<AssessmentData> assessments = new ArrayList<AssessmentData>();

		for( String id : taxon.getAssessments() ) {
			AssessmentData ass = AssessmentIO.readAssessment(SISContainerApp.getStaticVFS(), 
					id, BaseAssessment.PUBLISHED_ASSESSMENT_STATUS, "threatFixer");

			if (ass != null)
				assessments.add(ass);
			else if( ass == null )
				System.out.println("ERROR fetching assessment " + id + " for node "
						+ taxon.getId());
		}
		
		List<AssessmentData> updatedHistorical = MostRecentFlagger.flagMostRecentInList(assessments);
		
		if( updatedHistorical.size() > 0 ) {
			System.out.println("Changed " + updatedHistorical.size() + " historical flags for " +
					"taxon " + updatedHistorical.get(0).getSpeciesID());
		}
				
		for( AssessmentData curAss : assessments ) {
			if( !curAss.isHistorical() && (curAss.isGlobal() || curAss.isEndemic()) ) {
				fixAssessmentsThreats(taxon, curAss);
				foundAny = true;
			}
		}

		if( taxon.getAssessments().size() > 0 && !foundAny ) {
			log.append("Serious issue - did not find a non-historical, global assessment for taxon " + taxon.getId());
			System.out.println("Serious issue - did not find a non-historical, global assessment for taxon " + taxon.getId());
		}
	}

	public static boolean fixAssessmentsThreats(TaxonNode taxon, AssessmentData assessment) {
		HashMap<String, List<String>> threats = (HashMap<String, List<String>>)assessment.getDataMap().get(CanonicalNames.Threats);
		boolean changed = false;
		boolean logStarted = false;
		List<String> keysToRemove = new ArrayList<String>();

		if( threats == null )
			return false;

		for( Entry<String, List<String>> threat : threats.entrySet() ) {
			String desc = (String)threatScheme.getCodeToDesc().get(threat.getKey());
			if( threat.getKey().equals("0") ) {
				if( threats.size() > 1 ) {
					logStarted = writeHeader(taxon, assessment, logStarted);
					log.append(" has a code 0 threat, PLUS OTHERS.\n-------------");

					ArrayList<String> temp = new ArrayList<String>();
					temp.add("true");
					assessment.getDataMap().put(CanonicalNames.NoThreats, temp);

					keysToRemove.add(threat.getKey());
					changed = true;
				} else {
					logStarted = writeHeader(taxon, assessment, logStarted);
					log.append("Set NoThreats to true.\n");

					ArrayList<String> temp = new ArrayList<String>();
					temp.add("true");
					assessment.getDataMap().put(CanonicalNames.NoThreats, temp);
					keysToRemove.add(threat.getKey());
					changed = true;
				}
			} else if( desc.startsWith("OLD 12 Unknown")) {
				logStarted = writeHeader(taxon, assessment, logStarted);
				log.append("Threats Unknown ticked.\n");
				ArrayList<String> temp = new ArrayList<String>();
				temp.add("true");
				assessment.getDataMap().put(CanonicalNames.ThreatsUnknown, temp);
				keysToRemove.add(threat.getKey());

				changed = true;
			}
		}
		if( keysToRemove.size() > 0 ) {
			logStarted = writeHeader(taxon, assessment, logStarted);

			for( String key : keysToRemove ) {
				threats.remove(key);
				log.append("Removed threat " + key + ".\n");
			}
			changed = true;
		}

		if( !taxon.getFullName().equalsIgnoreCase(assessment.getSpeciesName()) && 
				assessment.getSpeciesID().equals(taxon.getId()+"") ) {
			logStarted = writeHeader(taxon, assessment, logStarted);
			log.append("Changed species name from " + assessment.getSpeciesName() + " to " + taxon.getFullName() + ".\n");
			assessment.setSpeciesName(taxon.getFullName());
			changed = true;
		} 
		if( !assessment.getSpeciesID().equals(taxon.getId()+"") ) {
			logStarted = writeHeader(taxon, assessment, logStarted);
			log.append("XXXXXXXXX - Suspicious overlap! Assessment thinks owning species is " + assessment.getSpeciesID()
					+ " but taxon " + taxon.getId() + " thinks it owns it. Bailing on it...\n");
			return false;
		}

		if( changed ) {
			if( writeback ) {
				AssessmentIOWriteResult result = AssessmentIO.writeAssessment(assessment, "threatFixer", SISContainerApp.getStaticVFS(), false);

				String url = "";
				if (assessment.getType().equals(BaseAssessment.DRAFT_ASSESSMENT_STATUS)) {
					url = ServerPaths.getDraftAssessmentURL(assessment.getAssessmentID());
				} else if (assessment.getType().equals(BaseAssessment.PUBLISHED_ASSESSMENT_STATUS)) {
					url = ServerPaths.getPublishedAssessmentURL(assessment.getAssessmentID());
				}
				pathsModified.append(url + "," + result.newLastModified + "\n");
			} else
				assessment.toXML();
		}

		return true;
	}

	private static boolean writeHeader(TaxonNode taxon, AssessmentData assessment, boolean logStarted) {
		if( !logStarted ) {
			logStarted = true;
			log.append("************************\n");
			log.append("Assessment " + assessment.getAssessmentID() + ": " + assessment.getType() + ", " + assessment.getDateAssessed() 
					+ " on species " + assessment.getSpeciesID()+"/"+taxon.getFullName()+"\n");
		}
		return logStarted;
	}
}
