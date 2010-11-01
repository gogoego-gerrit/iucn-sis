package org.iucn.sis.shared.helpers;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;



public class SynonymData {

	private String upperLevelName = "";

	private String oldVersionName = "";

	private String specieName = "";
	private String genusName = "";
	private String infrarankName = "";
	private int infrarankType = -1;
	private String stockName = "";

	/*
	 * <String level -> String authority>
	 */
	private HashMap<String, String> authorities;
	private String status = "";
	private String notes = "";

	private String redListCategory = "";
	private String redListCriteria = "";
	private String redListAssessor = "";
	private String redListDate = "";

	private int level;

	private String taxaID = "";

	private String assessmentAttachedToID = "";
	private String assessmentStatus = "";

	public final static String ACCEPTED = "ACCEPTED";
	public final static String ADDED = "ADD";
	public final static String DELETED = "DELETE";
	public final static String CHANGED = "CHANGED";

	@Deprecated
	public SynonymData(String oldVersionName, int level, HashMap<String, String> authority, String taxaID) {
		this(oldVersionName, level, authority, ADDED, "", taxaID);
	}

	@Deprecated
	public SynonymData(String oldVersionName, int level, HashMap<String, String> authority, String status,
			String notes, String taxaID) {
		super();
		this.oldVersionName = oldVersionName;
		this.level = level;
		if (authority == null) {
			authorities = new HashMap<String, String>();
		} else {
			authorities = authority;
		}
		this.status = status;
		this.notes = notes;
		this.taxaID = taxaID;

		assessmentAttachedToID = "";
		assessmentStatus = "";
	}

	@Deprecated
	public SynonymData(String oldVersionName, int level, String taxaID) {
		this(oldVersionName, level, null, taxaID);
	}
	
	public SynonymData(String upperLevelName, int level, String taxaID, HashMap<String, String> authority) {
		this(upperLevelName, null, null, null, -1, null, level, authority, null, null, taxaID);
	}

	public SynonymData(String genusName, String speciesName, String infrarankName, int infrarankType, int level,
			HashMap<String, String> authority, String taxaID) {
		this(null, genusName, speciesName, infrarankName, infrarankType, "", level, authority, ADDED, "", taxaID);
	}

	public SynonymData(String genusName, String speciesName, String infrarankName, int infrarankType, int level,
			HashMap<String, String> authority, String status, String notes, String taxaID) {
		this(null, genusName, speciesName, infrarankName, infrarankType, "", level, authority, status, notes, taxaID);
	}

	public SynonymData(String genusName, String speciesName, String infrarankName, int infrarankType, int level,
			String taxaID) {
		this(genusName, speciesName, infrarankName, infrarankType, level, null, taxaID);
	}

	public SynonymData(String genusName, String speciesName, String infrarankName, int infrarankType, String stockName,
			int level, HashMap<String, String> authority, String taxaID) {
		this(null, genusName, speciesName, infrarankName, infrarankType, stockName, level, authority, ADDED, "", taxaID);
	}

	public SynonymData(String upperLevelName, String genusName, String speciesName, String infrarankName, int infrarankType,
			String stockName, int level, HashMap<String, String> authority, String status, String notes, String taxaID) {
		super();
		this.upperLevelName = upperLevelName == null ? "" : upperLevelName.trim();
		this.genusName = genusName == null ? "" : genusName.trim();
		this.specieName = speciesName == null ? "" : speciesName.trim();
		this.infrarankName = infrarankName == null ? "" : infrarankName.trim();
		this.infrarankType = infrarankType;
		this.stockName = stockName == null ? "" : stockName.trim();

		this.level = level;
		if (authority == null) {
			authorities = new HashMap<String, String>();
		} else {
			authorities = authority;
		}
		this.status = status;
		this.notes = notes;
		this.taxaID = taxaID;

		assessmentAttachedToID = "";
		assessmentStatus = "";
	}

	public SynonymData(String genusName, String speciesName, String infrarankName, int infrarankType, String stockName,
			int level, String taxaID) {
		this(genusName, speciesName, infrarankName, infrarankType, stockName, level, null, taxaID);
	}

	public void clearAuthorities() {
		authorities.clear();
	}
	
	public String getFullURI() {
		return "resource/taxon/synonym";
	}
	
