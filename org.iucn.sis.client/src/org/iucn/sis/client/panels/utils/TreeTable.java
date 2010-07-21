/*
 * Copyright 2006 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.iucn.sis.client.panels.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.KeyboardListenerCollection;
import com.google.gwt.user.client.ui.MouseListener;
import com.google.gwt.user.client.ui.MouseListenerCollection;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;

/**
 * Shameless copy of com.google.gwt.user.client.ui.Tree. Extension of FlexTable
 * adding a tree in one column. Uses a TreeItem model and row based rendering of
 * table cells.
 * 
 * Changes:
 * <ul>
 * <li>Removed focus functionality from Tree code. It was causing problems with
 * IE. Not sure how applicable it is with FlexTable as the base class. It may
 * have problems playing well with GWT because of package scope work arounds.
 * Seems to work ok without the code, minus drawing an outline.</li>
 * <li>Made TreeItem a Widget to be added to a table cell. Removed ContentPanel
 * handling from the Tree code.
 * <li>Disabled some Widget add/remove code. This may cause strange bugs. Again,
 * package scope issues. This needs a work around.</li>
 * <li>Streamlined findItemByChain() and modified elementClicked() to search the
 * table. This should probably be rewritten to leverage FlexTable.
 * </ul>
 * 
 * Notes:
 * <ul>
 * <li>If anyone has a firm understanding of "focus" in GWT I could use the help
 * cleaning this up.</li>
 * </ul>
 * 
 * @author Matt Boyd (modifications to GWT's classes)
 */
public class TreeTable extends FlexTable {

	/**
	 * Default renderer for TreeTable. Renders the user object into the
	 * TreeItem. Widget user objects are preserved. Arrays are mapped into the
	 * row with first object rendered into the TreeItem. All other objects are
	 * rendered to the TreeItem with toString().
	 */
	class DefaultRenderer implements TreeTableRenderer {
		public void renderTreeItem(TreeTable table, TreeItem item, int row) {
			Object obj = item.getUserObject();
			if (obj instanceof Widget) {
				item.setWidget((Widget) obj);
			} else if (obj instanceof Object[]) {
				Object[] objs = (Object[]) obj;
				if (objs.length > 0) {
					Object o = objs[0];
					if (o instanceof Widget) {
						item.setWidget((Widget) o);
					} else if (o != null) {
						item.setHTML(o.toString());
					} else {
						item.setText(null);
					}
					for (int i = 1, s = objs.length; i < s; i++) {
						o = objs[i];
						if (o instanceof Widget) {
							setWidget(row, i, (Widget) o);
						} else if (o != null) {
							setHTML(row, i, o.toString());
						} else {
							setHTML(row, i, null);
						}
					}
				}
			} else if (obj != null) {
				item.setHTML(obj.toString());
			}
		}
	}

	/**
	 * Needed local instance. GWT's is hidden in package scope.
	 */
	// private FocusImpl impl = (FocusImpl) GWT.create(FocusImpl.class);
	public static class Renderer {
		public void renderRow(TreeTable tree, TreeItem item, int row) {

		}
	}

	// private final Element focusable;

	// private FocusListenerCollection focusListeners;

	private Element headElem;

	private TreeItem curSelection;

	private String imageBase = GWT.getModuleBaseURL();

	private KeyboardListenerCollection keyboardListeners;

	private TreeTableListenerCollection listeners;

	private MouseListenerCollection mouseListeners = null;

	private final TreeItem root;

	private TreeTableRenderer renderer;

	/**
	 * Keeps track of the last event type seen. We do this to determine if we
	 * have a duplicate key down.
	 */
	private int lastEventType;

