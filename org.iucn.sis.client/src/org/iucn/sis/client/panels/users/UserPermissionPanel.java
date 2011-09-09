package org.iucn.sis.client.panels.users;

import java.util.ArrayList;
import java.util.List;

import org.iucn.sis.client.api.caches.AuthorizationCache;
import org.iucn.sis.client.api.utils.BasicWindow;
import org.iucn.sis.shared.api.models.PermissionGroup;
import org.iucn.sis.shared.api.utils.CaseInsensitiveAlphanumericComparator;

import com.extjs.gxt.ui.client.Style.SortDir;
import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.data.ModelKeyProvider;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.store.StoreSorter;
import com.extjs.gxt.ui.client.widget.CheckBoxListView;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Timer;
import com.solertium.util.events.ComplexListener;
import com.solertium.util.extjs.client.WindowUtils;

public class UserPermissionPanel extends BasicWindow {
	
	private ComplexListener<List<String>> saveListener;
	private ListStore<BaseModelData> store;
	
	private CheckBoxListView<BaseModelData> view;
	private List<String> selection;
	
	public UserPermissionPanel() {
		super("Edit Permissions");
		setSize(500, 500);
		setLayout(new FillLayout());
		
		updateStore();
		
		addButton(new Button("Save", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				hide();
				
				final List<String> checked = new ArrayList<String>();
				for (BaseModelData model : view.getChecked())
					checked.add((String)model.get("value"));
				
				//Add filtered selections
				for (String group : selection)
					if (isFilteredOut(group))
						checked.add(group);
				
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
		store.setKeyProvider(new ModelKeyProvider<BaseModelData>() {
			public String getKey(BaseModelData model) {
				return model.get("value");
			}
		});
		for (PermissionGroup group : AuthorizationCache.impl.listGroups()) {
			if (!isFilteredOut(group.getName())) {
				BaseModelData model = new BaseModelData();
				model.set("text", group.getName());
				model.set("value", group.getName());
				
				store.add(model);
			}
		}
		
		this.store = store;
		
		view = new CheckBoxListView<BaseModelData>();
		view.setStore(store);
	}
	
	private boolean isFilteredOut(String groupName) {
		return groupName.matches("^ws\\d+.*");
	}
	
	private void draw() {
		for (BaseModelData model : store.getModels()) {
			String value = model.get("value");
			view.setChecked(model, selection != null && selection.contains(value));
		}
	
		store.setStoreSorter(new PermissionStoreSorter(selection));
		store.sort("text", SortDir.ASC);
		
		removeAll();
		add(view);
	}
	
	@Override
	public void show() {
		WindowUtils.showLoadingAlert("Loading all permission groups...");
		DeferredCommand.addPause();
		DeferredCommand.addCommand(new Command() {
			public void execute() {
				Timer t = new Timer() {
					public void run() {
						draw();
						open();
					}
				};
				t.schedule(1500);
			}
		});
	}
	
	private void open() {
		WindowUtils.hideLoadingAlert();
		super.show();
	}
	
	public static class PermissionStoreSorter extends StoreSorter<BaseModelData> {
		
		private final CaseInsensitiveAlphanumericComparator comparator;
		private final List<String> selection;
		
		public PermissionStoreSorter(List<String> selection) {
			super();
			this.selection = selection;
			this.comparator = new CaseInsensitiveAlphanumericComparator();
		}
		
		@Override
		public int compare(Store<BaseModelData> store, BaseModelData m1,
				BaseModelData m2, String property) {
			String v1 = m1.get(property);
			String v2 = m2.get(property);
			if (v1 == null)
				return 1;
			else if (v2 == null)
				return -1;
			else {
				if (selection.contains(v1))
					return -1;
				else if (selection.contains(v2))
					return 1;
				else
					return comparator.compare(v1, v2);
			}
		}
	}
	
}
