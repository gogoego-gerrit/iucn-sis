package org.iucn.sis.shared.api.acl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

public class UserAction implements Serializable {

	private static final long serialVersionUID = -877160852599538431L;

	private String id;

	private String primaryVersion;
	private ArrayList newVersions;
	private ArrayList newVersionDates;

	private boolean committed;
	private Date dateCommitted;
	private boolean rolledBack;
	private Date dateFetched;

	public UserAction() {
		primaryVersion = null;
		committed = false;
		rolledBack = false;

		newVersions = new ArrayList();
		newVersionDates = new ArrayList();
	}

	public boolean addNewVersion(String newVersionAsXML) {
		if (newVersionDates.add(new Date()))
			if (newVersions.add(newVersionAsXML))
				return true;
			else
				newVersionDates.remove(newVersionDates.size() - 1);

		return false;
	}

	public Date getDateCommitted() {
		return dateCommitted;
	}

	public Date getDateFetched() {
		return dateFetched;
	}

	public String getId() {
		return id;
	}

	public ArrayList getNewVersionDates() {
		return newVersionDates;
	}

	public ArrayList getNewVersions() {
		return newVersions;
	}

	public Object getPrimaryVersion() {
		return primaryVersion;
	}

	public boolean isCommitted() {
		return committed;
	}

	public boolean isRolledBack() {
		return rolledBack;
	}

	public void setCommitted(boolean committed) {
		if (committed)
			dateCommitted = new Date();

		this.committed = committed;
	}

	public void setPrimaryVersion(String id, String oldVersion) {
		dateFetched = new Date();
		this.id = id;
		this.primaryVersion = oldVersion;
	}

	public void setRolledBack(boolean rolledBack) {
		this.rolledBack = rolledBack;
	}

}
