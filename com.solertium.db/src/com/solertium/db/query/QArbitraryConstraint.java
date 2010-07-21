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

import com.solertium.db.DBSession;

/**
 * @author <a href="mailto:rob.heittman@solertium.com">Rob Heittman</a>, <a
 *         href="http://www.solertium.com">Solertium Corporation</a>
 */
public class QArbitraryConstraint implements QConstraint {

	public String id = UUID.randomUUID().toString();

	String sql = "";

	public QArbitraryConstraint() {
	}

	public QArbitraryConstraint(final String sql) {
		this.sql = sql;
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
		return sql;
	}

	public void loadConfig(final Element config) {
		id = config.getAttribute("id");
	}

	public Element saveConfig(final Document doc) {
		final Element el = doc.createElement("constraint");
		el.setAttribute("id", getID());
		el.setAttribute("class", this.getClass().getName());
		el.appendChild(doc.createTextNode(sql));
		return el;
	}
}
