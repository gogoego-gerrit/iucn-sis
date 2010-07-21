/*
 * Copyright (C) 2000-2005 Cluestream Ventures, LLC
 * Copyright (C) 2006-2009 Solertium Corporation
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

package com.solertium.util;

import java.util.ArrayList;
import java.util.List;

/** StripTagListener removes all tags from an HTML or XML stream. */
public class StripTagListener implements TagFilter.Listener {

	public List<String> interestingTagNames() {
		final ArrayList<String> l = new ArrayList<String>();
		l.add("*");
		return l;
	}

	public void process(final TagFilter.Tag t) {
		t.newTagText = "";
	}

	public void setTagFilter(final TagFilter tf) {
	}

}
