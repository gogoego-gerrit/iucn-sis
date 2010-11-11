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
public class TaxonLevel implements Serializable, Comparable<TaxonLevel> {
	
	/* THINGS I HAVE ADDED... IF YOU REGENERATE, MUST ALSO COPY THIS*/
	public static String[] displayableLevel = new String[] { "Kingdom", "Phylum", "Class", "Order", "Family", "Genus",
		"Species", "Infrarank", "Subpopulation", "Subpopulation" };
	
	public static final int KINGDOM = 0;
	public static final int PHYLUM = 1;
	public static final int CLASS = 2;
	public static final int ORDER = 3;
	public static final int FAMILY = 4;
	public static final int GENUS = 5;
	public static final int SPECIES = 6;

	public static final int INFRARANK = 7;
	public static final int SUBPOPULATION = 8;
	public static final int INFRARANK_SUBPOPULATION = 9;

	public static String getDisplayableLevel(int level) {
		return displayableLevel[level];	
	}
	
	public static String getDisplayableLevel(int level, int infratype) {
		if( level == INFRARANK ) {
			if( infratype == Infratype.INFRARANK_TYPE_SUBSPECIES )
				return Infratype.SUBSPECIES_NAME;
			else
				return Infratype.VARIETY_NAME;
		} else
			return getDisplayableLevel(level);
	}
	
	public static TaxonLevel getTaxonLevel(int level) {
		if (level < 0)
			return null;
		
		TaxonLevel tl = new TaxonLevel();
		tl.setLevel(level);
		tl.setName(displayableLevel[level]);
		tl.setId(level+1);
		return tl;
	}
	
	/* THINGS I HAVE ADDED... IF YOU REGENERATE, MUST ALSO COPY THIS*/
	
	public TaxonLevel() {
	}
	
	private int id;
	
	private String name;
	
	private int level;
	
	private java.util.Set<Taxon> taxa = new java.util.HashSet<Taxon>();
	
	private java.util.Set<Synonym> synonyms = new java.util.HashSet<Synonym>();
	
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
	
	public void setLevel(int value) {
		this.level = value;
	}
	
	public int getLevel() {
		return level;
	}
	
	public void setTaxa(java.util.Set<Taxon> value) {
		this.taxa = value;
	}
	
	public java.util.Set<Taxon> getTaxa() {
		return taxa;
	}
	
	
	public void setSynonyms(java.util.Set<Synonym> value) {
		this.synonyms = value;
	}
	
	public java.util.Set<Synonym> getSynonyms() {
		return synonyms;
	}
	
	
	public String toString() {
		return String.valueOf(getId());
	}

	@Override
	public int compareTo(TaxonLevel o) {
		if (this == o)
			return 0;
		//RETURNS KINGDOM AS HIGHEST, AND SO ON 
		return Integer.valueOf(o.getLevel()).compareTo(Integer.valueOf(this.getLevel()));
	}
	
	
	
}
