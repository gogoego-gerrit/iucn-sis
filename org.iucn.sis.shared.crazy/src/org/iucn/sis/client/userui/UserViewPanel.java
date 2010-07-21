/**
 *
 */
package org.iucn.sis.client.userui;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.iucn.sis.client.acl.AuthorizationCache;
import org.iucn.sis.client.displays.SISView;
import org.iucn.sis.client.simple.SimpleSISClient;
import org.iucn.sis.client.userui.UserModelTabPanel.HasRefreshableContent;
import org.iucn.sis.shared.acl.base.AuthorizableObject;
import org.iucn.sis.shared.acl.feature.AuthorizableFeature;
import org.iucn.sis.shared.data.ViewCache;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.KeyEvent;
import com.extjs.gxt.ui.client.event.KeyListener;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.event.WindowEvent;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Popup;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.form.Validator;
import com.extjs.gxt.ui.client.widget.form.FormPanel.LabelAlign;
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
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.extjs.gxt.ui.client.widget.toolbar.PagingToolBar;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.solertium.lwxml.factory.NativeDocumentFactory;
import com.solertium.lwxml.shared.GWTConflictException;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.NativeElement;
import com.solertium.lwxml.shared.NativeNodeList;
import com.solertium.lwxml.shared.utils.ArrayUtils;
import com.solertium.lwxml.shared.utils.RowData;
import com.solertium.lwxml.shared.utils.RowParser;
import com.solertium.util.extjs.client.CheckboxMultiTriggerField;
import com.solertium.util.extjs.client.GenericPagingLoader;
import com.solertium.util.extjs.client.PagingLoaderFilter;
import com.solertium.util.extjs.client.WindowUtils;
import com.solertium.util.portable.PortableAlphanumericComparator;

import ext.ux.pwd.client.PasswordField;

/**
 * UserViewPanel.java
 * 
 * Panel that allows for perusing of users to view and in-line edit their
 * profile and custom field information.
 * 
 * @author carl.scott <carl.scott@solertium.com>
 * 
 */
public class UserViewPanel extends LayoutContainer implements HasRefreshableContent {

	/**
	 * Allows a username to be entered into the server and validated against the
	 * database. Conflict exception is thrown (server) and handled (clietn) is
	 * the name exists.
	 * 
	 * @author carl.scott <carl.scott@solertium.com>
	 * 
	 */
	public static abstract class AddUserWindow extends Window {

		private final TextField<String> username;
		private final PasswordField password;
		private final TextField<String> confirmPassword;
		
		private final TextField<String> firstname;
		private final TextField<String> lastname;
		private final TextField<String> initials;
		private final TextField<String> affiliation;
		
		private final Button submit;
		
