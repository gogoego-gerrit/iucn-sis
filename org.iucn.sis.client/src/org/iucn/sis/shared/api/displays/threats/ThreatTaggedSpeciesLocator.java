package org.iucn.sis.shared.api.displays.threats;

import org.iucn.sis.client.panels.utils.SearchPanel;
import org.iucn.sis.client.panels.utils.TaxonomyBrowserPanel;

import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.TabPanelEvent;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;

public class ThreatTaggedSpeciesLocator extends TabPanel {

	public ThreatTaggedSpeciesLocator() {
		super();
		
		add(buildBrowserPanel());
		add(buildSearchPanel());
	}
	
	private TabItem buildBrowserPanel() {
		final TaxonomyBrowserPanel panel = new TaxonomyBrowserPanel();
		final TabItem browser = new TabItem("Taxonomy Browser");
		browser.addListener(Events.Select, new Listener<TabPanelEvent>() {
			public void handleEvent(TabPanelEvent be) {
				panel.update();
			}
		});
		browser.add(panel);
		
		return browser;
	}
	
	private TabItem buildSearchPanel() {
		final SearchPanel panel = new SearchPanel();
		final TabItem item = new TabItem("Taxonomy Browser");
		item.addListener(Events.Select, new Listener<TabPanelEvent>() {
			public void handleEvent(TabPanelEvent be) {
				panel.resetSearchBox();
			}
		});
		item.add(panel);
		
		return item;
	}
	
}
