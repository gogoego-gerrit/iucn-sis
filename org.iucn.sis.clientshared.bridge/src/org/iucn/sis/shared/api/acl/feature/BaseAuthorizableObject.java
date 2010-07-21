package org.iucn.sis.shared.api.acl.feature;

import org.iucn.sis.shared.api.acl.base.AuthorizableObject;

public abstract class BaseAuthorizableObject implements AuthorizableObject {

	public String getProperty(String key) {
		return "";
	}
}
