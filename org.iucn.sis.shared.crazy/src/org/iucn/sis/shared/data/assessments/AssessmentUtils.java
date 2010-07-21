package org.iucn.sis.shared.data.assessments;

import org.iucn.sis.shared.acl.InsufficientRightsException;

import com.solertium.lwxml.shared.GenericCallback;

public interface AssessmentUtils {

	public void saveAssessment(final AssessmentData assessmentToSave, final GenericCallback<Object> callback)
		throws InsufficientRightsException;
	
	public boolean shouldSaveCurrentAssessment();
	
}
