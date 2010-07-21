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

/**
 * @author <a href="mailto:rob.heittman@solertium.com">Rob Heittman</a>, <a
 *         href="http://www.solertium.com">Solertium Corporation</a>
 */
public class CBoolean extends BaseColumn {

	Boolean b;

	public CBoolean() {
	}

	public Column newInstance(){
		return new CBoolean();
	}

	public CBoolean(final String localName, final boolean b) {
		setLocalName(localName);
		try {
			setObject(b);
		} catch (final ConversionException unpossible) {
			// we know we are passing in the right kind of value
		}
	}

	public Date getDate() throws ConversionException {
		throw new ConversionException(
				"CBoolean column cannot be converted to Date");
	}

	public Double getDouble() throws ConversionException {
		if(b) return 1D;
		return 0D;
	}

	public Integer getInteger() throws ConversionException {
		if(b) return 1;
		return 0;
	}

	public Literal getLiteral() {
		return new NumericLiteral(getInteger());
	}

	public Long getLong() throws ConversionException {
		if(b) return 1L;
		return 0L;
	}

	public Object getObject() {
		return b;
	}

	public Class<?> getRequiredObjectType() {
		return Integer.class;
	}

	public String getString() throws ConversionException {
		if(b) return "Y";
		return "N";
	}

	public boolean isEmpty() {
		if (b==null) return true;
		return false;
	}

	public Object parseString(final String s) {
		if(s==null) return null;
		String l = s.toLowerCase();
		if("n".equals(l)) return false;
		if("false".equals(l)) return false;
		if("0".equals(l)) return false;
		return true;
	}

	public void setObject(final Object o) throws ConversionException {
		if (o == null) {
			b = null;
			return;
		}
		if (o instanceof Boolean)
			b = (Boolean) o;
		else
			throw new ConversionException(
					"CBoolean column must be loaded with Boolean object");
	}

}
