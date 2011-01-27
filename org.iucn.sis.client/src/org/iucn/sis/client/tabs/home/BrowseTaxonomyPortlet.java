package org.iucn.sis.client.tabs.home;

import org.iucn.sis.client.panels.utils.TaxonomyBrowserPanel;

import com.extjs.gxt.ui.client.widget.custom.Portlet;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;

public class BrowseTaxonomyPortlet extends Portlet {
	
	public BrowseTaxonomyPortlet() {
		super();
		setCollapsible(true);
		setAnimCollapse(false);
		setLayout(new FitLayout());
		setLayoutOnChange(true);
		setHeading("Browse Taxonomy");
		setHeight(350);
		
		final TaxonomyBrowserPanel browser = new TaxonomyBrowserPanel();
		browser.update();
		
		add(browser);
	}

}
