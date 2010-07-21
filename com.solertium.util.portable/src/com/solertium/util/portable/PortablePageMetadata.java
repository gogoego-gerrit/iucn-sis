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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class PortablePageMetadata extends PortableBaseTagListener {

	private String body = null;
	private Map<String, String> meta = new HashMap<String, String>();
	private String template = null;
	private PortableTagFilter tf = null;
	private String title = null;
	private StringBuffer titlewriter = null;

	/** Creates an uninitialized PageMetadata object */
	public PortablePageMetadata() {
	}

	/** Loads page level metadata from the given reader. */
	public PortablePageMetadata(final String fr) {
		init(fr);
	}

	public String getBody() {
		return body;
	}

	public Map<String, String> getMeta() {
		return meta;
	}

	public String getTemplate() {
		return template == null ? "" : template;
	}

	public String getTitle() {
		return title == null ? "" : title;
	}

	private void init(final String fr) {
		// now try to read from specified file
		try {
			titlewriter = new StringBuffer(512);
			tf = new PortableTagFilter(fr, titlewriter);
			tf.shortCircuitClosingTags = false;
			tf.registerListener(this);
			tf.stopWritingBeforeTag();
			tf.parse();
			titlewriter = null;
			tf = null;
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	public Iterator<String> interestingTagNames() {
		final List<String> l = new ArrayList<String>();
		l.add("meta");
		l.add("title");
		l.add("/title");
		l.add("body");
		l.add("/head");
		return l.iterator();
	}

	public void process(final PortableTagFilter.Tag t) {
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
		if (key == null || value == null)
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

	public void setBody(final String body) {
		this.body = body;
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

}
