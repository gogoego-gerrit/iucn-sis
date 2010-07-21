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

package org.gogoego.api.collections;

import java.io.IOException;
import java.io.StringReader;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.gogoego.api.plugins.GoGoEgo;
import org.gogoego.api.representations.GoGoEgoBaseRepresentation;
import org.gogoego.api.representations.GoGoEgoCollectionRepresentation;
import org.gogoego.api.representations.GoGoEgoItemRepresentation;
import org.gogoego.api.utils.DocumentUtils;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.data.Tag;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import com.solertium.util.TrivialExceptionHandler;
import com.solertium.util.restlet.InternalRequest;
import com.solertium.vfs.VFS;
import com.solertium.vfs.VFSPath;
import com.solertium.vfs.VFSPathToken;
import com.solertium.vfs.utils.VFSUtils;

/**
 * CollectionResourceBuilder.java
 * 
 * Object layer that supports operations on collections
 * 
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a href="http://www.solertium.com">Solertium Corporation</a>
 *
 */
public class CollectionResourceBuilder {
	
	public static final String IS_COLLECTION = "org.gogoego.api.collections.IS_COLLECTION";
	
	protected static final String BASE_URL = "/(SYSTEM)";

	protected final Context context;
	protected final VFS vfs;
	protected final VFSWrapper vfsWrapper;

	protected boolean nullOnFailure = true;

	public CollectionResourceBuilder(final Context context) {
		this(context, true);
	}

	public CollectionResourceBuilder(final Context context, final boolean nullOnFailure) {
		this.context = context;
		this.nullOnFailure = nullOnFailure;

		vfs = GoGoEgo.get().getFromContext(context).getVFS();
		vfsWrapper = new VFSWrapper(vfs);
	}

	public CategoryData getCurrentCategory(final VFSPath path) {
		return getCurrentCategory(path, new GenericCollection(getHeadDocument()));
	}

	public CategoryData getCurrentCategory(VFSPath path, final GenericCollection head) {
		final VFSPathToken[] uriAsPieces = path.getTokens();

		final CategoryData categoryData = new CategoryData();

		GenericCollection current = head;
		current.setCollectionFileLocation(getBaseDocumentPath());
		categoryData.setCategory(current);
		
		for (int i = (path.toString().startsWith("/" + getBaseID())) ? 1 : 0; i < uriAsPieces.length; i++) {
			final String currentKey = uriAsPieces[i].toString();
			if (current.getSubCollections().containsKey(currentKey)) {
				final GenericCollection parent = current;
				final GenericCollection subCollection;
				VFSPath vfsPath = new VFSPath(BASE_URL + current.getCollectionAccessURI() + "/" + currentKey + "/"
						+ Constants.COLLECTION_ROOT_FILENAME);
				categoryData.setVFSPath(vfsPath);
				//Placeholder....
				//subCollection = new GenericCollection(currentKey, currentKey);
				try {
					subCollection = new GenericCollection(DocumentUtils.getReadOnlyDocument(vfsPath, vfs));
					subCollection.setCollectionFileLocation(vfsPath);
				} catch (IOException e) {
					return null;
				}
				parent.addSubCollection(subCollection);
				categoryData.setCategory(subCollection);
				current = subCollection;
			} else if (current.getItems().containsKey(currentKey))
				categoryData.setItemID(currentKey);
			else {
				// Invalid request
				if (nullOnFailure)
					return null;
			}
		}

		return categoryData;
	}

	public Document getHeadDocument() {
		Document baseDoc;
		try {
			baseDoc = DocumentUtils.getReadWriteDocument(getBaseDocumentPath(), vfs);
		} catch (IOException e) {
			baseDoc = null;
		}

		if (baseDoc == null)
			baseDoc = getDefaultBaseDocument();

		return baseDoc;
	}
	
	protected String getBaseID() {
		return "collections";
	}
	
	protected VFSPath getBaseDocumentPath() {
		return new VFSPath(BASE_URL + "/" + getBaseID() + "/" + Constants.COLLECTION_ROOT_FILENAME);
	}
	
	protected Document getDefaultBaseDocument() {
		return DocumentUtils.impl
		.createDocumentFromString("<collection id=\"" + getBaseID() + "\" name=\"My Collections\"></collection>");
	}

