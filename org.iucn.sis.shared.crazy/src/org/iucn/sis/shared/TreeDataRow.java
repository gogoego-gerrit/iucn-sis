package org.iucn.sis.shared;

import java.util.ArrayList;

public class TreeDataRow extends DisplayData {

	// Tree data
	private ArrayList<TreeDataRow> children;

	private String codeable;
	private String expanded;
	private boolean usesDefaultStructure;

	private String rowNumber;
	private String depth;

	public TreeDataRow() {
		super(DisplayData.TREE);
		this.depth = "0";
		this.codeable = "true";
		this.expanded = "false";
		this.children = new ArrayList<TreeDataRow>();
	}

	public void addChild(TreeDataRow child) {
		children.add(child);
	}

	public ArrayList<TreeDataRow> getChildren() {
		return children;
	}

	public String getCodeable() {
		return codeable;
	}

	public String getDepth() {
		return depth;
	}

	public String getExpanded() {
		return expanded;
	}

	public String getLabel() {
		return rowNumber + " " + description;
	}

	public String getRowNumber() {
		return rowNumber;
	}

	public boolean getUsesDefaultStructure() {
		return usesDefaultStructure;
	}

	public boolean hasChildren() {
		return !children.isEmpty();
	}

	public void setChildren(ArrayList<TreeDataRow> children) {
		this.children = children;
	}

	public void setCodeable(String codeable) {
		this.codeable = codeable;
	}

	public void setDepth(String depth) {
		this.depth = depth;
	}

	public void setExpanded(String expanded) {
		this.expanded = expanded;
	}

	public void setLabel(String description) {
		setDescription(description);
	}

	public void setRowNumber(String rowNumber) {
		this.rowNumber = rowNumber;
	}

	public void setUsesDefaultStructure(boolean usesDefaultStructure) {
		this.usesDefaultStructure = usesDefaultStructure;
	}

}
