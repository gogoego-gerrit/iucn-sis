package org.iucn.sis.shared.api.models;
/**
 * "Visual Paradigm: DO NOT MODIFY THIS FILE!"
 * 
 * This is an automatic generated file. It will be regenerated every time 
 * you generate persistence class.
 * 
 * Modifying its content may cause the program not work, or your work may lost.
 */

/**
 * Licensee: 
 * License Type: Evaluation
 */
import java.io.Serializable;
import java.util.Map;

import org.iucn.sis.shared.api.acl.base.AuthorizableObject;
import org.iucn.sis.shared.api.citations.ReferenceCitationGeneratorShared;
import org.iucn.sis.shared.api.citations.ReferenceCitationGeneratorShared.ReturnedCitation;

import com.solertium.lwxml.shared.NativeElement;
public class Reference implements Serializable, AuthorizableObject {
	
	/* THINGS I HAVE ADDED... IF YOU REGENERATE, MUST ALSO COPY THIS*/
	public static final String ROOT_TAG = "reference";
	
	public static boolean isCitationValid(String complete) {
		return (complete != null && complete.equalsIgnoreCase("y"));
	}
	
	public String getFullURI() {
		return "resource/reference";
	}
	
	public String getProperty(String key) {
		return "";
	}
	
	public static Reference fromXML(NativeElement element) {
		Reference reference = new Reference();
		reference.setId(Integer.parseInt(element.getElementsByTagName("id").elementAt(0).getTextContent()));
		
		String type = element.getElementsByTagName("type").elementAt(0).getTextContent();
		if (!type.equalsIgnoreCase("null")) {
			reference.setType(type);
		}
		
		String citationShort = element.getElementsByTagName("citationShort").elementAt(0).getTextContent();
		if (!citationShort.equalsIgnoreCase("null")) {
			reference.setCitationShort(citationShort);
		}
		
		String citation = element.getElementsByTagName("citation").elementAt(0).getTextContent();
		if (!citation.equalsIgnoreCase("null")) {
			reference.setCitation(citation);
		}
		
		String citationComplete = element.getElementsByTagName("citationComplete").elementAt(0).getTextContent();
		if (!citationComplete.equalsIgnoreCase("null")) {
			reference.setCitationComplete(Boolean.valueOf(citationComplete));
		}
		
		String author = element.getElementsByTagName("author").elementAt(0).getTextContent();
		if (!author.equalsIgnoreCase("null")) {
			reference.setAuthor(author);
		}
		
		String year = element.getElementsByTagName("year").elementAt(0).getTextContent();
		if (!year.equalsIgnoreCase("null")) {
			reference.setYear(year);
		}
		
		String title = element.getElementsByTagName("title").elementAt(0).getTextContent();
		if (!title.equalsIgnoreCase("null")) {
			reference.setTitle(title);
		}
		String secondaryAuthor = element.getElementsByTagName("secondaryAuthor").elementAt(0).getTextContent();
		if (!secondaryAuthor.equalsIgnoreCase("null")) {
			reference.setSecondaryAuthor(secondaryAuthor);
		}
		
		String secondaryTitle = element.getElementsByTagName("secondaryTitle").elementAt(0).getTextContent();
		if (!secondaryTitle.equalsIgnoreCase("null")) {
			reference.setSecondaryTitle(secondaryTitle);
		}
		
		String placePublished = element.getElementsByTagName("placePublished").elementAt(0).getTextContent();
		if (!placePublished.equalsIgnoreCase("null")) {
			reference.setPlacePublished(placePublished);
		}
		
		String publisher = element.getElementsByTagName("publisher").elementAt(0).getTextContent();
		if (!publisher.equalsIgnoreCase("null")) {
			reference.setPublisher(publisher);
		}
		
		String volume = element.getElementsByTagName("volume").elementAt(0).getTextContent();
		if (!volume.equalsIgnoreCase("null")) {
			reference.setVolume(volume);
		}
		
		String numberOfVolumes = element.getElementsByTagName("numberOfVolumes").elementAt(0).getTextContent();
		if (!numberOfVolumes.equalsIgnoreCase("null")) {
			reference.setNumberOfVolumes(numberOfVolumes);
		}
		
		String number = element.getElementsByTagName("number").elementAt(0).getTextContent();
		if (!number.equalsIgnoreCase("null")) {
			reference.setNumber(number);
		}
		
		String pages = element.getElementsByTagName("pages").elementAt(0).getTextContent();
		if (!pages.equalsIgnoreCase("null")) {
			reference.setPages(pages);
		}
		
		String section = element.getElementsByTagName("section").elementAt(0).getTextContent();
		if (!section.equalsIgnoreCase("null")) {
			reference.setSection(section);
		}
		
		String tertiaryAuthor = element.getElementsByTagName("tertiaryAuthor").elementAt(0).getTextContent();
		if (!tertiaryAuthor.equalsIgnoreCase("null")) {
			reference.setTertiaryAuthor(tertiaryAuthor);
		}
		
		String tertiaryTitle = element.getElementsByTagName("tertiaryTitle").elementAt(0).getTextContent();
		if (!tertiaryTitle.equalsIgnoreCase("null")) {
			reference.setTertiaryTitle(tertiaryTitle);
		}
		
		String edition = element.getElementsByTagName("edition").elementAt(0).getTextContent();
		if (!edition.equalsIgnoreCase("null")) {
			reference.setEdition(edition);
		}
		
		String date = element.getElementsByTagName("date").elementAt(0).getTextContent();
		if (!date.equalsIgnoreCase("null")) {
			reference.setDateValue(date);
		}
		
		String subsidiaryAuthor = element.getElementsByTagName("subsidiaryAuthor").elementAt(0).getTextContent();
		if (!subsidiaryAuthor.equalsIgnoreCase("null")) {
			reference.setSecondaryAuthor(subsidiaryAuthor);
		}
		
		String shortTitle = element.getElementsByTagName("shortTitle").elementAt(0).getTextContent();
		if (!shortTitle.equalsIgnoreCase("null")) {
			reference.setShortTitle(shortTitle);
		}
		
		String alternateTitle = element.getElementsByTagName("alternateTitle").elementAt(0).getTextContent();
		if (!alternateTitle.equalsIgnoreCase("null")) {
			reference.setAlternateTitle(alternateTitle);
		}
		
		String isbnissn = element.getElementsByTagName("isbnissn").elementAt(0).getTextContent();
		if (!isbnissn.equalsIgnoreCase("null")) {
			reference.setIsbnIssn(isbnissn);
		}
		
		String keywords = element.getElementsByTagName("keywords").elementAt(0).getTextContent();
		if (!keywords.equalsIgnoreCase("null")) {
			reference.setKeywords(keywords);
		}
		
		String url = element.getElementsByTagName("url").elementAt(0).getTextContent();
		if (!url.equalsIgnoreCase("null")) {
			reference.setUrl(url);
		}
		
		String hash = element.getElementsByTagName("hash").elementAt(0).getTextContent();
		if (!hash.equalsIgnoreCase("null")) {
			reference.setHash(hash);
		}
		
		String bibCode = element.getElementsByTagName("bibCode").elementAt(0).getTextContent();
		if (!bibCode.equalsIgnoreCase("null")) {
			reference.setBibCode(Integer.valueOf(bibCode));
		}
		String bibNoInt = element.getElementsByTagName("bibNoInt").elementAt(0).getTextContent();
		if (!bibNoInt.equalsIgnoreCase("null")) {
			reference.setBibNoInt(Integer.valueOf(bibNoInt));
		}
		String bibNumber = element.getElementsByTagName("bibNumber").elementAt(0).getTextContent();
		if (!bibNumber.equalsIgnoreCase("null")) {
			reference.setBibNumber(Integer.valueOf(bibNumber));
		}
		String externalBibCode = element.getElementsByTagName("externalBibCode").elementAt(0).getTextContent();
		if (!externalBibCode.equalsIgnoreCase("null")) {
			reference.setExternalBibCode(externalBibCode);
		}
		String submissionType = element.getElementsByTagName("submissionType").elementAt(0).getTextContent();
		if (!submissionType.equalsIgnoreCase("null")) {
			reference.setSubmissionType(submissionType);
		}
		
		return reference;
	}
	
