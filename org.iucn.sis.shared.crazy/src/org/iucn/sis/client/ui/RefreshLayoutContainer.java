package org.iucn.sis.client.ui;

import com.extjs.gxt.ui.client.widget.LayoutContainer;

public abstract class RefreshLayoutContainer extends LayoutContainer {

	public RefreshLayoutContainer() {
		super();
	}

	public abstract void refresh();
}
