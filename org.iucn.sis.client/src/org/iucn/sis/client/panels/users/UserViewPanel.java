/**
 *
 */
package org.iucn.sis.client.panels.users;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.iucn.sis.client.api.caches.AuthorizationCache;
import org.iucn.sis.client.api.container.SISClientBase;
import org.iucn.sis.client.api.models.ClientUser;
import org.iucn.sis.client.api.ui.users.panels.ContentManager;
import org.iucn.sis.client.api.ui.users.panels.HasRefreshableContent;
import org.iucn.sis.client.api.utils.UriBase;
import org.iucn.sis.client.panels.users.UserViewToolBar.UserViewToolbarAPI;
import org.iucn.sis.shared.api.acl.base.AuthorizableObject;
import org.iucn.sis.shared.api.acl.feature.AuthorizableFeature;
import org.iucn.sis.shared.api.models.User;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.KeyListener;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.grid.CellEditor;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.EditorGrid;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.grid.GridSelectionModel;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.toolbar.PagingToolBar;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.IncrementalCommand;
import com.google.gwt.user.client.ui.HTML;
import com.solertium.lwxml.shared.GWTConflictException;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.NativeElement;
import com.solertium.lwxml.shared.NativeNodeList;
import com.solertium.lwxml.shared.utils.ArrayUtils;
import com.solertium.util.events.ComplexListener;
import com.solertium.util.events.SimpleListener;
import com.solertium.util.extjs.client.GenericPagingLoader;
import com.solertium.util.extjs.client.PagingLoaderFilter;
import com.solertium.util.extjs.client.WindowUtils;
import com.solertium.util.portable.PortableAlphanumericComparator;

/**
 * UserViewPanel.java
 * 
 * Panel that allows for perusing of users to view and in-line edit their
 * profile information.
 * 
 * @author carl.scott <carl.scott@solertium.com>
 * 
 */
public class UserViewPanel extends LayoutContainer implements HasRefreshableContent, UserViewToolbarAPI {

	private final LayoutContainer center;

	private EditorGrid<UserModelData> userGrid;
	private ListStore<UserModelData> store;
	private GenericPagingLoader<UserModelData> loader;
	private PagingToolBar pagingBar;

	private TextField<String> usernameFilter;
	private TextField<String> firstFilter;
	private TextField<String> lastFilter;
	private TextField<String> nicknameFilter;
	private TextField<String> affiliationFilter;
	private SimpleComboBox<String> activeAccountFilter;
	private FormPanel filterPanel;
	private Window filterPopup;

	private UserPermissionPanel permissionGroups;

	public UserViewPanel(ContentManager contentManager) {
		super();

		setScrollMode(Scroll.NONE);

		center = new LayoutContainer();
		center.setLayout(new FillLayout());
		center.setScrollMode(Scroll.NONE);

		loader = new GenericPagingLoader<UserModelData>();
		store = new ListStore<UserModelData>(loader.getPagingLoader());

		ArrayList<String> groups = new ArrayList<String>(AuthorizationCache.impl.getGroups().keySet());
		ArrayUtils.quicksort(groups, new PortableAlphanumericComparator());
		
		permissionGroups = new UserPermissionPanel();
		/*permissionGroups = new CheckboxMultiTriggerField(groups);
		permissionGroups.setDelimiter(",");
		permissionGroups.setFilterRegex("^ws\\d+.*");*/

		pagingBar = new PagingToolBar(50);
		pagingBar.bind(loader.getPagingLoader());

		usernameFilter = new TextField<String>();
		usernameFilter.setFieldLabel("Username");
		firstFilter = new TextField<String>();
		firstFilter.setFieldLabel("First Name");
		lastFilter = new TextField<String>();
		lastFilter.setFieldLabel("Last Name");
		nicknameFilter = new TextField<String>();
		nicknameFilter.setFieldLabel("Nickname");
		affiliationFilter = new TextField<String>();
		affiliationFilter.setFieldLabel("Affiliation");
		activeAccountFilter = new SimpleComboBox<String>();
		activeAccountFilter.setFieldLabel("Active Account");
		activeAccountFilter.add("");
		activeAccountFilter.add("true");
		activeAccountFilter.add("false");
		activeAccountFilter.findModel("").set("text", "Active");
		activeAccountFilter.findModel("true").set("text", "Active");
		activeAccountFilter.findModel("false").set("text", "Disabled");
		activeAccountFilter.setEditable(false);

		loader.setFilter(new PagingLoaderFilter<UserModelData>() {
			public boolean filter(UserModelData item, String property) {
				String active = (activeAccountFilter.getValue() == null || 
					"".equals(activeAccountFilter.getValue().getValue())) ? 
					null : activeAccountFilter.getValue().getValue();
				
				if (active != null & filterOut(active, (String) item.get(ClientUser.SIS_USER)))
					return true;
				if (filterOut(usernameFilter.getValue(), (String) item.get(ClientUser.USERNAME)))
					return true;
				if (filterOut(firstFilter.getValue(), (String) item.get(ClientUser.FIRST_NAME)))
					return true;
				if (filterOut(lastFilter.getValue(), (String) item.get(ClientUser.LAST_NAME)))
					return true;
				if (filterOut(nicknameFilter.getValue(), (String) item.get(ClientUser.NICKNAME)))
					return true;
				if (filterOut(affiliationFilter.getValue(), (String) item.get(ClientUser.AFFILIATION)))
					return true;

				return false;
			}

			private boolean filterOut(String value, String filterBy) {
				String text = value == null || "".equals(value) ? null : value.toLowerCase();
				return text != null && !filterBy.toLowerCase().startsWith(value.toLowerCase());
			}
		});

		setLayout(new BorderLayout());
	}

