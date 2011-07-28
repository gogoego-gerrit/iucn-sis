/**
 * SISPageHolder.java
 * 
 * Represents a Page
 * 
 * @author adam.schwartz
 * @author carl.scott
 */

package org.iucn.sis.client.api.ui.views;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.iucn.sis.client.api.caches.AssessmentCache;
import org.iucn.sis.client.api.caches.FieldWidgetCache;
import org.iucn.sis.client.api.caches.NotesCache;
import org.iucn.sis.shared.api.debug.Debug;
import org.iucn.sis.shared.api.displays.Display;
import org.iucn.sis.shared.api.displays.RelatedDisplays;
import org.iucn.sis.shared.api.structures.BooleanRule;
import org.iucn.sis.shared.api.structures.ContentRule;
import org.iucn.sis.shared.api.structures.Rule;
import org.iucn.sis.shared.api.structures.SelectRule;
import org.iucn.sis.shared.api.utils.XMLUtils;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment.HorizontalAlignmentConstant;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.lwxml.shared.NativeElement;
import com.solertium.lwxml.shared.NativeNode;
import com.solertium.lwxml.shared.NativeNodeList;
import com.solertium.util.extjs.client.WindowUtils;
import com.solertium.util.gwt.ui.DrawsLazily;
import com.solertium.util.gwt.ui.StyledHTML;
import com.solertium.util.portable.XMLWritingUtils;

@SuppressWarnings("deprecation")
public class SISPageHolder extends TabPanel {

	public static final String VERTICAL = "vertical";
	public static final String HORIZONTAL = "horizontal";

	public static final String TABLE = "table";
	private String pageTitle;
	private String pageID;
	private NativeNodeList pageOrganizations;

	private NativeElement pageTag;

	private boolean viewOnly = false;

	/**
	 * ArrayList<TabItem> - holds each pageOrganization as a tab
	 */
	// private ArrayList tabs;
	private int selectedTab = 0;

	private List<Display> myFields;

	/**
	 * Creates a new pageHolder. Takes a title, which will be displayed on a
	 * tab, and an id that uniquely identifies it from other pages.
	 * 
	 * @param title
	 *            the page title
	 * @param id
	 *            the page id
	 * @param DOMcontent
	 *            the page organizations in XML form
	 */
	public SISPageHolder(String title, String id, NativeElement pageTag) {
		super();
		pageOrganizations = pageTag.getElementsByTagName("pageOrganizations");
		this.pageTag = pageTag;
		pageTitle = title;
		pageID = id;
		myFields = new ArrayList<Display>();
		setTabScroll(true);
		setMonitorWindowResize(true);
	}
	
	protected void afterRender() {
		correctSize();
	}
	
	protected void onWindowResize(int width, int height) {
		correctSize();
	}
	
	/**
	 * Need this to correct the sizing of tabitems. 
	 * I think this is part GXT bug, part issues with 
	 * mixing use of GWT widgets & GXT widgets, but 
	 * overall, this needs to happen to ensure that 
	 * nothing gets lost when scrolling long pages in 
	 * tabs.  Could also opt to not use TabPanel here.
	 * 
	 * Am setting the size of the tab panel to its 
	 * parent, and laying out.  Using DeferredCommand 
	 * to ensure this happens after other stuff has 
	 * happened.
	 */
	private void correctSize() {
		DeferredCommand.addCommand(new Command() {
			public void execute() {
				try {
					Component parent = (Component)getParent();
					onResize(parent.getOffsetWidth(), parent.getOffsetHeight());
					layout(true);
				} catch (Throwable e) { 
					//Just in case stupidity happens...
				}
			}
		});
	}

	/**
	 * Adds the field's title as part of the display of the structure(s) (i.e. a
	 * section title)
	 * 
	 * @param currentNode
	 *            the organization node
	 * @param currentRow
	 *            the current row
	 * @param the
	 *            number of columns in this row
	 * @return the current row, incremented if successful/nec'y, not otherwise
	 */
	private void addTitleToOrganization(NativeElement currentNode, TabItem curTab, LayoutContainer content) {
		String title = XMLUtils.getXMLAttribute(currentNode, "title", null);
		String shortTitle = XMLUtils.getXMLAttribute(currentNode, "shortTitle", null);

		if (title == null && shortTitle == null) {
			shortTitle = "Page";
			title = "No name was specified";
		} else if (shortTitle == null) {
			shortTitle = title;
		} else if (title == null) {
			title = shortTitle;
		}

		curTab.setText(shortTitle);
		// curTab.setToolTip(title);

		HTML titleHTML = new HTML("&nbsp &nbsp " + title);
		titleHTML.addStyleName("bold");
		titleHTML.addStyleName("color-dark-blue");

		content.add(new HTML("<br />"));
		content.add(titleHTML);
		content.add(new HTML("<br />"));
	}

