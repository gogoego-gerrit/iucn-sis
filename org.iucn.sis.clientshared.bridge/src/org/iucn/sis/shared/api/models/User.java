package org.iucn.sis.shared.api.models;
/**
 * "Visual Paradigm: DO NOT MODIFY THIS FILE!"
 * 
 * This is an automatic generated file. It will be regenerated every time 
 * you generate persistence class.
 * 
 * Modifying its content may cause the program not work, or your work may lost.
 */

/**
 * Licensee: 
 * License Type: Evaluation
 */
import java.io.Serializable;

import com.solertium.lwxml.shared.NativeElement;
import com.solertium.lwxml.shared.NativeNodeList;
public class User implements Serializable {
	
	/* THINGS I HAVE ADDED... IF YOU REGENERATE, MUST ALSO COPY THIS*/
	public final static String ROOT_TAG = "user";
	public static final int DELETED = -1;
	public static final int ACTIVE = 0;
	
	public int state;
	public int getState() {
		return state;
	}
	public void setState(int state) {
		this.state = state;
	}
	
	public String toXML() {
		StringBuilder xml = new StringBuilder("<" + ROOT_TAG + ">");
		xml.append("<id>" + getId() + "</id>");
		xml.append("<state>" + getState() + "</state>");
		xml.append("<username><![CDATA[" + getUsername() + "]]></username>");
		xml.append("<firstName><![CDATA[" + getFirstName() + "]]></firstName>");
		xml.append("<lastName><![CDATA[" + getLastName() + "]]></lastName>");
		xml.append("<initials><![CDATA[");
		xml.append((getInitials() == null)? "" : getInitials());
		xml.append("]]></initials>");
		xml.append("<affiliation><![CDATA[" + getAffiliation() + "]]></affiliation>");
		xml.append("<sisUser><![CDATA[" + isSISUser() + "]]></sisUser>");
		xml.append("<rapidListUser><![CDATA[" + getRapidlistUser() + "]]></rapidListUser>");
		xml.append("<email><![CDATA[" + getEmail() + "]]></email>");
		xml.append("</" + ROOT_TAG + ">");
		return xml.toString();		
	}
	
	public String toFullXML() {
		StringBuilder xml = new StringBuilder("<" + ROOT_TAG + ">");
		
		xml.append("<id>" + getId() + "</id>");
		xml.append("<state>" + getState() + "</state>");
		xml.append("<username><![CDATA[" + getUsername() + "]]></username>");
		xml.append("<firstName><![CDATA[" + getFirstName() + "]]></firstName>");
		xml.append("<lastName><![CDATA[" + getLastName() + "]]></lastName>");
		xml.append("<initials><![CDATA[");
		xml.append((getInitials() == null)? "" : getInitials());
		xml.append("]]></initials>");
		xml.append("<affiliation><![CDATA[" + getAffiliation() + "]]></affiliation>");
		xml.append("<sisUser><![CDATA[" + isSISUser() + "]]></sisUser>");
		xml.append("<rapidListUser><![CDATA[" + getRapidlistUser() + "]]></rapidListUser>");
		xml.append("<email><![CDATA[" + getEmail() + "]]></email>");
		
		
		for (PermissionGroup group : getPermissionGroups()) {
			xml.append(group.toXML());
		}
		
		xml.append("</" + ROOT_TAG + ">");
		
		return xml.toString();	
	}
	
