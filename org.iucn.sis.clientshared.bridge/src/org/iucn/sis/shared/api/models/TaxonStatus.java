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
public class TaxonStatus implements Serializable {
	/* THINGS I HAVE ADDED... IF YOU REGENERATE, MUST ALSO COPY THIS*/
	public static final String ROOT_TAG = "taxon_status";
	public static final String STATUS_NEW = "N";
	public static final String STATUS_ACCEPTED = "A";
	public static final String STATUS_DISCARDED = "D";
	public static final String STATUS_SYNONYM = "S";
	
	protected static final Integer getIdFromCode(String code) {
		if (code.equals(STATUS_NEW))
			return 1;
		if (code.equals(STATUS_SYNONYM))
			return 2;
		if (code.equals(STATUS_DISCARDED))
			return 3;
		if (code.equals(STATUS_ACCEPTED))
			return 4;
		return 0;
		
	}
	
	public static final Map<String, String> displayableStatus = new LinkedHashMap<String, String>() {
		{
			put(STATUS_NEW, "New");
			put(STATUS_ACCEPTED, "Accepted");
			put(STATUS_DISCARDED, "Discard");
			put(STATUS_SYNONYM, "Synonym");
		}
	};
	
	public static TaxonStatus fromCode(String code) {
		TaxonStatus status = new TaxonStatus();
		status.id = getIdFromCode(code);
		if (status.id == 0) {
			if (code.equalsIgnoreCase("new")){
				code = "N";
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
		return "<" + ROOT_TAG + " code=\"" + code + "\"/>";
	}
	
	public static TaxonStatus fromXML(NativeElement element) {
		return TaxonStatus.fromCode(element.getAttribute("code"));
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
