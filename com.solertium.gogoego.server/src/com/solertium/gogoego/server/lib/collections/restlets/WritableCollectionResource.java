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
package com.solertium.gogoego.server.lib.collections.restlets;

import org.gogoego.api.collections.CategoryData;
import org.gogoego.api.collections.Constants;
import org.gogoego.api.collections.GenericCollection;
import org.gogoego.api.collections.GoGoEgoItem;
import org.gogoego.api.collections.ReferenceItem;
import org.gogoego.api.utils.DocumentUtils;
import org.gogoego.api.utils.MagicDisablingFilter;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.solertium.util.restlet.RestletUtils;
import com.solertium.vfs.VFSMetadata;
import com.solertium.vfs.VFSPath;
import com.solertium.vfs.restlet.VFSResource;
import com.solertium.vfs.utils.VFSUtils;

/**
 * WritableCollectionResource.java
 * 
 * Performs PUT, POST, DELETE, MOVE, and COPY operations on collections, in
 * addition to standard GET and PROPFIND
 * 
 * @author carl.scott
 * 
 */
public class WritableCollectionResource extends CollectionResource {

	public WritableCollectionResource(final Context context, final Request request, final Response response) {
		super(context, request, response);
		setModifiable(true);
	}

	/**
	 * The implementation now gets in the way of reference items :(
	 */
	public void handleDelete() {
		removeRepresentations();
	}

