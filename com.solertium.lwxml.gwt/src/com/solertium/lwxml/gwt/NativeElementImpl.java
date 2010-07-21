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
import com.solertium.lwxml.shared.NativeNamedNodeMap;
import com.solertium.lwxml.shared.NativeNodeList;

public class NativeElementImpl extends NativeNodeImpl implements NativeElement  {
	public NativeElementImpl(final JavaScriptObject peer) {
		super(peer);
	}

	private native String _getAttribute(JavaScriptObject peer, String name) /*-{
		return peer.getAttribute(name);
	}-*/;

	private native boolean _hasAttribute(JavaScriptObject peer, String name) /*-{
		return peer.hasAttribute(name);
	}-*/; 
	
	private native JavaScriptObject _getAttributes(JavaScriptObject peer) /*-{
	return peer.attributes;
	}-*/;
	
	private native JavaScriptObject _getElementByTagName(JavaScriptObject peer,
			String name) /*-{
		return peer.getElementsByTagName(name)[0];
	}-*/;

	private native JavaScriptObject _getElementsByTagName(
			JavaScriptObject peer, String name) /*-{
		return peer.getElementsByTagName(name);
	}-*/;

	private native String _getText(JavaScriptObject peer) /*-{
		var ret = "";
		    var cn = peer.childNodes;
	    for(var i=0;i<cn.length;i++)
	    	if(cn[i].nodeType==3) ret+=cn[i].nodeValue;
		return ret;
	}-*/;

	public String getAttribute(final String name) {
		return _getAttribute(peer, name);
	}

	public boolean hasAttribute(final String name) {
		return _hasAttribute(peer, name);
	}

	public NativeNamedNodeMap getAttributes() {
		try {
			return new NativeNamedNodeMapImpl(_getAttributes(peer));
		} catch (final Exception noSuchElement) {
			return null;
		}
	}
	
	public NativeElement getElementByTagName(final String name) {
		try {
			return new NativeElementImpl(_getElementByTagName(peer, name));
		} catch (final Exception noSuchElement) {
			return null;
		}
	}

	public NativeNodeList getElementsByTagName(final String name) {
		return new NativeNodeListImpl(_getElementsByTagName(peer, name));
	}

	/**
	 * Gets text from element's child node(s)
	 * @deprecated use getTextContent instead
	 */
	public String getText() {
		
		try 
		{
			return _getText(peer);
		}
		catch (Throwable e) 
		{
			return "";
		}
	}
}
