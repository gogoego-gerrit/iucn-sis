package org.iucn.sis.shared.api.structures;

import java.util.ArrayList;

public class SelectRule extends Rule {

	int[] indices = null;

	public SelectRule(String rule) {
		this(rule, new ArrayList());
	}

	public SelectRule(String rule, ArrayList affectedObjectIndices) {
		super(rule, affectedObjectIndices);

		if (rule.indexOf(",") >= 0) {
			String[] rules = rule.split(",");
			indices = new int[rules.length];

			for (int i = 0; i < rules.length; i++)
				indices[i] = Integer.parseInt(rules[i]);
		}
	}

	public int getIndexInQuestion() {
		return Integer.parseInt(rule);
	}

	public boolean isSelected(int index) {
		if (indices != null) {
			for (int i = 0; i < indices.length; i++)
				if (indices[i] == index)
					return true;

			return false;
		} else
			return (index == Integer.parseInt(rule));
	}

	@Override
	public String toXML() {
		String xmlRetString = "<selectRule>\n";
		xmlRetString += "\t<activateOnIndex>" + rule + "</activateOnIndex>\n";
		xmlRetString += "\t<actions>\n";
		xmlRetString += "\t\t<onTrue>" + onTrue + "</onTrue>\n";
		xmlRetString += "\t\t<onFalse>" + onFalse + "</onFalse>\n";
		for (int i = 0; i < affectedObjectIndices.size(); i++) {
			xmlRetString += "\t\t<affectedDependentStructures>" + ((Integer) affectedObjectIndices.get(i)).intValue()
					+ "</affectedDependentStructures>\n";
		}
		xmlRetString += "\t</actions>\n";
		xmlRetString += "\t</selectRule>";
		return xmlRetString;
	}

}
