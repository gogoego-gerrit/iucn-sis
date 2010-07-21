/*******************************************************************************
 * Copyright (C) 2007-2009 Solertium Corporation
 * 
 * This file is part of the open source GoGoEgo project.
 * 
 * Unless you have been granted a different license in writing by the
 * copyright holders for GoGoEgo, you may only modify or redistribute
 * this code under the terms of one of the following licenses:
 * 
 * 1) The Eclipse Public License, v.1.0
 *    http://www.eclipse.org/legal/epl-v10.html
 * 
 * 2) The GNU General Public License, version 2 or later
 *     http://www.gnu.org/licenses
 ******************************************************************************/
package com.solertium.util.extjs.client;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.data.BasePagingLoadResult;
import com.extjs.gxt.ui.client.data.BasePagingLoader;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.extjs.gxt.ui.client.data.PagingModelMemoryProxy;
import com.extjs.gxt.ui.client.data.RpcProxy;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * A way to store any type of ModelData in a paging manner. To access the PagingLoader
 * to pass to your List or Store, use the method getPagingLoader(). 
 * 
 * You can apply filters which will filter data out of the fullList until you
 * applyFilter(null) 
 * 
 * @author adam
 *
 * @param <T>
 */
public class GenericPagingLoader<T extends ModelData> {

	protected PagingModelMemoryProxy proxy;
//	protected RpcProxy<List<T>> proxy;
	protected BasePagingLoader<PagingLoadResult<T>> pagingLoader;
	private List<T> fullList;
	
	private PagingLoaderFilter<T> filter;
	private List<T> filtered;

	public GenericPagingLoader() {
		fullList = new ArrayList<T>();
		filtered = new ArrayList<T>(); 

		proxy = new PagingModelMemoryProxy(fullList);
		
//		proxy = new RpcProxy<List<T>>() {
//			@Override
//			protected void load(Object config, final AsyncCallback<List<T>> callback) {
//				try {
//					final PagingLoadConfig loadConfig = (PagingLoadConfig)config;
//					System.out.println("Load Config's offset " + loadConfig.getOffset() + " and limit " + loadConfig.getLimit());
//					int configBound = loadConfig.getLimit() + loadConfig.getOffset();
//					int storeBound = fullList.size();
//					int upperBound = (configBound <= storeBound) ? configBound : storeBound;
//
//					List<T> sublist = new ArrayList<T>();
//
//					for (int i = loadConfig.getOffset(); i < upperBound; i++)
//						sublist.add(fullList.get(i));
//
////					final BasePagingLoadResult<T> results = new BasePagingLoadResult<T>(sublist,
////							loadConfig.getOffset(), fullList.size());
//
//					callback.onSuccess(sublist);
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
//		};
		pagingLoader = new BasePagingLoader<PagingLoadResult<T>>(proxy);
		pagingLoader.setRemoteSort(false);
	}
	
	public void setFilter(PagingLoaderFilter<T> filter) {
		this.filter = filter;
	}
	
	/**
	 * Filter items from the list using the PagingLoaderFilter set via the addFilter method. If
	 * property argument is null, the full list is restored.
	 * 
	 * @param property to filter on
	 */
	public void applyFilter(String property) {
		fullList.addAll(filtered);
		filtered.clear();
		
		if( property != null && filter != null )
			for( T cur : fullList )
				if( filter.filter(cur, property) )
					filtered.add(cur);
		
		fullList.removeAll(filtered);
	}
		
	public void add(T item) {
		fullList.add(item);
	}
	
	public List<T> getFullList() {
		return fullList;
	}

	public BasePagingLoader<PagingLoadResult<T>> getPagingLoader() {
		return pagingLoader;
	}

	public PagingModelMemoryProxy getProxy() {
		return proxy;
	}
}
