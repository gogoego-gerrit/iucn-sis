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

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class RONodeList implements NodeList {

	private final NodeList peer;

	RONodeList(final NodeList peer) {
		this.peer = peer;
	}

	public int getLength() {
		return peer.getLength();
	}

	public Node item(final int arg0) {
		return RONode.representing(peer.item(arg0));
	}

}
