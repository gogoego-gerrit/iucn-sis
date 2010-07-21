package org.iucn.sis.client.components;

import org.iucn.sis.client.components.tabs.TabManager;
import org.iucn.sis.shared.data.TaxonomyCache;

import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.TabPanelEvent;
import com.extjs.gxt.ui.client.widget.TabPanel;

public class BodyContainer extends TabPanel {
	private static final int tabWidth = 85;

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
			if (TaxonomyCache.impl.getCurrentNode() != null)
				tabManager.panelManager.taxonomicSummaryPanel.update("" + TaxonomyCache.impl.getCurrentNode().getId());
		} else if (getSelectedItem().equals(tabManager.workingSetPage)) {
			tabManager.panelManager.DEM.stopAutosaveTimer();
			tabManager.workingSetPage.redraw();
		} else {
			tabManager.panelManager.DEM.stopAutosaveTimer();
			tabManager.panelManager.workingSetPanel.refresh();
			tabManager.panelManager.recentAssessmentsPanel.refresh();
			tabManager.panelManager.assessmentReviewPanel.refresh();
		}
	}
}
