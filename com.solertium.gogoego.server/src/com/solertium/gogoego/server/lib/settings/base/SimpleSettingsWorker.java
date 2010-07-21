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
package com.solertium.gogoego.server.lib.settings.base;

import org.restlet.representation.Representation;
import org.w3c.dom.Document;

import com.solertium.vfs.VFS;

/**
 * SimpleSettingsWorker.java
 * 
 * Simple settings are types of settings that have potential to be somewhat
 * automated with the server. They all share a common documented language for
 * serving and saving data, and they conform to a strict protocol that involves
 * passing strict URL-based information about accessing INIT, AUTHORITY, and
 * DATA information.
 * 
 * While appropriate for simple settings, this may not be available for more
 * complex applications, in which case they will still need to conform to INIT
 * specifications, but they should not implement this class any further as the
 * responsibility should be pushed elsewhere.
 * 
 * @author carl.scott
 * 
 */
public abstract class SimpleSettingsWorker {

	protected final VFS vfs;

	public SimpleSettingsWorker(VFS vfs) {
		this.vfs = vfs;
	}

	public abstract Representation getAuthority(String key) throws SimpleSettingsWorkerException;

	public abstract Representation getData(String key) throws SimpleSettingsWorkerException;

	public abstract Representation getInit() throws SimpleSettingsWorkerException;

	public abstract void setData(String key, Document document) throws SimpleSettingsWorkerException;

	public static class SimpleSettingsWorkerException extends Exception {
		private static final long serialVersionUID = 1L;

		public SimpleSettingsWorkerException() {
			super();
		}

		public SimpleSettingsWorkerException(String message) {
			super(message);
		}

		public SimpleSettingsWorkerException(Throwable cause) {
			super(cause);
		}

		public SimpleSettingsWorkerException(String message, Throwable cause) {
			super(message, cause);
		}
	}

}
