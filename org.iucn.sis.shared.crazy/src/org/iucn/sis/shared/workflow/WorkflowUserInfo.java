package org.iucn.sis.shared.workflow;

public class WorkflowUserInfo {
	
	private final String name;
	private final String email;
	private final String id;
	
	public WorkflowUserInfo(String id, String name, String email) {
		this.id = id;
		this.name = name;
		this.email = email;
	}
	
	public String getEmail() {
		return email;
	}
	
	public String getEmailForMailer() {
		return name + " <" + email + ">";
	}
	
	public String getID() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public String toString() {
		return name;
	}

}
