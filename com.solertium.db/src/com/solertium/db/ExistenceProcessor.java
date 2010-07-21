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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicBoolean;

import com.solertium.util.TrivialExceptionHandler;

/**
 * @author <a href="mailto:rob.heittman@solertium.com">Rob Heittman</a>, <a
 *         href="http://www.solertium.com">Solertium Corporation</a>
 */
public class ExistenceProcessor implements DBProcessor {

	private ExecutionContext ec = null;
	
	private final AtomicBoolean exists = new AtomicBoolean();
	
	public ExistenceProcessor(){
		exists.set(false);
	}
	
	public boolean exists(){
		return exists.get();
	}

	protected ExecutionContext getExecutionContext() {
		return ec;
	}

	public void process(final ResultSet rs, final ExecutionContext ec)
			throws Exception {
		try {
			if (!rs.isBeforeFirst())
				return; // no rows, do NOTHING.
		} catch (final SQLException ignored) { // possibly operation not
			// supported
			TrivialExceptionHandler.ignore(this, ignored);
		}
		if(rs.next()) exists.set(true);
	};

	protected void setExecutionContext(final ExecutionContext ec) {
		this.ec = ec;
	}

}
