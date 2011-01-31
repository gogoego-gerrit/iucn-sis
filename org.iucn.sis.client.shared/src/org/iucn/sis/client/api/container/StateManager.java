package org.iucn.sis.client.api.container;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.iucn.sis.shared.api.debug.Debug;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.Taxon;
import org.iucn.sis.shared.api.models.WorkingSet;

import com.solertium.util.events.ComplexListener;
import com.solertium.util.events.CoreObservable;

public class StateManager implements CoreObservable<ComplexListener<StateChangeEvent>> {
	
	public enum StateChangeEventType {
		WorkingSetChanged(0), TaxonChanged(1), AssessmentChanged(2), 
		BeforeWorkingSetChanged(3), BeforeTaxonChanged(4), BeforeAssessmentChanged(5);
		
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
	
	private WorkingSet workingSet;
	private Taxon taxon;
	private Assessment assessment;
	
	private StateManager() {
		listeners = new HashMap<Integer, List<ComplexListener<StateChangeEvent>>>();
	}
	
	public void addStateChangeListener(StateChangeEventType event, ComplexListener<StateChangeEvent> listener) {
		addListener(event.getValue(), listener);
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
	
	public void setAssessment(Assessment assessment) {
		if (!fireEvent(StateChangeEventType.BeforeAssessmentChanged.getValue(), new StateChangeEvent(workingSet, taxon, assessment))) {
			this.assessment = assessment;
			fireEvent(StateChangeEventType.AssessmentChanged.getValue());
		}
	}
	
	public void setTaxon(Taxon taxon) {
		if (!fireEvent(StateChangeEventType.BeforeTaxonChanged.getValue(), new StateChangeEvent(workingSet, taxon, assessment))) {
			this.taxon = taxon;
			this.assessment = null;
			fireEvent(StateChangeEventType.TaxonChanged.getValue());
		}
	}
	
	public void setWorkingSet(WorkingSet workingSet) {
		if (!fireEvent(StateChangeEventType.BeforeWorkingSetChanged.getValue(), new StateChangeEvent(workingSet, taxon, assessment))) {
			this.workingSet = workingSet;
			this.taxon = null;
			this.assessment = null;
			fireEvent(StateChangeEventType.WorkingSetChanged.getValue());
		}
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
		return fireEvent(eventType, new StateChangeEvent(getWorkingSet(), getTaxon(), getAssessment()));
	}
	
	/**
	 * Fires an event.
	 * @param eventType
	 * @param event
	 * @return true if the event got canceled, false otherwise
	 */
	public boolean fireEvent(int eventType, StateChangeEvent event) {
		Integer key = new Integer(eventType);
		List<ComplexListener<StateChangeEvent>> group = listeners.get(key);
		if (group == null)
			return false;
		
		boolean retValue = true;
		for (ComplexListener<StateChangeEvent> listener : group) {
			listener.handleEvent(event);
			retValue &= event.isCanceled();
		}
		
		Debug.println("{0} listeners fired for event {1}, canceled = {2}", group.size(), eventType, retValue);
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
