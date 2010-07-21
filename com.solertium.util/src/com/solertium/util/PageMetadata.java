/*
 * Copyright (C) 2000-2005 Cluestream Ventures, LLC
 * Copyright (C) 2006-2009 Solertium Corporation
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

package com.solertium.util;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Collects interesting metadata from the HEAD element of an HTML file.
 * 
 * @author <a href="mailto:rob.heittman@solertium.com">Rob Heittman</a>, <a
 *         href="http://www.solertium.com">Solertium Corporation</a>
 */
public class PageMetadata extends BaseTagListener {

	private Map<String, String> meta = new HashMap<String, String>();
	private String template = null;
	private TagFilter tf = null;
	private String title = null;
	private StringWriter titlewriter = null;
	private String body = null;

	/** Creates an uninitialized PageMetadata object */
	public PageMetadata() {
	}
	
	/**
	 * Creates a PageMetadata object from a given string, wrapping it
	 * in a string reader.
	 * @param string the string 
	 * @throws IOException
	 */
	public PageMetadata(String string) throws IOException {
		this(new StringReader(string));
	}

	/** Loads page level metadata from the given reader. */
	public PageMetadata(final Reader fr) throws IOException {
		parse(fr);
	}

	public Map<String, String> getMeta() {
		return meta;
	}

	public String getTemplate() {
		return template;
	}

	public String getTitle() {
		return title;
	}
	
	public String getBody() {
		return body;
	}

	public List<String> interestingTagNames() {
		final List<String> l = new ArrayList<String>();
		l.add("meta");
		l.add("title");
		l.add("/title");
		l.add("body");
		l.add("/head");
		return l;
	}

	private void parse(final Reader fr) throws IOException {
		titlewriter = new StringWriter(512);
		tf = new TagFilter(fr, titlewriter);
		tf.shortCircuitClosingTags = false;
		tf.registerListener(this);
		tf.stopWritingBeforeTag();
		tf.parse();
	}

	public void process(final TagFilter.Tag t) throws IOException {
		if ("meta".equals(t.name)) {
			final String metaname = t.getAttribute("name");
			final String metavalue = t.getAttribute("content");
			put(metaname, metavalue);
		}
		if ("title".equals(t.name))
			tf.startWritingAfterTag();
		if ("/title".equals(t.name)) {
			tf.stopWritingBeforeTag();
			setTitle(titlewriter.toString());
		}
		if ("/head".equals(t.name))
			// ignore everything after the HEAD tag
			tf.stopParsing();
		if ("body".equals(t.name))
			// body started, same as HEAD tag
			tf.stopParsing();
	}

	public void put(final String key, final String value) {
		if ((key == null) || (value == null))
			return;
		if (key.equals("title")) {
			setTitle(value);
			return;
		}
		if (key.equals("template")) {
			setTemplate(value);
			return;
		}
		meta.put(key, value);
	}

	public void setMeta(final Map<String, String> meta) {
		this.meta = meta;
	}

	public void setTemplate(final String template) {
		this.template = template;
	}

	public void setTitle(final String title) {
		this.title = title;
	}
	
	public void setBody(final String body) {
		this.body = body;
	}

}
