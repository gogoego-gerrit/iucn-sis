package org.iucn.sis.client.panels;

import java.util.ArrayList;
import java.util.List;

import org.iucn.sis.client.api.caches.MarkedCache;
import org.iucn.sis.client.api.caches.WorkingSetCache;
import org.iucn.sis.client.api.container.SISClientBase;
import org.iucn.sis.client.api.container.StateChangeEvent;
import org.iucn.sis.client.api.container.StateManager;
import org.iucn.sis.client.api.utils.FormattedDate;
import org.iucn.sis.client.panels.MonkeyNavigator.NavigationChangeEvent;
import org.iucn.sis.client.panels.workingsets.WorkingSetNewWSPanel;
import org.iucn.sis.client.panels.workingsets.WorkingSetSubscriber;
import org.iucn.sis.client.tabs.WorkingSetPage;
import org.iucn.sis.shared.api.debug.Debug;
import org.iucn.sis.shared.api.models.Taxon;
import org.iucn.sis.shared.api.models.WorkingSet;

import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.data.ModelKeyProvider;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.IconButtonEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.GroupingStore;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.IconButton;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.grid.GridGroupRenderer;
import com.extjs.gxt.ui.client.widget.grid.GridView;
import com.extjs.gxt.ui.client.widget.grid.GroupColumnData;
import com.extjs.gxt.ui.client.widget.grid.GroupingView;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.FlexTable;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.util.events.ComplexListener;
import com.solertium.util.events.SimpleListener;
import com.solertium.util.extjs.client.WindowUtils;
import com.solertium.util.gwt.ui.DrawsLazily;

public class WorkingSetMonkeyNavigatorPanel extends GridPagingMonkeyNavigatorPanel<WorkingSet> {
	
	private WorkingSet curNavWorkingSet;
	
	public WorkingSetMonkeyNavigatorPanel() {
		super();
		setHeading("Working Sets");
	}
	
	protected ListStore<NavigationModelData<WorkingSet>> getStoreInstance() {
		GroupingStore<NavigationModelData<WorkingSet>> store = 
			new GroupingStore<NavigationModelData<WorkingSet>>(getLoader());
		store.groupBy("ownerid");
		
		return store;
	}
	
	protected void onSelectionChanged(PagingMonkeyNavigatorPanel.NavigationModelData<WorkingSet> model) {
		MonkeyNavigator.NavigationChangeEvent<WorkingSet> e = 
			new NavigationChangeEvent<WorkingSet>(model.getModel());
		
		fireEvent(Events.SelectionChange, e);
	}
	
	@Override
	protected void mark(NavigationModelData<WorkingSet> model, String color) {
		if (model == null || model.getModel() == null)
			return;
		
		final Integer workingSetID = getSelected().getId();
		
		MarkedCache.impl.markWorkingSet(workingSetID, color);
		
		refreshView();
	}
	
	@Override
	protected GridView getView() {
		final String ownerID = SISClientBase.currentUser.getId() + "";
		GroupingView view = new GroupingView();
		view.setShowGroupedColumn(false);
		view.setEmptyText("No Working Sets");
		view.setGroupRenderer(new GridGroupRenderer() {
			public String render(GroupColumnData data) {
				if (ownerID.equals(data.group)) 
					return "My Working Sets (" + data.models.size() + ")";
				else
					return "Subscribed Working Sets (" + data.models.size() + ")";
			}
		});
		
		return view;
	}
	
	protected ColumnModel getColumnModel() {
		final List<ColumnConfig> list = new ArrayList<ColumnConfig>();

		ColumnConfig display = new ColumnConfig("name", "Name", 100);
		display.setRenderer(new GridCellRenderer<BaseModelData>() {
			@Override
			public Object render(BaseModelData model, String property,
					ColumnData config, int rowIndex, int colIndex,
					ListStore<BaseModelData> store, Grid<BaseModelData> grid) {
				WorkingSet ws = model.get("workingSet");
				String style;
				if (ws == null)
					style = MarkedCache.NONE;
				else
					style = MarkedCache.impl.getWorkingSetStyle(ws.getId());
				
				return "<span class=\"" + style + "\">" + model.get(property) + "</span>";
			}
		});
		
		ColumnConfig owner = new ColumnConfig("owner", "Owner", 100);
		
		ColumnConfig ownerid = new ColumnConfig("ownerid", "Owner ID", 100);
		
		list.add(display);
		list.add(owner);
		list.add(ownerid);
		
		return new ColumnModel(list);
	}
	
