package org.iucn.sis.shared.data.assessments;

import org.iucn.sis.client.data.assessments.AssessmentClientSaveUtils;

public class AssessmentUtilFactory {
	
	private static AssessmentUtilFactory defaultInstance = new AssessmentUtilFactory();
	
	public static AssessmentUtils getSaveUtils() {
		return new AssessmentClientSaveUtils();
	}
	
	public static AssessmentUtilFactory getDefaultInstance() {
		return defaultInstance;
	}
	
	public static void setDefaultInstance(AssessmentUtilFactory instance) {
		defaultInstance = instance;
	}
	
}
