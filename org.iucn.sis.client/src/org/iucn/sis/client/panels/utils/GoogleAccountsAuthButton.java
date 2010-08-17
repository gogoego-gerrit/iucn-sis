package org.iucn.sis.client.panels.utils;

import org.iucn.sis.client.api.utils.UriBase;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.solertium.lwxml.factory.NativeDocumentFactory;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.lwxml.shared.NativeDocument;

/**
 * Will communicate with the AppEngineAuthenticateRestlet to facilitate
 * redirection of this page to the appropriate Google Accounts login. After
 * validation has occurred, it will ensure the ticket originated from this
 * service, then invoke the custom callback if the validation succeeded.
 * 
 * @see com.solertium.baserestlets.AppEngineAuthenticateRestlet
 * @author adam.schwartz
 * 
 */
public class GoogleAccountsAuthButton extends VerticalPanel {
	private static native void redirectToUrl(String url) /*-{
		$wnd.location = url;
	}-*/;
	private GenericCallback<String> loginSuccessCallback;
	private String appName;

	private String appURL;
	private Button loginButton;
	private Button validateButton;
	private String ticket;

	private String initialUser;

	private boolean appURLContainsArguments;

	/**
	 * DEPRECATED - DO NOT USE THIS CONSTRUCTOR
	 */
	public GoogleAccountsAuthButton() {
		super();
	}

	/**
	 * 
	 * @param applicationName
	 *            - name of the app this panel is plugged into
	 * @param appURL
	 *            - FULL url of the app's main page. This will have arguments
	 *            tacked onto it, so...
	 * @param loginSuccessCallback
	 *            - if login succeeds, the onSuccess(...) function will be
	 *            invoked, passing the username as the argument. onFailure(...)
	 *            never gets invoked. TODO - @param appURLHasArguments -
	 *            specifies if the appURL ALREADY CONTAINS arguments in the
	 *            query string. if false, arguments will be appended with the
	 *            format ?(name)=(value); if true, arguments will be appended as
	 *            format &(name)=(value)
	 */
	public GoogleAccountsAuthButton(String applicationName, String appURL, GenericCallback<String> loginSuccessCallback) {
		super();

		this.appName = applicationName;
		this.appURL = appURL;
		this.appURLContainsArguments = false;
		this.loginSuccessCallback = loginSuccessCallback;
		this.ticket = parseVariableFromURL("t");
		this.initialUser = parseVariableFromURL("usr");

		setSpacing(4);

		if (ticket != null && !ticket.equals("") && initialUser != null && !initialUser.equals(""))
			generateValidateButton(initialUser);
		else
			generateUnloggedInButton();
	}

	private void doGoogleLogin() {
		final NativeDocument doc = NativeDocumentFactory.newNativeDocument();
		String loginURL = "/googleLogin?reentry=" + appURL;
		doc.get(loginURL, new GenericCallback<String>() {
			public void onFailure(Throwable caught) {
				Window.alert("Google Account login is currently experiencing technical difficulties.");
			}

			public void onSuccess(String result) {
				redirectToUrl(doc.getText());
			}
		});
	}

	private void doGoogleValidate() {
		final NativeDocument doc = NativeDocumentFactory.newNativeDocument();
		String loginURL = "/googleLogin?t=" + ticket + "&usr=" + initialUser;
		doc.post(loginURL, "<empty></empty>", new GenericCallback<String>() {
			public void onFailure(Throwable caught) {
				Window.alert("You are attempting to reuse a stale Google Accounts login "
						+ "ticket. Click OK to acquire a new ticket to log back in.");

				doGoogleLogin();
			}

			public void onSuccess(String result) {
				loginSuccessCallback.onSuccess(initialUser);
			}
		});
	}

	private void generateUnloggedInButton() {
		loginButton = new Button("Login using a Google Account");
		loginButton.addClickListener(new ClickListener() {
			public void onClick(Widget sender) {
				doGoogleLogin();
				loginButton.setEnabled(false);
			}
		});

		add(loginButton);
	}

	private void generateValidateButton(String username) {
		validateButton = new Button("Login to " + appName + ", " + username);
		validateButton.addClickListener(new ClickListener() {
			public void onClick(Widget sender) {
				doGoogleValidate();
				validateButton.setEnabled(false);
			}
		});

		final HTML logoutURL = new HTML();

		final NativeDocument ndoc = NativeDocumentFactory.newNativeDocument();
		ndoc.getAsText(UriBase.getInstance().getSISBase() + "/googleLogin/logout?reentry=" + appURL, new GenericCallback<String>() {
			public void onFailure(Throwable arg0) {
			}

			public void onSuccess(String arg0) {
				logoutURL.setHTML("Not you? <a href=\"" + ndoc.getText() + "\">Sign Out</a>");
			}
		});

		add(logoutURL);
		add(validateButton);
	}

	private native String parseVariableFromURL(String name) /*-{
	  name = name.replace(/[\[]/,"\\\[").replace(/[\]]/,"\\\]");
	  var regexS = "[\\?&]"+name+"=([^&#]*)";
	  var regex = new RegExp( regexS );
	  var results = regex.exec( $wnd.location.href );
	  if( results == null )
	    return "";
	  else
	    return results[1];
	}-*/;
}
