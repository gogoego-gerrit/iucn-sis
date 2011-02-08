package org.iucn.sis.client.api.caches;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.iucn.sis.client.api.container.SISClientBase;
import org.iucn.sis.client.api.utils.UriBase;
import org.iucn.sis.shared.api.debug.Debug;
import org.iucn.sis.shared.api.models.Bookmark;

import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.NativeNodeList;

public class BookmarkCache {
	
	public static final BookmarkCache impl = new BookmarkCache();
	
	private final Map<Integer, Bookmark> cache;
	
	private BookmarkCache() {
		cache = new HashMap<Integer, Bookmark>();
	}
	
	public void load() {
		final NativeDocument document = SISClientBase.getHttpBasicNativeDocument();
		document.get(UriBase.getInstance().getBookmarksBase() + "/bookmarks", new GenericCallback<String>() {
			public void onSuccess(String result) {
				cache.clear();
				
				final NativeNodeList nodes = document.getDocumentElement().getElementsByTagName("bookmark");
				for (int i = 0; i < nodes.getLength(); i++) {
					Bookmark bookmark = Bookmark.fromXML(nodes.elementAt(i));
					cache.put(bookmark.getId(), bookmark);
				}
			}
			public void onFailure(Throwable caught) {
				Debug.println("Failed to load bookmarks.");
			}
		});
	}
	
	public Bookmark get(Integer id) {
		return cache.get(id);
	}
	
	/**
	 * Retrieve a list of bookmarks sorted in 
	 * descending order by date.
	 * @return
	 */
	public List<Bookmark> list() {
		return list(new BookmarkDateComparator());
	}
	
	public List<Bookmark> list(Comparator<Bookmark> comparator) {
		List<Bookmark> list = new ArrayList<Bookmark>(cache.values());
		Collections.sort(list, comparator);
		
		return list;
	}
	
	public void remove(final Bookmark bookmark, final GenericCallback<String> callback) {
		final NativeDocument document = SISClientBase.getHttpBasicNativeDocument();
		document.delete(UriBase.getInstance().getBookmarksBase() + "/bookmarks/" + bookmark.getId(), 
				new GenericCallback<String>() {
			public void onSuccess(String result) {
				cache.remove(bookmark.getId());
				callback.onSuccess(result);
			}
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}
	
	public void update(final Bookmark bookmark, final GenericCallback<String> callback) {
		final NativeDocument document = SISClientBase.getHttpBasicNativeDocument();
		document.post(UriBase.getInstance().getBookmarksBase() + "/bookmarks/" + bookmark.getId(), 
				bookmark.toXML(), new GenericCallback<String>() {
			public void onSuccess(String result) {
				callback.onSuccess(result);
			}
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}
	
	public void add(final Bookmark bookmark, final GenericCallback<String> callback) {
		final NativeDocument document = SISClientBase.getHttpBasicNativeDocument();
		document.put(UriBase.getInstance().getBookmarksBase() + "/bookmarks", 
				bookmark.toXML(), new GenericCallback<String>() {
			public void onSuccess(String in) {
				Bookmark result = Bookmark.fromXML(document.getDocumentElement());
				
				bookmark.setId(result.getId());
				cache.put(bookmark.getId(), bookmark);
				
				callback.onSuccess(in);
			}
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}
	
	public static class BookmarkDateComparator implements Comparator<Bookmark> {
		
		@Override
		public int compare(Bookmark arg0, Bookmark arg1) {
			return arg1.getDate().compareTo(arg0.getDate());
		}
		
	}
	
	public static class BookmarkNameComparator implements Comparator<Bookmark> {
		
		@Override
		public int compare(Bookmark arg0, Bookmark arg1) {
			return arg0.getName().compareTo(arg1.getName());
		}
		
	}

}
