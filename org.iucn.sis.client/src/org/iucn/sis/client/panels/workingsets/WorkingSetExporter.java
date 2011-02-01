package org.iucn.sis.client.panels.workingsets;

import java.util.Iterator;
import java.util.Map;

import org.iucn.sis.client.api.caches.AuthorizationCache;
import org.iucn.sis.client.api.caches.RegionCache;
import org.iucn.sis.client.api.caches.TaxonomyCache;
import org.iucn.sis.client.api.caches.WorkingSetCache;
import org.iucn.sis.client.container.SimpleSISClient;
import org.iucn.sis.client.panels.utils.RefreshLayoutContainer;
import org.iucn.sis.client.tabs.WorkingSetPage;
import org.iucn.sis.shared.api.acl.base.AuthorizableObject;
import org.iucn.sis.shared.api.acl.feature.AuthorizableDraftAssessment;
import org.iucn.sis.shared.api.models.WorkingSet;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.DataListEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.DataList;
import com.extjs.gxt.ui.client.widget.DataListItem;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HTML;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.util.extjs.client.WindowUtils;

@SuppressWarnings("deprecation")
public class WorkingSetExporter extends RefreshLayoutContainer {

	private final WorkingSetPage parent;
	
	private Map<Integer, WorkingSet> workingSets = null;
	private HTML instructions = null;
	private DataList list = null;
	private Button exportButton = null;

