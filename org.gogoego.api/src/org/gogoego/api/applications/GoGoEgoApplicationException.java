/*
 * Copyright (C) 2007-2009 Solertium Corporation
 *
 * This file is part of the open source GoGoEgo project.
 *
 * Unless you have been granted a different license in writing by the
 * copyright holders for GoGoEgo, you may only modify or redistribute
 * this code under the terms of one of the following licenses:
 * 
 * 1) The Eclipse Public License, v.1.0
 *    http://www.eclipse.org/legal/epl-v10.html
 *
 * 2) The GNU General Public License, version 2 or later
 *    http://www.gnu.org/licenses
 */
package org.gogoego.api.applications;

/**
 * GoGoEgoApplicationException.java
 * 
 * Custom exception when application are attempting to install / uninstall.
 * 
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a
 *         href="http://www.solertium.com">Solertium Corporation</a>
 *
 */
public class GoGoEgoApplicationException extends Exception {

	private static final long serialVersionUID = 1L;

	public GoGoEgoApplicationException() {
		super();
		// TODO Auto-generated constructor stub
	}

	public GoGoEgoApplicationException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public GoGoEgoApplicationException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public GoGoEgoApplicationException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

}
