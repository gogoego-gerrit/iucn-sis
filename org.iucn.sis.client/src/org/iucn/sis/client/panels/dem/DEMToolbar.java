package org.iucn.sis.client.panels.dem;

import java.util.Date;

import org.iucn.sis.client.api.assessment.AssessmentClientSaveUtils;
import org.iucn.sis.client.api.assessment.FieldAttachmentManager;
import org.iucn.sis.client.api.assessment.FieldAttachmentWindow;
import org.iucn.sis.client.api.assessment.ReferenceableAssessment;
import org.iucn.sis.client.api.caches.AssessmentCache;
import org.iucn.sis.client.api.caches.AuthorizationCache;
import org.iucn.sis.client.api.caches.PublicationCache;
import org.iucn.sis.client.api.caches.TaxonomyCache;
import org.iucn.sis.client.api.caches.ViewCache;
import org.iucn.sis.client.api.caches.ViewCache.EditStatus;
import org.iucn.sis.client.api.container.SISClientBase;
import org.iucn.sis.client.api.ui.users.panels.ManageCreditsWindow;
import org.iucn.sis.client.api.ui.views.SISView;
import org.iucn.sis.client.api.utils.SIS;
import org.iucn.sis.client.container.SimpleSISClient;
import org.iucn.sis.client.panels.ClientUIContainer;
import org.iucn.sis.client.panels.assessments.NewAssessmentPanel;
import org.iucn.sis.client.panels.assessments.SingleFieldEditorPanel;
import org.iucn.sis.client.panels.assessments.TrackChangesPanel;
import org.iucn.sis.client.panels.criteracalculator.ExpertPanel;
import org.iucn.sis.client.panels.images.ImageManagerPanel;
import org.iucn.sis.client.panels.taxomatic.NewCommonNameEditor;
import org.iucn.sis.client.panels.taxomatic.NewTaxonSynonymEditor;
import org.iucn.sis.client.panels.utils.ReportOptionsPanel;
import org.iucn.sis.shared.api.acl.InsufficientRightsException;
import org.iucn.sis.shared.api.acl.UserPreferences;
import org.iucn.sis.shared.api.acl.base.AuthorizableObject;
import org.iucn.sis.shared.api.acl.feature.AuthorizableFeature;
import org.iucn.sis.shared.api.debug.Debug;
import org.iucn.sis.shared.api.integrity.ClientAssessmentValidator;
import org.iucn.sis.shared.api.io.AssessmentChangePacket;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.Field;
import org.iucn.sis.shared.api.models.Taxon;
import org.iucn.sis.shared.api.models.TaxonLevel;
import org.iucn.sis.shared.api.utils.CanonicalNames;

import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.HtmlContainer;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.InfoConfig;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.menu.CheckMenuItem;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.extjs.gxt.ui.client.widget.menu.SeparatorMenuItem;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Timer;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.util.events.ComplexListener;
import com.solertium.util.events.SimpleListener;
import com.solertium.util.extjs.client.WindowUtils;
import com.solertium.util.gwt.ui.DrawsLazily;

public class DEMToolbar extends ToolBar {
	
	private final AutosaveTimer autoSave;
	
	private Integer autoSaveInterval = 2;
	
	private Button editViewButton;
	private ComplexListener<EditStatus> refreshListener;
	private SimpleListener saveListener;
	
	private ChangeLog changeLog;
	
	public DEMToolbar() {
		this.autoSave = new AutosaveTimer();
		this.changeLog = new ChangeLog(15);
		setAutoSaveInterval(SISClientBase.currentUser.getPreference(UserPreferences.AUTO_SAVE_TIMER, "2"));
	}
	
	private void setAutoSaveInterval(String interval) {
		if ("-1".equals(interval))
			autoSaveInterval = null;
		else {
			try {
				this.autoSaveInterval = Integer.valueOf(interval);
			} catch (Exception e) {
				this.autoSaveInterval = 2;
			}
			if (autoSaveInterval.intValue() < 0)
				autoSaveInterval = null;
		}
	}
	
	public void setRefreshListener(ComplexListener<EditStatus> refreshListener) {
		this.refreshListener = refreshListener;
	}
	
	public void setSaveListener(SimpleListener saveListener) {
		this.saveListener = saveListener;
	}
	
	public void resetChangeLog() {
		this.changeLog = new ChangeLog(15);
	}
	
