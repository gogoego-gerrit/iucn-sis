package org.iucn.sis.shared.acl;

import java.util.Date;
import java.util.HashMap;

import org.iucn.sis.shared.ByteArray;

import com.solertium.lwxml.factory.NativeDocumentFactory;
import com.solertium.lwxml.shared.NativeDocument;

public class User implements Comparable<User> {

	public Long id;
	public String username;
	public String password;
	public String email;
	public String commonName;
	public String firstName;
	public String lastName;
	public String organizationName;
	public String businessUnit;
	public String phone;
	public String address;
	public String countForecast;
	public String countActual;
	public String accessLevel;
	public String actionLog;
	public Date lastLogin;
	public String initials;

	public ByteArray sessionKey;

	public HashMap<String, String> properties;

	public User() {
		properties = new HashMap<String, String>();
	}

	public int compareTo(User o) {
		return this.getDisplayableName().compareTo(o.getDisplayableName());
	}

	@Override
	public boolean equals(Object obj) {
		if( obj instanceof User )
			return this.id == ((User)obj).getId();
		else
			return super.equals(obj);
	}
	
	public String getAccessLevel() {
		return accessLevel;
	}

	public String getActionLog() {
		return actionLog;
	}

	public String getAddress() {
		return address;
	}

	public String getBusinessUnit() {
		return businessUnit;
	}

	public String getCitationName() {
		String name = "";
		String firstName = "";
		if (initials != null && !initials.equalsIgnoreCase(""))
			firstName = initials;
		else if (this.firstName != null && this.firstName.length() > 0)
			firstName = this.firstName.charAt(0) + ".";

		if (lastName != null) {
			name += lastName + ", ";
		}

		name += firstName;
		return name;
	}

	public String getCommonName() {
		return commonName;
	}

	public String getCountActual() {
		return countActual;
	}

	public String getCountForecast() {
		return countForecast;
	}

	public String getDisplayableName() {
		return this.firstName + " " + this.lastName;
	}

	public String getEmail() {
		return email;
	}

	public String getFirstName() {
		return firstName;
	}

	public NativeDocument getHttpBasicNativeDocument() {
		NativeDocument doc = NativeDocumentFactory.newNativeDocument();
		doc.setHeader("Authorization", "Basic " + properties.get("authn"));

		return doc;
	}

	public Long getId() {
		return id;
	}

	public String getInitials() {
		return initials;
	}

	public Date getLastLogin() {
		return lastLogin;
	}

	public String getLastName() {
		return lastName;
	}

	public String getOrganizationName() {
		return organizationName;
	}

	public String getPassword() {
		return password;
	}

	public String getPhone() {
		return phone;
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

	public String getProperty(String property) {
		return properties.get(property);
	}

	public ByteArray getSessionKey() {
		return sessionKey;
	}

	public String getUsername() {
		return username;
	}

	@Override
	public int hashCode() {
		if( id != null )
			return id.hashCode();
		else if( username != null )
			return username.hashCode();
		else
			return super.hashCode();
	}

	public void setAccessLevel(String accessLevel) {
		this.accessLevel = accessLevel;
	}

	public void setActionLog(String actionLog) {
		this.actionLog = actionLog;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public void setBusinessUnit(String businessUnit) {
		this.businessUnit = businessUnit;
	}

	public void setCommonName(String commonName) {
		this.commonName = commonName;
	}

	public void setCountActual(String countActual) {
		this.countActual = countActual;
	}

	public void setCountForecast(String countForecast) {
		this.countForecast = countForecast;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setInitials(String initials) {
		this.initials = initials;
	}

	public void setLastLogin(Date lastLogin) {
		this.lastLogin = lastLogin;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public void setOrganizationName(String organizationName) {
		this.organizationName = organizationName;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public void setProperty(String property, String action) {
		properties.put(property, action);
	}

	public void setSessionKey(ByteArray sessionKey) {
		this.sessionKey = sessionKey;
	}

	public void setUsername(String username) {
		this.username = username;
	}

}
