package org.iucn.sis.client.panels.utils;

import org.iucn.sis.client.panels.search.CheckableSearchResultsPage;
import org.iucn.sis.client.panels.search.SearchQuery;
import org.iucn.sis.client.panels.search.SearchResultPage;

public class CheckableSearchPanel extends SearchPanel {
	
	public CheckableSearchPanel() {
		super();
	}
	
	@Override
	protected SearchResultPage createSearchResultsPage(SearchQuery query) {
		return new CheckableSearchResultsPage(query);
	}

}
