package org.iucn.sis.shared.conversions;

public class LowLevelTaxonNode extends TaxonNode {

	public LowLevelTaxonNode(long id, int level, String name, String parentID, String parentName, boolean hybrid,
			String status, String addedBy, String creationDate) {
		super(id, name, level, parentID, parentName, addedBy, creationDate);
		this.hybrid = hybrid;
		this.status = status;
	}
}
