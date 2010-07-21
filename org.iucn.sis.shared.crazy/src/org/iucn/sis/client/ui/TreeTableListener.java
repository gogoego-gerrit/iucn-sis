package org.iucn.sis.client.ui;

import java.util.EventListener;

/**
 * Shameless copy of com.google.gwt.user.client.ui.TreeListener. Changed to
 * replace GWT's TreeItem with the altered TreeItem.
 * 
 * Event listener interface for tree events.
 */
public interface TreeTableListener extends EventListener {

	/**
	 * Fired when a tree item is selected.
	 * 
	 * @param item
	 *            the item being selected.
	 */
	void onTreeItemSelected(TreeItem item);

	/**
	 * Fired when a tree item is opened or closed.
	 * 
	 * @param item
	 *            the item whose state is changing.
	 */
	void onTreeItemStateChanged(TreeItem item);

}