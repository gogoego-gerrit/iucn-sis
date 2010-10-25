package org.iucn.sis.shared.api.models.fields;

import java.util.Date;
import java.util.HashSet;

import org.iucn.sis.shared.api.models.Field;
import org.iucn.sis.shared.api.models.PrimitiveField;
import org.iucn.sis.shared.api.models.primitivefields.BooleanPrimitiveField;
import org.iucn.sis.shared.api.models.primitivefields.DatePrimitiveField;
import org.iucn.sis.shared.api.models.primitivefields.ForeignKeyPrimitiveField;
import org.iucn.sis.shared.api.models.primitivefields.StringPrimitiveField;
import org.iucn.sis.shared.api.utils.CanonicalNames;

public class RedListCriteriaField {
	
	public static final String IS_MANUAL_KEY = "isManual";
	public static final String CRIT_VERSION_KEY = "critVersion";
	public static final String MANUAL_CATEGORY_KEY = "manualCategory";
	public static final String MANUAL_CRITERIA_KEY = "manualCriteria";
	public static final String GENERATED_CATEGORY_KEY = "autoCategory";
	public static final String GENERATED_CRITERIA_KEY = "autoCriteria";
	public static final String RLHISTORY_TEXT_KEY = "rlHistoryText";
	public static final String POSSIBLY_EXTINCT_KEY = "possiblyExtinct";
	public static final String POSSIBLY_EXTINCT_CANDIDATE_KEY = "possiblyExtinctCandidate";
	public static final String DATE_LAST_SEEN_KEY = "dateLastSeen";
	public static final String CATEGORY_TEXT_KEY = "categoryText";
	public static final String DATA_DEFICIENT_KEY = "dataDeficientReason";
	
	public static String CANONICAL_NAME = CanonicalNames.RedListCriteria;
	public static String RL_HISTORY_TEXT = "rlHistoryText";
	
	private final Field proxy;
	
	public RedListCriteriaField(Field proxy) {
		this.proxy = proxy == null ? new Field() : proxy;
		this.proxy.setFields(new HashSet<Field>()); 
	}
	
	public void setManual(Boolean isManual) {
		/*
		 * TODO: if manual, do we remove auto-gen data? also, 
		 * conversely, if not manual, do we remove all manual 
		 * data? 
		 */
		setBooleanPrimitiveField(IS_MANUAL_KEY, isManual, false);
	}
	
	/*
	 * This is false by default
	 */
	public boolean isManual() {
		return getBooleanPrimitiveField(IS_MANUAL_KEY, false);
	}
	
	public void setCriteriaVersion(Integer version) {
		setForeignKeyPrimitiveField(CRIT_VERSION_KEY, version);
	}
	
	public Integer getCriteriaVersion() {
		Integer value = getForeignKeyPrimitiveField(CRIT_VERSION_KEY);
		if (value == null)
			value = 0;
		return value;
	}
	
	public void setManualCategory(String value) {
		setStringPrimitiveField(MANUAL_CATEGORY_KEY, value);
	}
	
	public String getManualCategory() {
		return getStringPrimitiveField(MANUAL_CATEGORY_KEY);
	}
	
	public void setManualCriteria(String value) {
		setStringPrimitiveField(MANUAL_CRITERIA_KEY, value);
	}
	
	public String getManualCriteria() {
		return getStringPrimitiveField(MANUAL_CRITERIA_KEY);
	}
	
	public void setGeneratedCategory(String value) {
		setStringPrimitiveField(GENERATED_CATEGORY_KEY, value);
	}
	
	public String getGeneratedCategory() {
		return getStringPrimitiveField(GENERATED_CATEGORY_KEY);
	}
	
	public void setGeneratedCriteria(String value) {
		setStringPrimitiveField(GENERATED_CRITERIA_KEY, value);
	}
	
	public String getGeneratedCriteria() {
		return getStringPrimitiveField(GENERATED_CRITERIA_KEY);
	}
	
	public void setRLHistoryText(String value) {
		setStringPrimitiveField(RLHISTORY_TEXT_KEY, value);
	}
	
	public String getRLHistoryText() {
		return getStringPrimitiveField(RLHISTORY_TEXT_KEY);
	}
	
