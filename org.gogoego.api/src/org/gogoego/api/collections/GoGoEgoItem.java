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

/**
 * GoGoEgoItem.java
 * 
 * Abstraction of a standard GoGoEgo Collection Item object
 * 
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a href="http://www.solertium.com">Solertium Corporation</a>
 *
 */
public abstract class GoGoEgoItem implements GoGoEgoXMLObject {

	protected String itemID;
	protected String itemName;

	public GoGoEgoItem(final String itemID) {
		this(itemID, itemID);
	}

	public GoGoEgoItem(final String itemID, final String itemName) {
		this.itemID = itemID;
		this.itemName = itemName;
	}

	@Override
	public abstract boolean equals(Object object);

	public String getItemID() {
		return itemID;
	}

	public String getItemName() {
		return itemName;
	}

	public void setItemID(final String itemID) {
		this.itemID = itemID;
	}

	public void setItemName(final String itemName) {
		this.itemName = itemName;
	}

}
