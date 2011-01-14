package org.iucn.sis.shared.api.structures;

import java.util.ArrayList;

import org.iucn.sis.client.api.utils.autocomplete.AutoCompleteTextBox;
import org.iucn.sis.client.api.utils.autocomplete.SimpleAutoCompletionItems;
import org.iucn.sis.shared.api.models.PrimitiveField;
import org.iucn.sis.shared.api.models.primitivefields.StringPrimitiveField;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

public class SISAutoComplete extends SISPrimitiveStructure<String> {

	public AutoCompleteTextBox textbox;

	public SISAutoComplete(String struct, String descript, String structID) {
		this(struct, descript, structID, null);
	}

	public SISAutoComplete(String struct, String descript, String structID, Object data) {
		// the data is an arraylist of auto complete terms
		super(struct, descript, structID, data);
		// displayPanel = new ContentPanel();
		buildContentPanel(Orientation.HORIZONTAL);
	}

	@Override
	public void clearData() {
		textbox.setText("");
	}

	@Override
	protected Widget createLabel() {
		clearDisplayPanel();
		displayPanel.add(descriptionLabel);
		displayPanel.add(textbox);
		return displayPanel;
	}

	@Override
	public Widget createViewOnlyLabel() {
		clearDisplayPanel();
		displayPanel.add(descriptionLabel);
		displayPanel.add(new HTML(textbox.getText()));
		return displayPanel;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void createWidget() {
		descriptionLabel = new HTML(getDescription());
		textbox = new AutoCompleteTextBox();
		try {
			textbox.setCompletionItems(new SimpleAutoCompletionItems((String[]) ((ArrayList) data).toArray()));
		} catch (Exception e) {
			textbox.setCompletionItems(new SimpleAutoCompletionItems(new String[] {}));
		}
	}

	@Override
	public String getData() {
		return textbox.getText();
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

	public AutoCompleteTextBox getTextbox() {
		return textbox;
	}
	
	@Override
	public void setData(PrimitiveField<String> field) {
		if (field != null)
			textbox.setText(field.getValue());
	}

	@Override
	public void setEnabled(boolean isEnabled) {
		textbox.setEnabled(isEnabled);
	}

	protected String toDetailedXML() {
		String xmlRetString = "<structure>\n";
		xmlRetString += "\t<text>" + textbox.getText() + "</text>\n";
		xmlRetString += "</structure>\n";
		return xmlRetString;
	}

	@Override
	protected StringPrimitiveField getNewPrimitiveField() {
		return new StringPrimitiveField(getId(), null);
	}
}
