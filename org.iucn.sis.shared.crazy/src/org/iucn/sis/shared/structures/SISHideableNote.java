package org.iucn.sis.shared.structures;

import java.util.ArrayList;

import org.iucn.sis.shared.xml.XMLUtils;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;

public class SISHideableNote extends Structure {

	private Image showNotes;
	private TextArea textarea;

	public SISHideableNote(String struct, String descript) {
		super(struct, descript);
		buildContentPanel(Orientation.HORIZONTAL);
	}

	@Override
	public void clearData() {
		textarea.setText("");
	}

	@Override
	public Widget createLabel() {
		clearDisplayPanel();
		displayPanel.add(descriptionLabel);

		HorizontalPanel tNotes = new HorizontalPanel();
		tNotes.setSpacing(3);
		tNotes.setWidth("100%");
		tNotes.add(showNotes);
		tNotes.add(textarea);
		textarea.setVisible(!textarea.getText().equals(""));

		displayPanel.add(tNotes);
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

		showNotes = new Image("images/icon-note-go.png");
		showNotes.addStyleName("pointerCursor");
		showNotes.setSize("16px", "16px");
		showNotes.addClickListener(new ClickListener() {
			public void onClick(Widget arg0) {
				textarea.setVisible(!textarea.isVisible());
			}
		});
		textarea = new TextArea();
		textarea.setSize("400px", "70px");
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
