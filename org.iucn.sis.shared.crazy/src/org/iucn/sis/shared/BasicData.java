package org.iucn.sis.shared;

import java.io.Serializable;

public class BasicData implements Serializable {

	private String speciesName;
	private String speciesID;
	private String assessmentID;
	private String commonName;
	private String taxonomicAuthority;
	private String assessmentType;
	private String region;
	private String kingdom = "";
	private String phylum = "";
	private String taxClass = "";
	private String order = "";
	private String family = "";
	private String genus = "";
	private String species = "";
	private String rank;
	private String infrarank;
	private String subpopulation;
	private boolean plantType;

	public BasicData() {
	}

	public String getAssessmentID() {
		return assessmentID;
	}

	public String getAssessmentType() {
		return assessmentType;
	}

	public String getCommonName() {
		return commonName;
	}

	public String getFamily() {
		return family;
	}

	public String getGenus() {
		return genus;
	}

	public String getInfrarank() {
		return infrarank;
	}

	public String getKingdom() {
		return kingdom;
	}

	public String getOrder() {
		return order;
	}

	public String getPhylum() {
		return phylum;
	}

	public String getRank() {
		return rank;
	}

	public String getRegion() {
		return region;
	}

	public String getSpecies() {
		return species;
	}

	public String getSpeciesID() {
		return speciesID;
	}

	public String getSpeciesName() {
		return speciesName;
	}

	public String getSubpopulation() {
		return subpopulation;
	}

	public String getTaxClass() {
		return taxClass;
	}

	public String getTaxonomicAuthority() {
		return taxonomicAuthority;
	}

	public boolean isPlantType() {
		return plantType;
	}

	public void setAssessmentID(String assessmentID) {
		this.assessmentID = assessmentID;
	}

	public void setAssessmentType(String assessmentType) {
		this.assessmentType = assessmentType;
	}

	public void setCommonName(String commonName) {
		this.commonName = commonName;
	}

	public void setFamily(String family) {
		this.family = family;
	}

	public void setGenus(String genus) {
		this.genus = genus;
	}

	public void setInfrarank(String infrarank) {
		this.infrarank = infrarank;
	}

	public void setKingdom(String kingdom) {
		this.kingdom = kingdom;
	}

	public void setOrder(String order) {
		this.order = order;
	}

	public void setPhylum(String phylum) {
		this.phylum = phylum;
	}

	public void setPlantType(boolean plantType) {
		this.plantType = plantType;
	}

	public void setRank(String rank) {
		this.rank = rank;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	public void setSpecies(String species) {
		this.species = species;
	}

	public void setSpeciesID(String speciesID) {
		this.speciesID = speciesID;
	}

	public void setSpeciesName(String speciesName) {
		this.speciesName = speciesName;
	}

	public void setSubpopulation(String subpopulation) {
		this.subpopulation = subpopulation;
	}

	public void setTaxClass(String taxClass) {
		this.taxClass = taxClass;
	}

	public void setTaxonomicAuthority(String taxonomicAuthority) {
		this.taxonomicAuthority = taxonomicAuthority;
	}

}
