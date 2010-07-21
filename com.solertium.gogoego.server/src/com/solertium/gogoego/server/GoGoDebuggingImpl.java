/*
 * Copyright (C) 2007-2009 Solertium Corporation
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
package com.solertium.gogoego.server;

import java.io.OutputStream;

import org.gogoego.api.debugging.GoGoDebugger;

import com.solertium.util.SysDebugger;

/**
 * GoGoDebuggingImpl.java
 * 
 * GoGoDebugger implementation.
 * 
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>,
 * 	 <a href="http://www.solertium.com">Solertium Corporation</a>
 *
 */
public class GoGoDebuggingImpl extends SysDebugger implements GoGoDebugger {

	public GoGoDebuggingImpl() {
		super();
	}

	public GoGoDebuggingImpl(int logLevel, OutputStream output) {
		super(logLevel, output);
	}

	public GoGoDebuggingImpl(int logLevel) {
		super(logLevel);
	}

}
