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

import com.solertium.lwxml.shared.NativeNode;
import com.solertium.lwxml.shared.NativeNodeList;

public class JavaNativeNode implements NativeNode {

	protected static JavaNativeNode getTypedNode(Node peer) {
		final int jsotype = peer.getNodeType();
		if (jsotype == ELEMENT_NODE)
			return new JavaNativeElement((Element) peer);
		else
			return new JavaNativeNode(peer);
	}

	public Node peer;

	public JavaNativeNode() {
	}

	public JavaNativeNode(Node peer) {
		this.peer = peer;
	}

	public NativeNodeList getChildNodes() {
		return new JavaNativeNodeList(peer.getChildNodes());
	}

	public NativeNode getFirstChild() {
		if (getChildNodes().getLength() > 0)
			return getTypedNode(peer.getFirstChild());
		else
			return null;
	}
	
	public String getTextContent() {
		String text = null;
		switch(getNodeType()) {
			case ELEMENT_NODE: {
				text = getFirstChild() != null ? getFirstChild().getNodeValue() : 
					(this instanceof JavaNativeElement ? ((JavaNativeElement)this).getText() : getNodeValue());							
				break;
			}
			case CDATA_SECTION_NODE: {
				text = getNodeValue();
				break;
			}
			default:
				text = getNodeValue();
				break;
		}
		return text;
	}
	
	public NativeNode getParent() {
		Node parent = peer.getParentNode();
		if (parent == null)
			return null;
		else
			return getTypedNode(parent);
	}

	public String getNodeName() {
		return peer.getNodeName();
	}

	public int getNodeType() {
		return peer.getNodeType();
	}

	public String getNodeValue() {
		return peer.getNodeValue();
	}
}
