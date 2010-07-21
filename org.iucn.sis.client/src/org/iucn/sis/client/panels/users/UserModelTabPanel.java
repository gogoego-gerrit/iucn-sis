package org.iucn.sis.client.panels.users;

import org.iucn.sis.client.api.ui.users.panels.ContentManager;
import org.iucn.sis.client.api.ui.users.panels.HasRefreshableContent;

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
