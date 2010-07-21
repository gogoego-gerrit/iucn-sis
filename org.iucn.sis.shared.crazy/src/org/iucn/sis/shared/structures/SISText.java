package org.iucn.sis.shared.structures;

import java.util.ArrayList;

import org.iucn.sis.shared.xml.XMLUtils;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class SISText extends DominantStructure {

	private TextBox textbox;

	public SISText(String struct, String descript) {
		super(struct, descript);
		// displayPanel = new HorizontalPanel();
		buildContentPanel(Orientation.HORIZONTAL);
	}

	@Override
	public void addListenerToActiveStructure(ChangeListener changeListener, ClickListener clickListener,
			KeyboardListener keyboardListener) {
		textbox.addKeyboardListener(keyboardListener);
	}

	@Override
	public void clearData() {
		textbox.setText("");
	}

	@Override
	public Widget createLabel() {
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
		descriptionLabel = new HTML(description);
		textbox = new TextBox();
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

	public TextBox getTextbox() {
		return textbox;
	}

	@Override
	public boolean isActive(Rule activityRule) {
		try {
			return ((ContentRule) activityRule).matchesContent(textbox.getText());
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public int setData(ArrayList dataList, int dataOffset) {
		super.setData(dataList, dataOffset);
		textbox.setText(XMLUtils.cleanFromXML(dataList.get(dataOffset).toString()));
		return ++dataOffset;
	}

	@Override
	public void setEnabled(boolean isEnabled) {
		this.textbox.setEnabled(isEnabled);
	}

	@Override
	public String toXML() {
		return StructureSerializer.toXML(this);
	}

	// Put back in implements UsesClipboard and enable these, if necessary
	// public void copyToClipboard() {
	// Clipboard.getInstance().add(textbox.getText(), description);
	// }
	//	
	// public void pasteFromClipboard(ArrayList items) {
	// String text = textbox.getText();
	// for (int i = 0; i < items.size(); i++)
	// text += items.get(i) + " ";
	// textbox.setText(text);
	// }
}
