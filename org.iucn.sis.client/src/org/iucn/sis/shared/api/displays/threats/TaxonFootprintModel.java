package org.iucn.sis.shared.api.displays.threats;

import org.iucn.sis.shared.api.models.Taxon;

import com.extjs.gxt.ui.client.data.BaseModelData;

public class TaxonFootprintModel extends BaseModelData {
	private static final long serialVersionUID = 1L;
	
	private final Taxon taxon;
	
	public TaxonFootprintModel(Taxon taxon) {
		super();
		this.taxon = taxon;
		
		String[] data = taxon.getFootprint();
		for (int i = 0; i < data.length; i++)
			set(SelectExistingIASTaxaPanel.footprint[i], data[i]);
	}
	
	public Taxon getTaxon() {
		return taxon;
	}
	
}
