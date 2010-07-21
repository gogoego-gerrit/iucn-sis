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
 * A VFSPathToken is a single element of a VFSPath.<p>
 * 
 * Forward and back slashes and colons are forbidden. Otherwise, the entire
 * Unicode character set is permitted.<p>
 * 
 * The tokens "." and ".." are not permitted because of their idiomatic use
 * to mean "current" and "parent" in a hierarchy.
 *
 * @author <a href="mailto:rob.heittman@solertium.com">Rob Heittman</a>, <a
 *         href="http://www.solertium.com">Solertium Corporation</a>
 */
public class VFSPathToken implements Comparable<VFSPathToken> {

	final String canonicalRepresentation;

	public VFSPathToken(final String proposedRepresentation) {
		if ((proposedRepresentation.indexOf("/") > -1) || (proposedRepresentation.indexOf("\\") > -1)
				|| (proposedRepresentation.indexOf(":") > -1))
			throw new IllegalArgumentException(
					"VFSPathTokens may not contain /, \\, or :");
		if (proposedRepresentation.equals(".") ||
				proposedRepresentation.equals(".."))
			throw new IllegalArgumentException(
					"VFSPathTokens may not be the reserved symbols . or ..");
		canonicalRepresentation = proposedRepresentation;
	}

	@Override
	public boolean equals(final Object o) {
		try {
			return compareTo((VFSPathToken)o) == 0;
		} catch (ClassCastException e) {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return canonicalRepresentation.hashCode();
	}

	@Override
	public String toString() {
		return canonicalRepresentation;
	}
	
	public int compareTo(VFSPathToken o) {
		return canonicalRepresentation.compareTo(o.canonicalRepresentation);
	}
}
