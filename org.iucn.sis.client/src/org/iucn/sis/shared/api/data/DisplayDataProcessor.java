package org.iucn.sis.shared.api.data;

import java.util.ArrayList;
import java.util.HashMap;

import org.iucn.sis.shared.api.structures.DisplayData;
import org.iucn.sis.shared.api.structures.SISMap;
import org.iucn.sis.shared.api.structures.SISMapData;
import org.iucn.sis.shared.api.structures.SISSelect;
import org.iucn.sis.shared.api.structures.SISStructureCollection;
import org.iucn.sis.shared.api.structures.Structure;
import org.iucn.sis.shared.api.structures.WidgetGenerator;
import org.iucn.sis.shared.api.utils.XMLUtils;

import com.solertium.lwxml.gwt.debug.SysDebugger;

public class DisplayDataProcessor {

	private static void printDiagnostic(DisplayData data, Exception e) {
		// e.printStackTrace();

		String err = "Fatal error. Please report to SIS IT administrators.";
		err += "Adding Display ID " + data.getDisplayId() + " failed!";
		err += ("Canonical Name: " + data.getCanonicalName());
		err += ("Description: " + data.getDescription());
		err += ("Structure type: " + data.getStructure());
		err += ("Data: " + ((data.getData() == null) ? "null" : data.getData()));
		err += ("Error Message: " + e.getMessage());
		System.out.println(err);
		// Window.alert( err );
	}

