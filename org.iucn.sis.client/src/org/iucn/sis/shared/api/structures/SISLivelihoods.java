package org.iucn.sis.shared.api.structures;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.iucn.sis.client.api.utils.FormattedDate;
import org.iucn.sis.shared.api.models.Field;
import org.iucn.sis.shared.api.models.PrimitiveField;
import org.iucn.sis.shared.api.models.primitivefields.DatePrimitiveField;
import org.iucn.sis.shared.api.models.primitivefields.ForeignKeyPrimitiveField;
import org.iucn.sis.shared.api.models.primitivefields.StringPrimitiveField;

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

	public static final String SCALE_KEY = "scale";
	public static final String LOCALITY_NAME_KEY = "nameOfLocality";
	public static final String DATE_KEY = "date";
	public static final String PRODUCT_DESCRIPTION_KEY = "productDescription";
	public static final String ANNUAL_HARVEST_KEY = "annualHarvest";
	public static final String UNITS_ANNUAL_HARVEST_KEY = "unitsAnnualHarvest";
	public static final String ANNUAL_MULTI_SPECIES_HARVEST_KEY = "annualMultiSpeciesHarvest";
	public static final String UNITS_ANNUAL_MULTI_SPECIES_HARVEST_KEY = "unitsAnnualMultiSpeciesHarvest";
	public static final String PERCENT_IN_HARVEST_KEY = "percentInHarvest";
	public static final String AMOUNT_IN_HARVEST_KEY = "amountInHarvest";
	public static final String HUMAN_RELIANCE_KEY = "humanReliance";
	public static final String GENDER_AGE_KEY = "genderAge";
	public static final String SOCIO_ECONOMIC_KEY = "socioEconomic";
	public static final String OTHER_KEY = "other";
	public static final String TOTAL_POP_BENEFIT_KEY = "totalPopBenefit";
	public static final String HOUSEHOLD_CONSUMPTION_KEY = "householdConsumption";
	public static final String HOUSEHOLD_INCOME_KEY = "householdIncome";
	public static final String ANNUAL_CASH_INCOME_KEY = "annualCashIncome";
	
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

	public SISLivelihoods(String struct, String descript, String structID, Object data) {
		super(struct, descript, structID, data);
		buildContentPanel(Orientation.VERTICAL);

		try {
			buildWidgets();
		} catch (Error e) {
			SysDebugger.getInstance().println("ERROR building widgets for SISLivelihoods.");
			// Better be doing this on the client-side...
		}
	}
	
	@Override
	public boolean hasChanged() {
		return true;
	}

	@Override
	public void save(Field field) {
		//for each widget
		//field.addPrimitive(...);
		//  
		field.getPrimitiveField().add(new ForeignKeyPrimitiveField(SCALE_KEY, field, 
				Integer.valueOf(scale.getSelectedIndex()), null));
		field.getPrimitiveField().add(new StringPrimitiveField(LOCALITY_NAME_KEY, field, 
				localeName.getText()));
		field.getPrimitiveField().add(new DatePrimitiveField(DATE_KEY, field, 
				FormattedDate.impl.getDate(date.getText())));
		field.getPrimitiveField().add(new StringPrimitiveField(PRODUCT_DESCRIPTION_KEY, field, 
				product.getText()));
		field.getPrimitiveField().add(new StringPrimitiveField(ANNUAL_HARVEST_KEY, field, 
				singleSpeciesHarvest.getText()));
		field.getPrimitiveField().add(new ForeignKeyPrimitiveField(UNITS_ANNUAL_HARVEST_KEY, field, 
				Integer.valueOf(singleSpeciesHarvestUnits.getSelectedIndex()), null));
		field.getPrimitiveField().add(new StringPrimitiveField(ANNUAL_MULTI_SPECIES_HARVEST_KEY, field, 
				multiSpeciesHarvest.getText()));
		field.getPrimitiveField().add(new ForeignKeyPrimitiveField(UNITS_ANNUAL_MULTI_SPECIES_HARVEST_KEY, field, 
				Integer.valueOf(multiSpeciesHarvestUnits.getSelectedIndex()), null));
		field.getPrimitiveField().add(new StringPrimitiveField(PERCENT_IN_HARVEST_KEY, field, 
				multiSpeciesHarvestContributionPercent.getText()));
		field.getPrimitiveField().add(new StringPrimitiveField(AMOUNT_IN_HARVEST_KEY, field, 
				multiSpeciesHarvestAmount.getText()));
		
		field.getPrimitiveField().add(new ForeignKeyPrimitiveField(HUMAN_RELIANCE_KEY, field, 
				Integer.valueOf(humanReliance.getSelectedIndex()), null));
		field.getPrimitiveField().add(new ForeignKeyPrimitiveField(GENDER_AGE_KEY, field, 
				Integer.valueOf(byGenderAge.getSelectedIndex()), null));
		field.getPrimitiveField().add(new ForeignKeyPrimitiveField(SOCIO_ECONOMIC_KEY, field, 
				Integer.valueOf(bySocioEcon.getSelectedIndex()), null));
		field.getPrimitiveField().add(new StringPrimitiveField(OTHER_KEY, field, 
				other.getText()));
		
		field.getPrimitiveField().add(new ForeignKeyPrimitiveField(TOTAL_POP_BENEFIT_KEY, field, 
				Integer.valueOf(percentPopulationBenefiting.getSelectedIndex()), null));
		field.getPrimitiveField().add(new ForeignKeyPrimitiveField(HOUSEHOLD_CONSUMPTION_KEY, field, 
				Integer.valueOf(percentConsumption.getSelectedIndex()), null));
		field.getPrimitiveField().add(new ForeignKeyPrimitiveField(HOUSEHOLD_INCOME_KEY, field, 
				Integer.valueOf(percentIncome.getSelectedIndex()), null));
		field.getPrimitiveField().add(new StringPrimitiveField(ANNUAL_CASH_INCOME_KEY, field, 
				annualCashIncome.getText()));

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
	
	@Override
	public void setData(Field field) {
		Map<String, PrimitiveField> data = field.getKeyToPrimitiveFields();
		//super.setData(data);
		
		scale.setSelectedIndex(((ForeignKeyPrimitiveField)data.get(SCALE_KEY)).getValue());
		localeName.setText(((StringPrimitiveField)data.get(LOCALITY_NAME_KEY)).getValue());
		date.setText(((StringPrimitiveField)data.get(DATE_KEY)).getValue());
		product.setText(((StringPrimitiveField)data.get(PRODUCT_DESCRIPTION_KEY)).getValue());

		singleSpeciesHarvest.setText(((StringPrimitiveField)data.get(ANNUAL_HARVEST_KEY)).getValue());
		singleSpeciesHarvestUnits.setSelectedIndex(((ForeignKeyPrimitiveField)data.get(UNITS_ANNUAL_HARVEST_KEY)).getValue());
		multiSpeciesHarvest.setText(((StringPrimitiveField)data.get(ANNUAL_MULTI_SPECIES_HARVEST_KEY)).getValue());
		multiSpeciesHarvestUnits.setSelectedIndex(((ForeignKeyPrimitiveField)data.get(UNITS_ANNUAL_MULTI_SPECIES_HARVEST_KEY)).getValue());
		multiSpeciesHarvestContributionPercent.setText(((StringPrimitiveField)data.get(PERCENT_IN_HARVEST_KEY)).getValue());
		multiSpeciesHarvestAmount.setText(((StringPrimitiveField)data.get(AMOUNT_IN_HARVEST_KEY)).getValue());

		humanReliance.setSelectedIndex(((ForeignKeyPrimitiveField)data.get(HUMAN_RELIANCE_KEY)).getValue());
		byGenderAge.setSelectedIndex(((ForeignKeyPrimitiveField)data.get(GENDER_AGE_KEY)).getValue());
		bySocioEcon.setSelectedIndex(((ForeignKeyPrimitiveField)data.get(SOCIO_ECONOMIC_KEY)).getValue());
		other.setText(((StringPrimitiveField)data.get(OTHER_KEY)).getValue());

		percentPopulationBenefiting.setSelectedIndex(((ForeignKeyPrimitiveField)data.get(TOTAL_POP_BENEFIT_KEY)).getValue());
		percentConsumption.setSelectedIndex(((ForeignKeyPrimitiveField)data.get(HOUSEHOLD_CONSUMPTION_KEY)).getValue());
		percentIncome.setSelectedIndex(((ForeignKeyPrimitiveField)data.get(HOUSEHOLD_INCOME_KEY)).getValue());
		annualCashIncome.setText(((StringPrimitiveField)data.get(ANNUAL_CASH_INCOME_KEY)).getValue());
	}

	protected void setDataValues(HashMap fieldData) {
	}

	public void setEnabled(boolean isEnabled) {

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
