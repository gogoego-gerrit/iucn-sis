package org.iucn.sis.client.panels;

import java.util.List;

import org.iucn.sis.client.api.caches.AssessmentCache;
import org.iucn.sis.client.api.caches.AuthorizationCache;
import org.iucn.sis.client.api.caches.BookmarkCache;
import org.iucn.sis.client.api.caches.TaxonomyCache;
import org.iucn.sis.client.api.container.SISClientBase;
import org.iucn.sis.client.api.container.StateManager;
import org.iucn.sis.client.api.container.SISClientBase.SimpleSupport;
import org.iucn.sis.client.api.utils.UriBase;
import org.iucn.sis.client.container.SimpleSISClient;
import org.iucn.sis.client.panels.bookmarks.BookmarkManager;
import org.iucn.sis.client.panels.bookmarks.NewBookmarkPanel;
import org.iucn.sis.client.panels.definitions.DefinitionEditorPanel;
import org.iucn.sis.client.panels.header.BatchChangePanel;
import org.iucn.sis.client.panels.header.FindReplacePanel;
import org.iucn.sis.client.panels.header.TrashBinPanel;
import org.iucn.sis.client.panels.integrity.IntegrityApplicationPanel;
import org.iucn.sis.client.panels.locking.LockManagementPanel;
import org.iucn.sis.client.panels.permissions.PermissionGroupEditor;
import org.iucn.sis.client.panels.redlist.RedlistPanel;
import org.iucn.sis.client.panels.region.RegionPanel;
import org.iucn.sis.client.panels.search.SearchCache;
import org.iucn.sis.client.panels.search.SearchQuery;
import org.iucn.sis.client.panels.taxa.TaxonFinderPanel;
import org.iucn.sis.client.panels.taxa.tagging.TaxaTagManager;
import org.iucn.sis.client.panels.users.UploadUsersPanel;
import org.iucn.sis.client.panels.users.UserModelTabPanel;
import org.iucn.sis.client.panels.utils.TaxonomyBrowserPanel;
import org.iucn.sis.client.panels.viruses.VirusManager;
import org.iucn.sis.client.panels.zendesk.ZendeskPanel;
import org.iucn.sis.shared.api.acl.base.AuthorizableObject;
import org.iucn.sis.shared.api.acl.feature.AuthorizableFeature;
import org.iucn.sis.shared.api.models.Bookmark;
import org.iucn.sis.shared.api.utils.UserAffiliationProperties;
import org.iucn.sis.shared.api.utils.UserAffiliationPropertiesFactory;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.IconButtonEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.event.TabPanelEvent;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.WindowManager;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.IconButton;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.extjs.gxt.ui.client.widget.menu.SeparatorMenuItem;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.util.extjs.client.WindowUtils;
import com.solertium.util.gwt.ui.DrawsLazily;

public class HeaderContainer extends ContentPanel {
	public static final int defaultHeight = 100;

	public MonkeyNavigator centerPanel;
	private LayoutContainer leftPanel;

	private final FindReplacePanel findReplacePanel;
	private final BatchChangePanel batchChangePanel;
	private final TaxonFinderPanel taxonFinderPanel;
	private final DefinitionEditorPanel definitionPanel;
	private final IntegrityApplicationPanel integrityPanel;
	private final LockManagementPanel lockManagementPanel;
	private final RedlistPanel redlistPanel;
	private final VirusManager virusManagerPanel;
	private final TaxaTagManager taxaTagManagerPanel;
	
	private PermissionGroupEditor permEditor;
	private UserModelTabPanel userModelPanel;

