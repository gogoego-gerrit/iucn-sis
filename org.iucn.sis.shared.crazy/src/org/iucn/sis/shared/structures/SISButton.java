package org.iucn.sis.shared.structures;

import java.util.ArrayList;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.Widget;

public class SISButton extends DominantStructure {

	private Button button;
	private boolean toggleIsActive;

	public SISButton(String struct, String descript) {
		super(struct, descript);
		this.toggleIsActive = false;
		// displayPanel = new ContentPanel();
		buildContentPanel(Orientation.HORIZONTAL);
	}

	@Override
	public void addListenerToActiveStructure(ChangeListener changeListener, ClickListener clickListener,
			KeyboardListener keyboardListener) {
		button.addClickListener(clickListener);
		DOM.setEventListener(button.getElement(), button);
	}

	@Override
	public void clearData() {
		// ?
	}

	@Override
	public Widget createLabel() {
		displayPanel.add(this.button);
		return displayPanel;
	}

	@Override
	public Widget createViewOnlyLabel() {
		return createLabel();
	}

	@Override
	public void createWidget() {
		this.button = new Button(this.description);
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

	public Button getButton() {
		return button;
	}

	@Override
	public Object getData() {
		return button.getText();
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

	@Override
	public boolean isActive(Rule activityRule) {
		boolean retVal = this.toggleIsActive;
		this.toggleIsActive = !this.toggleIsActive;
		return retVal;
	}

	@Override
	public int setData(ArrayList dataList, int dataOffset) {
		super.setData(dataList, dataOffset);
		button.setText(dataList.get(dataOffset).toString());
		return ++dataOffset;
	}

	@Override
	public void setEnabled(boolean isEnabled) {
		this.button.setEnabled(isEnabled);
	}

	@Override
	public String toXML() {
		return StructureSerializer.toXML(this);
	}
}
