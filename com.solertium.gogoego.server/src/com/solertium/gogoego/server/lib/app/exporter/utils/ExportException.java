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
package com.solertium.gogoego.server.lib.app.exporter.utils;

import org.restlet.data.Status;
import org.restlet.resource.ResourceException;

/**
 * ExportException.java
 * 
 * An exception that could be thrown while attempting to perform an export.
 * Since exports are typically called from a waiting client (but not
 * necessarily), this exception extends ResourceException, to provide an HTTP
 * status to the client
 * 
 * @author carl.scott
 * 
 */
public class ExportException extends ResourceException {

	private static final long serialVersionUID = 1L;

	private String description;

	public ExportException() {
		this(Status.SERVER_ERROR_INTERNAL);
	}

	/**
	 * Constructor.
	 * 
	 * @param status
	 *            The status to associate.
	 */
	public ExportException(final Status status) {
		super(status);
	}

	/**
	 * Constructor.
	 * 
	 * @param status
	 *            The status to copy.
	 * @param description
	 *            The description to associate.
	 */
	public ExportException(final Status status, final String description) {
		super(new Status(status, description));
		this.description = description;
	}

	/**
	 * Constructor.
	 * 
	 * @param status
	 *            The status to copy.
	 * @param description
	 *            The description to associate.
	 * @param cause
	 *            The wrapped cause error or exception.
	 */
	public ExportException(final Status status, final String description, final Throwable cause) {
		this(new Status(status, description), cause);
		this.description = description;
	}

	/**
	 * Constructor.
	 * 
	 * @param status
	 *            The status to associate.
	 * @param cause
	 *            The wrapped cause error or exception.
	 */
	public ExportException(final Status status, final Throwable cause) {
		super(status, cause);
	}

	/**
	 * Constructor that set the status to
	 * {@link org.restlet.data.Status#SERVER_ERROR_INTERNAL}.
	 * 
	 * @param cause
	 *            The wrapped cause error or exception.
	 */
	public ExportException(final Throwable cause) {
		this(Status.SERVER_ERROR_INTERNAL, cause);
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

}
