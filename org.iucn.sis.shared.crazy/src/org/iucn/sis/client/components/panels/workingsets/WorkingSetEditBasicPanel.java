package org.iucn.sis.client.components.panels.workingsets;

import org.iucn.sis.client.acl.AuthorizationCache;
import org.iucn.sis.client.components.panels.PanelManager;
import org.iucn.sis.client.simple.SimpleSISClient;
import org.iucn.sis.shared.acl.base.AuthorizableObject;
import org.iucn.sis.shared.data.WorkingSetCache;
import org.iucn.sis.shared.data.WorkingSetData;

import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.solertium.util.extjs.client.WindowUtils;

public class WorkingSetEditBasicPanel extends WorkingSetNewWSPanel {

	public WorkingSetEditBasicPanel(PanelManager manager) {
		super(manager);
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
		WorkingSetData ws = WorkingSetCache.impl.getCurrentWorkingSet();
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
//		System.out.println("I am in refreshcells ");
		WorkingSetData ws = WorkingSetCache.impl.getCurrentWorkingSet();

		managerHTML.setText(ws.getCreator());
		dateCreatedHTML.setText(ws.getDate());
		workingSetName.setText(ws.getWorkingSetName());
		description.setText(ws.getDescription());
		notes.setText(ws.getNotes());
		people.setItemsInList(ws.getPeopleAsCSV());
		filterPanel.setFilter(ws.getFilter());

		int height = people.getOffsetHeight() + 20;
		grid.getCellFormatter().setHeight(PEOPLECELL, 1, height + "px");

		String mode = ws.getMode();
		if (mode.equalsIgnoreCase(WorkingSetData.PRIVATE)) {
			workingSetMode.setItemSelected(PRIVATEINDEX, true);
		} else if (mode.equalsIgnoreCase(WorkingSetData.PUBLIC)) {
			workingSetMode.setItemSelected(PUBLICINDEX, true);
		} else
			workingSetMode.setItemSelected(0, true);
		workingSetMode.setEnabled(false);


	}

}
