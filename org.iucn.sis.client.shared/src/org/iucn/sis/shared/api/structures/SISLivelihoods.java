package org.iucn.sis.shared.api.structures;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.iucn.sis.client.api.utils.FormattedDate;
import org.iucn.sis.shared.api.data.DisplayData.LookupDataContainer;
import org.iucn.sis.shared.api.data.LookupData;
import org.iucn.sis.shared.api.data.LookupData.LookupDataValue;
import org.iucn.sis.shared.api.debug.Debug;
import org.iucn.sis.shared.api.models.Field;
import org.iucn.sis.shared.api.models.PrimitiveField;
import org.iucn.sis.shared.api.models.fields.LivelihoodsField;
import org.iucn.sis.shared.api.utils.CanonicalNames;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class SISLivelihoods extends Structure<Field> {
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

	public SISLivelihoods(String struct, String descript, String structID, Object data) {
		super(struct, descript, structID, data);
		buildContentPanel(Orientation.VERTICAL);

		try {
			buildWidgets();
		} catch (Throwable e) {
			Debug.println("ERROR building widgets for SISLivelihoods.\n{0}", e);
			// Better be doing this on the client-side...
		}
	}
	
	@SuppressWarnings("unchecked")
	public boolean hasChanged(Field field) {
		Field fauxParent = new Field(), fauxChild = new Field(CanonicalNames.Livelihoods, null);
		
		save(fauxParent, fauxChild);
		
		if (field == null) {
			boolean childHasData = fauxChild.hasData();
			if (childHasData)
				Debug.println("HasChanged in Livelihoods: DB has null value, but child hasData, there are {0} primitive fields: \n{1}", fauxChild.getPrimitiveField().size(), fauxChild.getKeyToPrimitiveFields().keySet());
			else
				Debug.println("HasChanged in Livelihoods: DB has null value, child has no data, no changes.");
			return childHasData;
		}
		
		if (field.getPrimitiveField().size() != fauxChild.getPrimitiveField().size()) {
			Debug.println("HasChanged in Livelihoods: DB has {0} prims, but child has {1}, there are changes\nDB: {2}\nChild: {3}", field.getPrimitiveField().size(), fauxChild.getPrimitiveField().size(), field.getKeyToPrimitiveFields().keySet(), fauxChild.getKeyToPrimitiveFields().keySet());
			return true;
		}
		
		Map<String, PrimitiveField> savedFields = fauxChild.getKeyToPrimitiveFields();
		for (Map.Entry<String, PrimitiveField> entry : savedFields.entrySet()) {
			PrimitiveField oldPrimField = field.getPrimitiveField(entry.getKey());
			if (oldPrimField == null) {
				Debug.println("HasChanged in Livelihoods: DB missing new value for {0} of {1}", entry.getKey(), entry.getValue().getRawValue());
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
			
			Debug.println("HasChanged in Livelihoods: Interrogating {0} with DB value {1} and child value {2}, result is {3}", entry.getKey(), oldValue, newValue, hasChanged);
			
			if (hasChanged)
				return hasChanged;
		}
		
		return false;
	}
	
	@Override
	public void save(Field parent, Field field) {
		if (field == null) {
			field = new Field();
			field.setName(getId());
			field.setParent(parent);
		}
		
		LivelihoodsField proxy = new LivelihoodsField(field);
		proxy.setScale(getDropDownSelection(scale));
		proxy.setLocalityName(localeName.getText());
		try { 
			proxy.setDate(FormattedDate.impl.getDate(date.getText()));
		} catch (Exception e) {
			Debug.println("Invalid livelihood date {0}, changes unsaved", date.getText());
		}
		proxy.setProductDescription(product.getText());
		proxy.setAnnualHarvest(singleSpeciesHarvest.getText());
		proxy.setAnnualHarvestUnits(getDropDownSelection(singleSpeciesHarvestUnits));
		proxy.setMultiSpeciesHarvest(multiSpeciesHarvest.getText());
		proxy.setMultiSpeciesHarvestUnits(getDropDownSelection(multiSpeciesHarvestUnits));
		proxy.setPercentInHarvest(multiSpeciesHarvestContributionPercent.getText());
		proxy.setAmountInHarvest(multiSpeciesHarvestAmount.getText());
		proxy.setHumanReliance(getDropDownSelection(humanReliance));
		proxy.setGenderAge(getDropDownSelection(byGenderAge));
		proxy.setSocioEconomic(getDropDownSelection(bySocioEcon));
		proxy.setOther(other.getText());
		proxy.setTotalPopulationBenefit(getDropDownSelection(percentPopulationBenefiting));
		proxy.setHouseholdConsumption(getDropDownSelection(percentConsumption));
		proxy.setHouseholdIncome(getDropDownSelection(percentIncome));
		proxy.setAnnualCashIncome(annualCashIncome.getText());
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
	
	public void buildWidgets() {
		scale = new ListBox();
		populate(scale, LivelihoodsField.SCALE_KEY);

		localeName = new TextBox();
		date = new TextBox();
		product = new TextBox();

		singleSpeciesHarvest = new TextBox();
		singleSpeciesHarvestUnits = new ListBox();
		populate(singleSpeciesHarvestUnits, LivelihoodsField.UNITS_ANNUAL_HARVEST_KEY);

		multiSpeciesHarvest = new TextBox();
		multiSpeciesHarvestUnits = new ListBox();
		populate(multiSpeciesHarvestUnits, LivelihoodsField.UNITS_ANNUAL_MULTI_SPECIES_HARVEST_KEY);
		
		multiSpeciesHarvestAmount = new TextBox();
		multiSpeciesHarvestContributionPercent = new TextBox();

		humanReliance = new ListBox();
		populate(humanReliance, LivelihoodsField.HUMAN_RELIANCE_KEY);

		byGenderAge = new ListBox();
		populate(byGenderAge, LivelihoodsField.GENDER_AGE_KEY);

		bySocioEcon = new ListBox();
		populate(bySocioEcon, LivelihoodsField.SOCIO_ECONOMIC_KEY);

		other = new TextBox();

		percentPopulationBenefiting = new ListBox();
		populate(percentPopulationBenefiting, LivelihoodsField.TOTAL_POP_BENEFIT_KEY);
		
		percentConsumption = new ListBox();
		populate(percentConsumption, LivelihoodsField.HOUSEHOLD_CONSUMPTION_KEY);
		
		percentIncome = new ListBox();
		populate(percentIncome, LivelihoodsField.HOUSEHOLD_INCOME_KEY);

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
	public ArrayList<String> extractDescriptions() {
		ArrayList<String> ret = new ArrayList<String>();
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
	
	@Override
	public List<ClassificationInfo> getClassificationInfo() {
		return new ArrayList<ClassificationInfo>();
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
				getOptionLabels(LivelihoodsField.SCALE_KEY)));
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
				getOptionLabels(LivelihoodsField.UNITS_ANNUAL_HARVEST_KEY)));
		offset++;
		prettyData.add(offset, rawData.get(offset));
		offset++;
		prettyData.add(offset, DisplayableDataHelper.toDisplayableSingleSelect((String) rawData.get(offset),
				getOptionLabels(LivelihoodsField.UNITS_ANNUAL_MULTI_SPECIES_HARVEST_KEY)));
		offset++;
		prettyData.add(offset, rawData.get(offset));
		offset++;
		prettyData.add(offset, rawData.get(offset));
		offset++;

		prettyData.add(offset, DisplayableDataHelper.toDisplayableSingleSelect((String) rawData.get(offset),
				getOptionLabels(LivelihoodsField.HUMAN_RELIANCE_KEY)));
		offset++;
		prettyData.add(offset, DisplayableDataHelper.toDisplayableSingleSelect((String) rawData.get(offset),
				getOptionLabels(LivelihoodsField.GENDER_AGE_KEY)));
		offset++;
		prettyData.add(offset, DisplayableDataHelper.toDisplayableSingleSelect((String) rawData.get(offset),
				getOptionLabels(LivelihoodsField.SOCIO_ECONOMIC_KEY)));
		offset++;
		prettyData.add(offset, rawData.get(offset));
		offset++;

		prettyData.add(offset, DisplayableDataHelper.toDisplayableSingleSelect((String) rawData.get(offset),
				getOptionLabels(LivelihoodsField.TOTAL_POP_BENEFIT_KEY)));
		offset++;
		prettyData.add(offset, DisplayableDataHelper.toDisplayableSingleSelect((String) rawData.get(offset),
				getOptionLabels(LivelihoodsField.HOUSEHOLD_CONSUMPTION_KEY)));
		offset++;
		prettyData.add(offset, DisplayableDataHelper.toDisplayableSingleSelect((String) rawData.get(offset),
				getOptionLabels(LivelihoodsField.HOUSEHOLD_INCOME_KEY)));
		offset++;
		prettyData.add(offset, rawData.get(offset));
		offset++;

		return offset;
	}
	
	@Override
	public void setData(Field field) {
		LivelihoodsField proxy = new LivelihoodsField(field);
		
		setDropDownSelection(scale, proxy.getScale());
		localeName.setText(proxy.getLocalityName());
		date.setText(proxy.getDate() == null ? "" : FormattedDate.impl.getDate(proxy.getDate()));
		product.setText(proxy.getProductDescription());

		singleSpeciesHarvest.setText(proxy.getAnnualHarvest());
		setDropDownSelection(singleSpeciesHarvestUnits, proxy.getAnnualHarvestUnits());
		multiSpeciesHarvest.setText(proxy.getMultSpeciesHarvest());
		setDropDownSelection(multiSpeciesHarvestUnits, proxy.getMultiSpeciesHarvestUntils());
		multiSpeciesHarvestContributionPercent.setText(proxy.getPercentInHarvest());
		multiSpeciesHarvestAmount.setText(proxy.getAmountInHarvest());

		setDropDownSelection(humanReliance, proxy.getHumanReliance());
		setDropDownSelection(byGenderAge, proxy.getGenderAge());
		setDropDownSelection(bySocioEcon, proxy.getSocioEconomic());
		other.setText(proxy.getOther());

		setDropDownSelection(percentPopulationBenefiting, proxy.getTotalPopulationBenefit());
		setDropDownSelection(percentConsumption, proxy.getHouseholdConsumption());
		setDropDownSelection(percentIncome, proxy.getHouseholdIncome());
		
		annualCashIncome.setText(proxy.getAnnualCashIncome());
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
	
	private Integer getDropDownSelection(ListBox listBox) {
		if (listBox.getSelectedIndex() == -1)
			return 0;
		else
			return Integer.parseInt(listBox.getValue(listBox.getSelectedIndex()));
	}

	public void setEnabled(boolean isEnabled) {

	}

}
