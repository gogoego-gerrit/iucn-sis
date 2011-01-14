package org.iucn.sis.shared.api.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import com.solertium.lwxml.shared.NativeElement;

public abstract class DisplayData implements Serializable {
	
	private static final long serialVersionUID = 1L;

	@SuppressWarnings("unchecked")
	public static class SupportData implements Serializable {
		private static final long serialVersionUID = 4;
		private String data1 = null;
		private HashMap data2 = null;
		private ArrayList data3 = null;
		private TreeData data4 = null;
		private Object data5 = null;

		private String type = "";

		public SupportData() {
		}

		public SupportData(Object data) {
			if (data instanceof String) {
				data1 = (String) data;
				type = "String";
			} else if (data instanceof HashMap) {
				data2 = (HashMap) data;
				type = "HashMap";
			} else if (data instanceof ArrayList) {
				data3 = (ArrayList) data;
				type = "ArrayList";
			} else if (data instanceof TreeData) {
				data4 = (TreeData) data;
				type = "TreeData";
			} else {
				data5 = data;
			}
		}

		public Object getData() {
			if (type.equalsIgnoreCase("String"))
				return data1;
			else if (type.equalsIgnoreCase("HashMap"))
				return data2;
			else if (type.equalsIgnoreCase("ArrayList"))
				return data3;
			else if (type.equalsIgnoreCase("TreeData"))
				return data4;
			else
				return data5;
		}
	}

	public static final String FIELD = "field";

	public static final String TREE = "tree";
	protected String structure; // name of the structure, e.g. narrative, number, range
	protected String description; // the input prompt
	protected SupportData data; // data specific to the field (string, hashmap, arraylist)
	protected String displayId;

	protected String uniqueId;
	protected String canonicalName; // the canonical name - unique identifier!
	protected String classOfService; // the class of service
	protected LookupDataContainer lookups;

	protected ArrayList<String> references;
	protected NativeElement fieldDefinition;
	
	// Attributes
	protected String name;
	protected String isVisible;
	protected String style;

	private String type;

	public DisplayData() {
	}

	public DisplayData(String type) {
		this.structure = "";
		this.description = "";
		this.data = new SupportData();
		this.type = type;

		this.canonicalName = "";
		this.classOfService = "";
		this.references = new ArrayList<String>();
		this.lookups = new LookupDataContainer();
	}
	
	public void addLookup(String key, LookupData data) {
		lookups.put(key, data);
	}

	public String getCanonicalName() {
		return canonicalName;
	}

	public String getClassOfService() {
		return classOfService;
	}

	public Object getData() {
		return data.getData();
	}

	/*
	 * public Object getData() { for (int i = 0; i < data.getData().length; i++)
	 * { if (data.getData()[i] != null) return data.getData()[i]; } return null;
	 * }
	 */

	public String getDescription() {
		return description;
	}

	public String getDisplayId() {
		return displayId;
	}

	public String getIsVisible() {
		return isVisible;
	}
	
	public LookupDataContainer getLookups() {
		return lookups;
	}

	public String getName() {
		return name;
	}

	public ArrayList<String> getReferences() {
		return references;
	}

	public String getStructure() {
		return structure;
	}

	public String getStyle() {
		return style;
	}

	public String getType() {
		return type;
	}

	public String getUniqueId() {
		return uniqueId;
	}
	
	public NativeElement getFieldDefinition() {
		return fieldDefinition;
	}
	
	public void setFieldDefinition(NativeElement fieldDefinition) {
		this.fieldDefinition = fieldDefinition;
	}

	public void setCanonicalName(String canonicalName) {
		this.canonicalName = canonicalName;
		lookups.setFieldName(canonicalName);
	}

	public void setClassOfService(String classOfService) {
		this.classOfService = classOfService;
	}

	/**
	 * Be careful of what data you set here, based on structures. Here is a
	 * list:<br />
	 * BOOLEAN, BOOLEAN_UNKNOWN - Boolean objects <br />
	 * NUMBER - Long object<br />
	 * RANGE - ArrayList containing three Long objects<br />
	 * NARRATIVE, TEXT, DATE - Strings<br />
	 * MULTIPLE and SINGLE SELECTS - an ArrayList containing an ArrayList of
	 * options at index 0, and an ArrayList of the selected options at index 1<br />
	 * COLLECTION - an ArrayList of FieldData objects<br />
	 * TREE - hhmm...<br />
	 * IMAGE, MAP - An ArrayList of ArrayLists - one for ids, the next for
	 * latitudinal points, the next for longitudinal points, and the next for
	 * descriptions <br />
	 * 
	 * @param data
	 */
	/*
	 * public void setData(Object datum) { this.data = new SupportData(datum); }
	 */
	public void setData(Object datum) {
		this.data = new SupportData(datum);
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setDisplayId(String id) {
		this.displayId = id;
	}

	public void setIsVisible(String isVisible) {
		this.isVisible = isVisible;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setReferences(ArrayList<String> references) {
		this.references = references;
	}

	public void setStructure(String structure) {
		this.structure = structure;
	}

	public void setStyle(String style) {
		this.style = style;
	}

	public void setUniqueId(String uniqueId) {
		this.uniqueId = uniqueId;
	}
	
	public static class LookupDataContainer extends HashMap<String, LookupData> {
		
		private static final long serialVersionUID = 1L;
		private String fieldName;
		
		public LookupDataContainer() {
			super();
		}
		       
		@Override
		public LookupData put(String key, LookupData value) {
		       return super.put(key.toLowerCase(), value);
		}
		
		@Override
		public LookupData get(Object key) {
			if (key instanceof String)
				return super.get(((String)key).toLowerCase());
			else
				return null;
		}
       
		public void setFieldName(String fieldName) {
			this.fieldName = fieldName;
		}
		   
		public String getFieldName() {
			return fieldName;
		}
		
		public LookupData find(String structureID) {
			String probableKey = fieldName + "_" + structureID + "lookup";
		 
			return get(probableKey);
		}
	}

}
