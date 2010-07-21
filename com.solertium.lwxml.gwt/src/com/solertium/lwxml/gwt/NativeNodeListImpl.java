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

public class NativeNodeListImpl implements NativeNodeList {
	
	public JavaScriptObject peer;

	public NativeNodeListImpl(final JavaScriptObject peer) {
		this.peer = peer;
	}

	private native int _getLength(JavaScriptObject peer) /*-{
		if (peer)
			return peer.length;
		else
			return 0;
	}-*/;

	private native JavaScriptObject _item(JavaScriptObject peer, int i) /*-{
		return peer[i];
	}-*/;

	public NativeElement elementAt(final int i) {
		return new NativeElementImpl(_item(peer, i));
	}

	public int getLength() {
		return _getLength(peer);
	}

	public NativeNode item(final int i) {
		return NativeNodeImpl.getTypedNode(_item(peer, i));
	}
}
