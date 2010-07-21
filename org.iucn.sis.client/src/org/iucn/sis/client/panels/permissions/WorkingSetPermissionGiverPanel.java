package org.iucn.sis.client.panels.permissions;

import java.util.ArrayList;
import java.util.List;

import org.iucn.sis.client.api.caches.AuthorizationCache;
import org.iucn.sis.client.api.caches.WorkingSetCache;
import org.iucn.sis.client.api.models.ClientUser;
import org.iucn.sis.client.api.ui.users.panels.BrowseUsersWindow;
import org.iucn.sis.client.container.SimpleSISClient;
import org.iucn.sis.shared.api.acl.base.AuthorizableObject;
import org.iucn.sis.shared.api.models.WorkingSet;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.store.StoreFilter;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.grid.CellEditor;
import com.extjs.gxt.ui.client.widget.grid.CheckColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.EditorGrid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.grid.GridSelectionModel;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HTML;
import com.solertium.util.extjs.client.CheckboxMultiTriggerField;
import com.solertium.util.extjs.client.WindowUtils;

public abstract class WorkingSetPermissionGiverPanel extends ContentPanel {

	public static final String read = "read";
	public static final String write = "write";
	public static final String create = "create";
	public static final String assign = "grant";
	public static final String delete = "delete";

	private String instructions = "";
	private boolean allowRead;
	private boolean allowWrite;
	private boolean allowAssign;
	private boolean allowCreate;
	private boolean allowDelete;
	private int widthOfNameColumn = 200;
	private int widthOfPermissionColumn = 200;
	private String defaultPermission = read;

	protected ToolBar toolBar;
	protected Button removeButton;
	protected ListStore<PermissionUserModel> associatedPermissions;
	protected EditorGrid<PermissionUserModel> permissionGrid;

	public WorkingSetPermissionGiverPanel() {
		allowRead = true;
		allowWrite = true;
		allowAssign = true;
		allowCreate = false;
		allowDelete = false;
		associatedPermissions = new ListStore<PermissionUserModel>();
		setInstructions("<b>Instructions:</b>Add/Remove users to determine to what extent other users " +
			"can access this working set. These permissions govern who can subscribe to (read), edit " +
			"(write) or access the Permission Management panel (grant) for this working set. These permissions " +
			"<i>do not</i> grant permission to read or write the taxa contained in the working set - " +
			"that must be performed by an appropriate SIS permissions manager.<br />");
		toolBar = new ToolBar();
		setHeaderVisible(false);
//		setTopComponent(toolBar);
	}

	public void addAssociatedPermissions(List<PermissionUserModel> permissions) {
		associatedPermissions.add(permissions);
	}

	protected void addToolItem(Button item) {
		toolBar.add(item);
	}

	private boolean containsModel(int userID) {
		return permissionGrid.getStore().findModel("id", Integer.valueOf(userID)) != null;
	}

	/**
	 * Function that must be called when ready to display on the page. Must set
	 * allowRead, write and assign before this method is called.
	 */
	public void draw() {
		final WorkingSet curWS = WorkingSetCache.impl.getCurrentWorkingSet();
		final ClientUser curUser = SimpleSISClient.currentUser;
		
		setAllowWrite(AuthorizationCache.impl.hasRight(curUser, AuthorizableObject.WRITE, curWS));
		
		ArrayList<ColumnConfig> columns = new ArrayList<ColumnConfig>();
		ColumnConfig nameColumn = new ColumnConfig("name", "Name", widthOfNameColumn);
		nameColumn.setRenderer(new GridCellRenderer<PermissionUserModel>() {
			public Object render(PermissionUserModel model, String property, ColumnData config, int rowIndex, int colIndex, com.extjs.gxt.ui.client.store.ListStore<PermissionUserModel> store, com.extjs.gxt.ui.client.widget.grid.Grid<PermissionUserModel> grid) {
				return model.getName();
			}
		});
		columns.add(nameColumn);
		
		ColumnConfig permissionColumn = new ColumnConfig("permission",
				"Basic Permissions", widthOfPermissionColumn);
		CheckboxMultiTriggerField permissionField = new CheckboxMultiTriggerField(getOptions(), ",") {
			@Override
			public boolean validateValue(String value) {
				if (value == null || value.equalsIgnoreCase("")) {
					Window.alert("Must give some permission or remove from permission list.");
					setRawValue(read);
					layout();
					return true;
				} else
					return super.validateValue(value);
			}
		};
		CellEditor editor = new CellEditor(permissionField);
		editor.setCompleteOnEnter(true);
		editor.setCancelOnEsc(true);
		permissionColumn.setEditor(editor);
		columns.add(permissionColumn);

		CheckColumnConfig assessorColumn = new CheckColumnConfig("assessor", "Assessor Rights", 100);
		if( AuthorizationCache.impl.hasRight(curUser, AuthorizableObject.GRANT, curWS)
				 && (curUser.getProperty("quickGroup").contains("ws" + curWS.getId() + "assessor")
				   || curUser.getProperty("quickGroup").contains("rlu")
				   || curUser.getProperty("quickGroup").contains("sysAdmin") ) ) {
			assessorColumn.setHidden(false);
		} else
			assessorColumn.setHidden(true);
		
		columns.add(assessorColumn);

		ColumnModel cm = new ColumnModel(columns);

		permissionGrid = new EditorGrid<PermissionUserModel>(associatedPermissions, cm);
		permissionGrid.addPlugin(assessorColumn);
		associatedPermissions.addFilter(new StoreFilter<PermissionUserModel>() {
			public boolean select(Store<PermissionUserModel> store, PermissionUserModel parent,
					PermissionUserModel item, String property) {
				if( property.equals("permission") && item.getPermission() != null && item.getPermission().indexOf("write") > -1 )
					return AuthorizationCache.impl.hasRight(curUser, AuthorizableObject.WRITE, curWS);
				else
					return true;
			}
		});
		
		if( !AuthorizationCache.impl.hasRight(curUser, AuthorizableObject.WRITE, curWS) )
			associatedPermissions.filter("permission");
		
		GridSelectionModel<PermissionUserModel> model = new GridSelectionModel<PermissionUserModel>();
		model.bindGrid(permissionGrid);
		model.setFiresEvents(true);
		model.addListener(Events.SelectionChange,
				new Listener<SelectionEvent<PermissionUserModel>>() {

					public void handleEvent(SelectionEvent se) {
						removeButton.setEnabled(permissionGrid
								.getSelectionModel().getSelectedItem() != null);
					}
				});

		permissionGrid.setSelectionModel(model);
		permissionGrid.setAutoExpandColumn("permission");
		permissionGrid.setBorders(true);
		permissionGrid.addListener(Events.RowClick, new Listener<GridEvent>() {

			public void handleEvent(GridEvent be) {
				permissionGrid.getView().focusRow(be.getRowIndex());
			}
		});
		
		getSaveButtons();
		layoutWidgets();
	}

