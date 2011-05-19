package org.iucn.sis.client.panels.taxomatic;

import org.iucn.sis.client.api.utils.BasicWindow;

import com.extjs.gxt.ui.client.widget.layout.FillLayout;

public class TaxomaticWindow extends BasicWindow {
	
	public TaxomaticWindow(String heading, String iconStyle) {
		super(heading, iconStyle);
		setLayout(new FillLayout());
		setSize(TaxonChooser.PANEL_WIDTH + 30, TaxonChooser.PANEL_HEIGHT + 50);
		
		getButtonBar().setSpacing(5);
	}

}
