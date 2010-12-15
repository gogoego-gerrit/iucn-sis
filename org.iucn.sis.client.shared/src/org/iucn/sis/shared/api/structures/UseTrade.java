package org.iucn.sis.shared.api.structures;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.iucn.sis.shared.api.data.DisplayData.LookupDataContainer;
import org.iucn.sis.shared.api.data.LookupData;
import org.iucn.sis.shared.api.data.LookupData.LookupDataValue;
import org.iucn.sis.shared.api.debug.Debug;
import org.iucn.sis.shared.api.models.Field;
import org.iucn.sis.shared.api.models.PrimitiveField;
import org.iucn.sis.shared.api.models.fields.UseTradeField;
import org.iucn.sis.shared.api.utils.CanonicalNames;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class UseTrade extends Structure<Field> {

	private ListBox purpose;
	private ListBox source;
	private ListBox formRemoved;
	private CheckBox sub;
	private CheckBox nat;

	private CheckBox intBox;
	private TextBox harvestLevel;

	private ListBox units;
	private CheckBox possibleThreat;

	private TextArea justification;

	public UseTrade(String struct, String descript, String structID, Object data) {
		super(struct, descript, structID, data);
		buildContentPanel(Orientation.VERTICAL);
	}
	
	private Integer getDropDownSelection(ListBox listBox) {
		if (listBox.getSelectedIndex() == -1)
			return 0;
		else
			return Integer.parseInt(listBox.getValue(listBox.getSelectedIndex()));
	}
	
	@Override
	public void save(Field parent, Field field) {
		if (field == null) {
			field = new Field();
			field.setName(getId());
			field.setParent(parent);
		}
		
		UseTradeField proxy = new UseTradeField(field);
		proxy.setPurpose(getDropDownSelection(purpose));
		proxy.setSource(getDropDownSelection(source));
		proxy.setFormRemoved(getDropDownSelection(formRemoved));
		proxy.setSubsistence(sub.getValue());
		proxy.setNational(nat.getValue());
		proxy.setInternational(intBox.getValue());
		proxy.setHarvestLevel(harvestLevel.getText());
		proxy.setUnits(getDropDownSelection(units));
		proxy.setPossibleThreat(possibleThreat.getValue());
		proxy.setJustification(justification.getText());
	}
	
	@Override
	public boolean hasChanged(Field field) {
		Field fauxParent = new Field(), fauxChild = new Field(CanonicalNames.UseTradeDetails, null);
		
		save(fauxParent, fauxChild);
		
		if (field == null) {
			boolean childHasData = fauxChild.hasData();
			if (childHasData)
				Debug.println("HasChanged in UseTradeDetails: DB has null value, but child hasData, there are {0} primitive fields: \n{1}", fauxChild.getPrimitiveField().size(), fauxChild.getKeyToPrimitiveFields().keySet());
			else
				Debug.println("HasChanged in UseTradeDetails: DB has null value, child has no data, no changes.");
			return childHasData;
		}
		
		if (field.getPrimitiveField().size() != fauxChild.getPrimitiveField().size()) {
			Debug.println("HasChanged in UseTradeDetails: DB has {0} prims, but child has {1}, there are changes\nDB: {2}\nChild: {3}", field.getPrimitiveField().size(), fauxChild.getPrimitiveField().size(), field.getKeyToPrimitiveFields().keySet(), fauxChild.getKeyToPrimitiveFields().keySet());
			return true;
		}
		
		Map<String, PrimitiveField> savedFields = fauxChild.getKeyToPrimitiveFields();
		for (Map.Entry<String, PrimitiveField> entry : savedFields.entrySet()) {
			PrimitiveField oldPrimField = field.getPrimitiveField(entry.getKey());
			if (oldPrimField == null) {
				Debug.println("HasChanged in UseTradeDetails: DB missing new value for {0} of {1}", entry.getKey(), entry.getValue().getRawValue());
				return true;
			}
			
			String oldValue = oldPrimField.getRawValue();
			if ("".equals(oldValue))
				oldValue = null;
			
			String newValue = entry.getValue().getRawValue();
			if ("".equals(newValue))
				newValue = null;
						
			boolean hasChanged = false;
			if (newValue == null) {
				if (oldValue != null)
					hasChanged = true;
			} else {
				if (oldValue == null)
					hasChanged = true;
				else if (!newValue.equals(oldValue))
					hasChanged = true;
			}
			
			Debug.println("HasChanged in UseTradeDetails: Interrogating {0} with DB value {1} and child value {2}, result is {3}", entry.getKey(), oldValue, newValue, hasChanged);
			
			if (hasChanged)
				return hasChanged;
		}
		
		return false;
	}
	
	public void clearData() {
		purpose.setSelectedIndex(0);
		source.setSelectedIndex(0);
		formRemoved.setSelectedIndex(0);
		sub.setValue(false);
		nat.setValue(false);
		intBox.setValue(false);

		harvestLevel.setText("");
		units.setSelectedIndex(0);
		possibleThreat.setValue(false);
		justification.setText("");
		justification.setVisible(false);
	}

	protected Widget createLabel() {
		clearDisplayPanel();

		VerticalPanel wrapper = new VerticalPanel();
		wrapper.setBorderWidth(1);

		HorizontalPanel panel1 = new HorizontalPanel();
		panel1.setSpacing(2);
		panel1.add(wrapInVert("Purpose", purpose));
		panel1.add(wrapInVert("Source", source));
		panel1.add(wrapInVert("Form Removed", formRemoved));

		HorizontalPanel panel2 = new HorizontalPanel();
		panel2.setSpacing(2);
		panel2.add(wrapInVert("Sub", sub));
		panel2.add(wrapInVert("Nat", nat));
		panel2.add(wrapInVert("Int", intBox));
		panel2.add(wrapInVert("Harvest Level ", harvestLevel));
		panel2.add(wrapInVert("Units ", units));
		panel2.add(wrapInVert("Possible Threat: ", possibleThreat));

		final HorizontalPanel panel3 = new HorizontalPanel();
		panel3.add(wrapInVert("Notes and Justification", justification));
		panel3.setVisible(possibleThreat.getValue());

		possibleThreat.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				panel3.setVisible(possibleThreat.getValue());
			}
		});

		wrapper.add(panel1);
		wrapper.add(panel2);
		wrapper.add(panel3);

		displayPanel.add(wrapper);

		return displayPanel;
	}

	protected Widget createViewOnlyLabel() {
		clearDisplayPanel();

		HorizontalPanel panel1 = new HorizontalPanel();
		panel1.setSpacing(5);
		panel1.setBorderWidth(1);

		HorizontalPanel panel2 = new HorizontalPanel();
		panel2.setSpacing(5);
		panel2.setBorderWidth(1);

		HorizontalPanel panel3 = new HorizontalPanel();
		panel3.setSpacing(5);
		panel3.setBorderWidth(1);
		panel3.setWidth("90%");

		for (int i = 0; i < panel1.getWidgetCount(); i++)
			panel1.getWidget(i).setWidth((90 / panel1.getWidgetCount()) + "%");
		for (int i = 0; i < panel2.getWidgetCount(); i++)
			panel2.getWidget(i).setWidth((90 / panel1.getWidgetCount()) + "%");

		panel1.add(new HTML("Purpose: " + purpose.getItemText(purpose.getSelectedIndex())));
		panel1.add(new HTML("Source: " + source.getItemText(source.getSelectedIndex())));
		panel1.add(new HTML("Form Removed: " + formRemoved.getItemText(formRemoved.getSelectedIndex())));

		panel2.add(new HTML("Subsistence: " + sub.getValue()));
		panel2.add(new HTML("National: " + nat.getValue()));
		panel2.add(new HTML("International: " + intBox.getValue()));

		if (!harvestLevel.getText().equals(""))
			panel2.add(new HTML("Harvest Level: " + harvestLevel.getText() + " - "
					+ units.getItemText(units.getSelectedIndex())));

		panel3.add(new HTML("Possible Threat: " + possibleThreat.getValue()));
		if (possibleThreat.getValue()) {
			HTML tempHTML = new HTML("Notes and Justification: " + justification.getText());
			tempHTML.setWordWrap(true);
			panel3.add(tempHTML);
		}

		displayPanel.add(panel1);
		displayPanel.add(panel2);
		displayPanel.add(panel3);

		return displayPanel;
	}

	public void createWidget() {
		purpose = new ListBox();
		populate(purpose, UseTradeField.PURPOSE_KEY);
		
		source = new ListBox();
		populate(source, UseTradeField.SOURCE_KEY);
		
		formRemoved = new ListBox();
		populate(formRemoved, UseTradeField.FORM_REMOVED_KEY);
		
		sub = new CheckBox();
		nat = new CheckBox();
		intBox = new CheckBox();

		harvestLevel = new TextBox();
		units = new ListBox();
		populate(units, UseTradeField.UNITS_KEY);

		possibleThreat = new CheckBox();
		justification = new TextArea();
	}

	/**
	 * Returns an ArrayList of descriptions (as Strings) for this structure, and
	 * if it contains multiples structures, all of those, in order.
	 */
	public ArrayList<String> extractDescriptions() {
		ArrayList<String> ret = new ArrayList<String>();
		ret.add("Purpose");
		ret.add("Source");
		ret.add("Form Removed");
		ret.add("Subsistence");
		ret.add("National");
		ret.add("International");
		ret.add("Harvest Level");
		ret.add("Units");
		ret.add("Possible Threat");
		ret.add("Notes and Justification");
		return ret;
	}
	
	@Override
	public List<ClassificationInfo> getClassificationInfo() {
		return new ArrayList<ClassificationInfo>();
	}

	/**
	 * Returns null for this structure.
	 */
	public String getData() {
		return null;
	}

	/**
	 * Pass in the raw data from an Assessment object, and this will return
	 * it in happy, displayable String form
	 * 
	 * @return ArrayList of Strings, having converted the rawData to nicely
	 *         displayable String data. Happy days!
	 */
	public int getDisplayableData(ArrayList<String> rawData, ArrayList<String> prettyData, int offset) {
		// TODO: Build the drop-down options into Arrays to make them
		// displayable!!

		prettyData.add(offset, DisplayableDataHelper.toDisplayableSingleSelect((String) rawData.get(offset),
				getOptionLabels(UseTradeField.PURPOSE_KEY)));
		offset++;
		prettyData.add(offset, DisplayableDataHelper.toDisplayableSingleSelect((String) rawData.get(offset),
				getOptionLabels(UseTradeField.SOURCE_KEY)));
		offset++;
		prettyData.add(offset, DisplayableDataHelper.toDisplayableSingleSelect((String) rawData.get(offset),
				getOptionLabels(UseTradeField.FORM_REMOVED_KEY)));
		offset++;
		prettyData.add(offset, DisplayableDataHelper.toDisplayableBoolean((String) rawData.get(offset)));
		offset++;
		prettyData.add(offset, DisplayableDataHelper.toDisplayableBoolean((String) rawData.get(offset)));
		offset++;
		prettyData.add(offset, DisplayableDataHelper.toDisplayableBoolean((String) rawData.get(offset)));
		offset++;
		prettyData.add(offset, rawData.get(offset));
		offset++;
		prettyData.add(offset, DisplayableDataHelper.toDisplayableSingleSelect((String) rawData.get(offset), 
				getOptionLabels(UseTradeField.UNITS_KEY)));
		offset++;
		prettyData.add(offset, DisplayableDataHelper.toDisplayableBoolean((String) rawData.get(offset)));
		offset++;
		prettyData.add(offset, rawData.get(offset));
		offset++;

		return offset;
	}

	private String[] getOptionLabels(String lookupKey) {
		LookupDataContainer container;
		try {
			container = (LookupDataContainer)data;
		} catch (ClassCastException e) {
			return new String[0];
		}
		
		if (container != null) {
			LookupData data = container.find(lookupKey);
			if (data != null) {
				String[] values = new String[data.getValues().size()];
				int index = 0;
				for (LookupDataValue value : data.getValues())
					values[index++] = value.getLabel();
				
				return values;
			}
		}
		
		return new String[0];
	}
	
	private void populate(ListBox listbox, String lookupKey) {
		LookupDataContainer container;
		try {
			container = (LookupDataContainer)data;
		} catch (ClassCastException e) {
			return;
		}
		
		if (container != null) {
			LookupData data = container.find(lookupKey);
			if (data != null) {
				listbox.addItem("--- Select ---", "0");
				for (LookupDataValue value : data.getValues())
					listbox.addItem(value.getLabel(), value.getID());
			}
		}
	}
	
	@Override
	public void setData(Field field) {
		UseTradeField proxy = new UseTradeField(field);

		setDropDownSelection(purpose, proxy.getPurpose());
		setDropDownSelection(source, proxy.getSource());
		setDropDownSelection(formRemoved, proxy.getFormRemoved());

		sub.setValue(proxy.getSubsistence());
		nat.setValue(proxy.getNational());
		intBox.setValue(proxy.getInternational());

		harvestLevel.setText(proxy.getHarvestLevel());
		setDropDownSelection(units, proxy.getUnits());
		possibleThreat.setValue(proxy.getPossibleThreat());
		justification.setText(proxy.getJustification());
	}
	
	private void setDropDownSelection(ListBox listBox, Integer value) {
		if (value.intValue() == 0)
			listBox.setSelectedIndex(0);
		else {
			for (int i = 0; i < listBox.getItemCount(); i++) {
				String itemValue = listBox.getValue(i);
				if (value.equals(Integer.parseInt(itemValue))) {
					listBox.setSelectedIndex(i);
					break;
				}
			}
		}
	}

	public void setEnabled(boolean isEnabled) {

	}

	private VerticalPanel wrapInVert(String label, Widget widget) {
		VerticalPanel temp = new VerticalPanel();
		temp.add(new HTML(label));
		temp.add(widget);
		return temp;
	}
}
