package org.iucn.sis.client.panels.region;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.iucn.sis.client.api.caches.RegionCache;
import org.iucn.sis.client.api.ui.models.region.RegionModel;
import org.iucn.sis.shared.api.models.Region;

import com.extjs.gxt.ui.client.Style.VerticalAlignment;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.store.StoreFilter;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.VerticalPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.solertium.util.extjs.client.WindowUtils;

public class AddRegionPanel extends LayoutContainer{

	private final HashMap<ComboBox<RegionModel>, RegionModel> boxesToSelected;
	private final HashMap<Integer, RegionModel> idToModel;
	private final VerticalPanel innerPanel;
	private final ListStore<RegionModel> store;
	
	private Collection<Region> userSelection;

	public AddRegionPanel() {
		innerPanel = new VerticalPanel();
		boxesToSelected = new HashMap<ComboBox<RegionModel>, RegionModel>();
		idToModel = new HashMap<Integer, RegionModel>();
		store = new ListStore<RegionModel>();
		store.addFilter(new StoreFilter<RegionModel>() {
			public boolean select(Store<RegionModel> store, RegionModel parent, RegionModel item, String property) {
				if (boxesToSelected.containsValue(item))
					return false;
				else
					return true;
			}
		});
	}



	protected void refreshStore() {
		store.removeAll();
		idToModel.clear();
		for (Region cur : RegionCache.impl.getRegions()) {
			RegionModel model = new RegionModel(cur);
			store.add(model);
			idToModel.put(cur.getId(), model);
		}
	}


	/**
	 * Adds a region dropdown, defaulting the selection of the drop down to the
	 * ID selectedID. If you don't want anything selected, pass in null.
	 * 
	 * @param selectedID
	 */
	private void addRegionDropdown(Region region) {
		RegionModel model = idToModel.get(region.getId());
		if (model == null)
			return;
		
		ComboBox<RegionModel> box = buildNewComboBox();
		box.setAutoValidate(true);
		box.setValue(model);
		
		HorizontalPanel panel = new HorizontalPanel();
		panel.setSpacing(1);
		panel.setVerticalAlign(VerticalAlignment.MIDDLE);
		panel.add(box);
		panel.insert(buildDeleteButton(box, panel), 0);
		
		boxesToSelected.put(box, model);		

		innerPanel.add(panel);
	}

	private ComboBox<RegionModel> buildNewComboBox() {
		final ComboBox<RegionModel> box = new ComboBox<RegionModel>();
		box.setStore(store);
		box.addSelectionChangedListener(new SelectionChangedListener<RegionModel>() {
			@Override
			public void selectionChanged(SelectionChangedEvent<RegionModel> se) {
				boxesToSelected.put(box, se.getSelectedItem());
			}
		});
		box.setAllowBlank(false);
		box.setEmptyText("Select a region...");
		box.setDisplayField("name");
		box.setTypeAhead(true);
		box.setTriggerAction(TriggerAction.ALL);
		boxesToSelected.put(box, null);

		return box;
	}

	private Button buildDeleteButton(final ComboBox<RegionModel> box, final LayoutContainer container) {
		Button delete = new Button("", new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				WindowUtils.confirmAlert("Delete Region", "Are you sure you want to " + "delete this region?",
						new WindowUtils.SimpleMessageBoxListener() {
					public void onYes() {
						boxesToSelected.remove(box);
						container.removeFromParent();
					}
				});
			}
		});
		delete.setIconStyle("icon-world-delete");
		return delete;
	}

	public void refreshUI() {
		boxesToSelected.clear();
		innerPanel.removeAll();
		refreshStore();

		if (userSelection != null)
			for (Region region : userSelection)
				addRegionDropdown(region);
		
		innerPanel.layout();
	}

	public void clearData() {
		userSelection = null;
		refreshUI();
	}


	public void draw() {
		refreshUI();		
		RowLayout layout = new RowLayout();
		RowData bottom = new RowData();
		bottom.setMargins(new Margins(10,0,10,45));
		setLayout(layout);
		this.add(innerPanel, new RowData());
		this.add(new Button("Add New Region", new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				addRegionDropdown(null);
				innerPanel.layout();
			}
		}), bottom);
		layout();
	}

	public void setSelectedRegions(Collection<Region> regionsSelected) {
		this.userSelection = regionsSelected;
	}

	public HashMap<ComboBox<RegionModel>, RegionModel> getBoxesToSelected() {
		return boxesToSelected;
	}
	
	public List<Region> getSelectedRegions() {
		List<Region> regions = new ArrayList<Region>();
		for (Entry<ComboBox<RegionModel>, RegionModel> entry: getBoxesToSelected().entrySet())
			if (entry.getValue() != null)
				regions.add(entry.getValue().getRegion());
		
		return regions;		
	}

}
