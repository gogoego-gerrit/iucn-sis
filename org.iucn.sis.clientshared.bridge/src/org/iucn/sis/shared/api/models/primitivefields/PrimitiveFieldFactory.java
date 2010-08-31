package org.iucn.sis.shared.api.models.primitivefields;

import org.iucn.sis.shared.api.models.PrimitiveField;

public class PrimitiveFieldFactory {
	
	public static PrimitiveField generatePrimitiveField(String primitiveType) {
		return generatePrimitiveField(PrimitiveFieldType.get(primitiveType));
	}
	
	public static PrimitiveField generatePrimitiveField(PrimitiveFieldType primitiveType) {
		if( primitiveType == null )
			return null;
		else if( primitiveType.equals(PrimitiveFieldType.RANGE_PRIMITIVE) )
			return new RangePrimitiveField();
		else if( primitiveType.equals(PrimitiveFieldType.BOOLEAN_PRIMITIVE) )
			return new BooleanPrimitiveField();
		else if( primitiveType.equals(PrimitiveFieldType.BOOLEAN_RANGE_PRIMITIVE) )
			return new BooleanRangePrimitiveField();
		else if( primitiveType.equals(PrimitiveFieldType.BOOLEAN_UNKNOWN_PRIMITIVE) )
			return new BooleanUnknownPrimitiveField();
		else if( primitiveType.equals(PrimitiveFieldType.FOREIGN_KEY_PRIMITIVE) )
			return new ForeignKeyPrimitiveField();
		else if( primitiveType.equals(PrimitiveFieldType.FOREIGN_KEY_LIST_PRIMITIVE) )
			return new ForeignKeyListPrimitiveField();
		else if( primitiveType.equals(PrimitiveFieldType.STRING_PRIMITIVE) )
			return new StringPrimitiveField();
		else if( primitiveType.equals(PrimitiveFieldType.INTEGER_PRIMITIVE) )
			return new IntegerPrimitiveField();
		else if( primitiveType.equals(PrimitiveFieldType.DATE_PRIMITIVE) )
			return new DatePrimitiveField();
		else if( primitiveType.equals(PrimitiveFieldType.FLOAT_PRIMITIVE) )
			return new FloatPrimitiveField();
		else if( primitiveType.equals(PrimitiveFieldType.TEXT_PRIMITIVE) )
			return new TextPrimitiveField();
		else
			return null;

	}
}