		public AddUserWindow(final boolean createAccount) {
			super();
			setClosable(true);
			setModal(true);
			setHeading("Add User");

			setLayout(new FormLayout(LabelAlign.LEFT));
			
			add(username = new TextField<String>());
			username.setFieldLabel("Username (as e-mail address)");
			username.setAllowBlank(false);
			username.setValidator(new Validator() {
				public String validate(Field<?> field, String value) {
					if (!value.toUpperCase().matches("[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,4}"))
						return "This field must be a valid email address";

					return null;
				}
			});
			if( createAccount ) {
				add(password = new PasswordField());
				password.setFieldLabel("Password");
				
				add(confirmPassword = new TextField<String>());
				confirmPassword.setFieldLabel("Confirm Password");
				confirmPassword.setAllowBlank(false);
				confirmPassword.setPassword(true);
				confirmPassword.setValidator(new Validator() {
					public String validate(Field<?> field, String value) {
						if (!value.equals(password.getValue()))
							return "This field must match the password field.";

						return null;
					}
				});
			}
			else {
				password = null;
				confirmPassword = null;
			}
			
			add(firstname = new TextField<String>());
			firstname.setFieldLabel("First Name");
			add(lastname = new TextField<String>());
			lastname.setFieldLabel("Last Name");
			
			add(initials = new TextField<String>());
			initials.setFieldLabel("Initials (optional)");
			initials.setAllowBlank(true);
			
			add(affiliation = new TextField<String>());
			affiliation.setFieldLabel("Affiliation");

			submit = new Button("Submit", new SelectionListener<ButtonEvent>() {
				@Override
				public void componentSelected(ButtonEvent be) {
					if (!username.isValid())
						WindowUtils.errorAlert("Error", "Please enter a valid e-mail account as the user name.");
					else if(createAccount) {
						if (!username.isValid())
							WindowUtils.errorAlert("Error", "Please enter a valid password.");
						else if (!confirmPassword.isValid())
							WindowUtils.errorAlert("Error", "The contents of the \"confirm password\" field must match the contents of the \"password\" field exactly.");
						else {
							submit.setEnabled(false);
							putNewAccountAndProfile(username.getValue().toLowerCase(), password.getValue());
						}
					} else {
						submit.setEnabled(false);
						putNewAccountAndProfile(username.getValue().toLowerCase(), "");
					}

				}

				private void putNewAccountAndProfile(String u, String p) {
					StringBuffer xml = new StringBuffer("<auth>");
					xml.append("<u>");
					xml.append(u);
					xml.append("</u>");
					xml.append("<p>");
					xml.append(p);
					xml.append("</p>");
					xml.append("</auth>");

					final NativeDocument doc = NativeDocumentFactory.newNativeDocument();
					doc.putAsText("/authn", xml.toString(), new GenericCallback<String>() {
						public void onFailure(Throwable caught) {
							WindowUtils.errorAlert("Error!", "An account is already allocated "
									+ "to this e-mail address. Please try again.");
							submit.setEnabled(true);
						}

						public void onSuccess(String result) {
							putNewProfile();
						}
					});
				}

				private void putNewProfile() {
					final NativeDocument createDoc = NativeDocumentFactory.newNativeDocument();
					createDoc.put(UserModelTabPanel.CONSTANTS_ATTACHMENT_POINT + getUrl(), 
							"<root><username><![CDATA[" + username.getValue().toLowerCase() + "]]></username></root>", new GenericCallback<String>() {
						public void onSuccess(String result) {
							final NativeNodeList nodes = createDoc.getDocumentElement().getElementsByTagName(getElementName());
							String id = "";
							for (int i = 0; i < nodes.getLength(); i++) {
								NativeElement current = nodes.elementAt(i);
								id = current.getAttribute("id");
							}
								
							final NativeDocument document = NativeDocumentFactory.newNativeDocument();
							document.post(UserModelTabPanel.CONSTANTS_ATTACHMENT_POINT + getUrl() + "/" + id,
									getXML(username.getValue().toLowerCase(), firstname.getValue(), lastname.getValue(), 
											affiliation.getValue() == null ? "" : affiliation.getValue(), 
											initials.getValue() == null ? "" : initials.getValue()), 
											new GenericCallback<String>() {
										public void onFailure(Throwable caught) {
											if (caught instanceof GWTConflictException)
												WindowUtils.errorAlert("Error",
														"This entry already exists, please try again.");
											else
												WindowUtils.errorAlert("Error", "Server error, please try again later.");
										}

										public void onSuccess(String result) {
											close();
											AddUserWindow.this.onSuccess(username.getValue().toLowerCase());
										}
									});
						}
						public void onFailure(Throwable caught) {
							WindowUtils.errorAlert("Error", "Server error, please try again later.");
						}
					});
					
					
				}
			});
			addButton(submit);
			addButton(new Button("Cancel", new SelectionListener<ButtonEvent>() {
				@Override
				public void componentSelected(ButtonEvent ce) {
					close();
				}
			}));
		}

		/**
		 * Get the node element name
		 * 
		 * @return
		 */
		public abstract String getElementName();

		/**
		 * Get the URL where to PUT data to the server
		 * 
		 * @return
		 */
		public abstract String getUrl();

		/**
		 * Retrieve appropriate XML for this information, given the text entry
		 * @param initials TODO
		 * @param textEntry
		 * 
		 * @return
		 */
		public abstract String getXML(String username, String firstname, String lastname, String affiliation, String initials);

