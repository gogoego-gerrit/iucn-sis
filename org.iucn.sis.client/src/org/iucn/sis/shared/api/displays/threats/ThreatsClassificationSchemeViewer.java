package org.iucn.sis.shared.api.displays.threats;

import org.iucn.sis.shared.api.data.TreeData;
import org.iucn.sis.shared.api.schemes.BasicClassificationSchemeViewer;
import org.iucn.sis.shared.api.structures.Structure;

public class ThreatsClassificationSchemeViewer extends
		BasicClassificationSchemeViewer {

	public ThreatsClassificationSchemeViewer(String description, TreeData treeData) {
		super(description, treeData);
	}
	
	@Override
	protected ThreatClassificationSchemeModelData newInstance(Structure structure) {
		return new ThreatClassificationSchemeModelData(structure);
	}
	
	@Override
	protected Structure generateDefaultStructure() {
		ThreatsTreeData treeData = (ThreatsTreeData) this.treeData;
		
		BasicThreatViewer structure = new BasicThreatViewer(treeData);
		structure.setIsVisible(treeData.getIsVisible());
		structure.setName(treeData.getName());
		
		return structure;
	}

}
