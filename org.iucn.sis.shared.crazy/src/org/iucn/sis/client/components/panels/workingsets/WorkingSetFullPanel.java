package org.iucn.sis.client.components.panels.workingsets;

import java.util.HashMap;
import java.util.List;

import org.iucn.sis.client.acl.AuthorizationCache;
import org.iucn.sis.client.components.ClientUIContainer;
import org.iucn.sis.client.components.panels.PanelManager;
import org.iucn.sis.client.simple.SimpleSISClient;
import org.iucn.sis.client.utilities.FormattedDate;
import org.iucn.sis.shared.acl.base.AuthorizableObject;
import org.iucn.sis.shared.data.TaxonomyCache;
import org.iucn.sis.shared.data.WorkingSetCache;
import org.iucn.sis.shared.data.WorkingSetData;
import org.iucn.sis.shared.taxonomyTree.TaxonNode;
import org.iucn.sis.shared.xml.XMLUtils;

import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.InfoConfig;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.extjs.gxt.ui.client.widget.tree.TreeItem;
import com.extjs.gxt.ui.client.widget.treetable.TreeTable;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.util.extjs.client.WindowUtils;

public class WorkingSetFullPanel extends ContentPanel {

	// private static final String USERTEXT = "User Assessments";
	// private static final String DRAFTTEXT = "Draft Assessments";
	// private static final String PUBLISHEDTEXT = "Published Assessments";
	static final String EDIT = "edit";
	static final String NEW = "new";
	static final String READ = "read";
	static final String DRAFTONLY = "draftOnly";
	static final String ALL = "all";

	LayoutContainer content = null;
	private LayoutContainer assessmentTable = null;
	private WorkingSetData currentWorkingSet = null;
	private PanelManager panelManager = null;
	VerticalPanel basicInfo = null;
	private Grid grid = null;
	private boolean built;
	private AsyncTree treeInfo = null;
	private TreeTable tree = null;
	private List<TreeItem> treeItems = null;
	private ToolBar toolbar = null;
	private ToolBar taxonToolbar = null;
	private Button jumpItem = null;

	/**
	 * mode will either be edit, new or read depending on which mode it is in
	 */
	private String mode = null;

	/**
	 * either DRAFTONLY or ALL depending on what type of assessments the user
	 * wants to see
	 */
	private String viewMode;
	private HashMap taxonNameToTaxonID = null;

	public WorkingSetFullPanel(PanelManager manager) {
		// super(Style.HEADER, "x-panel");
		// panelManager = manager;
		// content = new LayoutContainer(){
		//			
		// protected void onResize(int width, int height) {
		// super.onResize(width, height);
		// treeInfo.resize();
		// }
		// };
		// basicInfo = new VerticalPanel();
		// built = false;
		// taxonNameToTaxonID = new HashMap();
		// viewMode = DRAFTONLY;
		// mode = READ;

	}

	public void addDraftAssessments() {
		// String id = null;
		// if (tree.getSelectedItem() != null){
		// id = (String)
		// taxonNameToTaxonID.get(tree.getSelectedItem().getText());
		// }
		// AddDraftAssessmentsPanel add = new
		// AddDraftAssessmentsPanel(panelManager, id);
		// add.show();
	}

