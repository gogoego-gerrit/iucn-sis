/*******************************************************************************
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
 *     http://www.gnu.org/licenses
 ******************************************************************************/
package com.solertium.util.extjs.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.util.BaseEventPreview;
import com.extjs.gxt.ui.client.util.KeyNav;
import com.extjs.gxt.ui.client.util.Rectangle;
import com.extjs.gxt.ui.client.widget.ComponentHelper;
import com.extjs.gxt.ui.client.widget.DataList;
import com.extjs.gxt.ui.client.widget.DataListItem;
import com.extjs.gxt.ui.client.widget.form.PropertyEditor;
import com.extjs.gxt.ui.client.widget.form.TriggerField;
import com.extjs.gxt.ui.client.widget.menu.Item;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.google.gwt.user.client.Element;

/**
 * 
 * @author liz.schwartz
 * 
 */
public class CheckboxMultiTriggerField extends TriggerField<String> {

	public class OptionsMenu extends Menu {

		protected OptionsMenuItem item;
		public DataList list;

		public OptionsMenu() {
			item = new OptionsMenuItem();
			list = item.list;
			list.setScrollMode(Scroll.AUTO);
			add(item);
			setWidth(listboxWidth);
			setHeight(listboxHeight);
		}

		@Override
		protected void doAttachChildren() {
			super.doAttachChildren();
			if (!list.isRendered())
				this.layout(true);
			ComponentHelper.doAttach(list);
		}

		@Override
		protected void doDetachChildren() {
			super.doDetachChildren();
			ComponentHelper.doDetach(list);
		}

		public String getOptions() {
			String options = "";
			for (DataListItem dli : item.list.getChecked())
				options += dli.getText() + delimiter;
			String v = options.length() == 0 ? options : options.substring(0, options.length() - delimiter.length());

			if (filteredValues.size() > 0) {
				String value = (v == null || v.equals("") ? "" : v + delimiter);

				for (String val : filteredValues)
					value += val + delimiter;

				v = value.substring(0, value.length() - delimiter.length());
			}
			return v;
		}

	}

	public class OptionsMenuItem extends Item {

		public DataList list;

		public OptionsMenuItem() {
			hideOnClick = false;
			list = getOptionsList();
			OptionsMenuItem.this.setWidth(listboxWidth);
			OptionsMenuItem.this.setHeight(listboxHeight);
		}

		@Override
		protected void onRender(Element target, int index) {
			super.onRender(target, index);
			list.render(target, index);
			setElement(list.getElement());
			list.focus();
		}

	}

	protected List<String> options;
	protected List<String> filteredOptions;
	protected List<String> filteredValues;

	protected String listboxHeight = "300px";
	protected String listboxWidth = "250px";
	protected DataList dataList;
	protected OptionsMenu menu;
	private BaseEventPreview focusPreview;
	protected String delimiter;

	protected String filterRegex;

	public CheckboxMultiTriggerField(List<String> options) {
		this(options, ", ");
	}

	public CheckboxMultiTriggerField(List<String> options, String delimiter) {
		super();
		this.options = options;
		this.delimiter = delimiter;
		this.filteredOptions = new ArrayList<String>();
		this.filteredValues = new ArrayList<String>();
		this.filterRegex = null;

		propertyEditor = new PropertyEditor<String>() {

			public String convertStringValue(String value) {
				return value;
			}

			public String getStringValue(String value) {
				return value;
			}
		};
		setReadOnly(true);
	}

	/**
	 * Will filter out options AND values passed in that match this regular
	 * expression. This allows you to keep users from selecting certain options
	 * but not having them be removed from the list because of it.
	 * 
	 * @param filterRegex
	 */
	public void setFilterRegex(String filterRegex) {
		this.filterRegex = filterRegex;
		filter();
	}

	protected void filter() {
		options.addAll(filteredOptions);
		filteredOptions.clear();

		if (filterRegex != null) {
			for (String curOption : options)
				if (curOption.matches(filterRegex))
					filteredOptions.add(curOption);

			options.removeAll(filteredOptions);
		}
	}

	private void doBlur(ComponentEvent ce) {
		if (menu != null && menu.isVisible()) {
			menu.hide();
		}
		super.onBlur(ce);
	}

	protected void expand() {
		if (menu == null) {
			menu = new OptionsMenu();
		}

		menu.show(trigger.dom, "tl-bl?");
		menu.focus();
	}

	public String getDelimiter() {
		return delimiter;
	}

	public String getListboxHeight() {
		return listboxHeight;
	}

