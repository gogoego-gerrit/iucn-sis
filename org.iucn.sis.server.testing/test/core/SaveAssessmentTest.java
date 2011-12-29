package core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Assert;

import org.iucn.sis.server.utils.AssessmentPersistence;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.AssessmentChange;
import org.iucn.sis.shared.api.models.Field;
import org.iucn.sis.shared.api.models.PrimitiveField;
import org.iucn.sis.shared.api.models.fields.ThreatsSubfield;
import org.iucn.sis.shared.api.models.primitivefields.StringPrimitiveField;
import org.iucn.sis.shared.api.utils.CanonicalNames;
import org.junit.Test;

import com.solertium.util.events.ComplexListener;

public class SaveAssessmentTest extends BasicTest {
	
	@Test
	public void testSaveNew() throws Exception {
		Assessment target = new Assessment();
		
		Assert.assertTrue(target.getField().isEmpty());
		
		AssessmentPersistence saver = new AssessmentPersistence(null, target);
		saver.sink(createBrandNewAssessmentWithFields());
		
		Assert.assertFalse(target.getField().isEmpty());
		
		Field a = target.getField("a");
		
		Assert.assertNotNull(a);
		Assert.assertNotNull(a.getPrimitiveField("value"));
		Assert.assertEquals("saved value", a.getPrimitiveField("value").getRawValue());
		
		List<AssessmentChange> changes = saver.getChangeSet();
		
		Assert.assertFalse(changes.isEmpty());
		Assert.assertEquals("a", changes.get(0).getFieldName());
		Assert.assertEquals(AssessmentChange.ADD, changes.get(0).getType());
	}
	
	@Test
	public void testSaveExisting() throws Exception {
		Assessment source = createExistingAssessmentWithFields();
		source.getField("a").getPrimitiveField("value").setRawValue("unsaved value");
		
		Assessment target = createExistingAssessmentWithFields();
		
		Assert.assertEquals("saved value", target.getField("a").getPrimitiveField("value").getRawValue());
		
		AssessmentPersistence saver = new AssessmentPersistence(null, target);
		saver.sink(source);
		
		Assert.assertEquals("unsaved value", target.getField("a").getPrimitiveField("value").getRawValue());
		
		List<AssessmentChange> changes = saver.getChangeSet();
		
		Assert.assertFalse(changes.isEmpty());
		Assert.assertEquals(2, changes.size());
		
		for (AssessmentChange change : changes)
			Assert.assertEquals(AssessmentChange.EDIT, change.getType());
	}
	
	@Test
	public void testSaveExistingAsDeletePrim() throws Exception {
		Assessment source = createExistingAssessmentWithFields();
		Assessment target = createExistingAssessmentWithFields();
		
		Assert.assertEquals(2, source.getField("b").getPrimitiveField().size());
		
		PrimitiveField prim = source.getField("b").getPrimitiveField("value");
		Assert.assertNotNull(prim);
		Assert.assertEquals("saved value", prim.getRawValue());
		
		source.getField("b").getPrimitiveField().remove(prim);
		
		Assert.assertEquals(1, source.getField("b").getPrimitiveField().size());
		
		final Map<String, PrimitiveField<?>> deletedPrims = new HashMap<String, PrimitiveField<?>>();
		
		AssessmentPersistence saver = new AssessmentPersistence(null, target);
		saver.setDeletePrimitiveFieldListener(new ComplexListener<PrimitiveField<?>>() {
			public void handleEvent(PrimitiveField<?> eventData) {
				deletedPrims.put(eventData.getName(), eventData);
			}
		});
		saver.sink(source);
		
		//System.out.println(toXML(target));
		
		Assert.assertFalse(deletedPrims.isEmpty());
		Assert.assertNotNull(deletedPrims.get("value"));
		
		Assert.assertNotNull(target.getField("b").getPrimitiveField("another"));
		
		Assert.assertEquals(2, saver.getChangeSet().size());
		for (AssessmentChange change : saver.getChangeSet())
			Assert.assertEquals(AssessmentChange.EDIT, change.getType());
	}
	
