package org.iucn.sis.client.ui;

import java.util.ArrayList;

import org.iucn.sis.shared.DisplayData;
import org.iucn.sis.shared.StructureConstructorPackage;
import org.iucn.sis.shared.structures.DominantStructure;
import org.iucn.sis.shared.structures.SISBoolean;
import org.iucn.sis.shared.structures.SISBooleanList;
import org.iucn.sis.shared.structures.SISBooleanRange;
import org.iucn.sis.shared.structures.SISButton;
import org.iucn.sis.shared.structures.SISCategoryAndCriteria;
import org.iucn.sis.shared.structures.SISClassificationSchemeStructure;
import org.iucn.sis.shared.structures.SISDate;
import org.iucn.sis.shared.structures.SISDominantStructureCollection;
import org.iucn.sis.shared.structures.SISEmptyStructure;
import org.iucn.sis.shared.structures.SISHideableNote;
import org.iucn.sis.shared.structures.SISImage;
import org.iucn.sis.shared.structures.SISLabel;
import org.iucn.sis.shared.structures.SISLivelihoods;
import org.iucn.sis.shared.structures.SISMap;
import org.iucn.sis.shared.structures.SISMultiSelect;
import org.iucn.sis.shared.structures.SISNumber;
import org.iucn.sis.shared.structures.SISOneToMany;
import org.iucn.sis.shared.structures.SISOptionsList;
import org.iucn.sis.shared.structures.SISQualifier;
import org.iucn.sis.shared.structures.SISRange;
import org.iucn.sis.shared.structures.SISRegionInformation;
import org.iucn.sis.shared.structures.SISRegionalExpertStructure;
import org.iucn.sis.shared.structures.SISRelatedStructures;
import org.iucn.sis.shared.structures.SISRichTextArea;
import org.iucn.sis.shared.structures.SISSelect;
import org.iucn.sis.shared.structures.SISStructureCollection;
import org.iucn.sis.shared.structures.SISText;
import org.iucn.sis.shared.structures.SISTextArea;
import org.iucn.sis.shared.structures.SISThreatStructure;
import org.iucn.sis.shared.structures.SISUpload;
import org.iucn.sis.shared.structures.Structure;
import org.iucn.sis.shared.structures.UseTrade;
import org.iucn.sis.shared.xml.XMLUtils;

import com.solertium.lwxml.gwt.debug.SysDebugger;

public class WidgetGenerator {

	public static final String COMPLEX = "complex";

	private static Structure buildStructureFromStructureConstructorPackage(StructureConstructorPackage structure) {
		return StructureGenerator(structure.getStructure(), structure.getDescription(), structure.getData());
	}

