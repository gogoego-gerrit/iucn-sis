package org.iucn.sis.client.panels.workingsets;

import java.util.Set;

import org.iucn.sis.client.api.caches.AssessmentCache;
import org.iucn.sis.client.api.caches.WorkingSetCache;
import org.iucn.sis.client.api.utils.FormattedDate;
import org.iucn.sis.client.panels.PanelManager;
import org.iucn.sis.client.panels.filters.AssessmentFilterPanel;
import org.iucn.sis.client.panels.utils.RefreshLayoutContainer;
import org.iucn.sis.client.panels.workingsets.WorkingSetTaxaList.TaxaData;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.WorkingSet;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;

public class WorkingSetSummaryPanel extends RefreshLayoutContainer {

	private FlexTable grid = null;
	private LayoutContainer gridContainer = null;

	private HTML managerHTML = null;
	private HTML dateCreatedHTML = null;
	private HTML workingSetNameHTML = null;
	private HTML descriptionHTML = null;
	private HTML notes = null;
	private HTML people = null;
	private HTML filterHTML = null;
	private WorkingSetTaxaList taxaList = null;
	private WorkingSetAssessmentPanel assessmentPanel = null;

	public WorkingSetSummaryPanel() {
		super();
		build();
		addStyleName("gwt-background");
		setScrollMode(Scroll.AUTO);
	}

	private void build() {
		RowLayout layout = new RowLayout();
		setLayout(layout);
		buildGrid();
		buildTaxaList();
	}

	private void buildGrid() {
		grid = new FlexTable();
		grid.setCellSpacing(10);

		HTML html = new HTML("Working Set Name:  ");
		html.addStyleName("color-dark-blue");
		workingSetNameHTML = new HTML();
		grid.setWidget(0, 0, html);
		grid.setWidget(0, 1, workingSetNameHTML);

		html = new HTML("Manager:  ");
		html.addStyleName("color-dark-blue");
		managerHTML = new HTML();
		grid.setWidget(1, 0, html);
		grid.setWidget(1, 1, managerHTML);

		html = new HTML("Date Created:  ");
		html.addStyleName("color-dark-blue");
		dateCreatedHTML = new HTML();
		grid.setWidget(2, 0, html);
		grid.setWidget(2, 1, dateCreatedHTML);
		

		html = new HTML("Associated People:  ");
		html.addStyleName("color-dark-blue");
		people = new HTML();
		grid.setWidget(3, 0, html);
		grid.setWidget(3, 1, people);
		
		html = new HTML("Assessment Scope:  ");
		html.addStyleName("color-dark-blue");
		filterHTML = new HTML();
		grid.setWidget(4, 0, html);
		grid.setWidget(4, 1, filterHTML);

		html = new HTML("Description:  ");
		html.addStyleName("color-dark-blue");
		descriptionHTML = new HTML();
		grid.setWidget(5, 0, html);
		grid.setWidget(5, 1, descriptionHTML);
		descriptionHTML.setSize("100%", "100%");

		html = new HTML("Working Set Notes: ");
		html.addStyleName("color-dark-blue");
		notes = new HTML();
		grid.setWidget(6, 0, html);
		grid.setWidget(6, 1, notes);
		notes.setSize("100%", "100%");

		grid.getColumnFormatter().setWidth(0, "130px");
		grid.getRowFormatter().addStyleName(4, "vertical-align-top");
		grid.getRowFormatter().addStyleName(5, "vertical-align-top");
		grid.getRowFormatter().addStyleName(6, "vertical-align-top");
		grid.setWidth("100%");

		gridContainer = new LayoutContainer(new FitLayout());
		gridContainer.setScrollMode(Scroll.AUTO);
		gridContainer.add(grid);

		add(gridContainer, new RowData(1d, .4));
	}

	private void buildTaxaList() {
		final LayoutContainer bottomContent = new LayoutContainer();
		bottomContent.setLayout(new RowLayout(Orientation.HORIZONTAL));

		taxaList = new WorkingSetTaxaList(false);
		taxaList.setFilterVisible(false);
		taxaList.setBorders(true);
		taxaList.setJumpToToolbar();
		assessmentPanel = new WorkingSetAssessmentPanel();
		Listener<BaseEvent> listener = new Listener<BaseEvent>() {
			public void handleEvent(BaseEvent be) {
				if (taxaList.isFilteredBySpecies()) {
					String selectedID = taxaList.getSelectedInList();
					if (selectedID != null) {
						Set<Assessment> drafts = AssessmentCache.impl.getDraftAssessmentsForTaxon(Integer.valueOf(selectedID));
						for (Assessment assessment : drafts) {
							if (assessment.isGlobal()) {
								assessmentPanel.refreshPanel(assessment.getId());
								assessmentPanel.show();								
							}
						}
					} else
						assessmentPanel.hide();
				} else
					assessmentPanel.hide();
				// bottomContent.layout();
			}
		};

		taxaList.addListener(Events.SelectionChange, listener);
		// taxaList.addListener(Events.Change, listener);

		taxaList.setFilter(TaxaData.ORDER);

		bottomContent.add(taxaList, new RowData(.6f, 1d));
		bottomContent.add(assessmentPanel, new RowData(.4f, 1d));

		add(bottomContent, new RowData(1d, .6));
	}

	private void clearFields() {
		managerHTML.setHTML("");
		descriptionHTML.setHTML("");
		dateCreatedHTML.setHTML("");
		notes.setText("");
		people.setText("");
		filterHTML.setText("");
	}

	private void refreshAssessmentPanel() {
		if (taxaList.isFilteredBySpecies()) {
			String selectedID = taxaList.getSelectedInList();
			if (selectedID != null) {
				Set<Assessment> drafts = AssessmentCache.impl.getDraftAssessmentsForTaxon(Integer.valueOf(selectedID));
				for (Assessment assessment : drafts) {
					if (assessment.isGlobal()) {
						assessmentPanel.refreshPanel(assessment.getId());
						assessmentPanel.show();
					}
				}
			} else
				assessmentPanel.hide();
		} else
			assessmentPanel.hide();
	}
	
	@Override
	public void refresh() {
		final WorkingSet ws = WorkingSetCache.impl.getCurrentWorkingSet();
		clearFields();

		if (ws != null) {

			// SET BASIC INFORMATION
			workingSetNameHTML.setHTML(ws.getWorkingSetName());
			descriptionHTML.setHTML(ws.getDescription());
			dateCreatedHTML.setHTML(FormattedDate.impl.getDate(ws.getCreatedDate()));
			managerHTML.setHTML(ws.getCreatorUsername());
//			people.setHTML(ws.getPeopleAsCSV().replaceAll(",", ", "));
			filterHTML.setHTML(AssessmentFilterPanel.getString(ws.getFilter()));

			// SET NOTES
			notes.setHTML(ws.getNotes());

			// SET TAXA
			/*FIXME if (manager.workingSetOptionsPanel.anyChanges)
				taxaList.forcedRefresh();
			else
				taxaList.refresh();*/
			
			refreshAssessmentPanel();

		}

		// CURRENT WORKING SET IS NULL
		else {
			workingSetNameHTML.setHTML("Please select a working set");
			assessmentPanel.refreshPanel(null);
			taxaList.forcedRefresh();
		}

		layout();
	}

}
