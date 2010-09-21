package org.iucn.sis.shared.api.structures;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.solertium.lwxml.gwt.debug.SysDebugger;

public class SISMultiSelect extends SISPrimitiveStructure<List<Integer>> implements DominantStructure<PrimitiveField<List<Integer>>> {

	public static final String LISTBOX = "listbox";

	private DataList list;
	private ArrayList<String> checkedItems;

	public SISMultiSelect(String struct, String descript, String structID, Object data) {
		super(struct, descript, structID, data);
		buildContentPanel(Orientation.VERTICAL);
	}

	@Override
	protected PrimitiveField<List<Integer>> getNewPrimitiveField() {
		return new ForeignKeyListPrimitiveField(getId(), null);
	}
	
	@Override
	public void addListenerToActiveStructure(final ChangeListener changeListener, ClickHandler clickListener,
			KeyboardListener keyboardListener) {
		list.addListener(Events.CheckChange, new Listener<BaseEvent>() {
			public void handleEvent(BaseEvent be) {
				changeListener.onChange(list);
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

		for (int i = 0; i < p.getWidgetCount(); i++)
			p.remove(i);

		((VerticalPanel) displayPanel).setVerticalAlignment(HasVerticalAlignment.ALIGN_BOTTOM);

		displayPanel.add(descriptionLabel);
		displayPanel.add(list);
		return displayPanel;
	}

	@Override
	public Widget createViewOnlyLabel() {
		// clearDisplayPanel();
		VerticalPanel p = ((VerticalPanel) displayPanel);

		for (int i = 0; i < p.getWidgetCount(); i++)
			p.remove(i);

		((VerticalPanel) displayPanel).setVerticalAlignment(HasVerticalAlignment.ALIGN_BOTTOM);

		displayPanel.add(descriptionLabel);

		List<DataListItem> checked = list.getChecked();

		if (checked.size() == 0) {
			displayPanel.add(new HTML("None Selected"));
		} else {
			String text = "";

			for (int i = 0; i < checked.size(); i++)
				text += checked.get(i) + ", ";

			displayPanel.add(new HTML(text.substring(0, text.length() - 1)));
		}

		return displayPanel;
	}

	@Override
	public void createWidget() {
		checkedItems = new ArrayList<String>();
		
		try {
			descriptionLabel = new HTML(description);
		} catch (Exception e) {
			//FIXME: What could go wrong here??
		}

		list = new DataList() {
			@Override
			protected void afterRender() {
				super.afterRender();

				for (String checked : checkedItems) {
					DataListItem item = list.getItemByItemId(checked);
					if (item != null)
						item.setChecked(true);
				}
			}
		};
		list.setCheckable(true);
		list.setSelectionMode(SelectionMode.MULTI);
		list.addListener(Events.CheckChange, new Listener<DataListEvent>() {
			public void handleEvent(DataListEvent be) {
				DataListItem item = be.getItem();
				if (item != null) {
					if (item.isChecked())
						checkedItems.add(item.getId());
					else
						checkedItems.remove(item.getId());
				}
			}
		});
		
		LookupData myData = (LookupData)data;
		
		for (LookupDataValue value : myData.getValues()) {
			DataListItem curItem = new DataListItem();
			curItem.setText(value.getLabel());
			curItem.setId(curItem.getId());

			list.add(curItem);
		}
		
		for (String defaultValue : myData.getDefaultValues()) {
			if (list.getItemByItemId(defaultValue) != null)
				checkedItems.add(defaultValue);
		}
		
	}

	@Override
	public String getData() {
		final StringBuilder builder = new StringBuilder();

		if (list.isVisible()) {
			for (Iterator<DataListItem> iter = list.getChecked().iterator(); iter.hasNext(); )
				builder.append(iter.next().getId() + (iter.hasNext() ? "," : ""));
		} else {
			for (Iterator<String> iter = checkedItems.iterator(); iter.hasNext(); )
				builder.append(iter.next() + (iter.hasNext() ? "," : ""));
		}

		return builder.toString();
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
		
		if (field != null) {
			for (Integer index : field.getValue()) {
				if (list.isRendered()) {
					DataListItem item = list.getItemByItemId(index.toString());
					if (item != null && !item.isChecked())
						item.setChecked(true);
				}
				else
					checkedItems.add(index.toString());
			}
		}
		
	}

	@Override
	public void setEnabled(boolean isEnabled) {
		list.setEnabled(isEnabled);
	}

	public String toXML() {
		return StructureSerializer.toXML(this);
	}
	

}
