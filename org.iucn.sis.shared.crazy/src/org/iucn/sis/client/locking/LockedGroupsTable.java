package org.iucn.sis.client.locking;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.iucn.sis.client.simple.SimpleSISClient;

import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.utils.RowData;
import com.solertium.util.extjs.client.WindowUtils;
import com.solertium.util.gwt.ui.DrawsLazily;

public class LockedGroupsTable extends BaseTablePanel {

	public String getAutoExpandColumn() {
		return "groupid";
	}
	
	public List<ColumnConfig> getColumns() {
		final List<ColumnConfig> cols = new ArrayList<ColumnConfig>();
		cols.add(new ColumnConfig("id", "ID", 150));
		cols.add(new ColumnConfig("groupid", "Group ID", 150));
		cols.add(new ColumnConfig("groupname", "Working Set", 150));
		return cols;
	}
	
	public Collection<RowData> getRows() {
		final Collection<RowData> rows = new ArrayList<RowData>();
		final ArrayList<String> found = new ArrayList<String>();
		for (RowData row : LockLoader.impl.getPersistentLockGroups()) {
			String current = (String)row.getField("groupid");
			if (!found.contains(current)) {
				found.add(current);
				rows.add(row);
			}
		}
		return rows;
	}
	
	public ToolBar getToolBar() {
		final ToolBar bar = new ToolBar();
		bar.add(new Button("Unlock Selected Group", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				final BaseModelData selected = grid.getSelectionModel().getSelectedItem();
				if (selected == null) {
					WindowUtils.errorAlert("Please select a group.");
					return;
				}
				
				final NativeDocument document = SimpleSISClient.getHttpBasicNativeDocument();
				document.delete("/management/locks/persistentlockgroup/" + selected.get("id").toString(), new GenericCallback<String>() {
					public void onSuccess(String result) {
						final String groupID = (String)selected.get("groupid");
						final ArrayList<String> lockIDs = new ArrayList<String>();
						for (RowData row : LockLoader.impl.getPersistentLockGroups()) {
							if (groupID.equals(row.getField("groupid")))
								lockIDs.add(row.getField("persistentlockid"));
						}
						LockLoader.impl.removePersistentLockGroup(
							selected.get("id").toString(), lockIDs
						);
						Info.display("Success", "Lock Released.");
						draw(new DrawsLazily.DoneDrawingWithNothingToDoCallback());
					}
					public void onFailure(Throwable caught) {
						WindowUtils.errorAlert("Could not release lock, please try again later.");
					}
				});
			}
		}));
		return bar;
	}

}
