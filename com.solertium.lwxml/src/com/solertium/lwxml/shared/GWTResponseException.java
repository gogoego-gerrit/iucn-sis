/*
 * Copyright (C) 2004-2005 Cluestream Ventures, LLC
 * Copyright (C) 2006-2009 Solertium Corporation
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

package com.solertium.lwxml.shared;

/**
 * GWTResponseException.java
 * 
 * A checked exception containing the status code and message of an 
 * exception from a HTTP request.
 * 
 * @author carl.scott
 */
public class GWTResponseException extends Exception {
	
	private static final long serialVersionUID = 1L;
	
	private int code;

	public GWTResponseException(int code) {
		super();
		this.code = code;
	}

	public GWTResponseException(int code, String message) {
		super(message);
		this.code = code;
	}

	public GWTResponseException(int code, Throwable cause) {
		super(cause);
		this.code = code;
	}

	public GWTResponseException(int code, String message, Throwable cause) {
		super(message, cause);
		this.code = code;
	}
	
	public int getCode() {
		return code;
	}

}
