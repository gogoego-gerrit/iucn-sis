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
import java.util.List;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.HasFocus;
import com.google.gwt.user.client.ui.HasHTML;
import com.google.gwt.user.client.ui.Widget;

/**
 * Shameless copy of com.google.gwt.user.client.ui.TreeItem. GWT's TreeItem does
 * not expose enough to allow a simple subclass. If that changes, this class
 * will be greatly simplified.
 * 
 * Changes: <li>Removed the DOM hierarchy of tree nodes. Each node is
 * independent and therefore placable is a table cell.</li> <li>Changed subclass
 * to Widget so the TreeItem could be added to a table.</li> <li>Changed parent
 * Tree to TreeTable.</li> <li>Worked around package scope methods from the GWT
 * ui package.</li> <li>Removed ContentPanel.</li> <li>Added row index cache.</li>
 * </ul>
 * 
 * @author Matt Boyd (modifications to GWT's classes)
 */
public class TreeItem extends Widget implements HasHTML {

	private ArrayList children = new ArrayList();
	private Element itemTable, contentElem, imgElem;
	private boolean open;
	private TreeItem parentItem;
	private boolean selected;
	private Object userObject;
	private TreeTable table;
	private int row;
	private Widget widget;

	/**
	 * Creates an empty tree item.
	 */
	public TreeItem() {
		setElement(DOM.createDiv());
		itemTable = DOM.createTable();
		contentElem = DOM.createSpan();
		imgElem = DOM.createImg();

		// Uses the following Element hierarchy:
		// <div (handle)>
		// <table (itemElem)>
		// <tr>
		// <td><img (imgElem)/></td>
		// <td><span (contents)/></td>
		// </tr>
		// </table>
		// </div>

		Element tbody = DOM.createTBody(), tr = DOM.createTR();
		Element tdImg = DOM.createTD(), tdContent = DOM.createTD();
		DOM.appendChild(itemTable, tbody);
		DOM.appendChild(tbody, tr);
		DOM.appendChild(tr, tdImg);
		DOM.appendChild(tr, tdContent);
		DOM.setStyleAttribute(tdImg, "verticalAlign", "middle");
		DOM.setStyleAttribute(tdContent, "background-color", "black");
		DOM.setStyleAttribute(tdContent, "verticalAlign", "middle");

		DOM.appendChild(getElement(), itemTable);
		DOM.appendChild(tdImg, imgElem);
		DOM.appendChild(tdContent, contentElem);

		DOM.setAttribute(getElement(), "position", "relative");
		DOM.setStyleAttribute(contentElem, "display", "inline");
		DOM.setStyleAttribute(getElement(), "whiteSpace", "nowrap");
		DOM.setAttribute(itemTable, "whiteSpace", "nowrap");
		setStyleName(contentElem, "gwt-TreeItem", true);
	}

	public TreeItem(Object userObj) {
		this();
		setUserObject(userObj);
	}

	public TreeItem addItem(Object userObj) {
		TreeItem ret = new TreeItem(userObj);
		addItem(ret);
		return ret;
	}

	/**
	 * Adds a child tree item containing the specified text.
	 * 
	 * @param itemText
	 *            the text to be added
	 * @return the item that was added
	 */
	public TreeItem addItem(String itemText) {
		TreeItem ret = new TreeItem(itemText);
		addItem(ret);
		return ret;
	}

	/**
	 * Adds another item as a child to this one.
	 * 
	 * @param item
	 *            the item to be added
	 */
	public void addItem(TreeItem item) {
		// If this element already belongs to a tree or tree item, it should be
		// removed.
		if ((item.getParentItem() != null) || (item.getTreeTable() != null)) {
			item.remove();
		}
		item.setTreeTable(table);
		item.setParentItem(this);
		children.add(item);
		int d = item.getDepth();
		if (d != 0) {
			DOM.setStyleAttribute(item.getElement(), "marginLeft", (d * 16) + "px");
		}
		if (table != null) {
			// table.insertItem(item, getRow() + getChildCount());
			table.insertItem(item, getRow() + getDescendentCount() - 1);
			table.updateRowCache();
			table.updateVisibility(item);
		}

		if (children.size() == 1) {
			updateState();
		}
	}

