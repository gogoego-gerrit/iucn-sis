package org.iucn.sis.client.panels.search;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.iucn.sis.client.api.caches.TaxonomyCache;
import org.iucn.sis.shared.api.models.SearchQuery;
import org.iucn.sis.shared.api.models.Taxon;

import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.widget.grid.CheckBoxSelectionModel;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.GridSelectionModel;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.util.events.ComplexListener;
import com.solertium.util.extjs.client.WindowUtils;

/**
 * A search page with checkable results, support for 
 * row-clicking to select, and select all/none features.
 * 
 * getSelection will lazily fetch taxonIDs, while 
 * loadSelection will fetch Taxon models from the 
 * server.
 *
 */
public class CheckableSearchResultsPage extends SearchResultPage {

	public CheckableSearchResultsPage(SearchQuery query) {
		super(query);
		
		grid.addPlugin((CheckBoxSelectionModel<TaxonSearchResult>)sm);
	}
	
	@Override
	protected GridSelectionModel<TaxonSearchResult> createSelectionModel() {
		CheckBoxSelectionModel<TaxonSearchResult> sm = new CheckBoxSelectionModel<TaxonSearchResult>();
		sm.setSelectionMode(SelectionMode.SIMPLE);
		
		return sm;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	protected ColumnModel getColumnModel() {
		ColumnModel cm = super.getColumnModel();
		
		List<ColumnConfig> cols = new ArrayList<ColumnConfig>();
		cols.add(((CheckBoxSelectionModel)sm).getColumn());
		cols.addAll(cm.getColumns());
		
		return new ColumnModel(cols);
	}
	
	/**
	 * Use loadSelection if you want to actually load 
	 * the selected taxon into cache.  If you just 
	 * need the IDs, this function is fine.
	 * @return
	 */
	public Collection<Integer> getSelection() {
		final List<Integer> taxa = new ArrayList<Integer>();
		for (TaxonSearchResult result : sm.getSelectedItems())
			taxa.add(result.getTaxonID());
		
		return taxa;
	}
	
	public void loadSelection(final ComplexListener<Collection<Taxon>> callback) {
		List<TaxonSearchResult> checked = sm.getSelectedItems();
		if (checked.isEmpty())
			callback.handleEvent(new ArrayList<Taxon>());
		else {
			final List<Integer> taxa = new ArrayList<Integer>();
			for (TaxonSearchResult result : checked)
				taxa.add(result.getTaxonID());
			TaxonomyCache.impl.fetchList(taxa, new GenericCallback<String>() {
				public void onSuccess(String result) {
					final List<Taxon> models = new ArrayList<Taxon>();
					
					for (Integer id : taxa)
						models.add(TaxonomyCache.impl.getTaxon(id));
					
					callback.handleEvent(models);
				}
				public void onFailure(Throwable caught) {
					WindowUtils.errorAlert("Error loading taxa.");
				}
			});
		}
	}
	
}
