package org.iucn.sis.client.panels.workingsets;

import org.iucn.sis.client.api.caches.AssessmentCache;
import org.iucn.sis.client.api.caches.AuthorizationCache;
import org.iucn.sis.client.api.utils.FormattedDate;
import org.iucn.sis.client.container.SimpleSISClient;
import org.iucn.sis.client.panels.ClientUIContainer;
import org.iucn.sis.client.panels.PanelManager;
import org.iucn.sis.shared.api.acl.base.AuthorizableObject;
import org.iucn.sis.shared.api.assessments.AssessmentFetchRequest;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.utils.AssessmentFormatter;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.InfoConfig;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.util.extjs.client.WindowUtils;

public class WorkingSetAssessmentPanel extends LayoutContainer {

	private Integer lastID = null;
	private ToolBar toolbar = null;
	private HTML title = null;
	private HTML dateCreated = null;
	private HTML assessors = null;
	private HTML status = null;
	private HTML evaluators = null;
	private boolean show = false;

	public WorkingSetAssessmentPanel(PanelManager manager) {
		super();
		build();

		hide();
	}

	private void build() {
		setBorders(true);

		FlexTable table = new FlexTable();
		buildToolBar();
		add(toolbar);
		toolbar.setWidth("100%");

		title = new HTML();
		title.addStyleName("color-dark-blue");
		title.addStyleName("bold");
		table.setWidget(0, 0, title);
		table.getFlexCellFormatter().setColSpan(0, 0, 2);

		HTML item = new HTML("Date Created: ");
		table.setWidget(1, 0, item);
		dateCreated = new HTML();
		table.setWidget(1, 1, dateCreated);

		item = new HTML("Assessors : ");
		table.setWidget(2, 0, item);
		assessors = new HTML();
		table.setWidget(2, 1, assessors);

		item = new HTML("Evaluators : ");
		table.setWidget(3, 0, item);
		evaluators = new HTML();
		table.setWidget(3, 1, evaluators);

		item = new HTML("Status : ");
		table.setWidget(4, 0, item);
		status = new HTML();
		table.setWidget(4, 1, status);

		// setSize("100%", "100%");
		add(table);
	}

	private void buildToolBar() {
		toolbar = new ToolBar();
		Button item = new Button();
		item.setIconStyle("icon-go-jump");
		item.setText("View");
		item.setToolTip("View in Assessment Browser");
		item.addSelectionListener(new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				Assessment assessment = AssessmentCache.impl.getDraftAssessment(lastID, false);
				if ( assessment != null) {
					if( AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, AuthorizableObject.READ, assessment ) ) {
						AssessmentCache.impl.setCurrentAssessment(assessment);
						ClientUIContainer.bodyContainer.setSelection(
							ClientUIContainer.bodyContainer.tabManager.assessmentEditor);
					} else {
						WindowUtils.errorAlert("Insufficient Rights", "Sorry, you do not have permission to read this assessment.");
					}
				} else {
					Info.display(new InfoConfig("Error", "Error loading assessment browser"));
				}
			}
		});
		toolbar.add(item);
	}

	private String displayText(String text) {
		if (text == null || text.trim().equals(""))
			return "N/A";
		else
			return text;
	}

	private void getDraftAssessment() {
		AssessmentCache.impl.fetchAssessments(new AssessmentFetchRequest(null, lastID), new GenericCallback<String>() {
			public void onFailure(Throwable caught) {
				// setVisible(false);
				hide();
			}

			public void onSuccess(String arg0) {
				refreshAssessmentInfo(lastID);

			}
		});
	}

	@Override
	protected void onLoad() {
		super.onLoad();
		setVisible(show);
	}

	private void refreshAssessmentInfo(Integer lastID) {

		// TODO: CHANGE TO DISPLAY ALL OF THEM!
		Assessment assessment = AssessmentCache.impl.getDraftAssessment(lastID, false);
		if (assessment == null) {
			title.setText("No Draft Assessment Exists");
			dateCreated.setText("");
			assessors.setText("");
			evaluators.setText("");
			status.setText("");
		} else if( AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, AuthorizableObject.READ, assessment ) ) {
			title.setText("Draft Assessment");
			dateCreated.setText(displayText(FormattedDate.impl.getDate(assessment.getDateAssessed())));
			assessors.setText(displayText(AssessmentFormatter.getDisplayableAssessors(assessment)));
			evaluators.setText(displayText(AssessmentFormatter.getDisplayableEvaluators(assessment)));
			status.setText(displayText(assessment.getCategoryAbbreviation()));
		} else {
			title.setText("Insufficient permission to read this assessment.");
			dateCreated.setText("");
			assessors.setText("");
			evaluators.setText("");
			status.setText("");
		}

		layout();
	}

	/**
	 * draftID = the id of the draft assessment to display if draftID = null,
	 * screen should become invisible
	 * 
	 * @param draftID
	 */
	public void refreshPanel(Integer draftID) {
		if (draftID == null) {
			show = false;
			setVisible(show);
			hide();
		} else {
			show = true;
			setVisible(show);
			show();

			if ((draftID != null) && ((lastID == null && draftID != null) || (!draftID.equals(lastID)))) {
				lastID = draftID;
				getDraftAssessment();
			}
		}
	}

}
