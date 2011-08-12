package org.iucn.sis.client.panels.taxomatic;

import org.iucn.sis.client.api.caches.TaxonomyCache;
import org.iucn.sis.client.panels.ClientUIContainer;
import org.iucn.sis.shared.api.models.Infratype;
import org.iucn.sis.shared.api.models.Synonym;
import org.iucn.sis.shared.api.models.Taxon;
import org.iucn.sis.shared.api.models.TaxonLevel;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.data.ModelKeyProvider;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
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
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.CenterLayout;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.util.events.SimpleListener;
import com.solertium.util.extjs.client.FormBuilder;
import com.solertium.util.extjs.client.WindowUtils;

public class NewTaxonSynonymEditor extends TaxomaticWindow {
	
	private final Taxon taxon;
	private final ListView<BaseModelData> view; 
	private final ListStore<BaseModelData> store;
	
	private final Html fullNameDisplay;
	private final ComboBox<BaseModelData> level;
	
	private final LayoutContainer center;
	private final FormPanel editingArea;
	private final TextField<String> upperLevelName, 
		genusName, specieName, infrarankName, stockName, 
		genusAuthor, specieAuthor, infrarankAuthor;
	private final SimpleComboBox<String> status;
	
	private LayoutContainer completeEditingArea;
	
	private EditableSynonym current;
	
	public NewTaxonSynonymEditor() {
		super("Synonym Editor", "icon-note-edit");
		
		this.taxon = TaxonomyCache.impl.getCurrentTaxon();
		this.store = new ListStore<BaseModelData>();
		this.store.setKeyProvider(new ModelKeyProvider<BaseModelData>() {
			public String getKey(BaseModelData model) {
				int id = model.get("id");
				return Integer.toString(id);
			}
		});
		this.view = new ListView<BaseModelData>(store);
		
		this.fullNameDisplay = new Html();
		this.center = new LayoutContainer(new FillLayout());
		this.center.setLayoutOnChange(true);
		
		this.level = new ComboBox<BaseModelData>();
		this.level.setTriggerAction(TriggerAction.ALL);
		this.level.setStore(generateLevelStore());
		this.level.addSelectionChangedListener(new SelectionChangedListener<BaseModelData>() {
			public void selectionChanged(SelectionChangedEvent<BaseModelData> se) {
				BaseModelData currentModel = se.getSelectedItem();
				if (currentModel == null) {
					return;
				}
				
				int level = currentModel.get("value");
				int infrarank = -1;
				if (currentModel.get("infrarank") != null) {
					Infratype t = Infratype.getInfratype((Integer)currentModel.get("infrarank"));
					if (t != null)
						infrarank = t.getId();
				}

				current.setTaxon_level(TaxonLevel.getTaxonLevel(level));
				if (infrarank > 0)
					current.setInfraTypeObject(Infratype.getInfratype(infrarank));
				
				fullNameDisplay.setHtml(current.generateFriendlyName());
				
				updateEditingArea(level, infrarank);
			}
		});
		
		FormLayout layout = new FormLayout();
		layout.setLabelWidth(150);
		layout.setLabelPad(25);
		
		this.editingArea = new FormPanel();
		this.editingArea.setBodyBorder(false);
		this.editingArea.setBorders(false);
		this.editingArea.setHeaderVisible(false);
		this.editingArea.setLayout(layout);
		this.editingArea.setScrollMode(Scroll.AUTO);
		
		this.upperLevelName = new TextField<String>();
		this.upperLevelName.setFieldLabel("Name");
		this.upperLevelName.addListener(Events.Blur, new BlurListener() {
			public void update() {
				current.setName(upperLevelName.getValue());
			}
		});
		this.genusName = new TextField<String>();
		this.genusName.setFieldLabel("Genus Name");
		this.genusName.addListener(Events.Blur, new BlurListener() {
			public void update() {
				current.setGenusName(genusName.getValue());
			}
		});
		
		this.genusAuthor = new TextField<String>();
		this.genusAuthor.setFieldLabel("Genus Author");
		this.genusAuthor.addListener(Events.Blur, new BlurListener() {
			public void update() {
				current.setGenusAuthor(genusAuthor.getValue());
			}
		});
		
		this.specieName = new TextField<String>();
		this.specieName.setFieldLabel("Species Name");
		this.specieName.addListener(Events.Blur, new BlurListener() {
			public void update() {
				current.setSpeciesName(specieName.getValue());
			}
		});
		
		this.infrarankName = new TextField<String>();
		this.infrarankName.setFieldLabel("Infrarank Name");
		this.infrarankName.addListener(Events.Blur, new BlurListener() {
			public void update() {
				current.setInfraName(infrarankName.getValue());
			}
		});
		
		this.specieAuthor = new TextField<String>();
		this.specieAuthor.setFieldLabel("Species Authority");
		this.specieAuthor.addListener(Events.Blur, new BlurListener() {
			public void update() {
				current.setSpeciesAuthor(specieAuthor.getValue());
			}
		});
		
		this.infrarankAuthor = new TextField<String>();
		this.infrarankAuthor.setFieldLabel("Infrarank Authority");
		this.infrarankAuthor.addListener(Events.Blur, new BlurListener() {
			public void update() {
				current.setInfrarankAuthor(infrarankAuthor.getValue());
			}
		});
		
		this.stockName = new TextField<String>();
		this.stockName.setFieldLabel("Subpopulation Name");
		this.stockName.addListener(Events.Blur, new BlurListener() {
			public void update() {
				current.setStockName(stockName.getValue());
			}
		});
		
		this.status = FormBuilder.createComboBox("status", null, "Action Proposed", false, 
				Synonym.ACCEPTED, Synonym.ADDED, Synonym.CHANGED, Synonym.DELETED);
		
		draw();
		
		stopEditing();
	}
	
