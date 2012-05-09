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
	
	public static final int INFRARANK_TYPE_SUBSPECIES = 1;
	public static final int INFRARANK_TYPE_VARIETY = 2;
	public static final int INFRARANK_TYPE_FORMA = 3;
	
	private static final String[] names = new String[] { "", 
		"subspecies", "variety", "forma"
	};
	
	private static final String[] codes = new String[] { "",
		"ssp.", "var.", "fma." 
	};
	
	public static final int[] ALL = new int[] {
		INFRARANK_TYPE_SUBSPECIES, INFRARANK_TYPE_VARIETY, INFRARANK_TYPE_FORMA
	};

	public String toXML() {
		return "<" + ROOT_NAME + " id=\"" + id + "\" code=\"" + code + "\">" + name
				+ "</" + ROOT_NAME + ">";
	}

	public static Infratype fromXML(NativeElement element, Taxon taxon) {
		Infratype type = new Infratype();
		type.setId(Integer.valueOf(element.getAttribute("id")));
		type.setCode(element.getAttribute("code"));
		type.setName(element.getTextContent());
		
		if (taxon != null)
			taxon.setInfratype(type);
		return type;

	}
	
	public static Infratype getInfratype(int code) {
		return getInfratype(code, null);
	}
	
	public static Infratype getInfratype(String name) {
		int id = -1;
		for (int i = 0; i < names.length && id < 0; i++)
			if (names[i].equals(name))
				id = i;
		
		return getInfratype(id, null);
	}
	
	public static Infratype getInfratype(int id, Taxon taxon) {
		if (id <= 0 || id >= names.length)
			return null;
		
		Infratype ret = new Infratype(id, names[id], codes[id]);
		
		if (taxon != null)
			taxon.setInfratype(ret);		
		
		return ret;
	}


	public Infratype() {
	}
	
	public Infratype(int id, String name, String code) {
		this.id = id;
		this.name = name;
		this.code = code;
	}

	private Integer id;

	private String name;
	
	private String code;

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
	
	public void setCode(String code) {
		this.code = code;
	}
	
	public String getCode() {
		return code;
	}
	
	/**
	 * Returns the name with the first character in upper case.
	 * @return
	 */
	public String getFormalName() {
		return Character.toUpperCase(name.charAt(0)) + name.substring(1);
	}

	public void setTaxa(Set<Taxon> taxa) {
		this.taxa = taxa;
	}

	public Set<Taxon> getTaxa() {
		return taxa;
	}

}
