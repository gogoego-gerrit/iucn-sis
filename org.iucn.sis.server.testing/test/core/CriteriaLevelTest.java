package core;

import junit.framework.Assert;

import org.iucn.sis.shared.api.criteriacalculator.CriteriaLevel;
import org.iucn.sis.shared.api.models.Field;
import org.iucn.sis.shared.api.models.primitivefields.StringPrimitiveField;
import org.junit.Test;

public class CriteriaLevelTest {
	
	@Test
	public void testC() {
		Field C1 = new Field();
		C1.addPrimitiveField(new StringPrimitiveField("C1", C1, "CR"));
		
		check("C1", C1, "CR");
		
		Field C2ai = new Field();
		C2ai.addPrimitiveField(new StringPrimitiveField("C2ai", C2ai, "CR"));
		
		check("C2a(i)", C2ai, "CR");
	}
	
	@Test
	public void testMultipleC() {
		Field C = new Field();
		C.addPrimitiveField(new StringPrimitiveField("C1", C, "CR"));
		C.addPrimitiveField(new StringPrimitiveField("C2b", C, "CR"));
		
		check("C1+2b", C, "CR");
		
		Field C2aiii = new Field();
		C2aiii.addPrimitiveField(new StringPrimitiveField("C2ai", C2aiii, "CR"));
		C2aiii.addPrimitiveField(new StringPrimitiveField("C2aii", C2aiii, "CR"));
		
		check("C2a(i,ii)", C2aiii, "CR");
	}
	
	@Test
	public void testMultiple() {
		Field ABC = new Field();
		ABC.addPrimitiveField(new StringPrimitiveField("C1", ABC, "CR"));
		ABC.addPrimitiveField(new StringPrimitiveField("C2b", ABC, "CR"));
		ABC.addPrimitiveField(new StringPrimitiveField("C2ai", ABC, "CR"));
		ABC.addPrimitiveField(new StringPrimitiveField("C2aii", ABC, "CR"));
		
		check("C1+2a(i,ii)b", ABC, "CR");
		
		ABC.addPrimitiveField(new StringPrimitiveField("D", ABC, "CR"));
		
		check("C1+2a(i,ii)b,D", ABC, "CR");
		
		ABC.addPrimitiveField(new StringPrimitiveField("A2", ABC, "CR"));
		ABC.addPrimitiveField(new StringPrimitiveField("A1c", ABC, "CR"));
		ABC.addPrimitiveField(new StringPrimitiveField("A1b", ABC, "CR"));
		ABC.addPrimitiveField(new StringPrimitiveField("B1a", ABC, "CR"));
		ABC.addPrimitiveField(new StringPrimitiveField("B2biv", ABC, "CR"));
		
		check("A1bc+2,B1a+2b(iv),C1+2a(i,ii)b,D", ABC, "CR");
	}
	
	private void check(String expected, Field field, String category) {
		CriteriaLevel l = CriteriaLevel.parse(field, category);
		
		String str = l.toString();
		System.out.println(str);
		
		Assert.assertEquals(expected, str);
	}

}
