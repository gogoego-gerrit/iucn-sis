package org.iucn.sis.shared.structures;

import java.util.ArrayList;

import org.iucn.sis.client.utilities.clipboard.Clipboard;
import org.iucn.sis.client.utilities.clipboard.UsesClipboard;
import org.iucn.sis.shared.xml.XMLUtils;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;

public class SISTextArea extends Structure implements UsesClipboard {

	private TextArea textarea;

	public SISTextArea(String struct, String descript) {
		super(struct, descript);
		// displayPanel = new VerticalPanel();
		buildContentPanel(Orientation.HORIZONTAL);
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

	@Override
	public void createWidget() {
		descriptionLabel = new HTML(description);
		textarea = new TextArea();
		textarea.setSize("200px", "100px");
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
		return textarea.getText();
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

	public TextArea getTextarea() {
		return textarea;
	}

	public void pasteFromClipboard(ArrayList items) {
		String text = textarea.getText();
		for (int i = 0; i < items.size(); i++)
			text += (String) items.get(i) + "<br/>";
		textarea.setText(text);
	}

	@Override
	public int setData(ArrayList dataList, int dataOffset) {
		super.setData(dataList, dataOffset);
		textarea.setText(XMLUtils.cleanFromXML(dataList.get(dataOffset).toString()));
		return ++dataOffset;
	}

	
	@Override
	public void setEnabled(boolean isEnabled) {
		this.textarea.setEnabled(isEnabled);
	}

	@Override
	public String toXML() {
		return StructureSerializer.toXML(this);
	}
}