	/**
	 * Constructs an empty tree.
	 */
	public TreeTable() {
		Element tableElem = getElement();
		headElem = DOM.createElement("thead");
		Element tr = DOM.createTR();
		DOM.appendChild(headElem, tr);
		DOM.insertChild(tableElem, headElem, 0);

		DOM.setStyleAttribute(getElement(), "position", "relative");
		// focusable = impl.createFocusable();
		// DOM.setStyleAttribute(focusable, "fontSize", "0");
		// DOM.setStyleAttribute(focusable, "position", "absolute");
		// DOM.setIntStyleAttribute(focusable, "zIndex", -1);
		// DOM.appendChild(getElement(), focusable);

		sinkEvents(Event.MOUSEEVENTS | Event.ONCLICK | Event.KEYEVENTS);
		// DOM.sinkEvents(focusable, Event.FOCUSEVENTS | Event.KEYEVENTS |
		// DOM.getEventsSunk(focusable));

		// The 'root' item is invisible and serves only as a container
		// for all top-level items.
		root = new TreeItem() {
			@Override
			public void addItem(TreeItem item) {
				// If this element already belongs to a tree or tree item,
				// remove it.
				if ((item.getParentItem() != null) || (item.getTreeTable() != null)) {
					item.remove();
				}
				item.setTreeTable(this.getTreeTable());

				// Explicitly set top-level items' parents to null.
				item.setParentItem(null);
				getChildren().add(item);

				// Use no margin on top-most items.
				DOM.setIntStyleAttribute(item.getElement(), "marginLeft", 0);
			}

			@Override
			public void removeItem(TreeItem item) {
				if (!getChildren().contains(item)) {
					return;
				}
				// Update Item state.
				item.setTreeTable(null);
				item.setParentItem(null);
				getChildren().remove(item);
			}
		};
		root.setTreeTable(this);
		setStyleName("gwt-TreeTable");
	}

	/**
	 * Adds the widget as a root tree item.
	 * 
	 * @see com.google.gwt.user.client.ui.HasWidgets#add(com.google.gwt.user.client.ui.Widget)
	 * @param widget
	 *            widget to add.
	 */
	@Override
	public void add(Widget widget) {
		addItem(widget);
	}

	public TreeItem addItem(Object userObj) {
		TreeItem ret = new TreeItem(userObj);
		addItem(ret);
		return ret;
	}

	/**
	 * Adds a simple tree item containing the specified text.
	 * 
	 * @param itemText
	 *            the text of the item to be added
	 * @return the item that was added
	 */
	public TreeItem addItem(String itemText) {
		TreeItem ret = new TreeItem(itemText);
		addItem(ret);

		return ret;
	}

	/**
	 * Adds an item to the root level of this tree.
	 * 
	 * @param item
	 *            the item to be added
	 */
	public void addItem(TreeItem item) {
		root.addItem(item);

		// Adds the item to the proper row
		insertItem(item, getRowCount());
		updateRowCache();
		updateVisibility(item);
	}

	/**
	 * Adds a new tree item containing the specified widget.
	 * 
	 * @param widget
	 *            the widget to be added
	 */
	public TreeItem addItem(Widget widget) {
		TreeItem item = new TreeItem(widget);
		addItem(item);
		return item;
	}

	public void addKeyboardListener(KeyboardListener listener) {
		if (keyboardListeners == null) {
			keyboardListeners = new KeyboardListenerCollection();
		}
		keyboardListeners.add(listener);
	}

	public void addMouseListener(MouseListener listener) {
		if (mouseListeners == null) {
			mouseListeners = new MouseListenerCollection();
		}
		mouseListeners.add(listener);
	}

	public void addTreeTableListener(TreeTableListener listener) {
		if (listeners == null) {
			listeners = new TreeTableListenerCollection();
		}
		listeners.add(listener);
	}

	/**
	 * Clears all tree items from the current tree.
	 */
	@Override
	public void clear() {
		int size = root.getChildCount();
		for (int i = size - 1; i >= 0; i--) {
			root.getChild(i).remove();
		}
	}

	/**
	 * Collects parents going up the element tree, terminated at the tree root.
	 */
	private void collectElementChain(Vector chain, Element hRoot, Element hElem) {
		if ((hElem == null) || DOM.compare(hElem, hRoot)) {
			return;
		}

		collectElementChain(chain, hRoot, DOM.getParent(hElem));
		chain.add(hElem);
	}

	private boolean elementClicked(TreeItem root, Element hElem) {
		Vector chain = new Vector();
		collectElementChain(chain, getElement(), hElem);

		TreeItem item = findItemByChain(chain, 0, root);
		if (item != null) {
			if (DOM.compare(item.getImageElement(), hElem)) {
				item.setState(!item.getState(), true);
				return true;
			} else if (DOM.isOrHasChild(item.getElement(), hElem)) {
				onSelection(item, true);
				return true;
			}
		}

		return false;
	}

	/**
	 * Ensures that the currently-selected item is visible, opening its parents
	 * and scrolling the tree as necessary.
	 */
	public void ensureSelectedItemVisible() {
		if (curSelection == null) {
			return;
		}

		TreeItem parent = curSelection.getParentItem();
		while (parent != null) {
			parent.setState(true);
			parent = parent.getParentItem();
		}
	}

