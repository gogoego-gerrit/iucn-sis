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

import java.util.HashMap;

/**
 * @author <a href="mailto:rob.heittman@solertium.com">Rob Heittman<Ä/a>, <a
 *         href="http://www.solertium.com">Solertium Corporation</a>
 */
public abstract class BaseColumn implements Column {

	private boolean index = false;
	private boolean key = false;
	private boolean trans = false;
	private int precision = 0;
	private CanonicalColumnName relatedColumn = null;
	private int scale = 0;
	
	private HashMap<String, String> arbitraryData = new HashMap<String, String>();
	
	protected abstract Object parseString(String in);
	
	public Column getCopy(){
		Column n = newInstance();
		n.setKey(isKey());
		n.setIndex(isIndex());
		n.setObject(getObject());
		n.setTransient(isTransient());
		n.setLocalName(getLocalName());
		n.setScale(getScale());
		n.setPrecision(getPrecision());
		n.setRelatedColumn(getRelatedColumn());
		return n;
	}
	
	public abstract Column newInstance();

	public boolean isTransient(){
		return trans;
	}
	
	public void setTransient(boolean trans){
		this.trans = trans;
	}
	
	public void addArbitraryData(String key, String value) {
		arbitraryData.put(key, value);
	}
	
	public HashMap<String, String> getArbitraryData() {
		return arbitraryData;
	}
	
	public void convert(final Column p_to) throws ConversionException {
		throw new ConversionException(this.getClass().getName()
				+ " does not support conversion to "
				+ p_to.getClass().getName());
	}

	public Double getDouble(final int null_policy) throws ConversionException {
		final Double d = getDouble();
		if (null_policy == NATURAL_NULL)
			return d;
		else if (null_policy == EMPTY_IS_NULL) {
			if (d == 0.0D)
				return null;
			return d;
		} else if (null_policy == NEVER_NULL) {
			if (d == null)
				return 0.0D;
			return d;
		}
		return null;
	}

	public Integer getInteger(final int null_policy) throws ConversionException {
		final Integer i = getInteger();
		if (null_policy == NATURAL_NULL)
			return i;
		else if (null_policy == EMPTY_IS_NULL) {
			if (i == 0)
				return null;
			return i;
		} else if (null_policy == NEVER_NULL) {
			if (i == null)
				return 0;
			return i;
		}
		return null;
	}

	public abstract String getLocalName();

	public Long getLong(final int null_policy) throws ConversionException {
		final Long l = getLong();
		if (null_policy == NATURAL_NULL)
			return l;
		else if (null_policy == EMPTY_IS_NULL) {
			if (l == 0)
				return null;
			return l;
		} else if (null_policy == NEVER_NULL) {
			if (l == null)
				return 0L;
			return l;
		}
		return null;
	}

	public int getPrecision() {
		return precision;
	}

	public double getPrimitiveDouble() throws ConversionException {
		return getDouble(NEVER_NULL).doubleValue();
	}

	public int getPrimitiveInt() throws ConversionException {
		return getInteger(NEVER_NULL).intValue();
	}

	public long getPrimitiveLong() throws ConversionException {
		return getLong(NEVER_NULL).longValue();
	}

	public CanonicalColumnName getRelatedColumn() {
		return relatedColumn;
	}

	public int getScale() {
		return scale;
	}

	public String getString(final int null_policy) throws ConversionException {
		final String s = getString();
		if (null_policy == NATURAL_NULL)
			return s;
		else if (null_policy == EMPTY_IS_NULL) {
			if ("".equals(s))
				return null;
			return s;
		} else if (null_policy == NEVER_NULL) {
			if (s == null)
				return "";
			return s;
		}
		return null;
	}

	public boolean isFilled() {
		return !isEmpty();
	}

	public boolean isIndex() {
		return index;
	}

	public boolean isKey() {
		return key;
	}

	public void setIndex(final boolean index) {
		this.index = index;
	}

	public void setKey(final boolean key) {
		this.key = key;
	}

	public abstract void setLocalName(final String localName);

	public void setPrecision(final int precision) {
		this.precision = precision;
	}

	public void setRelatedColumn(final CanonicalColumnName relatedColumn) {
		this.relatedColumn = relatedColumn;
	}

	public void setScale(final int scale) {
		this.scale = scale;
	}

	public void setString(String s) {
		setObject(parseString(s));
	}

	@Override
	public String toString() {
		try {
			return getString(NATURAL_NULL);
		} catch (final Exception x) {
			return "";
		}
	}
}
