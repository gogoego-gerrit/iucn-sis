package org.iucn.sis.client.api.ui.models.workingset;

import org.iucn.sis.client.api.utils.FormattedDate;
import org.iucn.sis.shared.api.models.WorkingSet;

import com.extjs.gxt.ui.client.data.BaseModelData;

public class WSModel extends BaseModelData {
	
	private static final long serialVersionUID = 1L;

	protected final WorkingSet ws;

	public WSModel(WorkingSet ws) {
		set("name", ws.getName());
		set("date", FormattedDate.impl.getDate(ws.getCreatedDate()));
		set("creator", ws.getCreator().getDisplayableName());
		set("id", ws.getId());
		set("workflow_status", ws.getWorkflow());
		this.ws = ws;
	}

	// public WSModel(String name, String date, String creator, String id) {
	// set("name", name);
	// set("date", date);
	// set("creator", creator);
	// set("id", id);
	// }

	public String getCreator() {
		return get("creator");
	}

	public String getDate() {
		return get("date");
	}

	public Integer getID() {
		return get("id");
	}

	public String getName() {
		return get("name");
	}

	public WorkingSet getWorkingSet() {
		return ws;
	}
	
	public String toString() {
		return getName();
	}

}