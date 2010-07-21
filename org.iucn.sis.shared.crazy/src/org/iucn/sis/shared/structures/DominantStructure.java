package org.iucn.sis.shared.structures;

import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.KeyboardListener;

public abstract class DominantStructure extends Structure {

	public DominantStructure(String struct, String descript) {
		super(struct, descript);
	}

	public DominantStructure(String struct, String descript, Object data) {
		super(struct, descript, data);
	}

	public abstract void addListenerToActiveStructure(ChangeListener changeListener, ClickListener clickListener,
			KeyboardListener keyboardListener);

	// public abstract Object getActiveStructure();

	/*
	 * public abstract void addListenerToActiveStructure(ChangeListener
	 * changeListener, ClickListener clickListener);
	 */

	public abstract boolean isActive(Rule activityRule);

}
