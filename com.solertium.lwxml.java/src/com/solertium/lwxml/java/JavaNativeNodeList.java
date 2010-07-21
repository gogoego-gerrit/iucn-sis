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

package com.solertium.lwxml.java;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.solertium.lwxml.shared.NativeElement;
import com.solertium.lwxml.shared.NativeNode;
import com.solertium.lwxml.shared.NativeNodeList;

public class JavaNativeNodeList implements NativeNodeList {
	public NodeList peer;

	public JavaNativeNodeList(final NodeList peer) {
		this.peer = peer;
	}

	public NativeElement elementAt(final int i) {
		Node n = peer.item(i);
		if(n.getNodeType()==JavaNativeNode.ELEMENT_NODE){
			return new JavaNativeElement((Element) n);
		} else {
			return null;
		}
	}

	public int getLength() {
		return peer.getLength();
	}

	public NativeNode item(final int i) {
		return JavaNativeNode.getTypedNode(peer.item(i));
	}
}