	public static Structure StructureGenerator(String theStructure, String description, Object data) {
		if (theStructure.equalsIgnoreCase(XMLUtils.BOOLEAN_STRUCTURE)) {
			return new SISBoolean(theStructure, description, data);
		}

		else if (theStructure.equalsIgnoreCase(XMLUtils.BOOLEAN_UNKNOWN_STRUCTURE)) {
			return new SISBooleanList(theStructure, description);
		}

		else if (theStructure.equalsIgnoreCase(XMLUtils.REGIONAL_INFORMATION)) {
			return new SISRegionInformation(theStructure, description);
		}

		else if (theStructure.equalsIgnoreCase(XMLUtils.EMPTY_STRUCTURE)) {
			return new SISEmptyStructure(theStructure, description, data);
		}

		else if (theStructure.equalsIgnoreCase(XMLUtils.RED_LIST_CATEGORIES_CRITERIA)) {
			return new SISCategoryAndCriteria(theStructure, description, "3.1");
		}

		else if (theStructure.equalsIgnoreCase(XMLUtils.REGIONAL_EXPERT_QUESTIONS_STRUCTURE)) {
			return new SISRegionalExpertStructure(theStructure, description);
		}

		else if (theStructure.equalsIgnoreCase(XMLUtils.REFERENCE_STRUCTURE)) {
			return new SISText(theStructure, description);
		} else if (theStructure.equalsIgnoreCase(XMLUtils.BOOLEAN_RANGE_STRUCTURE)) {
			return new SISBooleanRange(theStructure, description);
		} else if (theStructure.equalsIgnoreCase(XMLUtils.SINGLE_SELECT_STRUCTURE)) {
			return new SISSelect(theStructure, description, data);
		} else if (theStructure.equalsIgnoreCase(XMLUtils.MULTIPLE_SELECT_STRUCTURE)) {
			return new SISMultiSelect(theStructure, description, data);
		} else if (theStructure.equalsIgnoreCase(XMLUtils.QUALIFIER_STRUCTURE)) {
			return new SISQualifier(theStructure, "Qualification: ");
		} else if (theStructure.equalsIgnoreCase(XMLUtils.LABEL_STRUCTURE)) {
			return new SISLabel(theStructure, description);
		} else if (theStructure.equalsIgnoreCase(XMLUtils.OPTIONS_LIST)) {
			return new SISOptionsList(theStructure, description, data);
		} else if (theStructure.equalsIgnoreCase(XMLUtils.LIVELIHOODS)) {
			return new SISLivelihoods(theStructure, description, data);
		}

		else if (theStructure.equalsIgnoreCase(XMLUtils.ONE_TO_MANY)) {
			return new SISOneToMany(theStructure, description, (DisplayData) data);
		} else if (theStructure.equalsIgnoreCase(XMLUtils.USE_TRADE)) {
			return new UseTrade(theStructure, description);
		}

		// TEXTAREA
		else if (theStructure.equalsIgnoreCase(XMLUtils.NARRATIVE_STRUCTURE)) {
			return new SISTextArea(theStructure, description);
		} else if (theStructure.equalsIgnoreCase(XMLUtils.JUSTIFICATION_STRUCTURE)) {
			return new SISTextArea(theStructure, "Justification: ");
		} else if (theStructure.equalsIgnoreCase(XMLUtils.RICH_TEXT_STRUCTURE)) {
			return new SISRichTextArea(theStructure, description);
		} else if (theStructure.equalsIgnoreCase(XMLUtils.NOTE_STRUCTURE)) {
			return new SISHideableNote(theStructure, description);
		}

		// TEXTBOX
		else if (theStructure.equalsIgnoreCase(XMLUtils.TEXT_STRUCTURE)) {
			return new SISText(theStructure, description);
		} else if (theStructure.equalsIgnoreCase(XMLUtils.NUMBER_STRUCTURE)) {
			return new SISNumber(theStructure, description);
		} else if (theStructure.equalsIgnoreCase(XMLUtils.DATE_STRUCTURE)) { // wILL
			// NEED
			// FIXING
			// !
			return new SISDate(theStructure, description);
		} else if (theStructure.equalsIgnoreCase(XMLUtils.RANGE_STRUCTURE)) {
			return new SISRange(theStructure, description);
		} else if (theStructure.equalsIgnoreCase(XMLUtils.FILE_STRUCTURE)) {
			return new SISUpload(theStructure, description);
		}

		// COMPLEX STUFF
		else if (theStructure.equalsIgnoreCase(COMPLEX)) {
			return new SISBoolean(theStructure, description, null);
		} else if (theStructure.equalsIgnoreCase(XMLUtils.MAP_STRUCTURE)) {
			return new SISMap(theStructure, description, data);
		} else if (theStructure.equalsIgnoreCase(XMLUtils.IMAGE_STRUCTURE)) {
			return new SISImage(theStructure, description, data);
		} else if (theStructure.equalsIgnoreCase(XMLUtils.THREAT_STRUCTURE)) {
			Structure threat = new SISThreatStructure(theStructure, description);
			threat.setCanRemoveDescription(false);
			return threat;
		} else if (theStructure.equalsIgnoreCase(XMLUtils.STRUCTURE_COLLECTION)) {
			return new SISStructureCollection(theStructure, description, data);

			/*
			 * ArrayList structures = new ArrayList(); ArrayList structureData =
			 * (ArrayList)data; for (int i = 0; i < structureData.size(); i++) {
			 * structures.add(buildStructureFromStructureConstructorPackage((
			 * StructureConstructorPackage)structureData.get(i))); } return new
			 * SISStructureCollection(theStructure, description, structures);
			 */
		} else if (theStructure.equalsIgnoreCase(XMLUtils.DOMINANT_STRUCTURE_COLLECTION)) {
			return new SISDominantStructureCollection(theStructure, description, data);
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

			SISRelatedStructures s = new SISRelatedStructures(theStructure, description, doms, deps, rules);
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
			return new SISButton(theStructure, description);
		} else if (theStructure.equalsIgnoreCase(XMLUtils.TREE_STRUCTURE)) {
			return new SISClassificationSchemeStructure(theStructure, description, data);
			// return new SISTreeStructure(theStructure, description, data);
		} else {
			SysDebugger.getInstance().println(theStructure + " NULL!!!!!");
			return null;
		}
	}
}