	/**
	 * Adds a child tree item containing the specified widget.
	 * 
	 * @param widget
	 *            the widget to be added
	 * @return the item that was added
	 */
	public TreeItem addItem(Widget widget) {
		TreeItem ret = new TreeItem(widget);
		addItem(ret);
		return ret;
	}

	void addTreeItems(List accum) {
		for (int i = 0; i < children.size(); i++) {
			TreeItem item = (TreeItem) children.get(i);
			accum.add(item);
			item.addTreeItems(accum);
		}
	}

	/**
	 * Gets the child at the specified index.
	 * 
	 * @param index
	 *            the index to be retrieved
	 * @return the item at that index
	 */

	public TreeItem getChild(int index) {
		if ((index < 0) || (index >= children.size())) {
			return null;
		}

		return (TreeItem) children.get(index);
	}

	/**
	 * Gets the number of children contained in this item.
	 * 
	 * @return this item's child count.
	 */

	public int getChildCount() {
		return children.size();
	}

	/**
	 * Gets the index of the specified child item.
	 * 
	 * @param child
	 *            the child item to be found
	 * @return the child's index, or <code>-1</code> if none is found
	 */

	public int getChildIndex(TreeItem child) {
		return children.indexOf(child);
	}

	ArrayList getChildren() {
		return children;
	}

	Element getContentElem() {
		return contentElem;
	}

	int getContentHeight() {
		return DOM.getIntAttribute(itemTable, "offsetHeight");
	}

	/**
	 * Returns the depth of this item. Depth of root child is 0.
	 * 
	 * @return
	 */
	public int getDepth() {
		if (parentItem == null) {
			return 0;
		}
		return parentItem.getDepth() + 1;
	}

	/**
	 * Returns the count of all descendents; includes this item in the count.
	 * 
	 * @return
	 */
	int getDescendentCount() {
		int d = 1;
		for (int i = getChildCount() - 1; i >= 0; i--) {
			d += getChild(i).getDescendentCount();
		}
		return d;
	}

	/**
	 * Returns the widget, if any, that should be focused on if this TreeItem is
	 * selected.
	 * 
	 * @return widget to be focused.
	 */
	protected HasFocus getFocusableWidget() {
		Widget widget = getWidget();
		if (widget instanceof HasFocus) {
			return (HasFocus) widget;
		} else {
			return null;
		}
	}

	public String getHTML() {
		return DOM.getInnerHTML(contentElem);
	}

	Element getImageElement() {
		return imgElem;
	}

	/**
	 * Gets this item's parent.
	 * 
	 * @return the parent item
	 */
	public TreeItem getParentItem() {
		return parentItem;
	}

	public int getRow() {
		return row;
	}

	/**
	 * Gets whether this item's children are displayed.
	 * 
	 * @return <code>true</code> if the item is open
	 */
	public boolean getState() {
		return open;
	}

	public String getText() {
		return DOM.getInnerText(contentElem);
	}

	/**
	 * Gets the tree that contains this item.
	 * 
	 * @return the containing tree
	 */
	public TreeTable getTreeTable() {
		return table;
	}

	int getTreeTop() {
		TreeItem item = this;
		int ret = 0;

		while (item != null) {
			ret += DOM.getIntAttribute(item.getElement(), "offsetTop");
			item = item.getParentItem();
		}

		return ret;
	}

	/**
	 * Gets the user-defined object associated with this item.
	 * 
	 * @return the item's user-defined object
	 */
	public Object getUserObject() {
		return userObject;
	}

	/**
	 * Gets the <code>Widget</code> associated with this tree item.
	 * 
	 * @return the widget
	 */
	public Widget getWidget() {
		return widget;
	}

	String imgSrc(String img) {
		if (table == null) {
			return img;
		}
		return table.getImageBase() + img;
	}

	public boolean isOpen() {
		return getState();
	}

	/**
	 * Determines whether this item is currently selected.
	 * 
	 * @return <code>true</code> if it is selected
	 */
	public boolean isSelected() {
		return selected;
	}

	/**
	 * Removes this item from its tree.
	 */
	public void remove() {
		if (parentItem != null) {
			// If this item has a parent, remove self from it.
			parentItem.removeItem(this);
		} else if (table != null) {
			// If the item has no parent, but is in the Tree, it must be a
			// top-level
			// element.
			table.removeItem(this);
		}
	}

