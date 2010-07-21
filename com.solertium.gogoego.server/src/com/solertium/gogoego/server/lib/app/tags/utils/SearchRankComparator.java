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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;

import com.solertium.util.AlphanumericComparator;

/**
 * SearchRankComparator.java
 * 
 * Ranks TagData by the number of times it matches the desired tags. If the tag
 * match count is equal, it sorts on the URI.
 * 
 * Note that with the tag match count, the natural ordering is reverse order
 * (tagdata with the most matches should appear first).
 * 
 * @author carl.scott
 * 
 */
public class SearchRankComparator implements Comparator<TagData>, Serializable {

	private static final long serialVersionUID = 1L;
	private final ArrayList<String> matches;

	public SearchRankComparator() {
		this(new ArrayList<String>());
	}

	public SearchRankComparator(final ArrayList<String> matches) {
		this.matches = matches;
	}

	public int compare(final TagData l, final TagData r) {
		int lCount = 0, rCount = 0;
		for (String tag : matches) {
			if (l.containsTag(tag))
				lCount++;
			if (r.containsTag(tag))
				rCount++;
		}
		return lCount == rCount ? new AlphanumericComparator().compare(l.getUri(), r.getUri()) : (lCount > rCount ? -1
				: 1);
	}

}
