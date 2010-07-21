package org.iucn.sis.shared;

import java.io.Serializable;

public class BibliographyData implements Serializable {
	private static final long serialVersionUID = 5;

	private String CREATED = "";
	private String REF_TYPE = "";
	private String AUTHOR = "";
	private String TITLE = "";
	private String PUB_YEAR = "";
	private String PUB_DATE = "";
	private String PERIODICAL_FULL = "";
	private String PERIODICAL_ABBREV = "";
	private String VOLUME = "";
	private String ISSUE = "";
	private String START_PAGE = "";
	private String OTHER_PAGES = "";
	private String EDITION = "";
	private String PUBLISHER = "";
	private String PLACE_OF_PUBLICATION = "";
	private String ISSN_ISBN = "";
	private String LANGUAGE = "";
	private String FOREIGN_TITLE = "";
	private String LINKS = "";
	private String ABSTRACT = "";
	private String NOTES = "";
	private String RETRIEVED_DATE = "";
	private String URL = "";
	private String WEBSITE_TITLE = "";
	private String WEBSITE_EDITOR = "";
	private String COMMENTS = "";

	public BibliographyData() {

	}

	public String getABSTRACT() {
		return ABSTRACT;
	}

	public String getAUTHOR() {
		return AUTHOR;
	}

	public String getCOMMENTS() {
		return COMMENTS;
	}

	public String getCREATED() {
		return CREATED;
	}

	public String getEDITION() {
		return EDITION;
	}

	public String getFOREIGN_TITLE() {
		return FOREIGN_TITLE;
	}

	public String getISSN_ISBN() {
		return ISSN_ISBN;
	}

	public String getISSUE() {
		return ISSUE;
	}

	public String getLANGUAGE() {
		return LANGUAGE;
	}

	public String getLINKS() {
		return LINKS;
	}

	public String getNOTES() {
		return NOTES;
	}

	public String getOTHER_PAGES() {
		return OTHER_PAGES;
	}

	public String getPERIODICAL_ABBREV() {
		return PERIODICAL_ABBREV;
	}

	public String getPERIODICAL_FULL() {
		return PERIODICAL_FULL;
	}

	public String getPLACE_OF_PUBLICATION() {
		return PLACE_OF_PUBLICATION;
	}

	public String getPUB_DATE() {
		return PUB_DATE;
	}

	public String getPUB_YEAR() {
		return PUB_YEAR;
	}

	public String getPUBLISHER() {
		return PUBLISHER;
	}

	public String getREF_TYPE() {
		return REF_TYPE;
	}

	public String getRETRIEVED_DATE() {
		return RETRIEVED_DATE;
	}

	public String getSTART_PAGE() {
		return START_PAGE;
	}

	public String getTITLE() {
		return TITLE;
	}

	public String getURL() {
		return URL;
	}

	public String getVOLUME() {
		return VOLUME;
	}

	public String getWEBSITE_EDITOR() {
		return WEBSITE_EDITOR;
	}

	public String getWEBSITE_TITLE() {
		return WEBSITE_TITLE;
	}

	public void setABSTRACT(String theAbstract) {
		ABSTRACT = theAbstract;
	}

	public void setAUTHOR(String author) {
		AUTHOR = author;
	}

	public void setCOMMENTS(String comments) {
		COMMENTS = comments;
	}

	public void setCREATED(String created) {
		CREATED = created;
	}

	public void setEDITION(String edition) {
		EDITION = edition;
	}

	public void setFOREIGN_TITLE(String foreign_title) {
		FOREIGN_TITLE = foreign_title;
	}

	public void setISSN_ISBN(String issn_isbn) {
		ISSN_ISBN = issn_isbn;
	}

	public void setISSUE(String issue) {
		ISSUE = issue;
	}

	public void setLANGUAGE(String language) {
		LANGUAGE = language;
	}

	public void setLINKS(String links) {
		LINKS = links;
	}

	public void setNOTES(String notes) {
		NOTES = notes;
	}

	public void setOTHER_PAGES(String other_pages) {
		OTHER_PAGES = other_pages;
	}

	public void setPERIODICAL_ABBREV(String periodical_abbrev) {
		PERIODICAL_ABBREV = periodical_abbrev;
	}

	public void setPERIODICAL_FULL(String periodical_full) {
		PERIODICAL_FULL = periodical_full;
	}

	public void setPLACE_OF_PUBLICATION(String place_of_publication) {
		PLACE_OF_PUBLICATION = place_of_publication;
	}

	public void setPUB_DATE(String pub_date) {
		PUB_DATE = pub_date;
	}

	public void setPUB_YEAR(String pub_year) {
		PUB_YEAR = pub_year;
	}

	public void setPUBLISHER(String publisher) {
		PUBLISHER = publisher;
	}

	public void setREF_TYPE(String ref_type) {
		REF_TYPE = ref_type;
	}

	public void setRETRIEVED_DATE(String retrieved_date) {
		RETRIEVED_DATE = retrieved_date;
	}

	public void setSTART_PAGE(String start_page) {
		START_PAGE = start_page;
	}

	public void setTITLE(String title) {
		TITLE = title;
	}

	public void setURL(String url) {
		URL = url;
	}

	public void setVOLUME(String volume) {
		VOLUME = volume;
	}

	public void setWEBSITE_EDITOR(String website_editor) {
		WEBSITE_EDITOR = website_editor;
	}

	public void setWEBSITE_TITLE(String website_title) {
		WEBSITE_TITLE = website_title;
	}

}
