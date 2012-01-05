package org.iucn.sis.shared.api.displays.threats;

import org.iucn.sis.client.api.caches.TaxonomyCache;
import org.iucn.sis.shared.api.models.Field;
import org.iucn.sis.shared.api.models.Taxon;
import org.iucn.sis.shared.api.models.fields.IASTaxaThreatsSubfield;
import org.iucn.sis.shared.api.views.components.ThreatsTreeData;

import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;

public class FeralTaxonThreatViewer extends BasicThreatViewer {
	
	private ListBox ancestry;
	private boolean isMammal;

	public FeralTaxonThreatViewer(ThreatsTreeData data) {
		super(data);
		isMammal = false;
	}
	
	@Override
	public void createWidget() {
		super.createWidget();
		
		ThreatsTreeData treeData = (ThreatsTreeData)data;
		
		initializeListBox(ancestry = new ListBox(), treeData.getLookups().get("Threats_ancestryLookup"));
	}
	
	@Override
	public boolean hasChanged(Field rawField) {
		boolean hasChanged = super.hasChanged(rawField);
		if (hasChanged)
			return true;
		
		IASTaxaThreatsSubfield field = new IASTaxaThreatsSubfield(rawField);
		
		return hasListSelectionChanged(field.getAncestry(), ancestry);
	}
	
	@Override
	public void save(Field parent, Field rawField) {
		super.save(parent, rawField);
		
		IASTaxaThreatsSubfield field = new IASTaxaThreatsSubfield(rawField);
		
		if (hasListValue(ancestry))
			field.setAncestry(getListValue(ancestry));
	}
	
	@Override
	public void setData(Field raw) {
		super.setData(raw);
		
		IASTaxaThreatsSubfield field = new IASTaxaThreatsSubfield(raw);
		
		setListValue(ancestry, field.getAncestry());
		
		Taxon taxon = TaxonomyCache.impl.getTaxon(field.getIASTaxa());
		if (taxon != null)
			isMammal = ("MAMMALIA".equalsIgnoreCase(taxon.getFootprint()[2]));
		else
			isMammal = false;
		
	}
	
	@Override
	public void clearData() {
		super.clearData();
		ancestry.setSelectedIndex(-1);
	}
	
	@Override
	protected Widget createLabel() {
		super.createLabel();
		
		if (isMammal) {
			HorizontalPanel feralOption = new HorizontalPanel();
			feralOption.setSpacing(5);
			feralOption.add(new HTML("Ancestry: "));
			feralOption.add(ancestry);
			
			displayPanel.add(feralOption);
		}
		
		return displayPanel;
	}
	
	@Override
	protected Widget createViewOnlyLabel() {
		super.createViewOnlyLabel();

		if (isMammal) {
			HorizontalPanel feralOption = new HorizontalPanel();
			feralOption.setSpacing(5);
			feralOption.add(new HTML("Ancestry: "));
			feralOption.add(ancestry);
			
			displayPanel.add(feralOption);
		}
		
		return displayPanel;
	}

}
