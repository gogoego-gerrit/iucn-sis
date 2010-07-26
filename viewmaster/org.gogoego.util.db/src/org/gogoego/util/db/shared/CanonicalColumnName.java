/*
 * Copyright (C) 2004-2005 Cluestream Ventures, LLC
 * Copyright (C) 2006-2009 Solertium Corporation
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

package org.gogoego.util.db.shared;

import java.io.Serializable;


/**
 * @author <a href="mailto:rob.heittman@solertium.com">Rob Heittman</a>, <a
 *         href="http://www.solertium.com">Solertium Corporation</a>
 */
public class CanonicalColumnName implements Serializable {

	private final String field;
	private final String table;
	private static final long serialVersionUID = 1L;

	public CanonicalColumnName(final String formatted)
			throws CanonicalFormatException {
		if (formatted == null) {
			table = null;
			field = null;
			return; // fine, just create a null
		}
		// CanonicalColumnName
		if ("*".equals(formatted)) {
			table = null;
			field = "*";
			return;
		}
		final int i = formatted.indexOf(".");
		if (i == -1) {
			table = null;
			field = null;
			throw new CanonicalFormatException("Column name " + formatted
					+ " does not contain '.'");
		} else if (i == 1) {
			table = null;
			field = null;
			throw new CanonicalFormatException("Column name " + formatted
					+ " contains no table specifier");
		} else if (i == formatted.length()) {
			table = null;
			field = null;
			throw new CanonicalFormatException("Column name " + formatted
					+ " contains no field specifier");
		}
		table = formatted.substring(0, i);
		field = formatted.substring(i + 1);
	}

	public CanonicalColumnName(final String table, final String field) {
		this.table = table;
		this.field = field;
	}

	public String getField() {
		return field;
	}

	public String getTable() {
		return table;
	}

	@Override
	public String toString() {
		if (table == null)
			return field;
		return table + "." + field;
	}

}