	public String toXML() {
		StringBuilder xml = new StringBuilder();
		xml.append("<" + ROOT_TAG + ">");
		xml.append("<id>" + getId() + "</id>");
		xml.append("<type>" + getType() + "</type>");
		xml.append("<citationShort><![CDATA[" + getCitationShort() + "]]></citationShort>");
		xml.append("<citation><![CDATA[" + getCitation() + "]]></citation>");
		xml.append("<citationComplete><![CDATA[" + getCitationComplete() + "]]></citationComplete>");
		xml.append("<author><![CDATA[" + getAuthor() + "]]></author>");
		xml.append("<edition><![CDATA[" + getEdition() + "]]></edition>");
		xml.append("<year><![CDATA[" + getYear() + "]]></year>");
		xml.append("<title><![CDATA[" + getTitle() + "]]></title>");
		xml.append("<secondaryAuthor><![CDATA[" + getSecondaryAuthor() + "]]></secondaryAuthor>");
		xml.append("<secondaryTitle><![CDATA[" + getSecondaryTitle() + "]]></secondaryTitle>");
		xml.append("<placePublished><![CDATA[" + getPlacePublished() + "]]></placePublished>");
		xml.append("<publisher><![CDATA[" + getPublisher() + "]]></publisher>");
		xml.append("<volume><![CDATA[" + getVolume() + "]]></volume>");
		xml.append("<numberOfVolumes><![CDATA[" + getNumberOfVolumes() + "]]></numberOfVolumes>");
		xml.append("<number><![CDATA[" + getNumber() + "]]></number>");
		xml.append("<pages><![CDATA[" + getPages() + "]]></pages>");
		xml.append("<section><![CDATA[" + getSection() + "]]></section>");
		xml.append("<tertiaryAuthor><![CDATA[" + getTertiaryAuthor() + "]]></tertiaryAuthor>");
		xml.append("<tertiaryTitle><![CDATA[" + getTertiaryTitle() + "]]></tertiaryTitle>");
		xml.append("<date><![CDATA[" + getDateValue() + "]]></date>");
		xml.append("<subsidiaryAuthor><![CDATA[" + getSubsidiaryAuthor() + "]]></subsidiaryAuthor>");
		xml.append("<shortTitle><![CDATA[" + getShortTitle() + "]]></shortTitle>");
		xml.append("<alternateTitle><![CDATA[" + getAlternateTitle() + "]]></alternateTitle>");
		xml.append("<isbnissn><![CDATA[" + getIsbnIssn() + "]]></isbnissn>");
		xml.append("<keywords><![CDATA[" + getKeywords() + "]]></keywords>");
		xml.append("<url><![CDATA[" + getUrl() + "]]></url>");
		xml.append("<hash><![CDATA[" + getHash() + "]]></hash>");
		xml.append("<bibCode><![CDATA[" + getBibCode() + "]]></bibCode>");
		xml.append("<bibNumber><![CDATA[" + getBibNumber() + "]]></bibNumber>");
		xml.append("<bibNoInt><![CDATA[" + getBibNoInt() + "]]></bibNoInt>");
		xml.append("<externalBibCode><![CDATA[" + getExternalBibCode() + "]]></externalBibCode>");
		xml.append("<submissionType><![CDATA[" + getSubmissionType() + "]]></submissionType>");
		xml.append("</" + ROOT_TAG + ">");
		return xml.toString();
		
		
	}
	
