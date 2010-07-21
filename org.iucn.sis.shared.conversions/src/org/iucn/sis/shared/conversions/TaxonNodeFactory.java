package org.iucn.sis.shared.conversions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.iucn.sis.client.referenceui.ReferenceUI;
import org.iucn.sis.client.simple.SimpleSISClient;
import org.iucn.sis.shared.BaseAssessment;
import org.iucn.sis.shared.LongUtils;
import org.iucn.sis.shared.taxonomyTree.CommonNameData;
import org.iucn.sis.shared.taxonomyTree.CommonNameFactory;
import org.iucn.sis.shared.taxonomyTree.TaxonomyTree;
import org.iucn.sis.shared.xml.XMLUtils;

import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.NativeElement;
import com.solertium.lwxml.shared.NativeNodeList;

/**
 * Used to create, serialize and deserialize TaxonNodes! All these functions are
 * kept here, in one place, as they all change when some information about a
 * node is added or removed, as each of the processes has to be modified to
 * include/remove said information.
 * 
 * @author adam.schwartz
 * 
 */
public class TaxonNodeFactory {

	public static void copyNodeInformation(TaxonNode fromNode, TaxonNode toNode) {
		toNode.setId(fromNode.getId());
		toNode.setCommonNames(fromNode.getCommonNames());
		toNode.setSynonyms(fromNode.getSynonyms());
		toNode.setStatus(fromNode.getStatus());
		// toNode.setDeprecated(fromNode.isDeprecated());
		toNode.setTaxonomicAuthority(fromNode.getTaxonomicAuthority());
		toNode.addAssessments(fromNode.getAssessments());
		toNode.setSources(toNode.getSources());

		// VERIFY MY ASSESSMENTS KNOW THEY BELONG TO ME!
		for (Iterator iter = toNode.getAssessments().iterator(); iter.hasNext();) {
			BaseAssessment cur = (BaseAssessment) iter.next();

			cur.setSpeciesID(toNode.getName());
			cur.setSpeciesName(toNode.getName());
		}
	}

	public static TaxonNode createNode(long id, String name, int level, String parentID, String parentName,
			boolean hybrid, String status, String addedBy, String creationDate) {
		if (level < TaxonNode.SPECIES)
			return new HighLevelTaxonNode(id, level, name, parentID, parentName, status, addedBy, creationDate);
		else
			return new LowLevelTaxonNode(id, level, name, parentID, parentName, hybrid, status, addedBy, creationDate);
	}

	public static TaxonNode createNode(NativeDocument nodeDoc) {
		return createNode(nodeDoc.getDocumentElement());
	}

