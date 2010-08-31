package org.iucn.sis.client.panels.login;

import org.iucn.sis.client.api.container.SISClientBase.SimpleSupport;
import org.iucn.sis.client.api.utils.UriBase;
import org.iucn.sis.client.container.SimpleSISClient;
import org.iucn.sis.client.panels.users.AddUserWindow;
import org.iucn.sis.client.panels.utils.GoogleAccountsAuthButton;

import com.extjs.gxt.themes.client.Slate;
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.util.ThemeManager;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.custom.ThemeSelector;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.KeyboardListenerAdapter;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.solertium.lwxml.factory.NativeDocumentFactory;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.util.extjs.client.WindowUtils;
import com.solertium.util.extjs.login.client.ChangePasswordPanel;
import com.solertium.util.extjs.login.client.NewAccountPanel;

public class LoginPanel extends LayoutContainer {

	private final boolean lockdownCreateNewAccount = SimpleSISClient.iAmOnline;
	private NewAccountPanel newAccountPanel;

	private ChangePasswordPanel changePasswordPanel;
	private TextBox userName;
	private PasswordTextBox password;
	private Button login;

	private Button newAccount;
	// private HorizontalPanel loginBanner;
	private HorizontalPanel loginContentArea;
	private VerticalPanel loginPanel, loginFields, descriptionPanel;
	private Image headerImage;

	private FlexTable loginTable;

	private GoogleAccountsAuthButton b;

	public LoginPanel() {
		setLayout(new FillLayout(Orientation.HORIZONTAL));
		// setSize(623, 564);
		setMonitorWindowResize(false);
		addListener(Events.AfterLayout, new Listener<BaseEvent>() {
			public void handleEvent(BaseEvent be) {
				userName.setFocus(true);
				userName.setCursorPos(0);
			}
		});
	}

	private void openNewAccountPanel() {
		AddUserWindow win = new AddUserWindow(true, null);
		win.show();
		win.setSize(410, 325);
		win.center();
	}

