package org.iucn.sis.client.components.panels;

import java.util.Date;

import org.iucn.sis.client.data.assessments.AssessmentCache;
import org.iucn.sis.client.utilities.FormattedDate;
import org.iucn.sis.shared.BaseAssessment;
import org.iucn.sis.shared.data.TaxonomyCache;
import org.iucn.sis.shared.data.WorkingSetCache;
import org.iucn.sis.shared.data.WorkingSetData;
import org.iucn.sis.shared.data.assessments.AssessmentData;
import org.iucn.sis.shared.data.assessments.RegionCache;
import org.iucn.sis.shared.taxonomyTree.TaxonNode;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;

public class HeaderSummaryPanel extends LayoutContainer {

	private VerticalPanel workingSetSummary = null;
	private VerticalPanel assessmentSummary = null;
	private VerticalPanel taxonSummary = null;

	public HeaderSummaryPanel() {
		setLayout(new FillLayout(Orientation.HORIZONTAL));
		setBorders(false);
		setStyleName("x-panel");
		setLayoutOnChange(true);

		workingSetSummary = new VerticalPanel();
		assessmentSummary = new VerticalPanel();
		taxonSummary = new VerticalPanel();

		workingSetSummary.setHeight("100%");
		assessmentSummary.setHeight("100%");
		taxonSummary.setHeight("100%");

		update();
	}

	private void fillData() {
		removeAll();

		workingSetSummary.clear();
		workingSetSummary.setStyleName("hasSmallHTML");

		if (WorkingSetCache.impl.getCurrentWorkingSet() != null) {
			// taxonSummary.add( new HTML("<u>Taxon Summary</u>"));

			WorkingSetData curSet = WorkingSetCache.impl.getCurrentWorkingSet();
			workingSetSummary.add(new HTML("Name: " + curSet.getWorkingSetName() + "<br />"));
			workingSetSummary.add(new HTML("Manager: " + curSet.getCreator() + "<br />"));
			workingSetSummary.add(new HTML("Created: " + curSet.getDate()));
			// contained.layout();
		} else
			workingSetSummary.add(new HTML("No Working Set Selected"));

		taxonSummary.clear();
		taxonSummary.setStyleName("hasSmallHTML");

		if (TaxonomyCache.impl.getCurrentNode() != null) {
			// taxonSummary.add( new HTML("<u>Taxon Summary</u>"));

			String commonName = "";
			if (TaxonomyCache.impl.getCurrentNode().getCommonNames().size() > 0)
				commonName = "" + TaxonomyCache.impl.getCurrentNode().getCommonNames().get(0);
			else
				commonName = "[No Common Names]";

			taxonSummary.add(new HTML("Taxon Name: " + TaxonomyCache.impl.getCurrentNode().getFullName() + "<br />"));
			taxonSummary.add(new HTML("Status: " + TaxonomyCache.impl.getCurrentNode().getStatus() + "<br />"));
			taxonSummary.add(new HTML("Common Name: " + commonName + "<br />"));
			taxonSummary.add(new HTML("Level: "
					+ TaxonNode.getDisplayableLevel(TaxonomyCache.impl.getCurrentNode().getLevel())));
			// contained.layout();
		} else
			taxonSummary.add(new HTML("No Taxon Selected"));

		assessmentSummary.clear();
		assessmentSummary.setStyleName("hasSmallHTML");

		AssessmentData data = AssessmentCache.impl.getCurrentAssessment();
		if (data != null) {
			String text = "";
			if (data.getType().equalsIgnoreCase(BaseAssessment.DRAFT_ASSESSMENT_STATUS)) {
				text += "Draft";
			} else if (data.getType().equalsIgnoreCase(BaseAssessment.PUBLISHED_ASSESSMENT_STATUS)) {
				text += "Published";
			} else
				text += "Mine";
			if (data.isRegional()) {
				text += " -- " + RegionCache.impl.getRegionName(data.getRegionIDs());
				if (data.isEndemic()) {
					text += " -- Endemic";
				}
			} else
				text += " -- Global";
			assessmentSummary.add(new HTML(text + "<br />"));
			assessmentSummary.add(new HTML("Assessed Date: " + data.getDateAssessed() + "<br />"));
			assessmentSummary.add(new HTML("Last Modified: " + (data.getDateModified() != 0 ?
					FormattedDate.impl.getDate(new Date(data.getDateModified())) + "<br />" : "N/A<br />")));

			String cat = data.getProperCategoryAbbreviation();
			String crit = data.getProperCriteriaString();

			assessmentSummary.add(new HTML("Category: " + cat + "<br />"));
		} else {
			assessmentSummary.add(new HTML("No Assessment Selected"));
		}

		add(workingSetSummary);
		add(taxonSummary);
		add(assessmentSummary);

		layout();
	}

	public void update() {
		fillData();
	}
}
