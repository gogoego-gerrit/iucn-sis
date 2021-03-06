package org.iucn.sis.shared.structures;

import java.util.ArrayList;

import org.iucn.sis.client.utilities.autocomplete.AutoCompleteTextBox;
import org.iucn.sis.client.utilities.autocomplete.SimpleAutoCompletionItems;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

public class SISAutoComplete extends Structure {

	public AutoCompleteTextBox textbox;

	public SISAutoComplete(String struct, String descript) {
		this(struct, descript, null);
	}

	public SISAutoComplete(String struct, String descript, Object data) {
		// the data is an arraylist of auto complete terms
		super(struct, descript, data);
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
	public void createWidget() {
		descriptionLabel = new HTML(getDescription());
		textbox = new AutoCompleteTextBox();
		try {
			textbox.setCompletionItems(new SimpleAutoCompletionItems((String[]) ((ArrayList) data).toArray()));
		} catch (Exception e) {
			textbox.setCompletionItems(new SimpleAutoCompletionItems(new String[] {}));
		}
	}

	/**
	 * Returns an ArrayList of descriptions (as Strings) for this structure, and
	 * if it contains multiples structures, all of those, in order.
	 */
	@Override
	public ArrayList extractDescriptions() {
		ArrayList ret = new ArrayList();
		ret.add(description);
		return ret;
	}

	@Override
	public Object getData() {
		return textbox.getText();
	}

	/**
	 * Pass in the raw data from an AssessmentData object, and this will return
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
	public int setData(ArrayList dataList, int dataOffset) {
		textbox.setText(dataList.get(dataOffset).toString());

		return ++dataOffset;
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

	protected String toThinXML() {
		return StructureSerializer.toXML(this);
	}

	@Override
	public String toXML() {
		return StructureSerializer.toXML(this);
	}
}
