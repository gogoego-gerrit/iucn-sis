package org.iucn.sis.client.api.ui.models.taxa;

import org.iucn.sis.client.api.utils.TaxonComparator;
import org.iucn.sis.shared.api.models.Taxon;

import com.extjs.gxt.ui.client.data.BaseModel;

public class TaxonListElement extends BaseModel {
	Taxon node;
	String footprint;

	public TaxonListElement() {
		super();
	}

	public TaxonListElement(String name) {
		set("name", name);
		set("fullName", name);
	}

	public TaxonListElement(Taxon node, String footprint) {
		this.node = node;
		this.footprint = footprint;
		set("name", node.getName() + (node.getTaxonStatus().getName().matches("(^[Nn]$)|(^New$)") ? "*" : ""));
		set("fullName", node.getFriendlyName());
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

}
