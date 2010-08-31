package org.iucn.sis.shared.api.structures;

import org.iucn.sis.shared.api.models.Field;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.KeyboardListener;

public interface DominantStructure<T> extends DisplayStructure<T, Field> {

	public void addListenerToActiveStructure(ChangeListener changeListener, ClickHandler clickListener,
			KeyboardListener keyboardListener);

	// public abstract Object getActiveStructure();

	/*
	 * public abstract void addListenerToActiveStructure(ChangeListener
	 * changeListener, ClickHandler clickListener);
	 */

	public boolean isActive(Rule activityRule);

}
