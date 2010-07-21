package org.iucn.sis.client.components.panels;

import org.iucn.sis.client.components.panels.imagemanagement.ImagePopupPanel;
import org.iucn.sis.client.components.panels.workingsets.WorkingSetAddTaxaSearchPanel;
import org.iucn.sis.client.components.panels.workingsets.WorkingSetBrowser;
import org.iucn.sis.client.components.panels.workingsets.WorkingSetFullPanel;
import org.iucn.sis.client.components.panels.workingsets.WorkingSetHierarchy;
import org.iucn.sis.client.components.panels.workingsets.WorkingSetOptionsPanel;
import org.iucn.sis.client.components.panels.workingsets.WorkingSetPanel;
import org.iucn.sis.client.components.tabs.HomePageTab;
import org.iucn.sis.client.referenceui.ReferenceViewPanel;

import com.extjs.gxt.ui.client.fx.Draggable;
import com.extjs.gxt.ui.client.fx.Resizable;
import com.extjs.gxt.ui.client.widget.ComponentHelper;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.LayoutData;

public class PanelManager {
	public RecentAssessmentsPanel recentAssessmentsPanel = null;
	public TaxonConceptSummaryPanel taxonomicSummaryPanel = null;
	public TaxonomyBrowserPanel taxonomyBrowserPanel = null;
	// public AssessmentSummaryPanel assessmentSummaryPanel = null;
	public WorkingSetPanel workingSetPanel = null;
	public ExpertPanel expertPanel = null;
	public ImagePopupPanel imageViewerPanel = null;
	public InboxPanel inboxPanel = null;
	public DEMPanel DEM = null;
	public AssessmentsReviewPanel assessmentReviewPanel = null;
	public SearchPanel taxonomySearchPanel = null;
	public WorkingSetFullPanel workingSetFullPanel = null;
	public WorkingSetHierarchy workingSetHierarchy = null;
	public WorkingSetAddTaxaSearchPanel addTaxonPanel = null;
	public WorkingSetOptionsPanel workingSetOptionsPanel = null;
	public WorkingSetBrowser workingSetBrowser = null;
	public BugPanel bugPanel = null;
	public BugPanel resolvedBugPanel = null;

	public ReferenceViewPanel refViewPanel = null;

	public PanelManager() {
		recentAssessmentsPanel = new RecentAssessmentsPanel(this);
		taxonomicSummaryPanel = new TaxonConceptSummaryPanel(this);
		taxonomyBrowserPanel = new TaxonomyBrowserPanel();
		// assessmentSummaryPanel = new AssessmentSummaryPanel( this );
		workingSetPanel = new WorkingSetPanel(this);
		DEM = new DEMPanel(this);
		expertPanel = new ExpertPanel(this);
		imageViewerPanel = new ImagePopupPanel(this);
		inboxPanel = new InboxPanel(this);
		assessmentReviewPanel = new AssessmentsReviewPanel(this);
		taxonomySearchPanel = new SearchPanel(this);
		workingSetFullPanel = new WorkingSetFullPanel(this);
		workingSetHierarchy = new WorkingSetHierarchy(this);
		addTaxonPanel = new WorkingSetAddTaxaSearchPanel(this);
		workingSetOptionsPanel = new WorkingSetOptionsPanel(this);
		workingSetBrowser = new WorkingSetBrowser(this);
		bugPanel = new BugPanel("925407", this, "Zendesk Tickets");
		resolvedBugPanel = new BugPanel("926680", this, "Resolved Tickets");
		
		
		try {
			refViewPanel = new ReferenceViewPanel();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void addPanel(LayoutContainer container, LayoutContainer panel, LayoutData layoutData, boolean resizable,
			boolean draggable) {
		if (resizable) {
			Resizable r = new Resizable(panel);
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
		Resizable r = new Resizable(panel);

		container.add(panel);
	}

	public void layoutPanel(LayoutContainer panel) {
		panel.setWidth(HomePageTab.standardPanelWidth);
		panel.setHeight(HomePageTab.standardPanelHeight);
	}

}