	public boolean handleCopy(final CategoryData categoryData, final Request request, final String newItemID) {
		final GoGoEgoCollection category = categoryData.getCategory();
		final String itemID = categoryData.getItemID();
		final String itemURI = BASE_URL + category.getCollectionAccessURI() + "/" + itemID + ".xml";

		Document itemDocument;
		try {
			itemDocument = DocumentUtils.getReadOnlyDocument(new VFSPath(itemURI), vfs);
		} catch (IOException e) {
			itemDocument = null;
		}

		if (itemDocument == null)
			return false;

		final GenericItem item = new GenericItem(itemDocument);
		item.setItemID(newItemID);
		item.setItemName("Copy of " + item.getItemName());

		Request riap = new InternalRequest(request, Method.PUT,
				"riap://host/admin" + category.getCollectionAccessURI(), new DomRepresentation(MediaType.TEXT_XML,
						DocumentUtils.impl.createDocumentFromString(item.toXML())));

		Response resp = context.getClientDispatcher().handle(riap);
		
		boolean status = resp.getStatus().isSuccess();
		if (status)
			CollectionCache.getInstance().invalidate(context, category.getCollectionAccessURI());
		
		return status;
	}

	public boolean handleReferenceItemCopy(final Request request, final CategoryData categoryData,
			final ReferenceItem item, final String destination) {
		Request riap = new InternalRequest(request, Method.PUT, "riap://host" + destination, new DomRepresentation(
				MediaType.TEXT_XML, DocumentUtils.impl.createDocumentFromString(item.toXML())));

		Response resp = context.getClientDispatcher().handle(riap);
		
		boolean status = resp.getStatus().isSuccess();
		if (status)
			CollectionCache.getInstance().invalidate(context, categoryData.getCategory().getCollectionAccessURI());
		
		return status;
	}

	public GoGoEgoBaseRepresentation handleGet(final CategoryData categoryData, final Request request, final Response response, final Variant variant) {

		final GenericCollection category = (GenericCollection) categoryData.getCategory();

		GoGoEgoXMLObject object = category;
		VFSPath uri = category.getCollectionFileLocation();

		if (categoryData.getItemID() != null) {
			uri = new VFSPath(BASE_URL + category.getCollectionAccessURI() + "/" + categoryData.getItemID() + ".xml");
			response.getAttributes().put(Constants.VFS_URI, uri);

			Document itemDocument;
			try {
				itemDocument = DocumentUtils.getReadOnlyDocument(uri, vfs);
			} catch (IOException e) {
				itemDocument = null;
			}

			if (itemDocument == null) {
				response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
				return null;
			} else
				object = new GenericItem(itemDocument);
		} else {
			response.getAttributes().put(Constants.VFS_URI, categoryData.getVFSPath());
		}

		response.setStatus(Status.SUCCESS_OK);

		String xml = object.toXML();

		final GoGoEgoBaseRepresentation representation;
		if (categoryData.getItemID() == null) {
			representation = new GoGoEgoCollectionRepresentation(request, context, xml);
			representation.setMediaType(variant.getMediaType());
		}
		else {
			representation = new GoGoEgoItemRepresentation(request, context, 
				new VFSPath(BASE_URL + categoryData.getCategory().getCollectionAccessURI() + "/" + Constants.COLLECTION_ROOT_FILENAME), xml);
			representation.setMediaType(variant.getMediaType());
		}
		try {
			representation.setSize(xml.length());
			representation.setModificationDate(new Date(vfs.getLastModified(uri)));
			representation.setTag(new Tag(vfs.getETag(uri)));
		} catch (Exception e) {
			e.printStackTrace();
			TrivialExceptionHandler.ignore(this, e);
		}
		
		if (request.getAttributes().containsKey(Constants.COLLECTION_CACHE_CONTENT)) {
			CollectionCacheContent content;
			if (categoryData.getItemID() == null)
				content = new CollectionCacheContent(categoryData);
			else
				content = new CollectionCacheContent(categoryData, (GenericItem)object);
			response.getAttributes().put(Constants.COLLECTION_CACHE_CONTENT, content);
		}

		return representation;
	}

