package org.iucn.sis.client.referenceui;

import java.util.List;

import com.extjs.gxt.ui.client.data.BasePagingLoader;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.extjs.gxt.ui.client.data.RpcProxy;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.solertium.util.extjs.client.PagingLoaderFilter;

public abstract class GenericLazyPagingLoader<T extends ModelData> {

	protected RpcProxy<List<T>> proxy;
	protected BasePagingLoader<PagingLoadResult<T>> pagingLoader;
	protected int total;

	private PagingLoaderFilter<T> filter;
	private List<T> filtered;
	
	public GenericLazyPagingLoader() {

		proxy = new RpcProxy<List<T>>() {
			@Override
			protected void load(Object loadConfig, final AsyncCallback<List<T>> callback) {
				try {
					final PagingLoadConfig config = (PagingLoadConfig)loadConfig;
					fetchSublist(config.getOffset(), new AsyncCallback<List<T>>() {
						public void onFailure(Throwable caught) {

						}

						public void onSuccess(List<T> result) {
//							final BasePagingLoadResult<T> results = new BasePagingLoadResult<T>(
//									result, config.getOffset(), total);

							callback.onSuccess(result);
						}
					});

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		pagingLoader = new BasePagingLoader<PagingLoadResult<T>>(proxy);
		pagingLoader.setRemoteSort(false);
	}

	/**
	 * Performs a fetch, to get a sublist of items.
	 * 
	 * NOTE: Be sure to set the local data member <b>int total</b> to the total
	 * number of possible items.
	 * 
	 * @param start
	 *            - start index of subset
	 * @param end
	 *            - end index of subset
	 * @return the subset as a List
	 */
	public abstract void fetchSublist(int start, AsyncCallback<List<T>> showResultsCallback);

	public BasePagingLoader<PagingLoadResult<T>> getPagingLoader() {
		return pagingLoader;
	}

	public RpcProxy<List<T>> getProxy() {
		return proxy;
	}

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}
}
