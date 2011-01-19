package extensions;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.iucn.sis.server.extensions.batchchanges.BatchAssessmentChanger;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.Field;
import org.iucn.sis.shared.api.models.PrimitiveField;
import org.iucn.sis.shared.api.models.primitivefields.StringPrimitiveField;
import org.junit.Test;

public class BatchChange {
	
	@Test
	public void overwriteOnEmptyAssessment() {
		List<String> names = new ArrayList<String>();
		names.add("field");
		
		Assessment template = getTemplateAssessment("field");
		
		//it's empty :)
		Assessment target = new Assessment();
		
		boolean hasChanges = BatchAssessmentChanger.changeAssessment(target, template, true, false, names);
		
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
		
		boolean hasChanges = BatchAssessmentChanger.changeAssessment(target, template, true, false, names);
		
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
		
		boolean hasChanges = BatchAssessmentChanger.changeAssessment(target, template, false, true, names);
		
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
		
		boolean hasChanges = BatchAssessmentChanger.changeAssessment(target, template, false, true, names);
		
		printResults(target);
		
		Assert.assertTrue(hasChanges);
		Assert.assertTrue(!target.getField().isEmpty());
		Assert.assertTrue("raw value\r\n\r\ncooked value".equals(target.getField("Documentation").getPrimitiveField("value").getValue()));
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
		
		boolean hasChanges = BatchAssessmentChanger.changeAssessment(target, template, false, false, names);
		
		printResults(target);
		
		Assert.assertTrue(!hasChanges);
		Assert.assertTrue(!target.getField().isEmpty());
		Assert.assertTrue("raw value".equals(target.getField("field").getPrimitiveField("value").getValue()));
	}
	
	private void printResults(Assessment target) {
		System.out.println("After overwrite: " + target.getField("field").getPrimitiveField("value").getValue());
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

}
