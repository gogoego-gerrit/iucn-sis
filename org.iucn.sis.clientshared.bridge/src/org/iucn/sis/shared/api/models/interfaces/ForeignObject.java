package org.iucn.sis.shared.api.models.interfaces;

/**
 * ForeignObject.java
 * 
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a
 *         href="http://www.solertium.com">Solertium Corporation</a>
 *
 */
public interface ForeignObject<T> {
	
	public int getId();

	public T getOfflineCopy();
	
	/**
	 * Return true if created offline, false otherwise.
	 * @return
	 */
	public boolean getOfflineStatus(); 
	
	/**
	 * Set to true if this object was created offline, false otherwise.
	 * @param offlineStatus
	 */
	public void setOfflineStatus(boolean offlineStatus);
	
}
