package org.iucn.sis.shared.api.criteriacalculator;

import org.iucn.sis.shared.api.debug.Debug;

public class CriteriaResult {

	public String classification, category, resultString;
	public Range range;

	CriteriaResult(String classification, String category) {
		this.classification = classification;
		this.category = category;
		resultString = "";
		range = null;
	}
	
	protected void printRange() {
		if (FuzzyExpImpl.VERBOSE)
			if (range != null)
				Debug.println("Range results from {0}{1}: Low: {2}, LowBest: {3}, HighBest: {4}, High: {5}",
					classification, category, range.getLow(), range.getLowBest(), range.getHighBest(), range.getHigh()
				);
			else 
				Debug.println("- {0}{1} == null", classification, category);
	}

}
