package org.iucn.sis.client.components.panels.workingsets;

import org.iucn.sis.client.acl.AuthorizationCache;
import org.iucn.sis.client.components.ClientUIContainer;
import org.iucn.sis.client.components.panels.PanelManager;
import org.iucn.sis.client.components.panels.filters.AssessmentFilterPanel;
import org.iucn.sis.client.simple.SimpleSISClient;
import org.iucn.sis.client.ui.RefreshLayoutContainer;
import org.iucn.sis.client.utilities.FormattedDate;
import org.iucn.sis.shared.acl.base.AuthorizableObject;
import org.iucn.sis.shared.data.WorkingSetCache;
import org.iucn.sis.shared.data.WorkingSetData;
import org.iucn.sis.shared.data.assessments.AssessmentFilter;
import org.iucn.sis.shared.structures.SISCompleteList;
import org.iucn.sis.shared.xml.XMLUtils;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.WindowEvent;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.InfoConfig;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.solertium.lwxml.gwt.debug.SysDebugger;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.util.extjs.client.WindowUtils;

public class WorkingSetNewWSPanel extends RefreshLayoutContainer {

	public static final int SAVE = 0;
	public static final int SAVEANDEXIT = 1;
	public static final int SAVEANDCONTINUE = 2;

	protected static final int PRIVATEINDEX = 1;
	protected static final int PUBLICINDEX = 2;

	protected static final int PEOPLECELL = 4;

	private PanelManager manager = null;
	protected FlexTable grid = null;
	protected HTML managerHTML = null;
	protected HTML dateCreatedHTML = null;
	protected AssessmentFilterPanel filterPanel = null; 
	protected TextBox workingSetName = null;
	protected TextArea description = null;
	protected TextArea notes = null;
	protected ToolBar toolbar = null;
	protected ListBox workingSetMode = null;
	protected SISCompleteList people = null;

	protected Button saveAndAddTaxa = null;
	protected Button save = null;
	protected Button saveAndExit = null;
	protected Button cancel = null;
	protected Button currentlyModifying = null;

	/*
	 * The id is null if it hasn't be called by save yet, else aftercancel or
	 * save and exit is clicked, it needs to be reset at null.This variable
	 * knows if you need to save to an existing working set,or if you need to
	 * create a new working set.
	 */
	protected String id = null;

	public WorkingSetNewWSPanel(PanelManager manager) {
		super();
		this.manager = manager;
		build();
	}

	protected void addItemsToToolBar() {
		toolbar.add(saveAndAddTaxa);
		toolbar.add(new SeparatorToolItem());
		toolbar.add(save);
		toolbar.add(new SeparatorToolItem());
		toolbar.add(saveAndExit);
		toolbar.add(new SeparatorToolItem());
		toolbar.add(cancel);
		toolbar.add(new SeparatorToolItem());
	}

	private void build() {
		removeAll();
		RowLayout layout = new RowLayout();
		RowData north = new RowData(1d, 25);
		RowData center = new RowData(1d, 1d);
		setLayout(layout);
		// setScrollMode(Scroll.AUTO);
		addStyleName("gwt-background");

		buildToolbar(north);
		buildContent(center);
	}

