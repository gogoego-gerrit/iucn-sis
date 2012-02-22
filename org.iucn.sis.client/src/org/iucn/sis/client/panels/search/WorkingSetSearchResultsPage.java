package org.iucn.sis.client.panels.search;

import java.util.List;

import org.iucn.sis.shared.api.models.SearchQuery;
import org.iucn.sis.shared.api.models.WorkingSet;

import com.solertium.lwxml.shared.NativeElement;

/**
 * Custom checkable implementation of a result page 
 * that doesn't list species that are already in 
 * the given working set.
 *
 */
public class WorkingSetSearchResultsPage extends CheckableSearchResultsPage {
	
	private final List<Integer> speciesIDs;
	
	public WorkingSetSearchResultsPage(WorkingSet ws, SearchQuery query) {
		super(query);
		this.speciesIDs = ws.getSpeciesIDs();
	}
	
	@Override
	protected String getEmptyText() {
		return "No taxa that are not already in your working set were found for your query.";
	}
	
	@Override
	protected TaxonSearchResult buildModel(NativeElement el) {
		TaxonSearchResult result = super.buildModel(el);
		if (speciesIDs.contains(result.getTaxonID()))
			return null;
		else
			return result;
	}

}
