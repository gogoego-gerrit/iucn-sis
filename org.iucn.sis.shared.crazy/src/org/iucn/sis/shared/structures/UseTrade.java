package org.iucn.sis.shared.structures;

import java.util.ArrayList;
import java.util.HashMap;

import org.iucn.sis.shared.xml.XMLUtils;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class UseTrade extends Structure {

	public static ArrayList generateDefaultDataList() {
		ArrayList dataList = new ArrayList();
		dataList.add("0");
		dataList.add("0");
		dataList.add("0");
		dataList.add("false");
		dataList.add("false");
		dataList.add("false");
		dataList.add("");
		dataList.add("0");
		dataList.add("false");
		dataList.add("");

		return dataList;
	}

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

	private final String[] purposeOptions = new String[] { "Food - human", "Food - animal",
			"Medicine - human and veterinary", "Poisons", "Manufacturing chemicals", "Other chemicals", "Fuel",
			"Fibre", "Construction/structural materials", "Wearing apparel, accessories", "Other household goods",
			"Handicrafts, jewellery, decorations, curios, etc.", "Pets/display animals, horticulture", "Research",
			"Sport hunting/specimen collecting", "Other", "Unknown" };

	private final String[] sourceOptions = new String[] { "Wild", "Captive breeding/farming", "Ranching - ex situ",
			"Ranching - in situ", "Other", "Unknown", };

	private final String[] formRemovedOptions = new String[] { "Whole animal/plant", "Parts - non-lethal removal",
			"Parts - lethal removal", "Eggs, fruits, seeds", "Other", "Unknown" };

	private final String[] unitsOptions = new String[] { "Volume (cubic metres)", "Weight (in kilograms)",
			"Number of Individuals" };

	public UseTrade(String struct, String descript) {
		super(struct, descript);
		buildContentPanel(Orientation.VERTICAL);
	}

	public void clearData() {
		purpose.setSelectedIndex(0);
		source.setSelectedIndex(0);
		formRemoved.setSelectedIndex(0);
		sub.setChecked(false);
		nat.setChecked(false);
		intBox.setChecked(false);

		harvestLevel.setText("");
		units.setSelectedIndex(0);
		possibleThreat.setChecked(false);
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
		panel3.setVisible(possibleThreat.isChecked());

		possibleThreat.addClickListener(new ClickListener() {
			public void onClick(Widget sender) {
				panel3.setVisible(possibleThreat.isChecked());
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

		panel2.add(new HTML("Subsistence: " + sub.isChecked()));
		panel2.add(new HTML("National: " + nat.isChecked()));
		panel2.add(new HTML("International: " + intBox.isChecked()));

		if (!harvestLevel.getText().equals(""))
			panel2.add(new HTML("Harvest Level: " + harvestLevel.getText() + " - "
					+ units.getItemText(units.getSelectedIndex())));

		panel3.add(new HTML("Possible Threat: " + possibleThreat.isChecked()));
		if (possibleThreat.isChecked()) {
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
		purpose.addItem(" --- Select --- ");
		purpose.addItem("Food - human", "Food and beverages for human consumption/nutrition");
		purpose.addItem("Food - animal", "Food and liquids for consumption by domestic/captive animals");
		purpose
				.addItem(
						"Medicine - human and veterinary",
						"Materials administered specifically to treat or prevent a specific illness of injury.  Items administered as vitamins, tonics etc. should be included under food");
		purpose.addItem("Poisons", "Eg. pesticides, herbicides, fish poisons");
		purpose.addItem("Manufacturing chemicals",
				"Eg. solvents, dyes, adhesives, resins, etc. whether for domestic or commerical/industrial use");
		purpose.addItem("Other chemicals", "Eg. Incense, perfumes, cosmetics");
		purpose.addItem("Fuel", "Including wood (and charcoal production therefrom), grasses, etc.");
		purpose.addItem("Fibre", "Eg. for weaving, sewing, rope, paper, thatch, etc.");
		purpose.addItem("Construction/structural materials", "Eg. supports, timber, fencing, etc.");
		purpose.addItem("Wearing apparel, accessories", "Eg. clothing, footwear, belts, bags, trimmings");
		purpose
				.addItem("Other household goods",
						"Eg. containers, furnishings, etc. with primarily utilitarian functions, though potentially highly decorated");
		purpose.addItem("Handicrafts, jewelry, decorations, curios, etc.",
				"Finished goods with primarily ornamental/decorative rather than utilitarian functions");
		purpose.addItem("Pets/display animals, horticulture",
				"Includes animals used as pets and for display (eg. in zoos, aquaria, circuses)");
		purpose
				.addItem(
						"Research",
						"Includes specimens used in or as the subject of any type of research (eg. behavioural, medicine, propogation, disease resistance, etc.");
		purpose.addItem("Sport hunting/specimen collecting");
		purpose.addItem("Other", "Please specify in the Notes section below");
		purpose.addItem("Unknown", "Purpose is unknown");

		source = new ListBox();
		source.addItem(" --- Select --- ");
		source
				.addItem(
						"Wild",
						"Specimens taken from natural habitat, with no human intervention in terms of enhancing individual survival or production");
		source
				.addItem(
						"Captive breeding/farming",
						"Production of offspring in a controlled environment (ex situ) either from parents produced in captivity (F1) or from parents taken from the wild but maintained in captivity, where there is little further input from the wild, eg. essentially a closed cycle");
		source
				.addItem(
						"Ranching - ex situ",
						"Production of saleable specimens from eggs (including within gravid females), juveniles, immature plant specimens removed from the wild and raised ex site prior to commercial sale");
		source
				.addItem(
						"Ranching - in situ",
						"Specimens maintained within confined areas of wild habitat, with or without other forms of manipulation, eg. habitat manipulation");
		source.addItem("Other", "Please specify in the Notes section below");
		source.addItem("Unknown", "Source is unknown");

		formRemoved = new ListBox();
		formRemoved.addItem(" --- Select --- ");
		formRemoved.addItem("Whole animal/plant", "Removal of the whole individual from the wild population.");
		formRemoved
				.addItem(
						"Parts - non-lethal removal",
						"Removal of parts without obviously increasing the risk of death or decreasing reproductive ability of the individual, ie. so that it remains a functional part of the wild population");
		formRemoved
				.addItem(
						"Parts - lethal removal",
						"Removal of parts resulting in the death and or/reproductive incapacity of the individual and therefore its biological removal from the wild population.");
		formRemoved.addItem("Eggs, fruits, seeds",
				"Removal of eggs from gravid females should be included under 'parts' above.");
		formRemoved.addItem("Other", "Please specify in the Notes section below");
		formRemoved.addItem("Unknown", "Source is unknown");

		sub = new CheckBox();
		nat = new CheckBox();
		intBox = new CheckBox();

		harvestLevel = new TextBox();
		units = new ListBox();
		units.addItem("Volume (cubic metres)");
		units.addItem("Weight (in kilograms)");
		units.addItem("Number of Individuals");

		possibleThreat = new CheckBox();
		justification = new TextArea();
	}

	/**
	 * Returns an ArrayList of descriptions (as Strings) for this structure, and
	 * if it contains multiples structures, all of those, in order.
	 */
	public ArrayList extractDescriptions() {
		ArrayList ret = new ArrayList();
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

	/**
	 * Returns null for this structure.
	 */
	public Object getData() {
		return null;
	}

	/**
	 * Pass in the raw data from an AssessmentData object, and this will return
	 * it in happy, displayable String form
	 * 
	 * @return ArrayList of Strings, having converted the rawData to nicely
	 *         displayable String data. Happy days!
	 */
	public int getDisplayableData(ArrayList<String> rawData, ArrayList<String> prettyData, int offset) {
		// TODO: Build the drop-down options into Arrays to make them
		// displayable!!

		prettyData.add(offset, DisplayableDataHelper.toDisplayableSingleSelect((String) rawData.get(offset),
				purposeOptions));
		offset++;
		prettyData.add(offset, DisplayableDataHelper.toDisplayableSingleSelect((String) rawData.get(offset),
				sourceOptions));
		offset++;
		prettyData.add(offset, DisplayableDataHelper.toDisplayableSingleSelect((String) rawData.get(offset),
				formRemovedOptions));
		offset++;
		prettyData.add(offset, DisplayableDataHelper.toDisplayableBoolean((String) rawData.get(offset)));
		offset++;
		prettyData.add(offset, DisplayableDataHelper.toDisplayableBoolean((String) rawData.get(offset)));
		offset++;
		prettyData.add(offset, DisplayableDataHelper.toDisplayableBoolean((String) rawData.get(offset)));
		offset++;
		prettyData.add(offset, rawData.get(offset));
		offset++;
		prettyData.add(offset, unitsOptions[Integer.parseInt((String) rawData.get(offset))]);
		offset++;
		prettyData.add(offset, DisplayableDataHelper.toDisplayableBoolean((String) rawData.get(offset)));
		offset++;
		prettyData.add(offset, rawData.get(offset));
		offset++;

		return offset;
	}

	public String[] getFormRemovedOptions() {
		return formRemovedOptions;
	}

	public String[] getPurposeOptions() {
		return purposeOptions;
	}

	public String[] getSourceOptions() {
		return sourceOptions;
	}

	public String[] getUnitsOptions() {
		return unitsOptions;
	}

	public HashMap getValues() {
		return new HashMap();
	}

	public int setData(ArrayList dataList, int dataOffset) {
		super.setData(dataList, dataOffset);
		purpose.setSelectedIndex(Integer.parseInt(dataList.get(dataOffset++).toString()));
		source.setSelectedIndex(Integer.parseInt(dataList.get(dataOffset++).toString()));
		formRemoved.setSelectedIndex(Integer.parseInt(dataList.get(dataOffset++).toString()));

		sub.setChecked((dataList.get(dataOffset++).toString().equals("true") ? true : false));
		nat.setChecked((dataList.get(dataOffset++).toString().equals("true") ? true : false));
		intBox.setChecked((dataList.get(dataOffset++).toString().equals("true") ? true : false));

		harvestLevel.setText(XMLUtils.cleanFromXML(dataList.get(dataOffset++).toString()));
		units.setSelectedIndex(Integer.parseInt(dataList.get(dataOffset++).toString()));
		possibleThreat.setChecked((dataList.get(dataOffset++).toString().equals("true") ? true : false));
		justification.setText(XMLUtils.cleanFromXML(dataList.get(dataOffset++).toString()));

		return dataOffset;
	}

	protected void setDataValues(HashMap fieldData) {
	}

	protected void setEnabled(boolean isEnabled) {

	}

	public String toXML() {
		String xml = "<structure>" + purpose.getSelectedIndex() + "</structure>\r\n";
		xml += "<structure>" + source.getSelectedIndex() + "</structure>\r\n";
		xml += "<structure>" + formRemoved.getSelectedIndex() + "</structure>\r\n";
		xml += "<structure>" + sub.isChecked() + "</structure>\r\n";
		xml += "<structure>" + nat.isChecked() + "</structure>\r\n";
		xml += "<structure>" + intBox.isChecked() + "</structure>\r\n";

		xml += "<structure>" + XMLUtils.clean(harvestLevel.getText()) + "</structure>\r\n";
		xml += "<structure>" + units.getSelectedIndex() + "</structure>\r\n";
		xml += "<structure>" + possibleThreat.isChecked() + "</structure>\r\n";

		xml += "<structure>" + XMLUtils.clean(justification.getText()) + "</structure>\r\n";

		return xml;
	}

	private VerticalPanel wrapInVert(String label, Widget widget) {
		VerticalPanel temp = new VerticalPanel();
		temp.add(new HTML(label));
		temp.add(widget);
		return temp;
	}
}
