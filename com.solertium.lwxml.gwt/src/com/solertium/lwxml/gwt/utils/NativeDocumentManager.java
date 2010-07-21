/*
 * Copyright (C) 2007-2009 Solertium Corporation
 *
 * This file is part of the open source GoGoEgo project.
 *
 * Unless you have been granted a different license in writing by the
 * copyright holders for GoGoEgo, you may only modify or redistribute
 * this code under the terms of one of the following licenses:
 * 
 * 1) The Eclipse Public License, v.1.0
 *    http://www.eclipse.org/legal/epl-v10.html
 *
 * 2) The GNU General Public License, version 2 or later
 *    http://www.gnu.org/licenses
 */
package com.solertium.lwxml.gwt.utils;

import java.util.HashMap;

import com.solertium.lwxml.factory.NativeDocumentFactory;
import com.solertium.lwxml.shared.GWTNotFoundException;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.lwxml.shared.NativeDocument;

/**
 * NativeDocumentManager.java
 * 
 * Manages retrieval and caching of NativeDocuments so you don't have to!
 * 
 * @author carl.scott
 * @author david.fritz
 * 
 */
public class NativeDocumentManager {

	private static class DocumentCache extends HashMap<String, NativeDocument> {
		private static final long serialVersionUID = 1;

		public DocumentCache() {
			super();
		}

		public void addToCache(final String url, final NativeDocument doc) {
			put(url, doc);
		}

		public NativeDocument getDocumentFromCache(final String url) {
			if (isCached(url))
				return get(url);
			else
				return null;
		}

		public boolean isCached(final String url) {
			return containsKey(url) && get(url) != null;
		}

		public void removeFromCache(final String url) {
			remove(url);
		}

	}

	public final static int NO_CACHE = 0;
	public final static int CACHE = 1;
	public final static int REFETCH = 2;

	/**
	 * To enable the cache, set CACHE_ENABLED to 1. To disable it, set it to -1,
	 */
	private final static int CACHE_ENABLED = 1;

	private static NativeDocumentManager instance = new NativeDocumentManager();
	private final DocumentCache cache = new DocumentCache();

	public static NativeDocumentManager getInstance() {
		return instance;
	}

	private boolean isValid(Object result) {
		return (result != null && ((NativeDocument) result).getPeer() != null && ((NativeDocument) result)
				.getDocumentElement() != null);
	}

	public void getDocument(final String uri, final GenericCallback<NativeDocument> callback, final int noCache) {
		final NativeDocument documentCheck = getDocumentFromCache(uri);
		if (documentCheck == null || noCache == REFETCH) {
			final NativeDocument document = getDocumentInstance();
			document.get(uri, new GenericCallback<String>() {
				public void onSuccess(String arg0) {
					if (isValid(document)) {
						if (noCache == CACHE_ENABLED)
							cacheDocument(uri, document);
						callback.onSuccess(document);
					} else
						callback.onFailure(new GWTNotFoundException());
				}

				public void onFailure(Throwable arg0) {
					callback.onFailure(arg0);
				}
			});

		} else {
			callback.onSuccess(documentCheck);
		}
	}

	public void getAsText(final String uri, final GenericCallback<NativeDocument> callback, final int noCache) {
		final NativeDocument documentCheck = getDocumentFromCache(uri);
		if (documentCheck == null || noCache == REFETCH) {
			final NativeDocument document = getDocumentInstance();
			document.getAsText(uri, new GenericCallback<String>() {
				public void onSuccess(String arg0) {
					if (noCache == CACHE_ENABLED)
						cacheDocument(uri, document);
					callback.onSuccess(document);
				}

				public void onFailure(Throwable arg0) {
					callback.onFailure(arg0);
				}
			});
		} else {
			callback.onSuccess(documentCheck);
		}
	}

	public void postDocument(final String uri, final String body, final GenericCallback<NativeDocument> callback,
			final int noCache) {
		final NativeDocument documentCheck = getDocumentFromCache(uri + body);
		if (documentCheck == null || noCache == REFETCH) {
			final NativeDocument document = getDocumentInstance();
			document.post(uri, body, new GenericCallback<String>() {
				public void onSuccess(final String result) {
					if (noCache == CACHE_ENABLED)
						cacheDocument(uri + body, document);
					callback.onSuccess(document);
				}

				public void onFailure(Throwable arg0) {
					callback.onFailure(arg0);
				}
			});
		} else {
			callback.onSuccess(documentCheck);
		}
	}

	public void propfindDocument(final String uri, final GenericCallback<NativeDocument> callback, final int noCache) {
		final NativeDocument document = getDocumentInstance();
		document.propfind(uri, new GenericCallback<String>() {
			public void onSuccess(final String result) {
				callback.onSuccess(document);
			}

			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}

	public void putDocument(final String uri, final String body, final GenericCallback<NativeDocument> callback,
			final int noCache) {
		final NativeDocument document = getDocumentInstance();
		document.put(uri, body, new GenericCallback<String>() {
			public void onSuccess(final String result) {
				callback.onSuccess(document);
			}

			public void onFailure(Throwable arg0) {
				callback.onFailure(arg0);
			}
		});
	}

	public void deleteDocument(final String uri, final GenericCallback<NativeDocument> callback, final int noCache) {
		final NativeDocument document = getDocumentInstance();
		document.delete(uri, new GenericCallback<String>() {
			public void onSuccess(final String result) {
				callback.onSuccess(document);
			}

			public void onFailure(final Throwable arg0) {
				callback.onFailure(arg0);
			}
		});
	}

	protected NativeDocument getDocumentInstance() {
		return NativeDocumentFactory.newNativeDocument();
	}

	private NativeDocument getDocumentFromCache(final String url) {
		return cache.getDocumentFromCache(url);
	}

	private void cacheDocument(final String url, final NativeDocument document) {
		cache.addToCache(url, document);
	}
}
