package com.solertium.util.querybuilder.gwt.client.utils;

import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;

public class ContextMenuItem extends MenuItem {
	
	private final SmartCommand command;
	
	public ContextMenuItem(String text, SmartCommand command) {
		super(text);
		this.command = command;
		addSelectionListener(new SelectionListener<MenuEvent>() {
			public void componentSelected(MenuEvent ce) {
				getCommand().execute();
			}
		});	
	}
	
	public SmartCommand getCommand() {
		return command;
	}

	/*public void setEnabled(boolean isEnabled) {
		super.setEnabled(isEnabled);
		if (isEnabled) {
			removeStyleName("CIPD_gwtTreeEnabled");
		}
		else {
			addStyleName("CIPD_gwtTreeDisabled");
		}
	}*/

}
