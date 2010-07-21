package org.iucn.sis.shared.structures;

import java.util.ArrayList;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;

public class SISBooleanList extends DominantStructure {

	public static final int TRUE_INDEX = 1;
	public static final int FALSE_INDEX = 2;
	public static final int UNKNOWN_INDEX = 3;

	private ListBox listbox;

	public SISBooleanList(String struct, String descript) {
		super(struct, descript);
		buildContentPanel(Orientation.HORIZONTAL);
	}

	@Override
	public void addListenerToActiveStructure(ChangeListener changeListener, ClickListener clickListener,
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
		if (listbox.getSelectedIndex() == 0)
			return "";
		else
			return "" + listbox.getSelectedIndex();
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
	public int setData(ArrayList dataList, int dataOffset) {
		super.setData(dataList, dataOffset);
		String datum = dataList.get(dataOffset).toString();

		if (datum.equals("")) {
			listbox.setSelectedIndex(0);
		} else if (datum.equalsIgnoreCase("true")) {
			listbox.setSelectedIndex(TRUE_INDEX);
		} else if (datum.equalsIgnoreCase("false")) {
			listbox.setSelectedIndex(FALSE_INDEX);
		} else if (datum.indexOf(",") > -1) {
			String[] selected = datum.split(",");
			for (int i = 0; i < selected.length; i++)
				listbox.setItemSelected(Integer.parseInt(selected[i]), true);
		} else {
			listbox.setSelectedIndex(Integer.parseInt(datum));
		}
		return ++dataOffset;
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