	/**
	 * Creates a structure from DisplayData
	 * 
	 * @param currentDisplayData
	 *            data needed to make the structure
	 * @return the built Structure
	 */
	public static Structure processDisplayStructure(DisplayData currentDisplayData) {
		if (currentDisplayData == null) {
			SysDebugger.getInstance().println("No good data to build a Display from.");
			return null;
		}

		String fieldStructure = currentDisplayData.getStructure();
		String uniqueID = currentDisplayData.getUniqueId();
		Structure structure = null;

		if (fieldStructure.equalsIgnoreCase(XMLUtils.MULTIPLE_SELECT_STRUCTURE)
				|| fieldStructure.equalsIgnoreCase(XMLUtils.SINGLE_SELECT_STRUCTURE)) {

			try {
				HashMap dataValues = new HashMap();

				dataValues.put(SISSelect.LISTBOX, ((ArrayList) currentDisplayData.getData()).get(1));

				structure = WidgetGenerator.StructureGenerator(fieldStructure, currentDisplayData.getDescription(),
						uniqueID, currentDisplayData.getData());

				// structure.setValues( dataValues );

			} catch (Exception e) {
				printDiagnostic(currentDisplayData, e);
			}
		} else if (fieldStructure.equalsIgnoreCase(XMLUtils.MAP_STRUCTURE)) {
			ArrayList mapData = (ArrayList) currentDisplayData.getData();
			ArrayList dataPoints = new ArrayList();

			HashMap dataValues = new HashMap();
			try {
				for (int i = 0; i < ((ArrayList) mapData.get(0)).size(); i++) {
					dataPoints.add(new SISMapData((String) ((ArrayList) mapData.get(0)).get(i),
							((Double) ((ArrayList) mapData.get(1)).get(i)).doubleValue(),
							((Double) ((ArrayList) mapData.get(2)).get(i)).doubleValue(), "",
							(String) ((ArrayList) mapData.get(3)).get(i)));
				}

				dataValues.put(SISMap.MAP, dataPoints);

				structure = WidgetGenerator.StructureGenerator(fieldStructure, currentDisplayData.getDescription(),
						uniqueID, dataPoints);

				// structure.setValues(dataValues);
			} catch (Exception e) {
				dataValues.put(SISMap.MAP, null);

				structure = WidgetGenerator.StructureGenerator(fieldStructure, currentDisplayData.getDescription(),
						uniqueID, null);

				// structure.setValues(dataValues);
			}
		}

		else if (fieldStructure.equalsIgnoreCase(XMLUtils.THREAT_STRUCTURE)) {
			try {
				HashMap dataValues = new HashMap();
				ArrayList data = (ArrayList) currentDisplayData.getData();

				structure = WidgetGenerator.StructureGenerator(fieldStructure, currentDisplayData.getDescription(),
						uniqueID, null);

				// Non-codeable
				if (data == null || data.size() < 3) {
					structure.hideWidgets();
					// dataValues.put("NC", "NC");
				}

				// Codeable
				else {
					try {
						dataValues.put(XMLUtils.THREAT_TIMING, data.get(0));
						dataValues.put(XMLUtils.THREAT_SCOPE, data.get(1));
						dataValues.put(XMLUtils.THREAT_SEVERITY, data.get(2));
						dataValues.put(XMLUtils.THREAT_IMPACT, data.get(3));
					} catch (Exception e) {
						printDiagnostic(currentDisplayData, e);
						dataValues = null;
					}
					// structure.setValues(dataValues);
				}
			} catch (Exception e) {
				printDiagnostic(currentDisplayData, e);
			}
		}

		else if (fieldStructure.equalsIgnoreCase(XMLUtils.USE_TRADE)) {
			try {
				structure = WidgetGenerator.StructureGenerator(fieldStructure, currentDisplayData.getDescription(),
						uniqueID, null);

			} catch (Exception e) {
				printDiagnostic(currentDisplayData, e);
			}
		}

		else if (fieldStructure.equalsIgnoreCase(XMLUtils.ONE_TO_MANY)) {
			try {
				structure = WidgetGenerator.StructureGenerator(fieldStructure, currentDisplayData.getDescription(),
						uniqueID, currentDisplayData.getData());

			} catch (Exception e) {
				printDiagnostic(currentDisplayData, e);
			}
		}

		else if (fieldStructure.equalsIgnoreCase(XMLUtils.RANGE_STRUCTURE)) {
			try {
				structure = WidgetGenerator.StructureGenerator(fieldStructure, currentDisplayData.getDescription(),
						uniqueID, null);

				/*
				 * structure = new StructureConstructorPackage(fieldStructure,
				 * currentDisplayData.getDescription(), null, dataValues);
				 */
			} catch (Exception e) {
				printDiagnostic(currentDisplayData, e);
			}
		}

		else if (fieldStructure.equalsIgnoreCase(XMLUtils.RELATED_STRUCTURE)) {
			try {
				ArrayList data = (ArrayList) currentDisplayData.getData();
				ArrayList pack = new ArrayList();
				ArrayList dependentStructures = new ArrayList();

				DisplayData domDisplayData = (DisplayData) data.get(0);
				if (!currentDisplayData.getDescription().equals("") && (currentDisplayData.getType().equalsIgnoreCase(DisplayData.TREE)
						|| domDisplayData.getDescription() == null || domDisplayData.getDescription().equals("")))
					domDisplayData.setDescription(currentDisplayData.getDescription());
				/* 1 */pack.add(processDisplayStructure(domDisplayData));

				for (int i = 0; i < ((ArrayList) data.get(1)).size(); i++)
					dependentStructures.add(processDisplayStructure((DisplayData) ((ArrayList) data.get(1)).get(i)));
				/* 2 */pack.add(dependentStructures);

				/* 3 */pack.add((data.get(2)));

				// Layout
				pack.add((data).get(3));
				pack.add((data).get(4));

				structure = WidgetGenerator.StructureGenerator(fieldStructure, currentDisplayData.getDescription(),
						uniqueID, pack);

				// Does nothing BUT it initiates ChangeTracking/DataSaving
				// structure.setValues(null);

				/*
				 * structure = new StructureConstructorPackage(fieldStructure,
				 * currentDisplayData.getDescription(), pack, null);
				 */

			} catch (Exception e) {
				printDiagnostic(currentDisplayData, e);
			}
		}

		else if (fieldStructure.equalsIgnoreCase(XMLUtils.CLASSIFICATION_SCHEME_STRUCTURE)) {

		}

		else if (fieldStructure.equalsIgnoreCase(XMLUtils.STRUCTURE_COLLECTION)
				|| fieldStructure.equalsIgnoreCase(XMLUtils.DOMINANT_STRUCTURE_COLLECTION)) {

			try {
				ArrayList structures = new ArrayList();
				for (int i = 0; i < ((ArrayList) currentDisplayData.getData()).size(); i++) {
					DisplayData temp = (FieldData) ((ArrayList) currentDisplayData.getData()).get(i);
					structures.add(processDisplayStructure(temp));
				}

				int layout = 1;
				if (currentDisplayData.getStyle() != null) {
					if (currentDisplayData.getStyle().equalsIgnoreCase("vertical"))
						layout = 1;
					else if (currentDisplayData.getStyle().equalsIgnoreCase("horizontal"))
						layout = 2;
				}
				structure = WidgetGenerator.StructureGenerator(fieldStructure, currentDisplayData.getDescription(),
						uniqueID, structures);
				((SISStructureCollection) structure).setDisplayType(layout);

				/*
				 * structure = new StructureConstructorPackage(fieldStructure,
				 * currentDisplayData.getDescription(), structures, null);
				 */
			} catch (Exception e) {
				printDiagnostic(currentDisplayData, e);
			}
		}

		else if (fieldStructure.equalsIgnoreCase(XMLUtils.TREE_STRUCTURE)) {
			try {
				structure = WidgetGenerator.StructureGenerator(fieldStructure, currentDisplayData.getDescription(),
						uniqueID, currentDisplayData.getData());

				// structure.setValues(null);
				/*
				 * structure = new StructureConstructorPackage(fieldStructure,
				 * currentDisplayData.getDescription(),
				 * currentDisplayData.getData(), null);
				 */
			} catch (Exception e) {
				printDiagnostic(currentDisplayData, e);
			}
		}

		else {
			try {
				// HashMap dataValues = new HashMap();

				// dataValues.put(fieldStructure, currentDisplayData.getData());

				structure = WidgetGenerator.StructureGenerator(fieldStructure, currentDisplayData.getDescription(),
						uniqueID, currentDisplayData.getData());

				// structure.setValues(dataValues);

				/*
				 * structure = new StructureConstructorPackage(fieldStructure,
				 * currentDisplayData.getDescription(), null, dataValues);
				 */
			} catch (Exception e) {
				printDiagnostic(currentDisplayData, e);
			}
		}

		try {
			structure.setIsVisible(currentDisplayData.getIsVisible());
			structure.setName(currentDisplayData.getName());
			// structure.setStyle(currentDisplayData.getStyle());
			// structure.setTitle(currentDisplayData.getTitle());
			System.out.println("Setting ID for structure to be " + currentDisplayData.getUniqueId());
			structure.setId(currentDisplayData.getUniqueId());

			/* structure.setDisplayData(currentDisplayData); */
		} catch (NullPointerException e) {
			printDiagnostic(currentDisplayData, e);
		}

		return structure;
	}

}