		/**
		 * Occurs when a successful entry has been added to the server.
		 * 
		 * @param newID
		 *            the server-generated ID
		 * @param textEntry
		 *            the text entered.
		 */
		public abstract void onSuccess(String textEntry);

	}

	/**
	 * CustomField
	 * 
	 * Simple wrapper that parses a CustomField row result and prepares simple
	 * getter methods for the name, id and any appropriate editors.
	 * 
	 * @author carl.scott <carl.scott@solertium.com>
	 * 
	 */
	private static class CustomField {
		private final String delim = "::";
		private final RowData rowData;

		public CustomField(final RowData rowData) {
			this.rowData = rowData;
		}

		public CellEditor getEditor() {
			CellEditor editor = null;
			final String type = rowData.getField("type");
			if ("text".equalsIgnoreCase(type)) {
				TextField<String> text = new TextField<String>();
				text.setAllowBlank("true".equals(rowData.getField("required")));
				editor = new CellEditor(text);
			} else if ("select".equalsIgnoreCase(type)) {
				String options = rowData.getField("options");
				if (options != null && !options.equals("")) {
					final SimpleComboBox<String> box = new SimpleComboBox<String>();
					String[] split = options.split(delim);
					for (int i = 0; i < split.length; i++)
						box.add(split[i]);
					editor = new CellEditor(box) {
						@Override
						public Object postProcessValue(Object value) {
							return value == null ? value : ((ModelData) value).get("value");
						}

						@Override
						public Object preProcessValue(Object value) {
							return value == null ? value : box.findModel(value.toString());
						}
					};
				}
			}
			return editor;
		}

		public String getID() {
			return rowData.getField("id");
		}

		public String getName() {
			return rowData.getField("name");
		}

		@Override
		public String toString() {
			return rowData.toString();
		}
	}

	/**
	 * UserModelDaata
	 * 
	 * Takes search results for users and converts them to model data.
	 * 
	 * @author carl.scott <carl.scott@solertium.com>
	 * 
	 */
	private static class UserModelData extends BaseModelData {
		private static final long serialVersionUID = 1L;

		public UserModelData(RowData rowData) {
			super();
			final Iterator<String> iterator = rowData.keySet().iterator();
			while (iterator.hasNext()) {
				String key = iterator.next().toLowerCase();
				if (key.startsWith("custom")) {
					String id = key.split(":")[1];
					set(id, rowData.getField(key));
				} else
					set(key, rowData.getField(key));
			}
		}
	}

	private final LayoutContainer center;

	private final HashMap<String, CustomField> customFields;

	private EditorGrid<UserModelData> userGrid;
	private ListStore<UserModelData> store;
	private GenericPagingLoader<UserModelData> loader;
	private PagingToolBar pagingBar;
	
	private TextField<String> usernameFilter;
	private TextField<String> firstFilter;
	private TextField<String> lastFilter;
	private TextField<String> affiliationFilter;
	private SimpleComboBox<String> activeAccountFilter;
	private FormPanel filterPanel;
	private Popup filterPopup;
	
	private final LayoutContainer customFieldContainer;

	private final ContentManager contentManager;

	private final CheckboxMultiTriggerField permissionGroups;
	
