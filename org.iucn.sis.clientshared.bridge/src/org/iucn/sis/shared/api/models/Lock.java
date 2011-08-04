package org.iucn.sis.shared.api.models;

import java.util.Date;

public class Lock {
	
	private int id;
	private int lockid;
	private String type;
	private Date date;
	private User user;
	private String group;
	
	public Lock() {
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}
	
	public int getLockID() {
		return lockid;
	}

	public int getLockid() {
		return lockid;
	}
	
	public void setLockID(int lockID) {
		setLockid(lockID);
	}
	
	public void setLockid(int lockid) {
		this.lockid = lockid;
	}
	
	public String getGroup() {
		return group;
	}
	
	public void setGroup(String group) {
		this.group = group;
	}

}