	public String toBasicXML() {
		StringBuilder xml = new StringBuilder("<" + ROOT_TAG + ">");
		xml.append("<id>" + getId() + "</id>");
		xml.append("<username><![CDATA[" + getUsername() + "]]></username>");
		xml.append("<email><![CDATA[" + getEmail() + "]]></email>");
		xml.append("</" + ROOT_TAG + ">");
		return xml.toString();
	}
	
	
	public static User fromXML(NativeElement element) {
		User user = new User();
		user.setId(Integer.valueOf(element.getElementsByTagName("id").elementAt(0).getTextContent()));
		user.setUsername(element.getElementsByTagName("username").elementAt(0).getTextContent());
		user.setEmail(element.getElementsByTagName("email").elementAt(0).getTextContent());
		
		//REGULAR XML
		if (element.getElementsByTagName("state").getLength() > 0) {
			user.setState(Integer.valueOf(element.getElementsByTagName("state").elementAt(0).getTextContent()));
			user.setFirstName(element.getElementsByTagName("firstName").elementAt(0).getTextContent());
			user.setLastName(element.getElementsByTagName("lastName").elementAt(0).getTextContent());
			user.setInitials(element.getElementsByTagName("initials").elementAt(0).getTextContent());
			user.setAffiliation(element.getElementsByTagName("affiliation").elementAt(0).getTextContent());
			user.setSisUser("true".equalsIgnoreCase(element.getElementsByTagName("sisUser").elementAt(0).getTextContent()));
			user.setRapidlistUser("true".equalsIgnoreCase(element.getElementsByTagName("rapidListUser").elementAt(0).getTextContent()));
		}
		
		//FULL XML
		NativeNodeList permGroups = element.getElementsByTagName(PermissionGroup.ROOT_TAG);
		for (int i = 0; i < permGroups.getLength(); i++) {
			user.getPermissionGroups().add(PermissionGroup.fromXML(permGroups.elementAt(i)));
		}
		
		
		return user;
		
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
	
	public String getDisplayableName() {
		if (firstName == null && lastName == null)
			return email;
		else
			return this.firstName + " " + this.lastName;
    }
	
	public boolean isSISUser() {
		return getSisUser();
	}
	
	public boolean isRapidlistUser() {
		return getRapidlistUser();
	}
	
	/**
	 * Can only be called if you have the permission groups 
	 * attached at time of calling.
	 * 
	 * Returns to you the quickgroup string
	 * @return
	 */
	public String getQuickGroupString() {
		
		if (!getPermissionGroups().isEmpty()) {
			StringBuilder csv = new StringBuilder();
			for (PermissionGroup group : getPermissionGroups())
				csv.append(group.getName() + ",");
			return csv.substring(0, csv.length()-1);
		}
		return "";
		
	}
	
	private String password;
	
	public String getPassword() {
		return password;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
	
	/* THINGS I HAVE ADDED... IF YOU REGENERATE, MUST ALSO COPY THIS*/
	
	public User() {
		this.state = ACTIVE;
	}
	
	private int id;
	
	private String username;
	
	private String firstName;
	
	private String lastName;
	
	private String initials;
	
	private String affiliation;
	
	private boolean sisUser;
	
	private boolean rapidlistUser;
	
	private String email;
	
	private java.util.Set<WorkingSet> subscribedWorkingSets = new java.util.HashSet<WorkingSet>();
	
	private java.util.Set<PermissionGroup> permissionGroups = new java.util.HashSet<PermissionGroup>();
	
	private java.util.Set<WorkingSet> ownedWorkingSets = new java.util.HashSet<WorkingSet>();
	
	private java.util.Set<Edit> edit = new java.util.HashSet<Edit>();
	
	
	public void setId(int value) {
		this.id = value;
	}
	
	public int getId() {
		return id;
	}
	
	public int getORMID() {
		return getId();
	}
	
	public void setUsername(String value) {
		this.username = value;
	}
	
	public String getUsername() {
		return username;
	}
	
	public void setFirstName(String value) {
		this.firstName = value;
	}
	
	public String getFirstName() {
		return firstName;
	}
	
	public void setLastName(String value) {
		this.lastName = value;
	}
	
	public String getLastName() {
		return lastName;
	}
	
	public void setInitials(String value) {
		this.initials = value;
	}
	
	public String getInitials() {
		return initials;
	}
	
	public void setAffiliation(String value) {
		this.affiliation = value;
	}
	
	public String getAffiliation() {
		return affiliation;
	}
	
	public void setSisUser(boolean value) {
		this.sisUser = value;
	}
	
	public Boolean getSisUser() {
		return sisUser;
	}
	
	public void setRapidlistUser(Boolean value) {
		this.rapidlistUser = value;
	}
	
	public Boolean getRapidlistUser() {
		return rapidlistUser;
	}
	
	public void setEmail(String value) {
		this.email = value;
	}
	
	public String getEmail() {
		return email;
	}
	
	public void setSubscribedWorkingSets(java.util.Set<WorkingSet> value) {
		this.subscribedWorkingSets = value;
	}
	
	public java.util.Set<WorkingSet> getSubscribedWorkingSets() {
		return subscribedWorkingSets;
	}
	
	
	public void setPermissionGroups(java.util.Set<PermissionGroup> value) {
		this.permissionGroups = value;
	}
	
	public java.util.Set<PermissionGroup> getPermissionGroups() {
		return permissionGroups;
	}
	
	
	public void setOwnedWorkingSets(java.util.Set<WorkingSet> value) {
		this.ownedWorkingSets = value;
	}
	
	public java.util.Set<WorkingSet> getOwnedWorkingSets() {
		return ownedWorkingSets;
	}
	
	
	public void setEdit(java.util.Set<Edit> value) {
		this.edit = value;
	}
	
	public java.util.Set<Edit> getEdit() {
		return edit;
	}
		
	
	public String toString() {
		return String.valueOf(getId());
	}
	
}
