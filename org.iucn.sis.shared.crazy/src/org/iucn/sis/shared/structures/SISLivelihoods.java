package org.iucn.sis.shared.structures;

import java.util.ArrayList;
import java.util.HashMap;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.solertium.lwxml.gwt.debug.SysDebugger;

public class SISLivelihoods extends Structure {
	// Piece 0: Scale (DropDown)
	// Piece 1: Name of location/country/region (text)
	// Piece 2: Date (text)
	// Piece 3: Product description (text)
	// Piece 4: Estimated annual harvest (text)
	// Piece 5: Units of above (DropDown)
	// Piece 6: Esimated annual multi-species harvest (text)
	// Piece 7: Units of above (DropDown)
	// Piece 8: Percent of species in total harvest (text)
	// Piece 9: Amount of species within harvest (text)
	// Piece 10: Primary level of human reliance (DropDown)
	// Piece 11: Gender/Age (DropDown)
	// Piece 12: Socio-economic group (DropDown)
	// Piece 13: Other (text)
	// Piece 14: percentTotalPopulationBenefit (DropDown)
	// Piece 15: percentHouseholdConsumption (DropDown)
	// Piece 16: percentHouseholdIncome (DropDown)
	// Piece 17: Annual cash income in US$ (text)

	public static ArrayList generateDefaultDataList() {
		ArrayList dataList = new ArrayList();
		dataList.add("0");
		dataList.add("");
		dataList.add("");
		dataList.add("");
		dataList.add("");
		dataList.add("0");
		dataList.add("");
		dataList.add("0");
		dataList.add("");
		dataList.add("");
		dataList.add("0");
		dataList.add("0");
		dataList.add("0");
		dataList.add("");
		dataList.add("0");
		dataList.add("0");
		dataList.add("0");
		dataList.add("");

		return dataList;
	}

	private ListBox scale;
	private TextBox localeName;
	private TextBox date;

	private TextBox product;
	private TextBox singleSpeciesHarvest;

	private ListBox singleSpeciesHarvestUnits;
	private TextBox multiSpeciesHarvest;
	private ListBox multiSpeciesHarvestUnits;
	private TextBox multiSpeciesHarvestContributionPercent;

	private TextBox multiSpeciesHarvestAmount;
	private ListBox humanReliance;
	private ListBox byGenderAge;
	private ListBox bySocioEcon;

	private TextBox other;
	private ListBox percentPopulationBenefiting;
	private ListBox percentConsumption;

	private ListBox percentIncome;

	private TextBox annualCashIncome;
	private String[] scaleOptions = new String[] { " --- Select --- ", "Local", "National", "Regional", "Global" };
	private String[] unitsOptions = new String[] { " --- Select --- ", "Volume (cubic metres)",
			"Weight (in kilograms)", "Number of individuals" };
	private String[] humanRelianceOptions = new String[] { " --- Select --- ", "Emergency resource",
			"Optional alternative", "Essential staple", "Geographically variable", "Not known" };
	private String[] byGenderAgeOptions = new String[] { " --- Select --- ", "Men", "Women", "Children", "Multiple",
			"Not Known" };
	private String[] bySocioEconOptions = new String[] { " --- Select --- ", "Poorer households", "All households",
			"Richer households", "Other group (specify in notes)", "Not Known" };
	private String[] percentPopulationBenefitingOptions = new String[] { " --- Select --- ", "0-1%", "2-10%", "11-25%",
			"26-50%", "51-100%", "Not Known" };
	private String[] percentConsumptionOptions = new String[] { " --- Select --- ", "0-25%", "26-50%", "51-75%",
			"76-100%", "Not Known" };

	private String[] percentIncomeOptions = percentConsumptionOptions;

	public SISLivelihoods(String struct, String descript, Object data) {
		super(struct, descript, data);
		buildContentPanel(Orientation.VERTICAL);

		try {
			buildWidgets();
		} catch (Error e) {
			SysDebugger.getInstance().println("ERROR building widgets for SISLivelihoods.");
			// Better be doing this on the client-side...
		}
	}

