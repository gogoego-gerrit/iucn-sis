import java.util.Properties;
import java.util.Random;

import javax.naming.NamingException;

import org.iucn.sis.server.api.fields.FieldSchemaGenerator;
import org.iucn.sis.shared.api.criteriacalculator.ExpertResult;
import org.iucn.sis.shared.api.criteriacalculator.Factors;
import org.iucn.sis.shared.api.criteriacalculator.FuzzyExpImpl;
import org.iucn.sis.shared.api.debug.Debug;
import org.iucn.sis.shared.api.debug.Debugger;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.Field;
import org.iucn.sis.shared.api.models.PrimitiveField;
import org.iucn.sis.shared.api.models.primitivefields.BooleanRangePrimitiveField;
import org.iucn.sis.shared.api.models.primitivefields.ForeignKeyListPrimitiveField;
import org.iucn.sis.shared.api.models.primitivefields.RangePrimitiveField;

import com.solertium.db.DBSessionFactory;


public class ExpertSystemTesting extends QuickTest {
	
	public static void main(String[] args) {
		final ExpertSystemTesting testing = new ExpertSystemTesting();
		Debug.setInstance(testing);
		
		final FieldSchemaGenerator generator = createGenerator();
		
		Debug.println("--- Testing standard assessment ---");
		testing.test(createAssessment(generator, "0"));
		
		Debug.println("--- Testing standard assessment ---");
		testing.test(createAssessment(generator, "1"));
		
		Debug.println("--- Testing randomized assessment (100) ---");
		testing.test(createRandomRangeAssessment(generator, 100));
		
		Debug.println("--- Testing randomized assessment (1000) ---");
		testing.test(createRandomRangeAssessment(generator, 1000));
		
		Debug.println("--- Testing randomized assessment (10000) ---");
		testing.test(createRandomRangeAssessment(generator, 10000));
	}
	
	public void test(Assessment assessment) {
		FuzzyExpImpl.VERBOSE = true;
		FuzzyExpImpl expert = new FuzzyExpImpl();
		ExpertResult result = expert.doAnalysis(assessment);
		
		Debug.println("Result is {0}", result.getResult());
		Debug.println("Criteria string: {0}", result.getCriteriaString());	
	}
	
	public static FieldSchemaGenerator createGenerator() {
		Properties properties = new Properties();
		properties.setProperty("dbsession.sis_lookups.uri","jdbc:h2:file:/var/sis/databases/sis_lookups");
		properties.setProperty("dbsession.sis_lookups.driver","org.h2.Driver");
		properties.setProperty("dbsession.sis_lookups.user","sa");
		properties.setProperty("dbsession.sis_lookups.password","");
		
		final FieldSchemaGenerator generator;
		
		try {
			DBSessionFactory.registerDataSources(properties);
			generator = new FieldSchemaGenerator();
		} catch (NamingException e) {
			throw new RuntimeException(e);
		}
		
		return generator;
	}
	
	public static Assessment createRandomRangeAssessment(FieldSchemaGenerator generator, double multiply) {
		Assessment assessment = new Assessment();
		
		int fauxID = 0;
		for (String factor : Factors.factors) {
			Field field;
			try {
				field = generator.getField(factor);
				field.setId(fauxID++);
			} catch (Exception e) {
				throw new RuntimeException("Field " + factor + " was not defined.");
			}
			for (PrimitiveField<?> prim : field.getPrimitiveField()) {
				if (prim instanceof RangePrimitiveField) {
					Random r = new Random();
					double a = r.nextDouble() * multiply;
					double b = r.nextDouble() * multiply;
					String value;
					if (a >= b)
						value = b + "," + a;
					else
						value = a + "," + b;
					((RangePrimitiveField) prim).setValue(value);
					Debug.println("Setting range for {0} to {1}", factor, value);
					break;
				}
				else if (prim instanceof BooleanRangePrimitiveField) {
					Random r = new Random();
					int next = r.nextInt(2);
					String value;
					if (next == 0)
						value = "0";
					else if (next == 1)
						value = ".5";
					else
						value = "1";
							
					((BooleanRangePrimitiveField) prim).setValue(value);
					Debug.println("Setting booleanRange for {0}", factor);
					break;
				}
				else if (prim instanceof ForeignKeyListPrimitiveField) {
					prim.setRawValue("1,2");
					Debug.println("Setting foreign key for {0}", factor);
					break;
				}
			}
			assessment.getField().add(field);
		}
		return assessment;
	}
	
	public static Assessment createAssessment(FieldSchemaGenerator generator, String booleanRange) {
		Assessment assessment = new Assessment();
		
		int fauxID = 0;
		for (String factor : Factors.factors) {
			Field field;
			try {
				field = generator.getField(factor);
				field.setId(fauxID++);
			} catch (Exception e) {
				throw new RuntimeException("Field " + factor + " was not defined.");
			}
			for (PrimitiveField<?> prim : field.getPrimitiveField()) {
				if (prim instanceof RangePrimitiveField) {
					((RangePrimitiveField) prim).setValue("100,150");
					Debug.println("Setting range for {0}", factor);
					break;
				}
				else if (prim instanceof BooleanRangePrimitiveField) {
					((BooleanRangePrimitiveField) prim).setValue(booleanRange);
					Debug.println("Setting booleanRange for {0}", factor);
					break;
				}
				else if (prim instanceof ForeignKeyListPrimitiveField) {
					prim.setRawValue("1,2");
					Debug.println("Setting foreign key for {0}", factor);
					break;
				}
			}
			assessment.getField().add(field);
		}
		return assessment;
	}
	
}
