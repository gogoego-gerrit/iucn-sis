/*
 * Copyright (C) 2007-2008 Solertium Corporation
 *
 * This file is part of the open source GoGoEgo project.
 *
 * GoGoEgo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *  
 * GoGoEgo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GoGoEgo.  If not, see http://www.gnu.org/licenses/.
 * 
 * Unless you have been granted a different license in writing by the
 * copyright holders for GoGoEgo, only the GNU General Public License
 * grants you rights to modify or redistribute this code.
 */
package com.solertium.util.extjs.login.client;

import java.util.ArrayList;

import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.WindowEvent;
import com.extjs.gxt.ui.client.event.WindowListener;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.form.Validator;
import com.google.gwt.user.client.rpc.AsyncCallback;

import ext.ux.pwd.client.PasswordField;
import ext.ux.wizard.client.WizardCard;
import ext.ux.wizard.client.WizardWindow;

public abstract class ChangePasswordPanel {

	public static final String EMAIL_KEY = "email";
	public static final String PASSWORD_KEY = "password";
	public static final String FIRSTNAME_KEY = "firstname";
	public static final String LASTNAME_KEY = "lastname";
	public static final String AFFILIATION_KEY = "affiliation";

	protected WizardWindow wizwin;

	protected boolean normalizeUsernameToLowercase = true;

	public ChangePasswordPanel() {
	}

	/**
	 * Function that should process the password change.
	 * 
	 * @param username
	 *            /e-mail address
	 * @param oldPass
	 *            - used for validation
	 * @param newPass
	 *            - password to change to
	 * @param callback
	 *            - what to do after password is changed (onSuccess) or if the
	 *            process failed
	 */
	public abstract void changePassword(String username, String oldPass, String newPass, AsyncCallback<String> callback);

	public boolean isNormalizeUsernameToLowercase() {
		return normalizeUsernameToLowercase;
	}

	public void setNormalizeUsernameToLowercase(boolean normalizeUsernameToLowercase) {
		this.normalizeUsernameToLowercase = normalizeUsernameToLowercase;
	}

	public void showChangePasswordWizard() {
		// setup an array of WizardCards
		ArrayList<WizardCard> cards = new ArrayList<WizardCard>();

		// 1st card - a welcome
		WizardCard wc = new WizardCard("Welcome");
		wc.setHtmlText("Welcome to the change password wizard.<br/><br/>"
				+ "Please click the \"next\"-button to begin.");
		cards.add(wc);

		// 2nd card - a simple form
		wc = new WizardCard("Account Information");
		wc.setHtmlText("Please enter your e-mail address and current password "
				+ "for verification. Then enter your new password twice for " + "verification.");
		FormPanel formpanel = new FormPanel();

		final TextField<String> emailFld = new TextField<String>();
		emailFld.setFieldLabel("Email");
		emailFld.setEmptyText("Your e-mail address");
		emailFld.setSelectOnFocus(true);
		emailFld.setAllowBlank(false);

		emailFld.setValidator(new Validator() {
			public String validate(Field<?> field, String value) {
				if (!value.toUpperCase().matches("[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,4}"))
					return "This field must be a valid email address";

				return null;
			}
		});

		formpanel.add(emailFld);

		final TextField<String> curPassFld = new TextField<String>();
		curPassFld.setFieldLabel("Current Password");
		curPassFld.setAllowBlank(true);
		curPassFld.setPassword(true);

		formpanel.add(curPassFld);

		final PasswordField newPassFld = new PasswordField();
		newPassFld.setFieldLabel("New Password");
		newPassFld.setAllowBlank(false);
		newPassFld.setMinScore(10);

		formpanel.add(newPassFld);

		final TextField<String> newPassConfirm = new TextField<String>();
		newPassConfirm.setFieldLabel("New Password - Confirm");
		newPassConfirm.setAllowBlank(false);
		newPassConfirm.setPassword(true);
		newPassConfirm.setValidator(new Validator() {
			public String validate(Field<?> field, String value) {
				if (!value.equals(newPassFld.getValue()))
					return "The two password fields must contain the exact same values.";
				else if (value.equals(curPassFld.getValue()))
					return "Your new password must be different from the old password.";
				else
					return null;
			}
		});

		formpanel.add(newPassConfirm);

		wc.setFormPanel(formpanel);
		wc.addFinishListener(new Listener<BaseEvent>() {
			public void handleEvent(final BaseEvent be) {
				changePassword(normalizeUsernameToLowercase ? emailFld.getValue().toLowerCase() : emailFld.getValue(),
						curPassFld.getValue(), newPassFld.getValue(), new AsyncCallback<String>() {
							public void onFailure(Throwable caught) {
								MessageBox.alert("Error!", "There was an error processing "
										+ "your request. You most likely did not supply "
										+ "a currently valid e-mail and password combination.", new Listener<MessageBoxEvent>() {
									public void handleEvent(MessageBoxEvent be) {
										wizwin.show();
									}
								});
							}

							public void onSuccess(String result) {
								MessageBox.alert("Success!", "Your password was successfully " + "changed.", null);
							}
						});
			}
		});

		cards.add(wc);

		wizwin = new WizardWindow(cards);
		wizwin.setHeading("Change Password");
		wizwin.setHeaderTitle("Change Password Wizard");

		wizwin.show();
	}
}