	public void refresh(final WorkingSet curNavWorkingSet) {
		this.curNavWorkingSet = curNavWorkingSet;
		
		refresh(new DrawsLazily.DoneDrawingWithNothingToDoCallback());
	}
	
	@Override
	protected void onLoad() {
		super.onLoad();
		
		NavigationModelData<WorkingSet> selection;
		if (curNavWorkingSet == null)
			selection = getProxy().getStore().getAt(0);
		else
			selection = getProxy().getStore().findModel("" + curNavWorkingSet.getId());
		
		Debug.println("Selected working set from nav is {0}, found {1}", curNavWorkingSet, selection);
		if (selection != null) {
			
			((NavigationGridSelectionModel<WorkingSet>)grid.getSelectionModel()).
				highlight(selection);
			
			DeferredCommand.addPause();
			DeferredCommand.addCommand(new Command() {
				public void execute() {
					//grid.getView().focusRow(grid.getStore().indexOf(selection));
				}
			});
		}
	}
	
	@Override
	protected void getStore(GenericCallback<ListStore<NavigationModelData<WorkingSet>>> callback) {
		final GroupingStore<NavigationModelData<WorkingSet>> store = 
			new GroupingStore<NavigationModelData<WorkingSet>>();
		//final ListStore<NavigationModelData<WorkingSet>> store = new ListStore<NavigationModelData<WorkingSet>>();
		store.setKeyProvider(new ModelKeyProvider<NavigationModelData<WorkingSet>>() {
			public String getKey(NavigationModelData<WorkingSet> model) {
				if (model.getModel() == null)
					return "-1";
				else
					return Integer.toString(model.getModel().getId());
			}
		});
		
		final int myOwnerID = SISClientBase.currentUser.getId();
		
		NavigationModelData<WorkingSet> noneModel = new NavigationModelData<WorkingSet>(null);
		noneModel.set("name", "None");
		noneModel.set("owner", "");
		noneModel.set("ownerid", myOwnerID + "");
		
		store.add(noneModel);
		
		for (WorkingSet cur : WorkingSetCache.impl.getWorkingSets().values()) {
			NavigationModelData<WorkingSet> model = new NavigationModelData<WorkingSet>(cur);
			model.set("name", cur.getName());
			if (myOwnerID == cur.getCreator().getId()) {
				model.set("owner", "");
				model.set("ownerid", myOwnerID + "");
			}
			else {
				model.set("owner", " (" + cur.getCreator().getDisplayableName() + ")");
				model.set("ownerid", "-1");
			}
			
			store.add(model);
		}
	
		store.groupBy("ownerid");
		
		callback.onSuccess(store);
	}
	
