package org.iucn.sis.client.panels.utils;

import org.iucn.sis.client.api.caches.TaxonomyCache;
import org.iucn.sis.client.panels.PanelManager;
import org.iucn.sis.shared.api.models.Taxon;

import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.WindowManager;
import com.solertium.lwxml.shared.GenericCallback;

public class BasicSearchPanel extends SearchPanel {
	
	public BasicSearchPanel(final PanelManager manager) {
		super();
		addBeforeSearchListener(new Listener<SearchEvent<String>>() {
			public void handleEvent(SearchEvent<String> be) {
				if (be.getValue().matches("^[0-9]+$")) {
					Taxon taxon = TaxonomyCache.impl.getTaxon(be.getValue());
					if (taxon != null)
						TaxonomyCache.impl.setCurrentTaxon(taxon);
					//manager.taxonomicSummaryPanel.update(Integer.valueOf(be.getValue()));
					WindowManager.get().hideAll();
				}
			}
		});
		addSearchSelectionListener(new Listener<SearchEvent<Integer>>() {
			public void handleEvent(final SearchEvent<Integer> be) {
				TaxonomyCache.impl.fetchTaxon(be.getValue(), true, new GenericCallback<Taxon >() {
					public void onFailure(Throwable caught) {
					}
					public void onSuccess(Taxon result) {
						TaxonomyCache.impl.setCurrentTaxon(result);
						//manager.taxonomicSummaryPanel.update(be.getValue());
						WindowManager.get().hideAll();
					}
				});
			}
		});
	}

}
