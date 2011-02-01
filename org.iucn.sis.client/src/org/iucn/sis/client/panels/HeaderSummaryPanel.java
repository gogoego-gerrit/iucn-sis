package org.iucn.sis.client.panels;

import java.util.Date;

import org.iucn.sis.client.api.caches.AssessmentCache;
import org.iucn.sis.client.api.caches.RegionCache;
import org.iucn.sis.client.api.caches.TaxonomyCache;
import org.iucn.sis.client.api.caches.WorkingSetCache;
import org.iucn.sis.client.api.utils.FormattedDate;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.AssessmentType;
import org.iucn.sis.shared.api.models.TaxonLevel;
import org.iucn.sis.shared.api.models.WorkingSet;
import org.iucn.sis.shared.api.utils.AssessmentFormatter;

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

			WorkingSet curSet = WorkingSetCache.impl.getCurrentWorkingSet();
			workingSetSummary.add(new HTML("Name: " + curSet.getWorkingSetName() + "<br />"));
			workingSetSummary.add(new HTML("Manager: " + curSet.getCreatorUsername() + "<br />"));
			workingSetSummary.add(new HTML("Created: " + FormattedDate.impl.getDate(curSet.getCreatedDate())));
			// contained.layout();
		} else
			workingSetSummary.add(new HTML("No Working Set Selected"));

		taxonSummary.clear();
		taxonSummary.setStyleName("hasSmallHTML");

		if (TaxonomyCache.impl.getCurrentTaxon() != null) {
			// taxonSummary.add( new HTML("<u>Taxon Summary</u>"));

			String commonName = "";
			if (TaxonomyCache.impl.getCurrentTaxon().getPrimaryCommonName() != null)
				commonName = "" + TaxonomyCache.impl.getCurrentTaxon().getPrimaryCommonName().getName();
			else
				commonName = "[No Common Names]";

			taxonSummary.add(new HTML("Taxon Name: " + TaxonomyCache.impl.getCurrentTaxon().getFullName() + "<br />"));
			taxonSummary.add(new HTML("Status: " + TaxonomyCache.impl.getCurrentTaxon().getTaxonStatus().getName() + "<br />"));
			taxonSummary.add(new HTML("Common Name: " + commonName + "<br />"));
			taxonSummary.add(new HTML("Level: "
					+ TaxonLevel.displayableLevel[TaxonomyCache.impl.getCurrentTaxon().getLevel()]));
			// contained.layout();
		} else
			taxonSummary.add(new HTML("No Taxon Selected"));

		assessmentSummary.clear();
		assessmentSummary.setStyleName("hasSmallHTML");

		Assessment data = AssessmentCache.impl.getCurrentAssessment();
		if (data != null) {
			String text = "";
			if (data.getType().equalsIgnoreCase(AssessmentType.DRAFT_ASSESSMENT_TYPE)) {
				text += "Draft";
			} else if (data.getType().equalsIgnoreCase(AssessmentType.PUBLISHED_ASSESSMENT_TYPE)) {
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
			assessmentSummary.add(new HTML("Assessed Date: " + (data.getDateAssessed() == null ? "(not set)" : FormattedDate.impl.getDate(data.getDateAssessed())) + "<br />"));
			assessmentSummary.add(new HTML("Last Modified: " + (data.getLastEdit() != null ?
					FormattedDate.impl.getDate(data.getLastEdit().getCreatedDate()) + "<br />" : "N/A<br />")));

			String cat = AssessmentFormatter.getProperCategoryAbbreviation(data);

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