	public void buildWidgets() {
		scale = new ListBox();
		for (int i = 0; i < scaleOptions.length; i++)
			scale.addItem(scaleOptions[i]);

		localeName = new TextBox();
		date = new TextBox();
		product = new TextBox();

		singleSpeciesHarvest = new TextBox();
		singleSpeciesHarvestUnits = new ListBox();
		for (int i = 0; i < unitsOptions.length; i++)
			singleSpeciesHarvestUnits.addItem(unitsOptions[i]);

		multiSpeciesHarvest = new TextBox();
		multiSpeciesHarvestUnits = new ListBox();
		for (int i = 0; i < unitsOptions.length; i++)
			multiSpeciesHarvestUnits.addItem(unitsOptions[i]);
		multiSpeciesHarvestAmount = new TextBox();
		multiSpeciesHarvestContributionPercent = new TextBox();

		humanReliance = new ListBox();
		for (int i = 0; i < humanRelianceOptions.length; i++)
			humanReliance.addItem(humanRelianceOptions[i]);

		byGenderAge = new ListBox();
		for (int i = 0; i < byGenderAgeOptions.length; i++)
			byGenderAge.addItem(byGenderAgeOptions[i]);

		bySocioEcon = new ListBox();
		for (int i = 0; i < bySocioEconOptions.length; i++)
			bySocioEcon.addItem(bySocioEconOptions[i]);

		other = new TextBox();

		percentPopulationBenefiting = new ListBox();
		for (int i = 0; i < percentPopulationBenefitingOptions.length; i++)
			percentPopulationBenefiting.addItem(percentPopulationBenefitingOptions[i]);

		percentConsumption = new ListBox();
		for (int i = 0; i < percentConsumptionOptions.length; i++)
			percentConsumption.addItem(percentConsumptionOptions[i]);

		percentIncome = new ListBox();
		for (int i = 0; i < percentIncomeOptions.length; i++)
			percentIncome.addItem(percentIncomeOptions[i]);

		annualCashIncome = new TextBox();
	}

	public void clearData() {
		scale.setSelectedIndex(0);
		localeName.setText("");
		date.setText("");
		product.setText("");

		singleSpeciesHarvest.setText("");
		singleSpeciesHarvestUnits.setSelectedIndex(0);
		multiSpeciesHarvest.setText("");
		multiSpeciesHarvestUnits.setSelectedIndex(0);
		multiSpeciesHarvestContributionPercent.setText("");
		multiSpeciesHarvestAmount.setText("");

		humanReliance.setSelectedIndex(0);
		byGenderAge.setSelectedIndex(0);
		bySocioEcon.setSelectedIndex(0);
		other.setText("");

		percentPopulationBenefiting.setSelectedIndex(0);
		percentConsumption.setSelectedIndex(0);
		percentIncome.setSelectedIndex(0);
		annualCashIncome.setText("");
	}

