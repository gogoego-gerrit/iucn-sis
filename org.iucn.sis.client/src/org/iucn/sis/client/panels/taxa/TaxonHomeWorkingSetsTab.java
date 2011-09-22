package org.iucn.sis.client.panels.taxa;

import java.util.ArrayList;
import java.util.List;

import org.iucn.sis.client.api.caches.FetchMode;
import org.iucn.sis.client.api.caches.TaxonomyCache;
import org.iucn.sis.client.api.caches.WorkingSetCache;
import org.iucn.sis.client.api.container.StateManager;
import org.iucn.sis.shared.api.models.WorkingSet;
import org.iucn.sis.shared.api.utils.CaseInsensitiveAlphanumericComparator;

import com.extjs.gxt.ui.client.Style.SortDir;
import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.IconButtonEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.StoreSorter;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.IconButton;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.util.extjs.client.WindowUtils;
import com.solertium.util.gwt.ui.DrawsLazily;

public class TaxonHomeWorkingSetsTab extends LayoutContainer implements DrawsLazily {

	public TaxonHomeWorkingSetsTab() {
		super(new FillLayout());
		setLayoutOnChange(true);
	}
	
	@Override
	public void draw(DoneDrawingCallback callback) {
		loadWorkingSetInformationPanel(new DoneDrawingCallbackWithParam<Component>() {
			public void isDrawn(Component parameter) {
				removeAll();
				add(parameter);
			}
		});
	}
	
	/**
	 * Loads the working set information panel.  If this 
	 * panel is not applicable for the current taxon, it 
	 * returns null.
	 * @return
	 */
	private void loadWorkingSetInformationPanel(final DrawsLazily.DoneDrawingCallbackWithParam<Component> callback) {
		TaxonomyCache.impl.fetchWorkingSetsForTaxon(TaxonomyCache.impl.getCurrentTaxon(), new GenericCallback<List<WorkingSet>>() {
			public void onSuccess(List<WorkingSet> workingSets) {
				final ListStore<BaseModelData> store = new ListStore<BaseModelData>();
				store.setStoreSorter(new StoreSorter<BaseModelData>(new CaseInsensitiveAlphanumericComparator()));
				
				for (final WorkingSet data : workingSets) {
					BaseModelData model = new BaseModelData();
					model.set("wsname", data.getName());
					model.set("creator", data.getCreatorUsername());
					model.set("id", data.getId());
					model.set("open", WorkingSetCache.impl.isCached(data.getId(), FetchMode.PARTIAL));
					store.add(model);
				}

				List<ColumnConfig> columns = new ArrayList<ColumnConfig>();

				ColumnConfig openColumn = new ColumnConfig("open", "Open", 50);
				openColumn.setSortable(false);
				openColumn.setRenderer(new GridCellRenderer<BaseModelData>() {
					public Object render(final BaseModelData model, String property, ColumnData config,
							int rowIndex, int colIndex, ListStore<BaseModelData> store,
							Grid<BaseModelData> grid) {
						boolean open = model.get(property);
						if (open) {
							IconButton openIcon = new IconButton("icon-go-jump");
							openIcon.addSelectionListener(new SelectionListener<IconButtonEvent>() {
								public void componentSelected(IconButtonEvent ce) {
									openWorkingSet(model);
								}
							});
							
							return openIcon;
						}
						else
							return "";
					}
				});
				
				columns.add(new ColumnConfig("wsname", "Working Set Name", 200));
				columns.add(new ColumnConfig("creator", "Owner", 150));
				columns.add(openColumn);
				
				final Grid<BaseModelData> tbl = new Grid<BaseModelData>(store, new ColumnModel(columns));
				tbl.setBorders(false);
				tbl.removeAllListeners();
				tbl.addListener(Events.RowClick, new Listener<GridEvent<BaseModelData>>() {
					@Override
					public void handleEvent(GridEvent<BaseModelData> be) {
						if (be.getModel() != null && Boolean.TRUE.equals(be.getModel().get("open")))
							openWorkingSet(be.getModel());
					}
				});
					
				tbl.getStore().sort("wsname", SortDir.ASC);
					
				if (workingSets.isEmpty())
					Info.display("Info", "There are no working sets for this taxon.");
					
				callback.isDrawn(tbl);
			}
			public void onFailure(Throwable caught) {
				WindowUtils.errorAlert("Could not load working sets, please try again later.");
			}
		});
	}

	private void openWorkingSet(BaseModelData model) {
		final Integer id = model.get("id");
		StateManager.impl.setState(WorkingSetCache.impl.getWorkingSet(id), null, null);
	}
	
}
