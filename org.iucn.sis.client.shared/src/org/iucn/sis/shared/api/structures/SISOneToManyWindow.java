package org.iucn.sis.shared.api.structures;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.iucn.sis.client.api.assessment.ReferenceableField;
import org.iucn.sis.client.api.caches.AssessmentCache;
import org.iucn.sis.client.api.container.SISClientBase;
import org.iucn.sis.client.api.ui.notes.NotesWindow;
import org.iucn.sis.client.api.utils.BasicWindow;
import org.iucn.sis.shared.api.data.DisplayDataProcessor;
import org.iucn.sis.shared.api.displays.ClassificationScheme;
import org.iucn.sis.shared.api.displays.FieldNotes;
import org.iucn.sis.shared.api.models.Field;
import org.iucn.sis.shared.api.models.Notes;
import org.iucn.sis.shared.api.views.components.DisplayData;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.Style.SortDir;
import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.IconButtonEvent;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.StoreSorter;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.HtmlContainer;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.ListView;
import com.extjs.gxt.ui.client.widget.ListViewSelectionModel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.button.IconButton;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.CenterLayout;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.util.events.SimpleListener;
import com.solertium.util.extjs.client.WindowUtils;
import com.solertium.util.portable.PortableAlphanumericComparator;

public class SISOneToManyWindow extends BasicWindow {
	
	private final DisplayData defaultStructureData;
	
	private final ListView<BaseModelData> view; 
	private final ListStore<BaseModelData> store;
	
	private final Html fullNameDisplay;
	
	private final LayoutContainer center;
	private final LayoutContainer editingArea;
	
	private LayoutContainer completeEditingArea;
	
	private StructureHolder current;
	
	private boolean viewOnly = false;
	
	public SISOneToManyWindow(String heading, DisplayData defaultStructure) {
		super(heading);
		setLayout(new FillLayout());
		setSize(ClassificationScheme.WINDOW_WIDTH, ClassificationScheme.WINDOW_HEIGHT);
		
		this.defaultStructureData = defaultStructure;
		
		this.store = new ListStore<BaseModelData>();
		this.store.setStoreSorter(new StoreSorter<BaseModelData>(new PortableAlphanumericComparator()));
		
		this.view = new ListView<BaseModelData>(store);
		this.view.setSimpleTemplate("<span class=\"{style}\">" +
			"<tpl if=\"saved == true\">Record #{id}</tpl>" +
			"<tpl if=\"saved == false\">New Unsaved Record</tpl>" +
		"</span>");
		//this.view.setSimpleTemplate("<span class=\"{style}\">Record #{id}</span>");
		
		this.editingArea = new LayoutContainer();
		this.editingArea.setLayoutOnChange(true);
		this.editingArea.setScrollMode(Scroll.AUTO);
		
		this.fullNameDisplay = new Html();
		
		this.center = new LayoutContainer(new FillLayout());
		this.center.setLayoutOnChange(true);
		this.center.add(completeEditingArea = createEditingArea());
		
		final ContentPanel listing = new ContentPanel();
		listing.setBodyBorder(false);
		listing.setBorders(false);
		listing.setHeading("Records");
		listing.setLayout(new FillLayout());
		listing.add(createListingArea());
		
		final BorderLayoutData listingArea = 
			new BorderLayoutData(LayoutRegion.WEST, 150, 150, 150);
		listingArea.setCollapsible(true);
		
		final LayoutContainer container = new LayoutContainer(new BorderLayout());
		container.add(listing, listingArea);
		container.add(center, new BorderLayoutData(LayoutRegion.CENTER));
		
		add(container);
	}
	
	public void createLabel(boolean viewOnly) {
		this.viewOnly = viewOnly;
		
		for (BaseModelData model : store.getModels()) {
			StructureHolder holder = model.get("model");
			model.set("id", holder.updateId());
			model.set("saved", holder.isSaved());
		}
		
		view.refresh();
		
		stopEditing();
		
		show();
	}
	