	private void fetchFields(final GenericCallback<String> wayBack) {
		final ArrayList<String> fieldList = new ArrayList<String>();
		
		final NativeNodeList fields = pageTag.getElementsByTagName("field");
		for (int i = 0; i < fields.getLength(); i++) 
			fieldList.add(fields.elementAt(i).getAttribute("id"));

		if (fieldList.isEmpty())
			NotesCache.impl.fetchNotes(AssessmentCache.impl.getCurrentAssessment(), wayBack);
		else {
			FieldWidgetCache.impl.prefetchList(fieldList, new GenericCallback<String>() {
				public void onFailure(Throwable caught) {
					wayBack.onFailure(caught);
				}

				public void onSuccess(String arg0) {
					for (Iterator<String> iter = fieldList.iterator(); iter.hasNext();) {
						String displayName = iter.next();
						Display dis = null;
						try {
							dis = FieldWidgetCache.impl.get(displayName);
						} catch (Throwable e) {
							e.printStackTrace();
						}
						if (dis != null)
							myFields.add(dis);
						else
							Debug.println("PageHolder parsed null display - " + displayName);
					}

					// wayBack.onSuccess("OK");
					NotesCache.impl.fetchNotes(AssessmentCache.impl.getCurrentAssessment(), wayBack);
				}
			});
		}
	}

