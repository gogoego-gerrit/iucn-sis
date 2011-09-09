package org.iucn.sis.client.panels.permissions;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.iucn.sis.client.api.caches.AuthorizationCache;
import org.iucn.sis.client.api.ui.models.workingset.WSModel;
import org.iucn.sis.shared.api.acl.base.AuthorizableObject;
import org.iucn.sis.shared.api.models.WorkingSet;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.binder.DataListBinder;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.DataList;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.toolbar.PagingToolBar;
import com.solertium.lwxml.shared.utils.ArrayUtils;
import com.solertium.util.events.ComplexListener;
import com.solertium.util.extjs.client.GenericPagingLoader;
import com.solertium.util.extjs.client.PagingLoaderFilter;

@SuppressWarnings("deprecation")
public class PermissionWorkingSetList extends ContentPanel {

	private DataList list;
	private ListStore<WSModel> store;
	private DataListBinder<WSModel> binder;
	
	private PagingToolBar pagingBar = null;
	private GenericPagingLoader<WSModel> pagingLoader = null;
	
	private TextField<String> filter;
	private Button doFilter;
	
	private LayoutContainer listContainer;
	
	public PermissionWorkingSetList(final List<WorkingSet> workingSets, final ComplexListener<List<WorkingSet>> listener) {
		super();
		setLayout(new RowLayout());
		setHeaderVisible(false);
		setBorders(true);
		
		list = new DataList();
		list.setCheckable(true);
		list.setScrollMode(Scroll.AUTO);
		
		pagingBar = new PagingToolBar(35);
		pagingLoader = new GenericPagingLoader<WSModel>();
		pagingBar.bind(pagingLoader.getPagingLoader());
		
		store = new ListStore<WSModel>();
		store = new ListStore<WSModel>(pagingLoader.getPagingLoader());
		binder = new DataListBinder<WSModel>(list, store);
		binder.setDisplayProperty("name");
		
		filter = new TextField<String>();
		filter.setFieldLabel("Filter by Name:");
		
		doFilter = new Button("Filter", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				pagingLoader.applyFilter("name");
				pagingLoader.getPagingLoader().load();
			}
		});
		
		pagingLoader.setFilter(new PagingLoaderFilter<WSModel>() {
			public boolean filter(WSModel item, String property) {
				if( filter.getValue() == null || filter.getValue().equals("") )
					return false;
				else 
					return !item.getName().contains(filter.getValue());
			}
		});
		
		listContainer = new LayoutContainer();
		listContainer.setLayout(new FitLayout());
		listContainer.add(list);
		
		HorizontalPanel filterPanel = new HorizontalPanel();
		filterPanel.add(filter);
		filterPanel.add(doFilter);
		
		add(new Html("Please select one or more working sets.<br />"), new RowData(1, 35));
		add(filterPanel, new RowData(250, 25));
		add(listContainer, new RowData(1, 1));
		add(pagingBar, new RowData(1, 30));
		
		addButton(new Button("Select", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				ArrayList<WorkingSet> sets = new ArrayList<WorkingSet>();
				for( WSModel wsModel : binder.getCheckedSelection() )
					sets.add(wsModel.getWorkingSet());
				
				listener.handleEvent(sets);
			}
		}));
		addButton(new Button("Cancel", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				listener.handleEvent(new ArrayList<WorkingSet>());
			}
		}));
		
		for (WorkingSet cur : workingSets)
			if (AuthorizationCache.impl.hasRight(AuthorizableObject.GRANT, cur))
				pagingLoader.getFullList().add(new WSModel(cur));
		
				
//		store.sort("name", SortDir.ASC);
		ArrayUtils.quicksort(pagingLoader.getFullList(), new Comparator<WSModel>() {
			public int compare(WSModel o1, WSModel o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		pagingLoader.getPagingLoader().load();
		layout();
	}
}
