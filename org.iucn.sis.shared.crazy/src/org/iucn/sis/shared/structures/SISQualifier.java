package org.iucn.sis.shared.structures;

import java.util.ArrayList;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;

public class SISQualifier extends Structure {

	private ListBox listbox;
	private ArrayList items;

	public SISQualifier(String struct, String descript) {
		super(struct, descript);
		// displayPanel = new ContentPanel();
		buildContentPanel(Orientation.HORIZONTAL);
	}

	@Override
	public void clearData() {
		listbox.setSelectedIndex(0);
	}

	@Override
	public Widget createLabel() {
		clearDisplayPanel();
		displayPanel.add(descriptionLabel);
		displayPanel.add(listbox);
		return displayPanel;
	}

	@Override
	public Widget createViewOnlyLabel() {
		clearDisplayPanel();
		displayPanel.add(descriptionLabel);
		displayPanel.add(new HTML((listbox.isItemSelected(0)) ? "None Selected" : listbox.getItemText(listbox
				.getSelectedIndex())));
		return displayPanel;
	}

	@Override
	public void createWidget() {
		descriptionLabel = new HTML(description);
		listbox = new ListBox();
		items = new ArrayList();

		items.add("---Select---");
		items.add("Observed");
		items.add("Projected");
		items.add("Inferred");

		for (int theKey = 0; theKey < this.items.size(); theKey++) {
			listbox.addItem((String) this.items.get(theKey), "" + theKey);
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
	public Object getData() {
		return new Integer(listbox.getSelectedIndex()).toString();
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
		prettyData.add(offset, DisplayableDataHelper.toDisplayableSingleSelect((String) rawData.get(offset),
				new Object[] { "Observed", "Projected", "Inferred" }));
		return ++offset;
	}

	public ListBox getListbox() {
		return listbox;
	}

	
	@Override
	public int setData(ArrayList dataList, int dataOffset) {
		super.setData(dataList, dataOffset);
		String selection = dataList.get(dataOffset).toString();
		if (selection.trim().equalsIgnoreCase(""))
			listbox.setSelectedIndex(0);
		else
			listbox.setSelectedIndex(Integer.parseInt(selection));
		return ++dataOffset;
	}

	
	@Override
	public void setEnabled(boolean isEnabled) {
		this.listbox.setEnabled(isEnabled);
	}

	@Override
	public String toXML() {
		return StructureSerializer.toXML(this);
	}
}
