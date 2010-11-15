package org.iucn.sis.client.api.models;

import org.iucn.sis.shared.api.models.User;

import com.solertium.lwxml.factory.NativeDocumentFactory;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.NativeElement;
import com.solertium.lwxml.shared.utils.RowData;

public class ClientUser extends User {
	
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
		NativeDocument doc = NativeDocumentFactory.newNativeDocument();
		doc.setHeader("Authorization", "Basic " + auth);

		return doc;
	}

	public String getProperty(String prop) {
		return properties.getField(prop);
	}
	
	public static ClientUser fromXML(NativeElement element) {
		ClientUser target = new ClientUser();
		
		User.fromXML(element, target);
		
		target.setProperty("id", target.getId()+"");
		target.setProperty("username", target.getUsername());
		target.setProperty("email", target.getEmail());
		target.setProperty("state", target.getState() + "");
		target.setProperty("firstName", target.getFirstName());
		target.setProperty("lastName",target.getLastName());
		target.setProperty("nickname", target.getNickname());
		target.setProperty("initials", target.getInitials());
		target.setProperty("affiliation", target.getAffiliation());
		target.setProperty("sisUser", target.getSisUser().toString());
		target.setProperty("rapidListUser", target.getRapidlistUser().toString());
		target.setProperty("quickgroup", target.getQuickGroupString());
		
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
		return value == null ? notFoundValue : property;
	}

}
