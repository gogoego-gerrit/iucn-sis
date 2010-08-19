package org.iucn.sis.shared.api.models;

import java.util.HashSet;
import java.util.Set;

import org.iucn.sis.shared.api.models.WorkingSet;

public class WorkflowStatus {
	
	protected int id;
	protected WorkingSet workingset;
	protected String status;
	protected Set<WorkflowNote> workflowNotes = new HashSet<WorkflowNote>();
	
	public WorkflowStatus() {
		
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public WorkingSet getWorkingset() {
		return workingset;
	}
	public void setWorkingset(WorkingSet workingset) {
		this.workingset = workingset;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public Set<WorkflowNote> getWorkflowNotes() {
		return workflowNotes;
	}
	public void setWorkflowNotes(Set<WorkflowNote> workflowNotes) {
		this.workflowNotes = workflowNotes;
	}
	
}