	public GoGoEgoBaseRepresentation handleGet(final Request request, final VFSPath path) {
		GoGoEgoBaseRepresentation returnResource = null;

		final CategoryData categoryData = getCurrentCategory(path, new GenericCollection(getHeadDocument()));

		if (categoryData == null)
			return null;

		final GenericCollection category = (GenericCollection) categoryData.getCategory();
		
		final Map<String, Object> attributes = new HashMap<String, Object>();

		if (categoryData.getItemID() == null) {
			/*Document cDocument;
			try {
				VFSPath vp = new VFSPath(BASE_URL + category.getCollectionAccessURI() + "/index.xml");
				cDocument = DocumentUtils.getReadOnlyDocument(vp, vfs);
			} catch (IOException e) {
				cDocument = null;
			}*/
			// FIXME, if you're going to pass the document, you may was well
			// pass the object, will
			// probably be faster since its alredy parsed anyway.
			
			returnResource = new GoGoEgoCollectionRepresentation(
				new InternalRequest(request, Method.GET, "riap://host" + path), 
				context, category.toXML()
			);
			attributes.put(Constants.VFS_URI, categoryData.getVFSPath());
			attributes.put(IS_COLLECTION, Boolean.TRUE);
			
			cache(path.toString(), categoryData, returnResource, attributes);
		} else {
			//final String itemID = categoryData.getItemID();
			final VFSPath uri = new VFSPath(BASE_URL + 
				category.getCollectionAccessURI() + "/" + 
				categoryData.getItemID() + ".xml");
			
			Document itemDocument;
			try {
				itemDocument = DocumentUtils.getReadOnlyDocument(uri, vfs);
			} catch (IOException e) {
				itemDocument = null;
			}

			if (itemDocument == null)
				return null;

			// FIXME, if you're going to pass the document, you may was well
			// pass the object, will
			// probably be faster since its alredy parsed anyway.
			final GenericItem item = new GenericItem(itemDocument);
			
			returnResource = new GoGoEgoItemRepresentation(new InternalRequest(request, Method.GET, "riap://host" + path), 
					context, new VFSPath(category.getCollectionAccessURI() + "/" + item.getItemID()), item);
			
			attributes.put(Constants.VFS_URI, uri);			
			
			cache(path.toString(), categoryData, item, returnResource, attributes);
		}

		return returnResource;
	}
	
	private void cache(String path, CategoryData categoryData, Representation entity, Map<String, Object> attributes) {
		cache(path, new CollectionCacheContent(categoryData), entity, attributes);
	}
	
	private void cache(String path, CategoryData categoryData, GenericItem item, Representation entity, Map<String, Object> attributes) {
		cache(path, new CollectionCacheContent(categoryData, item), entity, attributes);
	}
	
	private void cache(String path, CollectionCacheContent cacheContent, Representation entity, Map<String, Object> attributes) {	
		cacheContent.setMediaType(entity.getMediaType());
		cacheContent.setModificationDate(entity.getModificationDate());
		cacheContent.setSize(entity.getSize());
		cacheContent.setTag(entity.getTag());
		cacheContent.setResponseAttributes(attributes);
		
		CollectionCache.getInstance().cache(context, path, cacheContent);
	}

	public boolean deleteItemFromSubcollection(CategoryData categoryData) {
		final GenericCollection category = (GenericCollection) categoryData.getCategory();

		GoGoEgoItem removedItem = category.removeItem(categoryData.getItemID());
		final String uriToRemove = BASE_URL + category.getCollectionAccessURI() + "/" + categoryData.getItemID()
				+ ".xml";

		boolean status = (removedItem instanceof GenericItem ? vfsWrapper.delete(uriToRemove) : true)
				&& vfsWrapper.writeCollection(category);
		
		if (status) {
			CollectionCache.getInstance().invalidate(context, category.getCollectionAccessURI() + "/" + categoryData.getItemID());
			CollectionCache.getInstance().invalidate(context, category.getCollectionAccessURI());
		}
		return status;
	}

	public boolean deleteSubcollectionFromCollection(CategoryData categoryData) {
		final GenericCollection category = (GenericCollection) categoryData.getCategory();

		if (category.getParent() == null)
			return false;
		else {
			category.getParent().getSubCollections().remove(category.getCollectionID());
			CollectionCache.getInstance().invalidate(context, category.getCollectionAccessURI());
			CollectionCache.getInstance().invalidate(context, category.getParent().getCollectionAccessURI());
			return (vfsWrapper.delete(BASE_URL + category.getCollectionAccessURI()) && vfsWrapper
					.writeCollection(category.getParent()));
		}
	}

