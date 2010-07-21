/**
 * SISPageHolder.java
 * 
 * Represents a Page
 * 
 * @author adam.schwartz
 * @author carl.scott
 */

package org.iucn.sis.client.displays;

import org.iucn.sis.client.components.ClientUIContainer;
import org.iucn.sis.shared.data.FieldWidgetCache;
import org.iucn.sis.shared.structures.BooleanRule;
import org.iucn.sis.shared.structures.ContentRule;
import org.iucn.sis.shared.structures.Rule;
import org.iucn.sis.shared.structures.SelectRule;
import org.iucn.sis.shared.xml.XMLUtils;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;
import com.solertium.lwxml.gwt.debug.SysDebugger;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.lwxml.shared.NativeElement;
import com.solertium.lwxml.shared.NativeNode;
import com.solertium.lwxml.shared.NativeNodeList;
import com.solertium.util.extjs.client.WindowUtils;

public class SISPageHolderMyGWT extends TabPanel {

	private class GeneratePageTimer extends Timer {
		private boolean view;

		GeneratePageTimer(boolean viewOnly) {
			view = viewOnly;
		}

		@Override
		public void run() {
			generatePage(view);
			WindowUtils.hideLoadingAlert();

			redrawDEM();
			setSelection(getItem(selectedTab));
		}
	}

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
	public SISPageHolderMyGWT(String title, String id, NativeElement pageTag) {
		super();
		pageOrganizations = pageTag.getElementsByTagName("pageOrganizations");
		this.pageTag = pageTag;
		pageTitle = title;
		pageID = id;
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
	private void addTitleToOrganization(NativeElement currentNode, TabItem curTab) {
		String title = XMLUtils.getXMLAttribute(currentNode, "title", null);
		String shortTitle = XMLUtils.getXMLAttribute(currentNode, "shortTitle", null);

		int magicLength = 10;

		if (title == null && shortTitle == null) {
			shortTitle = "Page";
			title = "No name was specified";
		} else if (shortTitle == null) {
			if (title.length() > magicLength)
				shortTitle = title.substring(0, magicLength) + "...";
			else
				shortTitle = title;
		} else if (title == null) {
			title = shortTitle;
		}

		curTab.setText(shortTitle);
		curTab.setToolTip(title);

		HTML titleHTML = new HTML("&nbsp &nbsp " + title);
		titleHTML.addStyleName("bold");
		titleHTML.addStyleName("color-dark-blue");

		curTab.add(new HTML("<br />"));
		curTab.add(titleHTML);
		curTab.add(new HTML("<br />"));
	}

	private void fetchFields(final GenericCallback<String> wayBack) {
		String fieldNames = "";
		NativeNodeList fields = pageTag.getElementsByTagName("field");
		for (int i = 0; i < fields.getLength(); i++)
			fieldNames += fields.elementAt(i).getAttribute("id") + ",";

		fieldNames = fieldNames.substring(0, fieldNames.length());

		if (fieldNames.equalsIgnoreCase(""))
			wayBack.onSuccess("OK");
		else {
			FieldWidgetCache.impl.prefetchList(fieldNames, new GenericCallback<String>() {
				public void onFailure(Throwable caught) {
					wayBack.onFailure(caught);
				}

				public void onSuccess(String arg0) {
					wayBack.onSuccess("OK");
				}
			});
		}
	}

	private void generatePage() {
		generatePage(false);
	}

	private void generatePage(boolean viewOnly) {
		// if( this.viewOnly == viewOnly && getItemCount() > 0 )
		// return;

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
				curTab.addListener(Events.Select, new Listener() {
					public void handleEvent(BaseEvent be) {
						selectedTab = indexOf(curTab);
						curTab.layout();
					}
				});
				LayoutContainer content = curTab;
				content.setLayout(new FlowLayout(4));
				content.addStyleName("gwt-background");
				content.setScrollMode(Scroll.AUTO);

				// Set the fieldArrangement type (there is only one per
				// organization)
				NativeElement currentNode = organizations.elementAt(i);
				addTitleToOrganization(currentNode, curTab);

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

					// Insert a block of text
					else if (fieldArrangement.equalsIgnoreCase("text")) {
						currentColumn = 0;
						/*
						 * contentTable.setWidget(currentRow++, currentColumn,
						 * new
						 * HTML(XMLUtils.getXMLValue(currentNode.getChildNodes
						 * ().item(j))));
						 */
						HTML myContent = new HTML(XMLUtils.getXMLValue(currentNode.getChildNodes().item(j)));
						myContent.addStyleName("SIS_titleTableRow");
						content.add(myContent);
					}
				}// for all the organizations children...

				add(curTab);

			}// for organizations
		}// for pageOrganizations
	}// method

	/**
	 * Returns this page's unique ID
	 * 
	 * @return the pageID
	 */
	public String getPageID() {
		return pageID;
	}

	// protected void onResize(int width, int height)
	// {
	// super.onResize(width, height);
	// // if( getSelection() != null )
	// // getSelection().setSize(getWidth(true), getHeight(true));
	// }

	/**
	 * Returns this page's title, which may or may not be unique
	 * 
	 * @return the page title
	 */
	public String getPageTitle() {
		return pageTitle;
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

		LayoutContainer myContent = new LayoutContainer();
		myContent.setLayout(new FlowLayout());
		LayoutContainer innerContent = new LayoutContainer();
		innerContent.setLayout(new FlowLayout());

		for (int k = 0; k < compositeFields.getLength(); k++) {
			currentNode = compositeFields.elementAt(k);

			String curField = XMLUtils.getXMLAttribute(currentNode, "id");
			try {
				if (viewOnly)
					innerContent.add(FieldWidgetCache.impl.get(curField).showViewOnly());
				else
					innerContent.add(FieldWidgetCache.impl.get(curField).showDisplay());
			}// END try
			catch (Exception e) {
				SysDebugger.getInstance().println("SISPH: ouch");
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
			LayoutContainer myContent = new LayoutContainer();
			myContent.setLayout(new FlowLayout());
			myContent.addStyleName("SISPage_Field");

			if (viewOnly)
				myContent.add(FieldWidgetCache.impl.get(curField).showViewOnly());
			else
				myContent.add(FieldWidgetCache.impl.get(curField).showDisplay());

			content.add(myContent);

		} catch (Exception e) {
			System.err.println("Failed to add " + curField + ". Check your spelling.");
			e.printStackTrace();
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

		LayoutContainer myContent = new LayoutContainer();
		myContent.setLayout(new FlowLayout());
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

	private void redrawDEM() {
		ClientUIContainer.bodyContainer.getTabManager().getPanelManager().DEM.layout();
	}

	public Widget showPage() {
		return showPage(false);
	}

	public Widget showPage(final boolean viewOnly) {
		WindowUtils.showLoadingAlert("Loading page...");

		fetchFields(new GenericCallback<String>() {
			public void onFailure(Throwable caught) {

			}

			public void onSuccess(String arg0) {
				new GeneratePageTimer(viewOnly).schedule(500);
			}
		});

		return this;
	}

	// private void clearDisplays()
	// {
	// displayObjects.clear();
	// }
}