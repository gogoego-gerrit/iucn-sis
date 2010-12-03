package org.iucn.sis.client.panels.dem;

import org.iucn.sis.client.api.assessment.AssessmentClientSaveUtils;
import org.iucn.sis.client.api.caches.AssessmentCache;
import org.iucn.sis.client.api.caches.AuthorizationCache;
import org.iucn.sis.client.container.SimpleSISClient;
import org.iucn.sis.client.panels.PanelManager;
import org.iucn.sis.client.panels.dem.DEMToolbar.EditStatus;
import org.iucn.sis.client.panels.dem.ViewDisplay.PageChangeRequest;
import org.iucn.sis.shared.api.acl.InsufficientRightsException;
import org.iucn.sis.shared.api.acl.UserPreferences;
import org.iucn.sis.shared.api.acl.base.AuthorizableObject;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.InfoConfig;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.layout.AccordionLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.HTML;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.util.events.ComplexListener;
import com.solertium.util.events.SimpleListener;
import com.solertium.util.extjs.client.WindowUtils;
import com.solertium.util.gwt.ui.DrawsLazily;

/**
 * Shows an assessment in the following steps:
 * 
 * 1) Based on the view and page selected, determines the displays needed by
 * canonical name.
 * 
 * 2) Fetches the Widget from the FieldWidgetCache and the current assessment
 * 
 * 3) Clears the contents of the Widgets and fills them with data from the
 * current assessment, if applicable
 * 
 * @author adam.schwartz
 * 
 */
public class DEMPanel extends LayoutContainer {

	private boolean viewOnly = false;
	private LayoutContainer scroller;
	private BorderLayoutData scrollerData;

	private AccordionLayout viewChooser;
	private ViewDisplay viewWrapper;

	private BorderLayoutData viewWrapperData;
	private DEMToolbar toolBar;

	private BorderLayoutData toolBarData;

	private PanelManager panelManager = null;


	public DEMPanel(PanelManager manager) {
		BorderLayout layout = new BorderLayout();
		setLayout(layout);

		panelManager = manager;

		viewChooser = new AccordionLayout();

		viewWrapper = new ViewDisplay();
		viewWrapper.setLayout(viewChooser);
		viewWrapper.setPageChangelistener(new ComplexListener<PageChangeRequest>() {
			public void handleEvent(PageChangeRequest eventData) {
				changePage(eventData);
			}
		});
		viewWrapperData = new BorderLayoutData(LayoutRegion.WEST, .18f, 20, 300);

		scroller = new LayoutContainer();
		scroller.setLayout(new FitLayout());
		scroller.setScrollMode(Scroll.NONE);

		scrollerData = new BorderLayoutData(LayoutRegion.CENTER, .82f, 300, 3000);

		toolBar = buildToolBar();
		toolBarData = new BorderLayoutData(LayoutRegion.NORTH);
		toolBarData.setSize(25);

		add(toolBar, toolBarData);
		add(scroller, scrollerData);
		add(viewWrapper, viewWrapperData);
	}

	private DEMToolbar buildToolBar() {
		DEMToolbar toolbar = new DEMToolbar(panelManager);
		toolbar.setRefreshListener(new ComplexListener<EditStatus>() {
			public void handleEvent(EditStatus eventData) {
				viewOnly = EditStatus.EDIT_DATA.equals(eventData);
				redraw();
			}
		});
		toolbar.build();
		return toolbar;
	}
	
	public void redraw() {
		if (AssessmentCache.impl.getCurrentAssessment() == null) {
			Info.display(new InfoConfig("No Assessment", "Please select an assessment first."));
			return;
		}
		
		viewWrapper.draw(new DrawsLazily.DoneDrawingCallback() {
			public void isDrawn() {
				final HTML label = viewWrapper.getLastSelected();
				if (label != null) {
					//If the user had a view in play, we want to 
					//show it again.
					
					/*SISView currentView = ViewCache.impl.getCurrentView();
					SISPageHolder page = currentView.getCurPage();
					
					changePage(
						new PageChangeRequest(currentView, page, label));*/
				}
				else
					clearDEM();
				
				layout();
			}
		});
	}
	

	public void clearDEM() {
		if (ViewCache.impl.getCurrentView() != null && ViewCache.impl.getCurrentView().getCurPage() != null)
			ViewCache.impl.getCurrentView().getCurPage().removeMyFields();

		scroller.removeAll();
	}
	
