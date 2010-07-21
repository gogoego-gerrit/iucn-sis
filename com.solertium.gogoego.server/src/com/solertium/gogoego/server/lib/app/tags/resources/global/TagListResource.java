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
package com.solertium.gogoego.server.lib.app.tags.resources.global;

import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;

import com.solertium.db.CanonicalColumnName;
import com.solertium.db.DBException;
import com.solertium.db.Row;
import com.solertium.db.query.QComparisonConstraint;
import com.solertium.db.query.SelectQuery;
import com.solertium.db.utils.QueryUtils;
import com.solertium.gogoego.server.lib.app.tags.resources.BaseTagResource;

/**
 * TagListResource.java
 * 
 * @author carl.scott
 * 
 */
public class TagListResource extends BaseTagResource {

	/**
	 * @param context
	 * @param request
	 * @param response
	 */
	public TagListResource(Context context, Request request, Response response) {
		super(context, request, response);
		setModifiable(false);
	}

	public Representation represent(Variant variant) throws ResourceException {
		SelectQuery query = new SelectQuery();
		if (getRequest().getAttributes().get("tagID") != null) {
			query.select(convertTable(TABLE_TAGS), "*");
			query.constrain(new QComparisonConstraint(new CanonicalColumnName(convertTable(TABLE_TAGS), "ID"),
					QComparisonConstraint.CT_EQUALS, getRequest().getAttributes().get("tagID")));
		} else {
			query.select(convertTable(TABLE_TAGS), "*");
		}
		Row.Set rs = new Row.Set();

		try {
			ec.doQuery(query, rs);
		} catch (DBException e) {
			getFailureDocument(e);
		}

		return new DomRepresentation(variant.getMediaType(), QueryUtils.writeDocumentFromRowSet(rs.getSet()));

	}

}
