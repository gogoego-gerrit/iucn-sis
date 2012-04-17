package org.iucn.sis.client.api.ui.users.panels;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.iucn.sis.client.api.caches.AuthorizationCache;
import org.iucn.sis.client.api.ui.users.panels.UserSearchController.SearchResults;
import org.iucn.sis.client.api.utils.BasicWindow;
import org.iucn.sis.shared.api.acl.feature.AuthorizableFeature;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.KeyListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.HtmlContainer;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.google.gwt.event.dom.client.KeyCodes;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.util.events.ComplexListener;
import com.solertium.util.events.SimpleListener;
import com.solertium.util.extjs.client.FormBuilder;
import com.solertium.util.extjs.client.WindowUtils;

public class AddProfileWindow extends BasicWindow {
	
	private final ComplexListener<String> successListener;
	
	private final TextField<String> firstName, lastName;
	private final FormPanel form;
	
	public AddProfileWindow(ComplexListener<String> listener) {
		super("Basic Information");
		setSize(410, 325);
		
		this.successListener = listener;
		
		form = new FormPanel();
		form.setHeaderVisible(false);
		form.setLayout(new FormLayout());
		form.setBorders(false);
		form.setBodyBorder(false);
		
		firstName = FormBuilder.createTextField(UserSearchController.SEARCH_KEY_FIRST_NAME, null, "First Name", false);
		lastName = FormBuilder.createTextField(UserSearchController.SEARCH_KEY_LAST_NAME, null, "Last Name", false);
		
		KeyListener boxListener = new KeyListener() {
			public void componentKeyPress(ComponentEvent event) {
				int keyCode = event.getKeyCode();
				if (keyCode == KeyCodes.KEY_ENTER)
					submit();
			}
		};
		firstName.addKeyListener(boxListener);
		lastName.addKeyListener(boxListener);
		
		add(new HtmlContainer("Enter the basic profile information here.  A database " +
			"search will then be performed to check for possible duplicates."));
		
		form.add(firstName);
		form.add(lastName);
		
		add(form);
		
		addButton(new Button("Submit", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				submit();
			}
		}));
		addButton(new Button("Cancel", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				hide();
			}
		}));
	}
	
	private void submit() {
		doServerValidation(new SimpleListener() {
			public void handleEvent() {
				hide();
				
				StringBuilder usernameBuilder = new StringBuilder();
				if (hasValue(firstName.getValue())) {
					usernameBuilder.append(firstName.getValue().toLowerCase());
					usernameBuilder.append('.');
				}
				if (hasValue(lastName.getValue())) {
					usernameBuilder.append(lastName.getValue().toLowerCase());
					usernameBuilder.append('.');
				}
				usernameBuilder.append("gen.");
				usernameBuilder.append(new Date().getTime());
				usernameBuilder.append("@gen.iucnsis.org");
				
				AddUserWindow window = 
					new AddUserWindow(false, usernameBuilder.toString(), new ComplexListener<String>() {
						public void handleEvent(String result) {
							successListener.handleEvent(result);
						}
					});
				window.setFirstname(firstName.getValue());
				window.setLastname(lastName.getValue());
				window.show();
			}
		});
	}
	
	private void doServerValidation(final SimpleListener listener) {
		Map<String, String> params = new HashMap<String, String>();
		if (hasValue(firstName.getValue()))
			params.put(UserSearchController.SEARCH_KEY_FIRST_NAME, firstName.getValue());
		if (hasValue(lastName.getValue()))
			params.put(UserSearchController.SEARCH_KEY_LAST_NAME, lastName.getValue());
		
		if (params.isEmpty()) {
			WindowUtils.errorAlert("Both fields can not be blank. " +
				"Please enter either a first name, last name, or both.");
		}
		else {
			Map<String, List<String>> newParams = new HashMap<String, List<String>>();
			for (Entry<String, String> entry : params.entrySet()) {
				ArrayList<String> list = new ArrayList<String>();
				list.add(entry.getValue());
				newParams.put(entry.getKey(), list);
			}
			
			UserSearchController.search(newParams, "and", new GenericCallback<List<SearchResults>>() {
				public void onSuccess(List<SearchResults> results) {
					if (results.isEmpty())
						listener.handleEvent();
					else {
						openConfirmationWindow(results, listener);
					}
				}
				public void onFailure(Throwable caught) {
					WindowUtils.errorAlert("Could not perform search verification, please try again later.");
				}
			});
		}
	}
	
	private void openConfirmationWindow(Collection<SearchResults> results, final SimpleListener listener) {
		
		final List<ColumnConfig> config = new ArrayList<ColumnConfig>();
		config.add(new ColumnConfig("firstname", "First Name", 150));
		config.add(new ColumnConfig("lastname", "Last Name", 150));
		config.add(new ColumnConfig("affiliation", "Affiliation", 150));
		
		final ListStore<SearchResults> store = new ListStore<SearchResults>();
		for (SearchResults result : results)
			store.add(result);
		
		final Grid<SearchResults> grid = new Grid<SearchResults>(store, new ColumnModel(config));
		grid.setAutoExpandColumn("firstname");
		grid.setAutoExpandMin(150);
		
		final LayoutContainer container = new LayoutContainer(new BorderLayout());
		container.add(new HtmlContainer("<div style=\"margin:5px;font-size:14px;font-weight:bold;\">" + 
			"<span style=\"color:red\">WARNING: </span><span>" + getErrorMessage(results) + "</span></div>"), 
			new BorderLayoutData(LayoutRegion.NORTH, 60, 60, 60));
		container.add(grid, new BorderLayoutData(LayoutRegion.CENTER));
		
		final Window window = WindowUtils.newWindow("Confirm Profile Creation");
		window.setSize(600, 400);
		window.setClosable(false);
		window.setLayout(new FillLayout());
		
		window.add(container);
		
		window.addButton(new Button("Add New Profile", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				AddProfileWindow.this.hide();
				window.hide();
				listener.handleEvent();
			}
		}));
		window.addButton(new Button("Cancel", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				AddProfileWindow.this.hide();
				window.hide();
			}
		}));
		
		window.show();
	}
	
	private String getErrorMessage(Collection<SearchResults> results) {
		String verb, noun;
		if (results.size() == 1) {
			verb = "is";
			noun = "user";
		}
		else {
			verb = "are";
			noun = "users";
		}
		
		return "There " + verb + " " + results.size() + " " + noun + 
			" that may match this profile, are you sure you want to continue?";
	}
	
	private boolean hasValue(String s) {
		return s != null && !"".equals(s);
	}
	
	@Override
	public void show() {
		if (AuthorizationCache.impl.canUse(AuthorizableFeature.ADD_PROFILE_FEATURE))
			open();
		else
			WindowUtils.errorAlert("You do not have permission to use this feature.");
	}

	private void open() {
		super.show();
	}
}