	/**
	 * Removes one of this item's children.
	 * 
	 * @param item
	 *            the item to be removed
	 */

	public void removeItem(TreeItem item) {
		if (!children.contains(item)) {
			return;
		}
		// Update Item state.
		item.setTreeTable(null);
		item.setParentItem(null);

		children.remove(item);
		if (table != null) {
			table.removeItemFromTable(item);
		}

		if (children.size() == 0) {
			updateState();
		}
	}

	/**
	 * Removes all of this item's children.
	 */
	public void removeItems() {
		while (getChildCount() > 0) {
			removeItem(getChild(0));
		}
	}

	public void setHTML(String html) {
		DOM.setInnerHTML(contentElem, html);
		// if (widget != null) {
		// DOM.removeChild(contentElem, widget.getElement());
		// widget = null;
		// }
	}

	void setParentItem(TreeItem parent) {
		this.parentItem = parent;
	}

	void setRow(int r) {
		row = r;
	}

	/**
	 * Selects or deselects this item.
	 * 
	 * @param selected
	 *            <code>true</code> to select the item, <code>false</code> to
	 *            deselect it
	 */
	public void setSelected(boolean selected) {
		if (this.selected == selected) {
			return;
		}
		this.selected = selected;
		setStyleName(contentElem, "gwt-TreeItem-selected", selected);
	}

	/**
	 * Sets whether this item's children are displayed.
	 * 
	 * @param open
	 *            whether the item is open
	 */
	public void setState(boolean open) {
		setState(open, true);
	}

	/**
	 * Sets whether this item's children are displayed.
	 * 
	 * @param open
	 *            whether the item is open
	 * @param fireEvents
	 *            <code>true</code> to allow open/close events to be fired
	 */
	public void setState(boolean open, boolean fireEvents) {
		if (open && children.size() == 0) {
			return;
		}

		this.open = open;
		if (open) {
			table.showChildren(this);
		} else {
			table.hideChildren(this);
		}
		updateState();

		if (fireEvents) {
			table.fireStateChanged(this);
		}
	}

	public void setText(String text) {
		DOM.setInnerText(contentElem, text);
		// if (widget != null) {
		// DOM.removeChild(contentElem, widget.getElement());
		// widget = null;
		// }
	}

	void setTreeTable(TreeTable table) {
		if (this.table == table) {
			return;
		}

		if (this.table != null) {
			if (this.table.getSelectedItem() == this) {
				this.table.setSelectedItem(null);
			}
		}
		this.table = table;
		for (int i = 0, n = children.size(); i < n; ++i) {
			((TreeItem) children.get(i)).setTreeTable(table);
		}
		updateState();
	}

	/**
	 * Sets the user-defined object associated with this item.
	 * 
	 * @param userObj
	 *            the item's user-defined object
	 */
	public void setUserObject(Object userObj) {
		userObject = userObj;
	}

	/**
	 * Sets the current widget. Any existing child widget will be removed.
	 * 
	 * @param widget
	 *            Widget to set
	 */
	public void setWidget(Widget w) {
		if (widget != null) {
			DOM.removeChild(contentElem, widget.getElement());
			// widget.setParent(null);
		}

		if (w != null) {
			widget = w;
			DOM.setInnerText(contentElem, null);
			DOM.appendChild(contentElem, w.getElement());
			// widget.setParent(this);
		}
	}

	void updateState() {
		if (children.size() == 0) {
			// UIObject.setVisible(childSpanElem, false);
			DOM.setAttribute(imgElem, "src", imgSrc("tree_white.gif"));
			return;
		}

		// We must use 'display' rather than 'visibility' here,
		// or the children will always take up space.
		if (open) {
			// UIObject.setVisible(childSpanElem, true);
			DOM.setAttribute(imgElem, "src", imgSrc("tree_open.gif"));
		} else {
			// UIObject.setVisible(childSpanElem, false);
			DOM.setAttribute(imgElem, "src", imgSrc("tree_closed.gif"));
		}

		// if (getParentItem() != null) {
		// table.updateVisibility(getParentItem());
		// }
	}

	void updateStateRecursive() {
		updateState();
		for (int i = 0, n = children.size(); i < n; ++i) {
			((TreeItem) children.get(i)).updateStateRecursive();
		}
	}
}
