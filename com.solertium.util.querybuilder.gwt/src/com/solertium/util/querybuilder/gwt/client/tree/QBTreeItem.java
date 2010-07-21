package com.solertium.util.querybuilder.gwt.client.tree;

import java.util.ArrayList;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.solertium.util.gwt.ui.StyledHTML;
import com.solertium.util.querybuilder.gwt.client.chooser.TableChooser;
import com.solertium.util.querybuilder.gwt.client.chooser.TableChooserSaveListener;
import com.solertium.util.querybuilder.gwt.client.tree.QBTreeItem.ContextMenu.SmartCommand;
import com.solertium.util.querybuilder.gwt.client.utils.GWTQBQuery;
import com.solertium.util.querybuilder.query.SelectedField;
import com.solertium.util.querybuilder.struct.DBStructure;


/**
 * CIPDTreeItem.java
 *
 * Creates a row in the Query Builder tree
 *
 * @author carl.scott
 */
public abstract class QBTreeItem extends VerticalPanel {

	protected ArrayList<QBTreeItem> children;	//Group of CIPDTreeItems
	protected VerticalPanel childPanel;			//Displays children

	protected QBTreeItem myParent;				//The parent
	protected String display = null;

	protected boolean isRoot;					//Is this the root?
	protected DBStructure db;

	protected Image button;						//The expand/collapse buttons
	protected boolean treeIsExpanded = false;	//Is the tree currently expanded

	protected SimplePanel leftMarginPanel;		//Indentations
	protected int leftMargin = 0;				//Size of the indentation

	protected boolean isShown = false;			//Has the tree item been rendered?
	private FieldRootLevelContextMenu menu;

	/**
	 * Creates a new CIPDTreeItem.
	 * @param url the url
	 * @param ri the resource info
	 * @param leftMargin indentation
	 */
	public QBTreeItem(int leftMargin) {
		this(leftMargin, false);
	}

	public abstract GWTQBQuery getQuery();


	/**
	 * Creates a new CIPDTreeItem, with the option of setting it as the root of the tree.
	 * There should only be one root.
	 * @param url the url
	 * @param ri the resource info
	 * @param leftMargin indentation
	 * @param isRoot true if root, false otherwise
	 */
	public QBTreeItem(int leftMargin, boolean isRoot) {
		super();
		this.db = DBStructure.getInstance();
		this.leftMargin = leftMargin;
		this.isRoot = isRoot;

		children = new ArrayList<QBTreeItem>();
		childPanel = new VerticalPanel();

		setSpacing(2);
	}

	/******* GETTERS, SETTERS, AND THE LIKE **********/

	/**
	 * Adds a child to this tree Item
	 * @param child the SISTreeItem to add
	 */
	public void addChild(QBTreeItem child) {
		child.setMyParent(this);
		children.add(child);
	}

	public void removeChild(QBTreeItem child) {
		children.remove(child);
		child.removeFromParent();
	}

	/**
	 * Determines if this tree item has children
	 * @return true if so, false otherwise
	 */
	public boolean hasChildren() {
		return !children.isEmpty();
	}

	/**
	 * Does a full refresh, clears UI and data structures,
	 * and shows the object again
	 *
	 */
	public void refresh() {
		clear();
		children.clear();
		childPanel.clear();

		isShown = false;
		button = null;
		show();
	}

	/********** SHOW THE TREE *************/

	/**
	 * Collapses the tree.  Obviously, trees are collapsed by
	 * default (to be consistent with our lazy setup)
	 */
	public void collapseTree() {
		treeIsExpanded = false;
		button.setUrl("images/tree_closed.gif");
		setChildrenVisible(false);
	}

	/**
	 * Expands the tree
	 */
	public void expandTree() {
		treeIsExpanded = true;
		button.setUrl("images/tree_open.gif");
		setChildrenVisible(true);
	}

	/********** BUILD THE TREE *************/

	/**
	 * Builds a expand/collapse button for this item, or a blank one
	 * if necessary. Only builds it once.
	 * @return the button as an image
	 */
	protected Image buildButton() {
		if (button == null) {
			if (isRoot) {
				button = new Image("images/tree_closed.gif");
				button.addClickListener(new ClickListener() {
					public void onClick(Widget sender) {
						if (treeIsExpanded)
							collapseTree();
						else
							expandTree();
					}
				});
			} else {
				button = new Image("images/tree_white.gif");
			}
		}
		return button;
	}

	/**
	 * Builds the indentations for the tree item. Only does this once.
	 * @return the panel to indent the tree item
	 */
	private Widget buildLeftMargin() {
		if (leftMarginPanel == null) {
			leftMarginPanel = new SimplePanel();
			leftMarginPanel.setWidth(leftMargin+"px");
		}
		return leftMarginPanel;
	}

	/**
	 * Builds the tree item.  Only does this one.
	 */
	public void show() {
		show(null);
	}

	/**
	 * Shows the tree row, with an optional display name
	 * @param display the display name, or *null* if you dont want one
	 */
	public void show(final String display) {
		if (this.display == null)
			this.display = display;
		if (!isShown) {
			load();
		}
	}

	public String getIcon() {
		return "images/small/folder.png";
	}

	/**
	 * This function must call draw cuz it could be async.
	 *
	 */
	public void load() {
		GWTQBQuery query = getQuery();
		for (int i = 0; i < query.getFields().size(); i++) {
			addChild(new QBFieldItem(query.getFields().get(i)) {
				public GWTQBQuery getQuery() {
					return QBTreeItem.this.getQuery();
				}
			});
		}
		draw();
	}

