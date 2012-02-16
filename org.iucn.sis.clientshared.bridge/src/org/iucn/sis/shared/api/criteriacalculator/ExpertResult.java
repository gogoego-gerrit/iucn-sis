package org.iucn.sis.shared.api.criteriacalculator;

import org.iucn.sis.shared.api.models.Assessment;

/**
 * Represents the results of an expert-system analysis.
 * 
 * @author liz.schwartz
 * 
 */
public class ExpertResult {
	
	public enum ResultCategory {
		CR("CR", "Critically Endangered", "CR"), 
		EN("EN", "Endangered", "EN", "CR", "EN"),
		VU("VU", "Vulnerable", "CR", "EN", "VU"),
		LR("LC", "Least Concern"),
		DD("DD", "Data Deficient");
		
		public static ResultCategory fromString(String shortName) {
			for (ResultCategory c : ResultCategory.values())
				if (c.shortName.equals(shortName))
					return c;
			return null;
		}
		
		private final String shortName, name;
		private final String[] includes;
		
		private ResultCategory(String shortName, String name, String... includes) {
			this.shortName = shortName;
			this.name = name;
			this.includes = includes;
		}
		
		public String getName() {
			return name;
		}
		
		public String getShortName() {
			return shortName;
		}
		
		public boolean includes(String categoryShortName) {
			for (String name : includes)
				if (name.equals(categoryShortName))
					return true;
			return false;
		}
		
	}
	
	protected Assessment assessment;
	protected int left;
	protected int right;
	protected int best;
	protected ResultCategory result;  //the category
	protected String notEnoughData; 
	
	protected CriteriaSet criteria;
	protected CriteriaSet criteriaVU;
	protected CriteriaSet criteriaEN;
	protected CriteriaSet criteriaCR;

	public ExpertResult() {
		this.criteria = new CriteriaSet(ResultCategory.DD);
		this.criteriaCR = new CriteriaSet(ResultCategory.CR);
		this.criteriaEN = new CriteriaSet(ResultCategory.EN);
		this.criteriaVU = new CriteriaSet(ResultCategory.VU);
	}

	public String getAbbreviatedCategory() {
		return getResult().getShortName();
	}

	public int getBest() {
		return best;
	}
	
	public String getCriteriaString() {
		return criteria.toString();
	}
	
	public CriteriaSet getCriteriaMet() {
		return criteria;
	}
	
	public CriteriaSet getCriteriaCR() {
		return criteriaCR;
	}
	
	public CriteriaSet getCriteriaEN() {
		return criteriaEN;
	}
	
	public CriteriaSet getCriteriaVU() {
		return criteriaVU;
	}

	public int getLeft() {
		return left;
	}

	public String getNotEnoughData() {
		return notEnoughData;
	}

	public ResultCategory getResult() {
		return result;
	}

	public int getRight() {
		return right;
	}

	public void setBest(int best) {
		this.best = best;
	}

	public void setCriteriaMet(CriteriaSet criteria) {
		this.criteria = criteria;
	}
	
	public void setCriteriaCR(CriteriaSet criteriaCR) {
		this.criteriaCR = criteriaCR;
	}
	
	public void setCriteriaEN(CriteriaSet criteriaEN) {
		this.criteriaEN = criteriaEN;
	}
	
	public void setCriteriaVU(CriteriaSet criteriaVU) {
		this.criteriaVU = criteriaVU;
	}

	public void setLeft(int left) {
		this.left = left;
	}

	public void setNotEnoughData(String notEnoughData) {
		this.notEnoughData = notEnoughData;
	}
	
	public void setResult(ResultCategory result) {
		this.result = result;
	}

	public void setRight(int right) {
		this.right = right;
	}

}
