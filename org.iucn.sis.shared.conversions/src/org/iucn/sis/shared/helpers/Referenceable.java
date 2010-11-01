package org.iucn.sis.shared.helpers;

import java.util.ArrayList;

import com.solertium.lwxml.shared.GenericCallback;

public interface Referenceable {
	/**
	 * Invoked when addition of references has been instigated. The argument
	 * contains ReferenceUI objects that should be added to this object's List
	 * or Map of references, a persisting save then MIGHT occur, then the
	 * callback's onSuccess or onFailure method will be invoked, as appropriate.
	 * 
	 * @param references
	 * @param callback
	 */
	public void addReferences(ArrayList<ReferenceUI> references, GenericCallback<Object> callback);

	/**
	 * Returns this object's references in an unsorted ArrayList, duplicates
	 * definitely allowed (the ReferenceViewPanel dedupes based on this list).
	 * 
	 * @return ArrayList of ReferenceUI objects
	 */
	public ArrayList<ReferenceUI> getReferencesAsList();

	/**
	 * Invoked when the references belonging to this Referenceable object have
	 * been changed; it should perform some persisting save, then invoke the
	 * callback's onSuccess or onFailure method, as appropriate.
	 * 
	 * @param callback
	 */
	public void onReferenceChanged(GenericCallback<Object> callback);

	/**
	 * Invoked when removal of references has been instigated. The argument
	 * contains ReferenceUI objects that should be removed from this object's
	 * List or Map of references, a persisting save then MIGHT occur, then the
	 * callback's onSuccess or onFailure method will be invoked, as appropriate.
	 * 
	 * @param references
	 * @param callback
	 */
	public void removeReferences(ArrayList<ReferenceUI> references, GenericCallback<Object> callback);
}
