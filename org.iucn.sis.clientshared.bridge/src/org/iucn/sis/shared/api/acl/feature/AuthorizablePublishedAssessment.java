package org.iucn.sis.shared.api.acl.feature;

import org.iucn.sis.shared.api.models.AssessmentType;
import org.iucn.sis.shared.api.models.Taxon;

/**
 * An implementation of AuthorizableObject that can be used to ask questions about 
 * a published assessment in general, scoped to the taxon given as a parameter to the constructor.
 * 
 * @author adam.schwartz
 */
public class AuthorizablePublishedAssessment extends AuthorizableAssessmentShim {

	private String regionValue;
	
	public AuthorizablePublishedAssessment(Taxon taxon, String schema) {
		this(taxon, schema, null);
	}
	
	public AuthorizablePublishedAssessment(Taxon taxon, String schema, String region) {
		super(taxon, schema);
		regionValue = region;
	}
	
	public String getFullURI() {
		return "resource/assessment/" + AssessmentType.PUBLISHED_ASSESSMENT_TYPE;
	}
	
	@Override
	public String getProperty(String key) {
		if(regionValue != null && "region".equalsIgnoreCase(key) )
			return regionValue;
		else
			return super.getProperty(key);
	}
}
