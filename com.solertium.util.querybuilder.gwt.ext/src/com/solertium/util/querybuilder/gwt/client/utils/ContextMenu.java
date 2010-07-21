package com.solertium.util.querybuilder.gwt.client.utils;

import com.extjs.gxt.ui.client.widget.menu.Menu;

public abstract class ContextMenu extends Menu {

	public ContextMenu(boolean bool) {
		super();
	}

	public void closeOnClick() {
		getParent().removeFromParent();
	}
	
	public void addItem(ContextMenuItem item) {
		if (item.getCommand() != null)
			item.getCommand().setCloseListener(new SmartCommand.CloseListener() {
				public void onClose() {
					closeOnClick();
				}
			});
		add(item);
	}

	/*public void onBrowserEvent(Event event) {
		MenuItem item = findItem(DOM.eventGetTarget(event));
		switch (DOM.eventGetType(event)) {
			case Event.ONCLICK: {
				//Fire an item's command when the user clicks on it.
				if (item != null && item instanceof ContextMenuItem &&
					((ContextMenuItem)item).isEnabled) {
					super.onBrowserEvent(event);
				}
				break;
			}
			default:
				super.onBrowserEvent(event);
		}
	}

	private MenuItem findItem(Element hItem) {
		for (int i = 0; i < getItems().size(); ++i) {
			MenuItem item = getItems().get(i);
			if (DOM.isOrHasChild(item.getElement(), hItem))
				return item;
		}

		return null;
	}*/
}
