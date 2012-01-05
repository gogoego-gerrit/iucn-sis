package org.iucn.sis.shared.api.views.components;

import java.util.ArrayList;

public class BooleanRule extends Rule {

	public BooleanRule(String rule) {
		this(rule, new ArrayList<Integer>());
	}

	public BooleanRule(String rule, ArrayList<Integer> affectedObjectIndices) {
		super(rule, affectedObjectIndices);
	}

	public boolean isTrue() {
		return rule.equalsIgnoreCase("true");
	}

	@Override
	public String toXML() {
		String xmlRetString = "<booleanRule>\n";
		xmlRetString += "\t<activateOnRule>" + rule + "</activateOnRule>\n";
		xmlRetString += "\t<actions>\n";
		xmlRetString += "\t\t<onTrue>" + onTrue + "</onTrue>\n";
		xmlRetString += "\t\t<onFalse>" + onFalse + "</onFalse>\n";
		for (int i = 0; i < affectedObjectIndices.size(); i++) {
			xmlRetString += "\t\t<affectedDependentStructures>" + ((Integer) affectedObjectIndices.get(i)).intValue()
					+ "</affectedDependentStructures>\n";
		}
		xmlRetString += "\t</actions>\n";
		xmlRetString += "\t</booleanRule>";
		return xmlRetString;
	}

}
