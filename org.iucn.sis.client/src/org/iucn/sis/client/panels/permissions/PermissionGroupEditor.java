package org.iucn.sis.client.panels.permissions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

import org.iucn.sis.client.api.caches.AuthorizationCache;
import org.iucn.sis.shared.api.acl.feature.AuthorizableFeature;
import org.iucn.sis.shared.api.models.Permission;
import org.iucn.sis.shared.api.models.PermissionGroup;
import org.iucn.sis.shared.api.models.Taxon;
import org.iucn.sis.shared.api.models.WorkingSet;
import org.iucn.sis.shared.api.utils.CaseInsensitiveAlphanumericComparator;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.Style.SortDir;
import com.extjs.gxt.ui.client.Style.VerticalAlignment;
import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.StoreSorter;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.VerticalPanel;
import com.extjs.gxt.ui.client.widget.WindowManager;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.form.Validator;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.ListBox;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.util.events.ComplexListener;
import com.solertium.util.extjs.client.CardLayoutContainer;
import com.solertium.util.extjs.client.WindowUtils;

public class PermissionGroupEditor extends LayoutContainer {
	
	private class PermissionGroupData extends BaseModelData {
		
		private static final long serialVersionUID = 1066219266682189684L;
		
		private PermissionGroup group;
		
		public PermissionGroupData(PermissionGroup baseGroup) {
			setGroup(baseGroup);			
		}
		
		public PermissionGroup getGroup() {
			return group;
		}
		
		public void setGroup(PermissionGroup group) {
			this.group = group;
			set("name", group.getName());
		}
	}
	
	private SimpleComboBox<String> features;
	
	private ComboBox<PermissionGroupData> groupSelector;
	private ListStore<PermissionGroupData> groupStore;
	private TextField<String> groupName;
	
	private Button newGroup;
	private Button saveChanges;
	private Button revertChanges;
	private Button delete;
	
	private PermissionGroup group;
	private List<PermissionResourceUI> resourceUIs;
	
	private PermissionSetUI defaultPermissionSet;
	private PermissionScopeTaxonomyBrowser scopeBrowser;
	private TextField<String> scope;
	private ListBox scopeChoices;
	
	private PermissionInheritenceUI inheritenceUI;
	private PermissionWorkingSetList workingSetList;
	
	private HorizontalPanel groupSelectorSection;
	private HorizontalPanel groupNameSection;
	private HorizontalPanel defaultSection;
	private VerticalPanel inheritsSection;
	private HorizontalPanel scopeSection;
	private LayoutContainer resourcesSection;
	
	private LayoutContainer blankAccessoryPanel;
	
	private CardLayoutContainer accessoryPanel;
	private VerticalPanel leftPanel;
	
	public PermissionGroupEditor() {
		this(null);
	}
	
