/**
 * Field.java
 * 
 * Represents a simple field structure derived from the master list of display objects
 * Creates a panel to hold structures.
 * 
 * @author carl.scott
 */

package org.iucn.sis.shared.api.displays;

import org.iucn.sis.shared.api.data.FieldData;
import org.iucn.sis.shared.api.models.Field;
import org.iucn.sis.shared.api.structures.DisplayStructure;

import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Widget;

public class FieldDisplay extends Display {
	
	public FieldDisplay() {
		super();
	}

	public FieldDisplay(FieldData data) {
		super(data);
	}

	@Override
	public boolean hasChanged() {
		for (DisplayStructure struct : getStructures()) {
			boolean hasChanged;
			if (struct.isPrimitive())
				hasChanged = struct.hasChanged(field == null ? null : field.getPrimitiveField(struct.getId()));
			else
				hasChanged = struct.hasChanged(field == null ? null : field.getField(struct.getId()));
			
			if (hasChanged)
				return true;
		}
		
		return false;
	}
	
	@Override
	public void save() {
		if (field == null) {
			field = new Field();
			field.setName(getCanonicalName());
		}
			
		for (DisplayStructure struct : getStructures()) {
			if (struct.isPrimitive())
				struct.save(field, field.getPrimitiveField(struct.getId()));
			else
				struct.save(field, field.getField(struct.getId()));
		}
	}
	
	/**
	 * Shows the structures' widgets in a HorizontalPanel
	 * 
	 * @return the widget to display the field
	 */
	@Override
	protected Widget generateContent(boolean viewOnly) {
		displayPanel = new HorizontalPanel();
		displayPanel.setSize("100%", "100%");
		// displayPanel.addStyleName("standout");

		for (int i = 0; i < myStructures.size(); i++) {
			if (viewOnly)
				displayPanel.add((myStructures.get(i)).generateViewOnly());
			else
				displayPanel.add((myStructures.get(i)).generate());
		}
		return displayPanel;
	}

	@Override
	public void setData(Field field) {
		this.field = field;
		
		for (DisplayStructure cur : getStructures()) {
			if (cur.isPrimitive())
				cur.setData(field == null ? null : field.getPrimitiveField(cur.getId()));
			else
				cur.setData(field == null ? null : field.getField(cur.getId()));
		}
	}
	
	/**
	 * Returns the string representation of a field.
	 * 
	 * @return the field, string form
	 */
	@Override
	public String toString() {
		return "< " + displayID + ", " + structure + ", " + canonicalName + ", " + description + " >\r\n";
	}

}// class Field
