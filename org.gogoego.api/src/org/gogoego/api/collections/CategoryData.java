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

package org.gogoego.api.collections;

import java.util.ArrayList;

import com.solertium.vfs.VFSPath;

/**
 * CategoryData.java
 * 
 * Used to hold category data after it's parsed.
 * 
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a href="http://www.solertium.com">Solertium Corporation</a>
 *
 */
public class CategoryData {

	private static ArrayList<String> reserved = new ArrayList<String>();

	public static boolean isReserved(final String key) {
		if (reserved.isEmpty()) {
			reserved.add("mail");
			reserved.add("review");
			reserved.add("reviews");
		}
		return reserved.contains(key);
	}

	private GoGoEgoCollection category;

	private String itemID;
	private String reservedWord;
	private VFSPath vfsPath;

	public CategoryData() {
	}

	public GoGoEgoCollection getCategory() {
		return category;
	}

	public String getItemID() {
		return itemID;
	}

	public String getReservedWord() {
		return reservedWord;
	}

	public VFSPath getVFSPath() {
		return vfsPath;
	}

	public void setCategory(final GoGoEgoCollection category) {
		this.category = category;
	}

	public void setItemID(final String itemID) {
		this.itemID = itemID;
	}

	public void setReservedWord(final String reservedWord) {
		this.reservedWord = reservedWord;
	}

	public void setVFSPath(final VFSPath vfsPath) {
		this.vfsPath = vfsPath;
	}

}
