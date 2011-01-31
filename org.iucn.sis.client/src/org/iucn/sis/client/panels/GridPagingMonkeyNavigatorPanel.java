package org.iucn.sis.client.panels;

import org.iucn.sis.client.panels.PagingMonkeyNavigatorPanel.NavigationModelData;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridView;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;

public abstract class GridPagingMonkeyNavigatorPanel<T> extends PagingMonkeyNavigatorPanel<NavigationModelData<T>> {
	
	protected Grid<NavigationModelData<T>> grid;
	
	public GridPagingMonkeyNavigatorPanel() {
		super(new FillLayout());
		
		final GridView view = getView();
		
		grid = new Grid<NavigationModelData<T>>(getStoreInstance(), getColumnModel());
		if (view != null)
			grid.setView(view);
		grid.setSelectionModel(new NavigationGridSelectionModel<T>());
		grid.setAutoExpandColumn("name");
		grid.setHideHeaders(true);
		grid.setBorders(false);
		grid.setContextMenu(createMarkingContextMenu(new SelectionListener<MenuEvent>() {
			public void componentSelected(MenuEvent ce) {
				if (!hasSelection())
					return;
				
				mark(grid.getSelectionModel().getSelectedItem(), ce.getItem().getItemId());
			}
		}));
		grid.addListener(Events.RowClick, new Listener<GridEvent<NavigationModelData<T>>>() {
			public void handleEvent(GridEvent<NavigationModelData<T>> be) {
				onSelectionChanged(be.getModel());
			}
		});
		grid.addListener(Events.RowDoubleClick, new Listener<GridEvent<NavigationModelData<T>>>() {
			public void handleEvent(GridEvent<NavigationModelData<T>> be) {
				open(be.getModel());
			}
		});
		
		setupToolbar();
		
		final LayoutContainer container = new LayoutContainer(new BorderLayout());
		container.add(grid, new BorderLayoutData(LayoutRegion.CENTER));
		container.add(getPagingToolbar(), new BorderLayoutData(LayoutRegion.SOUTH, 25, 25, 25));
		
		add(container);
	}
	
	protected GridView getView() {
		return null;
	}
	
	public T getSelected() {
		NavigationModelData<T> model = grid.getSelectionModel().getSelectedItem();
		if (model != null)
			return model.getModel();
		else
			return null;
	}
	
	public boolean hasSelection() {
		return getSelected() != null;
	}
	
	@Override
	protected void refreshView() {
		grid.getView().refresh(false);
	}
	
	protected abstract ColumnModel getColumnModel();
	
	protected abstract void open(NavigationModelData<T> model);
	
	protected abstract void mark(NavigationModelData<T> model, String color);
	
	protected abstract void onSelectionChanged(NavigationModelData<T> model);
	
	protected abstract void setupToolbar();
	

}
