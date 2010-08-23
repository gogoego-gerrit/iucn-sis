/**
 *
 */
package org.iucn.sis.client.userui;

import org.iucn.sis.client.acl.AuthorizationCache;
import org.iucn.sis.client.simple.SimpleSISClient;
import org.iucn.sis.shared.acl.base.AuthorizableObject;
import org.iucn.sis.shared.acl.feature.AuthorizableFeature;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.TabPanelEvent;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;

/**
 * UserModelTabPanel.java
 * 
 * Tab Panel wrapping the user model. This also implements ContentManager so its
 * tabs can update the manager when data becomes state. Each tab knows its stale
 * state and each TabItem implements HasRefreshableContent, so when data becomes
 * state, the next time it's loaded, the ContentManager will call refresh when
 * the user requests to see data.
 * 
 * @author carl.scott <carl.scott@solertium.com>
 * 
 */
public class UserModelTabPanel extends TabPanel implements ContentManager {

	/**
	 * HasRefreshableContent
	 * 
	 * Denotes that this object has content that can be refreshed by calling the
	 * appropritae function.
	 * 
	 * @author carl.scott <carl.scott@solertium.com>
	 * 
	 */
	public static interface HasRefreshableContent {

		/**
		 * Refresh the data and/or layout of a page, as appropriate.
		 * 
		 */
		public void refresh();

	}

	public static final String CONSTANTS_ATTACHMENT_POINT = "/manageusers";

	public UserModelTabPanel() {
		super();
		setAutoSelect(false);
		setTabPosition(TabPosition.BOTTOM);

		final TabItem users = new TabItem("Users");
		users.setData("id", "users");
		users.setLayout(new FillLayout());
		users.setClosable(false);
		users.setScrollMode(Scroll.AUTO);
		users.add(new UserViewPanel(this));
		users.setData("stale", "true");

		add(users);

		if( AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, 
				AuthorizableObject.USE_FEATURE, AuthorizableFeature.USER_MANAGEMENT_FEATURE)) {
			final TabItem fields = new TabItem("Custom Fields");
			fields.setData("id", "fields");
			fields.setLayout(new FillLayout());
			fields.setClosable(false);
			fields.setScrollMode(Scroll.AUTO);
			fields.add(new CustomFieldViewPanel(this));
			fields.setData("stale", "true");
			
			add(fields);
		}

		addListener(Events.Select, new Listener<TabPanelEvent>() {
			public void handleEvent(TabPanelEvent be) {
				if ("true".equals(be.getItem().getData("stale"))) {
					((HasRefreshableContent) be.getItem().getWidget(0)).refresh();
					be.getItem().setData("stale", "false");
				}
			}
		});

		/*WindowUtils.showLoadingAlert("Initializing...");*/
		setSelection(users);
	}

	/**
	 * Set some data as stale, forcing the affected tab to be refreshed next
	 * time it's accessed
	 * 
	 * @param contentID
	 *            the content ID
	 */
	public void setStale(String contentID) {
		for (int i = 0; i < getItemCount(); i++) {
			if (contentID.equals(getItem(i).getData("id")))
				getItem(i).setData("stale", "true");
		}
	}
}