	public HeaderContainer(String first, String last, String affiliation) {
		super();
		setLayout(new FillLayout());
		setHeight(defaultHeight);
		
		findReplacePanel = new FindReplacePanel();
		batchChangePanel = new BatchChangePanel();
		taxonFinderPanel = new TaxonFinderPanel();
		definitionPanel = new DefinitionEditorPanel();
		integrityPanel = new IntegrityApplicationPanel();
		lockManagementPanel = new LockManagementPanel();
		redlistPanel = new RedlistPanel();
		virusManagerPanel = new VirusManager();
		taxaTagManagerPanel = new TaxaTagManager();

		leftPanel = buildLeftPanel(first, last, affiliation);
		
		BorderLayoutData leftData = new BorderLayoutData(LayoutRegion.WEST);
		leftData.setSplit(false);
		leftData.setSize(200);
		leftData.setMargins(new Margins(0, 5, 0, 5));
		
		BorderLayoutData centerData = new BorderLayoutData(LayoutRegion.CENTER);
		centerData.setMargins(new Margins(0, 0, 0, 5));
		
		centerPanel = new MonkeyNavigator();
		centerPanel.draw(new DrawsLazily.DoneDrawingWithNothingToDoCallback());
		
		final LayoutContainer container = new LayoutContainer(new BorderLayout());
		container.add(leftPanel, leftData);
		container.add(centerPanel, centerData);
		
		add(container);
	}

	/*public void assessmentChanged() {
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
	}*/

