/*
 * Copyright (C) 2009 Solertium Corporation
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

package com.solertium.vfs;

import java.io.IOException;

/**
 * Base class for all VFS exceptions - makes them easier to catch
 * 
 * @author rob.heittman
 */
public class VFSException extends IOException {

	private static final long serialVersionUID = 1L;

	public VFSException() {
		super();
	}

	public VFSException(final String message) {
		super(message);
	}

	public VFSException(final String message, final Throwable t) {
		super(message);
		initCause(t);
	}

	public VFSException(final Throwable t) {
		super();
		initCause(t);
	}

}
