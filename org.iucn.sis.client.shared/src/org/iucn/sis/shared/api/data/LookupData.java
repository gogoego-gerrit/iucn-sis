package org.iucn.sis.shared.api.data;

import java.util.ArrayList;
import java.util.List;

public class LookupData {
	
	private final List<LookupDataValue> values;
	
	private List<String> defaultValues;
	
	public LookupData() {
		values = new ArrayList<LookupDataValue>();
		defaultValues = new ArrayList<String>();
	}
	
	public void addValue(String id, String label) {
		values.add(new LookupDataValue(id, label));
		defaultValues = new ArrayList<String>();
	}
	
	public void addDefaultValue(String id) {
		/*
		 * FIXME: This isn't really supported when this 
		 * data is being generated from the database.  
		 * We can't pull from file because the indices 
		 * are wrong.  Need to go through every file 
		 * and change this, or add this information to 
		 * the database.
		 */
		this.defaultValues.add(id);
	}
	
	public List<String> getDefaultValues() {
		return defaultValues;
	}
	
	public List<LookupDataValue> getValues() {
		return values;
	}
	
	public String getLabel(String id) {
		for (LookupDataValue value : values)
			if (value.getID().equals(id))
				return value.getLabel();
		return null;
	}
	
	public static class LookupDataValue {
		
		private String id, label;
		
		LookupDataValue(String id, String label) {
			this.id = id;
			this.label = label;
		}
		
		public String getID() {
			return id;
		}
		
		public String getLabel() {
			return label;
		}
		
		boolean matches(String label) {
			return this.label.equals(label);
		}
		
		public String toString() {
			return id + ": " + label;
		}
	}
	
	public String toString() {
		return "Values: " + getValues() + "\nDefault: " + getDefaultValues();
	}

}
