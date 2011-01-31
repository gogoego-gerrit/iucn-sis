package org.iucn.sis.client.api.container;

import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.Taxon;
import org.iucn.sis.shared.api.models.WorkingSet;

public class StateChangeEvent {
	
	private final WorkingSet workingSet;
	private final Taxon taxon;
	private final Assessment assessment;
	private final Object source;
	
	private boolean canceled;
	
	public StateChangeEvent(WorkingSet workingSet, Taxon taxon, Assessment assessment, Object source) {
		this.workingSet = workingSet;
		this.taxon = taxon;
		this.assessment = assessment;
		this.source = source;
		
		this.canceled = false;
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
	
	public void setCanceled(boolean canceled) {
		this.canceled = canceled;
	}

	public boolean isCanceled() {
		return canceled;
	}
}
