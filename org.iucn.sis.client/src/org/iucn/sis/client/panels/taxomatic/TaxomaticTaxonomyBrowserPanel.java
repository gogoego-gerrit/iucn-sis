package org.iucn.sis.client.panels.taxomatic;

import org.iucn.sis.client.api.caches.TaxonomyCache;
import org.iucn.sis.client.panels.utils.TaxonomyBrowserPanel;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.solertium.util.extjs.client.QuickButton;

public class TaxomaticTaxonomyBrowserPanel extends TaxonomyBrowserPanel {
	
	private final TaxonChooser chooser;
	
	public TaxomaticTaxonomyBrowserPanel(TaxonChooser chooser) {
		this.chooser = chooser;
	}
	
	protected void addViewButtonToFootprint() {
		footprintPanel.add(new QuickButton("Add", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				String display = footprints[footprints.length - 1];
				chooser.addItem(footprints, TaxonomyCache.impl.getTaxon(display));
			}
		}));
	}

}
