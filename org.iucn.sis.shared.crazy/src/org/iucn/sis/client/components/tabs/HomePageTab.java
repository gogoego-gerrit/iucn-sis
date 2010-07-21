package org.iucn.sis.client.components.tabs;

import org.iucn.sis.client.components.panels.PanelManager;

import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.custom.Portal;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;

public class HomePageTab extends TabItem {
	private PanelManager panelManager = null;

	public final static int standardPanelWidth = 200;
	public final static int standardPanelHeight = 400;

	// private LayoutContainer west = null;
	// private LayoutContainer east = null;
	// private LayoutContainer center = null;

	private Portal portal;

	/**
	 * Defaults to having Style.NONE
	 */
	public HomePageTab(PanelManager manager) {
		super();
		panelManager = manager;

		build();
	}

	public void build() {
		setText("Home Page");
		setLayout(new FitLayout());

		portal = new Portal(3);
		portal.setBorders(true);
		portal.setColumnWidth(0, .33);
		portal.setColumnWidth(1, .33);
		portal.setColumnWidth(2, .33);

		panelManager.workingSetPanel.configureThisAsPortlet();
		panelManager.recentAssessmentsPanel.configureThisAsPortlet();
		panelManager.inboxPanel.configureThisAsPortlet();
		panelManager.assessmentReviewPanel.configureThisAsPortlet();
		panelManager.bugPanel.configureThisAsPortlet();
		panelManager.resolvedBugPanel.configureThisAsPortlet();

		portal.add(panelManager.workingSetPanel, 0);
		portal.add(panelManager.recentAssessmentsPanel, 1);
		portal.add(panelManager.inboxPanel, 2);
		portal.add(panelManager.assessmentReviewPanel, 2);
		portal.add(panelManager.bugPanel, 2);
		portal.add(panelManager.resolvedBugPanel, 2);

		add(portal);

		// BorderLayoutData westData = new BorderLayoutData(LayoutRegion.WEST,
		// (float) .33);
		// westData.setMargins(new Margins(2, 2, 2, 2));
		// FlowLayout westLayout = new FlowLayout();
		// west = new LayoutContainer();
		// westLayout.setMargins(new Margins(2, 2, 2, 2));
		// west.setBorders(false);
		// west.setLayout(westLayout);
		//
		// BorderLayoutData centerData = new
		// BorderLayoutData(LayoutRegion.CENTER, (float) .34);
		// centerData.setMargins(new Margins(2, 2, 2, 2));
		// FlowLayout centerLayout = new FlowLayout();
		// centerLayout.setMargins(new Margins(2, 2, 2, 2));
		// center = new LayoutContainer();
		// center.setLayout(centerLayout);
		// center.setBorders(false);
		//
		// FlowLayout eastLayout = new FlowLayout();
		// eastLayout.setMargins(new Margins(2, 2, 2, 2));
		// BorderLayoutData eastData = new BorderLayoutData(LayoutRegion.EAST,
		// (float) .33);
		// eastData.setMargins(new Margins(2, 2, 2, 2));
		// east = new LayoutContainer();
		// east.setLayout(eastLayout);
		// east.setBorders(false);
		//
		// panelManager.addPanel(this, center, centerData, false, false);
		// panelManager.addPanel(this, west, westData, false, false);
		// panelManager.addPanel(this, east, eastData, false, false);
		//
		// panelManager.addPanel(west, panelManager.workingSetPanel, null,
		// false, false);
		// panelManager.addPanel(west, panelManager.myAssessmentsPanel, null,
		// false, false);
		// panelManager.addPanel(center, panelManager.recentAssessmentsPanel,
		// null, false, false);
		// panelManager.addPanel(east, panelManager.inboxPanel, null, false,
		// false);
		// panelManager.addPanel(east, panelManager.assessmentReviewPanel, null,
		// false, false);
		//
		// panelManager.workingSetPanel.refresh();
		// panelManager.recentAssessmentsPanel.refresh();

		layout();
	}

}
