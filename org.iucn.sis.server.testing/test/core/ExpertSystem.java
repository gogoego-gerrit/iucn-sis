package core;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.iucn.sis.shared.api.criteriacalculator.ExpertResult;
import org.iucn.sis.shared.api.criteriacalculator.FuzzyExpImpl;
import org.iucn.sis.shared.api.debug.Debug;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.Field;
import org.iucn.sis.shared.api.models.PrimitiveField;
import org.iucn.sis.shared.api.models.primitivefields.BooleanRangePrimitiveField;
import org.iucn.sis.shared.api.models.primitivefields.ForeignKeyListPrimitiveField;
import org.iucn.sis.shared.api.models.primitivefields.RangePrimitiveField;
import org.iucn.sis.shared.api.utils.CanonicalNames;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;

import com.solertium.lwxml.java.JavaNativeDocument;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.util.BaseDocumentUtils;

import files.Files;

public class ExpertSystem extends BasicTest {
	
	static AtomicInteger idGenerator = new AtomicInteger(0);
	
	@BeforeClass
	public static void setup() {
		FuzzyExpImpl.VERBOSE = true;
	}
	
	@Test
	public void tryIt() {
		run(getAssessment("A1854815"));
	}
	
	@Test
	public void goodA1() {
		Assessment assessment = new Assessment();
		assessment.getField().add(newField(CanonicalNames.PopulationReductionPast, 
			new RangePrimitiveField("range", null, "85")	
		));
		assessment.getField().add(newField(CanonicalNames.PopulationReductionPastReversible, 
			new BooleanRangePrimitiveField("value", null, "1")	
		));
		assessment.getField().add(newField(CanonicalNames.PopulationReductionPastUnderstood, 
			new BooleanRangePrimitiveField("value", null, "1")	
		));
		assessment.getField().add(newField(CanonicalNames.PopulationReductionPastCeased, 
			new BooleanRangePrimitiveField("value", null, "1")	
		));
		assessment.getField().add(newField(CanonicalNames.PopulationReductionPastBasis, 
			newFKList("value", 1, 4)	
		));
		
		int index = 0;
		String[] crit = new String[] { "CR", "EN", "VU", "DD" };
		String[] cat = new String[] { "A1ad", "A1ad", "A1ad", "" };
		for (String value : new String[] { "95", "85", "55", "45" }) {
			Field field = assessment.getField(CanonicalNames.PopulationReductionPast);
			field.getPrimitiveField("range").setRawValue(value);
			
			ExpertResult result = run(assessment);
			Assert.assertEquals(cat[index], result.getCriteriaString());
			Assert.assertEquals(crit[index++], result.getAbbreviatedCategory());
		}
	}
	
	@Test
	public void goodA2() {
		Assessment assessment = new Assessment();
		assessment.getField().add(newField(CanonicalNames.PopulationReductionPast, 
			new RangePrimitiveField("range", null, "85")	
		));
		assessment.getField().add(newField(CanonicalNames.PopulationReductionPastReversible, 
			new BooleanRangePrimitiveField("value", null, "1")	
		));
		assessment.getField().add(newField(CanonicalNames.PopulationReductionPastUnderstood, 
			new BooleanRangePrimitiveField("value", null, "0")	
		));
		assessment.getField().add(newField(CanonicalNames.PopulationReductionPastCeased, 
			new BooleanRangePrimitiveField("value", null, "0")	
		));
		assessment.getField().add(newField(CanonicalNames.PopulationReductionPastBasis, 
			newFKList("value", 1, 4)
		));
		
		int index = 0;
		String[] crit = new String[] { "CR", "EN", "VU", "DD" };
		String[] cat = new String[] { "A2ad", "A2ad", "A2ad", "" };
		for (String value : new String[] { "85", "55", "35", "15" }) {
			Field field = assessment.getField(CanonicalNames.PopulationReductionPast);
			field.getPrimitiveField("range").setRawValue(value);
			
			ExpertResult result = run(assessment);
			Assert.assertEquals(cat[index], result.getCriteriaString());
			Assert.assertEquals(crit[index++], result.getAbbreviatedCategory());
		}
	}
	
