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
package org.gogoego.api.collections;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.gogoego.api.applications.ServerApplicationAPI;
import org.gogoego.api.plugins.GoGoEgo;
import org.w3c.dom.Document;

import com.solertium.util.MD5Hash;
import com.solertium.vfs.NotFoundException;
import com.solertium.vfs.VFS;
import com.solertium.vfs.VFSPath;
import com.solertium.vfs.VFSPathToken;

public class CachedCollections {
	
	private static final Map<String, CachedCollections> cache = new HashMap<String, CachedCollections>();
	
	public static CachedCollections getCache(ServerApplicationAPI siteInfo) {
		if (!cache.containsKey(siteInfo.getSiteID())) {
			final CachedCollections cachedCollections = new CachedCollections(siteInfo.getVFS());
			cachedCollections.init();
			cache.put(siteInfo.getSiteID(), cachedCollections);
		}
		return cache.get(siteInfo.getSiteID());
	}
	
	private final VFS vfs;
	private final Map<VFSPath, GenericCollection> collections;
	private final Map<VFSPath, GoGoEgoItem> items;
	
	public CachedCollections(VFS vfs) {
		this.vfs = vfs;
		collections = new HashMap<VFSPath, GenericCollection>();
		items = new HashMap<VFSPath, GoGoEgoItem>();
	}
	
	public void init() {
		final Date startTime = Calendar.getInstance().getTime();
		GoGoEgo.debug().println("Initializing Collection Cache...");
		final VFSPath start = new VFSPath("/(SYSTEM)/collections");
		
		final Document document;
		try {
			document = vfs.getDocument(start.child(new VFSPathToken(Constants.COLLECTION_ROOT_FILENAME)));
		} catch (IOException e) {
			return;
		}
		
		loadCollection(new VFSPath("/collections"), new GenericCollection(document));
		
		Date endTime = Calendar.getInstance().getTime();
		
		GoGoEgo.debug().println("Done Initializing Collections in {0} ms", endTime.getTime() - startTime.getTime());
		/*// Print to ensure you cached what you think should be there...
		for (VFSPath uri : collections.keySet())
			System.out.println("Loaded Collection " + uri);
		for (VFSPath uri : items.keySet())
			System.out.println("Loaded Item " + uri);
		*/
	}
	
	private void loadCollection(final VFSPath uri, final GenericCollection collection) {
		for (GoGoEgoCollection subcollection : collection.getSubCollections().values()) {
			final VFSPath subUri = uri.child(new VFSPathToken(subcollection.getCollectionID()));
			final GenericCollection c;
			try {
				c = new GenericCollection(vfs.getDocument(new VFSPath("/(SYSTEM)" + subUri + "/" + Constants.COLLECTION_ROOT_FILENAME)));
			} catch (IOException e) {
				GoGoEgo.debug().println("Could not load {0}", subcollection.getCollectionAccessURI());
				continue;
			}
			loadCollection(subUri, c);
			collection.addSubCollection(c);
		}
		for (GoGoEgoItem item : collection.getItems().values()) {
			final VFSPath itemUri;
			if (item instanceof ReferenceItem) {
				itemUri = uri.child(new VFSPathToken("$RI$" + new MD5Hash(((ReferenceItem) item).getItemURI()).toString()));
			}
			else {
				itemUri = uri.child(new VFSPathToken(item.getItemID()));
				final VFSPath itemPath = new VFSPath("/(SYSTEM)" + itemUri + ".xml");
				try {
					item.convertFromXMLDocument(vfs.getDocument(itemPath));
				} catch (IOException e) {
					continue;
				}
			}
			items.put(itemUri, item);
		}
		collections.put(uri, collection);
	}
	
	public GenericCollection getCollection(final VFSPath uri) throws NotFoundException { 
		if (!collections.containsKey(uri))
			throw new NotFoundException(uri + " does not exist.");
		return collections.get(uri);
	}
	
	public GoGoEgoItem getItem(final VFSPath uri) throws NotFoundException {
		return getItem(uri, 0);
	}
	
	public void removeItem(final VFSPath uri) {
		items.remove(uri);
	}
	
	public void addItem(final VFSPath uri, final GoGoEgoItem item) {
		items.put(uri, item);
	}
	
	public void removeCollection(final VFSPath uri) {
		collections.remove(uri);
	}
	
	public void addCollection(final VFSPath uri, final GenericCollection collection) {
		collections.put(uri, collection);
	}
	
	private GoGoEgoItem getItem(final VFSPath uri, int counter) throws NotFoundException {
		if (!items.containsKey(uri) || counter >= 10)
			throw new NotFoundException(uri + " does not exist.");
		final GoGoEgoItem item = items.get(uri);
		if (item instanceof ReferenceItem)
			return getItem(new VFSPath(((ReferenceItem) item).getItemURI()), counter + 1);
		else
			return item;
	}

}
