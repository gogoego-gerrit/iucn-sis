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
import java.util.LinkedHashMap;
import java.util.Map;

import com.solertium.lwxml.shared.NativeElement;

@SuppressWarnings("serial")
public class TaxonStatus implements Serializable {
	/* THINGS I HAVE ADDED... IF YOU REGENERATE, MUST ALSO COPY THIS*/
	public static final String ROOT_TAG = "taxon_status";
	
	public static final String STATUS_NEW = "N";
	public static final String STATUS_ACCEPTED = "A";
	public static final String STATUS_DISCARDED = "D";
	public static final String STATUS_SYNONYM = "S";
	public static final String STATUS_UNCERTAIN = "Q";
	public static final String STATUS_UNPUBLISHED = "P";
	public static final String STATUS_NOT_ASSIGNED = "I";
	
	public static final String[] ALL = new String[] {
		STATUS_NEW, STATUS_ACCEPTED, STATUS_DISCARDED, STATUS_SYNONYM, 
		STATUS_UNCERTAIN, STATUS_UNPUBLISHED, STATUS_NOT_ASSIGNED
	};
	
	protected static final Integer getIdFromCode(String code) {
		final int id;
		if (STATUS_NEW.equals(code))
			id = 1;
		else if (STATUS_SYNONYM.equals(code))
			id = 2;
		else if (STATUS_DISCARDED.equals(code))
			id = 3;
		else if (STATUS_ACCEPTED.equals(code))
			id = 4;
		else if (STATUS_UNCERTAIN.equals(code))
			id = 5;
		else if (STATUS_UNPUBLISHED.equals(code))
			id = 6;
		else if (STATUS_NOT_ASSIGNED.equals(code))
			id = 7;
		else
			id = 0;
		
		return id;
	}
	
	public static final Map<String, String> displayableStatus = new LinkedHashMap<String, String>() {
		{
			put(STATUS_NEW, "New");
			put(STATUS_ACCEPTED, "Accepted");
			put(STATUS_DISCARDED, "Discard");
			put(STATUS_SYNONYM, "Synonym");
			put(STATUS_UNCERTAIN, "Uncertain");
			put(STATUS_UNPUBLISHED, "Unpublished");
			put(STATUS_NOT_ASSIGNED, "Not Assigned");
		}
	};
	
	public static TaxonStatus fromCode(String code) {
		TaxonStatus status = new TaxonStatus();
		status.id = getIdFromCode(code);
		if (status.id == 0) {
			if (code.equalsIgnoreCase("new")){
				code = STATUS_NEW;
				status.id = getIdFromCode(code);
			} else {
				code = STATUS_ACCEPTED;
				status.id = getIdFromCode(code);
			}	
		}
		status.code = code;
		status.name = displayableStatus.get(code);
		return status;
	}
	
	public String toXML() {
		return "<" + ROOT_TAG + " " +
				"code=\"" + getCode() + "\" " +
				"id=\"" + getId() + "\"><![CDATA[" + 
				getName() + "]]></" + ROOT_TAG + ">";
	}
	
	public static TaxonStatus fromXML(NativeElement element) {
		Integer id = Integer.valueOf(element.getAttribute("id"));
		String code = element.getAttribute("code");
		String name = element.getTextContent();
		
		TaxonStatus status = new TaxonStatus();
		status.setId(id);
		status.setName(name);
		status.setCode(code);
		
		return status;
	}
	
	/* THINGS I HAVE ADDED... IF YOU REGENERATE, MUST ALSO COPY THIS*/
	
	
	public TaxonStatus() {
	}
	
	private int id;
	
	private String name;
	
	private String code;
	
	private java.util.Set<Taxon> taxa = new java.util.HashSet<Taxon>();
	
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
	
	public void setCode(String value) {
		this.code = value;
	}
	
	public String getCode() {
		return code;
	}
	
	public void setTaxa(java.util.Set<Taxon> value) {
		this.taxa = value;
	}
	
	public java.util.Set<Taxon> getTaxa() {
		return taxa;
	}
	
	public String toString() {
		return String.valueOf(getId());
	}
	
}
