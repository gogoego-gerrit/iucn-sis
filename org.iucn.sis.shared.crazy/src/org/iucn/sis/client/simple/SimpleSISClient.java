package org.iucn.sis.client.simple;

import org.iucn.sis.client.acl.AuthorizationCache;
import org.iucn.sis.client.components.ClientUIContainer;
import org.iucn.sis.client.data.assessments.AssessmentCache;
import org.iucn.sis.shared.acl.User;
import org.iucn.sis.shared.acl.UserPreferences;
import org.iucn.sis.shared.data.DefinitionCache;
import org.iucn.sis.shared.data.FieldWidgetCache;
import org.iucn.sis.shared.data.MarkedCache;
import org.iucn.sis.shared.data.ProfileUtils;
import org.iucn.sis.shared.data.StatusCache;
import org.iucn.sis.shared.data.TaxonomyCache;
import org.iucn.sis.shared.data.ViewCache;
import org.iucn.sis.shared.data.WorkingSetCache;
import org.iucn.sis.shared.data.assessments.RegionCache;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.RootPanel;
import com.solertium.lwxml.factory.NativeDocumentFactory;
import com.solertium.lwxml.gwt.debug.DebuggingApplication;
import com.solertium.lwxml.gwt.debug.SysDebugger;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.NativeNodeList;
import com.solertium.util.extjs.client.WindowUtils;

public class SimpleSISClient implements EntryPoint, DebuggingApplication {
	public static class SimpleSupport {
		public static void doLogin(String user, final String password) {
			final String username = user;

			if (username != null && password != null && !username.equals("") && !password.equals("")) {
				StringBuffer xml = new StringBuffer("<auth>");
				xml.append("<u>");
				xml.append(username);
				xml.append("</u>");
				xml.append("<p>");
				xml.append(password);
				xml.append("</p>");
				xml.append("</auth>");

//				RequestBuilder b = new RequestBuilder(RequestBuilder.POST, "/authn/origin");
//				b.setRequestData(xml.toString());
//				b.setUser(username);
//				b.setPassword(password);
//				b.setCallback(new RequestCallback() {
//					public void onResponseReceived(Request request, Response response) {
//						getProfile(password, username, response.getText());
//					}
//					public void onError(Request request, Throwable exception) {
//						String ret = exception.getMessage();
//
//						if (ret != null && !ret.equals(""))
//							clientContainer.buildLogin(ret);
//						else
//							clientContainer.buildLogin("Invalid login credentials.");
//					}
//				});
//				try {
//					b.send();
//				} catch (RequestException e) {
//					e.printStackTrace();
//				}
				
				final NativeDocument auth = NativeDocumentFactory.newNativeDocument();
				auth.postAsText("/authn/origin", xml.toString(), new GenericCallback<String>() {
					public void onFailure(Throwable caught) {
						String ret = auth.getText();

						if (ret != null && !ret.equals(""))
							clientContainer.buildLogin(ret);
						else
							clientContainer.buildLogin("Invalid login credentials.");
					}

					public void onSuccess(String result) {
						getProfile(password, username, auth.getText());
					}
				});

			} else
				clientContainer.buildLogin("Invalid login credentials.");
		}

		public static void doLogout() {
			currentUser = null;
			clientContainer.buildLogin("Log out successful.");

			// TODO: FLUSH APPROPRIATE CACHES
			AssessmentCache.impl.doLogout();
			ViewCache.impl.doLogout();
			FieldWidgetCache.impl.doLogout();
			TaxonomyCache.impl.doLogout();
			WorkingSetCache.impl.doLogout();
			MarkedCache.impl.onLogout();
			StatusCache.impl.clearCache();
			AuthorizationCache.impl.clear();
		}

		private static void getProfile(final String password, final String username, final String authn) {
			final NativeDocument doc = NativeDocumentFactory.newNativeDocument();
			// doc.setUser(username);
			// doc.setPass(password);
			doc.setHeader("Authorization", "Basic " + authn);
			doc.get("/profile/" + username, new GenericCallback<String>() {
				public void onFailure(Throwable caught) {
					if( "403".equals(caught.getMessage()) )
						clientContainer.buildLogin("Login " + username + " not active.");
					else
						clientContainer.buildLogin("Invalid credentials.");
				}

				public void onSuccess(String result) {
					NativeNodeList sis = doc.getDocumentElement().getElementsByTagName("sis");
					if( sis.getLength() == 0 || sis.item(0).getTextContent().equalsIgnoreCase("true") ) {
						currentUser = ProfileUtils.buildUserFromProfile(doc, username);
						currentUser.setProperty("authn", authn);
						currentUser.setPassword(password);
							
						if (username.equalsIgnoreCase("admin"))
							currentUser.setProperty(UserPreferences.AUTO_SAVE, UserPreferences.DO_ACTION);

						AuthorizationCache.impl.setCredentials(authn);
						AuthorizationCache.impl.init(new GenericCallback<String>() {
							public void onSuccess(String result) {
								try {
								if( !iAmOnline )
									currentUser.setProperty("quickGroup", "offline");
								AuthorizationCache.impl.addUser(currentUser);

								System.out.println("Pre-postlogin call.");
								clientContainer.buildPostLogin(currentUser.getFirstName(), currentUser.getLastName(), currentUser
										.getBusinessUnit());
								System.out.println("Post-postlogin call.");
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
							public void onFailure(Throwable caught) {
								WindowUtils.errorAlert("Error Initializing Oracle", caught.getMessage());

								clientContainer.buildPostLogin(currentUser.getFirstName(), currentUser.getLastName(), currentUser
										.getBusinessUnit());
							}
						});

						DefinitionCache.impl.getDefinables();
						RegionCache.impl.fetchRegions(SimpleSISClient.getHttpBasicNativeDocument());
					} else
						clientContainer.buildLogin("Login " + username + " not active.");
				}
			});
		}
	}

	public static SimpleSISClient instance;
	
	public static User currentUser;
	public static ClientUIContainer clientContainer;
	public static boolean iAmOnline = true;

	public static NativeDocument getHttpBasicNativeDocument() {
		if (currentUser != null)
			return currentUser.getHttpBasicNativeDocument();

		return NativeDocumentFactory.newNativeDocument();
	}

	public static SimpleSISClient getInstance() {
		return instance;
	}

	public int getLogLevel() {
		return SysDebugger.OFF;
	}

	public void onModuleLoad() {
		instance = this;

		clientContainer = new ClientUIContainer();

		RootPanel.get().add(clientContainer);

		SysDebugger.autoSetup(this);
		SysDebugger.getInstance().addNamedInstance("info", new SysDebugger(SysDebugger.SEVERE + 1));
		// SysDebugger.getInstance().addNamedInstance("all", instance)
	}
}
