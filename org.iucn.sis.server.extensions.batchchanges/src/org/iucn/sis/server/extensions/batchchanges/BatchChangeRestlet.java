package org.iucn.sis.server.extensions.batchchanges;

import java.util.ArrayList;
import java.util.List;

import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.server.api.filters.AssessmentFilterHelper;
import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
import org.iucn.sis.server.api.restlets.ServiceRestlet;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.AssessmentFilter;
import org.iucn.sis.shared.api.models.AssessmentType;
import org.iucn.sis.shared.api.models.User;
import org.restlet.Context;
import org.restlet.data.CharacterSet;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;

import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.NativeElement;
import com.solertium.lwxml.shared.NativeNodeList;

public class BatchChangeRestlet extends ServiceRestlet {
	public BatchChangeRestlet(String vfsroot, Context context) {
		super(vfsroot, context);
	}

	@Override
	public void definePaths() {
		paths.add("/batchChange");
	}

	@Override
	public void performService(Request request, Response response) {
		try {
			String xml = null;
			try {
				xml = request.getEntity().getText();
			} catch (Exception e) {
				response.setStatus(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY);
				return;
			}
			User user = SIS.get().getUser(request);
			NativeDocument dataDoc = SIS.get().newNativeDocument(request.getChallengeResponse());
			dataDoc.parse(xml);

			NativeElement assessmentTag = dataDoc.getDocumentElement().getElementByTagName("assessment");
			NativeElement taxaTag = dataDoc.getDocumentElement().getElementByTagName("taxa");
			NativeElement filterTag = dataDoc.getDocumentElement().getElementByTagName(AssessmentFilter.ROOT_TAG);
			NativeElement appendTag = dataDoc.getDocumentElement().getElementByTagName("append");
			NativeElement overwriteTag = dataDoc.getDocumentElement().getElementByTagName("overwrite");
			NativeNodeList fieldNodes = dataDoc.getDocumentElement().getElementsByTagName("fields").elementAt(0).getElementsByTagName("field");
			boolean append = Boolean.parseBoolean(appendTag.getText());
			boolean overwrite = Boolean.parseBoolean(overwriteTag.getText());
			Assessment assessment = SIS.get().getAssessmentIO().getAssessment(Integer.valueOf(assessmentTag.getTextContent()));
			AssessmentFilter filter = AssessmentFilter.fromXML(filterTag);
			AssessmentFilterHelper helper = new AssessmentFilterHelper(filter);
			
			List<String> fieldNames = new ArrayList<String>();
			for (int i = 0; i < fieldNodes.getLength(); i++)
				fieldNames.add(fieldNodes.elementAt(i).getTextContent());

			StringBuilder returnXML = new StringBuilder("<changes>");
			List<Assessment> assessments = new ArrayList<Assessment>();
			try{
				for (String taxonID : taxaTag.getTextContent().split(",")) {
					for (Assessment asmToChange : helper.getAssessments(Integer
							.valueOf(taxonID))) {
						if (BatchAssessmentChanger.changeAssessment(
								asmToChange, assessment, overwrite, append,
								user, fieldNames)) {
							returnXML.append("<change id=\""
									+ asmToChange.getId() + "\">"
									+ asmToChange.getDisplayText()
									+ "</change>");
							assessments.add(asmToChange);
						}
					}
				}

				if (!SIS.get().getAssessmentIO().saveAssessmentsWithNoFail(
						assessments, user)) {
					throw new PersistentException(
							"Unable to save the changes in the assessment");
				}
			} catch (Exception e) {
				response.setEntity(e.getLocalizedMessage(), MediaType.TEXT_PLAIN);
				response.setStatus(Status.SERVER_ERROR_INTERNAL);
				return;
			}
			returnXML.append("</changes>");
			response.setEntity(returnXML.toString(), MediaType.TEXT_XML);
			response.getEntity().setCharacterSet(CharacterSet.UTF_8);
			response.setStatus(Status.SUCCESS_OK);
		} catch (Exception e) {
			e.printStackTrace();
			response.setStatus(Status.SERVER_ERROR_INTERNAL);
		}

	}
}
