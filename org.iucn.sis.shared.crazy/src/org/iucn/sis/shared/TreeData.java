package org.iucn.sis.shared;

import java.io.Serializable;
import java.util.ArrayList;

public class TreeData extends DisplayData implements Serializable {

	/**
	 * ArrayList<TreeDataRow>
	 */
	private ArrayList<TreeDataRow> treeRoots;
	private TreeData defaultStructure;

	public TreeData() {
		super(DisplayData.TREE);
		treeRoots = new ArrayList<TreeDataRow>();
	}

	public void addTreeRoot(TreeDataRow rootToAdd) {
		this.treeRoots.add(rootToAdd);
	}

	public DisplayData getDefaultStructure() {
		return defaultStructure;
	}

	public ArrayList<TreeDataRow> getTreeRoots() {
		return treeRoots;
	}

	public void setDefaultStructure(TreeData defaultStructure) {
		this.defaultStructure = defaultStructure;
	}

}
