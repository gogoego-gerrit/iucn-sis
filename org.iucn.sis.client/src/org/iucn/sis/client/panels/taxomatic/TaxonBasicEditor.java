package org.iucn.sis.client.panels.taxomatic;

import org.iucn.sis.client.api.caches.TaxonomyCache;
import org.iucn.sis.client.api.container.StateChangeEvent;
import org.iucn.sis.client.api.container.StateManager;
import org.iucn.sis.client.panels.taxomatic.TaxomaticUtils.TaxonomyException;
import org.iucn.sis.shared.api.debug.Debug;
import org.iucn.sis.shared.api.models.Infratype;
import org.iucn.sis.shared.api.models.Taxon;
import org.iucn.sis.shared.api.models.TaxonLevel;
import org.iucn.sis.shared.api.models.TaxonStatus;

import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.data.ModelKeyProvider;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.solertium.lwxml.shared.GWTConflictException;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.util.extjs.client.FormBuilder;
import com.solertium.util.extjs.client.WindowUtils;
import com.solertium.util.gwt.ui.DrawsLazily;

public class TaxonBasicEditor extends TaxomaticWindow implements DrawsLazily {
	private final Taxon taxon;
	
	private TextField<String> name;
	private ComboBox<TextValueModelData<Integer>> level;
	private ComboBox<TextValueModelData<String>> status;
	private ComboBox<TextValueModelData<Boolean>> hybrid;
	private TextField<String> taxonomicAuthority;
	
	private CheckBox invasive, feral;

	public TaxonBasicEditor() {
		this(TaxonomyCache.impl.getCurrentTaxon());
	}
	
	public TaxonBasicEditor(Taxon taxon) {
		super("Basic Taxon Information Editor", "icon-note-edit");
		this.taxon = taxon;
		setSize(500, 300);
		setLayout(new FillLayout());
		addStyleName("gwt-background");
	}

