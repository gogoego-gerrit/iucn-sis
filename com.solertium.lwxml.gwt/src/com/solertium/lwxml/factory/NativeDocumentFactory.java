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
package com.solertium.lwxml.factory;

import com.solertium.lwxml.gwt.NativeDocumentImpl;
import com.solertium.lwxml.shared.NativeDocument;

public class NativeDocumentFactory {

	private static NativeDocumentFactory defaultInstance = new NativeDocumentFactory();
	
	public NativeDocument createNativeDocument(){
		return new NativeDocumentImpl();
	}

	public static NativeDocumentFactory getDefaultInstance(){
		return defaultInstance;
	}

	public static void setDefaultInstance(NativeDocumentFactory instance){
		defaultInstance = instance;
	}
	
	public static NativeDocument newNativeDocument(){
		return getDefaultInstance().createNativeDocument();
	}

}
