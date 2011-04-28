package org.iucn.sis.client.panels.permissions;

import java.util.ArrayList;
import java.util.List;

import org.iucn.sis.client.panels.permissions.PermissionUserModel.PermissionsModelData;

import com.extjs.gxt.ui.client.GXT;
import com.extjs.gxt.ui.client.core.XTemplate;
import com.extjs.gxt.ui.client.widget.CheckBoxListView;
import com.extjs.gxt.ui.client.widget.form.ComboBox;

public class PermissionComboBox extends ComboBox<PermissionsModelData> {
	
	public PermissionComboBox() {
		super();
		setEditable(false);
		setForceSelection(true);
		setLazyRender(false);
		setTriggerAction(TriggerAction.ALL);
		setView(new CheckBoxListView<PermissionsModelData>());
		setTemplate(XTemplate.create(
			"<tpl for=\".\"><div class='x-view-item x-view-item-check'><table cellspacing='"
			+ (GXT.isIE && !GXT.isStrict ? "0" : "3")
			+ "' cellpadding=0><tr><td><input class=\"x-view-item-checkbox\" type=\"checkbox\" /></td><td><td>{"
			+ "text" + "}</td></tr></table></div></tpl>"));
		
	}
	
	@Override
	public PermissionsModelData getValue() {
		List<String> options = new ArrayList<String>();
		for (PermissionsModelData model : getCheckBoxView().getChecked())
			options.add(model.toString());
		
		return new PermissionsModelData(options);
	}
	
	@Override
	public String getRawValue() {
		return getValue().toCSV();
	}
	
	@Override
	public void setValue(PermissionsModelData value) {
		PermissionsModelData m = value;
		if (m == null)
			m = new PermissionsModelData("");
		
		for (PermissionsModelData model : getStore().getModels())
			getCheckBoxView().setChecked(model, m.hasPermission(model.toString()));
	}
	
	@Override
	public void setRawValue(String text) {
		if (text != null)
			setValue(new PermissionsModelData(text));
		
		super.setRawValue(text);
	}
	
	private CheckBoxListView<PermissionsModelData> getCheckBoxView() {
		return (CheckBoxListView)getView();
	}

}
