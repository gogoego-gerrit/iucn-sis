package org.iucn.sis.client.panels;

import java.util.ArrayList;
import java.util.List;

import org.iucn.sis.client.api.assessment.AssessmentClientSaveUtils;
import org.iucn.sis.client.api.caches.MarkedCache;
import org.iucn.sis.client.api.caches.TaxonomyCache;
import org.iucn.sis.client.api.caches.WorkingSetCache;
import org.iucn.sis.client.panels.MonkeyNavigator.NavigationChangeEvent;
import org.iucn.sis.shared.api.debug.Debug;
import org.iucn.sis.shared.api.models.Taxon;
import org.iucn.sis.shared.api.models.TaxonLevel;
import org.iucn.sis.shared.api.models.WorkingSet;

import com.extjs.gxt.ui.client.data.ModelKeyProvider;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.GroupingStore;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.grid.GridGroupRenderer;
import com.extjs.gxt.ui.client.widget.grid.GridView;
import com.extjs.gxt.ui.client.widget.grid.GroupColumnData;
import com.extjs.gxt.ui.client.widget.grid.GroupingView;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.util.events.SimpleListener;
import com.solertium.util.extjs.client.WindowUtils;
import com.solertium.util.gwt.ui.DrawsLazily;

public class TaxonMonkeyNavigatorPanel extends GridPagingMonkeyNavigatorPanel<Taxon> {
	
	private WorkingSet curNavWorkingSet;
	private Taxon curNavTaxon;
	
	public TaxonMonkeyNavigatorPanel() {
		super();
		setHeading("Taxon List");
		
		setPageCount(40);
	}
	
	public void refresh(final WorkingSet curNavWorkingSet, final Taxon curNavTaxon) {
		this.curNavWorkingSet = curNavWorkingSet;
		this.curNavTaxon = curNavTaxon;
		
		refresh(new DrawsLazily.DoneDrawingWithNothingToDoCallback());
	}
	
	protected void getStore(GenericCallback<ListStore<NavigationModelData<Taxon>>> callback) {
		if (curNavWorkingSet == null)
			getRecentTaxonStore(callback);
		else
			getTaxonForWorkingSetStore(callback);
	}
	
	private void getRecentTaxonStore(GenericCallback<ListStore<NavigationModelData<Taxon>>> callback) {
		final GroupingStore<NavigationModelData<Taxon>> store = 
			new GroupingStore<NavigationModelData<Taxon>>();
		store.groupBy("familyid");
		store.setKeyProvider(new ModelKeyProvider<NavigationModelData<Taxon>>() {
			public String getKey(NavigationModelData<Taxon> model) {
				if (model.getModel() == null)
					return "-1";
				else
					return Integer.toString(model.getModel().getId());
			}
		});
		
		for (Taxon taxon : TaxonomyCache.impl.getRecentlyAccessed()) {
			NavigationModelData<Taxon> model = new NavigationModelData<Taxon>(taxon);
			model.set("name", taxon.getFriendlyName());
			model.set("family", "Recent Taxa");
			
			store.add(model);
		}
		
		callback.onSuccess(store);
	}
	
	private void getTaxonForWorkingSetStore(final GenericCallback<ListStore<NavigationModelData<Taxon>>> callback) {
		/*final GroupingStore<NavigationModelData<Taxon>> store = 
			new GroupingStore<NavigationModelData<Taxon>>();
		store.groupBy("familyid");*/
		final ListStore<NavigationModelData<Taxon>> store = new ListStore<NavigationModelData<Taxon>>();
		store.setKeyProvider(new ModelKeyProvider<NavigationModelData<Taxon>>() {
			public String getKey(NavigationModelData<Taxon> model) {
				if (model.getModel() == null)
					return "-1";
				else
					return Integer.toString(model.getModel().getId());
			}
		});
		
		WorkingSetCache.impl.fetchTaxaForWorkingSet(curNavWorkingSet, new GenericCallback<List<Taxon>>() {
			public void onSuccess(List<Taxon> result) {
				for (Taxon taxon : result) {
					NavigationModelData<Taxon> model = new NavigationModelData<Taxon>(taxon);
					model.set("name", taxon.getFriendlyName());
					model.set("family", taxon.getFootprint()[TaxonLevel.FAMILY]);
					
					store.add(model);
				}
				
				callback.onSuccess(store);
			}
			public void onFailure(Throwable caught) {
				WindowUtils.hideLoadingAlert();
				WindowUtils.errorAlert("Error loading taxa for this working set.");
				
				callback.onSuccess(store);
			}
		});
	}
	