	private ListStore<BaseModelData> generateLevelStore() {
		ListStore<BaseModelData> store = new ListStore<BaseModelData>();
		store.setKeyProvider(new ModelKeyProvider<BaseModelData>() {
			public String getKey(BaseModelData model) {
				return model.get("text");
			}
		});
		for (int level : new int[] { TaxonLevel.KINGDOM, TaxonLevel.PHYLUM, TaxonLevel.CLASS, TaxonLevel.ORDER, TaxonLevel.FAMILY, TaxonLevel.GENUS, TaxonLevel.SPECIES }) {
			BaseModelData model = new BaseModelData();
			model.set("text", TaxonLevel.getDisplayableLevel(level));
			model.set("value", level);
			
			store.add(model);
		}
		
		for (int level : new int[] { Infratype.INFRARANK_TYPE_SUBSPECIES, Infratype.INFRARANK_TYPE_VARIETY }) {
			BaseModelData model = new BaseModelData();
			model.set("text", TaxonLevel.getDisplayableLevel(TaxonLevel.INFRARANK, level));
			model.set("value", TaxonLevel.INFRARANK);
			model.set("infrarank", level);
			
			store.add(model);
		}
		
		BaseModelData model = new BaseModelData();
		model.set("text", TaxonLevel.getDisplayableLevel(TaxonLevel.SUBPOPULATION));
		model.set("value", TaxonLevel.SUBPOPULATION);
		
		store.add(model);
		
		return store;
	}
	
	public void draw() {
		center.add(completeEditingArea = createEditingArea());
		
		final LayoutContainer container = new LayoutContainer(new BorderLayout());
		container.add(createListingArea(), new BorderLayoutData(LayoutRegion.WEST, 150, 150, 150));
		container.add(center, new BorderLayoutData(LayoutRegion.CENTER));
		
		add(container);
	}
	
	public void setSynonym(Synonym synonym) {
		BaseModelData model = store.findModel(Integer.toString(synonym.getId()));
		if (model != null)
			view.getSelectionModel().select(model, false);
	}
	