	private void setCurrent(StructureHolder holder) {
		center.removeAll();
		center.add(completeEditingArea);
		
		this.current = holder;
		
		if (holder.isSaved())
			this.fullNameDisplay.setHtml("Record #" + holder.getId());
		else
			this.fullNameDisplay.setHtml("New Unsaved Record");
		
		editingArea.removeAll();
		if (viewOnly)
			editingArea.add(holder.structure.createViewOnlyLabel());
		else
			editingArea.add(holder.structure.createLabel());
	}
	
	private void stopEditing() {
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
			"the record from the list on the left which you would " +
			"like to edit, or click \"Add\" to create a new record.");
		instructions.setBorders(true);
		
		container.add(instructions);
		
		center.removeAll();
		center.add(container);
	}
	
	private Menu buildOptionsMenu() {
		Menu optionsMenu = new Menu();
		
		boolean hasAssessment = AssessmentCache.impl.getCurrentAssessment() != null;
		
		if (hasAssessment) {
			String notesIconStyle;
			Set<Notes> notes = current.getField().getNotes();
			if (notes == null || notes.isEmpty())
				notesIconStyle = ("images/icon-note-grey.png");
			else
				notesIconStyle = ("images/icon-note.png");
			
			MenuItem notesMenu = new MenuItem("Notes");
			notesMenu.setIconStyle(notesIconStyle);
			notesMenu.addSelectionListener(new SelectionListener<MenuEvent>() {
				public void componentSelected(MenuEvent ce) {
					if (current.getField() == null || !(current.getField().getId() > 0))
						WindowUtils.errorAlert("Please save your changes before adding notes.");
					else {
						NotesWindow window = new NotesWindow(new FieldNotes(current.getField()) {
							public void onClose(List<Notes> list) {
								
							}
						});
						window.show();
					}
				}
			});
			optionsMenu.add(notesMenu);
		}
		
		String referencesIconStyle;
		if (current != null && !current.getField().getReference().isEmpty())
			referencesIconStyle = "images/icon-book.png";
		else
			referencesIconStyle = "images/icon-book-grey.png";
		
		MenuItem referenceMenu = new MenuItem("References");
		referenceMenu.setIconStyle(referencesIconStyle);
		referenceMenu.addSelectionListener(new SelectionListener<MenuEvent>() {
			public void componentSelected(MenuEvent ce) {
				GenericCallback<Object> callback = new GenericCallback<Object>() {
					public void onFailure(Throwable caught) {
						WindowUtils.errorAlert("Error saving changes, please try again.");
					}
					public void onSuccess(Object result) {
						
					}
				};
				
				if (current.getField() == null || !(current.getField().getId() > 0))
					WindowUtils.errorAlert("Please save your changes before adding references.");
				else
					SISClientBase.getInstance().onShowReferenceEditor(
						"Add references to " + getHeading() + " Record #" + current.getId(), 
						new ReferenceableField(current.getField()), 
						callback, callback
					);
			}
		});

		optionsMenu.add(referenceMenu);
		
		return optionsMenu;
	}
	
	private LayoutContainer createEditingArea() {
		final ToolBar bar = new ToolBar();
		bar.add(fullNameDisplay);
		bar.add(new FillToolItem());
		bar.add(new IconButton("icon-gear", new SelectionListener<IconButtonEvent>() {
			public void componentSelected(IconButtonEvent ce) {
				buildOptionsMenu().show(ce.getIconButton());
			}
		}));
		
		final ButtonBar bottom = new ButtonBar();
		bottom.setAlignment(HorizontalAlignment.CENTER);
		bottom.setSpacing(10);
		bottom.add(new Button("Done", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				stopEditing();
			}
		}));
		
		final BorderLayout layout = new BorderLayout();
		layout.setContainerStyle("whiteBackground");
		
		final LayoutContainer container = new LayoutContainer(layout);
		container.add(bar, new BorderLayoutData(LayoutRegion.NORTH, 25, 25, 25));
		container.add(editingArea, new BorderLayoutData(LayoutRegion.CENTER));
		container.add(bottom, new BorderLayoutData(LayoutRegion.SOUTH, 25, 25, 25));
		
		return container;
	}
	
	private BaseModelData newModelData(final StructureHolder structureHolder) {
		BaseModelData model = new BaseModelData();
		model.set("model", structureHolder);
		model.set("id", structureHolder.getId());
		model.set("saved", structureHolder.isSaved());
		
		return model;
	}
	
	private LayoutContainer createListingArea() {
		final ListViewSelectionModel<BaseModelData> sm = 
			new ListViewSelectionModel<BaseModelData>();
		sm.setSelectionMode(SelectionMode.SINGLE);
		sm.addSelectionChangedListener(new SelectionChangedListener<BaseModelData>() {
			public void selectionChanged(SelectionChangedEvent<BaseModelData> se) {
				BaseModelData selection = se.getSelectedItem();
				if (selection != null)
					setCurrent((StructureHolder)selection.get("model"));
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
					final StructureHolder holder = selected.get("model");
					WindowUtils.confirmAlert("Confirm", "Are you sure you want to delete this record?", new WindowUtils.SimpleMessageBoxListener() {
						public void onYes() {
							delete(holder, new SimpleListener() {
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
	
	@SuppressWarnings("unchecked")
	public void add() {
		stopEditing();
				
		StructureHolder holder = new StructureHolder(DisplayDataProcessor.processDisplayStructure(defaultStructureData));
				
		BaseModelData model = newModelData(holder);
				
		store.add(model);
		store.sort("id", SortDir.ASC);
				
		setCurrent(holder);
	}
	
	private void delete(final StructureHolder record, final SimpleListener listener) {
		listener.handleEvent();
		stopEditing();
	}
	
	public void clear() {
		store.removeAll();
	}
	
	public DisplayData getDisplayData() {
		return defaultStructureData;
	}
	
	@SuppressWarnings("unchecked")
	public void setData(Field field) {
		clear();
		
		if (field != null)
			for (Field subField : field.getFields())
				store.add(newModelData(new StructureHolder(DisplayDataProcessor.processDisplayStructure(defaultStructureData), subField)));
		
		store.sort("id", SortDir.ASC);
	}
	
	public void setEnabled(boolean isEnabled) {
		for (Iterator<BaseModelData> iter = store.getModels().iterator(); iter.hasNext();) {
			BaseModelData current = iter.next();
			StructureHolder model = current.get("model");
			model.structure.setEnabled(isEnabled);
		}
	}
	
	public List<StructureHolder> getSelected() {
		final List<StructureHolder> list = new ArrayList<StructureHolder>();
		for (BaseModelData data : store.getModels()) {
			StructureHolder holder = data.get("model");
			list.add(holder);
		}
		return list;
	}

	public static class StructureHolder {
		
		private long id;
		
		private Structure<Object> structure;
		private Field field;
		
		public StructureHolder(Structure<Object> structure) {
			this(structure, null);
		}
		
		public StructureHolder(Structure<Object> structure, Field field) {
			this.structure = structure;
			this.field = field;
			
			this.structure.setData(field);
			
			updateId();		
		}
		
		public long updateId() {
			this.id = 0;
			if (field != null)
				id = field.getId();
			
			if (id <= 0)
				id = new Date().getTime();
			
			return id;
		}
		
		public long getId() {
			return id;
		}
		
		public Field getField() {
			return field;
		}
		
		public boolean isSaved() {
			return field != null && field.getId() > 0;
		}
		
		public Structure<Object> getStructure() {
			return structure;
		}
		
		public boolean hasChanged() {
			//FIXME: ????
			return structure.hasChanged(field);
		}
		
		public void setField(Field field) {
			this.field = field;
		}
		
	}
	
}
