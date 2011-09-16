package org.iucn.sis.client.tabs;

import org.iucn.sis.client.panels.assessments.RecentAssessmentsPanel;
import org.iucn.sis.client.tabs.home.BrowseTaxonomyPortlet;
import org.iucn.sis.client.tabs.home.SearchPortlet;
import org.iucn.sis.client.tabs.home.VideoContentPortlet;

import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.custom.Portal;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;

public class HomePageTab extends LayoutContainer {

	public final static int standardPanelWidth = 200;
	public final static int standardPanelHeight = 400;

	// private LayoutContainer west = null;
	// private LayoutContainer east = null;
	// private LayoutContainer center = null;

	private Portal portal;

	/**
	 * Defaults to having Style.NONE
	 */
	public HomePageTab() {
		super();
		setLayout(new FitLayout());

		portal = new Portal(3);
		portal.setBorders(true);
		portal.setColumnWidth(0, .33);
		portal.setColumnWidth(1, .33);
		portal.setColumnWidth(2, .33);

		/*WorkingSetPanel workingSetPanel = new WorkingSetPanel();
		workingSetPanel.configureThisAsPortlet();*/
		
		RecentAssessmentsPanel recentAssessmentsPanel = new RecentAssessmentsPanel();
		recentAssessmentsPanel.configureThisAsPortlet();
		
		/*BugPanel bugPanel = new BugPanel("925407", "Zendesk Tickets");
		bugPanel.configureThisAsPortlet();
		
		BugPanel resolvedBugPanel = new BugPanel("926680", "Resolved Tickets");
		resolvedBugPanel.configureThisAsPortlet();*/

		portal.add(new SearchPortlet(), 0);
		portal.add(new BrowseTaxonomyPortlet(), 0);
		portal.add(recentAssessmentsPanel, 1);
		portal.add(new VideoContentPortlet(), 2);
		

		add(portal);
	}

}