	private LayoutContainer buildLeftPanel(String first, String last, String affiliation) {
		UserAffiliationProperties properties = UserAffiliationPropertiesFactory.
			get(SimpleSISClient.currentUser.getAffiliation());
		
		HTML logo = new HTML("<div style=\"text-align:center;margin:0px auto;cursor: pointer\">" + 
			"<img src=\"" + properties.getHeaderLogo() + 
			"\" alt=\"Species Information Service\" /></div>");
		logo.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				StateManager.impl.reset();
			}
		});

		HorizontalPanel namePanel = new HorizontalPanel();
		namePanel.setSpacing(2);

		Image userIcon = new Image("images/icon-user-green.png");
		userIcon.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
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

		HorizontalPanel affiliationPanel = new HorizontalPanel();
		affiliationPanel.setSpacing(2);
		affiliationPanel.add(new Image("images/icon-world.png"));
		affiliationPanel.add(new HTML(affiliation));
		
		final VerticalPanel userInfoWrapper = new VerticalPanel();
		userInfoWrapper.setWidth("100%");
		userInfoWrapper.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		userInfoWrapper.setSpacing(3);
		userInfoWrapper.add(namePanel);
		userInfoWrapper.add(affiliationPanel);
		
		IconButton optionsIcon = new IconButton("icon-header-options");
		optionsIcon.setSize(32, 32);
		optionsIcon.addSelectionListener(new SelectionListener<IconButtonEvent>() {
			public void componentSelected(IconButtonEvent ce) {
				getMenu().show(ce.getIconButton());
			}
		});
		
		IconButton reportBug = new IconButton("icon-header-zendesk");
		reportBug.setSize(32, 32);
		reportBug.setToolTip("Report a bug or question to the help desk.");
		reportBug.addSelectionListener(new SelectionListener<IconButtonEvent>() {
			public void componentSelected(IconButtonEvent ce) {
				 //new ZendeskPanel();
				new AssemblaSupportPanel().show();
			}
		});
		
		IconButton logout = new IconButton("icon-header-logout");
		logout.setSize(32, 32);
		logout.setToolTip("Logout");
		logout.addSelectionListener(new SelectionListener<IconButtonEvent>() {
			public void componentSelected(IconButtonEvent ce) {
				WindowUtils.confirmAlert("Logout", "Are you sure you want to log out?", new WindowUtils.SimpleMessageBoxListener() {
					public void onYes() {
						WindowManager.get().hideAll();
						SimpleSupport.doLogout();
					}
				});
			}
		});

		Grid bottom = new Grid(1, 3);
		bottom.setWidth("100%");
		bottom.setWidget(0, 0, optionsIcon);
		bottom.setWidget(0, 1, reportBug);
		bottom.setWidget(0, 2, logout);
		for (int i = 0; i < bottom.getCellCount(0); i++)
			bottom.getCellFormatter().setAlignment(0, i, HasHorizontalAlignment.ALIGN_CENTER, HasVerticalAlignment.ALIGN_MIDDLE);
		
		String panelStyle = properties.getHeaderBackgroundStyle();
		
		LayoutContainer panel = new LayoutContainer(new FlowLayout());
		if (panelStyle != null)
			panel.addStyleName(panelStyle);
		panel.setBorders(false);
		panel.add(logo);
		panel.add(createSpacer(10));
		panel.add(userInfoWrapper);
		panel.add(createSpacer(10));
		panel.add(bottom);

		return panel;
	}
	
	private HTML createSpacer(int size) {
		HTML spacer = new HTML("&nbsp;");
		spacer.setHeight(size + "px");
		
		return spacer;
	}
	
	private MenuItem createMenuItem(String icon, String text, SelectionListener<MenuEvent> listener) {
		MenuItem item = new MenuItem();
		item.setText(text);
		item.setIconStyle(icon);
		item.addSelectionListener(listener);
		
		return item;
	}
	
	private Menu getMenu() {
		final Menu options = new Menu();
		options.add(createMenuItem("icon-tree", "Peruse the Taxonomic Hierarchy", new SelectionListener<MenuEvent>() {
			public void componentSelected(MenuEvent ce) {
				final TaxonomyBrowserPanel browser = new TaxonomyBrowserPanel();
				
				Window window = WindowUtils.newWindow("Taxonomy Browser", null, false, true);
				window.setLayout(new FillLayout());
				window.setSize(400, 420);
				window.addListener(Events.Show, new Listener<ComponentEvent>() {
					public void handleEvent(ComponentEvent ce) {
						browser.update();
					}
				});
				//FIXME: content.add(ClientUIContainer.bodyContainer.getTabManager().getPanelManager().taxonomyBrowserPanel);
				window.add(browser);
				window.show();
				window.center();
				

				// if(!ClientUIContainer.bodyContainer.getTabManager().
				// getPanelManager().taxonomyBrowserPanel.isRendered())
				//FIXME: ClientUIContainer.bodyContainer.getTabManager().getPanelManager().taxonomyBrowserPanel.update();

				// ((Button)be.getSource()).setSelected( false );
			}
		}));
		
		List<SearchQuery> recent = SearchCache.impl.getRecentSearches();
		if (recent.isEmpty()) {
			options.add(createMenuItem("icon-search", "Search for a Taxonomic Concept", new SelectionListener<MenuEvent>() {
				public void componentSelected(MenuEvent ce) {
					ClientUIContainer.bodyContainer.openSearch();
				}
			}));
		}
		else {
			Menu menu = new Menu();
			menu.add(createMenuItem("icon-search", "New Search", new SelectionListener<MenuEvent>() {
				public void componentSelected(MenuEvent ce) {
					ClientUIContainer.bodyContainer.openSearch();
				}
			}));
			menu.add(new SeparatorMenuItem());
			for (final SearchQuery query : recent) {
				menu.add(createMenuItem("icon-search", query.getQuery(), new SelectionListener<MenuEvent>() {
					public void componentSelected(MenuEvent ce) {
						ClientUIContainer.bodyContainer.openSearch(query);
					}
				}));
			}
			
			MenuItem item = new MenuItem("Search for a Taxonomic Concept");
			item.setIconStyle("icon-search");
			item.setSubMenu(menu);
			
			options.add(item);
		}
		
		{
			Menu menu = new Menu();
			MenuItem addItem = new MenuItem("Bookmark this page...");
			addItem.setEnabled(StateManager.impl.getWorkingSet() != null || StateManager.impl.getTaxon() != null);
			addItem.addSelectionListener(new SelectionListener<MenuEvent>() {
				public void componentSelected(MenuEvent ce) {
					NewBookmarkPanel panel = new NewBookmarkPanel();
					panel.show();
				}
			});
			menu.add(addItem);
			
			menu.add(new MenuItem("Manage Bookmarks", new SelectionListener<MenuEvent>() {
				public void componentSelected(MenuEvent ce) {
					BookmarkManager manager = new BookmarkManager();
					manager.show();
				}
			}));
			
			menu.add(new SeparatorMenuItem());
			
			List<Bookmark> list = BookmarkCache.impl.list();
			if (list.isEmpty()) {
				MenuItem none = new MenuItem("(no bookmarks)");
				none.setEnabled(false);
				menu.add(none);
			}
			else {
				for (final Bookmark bookmark : list) {
					menu.add(new MenuItem(bookmark.getName(), new SelectionListener<MenuEvent>() {
						public void componentSelected(MenuEvent ce) {
							History.newItem(bookmark.getValue());
						}
					}));
				}
			}
			
			MenuItem item = new MenuItem("Bookmarks");
			item.setIconStyle("icon-bookmark");
			item.setSubMenu(menu);
			
			options.add(item);
		}
		
		options.add(createMenuItem("icon-prefs", "Administrative Tools", new SelectionListener<MenuEvent>() {
			public void componentSelected(MenuEvent ce) {
				final TabPanel tf = new TabPanel();
				tf.setTabScroll(true);
				Window alert = WindowUtils.newWindow("Administrative Tools", null, false, true);
				alert.setSize(680, 400);
				alert.setLayout(new FillLayout());

				if( SimpleSISClient.iAmOnline ) {

					if (AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, 
							AuthorizableObject.USE_FEATURE, AuthorizableFeature.DEM_UPLOAD_FEATURE)) {
						final TabItem tabItem = new TabItem();
						tabItem.setText("DEM Import");
						tabItem.add(new Html("This feature is not yet available."));
						/*
						 * FIXME: this form should be created client-side.
						 */
						/*final String target = UriBase.getInstance().getDEMBase() + "/demimport/submit/" + SimpleSISClient.currentUser.getUsername();
						tabItem.setUrl(target);*/
						tabItem.setIconStyle("icon-refresh");
						tabItem.getHeader().addListener(Events.OnClick, new Listener<BaseEvent>() {
							public void handleEvent(BaseEvent be) {
								/*AssessmentCache.impl.resetCurrentAssessment();
								TaxonomyCache.impl.resetCurrentTaxon();*/
								
								StateManager.impl.reset();

								TaxonomyCache.impl.clear();
								AssessmentCache.impl.clear();

								//tabItem.setUrl(target);
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
						tabItem3.addListener(Events.Select, new Listener<TabPanelEvent>() {
							public void handleEvent(TabPanelEvent be) {
								taxonFinderPanel.draw(new DrawsLazily.DoneDrawingCallback() {
									public void isDrawn() {
										taxonFinderPanel.layout();
									}
								});
							}
						});
						tf.add(tabItem3);
					}

					if (AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, 
							AuthorizableObject.USE_FEATURE, AuthorizableFeature.DEFINITION_MANAGEMENT_FEATURE)) {
						final TabItem tabItem4 = new TabItem();
						tabItem4.setText("Manage Definitions");
						tabItem4.setLayout(new FitLayout());
						tabItem4.add(definitionPanel);
						tabItem4.addListener(Events.Select, new Listener<TabPanelEvent>() {
							public void handleEvent(TabPanelEvent be) {
								tabItem4.layout();
							}
						});
						tf.add(tabItem4);
					}

					if (AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, 
							AuthorizableObject.USE_FEATURE, AuthorizableFeature.INTEGRITY_CHECK_FEATURE)) {
						final TabItem tabItem = new TabItem();
						tabItem.setText("Manage Integrity Checks");
						tabItem.setLayout(new FitLayout());
						tabItem.add(integrityPanel);
						tabItem.addListener(Events.Select, new Listener<TabPanelEvent>() {
							public void handleEvent(TabPanelEvent be) {
								final DrawsLazily.DoneDrawingCallback callback = new DrawsLazily.DoneDrawingCallback() {
									public void isDrawn() {
										tabItem.layout();
									}
								};
								if (integrityPanel.isDrawn())
									callback.isDrawn();
								else
									integrityPanel.draw(callback);
							}
						});
						tf.add(tabItem);
					}

					if (AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, 
							AuthorizableObject.USE_FEATURE, AuthorizableFeature.LOCK_MANAGEMENT_FEATURE)) {
						final TabItem tabItem = new TabItem();
						tabItem.setText("Manage Locks");
						tabItem.setLayout(new FitLayout());
						tabItem.add(lockManagementPanel);
						tabItem.addListener(Events.Select, new Listener<TabPanelEvent>() {
							public void handleEvent(TabPanelEvent be) {
								lockManagementPanel.draw(new DrawsLazily.DoneDrawingCallback() {
									public void isDrawn() {
										tabItem.layout();
									}
								});
							}
						});
						tf.add(tabItem);
					}
					
					if (AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, 
							AuthorizableObject.USE_FEATURE, AuthorizableFeature.BATCH_UPLOAD_FEATURE)) {
						final TabItem tabItem = new TabItem();
						tabItem.setText("Batch Upload");
						tabItem.setIconStyle("icon-refresh");
						tabItem.addListener(Events.Select, new Listener<TabPanelEvent>() {
							public void handleEvent(TabPanelEvent be) {
								/*
								 * FIXME: this form should be created client-side.
								 */
								tabItem.setUrl(UriBase.getInstance().getImageBase()+"/images/upload");
								tabItem.layout();
							}
						});
						tf.add(tabItem);
					}
					if (AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, 
							AuthorizableObject.USE_FEATURE, AuthorizableFeature.REDLIST_FEATURE)) {
						final TabItem tabItem = new TabItem();
						tabItem.setText("Redlist");
						tabItem.setLayout(new FitLayout());
						tabItem.add(redlistPanel);
						tf.add(tabItem);
					}
					
					if (AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, AuthorizableObject.USE_FEATURE, AuthorizableFeature.VIRUS_MANAGEMENT_FEATURE)) {
						final TabItem tabItem = new TabItem();
						tabItem.setText("Virus Management");
						tabItem.setLayout(new FitLayout());
						tabItem.add(virusManagerPanel);
						tabItem.addListener(Events.Select, new Listener<TabPanelEvent>() {
							public void handleEvent(TabPanelEvent be) {
								virusManagerPanel.draw(new DrawsLazily.DoneDrawingCallback() {
									public void isDrawn() {
										tabItem.layout();
									}
								});
							}
						});
						tf.add(tabItem);
					}
					
					if (AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, AuthorizableObject.USE_FEATURE, AuthorizableFeature.TAXA_TAGGING_FEATURE)) {
						final TabItem tabItem = new TabItem();
						tabItem.setText("Taxon Tag Management");
						tabItem.setLayout(new FitLayout());
						tabItem.add(taxaTagManagerPanel);
						tabItem.addListener(Events.Select, new Listener<TabPanelEvent>() {
							public void handleEvent(TabPanelEvent be) {
								taxaTagManagerPanel.draw(new DrawsLazily.DoneDrawingCallback() {
									public void isDrawn() {
										tabItem.layout();
									}
								});		
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
		}));

		options.add(createMenuItem("icon-find", "Find/Replace", new SelectionListener<MenuEvent>() {
			public void componentSelected(MenuEvent ce) {
				if (AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, AuthorizableObject.USE_FEATURE, AuthorizableFeature.FIND_REPLACE_FEATURE)) {
					Window nS = WindowUtils.newWindow("Find and Replace", null, true, true);
					nS.setSize(875, 600);

					nS.setLayout(new FillLayout());
					nS.add(findReplacePanel);
					findReplacePanel.refresh();
					nS.show();
					nS.layout();
				} else
					WindowUtils.errorAlert("This is a currently restricted to " + "administrative users only.");
			}

		}));

		options.add(createMenuItem("icon-trash", "Trash Bin", new SelectionListener<MenuEvent>() {
			public void componentSelected(MenuEvent ce) {
				Window nS = WindowUtils.newWindow("Trash Bin", null, true, true);
				nS.setSize(800, 550);
				TrashBinPanel tbp = new TrashBinPanel();
				nS.add(tbp);
				nS.setLayout(new FillLayout());
				nS.show();
				// trashBinPanel.refresh();
			}
		}));

		options.add(createMenuItem("icon-page-copy", "Batch Change", new SelectionListener<MenuEvent>() {
			public void componentSelected(MenuEvent ce) {
				if (!AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, AuthorizableObject.USE_FEATURE, AuthorizableFeature.BATCH_CHANGE_FEATURE)) {
					WindowUtils.errorAlert("This is a currently restricted to " + "administrative users only.");
				} else if (AssessmentCache.impl.getCurrentAssessment() == null) {
					WindowUtils.errorAlert("Error", "A current assessment must be selected; it"
							+ " is used as the template. Please try again.");
				} else {
					Window nS = WindowUtils.newWindow("Batch Change", null, true, true);
					nS.setSize(800, 550);
					nS.setLayout(new FillLayout());
					nS.add(batchChangePanel);
					batchChangePanel.refresh();
					nS.show();
				}
			}
		}));
		
		options.add(createMenuItem("icon-world-edit", "Edit Region List", new SelectionListener<MenuEvent>() {
			public void componentSelected(MenuEvent ce) {
				if (!AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, AuthorizableObject.USE_FEATURE, AuthorizableFeature.TAXON_FINDER_FEATURE)) {
					WindowUtils.errorAlert("This is a currently restricted to administrative users only.");
				} else {
					Window nS = WindowUtils.newWindow("", null, true, true);
					nS.setSize(800, 400);
					nS.setLayout(new FitLayout());
					nS.add(new RegionPanel());
					nS.show();
				}
			}
		}));

		options.add(createMenuItem("icon-book-edit", "Manage References", new SelectionListener<MenuEvent>() {
			public void componentSelected(MenuEvent ce) {
				ClientUIContainer.bodyContainer.openReferenceManager();
			}
		}));
		
		options.add(createMenuItem("icon-user-group", "Manage Users", new SelectionListener<MenuEvent>() {
			public void componentSelected(MenuEvent ce) {
				final Window s = WindowUtils.newWindow("Manage Users", "icon-user-group", true, true);
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
				
				if (AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, AuthorizableObject.USE_FEATURE, AuthorizableFeature.USER_MANAGEMENT_FEATURE)) {
					TabItem item = new TabItem("Bulk Upload from Spreadsheet");
					item.setLayout(new FillLayout());
					item.add(new UploadUsersPanel());
					
					userPanel.add(item);
				}
				
				s.add(userPanel);
				
				
				s.setSize(800, 600);
				s.show();
				s.center();
			}
		}));
		
		if ("true".equals(com.google.gwt.user.client.Window.Location.getParameter("debug"))) {
			options.add(createMenuItem("", "View Debugging Output", new SelectionListener<MenuEvent>() {
				public void componentSelected(MenuEvent ce) {
					final Window window = WindowUtils.newWindow("Debugging Output");
					window.setModal(false);
					window.setAutoHide(true);
					window.setLayout(new FitLayout());
					window.setSize(450, 300);
					window.addButton(new Button("Close", new SelectionListener<ButtonEvent>() {
						public void componentSelected(ButtonEvent ce) {
							window.hide();
						}
					}));
					final StringBuilder builder = new StringBuilder();
					final String[] out = SISClientBase.instance.getLog().getAll();
					for (String x : out)
						builder.append(x + "\r\n-----------------------------\r\n");
					
					final TextArea area = new TextArea();
					area.setReadOnly(true);
					area.setValue(builder.toString());
					
					window.add(area);
					window.show();
				}
			}));
		}
		
		return options;
	}

	/*public void taxonChanged() {
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

			/*if (ClientUIContainer.bodyContainer.getSelectedItem().equals(
					ClientUIContainer.bodyContainer.tabManager.taxonHomePage)) {
				ClientUIContainer.bodyContainer.tabManager.panelManager.taxonomicSummaryPanel.update(curNode.getId());
			}*//*
		} else
			update();
	}*/

	public void update() {
		//centerPanel.update();
		centerPanel.draw(new DrawsLazily.DoneDrawingWithNothingToDoCallback());
	}

	/*public void workingSetChanged() {
		Taxon curNode = TaxonomyCache.impl.getCurrentTaxon();
		WorkingSet curSet = WorkingSetCache.impl.getCurrentWorkingSet();

		if (curSet != null && curNode != null && !curSet.getSpeciesIDs().contains(curNode.getId()))
			TaxonomyCache.impl.resetCurrentTaxon(); // Will also reset curAssessment
		else
			update();

		// MAKE SURE THAT IF ON WORKING SET PAGE, THE WORKING SET TAB IS UPDATED
		/*if (ClientUIContainer.bodyContainer.getSelectedItem().equals(
				ClientUIContainer.bodyContainer.tabManager.workingSetPage)) {
			ClientUIContainer.bodyContainer.tabManager.workingSetPage.workingSetChanged();
		}*//*
	}*/
}