	private void generatePage(boolean viewOnly) {
		int currentRow = 0;
		int currentColumn = 0;
		this.viewOnly = viewOnly;

		// clearDisplays();
		// content.setText( pageTitle + " --- " +
		// AssessmentCache.impl.getCurrentAssessment().getSpeciesName() );
		// content.removeAll();

		removeAll();

		// FOR EACH PAGEORGANIZATION
		for (int h = 0; h < pageOrganizations.getLength(); h++) {

			// Parsing the organizations
			NativeNodeList organizations = pageOrganizations.elementAt(h).getElementsByTagName("organization");
			for (int i = 0; i < organizations.getLength(); i++) {
				final TabItem curTab = new TabItem();
				curTab.setLayout(new FlowLayout(0));
				curTab.setScrollMode(Scroll.AUTOY);
				curTab.addListener(Events.Select, new Listener<BaseEvent>() {
					public void handleEvent(BaseEvent be) {
						selectedTab = indexOf(curTab);
					}
				});
				
				LayoutContainer content = curTab;

				// Set the fieldArrangement type (there is only one per
				// organization)
				NativeElement currentNode = organizations.elementAt(i);
				addTitleToOrganization(currentNode, curTab, content);

				NativeNodeList arrangementNodes = currentNode.getChildNodes();
				for (int j = 0; j < arrangementNodes.getLength(); j++) {

					NativeNode curArrangementNode = arrangementNodes.item(j);
					String fieldArrangement = curArrangementNode.getNodeName();

					// Field - one field on one row
					if (fieldArrangement.equalsIgnoreCase("field")) {
						currentColumn = 0;
						parseFieldTag(currentRow, currentColumn, (NativeElement) curArrangementNode, content);
						currentRow++;
					}

					// Composite Field -- one row containing multiple fields
					else if (fieldArrangement.equalsIgnoreCase("composite")) {
						currentColumn = 0;
						parseCompositeTag(currentRow, currentColumn, (NativeElement) curArrangementNode, content);
						currentRow++;
					}

					// Related fields (Dominant/Dependant)
					else if (fieldArrangement.equalsIgnoreCase("related")) {
						currentColumn = 0;
						currentRow = parseRelatedTag(currentRow, currentColumn, (NativeElement) curArrangementNode,
								content);
					}

					// Insert a line break
					// else if( fieldArrangement.equalsIgnoreCase("break") ) {
					// String pixelHeight =
					// XMLUtils.getXMLAttribute(currentNode, "pixelHeight",
					// null);
					// if (pixelHeight == null) {
					// pixelHeight = XMLUtils.getXMLAttribute(currentNode,
					// "size", "small");
					// if (pixelHeight.equalsIgnoreCase("small")) {
					// pixelHeight = "25px";
					// }
					// else if (pixelHeight.equalsIgnoreCase("medium")) {
					// pixelHeight = "50px";
					// }
					// else if (pixelHeight.equalsIgnoreCase("large")) {
					// pixelHeight = "100px";
					// }
					// else {
					// pixelHeight = "25px";
					// }
					// }
					// HorizontalPanel myContent = new HorizontalPanel();
					// myContent.addStyleName("SISPage_Break");
					// try {
					// myContent.setHeight(pixelHeight);
					// } catch (Exception e) { }
					// content.add(myContent);
					// currentColumn = 0;
					// currentRow++;
					// }

					// Insert a block of text
					else if (XMLWritingUtils.matches(fieldArrangement, "text", "h1", "h2", "h3")) {
						currentColumn = 0;
						/*
						 * contentTable.setWidget(currentRow++, currentColumn,
						 * new
						 * HTML(XMLUtils.getXMLValue(currentNode.getChildNodes
						 * ().item(j))));
						 */
						HorizontalAlignmentConstant align;
						String alignStr = XMLUtils.getXMLAttribute(curArrangementNode, "align", "left");
						if ("right".equals(alignStr))
							align = HasHorizontalAlignment.ALIGN_RIGHT;
						else if ("center".equals(alignStr))
							align = HasHorizontalAlignment.ALIGN_CENTER;
						else
							align = HasHorizontalAlignment.ALIGN_LEFT;

						HTML myContent = new HTML(XMLUtils.getXMLValue(curArrangementNode,
								"&nbsp;&nbsp;&nbsp;"));
						myContent.addStyleName("SIS_dataBrowserCustomText_" + fieldArrangement);

						VerticalPanel wrapper = new VerticalPanel();
						wrapper.setSpacing(4);
						wrapper.addStyleName("outsetFrameBorder");
						wrapper.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
						wrapper.setHorizontalAlignment(align);
						wrapper.add(myContent);
						wrapper.setWidth("100%");
						
						content.add(wrapper);
					}
					else if (XMLWritingUtils.matches(fieldArrangement, "warning", "information")) {
						currentColumn = 0;
						
						Grid grid = new Grid(1, 2);
						grid.addStyleName("SIS_dataBrowserCustomText_container");
						grid.setWidth("100%");
						grid.getCellFormatter().setWidth(0, 0, "20%");
						grid.getCellFormatter().setWidth(0, 1, "80%");
						grid.getCellFormatter().setAlignment(0, 0, HasHorizontalAlignment.ALIGN_CENTER, HasVerticalAlignment.ALIGN_MIDDLE);
						grid.getCellFormatter().setAlignment(0, 1, HasHorizontalAlignment.ALIGN_CENTER, HasVerticalAlignment.ALIGN_MIDDLE);
						
						final VerticalPanel textWrapper = new VerticalPanel();
						textWrapper.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
						textWrapper.add(new StyledHTML(XMLUtils.getXMLValue(curArrangementNode,
						"&nbsp;&nbsp;&nbsp;"), "SIS_dataBrowserCustomText_" + fieldArrangement));
						
						grid.setWidget(0, 0, new Image("tango/status/dialog-" + fieldArrangement + ".png"));
						grid.setWidget(0, 1, textWrapper);
						
						VerticalPanel wrapper = new VerticalPanel();
						wrapper.setSpacing(4);
						wrapper.addStyleName("outsetFrameBorder");
						wrapper.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
						wrapper.add(grid);
						wrapper.setWidth("100%");
						
						content.add(wrapper);
					}
				}// for all the organizations children...

				add(curTab);

			}// for organizations
		}// for pageOrganizations
	}// method

	public List<Display> getMyFields() {
		return myFields;
	}
	
	/**
	 * Returns this page's unique ID
	 * 
	 * @return the pageID
	 */
	public String getPageID() {
		return pageID;
	}

	/**
	 * Returns this page's title, which may or may not be unique
	 * 
	 * @return the page title
	 */
	public String getPageTitle() {
		return pageTitle;
	}

	public boolean isViewOnly() {
		return viewOnly;
	}

