package org.iucn.sis.shared.api.utils;

import org.iucn.sis.client.api.caches.UserCache;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.Field;
import org.iucn.sis.shared.api.models.fields.RedListCriteriaField;
import org.iucn.sis.shared.api.models.primitivefields.ForeignKeyListPrimitiveField;

public class AssessmentFormatter {
	
	/**
	 * Convenience method that will attempt to return generated assessors text.
	 * 
	 * @return assessors as a String, or an empty string
	 */
	public static String getDisplayableAssessors(Assessment assessment) {
		Field assessors = assessment.getField(CanonicalNames.RedListAssessors);
		if( assessors != null ) {
			ForeignKeyListPrimitiveField fks = ((ForeignKeyListPrimitiveField)assessors.getKeyToPrimitiveFields().get("assessors"));
			return UserCache.impl.generateTextFromUserIDs(fks.getValue());
		} else
			return "";
	}
	
	/**
	 * Convenience method that will attempt to return generated assessors text.
	 * 
	 * @return assessors as a String, or an empty string
	 */
	public static String getDisplayableEvaluators(Assessment assessment) {
		Field evaluators = assessment.getField(CanonicalNames.RedListEvaluators);
		if( evaluators != null ) {
			ForeignKeyListPrimitiveField fks = ((ForeignKeyListPrimitiveField)evaluators.getKeyToPrimitiveFields().get("evaluators"));
			return UserCache.impl.generateTextFromUserIDs(fks.getValue());
		} else
			return "";
	}
	
	public static String getProperCategoryAbbreviation(Assessment assessment) {
		RedListCriteriaField proxy = new RedListCriteriaField(assessment.getField(CanonicalNames.RedListCriteria));
		
		String category = proxy.isManual() ? proxy.getManualCategory() : proxy.getGeneratedCategory();
		
		return "".equals(category) ? "N/A" : category;
	}
	
	public static String getProperCriteriaString(Assessment assessment) {
		RedListCriteriaField proxy = new RedListCriteriaField(assessment.getField(CanonicalNames.RedListCriteria));
		
		String criteria = proxy.isManual() ? proxy.getManualCriteria() : proxy.getGeneratedCriteria();
		
		return "".equals(criteria) ? "N/A" : criteria;
	}

}
