package org.iucn.sis.client.panels.dem;

import java.util.Arrays;
import java.util.List;

import org.iucn.sis.client.api.assessment.AssessmentClientSaveUtils;
import org.iucn.sis.client.api.caches.AssessmentCache;
import org.iucn.sis.client.api.caches.AuthorizationCache;
import org.iucn.sis.client.api.caches.TaxonomyCache;
import org.iucn.sis.client.api.caches.WorkingSetCache;
import org.iucn.sis.client.container.SimpleSISClient;
import org.iucn.sis.client.panels.ClientUIContainer;
import org.iucn.sis.client.panels.PanelManager;
import org.iucn.sis.client.panels.assessments.AssessmentAttachmentPanel;
import org.iucn.sis.client.panels.assessments.AssessmentChangesPanel;
import org.iucn.sis.client.panels.assessments.NewAssessmentPanel;
import org.iucn.sis.client.panels.criteracalculator.ExpertPanel;
import org.iucn.sis.client.panels.images.ImageManagerPanel;
import org.iucn.sis.client.panels.taxomatic.CommonNameDisplay;
import org.iucn.sis.client.panels.taxomatic.TaxonChooser;
import org.iucn.sis.client.panels.taxomatic.TaxonSynonymEditor;
import org.iucn.sis.client.panels.workflow.WorkflowNotesWindow;
import org.iucn.sis.shared.api.acl.InsufficientRightsException;
import org.iucn.sis.shared.api.acl.UserPreferences;
import org.iucn.sis.shared.api.acl.base.AuthorizableObject;
import org.iucn.sis.shared.api.debug.Debug;
import org.iucn.sis.shared.api.integrity.ClientAssessmentValidator;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.AssessmentType;
import org.iucn.sis.shared.api.models.CommonName;
import org.iucn.sis.shared.api.models.TaxonLevel;
import org.iucn.sis.shared.api.workflow.WorkflowStatus;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.InfoConfig;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Popup;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.layout.AccordionLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;
import com.solertium.lwxml.gwt.debug.SysDebugger;
import com.solertium.lwxml.shared.GenericCallback;
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
	private class AutosaveTimer extends Timer {
		public void run() {
			if (WindowUtils.loadingBox != null && WindowUtils.loadingBox.isVisible()) {
				// loading panel is up ... don't shoot!
				resetAutosaveTimer();
				return;
			}

			try {
				if (!ClientUIContainer.bodyContainer.getSelectedItem().equals(
						ClientUIContainer.bodyContainer.tabManager.assessmentEditor))
					// Whoops ... misfire
					return;

				boolean save = currentView != null && currentView.getCurPage() != null &&
					AssessmentClientSaveUtils.shouldSaveCurrentAssessment(currentView.getCurPage().getMyFields());
				if (save) {
					AssessmentClientSaveUtils.saveAssessment(currentView.getCurPage().getMyFields(),
							AssessmentCache.impl.getCurrentAssessment(), new GenericCallback<Object>() {
						public void onFailure(Throwable arg0) {
							WindowUtils.errorAlert("Save Failed", "Failed to save assessment! " + arg0.getMessage());
							startAutosaveTimer();
						}

						public void onSuccess(Object arg0) {
							Info.display("Auto-save Complete", "Successfully auto-saved assessment {0}.",
									AssessmentCache.impl.getCurrentAssessment().getSpeciesName());
							startAutosaveTimer();
						}
					});
				} else {
					startAutosaveTimer();
				}
			} catch (InsufficientRightsException e) {
				WindowUtils.errorAlert("Auto-save failed. You do not have sufficient "
						+ "rights to perform this action.");
			} catch (NullPointerException e1) {
				Debug.println(
						"Auto-save failed, on NPE. Probably logged " + "out and didn't stop the timer. {0}", e1);
			}

		}
	}

	private boolean built;

	private boolean viewOnly = false;
	private LayoutContainer scroller;
	private BorderLayoutData scrollerData;

	private SISView currentView;
	private AccordionLayout viewChooser;
	private LayoutContainer viewWrapper;

	private BorderLayoutData viewWrapperData;
	private ToolBar toolBar;

	private BorderLayoutData toolBarData;

	private PanelManager panelManager = null;

	private Assessment lastAssessmentShown = null;
	private AutosaveTimer autoSave = null;

	private int autoSaveInterval = 2 * 60 * 1000;

	private HTML lastSelected = null;

	private Button editViewButton = null;
	private Button workflowStatus = null;

	public DEMPanel(PanelManager manager) {
		BorderLayout layout = new BorderLayout();
		// layout.setMargin(0);
		// layout.setSpacing(0);
		setLayout(layout);

		panelManager = manager;

		built = false;
		autoSave = new AutosaveTimer();

		build();
	}

	public void build() {
//		setVisible(false);
		viewChooser = new AccordionLayout();

		viewWrapper = new LayoutContainer();
		viewWrapper.setLayout(viewChooser);
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

		ViewCache.impl.fetchViews(new GenericCallback<String>() {
			public void onFailure(Throwable caught) {
				caught.printStackTrace();
			}

			public void onSuccess(String arg0) {
				try {
					buildViewChooser();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

//		setVisible(true);
		built = true;
	}

	private ToolBar buildToolBar() {
		ToolBar toolbar = new ToolBar();

		editViewButton = new Button();
		if (!viewOnly) {
			editViewButton.setText("Edit Data Mode");
			editViewButton.setIconStyle("icon-unlocked");
		} else {
			editViewButton.setText("Read Only Mode");
			editViewButton.setIconStyle("icon-read-only");
		}

		editViewButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				Assessment cur = AssessmentCache.impl.getCurrentAssessment();

				if (cur != null && !AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, AuthorizableObject.WRITE, cur)) {
					WindowUtils.errorAlert("You do not have rights to edit this assessment.");
				} else {
					viewOnly = !viewOnly;

					toggleEditViewButton();
					// changeViewModeAssessmentSummaryPanel(viewOnly);
					redraw();
				}
			}
		});
		if (viewOnly)
			editViewButton.fireEvent(Events.Select);

		toolbar.add(editViewButton);
		toolbar.add(new SeparatorToolItem());

		Button item = new Button();
		item.setText("New");
		item.setIconStyle("icon-new-document");

		item.addSelectionListener(new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {

				if (TaxonomyCache.impl.getCurrentTaxon() == null) {
					WindowUtils.errorAlert("Please select a taxon to create an assessment for.  "
							+ "You can select a taxon using the navigator, the search function, " + " or the browser.");
				}

				else if (TaxonomyCache.impl.getCurrentTaxon().getFootprint().length < TaxonLevel.GENUS) {
					WindowUtils.errorAlert("You must select a species or lower taxa to assess.  "
							+ "You can select a different taxon using the navigator, the search function, "
							+ " or the browser.");
				} else {
					Window shell = WindowUtils.getWindow(false, false, "New "
							+ TaxonomyCache.impl.getCurrentTaxon().getFullName() + " Assessment");
					shell.setLayout(new FillLayout());
					shell.setSize(400, 400);
					shell.add(new NewAssessmentPanel(panelManager));
					shell.show();
				}
			}

		});

		// MenuItem mItem = new MenuItem();
		// mItem.setText("Assess Current Taxon");
		// mItem.setIconStyle("icon-symbolic-link");
		//
		// Menu mainMenu = new Menu();
		// mainMenu.add(mItem);
		// item.setMenu(mainMenu);
		//
		// Menu subMenu = new Menu();
		// MenuItem subMItem = new MenuItem(Style.MENU);
		// subMItem.setIconStyle("icon-copy");
		// subMItem.setText("Using This Data");
		// subMItem.addListener(Events.Select, new Listener() {
		// public void handleEvent(BaseEvent be) {
		// AssessmentCache.impl.createNewUserAssessment(true);
		// viewOnly = false;
		// redraw();
		// }
		// });
		// subMenu.add(subMItem);
		//
		// subMItem = new MenuItem(Style.MENU);
		// subMItem.setIconStyle("icon-copy");
		// subMItem.setText("From Scratch");
		// subMItem.addListener(Events.Select, new Listener() {
		// public void handleEvent(BaseEvent be) {
		// AssessmentCache.impl.createNewUserAssessment(false);
		// viewOnly = false;
		// redraw();
		// }
		// });
		// subMenu.add(subMItem);
		//
		// mItem.setSubMenu(subMenu);

		toolbar.add(item);
		toolbar.add(new SeparatorToolItem());

		item = new Button();
		item.setIconStyle("icon-save");
		item.setText("Save");
		item.addListener(Events.Select, new Listener() {
			public void handleEvent(BaseEvent be) {
				if (AssessmentCache.impl.getCurrentAssessment() == null)
					return;

				try {
					boolean save = ViewCache.impl.getCurrentView() != null && AssessmentClientSaveUtils.shouldSaveCurrentAssessment(
							ViewCache.impl.getCurrentView().getCurPage().getMyFields());

					if (save) {
						stopAutosaveTimer();
						WindowUtils.showLoadingAlert("Saving assessment...");
						AssessmentClientSaveUtils.saveAssessment(ViewCache.impl.getCurrentView().getCurPage().getMyFields(),
								AssessmentCache.impl.getCurrentAssessment(), new GenericCallback<Object>() {
							public void onFailure(Throwable arg0) {
								WindowUtils.hideLoadingAlert();
								layout();
								WindowUtils.errorAlert("Save Failed", "Failed to save assessment! " + arg0.getMessage());
								resetAutosaveTimer();
							}

							public void onSuccess(Object arg0) {
								WindowUtils.hideLoadingAlert();
								Info.display("Save Complete", "Successfully saved assessment {0}.",
										AssessmentCache.impl.getCurrentAssessment().getSpeciesName());
								resetAutosaveTimer();
								ClientUIContainer.headerContainer.update();
							}
						});
					} else {
						WindowUtils.hideLoadingAlert();
						layout();
						Info.display(new InfoConfig("Save not needed", "No changes were made."));
						resetAutosaveTimer();
					}
				} catch (InsufficientRightsException e) {
					WindowUtils.errorAlert("Sorry, but you do not have sufficient rights " + "to perform this action.");
				}
			}
		});
		toolbar.add(item);
		toolbar.add(new SeparatorToolItem());

		item = new Button();
		item.setIconStyle("icon-attachment");
		item.setText("Attachments");
		item.setEnabled(SimpleSISClient.iAmOnline);
		item.addListener(Events.Select, new Listener() {
			public void handleEvent(BaseEvent be) {
				if (!AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, AuthorizableObject.WRITE, TaxonomyCache.impl.getCurrentTaxon())) {
					WindowUtils.errorAlert("Sorry. You do not have sufficient permissions " + "to perform this action.");
					return;
				}

				final AssessmentAttachmentPanel attachPanel = new AssessmentAttachmentPanel(AssessmentCache.impl.getCurrentAssessment().getInternalId());
				attachPanel.draw(new AsyncCallback<String>() {

					public void onSuccess(String result) {

						final Window uploadShell = WindowUtils.getWindow(true, true, "");
						uploadShell.setLayout(new FitLayout());
						uploadShell.setWidth(800);
						uploadShell.setHeight(400);
						uploadShell.setHeading("Attachments");
						uploadShell.add(attachPanel);
						uploadShell.show();
						uploadShell.center();
						uploadShell.layout();

					}

					public void onFailure(Throwable caught) {
						WindowUtils.errorAlert("Server error: Unable to get file attachments for this assessment");				
					}
				});


			}
		});

		toolbar.add(item);

		toolbar.add(new Button());

		item = new Button();
		item.setIconStyle("icon-information");
		item.setText("Summary");

		Menu mainMenu = new Menu();
		item.setMenu(mainMenu);

		MenuItem mItem = new MenuItem();
		mItem.setIconStyle("icon-expert");
		mItem.setText("Quick " + ExpertPanel.titleText + " Result");
		mItem.addListener(Events.Select, new Listener() {
			public void handleEvent(BaseEvent be) {
				if (AssessmentCache.impl.getCurrentAssessment() == null) {
					WindowUtils.infoAlert("Alert", "Please select an assessment first.");
					return;
				}
				if (panelManager.expertPanel != null)
					panelManager.expertPanel.update();

				Window s = WindowUtils.getWindow(true, false, ExpertPanel.titleText);
				s.setLayout(new BorderLayout());
				s.add(new Html("&nbsp"), new BorderLayoutData(LayoutRegion.WEST, 20));
				s.add(new Html("&nbsp"), new BorderLayoutData(LayoutRegion.NORTH, 5));
				s.add(new Html("&nbsp"), new BorderLayoutData(LayoutRegion.SOUTH, 5));
				s.setSize(520, 360);
				s.add(panelManager.expertPanel, new BorderLayoutData(LayoutRegion.CENTER));
				s.show();
				s.center();
			}
		});

		mainMenu.add(mItem);

		toolbar.add(item);
		toolbar.add(new SeparatorToolItem());
		toolbar.add(new SeparatorToolItem());

		item = new Button();
		item.setText("Tools");
		item.setIconStyle("icon-preferences-wrench");

		mainMenu = new Menu();
		item.setMenu(mainMenu);

		mItem = new MenuItem();
		mItem.setText("Edit Common Names");
		mItem.setIconStyle("icon-text-bold");
		mItem.addSelectionListener(new SelectionListener<MenuEvent>() {
			public void componentSelected(MenuEvent ce) {

				if (TaxonomyCache.impl.getCurrentTaxon() == null) {
					Info.display(new InfoConfig("No Taxa Selected", "Please select a taxa first."));
					return;
				}

				if (!AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, AuthorizableObject.WRITE, TaxonomyCache.impl.getCurrentTaxon())) {
					WindowUtils
					.errorAlert("Sorry. You do not have sufficient permissions " + "to perform this action.");
					return;
				}

				final Window s = WindowUtils.getWindow(false, false, "Edit Common Names");
				LayoutContainer data = s;

				ToolBar tBar = new ToolBar();
				Button item = new Button();
				item.setText("New Common Name");
				item.setIconStyle("icon-add");
				// item.setMenu(newMenu);
				item.addSelectionListener(new SelectionListener<ButtonEvent>() {
					public void componentSelected(ButtonEvent ce) {
						s.hide();
						Window addNameBox = CommonNameDisplay.getNewCommonNameDisplay(TaxonomyCache.impl
								.getCurrentTaxon(), null, new GenericCallback<String>() {
							public void onFailure(Throwable arg0) {
							}

							public void onSuccess(String arg0) {
								// update( new Long(
								// TaxonomyCache.impl.getCurrentNode().getId()
								// ).toString() );

							}
						});

						addNameBox.show();
						addNameBox.center();
					}
				});

				tBar.add(item);
				data.add(tBar);
				// Image addName = new Image("images/add.png");
				// addName.setSize("14px", "14px");
				// addName.setTitle("Add New Common Name");
				/*
				 * addName.addClickListener(new ClickListener(){
				 * 
				 * public void onClick(Widget sender) { //add( addNameBox ); }
				 * });
				 */
				HTML commonNamesHeader = new HTML("<b>Common Name --- Language</b>");

				LayoutContainer commonNamePanel = new LayoutContainer();
				// commonNamePanel.add( addName );
				commonNamePanel.add(commonNamesHeader);

				data.add(new HTML("<hr><br />"));
				data.add(commonNamePanel);

				if (TaxonomyCache.impl.getCurrentTaxon().getCommonNames().size() != 0) {
					for (int i = 0; i < TaxonomyCache.impl.getCurrentTaxon().getCommonNames().size(); i++) {
						CommonName curName = (CommonName) TaxonomyCache.impl.getCurrentTaxon().getPrimaryCommonName();
						data.add(new CommonNameDisplay(TaxonomyCache.impl.getCurrentTaxon(), curName)
						.show(new GenericCallback<String>() {
							public void onFailure(Throwable arg0) {
							}

							public void onSuccess(String arg0) {
								// update( new Long(
								// TaxonomyCache.impl.getCurrentNode().
								// getId()
								// ).toString() );
							}
						}));
					}
				} else
					data.add(new HTML("No Common Names."));

				s.setSize(350, 550);
				s.show();
				s.center();
				// TODO edit common names popup
				// DEMToolsPopups.buildBibliographyPopup();
			}
		});
		mainMenu.add(mItem);

		mItem = new MenuItem();
		mItem.setText("Edit Synonyms");
		mItem.setIconStyle("icon-text-bold");
		mItem.addSelectionListener(new SelectionListener<MenuEvent>() {
			public void componentSelected(MenuEvent ce) {
				if (TaxonomyCache.impl.getCurrentTaxon() == null) {
					Info.display(new InfoConfig("No Taxa Selected", "Please select a taxa first."));
					return;
				}

				if (!AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, AuthorizableObject.WRITE, TaxonomyCache.impl.getCurrentTaxon())) {
					WindowUtils
					.errorAlert("Sorry. You do not have sufficient permissions " + "to perform this action.");
					return;
				}

				final Window shell = WindowUtils.getWindow(true, false, "Edit Synonyms");
				TaxonSynonymEditor editor = new TaxonSynonymEditor();
				editor.sinkEvents(Events.Close.getEventCode());
				editor.addListener(Events.Close, new Listener<BaseEvent>() {
					public void handleEvent(BaseEvent be) {
						shell.close();
					}
				});
				shell.setLayout(new FillLayout());
				shell.add(editor);
				shell.show();
				shell.setSize(TaxonChooser.PANEL_WIDTH + 30, TaxonChooser.PANEL_HEIGHT + 50);
				shell.center();
			}
		});
		mainMenu.add(mItem);

		mItem = new MenuItem();
		mItem.setText("Attach Image");
		mItem.setIconStyle("icon-image");
		mItem.addSelectionListener(new SelectionListener<MenuEvent>() {
			public void componentSelected(MenuEvent ce) {
				if (!AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, AuthorizableObject.WRITE, TaxonomyCache.impl.getCurrentTaxon())) {
					WindowUtils
					.errorAlert("Sorry. You do not have sufficient permissions " + "to perform this action.");
					return;
				}

				Popup imagePopup = new Popup();

				if (!imagePopup.isRendered()) {
					ImageManagerPanel imageManager = new ImageManagerPanel(String.valueOf(TaxonomyCache.impl
							.getCurrentTaxon().getId()));
					imagePopup.add(imageManager);
				}

				imagePopup.show();
				imagePopup.center();
			}
		});
		mainMenu.add(mItem);

		// mItem = new MenuItem(Style.PUSH);
		// mItem.setText("View Bibliography");
		// mItem.setIconStyle("icon-book-open");
		// mItem.addSelectionListener(new SelectionListener() {
		// public void widgetSelected(BaseEvent be) {
		// DEMToolsPopups.buildBibliographyPopup();
		// }
		// });
		// mainMenu.add(mItem);
		//
		// mItem = new MenuItem(Style.PUSH);
		// mItem.setText("View References By Field");
		// mItem.setIconStyle("icon-book-open");
		// mItem.addSelectionListener(new SelectionListener() {
		// public void widgetSelected(BaseEvent be) {
		// DEMToolsPopups.buildReferencePopup();
		// }
		// });
		// mainMenu.add(mItem);

		mItem = new MenuItem();
		mItem.setText("Manage References");
		mItem.setIconStyle("icon-book");
		mItem.addSelectionListener(new SelectionListener<MenuEvent>() {
			public void componentSelected(MenuEvent ce) {
				final Window s = WindowUtils.getWindow(false, false, "Manage References -- Add to Global References");
				s.setIconStyle("icon-book");
				s.setLayout(new FitLayout());

				GenericCallback<Object> callback = new GenericCallback<Object>() {
					public void onFailure(Throwable caught) {
						startAutosaveTimer();
						WindowUtils.errorAlert("Error!", "Error committing changes to the "
								+ "server. Ensure you are connected to the server, then try " + "the process again.");
					}

					public void onSuccess(Object result) {
						startAutosaveTimer();
						WindowUtils.infoAlert("Success!", "Successfully committed reference changes.");
					}
				};
				ClientUIContainer.bodyContainer.tabManager.panelManager.refViewPanel.setReferences(AssessmentCache.impl
						.getCurrentAssessment(), callback, callback);

				s.add(ClientUIContainer.bodyContainer.tabManager.panelManager.refViewPanel);

				stopAutosaveTimer();
				s.setSize(850, 550);
				s.show();
				s.center();
			}
		});
		mainMenu.add(mItem);

		mItem = new MenuItem();
		mItem.setText("View Notes");
		mItem.setIconStyle("icon-note");
		mItem.addSelectionListener(new SelectionListener<MenuEvent>() {
			public void componentSelected(MenuEvent ce) {
				DEMToolsPopups.buildNotePopup();
			}
		});
		mainMenu.add(mItem);

		mItem = new MenuItem();
		mItem.setIconStyle("icon-changes");
		mItem.setText("Changes");
		mItem.addListener(Events.Select, new Listener<BaseEvent>() {
			public void handleEvent(BaseEvent be) {
				if( SimpleSISClient.iAmOnline ) {
					AssessmentChangesPanel panel = new AssessmentChangesPanel();
					Window window = WindowUtils.getWindow(true, false, "Assessment Changes");
					window.setClosable(true);
					window.setSize(900, 500);
					window.setLayout(new FillLayout());
					window.add(panel);
					window.show();
				} else {
					WindowUtils.errorAlert("Not available offline.", "Sorry, this feature is not " +
							"available offline.");
				}
			}
		});

		mainMenu.add(mItem);

