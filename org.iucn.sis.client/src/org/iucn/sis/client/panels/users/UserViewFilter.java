package org.iucn.sis.client.panels.users;

import org.iucn.sis.client.api.models.ClientUser;
import org.iucn.sis.client.api.ui.models.users.UserModelData;
import org.iucn.sis.shared.api.debug.Debug;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.KeyListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.store.StoreFilter;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.google.gwt.event.dom.client.KeyCodes;
import com.solertium.util.extjs.client.WindowUtils;

public class UserViewFilter implements StoreFilter<UserModelData> {
	
	private final TextField<String> usernameFilter;
	private final TextField<String> firstFilter;
	private final TextField<String> lastFilter;
	private final TextField<String> nicknameFilter;
	private final TextField<String> affiliationFilter;
	private final SimpleComboBox<String> activeAccountFilter;
	
	private FormPanel filterPanel;
	private Window filterPopup;
	
	private FilterHandler handler;
	
	public UserViewFilter() {
		usernameFilter = new TextField<String>();
		usernameFilter.setFieldLabel("Username");
		firstFilter = new TextField<String>();
		firstFilter.setFieldLabel("First Name");
		lastFilter = new TextField<String>();
		lastFilter.setFieldLabel("Last Name");
		nicknameFilter = new TextField<String>();
		nicknameFilter.setFieldLabel("Nickname");
		affiliationFilter = new TextField<String>();
		affiliationFilter.setFieldLabel("Affiliation");
		activeAccountFilter = new SimpleComboBox<String>();
		activeAccountFilter.setFieldLabel("Active Account");
		activeAccountFilter.add("");
		activeAccountFilter.add("true");
		activeAccountFilter.add("false");
		activeAccountFilter.findModel("").set("text", "Active");
		activeAccountFilter.findModel("true").set("text", "Active");
		activeAccountFilter.findModel("false").set("text", "Disabled");
		activeAccountFilter.setEditable(false);
	
		filterPanel = new FormPanel();
		filterPanel.setBodyBorder(false);
		filterPanel.setBorders(false);
		filterPanel.setHeaderVisible(false);
		filterPanel.add(usernameFilter);
		filterPanel.add(firstFilter);
		filterPanel.add(lastFilter);
		filterPanel.add(nicknameFilter);
		filterPanel.add(affiliationFilter);
		filterPanel.add(activeAccountFilter);
		final Button applyFilters = new Button("Apply Filters", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				filterPopup.hide();
				
				if (handler != null)
					handler.filter();
				/*loader.applyFilter("");
				pagingBar.setActivePage(1);
				loader.getPagingLoader().load();*/
			}
		});
		KeyListener enter = new KeyListener() {
			public void componentKeyPress(ComponentEvent event) {
				if (event.getKeyCode() == KeyCodes.KEY_ENTER)
					applyFilters.fireEvent(Events.Select);
			}
		};
		usernameFilter.addKeyListener(enter);
		firstFilter.addKeyListener(enter);
		lastFilter.addKeyListener(enter);
		nicknameFilter.addKeyListener(enter);
		affiliationFilter.addKeyListener(enter);

		filterPanel.addButton(applyFilters);
		filterPanel.addButton(new Button("Clear Filters", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				filterPopup.hide();
				usernameFilter.setValue("");
				firstFilter.setValue("");
				lastFilter.setValue("");
				nicknameFilter.setValue("");
				affiliationFilter.setValue("");
				activeAccountFilter.setValue(null);
				
				if (handler != null)
					handler.clear();
				/*loader.applyFilter("");
				pagingBar.setActivePage(1);
				loader.getPagingLoader().load();*/
			}
		}));
	}
	
	public void setFilterHandler(FilterHandler handler) {
		this.handler = handler;
	}
	
	@Override
	public boolean select(Store<UserModelData> store, UserModelData parent,
			UserModelData item, String property) {
		String active = (activeAccountFilter.getValue() == null || 
			"".equals(activeAccountFilter.getValue().getValue())) ? 
			null : activeAccountFilter.getValue().getValue();
		
		boolean result = matches(active, (String) item.get(ClientUser.SIS_USER))
			&& matches(usernameFilter.getValue(), (String) item.get(ClientUser.USERNAME))
			&& matches(firstFilter.getValue(), (String) item.get(ClientUser.FIRST_NAME))
			&& matches(lastFilter.getValue(), (String) item.get(ClientUser.LAST_NAME))
			&& matches(nicknameFilter.getValue(), (String) item.get(ClientUser.NICKNAME))
			&& matches(affiliationFilter.getValue(), (String) item.get(ClientUser.AFFILIATION));

		Debug.println("Result for {0} is {1}", item.get(ClientUser.USERNAME), result);
		
		return result;
	}
	
	
	
	private boolean matches(String filter, String value) {
		String filterText = filter == null || "".equals(filter) ? null : filter.toLowerCase();
		
		if (filterText == null)
			return true; //match everything
		else
			return filterText.startsWith(value.toLowerCase());
	}
	
	public void showFilter() {
		filterPopup = WindowUtils.newWindow("Set Filters");
		filterPopup.setLayout(new FillLayout());
		filterPopup.setStyleName("navigator");
		filterPopup.setSize(380, 260);
		filterPopup.add(filterPanel);
		filterPopup.show();
	}
	
	public static interface FilterHandler {
		
		public void filter();
		
		public void clear();
		
	}

}
