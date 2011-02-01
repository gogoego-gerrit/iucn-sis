package org.iucn.sis.client.api.container;

import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.Taxon;
import org.iucn.sis.shared.api.models.WorkingSet;

public class StateChangeEvent {
	
	private final WorkingSet workingSet;
	private final Taxon taxon;
	private final Assessment assessment;
	private final Object source;
	
	private final String token;
	
	private String url;
	private boolean canceled;
	
	public StateChangeEvent(WorkingSet workingSet, Taxon taxon, Assessment assessment, Object source) {
		this(workingSet, taxon, assessment, source, null);
	}
	
	public StateChangeEvent(WorkingSet workingSet, Taxon taxon, Assessment assessment, Object source, String url) {
		this.workingSet = workingSet;
		this.taxon = taxon;
		this.assessment = assessment;
		this.source = source;
		this.url = url;
		
		this.canceled = false;
		
		StringBuilder builder = new StringBuilder();
		if (workingSet != null) {
			builder.append('W');
			builder.append(workingSet.getId());
		}
		if (taxon != null) {
			builder.append('T');
			builder.append(taxon.getId());
			if (assessment != null) {
				builder.append('A');
				builder.append(assessment.getId());
			}
		}
		this.token = builder.toString();
	}
	
	public Object getSource() {
		return source;
	}
	
	public Assessment getAssessment() {
		return assessment;
	}
	
	public Taxon getTaxon() {
		return taxon;
	}
	
	public WorkingSet getWorkingSet() {
		return workingSet;
	}
	
	public String getToken() {
		return token;
	}
	
	public void setCanceled(boolean canceled) {
		this.canceled = canceled;
	}

	public boolean isCanceled() {
		return canceled;
	}
	
	public void setUrl(String url) {
		this.url = url;
	}
	
	public String getUrl() {
		return url;
	}
}