	private TreeItem findDeepestOpenChild(TreeItem item) {
		if (!item.getState()) {
			return item;
		}
		return findDeepestOpenChild(item.getChild(item.getChildCount() - 1));
	}

	private TreeItem findItemByChain(Vector chain, int idx, TreeItem root) {
		if (idx == chain.size()) {
			return root;
		}

		for (int i = 0, s = chain.size(); i < s; i++) {
			Element elem = (Element) chain.get(i);
			String n = getNodeName(elem);
			if ("div".equalsIgnoreCase(n)) {
				return findItemByElement(root, elem);
			}
		}

		return null;
	}

	private TreeItem findItemByElement(TreeItem item, Element elem) {
		if (DOM.compare(item.getElement(), elem)) {
			return item;
		}
		for (int i = 0, n = item.getChildCount(); i < n; ++i) {
			TreeItem child = item.getChild(i);
			child = findItemByElement(child, elem);
			if (child != null) {
				return child;
			}
		}
		return null;
	}

	void fireStateChanged(TreeItem item) {
		if (listeners != null) {
			listeners.fireItemStateChanged(item);
		}
	}

	/**
	 * Gets this tree's default image package.
	 * 
	 * @return the tree's image package
	 * @see #setImageBase
	 */
	public String getImageBase() {
		return imageBase;
	}

	/**
	 * Gets the top-level tree item at the specified index.
	 * 
	 * @param index
	 *            the index to be retrieved
	 * @return the item at that index
	 */
	public TreeItem getItem(int index) {
		return root.getChild(index);
	}

	/**
	 * Gets the number of items contained at the root of this tree.
	 * 
	 * @return this tree's item count
	 */
	public int getItemCount() {
		return root.getChildCount();
	}

	public int getLastChildRow(TreeItem item) {
		// Checks the row of the next sibling
		TreeItem next = getNextNonChild(item);
		if (next != null) {
			return next.getRow() - 1;
		}

		return getRowCount() - 1;
	}

	protected TreeItem getNextNonChild(TreeItem item) {
		TreeItem next = getNextSibling(item);
		if (next != null) {
			return next;
		}
		TreeItem p = item.getParentItem();
		if (p != null) {
			return getNextNonChild(p);
		} else {
			return null;
		}
	}

	protected TreeItem getNextSibling(TreeItem item) {
		TreeItem p = item.getParentItem();
		if (p == null) {
			int idx = root.getChildIndex(item) + 1;
			if (idx < root.getChildCount()) {
				// Gets the next sibling
				return root.getChild(idx);
			}
		} else {
			int idx = p.getChildIndex(item) + 1;
			if (idx < p.getChildCount()) {
				// Gets the next sibling
				return p.getChild(idx);
			}
		}
		return null;
	}

	private native String getNodeName(Element elem) /*-{
		return elem.nodeName;
	}-*/;

	public TreeTableRenderer getRenderer() {
		if (renderer == null) {
			renderer = new DefaultRenderer();
		}
		return renderer;
	}

	/**
	 * Gets the currently selected item.
	 * 
	 * @return the selected item
	 */
	public TreeItem getSelectedItem() {
		return curSelection;
	}

	protected int getTreeColumn() {
		return 0;
	}

	public void hideChildren(TreeItem item) {
		setChildrenVisible(item, false);
	}

	/**
	 * Updates table rows to include children.
	 * 
	 * @param item
	 */
	void insertItem(TreeItem item, int r) {
		// inserts this item into the tree
		insertRow(r);
		setWidget(r, getTreeColumn(), item);
		item.setRow(r);
		render(item);

		ArrayList chlds = item.getChildren();
		for (int i = 0, s = chlds.size(); i < s; i++) {
			TreeItem chld = (TreeItem) chlds.get(i);
			insertItem(chld, r + 1);
		}

		TreeItem p = item.getParentItem();
		if (p != null) {
			if (!p.isOpen()) {
				setVisible(false, item.getRow());
				setChildrenVisible(item, false);
			}
		}
	}

	/**
	 * Moves to the next item, going into children as if dig is enabled.
	 */
	private void moveSelectionDown(TreeItem sel, boolean dig) {
		if (sel == root) {
			return;
		}

		TreeItem parent = sel.getParentItem();
		if (parent == null) {
			parent = root;
		}
		int idx = parent.getChildIndex(sel);

		if (!dig || !sel.getState()) {
			if (idx < parent.getChildCount() - 1) {
				onSelection(parent.getChild(idx + 1), true);
			} else {
				moveSelectionDown(parent, false);
			}
		} else if (sel.getChildCount() > 0) {
			onSelection(sel.getChild(0), true);
		}
	}

