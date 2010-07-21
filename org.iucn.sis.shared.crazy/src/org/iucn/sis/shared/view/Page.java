package org.iucn.sis.shared.view;

import java.util.ArrayList;

import com.solertium.lwxml.shared.NativeElement;
import com.solertium.lwxml.shared.NativeNodeList;

public class Page {

	ArrayList<Organization> organizations;
	String title;
	String id;

	public Page(String title, String id, NativeElement rootTag, boolean parseNow) {
		organizations = new ArrayList<Organization>();
		this.title = title;
		this.id = id;

		if (parseNow)
			parse(rootTag);
	}

	public String getId() {
		return id;
	}

	public ArrayList<String> getMyFields() {
		ArrayList<String> fields = new ArrayList<String>();

		for (Organization cur : organizations)
			fields.addAll(cur.getMyFields());

		return fields;
	}

	public ArrayList<Organization> getOrganizations() {
		return organizations;
	}

	public String getTitle() {
		return title;
	}

	public void parse(NativeElement rootTag) {

		NativeNodeList orgs = rootTag.getElementsByTagName("organization");

		for (int i = 0; i < orgs.getLength(); i++) {
			NativeElement curTag = orgs.elementAt(i);
			Organization organization = new Organization(curTag.getAttribute("title"), curTag
					.getAttribute("shortTitle"), curTag, true);

			organizations.add(organization);
		}
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setTitle(String title) {
		this.title = title;
	}

}