	public Map<String, String> toMap() {
		//FIXME
		return null;
	}
	
	public void addField(String name, String value) {
		
	}
	
	public String getField(String name) {
		return toMap().get(name);
	}
	
	public boolean hasField(String name) {
		return toMap().containsKey(name);
	}
	
	public boolean isCitationValid() {
		return getCitationComplete();
	}
	
	public void setCitationComplete(boolean isComplete) {
		this.citationComplete = isComplete;
	}
	
	public Boolean getCitationComplete() {
		return citationComplete;
	}
	
	public String generateCitation() {
		ReturnedCitation returnedCitation = ReferenceCitationGeneratorShared.generateNewCitation(toMap(), type);
		if (returnedCitation != null) {
			citation = returnedCitation.citation;
			citationComplete = returnedCitation.allFieldsEntered;
		}
		return citation;
	}
	
	public String generateCitationIfNotAlreadyGenerate() {
		if (citation == null || citation.trim().equalsIgnoreCase(""))
			return generateCitation();
		return citation;
	}
	
	private ReturnedCitation returnedCitation;
	
	public ReturnedCitation getReturnedCitation() {
		return returnedCitation;
	}
	
	public void setReturnedCitation(ReturnedCitation returnedCitation) {
		this.returnedCitation = returnedCitation;
	}
	