	public static TaxonNode createNode(NativeElement nodeElement) {
		TaxonNode newNode = null;
		long id = LongUtils.safeParseLong(nodeElement.getAttribute("id"));
		String name = nodeElement.getAttribute("name");
		int level = Integer.parseInt(nodeElement.getAttribute("level"));
		int infraType = -1;

		if (nodeElement.hasAttribute("infrarankType"))
			infraType = Integer.parseInt(nodeElement.getAttribute("infrarankType"));

		String parentName = nodeElement.getAttribute("parent");
		String parentId = nodeElement.getAttribute("parentid");
		String isDeprecated = nodeElement.getAttribute("deprecated");
		String status = nodeElement.getAttribute("status");
		String sequenceCode = nodeElement.getAttribute("sequenceCode");
		boolean hybrid = (nodeElement.getAttribute("hybrid") != null && nodeElement.getAttribute("hybrid").equals(
				"true"));

		String recordAdded = nodeElement.getAttribute("recordAdded");
		String addedBy = nodeElement.getAttribute("addedBy");
		String lastUpdatedBy = nodeElement.getAttribute("lastUpdatedBy");
		String lastUpdated = nodeElement.getAttribute("lastUpdated");

		newNode = createNode(id, name, level, parentId, parentName, hybrid, status, addedBy, recordAdded);
		newNode.setInfraType(infraType);
		newNode.setLastUpdated(lastUpdated);
		newNode.setUpdatedBy(lastUpdatedBy);

		// If it didn't have its infraType set and it needs it, then try to set
		// it...
		if ((newNode.getLevel() == TaxonNode.INFRARANK_SUBPOPULATION || newNode.getLevel() == TaxonNode.INFRARANK)
				&& infraType == -1) {
			if (newNode.getName().indexOf("ssp.") > -1) {
				newNode.setName(newNode.getName().substring(5));
				newNode.setInfraType(TaxonNode.INFRARANK_TYPE_SUBSPECIES);
			} else if (newNode.getName().indexOf("var.") > -1) {
				newNode.setName(newNode.getName().substring(5));
				newNode.setInfraType(TaxonNode.INFRARANK_TYPE_VARIETY);
			}
		}

		try {
			newNode.setSequenceCode(Float.parseFloat(sequenceCode));
		} catch (Exception e) {
			newNode.setSequenceCode(0f);
		}

		String[] footprint = null;
		if (level > 0) {
			String csvFootprint = nodeElement.getElementByTagName("footprint").getText();
			footprint = csvFootprint.split(",");
		} else
			footprint = new String[0];
		newNode.setFootprint(footprint);

		newNode.setFullName(newNode.generateFullName());

		ArrayList nodeCommonNames = new ArrayList();
		ArrayList nodeSynonyms = new ArrayList();
		ArrayList nodeDeadSynonyms = new ArrayList();
		ArrayList nodeAssessments = new ArrayList();

		// TODO: Why is it throwing errors in browser mode herein?
		try {
			if (nodeElement.getElementByTagName("taxonomicAuthority") != null)
				newNode.setTaxonomicAuthority(XMLUtils.clean(nodeElement.getElementByTagName("taxonomicAuthority")
						.getTextContent()));
		} catch (Exception IGNORED) {
		}

		try {
			if (nodeElement.getElementsByTagName("commonName") != null) {
				NativeNodeList names = nodeElement.getElementsByTagName("commonName");

				for (int i = 0; i < names.getLength(); i++) {
					CommonNameData curName = CommonNameFactory.buildFromXML(names.elementAt(i));
					nodeCommonNames.add(curName);
				}
			}
		} catch (Exception e) {
			System.out.println("Error parsing common names of taxon " + id);
			e.printStackTrace();
		}

		try {
			if (nodeElement.getElementsByTagName("synonym") != null) {
				NativeNodeList synonyms = nodeElement.getElementsByTagName("synonym");

				for (int i = 0; i < synonyms.getLength(); i++) {
					NativeElement synTag = (NativeElement) synonyms.item(i);

					String synId = synTag.getText();
					String synStatus = synTag.getAttribute("status");
					String synNotes = synTag.getAttribute("notes");
					String synName = synTag.getAttribute("name");
					String synUpperLevelName = synTag.getAttribute("upperLevelName");
					String synSpecName = synTag.getAttribute("speciesName");
					String synGenusName = synTag.getAttribute("genusName");
					String synInfraName = synTag.getAttribute("infrarankName");
					String synStockName = synTag.getAttribute("stockName");
					String synLevel = synTag.getAttribute("level");
					String synInfraType = synTag.getAttribute("infrarankType");

					HashMap authorities = new HashMap();
					for (int j = 0; j < TaxonNode.getDisplayableLevelCount(); j++) {
						if (synTag.hasAttribute(TaxonNode.getDisplayableLevel(j))) {
							authorities.put(j + "", XMLUtils.cleanFromXML(synTag.getAttribute(TaxonNode
									.getDisplayableLevel(j))));
						}

					}

					String synRlCat = synTag.getAttribute("rlCat");
					String synRlCrit = synTag.getAttribute("rlCrit");
					String synRlAssessors = synTag.getAttribute("rlAssessors");
					String synRlDate = synTag.getAttribute("rlDate");

					String synAssessment = synTag.getAttribute("relatedAssessment");
					String synAssessmentStatus = synTag.getAttribute("relatedAssessmentStatus");

					int realLevel;
					if (synLevel == null || !synLevel.matches("\\d+"))
						realLevel = -1;
					else
						realLevel = Integer.parseInt(synLevel);

					SynonymData curSyn = new SynonymData(synName, realLevel, authorities, synStatus, synNotes, synId);
					curSyn.setUpperLevelName(synUpperLevelName == null ? "" : XMLUtils.cleanFromXML(synUpperLevelName));
					curSyn.setGenus(synGenusName == null ? "" : XMLUtils.cleanFromXML(synGenusName));
					curSyn.setSpecie(synSpecName == null ? "" : XMLUtils.cleanFromXML(synSpecName));
					curSyn.setStockName(synStockName == null ? "" : XMLUtils.cleanFromXML(synStockName));
					curSyn.setInfrarank(synInfraName == null ? "" : XMLUtils.cleanFromXML(synInfraName));
					if (synInfraType == null || !synInfraType.matches("\\d+"))
						curSyn.setInfrarankType(-1);
					else
						curSyn.setInfrarankType(Integer.parseInt(XMLUtils.cleanFromXML(synInfraType)));

					curSyn.setRedListCategory(synRlCat == null ? "" : XMLUtils.cleanFromXML(synRlCat));
					curSyn.setRedListCriteria(synRlCrit == null ? "" : XMLUtils.cleanFromXML(synRlCrit));
					curSyn.setRedListAssessor(synRlAssessors == null ? "" : XMLUtils.cleanFromXML(synRlAssessors));
					curSyn.setRedListDate(synRlDate == null ? "" : XMLUtils.cleanFromXML(synRlDate));

					curSyn.setAssessmentAttachedToID(synAssessment);
					curSyn.setAssessmentStatus(synAssessmentStatus);

					nodeSynonyms.add(curSyn);
				}
			}
		} catch (Exception e) {
			System.out.println("Error parsing taxon " + id);
			e.printStackTrace();
		}

		try {
			if (nodeElement.getElementByTagName("assessments") != null) {
				NativeElement parent = nodeElement.getElementByTagName("assessments");

				if (parent != null && parent.getText() != null && !parent.getText().equals("")) {
					// NativeNodeList assessments =
					// parent.getElementsByTagName("assessment");
					String[] assessmentsList = parent.getText().split(",");
					for (int i = 0; i < assessmentsList.length; i++) {
						if (!assessmentsList[i].equals(""))
							nodeAssessments.add(assessmentsList[i]);
					}
				}
			}
		} catch (Exception e) {
			System.out.println("Error parsing taxon " + id);
			e.printStackTrace();
		}

		try {
			NativeNodeList sources = nodeElement.getElementsByTagName("sources");
			if (sources != null && sources.getLength() > 0) {
				NativeNodeList refs = sources.elementAt(0).getElementsByTagName("reference");
				for (int i = 0; i < refs.getLength(); i++) {
					ReferenceUI ref = new ReferenceUI(refs.elementAt(i));
					newNode.addReference(ref);
				}
			}
		} catch (Exception e) {
			System.out.println("Error parsing taxon " + id);
			e.printStackTrace();
		}

		newNode.setCommonNames(nodeCommonNames);
		newNode.setSynonyms(nodeSynonyms);
		newNode.addAssessments(nodeAssessments);

		return newNode;

	}

