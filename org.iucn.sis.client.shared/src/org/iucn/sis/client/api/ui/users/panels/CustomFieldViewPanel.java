/**
 *
 */
package org.iucn.sis.client.api.ui.users.panels;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.iucn.sis.client.api.container.SISClientBase;
import org.iucn.sis.client.api.utils.UriBase;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.EditorGrid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.grid.GridSelectionModel;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.ui.HTML;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.utils.RowData;
import com.solertium.lwxml.shared.utils.RowParser;
import com.solertium.util.extjs.client.WindowUtils;

/**
 * CustomFieldViewPanel.java
 * 
 * Allows users to view and edit custom field properties.
 * 
 * @author carl.scott <carl.scott@solertium.com>
 * 
 */
public class CustomFieldViewPanel extends LayoutContainer implements HasRefreshableContent {

	/**
	 * CustomFieldModelData
	 * 
	 * Model for custom field information
	 * 
	 * @author carl.scott <carl.scott@solertium.com>
	 * 
	 */
	public static class CustomFieldModelData extends BaseModelData {
		private static final long serialVersionUID = 1L;

		public CustomFieldModelData(final RowData rowData) {
			super();
			final Iterator<String> iterator = rowData.keySet().iterator();
			while (iterator.hasNext()) {
				String key = iterator.next().toLowerCase();
				set(key, rowData.getField(key));
			}
		}
	}

	private final LayoutContainer container, center;

	private EditorGrid<CustomFieldModelData> grid;

	private final ContentManager contentManager;

	public CustomFieldViewPanel(ContentManager contentManager) {
		super();

		this.contentManager = contentManager;

		container = new LayoutContainer();
		container.setLayout(new BorderLayout());

		center = new LayoutContainer();
		center.setLayout(new FillLayout());
	}

