package org.iucn.sis.client.api.ui.users.panels;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.iucn.sis.client.api.caches.AuthorizationCache;
import org.iucn.sis.client.api.container.SISClientBase;
import org.iucn.sis.client.api.ui.users.panels.UserSearchController.SearchResults;
import org.iucn.sis.shared.api.acl.base.AuthorizableObject;
import org.iucn.sis.shared.api.acl.feature.AuthorizableFeature;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.HtmlContainer;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.util.events.ComplexListener;
import com.solertium.util.events.SimpleListener;
import com.solertium.util.extjs.client.FormBuilder;
import com.solertium.util.extjs.client.WindowUtils;

public class AddProfileWindow extends Window {
	
	private final ComplexListener<String> successListener;
	
	private final TextField<String> firstName, lastName;
	private final FormPanel form;
	
	public AddProfileWindow(ComplexListener<String> listener) {
		super();
		this.successListener = listener;
		
		setHeading("Basic Information");
		setSize(410, 325);
		setButtonAlign(HorizontalAlignment.CENTER);
		
		form = new FormPanel();
		form.setHeaderVisible(false);
		form.setLayout(new FormLayout());
		form.setBorders(false);
		form.setBodyBorder(false);
		
		firstName = FormBuilder.createTextField(UserSearchController.SEARCH_KEY_FIRST_NAME, null, "First Name", true);
		lastName = FormBuilder.createTextField(UserSearchController.SEARCH_KEY_LAST_NAME, null, "Last Name", true);
		
		add(new HtmlContainer("Enter the basic profile information here.  A database " +
			"search will then be performed to check for possible duplicates."));
		
		form.add(firstName);
		form.add(lastName);
		
		add(form);
		
		addButton(new Button("Submit", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				doServerValidation(new SimpleListener() {
					public void handleEvent() {
						hide();
						
						String initialUsername = firstName.getValue().toLowerCase() + 
							"." + lastName.getValue().toLowerCase() + ".gen." + 
							new Date().getTime() + "@gen.iucnsis.org";
						
						AddUserWindow window = 
							new AddUserWindow(false, initialUsername, new GenericCallback<String>() {
								public void onSuccess(String result) {
									successListener.handleEvent(result);
								}
								public void onFailure(Throwable caught) {
									// TODO Auto-generated method stub
									
								}
							});
						window.setFirstname(firstName.getValue());
						window.setLastname(lastName.getValue());
						window.show();
					}
				});
			}
		}));
		addButton(new Button("Cancel", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				hide();
			}
		}));
	}
	
	private void doServerValidation(final SimpleListener listener) {
		Map<String, String> params = new HashMap<String, String>();
		params.put(UserSearchController.SEARCH_KEY_FIRST_NAME, firstName.getValue());
		params.put(UserSearchController.SEARCH_KEY_LAST_NAME, lastName.getValue());
		
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
					String verb, noun;
					if (results.size() == 1) {
						verb = "is";
						noun = "user";
					}
					else {
						verb = "are";
						noun = "users";
					}
					
					StringBuilder builder = new StringBuilder();
					builder.append("There " + verb + " " + results.size() + " " + noun + 
						" that may match this profile, are you sure you want to continue?");
					for (Iterator<SearchResults> iter = results.listIterator(); iter.hasNext(); ) {
						SearchResults result = iter.next();
						String affiliation = "", email = "";
						if (!"".equals(result.getUser().getAffiliation()))
							affiliation = " with " + result.getUser().getAffiliation();
						if (!"".equals(result.getUser().getEmail()) && !result.getUser().getEmail().equals(result.getUser().getUsername()))
							email = " (" + result.getUser().getEmail() + ")";
						
						builder.append("<br/> - " + result.getUser().getUsername() + email + affiliation);
					}
					
					WindowUtils.confirmAlert("Confirm", builder.toString(), new WindowUtils.MessageBoxListener() {
						public void onNo() {
							hide();
						}
						public void onYes() {
							listener.handleEvent();
						}
					});
				}
			}
			public void onFailure(Throwable caught) {
				WindowUtils.errorAlert("Could not perform search verification, please try again later.");
			}
		});
	}
	
	@Override
	public void show() {
		if (AuthorizationCache.impl.hasRight(SISClientBase.currentUser, AuthorizableObject.USE_FEATURE, AuthorizableFeature.ADD_PROFILE_FEATURE))
			open();
		else
			WindowUtils.errorAlert("You do not have permission to use this feature.");
	}

	private void open() {
		super.show();
	}
}