	/**
	 * Draws the tree row.
	 *
	 */
	protected void draw() {
		FlexTable row = new FlexTable();
		row.setWidget(0, 0, buildLeftMargin());
		row.setWidget(0, 1, buildButton());

		RowUI imgPanel = new RowUI();
		if (getIcon() != null)
			imgPanel.add(new Image(getIcon()));
		imgPanel.add(new StyledHTML(display, "fontSize80;clickable"));

		row.setWidget(0, 2, imgPanel);

		//Add the row and the child panel to this tree item
		add(row);
		add(childPanel);

		//Set this so this function never does work again!
		isShown = true;

		if (treeIsExpanded)
			expandTree();
	}

	/**
	 * When the tree is expanded or collapse, children's visibility
	 * needs to be updated!
	 * @param isVisible true if expanded, false otherwise
	 */
	private void setChildrenVisible(boolean isVisible) {
		if (isVisible) {	//Expand the tree

			for (int i = 0; i < children.size(); i++) {
				QBTreeItem current = children.get(i);

				//Now that this row is being requested, build it
				current.show();

				//Add it to the parent's childPanel
				childPanel.add(current);
			}

			childPanel.setVisible(true);
		}
		else {				//Collapse the tree
			childPanel.setVisible(false);
		}
	}

	public QBTreeItem getMyParent() {
		return myParent;
	}

	private void setMyParent(QBTreeItem item) {
		myParent = item;
	}

	/**
	 * Glorified HorizontalPanel that responds to both left click
	 * and right click to pull up the appropriate context menu for
	 * the current tree item.
	 *
	 * @author carl.scott
	 *
	 */
	class RowUI extends HorizontalPanel {

		public RowUI() {
			super();
			setSpacing(2);
			sinkEvents(Event.ONMOUSEDOWN);
		}

		public void onBrowserEvent(Event evt) {
			switch(DOM.eventGetType(evt)) {
				case Event.ONMOUSEDOWN: {
					if (DOM.eventGetButton(evt) == Event.BUTTON_RIGHT ||
						DOM.eventGetButton(evt) == Event.BUTTON_LEFT) {
						showContextMenu(evt);
					}
					break;
				}
				default:
					super.onBrowserEvent(evt);
			}
		}

		private void showContextMenu(Event evt) {
			final ContextMenu contextMenu = getContextMenu();
			if (contextMenu == null)
				return;

			int currentMouseXPos = DOM.eventGetClientX(evt);
			int currentMouseYPos = DOM.eventGetClientY(evt);


			final PopupPanel panel = new PopupPanel(true, false);
			panel.setWidget(contextMenu);
			panel.show();

			// Adjust position of right click menu.
			final int width = contextMenu.getOffsetWidth(), height = contextMenu
					.getOffsetHeight();
			int left = currentMouseXPos, top = currentMouseYPos;

			if (currentMouseXPos + width > Window.getClientWidth()) {
				left = Window.getClientWidth() - width - 20;
			}
			if (currentMouseYPos + height > Window.getClientHeight()) {
				top = Window.getClientHeight() - height - 20;
			}

			panel.setPopupPosition(left, top);
		}
	}

	protected ContextMenu getContextMenu() {
		if (menu == null)
			menu = new FieldRootLevelContextMenu();
		return menu;
	}

	class FieldRootLevelContextMenu extends ContextMenu {

		public FieldRootLevelContextMenu() {
			super(true);
			setAutoOpen(true);

			ContextMenuItem item = new ContextMenuItem("Add Field...", new SmartCommand() {
				public void doAction() {
					TableChooser tc = TableChooser.getInstance(getQuery(), true);
					tc.addSaveListener(new TableChooserSaveListener() {
						public void onSave(String selectedTable, ArrayList<String> selectedColumns) {
							for (int i = 0; i < selectedColumns.size(); i++) {
								SelectedField field = new SelectedField(
									selectedTable, selectedColumns.get(i)
								);
								getQuery().addField(field);
							}
							refresh();
						}
					});
					tc.draw();
				}
			});

			addItem(item);
		}

	}

	abstract class ContextMenu extends MenuBar {

		protected abstract class SmartCommand implements Command {
			public abstract void doAction();
			public void execute() {
				doAction();
				closeOnClick();
			}
		}

		public ContextMenu(boolean bool) {
			super(true);
		}

		public void closeOnClick() {
			getParent().removeFromParent();
		}

		public void onBrowserEvent(Event event) {
			MenuItem item = findItem(DOM.eventGetTarget(event));
			switch (DOM.eventGetType(event)) {
				case Event.ONCLICK: {
					//Fire an item's command when the user clicks on it.
					if (item != null && item instanceof ContextMenuItem &&
						((ContextMenuItem)item).isEnabled) {
						super.onBrowserEvent(event);
					}
					break;
				}
				default:
					super.onBrowserEvent(event);
			}
		}

		private MenuItem findItem(Element hItem) {
			for (int i = 0; i < getItems().size(); ++i) {
				MenuItem item = getItems().get(i);
				if (DOM.isOrHasChild(item.getElement(), hItem))
					return item;
			}

			return null;
		}
	}

	static class ContextMenuItem extends MenuItem {
		private boolean isEnabled = true;

		public ContextMenuItem(String text, SmartCommand command) {
			super(text, true, command);
		}

		public void setEnabled(boolean isEnabled) {
			this.isEnabled = isEnabled;
			if (isEnabled) {
				removeStyleName("CIPD_gwtTreeEnabled");
			}
			else {
				addStyleName("CIPD_gwtTreeDisabled");
			}
		}

	}

}
