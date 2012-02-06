package org.iucn.sis.client.panels.utils;

import org.iucn.sis.client.api.caches.TaxonomyCache;
import org.iucn.sis.client.api.container.StateManager;
import org.iucn.sis.shared.api.models.TaxonHierarchy;

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
					WindowUtils.showLoadingAlert("Loading...");
					TaxonomyCache.impl.fetchPathWithID(Integer.valueOf(be.getValue()), new GenericCallback<TaxonHierarchy>() {
						public void onFailure(Throwable caught) {
							WindowUtils.errorAlert("Failed to load taxon " + be.getValue() + ".");
						}
						public void onSuccess(TaxonHierarchy result) {
							if (result != null) {
								StateManager.impl.setTaxon(result.getTaxon());
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
				WindowUtils.showLoadingAlert("Loading...");
				TaxonomyCache.impl.fetchPathWithID(be.getValue(), new GenericCallback<TaxonHierarchy>() {
					public void onFailure(Throwable caught) {
					}
					public void onSuccess(TaxonHierarchy result) {
						//TaxonomyCache.impl.setCurrentTaxon(result);
						StateManager.impl.setTaxon(result.getTaxon());
						//manager.taxonomicSummaryPanel.update(be.getValue());
						WindowManager.get().hideAll();
					}
				});
			}
		});
	}

}
