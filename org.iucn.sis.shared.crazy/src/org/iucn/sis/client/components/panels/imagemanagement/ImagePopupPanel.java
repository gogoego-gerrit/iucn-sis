package org.iucn.sis.client.components.panels.imagemanagement;

import org.iucn.sis.client.components.panels.PanelManager;

import com.extjs.gxt.ui.client.widget.VerticalPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;

public class ImagePopupPanel extends VerticalPanel {

	public ImagePopupPanel(PanelManager manager) {
		super();
		setStyleName("x-panel");
		setLayoutOnChange(true);
	}

	public void update(ManagedImage image) {
		removeAll();

		Image largerImage = new Image(image.getImage().getUrl());
		add(largerImage);
		
		VerticalPanel details = new VerticalPanel();
		details.add(new HTML("<b>Description: </b>" + image.getField("caption")));
		details.add(new HTML("<b>Credit: </b>" + image.getField("credit")));
		details.add(new HTML("<b>Source: </b>" + image.getField("source")));
		add(details);
		// layout();

	}
}
