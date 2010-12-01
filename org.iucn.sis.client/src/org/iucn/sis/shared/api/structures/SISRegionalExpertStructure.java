package org.iucn.sis.shared.api.structures;

import java.util.ArrayList;

import org.iucn.sis.client.api.caches.AssessmentCache;
import org.iucn.sis.client.api.panels.RegionalExpertWidget;
import org.iucn.sis.shared.api.debug.Debug;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.PrimitiveField;
import org.iucn.sis.shared.api.models.primitivefields.TextPrimitiveField;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

public class SISRegionalExpertStructure extends SISPrimitiveStructure<String> {
	
	private RegionalExpertWidget questionsPanel;
	private ArrayList<String> defaultValues;

	public SISRegionalExpertStructure(String struct, String descript, String structID) {
		super(struct, descript, structID);
		buildContentPanel(Orientation.VERTICAL);
	}
	
	@Override
	protected PrimitiveField<String> getNewPrimitiveField() {
		return new TextPrimitiveField(getId(), null);
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

		if (isAssessmentApplicable())
			displayPanel.add(questionsPanel);
		else 
			displayPanel.add(new HTML("<br><i>N/A for global or endemic assessments.</i>"));
		
		questionsPanel.layout();
		
		return displayPanel;
	}

	@Override
	protected Widget createViewOnlyLabel() {
		clearDisplayPanel();

		displayPanel.setWidth("100%");
		displayPanel.add(descriptionLabel);

		if (isAssessmentApplicable())
			displayPanel.add(new HTML(questionsPanel.getResultString()));
		else
			displayPanel.add(new HTML("<br><i>N/A for non-regional assessments.</i>"));
		// questionsPanel.layout();
		return displayPanel;
	}

	@Override
	public void createWidget() {
		defaultValues = new ArrayList<String>();
		defaultValues.add(",-1,0");
		defaultValues.add(",-1");
		defaultValues.add(",0");
		defaultValues.add("");
		
		descriptionLabel = new HTML(description);
		questionsPanel = new RegionalExpertWidget(defaultValues);
	}

	@Override
	public ArrayList<String> extractDescriptions() {
		ArrayList<String> ret = new ArrayList<String>();
		ret.add("Regional Expert Questions");
		return ret;
	}

	@Override
	public String getData() {
		if (isAssessmentApplicable())
			return questionsPanel.getWidgetData();
		else
			return null;
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
	public boolean hasChanged(PrimitiveField<String> field) {
		String oldValue = field == null ? null : field.getRawValue();
		if ("".equals(oldValue) || defaultValues.contains(oldValue))
			oldValue = null;
		
		String newValue = getData();
		if ("".equals(newValue) || defaultValues.contains(newValue))
			newValue = null;
		
		Debug.println("Comparing {0} to {1}", oldValue, newValue);
		
		if (newValue == null)
			return oldValue != null;
		else
			if (oldValue == null)
				return true;
			else
				return !newValue.equals(oldValue);
	}
	
	@Override
	public void setData(PrimitiveField<String> field) {
		String datum = field != null ? field.getValue() : null;
		questionsPanel.setWidgetData(datum);
			
		clearDisplayPanel();
		
		if (isAssessmentApplicable())
			displayPanel.add(new HTML(questionsPanel.getResultString()));
		else {
			displayPanel.add(new HTML("<br><i>N/A for non-regional assessments.</i>"));
			questionsPanel.clearWidgetData();
		}
	}
	
	/**
	 * Determines if this structure should display 
	 * for this assessment.
	 * @return
	 */
	private boolean isAssessmentApplicable() {
		Assessment cur = AssessmentCache.impl.getCurrentAssessment();
		return cur != null && cur.isRegional() && !cur.isEndemic();
	}

	@Override
	public void setEnabled(boolean isEnabled) {
	}

}
