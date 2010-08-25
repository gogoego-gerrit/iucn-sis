package org.iucn.sis.shared.api.structures;

import java.util.ArrayList;
import java.util.Map;

import org.iucn.sis.shared.api.models.PrimitiveField;
import org.iucn.sis.shared.api.models.primitivefields.BooleanUnknownPrimitiveField;
import org.iucn.sis.shared.api.models.primitivefields.ForeignKeyPrimitiveField;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;

public class SISBooleanList extends SISPrimitiveStructure implements DominantStructure {

	public static final int TRUE_INDEX = 1;
	public static final int FALSE_INDEX = 2;
	public static final int UNKNOWN_INDEX = 3;

	private ListBox listbox;

	public SISBooleanList(String struct, String descript, String structID) {
		super(struct, descript, structID);
		buildContentPanel(Orientation.HORIZONTAL);
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

		((HorizontalPanel) displayPanel).setVerticalAlignment(HasVerticalAlignment.ALIGN_BOTTOM);
		((HorizontalPanel) displayPanel).setSpacing(5);

		displayPanel.add(descriptionLabel);
		displayPanel.add(listbox);
		return displayPanel;
	}

	@Override
	public Widget createViewOnlyLabel() {
		clearDisplayPanel();

		((HorizontalPanel) displayPanel).setVerticalAlignment(HasVerticalAlignment.ALIGN_BOTTOM);
		((HorizontalPanel) displayPanel).setSpacing(5);

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
		listbox.addItem("--- Select ---");
		listbox.addItem("True / Yes");
		listbox.addItem("False / No");
		listbox.addItem("Unknown");
	}

	@Override
	public String getData() {
		if (listbox.getSelectedIndex() == 0)
			return "";
		else
			return "" + listbox.getSelectedIndex();
	}
	
	@Override
	protected BooleanUnknownPrimitiveField getNewPrimitiveField() {
		return new BooleanUnknownPrimitiveField(getId(), null);
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
		ArrayList<String> data = new ArrayList<String>();
		// data.add("--- Select ---");
		data.add("True / Yes");
		data.add("False / No");
		data.add("Unknown");

		prettyData.add(offset, DisplayableDataHelper.toDisplayableSingleSelect((String) rawData.get(offset), data
				.toArray()));
		return ++offset;
	}

	public ListBox getListbox() {
		return listbox;
	}

	@Override
	public boolean isActive(Rule activityRule) {
		try {
			if (activityRule instanceof BooleanRule) {
				if (((BooleanRule) activityRule).isTrue())
					return listbox.getSelectedIndex() == TRUE_INDEX;
				else
					return listbox.getSelectedIndex() == FALSE_INDEX;
			} else
				return ((SelectRule) activityRule).isSelected(listbox.getSelectedIndex());
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public void setData(Map<String, PrimitiveField> data) {
		//super.setData(data);
		if( data.containsKey(getId())) {
			Integer datum = ((ForeignKeyPrimitiveField)data.get(getId())).getValue();
			listbox.setSelectedIndex(datum);
		}
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
