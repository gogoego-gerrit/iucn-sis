package org.iucn.sis.shared.api.structures;

import java.util.ArrayList;
import java.util.Map;

import org.iucn.sis.shared.api.models.PrimitiveField;
import org.iucn.sis.shared.api.models.primitivefields.BooleanPrimitiveField;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.Widget;
import com.solertium.lwxml.gwt.debug.SysDebugger;

public class SISBoolean extends SISPrimitiveStructure<Boolean> implements DominantStructure<PrimitiveField<Boolean>> {

	private CheckBox checkbox;

	private boolean defaultValue = false;

	public SISBoolean(String struct, String descript, String structID) {
		super(struct, descript, structID);
		// displayPanel = new ContentPanel();
		buildContentPanel(Orientation.HORIZONTAL);

		if ("true".equals(data))
			defaultValue = true;
	}

	@Override
	public void addListenerToActiveStructure(ChangeListener changeListener, ClickHandler clickListener,
			KeyboardListener keyboardListener) {
		checkbox.addClickHandler(clickListener);
		DOM.setEventListener(checkbox.getElement(), checkbox);
	}

	@Override
	public void clearData() {
		checkbox.setChecked(false);
	}

	@Override
	public Widget createLabel() {
		clearDisplayPanel();
		displayPanel.add(descriptionLabel);
		displayPanel.add(checkbox);
		return displayPanel;
	}

	@Override
	public Widget createViewOnlyLabel() {
		clearDisplayPanel();
		displayPanel.add(descriptionLabel);
		displayPanel.add(new HTML((checkbox.isChecked() ? "Yes" : "No")));
		return displayPanel;
	}

	@Override
	public void createWidget() {
		descriptionLabel = new HTML(description);
		checkbox = new CheckBox();
	}

	public String getData() {
		return checkbox.getValue().booleanValue() ? "true" : null;
	}
	
	@Override
	protected BooleanPrimitiveField getNewPrimitiveField() {
		return new BooleanPrimitiveField(getId(), null);
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
		prettyData.add(offset, DisplayableDataHelper.toDisplayableBoolean((String) rawData.get(offset)));
		return ++offset;
	}
	
	@Override
	public boolean hasChanged(PrimitiveField<Boolean> field) {
		boolean oldValue = field == null ? defaultValue : field.getValue();
		
		boolean newValue = checkbox.isChecked();
		
		return oldValue != newValue;
	}

	@Override
	public boolean isActive(Rule activityRule) {
		if (activityRule != null && activityRule instanceof BooleanRule) {
			if (((BooleanRule) activityRule).isTrue()) {
				return checkbox.isChecked();
			} else {
				return !checkbox.isChecked();
			}
		}
		else
			return false;
	}
	
	@Override
	public void setData(PrimitiveField<Boolean> field) {
		if (field != null)
			checkbox.setValue((field.getValue()).booleanValue());			
	}

	public void setEnabled(boolean isEnabled) {
		this.checkbox.setEnabled(isEnabled);
	}

}// class SISBoolean
