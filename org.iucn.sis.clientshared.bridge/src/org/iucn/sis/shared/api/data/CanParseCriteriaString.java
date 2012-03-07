package org.iucn.sis.shared.api.data;

import org.iucn.sis.shared.api.criteriacalculator.CriteriaSet;
import org.iucn.sis.shared.api.criteriacalculator.ExpertResult.ResultCategory;

public abstract class CanParseCriteriaString {

	/**
	 * This function will be called when a particular criterion is parsed out.
	 * The "prefix" is the first letter (A, B, C, D, or E) and the "suffix" is
	 * the rest of the criterion (e.g. 1, 2b(iii), etc).
	 * 
	 * @param prefix
	 * @param suffix
	 */
	public abstract void foundCriterion(String criterion);

	public final void parseCriteriaString(String criteria) {
		if (criteria == null || "".equals(criteria))
			return;

		CriteriaSet set = CriteriaSet.fromString(ResultCategory.DD, criteria);
		for (String criterion : set.getCriteria())
			foundCriterion(criterion);
	}

}
