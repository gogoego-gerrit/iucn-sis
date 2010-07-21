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

package com.solertium.gogoego.server;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import com.solertium.util.BaseTagListener;
import com.solertium.util.TagFilter;
import com.solertium.util.TrivialExceptionHandler;

/**
 * ScriptTagListener.java
 * 
 * Listens for and evaluates script tags.
 * 
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a href="http://www.solertium.com">Solertium Corporation</a>
 *
 */
public class ScriptTagListener extends BaseTagListener {
	private DynamicContext dc = null;
	boolean diverting = false;

	String passType = null;
	StringWriter scriptBuffer = null;

	public ScriptTagListener(final DynamicContext dc) {
		this.dc = dc;
	}
	
	public ScriptTagListener(final DynamicContext dc, String deprecated) {
		this(dc);
	}

	public List<String> interestingTagNames() {
		final ArrayList<String> l = new ArrayList<String>();
		l.add("script");
		l.add("/script");
		return l;
	}

	public void process(final TagFilter.Tag t) throws IOException {
		if ("script".equals(t.name)) {
			passType = t.getAttribute("type");
			if (passType != null)
				if (passType.startsWith("server-parsed/")) {
					passType = passType.substring("server-parsed/".length());
					t.newTagText = "";
					scriptBuffer = new StringWriter();
					parent.divert(scriptBuffer);
					diverting = true;
				}
		}
		if ("/script".equals(t.name))
			if (diverting) {
				t.newTagText = "";
				diverting = false;
				parent.stopDiverting();
				try {
					try {
						dc.exec(passType, scriptBuffer.toString());
						parent.write(dc.toString());
					} catch (final Error e) {
						GoGoDebug.get("error").println("######## Broken script: {0}", e.getMessage());
						parent.write(e.getMessage());
					}
				} catch (final IOException writeFailed) {
					TrivialExceptionHandler.handle(this, writeFailed);
				}
			}
	}

}
