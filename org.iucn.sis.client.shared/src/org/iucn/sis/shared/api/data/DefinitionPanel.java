package org.iucn.sis.shared.api.data;

import org.iucn.sis.client.api.caches.DefinitionCache;
import org.iucn.sis.shared.api.models.Definition;

import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.VerticalPanel;

public class DefinitionPanel extends DockPanel {

	public void updateContent(String word) {
		Definition definition = DefinitionCache.impl.getDefinition(word);
		if (definition == null)
			return;
		
		updateContent(definition);
	}
	
	public void updateContent(Definition definition) {
		clear();
		
		HTML wordHTML = new HTML("<b>" + definition.getName() + "</b>");
		HTML definitionHTML = new HTML(definition.getValue());

		add(wordHTML, DockPanel.NORTH);
		add(definitionHTML, DockPanel.CENTER);
		setSpacing(5);

		String lower = definition.getName().toLowerCase();
		if (lower.equals("aoo") || lower.startsWith("area")) {
			VerticalPanel imagePan = new VerticalPanel();
			HTML caption = new HTML("Area of occupancy: no of units x unit area");
			caption.addStyleName("RapidList-ImageCaption");

			imagePan.add(new Image("images/new_aoo.gif"));
			imagePan.add(caption);
			imagePan.setCellHorizontalAlignment(caption, HasHorizontalAlignment.ALIGN_CENTER);

			add(imagePan, DockPanel.WEST);
			setCellHorizontalAlignment(wordHTML, HasHorizontalAlignment.ALIGN_CENTER);
		} else if (lower.equals("eoo") || lower.startsWith("extent")) {
			add(new Image("images/eoo.gif"), DockPanel.WEST);
			setCellHorizontalAlignment(wordHTML, HasHorizontalAlignment.ALIGN_CENTER);
		}
	}

}