	private void buildContent(RowData data) {
		grid = new FlexTable();
		grid.setCellSpacing(5);

		HTML html = new HTML("Working Set Name:  ");
		html.addStyleName("color-dark-blue");
		workingSetName = new TextBox();
		grid.setWidget(0, 0, html);
		grid.setWidget(0, 1, workingSetName);
		workingSetName.setSize("95%", "100%");

		html = new HTML("Creator:  ");
		html.addStyleName("color-dark-blue");
		managerHTML = new HTML();
		grid.setWidget(1, 0, html);
		grid.setWidget(1, 1, managerHTML);

		html = new HTML("Date Created:  ");
		html.addStyleName("color-dark-blue");
		dateCreatedHTML = new HTML();
		grid.setWidget(2, 0, html);
		grid.setWidget(2, 1, dateCreatedHTML);

		html = new HTML("Working Set Type:  ");
		html.addStyleName("color-dark-blue");
		workingSetMode = new ListBox(false);
		workingSetMode.insertItem("none selected", 0);
		workingSetMode.insertItem(WorkingSetData.PRIVATE, PRIVATEINDEX);
		workingSetMode.insertItem(WorkingSetData.PUBLIC, PUBLICINDEX);
		workingSetMode.setVisibleItemCount(1);
		workingSetMode.setEnabled(false);
		workingSetMode.setSelectedIndex(PUBLICINDEX);
		grid.setWidget(3, 0, html);
		grid.setWidget(3, 1, workingSetMode);

		html = new HTML("Associated People:  ");
		html.addStyleName("color-dark-blue");
		people = new SISCompleteList(this, 400);
		grid.setWidget(PEOPLECELL, 0, html);
		grid.setWidget(PEOPLECELL, 1, people);
		
		html = new HTML("Assessment Scope:  ");
		html.addStyleName("color-dark-blue");
		filterPanel = new AssessmentFilterPanel(new AssessmentFilter(), false, true, false, true);
		grid.setWidget(5, 0, html);
		grid.setWidget(5, 1, filterPanel);

		html = new HTML("Description:  ");
		html.addStyleName("color-dark-blue");
		description = new TextArea();
		grid.setWidget(6, 0, html);
		grid.setWidget(6, 1, description);
		description.setSize("95%", "100%");

		html = new HTML("Working Set Notes: ");
		html.addStyleName("color-dark-blue");
		notes = new TextArea();
		grid.setWidget(7, 0, html);
		grid.setWidget(7, 1, notes);
		notes.setSize("95%", "100%");

		grid.getColumnFormatter().setWidth(0, "130px");
		grid.getColumnFormatter().setWidth(1, "400px");
		grid.getRowFormatter().addStyleName(5, "vertical-align-top");
		grid.getRowFormatter().addStyleName(6, "vertical-align-top");
		grid.getRowFormatter().addStyleName(7, "vertical-align-top");
		grid.getCellFormatter().setHeight(6, 1, "200px");
		grid.getCellFormatter().setHeight(7, 1, "200px");

		HorizontalPanel hp = new HorizontalPanel();
		hp.setVerticalAlignment(HasVerticalAlignment.ALIGN_TOP);
		hp.addStyleName("expert-border");
		hp.add(grid);

		// add(hp, data);

		LayoutContainer widget = new LayoutContainer();
		widget.setScrollMode(Scroll.AUTO);
		widget.setLayout(new FillLayout());
		widget.add(hp);
		add(widget, data);
		grid.setWidth("100%");

	}

	private void buildToolbar(RowData data) {
		toolbar = new ToolBar();

		saveAndAddTaxa = new Button();
		saveAndAddTaxa.setText("Save and Add Taxa");
		saveAndAddTaxa.setIconStyle("icon-go-jump");
		saveAndAddTaxa.setTitle("Save and Add Taxa");
		saveAndAddTaxa.addListener(Events.Select, new Listener() {
			public void handleEvent(BaseEvent be) {
				save(SAVEANDCONTINUE);
			}
		});

		save = new Button();
		save.setText("Save");
		save.setIconStyle("icon-save");
		save.setTitle("Save and Continue Editing");
		save.addListener(Events.Select, new Listener() {
			public void handleEvent(BaseEvent be) {
				save(SAVE);
			}
		});

		saveAndExit = new Button();
		saveAndExit.setIconStyle("icon-save-and-exit");
		saveAndExit.setText("Save and Exit");
		saveAndExit.setTitle("Save and Exit without adding taxa");
		saveAndExit.addListener(Events.Select, new Listener() {
			public void handleEvent(BaseEvent be) {
				save(SAVEANDEXIT);
			}
		});

		cancel = new Button();
		cancel.setIconStyle("icon-cancel");
		cancel.setText("Cancel");
		cancel.setTitle("Cancel");
		cancel.addListener(Events.Select, new Listener() {
			public void handleEvent(BaseEvent be) {
				cancel();
			}
		});

		addItemsToToolBar();
		add(toolbar, data);
	}

