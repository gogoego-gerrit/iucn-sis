package org.iucn.sis.shared.api.structures;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.iucn.sis.shared.api.models.PrimitiveField;
import org.iucn.sis.shared.api.models.primitivefields.ForeignKeyListPrimitiveField;
import org.iucn.sis.shared.api.models.primitivefields.StringPrimitiveField;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.event.BaseEvent;
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

public class SISMultiSelect extends DominantStructure {

	public static final String LISTBOX = "listbox";

	private DataList list;
	private ArrayList<Integer> checkedItems;

	public SISMultiSelect(String struct, String descript, String structID, Object data) {
		super(struct, descript, structID, data);
		buildContentPanel(Orientation.VERTICAL);
	}

	@Override
	protected PrimitiveField getNewPrimitiveField() {
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
		
		checkedItems = new ArrayList<Integer>();
		
		try {
			descriptionLabel = new HTML(description);
		} catch (Exception e) {
		}

		list = new DataList() {
			@Override
			protected void afterRender() {
				super.afterRender();

				for (Integer checked : checkedItems)
					list.getItem(checked.intValue()).setChecked(true);
			}
		};
		list.setCheckable(true);
		list.setSelectionMode(SelectionMode.MULTI);

		ArrayList<ArrayList<String>> myData = ((ArrayList<ArrayList<String>>)data);
		ArrayList<String> listItemsToAdd = myData.get(0);
		ArrayList<String> defaults = null;
		if( myData.size() > 1 )
			defaults = myData.get(1);

		
		model.set(description, defaults);
		
		for (int i = 0; i < listItemsToAdd.size(); i++) {
			String theKey = "" + i;
			DataListItem curItem = new DataListItem((String) listItemsToAdd.get(i));
			curItem.setId(theKey);

			list.add(curItem);
		}
		
		if( defaults != null && !defaults.equals("") ) {
			String [] split;
			if( defaults.indexOf(",") > -1 )
				split = defaults.get(0).split(",");
			else
				split = new String[] { defaults.get(0) };
			
			try {
				for( String cur : split ){
					checkedItems.add(Integer.valueOf(cur));
					
				}
			} catch (Exception e) {
				SysDebugger.getInstance().println("Invalid default value for multiselect " + description);
			}
		}
		
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
	public String getData() {
		String ret = "";

		if (list.isVisible()) {
			List<DataListItem> checked = list.getChecked();
			for (int i = 0; i < checked.size(); i++) {
				ret += (list.getItems().indexOf(checked.get(i)) + 1) + ",";
			}
		} else {
			for (Integer checked : checkedItems)
				ret += (checked.intValue() + 1) + ",";
		}

		return ret.substring(0, ret.length() == 0 ? 0 : ret.length() - 1);
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
		String ret = "";

		if (indices.equals("0") || indices.equals(""))
			ret = "(Not Specified)";
		else {
			String[] selections = indices.indexOf(",") > 0 ? indices.split(",") : new String[] { indices };

			ArrayList<ArrayList<String>> myData = ((ArrayList<ArrayList<String>>)data);
			ArrayList<String> listItemsToAdd = myData.get(0);
			for (int i = 0; i < selections.length - 1; i++)
				ret += listItemsToAdd.get(Integer.parseInt(selections[i]) - 1) + ", ";

			ret += listItemsToAdd.get(Integer.parseInt(selections[selections.length - 1]) - 1);
		}

		prettyData.add(offset, ret);

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
	public void setData(Map<String, PrimitiveField> data) {
		super.setData(data);
		clearData();

		List<Integer> keys = data.containsKey(getId()) ? ((ForeignKeyListPrimitiveField)data.get(getId())).getValue() : new ArrayList<Integer>();
		
		for (int i = 0; i < keys.size(); i++) {
			int index = keys.get(i).intValue();

			if (list.isRendered()) {
				if (!list.getItem(index).isChecked())
					list.getItem(index).setChecked(true);
			} else {
				checkedItems.add(Integer.valueOf(index));
			}
		}
		
	}

	@Override
	public void setEnabled(boolean isEnabled) {
		list.setEnabled(isEnabled);
	}

	@Override
	public String toXML() {
		return StructureSerializer.toXML(this);
	}
	

}
