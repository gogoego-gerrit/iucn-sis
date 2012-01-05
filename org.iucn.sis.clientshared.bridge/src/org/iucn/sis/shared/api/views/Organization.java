package org.iucn.sis.shared.api.views;

import java.util.ArrayList;

import com.solertium.lwxml.shared.NativeElement;
import com.solertium.lwxml.shared.NativeNode;
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
		Composite rootComposite = new Composite("", "", rootTag, true);
		if (!rootComposite.getFields().isEmpty())
			composites.add(rootComposite);
		
		NativeNodeList nodes = rootTag.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			NativeNode node = nodes.item(i);
			if ("text".equals(node.getNodeName())) {
				this.text = node.getTextContent();
			}
			else if ("composite".equals(node.getNodeName())) {
				NativeElement compositeTag = (NativeElement)node;
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
