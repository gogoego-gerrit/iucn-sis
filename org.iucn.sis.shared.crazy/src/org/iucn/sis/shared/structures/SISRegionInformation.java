package org.iucn.sis.shared.structures;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import org.iucn.sis.shared.data.assessments.AssessmentData;
import org.iucn.sis.shared.data.assessments.Region;
import org.iucn.sis.shared.data.assessments.RegionCache;
import org.iucn.sis.shared.data.assessments.RegionCache.RegionModel;
import org.iucn.sis.shared.xml.XMLUtils;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.Style.VerticalAlignment;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.event.WindowEvent;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.store.StoreFilter;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.VerticalPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;
import com.solertium.util.extjs.client.WindowUtils;

public class SISRegionInformation extends Structure {

	private HashMap<String, RegionModel> idToModel;
	private HashMap<ComboBox<RegionModel>, RegionModel> boxesToSelected;
	private ListStore<RegionModel> store;
	
	private CheckBox endemic;
	
	private String regionsSelected = "";
	private Button add;
	private VerticalPanel innerPanel;
	
	public SISRegionInformation(String struct, String descript) {
		super(struct, descript);
		// displayPanel = new HorizontalPanel();
		buildContentPanel(Orientation.VERTICAL);
	}

	/**
	 * Adds a region dropdown, defaulting the selection of the drop down to the
	 * ID selectedID. If you don't want anything selected, pass in null.
	 * 
	 * @param selectedID
	 */
	private void addRegionDropdown(String selectedID) {
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

	private Button buildDeleteButton(final ComboBox<RegionModel> box, final LayoutContainer container) {
		Button delete = new Button("", new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				WindowUtils.confirmAlert("Delete Region", "Are you sure you want to " + "delete this region?",
						new Listener<MessageBoxEvent>() {
							public void handleEvent(MessageBoxEvent be) {
								if (be.getButtonClicked().getText().equalsIgnoreCase("yes")) {
									if( boxesToSelected.size() > 1 ) {
									RegionModel removed = boxesToSelected.remove(box);
									if(removed != null && removed.getRegion().getId().equals(AssessmentData.GLOBAL_ID))
										endemic.setEnabled(true);
									container.removeFromParent();
									} else
										WindowUtils.errorAlert("Cannot Delete", "You must have " +
												"at least one locality selected.");
								}
							}
						});
			}
		});
		delete.setIconStyle("icon-world-delete");
		return delete;
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
		regionsSelected = "";
		endemic.setChecked(false);
		endemic.setEnabled(true);
		refreshUI();
	}
	
	protected void checkEndemicEnabled() {
		for( Entry<ComboBox<RegionModel>, RegionModel> cur : boxesToSelected.entrySet() ) {
			if (cur.getValue() != null && cur.getValue().getRegion().getId().equals(
						AssessmentData.GLOBAL_ID)) {
					endemic.setChecked(true);
					endemic.setEnabled(false);
					return;
				
			}
		}
		endemic.setEnabled(true);
	}
	
	protected void refreshUI() {
		boxesToSelected.clear();
		innerPanel.removeAll();
		
		String[] selected;
		if (regionsSelected.indexOf(",") > 0)
			selected = regionsSelected.split(",");
		else
			selected = new String[] { regionsSelected };

		HashMap<String, String> alreadySeen = new HashMap<String, String>();
		for (String cur : selected) {
			if (!alreadySeen.containsKey(cur)) {
				addRegionDropdown(cur);
				alreadySeen.put(cur, "");
			}
		}
		
		endemic.setEnabled(alreadySeen.containsKey(AssessmentData.GLOBAL_ID));
		innerPanel.layout();
	}
	
	@Override
	public Widget createLabel() {
		clearDisplayPanel();
		displayPanel.add(descriptionLabel);
		add.setIconStyle("icon-world-add");
		displayPanel.add(add);

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

		String[] selected;
		if (regionsSelected.indexOf(",") > 0)
			selected = regionsSelected.split(",");
		else
			selected = new String[] { regionsSelected };

		for (String cur : selected)
			displayPanel.add(new HTML(RegionCache.impl.getRegionName(cur)));

		return displayPanel;
	}

	@Override
	public void createWidget() {
		descriptionLabel = new HTML(description);
		boxesToSelected = new HashMap<ComboBox<RegionModel>, RegionModel>();
		idToModel = new HashMap<String, RegionModel>();
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
		
		add = new Button("Add New Region", new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				addRegionDropdown(null);
				innerPanel.layout();
			}
		});
		
		endemic = new CheckBox("Is Endemic? ");
		
		innerPanel = new VerticalPanel();
	}

	/**
	 * Returns an ArrayList of descriptions (as Strings) for this structure, and
	 * if it contains multiples structures, all of those, in order.
	 */
	@Override
	public ArrayList extractDescriptions() {
		ArrayList ret = new ArrayList();
		ret.add(description);
		return ret;
	}

	@Override
	public Object getData() {
		if (boxesToSelected.size() <= 0)
			return "";

		HashMap<String, String> alreadySeen = new HashMap<String, String>();
		StringBuffer select = new StringBuffer();
		for (Entry<ComboBox<RegionModel>, RegionModel> curSelected : boxesToSelected.entrySet()) {
			if (curSelected.getValue() != null) {
				if (!alreadySeen.containsKey(curSelected.getValue().getRegion().getId())) {
					select.append(curSelected.getValue().getRegion().getId());
					select.append(",");

					alreadySeen.put(curSelected.getValue().getRegion().getId(), "");
				}
			}
		}

		if( select.length() > 0 )
			regionsSelected = select.deleteCharAt(select.length() - 1).toString();
		else
			regionsSelected = "";
		
		return regionsSelected;
	}

	/**
	 * Pass in the raw data from an AssessmentData object, and this will return
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
	public int setData(ArrayList dataList, int dataOffset) {
		super.setData(dataList, dataOffset);
		regionsSelected = XMLUtils.cleanFromXML(dataList.get(dataOffset++).toString());
		endemic.setChecked(new Boolean(XMLUtils.cleanFromXML(dataList.get(dataOffset).toString())).booleanValue());
		refreshUI();
		return ++dataOffset;
	}

	@Override
	public void setEnabled(boolean isEnabled) {
		for (Entry<ComboBox<RegionModel>, RegionModel> curSelected : boxesToSelected.entrySet()) {
			curSelected.getKey().setEnabled(isEnabled);
		}
	}

	@Override
	public String toXML() {
		String ret = "<structure>" + getData() + "</structure>\n";
		ret += "<structure>" + endemic.isChecked() + "</structure>\n";
		return ret;
	}
}
