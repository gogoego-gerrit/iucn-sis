package org.iucn.sis.shared.api.models.fields;

import java.util.HashSet;
import java.util.Set;

import org.iucn.sis.shared.api.models.Field;
import org.iucn.sis.shared.api.utils.CanonicalNames;

public class WildHarvestRecord extends ProxyField {
	
	public WildHarvestRecord(Field field) {
		super(field);
	}
	
	public void setCountry(Field value) {
		proxy.addField(value);
	}
	
	public Field getCountry() {
		return proxy.getField(CanonicalNames.CountryOccurrence);
	}
	
	public void setFAOOceanAreas(Field value) {
		proxy.addField(value);
	}
	
	public Field getFAOOceanAreas() {
		return proxy.getField(CanonicalNames.FAOOccurrence);
	}
	
	public void setIsTotalSpeciesRange(Boolean value) {
		setBooleanPrimitiveField("isTotalSpeciesRange", value, Boolean.FALSE);
	}
	
	public Boolean getIsTotalSpeciesRange() {
		return getBooleanPrimitiveField("isTotalSpeciesRange", Boolean.FALSE);
	}
	
	public void setPercentGlobalRange(Float value) {
		setFloatPrimitiveField("percentGlobalRange", value);
	}
	
	public Float getPercentGlobalRange() {
		return getFloatPrimitiveField("percentGlobalRange");
	}
	
	public void setSource(Integer value) {
		setForeignKeyPrimitiveField("source", value);
	}
	
	public Integer getSource() {
		return getForeignKeyPrimitiveField("source");
	}
	
	/*
	 * Primary form harvested from wild
	 */
	public void setFormRemoved(Integer value) {
		setForeignKeyPrimitiveField("formRemoved", value);
	}
	
	public Integer getFormRemoved() {
		return getForeignKeyPrimitiveField("formRemoved");
	}
	
	public void setLifeStageRemoval(Integer value) {
		setForeignKeyPrimitiveField("lifeStageRemoval", value);	
	}
	
	public Integer getLifeStageRemoval() {
		return getForeignKeyPrimitiveField("lifeStageRemoval");
	}
	
	public void setGenderRemoval(Integer value) {
		setForeignKeyPrimitiveField("genderRemoval", value);
	}
	
	public Integer getGenderRemoval() {
		return getForeignKeyPrimitiveField("genderRemoval");
	}
	
	public void setPercentRelativeHarvestLevel(Integer value) {
		setForeignKeyPrimitiveField("relativeHarvest", value);
	}
	
	public Integer getPercentRelativeHarvestLevel() {
		return getForeignKeyPrimitiveField("relativeHarvest");
	}
	
	public void setHarvestDocumentation(String value) {
		setTextPrimitiveField("documentation", value);
	}
	
	public String getHarvestDocumentation() {
		return getTextPrimitiveField("documentation");
	}
	
	public void setThisConservationBenefits(Integer value) {
		setForeignKeyPrimitiveField("thisConservationBenefits", value);
	}
	
	public Integer getThisConservationBenefits() {
		return getForeignKeyPrimitiveField("thisConservationBenefits");
	}
	
	public void setThisConservationBenefitsText(String value) {
		setStringPrimitiveField("thisConservationBenefitsText", value);
	}
	
	public String getThisConservationBenefitsText() {
		return getStringPrimitiveField("thisConservationBenefitsText");
	}
	
	public void setOtherConservationBenefits(Integer value) {
		setForeignKeyPrimitiveField("otherConservationBenefits", value);
	}
	
	public Integer getOtherConservationBenefits() {
		return getForeignKeyPrimitiveField("otherConservationBenefits");
	}
	
	public void setOtherConservationBenefitsText(String value) {
		setStringPrimitiveField("otherConservationBenefitsText", value);
	}
	
	public String getOtherConservationBenefitsText() {
		return getStringPrimitiveField("otherConservationBenefitsText");
	}
	
	public void setHabitatConservationBenefits(Integer value) {
		setForeignKeyPrimitiveField("habitatConservationBenefits", value);
	}
	
	public Integer getHabitatConservationBenefits() {
		return getForeignKeyPrimitiveField("habitatConservationBenefits");
	}
	
	public void setHabitatConservationBenefitsText(String value) {
		setStringPrimitiveField("habitatConservationBenefitsText", value);
	}
	
	public String getHabitatConservationBenefitsText() {
		return getStringPrimitiveField("habitatConservationBenefitsText");
	}
	
	public void setEndUseRecords(Set<Field> fields) {
		proxy.setFields(fields);
	}
	
	public Set<EndUseRecordField> getEndUseRecords() {
		final Set<EndUseRecordField> set = new HashSet<EndUseRecordField>();
		for (Field field : proxy.getFields()) {
			if ("EndUseRecord".equals(field.getName()))
				set.add(new EndUseRecordField(field));
		}
		return set;
	}

}
