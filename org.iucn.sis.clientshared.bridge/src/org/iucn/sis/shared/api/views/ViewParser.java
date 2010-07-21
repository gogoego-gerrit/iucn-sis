package org.iucn.sis.shared.api.views;

import java.util.LinkedHashMap;

import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.NativeElement;
import com.solertium.lwxml.shared.NativeNodeList;

public class ViewParser {

	LinkedHashMap<String, View> views;

	public ViewParser() {
		views = new LinkedHashMap<String, View>();
	}

	public LinkedHashMap<String, View> getViews() {
		return views;
	}

	public void parse(NativeDocument viewsDocument) {
		NativeNodeList viewElements = viewsDocument.getDocumentElement().getElementsByTagName("view");

		for (int i = 0; i < viewElements.getLength(); i++)
			parseView(viewElements.elementAt(i));
	}

	private void parsePages(NativeNodeList pages, View curView) {
		Page curPage;

		for (int i = 0; i < pages.getLength(); i++) {
			NativeElement rootPageTag = (NativeElement) pages.item(i);

			if (rootPageTag.getNodeName().equalsIgnoreCase("page")) {
				curPage = new Page(rootPageTag.getAttribute("title"), rootPageTag.getAttribute("id"), rootPageTag, true);

				curView.addPage(curPage);
			}
		}
	}

	private void parseView(NativeElement root) {
		View curView = new View(root.getAttribute("title"), root.getAttribute("id"), root, true);

		parsePages(root.getElementsByTagName("page"), curView);

		views.put(curView.getId(), curView);
	}

}
