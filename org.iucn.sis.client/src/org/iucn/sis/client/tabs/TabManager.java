package org.iucn.sis.client.tabs;

import org.iucn.sis.client.api.caches.AssessmentCache;
import org.iucn.sis.client.api.caches.TaxonomyCache;
import org.iucn.sis.client.panels.PanelManager;

public class TabManager {
	public AssessmentEditorTab assessmentEditor = null;
	public HomePageTab homePage = null;
	public TaxonHomePageTab taxonHomePage = null;
	public TaxomaticTab taxomaticPage = null;
	public WorkingSetTab workingSetPage = null;

	public PanelManager panelManager = null;

	public TabManager() {
		panelManager = new PanelManager();

		assessmentEditor = new AssessmentEditorTab(panelManager);
		homePage = new HomePageTab(panelManager);
		taxonHomePage = new TaxonHomePageTab(panelManager);
		taxomaticPage = new TaxomaticTab(panelManager);
		workingSetPage = new WorkingSetTab(panelManager);
		update();
	}

	public PanelManager getPanelManager() {
		return panelManager;
	}

	public void update() {
		if (TaxonomyCache.impl.getCurrentTaxon() != null) {
			taxonHomePage.setEnabled(true);
		} else {
			taxonHomePage.setEnabled(false);
		}

		if (AssessmentCache.impl.getCurrentAssessment() != null) {
			// taxonHomePage.setEnabled(true);
			assessmentEditor.setEnabled(true);
		} else {
			// taxonHomePage.setEnabled(false);
			assessmentEditor.setEnabled(false);
		}
	}
}
