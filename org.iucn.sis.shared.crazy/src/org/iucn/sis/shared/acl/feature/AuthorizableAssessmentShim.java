package org.iucn.sis.shared.acl.feature;

import org.iucn.sis.shared.taxonomyTree.TaxonNode;

public abstract class AuthorizableAssessmentShim extends BaseAuthorizableObject {

	private TaxonNode taxon;
	
	public AuthorizableAssessmentShim(TaxonNode taxon) {
		this.taxon = taxon;
	}
	
	public TaxonNode getTaxon() {
		return taxon;
	}
	
}