	public Iterator<String> getURIPieces() {
		return Arrays.asList(getFullURI().split(",")).listIterator();
	}

	public SynonymData deepCopy() {
		SynonymData newSynonymData = new SynonymData(getGenus(), getSpecie(), getInfrarank(), getInfrarankType(),
				getLevel(), getTaxaID());

		newSynonymData.oldVersionName = oldVersionName;
		newSynonymData.setUpperLevelName(upperLevelName);
		newSynonymData.setAssessmentAttachedToID(assessmentAttachedToID);
		newSynonymData.setAssessmentStatus(status);
		newSynonymData.setLevel(level);
		newSynonymData.setNotes(notes);
		newSynonymData.setRedListAssessor(redListAssessor);
		newSynonymData.setRedListCategory(redListCategory);
		newSynonymData.setRedListCriteria(redListCriteria);
		newSynonymData.setRedListDate(redListDate);
		newSynonymData.authorities = new HashMap<String, String>();
		Iterator<String> iter = authorities.keySet().iterator();
		while (iter.hasNext()) {
			String key = iter.next();
			newSynonymData.authorities.put(key, authorities.get(key));
		}
		newSynonymData.setStatus(status);
		return newSynonymData;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof SynonymData) {
			SynonymData other = (SynonymData) o;
			if (getName().equalsIgnoreCase(other.getName())) {
				for (Iterator<Entry<String, String>> iter = authorities.entrySet().iterator(); iter.hasNext();) {
					Entry<String, String> curEntry = iter.next();

					if (!other.getAuthority(Integer.parseInt(curEntry.getKey().toString())).equalsIgnoreCase(
							curEntry.getValue()))
						return false;
				}
				return true;
			}
		}

