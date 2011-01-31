package org.iucn.sis.client.panels.workingsets;

import org.iucn.sis.client.api.caches.AuthorizationCache;
import org.iucn.sis.client.api.caches.WorkingSetCache;
import org.iucn.sis.client.container.SimpleSISClient;
import org.iucn.sis.client.panels.PanelManager;
import org.iucn.sis.shared.api.acl.base.AuthorizableObject;

import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.TabPanelEvent;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.solertium.util.extjs.client.WindowUtils;
import com.solertium.util.gwt.ui.DrawsLazily;

public class WorkingSetBrowser extends TabPanel {

	private PanelManager manager = null;
	private boolean built;
	private TabItem summaryTab = null;
	private TabItem reportTab = null;
	private TabItem editWorkingSetTab = null;
	private TabItem editTaxaTab = null;
	private TabItem managerTab = null;
	private TabItem exportTab = null;
	private TabItem accessExportTab = null;
	private TabItem importTab = null;
	private TabItem newWorkingSetTab = null;
	private TabItem deleteWorkingSetTab = null;
	private TabItem subscribeTab = null;
	private TabItem addAssessmentsTab = null;
	private TabItem permissionsTab = null;
	private TabItem workflowTab = null;
	private WorkingSetSummaryPanel summaryPanel = null;
	private WorkingSetReportPanel reportPanel = null;
	private WorkingSetWorkflowPanel workflowPanel = null;
	private WorkingSetManagerPanel managerPanel = null;
	private WorkingSetNewWSPanel addWorkingSetPanel = null;
	private WorkingSetEditBasicPanel editWorkingSetBasicInfo = null;
	private WorkingSetExporter exportPanel = null;
	private WorkingSetImporter importPanel = null;
	private WorkingSetSubscriber subscribePanel = null;
	private DeleteWorkingSetPanel deletePanel = null;
	private WorkingSetAddAssessmentsPanel addAssessmentsPanel = null;

	public WorkingSetBrowser(PanelManager manager) {
		this.manager = manager;

		setTabWidth(150);
		// TODO: This is not firing...;
		addListener(Events.Select, new Listener<TabPanelEvent>() {
			public void handleEvent(TabPanelEvent be) {
				refresh();
			}
		});
		build();
	}

	private void build() {
		if (!built) {
			summaryTab = new TabItem();
			summaryTab.setText("Summary");
			summaryPanel = new WorkingSetSummaryPanel();
			summaryTab.setLayout(new FillLayout());
			summaryTab.add(summaryPanel);

			reportTab = new TabItem();
			reportTab.getHeader().setVisible(false);
			reportTab.setText("Working Set Report");
			reportPanel = new WorkingSetReportPanel();
			reportTab.setLayout(new FillLayout());
			reportTab.add(reportPanel);
			
			workflowTab = new TabItem();
			workflowTab.setText("Submit for Review/Publish");
			workflowTab.setLayout(new FillLayout());
			workflowTab.getHeader().setVisible(false);
			workflowTab.add(workflowPanel = new WorkingSetWorkflowPanel(manager));

			managerTab = new TabItem();
			managerTab.setText("Working Set Manager");
			managerPanel = new WorkingSetManagerPanel();
			managerTab.add(managerPanel);

			editWorkingSetTab = new TabItem();
			editWorkingSetTab.setText("Edit Basic Information");
			editWorkingSetTab.getHeader().setVisible(false);
			editWorkingSetBasicInfo = new WorkingSetEditBasicPanel(null);
			editWorkingSetTab.setLayout(new FillLayout());
			editWorkingSetTab.add(editWorkingSetBasicInfo);

			editTaxaTab = new TabItem();
			editTaxaTab.setText("Edit Working Set Taxa");
			editTaxaTab.getHeader().setVisible(false);
			editTaxaTab.setLayout(new FillLayout());
			editTaxaTab.add(manager.workingSetOptionsPanel);

			exportTab = new TabItem();
			exportTab.getHeader().setVisible(false);
			exportPanel = new WorkingSetExporter(null);
			exportTab.setLayout(new FillLayout());
			exportTab.add(exportPanel);

			accessExportTab = new TabItem();
			accessExportTab.addListener(Events.Show, new Listener<BaseEvent>() {
				public void handleEvent(BaseEvent be) {
					final String atarget = "/export/access/" + WorkingSetCache.impl.getCurrentWorkingSet().getId();
					accessExportTab.setUrl(atarget);
				}
			});
			accessExportTab.getHeader().setVisible(false);
			accessExportTab.setLayout(new FillLayout());

			importTab = new TabItem();
			importTab.getHeader().setVisible(false);
			importPanel = new WorkingSetImporter(manager);
			importTab.setLayout(new FillLayout());
			importTab.add(importPanel);

			newWorkingSetTab = new TabItem();
			newWorkingSetTab.getHeader().setVisible(false);
			addWorkingSetPanel = new WorkingSetNewWSPanel(null);
			newWorkingSetTab.setLayout(new FillLayout());
			newWorkingSetTab.add(addWorkingSetPanel);

			deleteWorkingSetTab = new TabItem();
			deleteWorkingSetTab.getHeader().setVisible(false);
			deletePanel = new DeleteWorkingSetPanel(null);
			deleteWorkingSetTab.setLayout(new FillLayout());
			deleteWorkingSetTab.add(deletePanel);

			subscribeTab = new TabItem();
			subscribeTab.getHeader().setVisible(false);
			subscribePanel = new WorkingSetSubscriber();
			subscribeTab.setLayout(new FillLayout());
			subscribeTab.add(subscribePanel);

			addAssessmentsTab = new TabItem();
			addAssessmentsTab.getHeader().setVisible(false);
			addAssessmentsPanel = new WorkingSetAddAssessmentsPanel(null);
			addAssessmentsTab.setLayout(new FillLayout());
			addAssessmentsTab.add(addAssessmentsPanel);
			
			permissionsTab = new TabItem();
			permissionsTab.getHeader().setVisible(false);
			permissionsTab.setLayout(new FitLayout());
			

			add(summaryTab);
			add(reportTab);
			add(workflowTab);
			add(managerTab);
			add(editWorkingSetTab);
			add(editTaxaTab);
			add(exportTab);
			add(accessExportTab);
			add(importTab);
			add(newWorkingSetTab);
			add(deleteWorkingSetTab);
			add(subscribeTab);
			add(addAssessmentsTab);
			add(permissionsTab);

			built = true;
		}
	}

