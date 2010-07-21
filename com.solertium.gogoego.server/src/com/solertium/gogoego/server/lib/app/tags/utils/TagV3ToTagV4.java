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
package com.solertium.gogoego.server.lib.app.tags.utils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;

import org.gogoego.api.utils.DocumentUtils;
import org.w3c.dom.Document;

import com.solertium.db.CInteger;
import com.solertium.db.CString;
import com.solertium.db.DBException;
import com.solertium.db.ExecutionContext;
import com.solertium.db.Row;
import com.solertium.db.query.InsertQuery;
import com.solertium.db.query.SelectQuery;
import com.solertium.gogoego.server.lib.app.tags.container.TagApplication;
import com.solertium.util.TrivialExceptionHandler;

/**
 * TagV3ToTagV4.java
 * 
 * Need to change group tags, group directories, and directory rules, and group
 * views to groupkeys, gropkeygroups, and grouprules.
 * 
 * Extract: - fetch data
 * 
 * Create: - add in new data
 * 
 * Remove: - get rid of old tables
 * 
 * 
 * @author carl.scott
 * 
 */
public class TagV3ToTagV4 {

	private final ExecutionContext ec;
	private final String siteID;
	private final boolean TEST_MODE;

	private Writer out;

	private final HashMap<String, Integer> idCount = new HashMap<String, Integer>();

	private final ArrayList<Row> groupKey_Rows = new ArrayList<Row>();
	private final ArrayList<Row> groupKeyGroup_Rows = new ArrayList<Row>();

	public TagV3ToTagV4(ExecutionContext ec, String siteID, boolean testmode) {
		this.ec = ec;
		this.siteID = siteID;
		this.TEST_MODE = testmode;

		this.out = new BufferedWriter(new PrintWriter(System.out));
	}

	public void setWriter(Writer out) {
		this.out = out;
	}

	public void run() throws DBException {
		initIDCount();
		if (!TEST_MODE)
			remove(new String[] { "groupkeys", "groupkeygroups" });
		extract();
		if (!TEST_MODE)
			remove(new String[] { "directoryrules", "groupdirectories", "groupviews" });
		create();
	}

	public void initIDCount() {
		idCount.put("groupkeys", Integer.valueOf(1));
		idCount.put("groupkeygroups", Integer.valueOf(1));
		idCount.put("groupkeyrules", Integer.valueOf(1));
	}

	public void extract() throws DBException {
		// ec.setStructure(getOldXML());

		// write(DocumentUtils.impl.serializeDocumentToString(ec.
		// analyzeExistingStructure()));

		{
			final String table = convertTable("groupdirectories");
			final SelectQuery query = new SelectQuery();
			query.select(table, "uri");
			query.select(table, "groupid");

			final Row.Set rs = new Row.Set();

			try {
				ec.doQuery(query, rs);
				final HashMap<String, Integer> used = new HashMap<String, Integer>();
				for (Row current : rs.getSet()) {
					Integer keyID = used.get(current.get("uri").toString());
					if (keyID == null) {
						Row row = new Row();
						row.add(new CInteger("id", keyID = getID("groupkeys")));
						row.add(new CString("key", current.get("uri").toString()));
						row.add(new CString("protocol", "uri"));
						used.put(current.get("uri").toString(), keyID);
						groupKey_Rows.add(row);
					}
					{
						Row row = new Row();
						row.add(new CInteger("id", getID("groupkeygroups")));
						row.add(new CInteger("keyid", keyID));
						row.add(new CInteger("groupid", current.get("groupid").getInteger()));
						groupKeyGroup_Rows.add(row);
					}
				}
			} catch (DBException e) {
				write("Extract failure: " + e.getMessage());
				throw e;
			}
		}
		{
			final String table = convertTable("groupviews");
			final SelectQuery query = new SelectQuery();
			query.select(table, "viewid");
			query.select(table, "groupid");

			final Row.Set rs = new Row.Set();

			try {
				ec.doQuery(query, rs);
				final HashMap<String, Integer> used = new HashMap<String, Integer>();
				for (Row current : rs.getSet()) {
					Integer keyID = used.get(current.get("viewid").toString());
					if (keyID == null) {
						Row row = new Row();
						row.add(new CInteger("id", keyID = getID("groupkeys")));
						row.add(new CString("key", current.get("viewid").toString()));
						row.add(new CString("protocol", "view"));
						used.put(current.get("viewid").toString(), keyID);
						groupKey_Rows.add(row);
					}
					{
						Row row = new Row();
						row.add(new CInteger("id", getID("groupkeygroups")));
						row.add(new CInteger("keyid", keyID));
						row.add(new CInteger("groupid", current.get("groupid").getInteger()));
						groupKeyGroup_Rows.add(row);
					}
				}
			} catch (DBException e) {
				write("Extract failure: " + e.getMessage());
				throw e;
			}
		}
	}

