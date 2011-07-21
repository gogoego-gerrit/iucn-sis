package org.iucn.sis.shared.api.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.iucn.sis.shared.api.utils.CanonicalNames;

public class TreeData extends DisplayData implements Serializable {
	
	private static final long serialVersionUID = 1L;

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
	
	public Map<String, TreeDataRow> flattenTree() {
		final Map<String, TreeDataRow> map = new HashMap<String, TreeDataRow>();
		for (TreeDataRow row : treeRoots)
			flattenTree(map, row);
		
		return map;
	}
	
	private void flattenTree(Map<String, TreeDataRow> map, TreeDataRow parent) {
		map.put(parent.getDisplayId(), parent);
		for (TreeDataRow child : parent.getChildren())
			flattenTree(map, child);
	}

	public void setDefaultStructure(TreeData defaultStructure) {
		this.defaultStructure = defaultStructure;
	}
	
	public int getTopLevelDisplay() {
		if (CanonicalNames.CountryOccurrence.equals(getCanonicalName()))
			return 1;
		return 0;
	}

}
