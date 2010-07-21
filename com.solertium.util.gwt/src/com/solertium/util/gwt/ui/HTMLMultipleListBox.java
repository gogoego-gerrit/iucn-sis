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
import java.util.HashMap;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.SourcesTableEvents;
import com.google.gwt.user.client.ui.TableListener;
import com.google.gwt.user.client.ui.Widget;

/**
 * HTMLMultipleListBox.java
 * 
 * Works very similarly to HTMLListBox, except it utilizes checkboxes to make it
 * a multiple select (as opposed to CTRL+Click). Clicking on an item still calls
 * onChange(), but checking an item now selects it (not just clicking it), and
 * also calls onChecked()
 * 
 * @author carl.scott
 * 
 */
public abstract class HTMLMultipleListBox extends ScrollPanel {

	class SelectableHTML extends SimplePanel {

		private final CheckBox checkBox;
		private final HTML html;

		private PopupPanel popupPanel;
		private boolean isPanelShowing = false;
		private String toolTip;
		private Timer timer;

		private int left, top;

		public SelectableHTML(final String name, final String value) {
			this(name, value, null);
		}

		public SelectableHTML(final String name, final String value, final String toolTip) {
			super();
			checkBox = new CheckBox();
			checkBox.addClickListener(new ClickListener() {
				public void onClick(final Widget sender) {
					if (checkBox.isChecked()) {
						selectedValues.put(value, null);
						onChecked(value, true);
					} else if (selectedValues.containsKey(value)) {
						selectedValues.remove(value);
						onChecked(value, false);
					}
				}
			});

			html = new StyledHTML(name, "fontSize80");
			html.setWordWrap(false);

			final Grid checktable = new Grid(1,2);
			checktable.setWidget(0, 0, checkBox);
			checktable.setWidget(0, 1, html);
			setWidget(checktable);

			if (toolTip != null) {
				this.toolTip = toolTip;
				popupPanel = new PopupPanel(true, false) {
					public void hide() {
						super.hide();
						isPanelShowing = false;
					}
				};
				sinkEvents(Event.ONMOUSEOVER | Event.ONMOUSEOUT);
			}

			setStyleName("CIPD_ListBox_Item");
		}

		public String getText() {
			return html.getText();
		}

		public boolean isSelected() {
			return checkBox.isChecked();
		}

		public void onBrowserEvent(final Event evt) {
			switch (DOM.eventGetType(evt)) {
			case Event.ONMOUSEOVER: {
				left = DOM.eventGetClientX(evt);
				top = DOM.eventGetClientY(evt);
				timer = new Timer() {
					public void run() {
						showPopup(left, top);
					}
				};
				timer.schedule(schedule);
				break;
			}
			case Event.ONMOUSEOUT: {
				try {
					timer.cancel();
				} catch (final Exception e) {
				}
				if (isPanelShowing) {
					popupPanel.hide();
					isPanelShowing = false;
				}
				break;
			}
			}
		}

		public void setChecked(final boolean isChecked) {
			checkBox.setChecked(isChecked);
		}

		public void setEnabled(final boolean isEnabled) {
			checkBox.setEnabled(isEnabled);
		}

		public void setHTML(final String text) {
			html.setHTML(text);
		}

		private void showPopup(int left, int top) {
			if (!isPanelShowing) {
				popupPanel.setWidget(new StyledHTML(toolTip, "fontSize80"));
				popupPanel.addStyleName("CIPD_PopupPanel");

				popupPanel.setWidth("300px");
				popupPanel.show();

				final int bottom = Window.getClientHeight();
				final int right = Window.getClientWidth();

				if (left + popupPanel.getOffsetWidth() > right) {
					left = right - popupPanel.getOffsetWidth();
				}
				if (top + popupPanel.getOffsetHeight() > bottom) {
					top = bottom - popupPanel.getOffsetHeight();
				}

				popupPanel.setPopupPosition(left, top);
				isPanelShowing = true;
			}
		}
	}

	protected ArrayList<String> items;

	protected Grid table;
	protected int selectedIndex;
	protected HashMap<String, String> selectedValues;

	protected boolean isEnabled = true;

	protected String width;
	
	protected int schedule = 1500;

	public HTMLMultipleListBox(final String width) {
		super();
		items = new ArrayList<String>();
		selectedValues = new HashMap<String, String>();
		setWidth(this.width = width);
	}

	public void addItem(final String name, final String value) {
		addItem(name, value, null);
	}

	public void addItem(final String name, final String value,
			final String toolTip) {
		table.setWidget(items.size(), 0, 
			new SelectableHTML(name, value, toolTip));
		items.add(value);

		table.getColumnFormatter().setWidth(0, width);
	}

	public void clear() {
		items.clear();
		if(table!=null) remove(table);
		selectedIndex = -1;
		selectedValues.clear();
	}

	public ArrayList<String> getCheckedValues() {
		final ArrayList<String> checked = new ArrayList<String>();
		for (int i = 0; i < items.size(); i++)
			if (((SelectableHTML) table.getWidget(i, 0)).isSelected()) 
				checked.add(items.get(i));
		return checked;
	}

	public int getItemCount() {
		return items.size();
	}

	public int getSelectedIndex() {
		return selectedIndex;
	}

	public Grid getTableView() {
		return table;
	}

	public String getText(final int index) {
		return ((SelectableHTML) table.getWidget(index, 0)).getText();
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

	public abstract void onChange(String selectedValue);

	/**
	 * Method that occurs when an item is checked.
	 * 
	 * @param checkedValue
	 *            the value of the item checked
	 * @param isChecked
	 *            the isChecked() value of the checkbox
	 */
	public abstract void onChecked(String checkedValue, boolean isChecked);

	public void setChecked(final int index, final boolean isChecked) {
		((SelectableHTML) table.getWidget(index, 0)).setChecked(isChecked);
		if (isChecked) {
			selectedValues.put(items.get(index), null);
		} else {
			selectedValues.remove(items.get(index));
		}
	}

	public void setEnabled(final boolean isEnabled) {
		this.isEnabled = isEnabled;
		for (int i = 0; i < items.size(); i++) {
			((SelectableHTML) table.getWidget(i, 0)).setEnabled(isEnabled);
		}
	}

	public void setSelectedIndex(final int index) {
		selectedIndex = index;
		for (int i = 0; i < items.size(); i++) {
			((SelectableHTML) table.getWidget(i, 0))
					.setStyleName(i == index ? "CIPD_ListBox_ItemSelected"
							: "CIPD_ListBox_Item");
		}
	}

	public void setText(final String text, final int index) {
		((SelectableHTML) table.getWidget(index, 0)).setHTML(text);
	}
	
	public void setTooltipSchedule(final int schedule) {
		this.schedule = schedule;
	}

	public void init(int size) {
		table = new Grid(size,1);
		table.addTableListener(new TableListener() {
			public void onCellClicked(final SourcesTableEvents sender, final int row, final int cell) {
				if (isEnabled) {
					setSelectedIndex(row);
					onChange((String) items.get(row));
				}
			}
		});
		setWidget(table);
	}
}
