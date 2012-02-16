package org.iucn.sis.client.panels.workingsets;

import java.util.Date;

import org.iucn.sis.client.api.caches.AuthorizationCache;
import org.iucn.sis.client.api.caches.WorkingSetCache;
import org.iucn.sis.client.api.ui.models.workingset.WSStore;
import org.iucn.sis.client.api.utils.FormattedDate;
import org.iucn.sis.client.container.SimpleSISClient;
import org.iucn.sis.client.panels.ClientUIContainer;
import org.iucn.sis.client.panels.filters.AssessmentFilterPanel;
import org.iucn.sis.client.panels.utils.RefreshLayoutContainer;
import org.iucn.sis.client.tabs.WorkingSetPage;
import org.iucn.sis.shared.api.acl.base.AuthorizableObject;
import org.iucn.sis.shared.api.models.AssessmentFilter;
import org.iucn.sis.shared.api.models.WorkingSet;
import org.iucn.sis.shared.api.utils.XMLUtils;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.InfoConfig;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
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
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.util.events.ComplexListener;
import com.solertium.util.events.SimpleListener;
import com.solertium.util.extjs.client.WindowUtils;

public class WorkingSetNewWSPanel extends RefreshLayoutContainer {

	public static final int SAVE = 0;
	public static final int SAVEANDEXIT = 1;
	public static final int SAVEANDCONTINUE = 2;

	protected static final int PRIVATEINDEX = 1;
	protected static final int PUBLICINDEX = 2;

	protected static final int PEOPLECELL = 4;

	protected FlexTable grid = null;
	protected HTML managerHTML = null;
	protected HTML dateCreatedHTML = null;
	protected AssessmentFilterPanel filterPanel = null; 
	protected TextBox workingSetName = null;
	protected TextArea description = null;
	protected TextArea notes = null;
	protected ToolBar toolbar = null;

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
	protected Integer id = null;
	
	//private final WorkingSetPage parent;
	
	protected SimpleListener cancelListener;
	protected ComplexListener<WorkingSet> closeListener;
	protected ComplexListener<WorkingSet> saveNewListener;
	protected ComplexListener<WorkingSet> afterSaveListener;
	protected ComplexListener<WorkingSet> saveExistingListener;
	
	public WorkingSetNewWSPanel() {
		super();
		
		build();
	}

	public WorkingSetNewWSPanel(final WorkingSetPage parent) {
		super();
		cancelListener = new SimpleListener() {
			public void handleEvent() {
				parent.refreshFeature();
				parent.setManagerTab();	
			}
		};
		closeListener = new ComplexListener<WorkingSet>() {
			public void handleEvent(WorkingSet eventData) {
				parent.refreshFeature();
				parent.setManagerTab();
			}
		};
		saveNewListener = new ComplexListener<WorkingSet>() {
			public void handleEvent(WorkingSet eventData) {
				parent.refreshFeature();
				parent.setEditWorkingSetTab();
			}
		};
		afterSaveListener = new ComplexListener<WorkingSet>() {
			public void handleEvent(WorkingSet eventData) {
				parent.refreshFeature();
				parent.setEditTaxaTab();
			}
		};
		saveExistingListener = new ComplexListener<WorkingSet>() {
			public void handleEvent(WorkingSet eventData) {
				parent.refreshFeature();
			}
		};
		build();
	}
	
	public void setCancelListener(SimpleListener cancelListener) {
		this.cancelListener = cancelListener;
	}
	
	public void setCloseListener(ComplexListener<WorkingSet> closeListener) {
		this.closeListener = closeListener;
	}
	
	public void setSaveNewListener(ComplexListener<WorkingSet> saveNewListener) {
		this.saveNewListener = saveNewListener;
	}
	
	public void setAfterSaveListener(ComplexListener<WorkingSet> afterSaveListener) {
		this.afterSaveListener = afterSaveListener;
	}
	
