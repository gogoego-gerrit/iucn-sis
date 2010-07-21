package org.iucn.sis.client.components.panels;

import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.solertium.lwxml.shared.NativeDocument;

public abstract class BaseContentPanel extends ContentPanel {
	public void addTo(LayoutContainer container) {
		container.layout();

	}

	public abstract void buildFromStringContent(String content);

	public abstract void buildFromXMLContent(NativeDocument content);

	public abstract void doPrimaryRender();
}
