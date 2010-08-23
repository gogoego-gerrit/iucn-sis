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

/**
 * @author <a href="mailto:rob.heittman@solertium.com">Rob Heittman</a>, <a
 *         href="http://www.solertium.com">Solertium Corporation</a>
 */
public class TableRowProcessor extends RowProcessor {

	@Override
	public void finish() {
		getExecutionContext().write("</table>");
	}

	@Override
	public void process(final Row row) {
		final ExecutionContext ec = getExecutionContext();
		ec.write(" <tr>\n");
		for (int i = 0; i < row.size(); i++) {
			final Column c = row.get(i);
			ec.write("  <td>");
			// ec.write(c.getClass().getName()+": ");
			ec.write(ec.formatLiteral(c.getLiteral()));
			ec.write("</td>\n");
		}
		ec.write(" </tr>\n");
	}

	@Override
	public void start() {
		getExecutionContext().write("<table>\n");
	}

}
