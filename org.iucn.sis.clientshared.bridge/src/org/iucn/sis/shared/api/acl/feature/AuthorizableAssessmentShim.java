package org.iucn.sis.shared.api.acl.feature;

import org.iucn.sis.shared.api.models.Taxon;


public abstract class AuthorizableAssessmentShim extends BaseAuthorizableObject {

	private final Taxon taxon;
	private final String schema;
	
	public AuthorizableAssessmentShim(Taxon taxon, String defaultSchema) {
		this.taxon = taxon;
		this.schema = defaultSchema;
	}
	
	public Taxon getTaxon() {
		return taxon;
	}
	
	@Override
	public String getProperty(String key) {
		if ("schema".equals(key))
			return schema;
		else
			return super.getProperty(key);
	}
	
}
