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
package com.solertium.lwxml.shared.utils;

import java.util.LinkedHashMap;

import com.solertium.lwxml.shared.NativeElement;

/**
 * RowData.java
 * 
 * Wrapper for a row from RowParser query. This eliminates any case-sensitivity
 * issues and does convenient casting. It also extends HashMap to give access to
 * all of hashmap's functionality.
 * 
 * Update: With GWT 1.5 updates we can now extend LinkedHashMap
 * 
 * @author carl.scott
 * 
 */
public class RowData extends LinkedHashMap<String, String> {
	private static final long serialVersionUID = 1L;

	public static final String AMBIG_NAME = "NAME";
	public static final String AMBIG_VALUE = "VALUE";

	public RowData() {
		super();
	}

	public void addField(final NativeElement element) {
		addField(element.getAttribute("name"), element.getTextContent());
	}

	public void addField(final String name, final String value) {
		put(name.toUpperCase(), value);
	}
	
	public String get(Object key) {
		if (key == null)
			return null;
		if (key instanceof String)
			return super.get(((String) key).toUpperCase());
		return super.get(key);
	}

	public String getField(final String name) {
		return get(name.toUpperCase());
	}

}