	@Override
	protected void setupToolbar() {
		final IconButton goToSet = createIconButton("icon-go-jump", "Open Working Set", new SelectionListener<IconButtonEvent>() {
			public void componentSelected(IconButtonEvent ce) {
				open(grid.getSelectionModel().getSelectedItem());
			}
		});
		
		addTool(createIconButton("icon-add", "Add Working Set", new SelectionListener<IconButtonEvent>() {
			public void componentSelected(IconButtonEvent ce) {
				final MenuItem newWS = new MenuItem("Create New Working Set");
				newWS.addSelectionListener(new SelectionListener<MenuEvent>() {
					public void componentSelected(MenuEvent ce) {
						final Window window = new Window();
						window.setSize(700, 700);
						window.setHeading("Add New Working Set");
						window.setLayout(new FillLayout());
						
						WorkingSetNewWSPanel panel = new WorkingSetNewWSPanel();
						panel.setAfterSaveListener(new ComplexListener<WorkingSet>() {
							public void handleEvent(WorkingSet eventData) {
								window.hide();
								
								StateChangeEvent event = new StateChangeEvent(eventData, null, null, null);
								event.setCanceled(false);
								event.setUrl(WorkingSetPage.URL_TAXA);
								
								StateManager.impl.setState(event);
							}
						});
						panel.setCloseListener(new ComplexListener<WorkingSet>() {
							public void handleEvent(WorkingSet eventData) {
								window.hide();
								StateManager.impl.setWorkingSet(eventData);
							}
						});
						panel.setCancelListener(new SimpleListener() {
							public void handleEvent() {
								window.hide();	
							}
						});
						panel.setSaveNewListener(new ComplexListener<WorkingSet>() {
							public void handleEvent(WorkingSet eventData) {
								window.hide();
								
								StateChangeEvent event = new StateChangeEvent(eventData, null, null, null);
								event.setCanceled(false);
								event.setUrl(WorkingSetPage.URL_EDIT);
								
								StateManager.impl.setState(event);
							}
						});
						panel.refresh();
						
						window.add(panel);
						window.show();
					}
				});
				
				final MenuItem subscribe = new MenuItem("Subscribe to Existing Working Set");
				subscribe.addSelectionListener(new SelectionListener<MenuEvent>() {
					public void componentSelected(MenuEvent ce) {
						final WorkingSetSubscriber panel = new WorkingSetSubscriber();
						
						Window window = new Window();
						window.setLayout(new FillLayout());
						window.setSize(700, 700);
						window.setHeading("Subscribe to Working Set");
						window.addListener(Events.Show, new Listener<BaseEvent>() {
							public void handleEvent(BaseEvent be) {
								panel.refresh();
							}
						});
						window.add(panel);
						window.show();
					}
				});
				
				final Menu menu = new Menu();
				menu.add(newWS);
				menu.add(subscribe);
				
				menu.show(ce.getIconButton());
			}
		}));
		addTool(new SeparatorToolItem());
		addTool(createIconButton("icon-information", "Working Set Details", new SelectionListener<IconButtonEvent>() {
			public void componentSelected(IconButtonEvent ce) {
				if (hasSelection())
					WindowUtils.errorAlert("Please select a working set.");
				
				final WorkingSet ws = getSelected();
				if (ws == null)
					return;
					
				final Window s = WindowUtils.getWindow(false, true, ws.getWorkingSetName());
				s.setLayout(new FillLayout());
				s.setTitle(ws.getWorkingSetName());
				s.setSize(600, 400);
				
				final FlexTable table = new FlexTable();
				table.setCellSpacing(4);

				// IF THERE ARE SPECIES TO GET
				if (ws.getSpeciesIDs().size() > 0) {
					WorkingSetCache.impl.fetchTaxaForWorkingSet(ws, new GenericCallback<List<Taxon>>() {
						public void onFailure(Throwable caught) {
							table.setHTML(0, 0, "<b>Manager: </b>");
							table.setHTML(0, 1, ws.getCreator().getUsername());
							table.setHTML(1, 0, "<b>Date: </b>");
							table.setHTML(1, 1, FormattedDate.impl.getDate(ws.getCreatedDate()));
							table.setHTML(2, 0, "<b>Number of Species: </b>");
							table.setHTML(2, 1, "" + ws.getSpeciesIDs().size());
							table.setHTML(3, 0, "<b>Description: </b>");
							table.setHTML(3, 1, ws.getDescription());
							s.layout();
						};

						public void onSuccess(List<Taxon> arg0) {
							table.setHTML(0, 0, "<b>Manager: </b>");
							table.setHTML(0, 1, ws.getCreator().getUsername());
							table.setHTML(1, 0, "<b>Date: </b>");
							table.setHTML(1, 1, FormattedDate.impl.getDate(ws.getCreatedDate()));
							table.setHTML(2, 0, "<b>Number of Species: </b>");
							table.setHTML(2, 1, "" + ws.getSpeciesIDs().size());

							String species = "";
							for (Taxon taxon : arg0) {
								species += taxon.getFullName() + ", ";
							}
							if (species.length() > 0) {
								table.setHTML(3, 0, "<b>Species: </b>");
								table.setHTML(3, 1, species.substring(0, species.length() - 2));
								table.setHTML(4, 0, "<b>Description: </b>");
								table.setHTML(4, 1, ws.getDescription());
							} else {
								table.setHTML(3, 0, "<b>Description: </b>");
								table.setHTML(3, 1, ws.getDescription());
							}
							s.add(table);
							s.show();
						};
					});
				}
				// ELSE LOAD NO SPECIES
				else {
					table.setHTML(0, 0, "There are no species in the " + ws.getWorkingSetName() + " working set.");
					s.add(table);
					
					s.show();
				}
				
			}
		}));
		addTool(createIconButton("icon-image", "Download Working Set", new SelectionListener<IconButtonEvent>() {
			public void componentSelected(IconButtonEvent ce) {
				// TODO Auto-generated method stub
				
			}
		}));
		addTool(new SeparatorToolItem());
		addTool(goToSet);
	}
	
	@Override
	protected void open(NavigationModelData<WorkingSet> model) {
		if (hasSelection()) {
			navigate(getSelected(), null, null);
		}	
	}

}
