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

import org.w3c.dom.CharacterData;
import org.w3c.dom.DOMException;
import org.w3c.dom.Text;

public class ROCharacterData extends RONode implements CharacterData {

	private final CharacterData peer;

	ROCharacterData(final CharacterData peer) {
		super(peer);
		this.peer = peer;
	}

	public void appendData(final String arg) throws DOMException {
		throw RONode.unsupported();
	}

	public void deleteData(final int offset, final int count)
			throws DOMException {
		throw RONode.unsupported();
	}

	public String getData() throws DOMException {
		return peer.getData();
	}

	public int getLength() {
		return peer.getLength();
	}

	public void insertData(final int offset, final String arg)
			throws DOMException {
		throw RONode.unsupported();
	}

	public void replaceData(final int offset, final int count, final String arg)
			throws DOMException {
		throw RONode.unsupported();
	}

	public Text replaceWholeText(final String content) throws DOMException {
		throw RONode.unsupported();
	}

	public void setData(final String data) throws DOMException {
		throw RONode.unsupported();
	}

	public Text splitText(final int offset) throws DOMException {
		throw RONode.unsupported();
	}

	public String substringData(final int offset, final int count)
			throws DOMException {
		return peer.substringData(offset, count);
	}

}