	public String getListboxWidth() {
		return listboxWidth;
	}

	public List<String> getOptions() {
		return options;
	}

	public DataList getOptionsList() {

		if (dataList == null) {
			dataList = new DataList() {

				@Override
				protected void onAttach() {
					super.onAttach();

					List<String> selectedOptions;
					if (getValue() != null) {
						selectedOptions = Arrays.asList(getValue().split(getDelimiter()));
					} else {
						selectedOptions = new ArrayList<String>();
					}

					for (DataListItem item : getItems()) {
						item.setChecked(selectedOptions.contains(item.getText()));
					}
				}

				@Override
				protected void onUnload() {
					super.onUnload();
					setValue(menu.getOptions());

					for (DataListItem item : getChecked()) {
						item.setChecked(false);
					}
				}

			};
			dataList.setSelectionMode(SelectionMode.MULTI);
			dataList.setCheckable(true);
			dataList.setHeight("300px");

			for (String option : this.options) {
				dataList.add(new DataListItem(option));
			}
		}

		return dataList;
	}

	@Override
	public String getValue() {
		if (!rendered) {
			return value;
		}
		String v = getRawValue();
		if (emptyText != null && v.equals(emptyText)) {
			return null;
		}
		if (v == null || v.equals("")) {
			return null;
		}
		try {
			return propertyEditor.convertStringValue(v);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	protected void onBlur(final ComponentEvent ce) {
		Rectangle rec = trigger.getBounds();
		if (rec.contains(BaseEventPreview.getLastClientX(), BaseEventPreview.getLastClientY())) {
			ce.stopEvent();

			return;
		}
		if (menu != null && (menu.isVisible() || !menu.isRendered())) {
			ce.cancelBubble();
			menu.focus();
			return;
		}
		hasFocus = false;
		doBlur(ce);
	}

	@Override
	protected void onClick(ComponentEvent ce) {

		if (ce.getTarget() == getInputEl().dom) {

			onTriggerClick(ce);
			return;
		}
		super.onClick(ce);
	}

	protected void onDown(FieldEvent fe) {
		fe.cancelBubble();

		if (menu == null || !menu.isAttached()) {
			expand();
		}
	}

	@Override
	protected void onFocus(ComponentEvent ce) {
		super.onFocus(ce);
		focusPreview.add();
	}

	@Override
	protected void onKeyPress(FieldEvent fe) {
		super.onKeyPress(fe);
		int code = fe.getEvent().getKeyCode();
		if (code == 9 || code == 13) {
			if (menu != null && menu.isAttached()) {
				menu.hide();
			}
		}

	}

	@Override
	protected void onRender(Element target, int index) {
		super.onRender(target, index);
		focusPreview = new BaseEventPreview();

		new KeyNav<FieldEvent>(this) {
			public void onDown(FieldEvent fe) {
				CheckboxMultiTriggerField.this.onDown(fe);
			}
		};
	}

	@Override
	protected void onTriggerClick(ComponentEvent ce) {
		super.onTriggerClick(ce);
		if (disabled) {
			return;
		}
		expand();
		getInputEl().focus();
	}

	public void setDelimiter(String delimiter) {
		this.delimiter = delimiter;
	}

	public void setListboxHeight(String listboxHeight) {
		this.listboxHeight = listboxHeight;
	}

	public void setListboxWidth(String listboxWidth) {
		this.listboxWidth = listboxWidth;
	}

	public void setOptions(List<String> options) {
		this.options = options;
		filter();
	}

	@Override
	public void setRawValue(String value) {
		filteredValues.clear();

		if (filterRegex != null) {
			List<String> split = new ArrayList<String>(Arrays.asList(value.split(delimiter)));
			for (String test : split)
				if (test.matches(filterRegex))
					filteredValues.add(test);

			if (filteredValues.size() > 0)
				split.removeAll(filteredValues);

			StringBuilder newValue = new StringBuilder();
			for (String cur : split) {
				newValue.append(cur);
				newValue.append(delimiter);
			}

			super.setRawValue(newValue.length() == 0 ? newValue.toString() : newValue.substring(0, newValue.length()
					- delimiter.length()));
		}
		super.setRawValue(value);
	}

	@Override
	public boolean validateValue(String value) {
		if (value == null || "".equals(value.trim()))
			return getAllowBlank();

		String[] currentOptions = value.split("\\Q" + delimiter + "\\E");
		for (String current : currentOptions) {
			if (!options.contains(current) && !current.matches(filterRegex))
				return false;
		}

		return true;
	}

}
