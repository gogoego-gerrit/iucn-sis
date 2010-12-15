package org.iucn.sis.shared.api.structures;

import java.util.ArrayList;

import org.iucn.sis.shared.api.models.PrimitiveField;
import org.iucn.sis.shared.api.models.primitivefields.StringPrimitiveField;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;

public class SISHideableNote extends SISPrimitiveStructure<String> {

	private Image showNotes;
	private TextArea textarea;

	public SISHideableNote(String struct, String descript, String structID) {
		super(struct, descript, structID);
		buildContentPanel(Orientation.HORIZONTAL);
	}

	@Override
	protected StringPrimitiveField getNewPrimitiveField() {
		return new StringPrimitiveField(getId(), null);
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
		showNotes.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
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
	public ArrayList<String> extractDescriptions() {
		ArrayList<String> ret = new ArrayList<String>();
		ret.add(description);
		return ret;
	}

	@Override
	public String getData() {
		return textarea.getText();
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
	
	@Override
	public void setData(PrimitiveField<String> field) {
		String datum = field != null ? field.getValue() : "";
		textarea.setText(datum);
	}

	@Override
	public void setEnabled(boolean isEnabled) {
		this.textarea.setEnabled(isEnabled);
	}

	public String toXML() {
		return StructureSerializer.toXML(this);
	}
}
