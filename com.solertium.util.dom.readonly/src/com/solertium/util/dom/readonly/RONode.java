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
import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.Entity;
import org.w3c.dom.EntityReference;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Notation;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;
import org.w3c.dom.UserDataHandler;

public class RONode implements Node {

	/**
	 * This method returns an appropriately typed Node wrapped with
	 * the ReadOnlyDOM classes in this package.  Any nodes or other DOM
	 * constructs (NodeLists, NamedNodeMaps) reachable from the wrapped
	 * node will also be wrapped.  These will throw an UnsupportedOperationException
	 * on any attempted write operation.
	 * 
	 * @param n The Node to wrap
	 * @return A read only representation of the Node
	 */
	public static RONode representing(final Node n) {
		if (n==null) return null;
		if (n instanceof RONode)
			return (RONode) n;
		else if (n instanceof Element)
			return new ROElement((Element) n);
		else if (n instanceof Document)
			return new RODocument((Document) n);
		else if (n instanceof Attr)
			return new ROAttr((Attr) n);
		else if (n instanceof Text)
			return new ROText((Text) n);
		else if (n instanceof CDATASection)
			return new ROCDATASection((CDATASection) n);
		else if (n instanceof Comment)
			return new ROComment((Comment) n);
		else if (n instanceof DocumentType)
			return new RODocumentType((DocumentType) n);
		else if (n instanceof Entity)
			return new ROEntity((Entity) n);
		else if (n instanceof EntityReference)
			return new ROEntityReference(n);
		else if (n instanceof Notation)
			return new RONotation((Notation) n);
		else if (n instanceof ProcessingInstruction)
			return new ROProcessingInstruction((ProcessingInstruction) n);
		else
			return new RONode(n);
	}

	static RuntimeException unsupported() {
		return new UnsupportedOperationException("This document is read-only.");
	}

	private final Node peer;

	RONode(final Node peer) {
		this.peer = peer;
	}

	public Node appendChild(final Node arg0) throws DOMException {
		throw RONode.unsupported();
	}

	public Node cloneNode(final boolean arg0) {
		return RONode.representing(peer.cloneNode(arg0));
	}

	public short compareDocumentPosition(final Node arg0) throws DOMException {
		return peer.compareDocumentPosition(arg0);
	}

	public NamedNodeMap getAttributes() {
		return new RONamedNodeMap(peer.getAttributes());
	}

	public String getBaseURI() {
		return peer.getBaseURI();
	}

	public NodeList getChildNodes() {
		return new RONodeList(peer.getChildNodes());
	}

	public Object getFeature(final String arg0, final String arg1) {
		return peer.getFeature(arg0, arg1);
	}

	public Node getFirstChild() {
		return RONode.representing(peer.getFirstChild());
	}

	public Node getLastChild() {
		return RONode.representing(peer.getLastChild());
	}

	public String getLocalName() {
		return peer.getLocalName();
	}

	public String getNamespaceURI() {
		return peer.getNamespaceURI();
	}

	public Node getNextSibling() {
		return RONode.representing(peer.getNextSibling());
	}

	public String getNodeName() {
		return peer.getNodeName();
	}

	public short getNodeType() {
		return peer.getNodeType();
	}

	public String getNodeValue() throws DOMException {
		return peer.getNodeValue();
	}

	public Document getOwnerDocument() {
		return new RODocument(peer.getOwnerDocument());
	}

	public Node getParentNode() {
		return RONode.representing(peer.getParentNode());
	}

	public String getPrefix() {
		return peer.getPrefix();
	}

	public Node getPreviousSibling() {
		return RONode.representing(peer.getPreviousSibling());
	}

	public String getTextContent() throws DOMException {
		return peer.getTextContent();
	}

	public Object getUserData(final String arg0) {
		throw RONode.unsupported();
	}

	public boolean hasAttributes() {
		return peer.hasAttributes();
	}

	public boolean hasChildNodes() {
		return peer.hasChildNodes();
	}

	public Node insertBefore(final Node arg0, final Node arg1)
			throws DOMException {
		throw RONode.unsupported();
	}

	public boolean isDefaultNamespace(final String arg0) {
		return peer.isDefaultNamespace(arg0);
	}

	public boolean isEqualNode(final Node arg0) {
		return peer.isEqualNode(arg0);
	}

	public boolean isSameNode(final Node arg0) {
		return peer.isSameNode(arg0);
	}

	public boolean isSupported(final String arg0, final String arg1) {
		return peer.isSupported(arg0, arg1);
		// TODO Probably need to return some FALSEs here
	}

	public String lookupNamespaceURI(final String arg0) {
		return peer.lookupNamespaceURI(arg0);
	}

	public String lookupPrefix(final String arg0) {
		return peer.lookupPrefix(arg0);
	}

	public void normalize() {
		throw RONode.unsupported();
	}

	public Node removeChild(final Node arg0) throws DOMException {
		throw RONode.unsupported();
	}

	public Node replaceChild(final Node arg0, final Node arg1)
			throws DOMException {
		throw RONode.unsupported();
	}

	public void setNodeValue(final String arg0) throws DOMException {
		throw RONode.unsupported();
	}

	public void setPrefix(final String arg0) throws DOMException {
		throw RONode.unsupported();
	}

	public void setTextContent(final String arg0) throws DOMException {
		throw RONode.unsupported();
	}

	public Object setUserData(final String arg0, final Object arg1,
			final UserDataHandler arg2) {
		throw RONode.unsupported();
	}

}
