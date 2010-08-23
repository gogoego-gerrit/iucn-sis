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
package com.solertium.vfs;

/**
 * PartException.java
 * 
 * Thrown when an error occurs during versioning that causes a part file to be
 * left on the system.
 * 
 * @author carl.scott
 * 
 */
public class PartException extends VFSException {

	private static final long serialVersionUID = 1L;

	public PartException() {
		super();
	}

	public PartException(final String message) {
		super(message);
	}

	public PartException(final String message, final Throwable t) {
		super(message, t);
	}

	public PartException(final Throwable t) {
		super(t);
	}

}