	public static TaxonNode createNode(String xml, TaxonomyTree tree, boolean thin) {
		NativeDocument doc = SimpleSISClient.getHttpBasicNativeDocument();
		doc.parse(xml);

		return createNode(doc);
	}

	public static String nodeToDetailedXML(TaxonNode node) {
		if (node == null)
			return "";

		// TRY TO GET THE PARENT NAME FROM THE SOURCE, IF APPLICABLE
		String parentName = node.parentName;
		String parentID = node.parentId;

		if (node.getParent() != null) {
			parentName = node.getParent().getFullName();
			parentID = "" + node.getParent().getId();
		}

		String myXML = "";
		myXML += "<" + TaxonNode.getDisplayableLevel(node.level).toLowerCase() + " id=\"" + node.getId() + "\""
				+ " name=\"" + XMLUtils.clean(node.name.trim()) + "\"" + " level=\"" + node.level + "\""
				+ " infrarankType=\"" + node.infraType + "\"" + " parent=\"" + XMLUtils.clean(parentName.trim()) + "\""
				+ " parentid=\"" + XMLUtils.clean(parentID) + "\"" + " recordAdded=\""
				+ XMLUtils.clean(node.recordAdded) + "\"" + " addedBy=\"" + XMLUtils.clean(node.addedBy) + "\""
				+ " status=\"" + XMLUtils.clean(node.status) + "\"" + " sequenceCode=\"" + node.sequenceCode + "\""
				+ " lastUpdatedBy=\"" + XMLUtils.clean(node.lastUpdatedBy) + "\"" + " lastUpdated=\""
				+ XMLUtils.clean(node.lastUpdated) + "\"";

		if (node.level >= TaxonNode.SPECIES) {
			myXML += " hybrid=\"" + ((TaxonNode) node).hybrid + "\"";
		}
		myXML += ">\r\n";

		if (node.taxonomicAuthority != null && !node.taxonomicAuthority.equalsIgnoreCase(""))
			myXML += "<taxonomicAuthority>" + XMLUtils.clean(node.taxonomicAuthority) + "</taxonomicAuthority>\r\n";

		if (node.getFootprint().length > 0) {
			myXML += "<footprint>";
			for (int i = 0; i < node.getFootprint().length; i++)
				myXML += node.getFootprint()[i] + ",";
			myXML = myXML.substring(0, myXML.length() - 1);
			myXML += "</footprint>";
		}

		if (node.commonNames.size() > 0) {
			myXML += "<commonNames>\r\n";
			for (int i = 0; i < node.commonNames.size(); i++)
				myXML += CommonNameFactory.nameToXML((CommonNameData) node.commonNames.get(i));
			myXML += "</commonNames>\r\n";
		}

		if (node.synonyms.size() > 0) {
			myXML += "<synonyms>\r\n";
			for (int i = 0; i < node.synonyms.size(); i++) {
				SynonymData curSyn = ((SynonymData) node.synonyms.get(i));
				myXML += curSyn.toXML();
			}
			myXML += "</synonyms>\r\n";
		}

		if (node.assessmentSet.size() > 0) {
			myXML += "<assessments>";
			for (Iterator iter = node.assessmentSet.iterator(); iter.hasNext();) {
				myXML += ((String) iter.next());
				if (iter.hasNext())
					myXML += ",";
			}
			myXML += "</assessments>\r\n";
		}

		if (node.taxonomicSource.size() > 0) {
			myXML += "<sources>";
			for (ReferenceUI curRef : node.taxonomicSource)
				myXML += curRef.toXML();
			myXML += "</sources>\r\n";

		}

		myXML += "</" + TaxonNode.getDisplayableLevel(node.level).toLowerCase() + ">\r\n";
		return myXML;
	}

