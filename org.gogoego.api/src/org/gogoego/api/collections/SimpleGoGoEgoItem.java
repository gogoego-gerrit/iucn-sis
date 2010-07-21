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

import org.w3c.dom.Document;

import com.solertium.vfs.VFSPath;

/**
 * SimpleGoGoEgoItem.java
 * 
 * This represents the simplest of items in terms of the file-system model of
 * collection/item relations. It holds just the file location of the item, along
 * with superclass data (name & id)
 * 
 * @author carl.scott
 * 
 */
public class SimpleGoGoEgoItem extends GoGoEgoItem {

	private VFSPath fileLocation;

	public SimpleGoGoEgoItem(final String itemID, final String name, final VFSPath uri) {
		super(itemID);
		fileLocation = uri;
		itemName = name;
	}

	public void convertFromXMLDocument(final Document document) {

	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final SimpleGoGoEgoItem other = (SimpleGoGoEgoItem) obj;
		if (itemName == null) {
			if (other.itemName != null)
				return false;
		} else if (!itemName.equals(other.itemName))
			return false;
		if (fileLocation == null) {
			if (other.fileLocation != null)
				return false;
		} else if (!fileLocation.equals(other.fileLocation))
			return false;
		if (itemID == null) {
			if (other.itemID != null)
				return false;
		} else if (!itemID.equals(other.itemID))
			return false;
		return true;
	}

	/**
	 * Returns the full file name and extension of this item
	 * 
	 * @return file location
	 */
	public VFSPath getFileLocation() {
		return fileLocation;
	}

	@Override
	public int hashCode() {
		return (itemName + fileLocation + itemID).hashCode();
	}

	/**
	 * Set the full file name and extension of this item
	 * 
	 * @param fileLocation
	 *            the file location
	 */
	public void setFileLocation(final VFSPath fileLocation) {
		this.fileLocation = fileLocation;
	}

	@Override
	public String toString() {
		return toXML();
	}

	public String toXML() {
		return toXML("item");
	}

	public String toXML(final String tagID) {
		return "<" + tagID + " id=\"" + itemID + "\" name=\"" + itemName + "\" uri=\"" + fileLocation + "\" />";
	}

}
