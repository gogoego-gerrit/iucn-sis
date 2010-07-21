package org.iucn.sis.client.panels.dem;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.iucn.sis.client.api.container.SISClientBase;
import org.iucn.sis.client.api.utils.UriBase;
import org.iucn.sis.shared.api.displays.SaveAndShow;
import org.iucn.sis.shared.api.utils.XMLUtils;

import com.extjs.gxt.ui.client.widget.TabPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.NativeElement;
import com.solertium.lwxml.shared.NativeNodeList;

public class ViewCache {

	/**
	 * Creates a savable, displayable object to allow a user to choose which
	 * view they want to see from the available views. Used to create a new
	 * assessment
	 * 
	 * @author carl.scott
	 */
	private class ViewChooser implements SaveAndShow {

		private ListBox options;

		public ViewChooser() {
			options = new ListBox();
			Iterator iterator = getAvailableKeys().iterator();
			while (iterator.hasNext()) {
				String key = (String) iterator.next();
				options.addItem(getView(key).getDisplayableTitle(), getView(key).getId());
			}
			options.setSelectedIndex(0);
		}

		public HashMap saveDataToHashMap() {
			HashMap saveData = new HashMap();
			saveData.put("view", options.getValue(options.getSelectedIndex()));
			return saveData;
		}

		public Widget show(HashMap displaySetToUse) {
			HorizontalPanel display = new HorizontalPanel();
			display.add(new HTML("Choose a view"));
			display.add(options);
			return display;
		}
	}

	private class ViewParser {

		ViewParser() {
		}

		void doParse() {
			NativeNodeList viewElements = viewsDocument.getDocumentElement().getElementsByTagName("view");

			for (int i = 0; i < viewElements.getLength(); i++)
				parseViews(viewElements.elementAt(i));
		}

		private void parsePages(NativeNodeList pages, SISView curView) {
			SISPageHolder curPage;

			for (int i = 0; i < pages.getLength(); i++) {
				NativeElement rootPageTag = (NativeElement) pages.item(i);
				// Create new SISPageHolders for each page in the view
				if (rootPageTag.getNodeName().equalsIgnoreCase("page")) {
					curPage = new SISPageHolder(XMLUtils.getXMLAttribute(rootPageTag, "title", null), XMLUtils
							.getXMLAttribute(rootPageTag, "id"), rootPageTag);

					curView.addPage(curPage);
				}
			}
		}

		private void parseViews(NativeElement root) {
			SISView curView = new SISView();

			curView.setDisplayableTitle(root.getAttribute("title"));
			curView.setId(root.getAttribute("id"));

			parsePages(root.getElementsByTagName("page"), curView);

			views.put(curView.getId(), curView);
		}
	}

	public static final ViewCache impl = new ViewCache();

	private HashMap<String, SISView> views;
	private NativeDocument viewsDocument;

	private HashMap lastPageViewed;

	private ViewParser parser;

	SISView currentView = null;

	private ViewCache() {

		viewsDocument = SISClientBase.getHttpBasicNativeDocument();
		views = new HashMap<String, SISView>();
		lastPageViewed = new HashMap();
		parser = new ViewParser();
	}

	public void doLogout() {
		views.clear();
		lastPageViewed.clear();
	}

	public void fetchViews(final GenericCallback<String> wayback) {
		viewsDocument.get(UriBase.getInstance().getSISBase() + "/raw/browse/docs/views.xml", new GenericCallback<String>() {
			public void onFailure(Throwable arg0) {
				wayback.onFailure(arg0);
			}

			public void onSuccess(String arg0) {
				parser.doParse();
				wayback.onSuccess(null);
			}
		});
	}

	public Set<String> getAvailableKeys() {
		return views.keySet();
	}

	public Collection<SISView> getAvailableViews() {
		return views.values();
	}

	public SISView getCurrentView() {
		return currentView;
	}

	public int getLastPageViewed(String viewID) {
		try {
			return ((Integer) lastPageViewed.get(viewID)).intValue();
		} catch (Exception e) {
			return 0;
		}
	}

	public SISView getView(String viewID) {
		return (SISView) views.get(viewID);
	}

	public ViewChooser getViewChooser() {
		return new ViewChooser();
	}

	public boolean isEmpty() {
		return views.isEmpty();
	}

	public boolean needPageChange(String viewID, int pageNum, boolean viewOnly) {
		if (currentView == null || !currentView.getId().equals(viewID))
			return true;
		else
			return ((SISView) views.get(viewID)).needPageChange(pageNum, viewOnly);
	}

	public TabPanel showPage(String viewID, int pageNum, boolean viewOnly) {
		currentView = (SISView) views.get(viewID);
		lastPageViewed.put(viewID, new Integer(pageNum));
		return ((SISView) views.get(viewID)).showPage(pageNum, viewOnly);
	}
}
