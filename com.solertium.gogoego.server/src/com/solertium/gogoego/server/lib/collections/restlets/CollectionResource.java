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

package com.solertium.gogoego.server.lib.collections.restlets;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.gogoego.api.collections.CategoryData;
import org.gogoego.api.collections.CollectionResourceBuilder;
import org.gogoego.api.collections.Constants;
import org.gogoego.api.collections.GenericCollection;
import org.gogoego.api.collections.GenericItem;
import org.gogoego.api.collections.GoGoEgoCollection;
import org.gogoego.api.collections.GoGoEgoItem;
import org.gogoego.api.collections.SimpleRSSBuilder;
import org.gogoego.api.representations.GoGoEgoBaseRepresentation;
import org.gogoego.api.representations.GoGoEgoDomRepresentation;
import org.gogoego.api.utils.DocumentUtils;
import org.gogoego.api.utils.MagicDisablingFilter;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.Resource;
import org.restlet.resource.ResourceException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.solertium.gogoego.server.ServerApplication;
import com.solertium.util.TrivialExceptionHandler;
import com.solertium.vfs.VFS;
import com.solertium.vfs.VFSMetadata;
import com.solertium.vfs.VFSPath;
import com.solertium.vfs.VFSPathToken;
import com.solertium.vfs.restlet.VFSResource;
import com.solertium.vfs.utils.VFSUtils;

/**
 * CollectionResource.java
 * 
 * Handle read-only requests for collections
 * 
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a href="http://www.solertium.com">Solertium Corporation</a>
 *
 */
public class CollectionResource extends Resource {
	
	private final VFSPath ROOT;

	protected CollectionResourceBuilder builder;

	protected CategoryData categoryData;
	protected final VFSPath uri;

	protected final VFS vfs;
	protected final String queryMediaType;

	public CollectionResource(final Context context, final Request request, final Response response) {
		super(context, request, response);
		setModifiable(false);

		vfs = ServerApplication.getFromContext(context).getVFS();
		ROOT = getRoot();
		queryMediaType = getRequest().getResourceRef().getQueryAsForm().getFirstValue("mediaType");

		String internal_uri = null;
		try {
			internal_uri = VFSResource.decodeVFSPath(request.getResourceRef().getRemainingPart()).toString();
		} catch (VFSUtils.VFSPathParseException e) {
			TrivialExceptionHandler.ignore(this, e);
			uri = null;
			builder = null;
			categoryData = null;
			return;
		}

		if (internal_uri.equals(VFSPath.ROOT.toString()))
			internal_uri = ROOT.toString();
		else
			internal_uri = ROOT + internal_uri;
		if (internal_uri.endsWith(".rss")) {
			internal_uri = internal_uri.substring(0, internal_uri.indexOf(".rss"));
			getVariants().add(new Variant(MediaType.APPLICATION_RSS));
		}
		uri = new VFSPath(internal_uri);

		builder = getBuilder(context);

		if ((categoryData = builder.getCurrentCategory(uri, new GenericCollection(builder.getHeadDocument()))) != null) {
			if ("text".equals(queryMediaType)) {
				getVariants().add(new Variant(MediaType.TEXT_PLAIN));
			} else if ("rss".equals(queryMediaType)) {
				getVariants().add(new Variant(MediaType.APPLICATION_RSS));
			} 
		} 
			
		if (getVariants().isEmpty())
			getVariants().add(new Variant(MediaType.TEXT_XML));
	}
	
	protected CollectionResourceBuilder getBuilder(Context context) {
		return new CollectionResourceBuilder(context);
	}
	
	protected VFSPath getRoot() {
		return new VFSPath("/collections");
	}
	
	public Representation represent(Variant variant) throws ResourceException {
		return doRepresent(variant);
	}

	public GoGoEgoBaseRepresentation doRepresent(final Variant variant) throws ResourceException {
		if (categoryData == null)
			throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);		

		if (variant.getMediaType().equals(MediaType.APPLICATION_RSS))
			return representRSS(categoryData);
		
		if ("metadata".equals(queryMediaType))
			return handleGetrssmetadata();

		final GoGoEgoBaseRepresentation rep = builder.handleGet(categoryData, getRequest(), getResponse(), variant);

