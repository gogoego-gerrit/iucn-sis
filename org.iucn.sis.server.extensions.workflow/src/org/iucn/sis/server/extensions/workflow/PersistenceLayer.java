package org.iucn.sis.server.extensions.workflow;

import org.iucn.sis.shared.api.models.WorkingSet;
import org.iucn.sis.shared.api.workflow.WorkflowStatus;

public interface PersistenceLayer {
	
	public void addComment(Number id, WorkflowComment comment) throws WorkflowManagerException;
	
	public Number insertStatus(Integer workingSetID, WorkflowStatus status) throws WorkflowManagerException;
	
	public void updateStatus(Number id, WorkflowStatus status) throws WorkflowManagerException;
	
	public void ensureEvaluated(WorkingSet workingSet) throws WorkflowManagerException;
	
	public void ensureConsistent(WorkingSet workingSet) throws WorkflowManagerException;
	
	public org.iucn.sis.shared.api.models.WorkflowStatus getWorkflowRow(Integer workingSetID) throws WorkflowManagerException;

}
