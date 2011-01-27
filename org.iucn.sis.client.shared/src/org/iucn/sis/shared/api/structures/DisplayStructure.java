package org.iucn.sis.shared.api.structures;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.ui.Widget;

public interface DisplayStructure<T, V> {
	
	public void clearData();
	
	public void disable();
	
	public void enable();
	
	public ArrayList<String> extractDescriptions();
	
	public Widget generate();
	
	public Widget generateViewOnly();
	
	public List<ClassificationInfo> getClassificationInfo();
	
	public String getData();
	
	public String getDescription();
	
	/**
	 * Pass in the raw data from an Assessment object, and this will return
	 * it in happy, displayable String form
	 * 
	 * @return ArrayList of Strings, having converted the rawData to nicely
	 *         displayable String data. Happy days!
	 */
	public int getDisplayableData(ArrayList<String> rawData, ArrayList<String> prettyData, int offset);
	
	public String getId();
	
	/**
	 * Determine if this structure has an ID
	 * @return
	 */
	public boolean hasId();
	
	/**
	 * Gets a description of the type of structure this is
	 * 
	 * @return the structure
	 */
	public String getStructureType();
	
	/**
	 * Determine if the data has changed given the previous 
	 * value.  This value could be null if it was never set, 
	 * so do not forget to check for null.  
	 * 
	 * @param field the object containing the previous value
	 * @return true if changes were made, false otherwise
	 */
	public boolean hasChanged(T field);
	
	/**
	 * Hides a structure
	 */
	public void hide();
	
	public boolean isPrimitive();
	
	/**
	 * Save your data into this object.  This object should 
	 * never be null, so you may simply set it's value. 
	 * @param field the object to set your data in
	 */
	public void save(V parent, T field);
	
	public void setData(T field);
	
	public void setEnabled(boolean isEnabled);
	
	/**
	 * Unhides a structure
	 */
	public void show();

}
