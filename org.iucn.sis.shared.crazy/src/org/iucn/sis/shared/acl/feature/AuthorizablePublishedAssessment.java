package org.iucn.sis.shared.acl.feature;

import org.iucn.sis.shared.BaseAssessment;
import org.iucn.sis.shared.taxonomyTree.TaxonNode;

/**
 * An implementation of AuthorizableObject that can be used to ask questions about 
 * a published assessment in general, scoped to the taxon given as a parameter to the constructor.
 * 
 * @author adam.schwartz
 */
public class AuthorizablePublishedAssessment extends AuthorizableAssessmentShim {

	private String regionValue;
	
	public AuthorizablePublishedAssessment(TaxonNode taxon) {
		super(taxon);
	}
	
	public AuthorizablePublishedAssessment(TaxonNode taxon, String region) {
		super(taxon);
		regionValue = region;
	}
	
	public String getFullURI() {
		return "resource/assessment/" + BaseAssessment.PUBLISHED_ASSESSMENT_STATUS;
	}
	
	@Override
	public String getProperty(String key) {
		if(regionValue != null && "region".equalsIgnoreCase(key) )
			return regionValue;
		else
			return "";
	}
}
