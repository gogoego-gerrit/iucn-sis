package org.iucn.sis.shared.api.structures;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.iucn.sis.shared.api.debug.Debug;
import org.iucn.sis.shared.api.models.PrimitiveField;
import org.iucn.sis.shared.api.models.primitivefields.ForeignKeyPrimitiveField;
import org.iucn.sis.shared.api.views.components.LookupData;
import org.iucn.sis.shared.api.views.components.LookupData.LookupDataValue;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;

public class SISQualifier extends SISPrimitiveStructure<Integer> {

	private ListBox listbox;

	public SISQualifier(String struct, String descript, String structID, Object data) {
		super(struct, descript, structID, data);
		// displayPanel = new ContentPanel();
		buildContentPanel(Orientation.HORIZONTAL);
	}
	
	@Override
	protected PrimitiveField<Integer> getNewPrimitiveField() {
		return new ForeignKeyPrimitiveField(getId(), null);
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
		listbox.addItem("--- Select ---", "");
		
		LookupData myData = ((LookupData)data);
		List<LookupDataValue> listItemsToAdd = 
			new ArrayList<LookupDataValue>(myData.getValues());
		Collections.sort(listItemsToAdd, new QualifierComparator());
		
		for (LookupDataValue value : listItemsToAdd)
			listbox.addItem(value.getLabel(), value.getID());
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
	public String getData() {
		if (listbox.getSelectedIndex() <= 0)
			return null;
		else
			return listbox.getValue(listbox.getSelectedIndex());
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
		String selectedValue = rawData.get(offset);
		
		String pretty = ((LookupData)data).getLabel(selectedValue);
		if (pretty == null)
			pretty = "(Not Specified)";
		
		prettyData.add(offset, pretty);
		
		return ++offset;
	}

	public ListBox getListbox() {
		return listbox;
	}
	
	@Override
	public void setData(PrimitiveField<Integer> field) {
		String value = field != null ? field.getRawValue() : "";
		listbox.setSelectedIndex(0);
		try {
			for (int i = 1; i < listbox.getItemCount(); i++)
				if (listbox.getValue(i).equals(value))
					listbox.setSelectedIndex(i);
		} catch (IndexOutOfBoundsException unlikely) {
			Debug.println("Empty select list");
		}
	}

	
	@Override
	public void setEnabled(boolean isEnabled) {
		listbox.setEnabled(isEnabled);
	}
	
	private static class QualifierComparator implements Comparator<LookupDataValue> {
		
		private final List<String> order;
		
		public QualifierComparator() {
			order = new ArrayList<String>();
			order.add("Observed");
			order.add("Estimated");
			order.add("Projected");
			order.add("Inferred");
			order.add("Suspected");
		}
		
		@Override
		public int compare(LookupDataValue arg0, LookupDataValue arg1) {
			int left = order.indexOf(arg0.getLabel());
			int right = order.indexOf(arg1.getLabel());
			
			if (left == -1)
				return 1;
			else if (right == -1)
				return 1;
			else
				return Integer.valueOf(left).compareTo(right);
		}
		
	}
}
