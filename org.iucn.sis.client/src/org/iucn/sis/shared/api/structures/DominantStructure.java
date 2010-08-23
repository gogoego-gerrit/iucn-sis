package org.iucn.sis.shared.api.structures;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.KeyboardListener;

public abstract class DominantStructure extends Structure {

	public DominantStructure(String struct, String descript, String structID) {
		super(struct, descript, structID);
	}

	public DominantStructure(String struct, String descript, String structID, Object data) {
		super(struct, descript, structID, data);
	}

	public abstract void addListenerToActiveStructure(ChangeListener changeListener, ClickHandler clickListener,
			KeyboardListener keyboardListener);

	// public abstract Object getActiveStructure();

	/*
	 * public abstract void addListenerToActiveStructure(ChangeListener
	 * changeListener, ClickHandler clickListener);
	 */

	public abstract boolean isActive(Rule activityRule);

}