	protected abstract void onRemoveUsers(List<PermissionUserModel> removed);
	
	protected ButtonBar drawButtons() {
		ButtonBar buttonBar = new ButtonBar();
		buttonBar.setAlignment(HorizontalAlignment.RIGHT);
		removeButton = new Button("Remove User",
				new SelectionListener<ButtonEvent>() {

					public void componentSelected(ButtonEvent ce) {
						List<PermissionUserModel> toRemove = permissionGrid.getSelectionModel().getSelectedItems();
						if (toRemove.size() != 0) {
							for (PermissionUserModel cur : toRemove) {
								associatedPermissions.remove(cur);
							}
							
							onRemoveUsers(toRemove);
						} else {
							WindowUtils.errorAlert("Must first choose user to remove from permission group.");
						}
					}

				});

		Button addButton = new Button("Add User(s)", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
						BrowseUsersWindow window = new BrowseUsersWindow() {
							@Override
							public void onSelect(ArrayList<ClientUser> selectedUsers) {
								for (ClientUser user : selectedUsers) {
									if (!containsModel(user.getId())) {
										PermissionUserModel model = new PermissionUserModel(user, defaultPermission, false);
										associatedPermissions.add(model);
									}
								}

							}
						};
						window.setSelectedUsersHeading("Users to Add");
						window.setPossibleUsersHeading("Search results");
						window.setInstructions("<b>Add User:</b> Choose a recent user or search for a user and then drag and drop the user to the \"Users to Add\" list.  </br></br>");

						List<ClientUser> users = new ArrayList<ClientUser>();
						// for (PermissionUserModel model :
						// permissionGrid.getStore().getModels())
						// users.add(model.getUser());
						window.refresh(users);
						window.show();
					}
				});

		buttonBar.add(removeButton);
		buttonBar.add(addButton);

		return buttonBar;

	}

	public ListStore<PermissionUserModel> getAssociatedPermissions() {
		return associatedPermissions;
	}

	public String getDefaultPermission() {
		return defaultPermission;
	}

	public String getInstructions() {
		return instructions;
	}

	protected List<String> getOptions() {
		List<String> options = new ArrayList<String>();
		if (allowRead)
			options.add(read);
		if (allowWrite)
			options.add(write);
		if (allowCreate)
			options.add(create);
		if (allowDelete)
			options.add(delete);
		if (allowAssign)
			options.add(assign);

		return options;
	}

	public EditorGrid<PermissionUserModel> getPermissionGrid() {
		return permissionGrid;
	}

	protected void getSaveButtons() {

		Button save = new Button();
		save.setText("Save");
		save.setIconStyle("icon-save");
		save.setTitle("Save and Continue Editing");
		save.addListener(Events.Select, new Listener() {
			public void handleEvent(BaseEvent be) {
				onSave();
			}
		});

		addToolItem(save);
	}

	public boolean isAllowAssign() {
		return allowAssign;
	}

	public boolean isAllowCreate() {
		return allowCreate;
	}

	public boolean isAllowDelete() {
		return allowDelete;
	}

	public boolean isAllowRead() {
		return allowRead;
	}

	public boolean isAllowWrite() {
		return allowWrite;
	}

	protected void layoutWidgets() {
		RowLayout layout = new RowLayout();
		setLayout(layout);
		add(toolBar, new RowData(1, 25));
		add(new HTML(instructions, true), new RowData(1, -1));
		add(permissionGrid, new RowData(1, 1));
		add(drawButtons(), new RowData(1, -1));
	}

	public abstract void onSave();

	public void setAllowAssign(boolean allowAssign) {
		this.allowAssign = allowAssign;
	}

	public void setAllowCreate(boolean allowCreate) {
		this.allowCreate = allowCreate;
	}

	public void setAllowDelete(boolean allowDelete) {
		this.allowDelete = allowDelete;
	}

	public void setAllowRead(boolean allowRead) {
		this.allowRead = allowRead;
	}

	public void setAllowWrite(boolean allowWrite) {
		this.allowWrite = allowWrite;
	}

	public void setAssociatedPermissions(
			ListStore<PermissionUserModel> associatedPermissions) {
		this.associatedPermissions = associatedPermissions;
	}

	public void setDefaultPermission(String defaultPermission) {
		this.defaultPermission = defaultPermission;
	}

	public void setInstructions(String instructions) {
		this.instructions = instructions;
	}

	public void setPermissionGrid(EditorGrid<PermissionUserModel> permissionGrid) {
		this.permissionGrid = permissionGrid;
	}

}
