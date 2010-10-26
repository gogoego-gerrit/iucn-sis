package org.iucn.sis.client.tabs;

import org.iucn.sis.client.api.caches.WorkingSetCache;
import org.iucn.sis.client.api.ui.models.workingset.WSStore;
import org.iucn.sis.client.panels.PanelManager;
import org.iucn.sis.shared.api.models.WorkingSet;

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
		final Integer currentlySelectedID = panelManager.workingSetHierarchy.getCurrentlySelectedWorkingSetID();
		final WorkingSet ws = WorkingSetCache.impl.getCurrentWorkingSet();
		
		if (ws != null && currentlySelectedID != null && !currentlySelectedID.equals(ws.getId())) {
			panelManager.workingSetHierarchy.setCurrentlySelected(ws.getId());
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
	}

}
