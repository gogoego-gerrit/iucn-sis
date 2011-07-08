package org.iucn.sis.client.panels.workingsets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.iucn.sis.client.api.caches.TaxonomyCache;
import org.iucn.sis.client.api.caches.WorkingSetCache;
import org.iucn.sis.client.api.container.StateManager;
import org.iucn.sis.client.panels.utils.RefreshLayoutContainer;
import org.iucn.sis.shared.api.models.Taxon;
import org.iucn.sis.shared.api.models.TaxonLevel;
import org.iucn.sis.shared.api.models.WorkingSet;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.binder.DataListBinder;
import com.extjs.gxt.ui.client.data.BaseModel;
import com.extjs.gxt.ui.client.data.ModelStringProvider;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.CheckChangedEvent;
import com.extjs.gxt.ui.client.event.CheckChangedListener;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.store.StoreFilter;
import com.extjs.gxt.ui.client.store.StoreSorter;
import com.extjs.gxt.ui.client.widget.DataList;
import com.extjs.gxt.ui.client.widget.DataListItem;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.InfoConfig;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.toolbar.PagingToolBar;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.lwxml.shared.utils.ArrayUtils;
import com.solertium.util.extjs.client.GenericPagingLoader;
import com.solertium.util.extjs.client.ViewerFilterTextBox;
import com.solertium.util.portable.PortableAlphanumericComparator;

@SuppressWarnings("deprecation")
public class WorkingSetTaxaList extends RefreshLayoutContainer {

	public static class TaxaData extends BaseModel {
		private static final long serialVersionUID = 1L;
		
		public final static String FAMILY = "family";
		public final static String GENUS = "genus";
		public final static String FULLNAME = "fullname";
		public final static String ROOT = "root";
		public final static String ORDER = "order";

		/**
		 * 
		 * @param name
		 * @param type
		 *            - either family, genus, fullname, root
		 * @param id
		 *            - either the taxon node id if fullname, or the kingdom if
		 *            family or genus, null if root
		 * 
		 */
		public TaxaData(String name, String type, String id, String childIds) {
			set("name", name);
			set("type", type);
			set("id", id);
			set("childIDS", childIds);

			if (type.equalsIgnoreCase(FULLNAME) || type.equalsIgnoreCase(ROOT)) {
				set("display", name);
			} else {
				int numchildren = childIds.split(",").length;
				set("display", name + " -- " + numchildren + " species/infraspecies");
			}

		}

		public String getChildIDS() {
			return get("childIDS");
		}

		public String getID() {
			return get("id");
		}

		public String getName() {
			return get("name");
		}

		public String getType() {
			return get("type");
		}
	}

	public class WorkingSetTaxonPagingLoader extends GenericPagingLoader<TaxaData> {

	}

	private final String DATA_KEY = "dataKey";

	private DataList taxaList = null;
	private ToolBar toolbar = null;
	private WorkingSet recentWorkingSet = null;
	private Button selectedFilter = null;

	private Button filterByOrder = null;
	private Button filterByFamily = null;
	private Button filterByGenus = null;
	private Button filterBySpecies = null;

	private Button numberSpecies = null;
	private ListStore<TaxaData> store = null;
	private StoreSorter<TaxaData> sorter = null;
	private ViewerFilterTextBox<TaxaData> filterTextBox = null;
	private DataListBinder<TaxaData> storeBinder = null;
	private StoreFilter<TaxaData> filter = null;

	private PagingToolBar pagingBar = null;
	private WorkingSetTaxonPagingLoader pagingLoader = null;

	private CheckChangedListener<TaxaData> checkListener = null;
	private boolean checked = false;
	private boolean jumpToInToolbar = false;

	private boolean filteredBySpecies = false;

	public WorkingSetTaxaList(boolean checked) {
		this(checked, null);
	}

	public WorkingSetTaxaList(boolean checked, CheckChangedListener checkListener) {
		this.checkListener = checkListener;

		taxaList = new DataList();
		setLayoutOnChange(true);

		this.checked = checked;
		taxaList.setCheckable(checked);

		toolbar = new ToolBar();
		build();
	}

	@Override
	public void addListener(EventType eventType, Listener<? extends BaseEvent> listener) {
		taxaList.addListener(eventType, listener);
	}

