package org.iucn.sis.client.api.utils;

import java.util.ArrayList;
import java.util.List;

import org.iucn.sis.shared.api.debug.Debug;

import com.extjs.gxt.ui.client.Style.SortDir;
import com.extjs.gxt.ui.client.data.BasePagingLoadResult;
import com.extjs.gxt.ui.client.data.DataProxy;
import com.extjs.gxt.ui.client.data.DataReader;
import com.extjs.gxt.ui.client.data.FilterConfig;
import com.extjs.gxt.ui.client.data.FilterPagingLoadConfig;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.store.StoreFilter;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.solertium.util.extjs.client.WindowUtils;

public class MemoryProxy<T extends ModelData> implements DataProxy<BasePagingLoadResult<T>> {
	
	private ListStore<T> store;
	private SortDir currentSortDir;
	private String currentSortField;
	
	private StoreFilter<T> filter;
	
	private boolean sort;
	
	public MemoryProxy() {
		sort = true;
	}
	
	public void setStore(ListStore<T> store) {
		this.store = store;
	}
	
	public void setSort(boolean sort) {
		this.sort = sort;
	}
	
	public ListStore<T> getStore() {
		return store;
	}
	
	public void setFilter(StoreFilter<T> filter) {
		this.filter = filter;
	}
	
	public void filter(String property, String match) {
		if (filter == null) {
			filter = new SimpleEqualityFilter();
			((SimpleEqualityFilter)filter).setMatchingValue(match);
		}
		if (store.getFilters() == null || !store.getFilters().contains(filter))
			store.addFilter(filter);
		Debug.println("Filtering with {0}", store.getFilters());
		store.filter(property);
	}
	
	public void load(DataReader<BasePagingLoadResult<T>> reader,
			final Object loadConfigObj, final AsyncCallback<BasePagingLoadResult<T>> callback) {
		
		//List<T> list = data.getData();
		//WTF?
		final PagingLoadConfig loadConfig = (PagingLoadConfig)loadConfigObj;
		boolean needToSort = false;
		if (currentSortField == null || !loadConfig.getSortField().equals(currentSortField)) {
			currentSortField = loadConfig.getSortField();
			needToSort = true;
		}
		if (currentSortDir == null || !loadConfig.getSortDir().equals(currentSortDir)) {
			currentSortDir = loadConfig.getSortDir();
			needToSort = true;
		}
		
		final Command command = new Command() {
			public void execute() {
				ListStore<T> storeToPage;
				
				if (loadConfig instanceof FilterPagingLoadConfig) {
					List<FilterConfig> filters = ((FilterPagingLoadConfig)loadConfig).getFilterConfigs();
					if (filters == null || filters.isEmpty())
						storeToPage = store;
					else {
						storeToPage = new ListStore<T>();
						for (T model : store.getModels())
							if (select(model, filters))
								storeToPage.add(model);
					}
				}
				else
					storeToPage = store;
				
				List<T> retData = new ArrayList<T>();
				for (int i = loadConfig.getOffset(); i < (loadConfig.getOffset() + loadConfig.getLimit()) && i < storeToPage.getCount(); i++) {
					retData.add(storeToPage.getAt(i));
				}
				callback.onSuccess(new BasePagingLoadResult<T>(retData, loadConfig.getOffset(), storeToPage.getCount()));		
			}
		};
		
		if (sort && needToSort) {
			WindowUtils.showLoadingAlert("Sorting...");
			DeferredCommand.addPause();
			DeferredCommand.addCommand(new Command() {
				public void execute() {
					store.sort(currentSortField, currentSortDir);
					WindowUtils.hideLoadingAlert();
					command.execute();
				}
			});
		}
		else
			command.execute();
	}
	
	private boolean select(T model, List<FilterConfig> filters) {
		for (FilterConfig filter : filters) {
			if (filter.isFiltered(model, filter.getValue(), filter.getComparison(), model.get(filter.getField())))
				return false;
		}
		return true;
	}
	
	/*public void load(
			DataReader<BasePagingLoadConfig, BasePagingLoadResult<T>> reader,
			BasePagingLoadConfig loadConfig,
			AsyncCallback<BasePagingLoadResult<T>> callback) {
		
		List<T> retData = new ArrayList<T>();
		List<T> list = data.getData();
		for (int i = loadConfig.getOffset(); i < (loadConfig.getOffset() + loadConfig.getLimit()) && i < list.size(); i++) {
			retData.add(list.get(i));
		}
		callback.onSuccess(new BasePagingLoadResult<T>(retData, loadConfig.getOffset(), list.size()));
	}*/
	
	private class SimpleEqualityFilter implements StoreFilter<T> {
		
		private String matchingValue = null;
		
		public void setMatchingValue(String matchingValue) {
			this.matchingValue = matchingValue;
		}
		
		public boolean select(Store<T> store, T parent, T item, String property) {
			if (matchingValue == null)
				return true;
			
			return matchingValue.equals(item.get(property));
		}
		
	}
	
}