	public Reference deepCopy() {
		//FIXME
		return null;
	}
	
	/* THINGS I HAVE ADDED... IF YOU REGENERATE, MUST ALSO COPY THIS*/
	
	public Reference() {
	}
	
	private int id;
	
	private String type;
	
	private String citationShort;
	
	private String citation;
	
	private Boolean citationComplete;
	
	private String author;
	
	private String year;
	
	private String title;
	
	private String secondaryAuthor;
	
	private String secondaryTitle;
	
	private String placePublished;
	
	private String publisher;
	
	private String volume;
	
	private String numberOfVolumes;
	
	private String number;
	
	private String pages;
	
	private String section;
	
	private String tertiaryAuthor;
	
	private String tertiaryTitle;
	
	private String edition;
	
	private String dateValue;
	
	private String subsidiaryAuthor;
	
	private String shortTitle;
	
	private String alternateTitle;
	
	private String isbnIssn;
	
	private String keywords;
	
	private String url;
	
	private String hash;
	
	private Integer bibCode;
	
	private Integer bibNumber;
	
	private Integer bibNoInt;
	
	private String externalBibCode;
	
		
	private String submissionType;
	
	private java.util.Set<Synonym> synonym = new java.util.HashSet<Synonym>();
	
	private java.util.Set<CommonName> common_name = new java.util.HashSet<CommonName>();
	
	private java.util.Set<Assessment> assessment = new java.util.HashSet<Assessment>();
	
	private java.util.Set<Field> field = new java.util.HashSet<Field>();
	
	private java.util.Set<Taxon> taxon = new java.util.HashSet<Taxon>();
	
	
	
	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public Integer getBibCode() {
		return bibCode;
	}

	public void setBibCode(Integer bibCode) {
		this.bibCode = bibCode;
	}

	public Integer getBibNumber() {
		return bibNumber;
	}

	public void setBibNumber(Integer bibNumber) {
		this.bibNumber = bibNumber;
	}

	public Integer getBibNoInt() {
		return bibNoInt;
	}

	public void setBibNoInt(Integer bibNoInt) {
		this.bibNoInt = bibNoInt;
	}

	public String getExternalBibCode() {
		return externalBibCode;
	}

	public void setExternalBibCode(String externalBibCode) {
		this.externalBibCode = externalBibCode;
	}

	public String getSubmissionType() {
		return submissionType;
	}

	public void setSubmissionType(String submissionType) {
		this.submissionType = submissionType;
	}

	public void setId(int value) {
		this.id = value;
	}
	
	public void setReferenceID(int value) {
		setId(value);
	}
	
	public int getReferenceID() {
		return getId();
	}
	
	public int getId() {
		return id;
	}
	
	public int getORMID() {
		return getId();
	}
	
	public void setType(String value) {
		this.type = value;
	}
	
	public String getType() {
		return type;
	}
	
	public void setCitationShort(String value) {
		this.citationShort = value;
	}
	
	public String getCitationShort() {
		return citationShort;
	}
	
	public void setCitation(String value) {
		this.citation = value;
	}
	
	public String getCitation() {
		return citation;
	}
	
	
	
	public void setAuthor(String value) {
		this.author = value;
	}
	
