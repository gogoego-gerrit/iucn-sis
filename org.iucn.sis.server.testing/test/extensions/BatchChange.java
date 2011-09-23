package extensions;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.hibernate.Session;
import org.iucn.sis.server.extensions.batchchanges.BatchAssessmentChanger;
import org.iucn.sis.server.extensions.batchchanges.BatchAssessmentChanger.BatchChangeMode;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.Field;
import org.iucn.sis.shared.api.models.PrimitiveField;
import org.iucn.sis.shared.api.models.primitivefields.IntegerPrimitiveField;
import org.iucn.sis.shared.api.models.primitivefields.StringPrimitiveField;
import org.iucn.sis.shared.api.utils.CanonicalNames;
import org.junit.Test;

public class BatchChange {
	
	@Test
	public void overwriteOnEmptyAssessment() {
		List<String> names = new ArrayList<String>();
		names.add("field");
		
		Assessment template = getTemplateAssessment("field");
		
		//it's empty :)
		Assessment target = new Assessment();
		
		boolean hasChanges = BatchAssessmentChanger.changeAssessment(getSession(), target, template, BatchChangeMode.OVERWRITE, names);
		
		printResults(target);
		
		Assert.assertTrue(hasChanges);
		Assert.assertTrue(!target.getField().isEmpty());
		Assert.assertTrue("cooked value".equals(target.getField("field").getPrimitiveField("value").getValue()));
	}
	
	@Test
	public void overwriteOnExistingAssessment() {
		List<String> names = new ArrayList<String>();
		names.add("field");
		
		Assessment template = getTemplateAssessment("field");
		
		Assessment target = new Assessment(); 
		{
			PrimitiveField<String> prim = new StringPrimitiveField();
			prim.setName("value");
			prim.setRawValue("raw value");
			
			Field field = new Field();
			field.setId(2);
			field.setName("field");
			field.getPrimitiveField().add(prim);
			
			target.getField().add(field);
			
			Assert.assertTrue(!target.getField().isEmpty());
			Assert.assertTrue("raw value".equals(target.getField("field").getPrimitiveField("value").getValue()));
		}
		
		boolean hasChanges = BatchAssessmentChanger.changeAssessment(getSession(), target, template, BatchChangeMode.OVERWRITE, names);
		
		printResults(target);
		
		Assert.assertTrue(hasChanges);
		Assert.assertTrue(!target.getField().isEmpty());
		Assert.assertTrue("cooked value".equals(target.getField("field").getPrimitiveField("value").getValue()));
	}
	
	@Test
	public void appendOnEmptyAssessment() {
		List<String> names = new ArrayList<String>();
		names.add("Documentation");
		
		Assessment template = getTemplateAssessment("Documentation");
		
		Assessment target = new Assessment();
		
		boolean hasChanges = BatchAssessmentChanger.changeAssessment(getSession(), target, template, BatchChangeMode.APPEND, names);
		
		printResults(target);
		
		Assert.assertTrue(hasChanges);
		Assert.assertTrue(!target.getField().isEmpty());
		Assert.assertTrue("cooked value".equals(target.getField("Documentation").getPrimitiveField("value").getValue()));
	}
	
	@Test
	public void appendOnExistingAssessment() {
		List<String> names = new ArrayList<String>();
		names.add("Documentation");
		
		Assessment template = getTemplateAssessment("Documentation");
		
		Assessment target = new Assessment(); 
		{
			PrimitiveField<String> prim = new StringPrimitiveField();
			prim.setName("value");
			prim.setRawValue("raw value");
			
			Field field = new Field();
			field.setId(2);
			field.setName("Documentation");
			field.getPrimitiveField().add(prim);
			
			target.getField().add(field);
			
			Assert.assertTrue(!target.getField().isEmpty());
			Assert.assertTrue("raw value".equals(target.getField("Documentation").getPrimitiveField("value").getValue()));
		}
		
		boolean hasChanges = BatchAssessmentChanger.changeAssessment(getSession(), target, template, BatchChangeMode.APPEND, names);
		
		printResults(target);
		
		Assert.assertTrue(hasChanges);
		Assert.assertTrue(!target.getField().isEmpty());
		Assert.assertTrue("raw value<br/><br/>cooked value".equals(target.getField("Documentation").getPrimitiveField("value").getValue()));
	}
	
	@Test
	public void doNotOverwriteExistingAssessment() {
		List<String> names = new ArrayList<String>();
		names.add("field");
		
		Assessment template = getTemplateAssessment("field");
		
		Assessment target = new Assessment(); 
		{
			PrimitiveField<String> prim = new StringPrimitiveField();
			prim.setName("value");
			prim.setRawValue("raw value");
			
			Field field = new Field();
			field.setId(2);
			field.setName("field");
			field.getPrimitiveField().add(prim);
			
			target.getField().add(field);
			
			Assert.assertTrue(!target.getField().isEmpty());
			Assert.assertTrue("raw value".equals(target.getField("field").getPrimitiveField("value").getValue()));
		}
		
		boolean hasChanges = BatchAssessmentChanger.changeAssessment(getSession(), target, template, BatchChangeMode.OVERWRITE_IF_BLANK, names);
		
		printResults(target);
		
		Assert.assertTrue(!hasChanges);
		Assert.assertTrue(!target.getField().isEmpty());
		Assert.assertTrue("raw value".equals(target.getField("field").getPrimitiveField("value").getValue()));
	}
	
