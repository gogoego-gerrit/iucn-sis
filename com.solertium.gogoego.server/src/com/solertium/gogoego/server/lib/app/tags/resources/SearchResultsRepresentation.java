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
package com.solertium.gogoego.server.lib.app.tags.resources;

import java.util.ArrayList;
import java.util.HashMap;

import org.gogoego.api.representations.GoGoEgoDomRepresentation;
import org.restlet.data.MediaType;
import org.w3c.dom.Document;

import com.solertium.gogoego.server.lib.app.tags.utils.SearchRowParser;
import com.solertium.gogoego.server.lib.app.tags.utils.TagData;

/**
 * GoGoSearchResultsResource.java
 * 
 * An extension of a tagged resource element, this object can be used when
 * templating results from a tag search or tag browse. The methods are
 * simplified to allow the user to only access necessary content, and the
 * content that is provided is the entire raw XML as returned from a search or
 * browse.
 * 
 * @author carl.scott
 * 
 */
public class SearchResultsRepresentation extends GoGoEgoDomRepresentation {

	private final ArrayList<TagData> tagData;

	/**
	 * Contructor
	 * 
	 * @param request
	 * @param reference
	 * @param content
	 * @param context
	 */
	public SearchResultsRepresentation(Document document) {
		this(MediaType.TEXT_XML, document);
	}
	
	public SearchResultsRepresentation(MediaType mediaType, Document document) {
		super(mediaType, document);
		tagData = new ArrayList<TagData>();
		parse();
	}

	private void parse() {
		HashMap<String, TagData> uriToTagData = SearchRowParser.parse(getDocument());
		tagData.addAll(uriToTagData.values());
	}

	public int getResultCount() {
		return tagData.size();
	}

	public TagData getResult(int index) {
		try {
			return tagData.get(index);
		} catch (IndexOutOfBoundsException e) {
			return null;
		}
	}

	public String getResultUri(int index) {
		try {
			return tagData.get(index).getUri();
		} catch (IndexOutOfBoundsException e) {
			return null;
		}
	}

	public String getResultTags(int index) {
		try {
			return tagData.get(index).getTags();
		} catch (IndexOutOfBoundsException e) {
			return null;
		}
	}

}
