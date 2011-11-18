package org.iucn.sis.client.panels.publication;

import java.util.ArrayList;
import java.util.List;

import org.iucn.sis.client.api.caches.PublicationCache;
import org.iucn.sis.client.api.utils.PagingPanel;
import org.iucn.sis.shared.api.models.PublicationData;
import org.iucn.sis.shared.api.models.PublicationTarget;
import org.iucn.sis.shared.api.models.Taxon;

import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.grid.CheckBoxSelectionModel;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.util.events.ComplexListener;
import com.solertium.util.gwt.ui.DrawsLazily;

public class PublicationGrid extends PagingPanel<PublicationModelData> implements DrawsLazily {
	
	private final Grid<PublicationModelData> grid;
	private final CheckBoxSelectionModel<PublicationModelData> sm;
	
	public PublicationGrid() {
		super();
		setLayout(new FillLayout());
		
		sm = new CheckBoxSelectionModel<PublicationModelData>();
		sm.setSelectionMode(SelectionMode.SIMPLE);
		
		grid = new Grid<PublicationModelData>(getStoreInstance(), getColumnModel());
		grid.addPlugin(sm);
		grid.setSelectionModel(sm);
	}
	
	private ColumnModel getColumnModel() {
		List<ColumnConfig> cols = new ArrayList<ColumnConfig>();
		cols.add(sm.getColumn());
		
		cols.add(new ColumnConfig("group", "Working Set Submitted in", 100));
		cols.add(new ColumnConfig("taxon", "Species Name", 100));
		cols.add(new ColumnConfig("status", "Status", 100));
		cols.add(new ColumnConfig("submitter", "Submitted By", 100));
		cols.add(new ColumnConfig("goal", "Publication Target", 150));
		cols.add(new ColumnConfig("approved", "For Publication", 150));
		cols.add(new ColumnConfig("notes", "Notes RLU", 200));
		
		cols.get(0).setHidden(true);
		
		return new ColumnModel(cols);
	}

	@Override
	protected void getStore(final GenericCallback<ListStore<PublicationModelData>> callback) {
		PublicationCache.impl.listData(new ComplexListener<List<PublicationData>>() {
			public void handleEvent(List<PublicationData> eventData) {
				ListStore<PublicationModelData> store = new ListStore<PublicationModelData>();
				for (PublicationData data : eventData)
					store.add(new PublicationModelData(data));
				
				callback.onSuccess(store);
			}
		});
	}
	
	@Override
	protected void refreshView() {
		grid.getView().refresh(false);
	}
	
	@Override
	public void draw(final DoneDrawingCallback callback) {
		PublicationCache.impl.listTargets(new ComplexListener<List<PublicationTarget>>() {
			public void handleEvent(List<PublicationTarget> eventData) {
				System.out.println("Targets found: " + eventData);
				removeAll();
				refresh(new DrawsLazily.DoneDrawingCallback() {
					public void isDrawn() {
						add(grid);
						
						callback.isDrawn();
					}
				});
			}
		});
	}
	
	public void refresh() {
		refresh(new DrawsLazily.DoneDrawingWithNothingToDoCallback());
	}
	
	public void hideCheckbox() {
		grid.getColumnModel().setHidden(0, true);
	}
	
	public void showCheckbox() {
		grid.getColumnModel().setHidden(0, false);
	}
	
	public void filterByTaxon(Taxon taxon) {
		//TODO: implement
	}
	
	public List<PublicationModelData> getChecked() {
		return sm.getSelectedItems();
	}
	
}