	/**
	 * Moves the selected item up one.
	 */
	private void moveSelectionUp(TreeItem sel) {
		TreeItem parent = sel.getParentItem();
		if (parent == null) {
			parent = root;
		}
		int idx = parent.getChildIndex(sel);

		if (idx > 0) {
			TreeItem sibling = parent.getChild(idx - 1);
			onSelection(findDeepestOpenChild(sibling), true);
		} else {
			onSelection(parent, true);
		}
	}

	@Override
	public void onBrowserEvent(Event event) {
		int eventType = DOM.eventGetType(event);
		switch (eventType) {
		case Event.ONCLICK: {
			Element e = DOM.eventGetTarget(event);
			if (shouldTreeDelegateFocusToElement(e)) {
				// The click event should have given focus to this element
				// already.
				// Avoid moving focus back up to the tree (so that focusable
				// widgets
				// attached to TreeItems can receive keyboard events).
			} else {
				// setFocus(true);
			}
			break;
		}
		case Event.ONMOUSEDOWN: {
			if (mouseListeners != null) {
				mouseListeners.fireMouseEvent(this, event);
			}
			elementClicked(root, DOM.eventGetTarget(event));
			break;
		}

		case Event.ONMOUSEUP: {
			if (mouseListeners != null) {
				mouseListeners.fireMouseEvent(this, event);
			}
			break;
		}

		case Event.ONMOUSEMOVE: {
			if (mouseListeners != null) {
				mouseListeners.fireMouseEvent(this, event);
			}
			break;
		}

		case Event.ONMOUSEOVER: {
			if (mouseListeners != null) {
				mouseListeners.fireMouseEvent(this, event);
			}
			break;
		}

		case Event.ONMOUSEOUT: {
			if (mouseListeners != null) {
				mouseListeners.fireMouseEvent(this, event);
			}
			break;
		}

			// case Event.ONFOCUS:
			// // If we already have focus, ignore the focus event.
			// if (focusListeners != null) {
			// focusListeners.fireFocusEvent(this, event);
			// }
			// break;
			//
			// case Event.ONBLUR: {
			// if (focusListeners != null) {
			// focusListeners.fireFocusEvent(this, event);
			// }
			//
			// break;
			// }

		case Event.ONKEYDOWN:
			// If nothing's selected, select the first item.
			if (curSelection == null) {
				if (root.getChildCount() > 0) {
					onSelection(root.getChild(0), true);
				}
				super.onBrowserEvent(event);
				return;
			}

			if (lastEventType == Event.ONKEYDOWN) {
				// Two key downs in a row signal a duplicate event. Multiple key
				// downs can be triggered in the current configuration
				// independent
				// of the browser.
				return;
			}

			// Handle keyboard events
			switch (DOM.eventGetKeyCode(event)) {
			case KeyboardListener.KEY_UP: {
				moveSelectionUp(curSelection);
				DOM.eventPreventDefault(event);
				break;
			}
			case KeyboardListener.KEY_DOWN: {
				moveSelectionDown(curSelection, true);
				DOM.eventPreventDefault(event);
				break;
			}
			case KeyboardListener.KEY_LEFT: {
				if (curSelection.getState()) {
					curSelection.setState(false);
				}
				DOM.eventPreventDefault(event);
				break;
			}
			case KeyboardListener.KEY_RIGHT: {
				if (!curSelection.getState()) {
					curSelection.setState(true);
				}
				DOM.eventPreventDefault(event);
				break;
			}
			default:
				break;

			}

			// Intentional fallthrough.
		case Event.ONKEYUP:
			if (eventType == Event.ONKEYUP) {
				// If we got here because of a key tab, then we need to make
				// sure the
				// current tree item is selected.
				if (DOM.eventGetKeyCode(event) == KeyboardListener.KEY_TAB) {
					Vector chain = new Vector();
					collectElementChain(chain, getElement(), DOM.eventGetTarget(event));
					TreeItem item = findItemByChain(chain, 0, root);
					if (item != getSelectedItem()) {
						setSelectedItem(item, true);
					}
				}
			}

			// Intentional fallthrough.
		case Event.ONKEYPRESS: {
			if (keyboardListeners != null) {
				keyboardListeners.fireKeyboardEvent(this, event);
			}
			break;
		}
		}

		// We must call SynthesizedWidget's implementation for all other events.
		super.onBrowserEvent(event);
		lastEventType = eventType;
	}