	public Status createReferenceItemInCollection(Document document, CategoryData categoryData) {
		return createReferenceItemInCollection(new ReferenceItem(document), categoryData);
	}

	public Status createReferenceItemInCollection(ReferenceItem item, CategoryData categoryData) {
		final GenericCollection parent = (GenericCollection) categoryData.getCategory();
		if (parent.getParent() == null)
			return Status.CLIENT_ERROR_METHOD_NOT_ALLOWED;

		parent.addItem(item);

		if (vfsWrapper.writeCollection(parent)) {
			CollectionCache.getInstance().invalidate(context, parent.getCollectionAccessURI());
			return Status.SUCCESS_OK;
		} else
			return Status.SERVER_ERROR_INTERNAL;
	}

	public Status createItemInCollection(Document document, CategoryData categoryData, boolean isNew) {
		return createItemInCollection(new GenericItem(document), categoryData, isNew);
	}

	public Status createItemInCollection(GenericItem item, CategoryData categoryData, boolean isNew) {
		final GenericCollection parent = (GenericCollection) categoryData.getCategory();
		if (parent.getParent() == null)
			return Status.CLIENT_ERROR_METHOD_NOT_ALLOWED;

		if (isNew) {
			if (parent.getSubCollections().containsKey(item.getItemID())
					|| parent.getItems().containsKey(item.getItemID()))
				return Status.CLIENT_ERROR_CONFLICT;
		}

		// Add item should do a HashMap PUT over top of the old item.
		parent.addItem(item);

		if (vfsWrapper.writeItem(item, parent.getCollectionAccessURI()) && vfsWrapper.writeCollection(parent)) {
			CollectionCache.getInstance().invalidate(context, parent.getCollectionAccessURI());
			CollectionCache.getInstance().invalidate(context, parent.getCollectionAccessURI() + "/" + item.getItemID());
			if (isNew)
				return Status.SUCCESS_CREATED;
			else
				return Status.SUCCESS_OK;
		} else {
			if (isNew)
				parent.removeItem(item.getItemID());
			return Status.SERVER_ERROR_INTERNAL;
		}
	}

	public Status createSubcollectionInCollection(CategoryData categoryData, Document document) {
		final GenericCollection parent = (GenericCollection) categoryData.getCategory();

		final GenericCollection newCategory = new GenericCollection(document);

		if (parent.getSubCollections().containsKey(newCategory.getCollectionID())
				|| parent.getItems().containsKey(newCategory.getCollectionID()))
			return Status.CLIENT_ERROR_CONFLICT;
		else {
			parent.addSubCollection(newCategory);

			if (vfsWrapper.writeCollection(parent) && vfsWrapper.writeCollection(newCategory)) {
				CollectionCache.getInstance().invalidate(context, parent.getCollectionAccessURI());
				CollectionCache.getInstance().invalidate(context, newCategory.getCollectionAccessURI());
				return Status.SUCCESS_CREATED;
			}
			else {
				parent.removeSubCollection(newCategory.getCollectionID());
				return Status.SERVER_ERROR_INTERNAL;
			}
		}
	}

	public boolean updateItem(Document document, CategoryData categoryData) {
		return DocumentUtils.impl.getAttribute(document.getDocumentElement(), "id").equals(categoryData.getItemID())
				&& createItemInCollection(document, categoryData, false).isSuccess();
	}

	public boolean updateCollection(Document document, GenericCollection category) {
		if (!category.getCollectionID().equals(DocumentUtils.impl.getAttribute(document.getDocumentElement(), "id")))
			return false;

		category.convertFromXMLDocument(document);

		boolean status = vfsWrapper.writeCollection(category)
				&& (category.getParent() == null ? true : vfsWrapper.writeCollection(category.getParent()));
		if (status) {
			CollectionCache.getInstance().invalidate(context, category.getCollectionAccessURI());
			CollectionCache.getInstance().invalidate(context, category.getParent().getCollectionAccessURI());
		}
		return status;
	}

