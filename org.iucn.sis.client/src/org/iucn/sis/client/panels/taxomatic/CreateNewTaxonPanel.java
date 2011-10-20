package org.iucn.sis.client.panels.taxomatic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import org.iucn.sis.client.api.caches.WorkingSetCache;
import org.iucn.sis.client.api.ui.models.workingset.WSModel;
import org.iucn.sis.client.panels.taxomatic.TaxomaticUtils.TaxonomyException;
import org.iucn.sis.shared.api.models.Infratype;
import org.iucn.sis.shared.api.models.Taxon;
import org.iucn.sis.shared.api.models.TaxonLevel;
import org.iucn.sis.shared.api.models.TaxonStatus;
import org.iucn.sis.shared.api.models.WorkingSet;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.Params;
import com.extjs.gxt.ui.client.widget.CheckBoxListView;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.InfoConfig;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.google.gwt.user.client.ui.HTML;
import com.solertium.lwxml.shared.GWTResponseException;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.util.extjs.client.FormBuilder;
import com.solertium.util.extjs.client.WindowUtils;

public class CreateNewTaxonPanel extends TaxomaticWindow {

	private final Taxon parentNode;
	
	private FormPanel form;
	private TextField<String> name;
	private TextField<String> taxonomicAuthority;
	private ComboBox<ComboOption> rank;
	private ComboBox<StatusOption> status;
	private CheckBox hybrid;
	
