package org.iucn.sis.shared.api.displays.threats;

import java.util.ArrayList;
import java.util.List;

import org.iucn.sis.client.api.caches.TaxonomyCache;
import org.iucn.sis.client.api.caches.VirusCache;
import org.iucn.sis.shared.api.data.TreeData;
import org.iucn.sis.shared.api.data.TreeDataRow;
import org.iucn.sis.shared.api.debug.Debug;
import org.iucn.sis.shared.api.displays.ClassificationScheme;
import org.iucn.sis.shared.api.models.Field;
import org.iucn.sis.shared.api.models.Virus;
import org.iucn.sis.shared.api.models.fields.IASTaxaThreatsSubfield;
import org.iucn.sis.shared.api.models.fields.ViralThreatsSubfield;
import org.iucn.sis.shared.api.schemes.ClassificationSchemeModelData;
import org.iucn.sis.shared.api.schemes.ClassificationSchemeViewer;
import org.iucn.sis.shared.api.structures.DisplayStructure;

import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.lwxml.shared.NativeNode;
import com.solertium.util.events.ComplexListener;

public class Threats extends ClassificationScheme {
	
	public Threats(NativeNode node) {
		super(new ThreatsTreeData(node));
	}
	
	@Override
	protected BasicThreatViewer generateDefaultDisplayStructure(TreeDataRow row) {
		ThreatsTreeData treeData = (ThreatsTreeData) this.treeData;
		
		BasicThreatViewer structure = ThreatViewerFactory.generateStructure(treeData, row);
		structure.setIsVisible(treeData.getIsVisible());
		structure.setName(treeData.getName());
		
		return structure;
	}
	
	@Override
	public void setField(final Field field) {
		final List<Integer> taxaToFetch = new ArrayList<Integer>();
		final List<Integer> virusToFetch = new ArrayList<Integer>();
		
		for (Field subfield : field.getFields()) {
			if ((canonicalName + "Subfield").equals(subfield.getName())) {
				IASTaxaThreatsSubfield proxy = new IASTaxaThreatsSubfield(subfield);
				Integer taxonID = proxy.getIASTaxa();
				if (taxonID != null)
					taxaToFetch.add(taxonID);
				
				ViralThreatsSubfield vProxy = new ViralThreatsSubfield(subfield);
				Integer virusID = vProxy.getVirus();
				if (virusID != null)
					virusToFetch.add(virusID);
			}
		}
		
		TaxonomyCache.impl.fetchList(taxaToFetch, new GenericCallback<String>() {
			public void onSuccess(String result) {
				final ComplexListener<List<Virus>> callback = new ComplexListener<List<Virus>>() {
					public void handleEvent(List<Virus> eventData) {
						Threats.super.setField(field);		
					}
				};
				
				if (virusToFetch.isEmpty())
					callback.handleEvent(null);
				else
					VirusCache.impl.get(virusToFetch, callback);
			}
			public void onFailure(Throwable caught) {
				onSuccess(null);
			}
		});
	}
	
	@Override
	protected ClassificationSchemeViewer createViewer(String description, TreeData displayData) {
		return new ThreatsClassificationSchemeViewer(description, displayData);
	}
	
	@SuppressWarnings("unchecked")
	protected ClassificationSchemeModelData createModelData(DisplayStructure structure, Field field) {
		return new ThreatClassificationSchemeModelData(structure, field);
	}

}
