package org.iucn.sis.shared.api.displays.threats;

import java.util.HashMap;
import java.util.Map;

import org.iucn.sis.shared.api.data.LookupData;
import org.iucn.sis.shared.api.data.TreeData;
import org.iucn.sis.shared.api.utils.FieldParser;

import com.solertium.lwxml.shared.NativeElement;
import com.solertium.lwxml.shared.NativeNode;
import com.solertium.lwxml.shared.NativeNodeList;

public class ThreatsTreeData extends TreeData {
	
	private static final long serialVersionUID = 1L;
	
	private final Map<String, TreeData> additonalTreeData;

	public ThreatsTreeData(NativeNode document) {
		additonalTreeData = new HashMap<String, TreeData>();
		
		final NativeNodeList nodes = document.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			final NativeNode node = nodes.item(i);
			if ("canonicalName".equals(node.getNodeName()))
				setCanonicalName(node.getTextContent());
			else if ("description".equals(node.getNodeName()))
				setDescription(node.getTextContent());
			else if ("classOfService".equals(node.getNodeName()))
				setClassOfService(node.getTextContent());
			else if ("lookup".equals(node.getNodeName())) {
				final LookupData data = new LookupData();
				final NativeNodeList options = ((NativeElement)node).getChildNodes();
				for (int k = 0; k < options.getLength(); k++) {
					final NativeNode option = options.item(k);
					if ("option".equals(option.getNodeName()))
						data.addValue(((NativeElement)option).getAttribute("id"), option.getTextContent());
				}
				addLookup(((NativeElement)node).getAttribute("name"), data);
			}
			else if ("coding".equals(node.getNodeName())) {
				final String name = ((NativeElement)node).getAttribute("name");
				final boolean isAdditional;
				final TreeData treeDataContainer;
				if (isAdditional = !"Threats".equals(name))
					treeDataContainer = new TreeData();
				else
					treeDataContainer = this;
				
				final NativeNodeList children = node.getChildNodes();
				for (int k = 0; k < children.getLength(); k++) {
					final NativeNode current = children.item(k);
					if ("root".equals(current.getNodeName()))
						treeDataContainer.addTreeRoot(FieldParser.
							processRoot(current, treeDataContainer, 
								treeDataContainer.getLookups()));
				}
				
				if (isAdditional)
					additonalTreeData.put(name, treeDataContainer);
			}
		}
	}
	
	public TreeData getTreeData(String key) {
		if (getCanonicalName().equals(key))
			return this;
		else
			return additonalTreeData.get(key);
	}
	
	@Override
	public String toString() {
		return "Found " + additonalTreeData.size() + " tree data, " + lookups.size() + " lookups, and " + getTreeRoots().size() + " roots for " + getCanonicalName() + ": " + getDescription();
	}

}
