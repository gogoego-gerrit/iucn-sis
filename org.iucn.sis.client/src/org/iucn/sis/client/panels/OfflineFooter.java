package org.iucn.sis.client.panels;

import java.util.Date;

import org.iucn.sis.client.api.caches.WorkingSetCache;
import org.iucn.sis.client.api.container.SISClientBase;
import org.iucn.sis.client.api.utils.FormattedDate;
import org.iucn.sis.client.api.utils.SIS;
import org.iucn.sis.client.api.utils.UriBase;
import org.iucn.sis.shared.api.models.WorkingSet;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.IconButtonEvent;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
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
		
		if(WorkingSetCache.impl.getCurrentWorkingSet() != null)
			add(new LabelToolItem(WorkingSetCache.impl.getCurrentWorkingSet().getName()));
		
		add(new FillToolItem());	
		add(new LabelToolItem((SIS.isOffline()) ? " OFFLINE " : " ONLINE "));
		add(gear);
		
	}
	
	private Menu getMenu() {
		Menu menu = new Menu();
		
		MenuItem item = new MenuItem("Sync Online", new SelectionListener<MenuEvent>() {
			public void componentSelected(MenuEvent ce) {
				fireImport();
			}
		}); 
		menu.add(item);
		
		return menu;
	}
	
	public void fireImport() {
		
		WorkingSet workingSet = WorkingSetCache.impl.getCurrentWorkingSet();
		
		final String url = UriBase.getInstance().getOfflineBase() + "/offline/importToLive/"
			+ SISClientBase.currentUser.getUsername() + "/" + workingSet.getId();
		
		final ContentPanel content = new ContentPanel();
		content.setUrl(url);
		
		final Window exportWindow = WindowUtils.newWindow("Import to Live - " + workingSet.getName() + "...");
		exportWindow.setScrollMode(Scroll.AUTO);
		exportWindow.setSize(500, 400);
		exportWindow.addButton(new Button("Close", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				exportWindow.hide();
			}
		}));
		exportWindow.setUrl(url);
		exportWindow.show();
		
		WindowUtils.infoAlert("Live Import Started");
		
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
