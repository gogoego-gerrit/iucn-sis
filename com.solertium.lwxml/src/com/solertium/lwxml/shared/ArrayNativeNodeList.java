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
package com.solertium.lwxml.shared;

import java.util.ArrayList;

/**
 * Minimal GWT-space implementation of the NativeNodeList interface.
 * 
 * @author rob.heittman
 */
public class ArrayNativeNodeList implements NativeNodeList {

	private ArrayList<NativeNode> arrayList = new ArrayList<NativeNode>();
	
	public int getLength() {
		return arrayList.size();
	}

	public NativeNode item(int index) {
		return arrayList.get(index);
	}
	
	public void add(NativeNode node) {
		arrayList.add(node);
	}

	public NativeElement elementAt(int i) {
		return (NativeElement) item(i);
	}

}
