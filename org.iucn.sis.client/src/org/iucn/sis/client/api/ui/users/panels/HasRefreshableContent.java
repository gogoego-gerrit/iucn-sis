package org.iucn.sis.client.api.ui.users.panels;

/**
 * HasRefreshableContent
 * 
 * Denotes that this object has content that can be refreshed by calling the
 * appropritae function.
 * 
 * @author carl.scott <carl.scott@solertium.com>
 * 
 */
public interface HasRefreshableContent {

	/**
	 * Refresh the data and/or layout of a page, as appropriate.
	 * 
	 */
	public void refresh();

}
