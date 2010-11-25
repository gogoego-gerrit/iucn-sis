package org.iucn.sis.shared.api.displays.threats;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.iucn.sis.client.api.caches.TaxonomyCache;
import org.iucn.sis.shared.api.data.TreeData;
import org.iucn.sis.shared.api.data.TreeDataRow;
import org.iucn.sis.shared.api.debug.Debug;
import org.iucn.sis.shared.api.models.Taxon;
import org.iucn.sis.shared.api.models.fields.IASTaxaThreatsSubfield;
import org.iucn.sis.shared.api.schemes.BasicClassificationSchemeViewer;
import org.iucn.sis.shared.api.schemes.ClassificationSchemeModelData;
import org.iucn.sis.shared.api.schemes.ClassificationSchemeRowEditor;
import org.iucn.sis.shared.api.structures.Structure;

import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.util.events.ComplexListener;
import com.solertium.util.gwt.ui.DrawsLazily;

public class ThreatsClassificationSchemeViewer extends
		BasicClassificationSchemeViewer {
	
	private LayoutContainer threatContainer;

	public ThreatsClassificationSchemeViewer(String description, TreeData treeData) {
		super(description, treeData);
		threatContainer = new LayoutContainer(new FillLayout());
		threatContainer.setLayoutOnChange(true);
	}
	
	@Override
	protected ThreatClassificationSchemeModelData newInstance(Structure structure) {
		return new ThreatClassificationSchemeModelData(structure);
	}
	
	/**
	 * Overriden to provide a GroupedRowEditor when necessary, 
	 * only for the Named Taxa sections -- 8.5.2 is a special 
	 * case that is grouped, but for viruses instead of taxa. 
	 * The rest of the Named Taxa sections are, indeed, taxa, 
	 * and can use the IAS grouping.
	 * 
	 * All others are singular, not grouped, and can have the 
	 * standard row editor, with the threat viewer used being 
	 * determined by the row number.
	 */
	protected ClassificationSchemeRowEditor createRowEditor(ClassificationSchemeModelData model, boolean isViewOnly) {
		if (model.getSelectedRow() == null)
			return super.createRowEditor(model, isViewOnly); 
		
		if ("8.5.2".equals(model.getSelectedRow().getRowNumber())) {
			final Collection<ClassificationSchemeModelData> models = 
				new ArrayList<ClassificationSchemeModelData>();
			for (ClassificationSchemeModelData current : server.getModels())
				if (current.getSelectedRow().getDisplayId().equals(model.getSelectedRow().getDisplayId()))
					models.add(current);
			
			ViralThreatRowEditor editor = new ViralThreatRowEditor(models, treeData, isViewOnly);
			editor.setRemoveListener(new ComplexListener<ClassificationSchemeModelData>() {
				public void handleEvent(ClassificationSchemeModelData model) {
					server.remove(model);
					
					hasChanged = true;
				}
			});
			
			return editor;
		}
		else if ("Named taxa".equals(model.getSelectedRow().getDescription())) {
			final Collection<ClassificationSchemeModelData> models = 
				new ArrayList<ClassificationSchemeModelData>();
			for (ClassificationSchemeModelData current : server.getModels())
				if (current.getSelectedRow().getDisplayId().equals(model.getSelectedRow().getDisplayId()))
					models.add(current);
			
			IASThreatRowEditor editor = new IASThreatRowEditor(models, treeData, isViewOnly);
			editor.setRemoveListener(new ComplexListener<ClassificationSchemeModelData>() {
				public void handleEvent(ClassificationSchemeModelData model) {
					server.remove(model);
					
					hasChanged = true;
				}
			});
			
			return editor;
		}
		else
			return super.createRowEditor(model, isViewOnly);
	}
	
	@Override
	protected Structure generateDefaultStructure() {
		return generateDefaultStructure(null);
	}
	
	protected Structure generateDefaultStructure(TreeDataRow row) {
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
	protected ComboBox<CodingOption> createClassificationOptions(TreeDataRow selected) {
		ComboBox<CodingOption> box = super.createClassificationOptions(selected);
		box.addSelectionChangedListener(new SelectionChangedListener<CodingOption>() {
			public void selectionChanged(SelectionChangedEvent<CodingOption> se) {
				//TODO: create the right structure based on the selection...
				CodingOption selection = se.getSelectedItem();
				if (selection != null) {
					final ClassificationSchemeModelData model = 
						newInstance(generateDefaultStructure(selection.getRow()));
					model.setSelectedRow(selection.getRow());
				
					updateInnerContainer(model, true, false, new DrawsLazily.DoneDrawingCallbackWithParam<LayoutContainer>() {
						public void isDrawn(LayoutContainer parameter) {
							innerContainer.removeAll();
							innerContainer.add(parameter);
						}
					});
				}
			}
		});
		return box;
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