	private void cancel() {
		manager.workingSetBrowser.setManagerTab();
	}

	protected void clearCells() {
		managerHTML.setText("");
		dateCreatedHTML.setText("");
//		workingSetMode.setEnabled(true);
//		workingSetMode.setSelectedIndex(0);
//		workingSetName.setEnabled(true);
		filterPanel.setFilter(new AssessmentFilter());
		workingSetName.setText("");
		description.setEnabled(true);
		description.setText("");
		notes.setEnabled(true);
		notes.setText("");
		people.enable();
		people.clearItemsInList();
	}

	protected void disableCells() {
		workingSetMode.setEnabled(false);
		workingSetName.setEnabled(false);
		description.setEnabled(false);
		filterPanel.setEnabled(false);
		notes.setEnabled(false);
		people.disable();
	}

	protected void enableSaveButtons(boolean enable) {
		if (enable)
			for (int i = 0; i < toolbar.getItemCount(); i++) {
				toolbar.getItem(i).enable();
			}
		else
			for (int i = 0; i < toolbar.getItemCount(); i++) {
				toolbar.getItem(i).disable();
			}
	}

	/**
	 * Should be called when creating a new working set
	 */
	public void refresh() {
		id = null;

		clearCells();
		managerHTML.setText(SimpleSISClient.currentUser.getUsername());
		dateCreatedHTML.setText(FormattedDate.impl.getDate());

	}

	public void resize() {
		int height2 = people.getOffsetHeight() + 22;
		grid.getCellFormatter().setHeight(PEOPLECELL, 1, height2 + "px");
		layout();
		SysDebugger.getInstance().println("I am in resize with height2 " + height2);
	}

