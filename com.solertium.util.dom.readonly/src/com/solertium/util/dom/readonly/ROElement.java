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

import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.TypeInfo;

public class ROElement extends RONode implements Element {

	private final Element peer;

	ROElement(final Element peer) {
		super(peer);
		this.peer = peer;
	}

	public String getAttribute(final String arg0) {
		return peer.getAttribute(arg0);
	}

	public Attr getAttributeNode(final String arg0) {
		return new ROAttr(peer.getAttributeNode(arg0));
	}

	public Attr getAttributeNodeNS(final String arg0, final String arg1)
			throws DOMException {
		return new ROAttr(peer.getAttributeNodeNS(arg0, arg1));
	}

	public String getAttributeNS(final String arg0, final String arg1)
			throws DOMException {
		return peer.getAttributeNS(arg0, arg1);
	}

	public NodeList getElementsByTagName(final String arg0) {
		return new RONodeList(peer.getElementsByTagName(arg0));
	}

	public NodeList getElementsByTagNameNS(final String arg0, final String arg1)
			throws DOMException {
		return new RONodeList(peer.getElementsByTagNameNS(arg0, arg1));
	}

	public TypeInfo getSchemaTypeInfo() {
		return peer.getSchemaTypeInfo();
	}

	public String getTagName() {
		return peer.getTagName();
	}

	public boolean hasAttribute(final String arg0) {
		return peer.hasAttribute(arg0);
	}

	public boolean hasAttributeNS(final String arg0, final String arg1)
			throws DOMException {
		return peer.hasAttributeNS(arg0, arg1);
	}

	public void removeAttribute(final String arg0) throws DOMException {
		throw RONode.unsupported();
	}

	public Attr removeAttributeNode(final Attr arg0) throws DOMException {
		throw RONode.unsupported();
	}

	public void removeAttributeNS(final String arg0, final String arg1)
			throws DOMException {
		throw RONode.unsupported();
	}

	public void setAttribute(final String arg0, final String arg1)
			throws DOMException {
		throw RONode.unsupported();
	}

	public Attr setAttributeNode(final Attr arg0) throws DOMException {
		throw RONode.unsupported();
	}

	public Attr setAttributeNodeNS(final Attr arg0) throws DOMException {
		throw RONode.unsupported();
	}

	public void setAttributeNS(final String arg0, final String arg1,
			final String arg2) throws DOMException {
		throw RONode.unsupported();
	}

	public void setIdAttribute(final String arg0, final boolean arg1)
			throws DOMException {
		throw RONode.unsupported();
	}

	public void setIdAttributeNode(final Attr arg0, final boolean arg1)
			throws DOMException {
		throw RONode.unsupported();
	}

	public void setIdAttributeNS(final String arg0, final String arg1,
			final boolean arg2) throws DOMException {
		throw RONode.unsupported();
	}

}
