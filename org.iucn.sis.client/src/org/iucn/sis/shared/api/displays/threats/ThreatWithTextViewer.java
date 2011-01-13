package org.iucn.sis.shared.api.displays.threats;

import org.iucn.sis.shared.api.models.Field;
import org.iucn.sis.shared.api.models.fields.UnspecifiedTaxaThreatsSubfield;

import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

public class ThreatWithTextViewer extends BasicThreatViewer {
	
	private final String description;
	
	private TextArea textArea;

	public ThreatWithTextViewer(String description, ThreatsTreeData data) {
		super(data);
		this.description = description;
	}
	
	@Override
	public void createWidget() {
		super.createWidget();
		
		textArea = new TextArea();
		textArea.setSize(350, 125);
	}
	
	@Override
	public boolean hasChanged(Field rawField) {
		boolean hasChanged = super.hasChanged(rawField);
		if (hasChanged)
			return true;
		
		UnspecifiedTaxaThreatsSubfield field = 
			new UnspecifiedTaxaThreatsSubfield(rawField);
		
		String oldValue = field.getExplanation();
		String newValue = textArea.getValue();
		if (newValue == null)
			newValue = "";
		
		return !oldValue.equals(newValue);
	}
	
	@Override
	public void save(Field parent, Field rawField) {
		super.save(parent, rawField);
		
		UnspecifiedTaxaThreatsSubfield proxy = new UnspecifiedTaxaThreatsSubfield(rawField);
		proxy.setExplanation(textArea.getValue());
	}
	
	@Override
	public void setData(Field raw) {
		super.setData(raw);
		
		UnspecifiedTaxaThreatsSubfield proxy = new UnspecifiedTaxaThreatsSubfield(raw);
		
		textArea.setValue(proxy.getExplanation());
	}
	
	@Override
	public void clearData() {
		super.clearData();
		
		textArea.clear();
	}
	
	@Override
	protected Widget createLabel() {
		super.createLabel();
		
		displayPanel.add(new HTML(description));
		displayPanel.add(textArea);
		
		return displayPanel;
	}
	
	@Override
	protected Widget createViewOnlyLabel() {
		super.createViewOnlyLabel();
		
		displayPanel.add(new HTML(description));
		displayPanel.add(textArea);
		
		return displayPanel;
	}
	
	@Override
	public void setEnabled(boolean isEnabled) {
		super.setEnabled(isEnabled);
		textArea.setEnabled(isEnabled);
	}

}
