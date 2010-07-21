package org.iucn.sis.client.utilities;

import java.util.HashMap;

public interface Trackable {

	/**
	 * Returns the new values for the object
	 * 
	 * @return the new Values, in a HashMap (for now)
	 */
	public HashMap getCurrentValues();

	/**
	 * Returns The ID of this object
	 * 
	 * @return the id
	 */
	public String getID();

	/**
	 * Determines if this trackable object has had changes made to it
	 * 
	 * @return true if changed, false otherwise
	 */
	public boolean hasChanged();

	/**
	 * Reverts an object to its init values
	 */
	public void revert();
}
