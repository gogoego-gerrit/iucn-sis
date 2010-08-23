/*
 * Copyright (C) 2007-2009 Solertium Corporation
 *
 * This file is part of the open source GoGoEgo project.
 *
 * GoGoEgo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *  
 * GoGoEgo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GoGoEgo.  If not, see http://www.gnu.org/licenses/.
 * 
 * Unless you have been granted a different license in writing by the
 * copyright holders for GoGoEgo, only the GNU General Public License
 * grants you rights to modify or redistribute this code.
 */
package com.solertium.gogoego.server.lib.app.tags.utils;

import com.solertium.db.CanonicalColumnName;
import com.solertium.db.DBException;
import com.solertium.db.ExecutionContext;
import com.solertium.db.Row;
import com.solertium.db.StaticRowID;
import com.solertium.db.query.InsertQuery;
import com.solertium.db.query.QConstraint;
import com.solertium.db.query.SelectQuery;

/**
 * TagDatabaseUtils.java
 * 
 * Utility functions that help with writing and reading tag information from the
 * database in a standard way.
 * 
 * @author carl.scott
 * 
 */
public class TagDatabaseUtils {

	public static void addTag(String siteID, String tbl, String uriID, ExecutionContext ec, String tag)
			throws DBException {
		Row row = ec.getRow(tbl);

		row.get("uriid").setString(uriID);
		row.get("tag").setObject(tag);
		row.get("id").setString("" + getRowID(siteID).get(ec, tbl));

		InsertQuery q = new InsertQuery();
		q.setRow(row);
		q.setTable(tbl);

		ec.doUpdate(q);
	}

	/**
	 * Fetches the ID for a given URI from the database, or creates one if
	 * necessary.
	 * 
	 * @param siteID
	 *            the site ID
	 * @param tbl
	 *            the table to look in
	 * @param uri
	 *            the uri to look for
	 * @param ec
	 *            the execution context
	 * @return the ID, possibly newly created.
	 */
	public static String getURIID(String siteID, String tbl, String uri, ExecutionContext ec) {
		SelectQuery query = new SelectQuery();
		query.select(tbl, "id");
		query.constrain(new CanonicalColumnName(tbl, "uri"), QConstraint.CT_EQUALS, uri);

		try {
			Row.Loader rl = new Row.Loader();
			ec.doQuery(query, rl);

			if (rl.getRow() == null) {
				String rowID = getRowID(siteID).get(ec, tbl) + "";
				Row row = ec.getRow(tbl);
				row.get("id").setString(rowID);
				row.get("uri").setString(uri);
				row.get("datatype").setString("resource");

				InsertQuery q = new InsertQuery();
				q.setRow(row);
				q.setTable(tbl);

				ec.doUpdate(q);

				return rowID;
			} else
				return rl.getRow().get("id").toString();
		} catch (Exception e) {
			return null;
		}
	}

	public static StaticRowID getRowID(String siteID) {
		return StaticRowID.getInstance(new CanonicalColumnName(siteID + "_IDCOUNT", "id"), new CanonicalColumnName(
				siteID + "_IDCOUNT", "tbl"));
	}

}
