package org.iucn.sis.client.panels.users;

import org.iucn.sis.client.api.caches.AuthorizationCache;
import org.iucn.sis.client.api.container.SISClientBase;
import org.iucn.sis.client.api.models.ClientUser;
import org.iucn.sis.client.api.ui.users.panels.AddUserWindow;
import org.iucn.sis.client.api.utils.UriBase;
import org.iucn.sis.shared.api.acl.base.AuthorizableObject;
import org.iucn.sis.shared.api.acl.feature.AuthorizableFeature;
import org.iucn.sis.shared.api.models.User;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.extjs.gxt.ui.client.widget.menu.SeparatorMenuItem;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.util.events.ComplexListener;
import com.solertium.util.extjs.client.WindowUtils;

public class UserViewToolBar extends ToolBar {
	
	private final UserViewToolbarAPI api;
	
	public UserViewToolBar(final UserViewToolbarAPI api) {
		super();
		this.api = api;
		
		build();
	}
	
	private void build() {
		add(newButton("Add Profile and Create Password", "icon-user-suit", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				AddUserWindow win = new AddUserWindow(true, new ComplexListener<String>() {
					public void handleEvent(String result) {
						api.addUser(result);
					}
				});
				win.show();
				win.setSize(410, 325);
				win.center();
			}
		}));
		add(new SeparatorToolItem());
		
		add(newButton("Add Profile", "icon-user-green", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				AddUserWindow win = new AddUserWindow(false, new ComplexListener<String>() {
					public void handleEvent(String result) {
						api.addUser(result);
					}
				});
				win.show();
				win.setSize(410, 325);
				win.center();
			}
		}));
		
		add(new SeparatorToolItem());
		
		add(newButton("Show Filter(s)", "icon-user-comment", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				api.showFilter();
			}
		}));

		add(new FillToolItem());
		
		add(newButton("Manage Selected User", "icon-gear", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				UserModelData selected = api.getSelectedUser();
				if (selected == null)
					WindowUtils.errorAlert("Please select a user.");
				else {
					Menu menu = createMenu(selected);
					menu.show(ce.getButton());
				}
			}
		}));
		
	}
	
	private Menu createMenu(final UserModelData selected) {
		Menu menu = new Menu();
		
		if (canDelete()) {
			menu.add(newMenuItem("Delete User", "icon-user-delete", new SelectionListener<MenuEvent>() {
				public void componentSelected(MenuEvent ce) {
					WindowUtils.confirmAlert("Delete User", "Are you sure you want to delete the user "
							+ selected.get("username") + "? This SHOULD NOT be performed on a user that "
							+ "is an assessor or contributor on an assessment, as that information "
							+ "will be irretrievably lost.", new WindowUtils.SimpleMessageBoxListener() {
						public void onYes() {
							NativeDocument ndoc = SISClientBase.getHttpBasicNativeDocument();
							ndoc.post(UriBase.getInstance().getSISBase() + "/authn/authn/remove",
									"<root><u>" + selected.get("username") + "</u></root>",
									new GenericCallback<String>() {
								public void onFailure(Throwable caught) {
									String message;
									if (caught.getMessage().equals("412"))
										message = "Sorry - you cannot delete yourself.";
									else if (caught.getMessage().equals("500"))
										message = "Error deleting this user. Server failure - "
											+ "please report this to an SIS administrator, "
											+ "along with the user you were attempting to delete.";
									else
										message = "Error deleting this user. Please check your connection and try again.";
										
									WindowUtils.errorAlert("Delete failed!", message);
								}
								public void onSuccess(String result) {
									// Removing zendesk account
									NativeDocument zdoc = SISClientBase.getHttpBasicNativeDocument();
									String xml = "<root><user email=\"" + selected.get("username")
										+ "\"/></root>";
									zdoc.post(UriBase.getInstance().getZendeskBase()
											+ "/zendesk/remove/", xml,
											new GenericCallback<String>() {
										public void onSuccess(String result) {}
										public void onFailure(Throwable caught) {
											Info.display("Error", "Failed to delete zen desk account associated with user " + selected.get("username"));
										}
									});
									Info.display("Success", "User {0} removed.", (String) selected
											.get("username"));
									
									api.removeUser(selected);
								}
							});
						};
					});
				}
			}));
			
			menu.add(new SeparatorMenuItem());
		}
		
		menu.add(newMenuItem("Reset Password", "icon-user-go", new SelectionListener<MenuEvent>() {
			public void componentSelected(MenuEvent ce) {
				final String username = selected.get("username");
				WindowUtils.confirmAlert("Reset Password", "Are you sure you want to reset " + username
						+ "'s password? A new password will be supplied via e-mail.",
						new WindowUtils.SimpleMessageBoxListener() {
					public void onYes() {
						NativeDocument ndoc = SISClientBase.getHttpBasicNativeDocument();
						ndoc.post(UriBase.getInstance().getSISBase() + "/authn/reset", "<root><u>"
								+ username + "</u></root>", new GenericCallback<String>() {
							public void onSuccess(String result) {
								Info.display("Reset Success!",
										"A new password for {0} has been sent.", username);
							}
							public void onFailure(Throwable caught) {
								WindowUtils.errorAlert("Reset failed!", "Resetting this "
										+ "user's password failed. Please check your Internet connection and try again.");
							}
						});
					}
				});
			}
		}));
		
		menu.add(new SeparatorMenuItem());
		
		final boolean active = Integer.toString(User.ACTIVE).equals(selected.get(ClientUser.STATE));
		menu.add(newMenuItem(active ? "Disable Account" : "Activate Account", "", new SelectionListener<MenuEvent>() {
			public void componentSelected(MenuEvent ce) {
				api.setUserState(selected, active ? User.DELETED : User.ACTIVE);
			}
		}));
		
		return menu;
	}
	
	private boolean canDelete() {
		return AuthorizationCache.impl.hasRight(SISClientBase.currentUser, AuthorizableObject.USE_FEATURE,
				AuthorizableFeature.DELETE_USERS_FEATURE);		
	}

	private Button newButton(String text, String iconStyle, SelectionListener<ButtonEvent> listener) {
		Button button = new Button(text, listener);
		button.setIconStyle(iconStyle);
		
		return button;
	}
	
	private MenuItem newMenuItem(String text, String iconStyle, SelectionListener<MenuEvent> listener) {
		MenuItem item = new MenuItem(text, listener);
		item.setIconStyle(iconStyle);
		
		return item;
	}
	
	public static interface UserViewToolbarAPI {
		
		public void addUser(String username);
		
		public void removeUser(UserModelData user);
		
		public UserModelData getSelectedUser();
		
		public void setUserState(UserModelData user, int state);
		
		public void showFilter();
		
	}

}
