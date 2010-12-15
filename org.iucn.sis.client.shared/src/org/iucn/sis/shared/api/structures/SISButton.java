package org.iucn.sis.shared.api.structures;

import java.util.ArrayList;

import org.iucn.sis.shared.api.models.PrimitiveField;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.Widget;

public class SISButton extends SISPrimitiveStructure<String> implements DominantStructure<PrimitiveField<String>> {

	private Button button;
	private boolean toggleIsActive;

	public SISButton(String struct, String descript, String structID) {
		super(struct, descript, structID);
		this.toggleIsActive = false;
		// displayPanel = new ContentPanel();
		buildContentPanel(Orientation.HORIZONTAL);
	}

	@Override
	protected PrimitiveField<String> getNewPrimitiveField() {
		return null;
	}
	
	@Override
	public void addListenerToActiveStructure(ChangeListener changeListener, ClickHandler clickListener,
			KeyboardListener keyboardListener) {
		button.addClickHandler(clickListener);
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

	public Button getButton() {
		return button;
	}

	@Override
	public String getData() {
		return button.getText();
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
	public boolean isActive(Rule activityRule) {
		boolean retVal = this.toggleIsActive;
		this.toggleIsActive = !this.toggleIsActive;
		return retVal;
	}
	
	@Override
	public void setData(PrimitiveField<String> field) {
		if (field != null)
			button.setText(field.getValue());
	}

	@Override
	public void setEnabled(boolean isEnabled) {
		this.button.setEnabled(isEnabled);
	}

	public String toXML() {
		return StructureSerializer.toXML(this);
	}
}
