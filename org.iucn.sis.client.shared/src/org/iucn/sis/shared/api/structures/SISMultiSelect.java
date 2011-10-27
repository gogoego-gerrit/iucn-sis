package org.iucn.sis.shared.api.structures;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.iucn.sis.shared.api.data.LookupData;
import org.iucn.sis.shared.api.data.LookupData.LookupDataValue;
import org.iucn.sis.shared.api.models.PrimitiveField;
import org.iucn.sis.shared.api.models.primitivefields.ForeignKeyListPrimitiveField;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.DataListEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.DataList;
import com.extjs.gxt.ui.client.widget.DataListItem;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.solertium.util.portable.PortableAlphanumericComparator;

@SuppressWarnings("deprecation")
public class SISMultiSelect extends SISPrimitiveStructure<List<Integer>> implements DominantStructure<PrimitiveField<List<Integer>>> {

	public static final String LISTBOX = "listbox";
	private static final String LOOKUP = "org.iucn.sis.client.multiselect.lookup";

	private DataList list;
	private HashSet<String> checkedItems;

	public SISMultiSelect(String struct, String descript, String structID, Object data) {
		super(struct, descript, structID, data);
		buildContentPanel(Orientation.VERTICAL);
	}

	@Override
	protected PrimitiveField<List<Integer>> getNewPrimitiveField() {
		return new ForeignKeyListPrimitiveField(getId(), null);
	}
	
	@Override
	public void addListenerToActiveStructure(final ChangeHandler changeListener, ClickHandler clickListener,
			KeyUpHandler keyboardListener) {
		list.addListener(Events.CheckChange, new Listener<BaseEvent>() {
			public void handleEvent(BaseEvent be) {
				changeListener.onChange(null);
			}
		});

		DOM.setEventListener(list.getElement(), list);
	}

	@Override
	public void clearData() {
		if (list.isRendered())
			for (DataListItem checked : list.getChecked())
				checked.setChecked(false);

		checkedItems.clear();
	}

	@Override
	public Widget createLabel() {
		// clearDisplayPanel();
		VerticalPanel p = ((VerticalPanel) displayPanel);
		p.clear();
		p.setVerticalAlignment(HasVerticalAlignment.ALIGN_BOTTOM);

		displayPanel.add(descriptionLabel);
		displayPanel.add(list);
		return displayPanel;
	}

	@Override
	public Widget createViewOnlyLabel() {
		// clearDisplayPanel();
		VerticalPanel p = ((VerticalPanel) displayPanel);
		p.clear();
		p.setVerticalAlignment(HasVerticalAlignment.ALIGN_BOTTOM);

		displayPanel.add(descriptionLabel);

		List<DataListItem> checked = list.getChecked();

		if (checked.size() == 0) {
			displayPanel.add(new HTML("None Selected"));
		} else {
			String text = "";

			for (DataListItem item : checked) {
				LookupDataValue value = item.getData(LOOKUP);
				text += value.getLabel() + ", ";
			}
			displayPanel.add(new HTML(text.substring(0, text.length() - 1)));
		}

		return displayPanel;
	}

	@Override
	public void createWidget() {
		checkedItems = new HashSet<String>();
		
		descriptionLabel = new HTML(description);

		list = new DataList() {
			@Override
			protected void afterRender() {
				super.afterRender();
				for (String checked : checkedItems) {
					for (DataListItem item : list.getItems()) {
						LookupDataValue value = item.getData(LOOKUP);
						if (value != null && checked.equals(value.getID()))
							item.setChecked(true);
					}
				}
			}
		};
		list.setCheckable(true);
		list.setSelectionMode(SelectionMode.MULTI);
		list.addListener(Events.CheckChange, new Listener<DataListEvent>() {
			public void handleEvent(DataListEvent be) {
				DataListItem item = be.getItem();
				if (item != null) {
					LookupDataValue value = item.getData(LOOKUP);
					if (item.isChecked())
						checkedItems.add(value.getID());
					else
						checkedItems.remove(value.getID());
				}
			}
		});
		
		LookupData myData = (LookupData)data;
		
		for (LookupDataValue value : myData.getValues()) {
			DataListItem curItem = new DataListItem();
			curItem.setText(value.getLabel());
			curItem.setItemId(value.getID());
			curItem.setData(LOOKUP, value);

			list.add(curItem);
			
			if (myData.getDefaultValues().contains(value.getID()))
				checkedItems.add(value.getID());
		}
	}

	@Override
	public String getData() {
		final StringBuilder builder = new StringBuilder();

		final List<String> values = new ArrayList<String>();
		if (list.isVisible()) {
			for (DataListItem item : list.getChecked()) {
				LookupDataValue value = item.getData(LOOKUP);
				values.add(value.getID());
			}
		} else {
			for (String value : checkedItems)
				values.add(value);
		}

		Collections.sort(values, new PortableAlphanumericComparator());
		for (Iterator<String> iter = values.listIterator(); iter.hasNext(); )
			builder.append(iter.next() + (iter.hasNext() ? "," : ""));
		
		String value = builder.toString();
		
		return "".equals(value) ? null : value;
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
		String indices = (String) rawData.get(offset);
		StringBuilder ret = new StringBuilder();

		if (indices == null || indices.equals("0") || indices.equals(""))
			ret.append("(Not Specified)");
		else {
			String[] selections = indices.indexOf(",") > 0 ? indices.split(",") : new String[] { indices };

			LookupData myData = (LookupData)data;
			for (int i = 0; i < selections.length - 1; i++) {
				ret.append(myData.getLabel(selections[i]));
				ret.append(", ");
			}
			ret.append(myData.getLabel(selections[selections.length - 1]));
		}

		prettyData.add(offset, ret.toString());

		return ++offset;
	}

	public DataList getList() {
		return list;
	}

	@Override
	public boolean isActive(Rule activityRule) {
		try {
			return list.getItem((((SelectRule) activityRule).getIndexInQuestion())).isChecked();
		} catch (Exception e) {
			return false;
		}
	}
	
	@Override
	public void setData(PrimitiveField<List<Integer>> field) {
		clearData();

		List<Integer> keys = field != null ? field.getValue() : new ArrayList<Integer>();
		
		for (Integer index : keys) {
			LookupData myData = (LookupData)data;
			if (myData.getLabel("" + index) == null)
				continue;
			
			if (list.isRendered()) {
				for (DataListItem item : list.getItems()) {
					LookupDataValue value = item.getData(LOOKUP);
					if (index.toString().equals(value.getID()))
						item.setChecked(true);
				}
			}
			else
				checkedItems.add(index.toString());
		}
	}

	@Override
	public void setEnabled(boolean isEnabled) {
		list.setEnabled(isEnabled);
	}	

}
