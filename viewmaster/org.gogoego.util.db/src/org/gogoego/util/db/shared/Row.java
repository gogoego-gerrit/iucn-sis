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
import java.util.ArrayList;

/**
 * @author <a href="mailto:rob.heittman@solertium.com">Rob Heittman</a>, <a
 *         href="http://www.solertium.com">Solertium Corporation</a>
 */
public class Row implements Serializable {

	private static final long serialVersionUID = 1L;

	public Row(){
		super();
	}
	
	/**
	 * Copy constructor.
	 * 
	 * @param r Row to copy.
	 */
	public Row(Row r){
		for(Column c : r.getColumns()){
			Column cc = c.getCopy();
			this.add(cc);
		}
	}

	private ArrayList<Column> columns = new ArrayList<Column>();

	public void add(final Column c) {
		columns.add(c);
	}

	public Column get(final int index) {
		return columns.get(index);
	}

	public Column get(final String localName) {
		for (int i = 0; i < columns.size(); i++) {
			final Column c = get(i);
			if (c != null)
				if (localName.equalsIgnoreCase(c.getLocalName()))
					return c;
		}
		return null;
	}
	
	public ArrayList<Column> getColumns() {
		return columns;
	}

	public void set(final int index, final Column c) {
		columns.set(index, c);
	}

	public int size() {
		return columns.size();
	}

}
