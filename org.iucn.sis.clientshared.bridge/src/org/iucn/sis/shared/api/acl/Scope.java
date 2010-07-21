/**
 * Scope.java
 * 
 * 
 */

package org.iucn.sis.shared.api.acl;

import java.io.Serializable;

public abstract class Scope implements Serializable {

	public abstract boolean matches(Object requirement);

}