	@Test
	public void goodA3() {
		Assessment assessment = new Assessment();
		assessment.getField().add(newField(CanonicalNames.PopulationReductionFuture, 
			new RangePrimitiveField("range", null, "85")	
		));
		assessment.getField().add(newField(CanonicalNames.PopulationReductionFutureBasis, 
			newFKList("value", 3)
		));
		
		int index = 0;
		String[] crit = new String[] { "CR", "EN", "VU", "DD" };
		String[] cat = new String[] { "A3d", "A3d", "A3d", "" };
		for (String value : new String[] { "85", "55", "35", "15" }) {
			Field field = assessment.getField(CanonicalNames.PopulationReductionFuture);
			field.getPrimitiveField("range").setRawValue(value);
			
			ExpertResult result = run(assessment);
			Assert.assertEquals(cat[index], result.getCriteriaString());
			Assert.assertEquals(crit[index++], result.getAbbreviatedCategory());
		}
	}
	
	@Test
	public void goodB1() {
		Assessment assessment = new Assessment();
		assessment.getField().add(newField(CanonicalNames.EOO, 
			new RangePrimitiveField("range", null, "50")
		));
		assessment.getField().add(newField(CanonicalNames.SevereFragmentation, 
			new BooleanRangePrimitiveField("isFragmented", null, "1")	
		));
		assessment.getField().add(newField(CanonicalNames.AOOContinuingDecline, 
			new BooleanRangePrimitiveField("isInContinuingDecline", null, "1")	
		));
		
		ExpertResult result = run(assessment);
		
		Assert.assertEquals(result.getAbbreviatedCategory(), "CR");
		Assert.assertEquals(result.getCriteriaString(), "B1ab(ii)");
	}
	
	@Test
	public void goodB1B2() {
		Assessment assessment = new Assessment();
		assessment.getField().add(newField(CanonicalNames.EOO, 
			new RangePrimitiveField("range", null, "50")
		));
		assessment.getField().add(newField(CanonicalNames.AOO, 
			new RangePrimitiveField("range", null, "5")
		));
		assessment.getField().add(newField(CanonicalNames.SevereFragmentation, 
			new BooleanRangePrimitiveField("isFragmented", null, "1")	
		));
		assessment.getField().add(newField(CanonicalNames.AOOContinuingDecline, 
			new BooleanRangePrimitiveField("isInContinuingDecline", null, "1")	
		));
		assessment.getField().add(newField(CanonicalNames.PopulationSize, 
			new RangePrimitiveField("range", null, "999")));
		
		ExpertResult result = run(assessment);
		
		Assert.assertEquals(result.getAbbreviatedCategory(), "CR");
		Assert.assertEquals(result.getCriteriaString(), "B1ab(ii)+2ab(ii)");
	}
	
	@Test
	public void badC2B() {
		Assessment assessment = new Assessment();
		assessment.getField().add(newField(CanonicalNames.PopulationSize, 
			new RangePrimitiveField("range", null, "230")));
		assessment.getField().add(newField(CanonicalNames.PopulationExtremeFluctuation, 
			new BooleanRangePrimitiveField("isFluctuating", null, "1")	
		));
		
		ExpertResult result = run(assessment);
		
		Assert.assertFalse("C2b".equals(result.getCriteriaString()));
	}
	
	@Test
	public void goodC2B() {
		Assessment assessment = new Assessment();
		assessment.getField().add(newField(CanonicalNames.PopulationSize, 
			new RangePrimitiveField("range", null, "230")));
		assessment.getField().add(newField(CanonicalNames.PopulationExtremeFluctuation, 
			new BooleanRangePrimitiveField("isFluctuating", null, "1")	
		));
		assessment.getField().add(newField(CanonicalNames.PopulationContinuingDecline,
			new BooleanRangePrimitiveField("isDeclining", null, "1")
		));
		
		ExpertResult result = run(assessment);
		Assert.assertTrue("C2b".equals(result.getCriteriaString()));
	}
	
