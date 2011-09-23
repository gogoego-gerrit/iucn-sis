package org.iucn.sis.client.panels.taxomatic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.iucn.sis.client.api.caches.LanguageCache;
import org.iucn.sis.client.api.caches.TaxonomyCache;
import org.iucn.sis.client.panels.ClientUIContainer;
import org.iucn.sis.client.panels.taxomatic.EditCommonNamePanel.IsoLanguageComparator;
import org.iucn.sis.shared.api.debug.Debug;
import org.iucn.sis.shared.api.models.CommonName;
import org.iucn.sis.shared.api.models.IsoLanguage;
import org.iucn.sis.shared.api.models.Taxon;
import org.iucn.sis.shared.api.utils.CommonNameComparator;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.Style.SortDir;
import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.data.ModelKeyProvider;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.store.StoreSorter;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.HtmlContainer;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.ListView;
import com.extjs.gxt.ui.client.widget.ListViewSelectionModel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.extjs.gxt.ui.client.widget.form.SimpleComboValue;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.CenterLayout;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.util.events.ComplexListener;
import com.solertium.util.events.SimpleListener;
import com.solertium.util.extjs.client.FormBuilder;
import com.solertium.util.extjs.client.WindowUtils;
import com.solertium.util.gwt.ui.DrawsLazily;

public class NewCommonNameEditor extends TaxomaticWindow implements DrawsLazily {
	
	private static final CommonNameComparator cnComparator = new CommonNameComparator();
	
	private final Taxon taxon;
	private final ListView<BaseModelData> view; 
	private final ListStore<BaseModelData> store;
	
	private final Html fullNameDisplay;
	
	private final LayoutContainer center;
	private final FormPanel editingArea;
	private final TextField<String> name;
	private final SimpleComboBox<String> status, validated;
	private final ComboBox<IsoLanguageModelData> isoLanguage;
	
	private LayoutContainer completeEditingArea;
	
	private EditableCommonName current;
	
	
	public NewCommonNameEditor() {
		super("Common Name Editor", "icon-note-edit");
		
		this.taxon = TaxonomyCache.impl.getCurrentTaxon();
		this.store = new ListStore<BaseModelData>();
		this.store.setStoreSorter(new StoreSorter<BaseModelData>() {
			public int compare(Store<BaseModelData> store, BaseModelData m1,
					BaseModelData m2, String property) {
				CommonName c1 = m1.get("model"), c2 = m2.get("model");
				return cnComparator.compare(c1, c2);
			}
		});
		this.store.setKeyProvider(new ModelKeyProvider<BaseModelData>() {
			public String getKey(BaseModelData model) {
				int id = model.get("id");
				return Integer.toString(id);
			}
		});
		
		this.view = new ListView<BaseModelData>(store);
		this.view.setSimpleTemplate("<span class=\"{style}\">{primary}{name}</span>");
		
		this.fullNameDisplay = new Html();
		this.center = new LayoutContainer(new FillLayout());
		this.center.setLayoutOnChange(true);
		
		
		FormLayout layout = new FormLayout();
		layout.setLabelWidth(80);
		layout.setDefaultWidth(320);
		layout.setLabelPad(25);
		
		this.editingArea = new FormPanel();
		this.editingArea.setBodyBorder(false);
		this.editingArea.setBorders(false);
		this.editingArea.setHeaderVisible(false);
		this.editingArea.setLayout(layout);
		this.editingArea.setScrollMode(Scroll.AUTO);
		
		this.name = new TextField<String>();
		this.name.setFieldLabel("Name");
		
		this.isoLanguage = FormBuilder.createModelComboBox("language", null, "Language / ISO", false, 
				new IsoLanguageModelData());
		this.isoLanguage.getStore().setKeyProvider(new ModelKeyProvider<IsoLanguageModelData>() {
			public String getKey(IsoLanguageModelData model) {
				return "" + model.get("value");
			}
		});
		
		this.validated = FormBuilder.createComboBox("validated", null, "Validated", false, "Yes", "No");
		this.validated.addSelectionChangedListener(new SelectionChangedListener<SimpleComboValue<String>>() {
			public void selectionChanged(SelectionChangedEvent<SimpleComboValue<String>> se) {
				if (se.getSelectedItem() != null) {
					boolean validated = "Yes".equals(se.getSelectedItem().getValue());
					status.setVisible(!validated);
					if (validated)
						status.setValue(null);
				}
			}
		});
			
		this.status = FormBuilder.createComboBox("status", null, "Status", false, CommonName.reasons);
	}
	
