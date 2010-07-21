package org.iucn.sis.shared.api.views;

import java.util.ArrayList;

import com.solertium.lwxml.shared.NativeElement;
import com.solertium.lwxml.shared.NativeNodeList;

public class Composite {

	ArrayList<String> fields;
	String alignment;
	String style;

	public Composite(String alignment, String style, NativeElement rootTag, boolean parseNow) {
		fields = new ArrayList<String>();
		alignment = null;
		style = null;

		if (parseNow)
			parse(rootTag);
	}

	public String getAlignment() {
		return alignment;
	}

	public ArrayList<String> getFields() {
		return fields;
	}

	public String getStyle() {
		return style;
	}

	public void parse(NativeElement rootTag) {
		NativeNodeList fieldTags = rootTag.getElementsByTagName("field");

		for (int i = 0; i < fieldTags.getLength(); i++)
			fields.add(fieldTags.elementAt(i).getAttribute("id"));
	}

	public void setAlignment(String alignment) {
		this.alignment = alignment;
	}

	public void setFields(ArrayList<String> fields) {
		this.fields = fields;
	}

	public void setStyle(String style) {
		this.style = style;
	}

}
