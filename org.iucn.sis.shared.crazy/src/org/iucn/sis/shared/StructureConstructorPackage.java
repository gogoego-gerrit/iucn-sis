package org.iucn.sis.shared;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class StructureConstructorPackage implements Serializable {
	/**
	 * Allows an "object" to be serializable, suprisingly quick.
	 * 
	 * @author carl.scott
	 */
	public static class SupportData implements Serializable {
		private static final long serialVersionUID = 4;
		private String data1 = null;
		private HashMap data2 = null;
		private ArrayList data3 = null;
		private TreeData data4 = null;

		private String type = "";

		public SupportData() {
		}

		public SupportData(Object data) {
			if (data == null) {
				type = "String";
			} else if (data instanceof String) {
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
				return null;
		}
	}

	private static final long serialVersionUID = 3;
	// Main data
	private String structure;
	private String description;
	private SupportData data;

	private HashMap dataValues;

	// Variables held in ...
	private DisplayData displayData;

	public StructureConstructorPackage() {
		this("", "", "");
	}

	public StructureConstructorPackage(String structure, String description, Object data) {
		this.structure = structure;
		this.description = description;
		this.data = new SupportData(data);
	}

	public StructureConstructorPackage(String structure, String description, Object data, HashMap dataValues) {
		this(structure, description, data);
		this.dataValues = dataValues;
	}

	public Object getData() {
		return data.getData();
	}

	public HashMap getDataValues() {
		return dataValues;
	}

	public String getDescription() {
		return description;
	}

	public DisplayData getDisplayData() {
		return displayData;
	}

	public String getStructure() {
		return structure;
	}

	public void setData(Object data) {
		this.data = new SupportData(data);
	}

	public void setDataValues(HashMap dataValues) {
		this.dataValues = dataValues;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setDisplayData(DisplayData displayData) {
		this.displayData = displayData;
	}

	public void setStructure(String structure) {
		this.structure = structure;
	}

}
