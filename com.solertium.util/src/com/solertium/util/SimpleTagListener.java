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

/** Filters everything but simple tags in an HTML file. */
public class SimpleTagListener implements TagFilter.Listener {
	public List<String> interestingTagNames() {
		final ArrayList<String> l = new ArrayList<String>();
		l.add("*");
		return l;
	}

	public void process(final TagFilter.Tag t) {
		final String ltn = t.name.toLowerCase();
		if ("a".equals(ltn))
			return;
		if ("/a".equals(ltn))
			return;
		if ("i".equals(ltn)) {
			t.newTagText = "<em>";
			return;
		}
		if ("/i".equals(ltn)) {
			t.newTagText = "</em>";
			return;
		}
		if ("em".equals(ltn)) {
			t.newTagText = "<em>";
			return;
		}
		if ("/em".equals(ltn)) {
			t.newTagText = "</em>";
			return;
		}
		if ("b".equals(ltn)) {
			t.newTagText = "<strong>";
			return;
		}
		if ("/b".equals(ltn)) {
			t.newTagText = "</strong>";
			return;
		}
		if ("strong".equals(ltn)) {
			t.newTagText = "<strong>";
			return;
		}
		if ("/strong".equals(ltn)) {
			t.newTagText = "</strong>";
			return;
		}
		if ("div".equals(ltn)) {
			t.newTagText = "<div>";
			return;
		}
		if ("/div".equals(ltn)) {
			t.newTagText = "</div>";
			return;
		}
		if ("p".equals(ltn)) {
			t.newTagText = "<p/>";
			return;
		}
		if ("p/".equals(ltn)) {
			t.newTagText = "<p/>";
			return;
		}
		if ("br".equals(ltn)) {
			t.newTagText = "<br/>";
			return;
		}
		if ("br/".equals(ltn)) {
			t.newTagText = "<br/>";
			return;
		}
		if ("li".equals(ltn)) {
			t.newTagText = "<li>";
			return;
		}
		if ("/li".equals(ltn)) {
			t.newTagText = "</li>";
			return;
		}
		if ("ul".equals(ltn)) {
			t.newTagText = "<ul>";
			return;
		}
		if ("/ul".equals(ltn)) {
			t.newTagText = "</ul>";
			return;
		}
		if ("ol".equals(ltn)) {
			t.newTagText = "<ol>";
			return;
		}
		if ("/ol".equals(ltn)) {
			t.newTagText = "</ol>";
			return;
		}
		t.newTagText = "";
	}

	public void setTagFilter(final TagFilter tf) {
	}
}