	@Override
	public void draw(final DoneDrawingCallback callback) {
		LanguageCache.impl.list(new ComplexListener<List<IsoLanguage>>() {
			public void handleEvent(List<IsoLanguage> eventData) {
				Collections.sort(eventData, new IsoLanguageComparator());
				for (IsoLanguage current : eventData)
					isoLanguage.getStore().add(new IsoLanguageModelData(current));
				
				center.add(completeEditingArea = createEditingArea());
				
				final LayoutContainer container = new LayoutContainer(new BorderLayout());
				container.add(createListingArea(), new BorderLayoutData(LayoutRegion.WEST, 150, 150, 150));
				container.add(center, new BorderLayoutData(LayoutRegion.CENTER));
				
				add(container);
				
				stopEditing();
				
				callback.isDrawn();
			}
		});
	}
	
	public void setCommonName(CommonName commonName) {
		BaseModelData model = store.findModel(Integer.toString(commonName.getId()));
		if (model != null)
			view.getSelectionModel().select(model, false);
	}
	
	private void setCurrent(CommonName commonName) {
		center.removeAll();
		center.add(completeEditingArea);
		
		this.current = new EditableCommonName(commonName);
		
		this.fullNameDisplay.setHtml(current.getName());
		
		name.setValue(current.getName());
		
		if (current.getIso() != null)
			isoLanguage.setValue(isoLanguage.getStore().findModel(""+current.getIso().getId()));
		else
			isoLanguage.setValue(null);
		
		if (current.getChangeReason() > 0) {
			try {
				status.setValue(status.findModel(CommonName.reasons[current.getChangeReason()]));
			} catch (IndexOutOfBoundsException e) { }
		}
		else
			status.setValue(null);
		
		String validatedValue = current.getValidated() ? "Yes" : "No";
		
		validated.setValue(validated.findModel(validatedValue));
	}
	
	private void stopEditing() {
		editingArea.reset();
		
		center.removeAll();
		fullNameDisplay.setHtml("");
		view.getSelectionModel().deselectAll();
		
		current = null;
		
		showDefaultScreen();
	}
	
	private void showDefaultScreen() {
		final LayoutContainer container = new LayoutContainer(new CenterLayout());
		
		final HtmlContainer instructions = new HtmlContainer();
		instructions.addStyleName("gwt-background");
		instructions.setSize(200, 100);
		instructions.setHtml("<b>Instructions</b>: Select " +
			"the common name from the list on the left which you would " +
			"like to edit, or click \"Add\" to create a new common name.");
		instructions.setBorders(true);
		
		container.add(instructions);
		
		center.removeAll();
		center.add(container);
	}
	
