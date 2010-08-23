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
package com.solertium.gogoego.server.lib.app.tags.utils;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * TagData.java
 * 
 * Stores tag information from file or DB, containing the uri of the resource
 * and a list of tags associated with it.
 * 
 * @author carl.scott
 * 
 */
public class TagData {

	protected String uri;
	protected ArrayList<String> tags;

	public TagData(String uri) {
		this.uri = uri;
		tags = new ArrayList<String>() {
			private static final long serialVersionUID = 1L;

			public boolean add(String e) {
				return e != null && !contains(e) && super.add(e);
			}
		};
	}

	public boolean containsTag(String tag) {
		return tags.contains(tag);
	}

	public String getUri() {
		return uri;
	}

	public void addTag(String tag) {
		tags.add(tag);
	}

	public String getTags() {
		String csv = "";
		Iterator<String> iterator = tags.listIterator();
		while (iterator.hasNext())
			csv += iterator.next() + (iterator.hasNext() ? "," : "");
		return csv;
	}

	public int getNumberOfTags() {
		return tags.size();
	}

}
