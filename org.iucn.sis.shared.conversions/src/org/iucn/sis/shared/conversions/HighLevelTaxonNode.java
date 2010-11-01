package org.iucn.sis.shared.conversions;

import org.iucn.sis.shared.helpers.TaxonNode;

public class HighLevelTaxonNode extends TaxonNode {
	public HighLevelTaxonNode(long id, int level, String name, String parentID, String parentName, String status,
			String addedBy, String creationDate) {
		super(id, name, level, parentID, parentName, addedBy, creationDate);
		this.status = status;
	}
}
