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
package com.solertium.gogoego.server.lib.app.tags.resources;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.gogoego.api.utils.DocumentUtils;
import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.Resource;

import com.solertium.db.CanonicalColumnName;
import com.solertium.db.ExecutionContext;
import com.solertium.db.Row;
import com.solertium.db.query.QComparisonConstraint;
import com.solertium.db.query.QConstraint;
import com.solertium.db.query.QConstraintGroup;
import com.solertium.db.query.SelectQuery;
import com.solertium.db.utils.QueryUtils;
import com.solertium.gogoego.server.ServerApplication;
import com.solertium.gogoego.server.lib.app.tags.container.TagApplication;
import com.solertium.util.TrivialExceptionHandler;

/**
 * DumpResource.java
 * 
 * Dumps the entire contents for a given table, or, if no table is specified,
 * provides the (intended) database structure.
 * 
 * @author carl.scott
 * 
 */
public class DumpResource extends Resource {

	private final ExecutionContext ec;
	private final String tableName;
	private final String siteID;

	public DumpResource(Context context, Request request, Response response) {
		super(context, request, response);

		ec = ((TagApplication)ServerApplication.getFromContext(context, TagApplication.REGISTRATION)).getExecutionContext();
		siteID = ((TagApplication)ServerApplication.getFromContext(context, TagApplication.REGISTRATION)).getSiteID();
		tableName = (String) request.getAttributes().get("tableName");

		getVariants().add(new Variant(MediaType.TEXT_XML));
	}

	public Representation represent(Variant variant) {
		if (tableName == null) {
			try {
				return new DomRepresentation(variant.getMediaType(), TagApplication.getStructureDocument(siteID));
			} catch (Exception e) {
				e.printStackTrace();
				return new DomRepresentation(variant.getMediaType(), DocumentUtils.impl.createErrorDocument(e
						.getMessage()));
			}
		} else {
			String nameToUse = tableName.startsWith(siteID + "_") ? tableName : siteID + "_" + tableName;

			SelectQuery query = new SelectQuery();

			try {
				Row row = ec.getRow(nameToUse);

				HashMap<String, String> qS = new HashMap<String, String>();
				String selectStr = "*";

				Form form = getRequest().getResourceRef().getQueryAsForm();
				Iterator<String> it = form.getNames().iterator();
				while (it.hasNext()) {
					String cur = it.next();
					if (cur.equals("select"))
						selectStr = form.getValues(cur);
					else if (row.get(cur) != null)
						qS.put(cur, form.getValues(cur));
				}

				boolean hasOne = false;
				String[] selSplit = selectStr.split(",");
				for (int i = 0; i < selSplit.length; i++) {
					if (row.get(selSplit[i]) != null) {
						query.select(nameToUse, selSplit[i]);
						hasOne = true;
					}
				}

				if (!hasOne)
					query.select(nameToUse, "*");

				Iterator<Map.Entry<String, String>> entries = qS.entrySet().iterator();
				while (entries.hasNext()) {
					Map.Entry<String, String> cur = entries.next();
					String[] split = cur.getValue().split(",");
					if (split.length == 1) {
						query.constrain(new CanonicalColumnName(nameToUse, cur.getKey()), QConstraint.CT_EQUALS, cur
								.getValue());
					} else {
						QConstraintGroup g = new QConstraintGroup();
						g.addConstraint(new QComparisonConstraint(new CanonicalColumnName(nameToUse, cur.getKey()),
								QConstraint.CT_EQUALS, split[0]));
						for (int i = 1; i < split.length; i++) {
							g.addConstraint(new QComparisonConstraint(new CanonicalColumnName(nameToUse, cur.getKey()),
									QConstraint.CT_EQUALS, split[i]));
						}
						query.constrain(QConstraint.CG_OR, g);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				TrivialExceptionHandler.ignore(this, e);
			}

			Row.Set rs = new Row.Set();

			try {
				//GoGoDebug.get("debug").println(query.getSQL(ec.getDBSession()));
				ec.doQuery(query, rs);
			} catch (Exception e) {
				e.printStackTrace();
				return new DomRepresentation(variant.getMediaType(), DocumentUtils.impl.createErrorDocument(e
						.getMessage()));
			}

			return new DomRepresentation(variant.getMediaType(), QueryUtils.writeDocumentFromRowSet(rs.getSet()));
		}
	}

}
