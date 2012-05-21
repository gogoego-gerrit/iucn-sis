package core;

import junit.framework.Assert;

import org.iucn.sis.shared.api.criteriacalculator.CriteriaSet;
import org.iucn.sis.shared.api.criteriacalculator.ExpertResult.ResultCategory;
import org.iucn.sis.shared.api.models.Field;
import org.iucn.sis.shared.api.models.primitivefields.StringPrimitiveField;
import org.junit.Test;

public class CriteriaLevelTest {
	
	/*
	 * This is probably about as complicated as it gets...
	 */
	@Test
	public void testMultipleB() {
		Field B1abii = new Field();
		B1abii.addPrimitiveField(new StringPrimitiveField("B1a", B1abii, "CR"));
		B1abii.addPrimitiveField(new StringPrimitiveField("B1bii", B1abii, "CR"));
		
		check("B1ab(ii)", B1abii, "CR");
		
		Field B1aci = new Field();
		B1aci.addPrimitiveField(new StringPrimitiveField("B1a", B1aci, "CR"));
		B1aci.addPrimitiveField(new StringPrimitiveField("B1ci", B1aci, "CR"));
		
		check("B1ac(i)", B1aci, "CR");
		
		Field B1abii2abii = new Field();
		B1abii2abii.addPrimitiveField(new StringPrimitiveField("B1a", B1abii2abii, "CR"));
		B1abii2abii.addPrimitiveField(new StringPrimitiveField("B1bii", B1abii2abii, "CR"));
		B1abii2abii.addPrimitiveField(new StringPrimitiveField("B2a", B1abii2abii, "CR"));
		B1abii2abii.addPrimitiveField(new StringPrimitiveField("B2bii", B1abii2abii, "CR"));
		B1abii2abii.addPrimitiveField(new StringPrimitiveField("B2biv", B1abii2abii, "CR"));
		
		check("B1ab(ii)+2ab(ii,iv)", B1abii2abii, "CR");
	}
	
	@Test
	public void testB5() {
		CriteriaSet set = CriteriaSet.fromString(ResultCategory.DD, "B2ab(v)");
		for (String criterion : set.getCriteria())
			System.out.println(criterion);
		Assert.assertEquals(2, set.getCriteria().size());
		
		
	}
	
	@Test
	public void testC() {
		Field C1 = new Field();
		C1.addPrimitiveField(new StringPrimitiveField("C1", C1, "CR"));
		
		check("C1", C1, "CR");
		
		Field C2ai = new Field();
		C2ai.addPrimitiveField(new StringPrimitiveField("C2ai", C2ai, "CR"));
		
		check("C2a(i)", C2ai, "CR");
		
		Field E = new Field();
		E.addPrimitiveField(new StringPrimitiveField("E", E, "CR"));
		
		check("E", E, "CR");
		
		String manual = new CriteriaSet(ResultCategory.CR, "E").toString();
		
		Assert.assertEquals("E", manual);
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
		
		check("C1+2a(i,ii)b; D", ABC, "CR");
		
		ABC.addPrimitiveField(new StringPrimitiveField("A2", ABC, "CR"));
		ABC.addPrimitiveField(new StringPrimitiveField("A1c", ABC, "CR"));
		ABC.addPrimitiveField(new StringPrimitiveField("A1b", ABC, "CR"));
		ABC.addPrimitiveField(new StringPrimitiveField("B1a", ABC, "CR"));
		ABC.addPrimitiveField(new StringPrimitiveField("B2biv", ABC, "CR"));
		
		check("A1bc+2; B1a+2b(iv); C1+2a(i,ii)b; D", ABC, "CR");
	}
	
	@Test
	public void testLevels() {
		Field levels = new Field();
		levels.addPrimitiveField(new StringPrimitiveField("A2", levels, "VU"));
		levels.addPrimitiveField(new StringPrimitiveField("D", levels, "EN"));
		levels.addPrimitiveField(new StringPrimitiveField("D1", levels, "VU"));
		
		check("", levels, "CR");
		
		check("D", levels, "EN");
		
		check("A2; D1", levels, "VU");
		
		Field high = new Field();
		high.addPrimitiveField(new StringPrimitiveField("A2", high, "CR"));
		high.addPrimitiveField(new StringPrimitiveField("D", high, "CR"));
		high.addPrimitiveField(new StringPrimitiveField("D1", high, "VU"));
		high.addPrimitiveField(new StringPrimitiveField("E", high, "CR"));
		high.addPrimitiveField(new StringPrimitiveField("B2ciii", high, "CR"));
		
		check("A2; B2c(iii); D; E", high, "EN");
		check("A2; B2c(iii); D1; E", high, "VU");
	}
	
	@Test
	public void testFromString() {
		//Simple
		testString("A1+2");
		testString("D; E");
		testString("E");
		testString("B1a");
		testString("B1ac(i)");
		
		//Complete
		testString("B1ac(i,ii,iii)");
		testString("A2c+3c; B1ab(iii)");
		testString("B2ab(i,ii,iii)");
		testString("A1c; B1ab(iii); C2a(i)");
		testString("B1ab(i,ii,v)c(iii,iv)+2b(i)c(ii,v)");
		testString("A2abc+3bc+4abc; B1b(iii,iv,v)c(ii,iii,iv)+2b(iii,iv,v)c(ii,iii,iv)");
		testString("A1cd");
		testString("A2c; D");
		testString("D");
		testString("C2a(ii)");
		testString("B2b(iii)c(ii)");
		testString("B1ab(iii)+2ab(iii)");
		testString("A2c+3c");
		testString("D1+2");
		testString("D2");
	}
	
	private void testString(String criteria) {
		Assert.assertEquals(criteria, CriteriaSet.fromString(ResultCategory.CR, criteria).toString());
	}
	
	private void check(String expected, Field field, String category) {
		CriteriaSet set = new CriteriaSet(ResultCategory.fromString(category), field);
		
		String str = set.toString();
		System.out.println(str + " -> " + set.getCriteria());
		
		Assert.assertEquals(expected, str);
	}

}
