package core;

import org.iucn.sis.shared.api.criteriacalculator.ExpertResult;
import org.iucn.sis.shared.api.criteriacalculator.FuzzyExpImpl;
import org.iucn.sis.shared.api.debug.Debug;
import org.iucn.sis.shared.api.models.Assessment;
import org.junit.Test;
import org.w3c.dom.Document;

import com.solertium.lwxml.java.JavaNativeDocument;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.util.BaseDocumentUtils;

import files.Files;

public class ExpertSystem extends BasicTest {
	
	@Test
	public void run() {
		Assessment assessment = getAssessment();
		
		FuzzyExpImpl impl = new FuzzyExpImpl();
		impl.VERBOSE = true;
		
		ExpertResult result = impl.doAnalysis(assessment);
		Debug.println("Result: " + result.getCriteriaString());
		Debug.println("CR: " + result.getCriteriaStringCR());
		Debug.println("EN: " + result.getCriteriaStringEN());
		Debug.println("VU: " + result.getCriteriaStringVU());
	}
	
	private Assessment getAssessment() {
		Document base = Files.getXML("A1854815.xml");
		
		NativeDocument document = new JavaNativeDocument();
		document.parse(BaseDocumentUtils.impl.serializeDocumentToString(base, true, false));
		
		return Assessment.fromXML(document);
	}

}
