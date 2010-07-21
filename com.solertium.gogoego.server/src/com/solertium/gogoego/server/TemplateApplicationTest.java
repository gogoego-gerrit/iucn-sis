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
package com.solertium.gogoego.server;

import org.gogoego.api.representations.GoGoEgoStringRepresentation;

public class TemplateApplicationTest {
	
	public static void test() {
		GoGoEgoStringRepresentation base = new GoGoEgoStringRepresentation("<html><body>Hello world</body></html>");
		GoGoEgoStringRepresentation template = new GoGoEgoStringRepresentation("<html><head><meta name=\"template\" content=\"blue.html\" /><title>Title</title></head><body>I found:<br/>${base.body}</body></html>");
		
		System.out.println(GoGoMagicFilter.applyTemplating(template, base));
	}

}
