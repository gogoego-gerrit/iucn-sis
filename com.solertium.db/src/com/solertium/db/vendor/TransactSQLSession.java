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

package com.solertium.db.vendor;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import javax.sql.DataSource;

import net.jcip.annotations.ThreadSafe;

import com.solertium.db.CBoolean;
import com.solertium.db.CDate;
import com.solertium.db.CDateTime;
import com.solertium.db.CDouble;
import com.solertium.db.CInteger;
import com.solertium.db.CLong;
import com.solertium.db.CString;
import com.solertium.db.Column;
import com.solertium.db.ConversionException;
import com.solertium.db.DBSession;
import com.solertium.db.Row;
import com.solertium.db.StringLiteral;
import com.solertium.util.Replacer;

/**
 * @author <a href="mailto:rob.heittman@solertium.com">Rob Heittman</a>, <a
 *         href="http://www.solertium.com">Solertium Corporation</a>
 */
@ThreadSafe
public class TransactSQLSession extends DBSession {

	private final DataSource ds;

	public TransactSQLSession(final String name, final DataSource ds) {
		this.ds = ds;
	}

	// disambiguate identifier from reserved words using brackets
	@Override
	public String formatIdentifier(final String identifier) {
		return "[" + identifier + "]";
	}

	@Override
	public String formatLiteral(final StringLiteral literal) {
		if (literal == null)
			return "NULL";
		String s = literal.getString();
		if (s == null)
			return "NULL";
		s = Replacer.replace(s, "'", "''");
		s = Replacer.replace(s, "\\", "\\\\");
		return "N'" + s + "'";
	}

	@Override
	protected DataSource getDataSource() {
		return ds;
	}

	@Override
	protected String getDBColumnType(CBoolean c) {
		return "INT";
	}

	@Override
	public String getDBColumnType(final CDate c) {
		return "DATETIME";
	}

	@Override
	public String getDBColumnType(final CDateTime c) {
		return "DATETIME";
	}

	@Override
	public String getDBColumnType(final CDouble c) {
		return "FLOAT(53)";
	}

	@Override
	public String getDBColumnType(final CInteger c) {
		return "INT";
	}

	@Override
	public String getDBColumnType(final CLong c) {
		return "LONG";
	}

	@Override
	public String getDBColumnType(final CString c) {
		int scale = c.getScale();
		if (scale == 0)
			scale = 2048;
		if (scale <= 4000)
			return "NVARCHAR(" + scale + ")";
		return "NTEXT";
	}

	@Override
	public Row rsToRow(final ResultSet rs, final ResultSetMetaData rsmd)
			throws SQLException {
		final Row r = new Row();
		final int columns = rsmd.getColumnCount();
		for (int i = 1; i <= columns; i++) {
			String typename = rsmd.getColumnTypeName(i);
			typename = typename.toUpperCase();
			Column c = null;
			if (typename.startsWith("VARCHAR")
					|| typename.startsWith("NVARCHAR")) {
				c = new CString();
				c.setScale(rsmd.getPrecision(i));
				try {
					c.setObject(rs.getString(i));
				} catch (final ConversionException e) {
					throw new SQLException("Conversion problem: "
							+ e.getMessage());
				}
			} else if (typename.startsWith("TEXT")
					|| typename.startsWith("NTEXT")) {
				c = new CString();
				c.setScale(65536);
				final java.sql.Clob clob = (java.sql.Clob) rs.getObject(i);
				if (clob != null) {
					if (clob.length() > Integer.MAX_VALUE)
						throw new SQLException(
								"CLOBs larger than Integer.MAX_VALUE are not supported");
					try {
						if (clob.length() < 1)
							c.setObject("");
						else
							c.setObject(clob.getSubString(1, (int) clob
									.length()));
					} catch (final ConversionException e) {
						throw new SQLException("Conversion problem: "
								+ e.getMessage());
					}
				} else
					try {
						c.setObject("");
					} catch (final ConversionException e) {
						throw new SQLException("Conversion problem: "
								+ e.getMessage());
					}
			} else if (typename.startsWith("CHAR")
					|| typename.startsWith("NCHAR")) {
				c = new CString();
				c.setScale(rsmd.getPrecision(i));
				try {
					c.setObject(Replacer.stripWhitespace(rs.getString(i)));
				} catch (final ConversionException e) {
					throw new SQLException("Conversion problem: "
							+ e.getMessage());
				}
			} else if (typename.startsWith("FLOAT")) {
				c = new CDouble();
				try {
					c.setObject(rs.getDouble(i));
				} catch (final ConversionException e) {
					throw new SQLException("Conversion problem: "
							+ e.getMessage());
				}
			} else if (typename.startsWith("INT")) {
				c = new CInteger();
				try {
					c.setObject(rs.getInt(i));
				} catch (final ConversionException e) {
					throw new SQLException("Conversion problem: "
							+ e.getMessage());
				}
			} else if (typename.startsWith("DATETIME")) {
				c = new CDateTime();
				try {
					c.setObject(rs.getTimestamp(i));
				} catch (final ConversionException e) {
					throw new SQLException("Conversion problem: "
							+ e.getMessage());
				}
			}
			if (c != null) {
				c.setLocalName(rsmd.getColumnName(i));
				r.add(c);
			}
		}
		return r;
	}
}
