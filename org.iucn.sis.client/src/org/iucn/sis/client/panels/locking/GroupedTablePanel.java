package org.iucn.sis.client.panels.locking;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.iucn.sis.client.api.utils.UriBase;
import org.iucn.sis.client.container.SimpleSISClient;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.GroupingStore;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridGroupRenderer;
import com.extjs.gxt.ui.client.widget.grid.GridSelectionModel;
import com.extjs.gxt.ui.client.widget.grid.GroupColumnData;
import com.extjs.gxt.ui.client.widget.grid.GroupingView;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.utils.RowData;
import com.solertium.util.extjs.client.WindowUtils;
import com.solertium.util.gwt.ui.DrawsLazily;

public class GroupedTablePanel extends LayoutContainer implements DrawsLazily {
	
	private Grid<BaseModelData> grid;
	private Button groupButton;
	
	public GroupedTablePanel() {
		super();
		setLayout(new FillLayout());
		setLayoutOnChange(true);
	}
	
	public void draw(DoneDrawingCallback callback) {
		removeAll();
		
		final List<ColumnConfig> cols = getColumns();
		
		final Map<String, RowData> group = getGroupsForRows();
		final Map<String, String> groupIDToName = new HashMap<String, String>();
		final GroupingStore<BaseModelData> store = new GroupingStore<BaseModelData>();
		for (RowData row : getRows()) {
			final BaseModelData model = new BaseModelData();
			model.set("id", row.getField("id"));
			model.set("lockid", row.getField("lockid"));
			for (ColumnConfig col : cols)
				model.set(col.getId(), row.getField(col.getId()));
			RowData gID = group.get(row.getField("id"));
			if (gID == null) {
				model.set("groupid", "none");
				model.set("groupidentifier", null);
				groupIDToName.put("none", "No Group Defined");
			}
			else {
				model.set("groupid", gID.get("groupid"));
				model.set("groupidentifier", gID.get("id"));
				groupIDToName.put(gID.get("groupid"), "Working Set: " + gID.get("groupname"));
			}
				
			store.add(model);
		}
		store.groupBy("groupid");
		
		final GroupingView view = new GroupingView();
		view.setShowGroupedColumn(false);
		view.setGroupRenderer(new GridGroupRenderer() {
			public String render(GroupColumnData data) {
				return groupIDToName.get(data.group);
			}
		});
		
		final GridSelectionModel<BaseModelData> sel = new GridSelectionModel<BaseModelData>();
		sel.setSelectionMode(SelectionMode.SINGLE);
		
		grid = new Grid<BaseModelData>(store, new ColumnModel(cols));
		grid.setSelectionModel(sel);
		grid.setView(view);
		grid.addListener(Events.RowClick, new Listener<GridEvent>() {
			public void handleEvent(GridEvent be) {
				if (groupButton != null && be != null && be.getModel() != null)
					groupButton.setEnabled(be.getModel().get("groupidentifier") != null);
			}
		});
		//grid.setWidth(680);
		
		int size = 25;
		
		final LayoutContainer wrapper = new LayoutContainer();
		wrapper.setLayout(new FillLayout());
		//wrapper.setScrollMode(Scroll.ALWAYS);
		wrapper.add(grid);
		
		final LayoutContainer container = new LayoutContainer(new BorderLayout());
		container.setBorders(false);
		container.add(getToolBar(), new BorderLayoutData(LayoutRegion.SOUTH, size, size, size));
		container.add(wrapper, new BorderLayoutData(LayoutRegion.CENTER));
		
		add(container);
		
		callback.isDrawn();
	}
	
	public List<ColumnConfig> getColumns() {
		final List<ColumnConfig> cols = new ArrayList<ColumnConfig>();
		cols.add(new ColumnConfig("species", "Species", 150));
		cols.add(new ColumnConfig("status", "Status", 100));
		cols.add(new ColumnConfig("owner", "Owner", 150));
		cols.add(new ColumnConfig("date", "Date Locked", 150));
		cols.add(new ColumnConfig("type", "Lock Type", 150));
		cols.add(new ColumnConfig("groupid", "Group ID", 150));
		
		
		return cols;
	}
	
	public Map<String, RowData> getGroupsForRows() {
		final Map<String, RowData> map = new LinkedHashMap<String, RowData>();
		for (RowData row : LockLoader.impl.getPersistentLockGroups())
			map.put(row.getField("persistentlockid"), row);
		return map;
	}
	
	public Collection<RowData> getRows() {
		return LockLoader.impl.getPersistentLocks();
	}
	
	public ToolBar getToolBar() {
		final ToolBar bar = new ToolBar();
		bar.add(new FillToolItem());
		bar.add(new Button("Unlock Selected Assessment", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				final BaseModelData selected = grid.getSelectionModel().getSelectedItem();
				if (selected == null) {
					WindowUtils.errorAlert("Please select an assessment.");
					return;
				}
				
				final NativeDocument document = SimpleSISClient.getHttpBasicNativeDocument();
				document.delete(UriBase.getInstance().getSISBase() + "/management/locks/persistentlock/" + (String)selected.get("id"), new GenericCallback<String>() {
					public void onSuccess(String result) {
						LockLoader.impl.removePersistentLock((String)selected.get("id"));
						Info.display("Success", "Lock Released.");
						draw(new DrawsLazily.DoneDrawingWithNothingToDoCallback());
					}
					public void onFailure(Throwable caught) {
						WindowUtils.errorAlert("Could not release lock, please try again later.");
					}
				});
			}
		}));
		bar.add(new SeparatorToolItem());
		bar.add(groupButton = new Button("Unlock All In Group", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				final BaseModelData selected = grid.getSelectionModel().getSelectedItem();
				if (selected == null) {
					WindowUtils.errorAlert("Please select an assessment.");
					return;
				}
				
				final NativeDocument document = SimpleSISClient.getHttpBasicNativeDocument();
				document.delete(UriBase.getInstance().getSISBase() + "/management/locks/persistentlockgroup/" + selected.get("groupid").toString(), new GenericCallback<String>() {
					public void onSuccess(String result) {
						final String groupID = (String)selected.get("groupid");
						final ArrayList<String> lockIDs = new ArrayList<String>();
						for (RowData row : LockLoader.impl.getPersistentLockGroups()) {
							if (groupID.equals(row.getField("groupid")))
								lockIDs.add(row.getField("persistentlockid"));
						}
						LockLoader.impl.removePersistentLockGroup(
							(String)selected.get("id"), lockIDs
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
