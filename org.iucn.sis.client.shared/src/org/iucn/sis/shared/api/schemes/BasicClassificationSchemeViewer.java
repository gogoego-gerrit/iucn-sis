package org.iucn.sis.shared.api.schemes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.iucn.sis.client.api.container.SISClientBase;
import org.iucn.sis.client.api.ui.notes.NotesWindow;
import org.iucn.sis.client.api.utils.PagingPanel;
import org.iucn.sis.shared.api.citations.Referenceable;
import org.iucn.sis.shared.api.data.DisplayDataProcessor;
import org.iucn.sis.shared.api.data.TreeData;
import org.iucn.sis.shared.api.data.TreeDataRow;
import org.iucn.sis.shared.api.debug.Debug;
import org.iucn.sis.shared.api.displays.FieldNotes;
import org.iucn.sis.shared.api.models.Notes;
import org.iucn.sis.shared.api.schemes.ClassificationSchemeRowEditorWindow.EditMode;
import org.iucn.sis.shared.api.structures.Structure;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.Style.SortDir;
import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.IconButtonEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.StoreEvent;
import com.extjs.gxt.ui.client.store.StoreListener;
import com.extjs.gxt.ui.client.store.StoreSorter;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.IconButton;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.extjs.gxt.ui.client.widget.toolbar.LabelToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.util.events.ComplexListener;
import com.solertium.util.events.SimpleListener;
import com.solertium.util.extjs.client.WindowUtils;
import com.solertium.util.gwt.ui.DrawsLazily;
import com.solertium.util.gwt.ui.DrawsLazily.DoneDrawingCallback;
import com.solertium.util.portable.PortableAlphanumericComparator;

public class BasicClassificationSchemeViewer extends PagingPanel<ClassificationSchemeModelData> implements ClassificationSchemeViewer {
	
	protected final ListStore<ClassificationSchemeModelData> server;
	protected List<ClassificationSchemeModelData> saved;
	
	protected TreeData treeData;
	protected String description;
	
	//protected LayoutContainer innerContainer;
	protected LayoutContainer displayPanel;
	protected Grid<ClassificationSchemeModelData> grid;
	
	protected boolean hasChanged;
	
	public BasicClassificationSchemeViewer(String description, TreeData treeData) {
		super();
		setPageCount(15);
		getProxy().setSort(false);
		
		this.treeData = treeData;
		this.description = description;
		this.hasChanged = false;
		this.server = new ListStore<ClassificationSchemeModelData>();
		server.setStoreSorter(new StoreSorter<ClassificationSchemeModelData>(
			new ClassificationSchemeModelDataComparator()
		));
	}
	
	public LayoutContainer draw(final boolean isViewOnly) {
		if (displayPanel == null) {
			displayPanel = new LayoutContainer(new FillLayout());
			displayPanel.setSize(900, 800);
			displayPanel.addStyleName("thinFrameBorder");
			
			server.addStoreListener(new StoreListener<ClassificationSchemeModelData>() {
				public void storeAdd(StoreEvent<ClassificationSchemeModelData> se) {
					refresh(new DrawsLazily.DoneDrawingWithNothingToDoCallback());
				}
				@Override
				public void storeRemove(StoreEvent<ClassificationSchemeModelData> se) {
					refresh(new DrawsLazily.DoneDrawingWithNothingToDoCallback());
				}
				@Override
				public void storeUpdate(StoreEvent<ClassificationSchemeModelData> se) {
					refresh(new DrawsLazily.DoneDrawingWithNothingToDoCallback());
				}
			});
		}

		displayPanel.removeAll();
		
		grid = 
			new Grid<ClassificationSchemeModelData>(getStoreInstance(), new ColumnModel(buildColumnConfig(generateDefaultStructure(null))));
		
		final ToolBar gridBar = getLabelPanel(grid, isViewOnly);
		
		grid.setBorders(false);
		grid.setWidth(900);
		grid.setHeight(300);
		grid.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		grid.addListener(Events.RowClick, new Listener<GridEvent<ClassificationSchemeModelData>>() {
			public void handleEvent(GridEvent<ClassificationSchemeModelData> be) {
				ClassificationSchemeModelData selected = be.getModel();
				if (selected != null) {
					gridBar.fireEvent(Events.Change);
					
					editModel(selected, false, isViewOnly);
				}
				else{
					gridBar.setVisible(false);
				}
			}
		});

		final LayoutContainer toolbarContainer = new LayoutContainer();
		toolbarContainer.add(createToolbar(isViewOnly));
		toolbarContainer.add(gridBar);
		
		final LayoutContainer gridContainer = new LayoutContainer(new BorderLayout());
		if (!isViewOnly)
			gridContainer.add(toolbarContainer, new BorderLayoutData(LayoutRegion.NORTH, 50, 50, 50));
		gridContainer.add(grid, new BorderLayoutData(LayoutRegion.CENTER));
		gridContainer.add(getPagingToolbar(), new BorderLayoutData(LayoutRegion.SOUTH, 25, 25, 25));
		
		displayPanel.add(gridContainer);
		
		//This is a sync operation, so layout should be fine
		refresh(new DrawsLazily.DoneDrawingWithNothingToDoCallback());
		
		return displayPanel;
	}
	