	protected Widget createLabel() {
		displayPanel.clear();

		// ---- Scale ----- //
		VerticalPanel scalePanel = new VerticalPanel();
		scalePanel.addStyleName("outsetFrameBorder");
		scalePanel.setWidth("96%");

		HorizontalPanel curRow = new HorizontalPanel();
		curRow.setSpacing(3);
		curRow.add(new HTML("Scale: "));
		curRow.add(scale);
		curRow.add(new HTML("Name of location/country/region (leave blank if Global): "));
		curRow.add(localeName);

		scalePanel.add(curRow);

		curRow = new HorizontalPanel();
		curRow.setSpacing(3);
		curRow.add(new HTML("Date: "));
		curRow.add(date);
		curRow.add(new HTML("Description of product (e.g. skin, meat, horn, fibre, etc.): "));
		curRow.add(product);

		scalePanel.add(curRow);
		displayPanel.add(scalePanel);

		// ---- Single species harvest----//
		HorizontalPanel harvestPanel = new HorizontalPanel();
		harvestPanel.addStyleName("outsetFrameBorder");
		harvestPanel.setWidth("96%");

		VerticalPanel vertWrapper = new VerticalPanel();
		vertWrapper.setSpacing(3);
		vertWrapper.addStyleName("outsetFrameBorder");
		vertWrapper.add(new HTML("For Single Species Harvest"));

		curRow = new HorizontalPanel();
		curRow.setSpacing(3);
		curRow.add(new HTML("Estimated annual harvest of the product: "));
		curRow.add(singleSpeciesHarvest);

		vertWrapper.add(curRow);

		curRow = new HorizontalPanel();
		curRow.setSpacing(3);
		curRow.add(new HTML("Units: "));
		curRow.add(singleSpeciesHarvestUnits);

		vertWrapper.add(curRow);

		harvestPanel.add(vertWrapper);

		// ---- multi-species harvest----//
		vertWrapper = new VerticalPanel();
		vertWrapper.setSpacing(3);
		vertWrapper.addStyleName("outsetFrameBorder");
		vertWrapper.add(new HTML("When Part of a Multi-species Harvest"));

		curRow = new HorizontalPanel();
		curRow.setSpacing(3);
		curRow.add(new HTML("Estimated annual multi-species harvest: "));
		curRow.add(multiSpeciesHarvest);

		vertWrapper.add(curRow);

		curRow = new HorizontalPanel();
		curRow.setSpacing(3);
		curRow.add(new HTML("Units: "));
		curRow.add(multiSpeciesHarvestUnits);

		vertWrapper.add(curRow);

		harvestPanel.add(vertWrapper);
		displayPanel.add(harvestPanel);

		// ---- Users ----- //
		VerticalPanel userPanel = new VerticalPanel();
		userPanel.setSpacing(3);
		userPanel.setWidth("96%");
		userPanel.addStyleName("outsetFrameBorder");
		userPanel.add(new HTML("Users"));

		curRow = new HorizontalPanel();
		curRow.setSpacing(3);
		curRow.add(new HTML("Primary level of human reliance on this product: "));
		curRow.add(humanReliance);
		userPanel.add(curRow);
		userPanel.add(new HTML("Who are the primary harvesters of this resource?"));

		curRow = new HorizontalPanel();
		curRow.setSpacing(3);
		curRow.add(byGenderAge);
		curRow.add(bySocioEcon);
		curRow.add(other);
		userPanel.add(curRow);

		displayPanel.add(userPanel);

		// ---- value to livelihoods ---- //
		VerticalPanel valueToLivelihoods = new VerticalPanel();
		valueToLivelihoods.setSpacing(3);
		valueToLivelihoods.setWidth("96%");
		valueToLivelihoods.addStyleName("outsetFrameBorder");
		valueToLivelihoods.add(new HTML("Value to Livelihoods"));

		curRow = new HorizontalPanel();
		curRow.setSpacing(3);
		curRow.add(new HTML("Proportion (as %) of total population benefiting from this product: "));
		curRow.add(percentPopulationBenefiting);
		valueToLivelihoods.add(curRow);

		curRow = new HorizontalPanel();
		curRow.setSpacing(3);
		curRow
				.add(new HTML(
						"Proportion (as %) of household consumption (if dietary as a % of protein/carbohydrate): "));
		curRow.add(percentConsumption);
		valueToLivelihoods.add(curRow);

		curRow = new HorizontalPanel();
		curRow.setSpacing(3);
		curRow.add(new HTML("Proportion (as %) of household income for this product: "));
		curRow.add(percentIncome);
		valueToLivelihoods.add(curRow);

		displayPanel.add(valueToLivelihoods);

		// ---- Value to Economy ---- //
		HorizontalPanel valueToEconomy = new HorizontalPanel();
		valueToEconomy.addStyleName("outsetFrameBorder");
		valueToEconomy.setWidth("96%");
		valueToEconomy.setSpacing(5);

		valueToEconomy.add(new HTML("Value to Economy"));
		valueToEconomy.add(new HTML("Annual cash income from this product - gross (in US$): "));
		valueToEconomy.add(annualCashIncome);

		displayPanel.add(valueToEconomy);

		return displayPanel;
	}

