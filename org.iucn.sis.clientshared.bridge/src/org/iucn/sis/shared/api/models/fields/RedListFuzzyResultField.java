package org.iucn.sis.shared.api.models.fields;

import org.iucn.sis.shared.api.criteriacalculator.CriteriaSet;
import org.iucn.sis.shared.api.criteriacalculator.ExpertResult;
import org.iucn.sis.shared.api.criteriacalculator.ExpertResult.ResultCategory;
import org.iucn.sis.shared.api.models.Field;

public class RedListFuzzyResultField extends ProxyField {
	
	public static final String[] CRITERIA = new String[] {
		"A1a", "A1b", "A1c", "A1d", "A1e",
		"A2a", "A2b", "A2c", "A2d", "A2e",
		"A3b", "A3c", "A3d", "A3e",
		"A4a", "A4b", "A4c", "A4d", "A4e",
		"B1a", "B1bi", "B1bii", "B1biii", "B1biv", "B1bv",
		"B1ci", "B1cii", "B1ciii", "B1civ",
		"B2a", "B2bi", "B2bii", "B2biii", "B2biv", "B2bv",
		"B2ci", "B2cii", "B2ciii", "B2civ",
		"C1", "C2ai", "C2aii", "C2b",
		"D", "D1", "D2", "E"
	};
	
	public RedListFuzzyResultField(Field field) {
		super(field);
	}
	
	public String getCategory() {
		return getStringPrimitiveField("category");
	}
	
	public String getCriteriaCR() {
		return getStringPrimitiveField("criteriaCR");
	}

	public String getCriteriaEN() {
		return getStringPrimitiveField("criteriaEN");
	}
	
	public String getCriteriaVU() {
		return getStringPrimitiveField("criteriaVU");
	}
	
	public String getCriteriaMet() {
		return getStringPrimitiveField("criteriaMet");
	}
	
	public String getCriteria(String criterion) {
		return getStringPrimitiveField(criterion);
	}
	
	public String getResult() {
		return getStringPrimitiveField("text");
	}
	
	public void clearCriteria() {
		for (String criterion : CRITERIA)
			setCriteria(criterion, null);
	}
	
	public void setCategory(String category) {
		setStringPrimitiveField("category", category);
	}
	
	public void setCriteriaCR(String value) {
		setStringPrimitiveField("criteriaCR", value);
	}
	
	public void setCriteriaEN(String value) {
		setStringPrimitiveField("criteriaEN", value);
	}
	
	public void setCriteriaVU(String value) {
		setStringPrimitiveField("criteriaVU", value);
	}
	
	public void setCriteriaMet(String value) {
		setStringPrimitiveField("criteriaMet", value);
	}
	
	public void setCriteria(String criterion, ResultCategory category) {
		String value = category == null ? null : category.getShortName();
		setStringPrimitiveField(criterion, value);
	}
	
	public void setResult(String value) {
		setStringPrimitiveField("text", value);
	}
	
	public void setExpertResult(ExpertResult result) {
		setCategory(result.getAbbreviatedCategory());
		setCriteriaMet(result.getCriteriaString());
		setCriteriaCR(result.getCriteriaCR().toString());
		setCriteriaEN(result.getCriteriaEN().toString());
		setCriteriaVU(result.getCriteriaVU().toString());
		setResult(result.getLeft() + "," + result.getBest() + "," + result.getRight());
		
		clearCriteria();
		
		/*
		 * Write criteria lowest to highest, so that the 
		 * highest met criteria will overwrite the lower 
		 * ones...
		 */
		for (CriteriaSet set : new CriteriaSet[] { result.getCriteriaVU(), result.getCriteriaEN(), result.getCriteriaCR() })
			for (String criterion : set.getCriteria())
				setCriteria(criterion, set.getCategory());
	}
	
	public ExpertResult getExpertResult() {
		ExpertResult result = new ExpertResult();
		try {
			String[] split = getResult().split(",");
			result.setLeft(Integer.valueOf(split[0]));
			result.setBest(Integer.valueOf(split[1]));
			result.setRight(Integer.valueOf(split[2]));
		} catch (Exception e) {
			result.setLeft(-1);
			result.setBest(-1);
			result.setRight(-1);
		}
		
		ResultCategory category = ResultCategory.fromString(getCategory());
		if (category == null)
			category = ResultCategory.DD;
		
		CriteriaSet cr = new CriteriaSet(ResultCategory.CR, proxy);
		CriteriaSet en = new CriteriaSet(ResultCategory.EN, proxy);
		CriteriaSet vu = new CriteriaSet(ResultCategory.VU, proxy);
		
		result.setCriteriaCR(cr);
		result.setCriteriaEN(en);
		result.setCriteriaVU(vu);
		
		result.setResult(category);
		
		switch (category) {
			case CR:
				result.setCriteriaMet(cr);
				break;
			case EN:
				result.setCriteriaMet(en);
				break;
			case VU:
				result.setCriteriaMet(vu);
				break;
			default:
				result.setCriteriaMet(new CriteriaSet(ResultCategory.DD));
		}
		
		return result; 
	}

}
