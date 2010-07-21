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

package com.solertium.lwxml.shared;

public interface NativeNode {

	public static final short ELEMENT_NODE = 1;
	public static final short ATTRIBUTE_NODE = 2;
	public static final short TEXT_NODE = 3;
	public static final short CDATA_SECTION_NODE = 4;
	public static final short ENTITY_REFERENCE_NODE = 5;
	public static final short ENTITY_NODE = 6;
	public static final short PROCESSING_INSTRUCTION_NODE = 7;
	public static final short COMMENT_NODE = 8;
	public static final short DOCUMENT_NODE = 9;
	public static final short DOCUMENT_TYPE_NODE = 10;
	public static final short DOCUMENT_FRAGMENT_NODE = 11;
	public static final short NOTATION_NODE = 12;
	
	public NativeNodeList getChildNodes();

	public NativeNode getFirstChild();
	
	public NativeNode getParent();
	
	public String getTextContent();
	
	public String getNodeName();

	public int getNodeType();

	public String getNodeValue();
	
}