	protected Widget createViewOnlyLabel() {
		return createLabel();
	}

	public void createWidget() {
		// suxx0r
	}

	/**
	 * Returns an ArrayList of descriptions (as Strings) for this structure, and
	 * if it contains multiples structures, all of those, in order.
	 */
	public ArrayList extractDescriptions() {
		ArrayList ret = new ArrayList();
		ret.add("Number selected.");
		ret.add("Scale");
		ret.add("Locale");
		ret.add("Date");
		ret.add("Product Description");
		ret.add("Single Species Annual Harvest");
		ret.add("Units");
		ret.add("Multi-Species Annual Harvest");
		ret.add("Units");
		ret.add("");
		ret.add("");
		ret.add("Level of human reliance");
		ret.add("Primary Harvesters");
		ret.add("");
		ret.add("");
		ret.add("% benefiting");
		ret.add("% of household consumption");
		ret.add("% of household income");
		ret.add("Value to Economy");
		return ret;
	}

	public String[] getByGenderAgeOptions() {
		return byGenderAgeOptions;
	}

	public String[] getBySocioEconOptions() {
		return bySocioEconOptions;
	}

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
		prettyData.add(offset, DisplayableDataHelper.toDisplayableSingleSelect((String) rawData.get(offset),
				scaleOptions));
		offset++;
		prettyData.add(offset, rawData.get(offset));
		offset++;
		prettyData.add(offset, rawData.get(offset));
		offset++;
		prettyData.add(offset, rawData.get(offset));
		offset++;

		prettyData.add(offset, rawData.get(offset));
		offset++;
		prettyData.add(offset, DisplayableDataHelper.toDisplayableSingleSelect((String) rawData.get(offset),
				unitsOptions));
		offset++;
		prettyData.add(offset, rawData.get(offset));
		offset++;
		prettyData.add(offset, DisplayableDataHelper.toDisplayableSingleSelect((String) rawData.get(offset),
				unitsOptions));
		offset++;
		prettyData.add(offset, rawData.get(offset));
		offset++;
		prettyData.add(offset, rawData.get(offset));
		offset++;

		prettyData.add(offset, DisplayableDataHelper.toDisplayableSingleSelect((String) rawData.get(offset),
				humanRelianceOptions));
		offset++;
		prettyData.add(offset, DisplayableDataHelper.toDisplayableSingleSelect((String) rawData.get(offset),
				byGenderAgeOptions));
		offset++;
		prettyData.add(offset, DisplayableDataHelper.toDisplayableSingleSelect((String) rawData.get(offset),
				bySocioEconOptions));
		offset++;
		prettyData.add(offset, rawData.get(offset));
		offset++;

		prettyData.add(offset, DisplayableDataHelper.toDisplayableSingleSelect((String) rawData.get(offset),
				percentPopulationBenefitingOptions));
		offset++;
		prettyData.add(offset, DisplayableDataHelper.toDisplayableSingleSelect((String) rawData.get(offset),
				percentConsumptionOptions));
		offset++;
		prettyData.add(offset, DisplayableDataHelper.toDisplayableSingleSelect((String) rawData.get(offset),
				percentIncomeOptions));
		offset++;
		prettyData.add(offset, rawData.get(offset));
		offset++;

