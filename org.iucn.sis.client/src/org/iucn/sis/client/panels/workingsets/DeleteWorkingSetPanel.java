package org.iucn.sis.client.panels.workingsets;

import java.util.List;

import org.iucn.sis.client.api.caches.AuthorizationCache;
import org.iucn.sis.client.api.caches.WorkingSetCache;
import org.iucn.sis.client.api.ui.models.workingset.WSModel;
import org.iucn.sis.client.api.ui.models.workingset.WSStore;
import org.iucn.sis.client.api.utils.UriBase;
import org.iucn.sis.client.container.SimpleSISClient;
import org.iucn.sis.client.panels.PanelManager;
import org.iucn.sis.shared.api.acl.base.AuthorizableObject;
import org.iucn.sis.shared.api.models.WorkingSet;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.binder.DataListBinder;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.DataList;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.InfoConfig;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.user.client.ui.HTML;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.utils.RowParser;
import com.solertium.util.extjs.client.WindowUtils;

@SuppressWarnings("deprecation")
public class DeleteWorkingSetPanel extends LayoutContainer {

	private PanelManager manager = null;
	// HorizontalPanel buttons = null;
	private ButtonBar buttons = null;
	// private DataListItem selectedItem = null;
	private DataList list;
	private DataListBinder<WSModel> binder;

	// private ListStore<WSModel> store;

	public DeleteWorkingSetPanel(PanelManager manager) {
		super();
		this.manager = manager;
		list = new DataList();
		binder = new DataListBinder<WSModel>(list, WSStore.getStore());
		binder.setDisplayProperty("name");
		build();
	}

	private void build() {
		// BorderLayout layout = new BorderLayout();
		// BorderLayoutData north = new BorderLayoutData(LayoutRegion.NORTH,
		// 55f);
		// BorderLayoutData center = new BorderLayoutData(LayoutRegion.CENTER);
		// BorderLayoutData south = new BorderLayoutData(LayoutRegion.SOUTH,
		// 30f);
		// layout.setSpacing(4);
		RowLayout layout = new RowLayout();
		setLayout(layout);
		addStyleName("gwt-background");

		HTML html = new HTML(
				"<b> Instructions: </b> To delete a working set, select the working set"
						+ " you wish to remove, and then click delete.  If you delete a public working set, you are simply unsubscribing to "
						+ "it, and you may resubscribe later.  Removing a private working set will not delete the taxa or"
						+ " the assessments associated with the deleted working set.");
		add(html, new RowData(1d, -1));

		list.addStyleName("gwt-background");
		add(list, new RowData(1d, 1d));
		list.setScrollMode(Scroll.AUTOY);

		final Button delete = new Button("Delete", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				if (list.getSelectedItem() != null) {
					WindowUtils.confirmAlert("Delete working set?", "Are you sure you want to delete the working set "
							+ list.getSelectedItem().getText() + "?", new WindowUtils.SimpleMessageBoxListener() {
						@Override
						public void onYes() {
							List<WSModel> data = binder.getSelection();
							if (data.size() > 0)
								remove(data.get(0), true);
						}
					});
				} else {
					WindowUtils.errorAlert("Error", "Please first select a working set to delete.");
				}
			}
		});

		final Button unsubscribe = new Button("Unsubscribe", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				if (list.getSelectedItem() != null) {
					WindowUtils.confirmAlert("Unsubscribe?", "Are you sure you want to unsubscribe " +
							"to the working set " + list.getSelectedItem().getText() + "? " +
							"You will be able to subscribe again if your permissions are unchanged.",
								new WindowUtils.SimpleMessageBoxListener() {
						public void onYes() {
							List<WSModel> data = binder.getSelection();
							if (data.size() > 0)
								remove(data.get(0), false);
						}
					});
				} else {
					WindowUtils.errorAlert("Error", "Please first select a working set to delete.");
				}
			}
		});
		
		list.addListener(Events.SelectionChange, new Listener<BaseEvent>() {
			public void handleEvent(BaseEvent be) {
				if (list.getSelectedItem() != null) {
					delete.setEnabled(true);
					unsubscribe.setEnabled(true);
				} else {
					delete.setEnabled(false);
					unsubscribe.setEnabled(false);
				}
			}
		});
		
		buttons = new ButtonBar();
		buttons.setAlignment(HorizontalAlignment.LEFT);
		Button cancel = new Button("Cancel", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				cancel();
			}
		});

		buttons.add(delete);
		buttons.add(unsubscribe);
		buttons.add(cancel);
		add(buttons, new RowData(1d, -1));

	}

	private void cancel() {
		manager.workingSetBrowser.setManagerTab();
	}

	private void remove(WSModel modelToRemove, final boolean delete) {
		final WorkingSet ws = modelToRemove.getWorkingSet();
		final GenericCallback<String> wayback = new GenericCallback<String>() {
			public void onFailure(Throwable caught) {
				Info.display(new InfoConfig("ERROR", "Failed to delete/unsubscribe to working set " + ws.getWorkingSetName()));
				((Button)buttons.getItemByItemId(Dialog.CANCEL)).setText("Done");
			}

			public void onSuccess(String arg0) {
				if( delete )
					Info.display(new InfoConfig("DELETED", "Deleted working set " + ws.getWorkingSetName()));
				else
					Info.display(new InfoConfig("Unsubscribed", "Unsubscribed to working set " + ws.getWorkingSetName()));
				
				// manager.workingSetHierarchy.update();
				WSStore.getStore().update();
				if (WorkingSetCache.impl.getCurrentWorkingSet() != null && ws.getId() == WorkingSetCache.impl.getCurrentWorkingSet().getId()) {
					WorkingSetCache.impl.resetCurrentWorkingSet();
					manager.workingSetFullPanel.buildInfo();
				}
				((Button)buttons.getItemByItemId(Dialog.CANCEL)).setText("Done");
				// update();
			}
		};
		
		if( delete ) {
			if( AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, AuthorizableObject.DELETE, ws)) {
				ensurePermissionsCleared(ws.getId(), new GenericCallback<String>() {
					public void onSuccess(String result) {
						WorkingSetCache.impl.deleteWorkingSet(ws, wayback);
					}
					public void onFailure(Throwable caught) {
						WindowUtils.errorAlert("Permission Error", "There are still users that are granted permissions via this Working Set. " +
						"Before you can delete, please visit the Permission Manager and remove all of these users.");
					}
				});
			} else
				WindowUtils.errorAlert("Unauthorized", "You are not authorized to delete this working set.");
		} else
			WorkingSetCache.impl.unsubscribeToWorkingSet(ws, wayback);
	}

