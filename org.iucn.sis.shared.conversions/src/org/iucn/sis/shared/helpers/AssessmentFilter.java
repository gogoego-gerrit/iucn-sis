package org.iucn.sis.shared.helpers;

import java.util.ArrayList;
import java.util.List;



import com.solertium.lwxml.factory.NativeDocumentFactory;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.NativeElement;
import com.solertium.lwxml.shared.NativeNodeList;
import com.solertium.lwxml.shared.utils.NativeDocumentSerializer;


/**
 * Class that holds all information that could filter out assessments based on type and global and/or region
 * 
 * @author liz.schwartz
 *
 */
public class AssessmentFilter {

	protected boolean recentPublished;
	protected boolean allPublished;
	protected boolean draft;
	protected String regionType;

	protected List<String> regions;

	public final static String HEAD_TAG = "asmFilter";

	public final static String PUBLISHED_TAG = "published";

	public final static String DRAFT_TAG = "draft";

	public final static String REGIONS_TAG = "regions";

	public final static String REGION_TAG = "region";

	public final static String ALL_PUBLISHED = "all";

	public final static String RECENT_PUBLISHED = "recent";

	public final static String NO_PUBLISHED = "none";
	
	public final static String REGION_TYPE = "region_type";
	
	public final static String REGION_TYPE_AND = "and";
	
	public final static String REGION_TYPE_OR = "or";
	
	public final static String REGION_TYPE_ALL = "all";

	private static boolean hasTag(NativeElement element, String tag) {
		NativeNodeList tags = element.getElementsByTagName(tag);
		if (tags.getLength() > 0) {
			return (tags.elementAt(0).getTextContent().equalsIgnoreCase("true"));
		}
		return false;
	}

	public static AssessmentFilter parseXML(NativeDocument ndoc) {
		NativeNodeList items = ndoc.getDocumentElement().getElementsByTagName(HEAD_TAG);
		if (items.getLength() > 0)
			return parseXML(items.elementAt(0));
		return null;
	}

	public static AssessmentFilter parseXML(NativeElement assFilterElement) {
		AssessmentFilter assFilter = new AssessmentFilter(null);
		assFilter.regions.clear();
		assFilter.recentPublished = false;
		assFilter.allPublished = false;
		assFilter.draft = false;

		String published = assFilterElement.getElementsByTagName(PUBLISHED_TAG).elementAt(0).getTextContent();
		if (published.equalsIgnoreCase(RECENT_PUBLISHED))
		{
			assFilter.recentPublished = true;
			assFilter.allPublished = false;
		}			
		else if (published.equalsIgnoreCase(ALL_PUBLISHED)) {
			assFilter.allPublished = true;
			assFilter.recentPublished = false;
		}
		assFilter.draft = AssessmentFilter.hasTag(assFilterElement, DRAFT_TAG);
		NativeElement regionsTag = assFilterElement.getElementsByTagName(REGIONS_TAG).elementAt(0);
		assFilter.setRegionType(regionsTag.getAttribute(REGION_TYPE));
		NativeNodeList regions = regionsTag.getElementsByTagName(REGION_TAG);
		for (int i = 0; i < regions.getLength(); i++) {
//			System.out.println("adding region " + regions.elementAt(i).getTextContent());
			assFilter.regions.add(regions.elementAt(i).getTextContent());
		}
		return assFilter;
	}
	
	public AssessmentFilter deepCopy() {
		AssessmentFilter filter = new AssessmentFilter(null);
		filter.regions = new ArrayList<String>(this.getRegions());
		filter.recentPublished = recentPublished;
		filter.allPublished = allPublished;
		filter.draft = draft;
		filter.setRegionType(getRegionType());
		return filter;
	}

	public AssessmentFilter() {
		this(DRAFT_TAG);
	}
	
	public AssessmentFilter(String defaultType) {
//		System.out.println("creating defaultType " + defaultType);
		
		regions = new ArrayList<String>();
		this.regions.add(BaseAssessment.GLOBAL_ID);
		this.regionType = REGION_TYPE_AND;
		if (defaultType == null)
		{
			this.allPublished = false;
			this.draft = false;
			this.recentPublished = false;
			this.regions.clear();
		}
		else if (defaultType.equalsIgnoreCase(ALL_PUBLISHED)) {
			this.allPublished = true;
			this.draft = false;
			this.recentPublished = false;			
		}
		else if (defaultType.equalsIgnoreCase(RECENT_PUBLISHED)) {
			this.recentPublished = true;
			this.draft = false;
			this.allPublished = false;
		}
		else if (defaultType.equalsIgnoreCase(DRAFT_TAG)) {
			this.draft = true;
			this.recentPublished = false;
			this.allPublished = false;
		}
		
		
	}