	private void save(final int saveMode) {
		enableSaveButtons(false);

		String date = dateCreatedHTML.getText();
		String manager = managerHTML.getText();
		String descriptionText = XMLUtils.clean(description.getText());
		String notesText = XMLUtils.clean(notes.getText());
		final String name = XMLUtils.clean(workingSetName.getText());
		final String errorFilter = filterPanel.checkValidity();
		
		// CHECK TO MAKE SURE FIELDS ARE ENTERED IN
		if (name == null || name.trim().equals("")) {
			WindowUtils.errorAlert("Please enter a working set name.");
			enableSaveButtons(true);
			return;
		}
		
		if (errorFilter != null) {
			WindowUtils.errorAlert(errorFilter);
		}

		else if (descriptionText == null || descriptionText.trim().equals("")) {
			WindowUtils.errorAlert("Please enter a working set description.");
			enableSaveButtons(true);
			return;
		}

		else if (workingSetMode.getSelectedIndex() != PRIVATEINDEX && workingSetMode.getSelectedIndex() != PUBLICINDEX) {
			WindowUtils.errorAlert("Please select the working set type.");
			enableSaveButtons(true);
			return;
		}

		// HAVEN'T SAVED BEFORE
		if (id == null) {
			final WorkingSetData currentWorkingSet = new WorkingSetData();
			currentWorkingSet.setDate(date);
			currentWorkingSet.setCreator(manager);
			currentWorkingSet.setDescription(descriptionText);
			currentWorkingSet.setNotes(notesText);
			currentWorkingSet.setWorkingSetName(name);
			currentWorkingSet.setMode(workingSetMode.getItemText(workingSetMode.getSelectedIndex()));
			currentWorkingSet.setPeople(people.getItemsInList());
			currentWorkingSet.setFilter(filterPanel.getFilter());

			if (currentWorkingSet.getMode().equalsIgnoreCase(WorkingSetData.PRIVATE)) {
				savePrivateWorkingSet(currentWorkingSet, saveMode);
				workingSetMode.setEnabled(false);
			} else {
				savePublicWorkingSet(currentWorkingSet, saveMode);
				workingSetMode.setEnabled(false);
			}

			// this.manager.workingSetHierarchy.update();
			WSStore.getStore().update();
		}
		// ALREADY HAVE BEEN SAVED BEFORE, REALLY EDITTING
		else {

			final WorkingSetData currentWorkingSet = (WorkingSetData) WorkingSetCache.impl.getWorkingSets().get(id);

			if (currentWorkingSet != null) {
				currentWorkingSet.setDescription(descriptionText);
				currentWorkingSet.setNotes(notesText);
				currentWorkingSet.setWorkingSetName(name);
				currentWorkingSet.setPeople(people.getItemsInList());
				currentWorkingSet.setFilter(filterPanel.getFilter());
				String mode = currentWorkingSet.getMode();
				String newMode = workingSetMode.getItemText(workingSetMode.getSelectedIndex());

				if (!mode.equalsIgnoreCase(newMode)) {

					// CHANGING TO PRIVATE
					if (newMode.equalsIgnoreCase(WorkingSetData.PRIVATE)) {

						WindowUtils.confirmAlert("Warning", "Changing a working set from public to private creates"
								+ " a copy of the working set into your private directory but does not"
								+ " delete the working set out of the public directory.", new Listener<MessageBoxEvent>() {
							public void handleEvent(MessageBoxEvent be) {
								if (be.getType() == Events.Close) {
									if (be.getButtonClicked().getType().equals(MessageBox.OK)) {
										savePublicToPrivateWorkingSet(currentWorkingSet, saveMode);
									}
								}

							}

						});

					}

					// CHANGING TO PUBLIC
					if (newMode.equalsIgnoreCase(WorkingSetData.PRIVATE)) {

						WindowUtils.confirmAlert("Warning",
								"Changing a working set from private to public will allow your "
										+ "working set to be viewed by other people.", new Listener<MessageBoxEvent>() {
									public void handleEvent(MessageBoxEvent be) {
										if (be.getType() == Events.Close) {
											if (be.getButtonClicked().getType().equals(MessageBox.OK)) {
												savePrivateToPublicWorkingSet(currentWorkingSet, saveMode);
											}
										}

									}

								});

					}
				} else if (currentWorkingSet.getMode().equalsIgnoreCase(WorkingSetData.PRIVATE))
					savePrivateWorkingSet(currentWorkingSet, saveMode);
				else
					savePublicWorkingSet(currentWorkingSet, saveMode);

			}

		}

	}

	private void savePrivateToPublicWorkingSet(final WorkingSetData currentWorkingSet, final int saveMode) {
		enableSaveButtons(true);
	}

	private void savePrivateWorkingSet(final WorkingSetData currentWorkingSet, final int saveMode) {

		final String name = currentWorkingSet.getWorkingSetName();
		AssessmentFilter filter = filterPanel.getFilter();
		currentWorkingSet.setFilter(filter);
		if (id == null) {

			WorkingSetCache.impl.addToPrivateWorkingSets(currentWorkingSet, new GenericCallback<String>() {
				public void onFailure(Throwable caught) {
					Info.display("ERROR", "Error saving working set " + name + ".", "Please" + " try saving again.");
					enableSaveButtons(true);
				}

				public void onSuccess(String arg0) {
					id = currentWorkingSet.getId();
					WSStore.getStore().update();
					ClientUIContainer.bodyContainer.tabManager.panelManager.workingSetHierarchy
							.setCurrentlySelected(id);
					if (saveMode == SAVE) {
						Info.display(new InfoConfig("Successful Save", "Successfully saved working set " + name));
						manager.workingSetBrowser.setEditWorkingSetTab();
					} else if (saveMode == SAVEANDEXIT) {
						Info.display(new InfoConfig("Successful Save", "Successfully saved working set " + name));
						manager.workingSetBrowser.setManagerTab();
					} else {
						manager.workingSetBrowser.setEditTaxaTab();
						manager.workingSetOptionsPanel.forceRefreshTaxaList(WorkingSetOptionsPanel.ADDBROWSE);
					}
					enableSaveButtons(true);
				}
			});

		}

		else {

			WorkingSetCache.impl.editWorkingSet(currentWorkingSet, new GenericCallback<String>() {
				public void onFailure(Throwable caught) {
					Info.display("ERROR", "Error saving working set " + name + ".", "Please" + " try saving again.");
					enableSaveButtons(true);
				}

				public void onSuccess(String arg0) {
					id = currentWorkingSet.getId();
					WSStore.getStore().update();
					ClientUIContainer.bodyContainer.tabManager.panelManager.workingSetHierarchy
							.setCurrentlySelected(id);
					if (saveMode == SAVE) {
						Info.display(new InfoConfig("Successful Save", "Successfully saved working set " + name));
					} else if (saveMode == SAVEANDEXIT) {
						Info.display(new InfoConfig("Successful Save", "Successfully saved working set " + name));
						manager.workingSetBrowser.setManagerTab();
					} else {
						manager.workingSetBrowser.setEditTaxaTab();
						manager.workingSetOptionsPanel.forceRefreshTaxaList(WorkingSetOptionsPanel.ADDBROWSE);
					}
					enableSaveButtons(true);
				}
			});
		}
	}

