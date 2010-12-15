package org.iucn.sis.shared.api.structures;

import java.util.ArrayList;

public abstract class Rule {

	// On-true reaction constants
	public static final String ENABLE = "enable";
	public static final String SHOW = "show";

	// On-false reaction constants
	public static final String DISABLE = "disable";
	public static final String HIDE = "hide";

	protected String rule;
	protected ArrayList affectedObjectIndices;

	protected String onTrue = "show";
	protected String onFalse = "hide";

	public Rule(String rule) {
		this(rule, new ArrayList());
	}

	public Rule(String rule, ArrayList affectedObjectIndices) {
		this.rule = rule;
		this.affectedObjectIndices = affectedObjectIndices;
	}

	public void addAffectedObjectIndex(int index) {
		addAffectedObjectIndex("" + index);
	}

	public void addAffectedObjectIndex(String index) {
		this.affectedObjectIndices.add(Integer.valueOf(index.trim()));
	}

	public int getAffectedObjectIndex(int index) {
		return ((Integer) affectedObjectIndices.get(index)).intValue();
	}

	public ArrayList getAffectedObjectIndices() {
		return affectedObjectIndices;
	}

	public String getOnFalse() {
		return onFalse;
	}

	public String getOnTrue() {
		return onTrue;
	}

	public String getRule() {
		return rule;
	}

	public boolean isIndexAffected(int index) {
		return affectedObjectIndices.isEmpty() || affectedObjectIndices.contains(Integer.valueOf(index));
	}

	public void setOnFalse(String onFalse) {
		this.onFalse = onFalse;
	}

	public void setOnTrue(String onTrue) {
		this.onTrue = onTrue;
	}

	public abstract String toXML();

}
