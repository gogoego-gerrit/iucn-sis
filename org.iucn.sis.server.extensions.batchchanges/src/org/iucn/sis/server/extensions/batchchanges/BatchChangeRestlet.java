package org.iucn.sis.server.extensions.batchchanges;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;
import org.iucn.sis.server.api.filters.AssessmentFilterHelper;
import org.iucn.sis.server.api.io.AssessmentIO;
import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
import org.iucn.sis.server.api.restlets.BaseServiceRestlet;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.AssessmentFilter;
import org.iucn.sis.shared.api.models.User;
import org.restlet.Context;
import org.restlet.data.CharacterSet;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;

import com.solertium.lwxml.java.JavaNativeDocument;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.NativeElement;
import com.solertium.lwxml.shared.NativeNodeList;

public class BatchChangeRestlet extends BaseServiceRestlet {
	
	public BatchChangeRestlet(Context context) {
		super(context);
	}

	@Override
	public void definePaths() {
		paths.add("/batchChange");
	}
	
	@Override
	public void handlePost(Representation entity, Request request, Response response, Session session) throws ResourceException {
		final String xml;
		try {
			xml = request.getEntity().getText();
		} catch (Exception e) {
			throw new ResourceException(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY, e);
			
		}

		AssessmentIO assessmentIO = new AssessmentIO(session);
		User user = getUser(request, session);
		
		NativeDocument dataDoc = new JavaNativeDocument();
		dataDoc.parse(xml);

		NativeElement assessmentTag = dataDoc.getDocumentElement().getElementByTagName("assessment");
		NativeElement taxaTag = dataDoc.getDocumentElement().getElementByTagName("taxa");
		NativeElement filterTag = dataDoc.getDocumentElement().getElementByTagName(AssessmentFilter.ROOT_TAG);
		NativeElement appendTag = dataDoc.getDocumentElement().getElementByTagName("append");
		NativeElement overwriteTag = dataDoc.getDocumentElement().getElementByTagName("overwrite");
		NativeNodeList fieldNodes = dataDoc.getDocumentElement().getElementsByTagName("fields").elementAt(0).getElementsByTagName("field");
		
		boolean append = Boolean.parseBoolean(appendTag.getText());
		boolean overwrite = Boolean.parseBoolean(overwriteTag.getText());
		
		Assessment template = assessmentIO.
			getAssessment(Integer.valueOf(assessmentTag.getTextContent()));
		
		AssessmentFilter filter = AssessmentFilter.fromXML(filterTag);
		AssessmentFilterHelper helper = new AssessmentFilterHelper(session, filter);
			
		List<String> fieldNames = new ArrayList<String>();
		for (int i = 0; i < fieldNodes.getLength(); i++)
			fieldNames.add(fieldNodes.elementAt(i).getTextContent());

		StringBuilder returnXML = new StringBuilder("<changes>");
		List<Assessment> assessments = new ArrayList<Assessment>();
		
		for (String taxonID : taxaTag.getTextContent().split(",")) {
			for (Assessment asmToChange : helper.getAssessments(Integer.valueOf(taxonID))) {
				if (BatchAssessmentChanger.changeAssessment(
						asmToChange, template, overwrite, append,
						fieldNames)) {
					returnXML.append("<change id=\""
							+ asmToChange.getId() + "\">"
							+ asmToChange.getDisplayText()
							+ "</change>");
					assessments.add(asmToChange);
				}
			}
		}
		returnXML.append("</changes>");
			
		try {
			assessmentIO.saveAssessments(assessments, user);
		} catch (PersistentException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Unable to save the changes in the assessment", e);
		}
			
		response.setEntity(returnXML.toString(), MediaType.TEXT_XML);
		response.getEntity().setCharacterSet(CharacterSet.UTF_8);
		response.setStatus(Status.SUCCESS_OK);
	}
}
