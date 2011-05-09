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
import java.util.Date;
import java.util.List;

import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.DateField;
import com.extjs.gxt.ui.client.widget.form.FileUploadField;
import com.extjs.gxt.ui.client.widget.form.HiddenField;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.extjs.gxt.ui.client.widget.form.NumberField;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.extjs.gxt.ui.client.widget.form.SimpleComboValue;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;

/**
 * FormBuilder.java
 * 
 * Utility class to help with some of the more mundane steps in 
 * creating form fields in Ext.
 * 
 * @author carl.scott
 *
 */
public class FormBuilder {

	public static TextField<String> createTextField(String name, String value, String label, boolean isRequired) {
		final TextField<String> field = new TextField<String>();
		field.setName(name);
		field.setFieldLabel(label);
		if (value != null)
			field.setValue(value);
		field.setAllowBlank(!isRequired);
		
		return field;
	}
	
	public static LabelField createLabelField(String name, String value, String label) {
		final LabelField field = new LabelField(value);
		field.setName(name);
		field.setFieldLabel(label);
		
		return field;
	}
	
	public static TextField<String> createPasswordField(String name, String value, String label, boolean isRequired) {
		final TextField<String> field = createTextField(name, value, label, isRequired);
		field.setPassword(true);
		
		return field;
	}
	
	public static TextArea createTextArea(String name, String value, String label, boolean isRequired) {
		final TextArea field = new TextArea();
		field.setName(name);
		field.setFieldLabel(label);
		if (value != null)
			field.setValue(value);
		field.setAllowBlank(!isRequired);
		
		return field;
	}
	
	public static NumberField createNumberField(String name, String value, String label, boolean isRequired) {
		Number num = null;
		try {
			num = Integer.parseInt(value);
		} catch (NumberFormatException e) {
			//Do nothing
		}
		return createNumberField(name, num, label, isRequired);
	}
	
	public static NumberField createNumberField(String name, Number value, String label, boolean isRequired) {
		final NumberField field = new NumberField();
		field.setAllowDecimals(false);
		field.setAllowNegative(false);
		field.setName(name);
		field.setFieldLabel(label);
		field.setAllowBlank(!isRequired);
		if (value != null)
			field.setValue(value);
		
		return field;
	}
	
	public static SimpleComboBox<String> createComboBox(String name, String value, String label, boolean isRequired, String... options) {
		final SimpleComboBox<String> box = new SimpleComboBox<String>();
		box.setTriggerAction(TriggerAction.ALL);
		box.setName(name);
		box.setFieldLabel(label);
		box.setEditable(false);
		box.setForceSelection(true);
		box.setAllowBlank(!isRequired);
		for (String option : options)
			box.add(option);
		if (value != null) {
			final List<SimpleComboValue<String>> selected = new ArrayList<SimpleComboValue<String>>();
			for (SimpleComboValue<String> current : box.getStore().getModels())
				if (value.equals(current.getValue()))
					selected.add(current);
			box.setSelection(selected);
		}
		return box;
	}
	
	public static ComboBox<BaseModelData> createModelComboBox(String name, String value, String label, boolean isRequired, String... options) {
		final BaseModelData[] array = new BaseModelData[options.length];
		
		int index = 0;
		for (String option : options) {
			final BaseModelData model = new BaseModelData();
			model.set("text", option);
			model.set("value", option);
			
			array[index++] = model;
		}
		
		return createModelComboBox(name, value, label, isRequired, array);
	}
	
	public static <X extends ModelData> ComboBox<X> createModelComboBox(String name, String value, String label, boolean isRequired, X... options) {
		X selected = null;
		
		final ListStore<X> store = new ListStore<X>();
		for (X model : options) {
			store.add(model);
			if (selected == null && value != null && value.equals(model.get("value")))
				selected = model;
		}
		
		final ComboBox<X> box = new ComboBox<X>();
		box.setName(name);
		box.setTriggerAction(TriggerAction.ALL);
		box.setFieldLabel(label);
		box.setEditable(false);
		box.setForceSelection(true);
		box.setAllowBlank(!isRequired);
		box.setStore(store);
		if (selected != null)
			box.setValue(selected);
		
		return box;
	}
	
	public static DateField createDateField(String name, Date value, String label, boolean isRequired) {
		final DateField field = new DateField();
		field.setName(name);
		field.setFieldLabel(label);
		if (value != null)
			field.setValue(value);
		field.setAllowBlank(!isRequired);
		
		return field;
	}
	
	/**
	 * When adding this field, remember to set your form's encoding type to 
	 * MULTIPART.
	 * @param name
	 * @param label
	 * @param isRequired
	 * @return
	 */
	public static FileUploadField createFileUploadField(String name, String label, boolean isRequired) {
		final FileUploadField field = new FileUploadField();
		field.setName(name);
		field.setFieldLabel(label);
		field.setAllowBlank(!isRequired);
		
		return field;
	}
	
	public static CheckBox createCheckBoxField(String name, Boolean value, String label) {
		final CheckBox field = new CheckBox();
		field.setName(name);
		field.setFieldLabel(label);
		if (value != null)
			field.setValue(value);
		
		return field;
	}
	
	public static HiddenField<String> createHiddenField(String name, String value) {
		final HiddenField<String> field = new HiddenField<String>();
		field.setName(name);
		field.setValue(value);
		
		return field;
	}
	
}
