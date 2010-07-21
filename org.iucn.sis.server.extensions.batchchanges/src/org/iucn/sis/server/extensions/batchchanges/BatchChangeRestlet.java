package org.iucn.sis.server.extensions.batchchanges;

import java.util.ArrayList;
import java.util.List;

import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.server.api.filters.AssessmentFilterHelper;
import org.iucn.sis.server.api.restlets.ServiceRestlet;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.AssessmentFilter;
import org.iucn.sis.shared.api.models.AssessmentType;
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
			}

			NativeDocument dataDoc = SIS.get().newNativeDocument(request.getChallengeResponse());
			dataDoc.parse(xml);

			NativeElement assessmentTag = dataDoc.getDocumentElement().getElementByTagName("assessment");
			//			NativeElement publishedTag = dataDoc.getDocumentElement().getElementByTagName("published");
			//			NativeElement draftTag = dataDoc.getDocumentElement().getElementByTagName("draft");
			NativeElement taxaTag = dataDoc.getDocumentElement().getElementByTagName("taxa");
			NativeElement filterTag = dataDoc.getDocumentElement().getElementByTagName(AssessmentFilter.ROOT_TAG);
			NativeElement appendTag = dataDoc.getDocumentElement().getElementByTagName("append");
			NativeElement overwriteTag = dataDoc.getDocumentElement().getElementByTagName("overwrite");
			NativeElement regionsTag = dataDoc.getDocumentElement().getElementByTagName("regions");
			List<String> regions = new ArrayList<String>();
			NativeNodeList regionsList = regionsTag.getElementsByTagName("region");
			for (int i = 0; i < regionsList.getLength(); i++)
				regions.add(regionsList.elementAt(i).getTextContent());
			Assessment newData = Assessment.fromXML(assessmentTag);
			AssessmentFilter filter = AssessmentFilter.fromXML(filterTag);
			AssessmentFilterHelper helper = new AssessmentFilterHelper(filter);


			List<Assessment> pubAssessments = new ArrayList<Assessment>();
			List<Assessment> draftAssessments = new ArrayList<Assessment>();
			for (String taxonID : taxaTag.getTextContent().split(","))
			{
				List<Assessment> list = helper.getAssessments(Integer.valueOf(taxonID));
				for (Assessment data : list) {
					if (data.getType().equalsIgnoreCase(AssessmentType.DRAFT_ASSESSMENT_TYPE))
						draftAssessments.add(data);
					else if (data.getType().equalsIgnoreCase(AssessmentType.PUBLISHED_ASSESSMENT_TYPE))
						pubAssessments.add(data);
				}
			}

			boolean append = Boolean.parseBoolean(appendTag.getText());
			boolean overwrite = Boolean.parseBoolean(overwriteTag.getText());


			StringBuffer changed = new StringBuffer();
			changed.append("<changed>\n");

			if (!pubAssessments.isEmpty()) {
				changed.append("<published>");
				changed.append(BatchAssessmentChanger.changePublishedAssessments(pubAssessments, newData, append, overwrite, SIS.get().getUser(request)));
				changed.append("</published>\n");
			}

			if (!draftAssessments.equals("")) {
				changed.append("<draft>");
				changed.append(BatchAssessmentChanger.changeDraftAssessments(draftAssessments, 
						newData, append, overwrite, SIS.get().getUser(request)));
				changed.append("</draft>\n");
			}
			changed.append("</changed>");

			response.setEntity(changed.toString(), MediaType.TEXT_XML);
			response.getEntity().setCharacterSet(CharacterSet.UTF_8);
			response.setStatus(Status.SUCCESS_OK);
		} catch (Exception e) {
			e.printStackTrace();
			response.setStatus(Status.SERVER_ERROR_INTERNAL);
		}

	}

}