	public UserViewPanel(ContentManager contentManager) {
		super();

		setScrollMode(Scroll.NONE);
		
		this.contentManager = contentManager;

		customFields = new HashMap<String, CustomField>();

		customFieldContainer = new LayoutContainer();
		customFieldContainer.setLayout(new FillLayout());

		center = new LayoutContainer();
		center.setLayout(new FillLayout());
		center.setScrollMode(Scroll.NONE);
		
		loader = new GenericPagingLoader<UserModelData>();
		store = new ListStore<UserModelData>(loader.getPagingLoader());

		ArrayList<String> groups = new ArrayList<String>(AuthorizationCache.impl.getGroups().keySet());
		ArrayUtils.quicksort(groups, new PortableAlphanumericComparator());
		permissionGroups = new CheckboxMultiTriggerField(groups);
		permissionGroups.setDelimiter(",");
		permissionGroups.setFilterRegex("^ws\\d+.*");
		
		pagingBar = new PagingToolBar(50);
		pagingBar.bind(loader.getPagingLoader());
		
		usernameFilter = new TextField<String>();
		usernameFilter.setFieldLabel("Username");
		firstFilter = new TextField<String>();
		firstFilter.setFieldLabel("First Name");
		lastFilter = new TextField<String>();
		lastFilter.setFieldLabel("Last Name");
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
				if( activeAccountFilter.getValue() != null && filterOut(activeAccountFilter.getValue().getValue().equals("") ? null : 
						activeAccountFilter.getValue().getValue(), (String)item.get("sis")))
					return true;
				if( filterOut(usernameFilter.getValue(), (String)item.get("username")) )
					return true;
				if( filterOut(firstFilter.getValue(), (String)item.get("firstname")) )
					return true;
				if( filterOut(lastFilter.getValue(), (String)item.get("lastname")) )
					return true;
				if( filterOut(affiliationFilter.getValue(), (String)item.get("affiliation")) )
					return true;

				return false;
			}
			
