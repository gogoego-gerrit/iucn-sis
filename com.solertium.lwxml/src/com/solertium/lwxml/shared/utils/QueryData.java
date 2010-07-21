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


/**
 * QueryData.java
 * 
 * A wrapper class that represents the row results of a query, typically used
 * when there are multiple queries from one call.
 * 
 * @author carl.scott
 * 
 */
public class QueryData extends ArrayList<RowData> {
	private static final long serialVersionUID = 2L;

	private String queryID;

	public QueryData(String queryID) {
		super();
		this.queryID = queryID;
	}

	public void addRowData(RowData rowData) {
		add(rowData);
	}

	public String getQueryID() {
		return queryID;
	}

	public void setQueryID(String queryID) {
		this.queryID = queryID;
	}

}
