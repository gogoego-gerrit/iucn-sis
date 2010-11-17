package org.iucn.sis.shared.api.models.fields;

import java.util.Date;

import org.iucn.sis.shared.api.models.Field;

public class LivelihoodsField extends ProxyField {
	
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
	
	public LivelihoodsField(Field field) {
		super(field);
	}
	
	public Integer getScale() {
		return getForeignKeyPrimitiveField(SCALE_KEY, 0);
	}
	
	public void setScale(Integer value) {
		setForeignKeyPrimitiveField(SCALE_KEY, value);
	}
	
	public String getLocalityName() {
		return getStringPrimitiveField(LOCALITY_NAME_KEY);
	}
	
	public void setLocalityName(String value) {
		setStringPrimitiveField(LOCALITY_NAME_KEY, value);
	}
	
	public void setDate(Date value) {
		setDatePrimitiveField(DATE_KEY, value);
	}
	
	public Date getDate() {
		return getDatePrimitiveField(DATE_KEY);
	}
	
	public void setProductDescription(String value) {
		setStringPrimitiveField(PRODUCT_DESCRIPTION_KEY, value);
	}
	
	public String getProductDescription() {
		return getStringPrimitiveField(PRODUCT_DESCRIPTION_KEY);
	}
	
	public void setAnnualHarvest(String value) {
		setStringPrimitiveField(ANNUAL_HARVEST_KEY, value);
	}
	
	public String getAnnualHarvest() {
		return getStringPrimitiveField(ANNUAL_HARVEST_KEY);
	}
	
	public void setAnnualHarvestUnits(Integer value) {
		setForeignKeyPrimitiveField(UNITS_ANNUAL_HARVEST_KEY, value);
	}
	
	public Integer getAnnualHarvestUnits() {
		return getForeignKeyPrimitiveField(UNITS_ANNUAL_HARVEST_KEY, 0);
	}
	
	public void setMultiSpeciesHarvest(String value) {
		setStringPrimitiveField(ANNUAL_MULTI_SPECIES_HARVEST_KEY, value);
	}
	
	public String getMultSpeciesHarvest() {
		return getStringPrimitiveField(ANNUAL_MULTI_SPECIES_HARVEST_KEY);
	}
	
	public void setMultiSpeciesHarvestUnits(Integer value) {
		setForeignKeyPrimitiveField(UNITS_ANNUAL_MULTI_SPECIES_HARVEST_KEY, value);
	}
	
	public Integer getMultiSpeciesHarvestUntils() {
		return getForeignKeyPrimitiveField(UNITS_ANNUAL_MULTI_SPECIES_HARVEST_KEY, 0);
	}
	
	public void setPercentInHarvest(String value) {
		setStringPrimitiveField(PERCENT_IN_HARVEST_KEY, value);
	}
	
	public String getPercentInHarvest() {
		return getStringPrimitiveField(PERCENT_IN_HARVEST_KEY);
	}
	
	public void setAmountInHarvest(String value) {
		setStringPrimitiveField(AMOUNT_IN_HARVEST_KEY, value);
	}
	
	public String getAmountInHarvest() {
		return getStringPrimitiveField(AMOUNT_IN_HARVEST_KEY);
	}
	
	public void setHumanReliance(Integer value) {
		setForeignKeyPrimitiveField(HUMAN_RELIANCE_KEY, value);
	}
	
	public Integer getHumanReliance() {
		return getForeignKeyPrimitiveField(HUMAN_RELIANCE_KEY, 0);
	}
	
	public void setGenderAge(Integer value) {
		setForeignKeyPrimitiveField(GENDER_AGE_KEY, value);
	}
	
	public Integer getGenderAge() {
		return getForeignKeyPrimitiveField(GENDER_AGE_KEY, 0);
	}
	
	public void setSocioEconomic(Integer value) {
		setForeignKeyPrimitiveField(SOCIO_ECONOMIC_KEY, value);
	}
	
	public Integer getSocioEconomic() {
		return getForeignKeyPrimitiveField(SOCIO_ECONOMIC_KEY, 0);
	}
	
	public void setOther(String value) {
		setStringPrimitiveField(OTHER_KEY, value);
	}
	
	public String getOther() {
		return getStringPrimitiveField(OTHER_KEY);
	}
	
	public void setTotalPopulationBenefit(Integer value) {
		setForeignKeyPrimitiveField(TOTAL_POP_BENEFIT_KEY, value);
	}
	
	public Integer getTotalPopulationBenefit() {
		return getForeignKeyPrimitiveField(TOTAL_POP_BENEFIT_KEY, 0);
	}
	
	public void setHouseholdConsumption(Integer value) {
		setForeignKeyPrimitiveField(HOUSEHOLD_CONSUMPTION_KEY, value);
	}
	
	public Integer getHouseholdConsumption() {
		return getForeignKeyPrimitiveField(HOUSEHOLD_CONSUMPTION_KEY, 0);
	}
	
	public void setHouseholdIncome(Integer value) {
		setForeignKeyPrimitiveField(HOUSEHOLD_INCOME_KEY, value);
	}
	
	public Integer getHouseholdIncome() {
		return getForeignKeyPrimitiveField(HOUSEHOLD_INCOME_KEY, 0);
	}
	
	public void setAnnualCashIncome(String value) {
		setStringPrimitiveField(ANNUAL_CASH_INCOME_KEY, value);
	}
	
	public String getAnnualCashIncome() {
		return getStringPrimitiveField(ANNUAL_CASH_INCOME_KEY);
	}
	
	@Override
	public void setForeignKeyPrimitiveField(String key, Integer value) {
		Integer toSave = value;
		if (toSave.intValue() == 0)
			toSave = null;
		
		super.setForeignKeyPrimitiveField(key, toSave);
	}
	

}