	private void setCurrent(Synonym synonym) {
		center.removeAll();
		center.add(completeEditingArea);
		
		this.current = new EditableSynonym(synonym);
		
		this.fullNameDisplay.setHtml(synonym.getFriendlyName());
		
		String key = getLevelKey();
		
		if (key != null)
			this.level.setValue(this.level.getStore().findModel(key));
		
		level.setReadOnly(current.getTaxon_level().getLevel() <= TaxonLevel.GENUS);
		
		upperLevelName.setValue(current.getName());
		genusName.setValue(current.getGenusName());
		genusAuthor.setValue(current.getGenusAuthor());
		specieName.setValue(current.getSpeciesName());
		infrarankName.setValue(current.getInfraName());
		specieAuthor.setValue(current.getSpeciesAuthor());
		infrarankAuthor.setValue(current.getInfrarankAuthor());
		stockName.setValue(current.getStockName());
		status.setValue(status.findModel(current.getStatus()));
	}
	
	private void stopEditing() {
		editingArea.reset();
		editingArea.removeAll();
		center.removeAll();
		fullNameDisplay.setHtml("");
		level.setValue(null);
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
			"the synonym from the list on the left which you would " +
			"like to edit, or click \"Add\" to create a new synonym.");
		instructions.setBorders(true);
		
		container.add(instructions);
		
