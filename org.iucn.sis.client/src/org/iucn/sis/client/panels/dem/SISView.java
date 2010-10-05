package org.iucn.sis.client.panels.dem;

import java.util.ArrayList;
import java.util.HashMap;

import org.iucn.sis.shared.api.debug.Debug;

import com.extjs.gxt.ui.client.widget.TabPanel;
import com.solertium.util.gwt.ui.DrawsLazily;

/**
 * This class represents a way to view an assessment. It is a collection of
 * pages, each representing a chunk of assessment fields laid out in a
 * particular way.
 * 
 * @author adam.schwartz
 * 
 *         Can optionally parse its own XML Document and utilize attribute
 *         options to affect rendering, such as hiding headers, footers, and
 *         using different styles.
 * @author carl.scott
 */
public class SISView {

	private String id;
	private String displayableTitle;
	private ArrayList<SISPageHolder> pages;

	private boolean showHeader = true;
	private boolean showFooter = true;

	private HashMap displaySetToUse;

	private SISPageHolder curPage = null;

	public SISView() {
		this("", "");
	}

	private SISView(String ID, String title) {
		id = ID;
		displayableTitle = title;
		pages = new ArrayList<SISPageHolder>();
	}

	public void addPage(SISPageHolder newPage) {
		pages.add(newPage);
	}

	public SISPageHolder getCurPage() {
		return curPage;
	}

	public String getDisplayableTitle() {
		return displayableTitle;
	}

	public HashMap getDisplaySetToUse() {
		return displaySetToUse;
	}

	public String getId() {
		return id;
	}

	public SISPageHolder getPageAt(int index) {
		return (SISPageHolder) pages.get(index);
	}

	/**
	 * Returns an ArrayList of SISPageHolders.
	 * 
	 * @return ArrayList(SISPageHolder)
	 */
	public ArrayList<SISPageHolder> getPages() {
		return pages;
	}

	public boolean needPageChange(int pageNum, boolean viewOnly) {
		if (curPage != null && pages.get(pageNum).equals(curPage) && curPage.isViewOnly() == viewOnly)
			return false;
		else
			return true;
	}

	public void setDisplayableTitle(String displayableTitle) {
		this.displayableTitle = displayableTitle;
	}

	public void setDisplaySetToUse(HashMap set) {
		this.displaySetToUse = set;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void showPage(int pageNum, boolean viewOnly, DrawsLazily.DoneDrawingCallbackWithParam
			<TabPanel> callback) {
		if (pages != null)
			Debug.println("Showing page {0} of {1}", pageNum, pages.size());
		curPage = pages.get(pageNum);
		curPage.showPage(callback, viewOnly);
	}

}
