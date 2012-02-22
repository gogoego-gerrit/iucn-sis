package org.iucn.sis.client.panels.search;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.iucn.sis.client.api.container.SISClientBase;
import org.iucn.sis.client.api.utils.UriBase;
import org.iucn.sis.shared.api.models.SearchQuery;

import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.lwxml.shared.NativeDocument;

/**
 * Provide search result caching.  Searches are NOT 
 * cached by name; rather, by time.  Hence, two 
 * searches with the same query will NOT be determined 
 * to be equal.  This can be done later, if desirable.
 * 
 * Cache is accessible such that things like the 
 * recent search list can be drawn.
 *
 */
public class SearchCache {
	
	public static final SearchCache impl = new SearchCache();
	
	private final Map<String, SearchCacheEntry> cache; 
	
	private SearchCache() {
		cache = new LinkedHashMap<String, SearchCacheEntry>();
	}
	
	public List<SearchQuery> getRecentSearches() {
		List<SearchQuery> queries = new ArrayList<SearchQuery>();
		for (SearchCacheEntry entry : cache.values())
			queries.add(entry.getQuery());
		return queries;
	}
	
	public void search(final SearchQuery query, final GenericCallback<NativeDocument> callback) {
		if (cache.containsKey(query.getQuery())) {
			SearchCacheEntry entry = cache.get(query.getQuery());
			if (entry.getQuery().isSameTime(query))
				callback.onSuccess(entry.getDocument());
			else {
				cache.remove(query.getQuery());
				search(query, callback);
			}
		}
		else {
			final NativeDocument document = SISClientBase.getHttpBasicNativeDocument();
			document.post(UriBase.getInstance().getSISBase() + "/search", query.toXML(), new GenericCallback<String>() {
				public void onSuccess(String result) {
					cache.put(query.getQuery(), new SearchCacheEntry(query, document));
					callback.onSuccess(document);
				}
				public void onFailure(Throwable caught) {
					callback.onFailure(caught);
				}
			});
		}
	}
	
	private static class SearchCacheEntry {
		
		private final SearchQuery query;
		private final NativeDocument document;
		
		public SearchCacheEntry(SearchQuery query, NativeDocument document) {
			this.query = query;
			this.document = document;
		}
		
		public NativeDocument getDocument() {
			return document;
		}
		
		public SearchQuery getQuery() {
			return query;
		}
		
	}
	
}
