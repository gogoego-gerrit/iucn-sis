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
package com.solertium.db.restlet;

import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;

import com.solertium.db.DBException;
import com.solertium.db.query.SelectQuery;

public class DumpResource extends DBResource {
	
	private final String table;

	public DumpResource(Context context, Request request, Response response) {
		super(context, request, response);
		
		table = request.getResourceRef().getLastSegment();

		getVariants().add(new Variant(MediaType.TEXT_XML));
	}
	
	public Representation represent(Variant variant) throws ResourceException {
		boolean found = false;
		try {
			found = !ec.getMatchingTables(table).isEmpty();
		} catch (DBException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
		
		if (!found)
			throw new ResourceException(Status.CLIENT_ERROR_PRECONDITION_FAILED, "Table " + table + " not found in database.");
		
		final SelectQuery query = new SelectQuery();
		query.select(table, "*");
		
		return getRowsAsRepresentation(query);
	}

}