	private LayoutContainer createEditingArea() {
		final ToolBar bar = new ToolBar();
		bar.add(fullNameDisplay);
		bar.add(new FillToolItem());
		
		final ButtonBar bottom = new ButtonBar();
		bottom.setAlignment(HorizontalAlignment.CENTER);
		bottom.setSpacing(10);
		bottom.add(new Button("Save Changes", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				save();
			}
		}));
		bottom.add(new Button("Cancel Changes", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				current.rejectChanges();
				stopEditing();
			}
		}));
		
		editingArea.add(name);
		editingArea.add(isoLanguage);
		editingArea.add(validated);
		editingArea.add(status);
		
		final LayoutContainer container = new LayoutContainer(new BorderLayout());
		container.add(bar, new BorderLayoutData(LayoutRegion.NORTH, 25, 25, 25));
		container.add(editingArea, new BorderLayoutData(LayoutRegion.CENTER));
		container.add(bottom, new BorderLayoutData(LayoutRegion.SOUTH, 25, 25, 25));
		
		return container;
	}
	
	@SuppressWarnings("unchecked")
	private BaseModelData newModelData(final CommonName commonName) {
		BaseModelData model = new BaseModelData() {
			private static final long serialVersionUID = 1L;
			public <X> X get(String property) {
				if ("primary".equals(property)) {
					CommonName cn = get("model");
					String result = cn.isPrimary() ? "* " : "";
					return (X) result;
				}
				else
					return super.get(property);
			}
		};
		model.set("model", commonName);
		model.set("id", commonName.getId());
		model.set("primary", "");
		model.set("name", commonName.getName());
		model.set("text", commonName.getName());
		model.set("style", "");
		
		return model;
	}
	
	private LayoutContainer createListingArea() {
		List<CommonName> list = new ArrayList<CommonName>(taxon.getCommonNames());
		Collections.sort(list, new CommonNameComparator());
		for (CommonName commonName : list)
			store.add(newModelData(commonName));
		
		final ListViewSelectionModel<BaseModelData> sm = 
			new ListViewSelectionModel<BaseModelData>();
		sm.setSelectionMode(SelectionMode.SINGLE);
		sm.addSelectionChangedListener(new SelectionChangedListener<BaseModelData>() {
			public void selectionChanged(SelectionChangedEvent<BaseModelData> se) {
				BaseModelData selection = se.getSelectedItem();
				if (selection != null)
					setCurrent((CommonName)selection.get("model"));
			}
		});
		
		view.setSelectionModel(sm);
		
		final ButtonBar bar = new ButtonBar();
		bar.setAlignment(HorizontalAlignment.CENTER);
		bar.add(new Button("Add", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				add();
			}
		}));
		bar.add(new Button("Remove", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				final BaseModelData selected = sm.getSelectedItem();
				if (selected != null) {
					final CommonName commonName = selected.get("model");
					WindowUtils.confirmAlert("Confirm", "Are you sure you want to delete this common name?", new WindowUtils.SimpleMessageBoxListener() {
						public void onYes() {
							delete(commonName, new SimpleListener() {
								public void handleEvent() {
									store.remove(selected);
								}
							});
						}
					});
				}
			}
		}));
		bar.add(new Button(" * ", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				final BaseModelData selected = sm.getSelectedItem();
				if (selected != null) {
					final CommonName commonName = selected.get("model");
					if (commonName.isPrimary()) {
						Info.display("No change needed", commonName.getName() + " is already the primary common name.");
						return;
					}
					WindowUtils.confirmAlert("Confirm", "Are you sure you want to set " + commonName.getName() + " as the primary common name?", new WindowUtils.SimpleMessageBoxListener() {
						public void onYes() {
							TaxonomyCache.impl.setPrimaryCommonName(taxon, commonName, new GenericCallback<String>() {
								public void onSuccess(String result) {
									current.setPrincipal(true);
									for (BaseModelData model : view.getStore().getModels()) {
										CommonName cn = model.get("model");
										cn.setPrincipal(cn.getId() == current.getId());
									}
									store.sort("model", SortDir.ASC);
									Info.display("Success", "Primary common name updated.");
									ClientUIContainer.bodyContainer.refreshBody();
								}
								public void onFailure(Throwable caught) {
									WindowUtils.errorAlert("Could not make changes, please try again later.");
								}
							});
						}
					});
				}
			}
		}));
		
		final LayoutContainer container = new LayoutContainer(new BorderLayout());
		container.add(view, new BorderLayoutData(LayoutRegion.CENTER));
		container.add(bar, new BorderLayoutData(LayoutRegion.SOUTH, 25, 25, 25));
		
		return container;
	}
	
	public void add() {
		WindowUtils.SimpleMessageBoxListener callback = new WindowUtils.SimpleMessageBoxListener() {
			public void onYes() {
				stopEditing();
				
				CommonName commonName = new CommonName();
				commonName.setValidated(false);
				//TODO: set change reason to "added?"
				
				setCurrent(commonName);
			}
		};
		
		if (current == null)
			callback.onYes();
		else {
			WindowUtils.confirmAlert("Confirm", "Adding a new " +
					"common name now will cancel any current changes " +
					"you have.  Continue?", callback);
		}
	}
	
	private void delete(final CommonName commonName, final SimpleListener listener) {
		WindowUtils.showLoadingAlert("Deleting common name...");
		TaxonomyCache.impl.deleteCommonName(taxon, commonName, new GenericCallback<String>() {
			public void onSuccess(String result) {
				WindowUtils.hideLoadingAlert();
				WindowUtils.infoAlert("Saved", "Common Name has been deleted.");
				ClientUIContainer.bodyContainer.refreshTaxonPage();
				listener.handleEvent();
				stopEditing();
			}

			@Override
			public void onFailure(Throwable caught) {
				WindowUtils.hideLoadingAlert();
				WindowUtils.errorAlert("Unable to delete the common name");
			}
		});
	}
	
	private void save() {
		stageChanges();
		
		if (validateEntry()) {
			WindowUtils.showLoadingAlert("Saving Changes...");
			TaxonomyCache.impl.addOrEditCommonName(taxon, current, new GenericCallback<String>() {
				public void onSuccess(String result) {
					WindowUtils.hideLoadingAlert();
					Info.display("Success", "Changes saved.");
					
					current.acceptChanges();
					
					CommonName commonName = current.getModel();
					if (commonName.getId() == 0) { //New
						taxon.getCommonNames().remove(current);
						
						commonName.setId(current.getId());
						commonName.setPrincipal(taxon.getCommonNames().isEmpty());
						taxon.getCommonNames().add(commonName);
						
						BaseModelData model = newModelData(commonName);
						
						store.add(model);
						store.sort("model", SortDir.ASC);
						
						current = new EditableCommonName(commonName);
					}
					else {
						BaseModelData model = view.getSelectionModel().getSelectedItem();
						model.set("text", commonName.getName());
						model.set("name", commonName.getName());
						model.set("style", commonName.getChangeReason() == CommonName.DELETED ? "deleted" : "");
						
						view.refresh();
					}
					
					ClientUIContainer.bodyContainer.refreshTaxonPage();
					
					stopEditing();
				}

				@Override
				public void onFailure(Throwable caught) {
					WindowUtils.hideLoadingAlert();
					WindowUtils.errorAlert("Error",
							"An error occurred when trying to save the synonym data related to " + 
							taxon.getFullName() + ".");
				}
			});
		}
	}
	
	private void stageChanges() {
		current.sink(new CommonName(), current);

		current.setName(name.getValue());
		
		IsoLanguageModelData lang = isoLanguage.getValue();
		IsoLanguage model = lang != null ? (IsoLanguage)lang.get("model") : null;
		
		current.setIso(model);
		
		String statusValue = status.getValue() != null ? status.getValue().getValue() : null;
		if (statusValue != null) {
			int index = 0;
			for (String reason : CommonName.reasons) {
				if (reason.equals(statusValue))	
					break;
				index++;
			}
			if (index > 0)
				current.setChangeReason(index);
		}
		
		if (validated.getValue() != null) {
			Debug.println("Setting validated to {0}", validated.getValue().getValue());
			current.setValidated("Yes".equals(validated.getValue().getValue()));
		}
		else
			Debug.println("Validated is null");
	}
	
	private boolean validateEntry(){
		String message = null;
		
		if (isBlank(current.getName()))
			message = "Please enter a name for the common name.";
		else if (current.getIso() == null)
			message = "Please select a language for the common name.";

		if (message == null)
			return true;
		else {
			WindowUtils.errorAlert(message);
			return false;
		}
	}
	
	private boolean isBlank(String value) {
		return value == null || "".equals(value);
	}
	
	private static class IsoLanguageModelData extends BaseModelData {
		
		private static final long serialVersionUID = 1L;
		
		public IsoLanguageModelData() {
			super();
			set("text", "");
			set("value", null);
			set("model", null);
		}
		
		public IsoLanguageModelData(IsoLanguage language) {
			super();
			set("text", language.getName());
			set("value", language.getId());
			set("model", language);
		}
		
	}
	
	private static class EditableCommonName extends CommonName {

		private static final long serialVersionUID = 1L;
		
		private final CommonName commonName;
		
		public EditableCommonName(CommonName commonName) {
			this.commonName = commonName;
			if (commonName.getId() != 0)
				setId(commonName.getId());
			setPrincipal(commonName.getPrincipal());
			init();
		}
		
		public CommonName getModel() {
			return commonName;
		}
		
		public void rejectChanges() {
			init();
		}
		
		public void acceptChanges() {
			sink(this, commonName);
		}
		
		private void init() {
			sink(commonName, this);
		}
		
		public void sink(CommonName source, CommonName target) {
			target.setChangeReason(source.getChangeReason());
			target.setIso(source.getIso());
			target.setName(source.getName());
			target.setValidated(source.getValidated());
		}	
	}

}
