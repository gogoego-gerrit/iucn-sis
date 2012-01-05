package org.iucn.sis.shared.api.displays.threats;

import org.iucn.sis.shared.api.views.components.ThreatsTreeData;
import org.iucn.sis.shared.api.views.components.TreeDataRow;

public class ThreatViewerFactory {
	
	public static boolean hasTaxa(TreeDataRow row) {
		return matches(row.getRowNumber(), "8.1.2", "8.2.2", "8.4.2");
	}
	
	public static boolean hasVirus(TreeDataRow row) {
		return matches(row.getRowNumber(), "8.5.2");
	}
	
	public static BasicThreatViewer generateStructure(ThreatsTreeData data, TreeDataRow row) {
		String value = row.getRowNumber();
		if (matches(value, "8.1.1", "8.2.1", "8.3", "8.4.1", "8.5.1", "8.6")) {
			return new ThreatWithTextViewer("Explanation: ", data);
		}
		else if (matches(value, "8.1.2", "8.2.2", "8.4.2")) {
			return new FeralTaxonThreatViewer(data);
		}
		else {
			return new BasicThreatViewer(data);
		}
	}
	
	private static boolean matches(String value, String... options) {
		for (String option : options)
			if (value.equals(option))
				return true;
		return false;
	}

}
