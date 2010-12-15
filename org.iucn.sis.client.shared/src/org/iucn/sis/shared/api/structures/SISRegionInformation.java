package org.iucn.sis.shared.api.structures;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.iucn.sis.client.api.caches.RegionCache;
import org.iucn.sis.client.api.ui.models.region.RegionModel;
import org.iucn.sis.shared.api.debug.Debug;
import org.iucn.sis.shared.api.models.Field;
import org.iucn.sis.shared.api.models.Region;
import org.iucn.sis.shared.api.models.fields.RegionField;
import org.iucn.sis.shared.api.utils.CanonicalNames;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.Style.VerticalAlignment;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.IconButtonEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.store.StoreFilter;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.VerticalPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.IconButton;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;
import com.solertium.util.extjs.client.WindowUtils;

public class SISRegionInformation extends Structure<Field> {

	private HashMap<Integer, RegionModel> idToModel;
	private HashMap<ComboBox<RegionModel>, RegionModel> boxesToSelected;
	private ListStore<RegionModel> store;
	
	private CheckBox endemic;
	
	private List<Integer> regionsSelected;
	//private Button add;
	private VerticalPanel innerPanel;
	
	public SISRegionInformation(String struct, String descript, String structID) {
		super(struct, descript, null);
		// displayPanel = new HorizontalPanel();
		buildContentPanel(Orientation.VERTICAL);
	}

	@Override
	public boolean hasChanged(Field field) {
		Field fauxRegions = new Field(CanonicalNames.RegionInformation, null);
		
		save(null, fauxRegions);
		
		if (field == null) {
			boolean childHasData = fauxRegions.hasData();
			if (childHasData)
				Debug.println("HasChanged in RegionInfo: DB has null value, but child hasData, there are {0} primitive fields: \n{1}", fauxRegions.getPrimitiveField().size(), fauxRegions.getKeyToPrimitiveFields().keySet());
			else
				Debug.println("HasChanged in RegionInfo: DB has null value, child has no data, no changes.");
			return childHasData;
		}
		
		RegionField dbProxy = new RegionField(field);
		RegionField localProxy = new RegionField(fauxRegions);
		
		Debug.println("DB Data: endemic: {0}, regions: {1}", dbProxy.isEndemic(), dbProxy.getRegionIDs());
		Debug.println("Local Data: endemic: {0}, regions: {1}", localProxy.isEndemic(), localProxy.getRegionIDs());
		
		return !(dbProxy.isEndemic() == localProxy.isEndemic() && 
			dbProxy.getRegionIDs().containsAll(localProxy.getRegionIDs()) && 
			dbProxy.getRegionIDs().size() == localProxy.getRegionIDs().size());
	}
	
	@Override
	public void save(Field parent, Field field) {
		final List<Integer> list = new ArrayList<Integer>();
		for (String data : getData().split(",")) {
			try {
				list.add(Integer.parseInt(data));
			} catch (Exception e) {
				continue;
			}
		}
		
		if (field == null) {
			//This is a problem, why is this ever the case?  oh well...  
			//doing explicit set, and i know certain info...
			field = new Field(CanonicalNames.RegionInformation, null);
			field.setParent(null);
		}
		
		RegionField proxy = new RegionField(field);
		proxy.setEndemic(endemic.getValue());
		proxy.setRegions(list);
	}
	
	/**
	 * Adds a region dropdown, defaulting the selection of the drop down to the
	 * ID selectedID. If you don't want anything selected, pass in null.
	 * 
	 * @param selectedID
	 */
	private void addRegionDropdown(Integer selectedID) {
		ComboBox<RegionModel> box = buildNewComboBox();
		HorizontalPanel panel = new HorizontalPanel();
		panel.setSpacing(1);
		panel.setVerticalAlign(VerticalAlignment.BOTTOM);
		panel.add(box);
		panel.insert(buildDeleteButton(box, panel), 0);
		box.setAutoValidate(true);

		if (selectedID != null) {
			ArrayList<RegionModel> selectedRegion = new ArrayList<RegionModel>();
			selectedRegion.add(idToModel.get(selectedID));
			box.setSelection(selectedRegion);

			boxesToSelected.put(box, idToModel.get(selectedID));
		}

		checkEndemicEnabled();
		innerPanel.add(panel);
	}

	private Widget buildDeleteButton(final ComboBox<RegionModel> box, final LayoutContainer container) {
		return new IconButton("icon-world-delete", new SelectionListener<IconButtonEvent>() {
			public void componentSelected(IconButtonEvent ce) {
				WindowUtils.confirmAlert("Delete Region", "Are you sure you want to " + "delete this region?",
						new WindowUtils.SimpleMessageBoxListener() {
					public void onYes() {
						if ( boxesToSelected.size() > 1 ) {
							RegionModel removed = boxesToSelected.remove(box);
							if(removed != null && removed.getRegion().getId() == Region.GLOBAL_ID)
								endemic.setEnabled(true);
							container.removeFromParent();
						} else
							WindowUtils.errorAlert("Cannot Delete", "You must have " +
							"at least one locality selected.");
					}
				});
			}
		});
	}

