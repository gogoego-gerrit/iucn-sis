/*******************************************************************************
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
 *     http://www.gnu.org/licenses
 ******************************************************************************/
package com.solertium.util.dom.readonly;

import org.w3c.dom.DOMException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class RONamedNodeMap implements NamedNodeMap {

	private final NamedNodeMap peer;

	RONamedNodeMap(final NamedNodeMap peer) {
		this.peer = peer;
	}

	public int getLength() {
		return peer.getLength();
	}

	public Node getNamedItem(final String arg0) {
		return RONode.representing(peer.getNamedItem(arg0));
	}

	public Node getNamedItemNS(final String arg0, final String arg1)
			throws DOMException {
		return RONode.representing(peer.getNamedItemNS(arg0, arg1));
	}

	public Node item(final int arg0) {
		return RONode.representing(peer.item(arg0));
	}

	public Node removeNamedItem(final String arg0) throws DOMException {
		throw RONode.unsupported();
	}

	public Node removeNamedItemNS(final String arg0, final String arg1)
			throws DOMException {
		throw RONode.unsupported();
	}

	public Node setNamedItem(final Node arg0) throws DOMException {
		throw RONode.unsupported();
	}

	public Node setNamedItemNS(final Node arg0) throws DOMException {
		throw RONode.unsupported();
	}

}
