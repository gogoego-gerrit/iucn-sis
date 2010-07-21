package org.iucn.sis.shared.view;

import java.util.LinkedHashMap;

import com.solertium.lwxml.shared.NativeElement;
import com.solertium.lwxml.shared.NativeNodeList;

public class View {

	LinkedHashMap<String, Page> pages;
	String title;
	String id;

	public View(String title, String id, NativeElement rootTag, boolean parseNow) {
		pages = new LinkedHashMap<String, Page>();
		this.title = title;
		this.id = id;

		if (parseNow)
			parse(rootTag);
	}

	public Page addPage(Page page) {
		return pages.put(page.getId(), page);
	}

	public String getId() {
		return id;
	}

	public LinkedHashMap<String, Page> getPages() {
		return pages;
	}

	public String getTitle() {
		return title;
	}

	public void parse(NativeElement rootTag) {

		NativeNodeList pageTags = rootTag.getElementsByTagName("page");

		for (int i = 0; i < pageTags.getLength(); i++) {
			NativeElement curTag = pageTags.elementAt(i);
			Page page = new Page(curTag.getAttribute("title"), curTag.getAttribute("id"), curTag, true);

			pages.put(page.getId(), page);
		}
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setTitle(String title) {
		this.title = title;
	}

}
