package org.iucn.sis.shared.api.structures;


import java.util.ArrayList;
import java.util.Map;

import org.iucn.sis.shared.api.models.PrimitiveField;
import org.iucn.sis.shared.api.models.primitivefields.TextPrimitiveField;
import org.iucn.sis.shared.api.utils.clipboard.Clipboard;
import org.iucn.sis.shared.api.utils.clipboard.UsesClipboard;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;

public class SISTextArea extends SISPrimitiveStructure<String> implements UsesClipboard {

	/*
	 * Specified by data.get("size").  I explicitly want 
	 * enumeration here in order to maintain consistency
	 */
	private enum NarrativeSize {
		SMALL("small", "100px", "50px"), 
		MEDIUM("medium", "200px", "100px"), 
		LARGE("large", "400px", "200px"),
		XL("xl", "500px", "300px"),
		XXL("xxl", "600px", "400px");
		
		public static NarrativeSize fromString(String value) {
			for (NarrativeSize current : NarrativeSize.values())
				if (current.matches(value))
					return current;
			return MEDIUM; 
		}
		
		private String name, width, height;
		
		private NarrativeSize(String name, String width, String height) {
			this.name = name;
			this.width = width;
			this.height = height;
		}
		
		public String getHeight() {
			return height;
		}
		
		public String getWidth() {
			return width;
		}
		
		public boolean matches(String value) {
			return value != null && name.equals(value.toLowerCase());
		}
		
	}
	
	private TextArea textarea;

	public SISTextArea(String struct, String descript, String structID, Object data) {
		super(struct, descript, structID, data);
		// displayPanel = new VerticalPanel();
		buildContentPanel(Orientation.VERTICAL);
	}
	
	@Override
	protected PrimitiveField<String> getNewPrimitiveField() {
		return new TextPrimitiveField(getId(), null);
	}

	@Override
	public void clearData() {
		textarea.setText("");
	}

	public void copyToClipboard() {
		Clipboard.getInstance().add(textarea.getText(), description);
	}

	@Override
	public Widget createLabel() {
		clearDisplayPanel();
		displayPanel.add(descriptionLabel);
		displayPanel.add(textarea);
		return displayPanel;
	}

	@Override
	public Widget createViewOnlyLabel() {
		clearDisplayPanel();
		displayPanel.add(descriptionLabel);
		displayPanel.add(new HTML(textarea.getText()));
		return displayPanel;
	}

	@SuppressWarnings("unchecked")
	public void createWidget() {
		NarrativeSize size;
		if (data != null && data instanceof Map)
			size = NarrativeSize.fromString((String)((Map)data).get("size"));
		else
			size = NarrativeSize.MEDIUM;
		
		descriptionLabel = new HTML(description);
		textarea = new TextArea();
		textarea.setSize(size.getWidth(), size.getHeight());
	}

	@Override
	public String getData() {
		String value = textarea.getText();
		return ("".equals(value)) ? null : value;
	}

	/**
	 * Pass in the raw data from an Assessment object, and this will return
	 * it in happy, displayable String form
	 * 
	 * @return ArrayList of Strings, having converted the rawData to nicely
	 *         displayable String data. Happy days!
	 */
	@Override
	public int getDisplayableData(ArrayList<String> rawData, ArrayList<String> prettyData, int offset) {
		prettyData.add(offset, rawData.get(offset));
		return ++offset;
	}

	public TextArea getTextarea() {
		return textarea;
	}

	public void pasteFromClipboard(ArrayList<Object> items) {
		String text = textarea.getText();
		for (int i = 0; i < items.size(); i++)
			text += (String) items.get(i) + "<br/>";
		textarea.setText(text);
	}
	
	@Override
	public void setData(PrimitiveField<String> field) {
		String datum = field != null ? field.getValue() : "";
		textarea.setText(datum);
	}

	
	@Override
	public void setEnabled(boolean isEnabled) {
		this.textarea.setEnabled(isEnabled);
	}

}