	@Test
	public void testSaveExistingAsDeleteField() throws Exception {
		Assessment source = createExistingAssessmentWithFields();
		Assessment target = createExistingAssessmentWithFields();
		
		Assert.assertEquals(2, source.getField().size());
		
		source.getField().remove(source.getField("b"));
		
		Assert.assertEquals(1, source.getField().size());
		
		final Map<String, Field> deletedFields = new HashMap<String, Field>();
		
		AssessmentPersistence saver = new AssessmentPersistence(null, target);
		saver.setDeleteFieldListener(new ComplexListener<Field>() {
			public void handleEvent(Field eventData) {
				deletedFields.put(eventData.getName(), eventData);
			}
		});
		saver.sink(source);
		
		//System.out.println(toXML(target));
		
		Assert.assertFalse(deletedFields.isEmpty());
		Assert.assertNotNull(deletedFields.get("b"));
		
		Assert.assertNotNull(target.getField("a"));
	}
	
	@Test
	public void testSaveClassScheme() throws Exception {
		Assessment source = new Assessment();
		Field threat = new Field(CanonicalNames.Threats, source);
		threat.setId(10);
		for (int i = 1; i < 4; i++) {
			Field f = new Field(threat.getName()+"Subfield", null);
			//f.setId(i);
			ThreatsSubfield sub = new ThreatsSubfield(f);
			sub.setThreat(i);
			sub.setScope(1);
			sub.setTiming(1);
			sub.setSeverity(1);
			threat.getFields().add(f);
		}
		source.getField().add(threat);
		
		Assessment target = new Assessment();
		Field threat2 = new Field(CanonicalNames.Threats, source);
		threat2.setId(10);
		target.getField().add(threat2);
		
		AssessmentPersistence saver = new AssessmentPersistence(null, target);
		saver.sink(source);
		
		System.out.println(toXML(target));
	}
	
	private String toXML(Assessment assessment) {
		StringBuilder out = new StringBuilder();
		out.append("<assessment id=\"" + assessment.getId() + "\">\r\n");
		out.append("<fields>\r\n");
		for (Field field : assessment.getField())
			out.append(field.toXML() + "\r\n");
		out.append("</fields>\r\n");
		out.append("</assessment>");
		
		return out.toString();
	}
	
	private Assessment createBrandNewAssessmentWithFields() {
		Assessment assessment = new Assessment();
		
		Field a = new Field("a", assessment); {
			a.getPrimitiveField().add(new StringPrimitiveField("value", a, "saved value"));
			a.getPrimitiveField().add(new StringPrimitiveField("another", a, "saved value"));
		}
		
		assessment.getField().add(a);
		
		return assessment;
	}
	
	private Assessment createExistingAssessmentWithFields() {
		return createExistingAssessmentWithFields(true);
	}
	
	private Assessment createExistingAssessmentWithFields(boolean fieldB) {
		int index = 1;
		
		Assessment assessment = new Assessment();
		
		{
			Field a = new Field("a", assessment); 
			a.setId(1);
			a.getPrimitiveField().add(new StringPrimitiveField("value", a, "saved value"));
			a.getPrimitiveField().add(new StringPrimitiveField("another", a, "saved value"));
			
			
			for (PrimitiveField f : a.getPrimitiveField())
				f.setId(index++);
			
			assessment.getField().add(a);
		}
		
		if (fieldB) { 
			Field b = new Field("b", assessment);
			b.setId(2);
			b.getPrimitiveField().add(new StringPrimitiveField("value", b, "saved value"));
			b.getPrimitiveField().add(new StringPrimitiveField("another", b, "saved value"));
			
			for (PrimitiveField f : b.getPrimitiveField())
				f.setId(index++);
			
			assessment.getField().add(b);
		}
		
		
		
		return assessment;
	}

}