	/**
	 * helper function to parse a row with multiple fields
	 * 
	 * @param currentRow
	 *            the current row
	 * @param currentColumn
	 *            the current column
	 * @param displays
	 *            this assessments displays
	 * @param currentNode
	 *            the node with data
	 */
	private void parseCompositeTag(int currentRow, int currentColumn, NativeElement compositeNode,
			LayoutContainer content) {
		NativeElement currentNode;
		NativeNodeList compositeFields = compositeNode.getElementsByTagName("field");
		String layout = XMLUtils.getXMLAttribute(compositeNode, "layout", HORIZONTAL);

		FlexTable myContent = new FlexTable();
		myContent.setWidth("100%");
		myContent.setCellSpacing(0);
		myContent.setCellPadding(2);

		int row = 0;
		int column = 0;
		int numCols = compositeFields.getLength();

		double width = 100;
		try {
			width = 100 / numCols;
		} catch (Exception e) {
		}

		for (int k = 0; k < compositeFields.getLength(); k++) {
			currentNode = compositeFields.elementAt(k);

			String curField = XMLUtils.getXMLAttribute(currentNode, "id");
			try {
				// Make the inner panel contain the field, make it big as it
				// gets
				final SimplePanel myContent2 = new SimplePanel();
				myContent2.setWidth("100%");

				if (viewOnly)
					myContent2.setWidget(FieldWidgetCache.impl.get(curField).showViewOnly());
				else
					myContent2.setWidget(FieldWidgetCache.impl.get(curField).showDisplay());

				// Add it to the table appropriately, set the style, the
				// appropriate width
				// for the cell, and align it nicely
				myContent.setWidget(row, column, myContent2);
				myContent.getCellFormatter().addStyleName(row, column, "SISPage_Field");
				myContent.getCellFormatter().setWidth(row, column, width + "%");
				myContent.getCellFormatter().setVerticalAlignment(row, column, HasVerticalAlignment.ALIGN_TOP);

				// Increment appropriately
				if (layout.equalsIgnoreCase(HORIZONTAL))
					column++;
				else if (layout.equalsIgnoreCase(VERTICAL))
					row++;
				else if (layout.equalsIgnoreCase(TABLE)) {
					String userRow = XMLUtils.getXMLAttribute(currentNode, "row", null);
					if (userRow == null) {
						column++;
					} else { // Onto the next row...
						// But lets fix this one first.
						double tableWidth = 100 / (column + 1); // Can never
						// divide by 0
						for (int i = 0; i <= column; i++)
							myContent.getCellFormatter().setWidth(row, i, tableWidth + "%");

						// Now increment to the next row, restart column count
						row = Integer.parseInt(userRow);
						column = 0;
					}
				}
			}// END try
			catch (Exception e) {
				Debug.println(e);
			}
		}// End for

		content.add(myContent);
	}

	/**
	 * Helper function to parse a field tag
	 * 
	 * @param currentRow
	 *            the current row
	 * @param currentColumn
	 *            the current column
	 * @param displays
	 *            this assessments displays
	 * @param currentNode
	 *            the node with data
	 */
	private void parseFieldTag(int currentRow, int currentColumn, NativeElement fieldNode, LayoutContainer content) {
		String curField = XMLUtils.getXMLAttribute(fieldNode, "id");

		try {
			final HorizontalPanel myContent = new HorizontalPanel();
			myContent.addStyleName("SISPage_Field");

			if (viewOnly)
				myContent.add(FieldWidgetCache.impl.get(curField).showViewOnly());
			else
				myContent.add(FieldWidgetCache.impl.get(curField).showDisplay());

			//myContent.setWidth("96%");
			myContent.setWidth("100%");
			content.add(myContent);

		} catch (Exception e) {
			Debug.println("Failed to add {0}. Check your spelling.\n{1}", curField, e);
		}
	}

