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
import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.DOMException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.EntityReference;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;

public class RODocument extends RONode implements Document {

	private final Document peer;

	RODocument(final Document peer) {
		super(peer);
		this.peer = peer;
	}

	public Node adoptNode(final Node arg0) throws DOMException {
		throw RONode.unsupported();
	}

	public Attr createAttribute(final String arg0) throws DOMException {
		throw RONode.unsupported();
	}

	public Attr createAttributeNS(final String arg0, final String arg1)
			throws DOMException {
		throw RONode.unsupported();
	}

	public CDATASection createCDATASection(final String arg0)
			throws DOMException {
		throw RONode.unsupported();
	}

	public Comment createComment(final String arg0) {
		throw RONode.unsupported();
	}

	public DocumentFragment createDocumentFragment() {
		throw RONode.unsupported();
	}

	public Element createElement(final String arg0) throws DOMException {
		throw RONode.unsupported();
	}

	public Element createElementNS(final String arg0, final String arg1)
			throws DOMException {
		throw RONode.unsupported();
	}

	public EntityReference createEntityReference(final String arg0)
			throws DOMException {
		throw RONode.unsupported();
	}

	public ProcessingInstruction createProcessingInstruction(final String arg0,
			final String arg1) throws DOMException {
		throw RONode.unsupported();
	}

	public Text createTextNode(final String arg0) {
		throw RONode.unsupported();
	}

	public DocumentType getDoctype() {
		return peer.getDoctype();
	}

	public Element getDocumentElement() {
		return new ROElement(peer.getDocumentElement());
	}

	public String getDocumentURI() {
		return peer.getDocumentURI();
	}

	public DOMConfiguration getDomConfig() {
		return peer.getDomConfig();
	}

	public Element getElementById(final String arg0) {
		return new ROElement(peer.getElementById(arg0));
	}

	public NodeList getElementsByTagName(final String arg0) {
		return new RONodeList(peer.getElementsByTagName(arg0));
	}

	public NodeList getElementsByTagNameNS(final String arg0, final String arg1) {
		return new RONodeList(peer.getElementsByTagNameNS(arg0, arg1));
	}

	public DOMImplementation getImplementation() {
		return peer.getImplementation();
	}

	public String getInputEncoding() {
		return peer.getInputEncoding();
	}

	public boolean getStrictErrorChecking() {
		return peer.getStrictErrorChecking();
	}

	public String getXmlEncoding() {
		return peer.getXmlEncoding();
	}

	public boolean getXmlStandalone() {
		return peer.getXmlStandalone();
	}

	public String getXmlVersion() {
		return peer.getXmlVersion();
	}

	public Node importNode(final Node arg0, final boolean arg1)
			throws DOMException {
		throw RONode.unsupported();
	}

	public void normalizeDocument() {
		throw RONode.unsupported();
	}

	public Node renameNode(final Node arg0, final String arg1, final String arg2)
			throws DOMException {
		throw RONode.unsupported();
	}

	public void setDocumentURI(final String arg0) {
		throw RONode.unsupported();
	}

	public void setStrictErrorChecking(final boolean arg0) {
		throw RONode.unsupported();
	}

	public void setXmlStandalone(final boolean arg0) throws DOMException {
		throw RONode.unsupported();
	}

	public void setXmlVersion(final String arg0) throws DOMException {
		throw RONode.unsupported();
	}

}