		if (rep == null)
			getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND);
		else
			getResponse().setStatus(Status.SUCCESS_OK);

		if (categoryData.getItemID() == null)
			getResponse().getAttributes().put(CollectionResourceBuilder.IS_COLLECTION, Boolean.TRUE);

		String mt = getRequest().getResourceRef().getQueryAsForm().getFirstValue("mediaType");
		if (mt != null && mt.equals("xml"))
			getRequest().getAttributes().put(MagicDisablingFilter.MAGIC_DISABLING_KEY, Boolean.TRUE);

		return rep;
	}

	private GoGoEgoBaseRepresentation representRSS(CategoryData categoryData) {
		final VFSPath path;
		try {
			path = VFSUtils.parseVFSPath("/(SYSTEM)" + uri);
		} catch (Exception e) {
			getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			return null;
		}

		if (categoryData.getItemID() != null
				|| vfs.getMetadata(path.child(new VFSPathToken(Constants.COLLECTION_ROOT_FILENAME))).getArbitraryData()
						.get("rss") == null) {
			getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			return null;
		}

		String rss = vfs.getMetadata(path.child(new VFSPathToken(Constants.COLLECTION_ROOT_FILENAME)))
				.getArbitraryData().get("rss");

		GoGoEgoCollection category = categoryData.getCategory();

		String cType = categoryData.getCategory().getCollectionType();
		if (cType == null)
			return null;

		String[] params;
		try {
			params = rss.split(";");
		} catch (NullPointerException impossible) {
			TrivialExceptionHandler.impossible(this, impossible);
			return null;
		}
		for (int i = 0; i < params.length; i++) {
			if (params[i].startsWith("content")) {
				try {
					if (!cType.equals(params[i].split("=")[1])) {
						getResponse().setStatus(Status.CLIENT_ERROR_EXPECTATION_FAILED);
						return null;
					}
				} catch (IndexOutOfBoundsException e) {
					getResponse().setStatus(Status.CLIENT_ERROR_EXPECTATION_FAILED);
					return null;
				}
				break;
			}
		}

		HashMap<String, String> data = new HashMap<String, String>();
		for (int i = 0; i < params.length; i++)
			data.put(params[i].split("=")[0], params[i].split("=")[1]);

		final CollectionRSSBuilder rssBuilder = new CollectionRSSBuilder(getRequest(), uri, category, vfs, path);
		final Iterator<Map.Entry<String, String>> iter = data.entrySet().iterator();
		while (iter.hasNext()) {
			final Map.Entry<String, String> current = iter.next();
			rssBuilder.setField(current.getKey(), current.getValue());
		}

		return new GoGoEgoDomRepresentation(MediaType.APPLICATION_RSS, rssBuilder.build());
	}

	public boolean allowPropfind() {
		return true;
	}

	public void handlePropfind() {
		if (categoryData == null) {
			getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			return;
		}

		getRequest().getAttributes().put(MagicDisablingFilter.MAGIC_DISABLING_KEY, Boolean.TRUE);
		getResponse().setStatus(Status.SUCCESS_OK);
		getResponse().setEntity(builder.handlePropfind(categoryData));

	}

	public boolean allowGetrssmetadata() {
		return true;
	}

	public GoGoEgoBaseRepresentation handleGetrssmetadata() throws ResourceException {
		if (categoryData == null || categoryData.getItemID() != null)
			throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);

		getRequest().getAttributes().put(MagicDisablingFilter.MAGIC_DISABLING_KEY, Boolean.TRUE);

		VFSPath path = new VFSPath("/(SYSTEM)" + uri + "/" + Constants.COLLECTION_ROOT_FILENAME);
		VFSMetadata md = vfs.getMetadata(path);

		String rss = md.getArbitraryData().get("rss");
		if (rss == null) {
			throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);
		} else {
			String cType = categoryData.getCategory().getCollectionType();

			String[] params = rss.split(";");
			for (int i = 0; i < params.length; i++) {
				if (params[i].startsWith("content")) {
					try {
						if (!cType.equals(params[i].split("=")[1]))
							throw new ResourceException(Status.CLIENT_ERROR_EXPECTATION_FAILED);
					} catch (IndexOutOfBoundsException e) {
						throw new ResourceException(Status.CLIENT_ERROR_EXPECTATION_FAILED);
					}
					break;
				}
			}

			Document doc = DocumentUtils.impl.newDocument();

			Element root = doc.createElement("root");

			for (int i = 0; i < params.length; i++)
				root.appendChild(DocumentUtils.impl.createElementWithText(doc, params[i].split("=")[0], params[i]
						.split("=")[1]));

			doc.appendChild(root);

			return new GoGoEgoDomRepresentation(doc);
		}
	}

	private static final class CollectionRSSBuilder extends SimpleRSSBuilder {

		private final VFS vfs;
		private final VFSPath path;

		public CollectionRSSBuilder(final Request request, final VFSPath relativeURI, final GoGoEgoCollection category,
				final VFS vfs, final VFSPath lookupPath) {
			super(request, relativeURI, category);
			this.vfs = vfs;
			this.path = lookupPath;
		}

		protected String getItemLink(GoGoEgoItem item) {
			return hostIdentifier + relativeAccessURI.child(new VFSPathToken(item.getItemID()));
		}

		protected String getItemData(String key, GoGoEgoItem item) {
			if ("template".equals(key))
				return data.get(key);

			String dataKey = data.get(key);
			if (dataKey == null)
				return null;
			else if (dataKey.equals("itemName"))
				return item.getItemName();
			else if (dataKey.equals("itemID"))
				return item.getItemID();
			else
				return ((GenericItem) item).getData(dataKey);
		}

		protected GoGoEgoItem fetchAndBuildItem(GoGoEgoItem item) {
			GenericItem current = (GenericItem) item;
			Document itemDoc;
			try {
				itemDoc = DocumentUtils.getReadOnlyDocument(path.child(new VFSPathToken(current.getItemID() + ".xml")),
						vfs);
			} catch (IOException e) {
				itemDoc = null;
			}
			if (itemDoc == null)
				return null;
			else {
				current.convertFromXMLDocument(itemDoc);
				return current;
			}
		}
	}
}