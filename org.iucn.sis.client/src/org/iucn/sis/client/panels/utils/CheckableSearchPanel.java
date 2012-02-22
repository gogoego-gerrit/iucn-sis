package org.iucn.sis.client.panels.utils;

import org.iucn.sis.client.panels.search.CheckableSearchResultsPage;
import org.iucn.sis.client.panels.search.SearchResultPage;
import org.iucn.sis.shared.api.models.SearchQuery;

public class CheckableSearchPanel extends SearchPanel {
	
	public CheckableSearchPanel() {
		super();
	}
	
	@Override
	protected SearchResultPage createSearchResultsPage(SearchQuery query) {
		return new CheckableSearchResultsPage(query);
	}

}