	public WorkingSetExporter(WorkingSetPage parent) {
		super();
		this.parent = parent;
		
		addStyleName("gwt-background");

		setLayout(new BorderLayout());
		BorderLayoutData north = new BorderLayoutData(LayoutRegion.NORTH, 80);
		BorderLayoutData center = new BorderLayoutData(LayoutRegion.CENTER);
		BorderLayoutData south = new BorderLayoutData(LayoutRegion.SOUTH, 35f);

		instructions = new HTML("<b>Instructions:</b> Select a working set to "
				+ "export and click the export button.  A dialog box will appear and ask"
				+ " you where you like to save the zipped working set.  The zipped file "
				+ "will contain the entire working set including the basic information, the "
				+ "taxa information, and the draft assessments associated with each taxa if they" + " exist.");
		add(instructions, north);

		list = new DataList();
		list.setSelectionMode(SelectionMode.MULTI);
		list.setCheckable(true);
		list.addStyleName("gwt-background");
		add(list, center);
		list.setScrollMode(Scroll.AUTO);
		list.addListener(Events.CheckChange, new Listener<DataListEvent>() {
			public void handleEvent(DataListEvent be) {
				checkChange(be.getItem());
			}
		});

		ButtonBar buttons = new ButtonBar();
		buttons.setAlignment(HorizontalAlignment.LEFT);
		exportButton = new Button("Export", new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				export();
			}
		});
		buttons.add(exportButton);
		buttons.add(new Button("Cancel", new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				cancel();
			}
		}));
		add(buttons, south);

	}

	private void cancel() {
		parent.setManagerTab();
	}

	private void checkChange(DataListItem item) {
		DataListItem[] checked = list.getChecked().toArray(new DataListItem[0]);
		if (checked.length == 0) {
			exportButton.setEnabled(false);
		} else {
			for (int i = 0; i < checked.length; i++) {
				if (!item.equals(checked[i]))
					checked[i].setChecked(false);
			}
			exportButton.setEnabled(true);
		}
	}

	private void export() {
		final DataListItem[] checked = list.getChecked().toArray(new DataListItem[0]);
		if (checked.length == 1) {
			exportButton.setEnabled(false);
			final WorkingSet ws = WorkingSetCache.impl.getWorkingSet(Integer.valueOf(checked[0].getId()));
			
			if( SimpleSISClient.iAmOnline ) {
				WindowUtils.confirmAlert("Lock Assessments", "Would you like to lock the online version " +
						"of the draft assessments of the regions " + RegionCache.impl.getRegionNamesAsReadable(ws.getFilter()) + 
						" for this working set? You can only commit changes to online versions via an " +
						"import if you have obtained the locks.", new Listener<MessageBoxEvent>() {
					public void handleEvent(MessageBoxEvent be) {
						if( be.getButtonClicked().getText().equalsIgnoreCase("yes")) {
							startExport(checked, ws);
						} else {
							fireExport(checked, false);
						}
					}
				});
			} else {
				startExport(checked, ws);
			}
		} else if (checked.length == 0) {
			WindowUtils.errorAlert("Please select a working set to export.");
		} else {
			WindowUtils.errorAlert("Please only select 1 working set to export.");
		}
	}

	private void fireExport(final DataListItem[] checked, boolean lock) {
		WorkingSetCache.impl.exportWorkingSet(Integer.valueOf(checked[0].getId()), lock, new GenericCallback<String>() {
			public void onFailure(Throwable caught) {
				exportButton.setEnabled(true);
			}

			public void onSuccess(String arg0) {
				saveExportedZip((String) arg0, checked[0].getId());
				exportButton.setEnabled(true);
			}

		});
	}
	
	@Override
	public void refresh() {
		exportButton.setEnabled(false);
		list.removeAll();
		workingSets = WorkingSetCache.impl.getWorkingSets();

		Iterator<Integer> iter = workingSets.keySet().iterator();
		while (iter.hasNext()) {
			final Integer id = iter.next();
			String name = ((WorkingSet) workingSets.get(id)).getWorkingSetName();
			DataListItem item = new DataListItem(name);
			item.setIconStyle("tree-folder");
			item.setId(id.toString());
			list.add(item);
		}

	}

	private void saveExportedZip(final String pathOfZipped, String wsID) {
		Dialog dialog = new Dialog();
		dialog.setButtons(Dialog.OKCANCEL);
		dialog.setSize("400px", "300px");
		dialog.setHeading("Successful Export");
		dialog.addStyleName("my-shell-plain");
		dialog.addText("The working set " + ((WorkingSetCache.impl.getWorkingSets().get(wsID))).getWorkingSetName()
				+ " has been exported.  If you have problems downloading the file, make sure you have popups "
				+ "enabled for this website.");
		((Button)dialog.getButtonBar().getItemByItemId(Dialog.OK)).setText("Download File");
		((Button)dialog.getButtonBar().getItemByItemId(Dialog.OK)).addListener(Events.Select, new Listener<BaseEvent>() {
			public void handleEvent(BaseEvent be) {
				Window.open(pathOfZipped, "_blank", "");
			}
		});
		dialog.setHideOnButtonClick(true);
		dialog.show();
	}

	private void startExport(final DataListItem[] checked, final WorkingSet ws) {
		Info.display("Export Started", "Your working sets are being exported. A popup "
				+ "will notify you when the export has finished and when the files are "
				+ "available for download.");
		
		String permissionProblem = null;
		for( Integer curSpecies : ws.getSpeciesIDs() ) {
			AuthorizableDraftAssessment d = new AuthorizableDraftAssessment(
					TaxonomyCache.impl.getTaxon(curSpecies), ws.getFilter().getRegionIDsCSV());
			
			if(!AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, AuthorizableObject.WRITE, d))
				permissionProblem = d.getTaxon().getFullName();
		}

		if( permissionProblem == null ) {
			fireExport(checked, true);
		} else {
			WindowUtils.confirmAlert("Insufficient Permissions", "You cannot lock " +
					"the assessments for this working set as you do not have sufficient " +
					"permissions to edit the draft assessments for at least " +
					"the taxon " + permissionProblem + ". Would you like to export the " +
					"working set without locking anyway?", new Listener<MessageBoxEvent>() {
						public void handleEvent(MessageBoxEvent be) {
							if( be.getButtonClicked().getText().equalsIgnoreCase("yes"))
								fireExport(checked, false);
							else
								exportButton.setEnabled(true);
						}
					});
		}
	}

}
