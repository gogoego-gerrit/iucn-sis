package org.iucn.sis.shared.api.models.primitivefields;

import org.iucn.sis.shared.api.models.PrimitiveField;

public class PrimitiveFieldFactory {

	public static String RANGE_PRIMITIVE = "RangePrimitiveField";
	public static String BOOLEAN_PRIMITIVE = "BooleanPrimitiveField";
	public static String BOOLEAN_RANGE_PRIMITIVE = "BooleanRangePrimitiveField";
	public static String BOOLEAN_UNKNOWN_PRIMITIVE = "BooleanUnknownPrimitiveField";
	public static String FOREIGN_KEY_PRIMITIVE = "ForeignKeyPrimitiveField";
	public static String FOREIGN_KEY_LIST_PRIMITIVE = "ForeignKeyListPrimitiveField";
	public static String INTEGER_PRIMITIVE = "IntegerPrimitiveField";
	public static String STRING_PRIMITIVE = "StringPrimitiveField";
	public static String DATE_PRIMITIVE = "DatePrimitiveField";
	public static String FLOAT_PRIMITIVE = "FloatPrimitiveField";
	public static String TEXT_PRIMITIVE = "TextPrimitiveField";
	
	public static PrimitiveField generatePrimitiveField(String primitiveType) {
		
		if( primitiveType == null )
			return null;
		else if( primitiveType.equals(RANGE_PRIMITIVE) )
			return new RangePrimitiveField();
		else if( primitiveType.equals(BOOLEAN_PRIMITIVE) )
			return new BooleanPrimitiveField();
		else if( primitiveType.equals(BOOLEAN_RANGE_PRIMITIVE) )
			return new BooleanRangePrimitiveField();
		else if( primitiveType.equals(BOOLEAN_UNKNOWN_PRIMITIVE) )
			return new BooleanUnknownPrimitiveField();
		else if( primitiveType.equals(FOREIGN_KEY_PRIMITIVE) )
			return new ForeignKeyPrimitiveField();
		else if( primitiveType.equals(FOREIGN_KEY_LIST_PRIMITIVE) )
			return new ForeignKeyListPrimitiveField();
		else if( primitiveType.equals(STRING_PRIMITIVE) )
			return new StringPrimitiveField();
		else if( primitiveType.equals(INTEGER_PRIMITIVE) )
			return new IntegerPrimitiveField();
		else if( primitiveType.equals(DATE_PRIMITIVE) )
			return new DatePrimitiveField();
		else if( primitiveType.equals(FLOAT_PRIMITIVE) )
			return new FloatPrimitiveField();
		else if( primitiveType.equals(TEXT_PRIMITIVE) )
			return new TextPrimitiveField();
		else
			return null;

	}
}