	private ComboBox<RegionModel> buildNewComboBox() {
		final ComboBox<RegionModel> box = new ComboBox<RegionModel>();
		box.setStore(store);
		box.addSelectionChangedListener(new SelectionChangedListener<RegionModel>() {
			@Override
			public void selectionChanged(SelectionChangedEvent<RegionModel> se) {
				boxesToSelected.put(box, se.getSelectedItem());
				checkEndemicEnabled();
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

	@Override
	public void clearData() {
		regionsSelected = new ArrayList<Integer>();
		endemic.setValue(false);
		endemic.setEnabled(true);
		refreshUI();
	}
	
	protected void checkEndemicEnabled() {
		for( Entry<ComboBox<RegionModel>, RegionModel> cur : boxesToSelected.entrySet() ) {
			if (cur.getValue() != null && cur.getValue().getRegion().getId() == Region.GLOBAL_ID) {
					endemic.setValue(true);
					endemic.setEnabled(false);
					return;
				
			}
		}
		endemic.setEnabled(true);
	}
	
	protected void refreshUI() {
		boxesToSelected.clear();
		innerPanel.removeAll();
		
		HashMap<Integer, String> alreadySeen = new HashMap<Integer, String>();
		for (Integer cur : regionsSelected) {
			if (!alreadySeen.containsKey(cur)) {
				addRegionDropdown(cur);
				alreadySeen.put(cur, "");
			}
		}
		
		endemic.setEnabled(alreadySeen.containsKey(Region.GLOBAL_ID));
		innerPanel.layout();
	}
	
	@Override
	public Widget createLabel() {
		clearDisplayPanel();
		displayPanel.add(descriptionLabel);
		
		final HorizontalPanel iconButton = new HorizontalPanel();
		iconButton.setSpacing(1);
		iconButton.add(new IconButton("icon-world-add", new SelectionListener<IconButtonEvent>() {
			public void componentSelected(IconButtonEvent ce) {
				addRegionDropdown(null);
				innerPanel.layout();
			}
		}));
		iconButton.add(new Button("Add New Region", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				addRegionDropdown(null);
				innerPanel.layout();
			}
		}));
		
		displayPanel.add(iconButton);

		refreshUI();
		displayPanel.add(innerPanel);
		displayPanel.add(new HTML("<br />"));
		displayPanel.add(endemic);
		
		return displayPanel;
	}

	@Override
	public Widget createViewOnlyLabel() {
		clearDisplayPanel();
		displayPanel.add(descriptionLabel);

		for (Integer cur : regionsSelected)
			displayPanel.add(new HTML(RegionCache.impl.getRegionName(cur)));

		return displayPanel;
	}

	@Override
	public void createWidget() {
		descriptionLabel = new HTML(description);
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
		for (Region cur : RegionCache.impl.getRegions()) {
			RegionModel model = new RegionModel(cur);
			store.add(model);
			idToModel.put(cur.getId(), model);
		}
		
		endemic = new CheckBox("Is Endemic? ");
		
		innerPanel = new VerticalPanel();
	}

	/**
	 * Returns an ArrayList of descriptions (as Strings) for this structure, and
	 * if it contains multiples structures, all of those, in order.
	 */
	@Override
	public ArrayList<String> extractDescriptions() {
		ArrayList<String> ret = new ArrayList<String>();
		ret.add(description);
		return ret;
	}
	
	@Override
	public List<ClassificationInfo> getClassificationInfo() {
		final ArrayList<ClassificationInfo> list = new ArrayList<ClassificationInfo>();
		list.add(new ClassificationInfo(description, getData()));
		return list;
	}

	@Override
	public String getData() {
		if (boxesToSelected.size() <= 0)
			return "";

		HashMap<Integer, String> alreadySeen = new HashMap<Integer, String>();
		
		StringBuilder select = new StringBuilder();
		for (Entry<ComboBox<RegionModel>, RegionModel> curSelected : boxesToSelected.entrySet()) {
			if (curSelected.getValue() != null) {
				Integer identifier = curSelected.getValue().getRegion().getId();
				if (!alreadySeen.containsKey(identifier)) {
					if (!alreadySeen.isEmpty())
						select.append(",");
						
					select.append(identifier);
					alreadySeen.put(identifier, "");
				}
			}
		}

		return select.toString();
	}

	/**
	 * Pass in the raw data from an Assessment object, and this will return
	 * it in happy, displayable String form
	 * 
	 * @return ArrayList of Strings, having converted the rawData to nicely
	 *         displayable String data. Happy days!
	 */
	@Override
	public int getDisplayableData(ArrayList<String> rawData, ArrayList<String> prettyData, int offset) {
		prettyData.add(offset, rawData.get(offset));
		return ++offset;
	}
	
	@Override
	public void setData(Field field) {
		RegionField proxy = new RegionField(field);
		
		regionsSelected = proxy.getRegionIDs();
		endemic.setValue(proxy.isEndemic());
		
		refreshUI();
	}

	@Override
	public void setEnabled(boolean isEnabled) {
		for (Entry<ComboBox<RegionModel>, RegionModel> curSelected : boxesToSelected.entrySet()) {
			curSelected.getKey().setEnabled(isEnabled);
		}
	}

}
