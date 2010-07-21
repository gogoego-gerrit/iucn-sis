package org.iucn.sis.client.panels.locking;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.iucn.sis.client.api.utils.UriBase;
import org.iucn.sis.client.container.SimpleSISClient;

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

public class LockedAssessmentsTable extends BaseTablePanel {
	
	public List<ColumnConfig> getColumns() {
		final List<ColumnConfig> cols = new ArrayList<ColumnConfig>();
		cols.add(new ColumnConfig("species", "Species", 150));
		cols.add(new ColumnConfig("status", "Status", 150));
		cols.add(new ColumnConfig("owner", "Owner", 150));
		cols.add(new ColumnConfig("date", "Date Locked", 150));
		cols.add(new ColumnConfig("type", "Lock Type", 150));
		return cols;
	}
	
	public Collection<RowData> getRows() {
		return LockLoader.impl.getPersistentLocks();
	}
	
	public ToolBar getToolBar() {
		final ToolBar bar = new ToolBar();
		bar.add(new Button("Unlock Selected Assessment", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				final BaseModelData selected = grid.getSelectionModel().getSelectedItem();
				if (selected == null) {
					WindowUtils.errorAlert("Please select an assessment.");
					return;
				}
				
				final NativeDocument document = SimpleSISClient.getHttpBasicNativeDocument();
				document.delete(UriBase.getInstance().getSISBase() + "/management/locks/persistentlock/" + selected.get("id").toString(), new GenericCallback<String>() {
					public void onSuccess(String result) {
						LockLoader.impl.removePersistentLock(selected.get("id").toString());
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
	
	public String getAutoExpandColumn() {
		return "owner";
	}

}
