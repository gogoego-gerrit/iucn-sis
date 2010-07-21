package org.iucn.sis.client.components.tabs;

import org.iucn.sis.client.components.panels.PanelManager;
import org.iucn.sis.client.components.panels.workingsets.WSStore;
import org.iucn.sis.shared.data.WorkingSetCache;
import org.iucn.sis.shared.data.WorkingSetData;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.solertium.util.extjs.client.WindowUtils;

public class WorkingSetTab extends TabItem {

	private PanelManager panelManager;
	//private ContentPanel west;
	private ContentPanel content;

	public WorkingSetTab(PanelManager manager) {
		super();
		panelManager = manager;
		setText("Working Set Page");
		build();
	}

	private void build() {
		setLayout(new BorderLayout());

		BorderLayoutData westData = new BorderLayoutData(LayoutRegion.WEST, (float) .2, 100, 600);
		//FlowLayout westLayout = new FlowLayout();
		//west = new ContentPanel(new FillLayout());
		//west.setBorders(true);

		BorderLayoutData centerData = new BorderLayoutData(LayoutRegion.CENTER, (float) .8, 75, 1200);

		ContentPanel westContent = buildHierarchy();
		westContent.setSize("100%", "100%");
		
		add(panelManager.workingSetBrowser, centerData);
		
		panelManager.addPanel(this, westContent, westData, false, false);
		panelManager.workingSetBrowser.setSize("100%", "100%");
	}

	private ContentPanel buildHierarchy() {
		content = new ContentPanel();
		content.setLayout(new FillLayout());
		content.setStyleName("x-panel");
		content.setHeading("Working Set Navigation");
		content.add(panelManager.workingSetHierarchy);
		return content;
	}

	public void redraw() {
		WSStore.getStore().update();
		panelManager.workingSetBrowser.refresh();
		panelManager.workingSetHierarchy.updateSelected();
	}

	/**
	 * Call whenever the taxon has changed in order to update the working set
	 * tab to reflect the new working set information.
	 * 
	 */
	public void workingSetChanged() {
		try {

			final String currentlySelectedID = panelManager.workingSetHierarchy.getCurrentlySelectedWorkingSetID();
			final WorkingSetData ws = WorkingSetCache.impl.getCurrentWorkingSet();
			if (ws != null && currentlySelectedID != null && !currentlySelectedID.equalsIgnoreCase(ws.getId())) {
				WindowUtils.confirmModalAlert("Working Set Change", "The current working set  was just changed to "
						+ ws.getWorkingSetName() + " while you were viewing the working set "
						+ panelManager.workingSetHierarchy.getCurrentlySelectedWorkingSetName() + ".  "
						+ "Would you like to view the " + ws.getWorkingSetName() + " or the "
						+ panelManager.workingSetHierarchy.getCurrentlySelectedWorkingSetName() + " working set?",
						new WindowUtils.MessageBoxListener() {

							@Override
							public void onNo() {
								panelManager.workingSetHierarchy.setCurrentlySelected(currentlySelectedID);

							}

							@Override
							public void onYes() {
								panelManager.workingSetHierarchy.setCurrentlySelected(ws.getId());

							}
						}, ws.getWorkingSetName(), panelManager.workingSetHierarchy
								.getCurrentlySelectedWorkingSetName());
			} else if (ws == null && currentlySelectedID != null) {
				WindowUtils.confirmModalAlert("Working Set Change",
						"There is no current working set, however you are viewing the "
								+ panelManager.workingSetHierarchy.getCurrentlySelectedWorkingSetName()
								+ " working set.  " + "Would you like continue working on the "
								+ panelManager.workingSetHierarchy.getCurrentlySelectedWorkingSetName()
								+ " working set?", new WindowUtils.MessageBoxListener() {

							@Override
							public void onNo() {
								panelManager.workingSetHierarchy.setCurrentlySelected(null);

							}

							@Override
							public void onYes() {
								panelManager.workingSetHierarchy.setCurrentlySelected(currentlySelectedID);

							}
						}, "Yes", "No");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