	public void update(String message) {
		removeAll();

		loginPanel = new VerticalPanel();
		loginPanel.setWidth("622px");
		loginPanel.addStyleName("SIS_loginPanel");

		/* TOP PANEL: IMAGE */
		headerImage = new Image("images/logo-iucn.gif");
		headerImage.setWidth("89px");
		headerImage.setHeight("85px");
		headerImage.addStyleName("SIS_loginHeaderImage");

		/* BOTTOM PANEL: CONTENT */
		loginContentArea = new HorizontalPanel();
		loginContentArea.addStyleName("SIS_loginContentArea");
		loginContentArea.setWidth("622px");
		loginContentArea.setHeight("563px");

		/* Left side of the content area */
		descriptionPanel = new VerticalPanel();
		descriptionPanel.addStyleName("SIS_loginDescription");
		descriptionPanel.setSpacing(0);
		descriptionPanel.setWidth("210px");
		descriptionPanel.add(headerImage);

		// Add content to description panel
		descriptionPanel
				.add(new HTML("<div style='margin: 5px; margin-top: 20px;'>"
						+ "This is the Species Information Service (SIS) Toolkit, rev. 1.5-c4.</div>"
						+ "<div style='margin: 5px; margin-top: 20px;'>"
						+ "<a href=\"/raw/downloads/sisOffline.zip\">Click here</a> to download "
						+ "a standalone version of the software.</div>"
						+ "<div style='margin: 5px; margin-top: 20px;'>"
						+ "The following browsers are highly suggested for standards compliance and performance:"
						+ "<ul><li><a target=\"_blank\" href=\"http://www.google.com/chrome\">Google Chrome</a></li>"
						+ "<li><a target=\"_blank\" href=\"http://www.firefox.com\">Firefox 3</a></li>"
						+ "<li><a target=\"_blank\" href=\"http://www.apple.com/safari/\">Apple Safari</a></li>"
						+ "</ul></div><div style='margin: 5px; margin-top: 20px; margin-bottom: 0px;'>"
						+ "Select theme:</div>"));

		if (!ThemeManager.getThemes().contains(Slate.SLATE)) {
			ThemeManager.register(Slate.SLATE);
//			ThemeManager.register(Black.BLACK);
//			ThemeManager.register(DarkGray.DARKGRAY);
//			ThemeManager.register(Olive.OLIVE);
//			ThemeManager.register(Purple.PURPLE);
//			ThemeManager.register(Slickness.SLICKNESS);
		}

		ThemeSelector ts = new ThemeSelector();
		ts.addStyleName("SIS_loginTheme");
		descriptionPanel.add(ts);
		
		if (!SimpleSISClient.iAmOnline) {
			descriptionPanel.add(new HTML("<div style='margin:5px; margin-top: 20px; margin-bottom:0px;'>" +
					"Done with offline data?</div>"));
			final Button button = new Button("Clear data");
			button.addClickListener(new ClickListener() {
			
				public void onClick(Widget sender) {
					button.setEnabled(false);
					WindowUtils.confirmAlert("Confirm Deletion of Data", "This will clear all data from the offline version including all assessments and working sets.  This may take a while if you had a lot of data.  Are you sure you want to remove data?", 
							new WindowUtils.MessageBoxListener() {
							
								@Override
								public void onYes() {
									final NativeDocument ndoc = SimpleSISClient.getHttpBasicNativeDocument();
									ndoc.delete(UriBase.getInstance().getOfflineBase() +"/offline/clear", new GenericCallback<String>() {
									
										public void onSuccess(String result) {
											WindowUtils.infoAlert("Success", "All offline data has been removed.  To add in data, please import a working set.");
											button.setEnabled(true);
										}
									
										public void onFailure(Throwable caught) {
											WindowUtils.errorAlert(ndoc.getText());
											button.setEnabled(true);
										}
									});
							
								}
							
								@Override
								public void onNo() {
									button.setEnabled(true);
							
								}
							}, "Yes, Clear Data", "Cancel");
					
			
				}
			});
			descriptionPanel.add(button);
		}
		

		/* Right side of the content area */
		loginFields = new VerticalPanel();
		loginFields.addStyleName("SIS_loginFields");
		loginFields.setSpacing(6);

		userName = new TextBox();
		if (UriBase.getInstance().isHostedMode()) {
			String defaultUser = 
				com.google.gwt.user.client.Window.Location.getParameter("u");
			if (defaultUser != null)
				userName.setText(defaultUser);
		}
		userName.addKeyboardListener(new KeyboardListenerAdapter() {
			@Override
			public void onKeyPress(Widget sender, char keyCode, int modifiers) {
				if (keyCode == KEY_ENTER)
					login.click();
			}
		});
		userName.setTabIndex(0);
		
		password = new PasswordTextBox();
		if (UriBase.getInstance().isHostedMode()) {
			String defaultPassword = 
				com.google.gwt.user.client.Window.Location.getParameter("p");
			if (defaultPassword != null)
				password.setText(defaultPassword);
		}
		password.addKeyboardListener(new KeyboardListenerAdapter() {
			@Override
			public void onKeyPress(Widget sender, char keyCode, int modifiers) {
				if (keyCode == KEY_ENTER)
					login.click();
			}
		});
		
		loginTable = new FlexTable();
		loginTable.setWidget(0, 0, new HTML("<span style=\"color: #00428C\"><b>Username</b></span>"));
		loginTable.setWidget(0, 1, userName);
		loginTable.setWidget(1, 0, new HTML("<span style=\"color: #00428C\"><b>Password</b></span>"));
		loginTable.setWidget(1, 1, password);

		login = new Button("Login", new ClickListener() {
			public void onClick(Widget sender) {
				((Button) sender).setText("Please wait...");
				((Button) sender).setEnabled(false);
				SimpleSupport.doLogin(userName.getText().toLowerCase(), password.getText());
			}
		});

		newAccount = new Button("Create New Account", new ClickListener() {
			public void onClick(Widget sender) {

				if (lockdownCreateNewAccount) {

					MessageBox box = MessageBox.prompt("Restricted", "This is a temporarily "
							+ "restricted function. Please enter the passcode to gain access.");
					box.addCallback(new Listener<MessageBoxEvent>() {
						public void handleEvent(MessageBoxEvent be) {
							if (be.getValue() != null && be.getValue().equalsIgnoreCase("s3cr3t")) {
								openNewAccountPanel();
							}
						}
					});
					box.show();
				} else
					openNewAccountPanel();
			}
		});

		// Add content to login fields
		if (message != null)
			loginFields.add(new HTML("<span style=\"color: #C01B24\"><b>" + message + "</b></span>"));
		loginFields.add(loginTable);
		loginFields.add(login);

		loginFields.add(new HTML("Not Registered?"));
		loginFields.add(newAccount);

		HTML changePassword = new HTML("Change your password");
		changePassword.addStyleName("SIS_HyperlinkLookAlike");
		changePassword.addClickListener(new ClickListener() {
			public void onClick(Widget sender) {
				changePasswordPanel = new ChangePasswordPanel() {
					@Override
					public void changePassword(String username, String oldPass, String newPass,
							final AsyncCallback<String> callback) {
						StringBuffer xml = new StringBuffer("<auth>");
						xml.append("<u>");
						xml.append(username);
						xml.append("</u>");
						xml.append("<oldP>");
						xml.append(oldPass);
						xml.append("</oldP>");
						xml.append("<newP>");
						xml.append(newPass);
						xml.append("</newP>");
						xml.append("</auth>");

						final NativeDocument doc = SimpleSISClient.getHttpBasicNativeDocument();
						doc.postAsText(UriBase.getInstance().getSISBase() + "/authn", xml.toString(), new GenericCallback<String>() {
							public void onFailure(Throwable caught) {
								callback.onFailure(caught);
							}

							public void onSuccess(String result) {
								callback.onSuccess("Success");
							}
						});
					}
				};
				changePasswordPanel.showChangePasswordWizard();
			}
		});
		loginFields.add(changePassword);

		HTML resetPassword = new HTML("Reset your password");
		resetPassword.addStyleName("SIS_HyperlinkLookAlike");
		resetPassword.addClickListener(new ClickListener() {
			public void onClick(Widget sender) {
				if( userName.getText().equals("") )
					WindowUtils.errorAlert("Please enter your username into the box.");
				else {
					final String username = userName.getText();
					WindowUtils.confirmAlert("Reset Password", "Are you sure you want to reset " 
							+ username + "'s password? A new password will be supplied via e-mail.", new Listener<MessageBoxEvent>() {
						public void handleEvent(MessageBoxEvent be) {
							if( be.getButtonClicked().getText().equalsIgnoreCase("yes") ) {
								NativeDocument ndoc = SimpleSISClient.getHttpBasicNativeDocument();
								ndoc.post(UriBase.getInstance().getSISBase() +"/authn/reset", "<root><u>" + username + "</u></root>", new GenericCallback<String>() {
									public void onSuccess(String result) {
										Info.display("Reset Success!", "A new password for {0} has been sent.", username);
									}
									public void onFailure(Throwable caught) {
										WindowUtils.errorAlert("Reset failed!", "Resetting the password for " +
												username + " failed. Please check your Internet connection and try again.");
									}
								});
							}
						};
					});
				}
			}
		});
		loginFields.add(resetPassword);
		
		b = new GoogleAccountsAuthButton("SIS", GWT.getHostPageBaseURL() + "index.html", new GenericCallback<String>() {
			public void onFailure(Throwable caught) {
			}

			public void onSuccess(final String result) {
				SimpleSISClient.SimpleSupport.doLogin(result.toString(), "");
			}
		});

		// loginFields.add(new HTML(" or ")); // disable Google Accounts for
		// offline stick
		// loginFields.add(b);

		// Adding a quick wrapper to center the login fields panel
		SimplePanel wrap = new SimplePanel();
		wrap.addStyleName("SIS_CenteredPage");

		VerticalPanel moreWrap = new VerticalPanel();

		if (!SimpleSISClient.iAmOnline) {
			final VerticalPanel updateWrapper = new VerticalPanel();
			updateWrapper.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
			updateWrapper.addStyleName("dashed-border");
			updateWrapper.setWidth("270px");
			final HTML updateSummary = new HTML("Checking update status...");
			final NativeDocument updateDoc = NativeDocumentFactory.newNativeDocument();
			updateDoc.getAsText(UriBase.getInstance().getSISBase() + "/update/summary", new GenericCallback<String>() {
				public void onFailure(Throwable caught) {
					updateSummary.setHTML("<span style=\"color: gray;\"><b>No updates available.</b></span>");
				}

				public void onSuccess(String result) {
					String message = updateDoc.getText();
					updateSummary.setHTML("<span style=\"color: #gray;\"><b>" + message + "</b></span>");
					Button doUpdate = new Button("Process Updates", new ClickListener() {
						public void onClick(Widget sender) {
							WindowUtils.showLoadingAlert("Processing updates ... please wait.");
							final NativeDocument processUpdatesDoc = NativeDocumentFactory.newNativeDocument();
							processUpdatesDoc.getAsText(UriBase.getInstance().getSISBase() + "/update", new GenericCallback<String>() {
								public void onFailure(Throwable caught) {
									WindowUtils.hideLoadingAlert();
									Window w = WindowUtils.getWindow(false, false, "Update Message");
									w.add(new Html(processUpdatesDoc.getText()));
									w.show();
								}

								public void onSuccess(String result) {
									WindowUtils.hideLoadingAlert();
									Window w = WindowUtils.getWindow(false, false, "Update Message");
									w.add(new Html(processUpdatesDoc.getText()));
									w.setModal(true);
									w.setClosable(false);
									w.show();
								}
							});
						}
					});
					updateWrapper.add(doUpdate);
				}
			});
			updateWrapper.add(updateSummary);

			moreWrap.add(updateWrapper);
		}

		moreWrap.add(loginFields);
		wrap.setWidget(moreWrap);

		// Add content to content area
		loginContentArea.add(descriptionPanel);
		loginContentArea.add(wrap);

		add(loginContentArea);
	}
}
