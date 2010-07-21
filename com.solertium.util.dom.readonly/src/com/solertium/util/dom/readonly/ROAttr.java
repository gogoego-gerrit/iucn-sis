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
import org.w3c.dom.TypeInfo;

public class ROAttr extends RONode implements Attr {

	private final Attr peer;

	ROAttr(final Attr peer) {
		super(peer);
		this.peer = peer;
	}

	public String getName() {
		return peer.getName();
	}

	public Element getOwnerElement() {
		return new ROElement(peer.getOwnerElement());
	}

	public TypeInfo getSchemaTypeInfo() {
		return peer.getSchemaTypeInfo();
	}

	public boolean getSpecified() {
		return peer.getSpecified();
	}

	public String getValue() {
		return peer.getValue();
	}

	public boolean isId() {
		return peer.isId();
	}

	public void setValue(final String arg0) throws DOMException {
		throw RONode.unsupported();
	}

}
