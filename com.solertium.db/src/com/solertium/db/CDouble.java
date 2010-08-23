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
public class CDouble extends BaseColumn {

	Double d;

	public CDouble() {
	}

	public Column newInstance(){
		return new CDouble();
	}

	public CDouble(final String localName, final Number n) {
		setLocalName(localName);
		if (n != null)
			try {
				setObject(new Double(n.doubleValue()));
			} catch (final ConversionException unpossible) {
				// we know we are passing in the right kind of value
			}
	}

	public Date getDate() throws ConversionException {
		throw new ConversionException(
				"CDouble column cannot be converted to Date");
	}

	public Double getDouble() throws ConversionException {
		return d;
	}

	public Integer getInteger() throws ConversionException {
		if (d == null)
			return null;
		return d.intValue();
	}

	public Literal getLiteral() {
		return new NumericLiteral(d);
	}

	public Long getLong() throws ConversionException {
		if (d == null)
			return null;
		return d.longValue();
	}

	public Object getObject() {
		return d;
	}

	public Class<?> getRequiredObjectType() {
		return Double.class;
	}

	public String getString() throws ConversionException {
		if (d == null)
			return null;
		return d.toString();
	}

	public boolean isEmpty() {
		if ((d == null) || (d == 0))
			return true;
		return false;
	}

	public Object parseString(final String s) {
		if(s==null) return null;
		String ns = Replacer.stripWhitespace(s);
		if("".equals(ns)) return null;
		try {
			return Double.parseDouble(ns);
		} catch (final NumberFormatException nf) {
			throw new ConversionException(nf);
		}
	}

	public void setObject(final Object o) throws ConversionException {
		if (o == null) {
			d = null;
			return;
		}
		if (o instanceof Number)
			d = ((Number) o).doubleValue();
		else
			throw new ConversionException(
					"CDouble column must be loaded with Number object");
	}

}