//		mItem = new MenuItem();
//		mItem.setIconStyle("icon-comments");
//		mItem.setText("Comments");
//		mItem.addListener(Events.Select, new Listener<BaseEvent>() {
//			public void handleEvent(BaseEvent be) {
//				Assessment a = AssessmentCache.impl.getCurrentAssessment();
//				Window alert = WindowUtils.getWindow(false, false, "Assessment #" + a.getId());
//				LayoutContainer c = alert;
//				c.setLayout(new FillLayout());
//				c.setSize(300, 450);
//				TabItem item = new TabItem();
//				item.setIconStyle("icon-comments");
//				item.setText("Comments");
//				String target = "/comments/browse/assessment/"
//					+ FilenameStriper.getIDAsStripedPath(a.getId()) + ".comments.xml";
//				SysDebugger.getInstance().println(target);
//				item.setUrl(target);
//				TabPanel tf = new TabPanel();
//				tf.add(item);
//				c.add(tf);
//				alert.show();
//				return;
//			}
//		});
//		mainMenu.add(mItem);
		toolbar.add(item);

		mItem = new MenuItem();
		mItem.setText("View Report");
		mItem.setIconStyle("icon-report");
		mItem.addSelectionListener(new SelectionListener<MenuEvent>() {
			public void componentSelected(MenuEvent ce) {
				fetchReport();
			}
		});
		mainMenu.add(mItem);
		
		final MenuItem integrity = new MenuItem();
		integrity.setText("Validate Assessment");
		integrity.setIconStyle("icon-integrity");
		integrity.addSelectionListener(new SelectionListener<MenuEvent>() {
			public void componentSelected(MenuEvent ce) {
				runIntegrityValidator();
			}
		});
		/*
		 * Comment in the line below to add validation 
		 * to the Tools menu.  Don't forget to add the 
		 * IntegrityApplication to SISBootstrap.
		 */
		mainMenu.add(integrity);
		
		final MenuItem workflow = new MenuItem();
		workflow.setText("Submission Process Notes");
		workflow.setIconStyle("icon-workflow");
		workflow.addSelectionListener(new SelectionListener<MenuEvent>() {
			public void componentSelected(MenuEvent ce) {
				final WorkflowNotesWindow window = 
					new WorkflowNotesWindow(AssessmentCache.impl.getCurrentAssessment().getId()+"");
				window.show();
			}
		});
		
		mainMenu.add(workflow);

		toolbar.add(new SeparatorToolItem());
		
		toolbar.add(new FillToolItem());
		
		workflowStatus = new Button();
		workflowStatus.addStyleName("workflow-warning");
		
		toolbar.add(workflowStatus);

		return toolbar;
	}

	public void updateWorkflowStatus() {
		try {
			if (!WorkflowStatus.DRAFT.equals(WorkingSetCache.impl.getCurrentWorkingSet().getWorkflowStatus()))
				workflowStatus.setText("Submission Status: " + 
					WorkingSetCache.impl.getCurrentWorkingSet().getWorkflowStatus() 
				);
		} catch (Exception e) {
			workflowStatus.setText("");
		}
	}
	
	private void buildViewChooser() {
		String prefs = SimpleSISClient.currentUser.getPreference(UserPreferences.VIEW_CHOICES, null);
		List<String> viewsToShow = null;
		if( prefs != null && !prefs.equals("") )
			viewsToShow = Arrays.asList(prefs.split(","));

		for (final SISView curView : ViewCache.impl.getAvailableViews()) {
			if( viewsToShow == null || viewsToShow.contains(curView.getId()) ) {
				final ContentPanel curItem = new ContentPanel();
				curItem.setHeading(curView.getDisplayableTitle());
				FlowLayout layout = new FlowLayout();
				curItem.setLayout(layout);

				for (final SISPageHolder curPage : curView.getPages()) {
					final HTML curPageLabel = new HTML(curPage.getPageTitle());
					curPageLabel.addStyleName("SIS_HyperlinkBehavior");
					curPageLabel.addStyleName("padded-viewContainer");
					curPageLabel.addClickListener(new ClickListener() {
						public void onClick(Widget arg0) {
							doPageChangeRequested(curView, curPage, curPageLabel);
						}
					});
					curItem.add(curPageLabel);
				}

				viewWrapper.add(curItem);
			}
		}

		if (viewWrapper.getItemCount() > 0)
			viewChooser.setActiveItem(viewWrapper.getItem(0));
	}

	public void clearDEM() {
		if (ViewCache.impl.getCurrentView() != null && ViewCache.impl.getCurrentView().getCurPage() != null)
			ViewCache.impl.getCurrentView().getCurPage().removeMyFields();

		scroller.removeAll();
		scroller.layout();
	}

	private void doPageChange(int page) {
		if (!AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, AuthorizableObject.WRITE, AssessmentCache.impl
				.getCurrentAssessment())) {
			Info.display("Insufficient Permissions", "NOTICE: You do not have "
					+ "permission to save modifications to this assessment.");
			viewOnly = true;
			toggleEditViewButton();
		}

		if (ViewCache.impl.getCurrentView() != null && ViewCache.impl.getCurrentView().getCurPage() != null)
			ViewCache.impl.getCurrentView().getCurPage().removeMyFields();

		resetAutosaveTimer();
		
		scroller.removeAll();
		scroller.add(new HTML("Loading..."));
		
		ViewCache.impl.showPage(currentView.getId(), page, viewOnly, new DrawsLazily.DoneDrawingCallbackWithParam<TabPanel>() {
			public void isDrawn(TabPanel parameter) {
				scroller.removeAll();
				scroller.add(parameter);
				scroller.layout();
			}	
		});

		scroller.layout();
	}

	private void doPageChangeRequested(final SISView curView, final SISPageHolder curPage, final HTML curPageLabel) {
		if (AssessmentCache.impl.getCurrentAssessment() == null) {
			Info.display(new InfoConfig("No Assessment", "Please select an assessment first."));
			return;
		}

		boolean samePage = false;
		try {
			samePage = ViewCache.impl.getCurrentView().getCurPage().equals(curPage);
		} catch (Exception somethingsNull) {
		}
		
		if (samePage)
			return;

		if (!viewOnly && ViewCache.impl.getCurrentView() != null && AssessmentClientSaveUtils.shouldSaveCurrentAssessment(
				ViewCache.impl.getCurrentView().getCurPage().getMyFields())) {
			stopAutosaveTimer();

			if (SimpleSISClient.currentUser.getPreference(UserPreferences.AUTO_SAVE, UserPreferences.PROMPT) == UserPreferences.PROMPT)
				openChangesMadePopup(curView, curPage, curPageLabel);
			else if (SimpleSISClient.currentUser.getPreference(UserPreferences.AUTO_SAVE, UserPreferences.PROMPT) == UserPreferences.DO_ACTION)
				doSaveCurrentAssessment(curView, curPage, curPageLabel);
			else if (SimpleSISClient.currentUser.getPreference(UserPreferences.AUTO_SAVE, UserPreferences.PROMPT) == UserPreferences.IGNORE) {
				currentView = curView;
				updateBoldedPage(curPageLabel);
				showCurrentAssessment(curView.getPages().indexOf(curPage));
			}
		} else {
			currentView = curView;
			updateBoldedPage(curPageLabel);
			showCurrentAssessment(curView.getPages().indexOf(curPage));
		}
	}

	private void doSaveCurrentAssessment(final SISView curView, final SISPageHolder curPage, final HTML curPageLabel) {
		try {
			AssessmentClientSaveUtils.saveAssessment(AssessmentCache.impl.getCurrentAssessment(), new GenericCallback<Object>() {
				public void onFailure(Throwable arg0) {
					WindowUtils.hideLoadingAlert();
					layout();
					WindowUtils.errorAlert("Save Failed", "Failed to save assessment! " + arg0.getMessage());
				}

				public void onSuccess(Object arg0) {
					WindowUtils.hideLoadingAlert();
					Info.display("Save Complete", "Successfully saved assessment {0}.", AssessmentCache.impl
							.getCurrentAssessment().getSpeciesName());

					currentView = curView;
					updateBoldedPage(curPageLabel);
					showCurrentAssessment(curView.getPages().indexOf(curPage));
				}
			});
		} catch (InsufficientRightsException e) {
			WindowUtils.errorAlert("Auto-save failed. You do not have sufficient " + "rights to perform this action.");
		}
	}
	
	private void runIntegrityValidator() {
		final Assessment data = AssessmentCache.impl.getCurrentAssessment();
		//Popup new window:
		ClientAssessmentValidator.validate(data.getId(), data.getType());
		/*ClientAssessmentValidator.validate(data.getId(), data.getType(),
				new GenericCallback<NativeDocument>() {
			public void onSuccess(NativeDocument result) {
				final ValidationResultsWindow window = new ValidationResultsWindow(
						data.getId(), result.getText());
				window.show();
			}
			public void onFailure(Throwable caught) {
			}
		});*/
	}

	private void fetchReport() {
		final CheckBox useLimited = new CheckBox();
		useLimited.setValue(Boolean.valueOf(true));
		useLimited.setFieldLabel("Use limited field set (more compact report)");
		
		final CheckBox showEmpty = new CheckBox();
		showEmpty.setFieldLabel("Show empty fields");
		
		final FormPanel form = new FormPanel();
		form.setLabelSeparator("?");
		form.setLabelWidth(300);
		form.setFieldWidth(50);
		form.setHeaderVisible(false);
		form.setBorders(false);
		form.add(useLimited);
		form.add(showEmpty);
		
		final Window w = WindowUtils.getWindow(true, false, "Report Options");
		
		form.addButton(new Button("Submit", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				Assessment a = AssessmentCache.impl.getCurrentAssessment();
				String target = "/reports/";

				if (a.getType().equals(AssessmentType.DRAFT_ASSESSMENT_TYPE)) {
					target += "draft/";
				} else if (a.getType().equals(AssessmentType.PUBLISHED_ASSESSMENT_TYPE)) {
					target += "published/";
				} else if (a.getType().equals(AssessmentType.USER_ASSESSMENT_TYPE)) {
					target += "user/" + SimpleSISClient.currentUser.getUsername() + "/";
				}

				w.close();
				
				com.google.gwt.user.client.Window.open(target + AssessmentCache.impl.getCurrentAssessment().getId()
						+ "?empty=" + showEmpty.getValue() + "&limited=" + useLimited.getValue(),
						"_blank", "");
			}
		}));
		form.addButton(new Button("Cancel", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				w.close();
			}
		}));
		
		w.add(form);
