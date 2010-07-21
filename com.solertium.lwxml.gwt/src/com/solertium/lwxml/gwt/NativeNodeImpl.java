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

package com.solertium.lwxml.gwt;

import com.google.gwt.core.client.JavaScriptObject;
import com.solertium.lwxml.shared.NativeElement;
import com.solertium.lwxml.shared.NativeNode;
import com.solertium.lwxml.shared.NativeNodeList;

public class NativeNodeImpl implements NativeNode {

	protected static native String _getNodeName(JavaScriptObject peer) /*-{
		return peer.nodeName;
	}-*/;

	protected static native int _getNodeType(JavaScriptObject peer) /*-{
		return peer.nodeType;
	}-*/;

	protected static native String _getNodeValue(JavaScriptObject peer) /*-{
		return peer.nodeValue;
	}-*/;

	protected static NativeNodeImpl getTypedNode(final JavaScriptObject peer) {
		final int jsotype = _getNodeType(peer);
		if (jsotype == 1)
			return new NativeElementImpl(peer);
		else
			return new NativeNodeImpl(peer);
	}

	public JavaScriptObject peer;

	public NativeNodeImpl() {
	}

	public NativeNodeImpl(final JavaScriptObject peer) {
		this.peer = peer;
	}

	private native JavaScriptObject _getChildNodes(JavaScriptObject peer) /*-{
		return peer.childNodes;
	}-*/;

	private native JavaScriptObject _getFirstChild(JavaScriptObject peer) /*-{
		return peer.childNodes[0];		
	}-*/;
	
	private native JavaScriptObject _getParent(JavaScriptObject peer) /*-{
		return peer.parentNode;
	}-*/;

	public NativeNodeList getChildNodes() {
		return new NativeNodeListImpl(_getChildNodes(peer));
	}

	public NativeNode getFirstChild() {
		if (getChildNodes().getLength() > 0)
			return getTypedNode(_getFirstChild(peer));
		else
			return null;
	}
	
	public NativeNode getParent() {
		JavaScriptObject parent = _getParent(peer);
		if (parent == null)
			return null;
		else
			return getTypedNode(parent);
	}
	
	public String getTextContent() {
		String text = "";
		switch(getNodeType()) {
			case ELEMENT_NODE: {
				NativeNodeList cn = getChildNodes();
				if (cn != null)
					for (int i = 0; i < cn.getLength(); i++)
						text += cn.item(i) instanceof NativeElement ? 
							"" : cn.item(i).getTextContent();
				else
					text = getNodeValue();
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
		return text == null ? "" : text;
	}

	public String getNodeName() {
		return NativeNodeImpl._getNodeName(peer);
	}

	public int getNodeType() {
		return NativeNodeImpl._getNodeType(peer);
	}

	public String getNodeValue() {
		return NativeNodeImpl._getNodeValue(peer);
	}
}
