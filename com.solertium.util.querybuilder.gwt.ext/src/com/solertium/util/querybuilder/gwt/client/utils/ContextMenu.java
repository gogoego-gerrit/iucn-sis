package com.solertium.util.querybuilder.gwt.client.utils;

import com.extjs.gxt.ui.client.widget.menu.Menu;

public abstract class ContextMenu extends Menu {

	public ContextMenu(boolean bool) {
		super();
	}

	public void addItem(ContextMenuItem item) {
		add(item);
	}

}
