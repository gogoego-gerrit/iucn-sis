/**
 * SISTree.java
 * 
 * Represents a tree structure derived from the master list of display objects
 * Creates a tree item, gives it its own widgets and any children
 * 
 * @author carl.scott
 */

package org.iucn.sis.client.displays;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.iucn.sis.client.utilities.StructureCollection;
import org.iucn.sis.shared.structures.SISRelatedStructures;
import org.iucn.sis.shared.structures.SISStructureCollection;
import org.iucn.sis.shared.structures.Structure;
import org.iucn.sis.shared.xml.XMLUtils;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Widget;

public class SISRow {

	private ArrayList children;
	private StructureCollection myStructures;
	private HashMap values;

	private boolean codeable = true;
	private boolean expanded = true;
	private boolean usesDefaultStructure = false;

	private String rowID;
	private String label;
	private String depth;

	public SISRow(Structure structure) {
		children = new ArrayList();
		myStructures = new StructureCollection();
		if (structure.getStructureType().equalsIgnoreCase(XMLUtils.STRUCTURE_COLLECTION)) {
			((SISStructureCollection) structure).setDisplayType(SISStructureCollection.FLEXTABLE);
			for (int i = 0; i < ((SISStructureCollection) structure).getStructures().size(); i++) {
				// SysDebugger.getInstance().println("This is a " +
				// ((SISStructureCollection
				// )structure).getStructureAt(i).getStructureType());
				if ((((SISStructureCollection) structure).getStructureAt(i)).getStructureType().equalsIgnoreCase(
						XMLUtils.RELATED_STRUCTURE)) {
					((SISRelatedStructures) (((SISStructureCollection) structure).getStructureAt(i)))
							.setDisplayType(SISRelatedStructures.FLEXTABLE_NODESCRIPTION);
				}
			}
		}
		myStructures.add(structure);
	}

	/************ PUBLIC FUNCTIONS ***************/

	public void addChild(SISRow child) {
		children.add(child);
	}

	public void disableStructures() {
		for (int i = 0; i < myStructures.size(); i++)
			myStructures.getStructure(i).disable();
		if (hasChild())
			for (int j = 0; j < children.size(); j++)
				((SISRow) children.get(j)).disableStructures();
	}

	public void enableStructures() {
		for (int i = 0; i < myStructures.size(); i++)
			myStructures.getStructure(i).enable();
		if (hasChild())
			for (int j = 0; j < children.size(); j++)
				((SISRow) children.get(j)).enableStructures();
	}

	public ArrayList getChildren() {
		return children;
	}

	public String getDepth() {
		return depth;
	}

	public String getLabel() {
		return label;
	}

	public StructureCollection getMyStructures() {
		return myStructures;
	}

	// Overrides Display's version
	public ArrayList getMyWidgets() {
		ArrayList retWidgets = new ArrayList();
		for (int i = 0; i < myStructures.size(); i++) {
			if (!codeable)
				myStructures.getStructure(i).hideWidgets();
			retWidgets.add(myStructures.getStructure(i).generate());
		}
		return retWidgets;
	}

	public Widget getMyWidgetsAsFlexTable() {
		FlexTable table = new FlexTable();
		int insert = 0;

		for (int i = 0; i < myStructures.size(); i++) {
			Structure current = myStructures.getStructure(i);
			if (!codeable)
				current.hideWidgets();
			table.setWidget(0, insert++, current.generate());
		}

		return table;
	}

	/*************** GETTERS AND SETTERS *************/

	public String getRowID() {
		return rowID;
	}

	
	public boolean hasChild() {
		return (children.size() > 0);
	}

	public void hideStructures() {
		for (int i = 0; i < myStructures.size(); i++)
			myStructures.getStructure(i).hide();
		if (hasChild())
			for (int j = 0; j < children.size(); j++)
				((SISRow) children.get(j)).hideStructures();
	}

	public boolean isCodeable() {
		return codeable;
	}

	public boolean isExpanded() {
		return expanded;
	}

	/*********** PRIVATE HELPER FUNCTIONS *****************/

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

	
	public void setCodeable(boolean codeable) {
		this.codeable = codeable;
	}

	public void setDepth(String depth) {
		this.depth = depth;
	}

	public void setExpanded(boolean expanded) {
		this.expanded = expanded;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public void setRowID(String rowID) {
		this.rowID = rowID;
	}

	public void setUsesDefaultStructure(boolean usesDefaultStructure) {
		this.usesDefaultStructure = usesDefaultStructure;
	}

	public void showStructures() {
		for (int i = 0; i < myStructures.size(); i++)
			myStructures.getStructure(i).show();
		if (hasChild())
			for (int j = 0; j < children.size(); j++)
				((SISRow) children.get(j)).showStructures();
	}

	public String toThinXML() {
		String xmlRetString = "";

		for (int i = 0; i < myStructures.size(); i++) {
			String temp = myStructures.getStructure(i).toXML();
			if (temp != "") {
				xmlRetString += "<treeItem id=\"" + rowID + ">\n";
				xmlRetString += temp;
				xmlRetString += "</treeItem>\n";
			}
		}

		if (hasChild()) {
			for (int j = 0; j < children.size(); j++) {
				xmlRetString += ((SISRow) children.get(j)).toThinXML();
			}
		}
		return xmlRetString;
	}

	/**
	 * Write this root to XML
	 * 
	 * @return the tree root as XML
	 */
	public String toXML() {
		return toXML("root");
	}

	private String toXML(String tagText) {
		String xmlRetString = "";

		String xmlCodeable = codeable ? "" : "codeable=\"false\" ";
		String xmlExpanded = expanded ? "" : "expanded=\"false\"";

		xmlRetString += "\t\t<" + tagText + " id=\"" + rowID + "\" " + xmlCodeable + xmlExpanded + ">\n";
		xmlRetString += "\t\t\t<label>" + label.replaceFirst(rowID, "").trim() + "</label>\n";
		if (!usesDefaultStructure) {
			xmlRetString += "\t\t\t\t<treeStructures>\n";
			for (int i = 0; i < myStructures.size(); i++)
				xmlRetString += myStructures.getStructure(i).toXML() + "\n";
			xmlRetString += "\t\t\t\t</treeStructures>\n";
		}

		if (hasChild()) {
			for (int j = 0; j < children.size(); j++) {
				xmlRetString += ((SISRow) children.get(j)).toXML("child");
			}
		}

		xmlRetString += "\t\t</" + tagText + ">\n";
		return xmlRetString;
	}

}
