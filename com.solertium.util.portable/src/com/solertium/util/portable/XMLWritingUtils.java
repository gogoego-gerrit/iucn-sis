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
package com.solertium.util.portable;

/**
 * XMLWritingUtils.java
 * 
 * Utilities for writing XML documents as strings.
 *
 * @author carl.scott
 *
 */
public class XMLWritingUtils {
	
	/**
	 * Write a basic XML tag, no attributes, just contents.  It will 
	 * write blank strings when contents are null instead of "null".
	 * Tag name should never be null.
	 * @param tagName
	 * @param contents
	 * @return the string
	 */
	public static String writeTag(String tagName, String contents) {
		return writeTag(tagName, contents, false);
	}
	
	/**
	 * Write this tag only if the contents are not null when conditional is true
	 * @param tagName
	 * @param contents
	 * @param conditional true if conditional, false otherwise
	 * @return
	 */
	public static String writeTag(String tagName, String contents, boolean conditional) {
		return conditional && isNull(contents) ? "" : "<" + tagName + ">" + 
			(isNull(contents) ? "" : contents) + "</" + tagName + ">";
	}
	
	public static String writeCDATATag(String tagName, String contents) {
		return writeCDATATag(tagName, contents, false);
	}
	
	public static String writeCDATATag(String tagName, String contents, boolean conditional) {
		return conditional && isNull(contents) ? "" : "<" + tagName + ">" + 
			(isNull(contents) ? "" : "<![CDATA[" + contents + "]]>") + 
			"</" + tagName + ">";
	}
	
	private static boolean isNull(String contents) {
		return contents == null || contents.equals("");
	}
	
	/**
	 * Determine if a value matches any of the values 
	 * specified in the given list of test values.  
	 * Returns true if one is equal, false otherwise. 
	 * Does not except null values, will throw NPE. 
	 * @param value
	 * @param tests
	 * @return
	 */
	public static boolean matches(String value, String... tests) {
		for (String test : tests)
			if (value.equals(test))
				return true;
		return false;
	}

}