		return offset;
	}

	public String[] getHumanRelianceOptions() {
		return humanRelianceOptions;
	}

	private int getIndex(String data) {
		if (data == null || data.equals(""))
			return 0;
		else
			try {
				return Integer.parseInt(data);
			} catch (Exception e) {
				return 0;
			}
	}

	public String[] getPercentConsumptionOptions() {
		return percentConsumptionOptions;
	}

	public String[] getPercentIncomeOptions() {
		return percentIncomeOptions;
	}

	public String[] getPercentPopulationBenefitingOptions() {
		return percentPopulationBenefitingOptions;
	}

	public String[] getScaleOptions() {
		return scaleOptions;
	}

	public String[] getUnitsOptions() {
		return unitsOptions;
	}

	public HashMap getValues() {
		return new HashMap();
	}

	public int setData(ArrayList dataList, int dataOffset) {
		super.setData(dataList, dataOffset);
		scale.setSelectedIndex(getIndex(dataList.get(dataOffset++).toString()));
		localeName.setText(dataList.get(dataOffset++).toString());
		date.setText(dataList.get(dataOffset++).toString());
		product.setText(dataList.get(dataOffset++).toString());

		singleSpeciesHarvest.setText(dataList.get(dataOffset++).toString());
		singleSpeciesHarvestUnits.setSelectedIndex(getIndex(dataList.get(dataOffset++).toString()));
		multiSpeciesHarvest.setText(dataList.get(dataOffset++).toString());
		multiSpeciesHarvestUnits.setSelectedIndex(getIndex(dataList.get(dataOffset++).toString()));
		multiSpeciesHarvestContributionPercent.setText(dataList.get(dataOffset++).toString());
		multiSpeciesHarvestAmount.setText(dataList.get(dataOffset++).toString());

		humanReliance.setSelectedIndex(getIndex(dataList.get(dataOffset++).toString()));
		byGenderAge.setSelectedIndex(getIndex(dataList.get(dataOffset++).toString()));
		bySocioEcon.setSelectedIndex(getIndex(dataList.get(dataOffset++).toString()));
		other.setText(dataList.get(dataOffset++).toString());

		percentPopulationBenefiting.setSelectedIndex(getIndex(dataList.get(dataOffset++).toString()));
		percentConsumption.setSelectedIndex(getIndex(dataList.get(dataOffset++).toString()));
		percentIncome.setSelectedIndex(getIndex(dataList.get(dataOffset++).toString()));
		annualCashIncome.setText(dataList.get(dataOffset++).toString());

		return dataOffset;
	}

	protected void setDataValues(HashMap fieldData) {
	}

	protected void setEnabled(boolean isEnabled) {

	}

	public String toXML() {
		String ret = "<structure>" + scale.getSelectedIndex() + "</structure>";
		ret += "<structure>" + localeName.getText() + "</structure>";
		ret += "<structure>" + date.getText() + "</structure>";
		ret += "<structure>" + product.getText() + "</structure>";
		ret += "<structure>" + singleSpeciesHarvest.getText() + "</structure>";
		ret += "<structure>" + singleSpeciesHarvestUnits.getSelectedIndex() + "</structure>";
		ret += "<structure>" + multiSpeciesHarvest.getText() + "</structure>";
		ret += "<structure>" + multiSpeciesHarvestUnits.getSelectedIndex() + "</structure>";
		ret += "<structure>" + multiSpeciesHarvestContributionPercent.getText() + "</structure>";
		ret += "<structure>" + multiSpeciesHarvestAmount.getText() + "</structure>";
		ret += "<structure>" + humanReliance.getSelectedIndex() + "</structure>";
		ret += "<structure>" + byGenderAge.getSelectedIndex() + "</structure>";
		ret += "<structure>" + bySocioEcon.getSelectedIndex() + "</structure>";
		ret += "<structure>" + other.getText() + "</structure>";
		ret += "<structure>" + percentPopulationBenefiting.getSelectedIndex() + "</structure>";
		ret += "<structure>" + percentConsumption.getSelectedIndex() + "</structure>";
		ret += "<structure>" + percentIncome.getSelectedIndex() + "</structure>";
		ret += "<structure>" + annualCashIncome.getText() + "</structure>";

		return ret;
	}
}
