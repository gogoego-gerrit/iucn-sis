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
package com.solertium.db.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.solertium.db.Column;
import com.solertium.db.Row;
import com.solertium.util.BaseDocumentUtils;

/**
 * QueryUtils.java
 *
 * @author carl.scott
 *
 */
public class QueryUtils {
	
	public static Document writeDocumentFromRowSet(Collection<? extends Row> rowSet) {
		final Document document = BaseDocumentUtils.impl.newDocument();
		final Element rootEl = document.createElement("result");
		
		Iterator<? extends Row> iterator = rowSet.iterator();
		while (iterator.hasNext()) {
			Row current = iterator.next();
			Element rowEl = document.createElement("row");
			ArrayList<Column> columns = current.getColumns();
			Iterator<Column> it = columns.iterator();
			while (it.hasNext()) {
				Column c = it.next();
				Element cEl = BaseDocumentUtils.impl.createCDATAElementWithText(document, "field", c.toString());
				cEl.setAttribute("name", c.getLocalName());
				rowEl.appendChild(cEl);
			}
			rootEl.appendChild(rowEl);
		}
		
		document.appendChild(rootEl);
		
		return document;
	}

}
