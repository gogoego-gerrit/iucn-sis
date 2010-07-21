package org.iucn.sis.client.utilities;

import java.util.HashMap;
import java.util.Iterator;

import org.iucn.sis.client.displays.SaveAndShow;

import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.solertium.lwxml.gwt.debug.SysDebugger;

/**
 * A Page in a FlipBook.
 * 
 * @author carl.scott
 * 
 */
public class FlipBookPage extends SimplePanel {

	private SaveAndShow display; // The saveable, displayable object being shown
	private boolean mustBeValidToProceed = true; // Will check page for validity

	// if this is true

	public FlipBookPage(String title, SaveAndShow display) {
		this(title, display, null);
	}

	/**
	 * Creates a new FlipBookPage
	 * 
	 * @param title
	 *            the page title
	 * @param display
	 *            the saveable, displayable object
	 * @param displaySetToUse
	 *            the display set to use to show the object, if any
	 */
	public FlipBookPage(String title, SaveAndShow display, HashMap displaySetToUse) {
		super();

		if (title == null)
			setTitle("");
		else
			setTitle(title);

		this.display = display;

		setWidget(display.show(displaySetToUse));

	}

	public FlipBookPage(String title, Widget display) {
		super();
		if (title == null)
			setTitle("");
		else
			setTitle(title);

		setWidget(display);
	}

	/**
	 * Determines on-screen data validity
	 * 
	 * @return true if valid, false otherwise
	 */
	public boolean isValid() {
		if (mustBeValidToProceed) {
			HashMap currentData = display.saveDataToHashMap();
			Iterator validator = currentData.keySet().iterator();
			while (validator.hasNext()) {
				Object key = validator.next();
				try {
					if (((String) currentData.get(key)).equalsIgnoreCase("")) {
						SysDebugger.getInstance().println("Field with key " + (String) key + " is invalid!");
						return false;
					}
				} catch (Exception e) {
					// Probably CCE b/c of an arraylist, keep it moving
				}
			}
			return true;
		} else
			return true;
	}

	/**
	 * Saves the on-screen data to a HashMap and returns it. This is taken care
	 * of by the SaveAndShow methods
	 * 
	 * @return
	 */
	public HashMap saveData() {
		return display.saveDataToHashMap();
	}

	/**
	 * Sets whether or not data must be non-empty to be valid
	 * 
	 * @param mustBeValidToProceed
	 *            true if so, false otherwise
	 */
	public void setValidation(boolean mustBeValidToProceed) {
		this.mustBeValidToProceed = mustBeValidToProceed;
	}

}// class FlipBooKPage

