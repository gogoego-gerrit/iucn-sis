package org.iucn.sis.client.panels.region;

import java.util.ArrayList;
import java.util.List;

import org.iucn.sis.client.api.caches.RegionCache;
import org.iucn.sis.client.api.ui.models.region.RegionModel;
import org.iucn.sis.client.container.SimpleSISClient;
import org.iucn.sis.shared.api.models.Region;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.Style.SortDir;
import com.extjs.gxt.ui.client.binding.FormBinding;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.Record;
import com.extjs.gxt.ui.client.store.StoreEvent;
import com.extjs.gxt.ui.client.store.StoreListener;
import com.extjs.gxt.ui.client.store.Record.RecordUpdate;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.form.Validator;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridView;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.util.extjs.client.WindowUtils;

public class RegionPanel extends ContentPanel {
	
	private static int DEFAULT_NEW_ID = 0;
	public static final String ERROR_MESSAGE = "Please enter valid data for all fields.";
	
	private boolean built = false;

	private FormBinding formBindings;
	private ListStore<RegionModel> store;
	private Grid<RegionModel> grid;
	private FormPanel panel;
	private TextField<String> description;
	private TextField<String> name;
	private TextField<String> id;

	private Button add;
	private Button delete;
	private Button commit;
	private Button reject;

	public RegionPanel() {
		build(true);
	}

	public RegionPanel(boolean enableEditing) {
		build(enableEditing);
	}

