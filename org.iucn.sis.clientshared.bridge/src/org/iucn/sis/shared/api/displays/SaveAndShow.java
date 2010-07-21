package org.iucn.sis.shared.api.displays;

import java.util.HashMap;

import com.google.gwt.user.client.ui.Widget;

/**
 * An interface that forces object to be able to both display themselves on
 * screen as well as save data about themselves to a HashMap.
 * 
 * @author carl.scott
 */
public interface SaveAndShow {

	public HashMap saveDataToHashMap();

	public Widget show(HashMap displaySetToUse);

}
