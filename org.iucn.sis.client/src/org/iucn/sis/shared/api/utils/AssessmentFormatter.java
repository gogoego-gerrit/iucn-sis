package org.iucn.sis.shared.api.utils;

import org.iucn.sis.client.api.caches.UserCache;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.Field;
import org.iucn.sis.shared.api.models.primitivefields.BooleanPrimitiveField;
import org.iucn.sis.shared.api.models.primitivefields.ForeignKeyListPrimitiveField;
import org.iucn.sis.shared.api.models.primitivefields.StringPrimitiveField;
import org.iucn.sis.shared.api.structures.SISCategoryAndCriteria;

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
		String cat = "";
		BooleanPrimitiveField isManualPF = (BooleanPrimitiveField) assessment.getPrimitiveField(CanonicalNames.RedListCriteria, SISCategoryAndCriteria.IS_MANUAL_KEY);
		StringPrimitiveField categoryField;
		if (isManualPF != null && isManualPF.getValue())
			categoryField = (StringPrimitiveField) assessment.getPrimitiveField(CanonicalNames.RedListCriteria, SISCategoryAndCriteria.MANUAL_CATEGORY_KEY);	
		else 
			categoryField = (StringPrimitiveField) assessment.getPrimitiveField(CanonicalNames.RedListCriteria, SISCategoryAndCriteria.GENERATED_CATEGORY_KEY);
		
		if (categoryField != null) {
			cat = categoryField.getValue();
		} else {
			cat = "N/A";
		}			

		return cat;
	}
	
	public static String getProperCriteriaString(Assessment assessment) {
		String criteria = "";
		BooleanPrimitiveField isManualPF = (BooleanPrimitiveField) assessment.getPrimitiveField(CanonicalNames.RedListCriteria, SISCategoryAndCriteria.IS_MANUAL_KEY);
		StringPrimitiveField criteriaField;
		if (isManualPF != null && isManualPF.getValue()) 
			criteriaField = (StringPrimitiveField) assessment.getPrimitiveField(CanonicalNames.RedListCriteria, SISCategoryAndCriteria.MANUAL_CRITERIA_KEY);	
		else 
			criteriaField = (StringPrimitiveField) assessment.getPrimitiveField(CanonicalNames.RedListCriteria, SISCategoryAndCriteria.GENERATED_CRITERIA_KEY);
		
		if (criteriaField != null) {
			criteria = criteriaField.getValue();
		} else {
			criteria = "N/A";
		}			

		return criteria;
	}

}
