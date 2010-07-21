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
import org.w3c.dom.NodeList;

import com.solertium.lwxml.shared.NativeElement;
import com.solertium.lwxml.shared.NativeNamedNodeMap;
import com.solertium.lwxml.shared.NativeNodeList;

public class JavaNativeElement extends JavaNativeNode implements NativeElement {
	
	Element epeer = null;
	
	public JavaNativeElement(final Element peer) {
		super(peer);
		epeer = (Element) peer;
	}

	public String getAttribute(final String name) {
		return epeer.getAttribute(name);
	}

	public boolean hasAttribute(final String name) {
		return epeer.hasAttribute(name);
	}

	public NativeNamedNodeMap getAttributes() {
		return new JavaNativeNamedNodeMap(epeer.getAttributes());
	}
	
	public NativeElement getElementByTagName(final String name) {
		try {
			NodeList nl = epeer.getElementsByTagName(name);
			if ((nl!=null) && (nl.getLength()>0)) {
				return new JavaNativeElement((Element) nl.item(0));
			} else {
				return null;
			}
		} catch (final Exception noSuchElement) {
			return null;
		}
	}

	public NativeNodeList getElementsByTagName(final String name) {
		return new JavaNativeNodeList(epeer.getElementsByTagName(name));
	}

	public String getText() {
		return epeer.getTextContent();
	}
}
