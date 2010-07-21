package org.iucn.sis.shared.api.assessments;

import org.iucn.sis.shared.api.acl.InsufficientRightsException;
import org.iucn.sis.shared.api.models.Assessment;

import com.solertium.lwxml.shared.GenericCallback;

public interface AssessmentUtils {

	public void saveAssessment(final Assessment assessmentToSave, final GenericCallback<Object> callback)
		throws InsufficientRightsException;
	
	public boolean shouldSaveCurrentAssessment();
	
}