	@Test
	public void overwriteBlankAssessment() {
		List<String> names = new ArrayList<String>();
		names.add("field");
		
		Assessment template = getTemplateAssessment("field");
		
		Assessment target = new Assessment();
		
		boolean hasChanges = BatchAssessmentChanger.changeAssessment(getSession(),target, template, BatchChangeMode.OVERWRITE_IF_BLANK, names);
		
		printResults(target);
		
		Assert.assertTrue(hasChanges);
		Assert.assertTrue(!target.getField().isEmpty());
		Assert.assertTrue("cooked value".equals(target.getField("field").getPrimitiveField("value").getValue()));
	}
	
	@Test
	public void doAppendClassificationScheme() {
		List<String> names = new ArrayList<String>();
		names.add(CanonicalNames.CountryOccurrence);
		
		Assessment template = getClassSchemeAssessment(1, CanonicalNames.CountryOccurrence, 1);
		
		Assessment target = getClassSchemeAssessment(2, CanonicalNames.CountryOccurrence, 2, 3);
		
		Assert.assertTrue(template.getField(CanonicalNames.CountryOccurrence).getFields().size() == 1);
		Assert.assertTrue(target.getField(CanonicalNames.CountryOccurrence).getFields().size() == 2);
		
		boolean hasChanges = BatchAssessmentChanger.changeAssessment(getSession(),target, template, BatchChangeMode.APPEND, names);
		
		Assert.assertTrue(hasChanges);
		Assert.assertTrue(!target.getField(CanonicalNames.CountryOccurrence).getFields().isEmpty());
		Assert.assertTrue(target.getField(CanonicalNames.CountryOccurrence).getFields().size() == 3);
	}
	
	@Test
	public void doAppendClassificationSchemeNoOp() {
		List<String> names = new ArrayList<String>();
		names.add(CanonicalNames.CountryOccurrence);
		
		Assessment template = getClassSchemeAssessment(1, CanonicalNames.CountryOccurrence, 1, 2);
		
		Assessment target = getClassSchemeAssessment(2, CanonicalNames.CountryOccurrence, 1, 2, 3);
		
		Assert.assertTrue(template.getField(CanonicalNames.CountryOccurrence).getFields().size() == 2);
		Assert.assertTrue(target.getField(CanonicalNames.CountryOccurrence).getFields().size() == 3);
		
		boolean hasChanges = BatchAssessmentChanger.changeAssessment(getSession(),target, template, BatchChangeMode.APPEND, names);
		
		Assert.assertTrue(hasChanges);
		Assert.assertTrue(!target.getField(CanonicalNames.CountryOccurrence).getFields().isEmpty());
		Assert.assertTrue(target.getField(CanonicalNames.CountryOccurrence).getFields().size() == 3);
	}
	
	private void printResults(Assessment target) {
		if (target.getField("field") == null)
			System.out.println("After overwrite: no fields.");
		else
			System.out.println("After overwrite: " + target.getField("field").getPrimitiveField("value").getValue());
	}
	
	private Assessment getClassSchemeAssessment(int fieldID, String fieldName, Integer... values) {
		Assessment assessment = new Assessment();
		
		Field field = new Field(fieldName, assessment);
		field.setId(fieldID);
		
		for (Integer value : values){
			PrimitiveField<Integer> prim = new IntegerPrimitiveField();
			prim.setName(fieldName + "Lookup");
			prim.setValue(value);
			
			Field subField = new Field(fieldName + "Subfield", assessment);
			subField.setId(field.getFields().size() + 1);
			subField.getPrimitiveField().add(prim);
			
			prim.setField(subField);
		
			subField.setParent(field);
			
			field.getFields().add(subField);
		}
			
		assessment.getField().add(field);
		
		return assessment;
	}
	
	private Assessment getTemplateAssessment(String fieldName) {
		Assessment template = new Assessment(); 
		
		PrimitiveField<String> prim = new StringPrimitiveField();
		prim.setName("value");
		prim.setRawValue("cooked value");
		
		Field field = new Field();
		field.setId(1);
		field.setName(fieldName);
		field.getPrimitiveField().add(prim);
		template.getField().add(field);
		
		Assert.assertTrue(!template.getField().isEmpty());
		Assert.assertTrue("cooked value".equals(template.getField(fieldName).getPrimitiveField("value").getValue()));
		
		return template;
	}

	private Session getSession() {
		return null;
	}
	
}
