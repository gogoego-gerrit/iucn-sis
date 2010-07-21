package org.iucn.sis.client.utilities.clipboard;

import java.util.ArrayList;

/**
 * UsesClipboard.java
 * 
 * An interface that declares that this object can use the clipboard.
 * 
 * @author carl.scott
 * 
 */
public interface UsesClipboard {

	/**
	 * Copies the text contents of this object to the clipboard
	 * 
	 */
	public void copyToClipboard();

	/**
	 * Paste items from the clipboard into the contents of this object.
	 * 
	 * @param items
	 *            the items to paste.
	 */
	public void pasteFromClipboard(ArrayList items);

}
