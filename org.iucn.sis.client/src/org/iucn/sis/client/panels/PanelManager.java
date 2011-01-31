package org.iucn.sis.client.panels;

import org.iucn.sis.client.panels.assessments.RecentAssessmentsPanel;
import org.iucn.sis.client.panels.criteracalculator.ExpertPanel;
import org.iucn.sis.client.panels.dem.DEMPanel;
import org.iucn.sis.client.panels.images.ImagePopupPanel;
import org.iucn.sis.client.panels.references.ReferenceViewTabPanel;
import org.iucn.sis.client.panels.taxa.TaxonHomePage;
import org.iucn.sis.client.panels.utils.BasicSearchPanel;
import org.iucn.sis.client.panels.utils.SearchPanel;
import org.iucn.sis.client.panels.utils.TaxonomyBrowserPanel;
import org.iucn.sis.client.panels.workingsets.WorkingSetAddTaxaSearchPanel;
import org.iucn.sis.client.panels.workingsets.WorkingSetBrowser;
import org.iucn.sis.client.panels.workingsets.WorkingSetFullPanel;
import org.iucn.sis.client.panels.workingsets.WorkingSetHierarchy;
import org.iucn.sis.client.panels.workingsets.WorkingSetOptionsPanel;
import org.iucn.sis.client.panels.zendesk.BugPanel;
import org.iucn.sis.client.tabs.HomePageTab;
import org.iucn.sis.client.tabs.home.WorkingSetPortlet;

import com.extjs.gxt.ui.client.fx.Draggable;
import com.extjs.gxt.ui.client.fx.Resizable;
import com.extjs.gxt.ui.client.widget.ComponentHelper;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.LayoutData;

public class PanelManager {
	public RecentAssessmentsPanel recentAssessmentsPanel = null;
	//public TaxonHomePage taxonomicSummaryPanel = null;
	//public TaxonomyBrowserPanel taxonomyBrowserPanel = null;
	// public AssessmentSummaryPanel assessmentSummaryPanel = null;
	public WorkingSetPortlet workingSetPanel = null;
	public ExpertPanel expertPanel = null;
	public ImagePopupPanel imageViewerPanel = null;
	public DEMPanel DEM = null;
	public SearchPanel taxonomySearchPanel = null;
	public WorkingSetFullPanel workingSetFullPanel = null;
	public WorkingSetHierarchy workingSetHierarchy = null;
	public WorkingSetAddTaxaSearchPanel addTaxonPanel = null;
	public WorkingSetOptionsPanel workingSetOptionsPanel = null;
	public WorkingSetBrowser workingSetBrowser = null;
	public BugPanel bugPanel = null;
	public BugPanel resolvedBugPanel = null;

	public ReferenceViewTabPanel refViewPanel = null;

	public PanelManager() {
		//taxonomicSummaryPanel = new TaxonHomePage();
		//taxonomyBrowserPanel = new TaxonomyBrowserPanel();
		// assessmentSummaryPanel = new AssessmentSummaryPanel( this );
		
		DEM = new DEMPanel();
		expertPanel = new ExpertPanel();
		imageViewerPanel = new ImagePopupPanel(this);
		taxonomySearchPanel = new BasicSearchPanel();
		
		workingSetFullPanel = new WorkingSetFullPanel(this);
		workingSetHierarchy = new WorkingSetHierarchy(this);
		addTaxonPanel = new WorkingSetAddTaxaSearchPanel();
		workingSetOptionsPanel = new WorkingSetOptionsPanel();
		workingSetBrowser = new WorkingSetBrowser(this);
		
		
		try {
			refViewPanel = new ReferenceViewTabPanel();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void addPanel(LayoutContainer container, LayoutContainer panel, LayoutData layoutData, boolean resizable,
			boolean draggable) {
		if (resizable) {
			new Resizable(panel);
		}

		if (draggable) {
			Draggable d = new Draggable(panel);
			d.setContainer(container);
			d.setUseProxy(false);
		}

		if (layoutData != null) {
			container.add(panel);
			ComponentHelper.setLayoutData(panel, layoutData);
		} else
			container.add(panel);
	}

	public void addPanelAsDraggable(LayoutContainer container, LayoutContainer panel) {
		Draggable d = new Draggable(panel);
		d.setContainer(container);
		d.setUseProxy(false);

		container.add(panel);
	}

	public void addPanelAsResizable(LayoutContainer container, LayoutContainer panel) {
		new Resizable(panel);

		container.add(panel);
	}

	public void layoutPanel(LayoutContainer panel) {
		panel.setWidth(HomePageTab.standardPanelWidth);
		panel.setHeight(HomePageTab.standardPanelHeight);
	}

}
