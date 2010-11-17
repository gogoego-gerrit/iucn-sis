package org.iucn.sis.shared.api.criteriacalculator;

import org.iucn.sis.shared.api.debug.Debug;


public abstract class Classification {
	
	protected final String name;
	
	public Classification(String name) {
		this.name = name;
	}
	
	protected void println(String template, Object... args) {
		if (FuzzyExpImpl.VERBOSE)
			Debug.println(template, args);
	}
}
