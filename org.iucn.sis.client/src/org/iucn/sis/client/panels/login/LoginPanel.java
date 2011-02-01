package org.iucn.sis.client.panels.login;

import org.iucn.sis.client.api.container.SISClientBase.SimpleSupport;
import org.iucn.sis.client.api.utils.UriBase;
import org.iucn.sis.client.container.SimpleSISClient;
import org.iucn.sis.client.panels.users.AddUserWindow;
import org.iucn.sis.shared.api.debug.Debug;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.KeyListener;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.custom.ThemeSelector;
import com.extjs.gxt.ui.client.widget.form.FieldSet;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.solertium.lwxml.factory.NativeDocumentFactory;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.util.extjs.client.WindowUtils;
import com.solertium.util.extjs.login.client.ChangePasswordPanel;

public class LoginPanel extends LayoutContainer {

	private final boolean lockdownCreateNewAccount = SimpleSISClient.iAmOnline;

	private final HTML message;
	private final TextField<String> userName;
	private final TextField<String> password;

	public LoginPanel() {
		super();
		setLayout(new FitLayout());
		setLayoutOnChange(true);
		
		message = new HTML();
		message.addStyleName("SIS_loginMessage");
		
		userName = new TextField<String>();
		userName.setFieldLabel("Username");
		
		password = new TextField<String>();
		password.setFieldLabel("Password");
		password.setPassword(true);
		
		drawForm();
	}
	
	@Override
	protected void afterRender() {
		userName.focus();
	}

	private void openNewAccountPanel() {
		AddUserWindow win = new AddUserWindow(true, null);
		win.show();
		win.setSize(410, 325);
		win.center();
	}
	
	private FieldSet newFieldSet(String heading) {
		FormLayout layout = new FormLayout();
		layout.setLabelPad(2);
		
		FieldSet fieldSet = new FieldSet();
		fieldSet.setHeading("&nbsp;" + heading + "&nbsp;");
		fieldSet.setLayout(layout);
		
		return fieldSet;
	}
	
	public void drawForm() {
		/* TOP PANEL: IMAGE */
		final Image headerImage = new Image("images/logo-iucn.gif");
		headerImage.setWidth("89px");
		headerImage.setHeight("85px");
		headerImage.addStyleName("SIS_loginHeaderImage");
		
		
		String buildNumber = "2.0.0";
		try {
			buildNumber = RootPanel.get("version").getElement().getInnerText();
		} catch (Throwable e) {
			Debug.println("Error loading build number.");
		}

		/* Left side of the content area */
		// Add content to description panel
		final LayoutContainer descriptionPanel = new LayoutContainer();
		descriptionPanel.addStyleName("SIS_loginDescription");
		descriptionPanel.setWidth("210px");
		descriptionPanel.add(headerImage);
		descriptionPanel
				.add(new HTML("<div style='margin: 5px; margin-top: 20px;'>"
						+ "This is the Species Information Service (SIS) Toolkit, rev. 1.5-c4.</div>"
						+ "<div style='margin: 5px; margin-top: 20px;'>"
						+ buildNumber + "</div>"
						+ "<div style='margin: 5px; margin-top: 20px;'>"
						+ "<a href=\"/raw/downloads/sisOffline.zip\">Click here</a> to download "
						+ "a standalone version of the software.</div>"
						+ "<div style='margin: 5px; margin-top: 20px;'>"
						+ "The following browsers are highly suggested for standards compliance and performance:"
						+ "<ul><li><a target=\"_blank\" href=\"http://www.google.com/chrome\">Google Chrome</a></li>"
						+ "<li><a target=\"_blank\" href=\"http://www.firefox.com\">Firefox 3</a></li>"
						+ "<li><a target=\"_blank\" href=\"http://www.apple.com/safari/\">Apple Safari</a></li>"
						+ "</ul></div><div style='margin: 5px; margin-top: 20px; margin-bottom: 0px;'>"
						+ "Select theme:</div>"
						));

		ThemeSelector ts = new ThemeSelector();
		ts.addStyleName("SIS_loginTheme");
		
		descriptionPanel.add(ts);
		
		if (!SimpleSISClient.iAmOnline) {
			descriptionPanel.add(new HTML("<div style='margin:5px; margin-top: 20px; margin-bottom:0px;'>" +
					"Done with offline data?</div>"));
			descriptionPanel.add(new Button("Clear data", new ClickHandler() {
				public void onClick(ClickEvent event) {
					final Button button = (Button)event.getSource();
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
			}));
		}
		
		
		/*wrap.addStyleName("SIS_CenteredPage");
		wrap.setWidget(moreWrap);*/

		// Add content to content area
		/* BOTTOM PANEL: CONTENT */
		final Widget formArea = drawFormMiddle();
		
		final Grid loginContentArea = new Grid(1, 2);
		loginContentArea.setCellPadding(0);
		loginContentArea.setCellSpacing(0);
		loginContentArea.setSize("100%", "100%");
		loginContentArea.addStyleName("SIS_loginContentArea");
		
		loginContentArea.setWidget(0, 0, descriptionPanel);
		loginContentArea.setWidget(0, 1, formArea);
		
		loginContentArea.getCellFormatter().setWidth(0, 0, "210px");
		loginContentArea.getCellFormatter().setAlignment(0, 0, HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_TOP);
		loginContentArea.getCellFormatter().setAlignment(0, 1, HasHorizontalAlignment.ALIGN_CENTER, HasVerticalAlignment.ALIGN_MIDDLE);
		
		add(loginContentArea);
	}
	
	private Widget drawFormMiddle() {
		/* Center area */
		
		final com.extjs.gxt.ui.client.widget.button.Button login = 
			new com.extjs.gxt.ui.client.widget.button.Button();
		login.setText("Login");
		login.addSelectionListener(new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				if (userName.getValue() == null || "".equals(userName.getValue()))
					WindowUtils.errorAlert("Please enter user name.");
				else if (password.getValue() == null || "".equals(password.getValue()))
					WindowUtils.errorAlert("Please enter password.");
				else {
					WindowUtils.showLoadingAlert("Logging in...");
					
					SimpleSupport.doLogin(userName.getValue().toLowerCase(), password.getValue());
				}
			}
		});
		

