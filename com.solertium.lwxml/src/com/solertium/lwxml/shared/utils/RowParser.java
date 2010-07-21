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
package com.solertium.lwxml.shared.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.NativeElement;
import com.solertium.lwxml.shared.NativeNode;
import com.solertium.lwxml.shared.NativeNodeList;

/**
 * RowParser.java
 * 
 * Parses standard search results back from a request. Format is:
 * 
 * <root> <row> <field name="${key}">${value}</field> ... </row> ... </root>
 * 
 * @author carl.scott
 * 
 */
public class RowParser {

	private final ArrayList<RowData> rows;
	private final HashMap<String, QueryData> queries;

	/**
	 * Creates a new RowParser
	 * 
	 */
	public RowParser() {
		rows = new ArrayList<RowData>();
		queries = new HashMap<String, QueryData>();
	}

	/**
	 * Creates a new RowParser, and parses the document as standard search
	 * results
	 * 
	 * @param document
	 *            search results
	 */
	public RowParser(final NativeDocument document) {
		rows = new ArrayList<RowData>();
		queries = new HashMap<String, QueryData>();
		parseRows(document);
	}

	/**
	 * Get the first row of results.
	 * 
	 * @return the first row, or null if there is none
	 */
	public RowData getFirstRow() {
		try {
			return rows.get(0);
		} catch (final IndexOutOfBoundsException e) {
			return null;
		}
	}

	public int getRowCount() {
		return rows.size();
	}

	public ArrayList<RowData> getRows() {
		return rows;
	}
	
	/**
	 * Retrieves an iterator over the rows (RowData). Use this when you parse a
	 * single query.
	 * 
	 * @return
	 */
	public Iterator<RowData> iterator() {
		return rows.listIterator();
	}

	/**
	 * Retrieves a map of multiple search results. Use this when you parse
	 * multiple queries.
	 * 
	 * @return
	 */
	public HashMap<String, QueryData> getQueries() {
		return queries;
	}

	/**
	 * Pulls the attributes that you are looking for from search results them in
	 * RowData object
	 * 
	 * @param document
	 *            the doc
	 * @param attributeNames
	 *            interesting values
	 */
	public void parseForAttributes(final NativeDocument document, final ArrayList<String> attributeNames) {
		final NativeNodeList nodes = document.getDocumentElement().getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			final NativeElement current = nodes.elementAt(i);
			final RowData rowData = new RowData();
			for (int j = 0; j < attributeNames.size(); j++) {
				final String currAttr = attributeNames.get(j);
				if (current.getAttribute(currAttr) != null) {
					rowData.addField(currAttr, current.getAttribute(currAttr));
				}
			}
			rows.add(rowData);
		}
	}

	/**
	 * Parses rows specified retrieved after an insert query call
	 * 
	 * @param document
	 */
	public void parseInsertRows(final NativeDocument document) {
		final NativeNodeList nodes = document.getDocumentElement().getChildNodes();
		final RowData rowData = new RowData();
		for (int i = 0; i < nodes.getLength(); i++) {
			final NativeNode current = nodes.item(i);
			rowData.addField(current.getNodeName(), current.getTextContent());
		}
		rows.add(rowData);
	}

	public void parseMultipleInsertRows(final NativeDocument document) {
		NativeNodeList queries = document.getDocumentElement().getElementsByTagName("query");
		for (int i = 0; i < queries.getLength(); i++) {
			final NativeElement currentQuery = queries.elementAt(i);
			if (currentQuery.getNodeName().equalsIgnoreCase("query")) {
				NativeNodeList rows = currentQuery.getChildNodes();
				final QueryData queryData = new QueryData(currentQuery.getAttribute("queryID"));
				final RowData rowData = new RowData();
				for (int k = 0; k < rows.getLength(); k++) {
					final NativeNode currentRow = rows.item(k);
					rowData.addField(currentRow.getNodeName(), currentRow.getTextContent());
				}
				queryData.addRowData(rowData);
				this.queries.put(queryData.getQueryID(), queryData);
			}
		}
	}

	/**
	 * Parses rows specified retrieved after an listing
	 * 
	 * @param document
	 */
	public void parseListRows(final NativeDocument document) {
		final NativeNodeList nodes = document.getDocumentElement().getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			final NativeElement current = nodes.elementAt(i);
			final RowData rowData = new RowData();
			rowData.addField("id", current.getAttribute("id"));
			rowData.addField("name", current.getAttribute("name"));
			if (current.getAttribute("tooltip") != null) {
				rowData.addField("tooltip", current.getAttribute("tooltip"));
			}
			rows.add(rowData);
		}
	}

	/**
	 * Standard parsing of search rows as indicated previously.
	 * 
	 * @param document
	 */
	public void parseRows(final NativeDocument document) {
		final NativeNodeList rows = document.getDocumentElement().getElementsByTagName("row");
		for (int i = 0; i < rows.getLength(); i++) {
			final NativeNodeList fields = rows.elementAt(i).getElementsByTagName("field");
			final RowData rowData = new RowData();
			for (int j = 0; j < fields.getLength(); j++)
				rowData.addField(fields.elementAt(j));
			this.rows.add(rowData);
		}
	}

	/**
	 * Pulls the first two values from teh serach results of each row and labels
	 * them as VALUE and NAME, in that order
	 * 
	 * @param document
	 */
	public void parseAmbiguousSearchRows(NativeDocument document) {
		NativeNodeList rows = document.getDocumentElement().getElementsByTagName("row");
		for (int i = 0; i < rows.getLength(); i++) {
			NativeNode currentRow = rows.item(i);
			if (currentRow.getNodeName().equalsIgnoreCase("row")) {
				NativeNodeList fields = currentRow.getChildNodes();
				RowData rowData = new RowData();
				String name = null, value = null;
				for (int j = 0; j < fields.getLength(); j++) {
					NativeElement currentField = fields.elementAt(j);
					if (currentField.getNodeName().equalsIgnoreCase("field")) {
						if (value == null) {
							value = currentField.getFirstChild().getNodeValue();
						} else if (name == null) {
							name = currentField.getFirstChild().getNodeValue();
						} else
							break;
					}
				}
				if (name != null && value != null) {
					rowData.addField(RowData.AMBIG_VALUE, value);
					rowData.addField(RowData.AMBIG_NAME, name);
				}
				this.rows.add(rowData);
			}
		}
	}

	/**
	 * Parses results for multiple search rows, adding them to the queries map,
	 * does not add anything to the rows collection
	 * 
	 * @param document
	 */
	public void parseMultipleSearchRows(NativeDocument document) {
		NativeNodeList queries = document.getDocumentElement().getElementsByTagName("query");
		for (int i = 0; i < queries.getLength(); i++) {
			final NativeElement currentQuery = queries.elementAt(i);
			if (currentQuery.getNodeName().equalsIgnoreCase("query")) {
				NativeNodeList rows = currentQuery.getChildNodes();
				QueryData queryData = new QueryData(currentQuery.getAttribute("queryID"));
				for (int k = 0; k < rows.getLength(); k++) {
					final NativeNode currentRow = rows.item(k);
					if (currentRow.getNodeName().equalsIgnoreCase("row")) {
						final NativeNodeList fields = currentRow.getChildNodes();
						final RowData rowData = new RowData();
						for (int j = 0; j < fields.getLength(); j++) {
							final NativeElement currentField = fields.elementAt(j);
							if (currentField.getNodeName().equalsIgnoreCase("field")) {
								rowData.addField(currentField);
							}
						}
						queryData.addRowData(rowData);
					}
				}
				this.queries.put(queryData.getQueryID(), queryData);
			}

		}
	}
}
