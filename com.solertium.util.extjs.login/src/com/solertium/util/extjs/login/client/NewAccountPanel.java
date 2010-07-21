package com.solertium.util.extjs.login.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.WindowEvent;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.form.Validator;
import com.extjs.gxt.ui.client.widget.tips.ToolTipConfig;
import com.google.gwt.user.client.rpc.AsyncCallback;

import ext.ux.pwd.client.PasswordField;
import ext.ux.wizard.client.WizardCard;
import ext.ux.wizard.client.WizardWindow;

public abstract class NewAccountPanel {

	public static final String EMAIL_KEY = "email";
	public static final String PASSWORD_KEY = "password";
	public static final String FIRSTNAME_KEY = "firstname";
	public static final String LASTNAME_KEY = "lastname";
	public static final String AFFILIATION_KEY = "affiliation";

	protected HashMap<String, String> formFields;
	protected HashMap<String, TextField<String>> userFields;

	protected PasswordField passFld;
	protected TextField<String> firstnameFld;
	protected TextField<String> lastnameFld;
	protected TextField<String> affiliationFld;
	protected TextField<String> emailFld;
	protected CheckBox checkBox;

	protected WizardWindow wizwin;

	protected boolean allowGoogleAccounts = true;
	protected boolean enforceEmailUsername = true;
	protected boolean normalizeUsernameToLowercase = true;
	protected boolean requireConfirm = false;

	public NewAccountPanel() {
		formFields = new HashMap<String, String>();
		userFields = new HashMap<String, TextField<String>>();
	}

	public void addFormField(String label, String tagName, String emptyTextDescription, boolean allowedBlank) {
		TextField<String> temp = new TextField<String>();
		temp.setFieldLabel(label);
		temp.setEmptyText(emptyTextDescription);
		temp.setAllowBlank(allowedBlank);
		temp.setSelectOnFocus(true);

		userFields.put(tagName, temp);
	}

	public void beginWizard() {
		enterAccountInfoWizard(true);
	}

	/**
	 * Implement me to provide confirmation behavior.
	 * 
	 * @param email
	 *            - address to confirm
	 * @param confirmationCode
	 *            - the code
	 * @param callback
	 *            &lt;String&gt; - String supplied to the callback should be null for
	 *            success (proceed to confirmation) or a failure message in
	 *            String format
	 */
	public abstract void confirm(String email, String confirmationCode, AsyncCallback<String> callback);

	public void enterAccountInfoWizard(final boolean showDefaultNext) {
		// setup an array of WizardCards
		ArrayList<WizardCard> cards = new ArrayList<WizardCard>();

		// 1st card - a welcome
		WizardCard wc = new WizardCard("Welcome");
		wc.setHtmlText("Welcome to the create new account wizard.<br/><br/>"
				+ "Please click the \"next\"-button to begin.");
		cards.add(wc);

		// 2nd card - a simple form
		wc = new WizardCard("Account Information");
		wc.setHtmlText("Please enter a valid e-mail address. You will use this address "
				+ "to login. You cannot leave these fields empty. If you enter a "
				+ "Gmail address, this application can authenticate via your Google "
				+ "account. Check the \"Use Google Accounts\" box to enable this feature.");
		FormPanel formpanel = new FormPanel();

		emailFld = new TextField<String>();
		emailFld.setFieldLabel("Email");
		emailFld.setEmptyText("Your e-mail address");
		emailFld.setSelectOnFocus(true);
		emailFld.setAllowBlank(false);

		if (enforceEmailUsername)
			emailFld.setValidator(new Validator() {
				public String validate(Field<?> field, String value) {
					if (!value.toUpperCase().matches("[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,4}"))
						return "This field must be a valid email address";

					return null;
				}
			});

		formpanel.add(emailFld);

		passFld = new PasswordField();
		passFld.setFieldLabel("Password");
		passFld.setAllowBlank(false);
		passFld.setMinScore(10);

		if (allowGoogleAccounts) {
			checkBox = new CheckBox();
			checkBox.setFieldLabel("Use Google Accounts");
			checkBox.setToolTip(new ToolTipConfig("Use Google Accounts", "Selecting "
					+ "this option will enable authentication through Google Accounts, "
					+ "instead of supplying a separate password for this application. "
					+ "This address MUST be a valid Google address."));
			checkBox.addListener(Events.Change, new Listener<BaseEvent>() {
				public void handleEvent(BaseEvent be) {
					passFld.setEnabled(!checkBox.getValue().booleanValue());
				}
			});

			formpanel.add(checkBox);
		}

		formpanel.add(passFld);

		wc.setFormPanel(formpanel);
		wc.addFinishListener(new Listener<BaseEvent>() {
			public void handleEvent(final BaseEvent be) {
				saveAccountInfo(normalizeUsernameToLowercase ? emailFld.getValue().toLowerCase() : emailFld.getValue(),
						passFld.getValue(), new AsyncCallback<String>() {
							public void onFailure(Throwable caught) {
								MessageBox.alert("Error!", caught.getStackTrace().toString(), null);
							}

							public void onSuccess(String result) {
								if (result != null) {
									MessageBox.alert("Error!", "An account is already allocated "
											+ "to this e-mail address. Please try again.", 
											new Listener<MessageBoxEvent>() {
												public void handleEvent(MessageBoxEvent be) {
													wizwin.show();
												}
									});
								} else if (requireConfirm && showDefaultNext) {
									showConfirmation(emailFld.getValue(), true);
									// enterProfileInfoWizard(true);
								} else if (showDefaultNext) {
									// showConfirmation(emailFld.getValue(),
									// true);
									enterProfileInfoWizard(true);
								}
							}
						});
			}
		});

		cards.add(wc);

		wizwin = new WizardWindow(cards);
		wizwin.setHeading("Create New Account");
		wizwin.setHeaderTitle("New Account Creation Wizard");

		wizwin.show();
	}

