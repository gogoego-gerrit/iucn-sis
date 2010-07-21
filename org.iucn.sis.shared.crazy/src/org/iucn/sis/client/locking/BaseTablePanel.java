package org.iucn.sis.client.locking;

import java.util.Collection;
import java.util.List;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridSelectionModel;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.solertium.lwxml.shared.utils.RowData;
import com.solertium.util.gwt.ui.DrawsLazily;

public abstract class BaseTablePanel extends LayoutContainer implements DrawsLazily {

	private boolean isDrawn;
	protected Grid<BaseModelData> grid;
	
	public BaseTablePanel() {
		super();
		setLayout(new FillLayout());
		setLayoutOnChange(true);
		setScrollMode(Scroll.AUTO);
		
		isDrawn = false;
	}
	
	public void draw(DoneDrawingCallback callback) {
		removeAll();
		
		final List<ColumnConfig> cols = getColumns();
		
		final ListStore<BaseModelData> store = new ListStore<BaseModelData>();
		for (RowData row : getRows()) {
			final BaseModelData model = new BaseModelData();
			for (ColumnConfig col : cols)
				model.set(col.getId(), row.getField(col.getId()));
			store.add(model);
		}
		
		final GridSelectionModel<BaseModelData> sel = new GridSelectionModel<BaseModelData>();
		sel.setSelectionMode(SelectionMode.SINGLE);
		
		grid = new Grid<BaseModelData>(store, new ColumnModel(cols));
		grid.setSelectionModel(sel);
		//grid.setAutoExpandColumn(getAutoExpandColumn());
		
		final ToolBar bar = getToolBar();
		
		int size = 25;
		
		final LayoutContainer container = new LayoutContainer(new BorderLayout());
		container.add(bar, new BorderLayoutData(LayoutRegion.NORTH, size, size, size));
		container.add(grid, new BorderLayoutData(LayoutRegion.CENTER));
		
		add(container);
		
		callback.isDrawn();
	}
	
	public abstract Collection<RowData> getRows();
	
	public abstract List<ColumnConfig> getColumns();
	
	public abstract ToolBar getToolBar();
	
	public abstract String getAutoExpandColumn();
	

}
