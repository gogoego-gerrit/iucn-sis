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

package com.solertium.util.portable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PortableMetaFinder extends PortableBaseTagListener {

	String found = null;
	String name = null;

	public PortableMetaFinder(final String name) {
		this.name = name;
	}

	public String getFound() {
		return found;
	}

	public Iterator<String> interestingTagNames() {
		final List<String> l = new ArrayList<String>();
		l.add("meta");
		return l.iterator();
	}

	public void process(final PortableTagFilter.Tag t) {
		String metaname = (String) t.attr.get("name");
		if (metaname != null) {
			if ((metaname.startsWith("'")) || (metaname.startsWith("\"")))
				metaname = metaname.substring(1, metaname.length() - 1);
			if (metaname.equalsIgnoreCase(name)) {
				found = t.getAttribute("content");
				if (found != null)
					parent.stopParsing();
			}
		}
	}

}
