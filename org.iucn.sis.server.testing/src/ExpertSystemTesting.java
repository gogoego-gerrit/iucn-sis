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


public class ExpertSystemTesting implements Debugger {
	
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

	public void println(Throwable e) {
		writeOutput(e == null ? "null" : serializeThrowable(e));
	}
	
	@Override
	public void println(Object obj) {
		writeOutput(obj == null ? "null" : obj instanceof Throwable ? serializeThrowable((Throwable)obj) : obj.toString());
	}
	
	@Override
	public void println(String template, Object... args) {
		/*
		 * Since we can't use String.format style templating, which is 
		 * what GetOut uses, we will instead translate this format to 
		 * be Ext's format operation and use that instead.  Can spruce 
		 * this up later.
		 */
		
		int count = 0, limit = 50;
		String text = template;
		
		while (text.indexOf("%s") != -1 && count < limit)
			text = text.replaceFirst("%s", "{" + (count++) + "}");
		
		writeOutput(substitute(text, args));
	}
	
	private String substitute(String text, Object... params) {
		for (int i = 0; i < params.length; i++) {
			Object p = params[i];
			String toString;
			if (p == null)
				toString = "null";
			else if (p instanceof Throwable)
				toString = serializeThrowable((Throwable)p);
			else
				toString = p.toString();
			
			text = text.replaceAll("\\{" + i + "}", safeRegexReplacement(toString));
		}
		return text;
	}
	
	public static String serializeThrowable(Throwable e) {
		StringBuilder s = new StringBuilder();
		s.append(e + "\n");
        StackTraceElement[] trace = e.getStackTrace();
        for (int i=0; i < trace.length; i++)
            s.append("\tat " + trace[i] + "\n");

        Throwable ourCause = e.getCause();
        if (ourCause != null)
            printStackTraceAsCause(s, ourCause, trace);
        
        return s.toString();
	}
	
	private static void printStackTraceAsCause(StringBuilder s,
			Throwable caught, StackTraceElement[] causedTrace)
	{
		StackTraceElement[] trace = caught.getStackTrace();
		
		// Compute number of frames in common between this and caused
		int m = trace.length-1, n = causedTrace.length-1;
		while (m >= 0 && n >=0 && trace[m].equals(causedTrace[n])) {
			m--; n--;
		}
		
		int framesInCommon = trace.length - 1 - m;
		s.append("Caused by: " + caught + "\n");
		for (int i=0; i <= m; i++)
			s.append("\tat " + trace[i]);
		if (framesInCommon != 0)
			s.append("\t... " + framesInCommon + " more\n");
	
		// Recurse if we have a cause
		Throwable ourCause = caught.getCause();
		if (ourCause != null)
			printStackTraceAsCause(s, ourCause, trace);
	}
	
	private String safeRegexReplacement(String replacement) {
		if (replacement == null)
			return replacement;

		return replacement.replaceAll("\\\\", "\\\\\\\\").replaceAll("\\$", "\\\\\\$");
	}
	
	protected void writeOutput(String output) {
		System.out.println(output);
	}
	
}
