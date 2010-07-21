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

package com.solertium.vfs;

/**
 * A VFSPath is a simple hierarchical path consisting of a series of Strings.
 * The canonical representation of the path begins with the forward slash
 * character ("/") and is not terminated by any delimiter. Individual tokens are
 * separated by the forward slash character. Backslashes and colons are
 * forbidden. Otherwise, the entire Unicode character set is permitted.
 * <p>
 *
 * The tokens "." and ".." are not permitted because of their idiomatic use to
 * mean "current" and "parent" in a hierarchy.
 * <p>
 *
 * VFS implementations should take steps in the storage medium to ensure that
 * any valid VFSPath can be stored; e.g. encoding and decoding characters that
 * may not work on the underlying storage medium.
 * <p>
 *
 * VFS consumers should take steps to sanitize inputs that may produce paths not
 * compatible with the above rules. For example, paths read on the Windows
 * filesystem will likely contain colons and backslashes; it is the caller's
 * responsibility to transform these away in a reasonable fashion. Paths read
 * from Web URIs and forms may be encoded; it is the caller's responsibility to
 * perform the encoding and decoding step and submit plain slash-separated
 * Unicode paths to the VFS apparatus.
 *
 * @author <a href="mailto:rob.heittman@solertium.com">Rob Heittman</a>, <a
 *         href="http://www.solertium.com">Solertium Corporation</a>
 */
public class VFSPath implements Comparable<VFSPath> {

	public final static VFSPath ROOT = new VFSPath("/");

	final String canonicalRepresentation;

	public VFSPath(final String proposedRepresentation) {
		if (proposedRepresentation == null)
			throw new IllegalArgumentException("A VFSPath may not be null");
		if (!proposedRepresentation.startsWith("/"))
			throw new IllegalArgumentException("A VFSPath must start with /");
		if (proposedRepresentation.contains("/./")
				|| proposedRepresentation.contains("/../"))
			throw new IllegalArgumentException(
					"A VFSPath may not contain the token . or ..");
		if (proposedRepresentation.endsWith("/.")
				|| proposedRepresentation.endsWith("/.."))
			throw new IllegalArgumentException(
					"A VFSPath may not contain the token . or ..");
		if (proposedRepresentation.length() > 1) {
			if (proposedRepresentation.endsWith("/"))
				throw new IllegalArgumentException(
						"A VFSPath may not end with /");
			if (proposedRepresentation.contains("\\"))
				throw new IllegalArgumentException(
						"Backslashes (\"\\\") are not supported in a VFSPath");
			if (proposedRepresentation.contains(":"))
				throw new IllegalArgumentException(
						"Colons (\":\") are not supported in a VFSPath");
		}
		canonicalRepresentation = proposedRepresentation;
	}

	public VFSPath child(final VFSPathToken token) {
		return equals(ROOT) ? new VFSPath(canonicalRepresentation + token)
				: new VFSPath(canonicalRepresentation + "/" + token);
	}

	@Override
	public boolean equals(final Object o) {
		if (o instanceof VFSPath)
			return canonicalRepresentation.equals(o.toString());
		return false;
	}

	/**
	 * @return the parent VFSPath of this VFSPath
	 */
	public VFSPath getCollection() {
		final int i = canonicalRepresentation.lastIndexOf("/");
		if (i == 0)
			return VFSPath.ROOT;
		return new VFSPath(canonicalRepresentation.substring(0,
				canonicalRepresentation.lastIndexOf("/")));
	}

	/**
	 * @return the bottommost token in the hierarchical path
	 */
	public String getName() {
		return canonicalRepresentation.substring(canonicalRepresentation
				.lastIndexOf("/") + 1);
	}

	/**
	 * Essentially splits the canonicalRepresentation on the delimeter, but it
	 * parses each piece as a VFSPathToken, returning an array of them.
	 *
	 * If this.equals(VFSPath.ROOT), an empty array is returned.
	 *
	 * @return the list of tokens
	 */
	public VFSPathToken[] getTokens() {
		if (canonicalRepresentation.length() == 1)
			return new VFSPathToken[] {};

		final String[] pathTokens = canonicalRepresentation.substring(1).split(
				"/");
		final VFSPathToken[] tokens = new VFSPathToken[pathTokens.length];
		for (int i = 0; i < tokens.length; i++)
			tokens[i] = new VFSPathToken(pathTokens[i]);

		return tokens;
	}

	@Override
	public int hashCode() {
		return canonicalRepresentation.hashCode();
	}

	/**
	 * @param parent
	 *            The possible container VFSPath
	 * @return True if this VFSPath is contained within another VFSPath
	 */
	public boolean isIn(final VFSPath parent) {
		if (canonicalRepresentation.startsWith(parent.toString()))
			return true;
		return false;
	}

	@Override
	public String toString() {
		return canonicalRepresentation;
	}

	public int compareTo(VFSPath o) {
		return o == null ? -1 :
			canonicalRepresentation.compareTo(o.canonicalRepresentation);
	}

}