		/* Right side of the content area */
		final FieldSet loginFields = newFieldSet("Login");
		
		final VerticalPanel formWrapper = new VerticalPanel();
		formWrapper.addStyleName("SIS_loginFields");
		formWrapper.setSpacing(6);
		formWrapper.setSize("400px", "300px");

		userName.addKeyListener(new KeyListener() {
			public void componentKeyPress(ComponentEvent event) {
				int keyCode = event.getKeyCode();
				if (keyCode == KeyCodes.KEY_ENTER)
					login.fireEvent(Events.Select);
				
			}
		});
		userName.setTabIndex(0);
		
		password.addKeyListener(new KeyListener() {
			public void componentKeyPress(ComponentEvent event) {
				int keyCode = event.getKeyCode();
				if (keyCode == KeyCodes.KEY_ENTER)
					login.fireEvent(Events.Select);
			}
		});
		
		loginFields.add(message);
		loginFields.add(userName);
		loginFields.add(password);
		loginFields.add(login);
		
		formWrapper.add(loginFields);
		
		/*final FlexTable loginTable = new FlexTable();
		loginTable.setWidget(0, 0, new HTML("<span style=\"color: #00428C\"><b>Username</b></span>"));
		loginTable.setWidget(0, 1, userName);
		loginTable.setWidget(1, 0, new HTML("<span style=\"color: #00428C\"><b>Password</b></span>"));
		loginTable.setWidget(1, 1, password);*/

