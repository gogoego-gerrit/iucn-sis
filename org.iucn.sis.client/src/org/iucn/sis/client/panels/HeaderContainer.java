package org.iucn.sis.client.panels;

import org.iucn.sis.client.api.caches.AssessmentCache;
import org.iucn.sis.client.api.caches.AuthorizationCache;
import org.iucn.sis.client.api.caches.TaxonomyCache;
import org.iucn.sis.client.api.caches.WorkingSetCache;
import org.iucn.sis.client.api.container.SISClientBase.SimpleSupport;
import org.iucn.sis.client.api.panels.integrity.IntegrityApplicationPanel;
import org.iucn.sis.client.api.utils.UriBase;
import org.iucn.sis.client.container.SimpleSISClient;
import org.iucn.sis.client.panels.definitions.DefinitionEditorPanel;
import org.iucn.sis.client.panels.header.BatchChangePanel;
import org.iucn.sis.client.panels.header.FindReplacePanel;
import org.iucn.sis.client.panels.header.TrashBinPanel;
import org.iucn.sis.client.panels.locking.LockManagementPanel;
import org.iucn.sis.client.panels.permissions.PermissionGroupEditor;
import org.iucn.sis.client.panels.redlist.RedlistPanel;
import org.iucn.sis.client.panels.region.RegionPanel;
import org.iucn.sis.client.panels.taxa.TaxonFinderPanel;
import org.iucn.sis.client.panels.users.UserModelTabPanel;
import org.iucn.sis.shared.api.acl.base.AuthorizableObject;
import org.iucn.sis.shared.api.acl.feature.AuthorizableFeature;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.Taxon;
import org.iucn.sis.shared.api.models.WorkingSet;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.WindowManager;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.util.extjs.client.WindowUtils;
import com.solertium.util.gwt.ui.DrawsLazily;

public class HeaderContainer extends LayoutContainer {
	public static final int defaultHeight = 100;

	private NavigationHeader centerPanel;
	private LayoutContainer leftPanel;
	private LayoutContainer rightPanel;

	private BorderLayoutData leftData;
	private BorderLayoutData rightData;
	private BorderLayoutData centerData;

	private FindReplacePanel findReplacePanel;
	private TrashBinPanel trashBinPanel;
	private BatchChangePanel batchChangePanel;
	private TaxonFinderPanel taxonFinderPanel;
	private DefinitionEditorPanel definitionPanel;
	private IntegrityApplicationPanel integrityPanel;
	private LockManagementPanel lockManagementPanel;
	private RedlistPanel redlistPanel;
	
	private PermissionGroupEditor permEditor;
	private UserModelTabPanel userModelPanel;

	public HeaderContainer(String first, String last, String affiliation) {
		setLayout(new BorderLayout());
		setHeight(defaultHeight);
		setBorders(true);

		findReplacePanel = new FindReplacePanel();
		trashBinPanel = new TrashBinPanel();
		batchChangePanel = new BatchChangePanel();
		taxonFinderPanel = new TaxonFinderPanel();
		definitionPanel = new DefinitionEditorPanel();
		integrityPanel = new IntegrityApplicationPanel();
		lockManagementPanel = new LockManagementPanel();
		redlistPanel = new RedlistPanel();
		
		leftData = new BorderLayoutData(LayoutRegion.WEST, 155f);
		rightData = new BorderLayoutData(LayoutRegion.EAST, 205f);
		centerData = new BorderLayoutData(LayoutRegion.CENTER);

		leftPanel = buildLeftPanel();
		rightPanel = buildRightPanel(first, last, affiliation);
		centerPanel = new NavigationHeader();

		add(leftPanel, leftData);
		add(centerPanel, centerData);
		add(rightPanel, rightData);
	}

	public void assessmentChanged() {
		Assessment curAssessment = AssessmentCache.impl.getCurrentAssessment();
		Taxon curNode = TaxonomyCache.impl.getCurrentTaxon();

		if (curAssessment == null)
			update();
		else if (curNode != null && curAssessment.getSpeciesID() == curNode.getId()) {
			ClientUIContainer.bodyContainer.tabManager.panelManager.DEM.redraw();
			update();
		} else {
			ClientUIContainer.bodyContainer.tabManager.panelManager.DEM.redraw();
			TaxonomyCache.impl.fetchTaxon(curAssessment.getSpeciesID(), true, new GenericCallback<Taxon>() {
				public void onFailure(Throwable caught) {
					update();
				}

				public void onSuccess(Taxon arg0) {
					// setCurrentTaxon should invoke taxonChanged for us,
					// so there's nothing to do here.
				}
			});
		}
	}

