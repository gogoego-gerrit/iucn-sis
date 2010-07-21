/*******************************************************************************
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
 *     http://www.gnu.org/licenses
 ******************************************************************************/
package com.solertium.util.extjs.client;

import java.util.HashSet;
import java.util.Set;

import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.Layout;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.CardLayout;
import com.google.gwt.user.client.ui.Widget;

public class CardLayoutContainer extends LayoutContainer {

	private Set<Component> items = new HashSet<Component>();

	public CardLayoutContainer() {
		setLayout(new CardLayout());
	}

	@Override
	protected boolean add(Component component) {
		if (items.add(component)) {
			super.add(component);
			return true;
		}
		return false;
	}

	private boolean doRemove(Widget widget) {
		if (super.remove(widget)) {
			items.remove(widget);
			return true;
		}
		return false;
	}

	@Override
	public CardLayout getLayout() {
		return ((CardLayout) super.getLayout());
	}

	@Override
	public boolean remove(Widget widget) {
		return doRemove(widget);
	}

	@Override
	public boolean removeAll() {
		int count = getItemCount();
		for (int i = 0; i < count; i++) {
			doRemove(getItem(0));
		}
		return getItemCount() == 0;
	}

	@Override
	/*
	 * Does not need to be called, as this container is set to use a CardLayout
	 * by default. This will reset the layout of this container, but it MUST be
	 * of type CardLayout or this will throw an Error.
	 */
	public void setLayout(Layout layout) {
		if (layout instanceof CardLayout)
			super.setLayout(layout);
		else
			throw new Error("Layout for CardLayoutContainer must be of type CardLayout.");
	}

	public void switchToComponent(Component cp) {
		add(cp);
		(getLayout()).setActiveItem(cp);
		layout();
	}
}