	public void refresh() {
		TabItem selectedItem = getSelectedItem();
		if (selectedItem != null) {
			if (selectedItem.equals(summaryTab))
				summaryPanel.refresh();
			else if (selectedItem.equals(reportTab))
				reportPanel.refresh();
			else if (selectedItem.equals(editTaxaTab))
				manager.workingSetOptionsPanel.refresh();
			else if (selectedItem.equals(newWorkingSetTab)) {
				addWorkingSetPanel.refresh();
			} else if (selectedItem.equals(editWorkingSetTab))
				editWorkingSetBasicInfo.refresh();
			else if (selectedItem.equals(exportTab))
				exportPanel.refresh();
			else if (selectedItem.equals(importTab))
				importPanel.refresh();
			else if (selectedItem.equals(subscribeTab))
				subscribePanel.refresh();
			else if (selectedItem.equals(addAssessmentsTab))
				addAssessmentsPanel.refresh();
			else if (selectedItem.equals(reportTab))
				reportPanel.refresh();
			else if (selectedItem.equals(workflowTab)) {
				workflowPanel.draw(new DrawsLazily.DoneDrawingCallback() {
					public void isDrawn() {
						workflowPanel.layout();
					}
				});
			}

			selectedItem.layout();
		}
	}

	public void setAccessExportTab() {
		setSelection(accessExportTab);
	}

	public void setAssessmentTab() {
		setSelection(addAssessmentsTab);
	}

	public void setDeleteWorkingSetTab() {
		setSelection(deleteWorkingSetTab);
	}

	public void setEditTaxaTab() {
		setSelection(editTaxaTab);
	}

	public void setEditWorkingSetTab() {
		setSelection(editWorkingSetTab);
	}

	public void setExportTab() {
		setSelection(exportTab);
	}

	public void setImportTab() {
		setSelection(importTab);
	}

	public void setManagerTab() {
		setSelection(managerTab);
	}

	public void setNewWorkingSetTab() {
		setSelection(newWorkingSetTab);
	}

	public void setReportTab() {
		setSelection(reportTab);
	}
	
	public void setWorkflowTab() {
		setSelection(workflowTab);
	}

	public void setSubscribeTab() {
		setSelection(subscribeTab);
	}
	
	public void setPermissionTab() {
		if( WorkingSetCache.impl.getCurrentWorkingSet() == null )
			WindowUtils.errorAlert("Select a Working Set", "Please select a working set first.");
		else if( AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, AuthorizableObject.GRANT, 
				WorkingSetCache.impl.getCurrentWorkingSet())) {
				permissionsTab.removeAll();
				final WorkingSetPermissionPanel panel = new WorkingSetPermissionPanel();
				panel.draw(new DrawsLazily.DoneDrawingCallback() {
					public void isDrawn() {
						permissionsTab.add(panel);
						setSelection(permissionsTab);	
					}
				});
		} else
			WindowUtils.errorAlert("Insufficient Permissions", "You do not have permission to manage " +
					"the permissions for this Working Set.");
	}

}
