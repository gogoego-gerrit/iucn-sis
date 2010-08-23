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

import java.util.Date;

import com.solertium.util.Replacer;

/**
 * @author <a href="mailto:rob.heittman@solertium.com">Rob Heittman</a>, <a
 *         href="http://www.solertium.com">Solertium Corporation</a>
 */
public class CLong extends BaseColumn {

	Long l;

	public CLong() {
	}

	public Column newInstance(){
		return new CLong();
	}

	public CLong(final String localName, final Number n) {
		setLocalName(localName);
		if (n != null)
			try {
				setObject(Long.valueOf(n.longValue()));
			} catch (final ConversionException unpossible) {
				// we know we are passing in the right kind of value
			}
	}

	public Date getDate() throws ConversionException {
		if (l == null)
			return null;
		return new Date(l);
	}

	public Double getDouble() throws ConversionException {
		if (l == null)
			return null;
		return l.doubleValue();
	}

	public Integer getInteger() throws ConversionException {
		if (l == null)
			return null;
		return l.intValue();
	}

	public Literal getLiteral() {
		return new NumericLiteral(l);
	}

	public Long getLong() throws ConversionException {
		return l;
	}

	public Object getObject() {
		return l;
	}

	public Class<?> getRequiredObjectType() {
		return Long.class;
	}

	public String getString() throws ConversionException {
		if (l == null)
			return null;
		return l.toString();
	}

	public boolean isEmpty() {
		if ((l == null) || (l == 0))
			return true;
		return false;
	}

	public Object parseString(final String s) throws ConversionException {
		if(s==null) return null;
		String ns = Replacer.stripWhitespace(s);
		if("".equals(ns)) return null;
		try {
			return Long.parseLong(ns);
		} catch (final NumberFormatException nf) {
			throw new ConversionException(nf);
		}
	}

	public void setObject(final Object o) throws ConversionException {
		if (o == null) {
			l = null;
			return;
		}
		if (o instanceof Number)
			l = ((Number) o).longValue();
		else
			throw new ConversionException(
					"CLong column must be loaded with Number object");
	}

}