	private void changePage(final ViewDisplay.PageChangeRequest request) {
		onBeforePageChange(request, new SimpleListener() {
			public void handleEvent() {
				if (!AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, AuthorizableObject.WRITE, AssessmentCache.impl
						.getCurrentAssessment())) {
					Info.display("Insufficient Permissions", "NOTICE: You do not have "
							+ "permission to save modifications to this assessment.");
					viewOnly = true;
				}
				
				clearDEM();
				
				toolBar.setViewOnly(viewOnly);
				toolBar.resetAutosaveTimer();
				
				scroller.add(new HTML("Loading..."));
				
				updateBoldedPage(request.getLabel());
				
				DeferredCommand.addPause();
				DeferredCommand.addCommand(new Command() {
					public void execute() {
						int page = request.getView().getPages().indexOf(request.getPage());
						
						ViewCache.impl.showPage(request.getView().getId(), page, viewOnly, new DrawsLazily.DoneDrawingCallbackWithParam<TabPanel>() {
							public void isDrawn(TabPanel parameter) {
								scroller.removeAll();
								scroller.add(parameter);
								scroller.layout();
							}	
						});
					}
				});
			}
		});
	}

	private void onBeforePageChange(final PageChangeRequest request, final SimpleListener listener) {
		if (AssessmentCache.impl.getCurrentAssessment() == null) {
			Info.display(new InfoConfig("No Assessment", "Please select an assessment first."));
			return;
		}

		boolean samePage = false;
		try {
			samePage = ViewCache.impl.getCurrentView().getCurPage().equals(request.getPage());
		} catch (Exception somethingsNull) {
		}
		
		if (samePage)
			return;
		
		boolean hasEditablePageWithFields = 
			!viewOnly && ViewCache.impl.getCurrentView() != null && ViewCache.impl.getCurrentView().getCurPage() != null;
		
		if (hasEditablePageWithFields && AssessmentClientSaveUtils.shouldSaveCurrentAssessment(
				ViewCache.impl.getCurrentView().getCurPage().getMyFields())) {
			stopAutosaveTimer();

			String savePreference = 
				SimpleSISClient.currentUser.getPreference(UserPreferences.AUTO_SAVE, UserPreferences.PROMPT);
			
			if (savePreference.equals(UserPreferences.DO_ACTION))
				doSaveCurrentAssessment(listener);
			else if (savePreference.equals(UserPreferences.IGNORE)) {
				listener.handleEvent();
			}
			else {
				WindowUtils.confirmAlert("By the way...", "Navigating away from this page will"
						+ " revert unsaved changes. Would you like to save?", new WindowUtils.SimpleMessageBoxListener() {
					public void onYes() {
						toolBar.startAutosaveTimer();

						WindowUtils.showLoadingAlert("Saving assessment...");
						doSaveCurrentAssessment(listener);
					}
					public void onNo() {
						listener.handleEvent();		
					}
				});
			}
		} else {
			listener.handleEvent();
		}
	}

	private void doSaveCurrentAssessment(final SimpleListener callback) {
		try {
			AssessmentClientSaveUtils.saveAssessment(ViewCache.impl.getCurrentView().getCurPage().getMyFields(), 
					AssessmentCache.impl.getCurrentAssessment(), new GenericCallback<Object>() {
				public void onFailure(Throwable arg0) {
					WindowUtils.hideLoadingAlert();
					WindowUtils.errorAlert("Save Failed", "Failed to save assessment! " + arg0.getMessage());
				}

				public void onSuccess(Object arg0) {
					WindowUtils.hideLoadingAlert();
					Info.display("Save Complete", "Successfully saved assessment {0}.", AssessmentCache.impl
							.getCurrentAssessment().getSpeciesName());

					callback.handleEvent();
				}
			});
		} catch (InsufficientRightsException e) {
			WindowUtils.errorAlert("Auto-save failed. You do not have sufficient " + "rights to perform this action.");
		}
	}

	public void stopAutosaveTimer() {
		toolBar.stopAutosaveTimer();
	}

	protected void onDetach() {
		stopAutosaveTimer();
		super.onDetach();
	}

	protected void onHide() {
		stopAutosaveTimer();
		super.onHide();
	}

	private void updateBoldedPage(final HTML curPageLabel) {
		viewWrapper.selectPage(curPageLabel);
		viewWrapper.layout();
	}
	
	/*public void updateWorkflowStatus() {
		try {
			if (!WorkflowStatus.DRAFT.equals(WorkingSetCache.impl.getCurrentWorkingSet().getWorkflowStatus()))
				workflowStatus.setText("Submission Status: " + 
					WorkingSetCache.impl.getCurrentWorkingSet().getWorkflowStatus() 
				);
		} catch (Exception e) {
			workflowStatus.setText("");
		}
	}*/
}

