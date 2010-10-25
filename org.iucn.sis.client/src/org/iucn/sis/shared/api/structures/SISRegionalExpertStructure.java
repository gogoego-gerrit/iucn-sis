package org.iucn.sis.shared.api.structures;

import java.util.ArrayList;

import org.iucn.sis.client.api.caches.AssessmentCache;
import org.iucn.sis.client.api.panels.RegionalExpertWidget;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.PrimitiveField;
import org.iucn.sis.shared.api.models.primitivefields.StringPrimitiveField;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

public class SISRegionalExpertStructure extends SISPrimitiveStructure<String> {
	
	private RegionalExpertWidget questionsPanel;

	public SISRegionalExpertStructure(String struct, String descript, String structID) {
		super(struct, descript, structID);
		buildContentPanel(Orientation.VERTICAL);
	}
	
	@Override
	protected PrimitiveField<String> getNewPrimitiveField() {
		return new StringPrimitiveField(getId(), null);
	}
	
	@Override
	public void clearData() {
		questionsPanel.clearWidgetData();
	}

	@Override
	protected Widget createLabel() {
		clearDisplayPanel();

		displayPanel.setWidth("100%");
		displayPanel.add(descriptionLabel);

		Assessment cur = AssessmentCache.impl.getCurrentAssessment();

		if (cur != null && cur.isRegional() && !cur.isEndemic()) {
			displayPanel.add(questionsPanel);
			questionsPanel.layout();
		} else {
			displayPanel.add(new HTML("<br><i>N/A for global or endemic assessments.</i>"));
			questionsPanel.layout();
		}

		return displayPanel;
	}

	@Override
	protected Widget createViewOnlyLabel() {
		clearDisplayPanel();

		displayPanel.setWidth("100%");
		displayPanel.add(descriptionLabel);

		Assessment cur = AssessmentCache.impl.getCurrentAssessment();

		if (cur != null && cur.isRegional() && !cur.isEndemic())
			displayPanel.add(new HTML(questionsPanel.getResultString()));
		else
			displayPanel.add(new HTML("<br><i>N/A for non-regional assessments.</i>"));
		// questionsPanel.layout();
		return displayPanel;
	}

	@Override
	public void createWidget() {
		descriptionLabel = new HTML(description);
		questionsPanel = new RegionalExpertWidget();
	}

	@Override
	public ArrayList<String> extractDescriptions() {
		ArrayList<String> ret = new ArrayList<String>();
		ret.add("Regional Expert Questions");
		return ret;
	}

	@Override
	public String getData() {
		return questionsPanel.getWidgetData();
	}

	/**
	 * Pass in the raw data from an Assessment object, and this will return
	 * it in happy, displayable String form
	 * 
	 * @return ArrayList of Strings, having converted the rawData to nicely
	 *         displayable String data. Happy days!
	 */
	@Override
	public int getDisplayableData(ArrayList<String> rawData, ArrayList<String> prettyData, int offset) {
		prettyData.add(offset, rawData.get(offset));
		return ++offset;
	}
	
	@Override
	public void setData(PrimitiveField<String> field) {
		String datum = field != null ? field.getValue() : null;
		questionsPanel.setWidgetData(datum);
			
		clearDisplayPanel();
		
		Assessment cur = AssessmentCache.impl.getCurrentAssessment();
		if (cur != null && cur.isRegional() && !cur.isEndemic())
			displayPanel.add(new HTML(questionsPanel.getResultString()));
		else {
			displayPanel.add(new HTML("<br><i>N/A for non-regional assessments.</i>"));
			questionsPanel.clearWidgetData();
		}
	}

	@Override
	public void setEnabled(boolean isEnabled) {
	}

}