	public void build(boolean enableEditing) {
		if (built)
			return;

		// setSize(800, 400);
		setLayout(new RowLayout(Orientation.HORIZONTAL));
		setHeaderVisible(true);
		setFrame(true);
		setHeading("Edit Region List");
		setIconStyle("icon-world");

		createGrid();
		grid.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		grid.getSelectionModel().addListener(Events.BeforeSelect, new Listener<SelectionEvent<RegionModel>>() {
			public void handleEvent(SelectionEvent<RegionModel> be) {
				be.setCancelled(!isPanelDataValid());
				if (be.isCancelled())
					WindowUtils.errorAlert(ERROR_MESSAGE);
			}
		});
		grid.getSelectionModel().addListener(Events.SelectionChange, new Listener<SelectionChangedEvent<RegionModel>>() {
			public void handleEvent(SelectionChangedEvent<RegionModel> be) {
				if (be.getSelectedItem() != null) {
					formBindings.bind(be.getSelectedItem());
					name.setAllowBlank(false);
					description.setAllowBlank(false);

					if (be.getSelectedItem().get("id").equals(DEFAULT_NEW_ID+""))
						delete.setEnabled(true);
					else
						delete.setEnabled(false);
				} else {
					formBindings.unbind();
					delete.setEnabled(false);
					name.setAllowBlank(true);
					description.setAllowBlank(true);
				}
			}
		});
		grid.setView(new GridView() {

			@Override
			protected void refreshRow(int row) {
				// A hack, I suppose, so the GridView doesn't try to refresh a
				// row
				// referring to a Model that doesn't exist in the store anymore

				if (row != -1)
					super.refreshRow(row);
			}
		});
		add(grid, new RowData(.5, 1));

		createForm();
		formBindings = new FormBinding(panel, true);
		formBindings.setStore(store);

		add(panel, new RowData(.5, 1));
// setAlignment(HorizontalAlignment.LEFT);
		add = new Button("Add New Region", new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				if (isPanelDataValid()) {
					RegionModel newone = new RegionModel(new Region(DEFAULT_NEW_ID, "", ""));
					store.add(newone);
					store.getRecord(newone).set("name", "(Name)");
					store.getRecord(newone).set("description", "(Description)");
					grid.getSelectionModel().select(newone, false);
				} else {
					WindowUtils.errorAlert(ERROR_MESSAGE);
				}
			}
		});
		add.setIconStyle("icon-world-add");
		addButton(add);

		delete = new Button("Delete Region", new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				if (grid.getSelectionModel().getSelectedItem() != null)
					WindowUtils.confirmAlert("Delete Region", "Are you sure you want to " + "delete this region?",
							new Listener<MessageBoxEvent>() {
								public void handleEvent(MessageBoxEvent be) {
									if (be.getButtonClicked().getText().equalsIgnoreCase("yes")) {
										formBindings.unbind();
										Record rec = store.getRecord(grid.getSelectionModel().getSelectedItem());
										rec.reject(false);
									}
								};
							});
			}
		});
		delete.setIconStyle("icon-world-delete");
		delete.setToolTip("Can be used to remove a region that hasn't been committed. "
				+ "This CANNOT be used to remove a region that is available for use.");
		addButton(delete);

		commit = new Button("Commit Changes", new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				processCommitChanges();
			}
		});
		commit.setIconStyle("icon-thumbs-up");
		addButton(commit);

		reject = new Button("Reject Changes", new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				WindowUtils.confirmAlert("Delete Region", "Are you sure you want to "
						+ "reject changes? Any newly created, uncommited regions and edits "
						+ "to existing regions will be lost.", new Listener<MessageBoxEvent>() {
					public void handleEvent(MessageBoxEvent be) {
						if (be.getButtonClicked().getText().equalsIgnoreCase("yes"))
							grid.getStore().rejectChanges();
					}
				});
			}
		});
		reject.setIconStyle("icon-thumbs-down");
		addButton(reject);

		store.addStoreListener(new StoreListener<RegionModel>() {
			@Override
			public void storeUpdate(StoreEvent<RegionModel> se) {
				super.storeUpdate(se);
				if (se.getOperation() == RecordUpdate.REJECT) {
					if (se.getModel().get("id").equals(DEFAULT_NEW_ID+"")) {
						store.remove(se.getModel());
						grid.getView().refresh(false);
					}
				}
			}
		});

		resetStoreData();
	}

	private FormPanel createForm() {
		panel = new FormPanel();
		panel.setHeaderVisible(false);
		panel.setBorders(true);
		// panel.setHeading("Edit Selected Region");

		id = new TextField<String>();
		id.setName("id");
		id.setFieldLabel("ID");
		id.setEnabled(false);
		panel.add(id);

		name = new TextField<String>();
		name.setName("name");
		name.setFieldLabel("Name");
		name.setValidator(new Validator() {
			public String validate(Field<?> field, String value) {
				if (grid.getSelectionModel().getSelectedItem() == null)
					return null;
				else if (value == null)
					return "You must enter something for this field.";
				else if ((value.equals("") || value.matches("\\s"))
						&& value.equalsIgnoreCase("(" + field.getFieldLabel() + ")"))
					return "You must enter valid data for " + field.getFieldLabel();
				else if (grid.getSelectionModel().getSelectedItem().get("id").equals(DEFAULT_NEW_ID+"")
						&& RegionCache.impl.getRegionByName(value) != null)
					return "A region with the name " + value + "already exists.";
				else
					return null;
			}
		});
		panel.add(name);

		description = new TextField<String>();
		description.setName("description");
		description.setFieldLabel("Description");
		description.setValidator(new Validator() {
			public String validate(Field<?> field, String value) {
				if ((value.equals("") || value.matches("\\s"))
						|| (grid.getSelectionModel().getSelectedItem() != null && value.equalsIgnoreCase("("
								+ field.getFieldLabel() + ")")))
					return "You must enter valid data for " + field.getFieldLabel();
				else
					return null;
			}
		});
		panel.add(description);
		return panel;
	}

	private Grid<RegionModel> createGrid() {
		List<ColumnConfig> configs = new ArrayList<ColumnConfig>();

		ColumnConfig column = new ColumnConfig();
		column.setId("id");
		column.setHeader("id");
		column.setAlignment(HorizontalAlignment.LEFT);
		column.setWidth(75);
		configs.add(column);

		column = new ColumnConfig();
		column.setId("name");
		column.setHeader("Region Name");
		column.setWidth(150);
		configs.add(column);

		column = new ColumnConfig();
		column.setId("description");
		column.setHeader("Full Description");
		column.setWidth(200);
		configs.add(column);

		store = new ListStore<RegionModel>();
		store.setMonitorChanges(true);

		ColumnModel cm = new ColumnModel(configs);

		grid = new Grid<RegionModel>(store, cm);
		grid.getView().setAutoFill(true);
		grid.setBorders(false);
		grid.setAutoExpandColumn("description");
		grid.setBorders(true);

		return grid;
	}

	private boolean isPanelDataValid() {
		System.out.println("grid.getSelectionModel().getSelectedItem() " + grid.getSelectionModel().getSelectedItem());
		System.out.println("panel.isValid " + panel.isValid());
		System.out.println("returning " + (grid.getSelectionModel().getSelectedItem() != null) + " "  + (!panel.isValid()) + " " + (!(grid.getSelectionModel().getSelectedItem() != null && !panel.isValid())));
		return !(grid.getSelectionModel().getSelectedItem() != null && !panel.isValid());
	}

	private void processCommitChanges() {
		if (isPanelDataValid()) {
			if (store.getModifiedRecords().size() > 0) {
				ArrayList<Region> list = new ArrayList<Region>();
				for (Record rec : store.getModifiedRecords()) {
					RegionModel model = (RegionModel) rec.getModel();
					if (store.contains(model)) {
						// This is a workaround for an annoying GXT behavior
						// where if
						// you remove an item from the store, it still has a
						// record...
						model.sinkModelDataIntoRegion();
						list.add(model.getRegion());
					}
				}
				RegionCache.impl.saveRegions(SimpleSISClient.getHttpBasicNativeDocument(), list,
						new GenericCallback<String>() {
							public void onFailure(Throwable caught) {

							}

							public void onSuccess(String result) {
								store.commitChanges();
								store.removeAll();
								for (Region curRegion : RegionCache.impl.getRegions())
									store.add(new RegionModel(curRegion));
							}
						});
			} else {
				WindowUtils.errorAlert("Nothing to commit!");
			}
		} else
			WindowUtils.errorAlert(ERROR_MESSAGE);
	}

	public void resetStoreData() {
		store.removeAll();
		RegionCache.impl.fetchRegions(SimpleSISClient.getHttpBasicNativeDocument(), new GenericCallback<String>() {
			public void onFailure(Throwable caught) {
				WindowUtils.errorAlert("Failure fetching Regions from the server. Check your "
						+ "Internet connection and try again.");
			}

			public void onSuccess(String result) {
				for (Region curRegion : RegionCache.impl.getRegions())
					store.add(new RegionModel(curRegion));
				store.sort("id", SortDir.ASC);
			}
		});
	}
}
