package org.iucn.sis.client.acl.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.iucn.sis.shared.BaseAssessment;
import org.iucn.sis.shared.acl.base.PermissionGroup;
import org.iucn.sis.shared.acl.base.PermissionResource;
import org.iucn.sis.shared.acl.feature.AuthorizableFeature;
import org.iucn.sis.shared.data.WorkingSetCache;
import org.iucn.sis.shared.data.WorkingSetData;
import org.iucn.sis.shared.data.assessments.Region;
import org.iucn.sis.shared.data.assessments.RegionCache;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.SortDir;
import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.StoreSorter;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.util.extjs.client.WindowUtils;

public class PermissionResourceUI extends LayoutContainer {

	private class ResourceModelData extends BaseModelData {
		public ResourceModelData(String name, String value) {
			set("name", name);
			set("value", value);
		}
	}

	private PermissionGroup owner;
	private PermissionResource resource;
	private PermissionSetUI permSetUI;

	private HorizontalPanel listPanel;

	private HashMap<String, ComboBox<ResourceModelData>> listboxLookup;
	private ComboBox<ResourceModelData> first;

	private Button edit;
	private Button delete;
	
	private GenericCallback<String> deleteHandler;
	private GenericCallback<String> saveHandler;

	public PermissionResourceUI(PermissionGroup owner, PermissionResource resource, 
			GenericCallback<String> deleteHandler, GenericCallback<String> saveHandler) {
		this.owner = owner;
		this.resource = resource;
		this.deleteHandler = deleteHandler;
		this.saveHandler = saveHandler;

		permSetUI = new PermissionSetUI(resource.getSet());
		listPanel = new HorizontalPanel();
		listPanel.setSpacing(3);

		listboxLookup = new HashMap<String, ComboBox<ResourceModelData>>();
		buildButtons();

		draw();
	}

