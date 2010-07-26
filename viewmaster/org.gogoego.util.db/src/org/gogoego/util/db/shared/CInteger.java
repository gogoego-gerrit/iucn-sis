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
public class CInteger extends BaseColumn implements Serializable {

	Integer i;
	private static final long serialVersionUID = 1L;
	private String localName = null;

	public CInteger() {
	}

	public Column newInstance(){
		return new CInteger();
	}

	public CInteger(final String localName, final Number n) {
		setLocalName(localName);
		if (n != null)
			try {
				setObject(Integer.valueOf(n.intValue()));
			} catch (final ConversionException unpossible) {
				// we know we are passing in the right kind of value
			}
	}

	public Date getDate() throws ConversionException {
		throw new ConversionException(
				"CInteger column cannot be converted to Date");
	}

	public Double getDouble() throws ConversionException {
		if (i == null)
			return null;
		return i.doubleValue();
	}

	public Integer getInteger() throws ConversionException {
		return i;
	}

	public Literal getLiteral() {
		return new NumericLiteral(i);
	}

	public Long getLong() throws ConversionException {
		if (i == null)
			return null;
		return i.longValue();
	}

	public Object getObject() {
		return i;
	}

	public Class<?> getRequiredObjectType() {
		return Integer.class;
	}

	public String getString() throws ConversionException {
		if (i == null)
			return null;
		return i.toString();
	}

	public boolean isEmpty() {
		if ((i == null) || (i == 0))
			return true;
		return false;
	}

	public Object parseString(final String s) {
		if(s==null) return null;
		if("".equals(s)) return null;
		try {
			return Integer.parseInt(s);
		} catch (final NumberFormatException nf) {
			throw new ConversionException(nf);
		}
	}

	public void setObject(final Object o) throws ConversionException {
		if (o == null) {
			i = null;
			return;
		}
		if (o instanceof Number)
			i = ((Number) o).intValue();
		else
			throw new ConversionException(
					"CInteger column must be loaded with Number object");
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