	@Override
	protected void onLoad() {
		root.updateStateRecursive();

		renderTable();
		updateVisibility();
	}

	private void onSelection(TreeItem item, boolean fireEvents) {

		// 'root' isn't a real item, so don't let it be selected
		// (some cases in the keyboard handler will try to do this)
		if (item == root) {
			return;
		}

		if (curSelection != null) {
			curSelection.setSelected(false);
		}

		curSelection = item;

		if (curSelection != null) {
			// moveFocus(curSelection);

			// SISSelect the item and fire the selection event.
			curSelection.setSelected(true);
			if (fireEvents && (listeners != null)) {
				listeners.fireItemSelected(curSelection);
			}
		}
	}

	/**
	 * Removes an item from the root level of this tree.
	 * 
	 * @param item
	 *            the item to be removed
	 */
	public void removeItem(TreeItem item) {
		root.removeItem(item);
		removeItemFromTable(item);
	}

	void removeItemFromTable(TreeItem item) {
		int r = item.getRow();
		int rs = item.getDescendentCount();
		for (int i = 0; i < rs; i++) {
			removeRow(r);
		}
		updateRowCache();
	}

	/**
	 * Removes all items from the root level of this tree.
	 */
	public void removeItems() {
		while (getItemCount() > 0) {
			removeItem(getItem(0));
		}
	}

	public void removeKeyboardListener(KeyboardListener listener) {
		if (keyboardListeners != null) {
			keyboardListeners.remove(listener);
		}
	}

	public void removeTreeTableListener(TreeTableListener listener) {
		if (listeners != null) {
			listeners.remove(listener);
		}
	}

	/**
	 * Renders TreeItems recursively.
	 * 
	 * @param item
	 */
	public void render(TreeItem item) {
		getRenderer().renderTreeItem(this, item, item.getRow());
		if (item.getParentItem() != null) {
			updateVisibility(item.getParentItem());
		}

		for (int i = 0, s = item.getChildCount(); i < s; i++) {
			TreeItem child = item.getChild(i);
			render(child);
		}
	}

	public void renderTable() {
		render(root);
	}

	public void setChildrenVisible(TreeItem item, boolean visible) {
		if (item.getChildCount() == 0) {
			return;
		}
		int row = item.getRow() + 1;
		int lastChildRow = getLastChildRow(item);
		int count = lastChildRow - row + 1;
		setVisible(visible, row, count);
	}

	@Override
	public void setHTML(int row, int column, String text) {
		if (column != getTreeColumn()) {
			super.setHTML(row, column, text);
		} else {
			throw new RuntimeException("Cannot add non-TreeItem to tree column");
		}
	}

	/**
	 * Sets the base URL under which this tree will find its default images.
	 * These images must be named "tree_white.gif", "tree_open.gif", and
	 * "tree_closed.gif".
	 */
	public void setImageBase(String baseUrl) {
		imageBase = baseUrl;
		root.updateStateRecursive();
	}

	public void setRenderer(TreeTableRenderer renderer) {
		this.renderer = renderer;
	}

	/**
	 * Selects a specified item.
	 * 
	 * @param item
	 *            the item to be selected, or <code>null</code> to deselect all
	 *            items
	 */
	public void setSelectedItem(TreeItem item) {
		setSelectedItem(item, true);
	}

	/**
	 * Selects a specified item.
	 * 
	 * @param item
	 *            the item to be selected, or <code>null</code> to deselect all
	 *            items
	 * @param fireEvents
	 *            <code>true</code> to allow selection events to be fired
	 */
	public void setSelectedItem(TreeItem item, boolean fireEvents) {
		if (item == null) {
			if (curSelection == null) {
				return;
			}
			curSelection.setSelected(false);
			curSelection = null;
			return;
		}

		onSelection(item, fireEvents);
	}

	@Override
	public void setText(int row, int column, String text) {
		if (column != getTreeColumn()) {
			super.setText(row, column, text);
		} else {
			throw new RuntimeException("Cannot add non-TreeItem to tree column");
		}
	}

	void setVisible(boolean visible, int row) {
		UIObject.setVisible(getRowFormatter().getElement(row), visible);
	}

