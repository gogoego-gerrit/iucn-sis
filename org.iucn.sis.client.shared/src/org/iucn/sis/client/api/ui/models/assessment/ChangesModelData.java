package org.iucn.sis.client.api.ui.models.assessment;

import java.util.Date;

import com.extjs.gxt.ui.client.data.BaseModelData;
import com.solertium.lwxml.shared.NativeElement;

public class ChangesModelData extends BaseModelData {

	/**
	 * default serialized value
	 */
	private static final long serialVersionUID = 1L;

	public static final String CHANGE = "change";
	public static final String FIELD_NAME = "Field Name";
	public static final String FIELD = "Field";
	public static final String NAME = "Name";
	public static final String VALUE = "Value";
	public static final String DATE = "Date";
	public static final String USER = "User";
	
	/**
	 * Currently no support for deleting assessments.  This needs to be done at some point
	 */
	public final boolean deleted;

	
	public ChangesModelData(NativeElement changeElement) {


		if (changeElement.getAttribute("status") == null)  
		{
			set(NAME,changeElement.getElementsByTagName(NAME.toLowerCase()).elementAt(0).getTextContent());
			set(VALUE,changeElement.getElementsByTagName(VALUE.toLowerCase()).elementAt(0).getTextContent());
			deleted = false;
		}
		else
		{
			deleted = true;
			set(VALUE,"DELETED");
			set(NAME, "");
		}
		set(FIELD,changeElement.getElementsByTagName(FIELD.toLowerCase()).elementAt(0).getTextContent());
		set(DATE,new Date(Long.valueOf(changeElement.getElementsByTagName(DATE.toLowerCase()).elementAt(0).getTextContent() + "000")));
		set(USER, changeElement.getElementsByTagName(USER.toLowerCase()).elementAt(0).getTextContent());



	}



}
