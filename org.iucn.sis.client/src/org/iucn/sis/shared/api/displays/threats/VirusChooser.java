package org.iucn.sis.shared.api.displays.threats;

import java.util.ArrayList;
import java.util.List;

import org.iucn.sis.client.api.caches.VirusCache;
import org.iucn.sis.client.api.utils.PagingPanel;
import org.iucn.sis.client.panels.viruses.VirusModelData;
import org.iucn.sis.shared.api.models.Virus;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.SortDir;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.StoreSorter;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.grid.CheckBoxSelectionModel;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.util.events.ComplexListener;
import com.solertium.util.gwt.ui.DrawsLazily;
import com.solertium.util.portable.PortableAlphanumericComparator;

public class VirusChooser extends PagingPanel<VirusModelData> implements DrawsLazily {
	
	private Grid<VirusModelData> grid;

	public VirusChooser() {
		super();
		setLayout(new FillLayout());
	}
	
	public void draw(final DrawsLazily.DoneDrawingCallback callback) {
		final CheckBoxSelectionModel<VirusModelData> sm = 
			new CheckBoxSelectionModel<VirusModelData>();
		
		grid = 
			new Grid<VirusModelData>(getStoreInstance(), getColumnModel(sm.getColumn()));
		grid.addPlugin(sm);
		grid.setSelectionModel(sm);
		grid.setAutoExpandColumn("comments");
		
		final LayoutContainer container = new LayoutContainer(new BorderLayout());
		container.add(grid, new BorderLayoutData(LayoutRegion.CENTER));
		container.add(getPagingToolbar(), new BorderLayoutData(LayoutRegion.SOUTH, 25, 25, 25));
		
		add(container);
		
		refresh(callback);
	}
	
	@Override
	protected void getStore(final GenericCallback<ListStore<VirusModelData>> callback) {
		VirusCache.impl.list(new ComplexListener<List<Virus>>() {
			public void handleEvent(List<Virus> eventData) {
				final ListStore<VirusModelData> store = 
					new ListStore<VirusModelData>();
				store.setStoreSorter(new StoreSorter<VirusModelData>(new PortableAlphanumericComparator()));
				
				for (Virus virus : eventData)
					store.add(new VirusModelData(virus));
				
				store.sort("text", SortDir.ASC);
				
				callback.onSuccess(store);
			}
		});
	}
	
	private ColumnModel getColumnModel(ColumnConfig checkColumn) {
		final List<ColumnConfig> list = new ArrayList<ColumnConfig>();
		list.add(checkColumn);
		
		list.add(new ColumnConfig("text", "Virus Name", 250));
		list.add(new ColumnConfig("comments", "Comments", 350));
		
		return new ColumnModel(list);
	}
	
	@Override
	protected void refreshView() {
		grid.getView().refresh(false);
	}
	
	public List<Virus> getSelection() {
		final List<Virus> checked = new ArrayList<Virus>();
		for (VirusModelData model : grid.getSelectionModel().getSelectedItems())
			checked.add(model.getVirus());
		
		return checked;
	}
	
}
