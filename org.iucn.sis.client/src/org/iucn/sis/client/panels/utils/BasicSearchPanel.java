package org.iucn.sis.client.panels.utils;

import org.iucn.sis.client.api.caches.TaxonomyCache;
import org.iucn.sis.client.api.container.StateManager;
import org.iucn.sis.shared.api.models.Taxon;

import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.WindowManager;
import com.solertium.lwxml.shared.GWTNotFoundException;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.util.extjs.client.WindowUtils;

public class BasicSearchPanel extends SearchPanel {
	
	public BasicSearchPanel() {
		super();
		addBeforeSearchListener(new Listener<SearchEvent<String>>() {
			public void handleEvent(final SearchEvent<String> be) {
				if (be.getValue().matches("^[0-9]+$")) {
					TaxonomyCache.impl.fetchTaxon(Integer.valueOf(be.getValue()), true, new GenericCallback<Taxon>() {
						public void onFailure(Throwable caught) {
							WindowUtils.errorAlert("Failed to load taxon " + be.getValue() + ".");
						}
						public void onSuccess(Taxon result) {
							if (result != null) {
								StateManager.impl.setTaxon(result);
								WindowManager.get().hideAll();
							}
							else
								onFailure(new GWTNotFoundException());
						}
					});
				}
			}
		});
		addSearchSelectionListener(new Listener<SearchEvent<Integer>>() {
			public void handleEvent(final SearchEvent<Integer> be) {
				TaxonomyCache.impl.fetchTaxon(be.getValue(), true, new GenericCallback<Taxon >() {
					public void onFailure(Throwable caught) {
					}
					public void onSuccess(Taxon result) {
						//TaxonomyCache.impl.setCurrentTaxon(result);
						StateManager.impl.setTaxon(result);
						//manager.taxonomicSummaryPanel.update(be.getValue());
						WindowManager.get().hideAll();
					}
				});
			}
		});
	}

}
