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

import com.solertium.lwxml.shared.NativeElement;
public class Region implements Serializable {
	
	/* THINGS I HAVE ADDED... IF YOU REGENERATE, MUST ALSO COPY THIS*/
	public static final int GLOBAL_ID = 1;
	public static String ROOT_TAG = "region";
	
	
	public static Region getGlobalRegion() {
		Region global = new Region();
		global.setId(Integer.valueOf(GLOBAL_ID));
		global.setDescription("Global");
		global.setName("Global");
		return global;
	}
	
	public static Region fromXML(NativeElement el) {
		String id = el.getAttribute("id");
		String name = el.getAttribute("name");
		String description = el.getTextContent();
		return new Region(Integer.parseInt(id), name, description);
	}
	
	public String toXML() {
		StringBuilder str = new StringBuilder("<");
		str.append(ROOT_TAG);
		str.append(" name=\"");
		str.append(getName());
		str.append("\" id=\"");
		str.append(getId());
		str.append("\"><![CDATA[");
		str.append(getDescription());
		str.append("]]></" + ROOT_TAG + ">");
		return str.toString();
	}
		
	public String getRegionName() {
		return getName();
	}
	
	public Region(int id, String name, String description) {
		this.id = id;
		this.name = name; 
		this.description = description;
	}
	
	/* THINGS I HAVE ADDED... IF YOU REGENERATE, MUST ALSO COPY THIS*/
	
	public Region() {
	}
	
	private int id;
	
	private String name;
	
	private String description;
	
	private java.util.Set<WorkingSet> working_set = new java.util.HashSet<WorkingSet>();
	
	private void setId(int value) {
		this.id = value;
	}
	
	public int getId() {
		return id;
	}
	
	public int getORMID() {
		return getId();
	}
	
	public void setName(String value) {
		this.name = value;
	}
	
	public String getName() {
		return name;
	}
	
	public void setDescription(String value) {
		this.description = value;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setWorking_set(java.util.Set<WorkingSet> value) {
		this.working_set = value;
	}
	
	public java.util.Set<WorkingSet> getWorking_set() {
		return working_set;
	}
	
	
	public String toString() {
		return "Region #" + getId() + " (" + name + ")";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
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
		Region other = (Region) obj;
		if (id != other.id)
			return false;
		return true;
	}
	
}
