package org.iucn.sis.shared.api.displays.threats;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.iucn.sis.client.api.caches.TaxonomyCache;
import org.iucn.sis.shared.api.data.TreeData;
import org.iucn.sis.shared.api.data.TreeDataRow;
import org.iucn.sis.shared.api.debug.Debug;
import org.iucn.sis.shared.api.models.Taxon;
import org.iucn.sis.shared.api.models.fields.IASTaxaThreatsSubfield;
import org.iucn.sis.shared.api.schemes.BasicClassificationSchemeViewer;
import org.iucn.sis.shared.api.schemes.ClassificationSchemeModelData;
import org.iucn.sis.shared.api.schemes.ClassificationSchemeRowEditorWindow;
import org.iucn.sis.shared.api.schemes.ClassificationSchemeRowEditorWindow.EditMode;
import org.iucn.sis.shared.api.structures.Structure;

import com.solertium.lwxml.shared.GenericCallback;

public class ThreatsClassificationSchemeViewer extends
		BasicClassificationSchemeViewer {

	public ThreatsClassificationSchemeViewer(String description, TreeData treeData) {
		super(description, treeData);
	}
	
	@SuppressWarnings("unchecked")
	public ThreatClassificationSchemeModelData newInstance(Structure structure) {
		return new ThreatClassificationSchemeModelData(structure);
	}
	
	@Override
	public ClassificationSchemeRowEditorWindow createRowEditorWindow(
			ClassificationSchemeModelData model, boolean addToPagingLoader,
			boolean isViewOnly) {
		return new GroupedThreatRowEditorWindow(this, treeData, description, model, addToPagingLoader ? EditMode.NEW : EditMode.EXISTING, isViewOnly);
	}
	
	public boolean containsRow(TreeDataRow row) {
		return containsRow(row, true);
	}
	
	private boolean containsRow(TreeDataRow row, boolean allowDuplicates) {
		return super.containsRow(row) || (allowDuplicates && 
			("8.5.2".equals(row.getRowNumber()) || ThreatViewerFactory.hasTaxa(row)));
	}
	
	protected Collection<String> getDisabledTreeDataRows() {
		final ArrayList<String> list = new ArrayList<String>();
		list.add("8.1.2");
		list.add("8.2.2");
		list.add("8.4.2");
		list.add("8.5.2");
		return list;
	}
	
	@Override
	protected void bulkAdd(Set<TreeDataRow> rows) {
		final List<ClassificationSchemeModelData> models = 
			new ArrayList<ClassificationSchemeModelData>();
		for (TreeDataRow row : rows) {
			if (!containsRow(row, false)) {
				ClassificationSchemeModelData model = 
					newInstance(generateDefaultStructure(row));
				model.setSelectedRow(row);
			
				models.add(model);
			}
		}
		
		if (hasChanged = !models.isEmpty())
			server.add(models);
	}
	
	@SuppressWarnings("unchecked")
	public Structure generateDefaultStructure(TreeDataRow row) {
		ThreatsTreeData treeData = (ThreatsTreeData) this.treeData;
		
		BasicThreatViewer structure;
		if (row != null)
			structure = ThreatViewerFactory.generateStructure(treeData, row);
		else
			structure = new BasicThreatViewer(treeData);
		structure.setIsVisible(treeData.getIsVisible());
		structure.setName(treeData.getName());
		
		return structure;
	}
	
	@Override
	public List<ClassificationSchemeModelData> save(boolean deep) {
		List<ClassificationSchemeModelData> saved = super.save(deep);
		if (!deep)
			return saved;
		
		final List<Taxon> taxa = new ArrayList<Taxon>();
		for (ClassificationSchemeModelData model : saved) {
			if (ThreatViewerFactory.hasTaxa(model.getSelectedRow())) {
				IASTaxaThreatsSubfield field = new IASTaxaThreatsSubfield(model.getField());
				Integer value = field.getIASTaxa();
				if (value != null) {
					Taxon taxon = TaxonomyCache.impl.getTaxon(value);
					if (taxon != null)
						taxa.add(taxon);
				}
			}
		}
		
		if (!taxa.isEmpty()) {
			TaxonomyCache.impl.tagTaxa("invasive", taxa, new GenericCallback<Object>() {
				public void onFailure(Throwable caught) {
					Debug.println("Failed to tag taxa as invasive.");
				}
				public void onSuccess(Object result) {
					Debug.println("Tagged {0} taxa as invasive.", taxa.size());
				}
			});
		}
		
		return saved;
	}

}