	public List<String> getRegions() {
		return regions;
	}

	public NativeDocument insertAssessmentFilter(NativeDocument doc) {
		String xml = NativeDocumentSerializer.serialize(doc);
		xml = insertAssessmentFilter(xml);
		if (xml == null)
			return null;

		NativeDocument ndoc = NativeDocumentFactory.newNativeDocument();
		ndoc.parse(xml);
		ndoc.setUser(doc.getUser());
		ndoc.setPass(doc.getPass());
		return ndoc;

	}

	public String insertAssessmentFilter(String xml) {
		int index = -1;
		boolean done = false;
		while (!done) {
			index = xml.indexOf("<", index + 1);
			if (index == -1)
				done = true;
			if (!xml.substring(index + 1).matches("?|!.*")) {
				done = true;
				index = xml.indexOf('>', index);
				if (xml.startsWith("\r", index))
					index++;
				if (xml.startsWith("\n", index))
					index++;
			}
		}

		if (index > -1) {
			xml = xml.substring(0, index) + toXML() + xml.substring(index);
			return xml;
		}
		return null;
	}

	public boolean isAllPublished() {
		return allPublished;
	}


	public boolean isRecentPublished() {
		return recentPublished;
	}

	public NativeDocument remove(NativeDocument ndoc) {
		String xml = NativeDocumentSerializer.serialize(ndoc);
		String newXml = remove(xml);
		NativeDocument doc = NativeDocumentFactory.newNativeDocument();
		doc.parse(newXml);
		return doc;
	}

	public String remove(String xml) {
		int startIndex = xml.indexOf("<" + HEAD_TAG + ">");
		if (startIndex == -1) {
			return xml;
		}
		int endIndex = xml.indexOf("</" + HEAD_TAG + ">")
		+ ("</" + HEAD_TAG + ">").length();
		xml = (xml.substring(0, startIndex) + xml.substring(endIndex));
		return xml;
	}


	public void setAllPublished(boolean allPublished) {
		this.allPublished = allPublished;
	}


	public void setRecentPublished(boolean recentPublished) {
		this.recentPublished = recentPublished;
	}

	public String toXML() {
		
//		System.out.println("in toXML with regionSize " + regions.size());
		StringBuilder xml = new StringBuilder("<" + HEAD_TAG + ">\r");

		xml.append("<" + PUBLISHED_TAG + ">");
		if (recentPublished)
			xml.append(RECENT_PUBLISHED);
		else if (allPublished)
			xml.append(ALL_PUBLISHED);
		else
			xml.append(NO_PUBLISHED);
		xml.append("</" + PUBLISHED_TAG + ">\r");

		xml.append("<" + DRAFT_TAG + ">" + draft + "</" + DRAFT_TAG + ">\r");

		xml.append("<" + REGIONS_TAG + " " + REGION_TYPE + "=\"" + this.regionType + "\">\r");
		for (String region : regions) {
			xml.append("<" + REGION_TAG + ">" + region + "</" + REGION_TAG + ">\r");
//			System.out.println("just appeneded the region " + region + " with regions.size == " + regions.size());
		}
					
		xml.append("</" + REGIONS_TAG + ">\r");

		xml.append("</" + HEAD_TAG + ">");

		return xml.toString();
	}

	public boolean isDraft() {
		return draft;
	}

	public void setDraft(boolean draft) {
//		System.out.println("setting draft to " + draft);
		this.draft = draft;
	}

	public boolean isAllRegions() {
		return this.regionType.equalsIgnoreCase(REGION_TYPE_ALL);
	}

	public void setAllRegions() {
		this.regions.clear();
		this.regionType = REGION_TYPE_ALL;
	}
	
	public String getRegionType() {
		return regionType;
	}

	public boolean setRegionType(String regionType) {
		
//		System.out.println("coming in here with region type " + regionType);
		if (regionType.equalsIgnoreCase(REGION_TYPE_ALL) || regionType.equalsIgnoreCase(REGION_TYPE_AND) || 
				regionType.equalsIgnoreCase(REGION_TYPE_OR))
		{
			this.regionType = regionType;
//			System.out.println("able to set the region to " + regionType);
			return true;
		}
//		System.out.println("unable to set the region type");
		return false;
			
	}
	
	public String getRegionIDsCSV() {
		StringBuilder csv = new StringBuilder();
		for (String region : regions) {
			csv.append(region + ",");
		}
		if (csv.length() > 0)
			return csv.substring(0, csv.length()-1);
		return "";
	}
	
	public boolean hasSpecificallySpecifiedRegion() {
		return getRegionType().equals(AssessmentFilter.REGION_TYPE_AND) && getRegions().size() > 0;
		
	}
}