	private void buildButtons() {
		edit = new Button();
		edit.setIconStyle("icon-pencil");
		edit.addSelectionListener(new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				showEditingWindow();
			}
		});

		delete = new Button();
		delete.setIconStyle("icon-remove");
		delete.addSelectionListener(new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				deleteHandler.onSuccess(resource.getURI());
			}
		});
	}

	public void draw() {
		listPanel.removeAll();
		listPanel.add(edit);
		listPanel.add(delete);
		listPanel.add(new Html("<b>" + resource.getURI() + (resource.getAttributes().size() != 0 ? 
				" - " + resource.getAttributes() : "") + "</b>" ));

		add(listPanel);
		add(permSetUI);
	}

	private ComboBox<ResourceModelData> getComboBox() {
		ComboBox<ResourceModelData> box = new ComboBox<ResourceModelData>();
		box.setDisplayField("name");
		box.setEditable(false);
		box.setForceSelection(true);

		ListStore<ResourceModelData> store = new ListStore<ResourceModelData>();
		store.setStoreSorter(new StoreSorter<ResourceModelData>());
		box.setStore(store);

		return box;
	}

	private HorizontalPanel buildLists() {
		final HorizontalPanel panel = new HorizontalPanel();

		first = getComboBox();
		first.getStore().add(new ResourceModelData("Feature", "feature"));
		first.getStore().add(new ResourceModelData("Resource", "resource"));
		first.getStore().sort("name", SortDir.ASC);
		first.addSelectionChangedListener(new SelectionChangedListener<ResourceModelData>() {
			public void selectionChanged(SelectionChangedEvent<ResourceModelData> se) {
				String value = se.getSelectedItem().get("value");
				if( !value.equals("") ) {
					panel.removeAll();
					panel.add(first);
					if( listboxLookup.containsKey(value))
						panel.add(listboxLookup.get(value));
					else
						System.out.println("Couldn't find listbox for value " + value);
					panel.layout();
				}
			}
		});
		panel.add(first);

		final ComboBox<ResourceModelData> feature = getComboBox();
		feature.getStore().add(new ResourceModelData("", ""));
		for( String featureName : AuthorizableFeature.featureNames )
			feature.getStore().add(new ResourceModelData(featureName, featureName));
		feature.getStore().sort("name", SortDir.ASC);
		listboxLookup.put("feature", feature);

		final ComboBox<ResourceModelData> resources = getComboBox();
		resources.getStore().add(new ResourceModelData("", ""));
		resources.getStore().add(new ResourceModelData("Assessment", "assessment"));
		resources.getStore().add(new ResourceModelData("Taxon", "taxon"));
		resources.getStore().add(new ResourceModelData("Reference", "reference"));
		resources.getStore().add(new ResourceModelData("Working Set", "workingSet"));
		resources.addSelectionChangedListener(new SelectionChangedListener<ResourceModelData>() {
			public void selectionChanged(SelectionChangedEvent<ResourceModelData> se) {
				String value = se.getSelectedItem().get("value");
				if( !value.equals("") ) {
					panel.removeAll();
					panel.add(first);
					panel.add(resources);
					if( listboxLookup.containsKey(value))
						panel.add(listboxLookup.get(value));
					else
						System.out.println("Couldn't find listbox for value " + value);

					if( resources.getStore().indexOf(se.getSelectedItem()) == 1 ) {
						panel.add(listboxLookup.get("region"));
					}

					panel.layout();
				}
			}
		});
		resources.getStore().sort("name", SortDir.ASC);
		listboxLookup.put("resource", resources);

		final ComboBox<ResourceModelData> assessment = getComboBox();
		assessment.getStore().add(new ResourceModelData("", ""));
		assessment.getStore().add(new ResourceModelData("Draft", BaseAssessment.DRAFT_ASSESSMENT_STATUS));
		assessment.getStore().add(new ResourceModelData("Published", BaseAssessment.PUBLISHED_ASSESSMENT_STATUS));
		assessment.getStore().sort("name", SortDir.ASC);
		listboxLookup.put("assessment", assessment);

		final ComboBox<ResourceModelData> region = getComboBox();
		region.getStore().add(new ResourceModelData("Regional/Global", ""));
		region.getStore().add(new ResourceModelData("Global", "global"));
		region.getStore().add(new ResourceModelData("Any Region", "(\\d+,?)+"));
		for( Region curRegion : RegionCache.impl.getRegions() )
			region.getStore().add(new ResourceModelData(curRegion.getRegionName(), curRegion.getId()));
		region.getStore().sort("name", SortDir.ASC);
		region.setData("isAttribute", "region");
		listboxLookup.put("region", region);

		//		final ComboBox<ResourceModelData> taxon = getComboBox();
		//		assessment.addItem("", "");
		//		assessment.addItem("Common Name", "commonName");
		//		assessment.addItem("Synonyms", "synonym");
		//		listboxLookup.put("taxon", taxon);

		final ComboBox<ResourceModelData> workingSet = getComboBox();
		workingSet.getStore().add(new ResourceModelData("", ""));
		for( WorkingSetData ws : WorkingSetCache.impl.getSubscribable() )
			workingSet.getStore().add(new ResourceModelData(ws.getWorkingSetName(), ws.getId()));
		workingSet.getStore().sort("name", SortDir.ASC);
		listboxLookup.put("workingSet", workingSet);

		setProperValues();

		return panel;
	}

	private void setProperValues() {
		if( !resource.getURI().equals("new") ) {
			String [] settings = resource.getURI().split("/");
			ComboBox<ResourceModelData> curBox = first;
			for( int i = 0; i < settings.length; i++ ) {
				for( ResourceModelData cur : curBox.getStore().getModels() ) {
					if( settings[i].equals(cur.get("value") ) ) {
						List<ResourceModelData> data = new ArrayList<ResourceModelData>();
						data.add(cur);
						curBox.setSelection(data);

						curBox = listboxLookup.get(settings[i]);
						break; //Break out of inner loop
					}
				}
			}

			if( resource.getAttributes().size() > 0 ) {
				for( Entry<String, String> cur : resource.getAttributes().entrySet() ) { //For each attr
					if( listboxLookup.containsKey(cur.getKey()) ) { //If there's a listbox, get it
						curBox = listboxLookup.get(cur.getKey());
						for( ResourceModelData curModel : curBox.getStore().getModels() ) {
							if( cur.getValue().equals(curModel.get("value") ) ) {
								List<ResourceModelData> data = new ArrayList<ResourceModelData>();
								data.add(curModel);
								curBox.setSelection(data);
								break; //Break out of inner loop
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Sinks changes from the Permission Set UI into the Permission Set object, so saving can occur.
	 */
	public void sinkPermissionSetUIData() {
		resource.setSet(permSetUI.getSet());
	}
	
	public void showEditingWindow() {
		final Window w = WindowUtils.getWindow(true, true, "Editing Resource");
		w.setLayout(new FlowLayout());

		final HorizontalPanel listPanel = buildLists();
		listPanel.setSize(680, 80);

		w.getButtonBar().add(new Button("Done", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				StringBuilder uri = new StringBuilder();
				HashMap<String, String> attrs = new HashMap<String, String>();

				for( Component cur : listPanel.getItems() ) {
					ComboBox<ResourceModelData> box = (ComboBox<ResourceModelData>)cur;
					if( box.getSelection().size() > 0 ) {
						if( box.getData("isAttribute") == null ) {
							uri.append( box.getSelection().get(0).get("value") );
							uri.append( "/" );
						} else {
							ResourceModelData data = box.getSelection().get(0);
							attrs.put((String)box.getData("isAttribute"), (String)data.get("value"));
						}
					}
				}

				owner.removeResource(resource.getURI());
				resource.setURI(uri.length() > 0 ? uri.substring(0, uri.length()-1) : "");
				resource.setAttributes(attrs);
				owner.addPermissionResource(resource);
				
				saveHandler.onSuccess(resource.getURI());
			}
		}));

		w.getButtonBar().add(new Button("Cancel", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				w.close();
			}
		}));
//		w.setAlignment(HorizontalAlignment.LEFT);
w.add(listPanel);
		w.show();
		w.setSize(700, 100);
		w.center();
	}
}
