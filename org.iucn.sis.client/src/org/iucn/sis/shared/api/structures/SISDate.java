package org.iucn.sis.shared.api.structures;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import org.iucn.sis.client.api.utils.FormattedDate;
import org.iucn.sis.shared.api.models.PrimitiveField;
import org.iucn.sis.shared.api.models.primitivefields.DatePrimitiveField;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;
import com.solertium.util.gwt.api.DatePicker;

public class SISDate extends Structure {

	private DatePicker datePicker;

	public SISDate(String struct, String descript, String structID) {
		super(struct, descript, structID);
		buildContentPanel(Orientation.HORIZONTAL);
	}

	@Override
	protected DatePrimitiveField getNewPrimitiveField() {
		return new DatePrimitiveField(getId(), null);
	}
	
	@Override
	public void clearData() {
		datePicker.clearText();
	}

	@Override
	public Widget createLabel() {
		clearDisplayPanel();
		displayPanel.add(descriptionLabel);
		displayPanel.add(datePicker);
		return displayPanel;
	}

	@Override
	public Widget createViewOnlyLabel() {
		clearDisplayPanel();
		displayPanel.add(descriptionLabel);
		displayPanel.add(new HTML(datePicker.getText()));
		return displayPanel;
	}

	@Override
	public void createWidget() {
		descriptionLabel = new HTML(description);
		datePicker = new DatePicker("images/office-calendar.png", DatePicker.InternationalDate, false, false);
		datePicker.setSeparator("-");
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
	public String getData() {
		return datePicker.getText();
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

	@Override
	public void setData(Map<String, PrimitiveField> data) {
		super.setData(data);
		Date datum = data.containsKey(getId()) ? ((DatePrimitiveField)data.get(getId())).getValue() : null;
		if( datum != null )
			datePicker.setText(FormattedDate.impl.getDate(datum));
		else
			datePicker.setText("");
	}

	@Override
	public void setEnabled(boolean isEnabled) {
		datePicker.setEnabled(isEnabled);
	}

	@Override
	public String toXML() {
		return StructureSerializer.toXML(this);
	}

}