	protected void editModel(final ClassificationSchemeModelData model, 
			final boolean addToPagingLoader, final boolean isViewOnly) {
		final ClassificationSchemeRowEditorWindow window = 
			createRowEditorWindow(model, addToPagingLoader, isViewOnly);
		window.setSaveListener(new ComplexListener<ClassificationSchemeModelData>() {
			public void handleEvent(ClassificationSchemeModelData eventData) {
				eventData.updateDisplayableData();
					
				if (addToPagingLoader && !server.contains(eventData))
					server.add(eventData);
				else
					server.update(eventData);
									
				hasChanged = true;
			}
		});
		window.show();
	}
	
	public boolean containsRow(TreeDataRow row) {
		for (ClassificationSchemeModelData model : server.getModels())
			if (row.getDisplayId().equals(model.getSelectedRow().getDisplayId()))
				return true;
		return false;
	}
	
	public ClassificationSchemeRowEditorWindow createRowEditorWindow(ClassificationSchemeModelData model, boolean addToPagingLoader, boolean isViewOnly) {
		return new ClassificationSchemeRowEditorWindow(this, treeData, description, model, 
				addToPagingLoader ? EditMode.NEW : EditMode.EXISTING, isViewOnly);
	}
	
	public ToolBar getLabelPanel(final Grid<ClassificationSchemeModelData> grid, boolean isViewOnly){
		/*
		 * FIXME: I removed a lot of functionality from here, as I just 
		 * don't know how it works and how to hook it up in the database 
		 * appropriately.  Needs to be re-worked.
		 * 
		 * Also, these images will not load because we need to create 
		 * CSS classes for them instead of specifying images.  I think 
		 * there is an IconButton class in our libraries somewhere that 
		 * does take an image, but it's nowhere to be found right now. 
		 */

		final IconButton remove = new IconButton("icon-cross");
		remove.addStyleName("pointerCursor");
		remove.setSize("18px", "18px");
		remove.addSelectionListener(new SelectionListener<IconButtonEvent>() {
			public void componentSelected(IconButtonEvent ce) {
				final ClassificationSchemeModelData model = grid.getSelectionModel().getSelectedItem();
				if (model == null)
					WindowUtils.errorAlert("Please select a row to delete.");
				else {
					WindowUtils.confirmAlert("Delete Confirm", "Are you sure you want to delete this data?", new WindowUtils.SimpleMessageBoxListener() {
						public void onYes() {
							if (model != null) {
								server.remove(model);
							}
						}
					});
				}
			}
		});

		final IconButton refIcon = new IconButton("icon-book");
		refIcon.addStyleName("SIS_iconPanelIcon");
		refIcon.addSelectionListener(new SelectionListener<IconButtonEvent>() {
			public void componentSelected(IconButtonEvent ce) {
				GenericCallback<Object> callback = new GenericCallback<Object>() {
					public void onFailure(Throwable caught) {
						WindowUtils.errorAlert("Error!", "Error committing changes to the "
								+ "server. Ensure you are connected to the server, then try " + "the process again.");
					}

					public void onSuccess(Object result) {
						
					}
				};
				
				final ClassificationSchemeModelData model = grid.getSelectionModel().getSelectedItem();
				if (model == null)
					WindowUtils.errorAlert("Please select a row to add references.");
				else {
					String title = "Add references to " + treeData.getCanonicalName() + 
						" " + model.getSelectedRow().getLabel(); 
				
					SISClientBase.getInstance().onShowReferenceEditor(title, (Referenceable)model, callback, callback);
				
				/*SISClientBase.getInstance().onShowReferenceEditor("Add a references to " + canonicalName + " "
						+ ((ClassificationSchemeEntry)grid.getSelectionModel().getSelectedItem()).getKey(), 
						(ClassificationSchemeEntry)grid.getSelectionModel().getSelectedItem(), 
				new GenericCallback<Object>() {
					public void onFailure(Throwable caught) {
					};

					public void onSuccess(Object result) {
						ArrayList references = (ArrayList) result;
						ReferenceCache.getInstance().addReferences(
								AssessmentCache.impl.getCurrentAssessment().getId(), references);

						ReferenceCache.getInstance().addReferencesToAssessmentAndSave(references,
								canonicalName + "." + ((ClassificationSchemeEntry)grid.getSelectionModel().getSelectedItem()).getKey(), new GenericCallback<Object>() {
							public void onFailure(Throwable caught) {
							}

							public void onSuccess(Object result) {
								refIcon.changeStyle("images/icon-book.png");
							}
						});
					};
				}, 
				new GenericCallback<Object>() {
					public void onFailure(Throwable caught) {};

					public void onSuccess(Object result) {
						ArrayList list = (ArrayList) result;
						for (int i = 0; i < list.size(); i++) {
							AssessmentCache.impl.getCurrentAssessment().removeReference(
									(Reference) list.get(i), canonicalName + "." + ((ClassificationSchemeEntry)grid.getSelectionModel().getSelectedItem()).getKey());
						}

						if (AssessmentCache.impl.getCurrentAssessment().getReferences(
								canonicalName + "." + ((ClassificationSchemeEntry)grid.getSelectionModel().getSelectedItem()).getKey()).size() == 0)
							refIcon.changeStyle("images/icon-book-grey.png");
						// rebuildIconPanel();
					};
				});*/
				}
			}
		});

		

//		final ClickHandler noteListener = new ClickHandler() {
//			public void onClick(ClickEvent event) {
//				String temp = canonicalName + ((ClassificationSchemeEntry)grid.getSelectionModel().getSelectedItem()).getKey();
//				openEditViewNotesPopup(temp, notesImage);
//			}
//		};
		
		
		final IconButton notesImage = new IconButton("icon-note");
		notesImage.addStyleName("SIS_iconPanelIcon");
		notesImage.addSelectionListener(new SelectionListener<IconButtonEvent>() {
			public void componentSelected(IconButtonEvent ce) {
				final ClassificationSchemeModelData model = grid.getSelectionModel().getSelectedItem();
				if (model == null)
					WindowUtils.errorAlert("Please select a row to add references.");
				else if (model.getField() == null)
					WindowUtils.errorAlert("Please save your changes before adding notes.");
				else {
					NotesWindow window = new NotesWindow(new FieldNotes(model.getField()) {
						public void onClose(List<Notes> list) {
							
						}
					});
					window.show();
				}
			}
		});
		
		final ToolBar toolBar = new ToolBar();
		if (!isViewOnly) {
			toolBar.add(remove);
			toolBar.add(new SeparatorToolItem());
		}
		toolBar.add(refIcon);
		toolBar.add(new SeparatorToolItem());
		toolBar.add(notesImage);

		return toolBar;

	}
	
