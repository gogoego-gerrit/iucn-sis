package org.iucn.sis.server.batchChange;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.iucn.sis.server.baserestlets.ServiceRestlet;
import org.iucn.sis.server.filters.AssessmentFilterHelper;
import org.iucn.sis.server.simple.SISContainerApp;
import org.iucn.sis.shared.BaseAssessment;
import org.iucn.sis.shared.data.assessments.AssessmentData;
import org.iucn.sis.shared.data.assessments.AssessmentFilter;
import org.iucn.sis.shared.data.assessments.AssessmentParser;
import org.restlet.Context;
import org.restlet.data.CharacterSet;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;

import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.NativeElement;
import com.solertium.lwxml.shared.NativeNodeList;
import com.solertium.util.SysDebugger;

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

			NativeDocument dataDoc = SISContainerApp.newNativeDocument(request.getChallengeResponse());
			dataDoc.parse(xml);

			NativeElement assessmentTag = dataDoc.getDocumentElement().getElementByTagName("assessment");
			//			NativeElement publishedTag = dataDoc.getDocumentElement().getElementByTagName("published");
			//			NativeElement draftTag = dataDoc.getDocumentElement().getElementByTagName("draft");
			NativeElement taxaTag = dataDoc.getDocumentElement().getElementByTagName("taxa");
			NativeElement filterTag = dataDoc.getDocumentElement().getElementByTagName(AssessmentFilter.HEAD_TAG);
			NativeElement appendTag = dataDoc.getDocumentElement().getElementByTagName("append");
			NativeElement overwriteTag = dataDoc.getDocumentElement().getElementByTagName("overwrite");
			NativeElement regionsTag = dataDoc.getDocumentElement().getElementByTagName("regions");
			List<String> regions = new ArrayList<String>();
			NativeNodeList regionsList = regionsTag.getElementsByTagName("region");
			for (int i = 0; i < regionsList.getLength(); i++)
				regions.add(regionsList.elementAt(i).getTextContent());

			AssessmentParser parser = new AssessmentParser();
			parser.parse(assessmentTag);
			AssessmentFilter filter = AssessmentFilter.parseXML(filterTag);
			AssessmentFilterHelper helper = new AssessmentFilterHelper(filter);


			List<AssessmentData> pubAssessments = new ArrayList<AssessmentData>();
			List<AssessmentData> draftAssessments = new ArrayList<AssessmentData>();
			for (String taxonID : taxaTag.getTextContent().split(","))
			{
				List<AssessmentData> list = helper.getAssessments(taxonID, vfs);
				for (AssessmentData data : list) {
					if (data.getType().equalsIgnoreCase(BaseAssessment.DRAFT_ASSESSMENT_STATUS))
						draftAssessments.add(data);
					else if (data.getType().equalsIgnoreCase(BaseAssessment.PUBLISHED_ASSESSMENT_STATUS))
						pubAssessments.add(data);
				}
			}


			HashMap<String, Object> newData = parser.getAssessment().getDataMap();
//			String publishedAssessments = publishedTag.getText();
//			String draftAssessments = draftTag.getText();

			boolean append = Boolean.parseBoolean(appendTag.getText());
			boolean overwrite = Boolean.parseBoolean(overwriteTag.getText());

//			SysDebugger.getNamedInstance("info").println(
//					"Changing these published assessments: " + publishedAssessments);
//			SysDebugger.getNamedInstance("info").println("Changing these draft assessments: " + draftAssessments);
			SysDebugger.getNamedInstance("info").println("Append: " + append + " and overwrite: " + overwrite);
			SysDebugger.getNamedInstance("info").println("With " + newData.size() + " pieces of data.");

			StringBuffer changed = new StringBuffer();
			changed.append("<changed>\n");

			if (!pubAssessments.isEmpty()) {
				changed.append("<published>");
				changed.append(BatchAssessmentChanger.changePublishedAssessments(vfs, pubAssessments, request
						.getChallengeResponse().getIdentifier(), newData, overwrite, append));
				changed.append("</published>\n");
			}

			if (!draftAssessments.equals("")) {
				changed.append("<draft>");
				changed.append(BatchAssessmentChanger.changeDraftAssessments(vfs, draftAssessments, regions, request
						.getChallengeResponse().getIdentifier(), newData, append, overwrite));
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
