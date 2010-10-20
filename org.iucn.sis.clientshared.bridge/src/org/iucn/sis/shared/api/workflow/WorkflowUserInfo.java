package org.iucn.sis.shared.api.workflow;

public class WorkflowUserInfo {
	
	private final String username, displayName, email;
	private final Integer id;
	
	public WorkflowUserInfo(Integer id, String username, String displayName, String email) {
		this.id = id;
		this.username = username;
		this.displayName = displayName;
		this.email = email;
	}
	
	public String getEmail() {
		return email;
	}
	
	public String getEmailForMailer() {
		return displayName + " <" + email + ">";
	}
	
	public Integer getID() {
		return id;
	}
	
	public String getUsername() {
		return username;
	}
	
	public String getDisplayName() {
		return displayName;
	}
	
	public String toString() {
		return username + " (" + id + "): " + getEmailForMailer();
	}

}
