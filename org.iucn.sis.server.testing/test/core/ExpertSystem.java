package core;

import org.iucn.sis.shared.api.criteriacalculator.ExpertResult;
import org.iucn.sis.shared.api.criteriacalculator.FuzzyExpImpl;
import org.iucn.sis.shared.api.debug.Debug;
import org.iucn.sis.shared.api.models.Assessment;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;

import com.solertium.lwxml.java.JavaNativeDocument;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.util.BaseDocumentUtils;

import files.Files;

public class ExpertSystem extends BasicTest {
	
	@Test
	public void tryIt() {
		Assessment assessment = getAssessment("A1854815");
		
		FuzzyExpImpl impl = new FuzzyExpImpl();
		impl.VERBOSE = true;
		
		run(assessment, impl);
	}
	
	@Test
	public void badC2B() {
		Assessment assessment = getAssessment("BadC2B");
		
		FuzzyExpImpl impl = new FuzzyExpImpl();
		impl.VERBOSE = true;
		
		ExpertResult result = run(assessment, impl);
		Assert.assertFalse("C2b".equals(result.getCriteriaString()));
	}
	
	@Test
	public void goodC2B() {
		Assessment assessment = getAssessment("GoodC2B");
		
		FuzzyExpImpl impl = new FuzzyExpImpl();
		impl.VERBOSE = true;
		
		ExpertResult result = run(assessment, impl);
		Assert.assertTrue("C2b".equals(result.getCriteriaString()));
	}
	
	private ExpertResult run(Assessment assessment, FuzzyExpImpl impl) {
		ExpertResult result = impl.doAnalysis(assessment);
		Debug.println("Result: " + result.getCriteriaString());
		Debug.println("CR: " + result.getCriteriaStringCR());
		Debug.println("EN: " + result.getCriteriaStringEN());
		Debug.println("VU: " + result.getCriteriaStringVU());
		
		return result;
	}
	
	private Assessment getAssessment(String name) {
		Document base = Files.getXML(name + ".xml");
		
		NativeDocument document = new JavaNativeDocument();
		document.parse(BaseDocumentUtils.impl.serializeDocumentToString(base, true, false));
		
		return Assessment.fromXML(document);
	}

}