	private LayoutContainer buildLeftPanel() {
		LayoutContainer panel = new LayoutContainer(new FlowLayout());
		panel.setBorders(false);
		Image leftImage = new Image();
		if (SimpleSISClient.currentUser.getAffiliation().equalsIgnoreCase("birdlife"))
			leftImage.setUrl("images/logo-birdlifeStacked.gif");
		else
			leftImage.setUrl("images/sislogo_cropped.png");

		panel.add(leftImage);

		return panel;
	}

	private LayoutContainer buildRightPanel(String first, String last, String affiliation) {
		LayoutContainer panel = new LayoutContainer();
		FlowLayout layout = new FlowLayout(0);
		panel.setLayout(layout);
		panel.setBorders(false);

		HorizontalPanel namePanel = new HorizontalPanel();
		HTML logout = new HTML("[ logout ]");
		logout.addStyleName("SIS_HyperlinkLookAlike");
		logout.addClickListener(new ClickListener() {
			public void onClick(Widget sender) {
				WindowUtils.confirmAlert("Logout", "Are you sure you want to log out?", new Listener<MessageBoxEvent>() {
					public void handleEvent(MessageBoxEvent be) {
						if (be.getButtonClicked().getText().equalsIgnoreCase("yes")) {
							WindowManager.get().hideAll();
							SimpleSupport.doLogout();
						}
					}
				});
			}

		});

		namePanel.setSpacing(2);

		Image userIcon = new Image("images/icon-user-green.png");
		userIcon.addClickListener(new ClickListener() {
			public void onClick(Widget sender) {
				final NativeDocument profileDoc = SimpleSISClient.getHttpBasicNativeDocument();
				profileDoc.get(UriBase.getInstance().getSISBase() +"/profile/" + SimpleSISClient.currentUser.getUsername(), new GenericCallback<String>() {
					public void onFailure(Throwable caught) {
					}

					public void onSuccess(String result) {
						
//						 LoginPanel.newAccountPanel.enterProfileInfoWizard(false);
					}
				});
			}
		});

		namePanel.add(userIcon);
		namePanel.add(new HTML(first + " " + last));
		namePanel.add(logout);

		HorizontalPanel affiliationPanel = new HorizontalPanel();
		affiliationPanel.setSpacing(2);
		affiliationPanel.add(new Image("images/icon-world.png"));
		affiliationPanel.add(new HTML(affiliation));

		ToolBar options = new ToolBar();
		options.setBorders(false);

		Button item = new Button();
		item.setToolTip("Peruse the Taxonomic Hierarchy");
		item.setIconStyle("icon-tree");
		item.addListener(Events.Select, new Listener<BaseEvent>() {
			public void handleEvent(BaseEvent be) {
				Window s = WindowUtils.getWindow(true, false, "Taxonomy Browser");
				LayoutContainer content = s;
				content.setLayout(new FitLayout());
				content.add(ClientUIContainer.bodyContainer.getTabManager().getPanelManager().taxonomyBrowserPanel);
				// s.setLocation( Window.getClientWidth()/2,
				// Window.getClientHeight()/2 );
				s.setSize(400, 420);

				s.show();
				s.center();

				// if(!ClientUIContainer.bodyContainer.getTabManager().
				// getPanelManager().taxonomyBrowserPanel.isRendered())
				ClientUIContainer.bodyContainer.getTabManager().getPanelManager().taxonomyBrowserPanel.update();

				content.layout();
				// ((Button)be.getSource()).setSelected( false );
			}
		});
		options.add(item);

		item = new Button();
		item.setIconStyle("icon-search");
		item.setToolTip("Search for a Taxonomic Concept");
		item.addListener(Events.Select, new Listener() {
			public void handleEvent(BaseEvent be) {
				Window s = WindowUtils.getWindow(true, false, "Taxonomy Search");
				s.setSize(800, 600);
				LayoutContainer content = s;
				content.setLayout(new FillLayout());
				content.add(ClientUIContainer.bodyContainer.getTabManager().getPanelManager().taxonomySearchPanel);
				s.show();

				// ((Button)be.getSource()).setSelected( false );
			}
		});
		options.add(item);

		item = new Button();
		item.setIconStyle("icon-prefs");
		item.setToolTip("Administrative Tools");
		item.addListener(Events.Select, new Listener() {
			public void handleEvent(BaseEvent be) {
				final TabPanel tf = new TabPanel();
				tf.setTabScroll(true);
				Window alert = WindowUtils.getWindow(true, false, "Administrative Tools");
				alert.setSize(680, 400);
				alert.setLayout(new FillLayout());

				if( SimpleSISClient.iAmOnline ) {

					if (AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, 
							AuthorizableObject.USE_FEATURE, AuthorizableFeature.DEM_UPLOAD_FEATURE)) {
						final TabItem tabItem = new TabItem();
						tabItem.setText("DEM Import");
						
						final String target = UriBase.getInstance().getDEMBase() + "/demimport/submit/" + SimpleSISClient.currentUser.getUsername();
						tabItem.setUrl(target);
						tabItem.setIconStyle("icon-refresh");
						tabItem.getHeader().addListener(Events.OnClick, new Listener<BaseEvent>() {
							public void handleEvent(BaseEvent be) {
								AssessmentCache.impl.resetCurrentAssessment();
								TaxonomyCache.impl.resetCurrentTaxon();

								TaxonomyCache.impl.clear();
								AssessmentCache.impl.clear();

								tabItem.setUrl(target);
								tabItem.layout();
							}
						});
						tf.add(tabItem);
					}

					if (AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, 
							AuthorizableObject.USE_FEATURE, AuthorizableFeature.ACCESS_EXPORT_FEATURE)) {
						final TabItem tabItem2 = new TabItem();
						tabItem2.setText("Access Export");
						final String atarget = "/export/access";
						tabItem2.setUrl(atarget);
						tf.add(tabItem2);
					}

					if (AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, 
							AuthorizableObject.USE_FEATURE, AuthorizableFeature.TAXON_FINDER_FEATURE)) {
						final TabItem tabItem3 = new TabItem();
						tabItem3.setText("Manage New Taxa");
						tabItem3.setLayout(new FitLayout());
						tabItem3.add(taxonFinderPanel);
						tf.add(tabItem3);
						taxonFinderPanel.load();
					}

					if (AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, 
							AuthorizableObject.USE_FEATURE, AuthorizableFeature.DEFINITION_MANAGEMENT_FEATURE)) {
						final TabItem tabItem4 = new TabItem();
						tabItem4.setText("Manage Definitions");
						tabItem4.setLayout(new FitLayout());
						definitionPanel.draw();
						tabItem4.add(definitionPanel);
						tf.add(tabItem4);
					}

					if (AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, 
							AuthorizableObject.USE_FEATURE, AuthorizableFeature.INTEGRITY_CHECK_FEATURE)) {
						final TabItem tabItem = new TabItem();
						tabItem.setText("Manage Integrity Checks");
						tabItem.setLayout(new FitLayout());
						final DrawsLazily.DoneDrawingCallback callback = new DrawsLazily.DoneDrawingCallback() {
							public void isDrawn() {
								tabItem.add(integrityPanel);
							}
						};
						if (integrityPanel.isDrawn())
							callback.isDrawn();
						else
							integrityPanel.draw(callback);
						tf.add(tabItem);
					}

