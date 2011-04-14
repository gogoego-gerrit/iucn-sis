package org.iucn.sis.client.panels.permissions;

import java.util.List;
import java.util.Map.Entry;

import org.iucn.sis.client.api.caches.AuthorizationCache;
import org.iucn.sis.shared.api.models.PermissionGroup;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.Style.SortDir;
import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.dnd.ListViewDragSource;
import com.extjs.gxt.ui.client.dnd.ListViewDropTarget;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.StoreEvent;
import com.extjs.gxt.ui.client.store.StoreListener;
import com.extjs.gxt.ui.client.store.StoreSorter;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.ListView;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.solertium.util.portable.PortableAlphanumericComparator;

@SuppressWarnings("unused")
public class PermissionInheritenceUI extends LayoutContainer {

	private ListView<BaseModelData> unused;
	private ListStore<BaseModelData> unusedStore;
	private ListViewDragSource unusedDragSource;
	private ListViewDropTarget unusedDropTarget;
	
	private ListView<BaseModelData> used;
	private ListStore<BaseModelData> usedStore;
	private ListViewDragSource usedDragSource;
	private ListViewDropTarget usedDropTarget;
	
	private Html inheritsLabel;
	private String inheritsString;
	
	private LayoutContainer listContainer;
	
	public PermissionInheritenceUI() {
		setBorders(true);
		
		inheritsString = "";
		inheritsLabel = new Html("Inherits: ");
		
		unusedStore = new ListStore<BaseModelData>();
		unusedStore.setStoreSorter(new StoreSorter<BaseModelData>(new PortableAlphanumericComparator()));
		unused = new ListView<BaseModelData>(unusedStore);
		unused.setDisplayProperty("name");
		unusedDragSource = new ListViewDragSource(unused);
		unusedDropTarget = new ListViewDropTarget(unused);
		
		usedStore = new ListStore<BaseModelData>();
		usedStore.setStoreSorter(new StoreSorter<BaseModelData>(new PortableAlphanumericComparator()));
		usedStore.addStoreListener(new StoreListener<BaseModelData>() {
			public void storeAdd(StoreEvent<BaseModelData> se) {
				super.storeAdd(se);
				updateInheritsText();
			}
			
			public void storeRemove(StoreEvent<BaseModelData> se) {
				super.storeRemove(se);
				updateInheritsText();
			}
		});
		used = new ListView<BaseModelData>(usedStore);
		used.setDisplayProperty("name");
		usedDragSource = new ListViewDragSource(used);
		usedDropTarget = new ListViewDropTarget(used);
		
		setSize(350, 200);
		setLayout(new RowLayout(Orientation.VERTICAL));
		
		listContainer = new LayoutContainer();
		listContainer.setLayout(new RowLayout(Orientation.HORIZONTAL));
		listContainer.setSize(350, 175);
		
		RowData data = new RowData(.5, 1);
		data.setMargins( new Margins(5) );
		
		listContainer.add(unused, data);
		listContainer.add(used, data);
		
		add(inheritsLabel, new RowData(1, .1f));
		add(listContainer, new RowData(1, .9f));
	}
	
	private void updateInheritsText() {
		inheritsString = "Inherits: ";
		
		for( BaseModelData model : usedStore.getModels() )
			inheritsString += model.get("name") + " ";
		
		inheritsLabel.setHtml(inheritsString);
	}
	
	public void resetLists(List<String> selectedNames) {
		usedStore.removeAll();
		unusedStore.removeAll();
		usedStore.setFiresEvents(false);
		unusedStore.setFiresEvents(false);
		
		for( Entry<String, PermissionGroup> group : AuthorizationCache.impl.getGroups().entrySet() ) {
			if( !group.getValue().getName().matches("^ws\\d+.*") ) {
				BaseModelData data = new BaseModelData();
				data.set("name", group.getValue().getName());

				if( selectedNames.contains(group.getValue().getName()))
					usedStore.add(data);
				else
					unusedStore.add(data);
			}
		}
		
		usedStore.setFiresEvents(true);
		unusedStore.setFiresEvents(true);
		
		usedStore.sort("name", SortDir.ASC);
		unusedStore.sort("name", SortDir.ASC);
		
		used.refresh();
		unused.refresh();
		
		updateInheritsText();
	}
	
	public ListStore<BaseModelData> getUsedStore() {
		return usedStore;
	}
	
	public ListStore<BaseModelData> getUnusedStore() {
		return unusedStore;
	}
}