		return false;
	}

	public String getAssessmentAttachedToID() {
		return assessmentAttachedToID;
	}

	public String getAssessmentStatus() {
		return assessmentStatus;
	}

	public String getAuthority(int level) {
		String spc = authorities.get(String.valueOf(level));
		if (spc == null)
			spc = "";
		return spc;
	}

	public String getAuthorityString() {
		String auth = "";

		for (int i = 0; i < TaxonNode.INFRARANK_SUBPOPULATION; i++)
			if (authorities.containsKey(i + ""))
				auth += authorities.get(i + "") + " ";

		return auth;
	}

	public String getGenus() {
		return genusName;
	}

	public String getInfrarank() {
		return infrarankName;
	}

	public int getInfrarankType() {
		return infrarankType;
	}

	public int getLevel() {
		return level;
	}

	public String getName() {
		if (isOldVersion())
			return oldVersionName;
		else if (level < TaxonNode.GENUS)
			return upperLevelName;
		else {
			return ((genusName
					+ " "
					+ specieName
					+ " "
					+ (getLevel() == TaxonNode.SUBPOPULATION ? stockName + " " : "")
					+ (infrarankType == TaxonNode.INFRARANK_TYPE_SUBSPECIES ? "ssp. "
							: infrarankType == TaxonNode.INFRARANK_TYPE_VARIETY ? "var. " : "") + infrarankName)
					+ (getLevel() == TaxonNode.INFRARANK_SUBPOPULATION ? " " + stockName : "").trim()).trim();
		}
	}

	public String getNotes() {
		return notes;
	}

	// public String[] getAuthorities() {
	// String[] toReturn = new String[authorities.size()];
	// for(int i=5;i<authorities.size()+5;i++)
	// {
	// String spc = (String)authorities.get(String.valueOf(i));
	// if (spc == null)
	// spc = "";
	// toReturn[i-5]= spc;
	// }
	// return toReturn;
	// }

	// public void setSpcAuthor(String authority) {
	// this.spcAuthor = authority;
	// }

	public String getRedListAssessor() {
		return redListAssessor;
	}

	public String getRedListCategory() {
		return redListCategory;
	}

	public String getRedListCriteria() {
		return redListCriteria;
	}

	public String getRedListDate() {
		return redListDate;
	}

	public String getSpecie() {
		return specieName;
	}

	public String getStatus() {
		return status;
	}

	public String getStockName() {
		return stockName;
	}

	public String getTaxaID() {
		return taxaID;
	}

	public String getUpperLevelName() {
		return upperLevelName;
	}

	public boolean isOldVersion() {
		return !oldVersionName.equals("");
	}

	public void setAssessmentAttachedToID(String assessmentAttachedToID) {
		this.assessmentAttachedToID = assessmentAttachedToID;
	}

	/*
	 * public boolean isDeprecated() { return deprecated; }
	 * 
	 * public void setDeprecated(boolean deprecated) { this.deprecated =
	 * deprecated; }
	 */

	public void setAssessmentStatus(String status) {
		assessmentStatus = status;
	}

	public void setAuthority(String authority, int level) {
		this.authorities.put(String.valueOf(level), authority);
	}

	public void setGenus(String genus) {
		genusName = genus;
	}

	public void setInfrarank(String infrarank) {
		infrarankName = infrarank;
	}

	public void setInfrarankType(int infrarankType) {
		this.infrarankType = infrarankType;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	// public void setName(String name) {
	// this.name = name;
	// }

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public void setRedListAssessor(String redListAssessor) {
		this.redListAssessor = redListAssessor;
	}

	public void setRedListCategory(String redListCategory) {
		this.redListCategory = redListCategory;
	}

	public void setRedListCriteria(String redListCriteria) {
		this.redListCriteria = redListCriteria;
	}

	public void setRedListDate(String redListDate) {
		this.redListDate = redListDate;
	}

	public void setSpecie(String specie) {
		specieName = specie;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public void setStockName(String stockName) {
		this.stockName = stockName;
	}

	public void setTaxaID(String taxaID) {
		this.taxaID = taxaID;
	}

	public void setUpperLevelName(String name) {
		this.upperLevelName = name;
	}

	public String toXML() {
		String myXML = "";
		myXML += "<synonym name=\"" + XMLUtils.clean(oldVersionName) + "\" level=\"" + level + "\" status=\""
				+ getStatus() + "\" notes=\"" + XMLUtils.clean(getNotes());

		if (level < TaxonNode.GENUS) {
			myXML += "\" upperLevelName=\"" + XMLUtils.clean(upperLevelName);
		}

		if (level >= TaxonNode.GENUS) {
			myXML += "\" genusName=\"" + XMLUtils.clean(genusName);
		}

		if (level >= TaxonNode.SPECIES) {
			myXML += "\" speciesName=\"" + XMLUtils.clean(specieName);
		}

		if (level >= TaxonNode.INFRARANK) {
			myXML += "\" infrarankName=\"" + XMLUtils.clean(infrarankName);
		}
		if (level == TaxonNode.INFRARANK) {
			myXML += "\" infrarankType=\"" + XMLUtils.clean(infrarankType);
		}
		if (level == TaxonNode.SUBPOPULATION || level == TaxonNode.INFRARANK_SUBPOPULATION)
			myXML += "\" stockName=\"" + XMLUtils.clean(stockName);

		// String[] auth = getAuthorities();
		//		
		// for(int i=TaxonNode.GENUS; i<auth.length+TaxonNode.GENUS; i++){
		// myXML+= "\" spcAuthor" +i+ "=\"" + XMLUtils.clean(
		// getAuthorities()[i-TaxonNode.GENUS] );
		// }

		Iterator<String> iter = authorities.keySet().iterator();
		while (iter.hasNext()) {
			String key = iter.next();
			String value = XMLUtils.clean(authorities.get(key).toString());
			if (value != null && !value.equalsIgnoreCase(""))
				myXML += "\" " + TaxonNode.getDisplayableLevel(Integer.parseInt(key)) + "=\"" + value;
		}

		myXML += "\" rlCat=\"" + XMLUtils.clean(getRedListCategory()) + "\" rlCrit=\""
				+ XMLUtils.clean(getRedListCriteria()) + "\" rlAssessors=\"" + XMLUtils.clean(getRedListAssessor())
				+ "\" rlDate=\"" + XMLUtils.clean(getRedListDate()) + "\" relatedAssessment=\""
				+ getAssessmentAttachedToID() + "\" relatedAssessmentStatus=\"" + getAssessmentStatus() + "\">"
				+ XMLUtils.clean(getTaxaID()) + "</synonym>\r\n";

		return myXML;
	}
	
	public HashMap<String, String> getAuthorities() {
		return authorities;
		
	}

}
