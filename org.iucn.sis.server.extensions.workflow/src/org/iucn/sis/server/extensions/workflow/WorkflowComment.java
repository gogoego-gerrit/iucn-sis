package org.iucn.sis.server.extensions.workflow;

import java.util.Calendar;
import java.util.Date;

import org.iucn.sis.shared.api.workflow.WorkflowUserInfo;

public class WorkflowComment {
	
	private final String comment;
	private final String scope;
	private final WorkflowUserInfo user;
	private final Date date;
	
	public WorkflowComment(WorkflowUserInfo user, String comment) {
		this(user, comment, WorkflowConstants.IS_GLOBAL_STATUS);
	}
	
	public WorkflowComment(WorkflowUserInfo user, String comment, String scope) {
		this.user = user;
		this.scope = scope;
		this.comment = comment;
		this.date = Calendar.getInstance().getTime();
	}
	
	public String getComment() {
		return comment;
	}
	
	public Date getDate() {
		return date;
	}
	
	public String getScope() {
		return scope;
	}
	
	public WorkflowUserInfo getUser() {
		return user;
	}

}
