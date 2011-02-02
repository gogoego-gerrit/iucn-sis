package org.iucn.sis.client.panels;

import org.iucn.sis.client.api.utils.MemoryProxy;

import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.data.BasePagingLoadResult;
import com.extjs.gxt.ui.client.data.BasePagingLoader;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Layout;
import com.extjs.gxt.ui.client.widget.grid.GridSelectionModel;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.extjs.gxt.ui.client.widget.toolbar.PagingToolBar;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.util.extjs.client.WindowUtils;
import com.solertium.util.gwt.ui.DrawsLazily;

public abstract class PagingMonkeyNavigatorPanel<T extends ModelData> extends MonkeyNavigatorPanel {
	
	private BasePagingLoader<BasePagingLoadResult<T>> loader;
	private MemoryProxy<T> proxy;
	
	private int pageCount;
	
	public PagingMonkeyNavigatorPanel() {
		this(new FillLayout());
	}
	
	public PagingMonkeyNavigatorPanel(Layout layout) {
		super(layout);
		
		proxy = new MemoryProxy<T>();
		
		loader = 
			new BasePagingLoader<BasePagingLoadResult<T>>(proxy);
		loader.setRemoteSort(false);
		
		pageCount = 25;
		
		proxy.setSort(false);
	}
	
	public void setPageCount(int pageCount) {
		this.pageCount = pageCount;
	}
	
	protected ListStore<T> getStoreInstance() {
		return new ListStore<T>(loader);
	}
	
	public MemoryProxy<T> getProxy() {
		return proxy;
	}
	
	public BasePagingLoader<BasePagingLoadResult<T>> getLoader() {
		return loader;
	}
	
	protected PagingToolBar getPagingToolbar() {
		final PagingToolBar paging = new PagingToolBar(pageCount);
		paging.bind(loader);
		
		return paging;
	}
	
	protected void refresh(final DrawsLazily.DoneDrawingCallback callback) {
		getStore(new GenericCallback<ListStore<T>>() {
			public void onFailure(Throwable caught) {
				WindowUtils.errorAlert("Could not refresh, please try again later.");
				if (callback != null)
					callback.isDrawn();
			}
			public void onSuccess(ListStore<T> result) {
				proxy.setStore(result);
				
				loader.load(0, pageCount);
				
				refreshView();
				/*try {
					grid.getView().refresh(false);
				} catch (Throwable e) {
				}*/
				
				WindowUtils.hideLoadingAlert();
				if (callback != null)
					callback.isDrawn();
			}
		});
	}
	
	protected abstract void refreshView();
	
	protected abstract void getStore(final GenericCallback<ListStore<T>> callback);
	
	protected static class NavigationGridSelectionModel<T> extends GridSelectionModel<NavigationModelData<T>> {
		
		public NavigationGridSelectionModel() {
			super();
			setSelectionMode(SelectionMode.SINGLE);
		}
		
		public void highlight(NavigationModelData<T> model) {
			doSingleSelect(model, true);
		}
	}
	
	@SuppressWarnings("unchecked")
	protected static class NavigationModelData<T> extends BaseModelData {
		
		private static final long serialVersionUID = 1L;
		
		private T model;
		
		public NavigationModelData(T model) {
			this.model = model;
		}
		
		public T getModel() {
			return model;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((model == null) ? 0 : model.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			NavigationModelData other = (NavigationModelData) obj;
			if (model == null) {
				if (other.model != null)
					return false;
			} else if (!model.equals(other.model))
				return false;
			return true;
		}
		
	}

}
