package org.iucn.sis.shared.api.models.primitivefields;

public enum PrimitiveFieldType {
	
	RANGE_PRIMITIVE("RangePrimitiveField", "range_primitive_field"), 
	BOOLEAN_PRIMITIVE("BooleanPrimitiveField", "boolean_primitive_field"), 
	BOOLEAN_RANGE_PRIMITIVE("BooleanRangePrimitiveField", "boolean_range_primitive_field"), 
	BOOLEAN_UNKNOWN_PRIMITIVE("BooleanUnknownPrimitiveField", "boolean_unknown_primitive_field"),
	FOREIGN_KEY_PRIMITIVE("ForeignKeyPrimitiveField", "fk_primitive_field"),
	FOREIGN_KEY_LIST_PRIMITIVE("ForeignKeyListPrimitiveField", "fk_list_primitive_field"),
	INTEGER_PRIMITIVE("IntegerPrimitiveField", "integer_primitive_field"),
	STRING_PRIMITIVE("StringPrimitiveField", "string_primitive_field"),
	DATE_PRIMITIVE("DatePrimitiveField", "date_primitive_field"),
	FLOAT_PRIMITIVE("FloatPrimitiveField", "float_primitive_field"),
	TEXT_PRIMITIVE("TextPrimitiveField", "text_primitive_field");
	
	public static PrimitiveFieldType get(String name) {
		for (PrimitiveFieldType type : values())
			if (type.matches(name))
				return type;
		return null;
	}
	
	private String name;
	private String[] matches;
	
	private PrimitiveFieldType(String name, String... matches) {
		this.name = name;
		this.matches = matches;
	}
	
	public boolean matches(String name) {
		for (String match : matches)
			if (match.equals(name))
				return true;
		return this.name.equals(name);
	}
	
	public String getName() {
		return name;
	}
	
	public String toString() {
		return name;
	}

}