	public CreateNewTaxonPanel(Taxon parent) {
		super("Create New Taxon", "icon-new-document");
		this.parentNode = parent;
		
		setSize(350, 250);

		addButton(new Button("Save and Close", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				performSave();
			}
		}));

		addButton(new Button("Cancel", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				hide();
			}
		}));
	}

	private void addToWorkingSet(final Taxon newNode) {
		if (newNode.getLevel() >= TaxonLevel.SPECIES) {
			WindowUtils.confirmAlert("Add To Working Set", "Would you like to add this taxon to a working set?",
					new WindowUtils.SimpleMessageBoxListener() {
				public void onYes() {
					createWorkingSetSelector(newNode);
				}
			});
		}
	}

	private void createWorkingSetSelector(final Taxon newNode) {
		final Window window = WindowUtils.newWindow("Choose Working Set");
		window.setSize(500, 400);
		window.setScrollMode(Scroll.AUTO);
		window.setLayout(new FillLayout());
		
		final HTML message = new HTML("Choose the working set you would like to add " + newNode.getFullName() + " to.");
		
		final ListStore<WSModel> store = new ListStore<WSModel>();
		
		final List<WorkingSet> workingSets = new ArrayList<WorkingSet>(WorkingSetCache.impl.getWorkingSets().values());
		Collections.sort(workingSets, new WorkingSetCache.WorkingSetComparator());
		for (WorkingSet curWS : workingSets)
			store.add(new WSModel(curWS));
		
		final CheckBoxListView<WSModel> list = new CheckBoxListView<WSModel>();
		list.setDisplayProperty("name");
		list.setStore(store);

		final LayoutContainer container = new LayoutContainer(new BorderLayout());
		container.add(message, new BorderLayoutData(LayoutRegion.NORTH, 40, 40, 40));
		container.add(list, new BorderLayoutData(LayoutRegion.CENTER));
		
		window.add(container);

		window.addButton(newButton("Add to Selected", "icon-add", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				final List<Taxon> taxonToAdd = new ArrayList<Taxon>();
				taxonToAdd.add(newNode);
				
				for (final WSModel item : list.getChecked()) {
					WorkingSetCache.impl.editTaxaInWorkingSet(item.getWorkingSet(), taxonToAdd, null, new GenericCallback<String>() {
						public void onSuccess(String result) {
							Info.display("Success!", newNode.getFullName() + " was added to the set {0}.", item.getWorkingSet()
									.getWorkingSetName());
						}
						public void onFailure(Throwable caught) {
							WindowUtils.errorAlert("Failed to add taxon to working set.  Please use the taxon manager for the working set(s) you wish update.");
						}
					});
				}

				window.hide();
			}
		}));
		window.addButton(newButton("Close", "icon-cancel", new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				window.hide();
			}
		}));

		window.show();
	}
	
	private Button newButton(String name, String iconStyle, SelectionListener<ButtonEvent> listener) {
		Button button = new Button(name, listener);
		button.setIconStyle(iconStyle);
		
		return button;
	}
	
	public void show() {
		draw();
		super.show();
	}

	private void draw() {
		name = FormBuilder.createTextField("name", null, "Name", true);
		name.getMessages().setBlankText("Name must be specified.");
		
		taxonomicAuthority = FormBuilder.createTextField("authority", null, "Authority", false);
		
		ListStore<StatusOption> statusStore = new ListStore<StatusOption>();
		for (Entry<String, String> entry : TaxonStatus.displayableStatus.entrySet())
			statusStore.add(new StatusOption(entry.getValue(), entry.getKey()));
		if (parentNode.getLevel() >= TaxonLevel.GENUS)
			statusStore.add(new StatusOption("Undescribed", "U"));
		status = new ComboBox<StatusOption>();
		status.setTriggerAction(TriggerAction.ALL);
		status.setFieldLabel("Status");
		status.setAllowBlank(false);
		status.setForceSelection(true);
		status.setStore(statusStore);
		status.setValue(statusStore.getAt(0));
		
		rank = new ComboBox<ComboOption>();
		rank.setTriggerAction(TriggerAction.ALL);
		rank.setFieldLabel("Rank");
		rank.setAllowBlank(false);
		rank.setForceSelection(true);
		
		hybrid = new CheckBox();
		hybrid.setName("hybrid");
		hybrid.setFieldLabel(" Hybrid");
		hybrid.setValue(Boolean.FALSE);
		
		ListStore<ComboOption> rankStore = new ListStore<ComboOption>();
		if (parentNode.getLevel() == TaxonLevel.SPECIES) {
			rankStore.add(new ComboOption("Subspecies", TaxonLevel.INFRARANK, Infratype.INFRARANK_TYPE_SUBSPECIES));
			if (!parentNode.getFootprintAsString().contains("ANIMALIA"))
				rankStore.add(new ComboOption("Variety", TaxonLevel.INFRARANK, Infratype.INFRARANK_TYPE_VARIETY));
			rankStore.add(new ComboOption("Forma", TaxonLevel.INFRARANK, Infratype.INFRARANK_TYPE_FORMA));
			rankStore.add(new ComboOption("Subpopulation", TaxonLevel.SUBPOPULATION));
			rank.setEnabled(true);
			hybrid.setVisible(true);
		} else {
			if (parentNode.getLevel() == TaxonLevel.INFRARANK)
				rankStore.add(new ComboOption(Taxon.getDisplayableLevel(TaxonLevel.INFRARANK_SUBPOPULATION),
						TaxonLevel.INFRARANK_SUBPOPULATION));
			else
				rankStore.add(new ComboOption(Taxon.getDisplayableLevel(parentNode.getLevel() + 1), 
								(parentNode.getLevel() + 1)));

			rank.setEnabled(false);
			hybrid.setVisible(false);
		}
		rank.setStore(rankStore);
		rank.setValue(rankStore.getAt(0));
		
		form = new FormPanel();
		form.setLayout(new FormLayout());
		form.setHeaderVisible(false);
		form.setBodyBorder(false);
		form.setBorders(false);
		form.add(name);
		form.add(taxonomicAuthority);
		form.add(rank);
		form.add(status);
		form.add(hybrid);
		

		add(form);
	}

	private void performSave() {
		if (!form.isValid())
			return;
		
		int level = rank.getValue().getLevel();
		Integer infraType = rank.getValue().getInfrarank();

		if (level >= TaxonLevel.SPECIES && (taxonomicAuthority.getValue() == null || taxonomicAuthority.getValue().equals(""))) {
			taxonomicAuthority.markInvalid("A taxonomic authority must be specified.");
			return;
		}		

		WindowUtils.showLoadingAlert("Saving taxon...");

		final Taxon newNode = Taxon.createNode(0, name.getValue(), level, hybrid.getValue()); 
		newNode.setParent(parentNode);
		newNode.setStatus(status.getValue().getValue());
		newNode.setTaxonomicAuthority(taxonomicAuthority.getValue());
		if (infraType != null)
			newNode.setInfratype(Infratype.getInfratype(infraType, newNode));
		newNode.correctFullName();
		
		/*
		 * Must do this because the footprint may not be 
		 * downloaded to the client at the model level, 
		 * which causes incomplete footprint. 
		 */
		String[] newFootprint = new String[parentNode.getFootprint().length+1];
		int index = 0;
		for (String entry : parentNode.getFootprint())
			newFootprint[index++] = entry;
		newFootprint[index] = newNode.getName();
		
		newNode.setFootprint(newFootprint);

		TaxomaticUtils.impl.createNewTaxon(newNode, parentNode, new GenericCallback<Taxon>() {
			public void onFailure(Throwable caught) {
				WindowUtils.hideLoadingAlert();
				if (caught instanceof TaxonomyException) {
					WindowUtils.errorAlert(caught.getMessage());
				}
				else if (caught instanceof GWTResponseException && ((GWTResponseException)caught).getCode() == 423)
					WindowUtils.errorAlert("Taxomatic In Use", "Sorry, but another " +
							"taxomatic operation is currently running. Please try " +
						"again later!");
				else
					WindowUtils.errorAlert("Failure!", "New node " + newNode.getFullName() + " failed to save."
							+ " Either you have a bad connection to your server, or there "
							+ "already exists a node with the same name.");
			}
			public void onSuccess(Taxon arg0) {
				WindowUtils.hideLoadingAlert();
				Info.display(new InfoConfig("Taxon Created", "New taxon {0} was created successfully!", new Params(
						newNode.getFullName())));
				//TaxonomyCache.impl.evictPaths();
				//ClientUIContainer.bodyContainer.tabManager.panelManager.taxonomicSummaryPanel.update(arg0.getId());
				//TaxonomyCache.impl.setCurrentTaxon(arg0);
				//StateManager.impl.setState(arg0, null);
				BaseEvent be = new BaseEvent(newNode);
				be.setCancelled(false);
				fireEvent(Events.StateChange, be);
				hide();
				addToWorkingSet(newNode);
			}
		});
	}
	
	private static class StatusOption extends BaseModelData {
		
		private static final long serialVersionUID = 1L;
		
		public StatusOption(String text, String value) {
			super();
			set("text", text);
			set("value", value);
		}
		
		public String getValue() {
			return get("value");
		}
		
	}
	
	private static class ComboOption extends BaseModelData {
		
		private static final long serialVersionUID = 1L;
		
		public ComboOption(String text, int level) {
			this(text, level, null);
		}
		
		public ComboOption(String text, int level, Integer infrarank) {
			super();
			set("text", text);
			set("value", text);
			set("level", level);
			set("infrarank", infrarank);
		}
		
		public int getLevel() {
			return get("level");
		}
		
		public Integer getInfrarank() {
			return get("infrarank");
		}
		
	}
	
}