	public void setPossiblyExtinct(Boolean value) {
		setBooleanPrimitiveField(POSSIBLY_EXTINCT_KEY, value, false);
	}
	
	public Boolean isPossiblyExtinct() {
		return getBooleanPrimitiveField(POSSIBLY_EXTINCT_KEY, false);
	}
	
	public void setPossiblyExtinctCandidate(Boolean value) {
		setBooleanPrimitiveField(POSSIBLY_EXTINCT_CANDIDATE_KEY, value, false);
	}
	
	public Boolean isPossiblyExtinctCandidate() {
		return getBooleanPrimitiveField(POSSIBLY_EXTINCT_CANDIDATE_KEY, false);
	}
	
	public void setDateLastSeen(Date value) {
		PrimitiveField<?> field = proxy.getPrimitiveField(DATE_LAST_SEEN_KEY);
		if (field != null) {
			if (value != null)
				((DatePrimitiveField)field).setValue(value);
			else
				proxy.getPrimitiveField().remove(field);
		}
		else if (value != null)
			proxy.addPrimitiveField(new DatePrimitiveField(DATE_LAST_SEEN_KEY, proxy, value));
	}
	
	public Date getDateLastSeen() {
		PrimitiveField<?> field = proxy.getPrimitiveField(DATE_LAST_SEEN_KEY);
		if (field == null)
			return null;
		else
			return ((DatePrimitiveField)field).getValue();
	}
	
	public void setCategoryText(String value) {
		setStringPrimitiveField(CATEGORY_TEXT_KEY, value);
	}
	
	public String getCategoryText() {
		return getStringPrimitiveField(CATEGORY_TEXT_KEY);
	}
	
	public void setDataDeficient(String value) {
		setStringPrimitiveField(DATA_DEFICIENT_KEY, value);
	}
	
	public String getDataDeficient() {
		return getStringPrimitiveField(DATA_DEFICIENT_KEY);
	}
	
	private Integer getForeignKeyPrimitiveField(String key) {
		PrimitiveField<?> field = proxy.getPrimitiveField(key);
		if (field == null)
			return null;
		else
			return ((ForeignKeyPrimitiveField)field).getValue();
	}
	
	private void setForeignKeyPrimitiveField(String key, Integer value) {
		PrimitiveField<?> field = proxy.getPrimitiveField(key);
		if (field != null) {
			if (value != null)
				((ForeignKeyPrimitiveField)field).setValue(value);
			else
				proxy.getPrimitiveField().remove(field);
		}
		else if (value != null)
			proxy.addPrimitiveField(new ForeignKeyPrimitiveField(key, proxy, value, null));
	}
	
	private String getStringPrimitiveField(String key) {
		String value;
		PrimitiveField<?> field = proxy.getPrimitiveField(key);
		if (field == null)
			value = null;
		else
			value = ((StringPrimitiveField)field).getValue();
		if (value == null)
			value = "";
		return value;
	}
	
	private void setStringPrimitiveField(String key, String value) {
		PrimitiveField<?> field = proxy.getPrimitiveField(key);
		if (field != null) {
			if (value != null && !"".equals(value))
				field.setRawValue(value);
			else
				proxy.getPrimitiveField().remove(field);
		}
		else if (value != null && !"".equals(value))
			proxy.addPrimitiveField(new StringPrimitiveField(key, proxy, value));
	}
	
	private Boolean getBooleanPrimitiveField(String key, boolean defaultValue) {
		PrimitiveField<?> field = proxy.getPrimitiveField(IS_MANUAL_KEY);
		if (field != null)
			return ((BooleanPrimitiveField)field).getValue();
		else
			return defaultValue;
	}
	
	private void setBooleanPrimitiveField(String key, Boolean value, Boolean defaultValue) {
		PrimitiveField<?> field = proxy.getPrimitiveField(key);
		if (field != null) {
			if (value != null && !value.equals(defaultValue))
				((BooleanPrimitiveField)field).setValue(value);
			else
				proxy.getPrimitiveField().remove(field);
		}
		else if (value != null && !value.equals(defaultValue))
			proxy.addPrimitiveField(new BooleanPrimitiveField(key, proxy, value));
		else
			proxy.getPrimitiveField().remove(field);
	}

}
