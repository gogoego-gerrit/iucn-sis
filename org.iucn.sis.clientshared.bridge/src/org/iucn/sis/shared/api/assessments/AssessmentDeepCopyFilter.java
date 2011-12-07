package org.iucn.sis.shared.api.assessments;

import java.util.ArrayList;
import java.util.List;

import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.Field;
import org.iucn.sis.shared.api.models.PrimitiveField;
import org.iucn.sis.shared.api.models.Reference;
import org.iucn.sis.shared.api.models.fields.RedListCriteriaField;
import org.iucn.sis.shared.api.utils.CanonicalNames;

public class AssessmentDeepCopyFilter implements Assessment.DeepCopyFilter {
	
	private final List<String> excluded;
	
	public AssessmentDeepCopyFilter() {
		excluded = new ArrayList<String>();
		excluded.add(CanonicalNames.RedListAssessmentDate);
		excluded.add(CanonicalNames.RedListEvaluators);
		excluded.add(CanonicalNames.RedListAssessmentAuthors);
		excluded.add(CanonicalNames.RedListReasonsForChange);
		excluded.add(CanonicalNames.RedListPetition);
		excluded.add(CanonicalNames.RedListEvaluated);
		excluded.add(CanonicalNames.RedListConsistencyCheck);
	}
	
	@Override
	public Field copy(Assessment assessment, Field field) {
		if (excluded.contains(field.getName())) {
			/*
			 * First, exclude certain fields.
			 */
			return null;
		}
		else if (CanonicalNames.RedListCriteria.equals(field.getName())) {
			RedListCriteriaField proxy = new RedListCriteriaField(field);
			Integer version = proxy.getCriteriaVersion();
			if (0 == version.intValue()) {
				/*
				 * Will be 0 if there is no data or if 
				 * the most current version is selected. 
				 * Either way, we want to remove the data.
				 */
				return null;
			}
			else {
				/*
				 * Return the field, but remove the history text.
				 */
				Field copy = field.deepCopy(false, this);
				PrimitiveField<?> historyText = 
					copy.getPrimitiveField(RedListCriteriaField.RLHISTORY_TEXT_KEY);
				if (historyText != null)
					copy.getPrimitiveField().remove(historyText);
				
				return copy;
			}
		}
		else {
			/*
			 * Else, return a copy of the field
			 */
			return field.deepCopy(false, this);
		}
	}
	
	@Override
	public Reference copyReference(Reference source) {
		return source.deepCopy();
	}
	
}
