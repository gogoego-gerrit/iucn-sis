package com.solertium.util.extjs.client;

import com.extjs.gxt.ui.client.data.ModelData;

public interface PagingLoaderFilter<T extends ModelData> {

	/**
	 * Returns true if the item is to be filtered out of the list. Check the item based on
	 * the supplied property.
	 *  
	 * @param item - T extends ModelData
	 * @param property - String property of item to filter on
	 * @return true to be filtered, false to leave it in the list
	 */
	public abstract boolean filter(T item, String property);

}