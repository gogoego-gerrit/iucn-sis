package org.iucn.sis.client.panels;

import java.util.Date;

import org.iucn.sis.client.api.caches.OfflineCache;
import org.iucn.sis.client.api.caches.WorkingSetCache;
import org.iucn.sis.client.api.container.SISClientBase;
import org.iucn.sis.client.api.utils.FormattedDate;
import org.iucn.sis.client.api.utils.UriBase;
import org.iucn.sis.shared.api.debug.Debug;
import org.iucn.sis.shared.api.models.OfflineMetadata;
import org.iucn.sis.shared.api.models.WorkingSet;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.ButtonEvent;
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
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.util.extjs.client.WindowUtils;

public class OfflineFooter extends ToolBar {
	
	private static final LabelToolItem connectionStatus = new LabelToolItem();
	
	private OfflineMetadata metadata;
	
	public OfflineFooter() {
		
		metadata = OfflineCache.impl.get();

		ToolTipConfig tooltip = new ToolTipConfig();
		tooltip.setTitle("Details");
		tooltip.setText("Name: " + metadata.getName() + "<br/>" +
				"Location: " + metadata.getLocation() + "<br/>" +
				"Last Modified: " + FormattedDate.FULL.getDate(metadata.getLastModified()));
		tooltip.setCloseable(true);
		
		IconButton details = new IconButton("icon-information");
		details.setToolTip(tooltip);
		
		Button gear = new Button("Open SIS Offline Manager", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				com.google.gwt.user.client.Window.Location.assign(
					UriBase.getInstance().getOfflineBase() + "/manager"
				);
			}
		});
		gear.addStyleName("icon-gear");
		
		add(new LabelToolItem("Offline Database: "+metadata.getName()));
		add(details);
		add(new SeparatorToolItem());
		add(new LabelToolItem("Offline Working Set: "));
		add(new LabelToolItem(WorkingSetCache.impl.getOfflineWorkingSet().getName()));
		
		add(new FillToolItem());
		add(new LabelToolItem("Connection Status: "));
		add(connectionStatus);
		add(new SeparatorToolItem());
		add(gear);
	
		setConnectionStatus(initConnectionStatus());
	}

	private native String initConnectionStatus() /*-{
		$wnd.addEventListener('online', function(e) {
		  @org.iucn.sis.client.panels.OfflineFooter::setConnectionStatus(Ljava/lang/String;)("Online");
		}, false);
		
		$wnd.addEventListener('offline', function(e) {
		  @org.iucn.sis.client.panels.OfflineFooter::setConnectionStatus(Ljava/lang/String;)("Offline");
		}, false);			
	
	  	return $wnd.navigator.onLine ? "Online" : "Offline";
	}-*/;
	
	public static void setConnectionStatus(String value) {
		connectionStatus.setLabel(value);
	}
	
	private native void checkConnection()/*-{

		  if($wnd.navigator.onLine){
			$wnd.alert("Connected!");
		  } else {
			$wnd.alert("Connection Error!");
		  }		

	}-*/;
	
	@SuppressWarnings("unused")
	private Menu getMenu() {
		Menu menu = new Menu();
		
		//FIXME: re-instate once ready
		/*MenuItem syncItem = new MenuItem("Sync Online", new SelectionListener<MenuEvent>() {
			public void componentSelected(MenuEvent ce) {				
				WindowUtils.confirmAlert("Confirm", "Are your sure you want to Sync data to Online?", new WindowUtils.SimpleMessageBoxListener() {					
					@Override
					public void onYes() {
						fireImport();						
					}
				});				
			}
		});
		menu.add(syncItem);*/
		
		MenuItem backupItem = new MenuItem("Backup Offline", new SelectionListener<MenuEvent>() {
			public void componentSelected(MenuEvent ce) {				
				fireBackup();
			}
		});
		menu.add(backupItem);
		
		MenuItem checkItem = new MenuItem("Check Connection", new SelectionListener<MenuEvent>() {
			public void componentSelected(MenuEvent ce) {				
				checkConnection();
			}
		});
		menu.add(checkItem);
		
		return menu;
	}
	
	public void fireImport() {
		WorkingSet workingSet = WorkingSetCache.impl.getOfflineWorkingSet();
		
		final String url = UriBase.getInstance().getOfflineBase() + "/offline/importToLive/"
			+ SISClientBase.currentUser.getUsername() + "/" + workingSet.getId() + 
			"?uid=" + new Date().getTime();
		
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
		
	}
	
	public void fireBackup() {
		final NativeDocument ndoc = SISClientBase.getHttpBasicNativeDocument();
		ndoc.get(UriBase.getInstance().getOfflineBase() + "/offline/backupOffline", new GenericCallback<String>() {
			public void onSuccess(String arg0) {				
				WindowUtils.infoAlert(ndoc.getText());
				
			}
			@Override
			public void onFailure(Throwable caught) {
				Debug.println("Failed to load Offline Metadata.");	
			}
		});
		
	}	
}
