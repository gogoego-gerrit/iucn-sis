package org.iucn.sis.client.panels.search;

import java.util.Date;

import org.iucn.sis.shared.api.models.TaxonLevel;

import com.solertium.util.portable.XMLWritingUtils;

/**
 * Complete query used to search for taxa.  All 
 * configuration information is stored here, and 
 * a unique ID is stored as well to differentiate 
 * queries (queries are NOT currently the same 
 * if they simply query for the same result). 
 *
 */
public class SearchQuery {
	
	private final String query;
	private final String searchID;
	
	private boolean commonName;
	private boolean synonym;
	private boolean scientificName;
	private int level;
	
	private String countryOfOccurrence;
	private String assessor;
	
	public SearchQuery(String query) {
		this.query = query;
		this.searchID = new Date().getTime() + query.toString();
		this.level = TaxonLevel.SPECIES;
	}
	
	public String getQuery() {
		return query;
	}
	
	public boolean isCommonName() {
		return commonName;
	}
	
	public void setCommonName(boolean commonName) {
		this.commonName = commonName;
	}
	
	public boolean isSynonym() {
		return synonym;
	}
	
	public void setSynonym(boolean synonym) {
		this.synonym = synonym;
	}
	
	public boolean isScientificName() {
		return scientificName;
	}
	
	public void setScientificName(boolean scientificName) {
		this.scientificName = scientificName;
	}
	
	public String getCountryOfOccurrence() {
		return countryOfOccurrence;
	}
	
	public void setCountryOfOccurrence(String countryOfOccurrence) {
		this.countryOfOccurrence = countryOfOccurrence;
	}
	
	public String getAssessor() {
		return assessor;
	}
	
	public void setAssessor(String assessor) {
		this.assessor = assessor;
	}
	
	public int getLevel() {
		return level;
	}
	
	public void setLevel(int level) {
		this.level = level;
	}
	
	private boolean isBlank(String text) {
		return text == null || "".equals(text.trim());
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((searchID == null) ? 0 : searchID.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SearchQuery other = (SearchQuery) obj;
		if (searchID == null) {
			if (other.searchID != null)
				return false;
		} else if (!searchID.equals(other.searchID))
			return false;
		return true;
	}

	public String toXML() {
		StringBuilder xml = new StringBuilder();
		xml.append("<search>");
		xml.append(XMLWritingUtils.writeTag("level", getLevel()+""));
		if (isCommonName())
			xml.append(XMLWritingUtils.writeCDATATag("commonName", query));
		if (isSynonym())
			xml.append(XMLWritingUtils.writeCDATATag("synonym", query));
		if (isScientificName())
			xml.append(XMLWritingUtils.writeCDATATag("sciName", query));
		if (!isBlank(countryOfOccurrence))
			xml.append(XMLWritingUtils.writeCDATATag("country", getCountryOfOccurrence()));
		if (!isBlank(assessor))
			xml.append(XMLWritingUtils.writeCDATATag("assessor", getAssessor()));
		xml.append("</search>");
		
		return xml.toString();
	}

}
