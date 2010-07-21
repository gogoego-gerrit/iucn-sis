/*
 * Copyright (C) 2007-2009 Solertium Corporation
 *
 * This file is part of the open source GoGoEgo project.
 *
 * Unless you have been granted a different license in writing by the
 * copyright holders for GoGoEgo, you may only modify or redistribute
 * this code under the terms of one of the following licenses:
 * 
 * 1) The Eclipse Public License, v.1.0
 *    http://www.eclipse.org/legal/epl-v10.html
 *
 * 2) The GNU General Public License, version 2 or later
 *    http://www.gnu.org/licenses
 */
package com.solertium.util.extjs.client;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.DelayedTask;
import com.google.gwt.user.client.ui.KeyboardListenerAdapter;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class ViewerFilterTextBox<T extends ModelData> extends TextBox {
	private ListStore<T> viewer;
	private int delay = 300;
	private DelayedTask task;

	public ViewerFilterTextBox() {
		super();

		task = new DelayedTask(new Listener<BaseEvent>() {
			public void handleEvent(BaseEvent be) {
				applyFilters();
				setFocus(true);
			}
		});

		addKeyboardListener(new KeyboardListenerAdapter() {
			@Override
			public void onKeyPress(Widget sender, char keyCode, int modifiers) {
				task.delay(delay);
			}
		});
	}

	protected void applyFilters() {
		viewer.applyFilters("");
	}

	public void bind(ListStore<T> viewer) {
		this.viewer = viewer;
	}

	/**
	 * Returns the delay.
	 * 
	 * @return the delay in milliseconds
	 */
	public int getDelay() {
		return delay;
	}

	/**
	 * Sets the delay. Default value is 300.
	 * 
	 * @param delay
	 *            the delay in milliseconds
	 */
	public void setDelay(int delay) {
		this.delay = delay;
	}

}