	public Representation handlePropfind(CategoryData categoryData) {
		Document document = DocumentUtils.impl.newDocument();
		Element root = document.createElement("collection");

		GenericCollection category = (GenericCollection) categoryData.getCategory();
		root.setAttribute("type", category.getCollectionType() == null ? "" : category.getCollectionType());

		Iterator<GoGoEgoCollection> iterator = category.getSubCollections().values().iterator();
		while (iterator.hasNext()) {
			GenericCollection current = (GenericCollection) iterator.next();
			String uri = BASE_URL + category.getCollectionAccessURI() + "/" + current.getCollectionID() + "/"
					+ Constants.COLLECTION_ROOT_FILENAME;
			try {
				current = new GenericCollection(DocumentUtils.getReadOnlyDocument(new VFSPath(uri), vfs));
			} catch (IOException unlikely) {
				continue;
			}
			current.setParent(category);

			Element node = document.createElement("subcollection");
			node.setAttribute("id", current.getCollectionID());
			node.setAttribute("name", current.getName());
			node.setAttribute("uri", current.getCollectionAccessURI());
			node.setAttribute("numItems", current.getItems().size() + "");
			node.setAttribute("numCollections", current.getSubCollections().size() + "");
			if (current.getCollectionType() != null)
				node.setAttribute("type", current.getCollectionType());
			else
				node.setAttribute("type", "");

			try {
				VFSPath uriPath = VFSUtils.parseVFSPath(uri);
				node.setAttribute("modified", Long.toString(vfs.getLastModified(uriPath)));
			} catch (Exception e) {
				TrivialExceptionHandler.ignore(this, e);
			}

			root.appendChild(node);
		}

		Iterator<String> itemIter = category.getItems().keySet().iterator();
		while (itemIter.hasNext()) {
			String key = itemIter.next();
			GoGoEgoItem current = category.getItems().get(key);
			Element node = document.createElement("item");

			if (current == null)
				continue;
			else if (current instanceof GenericItem) {
				String uri = BASE_URL + category.getCollectionAccessURI() + "/" + current.getItemID() + ".xml";

				node.setAttribute("id", current.getItemID());
				node.setAttribute("name", current.getItemName());
				node.setAttribute("uri", category.getCollectionAccessURI() + "/" + current.getItemID());
				try {
					VFSPath uriPath = VFSUtils.parseVFSPath(uri);
					node.setAttribute("modified", Long.toString(vfs.getLastModified(uriPath)));
					node.setAttribute("size", vfs.getLength(uriPath) + "");
				} catch (Exception e) {
					TrivialExceptionHandler.ignore(this, e);
				}
			} else {
				String uri = ((ReferenceItem) current).getItemURI();

				FoundItem fi = findItem(new VFSPath(uri), 0);
				if (fi == null)
					continue;
				GoGoEgoItem item = fi.item;

				node.setAttribute("referenceID", key);
				node.setAttribute("id", item.getItemID());
				node.setAttribute("name", current.getItemName());
				node.setAttribute("uri", fi.uri.toString());

				String fileURI = BASE_URL + fi.uri + ".xml";
				try {
					VFSPath uriPath = VFSUtils.parseVFSPath(fileURI);
					node.setAttribute("modified", Long.toString(vfs.getLastModified(uriPath)));
					node.setAttribute("size", vfs.getLength(uriPath) + "");
				} catch (Exception e) {
					TrivialExceptionHandler.ignore(this, e);
				}
			}

			root.appendChild(node);
		}

		document.appendChild(root);

		return new DomRepresentation(MediaType.TEXT_XML, document);
	}

	private static class FoundItem {
		private GoGoEgoItem item;
		private VFSPath uri;

		public FoundItem(GoGoEgoItem item, VFSPath uri) {
			this.item = item;
			this.uri = uri;
		}
	}

