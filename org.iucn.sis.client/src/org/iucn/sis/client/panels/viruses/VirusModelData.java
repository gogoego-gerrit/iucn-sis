package org.iucn.sis.client.panels.viruses;

import org.iucn.sis.client.api.utils.FormattedDate;
import org.iucn.sis.shared.api.models.Virus;

import com.extjs.gxt.ui.client.data.BaseModelData;

public class VirusModelData extends BaseModelData {
	
	private static final long serialVersionUID = 1L;
	
	private final Virus virus;
	
	public VirusModelData(Virus virus) {
		this.virus = virus;
		
		//For listboxes
		set("text", virus.getName());
		set("value", virus.getId()+"");
		
		//For grid
		set("name", virus.getName());
		set("comments", virus.getComments());
		set("added", FormattedDate.impl.getDate(virus.getAdded()));
		set("user", virus.getUser().getDisplayableName());
	}
	
	public Virus getVirus() {
		return virus;
	}

}
