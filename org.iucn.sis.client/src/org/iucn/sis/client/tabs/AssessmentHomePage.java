package org.iucn.sis.client.tabs;

import org.iucn.sis.client.panels.PanelManager;

import com.extjs.gxt.ui.client.fx.Draggable;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;

public class AssessmentHomePage extends TabItem {

	/**
	 * Defaults to having Style.NONE
	 */
	public AssessmentHomePage(PanelManager manager) {
		super();

		build();
	}

	public void build() {
		setText("Assessment Home Page");

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
