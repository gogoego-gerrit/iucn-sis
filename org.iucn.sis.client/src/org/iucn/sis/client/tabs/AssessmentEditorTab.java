package org.iucn.sis.client.tabs;

import org.iucn.sis.client.panels.PanelManager;

import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;

public class AssessmentEditorTab extends TabItem {
	private PanelManager panelManager = null;

	/**
	 * Defaults to having Style.NONE
	 */
	public AssessmentEditorTab(PanelManager manager) {
		super();
		panelManager = manager;

		build();
	}

	public void build() {
		setText("Assessment Data Browser");

		setLayout(new FitLayout());

		add(panelManager.DEM);
		System.out.println("DEM Panel is added. Swanky.");
//		panelManager.addPanel(this, panelManager.DEM, null, false, false);
//		panelManager.layoutPanel(panelManager.DEM);

//		layout();
	}

}
