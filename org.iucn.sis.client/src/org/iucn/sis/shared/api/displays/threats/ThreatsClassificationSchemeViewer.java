package org.iucn.sis.shared.api.displays.threats;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.iucn.sis.client.api.caches.TaxonomyCache;
import org.iucn.sis.shared.api.data.TreeData;
import org.iucn.sis.shared.api.data.TreeDataRow;
import org.iucn.sis.shared.api.debug.Debug;
import org.iucn.sis.shared.api.models.Field;
import org.iucn.sis.shared.api.models.Taxon;
import org.iucn.sis.shared.api.models.fields.IASTaxaThreatsSubfield;
import org.iucn.sis.shared.api.schemes.BasicClassificationSchemeViewer;
import org.iucn.sis.shared.api.schemes.ClassificationSchemeModelData;
import org.iucn.sis.shared.api.structures.Structure;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.DataList;
import com.extjs.gxt.ui.client.widget.DataListItem;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.util.extjs.client.WindowUtils;
import com.solertium.util.gwt.ui.DrawsLazily;
import com.solertium.util.gwt.ui.DrawsLazily.DoneDrawingCallbackWithParam;

public class ThreatsClassificationSchemeViewer extends
		BasicClassificationSchemeViewer {

	public ThreatsClassificationSchemeViewer(String description, TreeData treeData) {
		super(description, treeData);
	}
	
	@Override
	protected ThreatClassificationSchemeModelData newInstance(Structure structure) {
		return new ThreatClassificationSchemeModelData(structure);
	}
	
	@Override
	protected void updateInnerContainer(final ClassificationSchemeModelData model,
			final boolean addToPagingLoader, final boolean isViewOnly,
			final DoneDrawingCallbackWithParam<LayoutContainer> callback) {
		if (model.getSelectedRow() != null)
			Debug.println("Updating container for {0}", model.getSelectedRow().getDescription());
		if (model.getSelectedRow() != null && "Named taxa".equals(model.getSelectedRow().getDescription())) {
			final Map<ClassificationSchemeModelData, Integer> map = 
				new HashMap<ClassificationSchemeModelData, Integer>();
			map.put(model, null);
			for (ClassificationSchemeModelData current : server.getModels()) {
				if (current.getSelectedRow().getDisplayId().equals(model.getSelectedRow().getDisplayId())) {
					IASTaxaThreatsSubfield field = new IASTaxaThreatsSubfield(current.getField());
					map.put(current, field.getIASTaxa());
				}
			}
			
			TaxonomyCache.impl.fetchList(new ArrayList<Integer>(map.values()), new GenericCallback<String>() {
				public void onSuccess(String result) {
					ThreatsClassificationSchemeViewer.super.updateInnerContainer(model, addToPagingLoader, isViewOnly, new DrawsLazily.DoneDrawingCallbackWithParam<LayoutContainer>() {
						public void isDrawn(LayoutContainer parameter) {
							final DataList list = new DataList();
							for (Map.Entry<ClassificationSchemeModelData, Integer> entry : map.entrySet()) {
								DataListItem item = new DataListItem();
								if (entry.getValue() == null)
									item.setText("(No taxon selected)");
								else {
									Taxon taxon = TaxonomyCache.impl.getTaxon(entry.getValue());
									if (taxon != null)
										item.setText(taxon.getFullName());
									else //Taxon specified doesnt exist.
										continue;
								}
								item.setData("taxon", entry.getValue());
								item.setData("value", entry.getKey());
								
								IASTaxaThreatsSubfield field = new IASTaxaThreatsSubfield(entry.getKey().getField());
								field.setIASTaxa(entry.getValue());
								
								list.add(item);
							}
							
							final ButtonBar bar = new ButtonBar();
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
											final Map<Integer, Taxon> map = new HashMap<Integer, Taxon>();
											for (Taxon taxon : locator.getSelection())
												map.put(taxon.getId(), taxon);
											
											for (DataListItem item : list.getItems()) {
												Integer value = item.getData("taxon");
												map.remove(value);
											}
											
											for (Map.Entry<Integer, Taxon> entry : map.entrySet()) {
												DataListItem item = new DataListItem();
												item.setText(entry.getValue().getFullName());
												item.setData("taxon", entry.getKey());
												
												ClassificationSchemeModelData model = 
													newInstance(generateNamedTaxaStructure());
												model.setField(new Field("ThreatsSubfield", null));
												
												IASTaxaThreatsSubfield field = new IASTaxaThreatsSubfield(model.getField());
												field.setIASTaxa(entry.getKey());
												
												list.add(item);				
											}
											window.hide();
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
									WindowUtils.confirmAlert("Confirm", "Are you sure you want to remove this row?", new WindowUtils.SimpleMessageBoxListener() {
										public void onYes() {
											//TODO: delete row
										}
									});
								}
							}));
							
							final LayoutContainer left = new LayoutContainer(new BorderLayout());
							left.add(list, new BorderLayoutData(LayoutRegion.CENTER));
							left.add(bar,new BorderLayoutData(LayoutRegion.SOUTH, 25, 25, 25));
							
							final LayoutContainer container = new LayoutContainer(new BorderLayout());
							container.add(left, new BorderLayoutData(LayoutRegion.WEST, 150));
							container.add(parameter, new BorderLayoutData(LayoutRegion.CENTER));
							
							callback.isDrawn(container);
						}
					});
				}
				
				@Override
				public void onFailure(Throwable caught) {
					onSuccess(null);
				}
			});
		}
		else
			super.updateInnerContainer(model, addToPagingLoader, isViewOnly, callback);
	}
	
	private void populateListWithTaxon(DataList list, Collection<Taxon> taxa) {
		
	}
	
	@Override
	protected Structure generateDefaultStructure() {
		ThreatsTreeData treeData = (ThreatsTreeData) this.treeData;
		
		BasicThreatViewer structure = new BasicThreatViewer(treeData);
		structure.setIsVisible(treeData.getIsVisible());
		structure.setName(treeData.getName());
		
		return structure;
	}
	
	private Structure generateNamedTaxaStructure() {
		ThreatsTreeData treeData = (ThreatsTreeData) this.treeData;
		
		BasicThreatViewer structure = new BasicThreatViewer(treeData);
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
					final ClassificationSchemeModelData model;
					if ("Named taxa".equals(selection.getRow().getDescription()))
						model = newInstance(generateNamedTaxaStructure());
					else
						model = newInstance(generateDefaultStructure());
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

}
