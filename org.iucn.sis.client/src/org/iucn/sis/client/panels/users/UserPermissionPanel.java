package org.iucn.sis.client.panels.users;

import java.util.ArrayList;
import java.util.List;

import org.iucn.sis.client.api.caches.AuthorizationCache;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.SortDir;
import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.data.ModelKeyProvider;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.store.StoreSorter;
import com.extjs.gxt.ui.client.widget.CheckBoxListView;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.solertium.util.events.ComplexListener;
import com.solertium.util.extjs.client.WindowUtils;
import com.solertium.util.portable.PortableAlphanumericComparator;

public class UserPermissionPanel extends Window {
	
	private ComplexListener<List<String>> saveListener;
	private ListStore<BaseModelData> store;
	
	private CheckBoxListView<BaseModelData> view;
	private List<String> selection;
	
	public UserPermissionPanel() {
		super();
		setSize(500, 500);
		setHeading("Edit Permissions");
		setLayout(new FillLayout());
		setButtonAlign(HorizontalAlignment.CENTER);
		
		updateStore();
		
		addButton(new Button("Save", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				hide();
				
				final List<String> checked = new ArrayList<String>();
				for (BaseModelData model : view.getChecked())
					checked.add((String)model.get("value"));
				
				saveListener.handleEvent(checked);
			}
		}));
		addButton(new Button("Cancel", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				hide();
			}
		}));
	}

	public void setSaveListener(ComplexListener<List<String>> saveListener) {
		this.saveListener = saveListener;
	}
	
	public void setSelection(List<String> selection) {
		this.selection = selection;
	}
	
	public void updateStore() {
		final ListStore<BaseModelData> store = new ListStore<BaseModelData>();
		store.setStoreSorter(new PermissionStoreSorter());
		store.setKeyProvider(new ModelKeyProvider<BaseModelData>() {
			public String getKey(BaseModelData model) {
				return model.get("value");
			}
		});
		for (String group : AuthorizationCache.impl.getGroups().keySet()) {
			if (!group.matches("^ws\\d+.*")) {
				BaseModelData model = new BaseModelData();
				model.set("text", group);
				model.set("value", group);
				
				store.add(model);
			}
		}
		store.sort("name", SortDir.ASC);
		
		this.store = store;
		
		view = new CheckBoxListView<BaseModelData>();
		view.setStore(store);
	}
	
	private void draw() {
		for (BaseModelData model : store.getModels()) {
			String value = model.get("value");
			view.setChecked(model, selection != null && selection.contains(value));
		}
	
		removeAll();
		add(view);
	}
	
	@Override
	public void show() {
		WindowUtils.showLoadingAlert("Loading all permission groups...");
		draw();
		super.show();
		WindowUtils.hideLoadingAlert();
	}
	
	public static class PermissionStoreSorter extends StoreSorter<BaseModelData> {
		
		private final PortableAlphanumericComparator comparator = 
			new PortableAlphanumericComparator();
		
		@Override
		public int compare(Store<BaseModelData> store, BaseModelData m1,
				BaseModelData m2, String property) {
			String v1 = m1.get(property);
			String v2 = m2.get(property);
			if (v1 == null)
				return 1;
			else if (v2 == null)
				return -1;
			else
				return comparator.compare(v1, v2);
		}
	}
	
}