	@Override
	protected ColumnModel getColumnModel() {
		final List<ColumnConfig> list = new ArrayList<ColumnConfig>();
		
		ColumnConfig display = new ColumnConfig("name", "Name", 100);
		display.setRenderer(new GridCellRenderer<NavigationModelData<Taxon>>() {
			@Override
			public Object render(NavigationModelData<Taxon> model, String property, ColumnData config,
					int rowIndex, int colIndex, ListStore<NavigationModelData<Taxon>> store,
					Grid<NavigationModelData<Taxon>> grid) {
				Taxon taxon = model.getModel();
				String style;
				if (taxon == null)
					style = MarkedCache.NONE;
				else
					style = MarkedCache.impl.getTaxaStyle(taxon.getId());
				return "<span class=\"" + style + "\">" + model.get(property) + "</span>";
			}
		});
		list.add(display);
		
		ColumnConfig family = new ColumnConfig("family", "Family", 100);
		family.setHidden(true);
		
		list.add(family);
		
		return new ColumnModel(list);
	}
	
	@Override
	protected GridView getView() {
		GroupingView view = new GroupingView();
		view.setEmptyText("No taxa to list.");
		view.setGroupRenderer(new GridGroupRenderer() {
			public String render(GroupColumnData data) {
				return data.group;
			}
		});
		
		return view;
	}
	
	protected ListStore<NavigationModelData<Taxon>> getStoreInstance() {
		GroupingStore<NavigationModelData<Taxon>> store = 
			new GroupingStore<NavigationModelData<Taxon>>(getLoader());
		store.groupBy("family");
		
		return store;
	}
	
	@Override
	protected void mark(NavigationModelData<Taxon> model, String color) {
		if (model.getModel() == null)
			return;
		
		Integer itemID = model.getModel().getId();
		
		MarkedCache.impl.markTaxa(itemID, color);
		
		refreshView();
	}
	
	@Override
	protected void onSelectionChanged(NavigationModelData<Taxon> model) {
		MonkeyNavigator.NavigationChangeEvent<Taxon> e = 
			new NavigationChangeEvent<Taxon>(model.getModel());
		
		fireEvent(Events.SelectionChange, e);
	}
	
	@Override
	protected void onLoad() {
		super.onLoad();
		
		NavigationModelData<Taxon> selection = null;
		if (curNavTaxon != null)
			selection = getProxy().getStore().findModel("" + curNavTaxon.getId());
		
		Debug.println("Selected taxon from nav is {0}, found {1}", curNavTaxon, selection);
		if (selection != null) {
			((NavigationGridSelectionModel<Taxon>)grid.getSelectionModel()).
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
	protected void open(NavigationModelData<Taxon> model) {
		if (hasSelection())
			navigate(curNavWorkingSet, getSelected(), null);
	}
	
	@Override
	protected void setupToolbar() {
		final Button goToTaxon = new Button();
		goToTaxon.setIconStyle("icon-go-jump");
		goToTaxon.addSelectionListener(new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				AssessmentClientSaveUtils.saveIfNecessary(new SimpleListener() {
					public void handleEvent() {
						open(grid.getSelectionModel().getSelectedItem());
					}
				});
			}
		});
		
		addTool(goToTaxon);
	}

}
