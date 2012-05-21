package org.iucn.sis.shared.api.displays.threats;

import org.iucn.sis.shared.api.models.Field;
import org.iucn.sis.shared.api.models.fields.ThreatsSubfield;
import org.iucn.sis.shared.api.views.components.ThreatsTreeData;

import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;

public class ThreatWithInternationalTrade extends BasicThreatViewer {
	
	private final String description;
	
	private ListBox internationalTrade;

	public ThreatWithInternationalTrade(String description, ThreatsTreeData data) {
		super(data);
		this.description = description;
	}
	
	@Override
	public void createWidget() {
		super.createWidget();
		
		ThreatsTreeData treeData = (ThreatsTreeData)data;
		initializeListBox(internationalTrade = new ListBox(), treeData.getLookups().get("Threats_internationalTradeLookup"));			
	}
	
	@Override
	public boolean hasChanged(Field rawField) {
		boolean hasChanged = super.hasChanged(rawField);		
		if (hasChanged)
			return true;
		
		ThreatsSubfield field = new ThreatsSubfield(rawField);		
		return hasListSelectionChanged(field.getInternationalTrade(), internationalTrade);	
	}
	
	@Override
	public void save(Field parent, Field rawField) {
		super.save(parent, rawField);
		
		ThreatsSubfield field = new ThreatsSubfield(rawField);

		if (hasListValue(internationalTrade))
			field.setInternationalTrade(getListValue(internationalTrade));
	}
	
	@Override
	public void setData(Field raw) {
		super.setData(raw);
		
		ThreatsSubfield field = new ThreatsSubfield(raw);
		
		setListValue(internationalTrade, field.getInternationalTrade());
	}
	
	@Override
	public void clearData() {
		super.clearData();
		
		internationalTrade.setSelectedIndex(-1);
	}
	
	@Override
	protected Widget createLabel() {
		super.createLabel();
		
		displayPanel.add(new HTML(description));
		displayPanel.add(internationalTrade);
		
		return displayPanel;
	}
	
	@Override
	protected Widget createViewOnlyLabel() {
		super.createViewOnlyLabel();
		
		displayPanel.add(new HTML(description));
		displayPanel.add(internationalTrade);
		
		return displayPanel;
	}
	
	@Override
	public void setEnabled(boolean isEnabled) {
		super.setEnabled(isEnabled);
		internationalTrade.setEnabled(isEnabled);
	}

}
