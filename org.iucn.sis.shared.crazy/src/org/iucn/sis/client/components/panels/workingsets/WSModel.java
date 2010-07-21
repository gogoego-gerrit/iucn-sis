package org.iucn.sis.client.components.panels.workingsets;

import org.iucn.sis.shared.data.WorkingSetData;

import com.extjs.gxt.ui.client.data.BaseModelData;

public class WSModel extends BaseModelData {

	protected final WorkingSetData ws;

	public WSModel(WorkingSetData ws) {
		set("name", ws.getWorkingSetName());
		set("date", ws.getDate());
		set("creator", ws.getCreator());
		set("id", ws.getId());
		set("workflow_status", ws.getWorkflowStatus().toString());
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

	public String getID() {
		return get("id");
	}

	public String getName() {
		return get("name");
	}

	public WorkingSetData getWorkingSetData() {
		return ws;
	}
	
	public String toString() {
		return getName();
	}

}