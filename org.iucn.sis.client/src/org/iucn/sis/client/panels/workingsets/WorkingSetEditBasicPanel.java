package org.iucn.sis.client.panels.workingsets;

import org.iucn.sis.client.api.caches.AuthorizationCache;
import org.iucn.sis.client.api.caches.WorkingSetCache;
import org.iucn.sis.client.api.utils.FormattedDate;
import org.iucn.sis.client.container.SimpleSISClient;
import org.iucn.sis.client.tabs.WorkingSetPage;
import org.iucn.sis.shared.api.acl.base.AuthorizableObject;
import org.iucn.sis.shared.api.models.AssessmentFilter;
import org.iucn.sis.shared.api.models.WorkingSet;

import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.solertium.util.extjs.client.WindowUtils;

public class WorkingSetEditBasicPanel extends WorkingSetNewWSPanel {

	public WorkingSetEditBasicPanel(WorkingSetPage parent) {
		super(parent);
	}

	@Override
	protected void addItemsToToolBar() {
		toolbar.add(save);
		toolbar.add(new SeparatorToolItem());
		toolbar.add(saveAndExit);
		toolbar.add(new SeparatorToolItem());
		toolbar.add(cancel);
		toolbar.add(new SeparatorToolItem());

		currentlyModifying = new Button();
		currentlyModifying.setText("Currently Modifying: ");
		toolbar.add(currentlyModifying);
	}

	public boolean hasPermissions() {
		WorkingSet ws = WorkingSetCache.impl.getCurrentWorkingSet();
		if (ws == null)
			return false;
		else 
			return AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, AuthorizableObject.WRITE, ws);
	}

	@Override
	public void refresh() {
		clearCells();

		if (hasPermissions()) {
			id = WorkingSetCache.impl.getCurrentWorkingSet().getId();

			refreshCells();

			// REFRESH TITLE
			currentlyModifying.setText("Currently Modifying: "
					+ WorkingSetCache.impl.getCurrentWorkingSet().getWorkingSetName());
		} else {
			disableCells();
			currentlyModifying.setText("Please select a working set");
			if (WorkingSetCache.impl.getCurrentWorkingSet() == null) {
				WindowUtils.errorAlert("You must first select a working set to edit...");
			} else {
				WindowUtils.errorAlert("You can not edit a public working set unless you are the "
						+ "creator of the working set.  Please select a different working set to edit.");
			}
		}
		layout();
	}

	private void refreshCells() {
		WorkingSet ws = WorkingSetCache.impl.getCurrentWorkingSet();
		managerHTML.setText(ws.getCreator().getUsername());
		dateCreatedHTML.setText(FormattedDate.impl.getDate(ws.getCreatedDate()));
		workingSetName.setText(ws.getWorkingSetName());
		description.setText(ws.getDescription());
		notes.setText(ws.getNotes());
		filterPanel.setFilter(getFilter());
	}
	
	@Override
	protected AssessmentFilter getFilter() {
		WorkingSet ws = WorkingSetCache.impl.getCurrentWorkingSet();
		return ws.getFilter();
	}

}
