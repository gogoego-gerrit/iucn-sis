/*
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

package org.gogoego.util.text;

import java.util.ArrayList;
import java.util.List;

/**
 * StripPIListener is responsible for removing extra XML processing instructions
 * from the outbound stream. Some parsers ignore extra PI's, other parsers fail
 * them. Serializing XML fragments can result in a compound document with a
 * bunch of extra processing instructions. This Listener will fix that.
 */
public class StripPITagListener implements TagFilter.Listener {

	/** We are interested in XML processing instructions */
	public List<String> interestingTagNames() {
		final ArrayList<String> l = new ArrayList<String>();
		l.add("?xml");
		return l;
	}

	/** We will only be sent XML PI's. Delete them altogether. */
	public void process(final TagFilter.Tag t) {
		t.newTagText = "";
	}

	public void setTagFilter(final TagFilter tf) {
	}

}
