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

package com.solertium.db;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.solertium.util.Replacer;

/**
 * @author <a href="mailto:rob.heittman@solertium.com">Rob Heittman</a>, <a
 *         href="http://www.solertium.com">Solertium Corporation</a>
 */
public class CDateTime extends BaseColumn {

	Date d;

	public CDateTime() {
	}

	public Column newInstance(){
		return new CDateTime();
	}

	public CDateTime(final String localName, final Date d) {
		setLocalName(localName);
		if (d != null)
			try {
				setObject(d);
			} catch (final ConversionException unpossible) {
				// we know we are passing in the right kind of value
			}
	}

	public Date getDate() throws ConversionException {
		return new Date(d.getTime());
	}

	public Double getDouble() throws ConversionException {
		throw new ConversionException(
				"CDateTime column cannot be represented as a double-precision number");
	}

	public Integer getInteger() throws ConversionException {
		throw new ConversionException(
				"CDateTime column cannot be represented as an integer");
	}

	public Literal getLiteral() {
		if (d == null)
			return new StringLiteral(null);
		return new StringLiteral(new SimpleDateFormat(
				SQLDateHelper.sqlDateTimeFormat).format(d));
	}

	public Long getLong() throws ConversionException {
		if (d == null)
			return null;
		return (d.getTime());
	}

	public Object getObject() {
		if (d == null)
			return null;
		return new Date(d.getTime());
	}

	public Class<?> getRequiredObjectType() {
		return String.class;
	}

	public String getString() throws ConversionException {
		if (d == null)
			return null;
		return new SimpleDateFormat(SQLDateHelper.sqlDateTimeFormat).format(d);
	}

	public boolean isEmpty() {
		if (d == null)
			return true;
		return false;
	}

	public Object parseString(final String s) throws ConversionException {
		if(s==null) return null;
		String ns = Replacer.stripWhitespace(s);
		if("".equals(ns)) return null;
		try {
			return (new SimpleDateFormat(SQLDateHelper.sqlDateTimeFormat))
					.parse(ns);
		} catch (final ParseException e) {
			throw new ConversionException(e);
		}
	}

	public void setObject(final Object o) throws ConversionException {
		if (o == null) {
			d = null;
			return;
		}
		if (o instanceof Date)
			d = (Date) o;
		else
			throw new ConversionException(
					"CDateTime column must be loaded with Date object");
	}

}
