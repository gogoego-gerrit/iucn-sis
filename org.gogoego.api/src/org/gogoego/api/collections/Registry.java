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

import java.util.HashMap;

import org.gogoego.api.utils.DocumentUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.solertium.util.BaseDocumentUtils;
import com.solertium.vfs.VFS;

/**
 * Registry.java
 * 
 * Knows where all items are located by URI so they can be found via ID ...
 * these are the public-accessible URIs
 * 
 * <registry> <item id="_id_" uri="_uri_" name="_name_" /> ... <collection
 * id="_id_" uri="_uri_" name="_name_" /> ... </registry>
 * 
 * @author carl.scott
 * 
 */
public class Registry {

	public static final String DATA_NAME = "name";
	public static final String DATA_URI = "uri";
	public static final String ITEM_DATA_STOCK = "stock";
	public static final String ITEM_DATA_PURCHASE_LIMIT = "purchaselimit";

	private static final String REGISTRY_PROTOCOL_COLLECTION = "collection";
	private static final String REGISTRY_PROTOCOL_ITEM = "item";

	private final String path;
	private final VFS vfs;
	
	private RegistryWriter writer;

	/**
	 * Creates a new registry instance
	 * 
	 * @param path
	 *            the location on the VFS of the registry file
	 */
	public Registry(final String path, final VFS vfs) {
		this.path = path;
		this.vfs = vfs;
		setImmediateWriter();
		/*
		 * try { VFSMetadata md = vfs.getMetadata(path); md.setGenerated(true);
		 * md.setVersioned(false); vfs.setMetadata(path, md); } catch
		 * (ConflictException e) { TrivialExceptionHandler.ignore(this, e); }
		 */
	}
	
	public void setDeferredWriter() {
		this.writer = new DeferredRegistryWriter(vfs, path);
	}
	
	public void setImmediateWriter() {
		this.writer = new ImmediateRegistryWriter(vfs, path);
	}
	
	public Document getDocument() {
		if (writer != null)
			return writer.getDocument();
		return BaseDocumentUtils.impl.createDocumentFromString("<registry/>");
	}

	/**
	 * Retrieves pertinent collection information from the registry
	 * 
	 * @param collectionID
	 *            the ID of the collection to retrieve
	 * @param storeInstance
	 *            the store the collection belongs to
	 * @return a hashmap containing the URI and the name of the collection,
	 *         accessible via specified constants
	 */
	public HashMap<String, String> getCollectionDataFromRegistry(final String collectionID) {
		return getDataFromRegistry(collectionID, REGISTRY_PROTOCOL_COLLECTION);
	}

	/** ******* PUBLICLY EXPOSED FUNCTIONALITY ********** */

	private HashMap<String, String> getDataFromRegistry(final String id, final String protocol) {
		final Document document = writer.getDocument();
		final NodeList items = document.getElementsByTagName(protocol);

		final HashMap<String, String> data = new HashMap<String, String>();
		data.put(DATA_URI, "");
		data.put(DATA_NAME, "");

		for (int i = 0; i < items.getLength(); i++) {
			final Node current = items.item(i);
			if (current.getNodeName().equalsIgnoreCase(protocol)
					&& DocumentUtils.impl.getAttribute(current, "id").equalsIgnoreCase(id)) {

				for (int k = 0; k < current.getAttributes().getLength(); k++) {
					data.put(current.getAttributes().item(k).getNodeName(), current.getAttributes().item(k)
							.getNodeValue());
				}
				break;
			}
		}

		return data;
	}

	/**
	 * Retrieves pertinent item information from the registry
	 * 
	 * @param itemID
	 *            the ID of the item to retrieve
	 * @param storeInstance
	 *            the store the item belongs to
	 * @return a hashmap containing the URI and the name of the item, accessible
	 *         via specified constants
	 */
	public HashMap<String, String> getItemDataFromRegistry(final String itemID) {
		return getDataFromRegistry(itemID, REGISTRY_PROTOCOL_ITEM);
	}

