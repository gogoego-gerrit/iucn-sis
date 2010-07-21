package org.iucn.sis.shared.structures;

import java.util.ArrayList;

import org.iucn.sis.client.data.assessments.AssessmentCache;
import org.iucn.sis.client.expert.RegionalExpertWidget;
import org.iucn.sis.shared.data.assessments.AssessmentData;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

public class SISRegionalExpertStructure extends Structure {
	private RegionalExpertWidget questionsPanel;

	public SISRegionalExpertStructure(String struct, String descript) {
		super(struct, descript);
		buildContentPanel(Orientation.VERTICAL);
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

		AssessmentData cur = AssessmentCache.impl.getCurrentAssessment();

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

		AssessmentData cur = AssessmentCache.impl.getCurrentAssessment();

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
	public ArrayList extractDescriptions() {
		ArrayList ret = new ArrayList();
		ret.add("Regional Expert Questions");
		return ret;
	}

	@Override
	public Object getData() {
		String ret = questionsPanel.getWidgetData();
		return ret;
	}

	/**
	 * Pass in the raw data from an AssessmentData object, and this will return
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
	public int setData(ArrayList dataList, int dataOffset) {
		super.setData(dataList, dataOffset);
		try {
			if (dataList.size() > dataOffset) {
				questionsPanel.setWidgetData((String) dataList.get(dataOffset++));
			} else {
				questionsPanel.clearWidgetData();
			}
			
			clearDisplayPanel();
			
			AssessmentData cur = AssessmentCache.impl.getCurrentAssessment();
			if (cur != null && cur.isRegional() && !cur.isEndemic())
				displayPanel.add(new HTML(questionsPanel.getResultString()));
			else {
				displayPanel.add(new HTML("<br><i>N/A for non-regional assessments.</i>"));
				questionsPanel.clearWidgetData();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return dataOffset;
	}

	@Override
	protected void setEnabled(boolean isEnabled) {
	}

	@Override
	public String toXML() {
		return StructureSerializer.toXML(this);
	}

}
