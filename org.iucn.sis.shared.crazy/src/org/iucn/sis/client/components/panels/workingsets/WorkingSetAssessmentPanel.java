package org.iucn.sis.client.components.panels.workingsets;

import org.iucn.sis.client.acl.AuthorizationCache;
import org.iucn.sis.client.components.ClientUIContainer;
import org.iucn.sis.client.components.panels.PanelManager;
import org.iucn.sis.client.data.assessments.AssessmentCache;
import org.iucn.sis.client.data.assessments.AssessmentFetchRequest;
import org.iucn.sis.client.simple.SimpleSISClient;
import org.iucn.sis.shared.acl.base.AuthorizableObject;
import org.iucn.sis.shared.data.assessments.AssessmentData;

import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.InfoConfig;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.solertium.lwxml.gwt.debug.SysDebugger;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.util.extjs.client.WindowUtils;

public class WorkingSetAssessmentPanel extends LayoutContainer {

	private PanelManager manager = null;
	private String lastID = null;
	private ToolBar toolbar = null;
	private HTML title = null;
	private HTML dateCreated = null;
	private HTML assessors = null;
	private HTML status = null;
	private HTML evaluators = null;
	private boolean show = false;
	private boolean first;

	public WorkingSetAssessmentPanel(PanelManager manager) {
		super();
		this.manager = manager;
		build();

		hide();
		first = true;
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
		item.addListener(Events.Select, new Listener() {
			public void handleEvent(BaseEvent be) {
				AssessmentData assessment = AssessmentCache.impl.getDraftAssessment(lastID, false);
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
		SysDebugger.getInstance().println("I am in get draft assessment with id " + lastID);
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

	private void refreshAssessmentInfo(String lastID) {

		// TODO: CHANGE TO DISPLAY ALL OF THEM!
		AssessmentData assessment = AssessmentCache.impl.getDraftAssessment(lastID, false);
		if (assessment == null) {
			title.setText("No Draft Assessment Exists");
			dateCreated.setText("");
			assessors.setText("");
			evaluators.setText("");
			status.setText("");
		} else if( AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, AuthorizableObject.READ, assessment ) ) {
			title.setText("Draft Assessment");
			dateCreated.setText(displayText(assessment.getDateAdded()));
			assessors.setText(displayText(assessment.getAssessors()));
			evaluators.setText(displayText(assessment.getEvaluators()));
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
	public void refreshPanel(String draftID) {
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

	// public void setVisible(boolean visible) {
	// SysDebugger.getInstance().println("Setting visible with show = " + show +
	// " and visible = " + visible);
	// if (visible != show){
	// SysDebugger.getInstance().println("I am different");
	// }
	//		
	// super.setVisible(show);
	// }

}
