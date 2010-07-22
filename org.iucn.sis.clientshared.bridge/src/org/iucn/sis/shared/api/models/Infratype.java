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
import java.util.HashSet;
import java.util.Set;

import com.solertium.lwxml.shared.NativeElement;

public class Infratype implements Serializable {

	/* THINGS I HAVE ADDED... IF YOU REGENERATE, MUST ALSO COPY THIS */
	public final static String ROOT_NAME = "infraType";
	public final static String SUBSPECIES_NAME = "subspecies";
	public final static String VARIETY_NAME = "variety";
	
	//public static final int INFRARANK_TYPE_NA = -1;
	public static final int INFRARANK_TYPE_SUBSPECIES = 1;
	public static final int INFRARANK_TYPE_VARIETY = 2;

	public String toXML() {
		return "<" + ROOT_NAME + " id=\"" + id + "\">" + name
				+ "</" + ROOT_NAME + ">";
	}

	public static Infratype fromXML(NativeElement element, Taxon taxon) {
		Infratype type = new Infratype();
		type.setId(Integer.valueOf(element.getAttribute("id")));
		type.setName(element.getTextContent());
		
		if (taxon != null)
			taxon.setInfratype(type);
		return type;

	}
	
	public static Infratype getInfratype(int code) {
		return getInfratype(code, null);
	}
	
	public static Infratype getInfratype(String name) {
		Infratype ret = new Infratype();
		if( name.equals(SUBSPECIES_NAME))
			ret.id= INFRARANK_TYPE_SUBSPECIES;
		else
			ret.id= INFRARANK_TYPE_VARIETY;
		ret.name = name;
		return ret;
	}
	
	public static Infratype getInfratype(int code, Taxon taxon) {
		Infratype ret = new Infratype();
		if( code == INFRARANK_TYPE_SUBSPECIES )
			ret.name = SUBSPECIES_NAME;
		else
			ret.name = VARIETY_NAME;
		ret.id = code;
		
		if (taxon != null)
			taxon.setInfratype(ret);		
		
		return ret;
	}

	/* THINGS I HAVE ADDED... IF YOU REGENERATE, MUST ALSO COPY THIS */

	public Infratype() {
	}
	
	public Infratype(Integer code, String infraName) {
		this.id = code;
		this.name = infraName;
	}

	private Integer id;

	private String name;

	private Set<Taxon> taxa = new HashSet<Taxon>();

	public void setId(Integer value) {
		this.id = value;
	}

	public Integer getId() {
		return id;
	}

	public void setName(String value) {
		this.name = value;
	}

	public String getName() {
		return name;
	}

	public void setTaxa(Set<Taxon> taxa) {
		this.taxa = taxa;
	}

	public Set<Taxon> getTaxa() {
		return taxa;
	}



}
