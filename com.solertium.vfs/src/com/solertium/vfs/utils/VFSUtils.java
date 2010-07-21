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

package com.solertium.vfs.utils;

import java.io.File;

import com.solertium.vfs.VFSException;
import com.solertium.vfs.VFSPath;

/**
 * VFSUtils.java
 * 
 * This class is meant to perform not only mundane VFS-related operations, but
 * is also a simple attempt at standardization and conformity to the rules and
 * expectations of the VFS.
 * 
 * @author carl.scott
 * 
 */
public class VFSUtils {

	/**
	 * VFSPathParseException
	 * 
	 * This exception is thrown when a path is parsed with blatantly incorrect
	 * input.
	 * 
	 * @author carl.scott
	 * 
	 */
	public static final class VFSPathParseException extends VFSException {
		private static final long serialVersionUID = 1L;

		public VFSPathParseException() {
			super();
		}

		public VFSPathParseException(final String message) {
			super(message);
		}

		public VFSPathParseException(final String message, final Throwable t) {
			super(message);
			initCause(t);
		}

		public VFSPathParseException(final Throwable t) {
			super();
			initCause(t);
		}
	}

	private static final char systemPathTokenSeparator = File.separatorChar;

	/**
	 * Tries to do some smart cleaning of a VFS path by adding a leading
	 * delimeter, removing any trailing delimeter, and cleaning up paths created
	 * by windows that uses '\' instead of '/' as a delimeter.
	 * 
	 * Note that this function calls the VFSPath constructor which throws
	 * IllegalArgumentException for malformed uris. While this function is meant
	 * to conform paths so this does not happen, it can still occur if your
	 * actual URL is less than blatantly wrong, but still contains bad
	 * characters or sequences of characters. This function makes no attempt to
	 * catch this error, placing the burden on the caller to handle this should
	 * it occur, or cleanse their input string of these characters before
	 * calling this function.
	 * 
	 * @param path
	 *            the path
	 * @return the VFSPath object
	 * @throws VFSPathParseException
	 *             for blatantly bad paths
	 * @see VFSPath
	 */
	public static VFSPath parseVFSPath(final String path)
			throws VFSPathParseException {
		String parsed = "";

		if ((path == null) || path.equals("")) {
			System.err.println("Warning: null path converted to /");
			return VFSPath.ROOT;
		}

		if (path.equals("/"))
			return VFSPath.ROOT;

		if (path.length() < 2)
			throw new VFSPathParseException(
					"Invalid path, too short and not root");

		parsed = path;

		// Check for Windows delimeter
		if (parsed.indexOf("\\") != -1)
			if (systemPathTokenSeparator == '\\')
				parsed = parsed.replace(systemPathTokenSeparator, '/');
			else
				throw new VFSPathParseException("Contains \\ at char "
						+ parsed.indexOf("\\") + ", but it isn't windows... ("
						+ systemPathTokenSeparator + ")");

		if (!parsed.startsWith("/"))
			parsed = "/" + parsed;
		if (parsed.endsWith("/"))
			parsed = parsed.substring(0, parsed.length() - 1);

		// We've done all we can to clean this string, you should
		// catch your own (now unlikely) IllegalArgumentException
		return new VFSPath(parsed);
	}

}