	/**
	 * Returns a synonym with all of the appropriate fields set, based on the
	 * taxon argument.
	 * 
	 * @param taxon
	 *            - used to generate the synonym
	 * @return a synonym
	 */
	public static SynonymData synonymizeNode(TaxonNode taxon) {
		if (taxon.getLevel() < TaxonNode.GENUS)
			return new SynonymData(taxon.getName(), taxon.getLevel(), taxon.getId() + "", null);
		else if (taxon.getLevel() == TaxonNode.GENUS)
			return new SynonymData(taxon.getName(), "", "", -1, taxon.getLevel(), taxon.getId() + "");
		else if (taxon.getLevel() == TaxonNode.SPECIES)
			return new SynonymData(taxon.getFootprint()[TaxonNode.GENUS], taxon.getName(), "",
					TaxonNode.INFRARANK_TYPE_NA, TaxonNode.SPECIES, taxon.getId() + "");
		else if (taxon.getLevel() == TaxonNode.INFRARANK_SUBPOPULATION)
			return new SynonymData(taxon.getFootprint()[TaxonNode.GENUS], taxon.getFootprint()[TaxonNode.SPECIES],
					taxon.getFootprint()[TaxonNode.INFRARANK], TaxonNode.INFRARANK_TYPE_NA, taxon.getName(),
					TaxonNode.INFRARANK_SUBPOPULATION, taxon.getId() + "");
		else
			return new SynonymData(taxon.getFootprint()[TaxonNode.GENUS], taxon.getFootprint()[TaxonNode.SPECIES],
					taxon.getName(), taxon.getInfrarankType(), taxon.getLevel(), taxon.getId() + "");

	}
}