	public void enterProfileInfoWizard(final boolean showDefaultNext) {
		// setup an array of WizardCards
		ArrayList<WizardCard> cards = new ArrayList<WizardCard>();

		WizardCard wc = new WizardCard("Profile Information");
		wc.setHtmlText("Please enter your first- and your lastname, and a group "
				+ "affiliation. You cannot leave these fields empty.");
		FormPanel formpanel = new FormPanel();

		firstnameFld = new TextField<String>();
		firstnameFld.setFieldLabel("Firstname");
		firstnameFld.setEmptyText("Your first name");
		firstnameFld.setAllowBlank(false);
		firstnameFld.setSelectOnFocus(true);
		formpanel.add(firstnameFld);

		lastnameFld = new TextField<String>();
		lastnameFld.setFieldLabel("Lastname");
		lastnameFld.setEmptyText("Your last name");
		lastnameFld.setAllowBlank(false);
		lastnameFld.setSelectOnFocus(true);
		formpanel.add(lastnameFld);

		affiliationFld = new TextField<String>();
		affiliationFld.setFieldLabel("Group Affiliation");
		affiliationFld.setEmptyText("Your group affiliation");
		affiliationFld.setAllowBlank(false);
		affiliationFld.setSelectOnFocus(true);
		formpanel.add(affiliationFld);

		wc.setFormPanel(formpanel);

		for (Entry<String, TextField<String>> curEntry : userFields.entrySet())
			formpanel.add(curEntry.getValue());

		wc.addFinishListener(new Listener<BaseEvent>() {
			public void handleEvent(BaseEvent be) {
				formFields.put(EMAIL_KEY, normalizeUsernameToLowercase ? emailFld.getValue().toLowerCase() : emailFld
						.getValue());
				formFields.put(PASSWORD_KEY, passFld.getValue());
				formFields.put(FIRSTNAME_KEY, firstnameFld.getValue());
				formFields.put(LASTNAME_KEY, lastnameFld.getValue());
				formFields.put(AFFILIATION_KEY, affiliationFld.getValue());

				for (Entry<String, TextField<String>> curEntry : userFields.entrySet())
					formFields.put(curEntry.getKey(), curEntry.getValue().getRawValue());

				saveProfileInfo(formFields, new AsyncCallback<String>() {
					public void onFailure(Throwable caught) {
						MessageBox.alert("Error Saving", caught.getMessage(), null);
					}

					public void onSuccess(String result) {
						if (result == null) {
							if (showDefaultNext)
								showFinished(emailFld.getValue(), showDefaultNext);
						} else {
							MessageBox.alert("Error Saving", result, null);
						}
					};
				});
			}
		});

		cards.add(wc);

		wizwin = new WizardWindow(cards);
		wizwin.setHeading("Create New Account");
		wizwin.setHeaderTitle("New Account Creation Wizard");

		wizwin.show();
	}

