package org.iucn.sis.shared.api.models;

import java.io.Serializable;

public class TaxomaticHistory implements Serializable {
	
	private int id;
	
	private TaxomaticOperation operation;
	
	private Taxon taxon;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public TaxomaticOperation getOperation() {
		return operation;
	}

	public void setOperation(TaxomaticOperation operation) {
		this.operation = operation;
	}

	public Taxon getTaxon() {
		return taxon;
	}

	public void setTaxon(Taxon taxon) {
		this.taxon = taxon;
	}
	
}
