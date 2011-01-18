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
		
		Assessment template = new Assessment(); 
		{
			PrimitiveField<String> prim = new StringPrimitiveField();
			prim.setName("value");
			prim.setRawValue("cooked value");
			
			Field field = new Field();
			field.setId(1);
			field.setName("field");
			field.getPrimitiveField().add(prim);
			template.getField().add(field);
			
			Assert.assertTrue(!template.getField().isEmpty());
			Assert.assertTrue("cooked value".equals(template.getField("field").getPrimitiveField("value").getValue()));
		}
		
		Assessment target = new Assessment();
		{
			//it's empty :)
		}
		
		BatchAssessmentChanger.changeAssessment(target, template, true, false, names);
		
		System.out.println("After overwrite: " + target.getField("field").getPrimitiveField("value").getValue());
		
		Assert.assertTrue(!target.getField().isEmpty());
		Assert.assertTrue("cooked value".equals(target.getField("field").getPrimitiveField("value").getValue()));
	}
	
	@Test
	public void overwriteOnExistingAssessment() {
		List<String> names = new ArrayList<String>();
		names.add("field");
		
		Assessment template = new Assessment(); 
		{
			PrimitiveField<String> prim = new StringPrimitiveField();
			prim.setName("value");
			prim.setRawValue("cooked value");
			
			Field field = new Field();
			field.setId(1);
			field.setName("field");
			field.getPrimitiveField().add(prim);
			template.getField().add(field);
			
			Assert.assertTrue(!template.getField().isEmpty());
			Assert.assertTrue("cooked value".equals(template.getField("field").getPrimitiveField("value").getValue()));
		}
		
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
		
		BatchAssessmentChanger.changeAssessment(target, template, true, false, names);
		
		System.out.println("After overwrite: " + target.getField("field").getPrimitiveField("value").getValue());
		
		Assert.assertTrue(!target.getField().isEmpty());
		Assert.assertTrue("cooked value".equals(target.getField("field").getPrimitiveField("value").getValue()));
	}
	
	@Test
	public void appendOnExistingAssessment() {
		List<String> names = new ArrayList<String>();
		names.add("Documentation");
		
		Assessment template = new Assessment(); 
		{
			PrimitiveField<String> prim = new StringPrimitiveField();
			prim.setName("value");
			prim.setRawValue("cooked value");
			
			Field field = new Field();
			field.setId(1);
			field.setName("Documentation");
			field.getPrimitiveField().add(prim);
			template.getField().add(field);
			
			Assert.assertTrue(!template.getField().isEmpty());
			Assert.assertTrue("cooked value".equals(template.getField("Documentation").getPrimitiveField("value").getValue()));
		}
		
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
		
		BatchAssessmentChanger.changeAssessment(target, template, false, true, names);
		
		System.out.println("After overwrite: " + target.getField("Documentation").getPrimitiveField("value").getValue());
		
		Assert.assertTrue(!target.getField().isEmpty());
		Assert.assertTrue("raw value\r\n\r\ncooked value".equals(target.getField("Documentation").getPrimitiveField("value").getValue()));
	}

}