	@Test
	public void goodD() {
		Assessment assessment = new Assessment();
		assessment.getField().add(newField(CanonicalNames.PopulationSize, 
			new RangePrimitiveField("range", null, "45")
		));
		
		String[] cat = new String[] { "CR", "EN", "VU", "LC", "DD" };
		String[] crit = new String[] { "D", "D", "D1", "", "" };
		
		for (int i = 0; i < cat.length; i++) {
			if (i == 1)
				assessment.getField(CanonicalNames.PopulationSize).getPrimitiveField("range").setRawValue("245");
			else if (i == 2)
				assessment.getField(CanonicalNames.PopulationSize).getPrimitiveField("range").setRawValue("545");
			else if (i == 3)
				assessment.getField(CanonicalNames.PopulationSize).getPrimitiveField("range").setRawValue("5000");
			else if (i == 4)
				assessment.getField().remove(assessment.getField(CanonicalNames.PopulationSize));
			
			ExpertResult result = run(assessment);
			Assert.assertEquals(cat[i], result.getAbbreviatedCategory());
			Assert.assertEquals(crit[i], result.getCriteriaString());
		}
	}
	
	@Test
	public void goodE() {
		Assessment assessment = new Assessment();
		assessment.getField().add(newField(CanonicalNames.ExtinctionProbabilityGenerations3,
			new RangePrimitiveField("range", null, "55")
		));
		assessment.getField().add(newField(CanonicalNames.ExtinctionProbabilityGenerations5,
			new RangePrimitiveField("range", null, "25")
		));
		assessment.getField().add(newField(CanonicalNames.ExtinctionProbabilityYears100,
			new RangePrimitiveField("range", null, "15")
		));
		
		String[] cat = new String[] { "CR", "EN", "VU", "LC", "DD" };
		String[] crit = new String[] { "E", "E", "E", "", "" };
		
		for (int i = 0; i < crit.length; i++) {
			if (i == 1) 
				assessment.getField().remove(assessment.getField(CanonicalNames.ExtinctionProbabilityGenerations3));
			else if (i == 2)
				assessment.getField().remove(assessment.getField(CanonicalNames.ExtinctionProbabilityGenerations5));
			else if (i == 3)
				assessment.getField(CanonicalNames.ExtinctionProbabilityYears100).getPrimitiveField("range").setRawValue("5");
			else if (i == 4)
				assessment.getField().remove(assessment.getField(CanonicalNames.ExtinctionProbabilityYears100));
			
			ExpertResult result = run(assessment);
			
			Assert.assertEquals(cat[i], result.getAbbreviatedCategory());
			Assert.assertEquals(crit[i], result.getCriteriaString());
		}
	}
	
	private Field newField(String name, PrimitiveField<?>... prims) {
		Field field = new Field();
		field.setId(idGenerator.incrementAndGet());
		field.setName(name);
		
		for (PrimitiveField<?> prim : prims) {
			prim.setField(field);
			field.addPrimitiveField(prim);
		}
		
		return field;
	}
	
	private ForeignKeyListPrimitiveField newFKList(String name, int... values) {
		List<Integer> list = new ArrayList<Integer>();
		for (int value : values)
			list.add(value);
		
		ForeignKeyListPrimitiveField prim = new ForeignKeyListPrimitiveField();
		prim.setName(name);
		prim.setValue(list);
		
		return prim;
		
	}
	
	private ExpertResult run(Assessment assessment) {
		System.out.println("----- Begin Test -----");
		
		FuzzyExpImpl impl = new FuzzyExpImpl();
		ExpertResult result = impl.doAnalysis(assessment);
		Debug.println("Result: {0} -> {1}", result.getAbbreviatedCategory(), result.getCriteriaString());
		Debug.println("CR: {0} -> {1}", result.getCriteriaCR(), result.getCriteriaCR().getCriteria());
		Debug.println("EN: {0} -> {1}", result.getCriteriaEN(), result.getCriteriaEN().getCriteria());
		Debug.println("VU: {0} -> {1}", result.getCriteriaVU(), result.getCriteriaVU().getCriteria());
		
		System.out.println("----- Done Test -----");
		
		return result;
	}
	
	private Assessment getAssessment(String name) {
		Document base = Files.getXML(name + ".xml");
		
		NativeDocument document = new JavaNativeDocument();
		document.parse(BaseDocumentUtils.impl.serializeDocumentToString(base, true, false));
		
		return Assessment.fromXML(document);
	}

}
