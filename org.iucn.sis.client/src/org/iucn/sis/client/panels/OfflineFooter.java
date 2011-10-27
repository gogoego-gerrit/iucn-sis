package org.iucn.sis.client.panels;

import java.util.Date;

import org.iucn.sis.client.api.utils.FormattedDate;

import com.extjs.gxt.ui.client.event.IconButtonEvent;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.button.IconButton;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.extjs.gxt.ui.client.widget.tips.ToolTipConfig;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.LabelToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.solertium.util.extjs.client.WindowUtils;

public class OfflineFooter extends ToolBar {
	
	private final OfflineMetadata metadata;
	
	public OfflineFooter() {
		/*
		 * TODO: suggest creating an OfflineCache that hits a resource on the server 
		 * on startup and yields metadata such as the database name, file location, 
		 * modification date, etc.  This can be displayed here...
		 * 
		 * Use the caching policies in SimpleSISClient to add it to the set of 
		 * cached data to be fetched prior to SIS loading.
		 * 
		 * FIXME: for now, making up the metadata...
		 */
		
		metadata = new OfflineMetadata();
		metadata.setName("Example Offline Database");
		metadata.setLocation("C:\\var\\sis\\databases\\sis.db");
		metadata.setLastModified(new Date());
		
		ToolTipConfig tooltip = new ToolTipConfig();
		tooltip.setTitle("Details");
		tooltip.setText("Name: " + metadata.getName() + "<br/>" +
				"Location: " + metadata.getLocation() + "<br/>" +
				"Last Modified: " + FormattedDate.FULL.getDate(metadata.getLastModified()));
		tooltip.setCloseable(true);
		
		IconButton details = new IconButton("icon-information");
		details.setToolTip(tooltip);
		
		//FIXME: need an icon for offline specifically
		IconButton gear = new IconButton("icon-gear");
		gear.addSelectionListener(new SelectionListener<IconButtonEvent>() {
			public void componentSelected(IconButtonEvent ce) {
				getMenu().show(ce.getIconButton());
			}
		});
		
		add(new LabelToolItem(metadata.getName()));
		add(details);
		add(new FillToolItem());
		add(gear);
		
		/*
		 * TODO: maybe add an indicator icon that shows whether a user is 
		 * online or offline?
		 */
	}
	
	private Menu getMenu() {
		Menu menu = new Menu();
		
		MenuItem item = new MenuItem("Sync Online", new SelectionListener<MenuEvent>() {
			public void componentSelected(MenuEvent ce) {
				WindowUtils.infoAlert("TODO: stuff...");
			}
		});
		menu.add(item);
		
		return menu;
	}


	public static class OfflineMetadata {
		
		private String name, location;
		private Date lastModified;
		
		public void setLastModified(Date lastModified) {
			this.lastModified = lastModified;
		}
		
		public void setLocation(String location) {
			this.location = location;
		}
		
		public void setName(String name) {
			this.name = name;
		}
		
		public Date getLastModified() {
			return lastModified;
		}
		
		public String getLocation() {
			return location;
		}
		
		public String getName() {
			return name;
		}
		
	}
	
}
