package org.iucn.sis.client.api.models;

import org.iucn.sis.shared.api.models.User;

import com.solertium.lwxml.gwt.NativeDocumentImpl;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.NativeElement;
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
	
	public String auth;
	public String password;
	public RowData properties;
	
	public ClientUser() {
		super();
		properties = new RowData();
	}
	
	public void setProperty(String key, String value) {
		properties.addField(key, value);
	}
	
	public NativeDocument getHttpBasicNativeDocument() {
		NativeDocument doc = new NativeDocumentImpl();
		doc.setHeader("Authorization", "Basic " + auth);

		return doc;
	}

	public String getProperty(String prop) {
		return properties.getField(prop);
	}
	
	public static ClientUser fromXML(NativeElement element) {
		ClientUser target = new ClientUser();
		
		User.fromXML(element, target);
		
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