	public boolean isAllowGoogleAccounts() {
		return allowGoogleAccounts;
	}

	public boolean isEnforceEmailUsername() {
		return enforceEmailUsername;
	}

	public boolean isNormalizeUsernameToLowercase() {
		return normalizeUsernameToLowercase;
	}

	/**
	 * This function is invoked when all account data has been correctly entered
	 * and is ready for saving/server processing.
	 * 
	 * @param email
	 *            - Email address for new account
	 * @param password
	 *            - Password for new account
	 * @param callback
	 *            <String> - String supplied to the callback should be null for
	 *            success (proceed to confirmation) or a failure message in
	 *            String format
	 */
	public abstract void saveAccountInfo(String email, String password, AsyncCallback<String> callback);

	/**
	 * This function is invoked when all account data has been correctly entered
	 * and is ready for saving/server processing.
	 * 
	 * @param parameters
	 *            - Map of values pulled from the Form
	 * @param callback
	 *            <String> - String supplied to the callback should be null for
	 *            success (proceed to confirmation) or a failure message in
	 *            String format
	 */
	public abstract void saveProfileInfo(Map<String, String> parameters, AsyncCallback<String> callback);

	public void setAllowGoogleAccounts(boolean allowGoogleAccounts) {
		this.allowGoogleAccounts = allowGoogleAccounts;
	}

	public void setEnforceEmailUsername(boolean enforceEmailUsername) {
		this.enforceEmailUsername = enforceEmailUsername;
	}

	public void setNormalizeUsernameToLowercase(boolean normalizeUsernameToLowercase) {
		this.normalizeUsernameToLowercase = normalizeUsernameToLowercase;
	}

	public void setRequireConfirm(boolean requireConfirm) {
		this.requireConfirm = requireConfirm;
	}
	
	public void showConfirmation(final String defaultAddress, final boolean showDefaultNext) {
		ArrayList<WizardCard> cards = new ArrayList<WizardCard>();

		// confirmation
		WizardCard wc = new WizardCard("Confirmation Code");
		wc.setHtmlText("Please enter the confirmation code sent to your e-mail "
				+ "address specified on the previous page.");

		FormPanel formpanel = new FormPanel();

		final TextField<String> confCodeFld = new TextField<String>();
		confCodeFld.setFieldLabel("Confirmation Code");
		confCodeFld.setEmptyText("Confirmation code from your e-mail");
		confCodeFld.setSelectOnFocus(true);
		confCodeFld.setAllowBlank(false);
		formpanel.add(confCodeFld);

		wc.setFormPanel(formpanel);
		wc.addFinishListener(new Listener<BaseEvent>() {
			public void handleEvent(final BaseEvent be) {
				confirm(normalizeUsernameToLowercase ? defaultAddress.toLowerCase() : defaultAddress, confCodeFld
						.getValue(), new AsyncCallback<String>() {
					public void onFailure(Throwable caught) {
						be.setCancelled(true);
						MessageBox.alert("Error", "This account could not be confirmed.", null);
					}

					public void onSuccess(String result) {

						if (showDefaultNext)
							enterProfileInfoWizard(true);
					}
				});
			}
		});
		cards.add(wc);

		wizwin = new WizardWindow(cards);
		wizwin.setHeading("Confirm Account Creation");
		wizwin.setHeaderTitle("Confirm Your New Account Creation");

		wizwin.show();
	}

	public void showFinished(final String defaultAddress, final boolean showDefaultNext) {
		ArrayList<WizardCard> cards = new ArrayList<WizardCard>();

		WizardCard wc = new WizardCard("Finished!");
		wc.setHtmlText("Thank you for signing up, " + defaultAddress + "! You may now log in.<br/><br/>");
		cards.add(wc);

		wizwin = new WizardWindow(cards);
		wizwin.setHeading("Create New Account");
		wizwin.setHeaderTitle("New Account Creation Wizard");

		wizwin.show();
	}

}
