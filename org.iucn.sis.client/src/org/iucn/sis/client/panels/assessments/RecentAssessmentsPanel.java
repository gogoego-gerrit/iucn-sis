package org.iucn.sis.client.panels.assessments;

import java.util.ArrayList;
import java.util.List;

import org.iucn.sis.client.api.caches.AssessmentCache;
import org.iucn.sis.client.api.caches.RegionCache;
import org.iucn.sis.client.api.caches.AssessmentCache.AssessmentInfo;
import org.iucn.sis.client.panels.PanelManager;
import org.iucn.sis.client.panels.utils.RefreshPortlet;
import org.iucn.sis.shared.api.assessments.AssessmentFetchRequest;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.AssessmentType;

import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.SourcesTableEvents;
import com.google.gwt.user.client.ui.TableListener;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.solertium.lwxml.shared.GenericCallback;

public class RecentAssessmentsPanel extends RefreshPortlet {

	private PanelManager panelManager = null;
	// private LayoutContainer content;
	private List<AssessmentInfo> recentAssessments;
	private boolean loaded;

//	private ArrayList assessments = null;

	public RecentAssessmentsPanel(PanelManager manager) {
		super("x-panel");
		panelManager = manager;
		setLayout(new FitLayout());
		loaded = false;
		setLayoutOnChange(true);
	}

	private void fetchAndDrawPanel() {

		final VerticalPanel fileList = new VerticalPanel();

		recentAssessments = AssessmentCache.impl.getRecentAssessments();
		if (recentAssessments == null || recentAssessments.size() == 0 ) {
			removeAll();
			fileList.add(new HTML("You have no recent assessments."));
			add(fileList);
		} else {
			if (recentAssessments.size() > 0) {
				removeAll();
				
				List<Integer> uids = new ArrayList<Integer>();
				for( AssessmentInfo curRecent : recentAssessments )
					uids.add(curRecent.id);
				
				AssessmentCache.impl.fetchAssessments(new AssessmentFetchRequest(uids, (List<Integer>)null), new GenericCallback<String>() {
					public void onFailure(Throwable caught) {
						fileList.add(new HTML("Failed to fetch your recent assessments."));
						add(fileList);
					}

					public void onSuccess(String arg0) {
						Grid grid = new Grid(recentAssessments.size() + 1, 3);
						grid.getColumnFormatter().addStyleName(1, "right");
						grid.getColumnFormatter().addStyleName(2, "right");
						grid.setWidget(0, 0, new HTML("Species"));
						grid.setWidget(0, 1, new HTML("Type"));
						grid.setWidget(0, 2, new HTML("Region"));
						grid.getRowFormatter().addStyleName(0, "hasBoldHTML");
						grid.getRowFormatter().addStyleName(0, "hasUnderlinedHTML");

						for (int j = 0; j < recentAssessments.size(); j++) {
							AssessmentInfo curInfo = recentAssessments.get(j);
							Assessment curAss = AssessmentCache.impl.getAssessment(curInfo.id, false);
							if (curAss != null) {
								grid.setText(j + 1, 0, curAss.getSpeciesName());

								String text = "";
								if (curAss.getType().equalsIgnoreCase(AssessmentType.DRAFT_ASSESSMENT_TYPE)) {
									text = "Draft";
								} else if (curAss.getType().equalsIgnoreCase(AssessmentType.PUBLISHED_ASSESSMENT_TYPE)) {
									text = "Published";
								} else
									text = "Mine";
								grid.setText(j + 1, 1, text);

								if (curAss.isRegional()) {
									text = RegionCache.impl.getRegionName(curAss.getRegionIDs());
									if (curAss.isEndemic()) {
										text += " -- Endemic";
									}
								} else
									text = "Global";
								grid.setText(j + 1, 2, text);
								grid.getRowFormatter().addStyleName(j + 1, "pointerCursor");
							}
							
						}

						grid.addTableListener(new TableListener() {
							public void onCellClicked(SourcesTableEvents sender, int row, int cell) {
								if (row != 0) {
									AssessmentInfo clicked = recentAssessments.get(row - 1);
									setAsCurrentAssessment(clicked.id, clicked.status);
								}

							}
						});
						grid.setWidth("100%");

						fileList.clear();
						fileList.add(grid);

						fileList.setWidth("100%");
						removeAll();
						add(fileList);
					}
				});
			} else {
				removeAll();
				fileList.add(new HTML("You have no recent assessments."));
				add(fileList);
			}
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
		AssessmentCache.impl.fetchAssessments(new AssessmentFetchRequest(id), new GenericCallback<String>() {
			public void onFailure(Throwable caught) {
			}

			public void onSuccess(String arg0) {
				AssessmentCache.impl.getAssessment(id, true);
			}
		});
	}

	public void update() {
		loaded = true;
//		assessments = new ArrayList();

		AssessmentCache.impl.loadRecentAssessments(new GenericCallback<String>() {
			public void onFailure(Throwable caught) {
				removeAll();
				add(new HTML("Unable to load recentAssessments, please check your internet connection"));
			}

			public void onSuccess(String arg0) {
				removeAll();
				loaded = true;
				recentAssessments = AssessmentCache.impl.getRecentAssessments();
				if (recentAssessments != null) {
					fetchAndDrawPanel();
				}
			}
		});

		setHeading("Recent Assessments Panel");
	}

}
