package org.iucn.sis.shared.api.views;

import java.util.ArrayList;

import com.solertium.lwxml.shared.NativeElement;
import com.solertium.lwxml.shared.NativeNodeList;

public class Organization {

	ArrayList<Composite> composites;
	String text;
	String title;
	String shortTitle;

	public Organization(String title, String shortTitle, NativeElement rootTag, boolean parseNow) {
		composites = new ArrayList<Composite>();

		this.text = "";
		this.title = title;
		this.shortTitle = shortTitle;

		if (parseNow)
			parse(rootTag);
	}

	public ArrayList<Composite> getComposites() {
		return composites;
	}

	public ArrayList<String> getMyFields() {
		ArrayList<String> fields = new ArrayList<String>();

		for (Composite curComposite : composites)
			fields.addAll(curComposite.getFields());

		return fields;
	}

	public String getShortTitle() {
		return shortTitle;
	}

	public String getText() {
		return text;
	}

	public String getTitle() {
		return title;
	}

	public void parse(NativeElement rootTag) {

		NativeElement textTag = rootTag.getElementByTagName("text");
		if (textTag != null)
			this.text = textTag.getTextContent();

		NativeNodeList compositeTags = rootTag.getElementsByTagName("composite");

		if (compositeTags.getLength() == 0) {
			Composite composite = new Composite("", "", rootTag, true);
			composites.add(composite);
		} else {
			for (int i = 0; i < compositeTags.getLength(); i++) {
				NativeElement compositeTag = compositeTags.elementAt(i);
				Composite composite = new Composite(compositeTag.getAttribute("alignment"), compositeTag
						.getAttribute("style"), compositeTag, true);
				composites.add(composite);
			}
		}
	}

	public void setComposites(ArrayList<Composite> composites) {
		this.composites = composites;
	}

	public void setShortTitle(String shortTitle) {
		this.shortTitle = shortTitle;
	}

	public void setText(String text) {
		this.text = text;
	}

	public void setTitle(String title) {
		this.title = title;
	}

}
