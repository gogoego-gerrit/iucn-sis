package org.iucn.sis.client.panels.images;

import org.iucn.sis.client.api.utils.UriBase;
import org.iucn.sis.shared.api.models.TaxonImage;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.google.gwt.user.client.ui.HTML;

public class ImagePopupPanel extends LayoutContainer {

	public ImagePopupPanel(TaxonImage image) {
		super(new FillLayout());
		setStyleName("x-panel");

		final ContentPanel panel = new ContentPanel();
		panel.setBorders(false);
		panel.setBodyBorder(false);
		panel.setHeaderVisible(false);
		panel.setUrl(UriBase.getInstance().getImageBase() + "/images/view/full/" + image.getTaxon().getId() + "/" + image.getFileName());

		final ContentPanel details = new ContentPanel();
		details.setHeading("Image Details");
		details.add(new HTML("<b>Description: </b>" + image.getCaption()));
		details.add(new HTML("<b>Credit: </b>" + image.getCredit()));
		details.add(new HTML("<b>Source: </b>" + image.getSource()));
		
		int size = 150;
		
		final BorderLayoutData south = new BorderLayoutData(LayoutRegion.SOUTH, size, size, size);
		south.setCollapsible(true);
		south.setSplit(false);
		
		final LayoutContainer container = new LayoutContainer(new BorderLayout());
		container.add(panel, new BorderLayoutData(LayoutRegion.CENTER));
		container.add(details, south);
		
		add(container);
	}
}