	public PermissionGroupEditor(PermissionGroup baseGroup) {
		groupStore = new ListStore<PermissionGroupData>();
		groupStore.setStoreSorter(new StoreSorter<PermissionGroupData>(new CaseInsensitiveAlphanumericComparator()));
		
		groupSelector = new ComboBox<PermissionGroupData>();
		groupSelector.setTriggerAction(TriggerAction.ALL);
		groupSelector.setForceSelection(true);
		groupSelector.setStore(groupStore);
		groupSelector.addListener(Events.BeforeSelect, new Listener<BaseEvent>() {
			public void handleEvent(BaseEvent be) {
				if( !groupName.validate() ) {
					WindowUtils.errorAlert("Please finish creation/editing of selected group.");
					be.setCancelled(true);
				}
			};
		});
		groupSelector.addSelectionChangedListener(new SelectionChangedListener<PermissionGroupData>() {
			@Override
			public void selectionChanged(SelectionChangedEvent<PermissionGroupData> se) {
				if( groupSelector.validate() ) {
					group = se.getSelectedItem().getGroup();				
					updateContent();
				} else {
					WindowUtils.errorAlert("Please finish creation/editing of selected group.");
					se.setCancelled(true);
				}
			}
		});
		groupSelector.setEditable(false);
		groupSelector.setDisplayField("name");
		
		groupName = new TextField<String>();
		groupName.setValidator(new Validator() {
			public String validate(Field<?> field, String value) {
				if( !value.matches("\\w+") )
					return "Only alpha characters are allowed in the Group name.";
				
				return null;
			}
		});
		
		newGroup = new Button();
		newGroup.setIconStyle("icon-new-bookmark");
		newGroup.setToolTip("Create New Permission Group");
		newGroup.addSelectionListener(new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				if( groupSelector.validate() ) {
					group = new PermissionGroup("(New Group)");
					PermissionGroupData data = new PermissionGroupData(group);
					groupStore.add(data);
					groupSelector.setValue(data);
					updateContent();
				} else
					WindowUtils.errorAlert("Please finish creation/editing of selected group.");
			}
		});
		
		saveChanges = new Button();
		saveChanges.setIconStyle("icon-save");
		saveChanges.setToolTip("Save Changes to Current Group");
		saveChanges.addSelectionListener(new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				final PermissionGroupData cur = groupSelector.getSelection().get(0);
				String selectedName = cur.getGroup().getName();
				//If it isn't new...
				if( AuthorizationCache.impl.hasGroup( selectedName ) ) {
					// Already saved group
					saveDataToGroup(group);
					
					if( selectedName.equals(group.getName())) {
						// Update Group
						if(group.getId() == 0)
							setGroupIDFromCache(group.getName());
						
						AuthorizationCache.impl.updateGroup(group, new GenericCallback<PermissionGroup>() {
							public void onSuccess(PermissionGroup result) {
								WindowUtils.infoAlert("Save Successful", "Permission Edits Successfully Saved");
									
								groupStore.remove(cur);
								cur.setGroup(group);
								groupStore.add(cur);
								groupSelector.setSelection(wrapInArray(cur));
								updateContent();
							}
							public void onFailure(Throwable caught) {
								WindowUtils.errorAlert("Save Failed", "Permission Edits Not Saved Successfully.");
							}
						});
					} else {
						// Create New Group with different name
						if(AuthorizationCache.impl.hasGroup( group.getName() ) ){
							WindowUtils.errorAlert("Save Failed", "Permission Group with the same name already exists.");
							groupSelector.setSelection(wrapInArray(cur));
						}else{
							AuthorizationCache.impl.saveGroup(group, new GenericCallback<String>() {
								public void onSuccess(String result) {
									WindowUtils.infoAlert("Save Successful", "Permission Edits Successfully Saved.");
									
									groupStore.remove(cur);
									cur.setGroup(group);
									groupStore.add(cur);
									groupSelector.setSelection(wrapInArray(cur));
									updateContent();
								}
								public void onFailure(Throwable caught) {
									WindowUtils.errorAlert("Save Failed", "Permission Edits Not Saved Successfully.");
								}
							});
						}
					}
				} else {
					if(AuthorizationCache.impl.hasGroup( groupName.getValue().trim() ) ){
						WindowUtils.errorAlert("Save Failed", "Permission Group with the same name already exists");
						groupSelector.setSelection(wrapInArray(cur));
					}else{
						saveDataToGroup(group);
						AuthorizationCache.impl.saveGroup(group, new GenericCallback<String>() {
							public void onSuccess(String result) {
								WindowUtils.infoAlert("Save Successful", "Permission Edits Successfully Saved");
								
								groupStore.remove(cur);
								cur.setGroup(group);
								groupStore.add(cur);
								groupSelector.setSelection(wrapInArray(cur));
								updateContent();
							}
							public void onFailure(Throwable caught) {
								WindowUtils.errorAlert("Save Failed", "Permission Edits Not Saved Successfully.");
							}
						});
					}
				}
			}
		});
		
		revertChanges = new Button();
		revertChanges.setIconStyle("icon-undo");
		revertChanges.setToolTip("Revert Unsaved Changes to Current Group");
		revertChanges.addSelectionListener(new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				PermissionGroupData cur = groupSelector.getSelection().get(0);
				if( AuthorizationCache.impl.hasGroup( cur.getGroup().getName() ) ) {
					cur.setGroup(AuthorizationCache.impl.getGroup( cur.getGroup().getName() ));
					groupStore.update(cur);
					groupName.setValue("");
				} else {
					groupStore.remove(cur);
					groupSelector.setSelection(wrapInArray(groupStore.getAt(0)));
				}
				
				updateContent();
			}
		});
		
		delete = new Button();
		delete.setIconStyle("icon-remove");
		delete.setToolTip("Delete Current Group");
		delete.addSelectionListener(new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				final PermissionGroupData cur = groupSelector.getSelection().get(0);
				WindowUtils.confirmAlert("Delete this Group?", "Are you sure you want to delete this group?", new Listener<MessageBoxEvent>() {
					public void handleEvent(MessageBoxEvent be) {
						if( be.getButtonClicked().getText().equalsIgnoreCase("yes")) {
							AuthorizationCache.impl.removeGroup(cur.getGroup().getName(), new GenericCallback<PermissionGroup>() {
								public void onSuccess(PermissionGroup result) {
									WindowUtils.infoAlert("Save Successful", "Permission Edits Successfully Saved");
									groupStore.remove(cur);
									groupSelector.setSelection(wrapInArray(groupStore.getAt(0)));
								}
								public void onFailure(Throwable caught) {
									WindowUtils.errorAlert("Save Failed", "Permission Edits Not Saved Successfully.");
								}
							});
						}
					};
				});
								
				updateContent();
			}
		});
		
		defaultPermissionSet = new PermissionSetUI();
		
		features = new SimpleComboBox<String>();
		features.add(Arrays.asList(AuthorizableFeature.featureNames));
		
		inheritenceUI = new PermissionInheritenceUI();
		
		scopeBrowser = new PermissionScopeTaxonomyBrowser(new ComplexListener<Taxon>() {
			public void handleEvent(Taxon eventData) {
				updateScope(eventData);
			}
		});
		scope = new TextField<String>();
		scope.setEmptyText("");
		scopeChoices = new ListBox(false);
		scopeChoices.addItem("All Taxonomy");
		scopeChoices.addItem("Taxon");
		scopeChoices.addItem("All Working Sets");
		scopeChoices.addItem("Working Set");
		scopeChoices.addChangeHandler(new ChangeHandler() {
			public void onChange(ChangeEvent event) {
				scopeChoicesChanged();
			}
		});
		
		resourceUIs = new ArrayList<PermissionResourceUI>();
		
		groupSelectorSection = getStyledHorizontalPanel();
		groupNameSection = getStyledHorizontalPanel();
		defaultSection = getStyledHorizontalPanel();
		inheritsSection = getStyledVerticalPanel();
		scopeSection = getStyledHorizontalPanel();
		resourcesSection = new LayoutContainer(new RowLayout(Orientation.VERTICAL));
		resourcesSection.setBorders(true);
		
		blankAccessoryPanel = new LayoutContainer();
		
		groupSelectorSection.add(new Html("Group: "));
		groupSelectorSection.add(groupSelector);
		groupSelectorSection.add(newGroup);
		groupSelectorSection.add(saveChanges);
		groupSelectorSection.add(revertChanges);
		groupSelectorSection.add(new SeparatorToolItem());
		groupSelectorSection.add(delete);
		groupNameSection.add(new Html("Name: "));
		groupNameSection.add(groupName);
		defaultSection.add(new Html("Default: "));
		defaultSection.add(defaultPermissionSet);
		inheritsSection.add(inheritenceUI);
		scopeSection.add(new Html("Scope"));
		scopeSection.add(scopeChoices);
		scopeSection.add(scope);
		resourcesSection.add(new Html("Resources"));
		
		leftPanel = new VerticalPanel();
		accessoryPanel = new CardLayoutContainer();
		
		leftPanel.setBorders(true);
		leftPanel.setSpacing(10);
		
		setLayout(new BorderLayout());
		add(leftPanel, new BorderLayoutData(LayoutRegion.CENTER, .5f, 50, 2000));
		add(accessoryPanel, new BorderLayoutData(LayoutRegion.EAST, .5f, 50, 2000));
		
		leftPanel.add(groupSelectorSection);
		leftPanel.add(groupNameSection);
		leftPanel.add(defaultSection);
		leftPanel.add(inheritsSection);
		leftPanel.add(scopeSection);
		leftPanel.add(resourcesSection);
	
		leftPanel.setScrollMode(Scroll.AUTOY);
		
		//Populate Group Store
		group = baseGroup;
		PermissionGroupData toSelect = null;
		
		for( PermissionGroup entry : AuthorizationCache.impl.listGroups() ) {
			if( !entry.getName().matches("^ws\\d+.*") ) {
				PermissionGroupData groupData = new PermissionGroupData(entry);
				groupStore.add(groupData);

				if( group == null )
					group = entry;
				if( entry.equals(group) )
					toSelect = groupData;
			}
		}
		groupStore.sort("name", SortDir.ASC);		
		groupSelector.setSelection(wrapInArray(toSelect)); //This will invoke updateContent()
	}
	
	private void setGroupIDFromCache(String groupName){
		for( PermissionGroup entry : AuthorizationCache.impl.listGroups() ) {
			if( entry.getName().equals(groupName) ) {
				group.setID(entry.getId());
			}
		}
	}
	
	private void scopeChoicesChanged() {
		String selected = scopeChoices.getItemText( scopeChoices.getSelectedIndex() );
		if( selected.equalsIgnoreCase("Taxon") ) {
			accessoryPanel.switchToComponent(scopeBrowser);
		} else if( selected.equalsIgnoreCase("Working Set") ) {
			switchToWorkingSetList();
		} else {
			clearAccessoryPanel();
		}
	}
	
	/**
	 * Puts data in the PermissionGroup. Returns an error message if processing fails, e.g. 
	 * if there is an invalid value.
	 * 
	 * @param the PermissionGroup to stuff data into
	 * @return null for success, a String message if a failure occurred
	 */
	private String saveDataToGroup(PermissionGroup group) {
		String scopeURI = "";
		defaultPermissionSet.sinkToPermission();
		Permission defaultPermission = defaultPermissionSet.getPermission();

		Object scopeData = scope.getData("scope");
		
		if( scopeChoices.getSelectedIndex() == 1 ) {
			if( scopeData == null )
				return "A taxonomic scope must be chosen if Taxon scope is selected.";
			else if( scopeData instanceof String )
				scopeURI = scopeData.toString();
			else {
				Taxon  scopeTaxon = (Taxon )scopeData;
				scopeURI = "taxon/" + scopeTaxon.getLevel() + "/" + scope.getValue();
			}
		} else if( scopeChoices.getSelectedIndex() == 2 ) {
			scopeURI = "workingSets";
		} else if( scopeChoices.getSelectedIndex() == 3 ) {
			if( scopeData == null )
				return "A working set must be selected if WorkingSet scope is selected.";
			else
				scopeURI = "workingSet/" + scopeData.toString();
		}
		
		//Looks like everything checked out fine. Do all the setting of values.
		group.setName(groupName.getValue().trim());
		group.setScopeURI(scopeURI);
		if( defaultPermission != null ) {
			defaultPermission.setPermissionGroup(group);
			defaultPermission.setUrl(PermissionGroup.DEFAULT_PERMISSION_URI);
			group.addPermission(defaultPermission);
		} else
			group.removePermission(PermissionGroup.DEFAULT_PERMISSION_URI);
		
		group.setParent(null);
		for( BaseModelData cur : inheritenceUI.getUsedStore().getModels() )
			group.setParent(AuthorizationCache.impl.getGroup((String)cur.get("name")));
		
		for( PermissionResourceUI uis : resourceUIs )
			uis.sinkPermissionSetUIData();
		
		return null;
	}
	
	private ArrayList<PermissionGroupData> wrapInArray(PermissionGroupData data) {
		ArrayList<PermissionGroupData> arr = new ArrayList<PermissionGroupData>();
		arr.add(data);
		return arr;
	}
	
	private void updateContent() {
		groupName.setValue(group.getName());
		
		clearAccessoryPanel();
		rebuildResourceSection();
		
		Permission defResource = group.getResourceToPermission().get(PermissionGroup.DEFAULT_PERMISSION_URI);
		if( defResource != null )
			defaultPermissionSet.setPermission(defResource);
		else
			defaultPermissionSet.setPermission(new Permission(PermissionGroup.DEFAULT_PERMISSION_URI));
		
		ArrayList<String> inherits = new ArrayList<String>();
		if (group.getParent() != null)
			inherits.add(group.getParent().getName());
		
		String scopeURI = group.getScopeURI(); 
		if( scopeURI == null ) {
			scope.setValue("");
			scopeChoices.setSelectedIndex(0);
			inheritenceUI.resetLists(inherits);
		} else {
			if( scopeURI.startsWith("taxon") ) {
				scope.setValue(scopeURI.replaceAll("taxon/\\d+?/", ""));
				scope.setData("scope", scopeURI);
				scopeChoices.setSelectedIndex(1);
			} else if( scopeURI.startsWith("workingSets") ) {
				scope.setValue("");
				scopeChoices.setSelectedIndex(2);
			} else if( scopeURI.startsWith("workingSet") ) {
				scope.setValue(scopeURI.substring( scopeURI.lastIndexOf("/")+1, 
						scopeURI.length()));
				scope.setData("scope", scopeURI);
				scopeChoices.setSelectedIndex(3);
			}
			
			inheritenceUI.resetLists(inherits);
		}
		
		scopeChoicesChanged();
		
		layout();
	}

	private void rebuildResourceSection() {
		resourceUIs.clear();
		resourcesSection.removeAll();
		resourcesSection.setBorders(true);
		
		HorizontalPanel p = new HorizontalPanel();
		p.setVerticalAlign(VerticalAlignment.BOTTOM);
		Button add = new Button();
		add.setIconStyle("icon-add");
		add.addSelectionListener(new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				if( !group.getResourceToPermission().containsKey("new") ) {
					Permission resource = new Permission("new");
					PermissionResourceUI ui = wrapInResourceUI(resource);
					resourceUIs.add(ui);
					
					group.addPermission(resource);
					resourcesSection.insert(ui, 1);
					resourcesSection.layout();
					
					ui.showEditingWindow();
				} else
					WindowUtils.errorAlert("New Resource Exists", "You have a newly created resource you haven't yet " +
							"modified. Please use that one first.");
			}
		});
		p.add(add);
		p.add(new Html("Resources"));
		
		resourcesSection.add(p);
		
		for( Entry<String, Permission> cur : group.getResourceToPermission().entrySet() ) {
			if( !cur.getKey().equalsIgnoreCase("default")) {
				PermissionResourceUI ui = wrapInResourceUI(cur.getValue());
				resourceUIs.add(ui);
				resourcesSection.add(ui, new RowData(1, -1, new Margins(5, 0, 0, 10)));
			}
		}
	}

	private PermissionResourceUI wrapInResourceUI(Permission resource) {
		return new PermissionResourceUI(group, resource, new GenericCallback<String>() {
			public void onSuccess(final String uri) {
				WindowUtils.confirmAlert("Remove Resource", "Are you sure you want to remove this resource?", new Listener<MessageBoxEvent>() {
					public void handleEvent(MessageBoxEvent be) {
						if( be.getButtonClicked().getText().equalsIgnoreCase("yes")) {
							group.removePermission(uri);
							updateContent();
						}
					}
				});
			}
			public void onFailure(Throwable caught) {} //Not implemented
		}, new GenericCallback<String>() { 
				public void onSuccess(final String uri) {
					WindowManager.get().getActive().hide();
					updateContent();
				}
				public void onFailure(Throwable caught) {} //Not implemented);
		});
	}
	
	private HorizontalPanel getStyledHorizontalPanel() {
		HorizontalPanel p = new HorizontalPanel();
		p.setSpacing(5);
		p.setVerticalAlign(VerticalAlignment.BOTTOM);
		p.setBorders(true);
		return p;
	}
	
	private VerticalPanel getStyledVerticalPanel() {
		VerticalPanel p = new VerticalPanel();
		p.setSpacing(5);
		p.setBorders(true);
		return p;
	}
	
	public void updateScope(Taxon  taxon) {
		scope.setValue(taxon.getFullName() + (taxon.getFootprint().length > 0 ? ("/" + taxon.getFootprint()[0]) : "") );
		scope.setData("scope", taxon);
	}
	
	public void updateScope(List<WorkingSet> sets) {
		String val = "";
		String ids = "";
		for( WorkingSet ws : sets ) {
			val += ws.getWorkingSetName() + ",";
			ids += ws.getId() + ",";
		}
		
		if( val.length() == 0 ) {
			scope.setValue("");
			scope.setData("scope", "");
		} else {
			scope.setValue(val.substring(0, val.length()-1));
			scope.setData("scope", ids.substring(0, ids.length()-1));
		}
	}

	private void switchToWorkingSetList() {
		if( workingSetList == null )
			workingSetList = new PermissionWorkingSetList(new ArrayList<WorkingSet>(), new ComplexListener<List<WorkingSet>>() {
				public void handleEvent(List<WorkingSet> eventData) {
					updateScope(eventData);
				}
			});
		
		accessoryPanel.switchToComponent(workingSetList);
	}
	
	private void clearAccessoryPanel() {
		accessoryPanel.switchToComponent(blankAccessoryPanel);
		scope.setValue("");
	}
}
