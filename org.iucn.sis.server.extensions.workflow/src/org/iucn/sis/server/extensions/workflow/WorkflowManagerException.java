package org.iucn.sis.server.extensions.workflow;

import org.restlet.data.Status;
import org.restlet.resource.ResourceException;

public class WorkflowManagerException extends ResourceException {
	
	private static final long serialVersionUID = 1L;
	
	public WorkflowManagerException(Throwable e) {
		super(Status.SERVER_ERROR_INTERNAL, e);
	}

	public WorkflowManagerException(String description) {
		super(Status.SERVER_ERROR_INTERNAL, description);
	}
	
	public WorkflowManagerException(String description, Throwable cause) {
		super(Status.SERVER_ERROR_INTERNAL, description, cause);
	}

	public WorkflowManagerException(String description, Status status) {
		super(status, description);
	}

	public WorkflowManagerException(String description, Status status, Throwable cause) {
		super(status, description, cause);
	}

}
