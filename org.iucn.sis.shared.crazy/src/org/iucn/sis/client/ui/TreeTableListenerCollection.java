package org.iucn.sis.client.ui;

import java.util.Iterator;
import java.util.Vector;

/**
 * Shameless copy of com.google.gwt.user.client.ui.TreeListenerCollection.
 * Changed to replace TreeListener with TreeTableListener.
 * 
 * A helper class for implementers of the SourcesClickEvents interface. This
 * subclass of Vector assumes that all objects added to it will be of type
 * {@link com.google.gwt.user.client.ui.ClickListener}.
 */
public class TreeTableListenerCollection extends Vector {

	/**
	 * Fires a "tree item selected" event to all listeners.
	 * 
	 * @param item
	 *            the tree item being selected.
	 */
	public void fireItemSelected(TreeItem item) {
		for (Iterator it = iterator(); it.hasNext();) {
			TreeTableListener listener = (TreeTableListener) it.next();
			listener.onTreeItemSelected(item);
		}
	}

	/**
	 * Fires a "tree item state changed" event to all listeners.
	 * 
	 * @param item
	 *            the tree item whose state has changed.
	 */
	public void fireItemStateChanged(TreeItem item) {
		for (Iterator it = iterator(); it.hasNext();) {
			TreeTableListener listener = (TreeTableListener) it.next();
			listener.onTreeItemStateChanged(item);
		}
	}
}
