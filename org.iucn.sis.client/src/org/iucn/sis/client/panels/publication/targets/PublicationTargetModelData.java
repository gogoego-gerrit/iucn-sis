package org.iucn.sis.client.panels.publication.targets;

import org.iucn.sis.client.api.utils.FormattedDate;
import org.iucn.sis.shared.api.models.PublicationTarget;

import com.extjs.gxt.ui.client.data.BaseModelData;

public class PublicationTargetModelData extends BaseModelData {
	
	private static final long serialVersionUID = 1L;
	
	private PublicationTarget model;
	
	public PublicationTargetModelData(PublicationTarget model) {
		super();
		update(model);
	}
	
	public void update(PublicationTarget model) {
		this.model = model;
		
		set("id", model.getId());
		set("name", model.getName());
		set("date", FormattedDate.SHORT.getDate(model.getDate()));
		if (model.getReference() != null)
			set("reference", model.getReference().getCitation());
		else
			remove("reference");
	}
	
	public PublicationTarget getModel() {
		return model;
	}
	
}