			private boolean filterOut(String value, String filterBy) {
				return value == null ? false : !filterBy.toLowerCase().startsWith(value.toLowerCase());
			}
		});
		
		setLayout(new BorderLayout());
	}

	private void draw() {
		removeAll();
		
		final ToolBar bar = new ToolBar();

		if( AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, 
				AuthorizableObject.USE_FEATURE, AuthorizableFeature.USER_MANAGEMENT_FEATURE)) {
			Button item = new Button("Add Profile and Create Password", new SelectionListener<ButtonEvent>() {
				@Override
				public void componentSelected(ButtonEvent ce) {
					AddUserWindow win = new AddUserWindow(true) {
						@Override
						public String getElementName() {
							return "user";
						}

						@Override
						public String getUrl() {
							return "/list";
						}

						@Override
						public String getXML(String username, String firstname, String lastname, String affiliation, String initials) {
							StringBuilder ret = new StringBuilder("<root>");
							ret.append("<field name=\"username\"><![CDATA[" + username + "]]></field>");
							ret.append("<field name=\"firstname\"><![CDATA[" + firstname + "]]></field>");
							ret.append("<field name=\"lastname\"><![CDATA[" + lastname + "]]></field>");
							ret.append("<field name=\"initials\"><![CDATA[" + initials + "]]></field>");
							ret.append("<field name=\"affiliation\"><![CDATA[" + affiliation + "]]></field>");
							ret.append("<field name=\"sis\">true</field>");
							ret.append("</root>");
							return ret.toString();
						}

						@Override
						public void onSuccess(String username) {
							Info.display("Success", "User {0} added", username);
							populateStore();
						}
					};
					win.show();
					win.setSize(410, 325);
					win.center();
				}
			});
			item.setIconStyle("icon-user-suit");
			bar.add(item);
			
			bar.add(new SeparatorToolItem());
			item = new Button("Add Profile", new SelectionListener<ButtonEvent>() {
				@Override
				public void componentSelected(ButtonEvent ce) {
					AddUserWindow win = new AddUserWindow(false) {
						@Override
						public String getElementName() {
							return "user";
						}

						@Override
						public String getUrl() {
							return "/list";
						}

						@Override
						public String getXML(String username, String firstname, String lastname, String affiliation, String initials) {
							StringBuilder ret = new StringBuilder("<root>");
							ret.append("<field name=\"username\"><![CDATA[" + username + "]]></field>");
							ret.append("<field name=\"firstname\"><![CDATA[" + firstname + "]]></field>");
							ret.append("<field name=\"lastname\"><![CDATA[" + lastname + "]]></field>");
							ret.append("<field name=\"initials\"><![CDATA[" + initials + "]]></field>");
							ret.append("<field name=\"affiliation\"><![CDATA[" + affiliation + "]]></field>");
							ret.append("<field name=\"sis\">false</field>");
							ret.append("</root>");
							return ret.toString();
						}

						@Override
						public void onSuccess(String username) {
							Info.display("Success", "User {0} added", username);
							populateStore();
						}
					};
					win.show();
					win.setSize(410, 325);
					win.center();
				}
			});
			item.setIconStyle("icon-user-green");
			bar.add(item);

			bar.add(new SeparatorToolItem());
			if( AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, 
					AuthorizableObject.USE_FEATURE, AuthorizableFeature.DELETE_USERS_FEATURE)) {
				item = new Button("Delete User", new SelectionListener<ButtonEvent>() {
					@Override
					public void componentSelected(ButtonEvent ce) {
						if( userGrid.getSelectionModel().getSelectedItem() != null ) {
							final UserModelData selected = userGrid.getSelectionModel().getSelectedItem();
							WindowUtils.confirmAlert("Delete User", "Are you sure you want to delete the user " 
									+ selected.get("username") +"? This SHOULD NOT be performed on a user that "
									+ "is an assessor or contributor on an assessment, as that information " +
									"will be irretrievably lost.", new Listener<MessageBoxEvent>() {
								public void handleEvent(MessageBoxEvent be) {
									if( be.getButtonClicked().getText().equalsIgnoreCase("yes") ) {
										NativeDocument ndoc = SimpleSISClient.getHttpBasicNativeDocument();
										ndoc.post("/authn/remove", "<root><u>" + selected.get("username") + "</u></root>", new GenericCallback<String>() {
											public void onSuccess(String result) {
												final NativeDocument document = NativeDocumentFactory.newNativeDocument();
												document.delete(UserModelTabPanel.CONSTANTS_ATTACHMENT_POINT + "/list/" + selected.get("id"),
														new GenericCallback<String>() {
													public void onFailure(Throwable caught) {
														WindowUtils.errorAlert("Error",
																"User login " + selected.get("username") + " removed, but " +
																"could not remove user information from database. " +
														"Please report this error to an SIS administrator.");
													}

													public void onSuccess(String result) {
														//Removing zendesk account
														NativeDocument zdoc = SimpleSISClient.getHttpBasicNativeDocument();
														//String external_id = String.valueOf(SimpleSISClient.currentUser.getId());
														
														String xml = "<root><user email=\""+selected.get("username")+"\"/></root>";
														zdoc.post("/zendesk/remove/", xml, new GenericCallback<String>() {
															
															public void onSuccess(String result) {
																// TODO Auto-generated method stub
																
															}
															
															public void onFailure(Throwable caught) {
																// TODO Auto-generated method stub
																
															}
														});
														
														Info.display("Success", "User {0} removed.", (String) selected.get("username"));
														populateStore();
													}
												});
											}
											public void onFailure(Throwable caught) {
												String message;
												if( caught.getMessage().equals("412") )
													message = "Sorry - you cannot delete yourself.";
												else if( caught.getMessage().equals("500") )
													message = "Error deleting this user. Server failure - " +
													"please report this to an SIS administrator, " +
													"along with the user you were attempting to delete.";
												else
													message = "Error deleting this user. Please check your connection and try again.";

												WindowUtils.errorAlert("Delete failed!", message);
											}
										});
									}
								};
							});
						} else
							WindowUtils.errorAlert("Please select a user to delete.");
					}
				});
				item.setIconStyle("icon-user-delete");
				bar.add(item);
			}
			
			bar.add(new SeparatorToolItem());
			item = new Button("Reset Password", new SelectionListener<ButtonEvent>() {
				@Override
				public void componentSelected(ButtonEvent ce) {
					if( userGrid.getSelectionModel().getSelectedItem() != null ) {
						final String username = userGrid.getSelectionModel().getSelectedItem().get("username");
						WindowUtils.confirmAlert("Reset Password", "Are you sure you want to reset " 
								+ username + "'s password? A new password will be supplied via e-mail.", new Listener<MessageBoxEvent>() {
							public void handleEvent(MessageBoxEvent be) {
								if( be.getButtonClicked().getText().equalsIgnoreCase("yes") ) {
									NativeDocument ndoc = SimpleSISClient.getHttpBasicNativeDocument();
									ndoc.post("/authn/reset", "<root><u>" + username + "</u></root>", new GenericCallback<String>() {
										public void onSuccess(String result) {
											Info.display("Reset Success!", "A new password for {0} has been sent.", username);
										}
										public void onFailure(Throwable caught) {
											WindowUtils.errorAlert("Reset failed!", "Resetting this " +
													"user's password failed. Please check your Internet connection and try again.");
										}
									});
								}
							};
						});
					} else
						WindowUtils.errorAlert("Please select a user.");
				}
			});
			item.setIconStyle("icon-user-go");
			bar.add(item);
			
			bar.add(new SeparatorToolItem());
			item = new Button("Show Filter(s)", new SelectionListener<ButtonEvent>() {
				public void componentSelected(ButtonEvent ce) {
					filterPopup = new Popup();
					filterPopup.setStyleName("navigator");
					filterPopup.setShim(true);
//					filterPopup.setShadow(true);
					filterPopup.add(filterPanel);
					filterPopup.setLayout(new FitLayout());
					filterPopup.setSize(380, 190);
					filterPopup.show();
					filterPopup.setPagePosition(ce.getButton().getAbsoluteLeft()-160 > 0 ? 
							ce.getButton().getAbsoluteLeft()-160 : 0, ce.getButton().getAbsoluteTop()+30);
				}
			});
			item.setIconStyle("icon-user-comment");
			bar.add(item);
		}

		LayoutContainer c = new LayoutContainer(new FitLayout());
		c.add(pagingBar);
		
		filterPanel = new FormPanel();
		filterPanel.setHeaderVisible(false);
		filterPanel.add(usernameFilter);
		filterPanel.add(firstFilter);
		filterPanel.add(lastFilter);
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
			public void componentKeyPress(KeyEvent event) {
				if( event.getKeyCode() == KeyboardListener.KEY_ENTER )
					applyFilters.fireEvent(Events.Select);
			}
		};
		usernameFilter.addKeyListener(enter);
		firstFilter.addKeyListener(enter);
		lastFilter.addKeyListener(enter);
		affiliationFilter.addKeyListener(enter);
		
		filterPanel.getButtonBar().add(applyFilters);
		filterPanel.getButtonBar().add(new Button("Clear Filters", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				filterPopup.hide();
				usernameFilter.setValue("");
				firstFilter.setValue("");
				lastFilter.setValue("");
				affiliationFilter.setValue("");
				activeAccountFilter.setValue(activeAccountFilter.findModel(""));
				loader.applyFilter("");
				pagingBar.setActivePage(1);
				loader.getPagingLoader().load();
			}
		}));
