package org.iucn.sis.shared.workflow;

public enum WorkflowStatus {
	
	DRAFT("draft", "draft review", null, "review"), 
	REVIEW("review", "review", "draft", "consistency-check"), 
	CONSISTENCY_CHECK("consistency-check", "consistency checking", "review", "final"),
	FINAL("final", "final publication", "consistency-check", "publish"), 
	PUBLISH("publish", "publish review", "final", null);
	
	public static WorkflowStatus getStatus(String desc) {
		for (WorkflowStatus status : WorkflowStatus.values())
			if (status.matches(desc))
				return status;
		return null;
	}
	
	private final String desc, emailFriendlyDesc;
	private final String previousStr, nextStr;
	
	private WorkflowStatus previous = null, next = null;
	
	private WorkflowStatus(String desc, String emailFriendlyDesc, String previous, String next) {
		this.desc = desc;
		this.emailFriendlyDesc = emailFriendlyDesc;
		this.previousStr = previous;
		this.nextStr = next;
	}
	
	public WorkflowStatus getPreviousStatus() {
		if (previous == null)
			previous = getStatus(previousStr);
		return previous;
	}
	
	public WorkflowStatus getNextStatus() {
		if (next == null)
			next = getStatus(nextStr);
		return next;
	}
	
	public String getEmailFriendlyDesc() {
		return emailFriendlyDesc;
	}
	
	public boolean matches(String desc) {
		return this.desc.equals(desc);
	}
	
	public String toString() {
		return desc;
	}

}
