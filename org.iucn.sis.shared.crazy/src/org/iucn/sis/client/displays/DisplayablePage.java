package org.iucn.sis.client.displays;

///**
// * SISPageHolder.java
// * 
// * Represents a Page
// * 
// * @author adam.schwartz
// * @author carl.scott
// */
//
//package org.iucn.sis.client.displays;
//
//import java.util.ArrayList;
//import java.util.Iterator;
//
//import org.iucn.sis.client.mygwt.ClientUIContainer;
//import org.iucn.sis.client.mygwt.panels.DEMPanel;
//import org.iucn.sis.shared.data.FieldWidgetCache;
//import org.iucn.sis.shared.data.assessments.AssessmentCache;
//import org.iucn.sis.shared.data.assessments.NotesCache;
//import org.iucn.sis.shared.structures.BooleanRule;
//import org.iucn.sis.shared.structures.ContentRule;
//import org.iucn.sis.shared.structures.Rule;
//import org.iucn.sis.shared.structures.SelectRule;
//import org.iucn.sis.shared.view.Composite;
//import org.iucn.sis.shared.view.Organization;
//import org.iucn.sis.shared.view.Page;
//import org.iucn.sis.shared.xml.XMLUtils;
//
//import com.extjs.gxt.ui.client.event.Events;
//import com.extjs.gxt.ui.client.Style.Scroll;
//import com.extjs.gxt.ui.client.event.BaseEvent;
//import com.extjs.gxt.ui.client.event.Listener;
//import com.extjs.gxt.ui.client.widget.LayoutContainer;
//import com.extjs.gxt.ui.client.widget.TabItem;
//import com.extjs.gxt.ui.client.widget.TabPanel;
//import com.extjs.gxt.ui.client.widget.layout.FitLayout;
//import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
//import com.google.gwt.user.client.Timer;
//import com.google.gwt.user.client.rpc.GenericCallback;
//import com.google.gwt.user.client.ui.FlexTable;
//import com.google.gwt.user.client.ui.HTML;
//import com.google.gwt.user.client.ui.HasVerticalAlignment;
//import com.google.gwt.user.client.ui.HorizontalPanel;
//import com.google.gwt.user.client.ui.SimplePanel;
//import com.google.gwt.user.client.ui.VerticalPanel;
//import com.solertium.extgwt.utils.WindowUtils;
//import com.solertium.gwt.lwxml.NativeElement;
//import com.solertium.gwt.lwxml.NativeNode;
//import com.solertium.gwt.lwxml.NativeNodeList;
//import com.solertium.gwt.utils.SysDebugger;
//
//public class DisplayablePage extends TabPanel {
//
//	public static final String VERTICAL = "vertical";
//	public static final String HORIZONTAL = "horizontal";
//	public static final String TABLE = "table";
//
//	private boolean viewOnly = false;
//
//	private Page page;
//
//	/**
//	 * ArrayList<TabItem> - holds each pageOrganization as a tab
//	 */
//	private int selectedTab = 0;
//
//	private ArrayList<Display> myFields;
//
//	public DisplayablePage(Page page) {
//		this.page = page;
//		myFields = new ArrayList<Display>();
//		setTabScroll(true);
//	}
//
//	/**
//	 * Returns this page's unique ID
//	 * 
//	 * @return the pageID
//	 */
//	public String getPageID() {
//		return page.getId();
//	}
//
//	/**
//	 * Returns this page's title, which may or may not be unique
//	 * 
//	 * @return the page title
//	 */
//	public String getPageTitle() {
//		return page.getTitle();
//	}
//
//	public boolean isViewOnly() {
//		return viewOnly;
//	}
//
//	public void removeMyFields() {
//		for (Display curDisplay : myFields ) {
//			curDisplay.removeStructures();
//		}
//	}
//
//	public TabPanel showPage() {
//		return showPage(false);
//	}
//
//	public TabPanel showPage(final boolean viewOnly) {
//		removeAll();
//		// add(new TabItem());
//		// WindowUtils.showLoadingAlert("Loading page...");
//
//		fetchFields(new GenericCallback<String>() {
//			public void onSuccess(String arg0) {
//				// new GeneratePageTimer(viewOnly).schedule(500);
//				new GeneratePageTimer(viewOnly).run();
//			}
//
//			public void onFailure(Throwable caught) {
//
//			}
//		});
//
//		return this;
//	}
//
//	private void fetchFields(final GenericCallback<String> wayBack) {
//		final ArrayList<String> fieldNameList = page.getMyFields();
//		String fieldNames = "";
//
//		for (String curField : fieldNameList ) {
//			fieldNames += curField + ",";
//			fieldNameList.add(curField);
//		}
//
//		fieldNames = fieldNames.substring(0, fieldNames.length() - 1);
//
//		if (fieldNames.equalsIgnoreCase(""))
//			NotesCache.impl.fetchNotes(AssessmentCache.impl.getCurrentAssessment(), wayBack);
//		else {
//			FieldWidgetCache.impl.prefetchList(fieldNames, new GenericCallback<String>() {
//				public void onSuccess(String arg0) {
//					for (String curFieldName : fieldNameList )
//						myFields.add( FieldWidgetCache.impl.get(curFieldName) );
//
//					NotesCache.impl.fetchNotes(AssessmentCache.impl.getCurrentAssessment(), wayBack);
//				}
//
//				public void onFailure(Throwable caught) {
//					wayBack.onFailure(caught);
//				}
//			});
//		}
//	}
//
//	private class GeneratePageTimer extends Timer {
//		private boolean view;
//
//		GeneratePageTimer(boolean viewOnly) {
//			view = viewOnly;
//		}
//
//		public void run() {
//			generatePage(view);
//			WindowUtils.hideLoadingAlert();
//
//			redrawDEM();
//			setSelection(getItem(selectedTab));
//		}
//	}
//
//	private void generatePage(boolean viewOnly) {
//		int currentRow = 0;
//		int currentColumn = 0;
//		this.viewOnly = viewOnly;
//
//		// clearDisplays();
//		// content.setText( pageTitle + " --- " +
//		// AssessmentCache.impl.getCurrentAssessment().getSpeciesName() );
//		// content.removeAll();
//
//		removeAll();
//
//		// FOR EACH PAGEORGANIZATION
//		for (Organization curOrganization : page.getOrganizations()) {
//
//			// Parsing the organizations
//			final TabItem curTab = new TabItem();
//			curTab.addListener(Events.Select, new Listener<BaseEvent>() {
//				public void handleEvent(BaseEvent be) {
//					selectedTab = indexOf(curTab);
//				}
//			});
//			LayoutContainer content = new LayoutContainer();
//			content.setLayout(new FlowLayout(0));
//			content.addStyleName("x-panel");
//			content.setScrollMode(Scroll.AUTOY);
//
//			curTab.setLayout(new FitLayout());
//			curTab.add(content);
//
//			// Set the fieldArrangement type (there is only one per
//			// organization)
//			addTitleToOrganization(curOrganization.getTitle(), 
//					curOrganization.getShortTitle(), curTab, content);
//
//			for (Composite curComposite : curOrganization.getComposites()) {
//				ArrayList<String> fields = curComposite.getFields();
//
//				// Composite Field -- one row containing multiple fields
//				if (fieldArrangement.equalsIgnoreCase("composite")) {
//					currentColumn = 0;
//					parseCompositeTag(currentRow, currentColumn, (NativeElement) curArrangementNode, content);
//					currentRow++;
//				}
//
//				// Insert a block of text
//				if (curOrganization.getText() != null) {
//					currentColumn = 0;
//					/*
//					 * contentTable.setWidget(currentRow++, currentColumn,
//					 * new
//					 * HTML(XMLUtils.getXMLValue(currentNode.getChildNodes
//					 * ().item(j))));
//					 */
//					VerticalPanel wrapper = new VerticalPanel();
//					// wrapper.addStyleName("SIS_titleTableRow");
//					wrapper.addStyleName("outsetFrameBorder");
//					wrapper.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
//
//					HTML myContent = new HTML(curOrganization.getText() + "&nbsp;&nbsp;&nbsp;");
//					// myContent.addStyleName("SIS_titleTableRow");
//					wrapper.add(myContent);
//					wrapper.setSize("96%", "20px");
//
//					content.add(wrapper);
//				}
//			}// for all the organizations children...
//
//			add(curTab);
//
//		}// for organizations
//	}// for pageOrganizations
//}// method
//
///**
// * Adds the field's title as part of the display of the structure(s) (i.e. a
// * section title)
// * 
// * @param currentNode
// *            the organization node
// * @param currentRow
// *            the current row
// * @param the
// *            number of columns in this row
// * @return the current row, incremented if successful/nec'y, not otherwise
// */
//private void addTitleToOrganization(String title, String shortTitle, TabItem curTab, LayoutContainer content) {
//	if (title == null && shortTitle == null) {
//		shortTitle = "Page";
//		title = "No name was specified";
//	} else if (shortTitle == null) {
//		shortTitle = title;
//	} else if (title == null) {
//		title = shortTitle;
//	}
//
//	curTab.setText(shortTitle);
//	// curTab.setToolTip(title);
//
//	HTML titleHTML = new HTML("&nbsp &nbsp " + title);
//	titleHTML.addStyleName("bold");
//	titleHTML.addStyleName("color-dark-blue");
//
//	content.add(new HTML("<br />"));
//	content.add(titleHTML);
//	content.add(new HTML("<br />"));
//}
//
///**
// * Helper function to parse related fields
// * 
// * @param currentRow
// *            the current row
// * @param currentColumn
// *            the current column
// * @param displays
// *            the displays for this assessment
// * @param currentNode
// *            the node with information
// * @return the current row
// */
//private int parseRelatedTag(int currentRow, int currentColumn, NativeElement currentNode, LayoutContainer content) {
//	NativeNodeList relatedFields = currentNode.getChildNodes();
//	RelatedDisplays dominant = null;
//	String curField = "";
//
//	String layout = XMLUtils.getXMLAttribute(currentNode, "layout", null);
//
//	for (int i = 0; i < relatedFields.getLength(); i++) {
//		currentNode = relatedFields.elementAt(i);
//		String fieldType = currentNode.getNodeName();
//
//		if (fieldType.equalsIgnoreCase("dominantField")) {
//			curField = XMLUtils.getXMLAttribute(currentNode, "id");
//			dominant = new RelatedDisplays(FieldWidgetCache.impl.get(curField));
//			String row = XMLUtils.getXMLAttribute(currentNode, "row", null);
//			if (row != null)
//				dominant.setDominantRow(Integer.parseInt(row));
//		} else if (fieldType.equalsIgnoreCase("dependent")) {
//			curField = XMLUtils.getXMLAttribute(currentNode, "id");
//
//			Display curDisplay = FieldWidgetCache.impl.get(curField);
//			String row = XMLUtils.getXMLAttribute(currentNode, "row", null);
//			if (row != null)
//				dominant.addDependantDisplay(curDisplay, Integer.parseInt(row));
//			else
//				dominant.addDependantDisplay(curDisplay);
//		} else if (fieldType.equalsIgnoreCase("activeRule")) {
//			String ruleType = currentNode.getChildNodes().item(1).getNodeName();
//			String onTrue = Rule.SHOW;
//			String onFalse = Rule.HIDE;
//			String activateOnValue = "";
//
//			NativeNodeList list = currentNode.getChildNodes().item(1).getChildNodes();
//
//			for (int listSearch = 0; listSearch < list.getLength(); listSearch++) {
//				if (list.item(listSearch).getNodeName().equalsIgnoreCase("activateOnValue")) {
//					activateOnValue = XMLUtils.getXMLValue(list.item(listSearch), "");
//				} else if (list.item(listSearch).getNodeName().equalsIgnoreCase("actions")) {
//					for (int acts = 0; acts < list.item(listSearch).getChildNodes().getLength(); acts++) {
//						if (list.item(listSearch).getChildNodes().item(acts).getNodeName().equalsIgnoreCase(
//								"onTrue")) {
//							onTrue = XMLUtils.getXMLValue(list.item(listSearch).getChildNodes().item(acts),
//									Rule.SHOW);
//						} else if (list.item(listSearch).getChildNodes().item(acts).getNodeName().equalsIgnoreCase(
//						"onFalse")) {
//							onFalse = XMLUtils.getXMLValue(list.item(listSearch).getChildNodes().item(acts),
//									Rule.HIDE);
//						}
//					}
//				}
//			}
//
//			if (ruleType.equalsIgnoreCase(XMLUtils.BOOLEAN_RULE)) {
//				BooleanRule rule = new BooleanRule(activateOnValue);
//				rule.setOnTrue(onTrue);
//				rule.setOnFalse(onFalse);
//				dominant.addRule(rule);
//			} else if (ruleType.equalsIgnoreCase(XMLUtils.SELECT_RULE)) {
//				SelectRule rule = new SelectRule(activateOnValue);
//				rule.setOnTrue(onTrue);
//				rule.setOnFalse(onFalse);
//				dominant.addRule(rule);
//			} else if (ruleType.equalsIgnoreCase(XMLUtils.CONTENT_RULE)) {
//				ContentRule rule = new ContentRule(activateOnValue);
//				rule.setOnTrue(onTrue);
//				rule.setOnFalse(onFalse);
//				dominant.addRule(rule);
//			}
//		}
//	}
//
//	dominant.setDisplayType(layout);
//
//	HorizontalPanel myContent = new HorizontalPanel();
//	myContent.addStyleName("SISPage_Field");
//	if (viewOnly)
//		myContent.add(dominant.showViewOnly());
//	else
//		myContent.add(dominant.showDisplay());
//
//	content.add(myContent);
//
//	// displayObjects.add(dominant);
//
//	// return currentRow;
//	return ++currentRow;
//}
//
///**
// * helper function to parse a row with multiple fields
// * 
// * @param currentRow
// *            the current row
// * @param currentColumn
// *            the current column
// * @param displays
// *            this assessments displays
// * @param currentNode
// *            the node with data
// */
//private void parseCompositeTag(int currentRow, int currentColumn, NativeElement compositeNode,
//		LayoutContainer content) {
//	NativeElement currentNode;
//	NativeNodeList compositeFields = compositeNode.getElementsByTagName("field");
//	String layout = XMLUtils.getXMLAttribute(compositeNode, "layout", HORIZONTAL);
//
//	FlexTable myContent = new FlexTable();
//	myContent.setWidth("96%");
//	myContent.setCellSpacing(2);
//	myContent.setCellPadding(2);
//
//	int row = 0;
//	int column = 0;
//	int numCols = compositeFields.getLength();
//
//	double width = 96;
//	try {
//		width = 96 / numCols;
//	} catch (Exception e) {
//	}
//	// SysDebugger.getInstance().println("Width is: " + width);
//
//	for (int k = 0; k < compositeFields.getLength(); k++) {
//		currentNode = compositeFields.elementAt(k);
//
//		String curField = XMLUtils.getXMLAttribute(currentNode, "id");
//		try {
//			// Make the inner panel contain the field, make it big as it
//			// gets
//			final SimplePanel myContent2 = new SimplePanel();
//			myContent2.setWidth("96%");
//
//			if (viewOnly)
//				myContent2.setWidget(FieldWidgetCache.impl.get(curField).showViewOnly());
//			else
//				myContent2.setWidget(FieldWidgetCache.impl.get(curField).showDisplay());
//
//			// Add it to the table appropriately, set the style, the
//			// appropriate width
//			// for the cell, and align it nicely
//			myContent.setWidget(row, column, myContent2);
//			myContent.getCellFormatter().addStyleName(row, column, "SISPage_Field");
//			myContent.getCellFormatter().setWidth(row, column, width + "%");
//			myContent.getCellFormatter().setVerticalAlignment(row, column, HasVerticalAlignment.ALIGN_TOP);
//
//			// Increment appropriately
//			if (layout.equalsIgnoreCase(HORIZONTAL))
//				column++;
//			else if (layout.equalsIgnoreCase(VERTICAL))
//				row++;
//			else if (layout.equalsIgnoreCase(TABLE)) {
//				String userRow = XMLUtils.getXMLAttribute(currentNode, "row", null);
//				if (userRow == null) {
//					column++;
//				} else { // Onto the next row...
//					// But lets fix this one first.
//					double tableWidth = 96 / (column + 1); // Can never
//					// divide by 0
//					for (int i = 0; i <= column; i++)
//						myContent.getCellFormatter().setWidth(row, i, tableWidth + "%");
//
//					// Now increment to the next row, restart column count
//					row = Integer.parseInt(userRow);
//					column = 0;
//				}
//			}
//		}// END try
//		catch (Exception e) {
//			SysDebugger.getInstance().println("SIS Page Holder: ouch");
//			e.printStackTrace();
//		}
//	}// End for
//
//	content.add(myContent);
//}
//
///**
// * Helper function to parse a field tag
// * 
// * @param currentRow
// *            the current row
// * @param currentColumn
// *            the current column
// * @param displays
// *            this assessments displays
// * @param currentNode
// *            the node with data
// */
//private void parseFieldTag(int currentRow, int currentColumn, NativeElement fieldNode, LayoutContainer content) {
//	String curField = XMLUtils.getXMLAttribute(fieldNode, "id");
//
//	try {
//		final HorizontalPanel myContent = new HorizontalPanel();
//		myContent.addStyleName("SISPage_Field");
//
//		if (viewOnly)
//			myContent.add(FieldWidgetCache.impl.get(curField).showViewOnly());
//		else
//			myContent.add(FieldWidgetCache.impl.get(curField).showDisplay());
//
//		myContent.setWidth("96%");
//		content.add(myContent);
//
//	} catch (Exception e) {
//		System.err.println("Failed to add " + curField + ". Check your spelling.");
//		e.printStackTrace();
//	}
//}
//
//private void redrawDEM() {
//	DEMPanel dem = ClientUIContainer.bodyContainer.getTabManager().getPanelManager().DEM;
//	dem.layout();
//	setSize(getOffsetWidth(), getOffsetHeight());
//}
//
//// private void clearDisplays()
//// {
//// displayObjects.clear();
//// }
//}