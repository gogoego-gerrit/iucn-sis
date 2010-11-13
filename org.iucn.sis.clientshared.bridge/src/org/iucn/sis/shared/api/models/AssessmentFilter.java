package org.iucn.sis.shared.api.models;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.NativeElement;
import com.solertium.lwxml.shared.NativeNodeList;


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

	protected Set<Region> regions;

	public final static String ROOT_TAG = "asmFilter";
	public final static String PUBLISHED_TAG = "published";
	public final static String DRAFT_TAG = "draft";
	public final static String REGIONS_TAG = "regions";
	public final static String REGION_TAG = "region";
	public final static String ALL_PUBLISHED = "all";
	public final static String RECENT_PUBLISHED = "recent";
	public final static String NO_PUBLISHED = "none";	
	public final static String REGION_TYPE = "region_type";	
	

	private static boolean hasTag(NativeElement element, String tag) {
		NativeNodeList tags = element.getElementsByTagName(tag);
		if (tags.getLength() > 0) {
			return (tags.elementAt(0).getTextContent().equalsIgnoreCase("true"));
		}
		return false;
	}

	public static AssessmentFilter fromXML(NativeDocument ndoc) {
		NativeNodeList items = ndoc.getDocumentElement().getElementsByTagName(ROOT_TAG);
		if (items.getLength() > 0)
			return fromXML(items.elementAt(0));
		return null;
	}

	public static AssessmentFilter fromXML(NativeElement assFilterElement) {
		AssessmentFilter filter = new AssessmentFilter(null);
		filter.regions.clear();
		filter.recentPublished = false;
		filter.allPublished = false;
		filter.draft = false;

		String published = assFilterElement.getElementsByTagName(PUBLISHED_TAG).elementAt(0).getTextContent();
		if (published.equalsIgnoreCase(RECENT_PUBLISHED))
		{
			filter.recentPublished = true;
			filter.allPublished = false;
		}			
		else if (published.equalsIgnoreCase(ALL_PUBLISHED)) {
			filter.allPublished = true;
			filter.recentPublished = false;
		}
		filter.draft = AssessmentFilter.hasTag(assFilterElement, DRAFT_TAG);
		NativeElement regionsTag = assFilterElement.getElementsByTagName(REGIONS_TAG).elementAt(0);
		filter.setRegionType(regionsTag.getAttribute(REGION_TYPE));
		NativeNodeList regions = regionsTag.getElementsByTagName(Region.ROOT_TAG);
		for (int i = 0; i < regions.getLength(); i++) {
			filter.regions.add(Region.fromXML(regions.elementAt(i)));
		}
		return filter;
	}
	
	public AssessmentFilter deepCopy() {
		AssessmentFilter filter = new AssessmentFilter(null);
		filter.regions = new HashSet<Region>(this.getRegions());
		filter.recentPublished = recentPublished;
		filter.allPublished = allPublished;
		filter.draft = draft;
		filter.setRegionType(getRelationshipName());
		return filter;
	}

	public AssessmentFilter() {
		this(DRAFT_TAG);
	}
	
	public AssessmentFilter(String defaultType) {
		
		regions = new HashSet<Region>();
		this.regions.add(Region.getGlobalRegion());
		this.regionType = Relationship.AND;
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

	public Collection<Region> getRegions() {
		return regions;
	}
	
	public List<Integer> listRegionIDs() {
		List<Integer> filterRegionIDs = new ArrayList<Integer>();
		for (Region curReg : regions)
			filterRegionIDs.add(curReg.getId());
		return filterRegionIDs;
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


	public String remove(String xml) {
		int startIndex = xml.indexOf("<" + ROOT_TAG + ">");
		if (startIndex == -1) {
			return xml;
		}
		int endIndex = xml.indexOf("</" + ROOT_TAG + ">")
		+ ("</" + ROOT_TAG + ">").length();
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
		
		StringBuilder xml = new StringBuilder("<" + ROOT_TAG + ">\r");

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
		for (Region region : regions) {
			xml.append(region.toXML());
		}
					
		xml.append("</" + REGIONS_TAG + ">\r");

		xml.append("</" + ROOT_TAG + ">");

		return xml.toString();
	}

	public boolean isDraft() {
		return draft;
	}

	public void setDraft(boolean draft) {
		this.draft = draft;
	}

	public boolean isAllRegions() {
		return this.regionType.equalsIgnoreCase(Relationship.ALL);
	}

	public void setAllRegions() {
		this.regions.clear();
		this.regionType = Relationship.ALL;
	}
	
	public String getRelationshipName() {
		return regionType;
	}

	public boolean setRegionType(String regionType) {
		
		if (regionType.equalsIgnoreCase(Relationship.ALL) || regionType.equalsIgnoreCase(Relationship.AND) || 
				regionType.equalsIgnoreCase(Relationship.OR))
		{
			this.regionType = regionType;
			return true;
		}
		return false;
			
	}
	
	public String getRegionIDsCSV() {
		StringBuilder csv = new StringBuilder();
		for (Region region : regions) {
			csv.append(region.getId() + ",");
		}
		if (csv.length() > 0)
			return csv.substring(0, csv.length()-1);
		return "";
	}
	
	public boolean hasSpecificallySpecifiedRegion() {
		return getRelationshipName().equals(Relationship.AND) && getRegions().size() > 0;
		
	}
	
	public String getRegionType() {
        return regionType;
    }

}
