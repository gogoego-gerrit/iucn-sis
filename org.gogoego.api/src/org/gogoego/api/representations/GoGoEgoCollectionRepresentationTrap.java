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
package org.gogoego.api.representations;


/**
 * GoGoEgoCollectionRepresentationTrap.java
 * 
 * The guided interface for accessing collection content.
 * 
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a href="http://www.solertium.com">Solertium Corporation</a>
 *
 */
public class GoGoEgoCollectionRepresentationTrap extends BaseRepresentationTrap<GoGoEgoCollectionRepresentation> implements HasURI {
	
	public GoGoEgoCollectionRepresentationTrap(GoGoEgoCollectionRepresentation rep) {
		super(rep);
	}
	
	public String getContentType() {
		return getWrapped().getContentType();
	}

	/**
	 * Creates a default grid view of the collection's object as best it can.
	 * 
	 * @param template
	 *            the template to use for each item
	 * @return the HTML for the grid
	 */
	public String getCollectionAsGrid(final String itemTemplate) {
		return getWrapped().getCollectionAsGrid(itemTemplate);
	}

	/**
	 * Creates a grid view of the collection's object given the item's template
	 * and number of items per row.
	 * 
	 * @param template
	 *            the template to use for each item
	 * @param itemsPerRow
	 *            the number of items per row
	 * @return the HTML for the grid
	 */
	public String getCollectionAsGrid(final String template, final int itemsPerRow) {
		return getWrapped().getCollectionAsGrid(template, itemsPerRow);
	}

	public String getCollectionAsGrid(final String template, final int itemsPerRow, int numItemsToReturn) {
		return getWrapped().getCollectionAsGrid(template, itemsPerRow, numItemsToReturn);
	}
	
	public String getCollectionAsList(final String itemTemplate) {
		return getWrapped().getCollectionAsList(itemTemplate);
	}

	/**
	 * Returns the name of the collection
	 * 
	 * @return the name
	 */
	public String getCollectionName() {
		return getWrapped().getCollectionName();
	}

	/**
	 * Returns the id of the collection
	 * 
	 * @return the id
	 */
	public String getCollectionID() {
		return getWrapped().getCollectionID();
	}
	
	public String getDescription() {
		return getWrapped().getDescription();
	}
	
	public String getKeywords() {
		return getWrapped().getKeywords();
	}

	public int getItemCount() {
		return getWrapped().getItemCount();
	}

	public int getCollectionCount() {
		return getWrapped().getCollectionCount();
	}

	public GoGoEgoItemRepresentationTrap getItemObject(final int index) {
		GoGoEgoItemRepresentation representation = getWrapped().getItemObject(index);
		if (representation == null)
			return null;
		else {
			GoGoEgoItemRepresentationTrap trap = 
				new GoGoEgoItemRepresentationTrap(representation);
			for (TouchListener listener : getTouchListeners())
				trap.addTouchListener(listener);
			return trap;
		}
	}

	public GoGoEgoCollectionRepresentationTrap getCollectionObject(final int index) {
		GoGoEgoCollectionRepresentation representation = getWrapped().getCollectionObject(index);
		if (representation == null)
			return null;
		else {
			GoGoEgoCollectionRepresentationTrap trap = 
				new GoGoEgoCollectionRepresentationTrap(representation);
			for (TouchListener listener : getTouchListeners())
				trap.addTouchListener(listener);
			return trap;
		}
	}

	public String getItemURI(final int index) {
		return getWrapped().getItemURI(index);
	}

	public String getCollectionURI(final int index) {
		return getWrapped().getCollectionURI(index);
	}
	
	public String getURI() {
		return getWrapped().getURI();
	}

	/**
	 * Get the value of a custom field.
	 * @param key
	 * @return
	 */
	public String getValue(final String key) {
		return getWrapped().getValue(key);
	}

	/**
	 * Sort by the given field
	 * 
	 * @param fieldName
	 *            the field
	 * @param ascending
	 *            1 for ascending, 0 for descending
	 */
	public String sortBy(final String fieldName, final int ascending) {
		return getWrapped().sortBy(fieldName, ascending);
	}

	public String sortCollectionsBy(final String fieldName, final int ascending) {
		return getWrapped().sortCollectionsBy(fieldName, ascending);
	}

}
