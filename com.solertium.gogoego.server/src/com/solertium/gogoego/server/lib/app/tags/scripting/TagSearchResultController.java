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
package com.solertium.gogoego.server.lib.app.tags.scripting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import org.gogoego.api.scripting.ReflectingELEntity;
import org.gogoego.api.scripting.ScriptableObjectFactory;
import org.restlet.Application;
import org.restlet.data.Request;
import org.w3c.dom.Document;

import com.solertium.gogoego.server.ServerApplication;
import com.solertium.gogoego.server.lib.app.tags.resources.TagSearchEngineResource;
import com.solertium.gogoego.server.lib.app.tags.utils.SearchRankComparator;
import com.solertium.gogoego.server.lib.app.tags.utils.SearchRowParser;
import com.solertium.gogoego.server.lib.app.tags.utils.TagData;
import com.solertium.util.restlet.ScratchResource;

/**
 * TagSearchResultController.java
 * 
 * Scripting object with the ability to peruse the results of a 
 * search based on tags.
 * 
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a
 *  href="http://www.solertium.com">Solertium Corporation</a>
 */
public class TagSearchResultController implements ScriptableObjectFactory {

	public static class Worker extends ReflectingELEntity {

		private ArrayList<TagData> tagData;
		private ArrayList<String> queryTags, queryURIs;
		private Request request;
		private String key;

		public Worker(Request request) {
			this.request = request;
			this.tagData = new ArrayList<TagData>();
			this.queryTags = new ArrayList<String>();
			this.queryURIs = new ArrayList<String>();
			this.key = request.getResourceRef().getQueryAsForm().getFirstValue("key");
		}

		public void setKey(String key) {
			this.key = key;
			this.tagData.clear();
			this.queryTags.clear();
			this.queryURIs.clear();
		}

		/**
		 * returns the number of results that matched all of the tags queried
		 * for
		 * 
		 * @return
		 */
		public int getNumberOfResultsMatchedAll() {
			int result = 0;
			int numberOfTags = queryTags.size();
			for (TagData tag : tagData) {
				if (tag.getNumberOfTags() == numberOfTags) {
					result++;
				}
			}
			return result;
		}

		/**
		 * returns the number of results that matched some (but not all) of the
		 * tags queried for
		 * 
		 * @return
		 */
		public int getNumberOfResultsMatchedSome() {
			int result = 0;
			int numberOfTags = queryTags.size();
			for (TagData tag : tagData) {
				if (tag.getNumberOfTags() != numberOfTags) {
					result++;
				}
			}
			return result;
		}

		public String getQueryParameters(String name) {
			return request.getResourceRef().getQueryAsForm().getValues(name);
		}

		public String load() {
			Document document = key != null ? getDocument() : null;
			if (document == null)
				return "false";

			try {
				tagData = new ArrayList<TagData>(SearchRowParser.parse(document).values());
				queryTags = SearchRowParser.parseQueryTags(document);
				queryURIs = SearchRowParser.parseQueryURIs(document);
			} catch (NullPointerException e) {
				e.printStackTrace();
				return "false";
			}

			return "true";
		}

		public int getResultCount() {
			return tagData.size();
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

		public void sort() {
			Collections.sort(tagData, new SearchRankComparator(queryTags));
		}

		public void sortReverse() {
			Collections.sort(tagData, new SearchRankComparator(queryTags));
			Collections.reverse(tagData);
		}

		public String getQueriedTags() {
			return listToCSV(queryTags);
		}

		public String getQueriedURIs() {
			return listToCSV(queryURIs);
		}

		private Document getDocument() {
			ScratchResource resource = ServerApplication.
				getFromContext(Application.getCurrent().getContext()).
				getScratchResourceBin().get(TagSearchEngineResource.SEARCH_STORAGE_URI_BASE + key);

			if (resource == null)
				return null;

			try {
				return (resource != null && resource.getExpires().after(new Date())) ? (Document) resource
						.getResource() : null;
			} catch (NullPointerException e) {
				return (Document) resource.getResource();
			}
		}

	}

	public Object getScriptableObject(Request request) {
		return new Worker(request);
	}

}