	private ToolBar addTaxonToWorkingSet() {
		ToolBar buttons = new ToolBar();

		Button item = new Button();
		item.setText(" Add Taxon ");
		item.setIconStyle("icon-add");
		item.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				Window s = WindowUtils.getWindow(false, false, "Add Taxon to Working Set "
						+ currentWorkingSet.getWorkingSetName());
				// panelManager.addTaxonPanel.updateWorkingSet();
				s.add(panelManager.addTaxonPanel);
				s.setSize(600, 600);
				s.show();
			}
		});
		buttons.add(item);

		item = new Button();
		item.setText("Modify List");
		item.setIconStyle("icon-preferences-wrench");
		item.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				if (currentWorkingSet == null || currentWorkingSet.getSpeciesIDs().size() == 0) {
					WindowUtils.errorAlert("Taxa must already be added to your working set.");
				} else {
					Window s = WindowUtils.getWindow(false, false, "Move Taxa from Working Set "
							+ currentWorkingSet.getWorkingSetName());
					// panelManager.workingSetOptionsPanel.update();
					s.add(panelManager.workingSetOptionsPanel);
					s.setSize(600, 600);
					s.show();
				}
			}
		});

		buttons.add(item);
		return buttons;

	}

	public void buildInfo() {
		if (!built) {
			// treeInfo = new AsyncTree(panelManager);
			tree = treeInfo.getTree();

			assessmentTable = new LayoutContainer() {
				@Override
				protected void onResize(int width, int height) {
					super.onResize(width, height);
					treeInfo.resize();
				}
			};

			buildToolBar();

			add(content);
			content.setSize("100%", "100%");
			setSize("100%", "100%");

			grid = new Grid(4, 2);
			grid.setCellSpacing(10);
			HTML html = new HTML("Manager:  ");
			html.addStyleName("bold");
			grid.setWidget(0, 0, html);
			html = new HTML("Date Created:  ");
			html.addStyleName("bold");
			grid.setWidget(1, 0, html);
			html = new HTML("Working Set Name:  ");
			html.addStyleName("bold");
			grid.setWidget(2, 0, html);
			html = new HTML("Description:  ");
			html.addStyleName("bold");
			grid.setWidget(3, 0, html);
			grid.getColumnFormatter().setWidth(0, "130px");
			grid.getRowFormatter().addStyleName(3, "vertical-align-top");

			basicInfo.setSpacing(4);
			basicInfo.addStyleName("expert-border");
			basicInfo.add(grid);

			content.add(basicInfo);
			grid.setWidth("100%");
			basicInfo.setWidth("100%");

			content.add(assessmentTable);
			assessmentTable.setSize("100%", "100%");

			final LayoutContainer treeHolder = new LayoutContainer() {
				@Override
				protected void afterRender() {
					super.afterRender();
					treeInfo.resize();
				}

				@Override
				protected void onResize(int width, int height) {
					super.onResize(width, height);
					treeInfo.resize();
				}
			};
			assessmentTable.add(treeHolder);
			treeHolder.setSize("100%", "100%");
			treeHolder.add(tree);

		}

		if (currentWorkingSet == null || !currentWorkingSet.equals(WorkingSetCache.impl.getCurrentWorkingSet())) {
			refresh();
		} else
			WindowUtils.hideLoadingAlert();

	}

	// VIEWING FUNCTIONS AND INNER CLASSES
	private void buildToolBar() {
		toolbar = new ToolBar();
		putNonEditToolbar();
		add(toolbar);
		taxonToolbar = addTaxonToWorkingSet();
		assessmentTable.insert(taxonToolbar, 0);
	}

	private void changeViewMode() {
		tree.collapseAll();
		List<TreeItem> items = tree.getRootItem().getItems();

		for (TreeItem item : items) {
			item.removeAll();
		}
	}

	/**
	 * compares fields in the grid, and in the current working set and checks to
	 * see if a save is needed
	 * 
	 * @return
	 */
	private boolean checkToSeeIfSaveNeeded() {
		if (currentWorkingSet != null) {
			String oldDes = currentWorkingSet.getDescription();
			String oldName = currentWorkingSet.getWorkingSetName();
			String newName = ((TextBox) grid.getWidget(2, 1)).getText();
			String newDes = ((TextArea) grid.getWidget(3, 1)).getText();
			List<TreeItem> newTreeItems = tree.getAllItems();

			StringBuffer newitems = new StringBuffer();
			// CHECK TO SEE IF WE NEED TO SAVE, RETURN IF WE DON'T
			if (oldDes.equals(newDes) && (oldName.equals(newName)) && (newTreeItems.size() == treeItems.size())) {
				StringBuffer olditems = new StringBuffer();
				for (int i = 0; i < newTreeItems.size(); i++) {
					newitems.append(taxonNameToTaxonID.get(newTreeItems.get(i).getText()) + ",");
					olditems.append(taxonNameToTaxonID.get(treeItems.get(i).getText()) + ",");
				}

				if (olditems.toString().equalsIgnoreCase(newitems.toString())) {
					return false;
				}
			}
		}

		return true;

	}

	private void clearEditCells() {
		grid.clearCell(2, 1);
		grid.clearCell(3, 1);
	}

	private void clearNewCells() {
		grid.clearCell(1, 1);
		grid.clearCell(0, 1);
	}

	// WORKING SET FUNCTIONS
	public void deleteWorkingSet() {
		DeleteWorkingSetPanel delete = new DeleteWorkingSetPanel(panelManager);
		delete.show();
	}

	private void editWorkingSet() {

		WorkingSetData ws = currentWorkingSet;
		treeItems = tree.getAllItems();

		// CHECK TO MAKE SURE THAT THERE IS A CURRENT WORKING SET,
		// OTHERWISE SEND IT TO newWorkingSet()
		if (ws == null) {
			WindowUtils.errorAlert("Please select a working set first.");
			return;
		}

		// CHECK TO MAKE SURE THE USER IS ALLOWED TO EDIT WORKING SET
		if (AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, AuthorizableObject.WRITE, ws)) {
			WindowUtils.errorAlert("You are not authorized to edit the " + currentWorkingSet.getWorkingSetName()
					+ " working set.");
			return;
		}

		mode = EDIT;
		setTitle();

		// CHANGE CONTENTS OF CELLS
		clearEditCells();
		TextBox name = new TextBox();
		name.setText(ws.getWorkingSetName());
		grid.setWidget(2, 1, name);
		name.setWidth("100%");

		TextArea description = new TextArea();
		description.setText(ws.getDescription());
		grid.setWidget(3, 1, description);
		description.setSize("100%", "100px");

		// DISPLAY CORRECT TOOLBARS
		taxonToolbar.show();
		putEditToolbar();
		treeInfo.resize();

	}

	public VerticalPanel getBasicInfo() {
		return basicInfo;
	}

	public LayoutContainer getContent() {
		return content;
	}

	public Button getJumpItem() {
		return jumpItem;
	}

	public String getMode() {
		return mode;
	}

	public String getViewMode() {
		return viewMode;
	}

	private void jumpTo() {
		TreeItem selected = tree.getSelectionModel().getSelectedItem();
		String name = selected.getText();
		taxonNameToTaxonID = treeInfo.getTaxonNameToTaxonID();

		// GO TO TAXON DATA BROWSER
		if (taxonNameToTaxonID.containsKey(name)) {
			TaxonomyCache.impl.fetchNode((String) taxonNameToTaxonID.get(name), true, new GenericCallback<TaxonNode>() {
				public void onFailure(Throwable caught) {
					Info.display(new InfoConfig("Error", "Error loading taxonomy browser."));
				}

				public void onSuccess(TaxonNode arg0) {
					ClientUIContainer.bodyContainer
							.setSelection(ClientUIContainer.bodyContainer.tabManager.taxonHomePage);
				}

			});

		}

		// GO TO ASSESSMENT BROWSER
		else {
			// String parent = selected.getParentItem().getText();
			// if (name.matches(".*Draft.*")){
			// // AssessmentCache.impl.fetchDraftAssessment(
			// (String)taxonNameToTaxonID.get(parent), true, new
			// GenericCallback<String>(){
			// // public void onFailure(Throwable caught) {
			// // Info.display( new InfoConfig("Error",
			// "Error loading assessment browser.") );
			// // }
			// // public void onSuccess(String arg0) {
			// //
			// ClientUIContainer.bodyContainer.setSelection(ClientUIContainer.
			// bodyContainer.tabManager.assessmentEditor);
			// // }
			//					
			// });
			// }
			// else if (name.matches(".*User.*")){
			// AssessmentCache.impl.fetchUserAssessment((String)taxonNameToTaxonID
			// .get(parent), true, new GenericCallback<String>(){
			// public void onFailure(Throwable caught) {
			// Info.display( new InfoConfig("Error",
			// "Error loading assessment browser.") );
			// }
			// public void onSuccess(String arg0) {
			// ClientUIContainer.bodyContainer.setSelection(ClientUIContainer.
			// bodyContainer.tabManager.assessmentEditor);
			// }
			//					
			// });
			// }
			// else {
			// //GET ID OF THE PUBLISHED ASSESSMENT, NOT VERY EFFICIENT IF CAN
			// FIGURE OUT A WAY TO ACCESS ID HERE
			// TreeItem [] possibleItems = selected.getParentItem().getItems();
			// int i = 0;
			// while (possibleItems[i] != selected && i < possibleItems.length){
			// i++;
			// }
			// final int count = i;
			// TaxonomyCache.impl.fetchNode((String)taxonNameToTaxonID.get(parent
			// ), true, new GenericCallback<String>(){
			// public void onFailure(Throwable caught) {
			// Info.display( new InfoConfig("Error",
			// "Error loading assessment browser.") );
			// }
			// public void onSuccess(String arg0) {
			// TaxonNode node = (TaxonNode)arg0;
			//						
			// if (count < node.getAssessments().size()){
			// String id = (String)node.getAssessments().get(count);
			// AssessmentCache.impl.fetchPublishedAssessment(id, true, new
			// GenericCallback<String>(){
			// public void onFailure(Throwable caught) {
			// Info.display( new InfoConfig("Error",
			// "Error loading assessment browser.") );
			// }
			// public void onSuccess(String arg0) {
			// ClientUIContainer.bodyContainer.setSelection(ClientUIContainer.
			// bodyContainer.tabManager.assessmentEditor);
			// }
			//								
			// });
			// }
			// else {
			// Info.display( new InfoConfig("Error",
			// "Error loading assessment browser.") );
			// }
			// }
			//					
			// });
			// }
		}
	}

	public void newWorkingSet() {
		mode = NEW;
		setTitle();
		clearNewCells();
		clearEditCells();

		// CHANGE TO NEW INFO
		HTML userName = new HTML(SimpleSISClient.currentUser.getUsername());
		grid.setWidget(0, 1, userName);

		HTML date = new HTML(FormattedDate.impl.getDate());
		grid.setWidget(1, 1, date);

		TextBox name = new TextBox();
		grid.setWidget(2, 1, name);
		name.setWidth("100%");

		TextArea description = new TextArea();
		grid.setWidget(3, 1, description);
		description.setSize("100%", "100px");

		// CHANGE TOOLBARS
		taxonToolbar.show();
		putEditToolbar();

		// SET CURRENT WORKING SET IN CLASS TO A NEW WORKING SET
		currentWorkingSet = new WorkingSetData();
		tree.getRootItem().removeAll();
		treeInfo.resize();

	}

	private void putEditToolbar() {
		toolbar.removeAll();

		Button item = new Button();
		item.setIconStyle("icon-save");
		item.setText("Save");
		item.setTitle("Save");
		item.addListener(Events.Select, new Listener() {
			public void handleEvent(BaseEvent be) {
				saveWorkingSet(false);
			}

		});
		toolbar.add(item);

		item = new Button();
		item.setIconStyle("icon-save-and-exit");
		item.setText("Save and Exit");
		item.setTitle("Save and Exit");
		item.addListener(Events.Select, new Listener() {
			public void handleEvent(BaseEvent be) {
				saveWorkingSet(true);
			}

		});
		toolbar.add(item);

		item = new Button();
		item.setIconStyle("icon-cancel");
		item.setText("Cancel");
		item.setTitle("Cancel");
		item.addListener(Events.Select, new Listener() {
			public void handleEvent(BaseEvent be) {
				refresh();
				WSStore.getStore().update();
				// panelManager.workingSetHierarchy.update();
			}

		});

		toolbar.add(item);

	}

	private void putNonEditToolbar() {

		toolbar.removeAll();

		Button item = new Button();
		item.setIconStyle("icon-note-edit");
		item.setText(" Edit ");
		item.setTitle("Edit Working Set");
		item.addListener(Events.OnMouseUp, new Listener<BaseEvent>() {
			public void handleEvent(BaseEvent be) {
				editWorkingSet();
			}
		});
		toolbar.add(item);

		item = new Button();
		item.setIconStyle("icon-new-document");
		item.setText("Create Draft Assessment");
		item.setTitle("Create new draft assessment");
		item.addListener(Events.Select, new Listener<BaseEvent>() {
			public void handleEvent(BaseEvent be) {
				addDraftAssessments();
			};
		});

		toolbar.add(item);

		jumpItem = new Button();
		jumpItem.setIconStyle("icon-go-jump");
		jumpItem.setText(" View Selected in Browser ");
		jumpItem.setTitle("View Selected in Browser");
		jumpItem.addListener(Events.OnMouseUp, new Listener<BaseEvent>() {
			public void handleEvent(BaseEvent be) {
				jumpTo();
			}
		});
		toolbar.add(jumpItem);

		// TODO: ADD THIS BACK IN WHEN ALLOWING DIFFERENT MODES
		// ListBox modeList = new ListBox(false);
		// modeList.addItem("View Draft Assessments", DRAFTONLY);
		// modeList.addItem("View all Assessments", ALL);
		// if (viewMode.equals(DRAFTONLY)){
		// modeList.setSelectedIndex(0);
		// }
		// else {
		// modeList.setSelectedIndex(1);
		// }
		// modeList.addChangeListener(new ChangeListener() {
		// public void onChange(Widget sender) {
		// ListBox widget = (ListBox)sender;
		// viewMode = widget.getValue(widget.getSelectedIndex());
		// changeViewMode();
		// }
		//		
		// });
		// ButtonAdapter menu = new ButtonAdapter(modeList);
		// toolbar.add(menu);

	}

	public void refresh() {
		WindowUtils.showLoadingAlert("Loading ...");
		Timer timer = new Timer() {

			@Override
			public void run() {
				refreshDelayed();
			}
		};
		timer.schedule(500);

	}

	public void refreshAssessmentTable() {
		refreshAssessmentTable(null);
	}

	/**
	 * Given a string of taxon Ids, refresh that part of the tree, csv is null,
	 * refreshes entire tree
	 * 
	 * @param csv
	 *            -- taxon ids
	 */
	public void refreshAssessmentTable(String csv) {
		if (built) {
			treeInfo.resize();
			treeInfo.update(csv);
			assessmentTable.layout();
		}
	}

	private void refreshDelayed() {
		currentWorkingSet = WorkingSetCache.impl.getCurrentWorkingSet();
		putNonEditToolbar();
		clearEditCells();
		taxonToolbar.hide();
		mode = READ;
		setTitle();

		if (currentWorkingSet == null) {
			grid.setText(0, 1, "N/A");
			grid.setText(1, 1, "N/A");
			grid.setText(2, 1, "N/A");
			grid.setText(3, 1, "N/A");
		} else {
			grid.setHTML(0, 1, currentWorkingSet.getCreator());
			grid.setHTML(1, 1, currentWorkingSet.getDate());
			grid.setHTML(2, 1, currentWorkingSet.getWorkingSetName());
			grid.setHTML(3, 1, currentWorkingSet.getDescription());
		}

		refreshAssessmentTable();
		content.layout();
		layout();
		built = true;
		WindowUtils.hideLoadingAlert();

	}

	/**
	 * sends in a boolean if editted, true if old working set was in edit mode,
	 * false if new working set
	 * 
	 * @param editted
	 */
	private void saveWorkingSet(final boolean saveAndExit) {

		boolean saveNeeded = true;
		String description = ((TextArea) grid.getWidget(3, 1)).getText();
		final String name = ((TextBox) grid.getWidget(2, 1)).getText();

		// CHECK TO MAKE SURE FIELDS ARE ENTERED IN
		if (name == null || name.trim().equals("")) {
			WindowUtils.errorAlert("Please enter a working set name.");
			return;
		}

		else if (description == null || description.trim().equals("")) {
			WindowUtils.errorAlert("Please enter a working set description.");
			return;
		}

		else if (mode.equalsIgnoreCase(EDIT))
			saveNeeded = checkToSeeIfSaveNeeded();

		// IF WE NEED TO SAVE
		if (saveNeeded) {

			List<TreeItem> newTreeItems = tree.getAllItems();
			StringBuffer newitems = new StringBuffer();
			if (newTreeItems != null) {
				for (TreeItem item : newTreeItems)
					newitems.append(taxonNameToTaxonID.get(item.getText()) + ",");
			}
			currentWorkingSet.setDescription(XMLUtils.clean(description));
			currentWorkingSet.setWorkingSetName(XMLUtils.clean(name));

			// IF WE ARE EDITING A WORKING SET
			if (mode.equalsIgnoreCase(EDIT)) {
				WorkingSetCache.impl.editWorkingSet(currentWorkingSet, new GenericCallback<String>() {

					public void onFailure(Throwable caught) {
						Info.display(new InfoConfig("ERROR", "Error saving working set " + name));
					}

					public void onSuccess(String arg0) {
						Info.display(new InfoConfig("Successful Save", "Successfully saved working set " + name));
						refresh();
						// panelManager.workingSetHierarchy.update();
						WSStore.getStore().update();
					}
				});
			}

			// IF WE ARE ADDING A NEW WORKING SET
			else {
				currentWorkingSet.setDate(((HTML) grid.getWidget(1, 1)).getText());
				currentWorkingSet.setCreator(((HTML) grid.getWidget(0, 1)).getText());

				WorkingSetCache.impl.addToPrivateWorkingSets(currentWorkingSet, new GenericCallback<String>() {
					public void onFailure(Throwable caught) {
						Info.display(new InfoConfig("ERROR", "Error saving working set " + name));
					}

					public void onSuccess(String arg0) {
						Info.display(new InfoConfig("Successful Save", "Successfully saved working set " + name));
						if (saveAndExit) {
							WorkingSetCache.impl.setCurrentWorkingSetData(currentWorkingSet);
							refresh();
						}
						// panelManager.workingSetHierarchy.update();
						WSStore.getStore().update();

					}
				});
			}

		}

		// DIDN'T NEED TO SAVE
		else {

			Info.display(new InfoConfig("Save", "Save not needed"));

		}

	}

	private void setTitle() {
		if (mode.equals(NEW)) {
			setHeading("New Working Set");
		} else if (mode.equals(EDIT)) {
			setHeading("Edit Working Set --- " + currentWorkingSet.getWorkingSetName());
		}

		else {
			if (currentWorkingSet == null) {
				setHeading("Please Select A Working Set");
			} else
				setHeading("Working Set --- " + currentWorkingSet.getWorkingSetName());
		}
	}

}