	private void buildButtons() {
		addButton(new Button("Save", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				save();
			}
		}));
		addButton(new Button("Cancel", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				hide();
			}
		}));
	}
	
	@Override
	public void show() {
		draw(new DrawsLazily.DoneDrawingCallback() {
			public void isDrawn() {
				open();
			}
		});
	}
	
	private void open() {
		super.show();
	}
	
	@Override
	public void draw(final DoneDrawingCallback callback) {
		TaxonomyCache.impl.fetchPathWithID(taxon.getId() + "", new GenericCallback<NativeDocument>() {
			public void onFailure(Throwable caught) {
				onSuccess(null);
			}
			public void onSuccess(NativeDocument ndoc) {
				// It has no children - it can be changed to a subpop.
				boolean canEditLevel;
				if (ndoc == null)
					canEditLevel = false;
				else
					canEditLevel = ndoc.getDocumentElement().getElementsByTagName("option").getLength() == 0;

				buildButtons();
				drawInfo(canEditLevel);
				
				callback.isDrawn();
			}
		});
	}

	private void drawInfo(boolean canEditLevel) {
		final FormLayout layout = new FormLayout();
		layout.setLabelWidth(150);
		layout.setDefaultWidth(200);
		
		final FormPanel editor = new FormPanel();
		editor.setStyleName("gwt-background");
		editor.setLayout(layout);
		editor.setHeaderVisible(false);
		editor.setBodyBorder(false);
		editor.setBorders(false);
		
		name = FormBuilder.createTextField("name", taxon.getName(), "Name", false);
		
		editor.add(name);

		level = new ComboBox<TextValueModelData<Integer>>();
		level.setTriggerAction(TriggerAction.ALL);
		level.setForceSelection(true);
		level.setEditable(false);
		level.setAllowBlank(false);
		level.setFieldLabel("Level");
		
		ListStore<TextValueModelData<Integer>> levelStore = new ListStore<TextValueModelData<Integer>>();
		levelStore.setKeyProvider(new ModelKeyProvider<TextValueModelData<Integer>>() {
			public String getKey(TextValueModelData<Integer> model) {
				return model.getValue().toString();
			}
		});
		if (taxon.getLevel() == TaxonLevel.SUBPOPULATION) {
			levelStore.add(new TextValueModelData<Integer>(taxon.getDisplayableLevel(), taxon.getLevel()));
			levelStore.add(new TextValueModelData<Integer>(
				TaxonLevel.getDisplayableLevel(TaxonLevel.INFRARANK, Infratype.INFRARANK_TYPE_SUBSPECIES),
				Infratype.INFRARANK_TYPE_SUBSPECIES));
			if (!taxon.getFootprintAsString().contains("ANIMALIA"))
				levelStore.add(new TextValueModelData<Integer>(
						TaxonLevel.getDisplayableLevel(TaxonLevel.INFRARANK, Infratype.INFRARANK_TYPE_VARIETY),
						Infratype.INFRARANK_TYPE_VARIETY));
			levelStore.add(new TextValueModelData<Integer>(
					TaxonLevel.getDisplayableLevel(TaxonLevel.INFRARANK, Infratype.INFRARANK_TYPE_FORMA),
					Infratype.INFRARANK_TYPE_FORMA));
			level.setStore(levelStore);
			level.setValue(levelStore.getAt(0));
		}
		else if (taxon.getLevel() == TaxonLevel.INFRARANK) {
			levelStore.add(new TextValueModelData<Integer>(
				TaxonLevel.getDisplayableLevel(TaxonLevel.INFRARANK, Infratype.INFRARANK_TYPE_SUBSPECIES),
				Infratype.INFRARANK_TYPE_SUBSPECIES));
			if (!taxon.getFootprintAsString().contains("ANIMALIA"))
				levelStore.add(new TextValueModelData<Integer>(
						TaxonLevel.getDisplayableLevel(TaxonLevel.INFRARANK, Infratype.INFRARANK_TYPE_VARIETY),
						Infratype.INFRARANK_TYPE_VARIETY));
			levelStore.add(new TextValueModelData<Integer>(
					TaxonLevel.getDisplayableLevel(TaxonLevel.INFRARANK, Infratype.INFRARANK_TYPE_FORMA),
					Infratype.INFRARANK_TYPE_FORMA));
			if (canEditLevel) {
				levelStore.add(new TextValueModelData<Integer>(
					TaxonLevel.getDisplayableLevel(TaxonLevel.SUBPOPULATION), TaxonLevel.SUBPOPULATION));
			}
			
			level.setStore(levelStore);
			level.setValue(levelStore.findModel(""+taxon.getInfratype().getId()));
		} else {
			levelStore.add(new TextValueModelData<Integer>(taxon.getDisplayableLevel(), taxon.getLevel()));
			level.setStore(levelStore);
			level.setValue(levelStore.getAt(0));
			level.setEnabled(false);
		}
		
		editor.add(level);

		status = new ComboBox<TextValueModelData<String>>();
		status.setTriggerAction(TriggerAction.ALL);
		status.setForceSelection(true);
		status.setEditable(false);
		status.setAllowBlank(false);
		status.setFieldLabel("Status");
		
		ListStore<TextValueModelData<String>> statusStore = new ListStore<TextValueModelData<String>>();
		statusStore.setKeyProvider(new ModelKeyProvider<TextValueModelData<String>>() {
			public String getKey(TextValueModelData<String> model) {
				return model.getValue();
			}
		});
		statusStore.add(new TextValueModelData<String>("<Unset>", ""));
		for (String code : TaxonStatus.ALL)
			statusStore.add(new TextValueModelData<String>(TaxonStatus.displayableStatus.get(code), code));
		if (taxon.getLevel() >= TaxonLevel.SPECIES)
			statusStore.add(new TextValueModelData<String>("Undescribed", "U"));
		
		status.setStore(statusStore);
		if (taxon.getTaxonStatus() == null)
			status.setValue(statusStore.getAt(0));
		else {
			@SuppressWarnings("unused")
			TextValueModelData<String> selection = null;
			status.setValue(selection = statusStore.findModel(taxon.getStatusCode()));
			//TODO: warn if selection is null
		}
		
		editor.add(status);

		if (taxon.getLevel() >= TaxonLevel.SPECIES) {
			hybrid = new ComboBox<TextValueModelData<Boolean>>();
			hybrid.setTriggerAction(TriggerAction.ALL);
			hybrid.setForceSelection(true);
			hybrid.setEditable(false);
			hybrid.setAllowBlank(false);
			hybrid.setFieldLabel("Hybrid");
			
			ListStore<TextValueModelData<Boolean>> hybridStore = new ListStore<TextValueModelData<Boolean>>();
			hybridStore.add(new TextValueModelData<Boolean>("Yes", Boolean.TRUE));
			hybridStore.add(new TextValueModelData<Boolean>("No", Boolean.FALSE));
			
			hybrid.setStore(hybridStore);
			hybrid.setValue(taxon.getHybrid() ? hybridStore.getAt(0) : hybridStore.getAt(1));
			
			editor.add(hybrid);
		}

		taxonomicAuthority = FormBuilder.createTextField("taxonomicAuthority", taxon.getTaxonomicAuthority(), "Taxonomic Authority", false);
		
		editor.add(taxonomicAuthority);
		
		//if (node.getLevel() == TaxonLevel.SPECIES) {
		invasive = FormBuilder.createCheckBoxField("invasive", taxon.getInvasive(), "Invasive");
		
		editor.add(invasive);
			
		feral = FormBuilder.createCheckBoxField("feral", taxon.getFeral(), "Feral");
		
		editor.add(feral);
		//}
		
		add(editor);
	}

	private void save() {
		WindowUtils.showLoadingAlert("Saving...");
		saveInfo();
		TaxomaticUtils.impl.saveTaxon(taxon, new GenericCallback<Object>() {
			public void onFailure(Throwable caught) {
				WindowUtils.hideLoadingAlert();
				if (caught instanceof TaxonomyException)
					WindowUtils.errorAlert(caught.getMessage());
				else if (caught instanceof GWTConflictException) {
					WindowUtils.infoAlert("Error", taxon.getFullName() + " has not been saved. A taxon"
							+ " in the kingdom " + taxon.getKingdomName() + " already exists.");
					TaxonomyCache.impl.evict(taxon.getId()+"");
				} else {
					WindowUtils.errorAlert(taxon.getFullName() + " has not been saved.  "
							+ "Please try again later.");
				}
			}
			public void onSuccess(Object arg0) {
				WindowUtils.hideLoadingAlert();
				Info.display("Success", taxon.getFullName() + " has been saved.");
				hide();
				
				StateChangeEvent event = new StateChangeEvent(StateManager.impl.getWorkingSet(), taxon, null, null);
				event.setCanceled(false);
				
				StateManager.impl.reset(event);
			}
		});
	}

	private void saveInfo() {
		if (taxon.getLevel() >= TaxonLevel.SPECIES)
			taxon.setHybrid(hybrid.getValue().getValue());

		if (level.isEnabled()) {
			int newLevel = level.getValue().getValue();
			if (newLevel == TaxonLevel.SUBPOPULATION) {
				taxon.setTaxonLevel(TaxonLevel.getTaxonLevel(newLevel));
				taxon.setInfratype(null);
			}
			else if (newLevel == Infratype.INFRARANK_TYPE_SUBSPECIES || 
					newLevel == Infratype.INFRARANK_TYPE_VARIETY || 
					newLevel == Infratype.INFRARANK_TYPE_FORMA) {
				taxon.setTaxonLevel(TaxonLevel.getTaxonLevel(TaxonLevel.INFRARANK));
				taxon.setInfratype(Infratype.getInfratype(newLevel));
			} else {
				Debug.println("Infratype set to null for selected level {0}", newLevel);
				taxon.setInfratype(null);
			}
		}

		taxon.setName(name.getValue());
		taxon.setStatus(status.getValue().getValue());
		taxon.setTaxonomicAuthority(taxonomicAuthority.getValue());
		taxon.setInvasive(invasive.getValue());
		taxon.setFeral(feral.getValue());
		
		taxon.correctFullName();
	}
	
	private static class TextValueModelData<T> extends BaseModelData {
		
		private static final long serialVersionUID = 1L;
		
		public TextValueModelData(String text, T value) {
			super();
			set("text", text);
			set("value", value);
		}
		
		public T getValue() {
			return get("value");
		}
		
	}
}
