package org.iucn.sis.shared.data.assessments;

import org.iucn.sis.shared.xml.XMLUtils;

import com.solertium.lwxml.shared.NativeElement;

public class Region {

	public final static String DEFAULT_NEW_ID = "New";

	private String id;
	private String regionName;
	private String description;

	public Region(NativeElement el) {
		this.id = el.getAttribute("id");
		this.regionName = XMLUtils.cleanFromXML(el.getElementByTagName("name").getTextContent());
		this.description = XMLUtils.cleanFromXML(el.getElementByTagName("description").getTextContent());
	}

	public Region(String id, String regionName, String description) {
		this.id = id;
		this.regionName = regionName;
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

	public String getId() {
		return id;
	}

	public String getRegionName() {
		return regionName;
	}

	public void setDescription(String desc) {
		description = desc;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setRegionName(String region) {
		regionName = region;
	}

	public String toXML() {
		String xml = "<region id=\"" + id + "\">" + "<name>" + XMLUtils.clean(regionName.trim()) + "</name>"
				+ "<description>" + XMLUtils.clean(description.trim()) + "</description>" + "</region>";
		return xml;

	}

}
