package org.iucn.sis.shared.api.structures;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.KeyboardListener;

public interface DominantStructure extends DisplayStructure {

	public void addListenerToActiveStructure(ChangeListener changeListener, ClickHandler clickListener,
			KeyboardListener keyboardListener);

	// public abstract Object getActiveStructure();

	/*
	 * public abstract void addListenerToActiveStructure(ChangeListener
	 * changeListener, ClickHandler clickListener);
	 */

	public boolean isActive(Rule activityRule);

}
