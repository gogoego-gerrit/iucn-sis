package org.iucn.sis.client.panels.integrity;

import org.iucn.sis.shared.api.integrity.SQLQuery;

import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.form.FieldSet;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.form.FormPanel.LabelAlign;
import com.solertium.util.extjs.client.FormBuilder;

public class SQLValidationDesigner extends LayoutContainer {
	
	private final SQLQuery query;
	
	private final TextArea joins, conditions, message; 
	
	public SQLValidationDesigner(SQLQuery query) {
		this.query = query;
		
		FormPanel panel = newForm();
		panel.add(newHtml("SELECT assessment.id FROM assessment"));
		panel.add(joins = newTextArea("joins", query.getJoins(), "Enter JOIN clauses"));
		panel.add(newHtml("WHERE (assessment.id = ?) AND ("));
		panel.add(conditions = newTextArea("conditions", query.getConditions(), "Enter additional WHERE clauses"));
		panel.add(newHtml(")"));
		
		FieldSet queryParts = new FieldSet();
		queryParts.setHeading("Create Query");
		queryParts.add(panel);
		
		add(queryParts);
		
		FormPanel errorPanel = newForm();
		errorPanel.add(message = newTextArea("message", query.getMessage(), "Enter message to present if assessment fails validation"));
		
		FieldSet errorPart = new FieldSet();
		errorPart.setHeading("Failed Validation Message");
		errorPart.add(errorPanel);
		
		add(errorPart);
	}
	
	private FormPanel newForm() {
		FormPanel panel = new FormPanel();
		panel.setLabelAlign(LabelAlign.TOP);
		panel.setBorders(false);
		panel.setBodyBorder(false);
		panel.setHeaderVisible(false);
		panel.setFieldWidth(400);
		
		return panel;
	}
	
	private TextArea newTextArea(String name, String value, String label) {
		TextArea area = FormBuilder.createTextArea(name, value, label, false);
		area.setHeight(100);
		area.setWidth(400);
		
		return area;
	}
	
	private Html newHtml(String value) {
		Html html = new Html(value);
		html.addStyleName("integrity_sql_text");
		
		return html;
	}
	
	public void save() {
		query.setJoins(getValue(joins));
		query.setConditions(getValue(conditions));
		query.setMessage(getValue(message));
	}
	
	private String getValue(TextArea area) {
		String value = area.getValue();
		if ("".equals(value))
			value = null;
		
		return value;
	}
	
}
