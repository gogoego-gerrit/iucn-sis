package org.iucn.sis.client.api.models;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.iucn.sis.client.api.caches.AuthorizationCache;
import org.iucn.sis.shared.api.models.PermissionGroup;
import org.iucn.sis.shared.api.models.User;

import com.solertium.lwxml.factory.NativeDocumentFactory;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.NativeElement;
import com.solertium.lwxml.shared.NativeNodeList;

public class ClientUser extends User {
	
	public String auth;
	public String password;
	public Map<String,String> properties;
	
	public ClientUser() {
		super();
		properties = new HashMap<String, String>();
	}
	
	public void setProperty(String key, String value) {
		properties.put(key, value);
	}
	
	
	
	public NativeDocument getHttpBasicNativeDocument() {
		NativeDocument doc = NativeDocumentFactory.newNativeDocument();
		doc.setHeader("Authorization", "Basic " + auth);

		return doc;
	}

	public String getProperty(String prop) {
		return properties.get(prop);
	}
	
	public static ClientUser fromXML(NativeElement element) {
			
		ClientUser user = new ClientUser();
		user.setId(Integer.valueOf(element.getElementsByTagName("id").elementAt(0).getTextContent()));
		user.setProperty("id", user.getId()+"");
		
		user.setUsername(element.getElementsByTagName("username").elementAt(0).getTextContent());
		user.setProperty("username", user.getUsername());
		user.setEmail(element.getElementsByTagName("email").elementAt(0).getTextContent());
		user.setProperty("email", user.getEmail());
		
		//FULL XML
		if (element.getElementsByTagName("state").getLength() > 0) {
			user.setState(Integer.valueOf(element.getElementsByTagName("state").elementAt(0).getTextContent()));
			user.setProperty("state", user.getState() + "");
			user.setFirstName(element.getElementsByTagName("firstName").elementAt(0).getTextContent());
			user.setProperty("firstName", user.getFirstName());
			user.setLastName(element.getElementsByTagName("lastName").elementAt(0).getTextContent());
			user.setProperty("lastName",user.getLastName());
			user.setInitials(element.getElementsByTagName("initials").elementAt(0).getTextContent());
			user.setProperty("initials", user.getInitials());
			user.setAffiliation(element.getElementsByTagName("affiliation").elementAt(0).getTextContent());
			user.setProperty("affiliation", user.getAffiliation());
			user.setSisUser("true".equalsIgnoreCase(element.getElementsByTagName("sisUser").elementAt(0).getTextContent()));
			user.setProperty("sisUser", user.getSisUser().toString());
			user.setRapidlistUser("true".equalsIgnoreCase(element.getElementsByTagName("rapidListUser").elementAt(0).getTextContent()));
			user.setProperty("rapidListUser", user.getRapidlistUser().toString());
			
		}
		
		//FULL XML
		NativeNodeList permGroups = element.getElementsByTagName(PermissionGroup.ROOT_TAG);
		for (int i = 0; i < permGroups.getLength(); i++) {
			user.getPermissionGroups().add(PermissionGroup.fromXML(permGroups.elementAt(i)));
		}
		
		HashSet<PermissionGroup> groups =new HashSet<PermissionGroup>();
		for (PermissionGroup group : user.getPermissionGroups()) {
			groups.add(AuthorizationCache.impl.getIdToGroups().get(Integer.valueOf(group.getId())));
		}
		user.setPermissionGroups(groups);
		
		user.setProperty("quickgroup", user.getQuickGroupString());
		
		return user;
		
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
		if (properties.containsKey(property))
			return properties.get(property);
		else
			return notFoundValue;
	}

}