	protected ToolBar createToolbar(final boolean isViewOnly) {
		final Button addClassification = new Button("Quick Add to " + description);
		addClassification.setIconStyle("icon-add");
		addClassification.addSelectionListener(new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				final ClassificationSchemeModelData model = 
					newInstance(generateDefaultStructure(null));
				
				editModel(model, true, isViewOnly);
			}
		});
		
		final Button modClassification = new Button("Add " + description);
		modClassification.setIconStyle("icon-browse-add");
		modClassification.addSelectionListener(new SelectionListener<ButtonEvent>(){
			public void componentSelected(ButtonEvent ce) {
				final Collection<TreeDataRow> selected = new ArrayList<TreeDataRow>();
				for (ClassificationSchemeModelData model : server.getModels())
					selected.add(model.getSelectedRow());
				
				final Window window = WindowUtils.getWindow(true, false, "Add " + description);
				window.setLayout(new FillLayout());
				window.setModal(true);
				window.setScrollMode(Scroll.AUTO);
				if (description != null && !description.equals(""))
					window.setHeading(description);
				
				window.add(TreePanelBuilder.build(new ComplexListener<Set<TreeDataRow>>() {
					public void handleEvent(Set<TreeDataRow> eventData) {
						window.hide();
						
						final List<ClassificationSchemeModelData> models = 
							new ArrayList<ClassificationSchemeModelData>();
						for (TreeDataRow row : eventData) {
							if (!containsRow(row)) {
								ClassificationSchemeModelData model = 
									newInstance(generateDefaultStructure(row));
								model.setSelectedRow(row);
							
								models.add(model);
							}
						}
						
						server.add(models);
						
						hasChanged = true;
					}
				}, new SimpleListener() {
					public void handleEvent() {
						window.hide();
					}
				}, treeData, selected));
				window.setSize(500, 500);
				window.show();
			}
		});

		final ToolBar bar = new ToolBar();
		bar.add(addClassification);
		bar.add(modClassification);
		
		return bar;
	}
	
	public ClassificationSchemeModelData newInstance(Structure structure) {
		return new ClassificationSchemeModelData(structure);
	}

	@Override
	public boolean hasChanged() {
		return hasChanged;
	}

	@Override
	public List<ClassificationSchemeModelData> save(boolean deep) {
		if (deep)
			hasChanged = false;
		saved = server.getModels();
		Debug.println("Classification scheme saved {0} models", saved.size());
		return saved;
	}

	@Override
	public void setData(List<ClassificationSchemeModelData> models) {
		this.saved = models;
		if (saved != null) {
			server.removeAll();
			server.add(models);
		}
	}
	
	public void removeModel(ClassificationSchemeModelData model) {
		server.remove(model);
		
		hasChanged = true;
	}
	
	@Override
	public void addModel(ClassificationSchemeModelData model) {
		server.add(model);
		
		hasChanged = true;
	}
	
	@Override
	public void updateModel(ClassificationSchemeModelData model) {
		server.update(model);
							
		hasChanged = true;
	}
	
	public List<ClassificationSchemeModelData> getModels() {
		return server.getModels();
	}
	
	@Override
	public void revert() {
		setData(saved);
	}
	
	public Structure generateDefaultStructure(TreeDataRow row) {
		return DisplayDataProcessor.processDisplayStructure(treeData.getDefaultStructure());
	}
	
	protected ArrayList<ColumnConfig> buildColumnConfig(Structure<?> str){
		ArrayList<ColumnConfig> cc = new ArrayList<ColumnConfig>();
		cc.add(new ColumnConfig("text", description, 450));

		for (String d : str.extractDescriptions())
			cc.add(new ColumnConfig(d, d, 80));
		
		return cc;
	}
	
	@Override
	protected void getStore(GenericCallback<ListStore<ClassificationSchemeModelData>> callback) {
		server.sort("text", SortDir.ASC);
		callback.onSuccess(server);
	}
	
	@Override
	protected void refreshView() {
		grid.getView().refresh(false);
	}
	
	private class ClassificationSchemeModelDataComparator implements Comparator<Object> {
		
		private final TreeDataRowComparator comparator = 
			new TreeDataRowComparator();
		private final PortableAlphanumericComparator stringComparator = 
			new PortableAlphanumericComparator();
		
		@Override
		public int compare(Object o1, Object o2) {
			if (o1 instanceof ClassificationSchemeModelData && o2 instanceof ClassificationSchemeModelData)
				return compareModels((ClassificationSchemeModelData)o1, (ClassificationSchemeModelData)o2);
			else
				return stringComparator.compare(o1, o2);
		}
		
		private int compareModels(ClassificationSchemeModelData o1, ClassificationSchemeModelData o2) {
			if (o1.getSelectedRow() == null)
				return 1;
			else if (o2.getSelectedRow() == null)
				return -1;
			
			return comparator.compare(o1.getSelectedRow(), o2.getSelectedRow());
		}
		
	}
	
	public static class TreeDataRowComparator implements Comparator<TreeDataRow> {
		
		private final PortableAlphanumericComparator comparator = 
			new PortableAlphanumericComparator();
		
		public int compare(TreeDataRow o1, TreeDataRow o2) {
			//return comparator.compare(o1.getLabel(), o2.getLabel());
			return comparator.compare(o1.getFullLineage(), o2.getFullLineage());
		}
		
	}
	
	

}
