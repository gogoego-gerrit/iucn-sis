package org.iucn.sis.client.api.ui.users.panels;

/**
 * ContentManager.java
 * 
 * Interface to allow for panels to report to a manager that some content
 * elsewhere is stale and should be updated.
 * 
 * This class is merely to mask additional functionality that the class using
 * the content manager need not have access to.
 * 
 * @author carl.scott <carl.scott@solertium.com>
 * 
 */
public interface ContentManager {

	/**
	 * Sets the given content as stale.
	 * 
	 * @param contentID
	 *            the id of the content
	 */
	public void setStale(final String contentID);

}
