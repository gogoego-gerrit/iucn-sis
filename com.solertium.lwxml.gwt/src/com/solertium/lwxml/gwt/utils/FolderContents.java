/*
 * Copyright (C) 2007-2009 Solertium Corporation
 *
 * This file is part of the open source GoGoEgo project.
 *
 * GoGoEgo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *  
 * GoGoEgo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GoGoEgo.  If not, see http://www.gnu.org/licenses/.
 * 
 * Unless you have been granted a different license in writing by the
 * copyright holders for GoGoEgo, only the GNU General Public License
 * grants you rights to modify or redistribute this code.
 */
package com.solertium.lwxml.gwt.utils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import com.google.gwt.http.client.URL;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.solertium.lwxml.factory.NativeDocumentFactory;
import com.solertium.lwxml.gwt.debug.SysDebugger;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.NativeElement;
import com.solertium.lwxml.shared.NativeNode;
import com.solertium.lwxml.shared.NativeNodeList;
import com.solertium.lwxml.shared.utils.ArrayUtils;
import com.solertium.util.portable.PortableAlphanumericComparator;

public class FolderContents {

	public static class CollectionComparator implements Comparator<ResourceInfo> {
		public int compare(final ResourceInfo arg0, final ResourceInfo arg1) {
			final ResourceInfo r0 = arg0;
			final ResourceInfo r1 = arg1;
			int res = 0;
			if (r0.isCollection && !r1.isCollection)
				res = -1;
			if (r1.isCollection && !r0.isCollection)
				res = 1;
			return res;
		}
	}

	public static class DateComparator implements Comparator<ResourceInfo> {
		public int compare(final ResourceInfo arg0, final ResourceInfo arg1) {
			final ResourceInfo r0 = arg0;
			final ResourceInfo r1 = arg1;
			int res = r0.lastModified.compareTo(r1.lastModified);
			if (res < 0)
				res = -1;
			if (res > 0)
				res = 1;
			return res;
		}
	}

	public static class HrefComparator extends PortableAlphanumericComparator {
		private static final long serialVersionUID = 1L;

		public int compare(final Object arg0, final Object arg1) {
			final ResourceInfo r0 = (ResourceInfo) arg0;
			final ResourceInfo r1 = (ResourceInfo) arg1;
			return super.compare(r0.href.toLowerCase(), r1.href.toLowerCase());
		}
	}

	public static class ResourceInfo {
		public String href = "";
		public boolean isCollection = false;
		public Date lastModified = new Date();
		public int size = 0;
		public HashMap<String, ArrayList<NativeNode>> otherNodes = new HashMap<String, ArrayList<NativeNode>>();
	}

	public static class SizeComparator implements Comparator<ResourceInfo> {
		public int compare(final ResourceInfo arg0, final ResourceInfo arg1) {
			final ResourceInfo r0 = arg0;
			final ResourceInfo r1 = arg1;
			int res = 0;
			if (r0.size > r1.size)
				res = -1;
			if (r1.size > r0.size)
				res = 1;
			return res;
		}
	}

	private static HashMap<String, FolderContents> cache = new HashMap<String, FolderContents>();
	private static DateTimeFormat httpDateFormat = DateTimeFormat.getFormat("EEE, dd MMM yyyy HH:mm:ss");

	public static void get(final String url, final GenericCallback<FolderContents> cb) {
		if (cache.containsKey(url)) {
			cb.onSuccess(cache.get(url));
			return;
		}
		final FolderContents fc = new FolderContents();
		fc.load(url, cb);
	}

	public static void invalidate(final String url) {
		final Iterator<String> it = cache.keySet().iterator();
		final ArrayList<String> doomed = new ArrayList<String>();
		while (it.hasNext()) {
			final String key = it.next();
			if (key.startsWith(url))
				doomed.add(key);
		}
		for (int i = 0; i < doomed.size(); i++)
			cache.remove(doomed.get(i));
	}

	/**
	 * Used when you dont want to use the cache. Obviously use with caution...
	 */
	public static void loadNoCache(final String url, final GenericCallback<FolderContents> cb) {
		final FolderContents fc = new FolderContents();
		fc.load(url, cb);
	}

	private ArrayList<ResourceInfo> resources = null;

	public FolderContents() {
	}

	public boolean containsCollections() {
		for (final Iterator<ResourceInfo> iter = resources.iterator(); iter.hasNext();)
			if ((iter.next()).isCollection)
				return true;

		return false;
	}

	public ArrayList<ResourceInfo> getResources() {
		return resources;
	}

	private void load(final String url, final GenericCallback<FolderContents> cb) {
		final NativeDocument ndoc = NativeDocumentFactory.newNativeDocument();
		final String furl = url;
		final GenericCallback<FolderContents> fcb = cb;
		final FolderContents ret = this;
		ndoc.propfind(furl, new GenericCallback<String>() {
			public void onFailure(final Throwable caught) {
				SysDebugger.getNamedInstance("error").println("onFailure for " + furl);
				caught.printStackTrace();
				fcb.onFailure(caught);
			}

			public void onSuccess(final String result) {
				try {
					resources = new ArrayList<ResourceInfo>();
					final NativeNodeList resps = ndoc.getDocumentElement().getElementsByTagName("response");
					for (int i = 0; i < resps.getLength(); i++) {
						final ResourceInfo ri = new ResourceInfo();
						ri.href = URL.decode(resps.elementAt(i).getElementByTagName("href").getText());
						NativeElement col = null;
						NativeElement prop = null;
						try {
							prop = resps.elementAt(i).getElementByTagName("propstat").getElementByTagName("prop");
							col = prop.getElementByTagName("resourcetype").getElementByTagName("collection");
						} catch (final Exception e) {
							continue;
						}
						try {
							if ("collection".equals(col.getNodeName()))
								ri.isCollection = true;
						} catch (final Exception ignored) {
						}
						try {
							final String size = prop.getElementByTagName("getcontentlength").getText();
							ri.size = Integer.parseInt(size);
						} catch (final Exception ignored) {
						}
						try {
							String lastmodified = prop.getElementByTagName("getlastmodified").getText();
							lastmodified = lastmodified.replaceAll(" GMT", "");
							ri.lastModified = httpDateFormat.parse(lastmodified);
						} catch (final Exception ignored) {
						}
						resources.add(ri);
					}
				} catch (final Exception ignored) {
				}
				cache.put(furl, ret);
				fcb.onSuccess(ret);
			}
		});
	}

	public void sortByCollection() {
		ArrayUtils.insertionSort(resources, new CollectionComparator());
	}

	public void sortByDate() {
		ArrayUtils.insertionSort(resources, new DateComparator());
	}

	public void sortByHref() {
		ArrayUtils.insertionSort(resources, new HrefComparator());
	}

	public void sortBySize() {
		ArrayUtils.insertionSort(resources, new SizeComparator());
	}

}