		center.removeAll();
		center.add(container);
	}
	
	private String getLevelKey() {
		TaxonLevel level = current.getTaxon_level();
		String infratype = current.getInfraType();
		
		String key = null;
		if (level.getLevel() == TaxonLevel.INFRARANK) {
			Infratype type = Infratype.getInfratype(infratype);
			if (type != null)
				key = TaxonLevel.getDisplayableLevel(level.getLevel(), type.getId());
		}
		else
			key = TaxonLevel.getDisplayableLevel(level.getLevel());
		
		return key;
	}
	
	private LayoutContainer createEditingArea() {
		final ToolBar bar = new ToolBar();
		bar.add(fullNameDisplay);
		bar.add(new FillToolItem());
		bar.add(new Html("Level:&nbsp;"));
		bar.add(level);
		
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
		
		final LayoutContainer container = new LayoutContainer(new BorderLayout());
		container.add(bar, new BorderLayoutData(LayoutRegion.NORTH, 25, 25, 25));
		container.add(editingArea, new BorderLayoutData(LayoutRegion.CENTER));
		container.add(bottom, new BorderLayoutData(LayoutRegion.SOUTH, 25, 25, 25));
		
		return container;
	}
	
	private void updateEditingArea(int level, int infrarank) {
		editingArea.removeAll();
		
		if (level < TaxonLevel.GENUS) {
			genusAuthor.setFieldLabel("Authority");
			editingArea.add(upperLevelName);
			editingArea.add(genusAuthor);
		}
		
		if (level == TaxonLevel.GENUS) {
			genusAuthor.setFieldLabel("Genus Authority");
			editingArea.add(genusName);
			editingArea.add(genusAuthor);
		}
		
		if (level >= TaxonLevel.SPECIES) {
			editingArea.add(genusName);
			editingArea.add(specieName);
		}
		
		if (level == TaxonLevel.INFRARANK_SUBPOPULATION || level == TaxonLevel.INFRARANK)
			editingArea.add(infrarankName);
		
		if (level == TaxonLevel.INFRARANK_SUBPOPULATION || level == TaxonLevel.SUBPOPULATION)
			editingArea.add(stockName);
		
		editingArea.add(status);
		
		if (level >= TaxonLevel.SPECIES) {
			editingArea.add(specieAuthor);
			if (level != TaxonLevel.SPECIES)
				editingArea.add(infrarankAuthor);
		}
		
		editingArea.layout();
	}
	
	private LayoutContainer createListingArea() {
		for (Synonym synonym : taxon.getSynonyms()) {
			BaseModelData model = new BaseModelData();
			model.set("model", synonym);
			model.set("id", synonym.getId());
			model.set("text", synonym.getFriendlyName());
			
			store.add(model);
		}
		
		final ListViewSelectionModel<BaseModelData> sm = 
			new ListViewSelectionModel<BaseModelData>();
		sm.setSelectionMode(SelectionMode.SINGLE);
		sm.addSelectionChangedListener(new SelectionChangedListener<BaseModelData>() {
			public void selectionChanged(SelectionChangedEvent<BaseModelData> se) {
				BaseModelData selection = se.getSelectedItem();
				if (selection != null)
					setCurrent((Synonym)selection.get("model"));
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
					final Synonym synonym = selected.get("model");
					WindowUtils.confirmAlert("Confirm", "Are you sure you want to delete this synonym?", new WindowUtils.SimpleMessageBoxListener() {
						public void onYes() {
							delete(synonym, new SimpleListener() {
								public void handleEvent() {
									store.remove(selected);
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
				
				Synonym synonym = new Synonym();
				synonym.setStatus(Synonym.ADDED);
				synonym.setTaxon(taxon);
				synonym.setTaxon_level(taxon.getTaxonLevel());
				
				setCurrent(synonym);		
			}
		};
		
		if (current == null)
			callback.onYes();
		else {
			WindowUtils.confirmAlert("Confirm", "Adding a new " +
					"synonym now will cancel any current changes " +
					"you have.  Continue?", callback);
		}
	}
	
	private void delete(final Synonym synonym, final SimpleListener listener) {
		WindowUtils.showLoadingAlert("Deleting synonym...");
		TaxonomyCache.impl.deleteSynonymn(taxon, synonym, new GenericCallback<String>() {
			public void onSuccess(String result) {
				WindowUtils.hideLoadingAlert();
				WindowUtils.infoAlert("Saved", "Synonym has been deleted.");
				ClientUIContainer.bodyContainer.refreshTaxonPage();
				listener.handleEvent();
				stopEditing();
			}

			@Override
			public void onFailure(Throwable caught) {
				WindowUtils.hideLoadingAlert();
				WindowUtils.errorAlert("Unable to delete the synonym");
			}
		});
	}
	
	private void save() {
		stageChanges();
		
		if (validateEntry()) {
			WindowUtils.showLoadingAlert("Saving Changes...");
			TaxonomyCache.impl.addOrEditSynonymn(taxon, current, new GenericCallback<String>() {
				public void onSuccess(String result) {
					WindowUtils.hideLoadingAlert();
					Info.display("Success", "Changes saved.");
					
					current.acceptChanges();
					
					Synonym synonym = current.getModel();
					if (synonym.getId() == 0) { //New
						taxon.getSynonyms().remove(current);
						
						synonym.setId(current.getId());
						taxon.getSynonyms().add(synonym);
						
						BaseModelData model = new BaseModelData();
						model.set("model", synonym);
						model.set("id", synonym.getId());
						model.set("text", synonym.getFriendlyName());
						
						store.add(model);			
						
						current = new EditableSynonym(synonym);
					}
					else {
						BaseModelData model = view.getSelectionModel().getSelectedItem();
						model.set("text", synonym.getFriendlyName());
						
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
		current.sink(new Synonym(), current);
		
		current.clearAuthorities();
		
		int curLevel = level.getValue().get("value");

		if (curLevel < TaxonLevel.GENUS) {
			current.setName(upperLevelName.getValue());
			current.setAuthor(genusAuthor.getValue());
		}
		else if (curLevel == TaxonLevel.GENUS) {
			current.setGenusName(genusName.getValue());
			current.setGenusAuthor(genusAuthor.getValue());
		} else if (curLevel == TaxonLevel.SPECIES) {
			current.setGenusName(genusName.getValue());
			current.setSpeciesName(specieName.getValue());
		} else if (curLevel == TaxonLevel.SUBPOPULATION) {
			current.setGenusName(genusName.getValue());
			current.setSpeciesName(specieName.getValue());
			current.setStockName(stockName.getValue());
		} else if (curLevel == TaxonLevel.INFRARANK) {
			current.setGenusName(genusName.getValue());
			current.setSpeciesName(specieName.getValue());
			current.setInfraName(infrarankName.getValue());
			
			String text = level.getValue().get("text");
			if (text.equalsIgnoreCase(Infratype.VARIETY_NAME)) {
				current.setInfraTypeObject(Infratype.getInfratype(Infratype.VARIETY_NAME));
				current.setInfraType(Infratype.VARIETY_NAME);
			} else {
				current.setInfraTypeObject(Infratype.getInfratype(Infratype.SUBSPECIES_NAME));
				current.setInfraType(Infratype.SUBSPECIES_NAME);
			}
		} else if (curLevel == TaxonLevel.INFRARANK_SUBPOPULATION) {
			current.setGenusName(genusName.getValue());
			current.setSpeciesName(specieName.getValue());
			current.setInfraName(infrarankName.getValue());
			current.setStockName(stockName.getValue());
		}

		current.setStatus(status.getValue().getValue());
		current.setTaxon_level(TaxonLevel.getTaxonLevel(curLevel));
		current.setSpeciesAuthor(specieAuthor.getValue());
		current.setInfrarankAuthor(infrarankAuthor.getValue());
		current.setFriendlyName(null);
		current.getFriendlyName();
	}
	
	private boolean validateEntry(){
		int curLevel = level.getValue().get("value");
		
		String message = null;

		if (curLevel < TaxonLevel.GENUS && isBlank(current.getName())) {
			message = "Please enter the Synonym name.";
		} else if (curLevel == TaxonLevel.GENUS && isBlank(current.getGenusName())) {
			message = "Please enter the Genus name.";
		} else if (curLevel == TaxonLevel.SPECIES) {
			if (isBlank(current.getGenusName())){
				message = "Please enter the Genus name.";
			} else if (isBlank(current.getSpeciesName())){
				message = "Please enter the Species name."; 
			}
		} else if (curLevel == TaxonLevel.INFRARANK) {
			if (isBlank(current.getGenusName()))
				message = "Please enter the Genus name.";
			else if(isBlank(current.getSpeciesName()))
				message = "Please enter the Species name.";
			else if(isBlank(current.getInfraName()))
				message = "Please enter the Infrarank name.";
		} else if (curLevel == TaxonLevel.SUBPOPULATION) {
			if (isBlank(current.getGenusName()))
				message = "Please enter the Genus name."; 
			else if (isBlank(current.getSpeciesName()))
				message = "Please enter the Species name.";
			else if (isBlank(current.getStockName()))
				message = "Please enter the Subpopulation name.";
		}
		
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
	
	private abstract class BlurListener implements Listener<FieldEvent> {

		public void handleEvent(FieldEvent be) {
			update();
			fullNameDisplay.setHtml(current.generateFriendlyName());
		}
		
		public abstract void update();
		
	}
	
	private static class EditableSynonym extends Synonym {

		private static final long serialVersionUID = 1L;
		
		private final Synonym synonym;
		
		public EditableSynonym(Synonym synonym) {
			this.synonym = synonym;
			if (synonym.getId() != 0)
				setId(synonym.getId());
			
			init();
		}
		
		public Synonym getModel() {
			return synonym;
		}
		
		public void rejectChanges() {
			init();
		}
		
		public void acceptChanges() {
			sink(this, synonym);
		}
		
		private void init() {
			sink(synonym, this);
		}
		
		public void sink(Synonym source, Synonym target) {
			target.setAuthor(source.getAuthor());
			target.setFriendlyName(source.getFriendlyName());
			target.setGenusAuthor(source.getGenusAuthor());
			target.setGenusName(source.getGenusName());
			target.setInfraName(source.getInfraName());
			target.setInfrarankAuthor(source.getInfrarankAuthor());
			target.setInfraType(source.getInfraType());
			target.setName(source.getName());
			target.setSpeciesAuthor(source.getSpeciesAuthor());
			target.setSpeciesName(source.getSpeciesName());
			target.setStatus(source.getStatus());
			target.setStockName(source.getStockName());
			target.setTaxon(source.getTaxon());
			target.setTaxon_level(source.getTaxon_level());
		}	
	}
}
