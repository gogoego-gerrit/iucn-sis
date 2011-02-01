package org.iucn.sis.client.panels.workingsets;

import com.extjs.gxt.ui.client.widget.LayoutContainer;

/**
 * Class that allows the user to select from all working set operations.
 * 
 * @author liz.schwartz
 * 
 */
public class WorkingSetManagerPanel extends LayoutContainer {

	/*private final WorkingSetPage parent;
	
	public WorkingSetManagerPanel(WorkingSetPage parent) {
		super();
		this.parent = parent;
		
		build();
		addStyleName("gwt-background");
		setScrollMode(Scroll.AUTO);
		setSize("100%", "100%");
	}

	private void addAssessments() {
		parent.setAssessmentTab();
	}

	private void build() {
		TableLayout layout = new TableLayout(2);
		setLayout(layout);
		TableData data1 = new TableData(HorizontalAlignment.CENTER, VerticalAlignment.MIDDLE);
		data1.setWidth("160px");
		TableData data2 = new TableData(HorizontalAlignment.LEFT, VerticalAlignment.MIDDLE);
		layout.setCellSpacing(30);

		HTML html = new HTML("Manager Selection");
		html.addStyleName("bold");
		add(html, data1);
		html = new HTML("Description");
		html.addStyleName("bold");
		add(html, data2);

		Button createWorkingSet = createButton("New Working Set", new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				createWorkingSet();

			}
		});
		html = new HTML("-- Create a new working set.", true);
		add(createWorkingSet, data1);
		add(html, data2);

		Button editWorkingSetBasicInformation = createButton("Edit Basic Information",
				new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				if (validateWorkingSet(false, true))
					editWorkingSet();
			}
		});
		html = new HTML("-- Edit the basic information in a "
				+ "working set of your choosing.  Editable information includes "
				+ "working set name, description, and notes.", true);
		add(editWorkingSetBasicInformation, data1);
		add(html, data2);

		Button editTaxa = createButton("Taxa Manager", new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				if (validateWorkingSet(false, true))
					editTaxa();
			}
		});
		html = new HTML("-- Add, move, copy and delete taxa in your " + "working sets.", true);
		add(editTaxa, data1);
		add(html, data2);

		Button addAssessments = createButton("Assessment Manager", new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				if (validateWorkingSet(true))
					addAssessments();
			}
		});
		html = new HTML("-- Create draft assessments for taxa in your working set.", true);
		add(addAssessments, data1);
		add(html, data2);

		Button reportManager = createButton("Report Generator", new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				reportGenerator();

			}
		});
		html = new HTML("-- Generate reports.", true);
		add(reportManager, data1);
		add(html, data2);

		Button permissionManager = createButton("Permission Manager", new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				viewPermissionManager();		
			}
		});
		html = new HTML("-- Manage authorized access to this working set");
		add(permissionManager, data1);
		add(html, data2);

		Button deleteWorkingSet = createButton("Delete/Unsubscribe", new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				deleteWorkingSet();
			}
		});
		html = new HTML("-- Delete or unsubscribe to your working sets.", true);
		add(deleteWorkingSet, data1);
		add(html, data2);

		Button subscribeWorkingSet = createButton("Subscribe", new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				subscribeToAWorkingSet();

			}
		});
		html = new HTML("-- Subscribe to public working sets.", true);
		add(subscribeWorkingSet, data1);
		add(html, data2);

		Button exportWorkingSet = createButton("Export to Offline", new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				exportWorkingSet();
			}
		});
		html = new HTML("-- Export the current working set for offline use.", true);
		add(exportWorkingSet, data1);
		add(html, data2);

		Button importWorkingSet = createButton("Import from Offline", new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				importWorkingSet();
			}
		});
		html = new HTML("-- Import a new working set.", true);
		add(importWorkingSet, data1);
		add(html, data2);

		Button exportAccessWorkingSet = createButton("Export to Access", new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				exportAccessWorkingSet();
			}
		});
		// exportAccessWorkingSet.setEnabled(false);
		html = new HTML("-- Export the current working set to a Microsoft Access DB.", true);
		add(exportAccessWorkingSet, data1);
		add(html, data2);
		
		final Button workflow = createButton("Submission Process", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				if (validateWorkingSet(true))
					workflowManager();
			}
		});
		add(workflow, data1);
		add(new HTML("-- View and manage the submission and publication process for assessments in this working set."), data2);
	}

	private Button createButton(String text, SelectionListener<ButtonEvent> listener) {
		Button b = new Button(text, listener);
		b.setMinWidth(150);

		return b;
	}
	
	private boolean validateWorkingSet(boolean mustSpecifyRegions) {
		return validateWorkingSet(mustSpecifyRegions, false);
	}
	
	private boolean validateWorkingSet(boolean mustSpecifyRegions, boolean mustBeDraftWorkflow) {
		WorkingSet current = WorkingSetCache.impl.getCurrentWorkingSet();
		if (current == null) {
			WindowUtils.errorAlert("You must first select a working set");
			return false;
		}
		
		if (mustSpecifyRegions && !current.getFilter().hasSpecificallySpecifiedRegion()) {
			WindowUtils.errorAlert("You must change the working set so that specific regions are selected and the locale must match \"all selected locales\". ");
			return false;
		}
		
		if (mustBeDraftWorkflow && !WorkflowStatus.DRAFT.matches(current.getWorkflowStatus())) {
			WindowUtils.errorAlert("This working set is undergoing the submission process (" + current.getWorkflowStatus() + ") and can not be changed.");
			return false;
		}
		
		return true;
	}
	

	private void createWorkingSet() {
		manager.workingSetBrowser.setNewWorkingSetTab();
	}

	private void deleteWorkingSet() {
		manager.workingSetBrowser.setDeleteWorkingSetTab();
	}

	private void editTaxa() {
		manager.workingSetBrowser.setEditTaxaTab();
	}

	private void editWorkingSet() {
		manager.workingSetBrowser.setEditWorkingSetTab();
	}

	private void exportAccessWorkingSet() {
		manager.workingSetBrowser.setAccessExportTab();
	}

	private void exportWorkingSet() {
		manager.workingSetBrowser.setExportTab();
	}

	private void importWorkingSet() {
		manager.workingSetBrowser.setImportTab();
	}

	private void reportGenerator() {
		manager.workingSetBrowser.setReportTab();
	}

	private void subscribeToAWorkingSet() {
		manager.workingSetBrowser.setSubscribeTab();
	}

	private void viewPermissionManager() {
		manager.workingSetBrowser.setPermissionTab();
	}
	
	private void workflowManager() {
		manager.workingSetBrowser.setWorkflowTab();
	}*/

}
