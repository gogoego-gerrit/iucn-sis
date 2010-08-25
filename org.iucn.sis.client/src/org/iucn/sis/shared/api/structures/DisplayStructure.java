package org.iucn.sis.shared.api.structures;

import java.util.ArrayList;

import org.iucn.sis.shared.api.models.Field;

import com.google.gwt.user.client.ui.Widget;

public interface DisplayStructure {
	
	public void clearData();
	
	public ArrayList<String> extractDescriptions();
	
	public Widget generate();
	
	public Widget generateViewOnly();
	
	public int getDisplayableData(ArrayList<String> rawData, ArrayList<String> prettyData, int offset);
	
	public boolean hasChanged();
	
	public void save(Field field);
	
	public void setData(Field field);
	
	public void setEnabled(boolean isEnabled);
	
	public String toXML();

}
