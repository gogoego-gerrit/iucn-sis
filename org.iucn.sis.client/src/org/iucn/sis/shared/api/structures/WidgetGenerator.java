package org.iucn.sis.shared.api.structures;

import java.util.ArrayList;

import org.iucn.sis.shared.api.data.DisplayData;
import org.iucn.sis.shared.api.debug.Debug;
import org.iucn.sis.shared.api.utils.XMLUtils;

public class WidgetGenerator {

	public static final String COMPLEX = "complex";

//	private static Structure buildStructureFromStructureConstructorPackage(StructureConstructorPackage structure) {
//		return StructureGenerator(structure.getStructure(), structure.getDescription(), uniqueID, structure.getData());
//	}

	public static Structure StructureGenerator(String theStructure, String description, String structID, Object data) {
		if (theStructure.equalsIgnoreCase(XMLUtils.BOOLEAN_STRUCTURE)) {
			return new SISBoolean(theStructure, description, structID);
		}

		else if (theStructure.equalsIgnoreCase(XMLUtils.BOOLEAN_UNKNOWN_STRUCTURE)) {
			return new SISBooleanList(theStructure, description, structID);
		}

		else if (theStructure.equalsIgnoreCase(XMLUtils.REGIONAL_INFORMATION)) {
			return new SISRegionInformation(theStructure, description, structID);
		}

		else if (theStructure.equalsIgnoreCase(XMLUtils.EMPTY_STRUCTURE)) {
			return new SISEmptyStructure(theStructure, description, structID, data);
		}

		else if (theStructure.equalsIgnoreCase(XMLUtils.RED_LIST_CATEGORIES_CRITERIA)) {
			return new SISCategoryAndCriteria(theStructure, description, structID, "3.1");
		}

		else if (theStructure.equalsIgnoreCase(XMLUtils.REGIONAL_EXPERT_QUESTIONS_STRUCTURE)) {
			return new SISRegionalExpertStructure(theStructure, description, structID);
		}

		else if (theStructure.equalsIgnoreCase(XMLUtils.REFERENCE_STRUCTURE)) {
			return new SISText(theStructure, description, structID);
		} else if (theStructure.equalsIgnoreCase(XMLUtils.BOOLEAN_RANGE_STRUCTURE)) {
			return new SISBooleanRange(theStructure, description, structID);
		} else if (theStructure.equalsIgnoreCase(XMLUtils.SINGLE_SELECT_STRUCTURE)) {
			return new SISSelect(theStructure, description, structID, data);
		} else if (theStructure.equalsIgnoreCase(XMLUtils.MULTIPLE_SELECT_STRUCTURE)) {
			return new SISMultiSelect(theStructure, description, structID, data);
		} else if (theStructure.equalsIgnoreCase(XMLUtils.QUALIFIER_STRUCTURE)) {
			return new SISQualifier(theStructure, "Qualification: ", structID, data);
		} else if (theStructure.equalsIgnoreCase(XMLUtils.LABEL_STRUCTURE)) {
			return new SISLabel(theStructure, description, structID);
		} else if (theStructure.equalsIgnoreCase(XMLUtils.OPTIONS_LIST)) {
			return new SISOptionsList(theStructure, description, structID);
		} else if (theStructure.equalsIgnoreCase(XMLUtils.LIVELIHOODS)) {
			return new SISLivelihoods(theStructure, description, structID, data);
		}

		else if (theStructure.equalsIgnoreCase(XMLUtils.ONE_TO_MANY)) {
			return new SISOneToMany(theStructure, description, structID, (DisplayData) data);
		} else if (theStructure.equalsIgnoreCase(XMLUtils.USE_TRADE)) {
			Debug.println("Creating use trade with data type {0}", data.getClass().getName());
			return new UseTrade(theStructure, description, structID, data);
		}

		// TEXTAREA
		else if (theStructure.equalsIgnoreCase(XMLUtils.NARRATIVE_STRUCTURE)) {
			return new SISTextArea(theStructure, description, structID);
		} else if (theStructure.equalsIgnoreCase(XMLUtils.JUSTIFICATION_STRUCTURE)) {
			return new SISTextArea(theStructure, "Justification: ", structID);
		} else if (theStructure.equalsIgnoreCase(XMLUtils.RICH_TEXT_STRUCTURE)) {
			return new SISRichTextArea(theStructure, description, structID);
		} else if (theStructure.equalsIgnoreCase(XMLUtils.NOTE_STRUCTURE)) {
			return new SISHideableNote(theStructure, description, structID);
		}

		// TEXTBOX
		else if (theStructure.equalsIgnoreCase(XMLUtils.TEXT_STRUCTURE)) {
			return new SISText(theStructure, description, structID);
		} else if (theStructure.equalsIgnoreCase(XMLUtils.NUMBER_STRUCTURE)) {
			return new SISNumber(theStructure, description, structID, data);
		} else if (theStructure.equalsIgnoreCase(XMLUtils.DATE_STRUCTURE)) { // wILL
			// NEED
			// FIXING
			// !
			return new SISDate(theStructure, description, structID);
		} else if (theStructure.equalsIgnoreCase(XMLUtils.RANGE_STRUCTURE)) {
			return new SISRange(theStructure, description, structID);
		} else if (theStructure.equalsIgnoreCase(XMLUtils.FILE_STRUCTURE)) {
			return new SISUpload(theStructure, description, structID);
		}

		// COMPLEX STUFF
		else if (theStructure.equalsIgnoreCase(COMPLEX)) {
			return new SISBoolean(theStructure, description, structID);
		} else if (theStructure.equalsIgnoreCase(XMLUtils.MAP_STRUCTURE)) {
			return new SISMap(theStructure, description, structID, data);
		} else if (theStructure.equalsIgnoreCase(XMLUtils.IMAGE_STRUCTURE)) {
			return new SISImage(theStructure, description, structID, data);
		} else if (theStructure.equalsIgnoreCase(XMLUtils.THREAT_STRUCTURE)) {
			Structure threat = new SISThreatStructure(theStructure, description, structID);
			threat.setCanRemoveDescription(false);
			return threat;
		} else if (theStructure.equalsIgnoreCase(XMLUtils.STRUCTURE_COLLECTION)) {
			return new SISStructureCollection(theStructure, description, structID, data);

			/*
			 * ArrayList structures = new ArrayList(); ArrayList structureData =
			 * (ArrayList)data; for (int i = 0; i < structureData.size(); i++) {
			 * structures.add(buildStructureFromStructureConstructorPackage((
			 * StructureConstructorPackage)structureData.get(i))); } return new
			 * SISStructureCollection(theStructure, description, structures);
			 */
		} else if (theStructure.equalsIgnoreCase(XMLUtils.DOMINANT_STRUCTURE_COLLECTION)) {
			return new SISDominantStructureCollection(theStructure, description, structID, data);
			/*
			 * ArrayList structures = new ArrayList(); ArrayList structureData =
			 * (ArrayList)data; for (int i = 0; i < structureData.size(); i++) {
			 * structures.add(buildStructureFromStructureConstructorPackage((
			 * StructureConstructorPackage)structureData.get(i))); } return new
			 * SISDominantStructureCollection(theStructure, description,
			 * structures);
			 */
		} else if (theStructure.equalsIgnoreCase(XMLUtils.RELATED_STRUCTURE)) {
			DominantStructure doms = (DominantStructure) ((ArrayList) data).get(0);
			ArrayList deps = (ArrayList) ((ArrayList) data).get(1);
			ArrayList rules = (ArrayList) ((ArrayList) data).get(2);

			/*
			 * DominantStructure doms =
			 * (DominantStructure)buildStructureFromStructureConstructorPackage(
			 * (StructureConstructorPackage)((ArrayList)data).get(0));
			 * 
			 * ArrayList deps = new ArrayList(); ArrayList depData =
			 * (ArrayList)((ArrayList)data).get(1); for (int i = 0; i <
			 * depData.size(); i++) {
			 * deps.add(buildStructureFromStructureConstructorPackage
			 * ((StructureConstructorPackage)depData.get(i))); }
			 * 
			 * ArrayList rules = (ArrayList)((ArrayList)data).get(2);
			 */
			String layout = null;
			String dependentsLayout = null;

			layout = (String) ((ArrayList) data).get(3);
			dependentsLayout = (String) ((ArrayList) data).get(4);

			SISRelatedStructures s = new SISRelatedStructures(theStructure, description, structID, doms, deps, rules);
			int myLayout = 1;

			if (!layout.equals("")) {
				if (layout.equalsIgnoreCase("vertical"))
					myLayout = 1;
				else if (layout.equalsIgnoreCase("horizontal"))
					myLayout = 2;
				else if (layout.equalsIgnoreCase("table"))
					myLayout = 3;
				else if (layout.equalsIgnoreCase("thin_table"))
					myLayout = 4;
				s.setDisplayType(myLayout);
			}

			if (!dependentsLayout.equals("")) {
				if (dependentsLayout.equalsIgnoreCase("vertical"))
					myLayout = 1;
				else if (dependentsLayout.equalsIgnoreCase("horizontal"))
					myLayout = 2;
				else if (dependentsLayout.equalsIgnoreCase("table"))
					myLayout = 3;
				else if (dependentsLayout.equalsIgnoreCase("thin_table"))
					myLayout = 4;

				s.setDependentsLayout(myLayout);
			}

			return s;
		}

		// OTHER
		else if (theStructure.equalsIgnoreCase(XMLUtils.BUTTON_STRUCTURE)) {
			return new SISButton(theStructure, description, structID);
		} /*else if (theStructure.equalsIgnoreCase(XMLUtils.TREE_STRUCTURE)) {
			return new SISClassificationSchemeStructure(theStructure, description, structID, data);
			// return new SISTreeStructure(theStructure, description, structID, data);
		} */else {
			return null;
		}
	}
}
