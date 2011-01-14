package org.iucn.sis.client.api.ui.models.assessment;

import org.iucn.sis.shared.api.models.AssessmentType;

import com.extjs.gxt.ui.client.data.BaseModelData;

public class AssessmentOverviewModelData extends BaseModelData {
	
	private static final long serialVersionUID = 1L;

	public AssessmentOverviewModelData() {

	}

	public AssessmentOverviewModelData(String name, String created, String modified,
			String status, String evaluated, String id) {

		if (AssessmentType.DRAFT_ASSESSMENT_TYPE.equalsIgnoreCase(name))
			set("name", "Draft Assessment");
		else if (AssessmentType.USER_ASSESSMENT_TYPE.equalsIgnoreCase(name))
			set("name", "User Assessment");
		else
			set("name", "Published Assessment");
		if (created == null || created.trim().equals(""))
			set("created", "N/A");
		else
			set("created", created);
		if (modified == null || modified.trim().equals(""))
			set("modified", "N/A");
		else
			set("modified", modified);
		if (status == null || status.trim().equals(""))
			set("status", "N/A");
		else
			set("status", status);
		if (evaluated == null || evaluated.trim().equals(""))
			set("evaluated", "N/A");
		else
			set("evaluated", evaluated);

		set("id", id);
	}

	public String getCreated() {
		return get("created");
	}

	public String getEvaluated() {
		return get("evaluated");
	}

	public String getModified() {
		return get("modified");
	}

	public String getStatus() {
		return get("status");
	}

}