	private void savePublicToPrivateWorkingSet(final WorkingSetData currentWorkingSet, final int saveMode) {
		enableSaveButtons(true);
	}

	private void savePublicWorkingSet(final WorkingSetData currentWorkingSet, final int saveMode) {

		final String name = currentWorkingSet.getWorkingSetName();
		currentWorkingSet.setFilter(filterPanel.getFilter());
		if (id == null) {

			WorkingSetCache.impl.addToPublicWorkingSets(currentWorkingSet, new GenericCallback<String>() {
				public void onFailure(Throwable caught) {
					Info.display("ERROR", "Error saving working set " + name + ".", "Please" + " try saving again.");
				}

				public void onSuccess(String arg0) {
					id = currentWorkingSet.getId();
					WSStore.getStore().update();
					ClientUIContainer.bodyContainer.tabManager.panelManager.workingSetHierarchy
							.setCurrentlySelected(id);
					if (saveMode == SAVE) {
						Info.display(new InfoConfig("Successful Save", "Successfully saved working set " + name));
						manager.workingSetBrowser.setEditWorkingSetTab();
					} else if (saveMode == SAVEANDEXIT) {
						Info.display(new InfoConfig("Successful Save", "Successfully saved working set " + name));
						manager.workingSetBrowser.setManagerTab();
					} else {
						manager.workingSetBrowser.setEditTaxaTab();
						manager.workingSetOptionsPanel.forceRefreshTaxaList(WorkingSetOptionsPanel.ADDBROWSE);
					}

					enableSaveButtons(true);
				}
			});

		}

		else if (AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, AuthorizableObject.WRITE, WorkingSetCache.impl.getCurrentWorkingSet())) {
			WorkingSetCache.impl.editWorkingSet(currentWorkingSet, new GenericCallback<String>() {
				public void onFailure(Throwable caught) {
					Info.display("ERROR", "Error saving working set " + name + ".", "Please" + " try saving again.");
				}

				public void onSuccess(String arg0) {
					WSStore.getStore().update();
					ClientUIContainer.bodyContainer.tabManager.panelManager.workingSetHierarchy
							.setCurrentlySelected(id);
					if (saveMode == SAVE) {
						Info.display(new InfoConfig("Successful Save", "Successfully saved working set " + name));
					} else if (saveMode == SAVEANDEXIT) {
						Info.display(new InfoConfig("Successful Save", "Successfully saved working set " + name));
						manager.workingSetBrowser.setManagerTab();
					} else {
						manager.workingSetBrowser.setEditTaxaTab();
						manager.workingSetOptionsPanel.forceRefreshTaxaList(WorkingSetOptionsPanel.ADDBROWSE);
					}
					enableSaveButtons(true);
				}
			});

		} else {
			WindowUtils.errorAlert("You do not have permissions to edit this working set.");
			enableSaveButtons(true);
		}
	}

}
