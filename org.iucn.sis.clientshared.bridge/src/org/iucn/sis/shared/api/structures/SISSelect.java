package org.iucn.sis.shared.api.structures;

import java.util.ArrayList;
import java.util.Map;

import org.iucn.sis.client.api.caches.AssessmentCache;
import org.iucn.sis.shared.api.models.PrimitiveField;
import org.iucn.sis.shared.api.models.primitivefields.ForeignKeyPrimitiveField;
import org.iucn.sis.shared.api.models.primitivefields.StringPrimitiveField;
import org.iucn.sis.shared.api.utils.XMLUtils;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.solertium.util.extjs.client.WindowUtils;

public class SISSelect extends DominantStructure {

	public static final String LISTBOX = "listbox";

	private ListBox listbox;

	public SISSelect(String struct, String descript, String structID, Object data) {
		super(struct, descript, structID, data);
		if (isSingle())
			buildContentPanel(Orientation.HORIZONTAL);
		else
			buildContentPanel(Orientation.VERTICAL);
	}

	@Override
	protected PrimitiveField getNewPrimitiveField() {
		return new ForeignKeyPrimitiveField(getId(), null);
	}
	
	@Override
	public void addListenerToActiveStructure(ChangeListener changeListener, ClickHandler clickListener,
			KeyboardListener keyboardListener) {
		listbox.addChangeListener(changeListener);
		DOM.setEventListener(listbox.getElement(), listbox);
	}

	@Override
	public void clearData() {
		listbox.setSelectedIndex(0);
	}

	@Override
	public Widget createLabel() {
		clearDisplayPanel();

		if (isSingle()) {
			((HorizontalPanel) displayPanel).setVerticalAlignment(HasVerticalAlignment.ALIGN_BOTTOM);
			((HorizontalPanel) displayPanel).setSpacing(5);
		} else
			((VerticalPanel) displayPanel).setVerticalAlignment(HasVerticalAlignment.ALIGN_BOTTOM);

		displayPanel.add(descriptionLabel);
		displayPanel.add(listbox);
		return displayPanel;
	}

	@Override
	public Widget createViewOnlyLabel() {
		clearDisplayPanel();

		if (isSingle()) {
			((HorizontalPanel) displayPanel).setVerticalAlignment(HasVerticalAlignment.ALIGN_BOTTOM);
			((HorizontalPanel) displayPanel).setSpacing(5);
		} else
			((VerticalPanel) displayPanel).setVerticalAlignment(HasVerticalAlignment.ALIGN_BOTTOM);

		displayPanel.add(descriptionLabel);
		if (listbox.isItemSelected(0)) {
			displayPanel.add(new HTML("None Selected"));
		} else {
			String text = "";
			int numSelected = 0;
			int numWritten = 0;
			for (int i = 0; i < listbox.getItemCount(); i++) {
				if (listbox.isItemSelected(i))
					numSelected++;
			}
			for (int i = 0; i < listbox.getItemCount(); i++) {
				if (listbox.isItemSelected(i)) {
					text += listbox.getItemText(i);
					text += (++numWritten < numSelected) ? "," : "";
				}
			}
			displayPanel.add(new HTML(text));
		}
		return displayPanel;
	}

	@Override
	public void createWidget() {
		try {
			descriptionLabel = new HTML(description);
		} catch (Exception e) {
		}

		listbox = new ListBox();
		ArrayList<ArrayList<String>> myData = ((ArrayList<ArrayList<String>>)data);
		ArrayList<String> listItemsToAdd = myData.get(0);
		String defaults = null;
		if( myData.size() > 1 )
			defaults = myData.get(1).get(0);
		
		listbox.addItem("--- Select ---");

		for (int i = 0; i < listItemsToAdd.size(); i++) {
			String theKey = "" + i;
			listbox.addItem((String) listItemsToAdd.get(i), theKey);
			
			if( defaults != null ) {
				if( defaults.contains(""+i) )
					listbox.setSelectedIndex(i);
				//model.set(id, i);
			}
		}
		
		listbox.setMultipleSelect(!isSingle());
		if (isSingle())
			listbox.setVisibleItemCount(1);
		else
			listbox.setVisibleItemCount(listbox.getItemCount());

		if (!isSingle()) {
			listbox.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					if (listbox.isItemSelected(0)) {
						for (int i = 1; i < listbox.getItemCount(); i++) {
							listbox.setItemSelected(i, false);
						}
					}
				}
			});
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
		if (listbox.getSelectedIndex() == 0)
			return null;
		else
			return "" + listbox.getSelectedIndex();
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
		prettyData.add(offset, DisplayableDataHelper.toDisplayableSingleSelect((String) rawData.get(offset),
				((ArrayList) data).toArray()));
		return ++offset;
	}

	public ListBox getListbox() {
		return listbox;
	}

	@Override
	public boolean isActive(Rule activityRule) {
		try {
			if (isSingle())
				return ((SelectRule) activityRule).isSelected(listbox.getSelectedIndex());
			else
				return listbox.isItemSelected(((SelectRule) activityRule).getIndexInQuestion());
		} catch (Exception e) {
			return false;
		}
	}

	public boolean isSingle() {
		return structure.equalsIgnoreCase(XMLUtils.SINGLE_SELECT_STRUCTURE);
	}

	@Override
	public void setData(Map<String, PrimitiveField> data) {
		super.setData(data);
		Integer datum = data.containsKey(getId()) ? ((ForeignKeyPrimitiveField)data.get(getId())).getValue() : 0;
		listbox.setSelectedIndex(datum);
	}

	@Override
	public void setEnabled(boolean isEnabled) {
		listbox.setEnabled(isEnabled);
	}

	@Override
	public String toXML() {
		return StructureSerializer.toXML(this);
	}
}
