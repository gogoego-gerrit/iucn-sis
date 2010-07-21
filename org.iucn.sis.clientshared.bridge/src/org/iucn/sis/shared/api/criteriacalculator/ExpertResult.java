package org.iucn.sis.shared.api.criteriacalculator;

import org.iucn.sis.shared.api.models.Assessment;

/**
 * Represents the results of an expert-system analysis.
 * 
 * @author liz.schwartz
 * 
 */
public class ExpertResult {
	protected Assessment assessment;
	protected int left;
	protected int right;
	protected int best;
	protected String result;  //the category
	protected String notEnoughData; 
	protected String criteriaString;
	protected String criteriaStringVU;
	protected String criteriaStringEN;
	protected String criteriaStringCR;

	public ExpertResult(Assessment assessment) {
		this.assessment = assessment;
		this.criteriaString = "";
		this.criteriaStringCR = "";
		this.criteriaStringEN = "";
		this.criteriaStringVU = "";
	}

	public String getAbbreviatedCategory() {
		if ((getCriteriaString() == null || getResult() == null))
		{
			return "DD";
		}
		else if (getResult().equalsIgnoreCase("endangered"))
		{
			return "EN";
		}
		else if (getResult().equalsIgnoreCase("vulnerable"))
		{
			return "VU";
		}
		else if (getResult().equalsIgnoreCase("lower risk"))
		{
			return "LC";
		}
		else
		{
			return "CR";
		}
	}

	public Assessment getAssessment() {
		return assessment;
	}

	public int getBest() {
		return best;
	}

	public String getCriteriaString() {
		return criteriaString;
	}

	public String getCriteriaStringCR() {
		return criteriaStringCR;
	}

	public String getCriteriaStringEN() {
		
		return criteriaStringEN;
	}

	public String getCriteriaStringVU() {
		return criteriaStringVU;
	}

	public int getLeft() {
		return left;
	}

	public String getNotEnoughData() {
		return notEnoughData;
	}
	

	public String getResult() {
		return result;
	}

	public int getRight() {
		return right;
	}

	public void setBest(int best) {
		this.best = best;
	}

	public void setCriteriaString(String criteriaString) {
		if (criteriaString == null)
			criteriaString = "";
		this.criteriaString = criteriaString;
	}

	public void setCriteriaStringCR(String criteriaStringCR) {
		if (criteriaStringCR == null)
			criteriaStringCR = "";
		this.criteriaStringCR = criteriaStringCR;
	}

	public void setCriteriaStringEN(String criteriaStringEN) {
		if (criteriaStringEN == null)
			criteriaStringEN = "";
		this.criteriaStringEN = criteriaStringEN;
	}

	public void setCriteriaStringVU(String criteriaStringVU) {
		if (criteriaStringVU == null)
			criteriaStringVU = "";
		this.criteriaStringVU = criteriaStringVU;
	}

	public void setLeft(int left) {
		this.left = left;
	}

	public void setNotEnoughData(String notEnoughData) {
		this.notEnoughData = notEnoughData;
	}
	
	public void setResult(String result) {
		this.result = result;
	}

	public void setRight(int right) {
		this.right = right;
	}

}
