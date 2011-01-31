package org.iucn.sis.client.api.container;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.iucn.sis.client.api.caches.AssessmentCache;
import org.iucn.sis.client.api.caches.TaxonomyCache;
import org.iucn.sis.client.api.caches.WorkingSetCache;
import org.iucn.sis.shared.api.debug.Debug;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.Taxon;
import org.iucn.sis.shared.api.models.WorkingSet;

import com.solertium.util.events.ComplexListener;
import com.solertium.util.events.CoreObservable;

public class StateManager implements CoreObservable<ComplexListener<StateChangeEvent>> {
	
	public enum StateChangeEventType {
		BeforeStateChanged(0), StateChanged(1);
		
		private final int value;
		
		private StateChangeEventType(int value) {
			this.value = value;
		}
		
		public int getValue() {
			return value;
		}
	}
	
	public static final StateManager impl = new StateManager();
	
	private final Map<Integer, List<ComplexListener<StateChangeEvent>>> listeners;
	
	private Integer workingSet;
	private Integer taxon;
	private Integer assessment;
	
	private StateManager() {
		listeners = new HashMap<Integer, List<ComplexListener<StateChangeEvent>>>();
	}
	
	public void addStateChangeListener(StateChangeEventType event, ComplexListener<StateChangeEvent> listener) {
		addListener(event.getValue(), listener);
	}
	
	public Assessment getAssessment() {
		return assessment == null ? null : AssessmentCache.impl.getAssessment(assessment);
	}
	
	public Taxon getTaxon() {
		return taxon == null ? null : TaxonomyCache.impl.getTaxon(taxon);
	}
	
	public WorkingSet getWorkingSet() {
		return workingSet == null ? null : WorkingSetCache.impl.getWorkingSet(workingSet);
	}

	public void reset() {
		setState(null, null, null);
	}
	
	public void setTaxon(Taxon taxon) {
		setState(getWorkingSet(), taxon, null);
	}
	
	public void setWorkingSet(WorkingSet workingSet) {
		setState(workingSet, null, null);
	}
	
	public void setState(Assessment assessment) {
		setState(getWorkingSet(), getTaxon(), assessment);
	}
	
	public void setState(Taxon taxon, Assessment assessment) {
		setState(getWorkingSet(), taxon, assessment);
	}
	
	public void setState(WorkingSet workingSet, Taxon taxon, Assessment assessment) {
		setState(workingSet, taxon, assessment, this);
	}
	
	public void setState(WorkingSet workingSet, Taxon taxon, Assessment assessment, Object source) {
		if (hasChanges(getWorkingSet(), workingSet) || 
				hasChanges(getTaxon(), taxon) || 
				hasChanges(getAssessment(), assessment)) {
	
			if (!fireEvent(StateChangeEventType.BeforeStateChanged.getValue(), new StateChangeEvent(workingSet, taxon, assessment, this))) {
				this.workingSet = workingSet == null ? null : workingSet.getId();
				this.taxon = taxon == null ? null : taxon.getId();
				this.assessment = assessment == null ? null : assessment.getId();
				
				if (taxon != null)
					TaxonomyCache.impl.updateRecentTaxa();
				if (assessment != null)
					AssessmentCache.impl.updateRecentAssessments();
				
				fireEvent(StateChangeEventType.StateChanged.getValue(), source);
			}
		}
	}
	
	/*public void setAssessment(Assessment assessment) {
		setAssessment(taxon, assessment);
	}
	
	public void setAssessment(Taxon taxon, Assessment assessment) {
		if (!hasChanges(this.assessment, assessment))
			return;
		
		if (!fireEvent(StateChangeEventType.BeforeAssessmentChanged.getValue(), new StateChangeEvent(workingSet, taxon, assessment))) {
			this.taxon = taxon;
			this.assessment = assessment;
			fireEvent(StateChangeEventType.AssessmentChanged.getValue());
		}
	}
	
	public void setTaxon(Taxon taxon) {
		if (!hasChanges(this.taxon, taxon))
			return;
		
		if (!fireEvent(StateChangeEventType.BeforeTaxonChanged.getValue(), new StateChangeEvent(workingSet, taxon, assessment))) {
			this.taxon = taxon;
			this.assessment = null;
			fireEvent(StateChangeEventType.TaxonChanged.getValue());
		}
	}
	
	public void setWorkingSet(WorkingSet workingSet) {
		if (!hasChanges(this.workingSet, workingSet))
			return;
		
		if (!fireEvent(StateChangeEventType.BeforeWorkingSetChanged.getValue(), new StateChangeEvent(workingSet, taxon, assessment))) {
			this.workingSet = workingSet;
			this.taxon = null;
			this.assessment = null;
			fireEvent(StateChangeEventType.WorkingSetChanged.getValue());
		}
	}*/
	
	private boolean hasChanges(Object currentValue, Object newValue) {
		if (currentValue == null)
			return newValue != null;
		else
			return !currentValue.equals(newValue);
	}
	
	public void doLogout() {
		listeners.clear();
		workingSet = null;
		taxon = null;
		assessment = null;
	}
	
	public void addListener(int eventType, ComplexListener<StateChangeEvent> listener) {
		Integer key = new Integer(eventType);
		List<ComplexListener<StateChangeEvent>> group = listeners.get(key);
		if (group == null)
			group = new ArrayList<ComplexListener<StateChangeEvent>>();
		group.add(listener);
		listeners.put(key, group);
	}

	public boolean fireEvent(int eventType) {
		return fireEvent(eventType, this);
	}
	
	public boolean fireEvent(int eventType, Object source) {
		return fireEvent(eventType, new StateChangeEvent(getWorkingSet(), getTaxon(), getAssessment(), source));
	}
	
	/**
	 * Fires an event.
	 * @param eventType
	 * @param event
	 * @return true if the event got canceled, false otherwise
	 */
	public boolean fireEvent(int eventType, StateChangeEvent event) {
		Debug.println("Firing event {0}", eventType);
		Integer key = new Integer(eventType);
		List<ComplexListener<StateChangeEvent>> group = listeners.get(key);
		
		boolean retValue = false;
		if (group != null) {
			for (ComplexListener<StateChangeEvent> listener : group) {
				listener.handleEvent(event);
				retValue |= event.isCanceled();
			}
		}
		
		Debug.println("{0} listeners fired for event {1}, canceled = {2}", group == null ? 0 : group.size(), eventType, retValue);
		return retValue;
	}

	public void removeAllListeners() {
		listeners.clear();
	}

	public void removeListener(int eventType, ComplexListener<StateChangeEvent> listener) {
		Integer key = new Integer(eventType);
		List<ComplexListener<StateChangeEvent>> group = listeners.get(key);
		if (group != null)
			group.remove(listener);
	}
	
}
