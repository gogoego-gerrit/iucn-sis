package org.iucn.sis.client.api.assessment;

import java.util.ArrayList;
import java.util.List;

import org.iucn.sis.client.api.assessment.FieldAttachmentManager.AttachableFieldModelData;

import com.extjs.gxt.ui.client.GXT;
import com.extjs.gxt.ui.client.core.XTemplate;
import com.extjs.gxt.ui.client.widget.CheckBoxListView;
import com.extjs.gxt.ui.client.widget.form.ComboBox;

public class AttachableFieldComboBox extends ComboBox<AttachableFieldModelData> {
	
	public AttachableFieldComboBox() {
		super();
		setEditable(false);
		setForceSelection(true);
		setLazyRender(false);
		setTriggerAction(TriggerAction.ALL);
		setView(new CheckBoxListView<AttachableFieldModelData>());
		setTemplate(XTemplate.create(
			"<tpl for=\".\"><div class='x-view-item x-view-item-check'><table cellspacing='"
			+ (GXT.isIE && !GXT.isStrict ? "0" : "3")
			+ "' cellpadding=0><tr><td><input class=\"x-view-item-checkbox\" type=\"checkbox\" /></td><td><td>{"
			+ "text" + "}</td></tr></table></div></tpl>"));
		
	}
	
	@Override
	public AttachableFieldModelData getValue() {
		List<String> options = new ArrayList<String>();
		for (AttachableFieldModelData model : getCheckBoxView().getChecked())
			options.add(model.toString());
		
		return new AttachableFieldModelData(options);
	}
	
	@Override
	public String getRawValue() {
		return getValue().toCSV();
	}
	
	@Override
	public void setValue(AttachableFieldModelData value) {
		AttachableFieldModelData m = value;
		if (m == null)
			m = new AttachableFieldModelData("");
		
		for (AttachableFieldModelData model : getStore().getModels())
			getCheckBoxView().setChecked(model, m.hasValue(model.toString()));
	}
	
	@Override
	public void setRawValue(String text) {
		if (text != null)
			setValue(new AttachableFieldModelData(text));
		
		super.setRawValue(text);
	}
	
	@SuppressWarnings("unchecked")
	private CheckBoxListView<AttachableFieldModelData> getCheckBoxView() {
		return (CheckBoxListView)getView();
	}

}