	public boolean moveCategory(CategoryData from, CategoryData to, boolean isMove) throws IOException {
		if (from == null || to == null)
			return false;

		if (to.getCategory().getSubCollections().containsKey(from.getCategory().getCollectionID()))
			return false;

		final VFSPath fromPath = new VFSPath(BASE_URL + from.getCategory().getCollectionAccessURI());
		final VFSPath destination = new VFSPath(BASE_URL + to.getCategory().getCollectionAccessURI())
				.child(new VFSPathToken(from.getCategory().getCollectionID()));

		// Nothing to do?
		if (fromPath.equals(destination))
			return true;

		GoGoEgo.debug("fine").println(
				(isMove ? "Moving " : "Copying ") + "all files from {0} to {1}", fromPath, destination);

		vfs.copy(fromPath, destination);

		if (!isMove || deleteSubcollectionFromCollection(from)) {
			to.getCategory().addSubCollection(from.getCategory());
			boolean success = vfsWrapper.writeCollection(to.getCategory())
					&& (!isMove || vfsWrapper.writeCollection(from.getCategory()));
			if (!success)
				to.getCategory().removeSubCollection(from.getCategory().getCollectionID());
			
			if (success) {
				CollectionCache.getInstance().invalidate(context, from.getCategory().getCollectionAccessURI());
				CollectionCache.getInstance().invalidate(context, from.getCategory().getParent().getCollectionAccessURI());
				CollectionCache.getInstance().invalidate(context, to.getCategory().getCollectionAccessURI());
			}
			
			return success;
		} else
			return false;
	}

	public boolean moveItem(CategoryData categoryData, String destination) {
		boolean success = false;

		final GoGoEgoItem item;
		final String itemID = categoryData.getItemID();
		if (itemID.startsWith("$RI$"))
			item = categoryData.getCategory().getItems().get(itemID);
		else {
			final String itemURI = BASE_URL + categoryData.getCategory().getCollectionAccessURI() + "/" + itemID
					+ ".xml";
			Document itemDocument;
			try {
				itemDocument = DocumentUtils.getReadOnlyDocument(new VFSPath(itemURI), vfs);
			} catch (IOException e) {
				itemDocument = null;
			}

			if (itemDocument == null)
				return false;
			item = new GenericItem(itemDocument);
		}

		CategoryData newParent = getCurrentCategory(new VFSPath(destination));

		return (newParent != null && deleteItemFromSubcollection(categoryData)) ? ((item instanceof GenericItem) ? createItemInCollection(
				(GenericItem) item, newParent, true).isSuccess()
				: createReferenceItemInCollection((ReferenceItem) item, newParent).isSuccess())
				: success;
	}
	
	private FoundItem findItem(VFSPath uri, final int recursionKillCount) {
		if (recursionKillCount >= 10)
			return null;

		CategoryData data = getCurrentCategory(uri);
		GoGoEgoCollection c = data.getCategory();
		if (c == null || data.getItemID() == null)
			return null;

		GoGoEgoItem item = c.getItems().get(data.getItemID());
		if (item instanceof ReferenceItem)
			return findItem(new VFSPath(((ReferenceItem) item).getItemURI()), recursionKillCount + 1);
		else
			return new FoundItem(item, uri);
	}

	public static class VFSWrapper {
		private final VFS vfs;

		public VFSWrapper(final VFS vfs) {
			this.vfs = vfs;
		}

		public boolean delete(final String fullURI) {
			try {
				vfs.delete(VFSUtils.parseVFSPath(fullURI));
				return true;
			} catch (final Exception e) {
				return false;
			}
		}

		public boolean writeCollection(final GoGoEgoCollection category) {
			Document xml;
			try {
				xml = createDocumentFromString(category.toXML());
			} catch (Throwable e) {
				GoGoEgo.debug("error").println("Error Writing Collection: {0}\r\n{1}", e.getMessage(), e);
				GoGoEgo.debug("error").println("Bad XML: {0}", category.toXML());
				return false;
			}
			
			return DocumentUtils.writeVFSFile(BASE_URL + category.getCollectionAccessURI() + "/"
					+ Constants.COLLECTION_ROOT_FILENAME, vfs, xml);
		}

		public boolean writeItem(GenericItem item, String collectionURI) {
			Document xml;
			try {
				xml = createDocumentFromString(item.toXML());
			} catch (Throwable e) {
				GoGoEgo.debug("error").println("Error Writing Collection: {0}\r\n{1}", e.getMessage(), e);
				GoGoEgo.debug("fine").println("Bad XML: {0}", item.toXML());
				return false;
			}
			return DocumentUtils.writeVFSFile(BASE_URL + collectionURI + "/" + item.getItemID() + ".xml", vfs,
					xml);
		}
		
		private Document createDocumentFromString(final String xml) throws Exception {
			final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			final DocumentBuilder builder = factory.newDocumentBuilder();
			return builder.parse(new InputSource(new StringReader(xml)));
		}
	}
}