	protected void setVisible(boolean visible, int row, int count) {
		for (int r = row, s = row + count; r < s; r++) {
			setVisible(visible, r);
		}
	}

	@Override
	public void setWidget(int row, int column, Widget widget) {
		if (column != getTreeColumn()) {
			super.setWidget(row, column, widget);
		} else {
			if (widget instanceof TreeItem) {
				super.setWidget(row, column, widget);
			} else {
				throw new RuntimeException("Cannot add non-TreeItem to tree column");
			}
		}
	}

	private native boolean shouldTreeDelegateFocusToElement(Element elem) /*-{
		var focus = 
			((elem.nodeName == "SELECT") || 
			(elem.nodeName == "INPUT")  || 
			(elem.nodeName == "CHECKBOX")
		);
		return focus;
	}-*/;

	public void showChildren(TreeItem item) {
		for (int i = 0, s = item.getChildCount(); i < s; i++) {
			TreeItem child = item.getChild(i);
			setVisible(true, child.getRow());

			if (child.isOpen()) {
				showChildren(child);
			}
		}
	}

	/**
	 * Iterator of tree items.
	 */
	public Iterator treeItemIterator() {
		List accum = new ArrayList();
		root.addTreeItems(accum);
		return accum.iterator();
	}

	// /**
	// * Move the tree focus to the specified selected item.
	// *
	// * @param selection
	// */
	// private void moveFocus(TreeItem selection) {
	// HasFocus focusableWidget = selection.getFocusableWidget();
	// if (focusableWidget != null) {
	// focusableWidget.setFocus(true);
	// DOM.scrollIntoView(((Widget) focusableWidget).getElement());
	// } else {
	// // Get the location and size of the given item's content element
	// // relative
	// // to the tree.
	// Element selectedElem = selection.getContentElem();
	// int containerLeft = getAbsoluteLeft();
	// int containerTop = getAbsoluteTop();
	//
	// int left = DOM.getAbsoluteLeft(selectedElem) - containerLeft;
	// int top = DOM.getAbsoluteTop(selectedElem) - containerTop;
	// int width = DOM.getIntAttribute(selectedElem, "offsetWidth");
	// int height = DOM.getIntAttribute(selectedElem, "offsetHeight");
	//
	// // Set the focusable element's position and size to exactly underlap
	// // the
	// // item's content element.
	// DOM.setIntStyleAttribute(focusable, "left", left);
	// DOM.setIntStyleAttribute(focusable, "top", top);
	// DOM.setIntStyleAttribute(focusable, "width", width);
	// DOM.setIntStyleAttribute(focusable, "height", height);
	//
	// // Scroll it into view.
	// DOM.scrollIntoView(focusable);
	//
	// // Ensure Focus is set, as focus may have been previously delegated
	// // by
	// // tree.
	// impl.focus(focusable);
	// }
	// }

	// public int getTabIndex() {
	// return impl.getTabIndex(focusable);
	// }

	// public void addFocusListener(FocusListener listener) {
	// if (focusListeners == null) {
	// focusListeners = new FocusListenerCollection();
	// }
	// focusListeners.add(listener);
	// }

	// public void removeFocusListener(FocusListener listener) {
	// if (focusListeners != null) {
	// focusListeners.remove(listener);
	// }
	// }

	// public void setAccessKey(char key) {
	// impl.setAccessKey(focusable, key);
	// }

	// public void setFocus(boolean focus) {
	// if (focus) {
	// impl.focus(focusable);
	// } else {
	// impl.blur(focusable);
	// }
	// }

	// public void setTabIndex(int index) {
	// impl.setTabIndex(focusable, index);
	// }

	/**
	 * Updates the cached row index for each tree item. TODO - Optomize with
	 * start item.
	 */
	void updateRowCache() {
		updateRowCache(root, -1);
	}

	int updateRowCache(TreeItem item, int r) {
		item.setRow(r);

		ArrayList chlds = item.getChildren();
		for (int i = 0, s = chlds.size(); i < s; i++) {
			TreeItem chld = (TreeItem) chlds.get(i);
			r++;
			r = updateRowCache(chld, r);
		}

		return r;
	}

	public void updateVisibility() {
		for (int i = 0, s = root.getChildCount(); i < s; i++) {
			TreeItem item = root.getChild(i);
			updateVisibility(item);
		}
	}

	protected void updateVisibility(TreeItem item) {
		if (item.isOpen()) {
			showChildren(item);
		} else {
			hideChildren(item);
		}
	}
}
