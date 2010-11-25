package org.iucn.sis.shared.api.displays;

import org.iucn.sis.shared.api.data.TreeData;
import org.iucn.sis.shared.api.data.TreeDataRow;
import org.iucn.sis.shared.api.debug.Debug;
import org.iucn.sis.shared.api.displays.threats.BasicThreatViewer;
import org.iucn.sis.shared.api.displays.threats.ThreatClassificationSchemeModelData;
import org.iucn.sis.shared.api.displays.threats.ThreatViewerFactory;
import org.iucn.sis.shared.api.displays.threats.ThreatsClassificationSchemeViewer;
import org.iucn.sis.shared.api.displays.threats.ThreatsTreeData;
import org.iucn.sis.shared.api.models.Field;
import org.iucn.sis.shared.api.schemes.ClassificationSchemeModelData;
import org.iucn.sis.shared.api.schemes.ClassificationSchemeViewer;
import org.iucn.sis.shared.api.structures.DisplayStructure;

import com.solertium.lwxml.shared.NativeNode;

public class Threats extends ClassificationScheme {
	
	public Threats(NativeNode node) {
		super(new ThreatsTreeData(node));
	}
	
	@Override
	protected BasicThreatViewer generateDefaultDisplayStructure(TreeDataRow row) {
		ThreatsTreeData treeData = (ThreatsTreeData) this.treeData;
		
		BasicThreatViewer structure = ThreatViewerFactory.generateStructure(treeData, row);
		structure.setIsVisible(treeData.getIsVisible());
		structure.setName(treeData.getName());
		
		return structure;
	}
	
	@Override
	public void save() {
		super.save();
		Debug.println(field);
	}
	
	@Override
	protected ClassificationSchemeViewer createViewer(String description, TreeData displayData) {
		return new ThreatsClassificationSchemeViewer(description, displayData);
	}
	
	protected ClassificationSchemeModelData createModelData(DisplayStructure structure, Field field) {
		return new ThreatClassificationSchemeModelData(structure, field);
	}

}
