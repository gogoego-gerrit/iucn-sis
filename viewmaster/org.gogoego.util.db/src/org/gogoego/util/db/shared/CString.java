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
import java.util.Date;

/**
 * @author <a href="mailto:rob.heittman@solertium.com">Rob Heittman</a>, <a
 *         href="http://www.solertium.com">Solertium Corporation</a>
 */
public class CString extends BaseColumn implements Serializable {

	String s;
	private static final long serialVersionUID = 1L;
	private String localName = null;

	public CString() {
	}

	public Column newInstance(){
		return new CString();
	}
	
	public CString(final String localName, final String s) {
		setLocalName(localName);
		if (s != null)
			try {
				setObject(s);
			} catch (final ConversionException unpossible) {
				// we know we are passing in the right kind of value
			}
	}

	public Date getDate() throws ConversionException {
		//FIXME:!!!
		return new Date();
	}

	public Double getDouble() throws ConversionException {
		if (s == null)
			return null; // a null String == a null Double
		Double d;
		try {
			d = Double.parseDouble(s);
		} catch (final NumberFormatException p1) {
			throw new ConversionException(
					"CString column does not contain a double-precision number");
		}
		return d;
	}

	public Integer getInteger() throws ConversionException {
		if (s == null)
			return null; // a null String == a null Integer
		Integer i;
		try {
			i = Integer.parseInt(s);
		} catch (final NumberFormatException p1) {
			throw new ConversionException(
					"CString column does not contain an integer");
		}
		return i;
	}

	public Literal getLiteral() {
		return new StringLiteral(s);
	}

	public Long getLong() throws ConversionException {
		if (s == null)
			return null; // a null String == a null Integer
		Long l;
		try {
			l = Long.parseLong(s);
		} catch (final NumberFormatException p1) {
			throw new ConversionException(
					"CString column does not contain a long integer");
		}
		return l;
	}

	public Object getObject() {
		return s;
	}

	public Class<?> getRequiredObjectType() {
		return String.class;
	}

	public String getString() throws ConversionException {
		return s;
	}

	public boolean isEmpty() {
		if ((s == null) || ("".equals(s)))
			return true;
		return false;
	}

	public Object parseString(final String s) {
		return s;
	}

	public void setObject(final Object o) throws ConversionException {
		if (o == null) {
			s = null;
			return;
		}
		if (o instanceof String)
			s = (String) o;
		else
			throw new ConversionException(
					"CString column must be loaded with String object");
	}

	@Override
	public String toString() {
		if (s == null)
			return "";
		return s;
	}

	@Override
	public String getLocalName() {
		return localName;
	}

	@Override
	public void setLocalName(String localName) {
		this.localName = localName;
	}

}