//	private void doDelete(final WorkingSet ws) {
//		WorkingSetCache.impl.deleteWorkingSet(ws, 
//	}

	private void ensurePermissionsCleared(final Integer wsID, final GenericCallback<String> callback) {
		final String permGroupName = "ws" + wsID;
		final String query = "?quickgroup=" + permGroupName + "";
		final NativeDocument document = SimpleSISClient.getHttpBasicNativeDocument();
		document.get(UriBase.getInstance().getUserBase()
				+ "/browse/profile" + query, new GenericCallback<String>() {
			public void onFailure(Throwable caught) {
			}

			public void onSuccess(String result) {
				final RowParser parser = new RowParser(document);
				
				if( parser.getRows().size() > 0 )
					callback.onFailure(null);
				else
					callback.onSuccess(null);
			}
		});
	}
	
	// private void remove(final String id, final String currentID) {
	// WorkingSet ws = (WorkingSet) workingSets.get(id);
	// final String name = ws.getWorkingSetName();
	// WorkingSetCache.impl.deleteWorkingSet(ws, new GenericCallback<String>() {
	// public void onFailure(Throwable caught) {
	// Info.display(new InfoConfig("ERROR", "Failed to delete working set " +
	// name));
	// buttons.getButtonById(Dialog.CANCEL).setText("Done");
	// }
	//
	// public void onSuccess(String arg0) {
	// Info.display(new InfoConfig("DELETED", "Deleted working set " + name));
	// // manager.workingSetHierarchy.update();
	// WSStore.getStore().update();
	// if (currentID.equals(id)) {
	// WorkingSetCache.impl.setCurrentWorkingSet("");
	// manager.workingSetFullPanel.buildInfo();
	// }
	// buttons.getButtonById(Dialog.CANCEL).setText("Done");
	// update();
	// }
	// });
	// }

	@Override
	public void show() {
		// if (selectedItem != null)
		// list.getSelectionModel().select(selectedItem);
	}

	// public void update() {
	// this.setVisible(true);
	// list.removeAll();
	// workingSets = WorkingSetCache.impl.getWorkingSets();
	// final StringBuffer currentID = new StringBuffer();
	// if (WorkingSetCache.impl.getCurrentWorkingSet() != null)
	// currentID.append(WorkingSetCache.impl.getCurrentWorkingSet().getId());
	//
	// Iterator iter = workingSets.keySet().iterator();
	// while (iter.hasNext()) {
	// final String id = (String) iter.next();
	// String name = ((WorkingSet) workingSets.get(id)).getWorkingSetName();
	// DataListItem item = new DataListItem(name);
	// item.setIconStyle("tree-folder");
	// item.setId(id);
	// list.add(item);
	// if (id.equals(currentID)) {
	// selectedItem = item;
	// }
	//
	// }
	//		
	// WSStore.getStore().update();
	//		
	// }

}
