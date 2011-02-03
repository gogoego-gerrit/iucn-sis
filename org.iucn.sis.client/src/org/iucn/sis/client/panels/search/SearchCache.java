package org.iucn.sis.client.panels.search;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.iucn.sis.client.api.container.SISClientBase;
import org.iucn.sis.client.api.utils.UriBase;

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
	
	private final Map<SearchQuery, NativeDocument> cache; 
	
	private SearchCache() {
		cache = new HashMap<SearchQuery, NativeDocument>();
	}
	
	public List<SearchQuery> getRecentSearches() {
		return new ArrayList<SearchQuery>(cache.keySet());
	}
	
	public void search(final SearchQuery query, final GenericCallback<NativeDocument> callback) {
		if (cache.containsKey(query))
			callback.onSuccess(cache.get(query));
		else {
			final NativeDocument document = SISClientBase.getHttpBasicNativeDocument();
			document.post(UriBase.getInstance().getSISBase() + "/search", query.toXML(), new GenericCallback<String>() {
				public void onSuccess(String result) {
					cache.put(query, document);
					callback.onSuccess(document);
				}
				public void onFailure(Throwable caught) {
					callback.onFailure(caught);
				}
			});
		}
	}
	
}