	public boolean isCollectionRegistered(final String id) {
		return isRegistered(id, REGISTRY_PROTOCOL_COLLECTION);
	}

	public boolean isItemRegistered(final String id) {
		return isRegistered(id, REGISTRY_PROTOCOL_ITEM);
	}

	private boolean isRegistered(final String id, final String protocol) {
		if (writer == null)
			return false;
		
		final Document document = writer.getDocument();
		final NodeList nodes = document.getElementsByTagName(protocol);

		for (int i = 0; i < nodes.getLength(); i++) {
			final Node current = nodes.item(i);
			if (current.getNodeName().equals(protocol) && DocumentUtils.impl.getAttribute(current, "id").equals(id))
				return true;
		}
		
		return false;
	}

	/** ******** GETTERS, SETTERS *********** */

	public String getPath() {
		return path;
	}

	/**
	 * Removes a collection from the registry
	 * 
	 * @param collectionID
	 *            the ID of the collection
	 * @param storeInstance
	 *            the store the collection belongs to
	 * @return true if removed, false otherwise
	 */
	public boolean removeFromCollectionRegistry(final String collectionID) {
		return removeFromRegistry(collectionID, REGISTRY_PROTOCOL_COLLECTION);
	}

	/**
	 * Removes an item from the registry
	 * 
	 * @param itemID
	 *            the ID of the item
	 * @param storeInstance
	 *            the store the item belongs to
	 * @return true if removed, false otherwise
	 */
	public boolean removeFromItemRegistry(final String itemID) {
		return removeFromRegistry(itemID, REGISTRY_PROTOCOL_ITEM);
	}

	private boolean removeFromRegistry(final String id, final String protocol) {
		return writer != null && writer.removeFromRegistry(id, protocol);
	}

	/**
	 * Adds or updates a collection in the registry
	 * 
	 * @param collectionID
	 *            the ID of the collection
	 * @param collectionURI
	 *            the ACTUAL URI of the file (not nec'y the same as the access
	 *            URI)
	 * @param collectionName
	 *            the name of the collection, so we dont have to go looking for
	 *            it
	 * @param storeInstance
	 *            the store the collection belongs to
	 * @return true if registry was updated, false otherwise
	 */
	public boolean updateCollectionRegistry(final GoGoEgoCollection collection) {
		return updateRegistry(collection.getCollectionID(), collection.getCollectionAccessURI(), collection.getName(),
				REGISTRY_PROTOCOL_COLLECTION);
	}

	/**
	 * Adds or updates an item in the registry
	 * 
	 * @param itemID
	 *            the ID of the item
	 * @param itemURI
	 *            the ACTUAL URI of the file (not nec'y the same as the access
	 *            URI)
	 * @param itemName
	 *            the name of the item, so we dont have to go looking for it
	 * @param vfs
	 *            the store the item belongs to
	 * @return true if registry was updated, false otherwise
	 */
	public boolean updateItemRegistry(final GoGoEgoItem item, final GoGoEgoCollection parent) {
		return updateRegistry(item.getItemID(), parent.getCollectionAccessURI() + "/" + item.getItemID(), item
				.getItemName(), REGISTRY_PROTOCOL_ITEM);
	}

	/**
	 * Adds arbitrary data to an existing item in the registry
	 * 
	 * @param itemID
	 * @param data
	 * @return true if found and added, false otherwise
	 */
	public boolean updateItemRegistryData(final String itemID, final HashMap<String, String> data) {
		return updateRegistryData(itemID, REGISTRY_PROTOCOL_ITEM, data);
	}
	
	public boolean persistRegistry() {
		return writer.persistDocument(writer.getDocument());
	}

	/** ********* PRIVATE HELPER FUNCTIONS ********* */

	private boolean updateRegistry(final String id, final String uri, final String name, final String protocol) {
		return writer != null && writer.updateRegistry(id, uri, name, protocol);
	}

	private boolean updateRegistryData(final String id, final String protocol, final HashMap<String, String> data) {
		return writer != null && writer.updateRegistryData(id, protocol, data);
	}

}
