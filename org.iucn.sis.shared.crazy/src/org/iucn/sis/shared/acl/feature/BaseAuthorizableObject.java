package org.iucn.sis.shared.acl.feature;

import org.iucn.sis.shared.acl.base.AuthorizableObject;

public abstract class BaseAuthorizableObject implements AuthorizableObject {

	public String getProperty(String key) {
		return "";
	}
}
