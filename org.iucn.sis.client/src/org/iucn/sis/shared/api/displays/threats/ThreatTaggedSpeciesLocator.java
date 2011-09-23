package org.iucn.sis.shared.api.displays.threats;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.iucn.sis.client.api.ui.models.taxa.TaxonListElement;
import org.iucn.sis.client.panels.search.CheckableSearchResultsPage;
import org.iucn.sis.client.panels.utils.CheckableSearchPanel;
import org.iucn.sis.client.panels.utils.TaxonomyBrowserPanel;
import org.iucn.sis.shared.api.models.Taxon;

import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.TabPanelEvent;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.solertium.util.events.ComplexListener;
import com.solertium.util.extjs.client.WindowUtils;
import com.solertium.util.gwt.ui.DrawsLazily;

public class ThreatTaggedSpeciesLocator extends TabPanel {

	public ThreatTaggedSpeciesLocator() {
		super();
		
		add(buildBrowserPanel());
		add(buildSearchPanel());
		add(buildListingPanel());
	}
	
	private TabItem buildBrowserPanel() {
		final TaxonomyBrowserPanel panel = new SelectableTaxonomyBrowserPanel();
		
		final TabItem browser = new TabItem("Taxonomy Browser");
		browser.setLayout(new FillLayout());
		browser.addListener(Events.Select, new Listener<TabPanelEvent>() {
			public void handleEvent(TabPanelEvent be) {
				panel.update();
			}
		});
		browser.add(panel);
		
		return browser;
	}
	
	private TabItem buildSearchPanel() {
		final SelectableTaxonomySearchPanel panel = new SelectableTaxonomySearchPanel();
		//final SearchPanel panel = new SearchPanel();
		final TabItem item = new TabItem("Taxonomy Search");
		item.setLayout(new FitLayout());
		item.addListener(Events.Select, new Listener<TabPanelEvent>() {
			public void handleEvent(TabPanelEvent be) {
				panel.resetSearchBox();
			}
		});
		item.add(panel);
		
		return item;
	}
	
	private TabItem buildListingPanel() {
		final SelectExistingIASTaxaPanel panel = new SelectExistingIASTaxaPanel();
		
		final TabItem item = new TabItem("Existing IAS Taxa");
		item.setLayout(new FitLayout());
		item.addListener(Events.Select, new Listener<TabPanelEvent>() {
			public void handleEvent(TabPanelEvent be) {
				WindowUtils.showLoadingAlert("Loading...");
				panel.draw(new DrawsLazily.DoneDrawingCallback() {
					public void isDrawn() {
						panel.refresh(new DrawsLazily.DoneDrawingCallback() {
							public void isDrawn() {
								WindowUtils.hideLoadingAlert();
								item.layout();
							}
						});
					}
				});
			}
		});
		item.add(panel);
		
		return item;
	}
	
	public void getSelection(ComplexListener<Collection<Taxon>> listener) {
		TabItem item = getSelectedItem();
		if (item == null)
			listener.handleEvent(new ArrayList<Taxon>());
		else
			((Selectable)item.getItem(0)).getSelection(listener);
	}
	
	
	
	public static interface Selectable {
		
		public void getSelection(ComplexListener<Collection<Taxon>> listener); 
		
	}
	
	public static class SelectableTaxonomySearchPanel extends CheckableSearchPanel implements Selectable {
		
		public SelectableTaxonomySearchPanel() {
			super();
		}
		
		@Override
		public void getSelection(ComplexListener<Collection<Taxon>> listener) {
			((CheckableSearchResultsPage)resultsPage).loadSelection(listener);
		}
		
	}
	
	public static class SelectableTaxonomyBrowserPanel extends TaxonomyBrowserPanel implements Selectable {
		
		public SelectableTaxonomyBrowserPanel() {
			super();
			setAsCheckable(true);
		}
		
		@Override
		protected void addViewButtonToFootprint() {
			// Don't do it for this one...
		}
		
		@SuppressWarnings("deprecation")
		public void getSelection(ComplexListener<Collection<Taxon>> listener) {
			final List<Taxon> list = new ArrayList<Taxon>();
			for (TaxonListElement el : getBinder().getCheckedSelection())
				list.add(el.getNode());

			listener.handleEvent(list);
		}
		
	}
	
}
