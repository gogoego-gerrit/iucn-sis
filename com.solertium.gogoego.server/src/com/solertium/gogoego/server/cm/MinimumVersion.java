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
package com.solertium.gogoego.server.cm;

import java.util.ArrayList;

/**
 * Reasonably quick way of checking multi-token numerical versions as used in
 * bundles (e.g. 1.0.3.5) ... all the existing mechanisms I spotted for this are
 * very slow. Pointers to an existing fast mechanism are welcome.
 */
public class MinimumVersion {

	public static final MinimumVersion NONE = new MinimumVersion("0");

	final ArrayList<Integer> minimumList;

	public MinimumVersion(String minimum) {
		minimumList = stringToList(minimum);
	}

	private ArrayList<Integer> stringToList(String in) {
		ArrayList<Integer> out = new ArrayList<Integer>();
		try {
			for (String token : in.split("\\."))
				out.add(Integer.valueOf(token));
		} catch (RuntimeException re) {
			// unparseable token
		}
		return out;
	}

	public boolean isCompatible(String actual) {
		ArrayList<Integer> actualList = stringToList(actual);
		for (int i = 0; i < minimumList.size() && i < actualList.size(); i++) {
			final int l = minimumList.get(i);
			final int r = actualList.get(i);
			if (l < r)
				return true;
			if (l > r)
				return false;
		}
		return true;
	}
}
