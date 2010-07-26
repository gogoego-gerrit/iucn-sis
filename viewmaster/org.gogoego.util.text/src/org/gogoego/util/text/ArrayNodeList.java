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
package org.gogoego.util.text;

import java.util.ArrayList;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Implementation of the W3C NodeList interface that isn't bound to a
 * particular parser.
 * 
 * @author rob.heittman
 */
public class ArrayNodeList implements NodeList {

	private ArrayList<Node> arrayList = new ArrayList<Node>();
	
	public int getLength() {
		return arrayList.size();
	}

	public Node item(int index) {
		return arrayList.get(index);
	}
	
	public void add(Node node) {
		arrayList.add(node);
	}

}