	/**
	 * Drawing removes all so it can be called multiple times without worry.
	 * 
	 */
	private void draw() {
		removeAll();
		setLayout(new BorderLayout());

		final ToolBar bar = new ToolBar();
		bar.add(new Button("Add Custom Field", new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				CustomFieldCreator window = new CustomFieldCreator() {
					@Override
					public void onChange() {
						WindowUtils.showLoadingAlert("Loading Changes...");
						populateStore();
						contentManager.setStale("users");
					}
				};
				window.setHeading("Create Field");
				window.draw();
				window.show();
			}
		}));
		bar.add(new Button("Remove Custom Field", new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				if (grid == null)
					return;
				final CustomFieldModelData selected = grid.getSelectionModel().getSelectedItem();
				if (selected == null)
					return;

				WindowUtils.confirmAlert("Confirm", "Are you sure you want to remove the \"" + selected.get("name")
						+ "\" field?", new WindowUtils.MessageBoxListener() {
					@Override
					public void onNo() {
					}

					@Override
					public void onYes() {
						final NativeDocument document = SISClientBase.getHttpBasicNativeDocument();
						document.delete(UriBase.getInstance().getUserBase() + "/manager/custom/"
								+ selected.get("id"), new GenericCallback<String>() {
							public void onFailure(Throwable caught) {
								WindowUtils.errorAlert("Error", "Could not remove field, please try again later.");
							}

							public void onSuccess(String result) {
								Info.display("Success", "Field {0} removed.", (String) selected.get("name"));
								populateStore();
								contentManager.setStale("users");
							}
						});
					}
				});
			}
		}));
		bar.add(new SeparatorToolItem());
		bar.add(new Html("<b>Double-click a row to edit</b>"));

		add(center, new BorderLayoutData(LayoutRegion.CENTER, 800));
		add(bar, new BorderLayoutData(LayoutRegion.NORTH, 25, 25, 25));

		populateStore();

		layout();
	}

	private ColumnModel getColumnModel() {
		final List<ColumnConfig> configs = new ArrayList<ColumnConfig>();

		final ColumnConfig id = new ColumnConfig();
		id.setId("id");
		id.setHeader("ID");
		id.setWidth(30);
		configs.add(id);

		final ColumnConfig name = new ColumnConfig();
		name.setId("name");
		name.setHeader("Field Name");
		name.setWidth(120);
		configs.add(name);

		final ColumnConfig required = new ColumnConfig();
		required.setId("required");
		required.setHeader("Required?");
		required.setWidth(60);
		required.setRenderer(new GridCellRenderer<CustomFieldModelData>() {
			public Object render(CustomFieldModelData model, String property, ColumnData config, int rowIndex, int colIndex, com.extjs.gxt.ui.client.store.ListStore<CustomFieldModelData> store, com.extjs.gxt.ui.client.widget.grid.Grid<CustomFieldModelData> grid) {
				return "true".equalsIgnoreCase((String) model.get(property)) ? "Yes" : "No";
			}
		});
		configs.add(required);

		final ColumnConfig type = new ColumnConfig();
		type.setId("type");
		type.setHeader("Field Type");
		type.setWidth(120);
		type.setRenderer(new GridCellRenderer<CustomFieldModelData>() {
			public Object render(CustomFieldModelData model, String property, ColumnData config, int rowIndex, int colIndex, com.extjs.gxt.ui.client.store.ListStore<CustomFieldModelData> store, com.extjs.gxt.ui.client.widget.grid.Grid<CustomFieldModelData> grid) {
				return "text".equalsIgnoreCase((String) model.get(property)) ? "Text Input" : "Drop-Down Menu";
			}
		});
		configs.add(type);

		final ColumnConfig options = new ColumnConfig();
		options.setId("options");
		options.setHeader("Options");
		options.setWidth(220);
		options.setRenderer(new GridCellRenderer<CustomFieldModelData>() {
			public Object render(CustomFieldModelData model, String property, ColumnData config, int rowIndex, int colIndex, com.extjs.gxt.ui.client.store.ListStore<CustomFieldModelData> store, com.extjs.gxt.ui.client.widget.grid.Grid<CustomFieldModelData> grid) {
				String value = (String) model.get(property);
				if (value == null)
					return null;
				String[] split = value.split("::");
				String out = "";
				for (int i = 0; i < split.length; i++)
					out += split[i] + ((i + 1) < split.length ? ", " : "");
				return out;
			}
		});
		configs.add(options);

		return new ColumnModel(configs);
	}

	private void populateStore() {
		center.removeAll();
		final NativeDocument document = SISClientBase.getHttpBasicNativeDocument();
		document.get(UriBase.getInstance().getUserBase() + "/dump/customfield", new GenericCallback<String>() {
			public void onFailure(Throwable caught) {
				add(new HTML("Could not load custom fields."));
			}

			public void onSuccess(String result) {
				final RowParser parser = new RowParser(document);
				final ListStore<CustomFieldModelData> store = new ListStore<CustomFieldModelData>();
				final Iterator<RowData> iterator = parser.iterator();
				while (iterator.hasNext())
					store.add(new CustomFieldModelData(iterator.next()));

				final GridSelectionModel<CustomFieldModelData> sm = new GridSelectionModel<CustomFieldModelData>();
				sm.setSelectionMode(SelectionMode.SINGLE);

				grid = new EditorGrid<CustomFieldModelData>(store, getColumnModel());
				grid.setSelectionModel(sm);
				grid.setBorders(false);
				grid.setSize(700, 400);
				grid.addListener(Events.RowDoubleClick, new Listener<GridEvent<CustomFieldModelData>>() {
					public void handleEvent(GridEvent<CustomFieldModelData> be) {
						CustomFieldCreator window = new CustomFieldCreator() {
							@Override
							public void onChange() {
								populateStore();
							}
						};
						window.setHeading("Update Field");
						window.setEditing((CustomFieldModelData) be.getGrid().getStore().getAt(be.getRowIndex()));
						window.draw();
						window.show();
					}
				});

				center.add(grid);

				center.layout();

				WindowUtils.hideLoadingAlert();
			}
		});
	}

	public void refresh() {
		draw();
	}
}