					if (AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, 
							AuthorizableObject.USE_FEATURE, AuthorizableFeature.LOCK_MANAGEMENT_FEATURE)) {
						final TabItem tabItem = new TabItem();
						tabItem.setText("Manage Locks");
						tabItem.setLayout(new FitLayout());
						lockManagementPanel.draw(new DrawsLazily.DoneDrawingCallback() {
							public void isDrawn() {
								tabItem.add(lockManagementPanel);
							}
						});
						tf.add(tabItem);
					}
					
					if (AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, 
							AuthorizableObject.USE_FEATURE, AuthorizableFeature.BATCH_UPLOAD_FEATURE)) {
						final TabItem tabItem = new TabItem();
						tabItem.setText("Batch Upload");
						tabItem.setIconStyle("icon-refresh");
						tabItem.getHeader().addListener(Events.OnClick, new Listener<BaseEvent>() {
							public void handleEvent(BaseEvent be) {
								tabItem.setUrl(UriBase.getInstance().getImageBase()+"/images");
								tabItem.layout();
							}
						});
						tabItem.setUrl(UriBase.getInstance().getImageBase()+"/images");
						tf.add(tabItem);
					}
					if (AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, 
							AuthorizableObject.USE_FEATURE, AuthorizableFeature.REDLIST_FEATURE)) {
						final TabItem tabItem = new TabItem();
						tabItem.setText("Redlist");
						tabItem.setLayout(new FitLayout());
						redlistPanel.draw(new DrawsLazily.DoneDrawingCallback() {
							public void isDrawn() {
								tabItem.add(redlistPanel);
							}
						});
						tf.add(tabItem);
					}

				}
				
				if( tf.getItems().size() > 0 ) {
					alert.add(tf);
					alert.show();
				} else
					WindowUtils.errorAlert("Sorry, but you do not have permission to access to these tools.");
				return;
			}
		});
		options.add(item);

		item = new Button();
		item.setIconStyle("icon-find");
		item.setToolTip("Find/Replace");
		item.addListener(Events.Select, new Listener<BaseEvent>() {
			public void handleEvent(BaseEvent be) {
				if (AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, AuthorizableObject.USE_FEATURE, AuthorizableFeature.FIND_REPLACE_FEATURE)) {
					Window nS = WindowUtils.getWindow(true, true, "Find and Replace");
					nS.setSize(875, 600);

					nS.setLayout(new FillLayout());
					nS.add(findReplacePanel);
					findReplacePanel.refresh();
					nS.show();
					nS.layout();
				} else
					WindowUtils.errorAlert("This is a currently restricted to " + "administrative users only.");
			}

		});
		options.add(item);

		item = new Button();
		item.setIconStyle("icon-trash");
		item.setToolTip("Trash Bin");
		item.addListener(Events.Select, new Listener() {

			public void handleEvent(BaseEvent be) {
				Window nS = WindowUtils.getWindow(true, true, "Trash Bin");
				nS.setSize(800, 550);
				TrashBinPanel tbp = new TrashBinPanel();
				nS.add(tbp);
				nS.setLayout(new FillLayout());
				nS.show();
				// trashBinPanel.refresh();
			}

		});
		options.add(item);

		item = new Button();
		item.setIconStyle("icon-page-copy");
		item.setToolTip("Batch Change");
		item.addListener(Events.Select, new Listener() {

			public void handleEvent(BaseEvent be) {
				if (!AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, AuthorizableObject.USE_FEATURE, AuthorizableFeature.BATCH_CHANGE_FEATURE)) {
					WindowUtils.errorAlert("This is a currently restricted to " + "administrative users only.");
				} else if (AssessmentCache.impl.getCurrentAssessment() == null) {
					WindowUtils.errorAlert("Error", "A current assessment must be selected; it"
							+ " is used as the template. Please try again.");
				} else {
					Window nS = WindowUtils.getWindow(true, true, "Batch Change");
					nS.setSize(800, 550);
					nS.setLayout(new FillLayout());
					nS.add(batchChangePanel);
					batchChangePanel.refresh();
					nS.show();
				}
				// trashBinPanel.refresh();
			}

		});
		options.add(item);

		item = new Button();
		item.setIconStyle("icon-world-edit");
		item.setToolTip("Edit Region List");
		item.addListener(Events.Select, new Listener<BaseEvent>() {

			public void handleEvent(BaseEvent be) {
				if (!AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, AuthorizableObject.USE_FEATURE, AuthorizableFeature.TAXON_FINDER_FEATURE)) {
					WindowUtils.errorAlert("This is a currently restricted to administrative users only.");
				} else {
					Window nS = WindowUtils.getWindow(true, true, "");
					nS.setSize(800, 400);
					nS.setLayout(new FitLayout());
					nS.add(new RegionPanel());
					nS.show();
					// trashBinPanel.refresh();
				}
			}

		});
		options.add(item);

		item = new Button();
		item.setIconStyle("icon-book-edit");
		item.setToolTip("Manage References");
		item.addListener(Events.Select, new Listener<BaseEvent>() {

			public void handleEvent(BaseEvent be) {
				final Window s = WindowUtils.getWindow(true, true, "Manage References");
				s.setIconStyle("icon-book");
				s.setLayout(new FitLayout());
				ClientUIContainer.bodyContainer.tabManager.panelManager.refViewPanel.setReferences(null);
				s.add(ClientUIContainer.bodyContainer.tabManager.panelManager.refViewPanel);
				s.setSize(850, 550);
				s.show();
				s.center();
			}

		});
		options.add(item);
		
		item = new Button();
		item.setIconStyle("icon-user-group");
		item.setToolTip("Manage Users");
		item.addListener(Events.Select, new Listener<BaseEvent>() {

			public void handleEvent(BaseEvent be) {
				final Window s = WindowUtils.getWindow(true, true, "Manage Users");
				s.setIconStyle("icon-user-group");
				s.setLayout(new FitLayout());
				
				TabPanel userPanel = new TabPanel();
				
				if( AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, 
						AuthorizableObject.USE_FEATURE, AuthorizableFeature.PERMISSION_MANAGEMENT_FEATURE)) {
					permEditor = new PermissionGroupEditor();
					
					TabItem ti = new TabItem("Edit Permissions");
					ti.setIconStyle("icon-user-group-edit");
					ti.setLayout(new FitLayout());
					ti.add(permEditor);
					userPanel.add(ti);
				}
				
				userModelPanel = new UserModelTabPanel();
				TabItem ti = new TabItem("Edit Users");
				ti.setIconStyle("icon-user-suit");
				ti.setLayout(new FitLayout());
				ti.add(userModelPanel);
				userPanel.add(ti);
				
				s.add(userPanel);
				
				
				s.setSize(800, 600);
				s.show();
				s.center();
			}

		});
		options.add(item);

		panel.add(options);
		panel.add(namePanel);
		panel.add(affiliationPanel);

		return panel;
	}

	public void taxonChanged() {
		Assessment curAssessment = AssessmentCache.impl.getCurrentAssessment();
		Taxon curNode = TaxonomyCache.impl.getCurrentTaxon();
		WorkingSet curSet = WorkingSetCache.impl.getCurrentWorkingSet();

		// Make sure Assessment is correct.
		if (curNode == null || (curAssessment != null && curAssessment.getSpeciesID() != curNode.getId())) {
			AssessmentCache.impl.resetCurrentAssessment();
		}

		// Make sure working set isn't wrong.
		if (curNode != null) {
			if (curSet != null && !curSet.getSpeciesIDs().contains(Integer.valueOf(curNode.getId())))
				WorkingSetCache.impl.resetCurrentWorkingSet();
			else
				update();

			if (ClientUIContainer.bodyContainer.getSelectedItem().equals(
					ClientUIContainer.bodyContainer.tabManager.taxonHomePage)) {
				ClientUIContainer.bodyContainer.tabManager.panelManager.taxonomicSummaryPanel.update(curNode.getId());
			}
		} else
			update();
	}

	public void update() {
		centerPanel.update();
	}

	public void workingSetChanged() {
		Taxon curNode = TaxonomyCache.impl.getCurrentTaxon();
		WorkingSet curSet = WorkingSetCache.impl.getCurrentWorkingSet();

		if (curSet != null && curNode != null && !curSet.getSpeciesIDs().contains(curNode.getId() + ""))
			TaxonomyCache.impl.resetCurrentTaxon(); // Will also reset
		// curAssessment
		else
			update();

		// MAKE SURE THAT IF ON WORKING SET PAGE, THE WORKING SET TAB IS UPDATED
		if (ClientUIContainer.bodyContainer.getSelectedItem().equals(
				ClientUIContainer.bodyContainer.tabManager.workingSetPage)) {
			ClientUIContainer.bodyContainer.tabManager.workingSetPage.workingSetChanged();
		}
	}
}
