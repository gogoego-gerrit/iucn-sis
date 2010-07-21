package org.iucn.sis.shared.api.acl.feature;

import org.iucn.sis.shared.api.models.Taxon;


public abstract class AuthorizableAssessmentShim extends BaseAuthorizableObject {

	private Taxon taxon;
	
	public AuthorizableAssessmentShim(Taxon taxon) {
		this.taxon = taxon;
	}
	
	public Taxon getTaxon() {
		return taxon;
	}
	
}