	protected AssessmentFilter getFilter() {
		return new AssessmentFilter();
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

		html = new HTML("Assessment Scope:  ");
		html.addStyleName("color-dark-blue");
		filterPanel = new AssessmentFilterPanel(new AssessmentFilter(), false, true, false, true);
		grid.setWidget(3, 0, html);
		grid.setWidget(3, 1, filterPanel);

		html = new HTML("Description:  ");
		html.addStyleName("color-dark-blue");
		description = new TextArea();
		grid.setWidget(4, 0, html);
		grid.setWidget(4, 1, description);
		description.setSize("95%", "100%");
		
		html = new HTML("Working Set Notes: ");
		html.addStyleName("color-dark-blue");
		notes = new TextArea();
		grid.setWidget(5, 0, html);
		grid.setWidget(5, 1, notes);
		notes.setSize("100%", "100%");

		grid.getColumnFormatter().setWidth(0, "130px");
		grid.getColumnFormatter().setWidth(1, "400px");
		grid.getRowFormatter().addStyleName(3, "vertical-align-top");
		grid.getRowFormatter().addStyleName(4, "vertical-align-top");
		grid.getRowFormatter().addStyleName(5, "vertical-align-top");
		grid.getCellFormatter().setHeight(4, 1, "200px");
		
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
		saveAndAddTaxa.addSelectionListener(new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				save(SAVEANDCONTINUE);
			}
		});

		save = new Button();
		save.setText("Save");
		save.setIconStyle("icon-save");
		save.setTitle("Save and Continue Editing");
		save.addSelectionListener(new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				save(SAVE);
			}
		});

		saveAndExit = new Button();
		saveAndExit.setIconStyle("icon-save-and-exit");
		saveAndExit.setText("Save and Exit");
		saveAndExit.setTitle("Save and Exit without adding taxa");
		saveAndExit.addSelectionListener(new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				save(SAVEANDEXIT);
			}
		});

		cancel = new Button();
		cancel.setIconStyle("icon-cancel");
		cancel.setText("Cancel");
		cancel.setTitle("Cancel");
		cancel.addSelectionListener(new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				fireCancelListener();
			}
		});

		addItemsToToolBar();
		add(toolbar, data);
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
//		people.enable();
//		people.clearItemsInList();
	}

	protected void disableCells() {
		workingSetName.setEnabled(false);
		description.setEnabled(false);
		filterPanel.setEnabled(false);
		notes.setEnabled(false);
//		people.disable();
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

//	public void resize() {
//		int height2 = people.getOffsetHeight() + 22;
//		grid.getCellFormatter().setHeight(PEOPLECELL, 1, height2 + "px");
//		layout();
//		SysDebugger.getInstance().println("I am in resize with height2 " + height2);
//	}

	private void save(final int saveMode) {
		enableSaveButtons(false);

		String descriptionText = description.getText();
		String notesText = notes.getText();
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
			return;
		}

		else if (descriptionText == null || descriptionText.trim().equals("")) {
			WindowUtils.errorAlert("Please enter a working set description.");
			enableSaveButtons(true);
			return;
		}

		// HAVEN'T SAVED BEFORE
		if (id == null) {
			final WorkingSet currentWorkingSet = new WorkingSet();
			currentWorkingSet.setCreatedDate(new Date());
			currentWorkingSet.setCreator(SimpleSISClient.currentUser);
			currentWorkingSet.setDescription(descriptionText);
			currentWorkingSet.setNotes(notesText);
			currentWorkingSet.setName(name);
			currentWorkingSet.setFilter(filterPanel.getFilter());	
			saveWorkingSet(currentWorkingSet, saveMode);
			WSStore.getStore().update();
		}
		// ALREADY HAVE BEEN SAVED BEFORE, REALLY EDITTING
		else {

			final WorkingSet currentWorkingSet = (WorkingSet) WorkingSetCache.impl.getWorkingSets().get(id);

			if (currentWorkingSet != null) {
				currentWorkingSet.setDescription(descriptionText);
				currentWorkingSet.setNotes(notesText);
				currentWorkingSet.setName(name);
				currentWorkingSet.setFilter(filterPanel.getFilter());
				saveWorkingSet(currentWorkingSet, saveMode);
			}

		}

	}

	
	private void saveWorkingSet(final WorkingSet currentWorkingSet, final int saveMode) {

		final String name = currentWorkingSet.getWorkingSetName();
		currentWorkingSet.setFilter(filterPanel.getFilter());
		if (id == null) {
			WorkingSetCache.impl.createWorkingSet(currentWorkingSet, new GenericCallback<String>() {
				public void onFailure(Throwable caught) {
					enableSaveButtons(true);
					Info.display("ERROR", "Error saving working set " + name + ".", "Please" + " try saving again.");
				}

				public void onSuccess(String arg0) {
					id = currentWorkingSet.getId();
					WSStore.getStore().update();
					/*ClientUIContainer.bodyContainer.tabManager.panelManager.workingSetHierarchy
							.setCurrentlySelected(id);*/
					if (saveMode == SAVE) {
						Info.display(new InfoConfig("Successful Save", "Successfully saved new working set " + name));
						fireSaveNewListener(currentWorkingSet);
					} else if (saveMode == SAVEANDEXIT) {
						Info.display(new InfoConfig("Successful Save", "Successfully saved new working set " + name));
						fireCloseListener(currentWorkingSet);
					} else {
						fireAfterSaveListener(currentWorkingSet);
						//manager.workingSetOptionsPanel.forceRefreshTaxaList(WorkingSetOptionsPanel.ADDBROWSE);
					}

					enableSaveButtons(true);
				}
			});

		}

		else if (AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, AuthorizableObject.WRITE, WorkingSetCache.impl.getCurrentWorkingSet())) {
			WorkingSetCache.impl.editWorkingSet(currentWorkingSet, new GenericCallback<String>() {
				public void onFailure(Throwable caught) {
					enableSaveButtons(true);
					Info.display("ERROR", "Error saving working set " + name + ".", "Please" + " try saving again.");
				}

				public void onSuccess(String arg0) {
					enableSaveButtons(true);
					WSStore.getStore().update();
					//Taxa can't change on this page, but assessments may
					ClientUIContainer.headerContainer.centerPanel.updateAssessmentList();
					if (saveMode == SAVE) {
						Info.display(new InfoConfig("Successful Save", "Successfully saved working set " + name));
						fireSaveExistingListener(currentWorkingSet);
					} else if (saveMode == SAVEANDEXIT) {
						Info.display(new InfoConfig("Successful Save", "Successfully saved working set " + name));
						fireCloseListener(currentWorkingSet);
					} else {
						fireAfterSaveListener(currentWorkingSet);
						//FIXME: manager.workingSetOptionsPanel.forceRefreshTaxaList(WorkingSetOptionsPanel.ADDBROWSE);
					}
				}
			});

		} else {
			WindowUtils.errorAlert("You do not have permissions to edit this working set.");
			enableSaveButtons(true);
		}
	}
	
	private void fireSaveNewListener(WorkingSet ws) {
		saveNewListener.handleEvent(ws);
	}
	
	private void fireCloseListener(WorkingSet ws) {
		closeListener.handleEvent(ws);
	}
	
	private void fireAfterSaveListener(WorkingSet ws) {
		afterSaveListener.handleEvent(ws);
	}
	
	private void fireCancelListener() {
		cancelListener.handleEvent();
	}

	private void fireSaveExistingListener(WorkingSet ws) {
		saveExistingListener.handleEvent(ws);
	}
	
}
