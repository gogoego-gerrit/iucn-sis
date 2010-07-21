/**
 * Field.java
 * 
 * Represents a simple field structure derived from the master list of display objects
 * Creates a panel to hold structures.
 * 
 * @author carl.scott
 */

package org.iucn.sis.client.displays;

import org.iucn.sis.shared.FieldData;
import org.iucn.sis.shared.structures.Structure;

import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Widget;

public class Field extends Display {

	public Field() {
		super();
	}

	public Field(FieldData data) {
		super(data);
	}

	/**
	 * Creates a new field object, given all of the data in
	 */
	public Field(String struct, String descript, Object data, String group, String fieldId, String canonicalName,
			String classOfService, String associate) {
		super(struct, descript, data, group, fieldId, canonicalName, classOfService, associate);
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
				displayPanel.add(((Structure) myStructures.get(i)).generateViewOnly());
			else
				displayPanel.add(((Structure) myStructures.get(i)).generate());
		}
		return displayPanel;
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

	@Override
	public String toThinXML() {
		String xmlRetString = "<field id=\"" + this.displayID + "\">\n";
		xmlRetString += "\t<canonicalName>" + this.canonicalName + "</canonicalName>\n";
		for (int i = 0; i < myStructures.size(); i++)
			xmlRetString += ((Structure) myStructures.get(i)).toXML();
		xmlRetString += "</field>\n";
		return xmlRetString;
	}

	@Override
	public String toXML() {
		StringBuffer xml = new StringBuffer();
		xml.append("<field id=\"" + this.canonicalName + "\">\r\n");
		for (int i = 0; i < myStructures.size(); i++) {
			xml.append(myStructures.getStructure(i).toXML());
		}
		xml.append("</field>\r\n");

		return xml.toString();
	}

}// class Field
