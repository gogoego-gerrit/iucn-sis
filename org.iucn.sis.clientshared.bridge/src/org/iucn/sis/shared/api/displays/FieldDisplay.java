/**
 * Field.java
 * 
 * Represents a simple field structure derived from the master list of display objects
 * Creates a panel to hold structures.
 * 
 * @author carl.scott
 */

package org.iucn.sis.shared.api.displays;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.iucn.sis.shared.api.models.Field;
import org.iucn.sis.shared.api.models.PrimitiveField;
import org.iucn.sis.shared.api.structures.FieldData;
import org.iucn.sis.shared.api.structures.Structure;

import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.solertium.lwxml.gwt.debug.SysDebugger;

public class FieldDisplay extends Display {

	public FieldDisplay() {
		super();
	}

	public FieldDisplay(FieldData data) {
		super(data);
	}

	/**
	 * Creates a new field object, given all of the data in
	 */
	public FieldDisplay(String struct, String descript, Object data, String group, String fieldId, String canonicalName,
			String classOfService, String associate) {
		super(struct, descript, data, group, fieldId, canonicalName, classOfService, associate);
	}

	@Override
	public boolean hasChanged() {
		for( Structure struct : myStructures )
			if( struct.hasChanged() )
				return true;
		
		return false;
	}
	
	@Override
	public void save() {
//		if( field.getFields() != null )
//			field.getFields().clear();
//		
//		if( field.getPrimitiveField() != null )
//			field.getPrimitiveField().clear();
		
		for( Structure struct : myStructures )
			struct.save(field);
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

	@Override
	public void setData(Field field) {
		this.field = field;
		
		if (field != null) {
			if( field.getFields() == null ) {
				Map<String, PrimitiveField> prims = field.getKeyToPrimitiveFields();
				for (Structure cur : getStructures()) {
					try {
						cur.setData(prims);
					} catch (Exception e) {
						cur.clearData();
						System.out.println(
								"setData error in FieldWidgetCache for display " + getCanonicalName());
						e.printStackTrace();
					}
				}
			} else {
				//It has subfields, most likely. The structure should know how to handle it.
				for (Structure cur : getStructures()) {
					try {
						cur.setFieldData(field);
					} catch (Exception e) {
						System.out.println(
								"setData error in FieldWidgetCache for display " + getCanonicalName());
						e.printStackTrace();
					}
				}
			}
			
		} else {
			for (Structure cur : getStructures())
				cur.clearData();
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
			xml.append(myStructures.get(i).toXML());
		}
		xml.append("</field>\r\n");

		return xml.toString();
	}

}// class Field
