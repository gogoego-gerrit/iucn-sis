package org.iucn.sis.shared.api.criteriacalculator;

import org.iucn.sis.shared.api.criteriacalculator.ExpertResult.ResultCategory;
import org.iucn.sis.shared.api.debug.Debug;

public class CriteriaResult {

	private final ResultCategory category;
	private final String criterion;
	
	public CriteriaSet criteriaSet;
	public Range range;

	CriteriaResult(ResultCategory category, String criterion) {
		this.category = category;
		this.criterion = criterion;
		this.criteriaSet = new CriteriaSet(category);
		range = null;
	}
	
	protected void printRange() {
		if (FuzzyExpImpl.VERBOSE)
			if (range != null)
				Debug.println("Range results from {0}{1}: Low: {2}, LowBest: {3}, HighBest: {4}, High: {5}",
						category, criterion, range.getLow(), range.getLowBest(), range.getHighBest(), range.getHigh()
				);
			else 
				Debug.println("- {0}{1} == null", category, criterion);
	}
	
	public void setCriteriaSet(CriteriaSet criteriaSet) {
		this.criteriaSet = criteriaSet;
	}
	
	public CriteriaSet getCriteriaSet() {
		return criteriaSet;
	}
	
	public String getResultString() {
		return criteriaSet.toString();
	}

}
