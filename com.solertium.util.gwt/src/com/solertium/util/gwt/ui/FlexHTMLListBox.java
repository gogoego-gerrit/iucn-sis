/*
 * Copyright (C) 2007-2009 Solertium Corporation
 *
 * This file is part of the open source GoGoEgo project.
 *
 * Unless you have been granted a different license in writing by the
 * copyright holders for GoGoEgo, you may only modify or redistribute
 * this code under the terms of one of the following licenses:
 * 
 * 1) The Eclipse Public License, v.1.0
 *    http://www.eclipse.org/legal/epl-v10.html
 *
 * 2) The GNU General Public License, version 2 or later
 *    http://www.gnu.org/licenses
 */
package com.solertium.util.gwt.ui;

import java.util.ArrayList;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SourcesTableEvents;
import com.google.gwt.user.client.ui.TableListener;

/**
 * FlexHTMLListBox.java
 * 
 * Works similarly to the GWT ListBox but users HTML widgets to expose some
 * unique and powerful functionality.
 * 
 * HTMLListBox supports tooltips as well..
 * 
 * @author carl.scott
 * 
 */
public abstract class FlexHTMLListBox extends ScrollPanel {

	protected ArrayList<String> items;
	protected FlexTable table;

	protected int selectedIndex;
	protected boolean isEnabled = true;

	protected String width;

	/**
	 * Creates a new Listbox with the designated width. If any widgets within
	 * the listbox violate this width, the box will scroll horizontally.
	 * 
	 * @param width
	 */
	public FlexHTMLListBox(final String width) {
		super();
		items = new ArrayList<String>();
		setWidth(this.width = width);
		show();
	}

	/**
	 * Adds a new item, using its display name as the key
	 * 
	 * @param name
	 */
	public void addItem(final String name) {
		addItem(name, name);
	}

	/**
	 * Adds a new item with a display name and selection value
	 * 
	 * @param name
	 * @param value
	 */
	public void addItem(final String name, final String value) {
		addItem(name, value, null);
	}

	/**
	 * Adds an item with a display name, selection value, and, optionally, a
	 * tooltip. Will be ignored if null.
	 * 
	 * @param name
	 * @param value
	 * @param toolTip
	 */
	public void addItem(final String name, final String value,
			final String toolTip) {
		HTML html;
		if (toolTip == null) {
			html = new HTML(name);
		} else {
			html = new HTMLWithToolTip(name, toolTip);
		}

		html.setWordWrap(false);
		html.setStyleName("CIPD_ListBox_Item");

		table.setWidget(items.size(), 0, html);
		items.add(value);

		table.getColumnFormatter().setWidth(0, width);
	}

	/**
	 * Clears the list.
	 */
	public void clear() {
		items.clear();
		show();
		selectedIndex = -1;
	}

	public int getItemCount() {
		return items.size();
	}

	public int getSelectedIndex() {
		return selectedIndex;
	}

	public String getSelectedText() {
		return getText(selectedIndex);
	}

	public String getSelectedValue() {
		return (String) items.get(selectedIndex);
	}

	/**
	 * Pulls the table from the scrollpanel
	 * 
	 * @return
	 */
	public FlexTable getTableView() {
		return table;
	}

	public String getText(final int index) {
		return ((HTML) table.getWidget(index, 0)).getText();
	}

	public String getValue(final int index) {
		return (String) items.get(index);
	}

	public int indexOf(final String value) {
		return items.indexOf(value);
	}

	public boolean isEmpty() {
		return items.isEmpty();
	}

	/**
	 * Method that occurs when an item is clicked.
	 * 
	 * @param selectedValue
	 */
	public abstract void onChange(String selectedValue);

	/**
	 * Removes the item at the given index.
	 * 
	 * @param index
	 */
	public void removeItem(final int index) {
		items.remove(index);
		table.removeRow(index);
	}

	public void setEnabled(final boolean isEnabled) {
		this.isEnabled = isEnabled;
	}

	public void setSelectedIndex(final int index) {
		selectedIndex = index;
		for (int i = 0; i < items.size(); i++) {
			((HTML) table.getWidget(i, 0))
					.setStyleName(i == index ? "CIPD_ListBox_ItemSelected"
							: "CIPD_ListBox_Item");
		}
	}

	public void setText(final String text, final int index) {
		((HTML) table.getWidget(index, 0)).setHTML(text);
	}

	public void setValue(final String value, final int index){
		items.set(index, value);
	}
	/**
	 * Sets the table. This is used to prevent the UI from becoming stale when
	 * this widget is clear()ed
	 * 
	 */
	private void show() {
		table = new FlexTable();
		table.addTableListener(new TableListener() {
			public void onCellClicked(final SourcesTableEvents sender,
					final int row, final int cell) {
				if (isEnabled) {
					setSelectedIndex(row);
					onChange((String) items.get(row));
				}
			}
		});
		setWidget(table);
	}

	/**
	 * TODO: apparently does not work 100% correctly yet
	 * @param index
	 */
	public void showItemInView(final int index) {
		setScrollPosition(index * 10);
	}

}
