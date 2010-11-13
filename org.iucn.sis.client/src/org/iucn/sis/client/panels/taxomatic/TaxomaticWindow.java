package org.iucn.sis.client.panels.taxomatic;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;

public class TaxomaticWindow extends Window {
	
	public TaxomaticWindow() {
		super();
		setLayout(new FillLayout());
		setSize(TaxonChooser.PANEL_WIDTH + 30, TaxonChooser.PANEL_HEIGHT + 50);
		setButtonAlign(HorizontalAlignment.CENTER);
		getButtonBar().setSpacing(5);
	}

}
