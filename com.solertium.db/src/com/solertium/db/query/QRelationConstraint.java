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

package com.solertium.db.query;

import java.util.UUID;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.solertium.db.CanonicalColumnName;
import com.solertium.db.DBSession;
import com.solertium.util.TrivialExceptionHandler;

/**
 * @author <a href="mailto:rob.heittman@solertium.com">Rob Heittman</a>, <a
 *         href="http://www.solertium.com">Solertium Corporation</a>
 */
public class QRelationConstraint implements QConstraint {

	protected int comparisonType;
	
	protected CanonicalColumnName f_tfspec;
	public String id = UUID.randomUUID().toString();

	protected CanonicalColumnName t_tfspec;

	public QRelationConstraint() {
	}

	public QRelationConstraint(final CanonicalColumnName f_tfspec,
			final CanonicalColumnName t_tfspec) {
		this.f_tfspec = f_tfspec;
		this.t_tfspec = t_tfspec;
		this.comparisonType = QConstraint.CT_EQUALS;
	}

	public QConstraint findByID(final String id) {
		if (id == null)
			return null;
		if (id.equals(getID()))
			return this;
		return null;
	}

	public String getID() {
		if ((id == null) || ("".equals(id)))
			id = UUID.randomUUID().toString();
		return id;
	}

	public String getSQL(final DBSession ds) {
		final StringBuffer sb = new StringBuffer(512);
		sb.append(ds.formatCanonicalColumnName(f_tfspec));
		sb.append(getComparator());
		sb.append(ds.formatCanonicalColumnName(t_tfspec));
		return sb.toString();
	}

	public void loadConfig(final Element config) {
		id = config.getAttribute("id");
		try {
			f_tfspec = new CanonicalColumnName(config
					.getAttribute("from_fieldspec"));
			t_tfspec = new CanonicalColumnName(config
					.getAttribute("to_fieldspec"));
		} catch (final Exception weaklyHandled) {
			TrivialExceptionHandler.handle(this, weaklyHandled);
		}
		try {
			comparisonType = Integer.parseInt(config.getAttribute("comparisonType"));
		} catch (Exception e) {
			comparisonType = QConstraint.CT_EQUALS;
		}
	}

	public Element saveConfig(final Document doc) {
		final Element el = doc.createElement("constraint");
		el.setAttribute("id", getID());
		el.setAttribute("class", this.getClass().getName());
		el.setAttribute("from_fieldspec", f_tfspec.toString());
		el.setAttribute("to_fieldspec", t_tfspec.toString());
		el.setAttribute("comparisonType", Integer.toString(comparisonType));
		return el;
	}
	
	public String toString() {
		return f_tfspec + " : " + t_tfspec;
	}
	
	private String getComparator() {
		if (comparisonType == CT_NOT)
			return "!=";
		if (comparisonType == CT_GT)
			return ">";
		if (comparisonType == CT_LT) 
			return "<";
		return "=";
	}
}
