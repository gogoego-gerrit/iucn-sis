package org.iucn.sis.client.panels;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
import com.solertium.util.portable.PortableAlphanumericComparator;

public class WorkingSetMonkeyNavigatorPanel extends GridNonPagingMonkeyNavigatorPanel<WorkingSet> {
	
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
	
	protected void onSelectionChanged(NavigationModelData<WorkingSet> model) {
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
	protected String getEmptyText() {
		return "No Working Sets";
	}
	
	protected ColumnModel getColumnModel() {
		final List<ColumnConfig> list = new ArrayList<ColumnConfig>();

		ColumnConfig display = new ColumnConfig("name", "Name", 100);
		display.setRenderer(new GridCellRenderer<NavigationModelData<WorkingSet>>() {
			@Override
			public Object render(NavigationModelData<WorkingSet> model, String property,
					ColumnData config, int rowIndex, int colIndex,
					ListStore<NavigationModelData<WorkingSet>> store, Grid<NavigationModelData<WorkingSet>> grid) {
				Boolean header = model.get("header");
				if (header == null)
					header = Boolean.FALSE;
				WorkingSet ws = model.getModel();
				String style;
				String value;
				if (ws == null) {
					style = header ? "monkey_navigation_section_header" : MarkedCache.NONE;
					value = model.get(property);
				}
				else {
					Boolean mine = model.get("mine");
					if (mine == null)
						mine = Boolean.FALSE;
					style = MarkedCache.impl.getWorkingSetStyle(ws.getId());
					value = ws.getName() + (mine ? "" : " via " + ws.getCreator().getDisplayableName());
				}
				return "<div class=\"" + style + "\">" + value + "</div>";
			}
		});
		
		list.add(display);
		
		return new ColumnModel(list);
	}
	
	public void refresh(final WorkingSet curNavWorkingSet) {
		this.curNavWorkingSet = curNavWorkingSet;
		
		refresh(new DrawsLazily.DoneDrawingCallback() {
			public void isDrawn() {
				DeferredCommand.addCommand(new Command() {
					public void execute() {
						setSelection(curNavWorkingSet);
					}
				});
			}
		});
	}
	
	@Override
	public void setSelection(WorkingSet navigationModel) {
		final NavigationModelData<WorkingSet> selection;
		if (curNavWorkingSet == null)
			selection = getProxy().getStore().findModel("-1");
		else
			selection = getProxy().getStore().findModel("" + curNavWorkingSet.getId());
		
		Debug.println("Selected working set from nav is {0}, found {1}", curNavWorkingSet, selection);
		if (selection != null) {
			((NavigationGridSelectionModel<WorkingSet>)grid.getSelectionModel()).
				highlight(selection);
			
			DeferredCommand.addPause();
			DeferredCommand.addCommand(new Command() {
				public void execute() {
					grid.getView().focusRow(grid.getStore().indexOf(selection));
				}
			});
		}
	}
	
	@Override
	protected void onLoad() {
		super.onLoad();
		
		setSelection(curNavWorkingSet);
	}
	
	@Override
	protected void getStore(GenericCallback<ListStore<NavigationModelData<WorkingSet>>> callback) {
		final ListStore<NavigationModelData<WorkingSet>> store = new ListStore<NavigationModelData<WorkingSet>>();
		store.setKeyProvider(new ModelKeyProvider<NavigationModelData<WorkingSet>>() {
			public String getKey(NavigationModelData<WorkingSet> model) {
				if (model.getModel() == null)
					if (model.get("none") != null)
						return "-1";
					else
						return null;
				else
					return Integer.toString(model.getModel().getId());
			}
		});
		
		final int myOwnerID = SISClientBase.currentUser.getId();
		
		NavigationModelData<WorkingSet> noneModel = new NavigationModelData<WorkingSet>(null);
		noneModel.set("name", "None");
		noneModel.set("none", Boolean.TRUE);
		noneModel.set("header", Boolean.FALSE);
		noneModel.set("mine", Boolean.FALSE);
		
		store.add(noneModel);
		
		final List<WorkingSet> list = new ArrayList<WorkingSet>(WorkingSetCache.impl.getWorkingSets().values());
		Collections.sort(list, new WorkingSetComparator());
		
		int currentCreator = -1;
		NavigationModelData<WorkingSet> currentHeader = null;
		int groupCount = 0;
		
		int size = list.size();
		
		for (WorkingSet cur : list) {
			if (cur.getCreator().getId() != currentCreator) {
				if (currentHeader != null)
					updateHeaderCount(currentHeader, groupCount, size);
				
				currentCreator = cur.getCreator().getId();
				
				NavigationModelData<WorkingSet> model = new NavigationModelData<WorkingSet>(null);
				model.set("header", Boolean.TRUE);
				model.set("name", myOwnerID == currentCreator ? "My Working Sets" : "Subscribed Working Sets");
				
				store.add(model);
				
				currentHeader = model;
				groupCount = 0;
			}
			
			NavigationModelData<WorkingSet> model = new NavigationModelData<WorkingSet>(cur);
			model.set("name", cur.getName());
			model.set("header", Boolean.FALSE);
			model.set("mine", cur.getCreator().getId() == myOwnerID);
			
			store.add(model);
			
			groupCount++;
		}
		
		updateHeaderCount(currentHeader, groupCount, size);
		
		callback.onSuccess(store);
	}
	
	private void updateHeaderCount(NavigationModelData<WorkingSet> header, int count, int size) {
		if (header != null) {
			String name = header.get("name");
			name += " (" + count + "/" + size + ")";
			header.set("name", name);
		}
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
	
	private static class WorkingSetComparator implements Comparator<WorkingSet> {
		
		private final PortableAlphanumericComparator comparator;
		private final int myOwnerID;
		
		public WorkingSetComparator() {
			comparator = new PortableAlphanumericComparator();
			myOwnerID = SISClientBase.currentUser.getId();
		}
		
		@Override
		public int compare(WorkingSet o1, WorkingSet o2) {
			int o1Points = 0;
			int o2Points = 0;
			
			String u1 = o1.getCreator().getDisplayableName();
			String u2 = o2.getCreator().getDisplayableName();
			
			Integer i1 = o1.getCreator().getId();
			if (i1.intValue() == myOwnerID)
				i1 = -1;
			Integer i2 = o2.getCreator().getId();
			if (i2.intValue() == myOwnerID)
				i2 = -1;
				
			String ws1 = o1.getName();
			String ws2 = o2.getName();
			
			int result = comparator.compare(u1, u2);
			if (result > 0)
				o2Points++;
			else
				o1Points++;
			
			result = i1.compareTo(i2);
			if (result > 0)
				o2Points++;
			else
				o1Points++;
			
			result = comparator.compare(ws1, ws2);
			if (result > 0)
				o2Points++;
			else
				o1Points++;
			
			result = o2Points - o1Points;
		
			if (result > 0)
				return 1;
			else if (result < 0)
				return -1;
			else
				return 0;
		}
		
	}

}