	public void removeRepresentations() {
		if (categoryData == null) {
			getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			return;
		}

		if ((categoryData.getItemID() == null) ? builder.deleteSubcollectionFromCollection(categoryData) : builder
				.deleteItemFromSubcollection(categoryData)) {
			getResponse().setStatus(Status.SUCCESS_OK);
		} else {
			getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, "Could not delete " + categoryData.getItemID() + ".");
		}
	}
	
	public void handlePut() {
		try {
			storeRepresentation(getRequest().getEntity());
		} catch (ResourceException e) {
			getResponse().setStatus(e.getStatus());
		}
	}

	public void storeRepresentation(Representation entity) throws ResourceException {
		if (categoryData == null) {
			getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			return;
		}
		
		if ("metadata".equals(queryMediaType)) {
			try {
				handlePutrssmetadata();
			} catch (ResourceException e) {
				getResponse().setStatus(e.getStatus());
			}
			return;
		}
		
		Document document;
		try {
			document = new DomRepresentation(entity).getDocument();
		} catch (Exception e) {
			getResponse().setStatus(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY);
			return;
		}

		GenericCollection parentCategory = (GenericCollection) categoryData.getCategory();

		final Node head = document.getFirstChild();
		if (head.getNodeName().equalsIgnoreCase("collection")) {
			String collectionID = DocumentUtils.impl.getAttribute(head, "id");
			if (!collectionID.equals(parentCategory.getCollectionID())) {
				getResponse().setStatus(builder.createSubcollectionInCollection(categoryData, document));
			} else {
				getRequest().getAttributes().put(MagicDisablingFilter.MAGIC_DISABLING_KEY, Boolean.TRUE);
				getResponse().setStatus(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED, "Use POST to update a collection");
			}
		} else if (head.getNodeName().equalsIgnoreCase("item")
				&& !DocumentUtils.impl.getAttribute(head, "id").equals("")) {
			if (categoryData.getItemID() != null) {
				getRequest().getAttributes().put(MagicDisablingFilter.MAGIC_DISABLING_KEY, Boolean.TRUE);
				getResponse().setStatus(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED, "Use POST to update an item");
			} else {
				getResponse().setStatus(builder.createItemInCollection(document, categoryData, true));
			}
		} else if (head.getNodeName().equalsIgnoreCase("referenceItem")) {
			if (categoryData.getItemID() != null) {
				getRequest().getAttributes().put(MagicDisablingFilter.MAGIC_DISABLING_KEY, Boolean.TRUE);
				getResponse().setStatus(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED, "Use POST to update an item");
			} else {
				getResponse().setStatus(builder.createReferenceItemInCollection(document, categoryData));
			}
		} else {
			getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			getRequest().getAttributes().put(MagicDisablingFilter.MAGIC_DISABLING_KEY, Boolean.TRUE);
		}
	}

	public void acceptRepresentation(Representation entity) {
		if (categoryData == null) {
			getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			return;
		}
		Document document;
		try {
			document = new DomRepresentation(entity).getDocument();
		} catch (Exception e) {
			getResponse().setStatus(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY);
			return;
		}

		GenericCollection parentCategory = (GenericCollection) categoryData.getCategory();

		boolean success = false;
		final Node head = document.getFirstChild();
		if (head.getNodeName().equalsIgnoreCase("collection")) {
			String collectionID = DocumentUtils.impl.getAttribute(head, "id");
			if (collectionID.equals(parentCategory.getCollectionID())) {
				success = builder.updateCollection(document, parentCategory);
			}
		} else if (head.getNodeName().equalsIgnoreCase("item")
				&& !DocumentUtils.impl.getAttribute(head, "id").equals("")) {
			if (categoryData.getItemID() != null) {
				success = builder.updateItem(document, categoryData);
			}
		}

		if (success) {
			getResponse().setStatus(Status.SUCCESS_OK);
		} else {
			getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			getRequest().getAttributes().put(MagicDisablingFilter.MAGIC_DISABLING_KEY, Boolean.TRUE);
		}

	}

	public boolean allowCopy() {
		return true;
	}

	public void handleCopy() {
		if (categoryData == null) {
			getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			return;
		}

		if (categoryData.getItemID() == null) {
			String destination = RestletUtils.getHeader(getRequest(), "Destination");
			try {
				performCategoryMove(destination, false);
				getResponse().setStatus(Status.SUCCESS_CREATED);
			} catch (ResourceException e) {
				getResponse().setStatus(e.getStatus());
			}
		} else {
			String newItemID = RestletUtils.getHeader(getRequest(), "newItemID");
			if (newItemID == null) {
				String destination = RestletUtils.getHeader(getRequest(), "Destination");
				String referenceItemID = categoryData.getItemID();
				GoGoEgoItem item = categoryData.getCategory().getItems().get(referenceItemID);
				if (item != null && item instanceof ReferenceItem) {
					if (builder.handleReferenceItemCopy(getRequest(), categoryData, (ReferenceItem) item, destination))
						getResponse().setStatus(Status.SUCCESS_CREATED);
					else
						getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
				} else
					getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			} else if (categoryData.getCategory().getItems().containsKey(newItemID))
				getResponse().setStatus(Status.CLIENT_ERROR_CONFLICT);
			else {
				getResponse().setStatus(
						builder.handleCopy(categoryData, getRequest(), newItemID) ? Status.SUCCESS_CREATED
								: Status.SERVER_ERROR_INTERNAL);
			}

		}

		getRequest().getAttributes().put(MagicDisablingFilter.MAGIC_DISABLING_KEY, Boolean.TRUE);
	}

	public boolean allowMove() {
		return true;
	}

	public void handleMove() {
		if (categoryData == null) {
			getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			return;
		}

		String destination = RestletUtils.getHeader(getRequest(), "Destination");

		if (destination == null) {
			getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Request not sent with appropriate headers.");
			return;
		}

		if (categoryData.getItemID() != null) {
			if (builder.moveItem(categoryData, destination))
				getResponse().setStatus(Status.SUCCESS_OK);
			else
				getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
		} else {
			try {
				performCategoryMove(destination, true);
				getResponse().setStatus(Status.SUCCESS_OK);
			} catch (ResourceException e) {
				getResponse().setStatus(e.getStatus());
			}
		}
	}

	private void performCategoryMove(final String destination, boolean isMove) throws ResourceException {
		CategoryData to;
		try {
			to = builder.getCurrentCategory(VFSResource.decodeVFSPath(destination));
		} catch (VFSUtils.VFSPathParseException e) {
			throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);
		}
		try {
			getResponse().setStatus(
					(to != null && builder.moveCategory(categoryData, to, isMove)) ? Status.SUCCESS_OK
							: Status.CLIENT_ERROR_BAD_REQUEST);
		} catch (Exception e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
		}
	}

	public void handlePutrssmetadata() throws ResourceException {
		if (categoryData == null || categoryData.getItemID() != null)
			throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);

		getRequest().getAttributes().put(MagicDisablingFilter.MAGIC_DISABLING_KEY, Boolean.TRUE);

		Document document;
		try {
			document = new DomRepresentation(getRequest().getEntity()).getDocument();
		} catch (Exception e) {
			throw new ResourceException(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY);
		}

		String rss = "";
		NodeList nodes = document.getDocumentElement().getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node current = nodes.item(i);
			if (current.getNodeType() != Node.TEXT_NODE) {
				rss += current.getNodeName() + "=" + current.getTextContent() + ";";
			}
		}

		if (rss.length() > 0)
			rss = rss.substring(0, rss.length() - 1);

		VFSPath path = new VFSPath("/(SYSTEM)" + uri + "/" + Constants.COLLECTION_ROOT_FILENAME);
		VFSMetadata md = vfs.getMetadata(path);
		md.addArbitraryData("rss", rss);

		try {
			vfs.setMetadata(path, md);
			getResponse().setStatus(Status.SUCCESS_CREATED);
		} catch (Exception e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
		}
	}
}
