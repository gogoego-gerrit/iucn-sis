package org.iucn.sis.shared.api.assessments;

import java.util.ArrayList;
import java.util.List;

import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.Field;
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
		excluded.add(CanonicalNames.RedListHistory);
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
			if (RedListCriteriaField.CRIT_VERSION_CURRENT >= version.intValue()) {
				return field.deepCopy(false, this);
			}
			else {
				Field newField = new Field(CanonicalNames.RedListCriteria, null);
				
				RedListCriteriaField newFieldProxy = new RedListCriteriaField(newField);
				newFieldProxy.setCriteriaVersion(RedListCriteriaField.CRIT_VERSION_CURRENT);
				
				return newField;
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