	public void addUser(String username) {
		final NativeDocument ndoc = SISClientBase.getHttpBasicNativeDocument();
		ndoc.get(UriBase.getInstance().getUserBase() + "/users/" + username, new GenericCallback<String>() {
			public void onSuccess(String result) {
				NativeElement node = ndoc.getDocumentElement().getElementByTagName(User.ROOT_TAG);
				loader.add(new UserModelData(ClientUser.fromXML(node)));
			}
			public void onFailure(Throwable caught) {
				WindowUtils.errorAlert("Error",
						"Failed to load new user.  If you want to view the new user, please reopen window.");
			}
		});
	}
	
	@Override
	public UserModelData getSelectedUser() {
		return userGrid.getSelectionModel().getSelectedItem();
	}
	
	@Override
	public void removeUser(UserModelData user) {
		loader.getFullList().remove(user);
		loader.getPagingLoader().load();
	}
	
	public void setUserState(UserModelData user, int state) {
		saveChange((String)user.get(ClientUser.USERNAME), ClientUser.STATE, Integer.toString(state));
	}
	
	@Override
	public void showFilter() {
		filterPopup = WindowUtils.newWindow("Set Filters");
		filterPopup.setLayout(new FillLayout());
		filterPopup.setStyleName("navigator");
		filterPopup.setSize(380, 260);
		filterPopup.add(filterPanel);
		filterPopup.show();
	}

	private void draw() {
		removeAll();
		
		ToolBar bar = new ToolBar();
		if (AuthorizationCache.impl.hasRight(SISClientBase.currentUser, AuthorizableObject.USE_FEATURE,
				AuthorizableFeature.USER_MANAGEMENT_FEATURE)) {
			bar = new UserViewToolBar(this);
		}

		LayoutContainer c = new LayoutContainer(new FitLayout());
		c.add(pagingBar);

		buildFilterPanel();

		add(bar, new BorderLayoutData(LayoutRegion.NORTH, 25));
		add(c, new BorderLayoutData(LayoutRegion.SOUTH, 25));
		add(center, new BorderLayoutData(LayoutRegion.CENTER));

		populateStore();

		layout();
	}
	
