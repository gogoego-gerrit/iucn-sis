package core;

import org.iucn.sis.server.utils.AssessmentPersistence;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.Field;
import org.iucn.sis.shared.api.models.PrimitiveField;
import org.iucn.sis.shared.api.models.fields.ThreatsSubfield;
import org.iucn.sis.shared.api.models.primitivefields.StringPrimitiveField;
import org.iucn.sis.shared.api.utils.CanonicalNames;
import org.junit.Test;

public class SaveAssessmentTest extends BasicTest {
	
	@Test
	public void testSaveNew() throws Exception {
		Assessment target = new Assessment();
		
		AssessmentPersistence saver = new AssessmentPersistence(null, target);
		saver.sink(createBrandNewAssessmentWithFields());
		
		//System.out.println(toXML(target));
	}
	
	@Test
	public void testSaveExisting() throws Exception {
		Assessment target = createExistingAssessmentWithFields();
		
		AssessmentPersistence saver = new AssessmentPersistence(null, target);
		saver.sink(createExistingAssessmentWithFields());
		
		//System.out.println(toXML(target));
	}
	
	@Test
	public void testSaveExistingAsDelete() throws Exception {
		Assessment source = createExistingAssessmentWithFields();
		Assessment target = createExistingAssessmentWithFields(false);
		
		AssessmentPersistence saver = new AssessmentPersistence(null, target);
		saver.sink(source);
		
		System.out.println(toXML(target));
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
		return createExistingAssessmentWithFields(false);
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
		
		{ 
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
