package org.iucn.sis.shared.api.data;

import org.iucn.sis.client.api.caches.DefinitionCache;
import org.iucn.sis.shared.api.models.Definition;

import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.VerticalPanel;

public class DefinitionPanel extends DockPanel {
	public DefinitionPanel() {

	}

	public void updateContent(String word) {
		clear();

		Definition definition = DefinitionCache.impl.getDefinition(word);

		HTML wordHTML = new HTML("<b>" + word + "</b>");
		HTML definitionHTML = new HTML(definition.getValue());

		add(wordHTML, DockPanel.NORTH);
		add(definitionHTML, DockPanel.CENTER);
		setSpacing(5);

		if (word.equalsIgnoreCase("aoo") || word.toLowerCase().startsWith("area")) {
			VerticalPanel imagePan = new VerticalPanel();
			HTML caption = new HTML("Area of occupancy: no of units x unit area");
			caption.addStyleName("RapidList-ImageCaption");

			imagePan.add(new Image("images/new_aoo.gif"));
			imagePan.add(caption);
			imagePan.setCellHorizontalAlignment(caption, HasHorizontalAlignment.ALIGN_CENTER);

			add(imagePan, DockPanel.WEST);
			setCellHorizontalAlignment(wordHTML, HasHorizontalAlignment.ALIGN_CENTER);
		} else if (word.equalsIgnoreCase("eoo") || word.toLowerCase().startsWith("extent")) {
			add(new Image("images/eoo.gif"), DockPanel.WEST);
			setCellHorizontalAlignment(wordHTML, HasHorizontalAlignment.ALIGN_CENTER);
		}
	}

}
