package org.iucn.sis.client.api.ui.models.taxa;

import org.iucn.sis.client.api.utils.TaxonComparator;
import org.iucn.sis.shared.api.models.Taxon;
import org.iucn.sis.shared.api.models.TaxonStatus;

import com.extjs.gxt.ui.client.data.BaseModel;

public class TaxonListElement extends BaseModel {
	private static final long serialVersionUID = 1L;
	
	private final Taxon node;
	private final String footprint;

	public TaxonListElement(Taxon node, String footprint) {
		this.node = node;
		this.footprint = footprint;
		
		boolean isNew = TaxonStatus.STATUS_NEW.equals(node.getTaxonStatus().getCode());
		
		set("name", node.getName() + (isNew ? "*" : ""));
		set("fullName", node.getFriendlyName());
		set("status", node.getStatusCode());
		set(TaxonComparator.SEQ_CODE, String.valueOf(node.getSequenceCode()));
	}

	public String getFootprint() {
		return footprint;
	}

	public Taxon getNode() {
		return node;
	}

	public void setSequenceCode(String code) {
		set(TaxonComparator.SEQ_CODE, code);
	}
	
	public String getStyleName() {
		return "taxon_status_" + get("status");
	}
	
	public String toHtml() {
		return toHtml((String)get("fullName"));
	}
	
	public String toHtml(String name) {
		return "<span class=\"" + getStyleName() + "\">" + name + "</span>";
	}

}
