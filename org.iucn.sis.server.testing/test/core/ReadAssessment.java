package core;

import junit.framework.Assert;

import org.iucn.sis.shared.api.models.Assessment;
import org.junit.Test;

import com.solertium.lwxml.java.JavaNativeDocument;
import com.solertium.lwxml.shared.NativeDocument;

public class ReadAssessment extends BasicTest {
	
	@Test
	public void read() {
		NativeDocument doc = new JavaNativeDocument();
		doc.parse(getXML());
	
		Assessment assessment = Assessment.fromXML(doc);
		
		Assert.assertTrue(assessment.getField().size() == 3);
	}
	
	private String getXML() {
		return "<assessment id=\"0\" internalID=\"null\"><source><![CDATA[null]]></source>" +
		"<sourceDate><![CDATA[null]]></sourceDate><schema>" +
		"<![CDATA[org.iucn.sis.server.schemas.redlist]]></schema>" +
		"<taxon id=\"100114\" name=\"velox\" fullname=\"Vulpes velox\"/><assessment_type id=\"2\"/>" +
		"<fields><RegionExpertQuestions version=\"2\" id=\"0\"> <answers id=\"0\" type=\"TextPrimitiveField\"><![CDATA[Downgrade,1,1,3,3,1]]></answers> </RegionExpertQuestions> <Threats version=\"2\" id=\"0\"> <subfields> <ThreatsSubfield version=\"2\" id=\"0\"> <ThreatsLookup id=\"0\" type=\"ForeignKeyPrimitiveField\" tableID=\"null\"><![CDATA[4]]></ThreatsLookup> <score id=\"0\" type=\"StringPrimitiveField\"><![CDATA[(Not specified)]]></score> </ThreatsSubfield> </subfields> </Threats> <RegionInformation version=\"2\" id=\"0\"> <regions id=\"0\" type=\"ForeignKeyListPrimitiveField\" tableID=\"region\"><![CDATA[5]]></regions> </RegionInformation> </fields></assessment>";
	}

}
