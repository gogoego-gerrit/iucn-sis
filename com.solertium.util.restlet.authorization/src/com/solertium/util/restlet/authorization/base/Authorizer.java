/**
 * 
 */
package com.solertium.util.restlet.authorization.base;

/**
 * Authorizer.java
 *
 * @author carl.scott <carl.scott@solertium.com>
 *
 */
public interface Authorizer {
	
	public boolean isAuthorized(final String uri, final String actor, final String action);

}