w.setSize(400, 250);
		w.show();
		w.center();
	}

	public boolean isBuilt() {
		return built;
	}

	protected void onDetach() {
		stopAutosaveTimer();
		super.onDetach();
	}

	protected void onHide() {
		stopAutosaveTimer();
		super.onHide();
	}

	private void openChangesMadePopup(final SISView curView, final SISPageHolder curPage, final HTML curPageLabel) {
		WindowUtils.confirmAlert("By the way...", "Navigating away from this page will"
				+ " revert unsaved changes. Would you like to save?", new Listener<MessageBoxEvent>() {
			public void handleEvent(MessageBoxEvent be) {
				{
					startAutosaveTimer();

					if (be.getButtonClicked().getText().equalsIgnoreCase("cancel")) {

					} else if (be.getButtonClicked().getText().equalsIgnoreCase("yes")) {
						WindowUtils.showLoadingAlert("Saving assessment...");
						doSaveCurrentAssessment(curView, curPage, curPageLabel);
					} else {
						currentView = curView;
						updateBoldedPage(curPageLabel);
						showCurrentAssessment(curView.getPages().indexOf(curPage));
					}
				}
			}
		});
	}

	public void redraw() {
		if (built && currentView != null) {
			showCurrentAssessment(ViewCache.impl.getLastPageViewed(currentView.getId()));
		}
	}

	public void redraw(boolean viewOnly) {
		this.viewOnly = viewOnly;
		if (built && currentView != null) {
			showCurrentAssessment(ViewCache.impl.getLastPageViewed(currentView.getId()));
		}
	}

	public void resetAutosaveTimer() {
		autoSave.cancel();
		startAutosaveTimer();
	}

	private void showCurrentAssessment(int page) {
		if (!built)
			return;
		Debug.println("Redrawing DEM!");

		if (AssessmentCache.impl.getCurrentAssessment() == null) {
			WindowUtils.infoAlert("Alert", "No assessment selected.");
			currentView = null;

			if (ViewCache.impl.getCurrentView() != null && ViewCache.impl.getCurrentView().getCurPage() != null)
				ViewCache.impl.getCurrentView().getCurPage().removeMyFields();

			scroller.removeAll();
			scroller.layout();
		} else {
			if (lastAssessmentShown == null || !lastAssessmentShown.equals(AssessmentCache.impl.getCurrentAssessment()))
				lastAssessmentShown = AssessmentCache.impl.getCurrentAssessment();
	
			doPageChange(page);
		}
	}

	public void startAutosaveTimer() {
		if (AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, AuthorizableObject.WRITE, AssessmentCache.impl.getCurrentAssessment())) {
			Debug.println("Starting autosave.");
			autoSave.schedule(autoSaveInterval);
		}
	}

	public void stopAutosaveTimer() {
		Debug.println("Stopping autosave.");
		autoSave.cancel();
	}

	private void toggleEditViewButton() {
		if (!viewOnly) {
			editViewButton.setText("Edit Data Mode");
			editViewButton.setIconStyle("icon-unlocked");
		} else {
			editViewButton.setText("Read Only Mode");
			editViewButton.setIconStyle("icon-read-only");
		}
	}

	private void updateBoldedPage(final HTML curPageLabel) {
		if (lastSelected != null)
			lastSelected.removeStyleName("bold");

		lastSelected = curPageLabel;
		curPageLabel.addStyleName("bold");
	}
}

