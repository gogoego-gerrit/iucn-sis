package org.iucn.sis.client.components.tabs;

import org.iucn.sis.client.components.panels.PanelManager;

import com.extjs.gxt.ui.client.fx.Draggable;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;

public class TaxomaticTab extends TabItem {
	private PanelManager panelManager = null;

	/**
	 * Defaults to having Style.NONE
	 */
	public TaxomaticTab(PanelManager manager) {
		super();
		panelManager = manager;

		build();
	}

	public void build() {
		setText("Taxonomy Editor");

		setLayout(new FlowLayout());

		ContentPanel testPanel = new ContentPanel();
		testPanel.setStyleName("x-panel");
		testPanel.setWidth(100);
		testPanel.setHeight(100);

		testPanel.setStyleAttribute("cursor", "move");
		testPanel.setHeading("Test Panel");

		testPanel.addText("Woo hoo!");
		testPanel.disableTextSelection(true);

		Draggable d = new Draggable(testPanel);
		d.setContainer(this);
		d.setUseProxy(false);

		add(testPanel);

		layout();
	}

}