	public String getAuthor() {
		return author;
	}
	
	public void setYear(String value) {
		this.year = value;
	}
	
	public String getYear() {
		return year;
	}
	
	public void setTitle(String value) {
		this.title = value;
	}
	
	public String getTitle() {
		return title;
	}
	
	public void setSecondaryAuthor(String value) {
		this.secondaryAuthor = value;
	}
	
	public String getSecondaryAuthor() {
		return secondaryAuthor;
	}
	
	public void setSecondaryTitle(String value) {
		this.secondaryTitle = value;
	}
	
	public String getSecondaryTitle() {
		return secondaryTitle;
	}
	
	public void setPlacePublished(String value) {
		this.placePublished = value;
	}
	
	public String getPlacePublished() {
		return placePublished;
	}
	
	public void setPublisher(String value) {
		this.publisher = value;
	}
	
	public String getPublisher() {
		return publisher;
	}
	
	public void setVolume(String value) {
		this.volume = value;
	}
	
	public String getVolume() {
		return volume;
	}
	
	public void setNumberOfVolumes(String value) {
		this.numberOfVolumes = value;
	}
	
	public String getNumberOfVolumes() {
		return numberOfVolumes;
	}
	
	public void setNumber(String value) {
		this.number = value;
	}
	
	public String getNumber() {
		return number;
	}
	
	public void setPages(String value) {
		this.pages = value;
	}
	
	public String getPages() {
		return pages;
	}
	
	public void setSection(String value) {
		this.section = value;
	}
	
	public String getSection() {
		return section;
	}
	
	public void setTertiaryAuthor(String value) {
		this.tertiaryAuthor = value;
	}
	
	public String getTertiaryAuthor() {
		return tertiaryAuthor;
	}
	
	public void setTertiaryTitle(String value) {
		this.tertiaryTitle = value;
	}
	
	public String getTertiaryTitle() {
		return tertiaryTitle;
	}
	
	public void setEdition(String value) {
		this.edition = value;
	}
	
	public String getEdition() {
		return edition;
	}
	
	public void setDateValue(String value) {
		this.dateValue = value;
	}
	
	public String getDateValue() {
		return dateValue;
	}
	
	public void setSubsidiaryAuthor(String value) {
		this.subsidiaryAuthor = value;
	}
	
	public String getSubsidiaryAuthor() {
		return subsidiaryAuthor;
	}
	
	public void setShortTitle(String value) {
		this.shortTitle = value;
	}
	
	public String getShortTitle() {
		return shortTitle;
	}
	
	public void setAlternateTitle(String value) {
		this.alternateTitle = value;
	}
	
	public String getAlternateTitle() {
		return alternateTitle;
	}
	
	public void setIsbnIssn(String value) {
		this.isbnIssn = value;
	}
	
	public String getIsbnIssn() {
		return isbnIssn;
	}
	
	public void setKeywords(String value) {
		this.keywords = value;
	}
	
	public String getKeywords() {
		return keywords;
	}
	
	public void setUrl(String value) {
		this.url = value;
	}
	
	public String getUrl() {
		return url;
	}
	
	public void setSynonym(java.util.Set<Synonym> value) {
		this.synonym = value;
	}
	
	public java.util.Set<Synonym> getSynonym() {
		return synonym;
	}
	
	
	public void setCommon_name(java.util.Set<CommonName> value) {
		this.common_name = value;
	}
	
	public java.util.Set<CommonName> getCommon_name() {
		return common_name;
	}
	
	
	public void setAssessment(java.util.Set<Assessment> value) {
		this.assessment = value;
	}
	
	public java.util.Set<Assessment> getAssessment() {
		return assessment;
	}
	
	
	public void setField(java.util.Set<Field> value) {
		this.field = value;
	}
	
	public java.util.Set<Field> getField() {
		return field;
	}
	
	
	public void setTaxon(java.util.Set<Taxon> value) {
		this.taxon = value;
	}
	
	public java.util.Set<Taxon> getTaxon() {
		return taxon;
	}
	
	
	public String toString() {
		return String.valueOf(getId());
	}
	
}
