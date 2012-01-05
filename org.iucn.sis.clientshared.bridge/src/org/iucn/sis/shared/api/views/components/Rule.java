package org.iucn.sis.shared.api.views.components;

import java.util.ArrayList;

public abstract class Rule {

	// On-true reaction constants
	public static final String ENABLE = "enable";
	public static final String SHOW = "show";

	// On-false reaction constants
	public static final String DISABLE = "disable";
	public static final String HIDE = "hide";

	protected String rule;
	protected ArrayList<Integer> affectedObjectIndices;

	protected String onTrue = "show";
	protected String onFalse = "hide";

	public Rule(String rule) {
		this(rule, new ArrayList<Integer>());
	}

	public Rule(String rule, ArrayList<Integer> affectedObjectIndices) {
		this.rule = rule;
		this.affectedObjectIndices = affectedObjectIndices;
	}

	public void addAffectedObjectIndex(String index) {
		addAffectedObjectIndex(Integer.valueOf(index.trim()));
	}

	public void addAffectedObjectIndex(int index) {
		this.affectedObjectIndices.add(index);
	}

	public ArrayList<Integer> getAffectedObjectIndices() {
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
