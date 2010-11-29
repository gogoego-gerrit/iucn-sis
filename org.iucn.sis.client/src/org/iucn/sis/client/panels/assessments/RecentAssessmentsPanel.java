package org.iucn.sis.client.panels.assessments;

import java.util.List;

import org.iucn.sis.client.api.caches.AssessmentCache;
import org.iucn.sis.client.api.caches.AssessmentCache.AssessmentInfo;
import org.iucn.sis.client.panels.PanelManager;
import org.iucn.sis.client.panels.utils.RefreshPortlet;
import org.iucn.sis.shared.api.assessments.AssessmentFetchRequest;
import org.iucn.sis.shared.api.models.AssessmentType;

import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.HTMLTable.Cell;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.util.extjs.client.WindowUtils;

public class RecentAssessmentsPanel extends RefreshPortlet {

	// private LayoutContainer content;
	private List<AssessmentInfo> recentAssessments;
	private boolean loaded;

//	private ArrayList assessments = null;

	public RecentAssessmentsPanel(PanelManager manager) {
		super("x-panel");
		setLayout(new FitLayout());
		loaded = false;
		setLayoutOnChange(true);
	}

	private void fetchAndDrawPanel() {
		removeAll();
		recentAssessments = AssessmentCache.impl.getRecentAssessments();
		
		if (recentAssessments == null || recentAssessments.isEmpty() ) {
			add(new HTML("You have no recent assessments."));
		} else {
			int row = 0;
			
			final Grid grid = new Grid(recentAssessments.size() + 1, 3);
			grid.getColumnFormatter().addStyleName(1, "right");
			grid.getColumnFormatter().addStyleName(2, "right");
			grid.setWidget(row, 0, new HTML("Species"));
			grid.setWidget(row, 1, new HTML("Type"));
			grid.setWidget(row, 2, new HTML("Region"));
			grid.getRowFormatter().addStyleName(0, "hasBoldHTML");
			grid.getRowFormatter().addStyleName(0, "hasUnderlinedHTML");

			row++;
			
			for (AssessmentInfo curInfo : recentAssessments) {
				grid.setText(row, 0, curInfo.name);
				
				final String text;
				if (AssessmentType.DRAFT_ASSESSMENT_TYPE.equals(curInfo.type))
					text = "Draft";
				else if (AssessmentType.PUBLISHED_ASSESSMENT_TYPE.equals(curInfo.type))
					text = "Published";
				else
					text = "Mine";
				grid.setText(row, 1, text);

				grid.setText(row, 2, curInfo.region);
								
				grid.getRowFormatter().addStyleName(row, "pointerCursor");
				
				row++;
			}

			grid.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					Cell cell = grid.getCellForEvent(event);
					if (cell != null && cell.getRowIndex() != 0) {
						AssessmentInfo clicked = recentAssessments.get(cell.getRowIndex() - 1);
						setAsCurrentAssessment(clicked.id, clicked.type);
					}
				}
			});
			grid.setWidth("100%");
			
			add(grid);
		}
	}

	@Override
	public void refresh() {
		if (loaded) {
			fetchAndDrawPanel();
		} else {
			update();
		}
	}

	public void setAsCurrentAssessment(final Integer id, final String status) {
		WindowUtils.showLoadingAlert("Loading...");
		AssessmentCache.impl.fetchAssessments(new AssessmentFetchRequest(id), new GenericCallback<String>() {
			public void onFailure(Throwable caught) {
				WindowUtils.hideLoadingAlert();
			}

			public void onSuccess(String arg0) {
				AssessmentCache.impl.getAssessment(id, true);
				WindowUtils.hideLoadingAlert();
			}
		});
	}

	public void update() {
		loaded = true;

		AssessmentCache.impl.loadRecentAssessments(new GenericCallback<String>() {
			public void onFailure(Throwable caught) {
				removeAll();
				add(new HTML("Unable to load recent assessments, please check your internet connection."));
			}

			public void onSuccess(String arg0) {
				loaded = true;
				
				fetchAndDrawPanel();
			}
		});

		setHeading("Recent Assessments Panel");
	}

}