		com.extjs.gxt.ui.client.widget.button.Button newAccount = 
			new com.extjs.gxt.ui.client.widget.button.Button("Create New Account");
		newAccount.addSelectionListener(new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
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
		
		
		/*loginFields.add(message);	
		loginFields.add(loginTable);
		loginFields.add(login);*/

		FieldSet register = newFieldSet("Not Registered?");
		
		//formWrapper.add(new HTML("Not Registered?"));
		register.add(newAccount);
		
		formWrapper.add(register);
		
		FieldSet needHelp = newFieldSet("Need Help?");

		HTML changePassword = new HTML("Change your password");
		changePassword.addStyleName("SIS_HyperlinkLookAlike");
		changePassword.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				final ChangePasswordPanel changePasswordPanel = new ChangePasswordPanel() {
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
		needHelp.add(changePassword);

		HTML aLittleSpace = new HTML("&nbsp;");
		aLittleSpace.setHeight("5px");
		needHelp.add(aLittleSpace);
		
		HTML resetPassword = new HTML("Reset your password");
		resetPassword.addStyleName("SIS_HyperlinkLookAlike");
		resetPassword.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				if ("".equals(userName.getValue()) || null == userName.getValue())
					WindowUtils.errorAlert("Please enter your username into the box.");
				else {
					final String username = userName.getValue();
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
		needHelp.add(resetPassword);
		
		formWrapper.add(needHelp);
		
		/*GoogleAccountsAuthButton b = new GoogleAccountsAuthButton("SIS", GWT.getHostPageBaseURL() + "index.html", new GenericCallback<String>() {
			public void onFailure(Throwable caught) {
			}

			public void onSuccess(final String result) {
				SimpleSISClient.SimpleSupport.doLogin(result.toString(), "");
			}
		});

		// loginFields.add(new HTML(" or ")); // disable Google Accounts for
		// offline stick
		// loginFields.add(b);*/

		// Adding a quick wrapper to center the login fields panel
		
		
		LayoutContainer moreWrap = new LayoutContainer();
		
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
					Button doUpdate = new Button("Process Updates", new ClickHandler() {
						public void onClick(ClickEvent event) {
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

		moreWrap.add(formWrapper);
		
		/*
		 * Adding spacer to push the login area up a 
		 * bit closer to the top.  Could play CSS 
		 * tricks with the margins, but would rather 
		 * have the layout be code-driven instead.
		 */
		HTML spacer = new HTML("&nbsp;");
		spacer.setHeight("200px");
		
		moreWrap.add(spacer);
		
		return moreWrap;
	}

	public void draw() {
		VerticalPanel loginPanel = new VerticalPanel();
		loginPanel.setWidth("622px");
		loginPanel.addStyleName("SIS_loginPanel");

		/* TOP PANEL: IMAGE */
		final Image headerImage = new Image("images/logo-iucn.gif");
		headerImage.setWidth("89px");
		headerImage.setHeight("85px");
		headerImage.addStyleName("SIS_loginHeaderImage");

		/* BOTTOM PANEL: CONTENT */
		final HorizontalPanel loginContentArea = new HorizontalPanel();
		loginContentArea.addStyleName("SIS_loginContentArea");
		loginContentArea.setWidth("622px");
		loginContentArea.setHeight("100%");

		
		
		
		String buildNumber = "2.0.0";
		try {
			buildNumber = RootPanel.get("version").getElement().getInnerText();
		} catch (Throwable e) {
			Debug.println("Error loading build number.");
		}

		/* Left side of the content area */
		// Add content to description panel
		final VerticalPanel descriptionPanel = new VerticalPanel();
		descriptionPanel.addStyleName("SIS_loginDescription");
		descriptionPanel.setSpacing(0);
		descriptionPanel.setWidth("210px");
		descriptionPanel.add(headerImage);
		descriptionPanel
				.add(new HTML("<div style='margin: 5px; margin-top: 20px;'>"
						+ "This is the Species Information Service (SIS) Toolkit, rev. 1.5-c4.</div>"
						+ "<div style='margin: 5px; margin-top: 20px;'>"
						+ buildNumber + "</div>"
						+ "<div style='margin: 5px; margin-top: 20px;'>"
						+ "<a href=\"/raw/downloads/sisOffline.zip\">Click here</a> to download "
						+ "a standalone version of the software.</div>"
						+ "<div style='margin: 5px; margin-top: 20px;'>"
						+ "The following browsers are highly suggested for standards compliance and performance:"
						+ "<ul><li><a target=\"_blank\" href=\"http://www.google.com/chrome\">Google Chrome</a></li>"
						+ "<li><a target=\"_blank\" href=\"http://www.firefox.com\">Firefox 3</a></li>"
						+ "<li><a target=\"_blank\" href=\"http://www.apple.com/safari/\">Apple Safari</a></li>"
						+ "</ul></div><div style='margin: 5px; margin-top: 20px; margin-bottom: 0px;'>"
						+ "Select theme:</div>"
						));

		ThemeSelector ts = new ThemeSelector();
		ts.addStyleName("SIS_loginTheme");
		
		descriptionPanel.add(ts);
		
		if (!SimpleSISClient.iAmOnline) {
			descriptionPanel.add(new HTML("<div style='margin:5px; margin-top: 20px; margin-bottom:0px;'>" +
					"Done with offline data?</div>"));
			descriptionPanel.add(new Button("Clear data", new ClickHandler() {
				public void onClick(ClickEvent event) {
					final Button button = (Button)event.getSource();
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
			}));
		}
		
		/* Center area */
		
		final Button login = new Button("Login", new ClickHandler() {
			public void onClick(ClickEvent event) {
				WindowUtils.showLoadingAlert("Logging in...");
				
				SimpleSupport.doLogin(userName.getValue().toLowerCase(), password.getValue());
			}
		});
		

		/* Right side of the content area */
		final VerticalPanel loginFields = new VerticalPanel();
		loginFields.addStyleName("SIS_loginFields");
		loginFields.setSpacing(6);

		userName.addKeyListener(new KeyListener() {
			public void componentKeyPress(ComponentEvent event) {
				int keyCode = event.getKeyCode();
				if (keyCode == KeyCodes.KEY_ENTER)
					login.click();
			}
		});
		userName.setTabIndex(0);
		
		password.addKeyListener(new KeyListener() {
			public void componentKeyPress(ComponentEvent event) {
				int keyCode = event.getKeyCode();
				if (keyCode == KeyCodes.KEY_ENTER)
					login.click();
			}
		});
		
		final FlexTable loginTable = new FlexTable();
		loginTable.setWidget(0, 0, new HTML("<span style=\"color: #00428C\"><b>Username</b></span>"));
		loginTable.setWidget(0, 1, userName);
		loginTable.setWidget(1, 0, new HTML("<span style=\"color: #00428C\"><b>Password</b></span>"));
		loginTable.setWidget(1, 1, password);

		Button newAccount = new Button("Create New Account", new ClickHandler() {
			public void onClick(ClickEvent event) {
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
		
		loginFields.add(message);
		loginFields.add(loginTable);
		loginFields.add(login);

		loginFields.add(new HTML("Not Registered?"));
		loginFields.add(newAccount);

		HTML changePassword = new HTML("Change your password");
		changePassword.addStyleName("SIS_HyperlinkLookAlike");
		changePassword.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				final ChangePasswordPanel changePasswordPanel = new ChangePasswordPanel() {
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
		resetPassword.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				if ("".equals(userName.getValue()))
					WindowUtils.errorAlert("Please enter your username into the box.");
				else {
					final String username = userName.getValue();
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
		
		/*GoogleAccountsAuthButton b = new GoogleAccountsAuthButton("SIS", GWT.getHostPageBaseURL() + "index.html", new GenericCallback<String>() {
			public void onFailure(Throwable caught) {
			}

			public void onSuccess(final String result) {
				SimpleSISClient.SimpleSupport.doLogin(result.toString(), "");
			}
		});

		// loginFields.add(new HTML(" or ")); // disable Google Accounts for
		// offline stick
		// loginFields.add(b);*/

		// Adding a quick wrapper to center the login fields panel
		
		
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
					Button doUpdate = new Button("Process Updates", new ClickHandler() {
						public void onClick(ClickEvent event) {
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
		
		SimplePanel wrap = new SimplePanel();
		wrap.addStyleName("SIS_CenteredPage");
		wrap.setWidget(moreWrap);

		// Add content to content area
		loginContentArea.add(descriptionPanel);
		loginContentArea.add(wrap);

		add(loginContentArea);
	}
	
	public void setMessage(String messageText) {
		message.setVisible(messageText != null);
		message.setText(messageText);
	}
	
	public void clearCredentials() {
		clearPassword(false);
		
		userName.setValue("");
		
		if (UriBase.getInstance().isHostedMode()) {
			String defaultUser = 
				com.google.gwt.user.client.Window.Location.getParameter("u");
			if (defaultUser != null)
				userName.setValue(defaultUser);
		}
		
		userName.focus();
	}
	
	public void clearPassword(boolean focus) {
		password.setValue("");
		
		if (UriBase.getInstance().isHostedMode()) {
			String defaultPassword = 
				com.google.gwt.user.client.Window.Location.getParameter("p");
			if (defaultPassword != null)
				password.setValue(defaultPassword);
		}
		
		if (focus)
			password.focus();
	}
}
