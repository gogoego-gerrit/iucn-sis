package org.iucn.sis.shared.api.models;

import java.io.Serializable;

import com.solertium.lwxml.shared.NativeElement;

public class UserPreference implements Serializable {
	
	public static final String ROOT_TAG = "preference";
	
	private int id;
	private String name;
	private String value;
	private User user;
	
	public UserPreference() {
	}
	
	public UserPreference(String name, String value) {
		setName(name);
		setValue(value);
	}
	
	public int getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public User getUser() {
		return user;
	}
	
	public String getValue() {
		return value;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setUser(User user) {
		this.user = user;
	}
	
	public void setValue(String value) {
		this.value = value;
	}
	
	public boolean hasValue() {
		return value != null && !"".equals(value);
	}
	
	public String toXML() {
		return "<" + ROOT_TAG + " id=\"" + getId() + "\" name=\"" + name + "\"><![CDATA[" + value + "]]></" + ROOT_TAG + ">";
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UserPreference other = (UserPreference) obj;
		if (id != other.id)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

	public static UserPreference fromXML(NativeElement node) {
		UserPreference preference = new UserPreference();
		preference.setId(Integer.valueOf(node.getAttribute("id")));
		preference.setName(node.getAttribute("name"));
		preference.setValue(node.getTextContent());
		
		return preference;
	}

}
