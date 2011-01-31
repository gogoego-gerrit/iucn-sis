package org.iucn.sis.client.tabs;

import com.extjs.gxt.ui.client.widget.Layout;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.solertium.util.events.SimpleListener;

public abstract class PageContainer extends LayoutContainer {
	
	public PageContainer() {
		super();
	}
	
	public PageContainer(Layout layout) {
		super(layout);
	}
	
}
