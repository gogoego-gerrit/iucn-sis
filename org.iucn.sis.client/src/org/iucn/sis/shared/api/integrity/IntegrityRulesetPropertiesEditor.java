package org.iucn.sis.shared.api.integrity;

import org.iucn.sis.client.api.utils.BasicWindow;
import org.iucn.sis.client.panels.integrity.IntegrityQuery;
import org.iucn.sis.shared.api.models.AssessmentIntegrityValidation;

import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.solertium.util.extjs.client.FormBuilder;
import com.solertium.util.extjs.client.WindowUtils;

public class IntegrityRulesetPropertiesEditor extends BasicWindow {
	
	private final IntegrityQuery query;
	
	private ComboBox<TextValueModelData> failureCondition;
	private ComboBox<TextValueModelData> failureMode;

	public IntegrityRulesetPropertiesEditor(IntegrityQuery query) {
		super("Edit Properties");
		this.query = query;
		
		setSize(450, 200);
		setLayout(new FillLayout());
		
		buildFailureConditionComboBox();
		
		final FormPanel form = new FormPanel();
		form.setBorders(false);
		form.setBodyBorder(false);
		form.setHeaderVisible(false);
		form.setLabelWidth(150);
		form.add(failureCondition);
		form.add(failureMode);
		
		add(form);
		
		addButton(new Button("Done", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				if (form.isValid()) {
					IntegrityRulesetPropertiesEditor.this.query.setProperty(
						failureCondition.getName(), failureCondition.getValue().getValue()
					);
					IntegrityRulesetPropertiesEditor.this.query.setProperty(
						failureMode.getName(), failureMode.getValue().getValue()
					);
					
					hide();
				}
				else
					WindowUtils.errorAlert("Please fill in all fields first.");
			}
		}));
		addButton(new Button("Cancel", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				hide();
			}
		}));
	}
	
	private void buildFailureConditionComboBox() {
		failureCondition = FormBuilder.createModelComboBox(AssessmentIntegrityValidation.PROPERTY_FAILURE_CONDITION, 
				query.getProperty(AssessmentIntegrityValidation.PROPERTY_FAILURE_CONDITION, "default"), 
				"Failure Condition", true, 
				new TextValueModelData("Fail if conditions met (default)", "default"), 
				new TextValueModelData("Fail if conditions not met", "not_met"));

		failureMode = FormBuilder.createModelComboBox(AssessmentIntegrityValidation.PROPERTY_FAILURE_MODE, 
				query.getProperty(AssessmentIntegrityValidation.PROPERTY_FAILURE_MODE, "default"), 
				"Failure Mode", true, 
				new TextValueModelData("Fail Validation", "default"), 
				new TextValueModelData("Passes with Warning", "warning"));
	}
	
	private static class TextValueModelData extends BaseModelData {
		private static final long serialVersionUID = 1L;
		
		public TextValueModelData(String text, String value) {
			super();
			set("text", text);
			set("value", value);
		}
		
		public String getValue() {
			return get("value");
		}
	}

}
