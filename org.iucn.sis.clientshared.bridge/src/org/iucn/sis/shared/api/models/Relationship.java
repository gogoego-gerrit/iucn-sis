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
public class Relationship implements Serializable {
	
	/* THINGS I HAVE ADDED... IF YOU REGENERATE, MUST ALSO COPY THIS*/
	public final static String AND = "and";
	public final static String OR = "or";
	public final static String ALL = "all";
	public final static Integer AND_ID = 1;
	public final static Integer OR_ID = 2;
	public final static Integer ALL_ID = 3;
	
	
	public final static String ROOT_TAG = "relationship";
	
	public String toXML() {
		return "<" + ROOT_TAG + " id=\"" + getId() + "\" >" + getName() + "</" + ROOT_TAG + ">";
	}
	
	public static Relationship fromXML(NativeElement element) {
		Relationship relationship = new Relationship();
		relationship.setId(Integer.valueOf(element.getAttribute("id")));
		relationship.setName(element.getTextContent());
		return relationship;
	}
	
	public static Relationship fromName(String name) {
		Relationship rel = new Relationship();
		
		if (AND.equals(name))
			rel.setId(AND_ID);
		else if (OR.equals(name))
			rel.setId(OR_ID);
		else if (ALL.equals(name))
			rel.setId(ALL_ID);		
		else
			return null;
		
		rel.setName(name);		
		return rel;
	}
	
	
	
	/* THINGS I HAVE ADDED... IF YOU REGENERATE, MUST ALSO COPY THIS*/
	
	public Relationship() {
	}
	
	public boolean equals(Object aObj) {
		if (aObj == this)
			return true;
		if (!(aObj instanceof Relationship))
			return false;
		Relationship relationship = (Relationship)aObj;
		if (getId() != relationship.getId())
			return false;
		return true;
	}
	
	public int hashCode() {
		int hashcode = 0;
		hashcode = hashcode + (int) getId();
		return hashcode;
	}
	
	private int id;
	
	private String name;
	
	private java.util.Set<WorkingSet> workingSet = new java.util.HashSet<WorkingSet>();
	
	public void setId(int value) {
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
	
	public void setWorkingSet(java.util.Set<WorkingSet> value) {
		this.workingSet = value;
	}
	
	public java.util.Set<WorkingSet> getWorkingSet() {
		return workingSet;
	}
	
	
	public String toString() {
		return String.valueOf(getId());
	}
	
}
