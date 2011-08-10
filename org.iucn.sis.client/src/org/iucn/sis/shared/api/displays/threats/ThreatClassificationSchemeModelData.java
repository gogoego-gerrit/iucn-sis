package org.iucn.sis.shared.api.displays.threats;

import org.iucn.sis.client.api.caches.TaxonomyCache;
import org.iucn.sis.client.api.caches.VirusCache;
import org.iucn.sis.shared.api.data.TreeDataRow;
import org.iucn.sis.shared.api.models.Field;
import org.iucn.sis.shared.api.models.Taxon;
import org.iucn.sis.shared.api.models.TaxonLevel;
import org.iucn.sis.shared.api.models.Virus;
import org.iucn.sis.shared.api.models.fields.IASTaxaThreatsSubfield;
import org.iucn.sis.shared.api.models.fields.ViralThreatsSubfield;
import org.iucn.sis.shared.api.schemes.ClassificationSchemeModelData;
import org.iucn.sis.shared.api.structures.DisplayStructure;

@SuppressWarnings("unchecked")
public class ThreatClassificationSchemeModelData extends
		ClassificationSchemeModelData {

	public ThreatClassificationSchemeModelData(DisplayStructure structure) {
		super(structure);
	}

	public ThreatClassificationSchemeModelData(DisplayStructure structure, Field field) {
		super(structure, field);
	}
	
	@Override
	public void setSelectedRow(TreeDataRow selectedRow) {
		super.setSelectedRow(selectedRow);
		if (selectedRow != null) {
			if (ThreatViewerFactory.hasTaxa(selectedRow)) {
				IASTaxaThreatsSubfield proxy = new IASTaxaThreatsSubfield(field);
				if (proxy.getIASTaxa() != null) {
					Taxon taxon = TaxonomyCache.impl.getTaxon(proxy.getIASTaxa());
					if (taxon != null)
						replaceNamedText(taxon.getLevel() <= TaxonLevel.GENUS ? 
								"Unspecified " + taxon.getFullName() : taxon.getFullName());
				}
			}
			else if (ThreatViewerFactory.hasVirus(selectedRow)) {
				ViralThreatsSubfield proxy = new ViralThreatsSubfield(field);
				if (proxy.getVirus() != null) {
					Virus virus = VirusCache.impl.getFromCache(proxy.getVirus());
					if (virus != null)
						replaceNamedText(virus.getName());
				}
			}
		}
	}
	
	private void replaceNamedText(String value) {
		String text = selectedRow.getFullLineage();
		int index = text.lastIndexOf("Named");
	
		set("text", text.substring(0, index > 0 ? index : text.length()) + value);
	}
	
	public void save(Field parent, Field field) {
		structure.save(parent, field);
		
		field.setReference(references);
	}

}
