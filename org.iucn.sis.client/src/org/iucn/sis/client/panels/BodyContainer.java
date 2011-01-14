package org.iucn.sis.client.panels;

import org.iucn.sis.client.api.caches.TaxonomyCache;
import org.iucn.sis.client.tabs.TabManager;
import org.iucn.sis.shared.api.debug.Debug;

import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.TabPanelEvent;
import com.extjs.gxt.ui.client.widget.TabPanel;

public class BodyContainer extends TabPanel {

	public TabManager tabManager = null;

	public BodyContainer() {
		tabManager = new TabManager();

		setTabWidth(85);

		addListener(Events.Select, new Listener<TabPanelEvent>() {
			public void handleEvent(TabPanelEvent be) {
				try {
					refreshBody();
				} catch (Exception notBuiltYet) {
				}
			}
		});

		setResizeTabs(false);

		build();
	}

	public void build() {
		add(tabManager.homePage);
		add(tabManager.workingSetPage);
		add(tabManager.taxonHomePage);
		add(tabManager.assessmentEditor);
	}

	public TabManager getTabManager() {
		return tabManager;
	}

	public void refreshBody() {
		if (getSelectedItem().equals(tabManager.assessmentEditor))
			tabManager.panelManager.DEM.redraw();

		if (getSelectedItem().equals(tabManager.taxonHomePage)) {
			tabManager.panelManager.DEM.stopAutosaveTimer();
			if (TaxonomyCache.impl.getCurrentTaxon() != null)
				tabManager.panelManager.taxonomicSummaryPanel.update(TaxonomyCache.impl.getCurrentTaxon().getId());
		} else if (getSelectedItem().equals(tabManager.workingSetPage)) {
			tabManager.panelManager.DEM.stopAutosaveTimer();
			tabManager.workingSetPage.redraw();
		} else {
			try {
			tabManager.panelManager.DEM.layout();
			tabManager.panelManager.DEM.stopAutosaveTimer();
			tabManager.panelManager.workingSetPanel.refresh();
			tabManager.panelManager.recentAssessmentsPanel.refresh();
			} catch (Exception e) {
				Debug.println(e);
			}
		}
	}
}