//		filterPanel.setAlignment(HorizontalAlignment.LEFT);
		
		add(bar, new BorderLayoutData(LayoutRegion.NORTH, 25));
		add(c, new BorderLayoutData(LayoutRegion.SOUTH, 25));
		add(center, new BorderLayoutData(LayoutRegion.CENTER));

		populateStore();

		layout();
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
		configs.add(name);

		// First name, editable
		final ColumnConfig first = new ColumnConfig();
		first.setId("firstname");
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
		last.setId("lastname");
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
			initials.getEditor().setToolTip("If supplied, initials will be used in publications." +
			"Otherwise, the first character of your first name will be used.");
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
		quickGroup.setHeader("Permission Groups");
		quickGroup.setEditor(new CellEditor(permissionGroups));
		quickGroup.setHidden(!AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, 
				AuthorizableObject.USE_FEATURE, AuthorizableFeature.PERMISSION_MANAGEMENT_FEATURE));
		configs.add(quickGroup);
		
		final ColumnConfig sis = new ColumnConfig();
		sis.setId("sis");
		sis.setWidth(120);
		sis.setHeader("SIS Account");
		{
			final SimpleComboBox<String> text = new SimpleComboBox<String>();
			text.add("true");
			text.add("false");
			text.setDisplayField("name");
			text.findModel("true").set("name", "Active");
			text.findModel("true").set("style", "greenFont");
			text.findModel("false").set("name", "Disabled");
			text.findModel("false").set("style", "redFont");
			text.setEditable(false);
			text.setForceSelection(true);
			sis.setEditor(new CellEditor(text) {
				@Override
				public Object postProcessValue(Object value) {
					return value == null ? value : ((ModelData) value).get("value");
				}

				@Override
				public Object preProcessValue(Object value) {
					Object ret = value == null ? value : (value.equals("") ? text.findModel("true") : text.findModel(value.toString()));
					setStyleAttribute("color", "green");
					
					return ret;
				}
			});
			
			sis.setRenderer(new GridCellRenderer() {
				public Object render(ModelData model, String property,
						ColumnData config, int rowIndex, int colIndex,
						ListStore store, Grid grid) {
					ModelData found = text.findModel((String)model.get(property));
					if( found != null ) {
						return found.get("name");
					} else
						return model.get(property);
				}
			});
		}
		sis.setHidden(!AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, 
				AuthorizableObject.USE_FEATURE, AuthorizableFeature.USER_MANAGEMENT_FEATURE));
		configs.add(sis);
		
		final ColumnConfig rapidlist = new ColumnConfig();
		rapidlist.setId("rapidlist");
		rapidlist.setWidth(120);
		rapidlist.setHeader("RapidList Account");
		{
			final SimpleComboBox<String> text = new SimpleComboBox<String>();
			text.add("true");
			text.add("false");
			text.setEditable(false);
			text.setForceSelection(true);
			rapidlist.setEditor(new CellEditor(text) {
				@Override
				public Object postProcessValue(Object value) {
					return value == null ? value : ((ModelData) value).get("value");
				}

				@Override
				public Object preProcessValue(Object value) {
					return value == null ? value : (value.equals("") ? text.findModel("true") : text.findModel(value.toString()));
				}
			});
		}
		rapidlist.setHidden(!AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, 
				AuthorizableObject.USE_FEATURE, AuthorizableFeature.USER_MANAGEMENT_FEATURE));
		configs.add(rapidlist);
		
		// Custom Fields
		for( Entry<String, CustomField> curEntry : customFields.entrySet() ) {
			final CustomField current = curEntry.getValue();
			final ColumnConfig col = new ColumnConfig();
			col.setId(current.getID());
			col.setHeader(current.getName());
			col.setWidth(120);
			CellEditor editor = current.getEditor();
			if (editor != null)
				col.setEditor(current.getEditor());
			
			if( current.getName().equalsIgnoreCase("viewPreference") ) {
				List<String> viewIDs = new ArrayList<String>();
				for( SISView curView : ViewCache.impl.getAvailableViews() )
					viewIDs.add(curView.getId());
					
				CheckboxMultiTriggerField viewSelectorField = new CheckboxMultiTriggerField(viewIDs, ",");
				col.setEditor(new CellEditor(viewSelectorField));
			}
			
			configs.add(col);
		}

		return new ColumnModel(configs);
	}

	/**
	 * Load in initial custom field data so the appropriate columns can be
	 * drawn.
	 * 
	 * @param callback
	 */
	private void init(final GenericCallback<String> callback) {
		final NativeDocument cf = NativeDocumentFactory.newNativeDocument();
		cf.get(UserModelTabPanel.CONSTANTS_ATTACHMENT_POINT + "/dump/customfield", new GenericCallback<String>() {
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}

			public void onSuccess(String result) {
				ArrayList<String> groups = new ArrayList<String>(AuthorizationCache.impl.getGroups().keySet());
				ArrayUtils.quicksort(groups, new PortableAlphanumericComparator());
				permissionGroups.setOptions(groups);
				
				customFields.clear();
				final RowParser parser = new RowParser(cf);
				for( RowData current : parser.getRows() )
					customFields.put(current.getField("id"), new CustomField(current));
				
				callback.onSuccess(null);
			}
		});
	}

	private void populateStore() {
		center.removeAll();
		final NativeDocument document = NativeDocumentFactory.newNativeDocument();
		document.get(UserModelTabPanel.CONSTANTS_ATTACHMENT_POINT + "/list", new GenericCallback<String>() {
			public void onFailure(Throwable caught) {
				center.add(new HTML("Could not load users"));
			}

			public void onSuccess(String result) {
				final LayoutContainer container = new LayoutContainer();
				container.setLayout(new BorderLayout());

//				store.removeAll();
				loader.getFullList().clear();
				final RowParser parser = new RowParser(document);
				
				if( !AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, 
						AuthorizableObject.USE_FEATURE, AuthorizableFeature.USER_MANAGEMENT_FEATURE) ) {
					for( RowData curRow : parser.getRows() ) {
						if( curRow.getField("username").equalsIgnoreCase(SimpleSISClient.currentUser.username) )
							loader.add(new UserModelData(curRow));
					}
				} else {
					for( RowData curRow : parser.getRows() )
						loader.add(new UserModelData(curRow));
				}
				
				ArrayUtils.quicksort(loader.getFullList(), new Comparator<UserModelData>() {
					public int compare(UserModelData o1, UserModelData o2) {
						return ((String)o1.get("username")).compareTo((String)o2.get("username"));
					}
				});
				
				loader.getPagingLoader().load(0, 50);
				pagingBar.setActivePage(1);
				store.filter("username");
				
				final GridSelectionModel<UserModelData> sm = new GridSelectionModel<UserModelData>();
				sm.setSelectionMode(SelectionMode.SINGLE);

				userGrid = new EditorGrid<UserModelData>(store, getColumnModel());
				userGrid.setSelectionModel(sm);
				userGrid.setBorders(false);
//				userGrid.setWidth(700 + (customFields.size() * 120));
				userGrid.addListener(Events.AfterEdit, new Listener<GridEvent>() {
					public void handleEvent(final GridEvent be) {
						final ModelData model = be.getGrid().getStore().getAt(be.getRowIndex());
						final String colID = be.getGrid().getColumnModel().getColumnId(be.getColIndex());
						if (be.getValue() == null) {
							store.rejectChanges();
							return;
						}
						if (customFields.containsKey(colID)) {
							final String id = model.get("id");
							final String body = "<root><customfield name=\"" + colID + "\">" + be.getValue()
									+ "</customfield></root>";
							final NativeDocument document = NativeDocumentFactory.newNativeDocument();
							document.post(UserModelTabPanel.CONSTANTS_ATTACHMENT_POINT + "/list/" + id, body,
									new GenericCallback<String>() {
										public void onFailure(Throwable caught) {
											Info.display("Error", "Could not save changes, please try again later.");
										}

										public void onSuccess(String result) {
											Info.display("Success", "Changes saved.");
											be.getGrid().getStore().commitChanges();
										}
									});
						} else {
							final String id = model.get("id");
							final String col = be.getGrid().getColumnModel().getColumnId(be.getColIndex());
							final String body = "<root><field name=\"" + col + "\">" + be.getValue() + "</field></root>";
							final NativeDocument document = NativeDocumentFactory.newNativeDocument();
							document.post(UserModelTabPanel.CONSTANTS_ATTACHMENT_POINT + "/list/" + id, body,
									new GenericCallback<String>() {
										public void onFailure(Throwable caught) {
											Info.display("Error", "Could not save changes, please try again later.");
										}

										public void onSuccess(String result) {
											Info.display("Success", "Changes saved.");
											be.getGrid().getStore().commitChanges();
										}
									});
						}
					}
				});

				center.add(userGrid);
//				center.setScrollMode(Scroll.AUTO);
				center.layout();
			}
		});
	}

	public void refresh() {
		init(new GenericCallback<String>() {
			public void onFailure(Throwable caught) {
				WindowUtils.errorAlert("Error", "Could not initialize, please try again later");
			}

			public void onSuccess(String result) {
				draw();
//				WindowUtils.hideLoadingAlert();
			}
		});
	}

}