	public void build() {
		editViewButton = new Button();
		editViewButton.setText("Read Only Mode");
		editViewButton.setIconStyle("icon-read-only");
		editViewButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				Assessment cur = AssessmentCache.impl.getCurrentAssessment();
				Button source = ce.getButton();

				if (cur != null && !AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, AuthorizableObject.WRITE, cur)) {
					WindowUtils.errorAlert("You do not have rights to edit this assessment.");
				} else {
					EditStatus eventData;
					if ("Read Only Mode".equals(source.getText())) {
						source.setText("Edit Data Mode");
						source.setIconStyle("icon-unlocked");
						eventData = EditStatus.READ_ONLY;
					} else {
						source.setText("Read Only Mode");
						source.setIconStyle("icon-read-only");
						eventData = EditStatus.EDIT_DATA;
					}
					
					if (refreshListener != null)
						refreshListener.handleEvent(eventData);
				}
			}
		});

		add(editViewButton);
		add(new SeparatorToolItem());

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
					final NewAssessmentPanel panel = new NewAssessmentPanel();
					panel.show();
				}
			}

		});

		add(item);
		add(new SeparatorToolItem());

		item = new Button("Save");
		item.setIconStyle("icon-save");
		item.addSelectionListener(new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				if (AssessmentCache.impl.getCurrentAssessment() == null)
					return;

				try {
					boolean save = ViewCache.impl.getCurrentView() != null && AssessmentClientSaveUtils.shouldSaveCurrentAssessment(
							ViewCache.impl.getCurrentView().getCurPage().getMyFields());

					if (save) {
						stopAutosaveTimer();
						WindowUtils.showLoadingAlert("Saving assessment...");
						AssessmentClientSaveUtils.saveAssessment(ViewCache.impl.getCurrentView().getCurPage().getMyFields(),
								AssessmentCache.impl.getCurrentAssessment(), new GenericCallback<AssessmentChangePacket>() {
							public void onFailure(Throwable arg0) {
								WindowUtils.hideLoadingAlert();
								layout();
								WindowUtils.errorAlert("Save Failed", "Failed to save assessment! " + arg0.getMessage());
								resetAutosaveTimer();
							}

							public void onSuccess(AssessmentChangePacket arg0) {
								WindowUtils.hideLoadingAlert();
								Info.display("Save Complete", "Successfully saved assessment {0}.",
										AssessmentCache.impl.getCurrentAssessment().getSpeciesName());
								Debug.println("Explicit save happened at {0}", AssessmentCache.impl.getCurrentAssessment().getLastEdit().getCreatedDate());
								
								log(arg0);
								
								resetAutosaveTimer();
								//TODO: ClientUIContainer.headerContainer.update();
								if (saveListener != null)
									saveListener.handleEvent();
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
		add(item);
		add(new SeparatorToolItem());

		item = new Button();
		item.setIconStyle("icon-attachment");
		item.setText("Attachments");
		item.setEnabled(SIS.isOnline());
		
		Menu attachmentMenu = new Menu();
		MenuItem newAttachment = new MenuItem();
		newAttachment.setText("Attach File");
		newAttachment.setIconStyle("icon-attachment");
		newAttachment.addSelectionListener(new SelectionListener<MenuEvent>() {
			public void componentSelected(MenuEvent ce) {
				if (!AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, AuthorizableObject.WRITE, TaxonomyCache.impl.getCurrentTaxon())) {
					WindowUtils.errorAlert("Sorry. You do not have sufficient permissions " + "to perform this action.");
					return;
				}
				
				FieldAttachmentWindow window = 
					new FieldAttachmentWindow(AssessmentCache.impl.getCurrentAssessment(), null);
				window.show();
			}
		});
		attachmentMenu.add(newAttachment);
		
		MenuItem manageAttachments = new MenuItem();
		manageAttachments.setText("Manage Attachments");
		manageAttachments.setIconStyle("icon-attachment");
		manageAttachments.addSelectionListener(new SelectionListener<MenuEvent>() {
			public void componentSelected(MenuEvent ce) {
				if (!AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, AuthorizableObject.WRITE, TaxonomyCache.impl.getCurrentTaxon())) {
					WindowUtils.errorAlert("Sorry. You do not have sufficient permissions " + "to perform this action.");
					return;
				}

				final FieldAttachmentManager window = new FieldAttachmentManager();
				window.show();
			}
		});
		attachmentMenu.add(manageAttachments);
		
		item.setMenu(attachmentMenu);

		add(item);

		add(new SeparatorToolItem());

		item = new Button();
		item.setIconStyle("icon-information");
		item.setText("Summary");

		Menu mainMenu = new Menu();
		item.setMenu(mainMenu);

		MenuItem mItem = new MenuItem();
		mItem.setIconStyle("icon-expert");
		mItem.setText("Quick Criteria Generator Result");
		mItem.addSelectionListener(new SelectionListener<MenuEvent>() {
			public void componentSelected(MenuEvent ce) {
				if (AssessmentCache.impl.getCurrentAssessment() == null) {
					WindowUtils.infoAlert("Alert", "Please select an assessment first.");
					return;
				}
				
				ExpertPanel expertPanel = new ExpertPanel();
				expertPanel.show();
			}
		});

		mainMenu.add(mItem);

		add(item);
		add(new SeparatorToolItem());
		//add(new SeparatorToolItem());

		mainMenu = new Menu();

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
				
				/*TaxonCommonNameEditor editor = new TaxonCommonNameEditor();
				editor.show();*/
				final NewCommonNameEditor editor = new NewCommonNameEditor();
				editor.draw(new DrawsLazily.DoneDrawingCallback() {
					@Override
					public void isDrawn() {
						editor.show();
					}
				});
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

				NewTaxonSynonymEditor editor = new NewTaxonSynonymEditor();
				editor.show();
			}
		});
		mainMenu.add(mItem);
		
		mItem = new MenuItem();
		mItem.setText("Edit Taxonomic Notes");
		mItem.setIconStyle("icon-text-bold");
		mItem.addSelectionListener(new SelectionListener<MenuEvent>() {
			public void componentSelected(MenuEvent ce) {
				final Taxon node = TaxonomyCache.impl.getCurrentTaxon(); 
				if (node == null) {
					Info.display(new InfoConfig("No Taxa Selected", "Please select a taxa first."));
					return;
				}

				if (!AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, AuthorizableObject.WRITE, node)) {
					WindowUtils
					.errorAlert("Sorry. You do not have sufficient permissions " + "to perform this action.");
					return;
				}
				
				final Field field = node.getTaxonomicNotes() != null? 
					node.getTaxonomicNotes() : 
					new Field(CanonicalNames.TaxonomicNotes, null);
					
				SingleFieldEditorPanel editor = new SingleFieldEditorPanel(field);
				editor.setSaveListener(new ComplexListener<Field>() {
					public void handleEvent(final Field eventData) {
						if (!eventData.hasData() && (eventData.getReference() == null || eventData.getReference().isEmpty()))
							node.setTaxonomicNotes(null);
						else
							node.setTaxonomicNotes(field);
						
						// TODO save to server...
						TaxonomyCache.impl.saveTaxon(node, new GenericCallback<String>() {
							public void onSuccess(String result) {
								Info.display("Success", "Changes saved.");
							}
							public void onFailure(Throwable caught) {
								WindowUtils.errorAlert("Could not save changes, please try again later.");
							}
						});
					}
				});
				editor.show();
			}
		});
		mainMenu.add(mItem);
		
		mainMenu.add(new SeparatorMenuItem());

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

				final ImageManagerPanel manager = new ImageManagerPanel(TaxonomyCache.impl.getCurrentTaxon());
				manager.update(new DrawsLazily.DoneDrawingCallback() {
					public void isDrawn() {
						Window window = WindowUtils.newWindow("Manage Images");
						window.add(manager);
						window.setWidth(600);
						window.setHeight(300);
						window.show();
					}
				});
			}
		});
		mainMenu.add(mItem);

		mItem = new MenuItem();
		mItem.setText("Manage References");
		mItem.setIconStyle("icon-book");
		mItem.addSelectionListener(new SelectionListener<MenuEvent>() {
			public void componentSelected(MenuEvent ce) {
				GenericCallback<Object> callback = new GenericCallback<Object>() {
					public void onFailure(Throwable caught) {
						startAutosaveTimer();
						WindowUtils.errorAlert("Error committing changes to the "
								+ "server. Ensure you are connected to the server, then try " + "the process again.");
					}

					public void onSuccess(Object result) {
						startAutosaveTimer();
						WindowUtils.infoAlert("Successfully committed reference changes.");
					}
				};
				
				ClientUIContainer.bodyContainer.openReferenceManager(
						new ReferenceableAssessment(AssessmentCache.impl.getCurrentAssessment()), 
						"Manage References -- Add to Global References", callback, callback);
				stopAutosaveTimer();
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
		
		mainMenu.add(new SeparatorMenuItem());

		mItem = new MenuItem();
		mItem.setIconStyle("icon-changes");
		mItem.setText("Changes");
		mItem.addListener(Events.Select, new Listener<BaseEvent>() {
			public void handleEvent(BaseEvent be) {
				TrackChangesPanel panel = new TrackChangesPanel(AssessmentCache.impl.getCurrentAssessment());
				panel.show();
			}
		});

		mainMenu.add(mItem);

		mItem = new MenuItem();
		mItem.setText("View Report");
		mItem.setIconStyle("icon-report");
		mItem.addSelectionListener(new SelectionListener<MenuEvent>() {
			public void componentSelected(MenuEvent ce) {
				fetchReport();
			}
		});
		mainMenu.add(mItem);
		
		mainMenu.add(new SeparatorMenuItem());
		
		final MenuItem integrity = new MenuItem();
		integrity.setText("Validate Assessment");
		integrity.setIconStyle("icon-integrity");
		integrity.addSelectionListener(new SelectionListener<MenuEvent>() {
			public void componentSelected(MenuEvent ce) {
				runIntegrityValidator();
			}
		});
		
		mainMenu.add(integrity);
		
		if (AuthorizationCache.impl.canUse(AuthorizableFeature.PUBLICATION_MANAGER_FEATURE)) {
			final MenuItem submit = new MenuItem();
			submit.setText("Submit Assessment");
			submit.setIconStyle("icon-workflow");
			submit.addSelectionListener(new SelectionListener<MenuEvent>() {
				public void componentSelected(MenuEvent ce) {
					final Assessment assessment = AssessmentCache.impl.getCurrentAssessment();
					PublicationCache.impl.submit(assessment, new GenericCallback<Object>() {
						public void onSuccess(Object result) {
							WindowUtils.infoAlert("Assessment has been submitted.");
							if (saveListener != null)
								saveListener.handleEvent();
							if (refreshListener != null)
								refreshListener.handleEvent(ViewCache.impl.getEditStatus());
						}
						public void onFailure(Throwable caught) {
							
						}
					});
				}
			});
			
			mainMenu.add(submit);
		}

		item = new Button();
		item.setText("Tools");
		item.setIconStyle("icon-preferences-wrench");
		item.setMenu(mainMenu);
		add(item);
		
		add(new SeparatorToolItem());
		
		Button mcbutton = new Button();
		mcbutton.setText("Manage Credits");
		mcbutton.setIconStyle("icon-user-group");

		mcbutton.addSelectionListener(new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {

				final ManageCreditsWindow panel = new ManageCreditsWindow();
				panel.show(); 
			}

		});

		add(mcbutton);
		add(new SeparatorToolItem());
		
		Button saveMode = new Button("Auto-Save Options"); {
			MenuItem timedAutoSave = new MenuItem("Timed Auto-Save"); {
				Menu timedMenu = new Menu();
				
				SelectionListener<MenuEvent> listener = new SelectionListener<MenuEvent>() {
					public void componentSelected(MenuEvent ce) {
						String newPreference = ce.getItem().getData("value");
						
						SimpleSISClient.currentUser.setPreference(UserPreferences.AUTO_SAVE_TIMER, newPreference);
						setAutoSaveInterval(newPreference);
						resetAutosaveTimer();
					}
				};
				
				for (String value : new String[] {"-1", "2", "5", "10"}) {
					CheckMenuItem interval = new CheckMenuItem("-1".equals(value) ? "Off" : "Every " + value + " minutes.");
					interval.setData("value", value);
					interval.setGroup(UserPreferences.AUTO_SAVE_TIMER);
					interval.setChecked("-1".equals(value) ? autoSaveInterval == null : Integer.valueOf(value).equals(autoSaveInterval));
					interval.addSelectionListener(listener);
					timedMenu.add(interval);
				}
				
				timedAutoSave.setSubMenu(timedMenu);
			}
			MenuItem onPageChange = new MenuItem("On Page Change..."); {
				Menu pageChangeMenu = new Menu();
				
				String savePreference = 
					SimpleSISClient.currentUser.getPreference(UserPreferences.AUTO_SAVE, UserPreferences.AutoSave.PROMPT);
				
				SelectionListener<MenuEvent> listener = new SelectionListener<MenuEvent>() {
					public void componentSelected(MenuEvent ce) {
						String newPreference = ce.getItem().getData("value");
						SimpleSISClient.currentUser.setPreference(UserPreferences.AUTO_SAVE, newPreference);
					}
				};
				
				CheckMenuItem autoSave = new CheckMenuItem("Auto-Save");
				autoSave.setData("value", UserPreferences.AutoSave.DO_ACTION);
				autoSave.setGroup(UserPreferences.AUTO_SAVE);
				autoSave.setChecked(savePreference.equals(UserPreferences.AutoSave.DO_ACTION));
				autoSave.addSelectionListener(listener);
				autoSave.setToolTip("When switching pages or assessments, any unsaved changes to an " +
					"assessment will automatically be saved.");
				pageChangeMenu.add(autoSave);
				
				CheckMenuItem autoPrompt = new CheckMenuItem("Prompt Before Auto-Save");
				autoPrompt.setData("value", UserPreferences.AutoSave.PROMPT);
				autoPrompt.setGroup(UserPreferences.AUTO_SAVE);
				autoPrompt.setChecked(savePreference.equals(UserPreferences.AutoSave.PROMPT));
				autoPrompt.addSelectionListener(listener);
				autoPrompt.setToolTip("When switching pages or assessments, you will be prompted " +
					"to save your changes if any unsaved changes are detected.");
				pageChangeMenu.add(autoPrompt);
				
				if(SimpleSISClient.currentUser.getUsername().equalsIgnoreCase("admin")){
					CheckMenuItem ignore = new CheckMenuItem("Ignore");
					ignore.setData("value", UserPreferences.AutoSave.IGNORE);
					ignore.setGroup(UserPreferences.AUTO_SAVE);
					ignore.setChecked(savePreference.equals(UserPreferences.AutoSave.IGNORE));
					ignore.addSelectionListener(listener);
					ignore.setToolTip("When switching pages or assessments, any unsaved changes to an " +
						"assessment will be thrown away; you will not be prompted to save them, nor " +
						"will they be automatically saved.  Only clicking the \"Save\" button will save " +
						"changes.");
					pageChangeMenu.add(ignore);
				}
				onPageChange.setSubMenu(pageChangeMenu);
			}
			MenuItem quickChangeLog = new MenuItem("Quick Change Log");
			{
				quickChangeLog.addSelectionListener(new SelectionListener<MenuEvent>() {
					public void componentSelected(MenuEvent ce) {
						AssessmentChangePacket[] packets = changeLog.getAll();
						if (packets.length == 0) {
							WindowUtils.errorAlert("No recent changes logged for this assessment.");
							return;
						}
						
						Menu changes = new Menu();
						DateTimeFormat fmt = DateTimeFormat.getFormat("h:mm aa zzz");
						for (int i = packets.length - 1; i >= 0; i--) {
							final AssessmentChangePacket packet = packets[i];
							Date date = new Date(packet.getVersion());
							MenuItem change = new MenuItem(fmt.format(date));
							change.addSelectionListener(new SelectionListener<MenuEvent>() {
								public void componentSelected(MenuEvent ce) {
									showQuickChanges(packet);
								}
							});
							
							changes.add(change);
						}
						
						changes.show(ce.getItem());
					}
				});
			}
			
			Menu saveModeOptions = new Menu();
			saveModeOptions.add(onPageChange);
			saveModeOptions.add(timedAutoSave);
			saveModeOptions.add(quickChangeLog);
			
			saveMode.setMenu(saveModeOptions);
		}
		add(saveMode);
		
		add(new FillToolItem());
	}
	
	private void showQuickChanges(AssessmentChangePacket packet) {
		final Window window = WindowUtils.newWindow("Quick Change Log");
		window.setModal(false);
		window.setAutoHide(true);
		window.setLayout(new FitLayout());
		window.setSize(450, 300);
		window.addButton(new Button("Close", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				window.hide();
			}
		}));
		
		final HtmlContainer area = new HtmlContainer();
		area.setHtml(packet.toHTML() + "<br/></br>" +
			"Version: " + packet.getVersion() + " by " + SISClientBase.currentUser.getUsername() + 
			" (" + SISClientBase.currentUser.getId() + ")<br/><br/>" +
			"If this is not what you intended to save, please copy the entire contents of " +
			"this window and report this as a bug in Assembla or to your system " +
			"administrator."
		);
		
		window.add(area);
		window.show();
	}
	
	public void resetAutosaveTimer() {
		autoSave.cancel();
		startAutosaveTimer();
	}
	
	public void startAutosaveTimer() {
		if (autoSaveInterval != null && AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, AuthorizableObject.WRITE, AssessmentCache.impl.getCurrentAssessment())) {
			Debug.println("Starting autosave.");
			autoSave.schedule(autoSaveInterval.intValue() * 60 * 1000);
		}
	}

	public void stopAutosaveTimer() {
		Debug.println("Stopping autosave.");
		autoSave.cancel();
	}
	
	public void setViewOnly(boolean viewOnly) {
		setViewOnly(viewOnly, editViewButton.isEnabled());
	}
	
	public void setViewOnly(boolean viewOnly, boolean enabled) {
		if (viewOnly) {
			editViewButton.setText("Edit Data Mode");
			editViewButton.setIconStyle("icon-unlocked");
		}
		else {
			editViewButton.setText("Read Only Mode");
			editViewButton.setIconStyle("icon-read-only");
		}
		editViewButton.setEnabled(enabled);
	}
	
	private void runIntegrityValidator() {
		final Assessment data = AssessmentCache.impl.getCurrentAssessment();
		//Popup new window:
		ClientAssessmentValidator.validate(data.getId(), data.getType());
	}

	private void fetchReport() {		
		ReportOptionsPanel panel = new ReportOptionsPanel();
		panel.loadAssessmentReport(AssessmentCache.impl.getCurrentAssessment().getId());
		
	}
	
	public void log(AssessmentChangePacket packet) {
		changeLog.add(packet);
	}
	
	private class AutosaveTimer extends Timer {
		public void run() {
			if (WindowUtils.loadingBox != null && WindowUtils.loadingBox.isVisible()) {
				// loading panel is up ... don't shoot!
				resetAutosaveTimer();
				return;
			}

			try {
				if (!ClientUIContainer.bodyContainer.isAssessmentEditor())
					return;

				final SISView currentView = ViewCache.impl.getCurrentView();
				boolean save = currentView != null && currentView.getCurPage() != null &&
					AssessmentClientSaveUtils.shouldSaveCurrentAssessment(currentView.getCurPage().getMyFields());
				if (save) {
					AssessmentClientSaveUtils.saveAssessment(currentView.getCurPage().getMyFields(),
							AssessmentCache.impl.getCurrentAssessment(), new GenericCallback<AssessmentChangePacket>() {
						public void onFailure(Throwable arg0) {
							WindowUtils.errorAlert("Save Failed", "Failed to save assessment! " + arg0.getMessage());
							startAutosaveTimer();
						}

						public void onSuccess(AssessmentChangePacket arg0) {
							Info.display("Auto-save Complete", "Successfully auto-saved assessment {0}.",
									AssessmentCache.impl.getCurrentAssessment().getSpeciesName());
							startAutosaveTimer();
							if (saveListener != null)
								saveListener.handleEvent();
							
							log(arg0);
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
	
	private static class ChangeLog {
		
		private AssessmentChangePacket [] array;
	    
	    int start;
	    int end;
	    int capacity;
	   
	    public ChangeLog(int capacity) {
	        array = new AssessmentChangePacket[capacity];
	        this.capacity = capacity;
	        start = 0;
	        end = 0 ;
	    }
	   
	    public void add(AssessmentChangePacket object) {
	    	   	
	        if( end == capacity ) {
	        	array[start] = object;
	        	start = (start+1) % capacity;
	        } else {
	             array[end++] = object;
	        }
	    }
	   
	    @SuppressWarnings("unused")
	    public AssessmentChangePacket get(int i) {
	    	if (i > end)
	    		throw new IndexOutOfBoundsException("Index " + i + " out of bounds.");
	    	
	        if( end == capacity ) {
	            //offset i using start
	            return array[(i + start) % capacity];
	        } else
	            return array[i];
	    }

	    public AssessmentChangePacket[] getAll() {
	    	int size = end < capacity ? end : capacity; 
	    	final AssessmentChangePacket[] out = new AssessmentChangePacket[size];
	    	for (int i = 0; i < size; i++)
	    		out[i] = array[(i + start) % capacity];
	    	return out;
	    }
	    
	}
	
}