	public void create() throws DBException {
		ec.createStructure(TagApplication.getStructureDocument(siteID));
		for (Row row : groupKey_Rows) {
			final InsertQuery query = new InsertQuery();
			query.setTable(convertTable("groupkeys"));
			query.setRow(row);
			try {
				if (TEST_MODE)
					write(query.getSQL(ec.getDBSession()));
				else
					ec.doUpdate(query);
			} catch (DBException e) {
				write("Create Failure: " + e.getMessage());
			}
		}

		for (Row row : groupKeyGroup_Rows) {
			final InsertQuery query = new InsertQuery();
			query.setTable(convertTable("groupkeygroups"));
			query.setRow(row);
			try {
				if (TEST_MODE)
					write(query.getSQL(ec.getDBSession()));
				else
					ec.doUpdate(query);
			} catch (DBException e) {
				write("Failure: " + e.getMessage());
			}
		}
	}

	private void write(String output) {
		try {
			out.write(output + "<br/>");
		} catch (IOException e) {
			TrivialExceptionHandler.ignore(this, e);
		}
	}

	public void remove(String[] tables) {
		for (String table : tables) {
			try {
				ec.doUpdate("DROP TABLE " + convertTable(table));
			} catch (DBException e) {
				write("Remove failure: " + e.getMessage());
				// If it fails, I really don't care.
			}
		}
	}

	private String convertTable(String table) {
		return siteID + "_" + table;
	}

	private Integer getID(String table) {
		Integer cur = idCount.get(table);
		Integer next = Integer.valueOf(cur.intValue() + 1);
		idCount.put(table, next);
		return cur;
	}

	@SuppressWarnings("unused")
	private Document getOldXML() {
		return DocumentUtils.impl
				.createDocumentFromString("<structure>"
						+ "<table name=\"${siteID}_groupdirectories\">"
						+ "<column name=\"id\" type=\"CInteger\" key=\"true\" />"
						+ "<column name=\"uri\" type=\"CString\" scale=\"4000\" />"
						+ "<column name=\"groupid\" type=\"CInteger\" relatedTable=\"${siteID}_groups\" relatedColumn=\"id\" />"
						+ "</table>"
						+ "<table name=\"${siteID}_directoryrules\">"
						+ "	<column name=\"id\" type=\"CInteger\" key=\"true\" />"
						+ "	<column name=\"uri\" type=\"CString\" scale=\"4000\" />"
						+ "<column name=\"cascade\" type=\"CString\" scale=\"255\" />		"
						+ "</table>"
						+ "<table name=\"${siteID}_groupviews\">"
						+ "	<column name=\"id\" type=\"CInteger\" key=\"true\" />"
						+ "	<column name=\"viewid\" type=\"CString\" scale=\"4000\" />"
						+ "	<column name=\"groupid\" type=\"CInteger\" relatedTable=\"${siteID}_groups\" relatedColumn=\"id\" />"
						+ "</table></structure>");
	}
}
