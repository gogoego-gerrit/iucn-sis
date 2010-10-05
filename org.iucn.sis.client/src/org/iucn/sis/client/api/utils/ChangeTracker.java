/**
 * ChangeTracker.java
 * 
 * Determines whether or not a Trackable object has changed, and what
 * values in that trackable object have changed.
 * 
 * @author carl.scott
 */

package org.iucn.sis.client.api.utils;

import java.util.HashMap;
import java.util.Iterator;

import org.iucn.sis.shared.api.debug.Debug;

public class ChangeTracker {

	private HashMap initValues;
	private HashMap changedValues; // TODO: store the new values to present!

	/**
	 * Constructor
	 */
	public ChangeTracker() {
		changedValues = new HashMap();
	}

	public void clearChanges() {
		changedValues.clear();
	}

	/**
	 * Accessfor for the changed values
	 * 
	 * @return the values as a HashMap
	 */
	public HashMap getChangedValues() {
		return this.changedValues;
	}

	/**
	 * Returns an iterator of the initial values keyset
	 * 
	 * @return iterator
	 */
	public Iterator getInitKeySetIterator() {
		return initValues.keySet().iterator();
	}

	/**
	 * Returns the initial values of the trackable object
	 * 
	 * @return hashmap w/the values
	 */
	public HashMap getInitValues() {
		return initValues;
	}

	/**
	 * Determines if a Trackable object has changed, based on the Trackable
	 * object's hasChanged method.
	 * 
	 * @param trackableObject
	 *            the object to check
	 * @return true if changed, false otherwise
	 */
	public boolean hasChanged(Trackable trackableObject) {
		return trackableObject.hasChanged();
	}

	/**
	 * Determines if the initial values hashmap has the same keys as another map
	 * 
	 * @param newValues
	 *            the other map
	 * @return true if they have identical keysets, false otherwise
	 */
	public boolean identicalKeySet(HashMap newValues) {
		return initValues.keySet().containsAll(newValues.keySet()) && initValues.size() == newValues.size();
	}

	/**
	 * Given a key and another hashmap, determines if one hashmap's value at
	 * that key is the same as the initial one. Different is defined by unequal
	 * objects OR a null initial object
	 * 
	 * @param key
	 *            the key to check
	 * @param otherMap
	 *            the other map
	 * @return true if diffferent, false otherwise
	 */
	public boolean isDiff(Object key, HashMap otherMap) {
		return (initValues.get(key) == null) || !initValues.get(key).equals(otherMap.get(key));
	}

	public void printChanges() {
		Debug.println(
				(changedValues.size() == 0) ? "" : "-----------CHANGE FIELDS (" + changedValues.size()
						+ ")------------");
		Iterator iterator = changedValues.keySet().iterator();
		while (iterator.hasNext()) {
			Object key = iterator.next();
			Debug.println(key + ": " + changedValues.get(key));
		}
	}

	/**
	 * TODO: PUT THIS FUNCTIONALITY AT THE STRUCTURE LEVEL
	 * 
	 * @param trackableObject
	 */
	public void revertValues(Trackable trackableObject) {
		trackableObject.revert();
	}

	/**
	 * Sets the initial values of the object this is tracking
	 * 
	 * @param initValues
	 */
	public void setInitValues(HashMap initValues) {
		this.initValues = new HashMap();
		if (initValues != null)
			this.initValues.putAll(initValues);
	}

	/**
	 * Tracks changes for a given trackable object, such as IDs for changed
	 * objects and also their new values
	 * 
	 * @param trackableObject
	 */
	public void trackChanges(Trackable trackableObject, HashMap currentValues) {
		changedValues.put(trackableObject.getID(), currentValues);
	}

	/**
	 * On a change, one should update the initial value set to reflect the
	 * changes
	 * 
	 * @param update
	 *            the new map
	 */
	public void updateInitValues(HashMap update) {
		initValues = new HashMap();
		initValues.putAll(update);
	}

}// class ChangeTracker
