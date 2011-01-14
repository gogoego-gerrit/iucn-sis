package org.iucn.sis.shared.api.displays.threats;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.iucn.sis.client.api.caches.TaxonomyCache;
import org.iucn.sis.shared.api.data.TreeData;
import org.iucn.sis.shared.api.data.TreeDataRow;
import org.iucn.sis.shared.api.models.Field;
import org.iucn.sis.shared.api.models.Taxon;
import org.iucn.sis.shared.api.models.fields.IASTaxaThreatsSubfield;
import org.iucn.sis.shared.api.schemes.ClassificationSchemeModelData;
import org.iucn.sis.shared.api.structures.Structure;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.DataListItem;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.util.events.SimpleListener;
import com.solertium.util.extjs.client.WindowUtils;

@SuppressWarnings("deprecation")
public class IASThreatRowEditor extends GroupedThreatRowEditor {
	
	public IASThreatRowEditor(final Collection<ClassificationSchemeModelData> models, final TreeData treeData, final TreeDataRow groupBy, boolean isViewOnly) {
		super(models, treeData, groupBy, isViewOnly);
	}
	
	@Override
	protected void init(SimpleListener listener) {
		final ArrayList<Integer> initTaxa = new ArrayList<Integer>();
		for (ClassificationSchemeModelData model : models) {
			IASTaxaThreatsSubfield field = new IASTaxaThreatsSubfield(model.getField());
			Integer value = field.getIASTaxa();
			if (value != null)
				initTaxa.add(value);
		}
		
		fetchList(initTaxa, listener);
	}
	
	@Override
	protected DataListItem createDataListItem(ClassificationSchemeModelData model) {
		IASTaxaThreatsSubfield field = new IASTaxaThreatsSubfield(model.getField());
		Integer taxonID = field.getIASTaxa();
		
		DataListItem item = new DataListItem();
		if (taxonID == null)
			item.setText("(No taxon selected)");
		else {
			Taxon taxon = TaxonomyCache.impl.getTaxon(taxonID);
			if (taxon != null)
				item.setText(taxon.getFullName());
			else //Taxon specified doesnt exist.
				return null;
		}
		item.setData("taxon", taxonID);
		
		return item;
	}
	
	protected ButtonBar createButtonBar() {
		final ButtonBar bar = new ButtonBar();
		bar.setAlignment(HorizontalAlignment.CENTER);
		bar.add(new Button("Add Taxa", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				final ThreatTaggedSpeciesLocator locator = 
					new ThreatTaggedSpeciesLocator();
				final Window window = new Window();
				window.setHeading("Taxonomy Finder");
				window.setButtonAlign(HorizontalAlignment.CENTER);
				window.setSize(600, 600);
				window.setLayout(new FitLayout());
				window.addButton(new Button("Add Selected", new SelectionListener<ButtonEvent>() {
					public void componentSelected(ButtonEvent ce) {
						window.hide();
						
						final Map<Integer, Taxon> map = new HashMap<Integer, Taxon>();
						for (Taxon taxon : locator.getSelection())
							map.put(taxon.getId(), taxon);
						
						for (DataListItem item : list.getItems()) {
							Integer value = item.getData("taxon");
							map.remove(value);
						}
						
						for (Map.Entry<Integer, Taxon> entry : map.entrySet()) {
							Field field = new Field("ThreatsSubfield", null);
							IASTaxaThreatsSubfield proxy = new IASTaxaThreatsSubfield(field);
							proxy.setIASTaxa(entry.getKey());
							
							ClassificationSchemeModelData model = 
								new ThreatClassificationSchemeModelData(generateNamedTaxaStructure(), field);
							model.setSelectedRow(groupBy);
							
							DataListItem item = new DataListItem();
							item.setText(entry.getValue().getFullName());
							item.setData("taxon", entry.getKey());
							item.setData("value", model);
							
							list.add(item);
							
							if (addListener != null)
								addListener.handleEvent(model);
						}
						
					}
				}));
				window.addButton(new Button("Cancel", new SelectionListener<ButtonEvent>() {
					public void componentSelected(ButtonEvent ce) {
						window.hide();
					}
				}));
				window.add(locator);
				window.show();
			}
		}));
		bar.add(new Button("Remove Taxon", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				if (list.getSelectedItem() == null)
					return;
				
				WindowUtils.confirmAlert("Confirm", "Are you sure you want to remove this row?", new WindowUtils.SimpleMessageBoxListener() {
					public void onYes() {
						removeSelectedModel();
					}
				});
			}
		}));
		
		return bar;
	}
	
	@SuppressWarnings("unchecked")
	private Structure generateNamedTaxaStructure() {
		ThreatsTreeData treeData = (ThreatsTreeData) this.treeData;
		
		FeralTaxonThreatViewer structure = new FeralTaxonThreatViewer(treeData);
		structure.setIsVisible(treeData.getIsVisible());
		structure.setName(treeData.getName());
		
		return structure;
	}

	private void fetchList(ArrayList<Integer> taxa, final SimpleListener callback) {
		if (taxa.isEmpty())
			callback.handleEvent();
		else {
			TaxonomyCache.impl.fetchList(taxa, new GenericCallback<String>() {
				public void onSuccess(String result) {
					callback.handleEvent();
				}
				public void onFailure(Throwable caught) {
					WindowUtils.errorAlert("Could not load taxa, please check your connection.");
				}
			});
		}
	}
	
}