	/**
	 * Helper function to parse related fields
	 * 
	 * @param currentRow
	 *            the current row
	 * @param currentColumn
	 *            the current column
	 * @param displays
	 *            the displays for this assessment
	 * @param currentNode
	 *            the node with information
	 * @return the current row
	 */
	private int parseRelatedTag(int currentRow, int currentColumn, NativeElement currentNode, LayoutContainer content) {
		NativeNodeList relatedFields = currentNode.getChildNodes();
		RelatedDisplays dominant = null;
		String curField = "";

		String layout = XMLUtils.getXMLAttribute(currentNode, "layout", null);

		for (int i = 0; i < relatedFields.getLength(); i++) {
			currentNode = relatedFields.elementAt(i);
			String fieldType = currentNode.getNodeName();

			if (fieldType.equalsIgnoreCase("dominantField")) {
				curField = XMLUtils.getXMLAttribute(currentNode, "id");
				dominant = new RelatedDisplays(FieldWidgetCache.impl.get(curField));
				String row = XMLUtils.getXMLAttribute(currentNode, "row", null);
				if (row != null)
					dominant.setDominantRow(Integer.parseInt(row));
			} else if (fieldType.equalsIgnoreCase("dependent")) {
				curField = XMLUtils.getXMLAttribute(currentNode, "id");

				Display curDisplay = FieldWidgetCache.impl.get(curField);
				String row = XMLUtils.getXMLAttribute(currentNode, "row", null);
				if (row != null)
					dominant.addDependantDisplay(curDisplay, Integer.parseInt(row));
				else
					dominant.addDependantDisplay(curDisplay);
			} else if (fieldType.equalsIgnoreCase("activeRule")) {
				String ruleType = currentNode.getChildNodes().item(1).getNodeName();
				String onTrue = Rule.SHOW;
				String onFalse = Rule.HIDE;
				String activateOnValue = "";

				NativeNodeList list = currentNode.getChildNodes().item(1).getChildNodes();

				for (int listSearch = 0; listSearch < list.getLength(); listSearch++) {
					if (list.item(listSearch).getNodeName().equalsIgnoreCase("activateOnValue")) {
						activateOnValue = XMLUtils.getXMLValue(list.item(listSearch), "");
					} else if (list.item(listSearch).getNodeName().equalsIgnoreCase("actions")) {
						for (int acts = 0; acts < list.item(listSearch).getChildNodes().getLength(); acts++) {
							if (list.item(listSearch).getChildNodes().item(acts).getNodeName().equalsIgnoreCase(
									"onTrue")) {
								onTrue = XMLUtils.getXMLValue(list.item(listSearch).getChildNodes().item(acts),
										Rule.SHOW);
							} else if (list.item(listSearch).getChildNodes().item(acts).getNodeName().equalsIgnoreCase(
									"onFalse")) {
								onFalse = XMLUtils.getXMLValue(list.item(listSearch).getChildNodes().item(acts),
										Rule.HIDE);
							}
						}
					}
				}

				if (ruleType.equalsIgnoreCase(XMLUtils.BOOLEAN_RULE)) {
					BooleanRule rule = new BooleanRule(activateOnValue);
					rule.setOnTrue(onTrue);
					rule.setOnFalse(onFalse);
					dominant.addRule(rule);
				} else if (ruleType.equalsIgnoreCase(XMLUtils.SELECT_RULE)) {
					SelectRule rule = new SelectRule(activateOnValue);
					rule.setOnTrue(onTrue);
					rule.setOnFalse(onFalse);
					dominant.addRule(rule);
				} else if (ruleType.equalsIgnoreCase(XMLUtils.CONTENT_RULE)) {
					ContentRule rule = new ContentRule(activateOnValue);
					rule.setOnTrue(onTrue);
					rule.setOnFalse(onFalse);
					dominant.addRule(rule);
				}
			}
		}

		dominant.setDisplayType(layout);

		HorizontalPanel myContent = new HorizontalPanel();
		myContent.addStyleName("SISPage_Field");
		if (viewOnly)
			myContent.add(dominant.showViewOnly());
		else
			myContent.add(dominant.showDisplay());

		content.add(myContent);

		// displayObjects.add(dominant);

		// return currentRow;
		return ++currentRow;
	}

	public void removeMyFields() {
		for (Display dis : myFields)
			if (dis != null)
				dis.removeStructures();
	}

	public void showPage(final DrawsLazily.DoneDrawingCallbackWithParam<SISPageHolder> callback) {
		showPage(callback, false);
	}

	public void showPage(final DrawsLazily.DoneDrawingCallbackWithParam<SISPageHolder> callback, final boolean viewOnly) {
		removeAll();
		// add(new TabItem());
		// WindowUtils.showLoadingAlert("Loading page...");

		fetchFields(new GenericCallback<String>() {
			public void onFailure(Throwable caught) {
				GWT.log("Failed to show page " + pageTitle, caught);
			}

			public void onSuccess(String arg0) {
				// new GeneratePageTimer(viewOnly).schedule(500);
				try {
					generatePage(viewOnly);
				} catch (Throwable e) {
					e.printStackTrace();
					GWT.log("Failed to generate page " + pageTitle, e);
				}
				WindowUtils.hideLoadingAlert();
				
				layout();
				setSelection(getItem(selectedTab));
				
				callback.isDrawn(SISPageHolder.this);
			}
		});

		//return this;
	}

	// private void clearDisplays()
	// {
	// displayObjects.clear();
	// }
}