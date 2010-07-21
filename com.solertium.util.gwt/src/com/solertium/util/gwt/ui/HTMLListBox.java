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

import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SourcesTableEvents;
import com.google.gwt.user.client.ui.TableListener;

/**
 * HTMLListBox.java
 * 
 * Works similarly to the GWT ListBox but users HTML widgets to expose some
 * unique and powerful functionality.
 * 
 * HTMLListBox supports tooltips as well..
 * 
 * @author carl.scott
 * 
 */
public abstract class HTMLListBox extends ScrollPanel {

	protected ArrayList<String> items;
	protected Grid table;

	protected int selectedIndex;
	protected boolean isEnabled = true;

	protected String width;
	protected int initSize;
	
	protected int schedule = 1500;

	/**
	 * Creates a new Listbox with the designated width. If any widgets within
	 * the listbox violate this width, the box will scroll horizontally.
	 * 
	 * @param width
	 */
	public HTMLListBox(final String width, int initSize) {
		super();
		items = new ArrayList<String>();
		setWidth(this.width = width);
		init(initSize);
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
	public void addItem(final String name, final String value, final String toolTip) {
		HTML html;
		if (toolTip == null) {
			html = new HTML(name);
		} else {
			html = new HTMLWithToolTip(name, toolTip);
			((HTMLWithToolTip)html).setSchedule(schedule);
		}

		html.setWordWrap(false);
		html.setStyleName("CIPD_ListBox_Item");

		if (initSize > items.size()) {
			table.setWidget(items.size(), 0, html);
			items.add(value);

			table.getColumnFormatter().setWidth(0, width);
		}
	}

	/**
	 * Clears the list.
	 */
	public void clear() {
		items.clear();
		if(table!=null) remove(table);
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
	public Grid getTableView() {
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

	public void setTooltipSchedule(final int schedule) {
		this.schedule = schedule;
	}

	public void init(int size) {
		this.initSize = size;
		table = new Grid(size,1);
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

	public void showItemInView(final int index) {
		setScrollPosition(index * 10);
	}

}
