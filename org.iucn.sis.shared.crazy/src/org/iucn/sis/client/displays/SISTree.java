package org.iucn.sis.client.displays;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.iucn.sis.client.ui.TableGenerator;
import org.iucn.sis.shared.TreeData;
import org.iucn.sis.shared.structures.Structure;

import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class SISTree extends Display {

	private ArrayList rootRows; // SISRows
	// private Structure defaultTreeStructure = null;
	private HashMap values;

	public SISTree() {
		this("", "", null, "", "", "", "", "");
	}

	public SISTree(String struct, String descript, Object data, String group, String displayId, String canonicalName,
			String classOfService, String associate) {
		super(struct, descript, data, group, displayId, canonicalName, classOfService, associate);
	}

	public SISTree(TreeData treeData) {
		super(treeData);
		rootRows = new ArrayList();
	}

	public void addRoot(SISRow row) {
		rootRows.add(row);
	}

	@Override
	public void disableStructures() {
		for (int i = 0; i < rootRows.size(); i++)
			((SISRow) rootRows.get(i)).disableStructures();
	}

	@Override
	public void enableStructures() {
		for (int i = 0; i < rootRows.size(); i++)
			((SISRow) rootRows.get(i)).enableStructures();
	}

	@Override
	public Widget generateContent(boolean viewOnly) {
		displayPanel = new VerticalPanel();

		for (int i = 0; i < rootRows.size(); i++) {
			SISRow current = (SISRow) rootRows.get(i);
			if (current.getMyStructures().isEmpty()) {
				displayPanel.add(TableGenerator.generateEmptyTable());
			} else {
				displayPanel.add(TableGenerator.generateSISTreeTableRecursive(current));
			}
		}

		return displayPanel;
	}

	/**
	 * You should not need to ever use this, but it implements the Display
	 * function for it
	 */
	@Override
	public ArrayList getMyWidgets() {
		ArrayList retWidgets = new ArrayList();

		for (int i = 0; i < rootRows.size(); i++) {
			SISRow currentRoot = (SISRow) rootRows.get(i);
			retWidgets.addAll(currentRoot.getMyWidgets());

			if (currentRoot.hasChild())
				for (int j = 0; j < currentRoot.getChildren().size(); j++)
					retWidgets.addAll(((SISRow) currentRoot.getChildren().get(j)).getMyWidgets());
		}

		return retWidgets;
	}

	public ArrayList getRoots() {
		return rootRows;
	}

	@Override
	public void hideStructures() {
		for (int i = 0; i < rootRows.size(); i++)
			((SISRow) rootRows.get(i)).hideStructures();
	}

	private void putUnique(SISRow row, HashMap toMap, HashMap fromMap) {
		try {
			Iterator iterator = fromMap.keySet().iterator();
			while (iterator.hasNext()) {
				String key = (String) iterator.next();
				if (toMap.containsKey(row.getRowID() + key)) {
					int diff = -1;
					while (toMap.containsKey(row.getRowID() + key + (++diff)))
						;
					toMap.put(row.getRowID() + key + diff, fromMap.get(key));
				} else {
					toMap.put(row.getRowID() + key, fromMap.get(key));
				}
			}

		} catch (NullPointerException e) {
		}
	}

	public void setDefaultTreeStructure(Structure defaultTreeStructure) {
		// this.defaultTreeStructure = defaultTreeStructure;
	}

	@Override
	public void showStructures() {
		for (int i = 0; i < rootRows.size(); i++)
			((SISRow) rootRows.get(i)).showStructures();
	}

	@Override
	public String toThinXML() {
		String xmlRetString = "<tree id=\"" + this.displayID + "\">\n";
		String innerXML = "";
		xmlRetString += "\t<canonicalName>" + this.canonicalName + "</canonicalName>\n";
		for (int i = 0; i < rootRows.size(); i++) {
			String curRow = ((SISRow) rootRows.get(i)).toThinXML();
			if (curRow != "")
				innerXML += curRow;
		}
		if (innerXML.equalsIgnoreCase(""))
			return "";

		xmlRetString += innerXML;
		xmlRetString += "</tree>\n";
		return xmlRetString;
	}

	@Override
	public String toXML() {

		String xmlRetString = "";
		xmlRetString += "<tree id=\"" + displayID + "\">\n";
		xmlRetString += "\t<canonicalName>" + canonicalName + "</canonicalName>\n";
		xmlRetString += "\t<description>" + description + "</description>\n";
		xmlRetString += "\t<classOfService>" + classOfService + "</classOfService>\n";
		xmlRetString += "\t<location>" + location + "</location>\n";
		if (!referenceIds.isEmpty()) {
			xmlRetString += "\t<references>\n";
			for (int i = 0; i < referenceIds.size(); i++) {
				xmlRetString += "\t\t<referenceId>" + (String) referenceIds.get(i) + "</referenceId>\n";
			}
			xmlRetString += "\t</references>\n";
		}

		xmlRetString += "\t<treeRoot>\n";

		// //Write default structure
		// if (defaultTreeStructure != null) {
		// xmlRetString += "<defaultStructure>\n";
		// xmlRetString += "<treeStructures>\n";
		// xmlRetString += defaultTreeStructure.toXML( false );
		// xmlRetString += "</treeStructures>\n";
		// xmlRetString += "</defaultStructure>\n";
		// }

		// Write roots
		for (int i = 0; i < rootRows.size(); i++) {
			xmlRetString += ((SISRow) rootRows.get(i)).toXML();
		}

		xmlRetString += "\t</treeRoot>\n";
		xmlRetString += "\t</tree>\n";
		return xmlRetString;
	}

}
