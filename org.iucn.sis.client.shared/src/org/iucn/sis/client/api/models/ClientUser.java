package org.iucn.sis.client.api.models;

import org.iucn.sis.client.api.caches.AuthorizationCache;
import org.iucn.sis.client.api.container.SISClientBase;
import org.iucn.sis.client.api.utils.UriBase;
import org.iucn.sis.shared.api.debug.Debug;
import org.iucn.sis.shared.api.models.User;
import org.iucn.sis.shared.api.models.UserPreference;

import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.NativeElement;
import com.solertium.lwxml.shared.NativeNodeList;
import com.solertium.lwxml.shared.utils.RowData;

public class ClientUser extends User {
	
	public static final String ID = "id";
	public static final String USERNAME = "username";
	public static final String EMAIL = "email";
	public static final String STATE = "state";
	public static final String FIRST_NAME = "firstName";
	public static final String LAST_NAME = "lastName";
	public static final String NICKNAME = "nickname";
	public static final String INITIALS = "initials";
	public static final String AFFILIATION = "affiliation";
	public static final String SIS_USER = "sisUser";
	public static final String RAPIDLIST_USER = "rapidListUser";
	public static final String QUICK_GROUP = "quickGroup";
	
	private static final long serialVersionUID = 1L;
	
	public String password;
	public RowData properties;
	
	public ClientUser() {
		super();
		properties = new RowData();
	}
	
	public void setProperty(String key, String value) {
		properties.addField(key, value);
	}

	public String getProperty(String prop) {
		return properties.getField(prop);
	}
	
	public void setPreference(String name, String value) {
		setProperty(name, value);
		
		UserPreference preference = null;
		for (UserPreference current : getPreferences()) {
			if (name.equals(current.getName())) {
				preference = current;
				break;
			}
		}
		
		if (preference == null) {
			preference = new UserPreference(name, value);
			getPreferences().add(preference);
		}
		else {
			if (value == null) {
				if (preference.getValue() == null)
					return;
			}
			else if (value.equals(preference.getValue()))
				return;
			
			preference.setValue(value);
		}
		
		setPreference(preference);
	}
	
	public void setPreference(final UserPreference preference) {
		final StringBuilder out = new StringBuilder();
		out.append("<root>");
		out.append(preference.toXML());
		out.append("</root>");
		
		final NativeDocument document = SISClientBase.getHttpBasicNativeDocument();
		document.post(UriBase.getInstance().getUserBase() + "/users/" + getUsername() + "/preferences", out.toString(), new GenericCallback<String>() {
			public void onSuccess(String result) {
				final NativeNodeList nodes = document.getDocumentElement().getElementsByTagName(UserPreference.ROOT_TAG);
				for (int i = 0; i < nodes.getLength(); i++) {
					UserPreference updated = UserPreference.fromXML(nodes.elementAt(i));
					if (preference.getName().equals(updated.getName()))
						preference.setId(updated.getId());
				}
			}
			public void onFailure(Throwable caught) {
				Debug.println("Preference for {0} not saved: {1}", preference.getName(), caught.getMessage());
			}
		});
	}
	
	public static ClientUser fromXML(NativeElement element) {
		ClientUser target = new ClientUser();
		
		User.fromXML(element, target, AuthorizationCache.impl.getFinder());
		
		for (UserPreference preference : target.getPreferences())
			target.setProperty(preference.getName(), preference.getValue());
		
		target.setProperty(ID, target.getId()+"");
		target.setProperty(USERNAME, target.getUsername());
		target.setProperty(EMAIL, target.getEmail());
		target.setProperty(STATE, target.getState() + "");
		target.setProperty(FIRST_NAME, target.getFirstName());
		target.setProperty(LAST_NAME,target.getLastName());
		target.setProperty(NICKNAME, target.getNickname());
		target.setProperty(INITIALS, target.getInitials());
		target.setProperty(AFFILIATION, target.getAffiliation());
		target.setProperty(SIS_USER, target.getSisUser().toString());
		target.setProperty(RAPIDLIST_USER, target.getRapidlistUser().toString());
		target.setProperty(QUICK_GROUP, target.getQuickGroupString());
		
		return target;
	}
	
	/**
	 * Pulls a property out of the User's properties map but treats it as a
	 * preference, i.e. if there is NO preference defined, it will return the
	 * default preference instead of null.
	 * 
	 * @param property
	 * @param notFoundValue
	 * @return - will never be NULL
	 */
	public String getPreference(String property, String notFoundValue) {
		String value = properties.getField(property);
		return value == null ? notFoundValue : value;
	}

}