	private void buildFilterPanel() {
		filterPanel = new FormPanel();
		filterPanel.setBodyBorder(false);
		filterPanel.setBorders(false);
		filterPanel.setHeaderVisible(false);
		filterPanel.add(usernameFilter);
		filterPanel.add(firstFilter);
		filterPanel.add(lastFilter);
		filterPanel.add(nicknameFilter);
		filterPanel.add(affiliationFilter);
		filterPanel.add(activeAccountFilter);
		final Button applyFilters = new Button("Apply Filters", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				filterPopup.hide();
				loader.applyFilter("");
				pagingBar.setActivePage(1);
				loader.getPagingLoader().load();
			}
		});
		KeyListener enter = new KeyListener() {
			public void componentKeyPress(ComponentEvent event) {
				if (event.getKeyCode() == KeyCodes.KEY_ENTER)
					applyFilters.fireEvent(Events.Select);
			}
		};
		usernameFilter.addKeyListener(enter);
		firstFilter.addKeyListener(enter);
		lastFilter.addKeyListener(enter);
		nicknameFilter.addKeyListener(enter);
		affiliationFilter.addKeyListener(enter);

		filterPanel.addButton(applyFilters);
		filterPanel.addButton(new Button("Clear Filters", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				filterPopup.hide();
				usernameFilter.setValue("");
				firstFilter.setValue("");
				lastFilter.setValue("");
				nicknameFilter.setValue("");
				affiliationFilter.setValue("");
				activeAccountFilter.setValue(null);
				loader.applyFilter("");
				pagingBar.setActivePage(1);
				loader.getPagingLoader().load();
			}
		}));
	}

	private ColumnModel getColumnModel() {
		final List<ColumnConfig> configs = new ArrayList<ColumnConfig>();

		// ID field, not editable
		final ColumnConfig id = new ColumnConfig();
		id.setId("id");
		id.setHeader("ID");
		id.setWidth(30);
		configs.add(id);

		// User name, not editable
		final ColumnConfig name = new ColumnConfig();
		name.setId("username");
		name.setHeader("User Name");
		name.setWidth(100);
		{
			final TextField<String> text = new TextField<String>();
			text.setAllowBlank(false);
			text.setMaxLength(255);
			name.setEditor(new CellEditor(text));
		}
		configs.add(name);

		// First name, editable
		final ColumnConfig first = new ColumnConfig();
		first.setId("firstName");
		first.setWidth(100);
		first.setHeader("First Name");
		{
			final TextField<String> text = new TextField<String>();
			text.setAllowBlank(false);
			text.setAutoValidate(true);
			text.setMaxLength(255);
			first.setEditor(new CellEditor(text));
		}
		configs.add(first);

		final ColumnConfig last = new ColumnConfig();
		last.setId("lastName");
		last.setWidth(100);
		last.setHeader("Last Name");
		{
			final TextField<String> text = new TextField<String>();
			text.setAllowBlank(false);
			text.setAutoValidate(true);
			text.setMaxLength(255);
			last.setEditor(new CellEditor(text));
		}
		configs.add(last);
		
		final ColumnConfig nickname = new ColumnConfig();
		nickname.setId("nickname");
		nickname.setWidth(100);
		nickname.setHeader("Nickname");
		{
			final TextField<String> text = new TextField<String>();
			text.setAllowBlank(true);
			text.setAutoValidate(true);
			text.setMaxLength(255);
			nickname.setEditor(new CellEditor(text));
		}
		configs.add(nickname);

		final ColumnConfig initials = new ColumnConfig();
		initials.setId("initials");
		initials.setWidth(60);
		initials.setHeader("Initials");
		{
			final TextField<String> text = new TextField<String>();
			text.setAllowBlank(false);
			text.setAutoValidate(true);
			text.setMaxLength(255);
			initials.setEditor(new CellEditor(text));
			initials.getEditor().setToolTip(
					"If supplied, initials will be used in publications."
							+ "Otherwise, the first character of your first name will be used.");
		}
		configs.add(initials);

		final ColumnConfig affiliation = new ColumnConfig();
		affiliation.setId("affiliation");
		affiliation.setWidth(120);
		affiliation.setHeader("Affiliation");
		{
			final TextField<String> text = new TextField<String>();
			text.setAllowBlank(true);
			text.setAutoValidate(false);
			text.setMaxLength(2000);
			affiliation.setEditor(new CellEditor(text));
		}
		configs.add(affiliation);

		final ColumnConfig quickGroup = new ColumnConfig();
		quickGroup.setId("quickgroup");
		quickGroup.setWidth(120);
		quickGroup.setAlignment(HorizontalAlignment.CENTER);
		quickGroup.setHeader("Permission Groups");
		/*CellEditor cellEditor = new CellEditor(permissionGroups) {
			@Override
			protected void onBlur(FieldEvent fe) {
				fe.cancelBubble();
			}
		};
		quickGroup.setEditor(cellEditor);*/
		quickGroup.setHidden(!AuthorizationCache.impl.hasRight(SISClientBase.currentUser,
				AuthorizableObject.USE_FEATURE, AuthorizableFeature.PERMISSION_MANAGEMENT_FEATURE));
		quickGroup.setRenderer(new GridCellRenderer<UserModelData>() {
			@Override
			public Object render(final UserModelData model, final String property,
					ColumnData config, final int rowIndex, final int colIndex,
					final ListStore<UserModelData> store, final Grid<UserModelData> grid) {
				HTML button = new HTML("[Edit]");
				button.addStyleName("SIS_HyperlinkLookAlike");
				button.addClickHandler(new ClickHandler() {
					public void onClick(ClickEvent event) {
						final List<String> selected = new ArrayList<String>();
						final String q = model.get(property);
						if (q != null)
							for (String value : q.split(","))
								selected.add(value);
						
						permissionGroups.setSaveListener(new ComplexListener<List<String>>() {
							public void handleEvent(List<String> eventData) {
								final StringBuilder builder = new StringBuilder();
								for (Iterator<String> iter = eventData.iterator(); iter.hasNext(); )
									builder.append(iter.next() + (iter.hasNext() ? "," : ""));
								
								model.set(property, builder.toString());
								
								final GridEvent<UserModelData> event = 
									new GridEvent<UserModelData>(grid);
								event.setProperty(property);
								event.setValue(builder.toString());
								event.setStartValue(q);
								event.setRowIndex(rowIndex);
								event.setColIndex(colIndex);
								
								grid.fireEvent(Events.AfterEdit, event);
							}
						});
						permissionGroups.setSelection(selected);
						permissionGroups.show();
					}
				});
				
				return button;
			}
		});
		configs.add(quickGroup);

		final ColumnConfig sis = new ColumnConfig();

		final SimpleComboBox<String> sisUser = new SimpleComboBox<String>();
		sisUser.setForceSelection(true);
		sisUser.setTriggerAction(TriggerAction.ALL);
		sisUser.add("true");
		sisUser.add("false");
		CellEditor editor = new CellEditor(sisUser) {

			@Override
			public Object preProcessValue(Object value) {
				if (value == null)
					return value;
				return sisUser.findModel(value.toString());
			}

			@Override
			public Object postProcessValue(Object value) {
				if (value == null)
					return value;
				return ((ModelData) value).get("value");
			}

		};
		sis.setEditor(editor);
		sis.setId("sisUser");
		sis.setWidth(120);
		sis.setHeader("SIS Account");
		sis.setHidden(!AuthorizationCache.impl.hasRight(SISClientBase.currentUser, AuthorizableObject.USE_FEATURE,
				AuthorizableFeature.USER_MANAGEMENT_FEATURE));
		configs.add(sis);

		final ColumnConfig rapidlist = new ColumnConfig();
		rapidlist.setId("rapidListUser");
		rapidlist.setWidth(120);
		rapidlist.setHeader("RapidList Account");
		final SimpleComboBox<String> rapidListUser = new SimpleComboBox<String>();
		rapidListUser.setForceSelection(true);
		rapidListUser.setTriggerAction(TriggerAction.ALL);
		rapidListUser.add("true");
		rapidListUser.add("false");

		editor = new CellEditor(rapidListUser) {

			@Override
			public Object preProcessValue(Object value) {
				if (value == null)
					return value;
				return rapidListUser.findModel(value.toString());
			}

			@Override
			public Object postProcessValue(Object value) {
				if (value == null)
					return value;
				return ((ModelData) value).get("value");
			}

		};
		rapidlist.setEditor(editor);

		rapidlist.setHidden(!AuthorizationCache.impl.hasRight(SISClientBase.currentUser,
				AuthorizableObject.USE_FEATURE, AuthorizableFeature.USER_MANAGEMENT_FEATURE));
		configs.add(rapidlist);

		return new ColumnModel(configs);
	}

	private void finalizeStorePopulation() {

		loader.getPagingLoader().load(0, 50);
		pagingBar.setActivePage(1);
		store.filter("username");

		final GridSelectionModel<UserModelData> sm = new GridSelectionModel<UserModelData>();
		sm.setSelectionMode(SelectionMode.SINGLE);

		userGrid = new EditorGrid<UserModelData>(store, getColumnModel());
		userGrid.setSelectionModel(sm);
		userGrid.setBorders(false);
		userGrid.addListener(Events.AfterEdit, new Listener<GridEvent<UserModelData>>() {
			public void handleEvent(final GridEvent<UserModelData> be) {
				final ModelData model = be.getGrid().getStore().getAt(be.getRowIndex());
				if (be.getValue() == null) {
					store.rejectChanges();
					return;
				}

				final String value = ((String) be.getValue()).trim();
				final String originalValue = (String)be.getStartValue();
				
				if ((value == null && originalValue == null) || value != null && value.equals(originalValue)) {
					//No changes made.
					return;
				}
				
				final String col = be.getGrid().getColumnModel().getColumnId(be.getColIndex());
				
				final String username;
				if ("username".equals(col))
					username = originalValue;
				else
					username = model.get("username");
				
				saveChange(username, col, value, new GenericCallback<String>() {
					public void onFailure(Throwable caught) {
						be.getGrid().getStore().rejectChanges();
					}
					public void onSuccess(String result) {
						be.getGrid().getStore().commitChanges();
					}
				});
			}
		});

		center.add(userGrid);
		center.layout();
	}
	
	private void saveChange(final String username, final String column, final String value) {
		saveChange(username, column, value, null);
	}
	
	private void saveChange(final String username, final String column, final String value, final GenericCallback<String> callback) {
		final String body = "<root><field name=\"" + column + "\">" + value + "</field></root>";
		
		final NativeDocument document = SISClientBase.getHttpBasicNativeDocument();
		document.post(UriBase.getInstance().getUserBase() + "/users/" + username, body,
				new GenericCallback<String>() {
			public void onFailure(Throwable caught) {
				String error;
				if (caught instanceof GWTConflictException) {
					if ("username".equals(username))
						error = "An active user already exists with this username.";
					else
						error = "A conflict occured that prevented your changes from being saved.";
				}
				else
					error = "Could not save changes, please try again later.";
				
				WindowUtils.errorAlert(error);
				
				if (callback != null)
					callback.onFailure(caught);
			}
			public void onSuccess(String result) {
				Info.display("Success", "Changes saved.");
				
				if (callback != null)
					callback.onSuccess(result);
			}
		});
	}

	private void populateStore() {
		center.removeAll();

		final LayoutContainer container = new LayoutContainer();
		container.setLayout(new BorderLayout());
		loader.getFullList().clear();

		if (!AuthorizationCache.impl.hasRight(SISClientBase.currentUser, AuthorizableObject.USE_FEATURE,
				AuthorizableFeature.USER_MANAGEMENT_FEATURE)) {
			loader.add(new UserModelData(SISClientBase.currentUser));
			finalizeStorePopulation();

		} else {
			final NativeDocument ndoc = SISClientBase.getHttpBasicNativeDocument();
			ndoc.get(UriBase.getInstance().getUserBase() + "/users", new GenericCallback<String>() {
				public void onSuccess(String result) {
					IncrementalUserParser parser = new IncrementalUserParser(ndoc, loader);
					parser.setListener(new SimpleListener() {
						public void handleEvent() {
							finalizeStorePopulation();
						}
					});
					
					WindowUtils.showLoadingAlert("Loading Users...");
					
					DeferredCommand.addPause();
					DeferredCommand.addCommand(parser);
				}

				@Override
				public void onFailure(Throwable caught) {
					WindowUtils.errorAlert("Unable to load users");

				}
			});

		}

	}
	
	public void refresh() {
		permissionGroups.updateStore();
		draw();

	}
	
	public static class IncrementalUserParser implements IncrementalCommand {
		
		private static final int NUM_TO_PARSE = 200;
		
		private final NativeNodeList nodes;
		private final GenericPagingLoader<UserModelData> loader;
		
		private int current = 0;
		private int size;
		
		private SimpleListener listener;
		
		public IncrementalUserParser(NativeDocument document, GenericPagingLoader<UserModelData> loader) {
			this.loader = loader;
			this.nodes = document.getDocumentElement().getElementsByTagName(User.ROOT_TAG);
			this.size = nodes.getLength();
		}
		
		@Override
		public boolean execute() {
			if (current >= size) {
				ArrayUtils.quicksort(loader.getFullList(), new Comparator<UserModelData>() {
					public int compare(UserModelData o1, UserModelData o2) {
						return ((String) o1.get("username")).compareTo((String) o2.get("username"));
					}
				});
				//loader.getPagingLoader().load();
				
				WindowUtils.hideLoadingAlert();
				
				if (listener != null)
					listener.handleEvent();
				
				return false;
			}
			
			int max = current + NUM_TO_PARSE;
			if (max > size)
				max = size;
			
			WindowUtils.showLoadingAlert("Loading Users " + (current+1) + "-" + (max) + " of " + size);
			
			for (int i = current; i < current + NUM_TO_PARSE && i < size; i++)
				loader.add(new UserModelData(ClientUser.fromXML(nodes.elementAt(i))));
			
			current += NUM_TO_PARSE;
			
			return true;
		}
		
		public void setListener(SimpleListener listener) {
			this.listener = listener;
		}
		
	}

}
