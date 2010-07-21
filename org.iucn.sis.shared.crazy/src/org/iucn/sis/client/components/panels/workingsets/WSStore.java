package org.iucn.sis.client.components.panels.workingsets;

import org.iucn.sis.shared.data.WorkingSetCache;
import org.iucn.sis.shared.data.WorkingSetData;

import com.extjs.gxt.ui.client.Style.SortDir;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.StoreSorter;
import com.solertium.util.portable.PortableAlphanumericComparator;

public class WSStore extends ListStore<WSModel> {

	protected static WSStore store;
	protected String filterProperty = null;

	public static WSStore getStore() {

		if (store == null) {

			store = new WSStore();
			store.setStoreSorter(new StoreSorter(new PortableAlphanumericComparator()));
			store.update();

		}
		return store;
	}

	protected WSStore() {
		super();
	}

	public void setFilterProperty(String filterProperty) {
		this.filterProperty = filterProperty;
	}
	
	public void update() {
		clearFilters();
		removeAll();
		for (WorkingSetData data : WorkingSetCache.impl.getWorkingSets().values()) {
			WSModel model = new WSModel(data);
			add(model);
		}
		commitChanges();
		sort("name", SortDir.ASC);
		
		if( filterProperty != null )
			store.filter(filterProperty);
	}

}
