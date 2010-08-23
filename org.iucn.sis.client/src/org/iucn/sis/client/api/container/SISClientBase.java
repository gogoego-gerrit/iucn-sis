package org.iucn.sis.client.api.container;

import org.iucn.sis.client.api.caches.AssessmentCache;
import org.iucn.sis.client.api.caches.AuthorizationCache;
import org.iucn.sis.client.api.caches.DefinitionCache;
import org.iucn.sis.client.api.caches.FieldWidgetCache;
import org.iucn.sis.client.api.caches.MarkedCache;
import org.iucn.sis.client.api.caches.RegionCache;
import org.iucn.sis.client.api.caches.StatusCache;
import org.iucn.sis.client.api.caches.TaxonomyCache;
import org.iucn.sis.client.api.caches.WorkingSetCache;
import org.iucn.sis.client.api.models.ClientUser;
import org.iucn.sis.client.api.utils.UriBase;
import org.iucn.sis.shared.api.acl.UserPreferences;
import org.iucn.sis.shared.api.citations.Referenceable;

import com.google.gwt.core.client.EntryPoint;
import com.solertium.lwxml.factory.NativeDocumentFactory;
import com.solertium.lwxml.gwt.debug.DebuggingApplication;
import com.solertium.lwxml.gwt.debug.SysDebugger;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.NativeNodeList;
import com.solertium.util.extjs.client.WindowUtils;

public abstract class SISClientBase implements EntryPoint, DebuggingApplication {
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
				
				final NativeDocument auth = NativeDocumentFactory.newNativeDocument();
				auth.postAsText(UriBase.getInstance().getSISBase() + "/authn/origin", xml.toString(), new GenericCallback<String>() {
					public void onFailure(Throwable caught) {
						String ret = auth.getText();

						if (ret != null && !ret.equals(""))
							instance.buildLogin(ret);
						else
							instance.buildLogin("Invalid login credentials.");
					}

					public void onSuccess(String result) {
						getProfile(password, username, auth.getText());
					}
				});

			} else
				instance.buildLogin("Invalid login credentials.");
		}

		public static void doLogout() {
			currentUser = null;
			instance.buildLogin("Log out successful.");

			// TODO: FLUSH APPROPRIATE CACHES
			AssessmentCache.impl.doLogout();
			FieldWidgetCache.impl.doLogout();
			TaxonomyCache.impl.doLogout();
			WorkingSetCache.impl.doLogout();
			MarkedCache.impl.onLogout();
			StatusCache.impl.clearCache();
			AuthorizationCache.impl.clear();
		}

		private static void getProfile(final String password, final String username, final String authn) {
			final NativeDocument doc = NativeDocumentFactory.newNativeDocument();
			doc.setHeader("Authorization", "Basic " + authn);
			doc.get(UriBase.getInstance().getSISBase() + "/profile/" + username, new GenericCallback<String>() {
				public void onFailure(Throwable caught) {
					if( "403".equals(caught.getMessage()) )
						instance.buildLogin("Login " + username + " not active.");
					else
						instance.buildLogin("Invalid credentials.");
				}

				public void onSuccess(String result) {
					NativeNodeList sis = doc.getDocumentElement().getElementsByTagName("sis");
					if( sis.getLength() == 0 || sis.item(0).getTextContent().equalsIgnoreCase("true") ) {
//						currentUser = ProfileUtils.buildUserFromProfile(doc, username);
						currentUser = ClientUser.fromXML(doc.getDocumentElement());
						currentUser.auth = authn;
						currentUser.password = password;
							
						if (username.equalsIgnoreCase("admin"))
							currentUser.setProperty(UserPreferences.AUTO_SAVE, UserPreferences.DO_ACTION);

					AuthorizationCache.impl.setCredentials(authn);
						AuthorizationCache.impl.init(new GenericCallback<String>() {
							public void onSuccess(String result) {
								try {
								if( !iAmOnline )
									currentUser.setProperty("quickGroup", "offline");
								AuthorizationCache.impl.addUser(currentUser);

								instance.buildPostLogin();
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
							public void onFailure(Throwable caught) {
								WindowUtils.errorAlert("Error Initializing Oracle", caught.getMessage());

								instance.buildPostLogin();
							}
						});

						DefinitionCache.impl.getDefinables();
						RegionCache.impl.fetchRegions(SISClientBase.getHttpBasicNativeDocument());
					} else
						instance.buildLogin("Login " + username + " not active.");
				}
			});
		}
	}

	public static SISClientBase instance;
	
	public static ClientUser currentUser;
	
	public static boolean iAmOnline = true;

	public static NativeDocument getHttpBasicNativeDocument() {
		if (currentUser != null)
			return currentUser.getHttpBasicNativeDocument();

		return NativeDocumentFactory.newNativeDocument();
	}

	public static SISClientBase getInstance() {
		return instance;
	}

	public int getLogLevel() {
		return SysDebugger.OFF;
	}

	public abstract void onModuleLoad();
	
	public abstract void buildLogin(String message);
	
	public abstract void buildPostLogin();
	
	/**
	 * This method is invoked whenever a user elects to logout of the system.
	 */
	public abstract void onLogout();
	
	/**
	 * This method is invoked when the current assessment gets changed in the 
	 * AssessmentCache, so the client can update panels as necessary.
	 */
	public abstract void onAssessmentChanged();
	
	/**
	 * This method is invoked when the current working set gets changed in the 
	 * WorkingSetCache, so the client can update panels as necessary.
	 */
	public abstract void onWorkingSetChanged();
	
	/**
	 * This method is invoked when the current taxon gets changed in the 
	 * TaxonomyCache, so the client can update panels as necessary.
	 */
	public abstract void onTaxonChanged();
	
	/**
	 * This method is invoked when some class in the shared packages needs the
	 * client to display the reference editor, with the supplied hooks.
	 */
	public abstract void onShowReferenceEditor(String title, Referenceable referenceable, 
			GenericCallback<Object> onAddCallback, GenericCallback<Object> onRemoveCallback);
}
