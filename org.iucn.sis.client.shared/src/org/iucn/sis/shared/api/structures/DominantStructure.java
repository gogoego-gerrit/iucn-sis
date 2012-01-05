package org.iucn.sis.shared.api.structures;

import org.iucn.sis.shared.api.models.Field;
import org.iucn.sis.shared.api.views.components.Rule;

import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpHandler;

public interface DominantStructure<T> extends DisplayStructure<T, Field> {

	public void addListenerToActiveStructure(ChangeHandler changeListener, ClickHandler clickListener,
			KeyUpHandler keyboardListener);

	// public abstract Object getActiveStructure();

	/*
	 * public abstract void addListenerToActiveStructure(ChangeListener
	 * changeListener, ClickHandler clickListener);
	 */

	public boolean isActive(Rule activityRule);

}
