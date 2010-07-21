package org.iucn.sis.client.components.panels;

import org.iucn.sis.client.simple.SimpleSISClient;
import org.iucn.sis.client.ui.RefreshPortlet;

import com.google.gwt.user.client.ui.HTML;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.NativeNodeList;

public class AssessmentsReviewPanel extends RefreshPortlet {

	PanelManager panelManager = null;

	public AssessmentsReviewPanel(PanelManager manager) {
		super("x-panel");
		setHeading("Assessments to Review");
		panelManager = manager;
		fetch();
	}

	private void buildList(NativeDocument doc) {
		removeAll();

		NativeNodeList list = doc.getDocumentElement().getElementsByTagName("assessment");

		if (list.getLength() <= 0) {
			add(new HTML("You have no assessments to review."));
			layout();
			return;
		}

//		final ArrayList ids = new ArrayList();
//		StringBuffer csv = new StringBuffer();
//		for (int i = 0; i < list.getLength(); i++) {
//			String id = list.elementAt(i).getText();
//			if (!ids.contains(id)) {
//				ids.add(id);
//				csv.append(id + ",");
//			}
//		}
//
//		csv.replace(csv.length() - 1, csv.length(), "");
//		AssessmentCache.impl.fetchDraftList(csv.toString(), false, new GenericCallback<String>() {
//			public void onFailure(Throwable caught) {
//				add(new HTML("Unable to get assessments."));
//				layout();
//			}
//
//			public void onSuccess(String arg0) {
//				Grid grid = new Grid(ids.size() + 1, 3);
//				grid.setHTML(0, 0, "Species Name");
//				grid.setHTML(0, 1, "Evaluators");
//				grid.setHTML(0, 2, "Status");
//				grid.getRowFormatter().addStyleName(0, "bold");
//				grid.getRowFormatter().addStyleName(0, "underlined");
//				for (int i = 0; i < ids.size(); i++) {
//					AssessmentData assess = AssessmentCache.impl.getDraftAssessment( (String) ids.get(i), false);
//					SysDebugger.getInstance().println("This is the id " + ids.get(i));
//					grid.setWidget(i + 1, 0, new HTML(assess.getSpeciesName()));
//					if (assess.getEvaluators().trim().equals(""))
//						grid.setWidget(i + 1, 1, new HTML("N/A"));
//					else
//						grid.setWidget(i + 1, 1, new HTML(assess.getEvaluators(), true));
//					grid.setWidget(i + 1, 2, new HTML(assess.getCategoryAbbreviation(), false));
//					grid.getRowFormatter().setVerticalAlign(i + 1, HasVerticalAlignment.ALIGN_TOP);
//					grid.getRowFormatter().addStyleName(i + 1, "pointerCursor");
//				}
//				// grid.setSize(getWidth()+"px", getHeight()+"px");
//				grid.getColumnFormatter().setWidth(0, "50%");
//				grid.getColumnFormatter().setWidth(1, "35%");
//				grid.getColumnFormatter().setWidth(2, "15%");
//				grid.addTableListener(new TableListener() {
//					public void onCellClicked(SourcesTableEvents sender, int row, int cell) {
//						if (row > 0) {
//							String id = (String) ids.get(row - 1);
//
//							// AssessmentCache.impl.fetchDraftAssessment(id,
//							// true, new GenericCallback<String>(){
//							// public void onFailure(Throwable caught) {};
//							// public void onSuccess(String arg0) {};
//							// });
//						}
//					}
//				});
//
//				removeAll();
//				add(grid);
//				layout();
//			}
//		});
	}

	private void fetch() {
		final NativeDocument ndoc = SimpleSISClient.getHttpBasicNativeDocument();
		ndoc.load("/assessmentReview/" + SimpleSISClient.currentUser.username + "/", new GenericCallback<String>() {
			public void onFailure(Throwable caught) {
				removeAll();
				add(new HTML("You have no assessments needing review."));
				layout();
			}

			public void onSuccess(String arg0) {
				buildList(ndoc);
				layout();
			}
		});
	}

	@Override
	public void refresh() {
		fetch();
	}

	public void update() {
		fetch();
	}
}
