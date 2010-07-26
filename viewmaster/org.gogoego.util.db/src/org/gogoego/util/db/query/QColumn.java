/*
 * Copyright (C) 2004-2005 Cluestream Ventures, LLC
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

package org.gogoego.util.db.query;

import org.gogoego.util.db.shared.CanonicalColumnName;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author <a href="mailto:rob.heittman@solertium.com">Rob Heittman</a>, <a
 *         href="http://www.solertium.com">Solertium Corporation</a>
 */
public class QColumn {

	private Boolean header = null;

	private CanonicalColumnName name = null;
	private Boolean outer = null;
	private Boolean range = null;
	private String sort = null;
	private Boolean writable = null;
	private String label = null;

	public QColumn() {
	}

	public Boolean getHeader() {
		return header;
	}

	public CanonicalColumnName getName() {
		return name;
	}

	public String getLabel() {
		return label;
	}

	public Boolean getOuter() {
		return outer;
	}

	public Boolean getRange() {
		return range;
	}

	public String getSort() {
		return sort;
	}

	public Boolean getWritable() {
		return writable;
	}

	public void loadConfig(final Element config) {
		try {
			name = new CanonicalColumnName(config.getTextContent());
		} catch (final Exception poorly_handled) {
			name = null;
		}
		sort = config.getAttribute("sort");
		if ("".equals(sort))
			sort = null;
		final String h = config.getAttribute("header");
		if ((h != null) && (!"".equals(h)))
			header = Boolean.parseBoolean(h);
		label = config.getAttribute("label");
		if ("".equals(label))
			label = null;
		final String r = config.getAttribute("range");
		if ((r != null) && (!"".equals(r)))
			range = Boolean.parseBoolean(r);
		final String o = config.getAttribute("outer");
		if ((o != null) && (!"".equals(o)))
			outer = Boolean.parseBoolean(o);
		final String w = config.getAttribute("writable");
		if ((w != null) && (!"".equals(w)))
			writable = Boolean.parseBoolean(w);
	}

	public Element saveConfig(final Document doc) {
		final Element el = doc.createElement("select");
		el.appendChild(doc.createTextNode(name.toString()));
		if (sort != null)
			el.setAttribute("sort", sort);
		if (label != null)
			el.setAttribute("label", label);
		if (header != null)
			el.setAttribute("header", header.toString());
		if (range != null)
			el.setAttribute("range", range.toString());
		if (outer != null)
			el.setAttribute("outer", outer.toString());
		if (writable != null)
			el.setAttribute("writable", writable.toString());
		return el;
	}

	public void setHeader(final Boolean header) {
		this.header = header;
	}

	public void setLabel(final String label) {
		this.label = label;
	}

	public void setName(final CanonicalColumnName name) {
		this.name = name;
	}

	public void setOuter(final Boolean outer) {
		this.outer = outer;
	}

	public void setRange(final Boolean range) {
		this.range = range;
	}

	public void setSort(final String sort) {
		this.sort = sort;
	}

	public void setWritable(final Boolean writable) {
		this.writable = writable;
	}

}