	private void build() {
		setLayout(new RowLayout(Orientation.VERTICAL));

		buildFilter();
		add(filterTextBox, new RowData(1d, 25));

		buildToolbar();
		add(toolbar, new RowData(1d, 25));

		buildList();
		add(taxaList, new RowData(1d, -1));
		add(pagingBar, new RowData(1d, -1));

		if (checked) {
			ButtonBar buttons = buildButtons();
			add(buttons, new RowData(1d, -1));
		}

	}

	private ButtonBar buildButtons() {
		ButtonBar buttons = new ButtonBar();
		buttons.setBorders(true);
		Button selectAll = new Button("Select All", new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				selectAll();
			}

		});

		Button deselect = new Button("Deselect All", new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				deselectAll();
			}
		});
		buttons.add(selectAll);
		buttons.add(deselect);
		return buttons;
	}

	private void buildFilter() {

		pagingLoader = new WorkingSetTaxonPagingLoader();
		pagingLoader.getPagingLoader().setOffset(0);

		store = new ListStore<TaxaData>(pagingLoader.getPagingLoader());
		storeBinder = new DataListBinder<TaxaData>(taxaList, store);
		storeBinder.setDisplayProperty("display");
		storeBinder.setIconProvider(new ModelStringProvider<TaxaData>() {
			public String getStringValue(TaxaData arg0, String arg1) {
				return "icon-monkey-face";
			}
		});

		pagingBar = new PagingToolBar(40);
		pagingBar.bind(pagingLoader.getPagingLoader());

		storeBinder.init();

		sorter = new StoreSorter<TaxaData>();
		store.setStoreSorter(sorter);

		filter = new StoreFilter<TaxaData>() {
			public boolean select(Store<TaxaData> store, TaxaData parent, TaxaData item, String property) {
				String txt = filterTextBox.getText();
				if (txt != null && !txt.equals("")) {
					String name = item.get("name");
					return name.toLowerCase().startsWith(txt.toLowerCase());
				}
				return true;
			}
		};

		filterTextBox = new ViewerFilterTextBox<TaxaData>();
		filterTextBox.bind(store);
		store.addFilter(filter);

		if (checkListener != null)
			storeBinder.addCheckListener(checkListener);
	}

	private void buildList() {
		taxaList.addStyleName("gwt-background");
		taxaList.setBorders(false);
		taxaList.setScrollMode(Scroll.AUTOY);
	}

	private void buildToolbar() {
		filterByOrder = new Button();
		filterByOrder.setText("Order");
		filterByOrder.setTitle("Show only Order");
		filterByOrder.setId(TaxaData.ORDER);
		filterByOrder.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				filterByOrder();
				putFilterIcon((Button) ce.getSource());
				if (jumpToInToolbar) {
					toolbar.getItem(toolbar.getItemCount() - 1).setVisible(false);
					toolbar.getItem(toolbar.getItemCount() - 1).disable();
				}
			}
		});
		toolbar.add(filterByOrder);
		toolbar.add(new SeparatorToolItem());

		selectedFilter = filterByOrder;

		filterByFamily = new Button();
		filterByFamily.setText("Family");
		filterByFamily.setTitle("Show only Family");
		filterByFamily.setId(TaxaData.FAMILY);
		filterByFamily.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				filterByFamily();
				putFilterIcon((Button) ce.getSource());
				if (jumpToInToolbar) {
					toolbar.getItem(toolbar.getItemCount() - 1).setVisible(false);
					toolbar.getItem(toolbar.getItemCount() - 1).disable();
				}
			}
		});
		toolbar.add(filterByFamily);
		toolbar.add(new SeparatorToolItem());

		filterByGenus = new Button();
		filterByGenus.setText("Genus");
		filterByGenus.setId(TaxaData.GENUS);
		filterByGenus.setTitle("Show only Genus");
		filterByGenus.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				filterByGenus();
				putFilterIcon((Button) ce.getSource());
				if (jumpToInToolbar) {
					toolbar.getItem(toolbar.getItemCount() - 1).setVisible(false);
					toolbar.getItem(toolbar.getItemCount() - 1).disable();
				}
			}
		});
		toolbar.add(filterByGenus);
		toolbar.add(new SeparatorToolItem());

		filterBySpecies = new Button();
		filterBySpecies.setText("Species");
		filterBySpecies.setId(TaxaData.FULLNAME);
		filterBySpecies.setTitle("Show Genus Species");
		filterBySpecies.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				filterBySpecies();
				putFilterIcon((Button) ce.getSource());
				if (jumpToInToolbar) {
					toolbar.getItem(toolbar.getItemCount() - 1).setVisible(true);
					toolbar.getItem(toolbar.getItemCount() - 1).enable();
				}
			}
		});

		toolbar.add(filterBySpecies);
		toolbar.add(new SeparatorToolItem());

		numberSpecies = new Button();
		numberSpecies.setTitle("Number of species in working set");
		numberSpecies.setWidth("35px");
		numberSpecies.setText("(0)");
		numberSpecies.disable();

		toolbar.add(numberSpecies);
	}

	private void callCorrectFilter() {

		if (selectedFilter == null) {
			selectedFilter = filterByFamily;
			filterByFamily();
		}

		else if (selectedFilter.getId().equalsIgnoreCase(TaxaData.FAMILY)) {
			filterByFamily();
		}

		else if (selectedFilter.getId().equalsIgnoreCase(TaxaData.GENUS)) {
			filterByGenus();
		}

		else if (selectedFilter.getId().equalsIgnoreCase(TaxaData.ORDER)) {
			filterByOrder();
		}

		else {
			filterBySpecies();
		}

	}

	private void clearList() {
		// taxaList.removeAll();
		// store.removeAll();
		taxaList.removeAll();
		pagingLoader.getFullList().clear();
		pagingLoader.getPagingLoader().setOffset(0);
		storeBinder.setCheckedSelection(new ArrayList<TaxaData>());
	}

	public void deselectAll() {
		for (int i = 0; i < taxaList.getItemCount(); i++) {
			DataListItem item = taxaList.getItem(i);
			if (item.isChecked()) {
				item.setChecked(false);
				CheckChangedEvent<TaxaData> event = new CheckChangedEvent<TaxaData>(null, (TaxaData) item
						.getData(DATA_KEY));
				if (checkListener != null)
					checkListener.handleEvent(event);
			}
		}
	}

	private void filterByFamily() {

		// IF THERE IS NOTHING TO FILTER, RETURN
		if (recentWorkingSet == null) {
			return;
		}

		/**
		 * key -> value String -> String families -> taxonID, taxonID, taxonID
		 * ... all taxons that are in hashmap are in the taxonomycache
		 */
		final HashMap<String, String> families = new HashMap<String, String>();
		filteredBySpecies = false;
		taxaList.removeAll();
		taxaList.add(new DataListItem("Loading ..."));
		if (recentWorkingSet.getSpeciesIDs().size() > 0) {
			WorkingSetCache.impl.fetchTaxaForWorkingSet(recentWorkingSet, new GenericCallback<List<Taxon>>() {
				public void onFailure(Throwable caught) {
					clearList();
					taxaList.add(new DataListItem("Error fetching taxa."));
				}

				public void onSuccess(List<Taxon> arg0) {
					clearList();
					HashMap<String, String> childIDS = new HashMap<String, String>();
					for (Taxon node : arg0) {
						String family = node.getFootprint()[TaxonLevel.FAMILY];
						String kingdom = node.getFootprint()[TaxonLevel.KINGDOM];
						families.put(family, kingdom);
						if (childIDS.containsKey(family)) {
							String ids = childIDS.get(family);
							ids += "," + node.getId();
							childIDS.put(family, ids);
						} else {
							childIDS.put(family, String.valueOf(node.getId()));
						}
					}

					for (Entry<String, String> curEntry : families.entrySet())
						pagingLoader.getFullList().add(
								new TaxaData(curEntry.getKey(), TaxaData.FAMILY, curEntry.getValue(), childIDS
										.get(curEntry.getKey())));

					ArrayUtils.quicksort(pagingLoader.getFullList(), new Comparator<TaxaData>() {
						public int compare(TaxaData o1, TaxaData o2) {
							return ((String) o1.get("display")).compareTo((String) o2.get("display"));
						}
					});
					pagingLoader.getPagingLoader().load();
				}
			});
		} else {
			clearList();
			pagingLoader.getFullList().clear();
			pagingLoader.getPagingLoader().setOffset(0);
		}

	}

	private void filterByGenus() {

		// IF THERE IS NOTHING TO FILTER, RETURN
		if (recentWorkingSet == null) {
			return;
		}

		/**
		 * key -> value String -> String families -> taxonID, taxonID, taxonID
		 * ... all taxons that are in hashmap are in the taxonomycache
		 */
		final HashMap<String, String> geni = new HashMap<String, String>();
		filteredBySpecies = false;
		taxaList.removeAll();
		taxaList.add(new DataListItem("Loading ..."));

		if (recentWorkingSet.getSpeciesIDs().size() > 0) {
			WorkingSetCache.impl.fetchTaxaForWorkingSet(recentWorkingSet, new GenericCallback<List<Taxon>>() {
				public void onFailure(Throwable caught) {
					clearList();
					taxaList.add(new DataListItem("Error fetching taxa."));
					;
				}

				public void onSuccess(List<Taxon> arg0) {

					clearList();
					HashMap<String, String> childIDS = new HashMap<String, String>();
					for (Integer taxaID : recentWorkingSet.getSpeciesIDs()) {
						Taxon node = TaxonomyCache.impl.getTaxon(taxaID);
						String genus = node.getFootprint()[TaxonLevel.GENUS];
						String kingdom = node.getFootprint()[TaxonLevel.KINGDOM];
						geni.put(genus, kingdom);
						if (childIDS.containsKey(genus)) {
							String ids = childIDS.get(genus);
							ids += "," + taxaID;
							childIDS.put(genus, ids);
						} else {
							childIDS.put(genus, String.valueOf(taxaID));
						}
					}

					for (Entry<String, String> curEntry : geni.entrySet()) {
						pagingLoader.getFullList().add(
								new TaxaData(curEntry.getKey(), TaxaData.GENUS, curEntry.getValue(), childIDS
										.get(curEntry.getKey())));
					}

					ArrayUtils.quicksort(pagingLoader.getFullList(), new Comparator<TaxaData>() {
						public int compare(TaxaData o1, TaxaData o2) {
							return ((String) o1.get("display")).compareTo((String) o2.get("display"));
						}
					});
					pagingLoader.getPagingLoader().load();
				}
			});
		} else {
			clearList();
			pagingLoader.getFullList().clear();
			pagingLoader.getPagingLoader().setOffset(0);
		}

	}

	private void filterByOrder() {
		// IF THERE IS NOTHING TO FILTER, RETURN
		if (recentWorkingSet == null) {
			return;
		}

		/**
		 * key -> value String -> String orders -> taxonID, taxonID, taxonID ...
		 * all taxons that are in hashmap are in the taxonomycache
		 */
		final HashMap<String, String> orders = new HashMap<String, String>();
		filteredBySpecies = false;
		taxaList.removeAll();
		taxaList.add(new DataListItem("Loading ..."));

		if (recentWorkingSet.getSpeciesIDs().size() > 0) {
			WorkingSetCache.impl.fetchTaxaForWorkingSet(recentWorkingSet, new GenericCallback<List<Taxon>>() {
				public void onFailure(Throwable caught) {
					clearList();
					taxaList.add(new DataListItem("Error fetching taxa."));
				}

				public void onSuccess(List<Taxon> arg0) {
					try {
						clearList();
						HashMap<String, String> childIDS = new HashMap<String, String>();
						for (Integer taxaID : recentWorkingSet.getSpeciesIDs()) {
							Taxon node = TaxonomyCache.impl.getTaxon(taxaID);
							String order = node.getFootprint()[TaxonLevel.ORDER];
							String kingdom = node.getFootprint()[TaxonLevel.KINGDOM];
							orders.put(order, kingdom);
							if (childIDS.containsKey(order)) {
								String ids = childIDS.get(order);
								ids += "," + taxaID;
								childIDS.put(order, ids);
							} else {
								childIDS.put(order, String.valueOf(taxaID));
							}
						}

						for (Entry<String, String> curEntry : orders.entrySet())
							pagingLoader.getFullList().add(
									new TaxaData(curEntry.getKey(), TaxaData.ORDER, curEntry.getValue(), childIDS
											.get(curEntry.getKey())));

						ArrayUtils.quicksort(pagingLoader.getFullList(), new Comparator<TaxaData>() {
							public int compare(TaxaData o1, TaxaData o2) {
								return ((String) o1.get("display")).compareTo((String) o2.get("display"));
							}
						});
						pagingLoader.getPagingLoader().load();
					} catch (Throwable e) {
						e.printStackTrace();
					}
				}
			});
		} else {
			clearList();
			pagingLoader.getFullList().clear();
			pagingLoader.getPagingLoader().setOffset(0);
		}

	}

	private void filterBySpecies() {

		// IF THERE IS NOTHING TO FILTER, RETURN
		if (recentWorkingSet == null) {
			return;
		}
		
		if (jumpToInToolbar) {
			toolbar.getItem(toolbar.getItemCount() - 1).setVisible(true);
			toolbar.getItem(toolbar.getItemCount() - 1).enable();
		}

		/**
		 * key -> value String -> String families -> taxonID, taxonID, taxonID
		 * ... all taxons that are in hashmap are in the taxonomycache
		 */
		final HashMap<String, String> fullNames = new HashMap<String, String>();
		filteredBySpecies = false;
		taxaList.removeAll();
		taxaList.add(new DataListItem("Loading ..."));
		if (recentWorkingSet.getSpeciesIDs().size() > 0) {
			WorkingSetCache.impl.fetchTaxaForWorkingSet(recentWorkingSet, new GenericCallback<List<Taxon>>() {
				public void onFailure(Throwable caught) {
					clearList();
					taxaList.add(new DataListItem("Error fetching taxa."));
				}

				public void onSuccess(List<Taxon> arg0) {
					clearList();
					
					for (Integer taxaID : recentWorkingSet.getSpeciesIDs()) {
						Taxon node = TaxonomyCache.impl.getTaxon(taxaID);
						if (node != null) {
							String taxaName = node.getFullName();
							fullNames.put(taxaName, String.valueOf(taxaID));
							pagingLoader.getFullList().add(
									new TaxaData(taxaName, TaxaData.FULLNAME, String.valueOf(taxaID), null));
						}
					}
					
					Collections.sort(pagingLoader.getFullList(), new Comparator<TaxaData>() {
						private final PortableAlphanumericComparator comparator = 
							new PortableAlphanumericComparator();
						public int compare(TaxaData o1, TaxaData o2) {
							return comparator.compare(o1.get("display"), o2.get("display"));
						}
					});

					// ArrayUtils.quicksort((ArrayList)
					// pagingLoader.getFullList(), new Comparator<TaxaData>() {
					// public int compare(TaxaData o1, TaxaData o2) {
					// return ((String) o1.get("display")).compareTo((String)
					// o2.get("display"));
					// }
					// });
					pagingLoader.getPagingLoader().load();
				}
			});
		} else {
			clearList();
		}

	}

	public void forcedRefresh() {

		if (WorkingSetCache.impl.getCurrentWorkingSet() != null) {
			recentWorkingSet = WorkingSetCache.impl.getCurrentWorkingSet();
			callCorrectFilter();
			putFilterIcon(selectedFilter);
		}

		// CURRENT WORKING SET IS NULL
		else {
			clearList();
		}
		refreshNumber();
		layout();
	}

	public List<TaxaData> getChecked() {
		return storeBinder.getCheckedSelection();
	}

	public String getFilter() {
		if (selectedFilter != null) {
			return selectedFilter.getId();
		} else
			return filterByFamily.getId();
	}

	public List<DataListItem> getListChecked() {
		return taxaList.getChecked();
	}

	public String getSelectedInList() {
		if (storeBinder.getSelection() != null && storeBinder.getSelection().size() > 0)
			return storeBinder.getSelection().get(0).getID();
		else
			return null;
	}

	public boolean isFilteredBySpecies() {
		return filteredBySpecies;
	}

	private void putFilterIcon(Button item) {
		for (int i = 0; i < toolbar.getItemCount() - 1; i++) {
			if (toolbar.getItem(i).equals(item))
				((Button) toolbar.getItem(i)).setIconStyle("icon-bullet-arrow-down");
			else if (toolbar.getItem(i) instanceof Button)
				((Button) toolbar.getItem(i)).setIconStyle(null);
		}

		selectedFilter = item;
	}

	@Override
	public void refresh() {

		// IF THE WORKING SET HASN'T CHANGED SINCE THE LAST TIME IT WAS LOADED
		// DON'T DO ANYTHING
		if ((recentWorkingSet == null && WorkingSetCache.impl.getCurrentWorkingSet() != null)
				|| (WorkingSetCache.impl.getCurrentWorkingSet() != null && recentWorkingSet != null && !recentWorkingSet
						.equals(WorkingSetCache.impl.getCurrentWorkingSet()))) {
			recentWorkingSet = WorkingSetCache.impl.getCurrentWorkingSet();
			callCorrectFilter();
			putFilterIcon(selectedFilter);
		}

		// CURRENT WORKING SET IS NULL
		if (WorkingSetCache.impl.getCurrentWorkingSet() == null) {
			clearList();
		}

		refreshNumber();

		layout();
	}

	private void refreshNumber() {
		if (recentWorkingSet != null)
			numberSpecies.setText("(" + recentWorkingSet.getSpeciesIDs().size() + ")");
		else
			numberSpecies.setText("(0)");

		numberSpecies.setWidth("35px");
	}

	public void refreshWithFilter(String filter) {

		if ((recentWorkingSet == null && WorkingSetCache.impl.getCurrentWorkingSet() != null)
				|| (WorkingSetCache.impl.getCurrentWorkingSet() != null && recentWorkingSet != null && !recentWorkingSet
						.equals(WorkingSetCache.impl.getCurrentWorkingSet()))) {
			recentWorkingSet = WorkingSetCache.impl.getCurrentWorkingSet();
			setFilter(filter);
			callCorrectFilter();
		}

		// CURRENT WORKING SET IS NULL
		else if (WorkingSetCache.impl.getCurrentWorkingSet() == null) {
			clearList();
		}

		refreshNumber();
		layout();
	}

	private void selectAll() {
		for (int i = 0; i < taxaList.getItemCount(); i++) {
			DataListItem item = taxaList.getItem(i);
			if (!item.isChecked()) {
				item.setChecked(true);
				CheckChangedEvent<TaxaData> event = new CheckChangedEvent<TaxaData>(null, (TaxaData) item
						.getData(DATA_KEY));
				if (checkListener != null)
					checkListener.handleEvent(event);
			}
		}

	}

	public void setFilter(String filter) {
		if (filter.equalsIgnoreCase(TaxaData.FAMILY) || filter.equalsIgnoreCase(TaxaData.GENUS)
				|| filter.equalsIgnoreCase(TaxaData.FULLNAME)) {
			for (int i = 0; i < toolbar.getItemCount() - 1; i++) {
				if (toolbar.getItem(i).getId().equalsIgnoreCase(filter)) {
					selectedFilter = (Button) toolbar.getItem(i);
				}
			}
			callCorrectFilter();
			putFilterIcon(selectedFilter);
		}
	}

	public void setFilterVisible(boolean visible) {
		filterTextBox.setVisible(visible);
	}

	/**
	 * puts an item in the toolbar that lets you jump to a specific node
	 */
	public void setJumpToToolbar() {
		jumpToInToolbar = true;

		Button item = new Button();
		item.setIconStyle("icon-go-jump");
		item.setText("View");
		item.setToolTip("View in Taxonomy Browser");
		item.setId("jump");
		item.setVisible(false);
		item.disable();
		item.addSelectionListener(new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				if (storeBinder.getSelection() != null && storeBinder.getSelection().size() > 0) {
					TaxaData data = storeBinder.getSelection().get(0);

					if (data.getType().equalsIgnoreCase(TaxaData.FULLNAME)) {
						TaxonomyCache.impl.fetchTaxon(Integer.valueOf(data.getID()), true,
								new GenericCallback<Taxon>() {
							public void onFailure(Throwable caught) {
								Info.display(new InfoConfig("Error", "Error loading taxonomy browser."));
							}

							public void onSuccess(Taxon arg0) {
								StateManager.impl.setTaxon(arg0);
								/*
								ClientUIContainer.bodyContainer
										.setSelection(ClientUIContainer.bodyContainer.tabManager.taxonHomePage);*/
							}

						});

					}

					else {
						TaxonomyCache.impl.fetchTaxonWithKingdom(data.getID(), data.getName(),
								new GenericCallback<Taxon>() {
							public void onFailure(Throwable caught) {
								Info.display(new InfoConfig("Error", "Error loading taxonomy browser."));
							}
							public void onSuccess(Taxon arg0) {
								StateManager.impl.setTaxon(arg0);
								/*ClientUIContainer.bodyContainer
												.setSelection(ClientUIContainer.bodyContainer.tabManager.taxonHomePage);*/
							}
						});
					}
				}
			}
		});
		toolbar.add(item);

	}

	@Override
	public void setSize(int width, int height) {
		super.setSize(width, height);

		toolbar.setWidth(width);

		if (filterTextBox.isVisible()) {
			if (checked)
				taxaList.setSize(width, height - 103);
			else
				taxaList.setSize(width, height - 73);
		} else {
			if (!checked)
				taxaList.setSize(width, height - 55);
			else
				taxaList.setSize(width, height - 85);
		}
	}

}
