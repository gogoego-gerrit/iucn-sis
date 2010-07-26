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

import java.util.Date;
import java.util.HashMap;

/**
 * @author <a href="mailto:rob.heittman@solertium.com">Rob Heittman</a>, <a
 *         href="http://www.solertium.com">Solertium Corporation</a>
 */
public interface Column {
	public static final int EMPTY_IS_NULL = 2;
	public static final int NATURAL_NULL = 0;
	public static final int NEVER_NULL = 1;
	
	public boolean isTransient();
	
	public void setTransient(boolean hash);
	
	public void addArbitraryData(String key, String value);
	
	public HashMap<String, String> getArbitraryData();
	
	public Column getCopy();
	
	public Date getDate();

	public Double getDouble();

	public Double getDouble(int null_policy);

	public Integer getInteger();

	public Integer getInteger(int null_policy);

	public Literal getLiteral();

	public String getLocalName();

	public Long getLong();

	public Long getLong(int null_policy);

	public Object getObject();

	public int getPrecision();

	public double getPrimitiveDouble();

	public int getPrimitiveInt();

	public long getPrimitiveLong();

	public CanonicalColumnName getRelatedColumn();

	public Class<?> getRequiredObjectType();

	public int getScale();

	public String getString();

	public String getString(int null_policy);
	
	public boolean isEmpty();

	public boolean isFilled();

	public boolean isIndex();

	public boolean isKey();

	public void setIndex(boolean index);

	public void setKey(boolean key);

	public void setLocalName(String localName);

	public void setObject(Object o);

	public void setPrecision(int precision);

	public void setRelatedColumn(CanonicalColumnName relatedColumn);

	public void setScale(int scale);
	
	public void setString(String s);

}
