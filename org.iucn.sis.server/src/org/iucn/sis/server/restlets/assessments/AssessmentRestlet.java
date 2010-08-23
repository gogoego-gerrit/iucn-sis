package org.iucn.sis.server.restlets.assessments;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.server.api.filters.AssessmentFilterHelper;
import org.iucn.sis.server.api.io.AssessmentIO.AssessmentIOWriteResult;
import org.iucn.sis.server.api.restlets.ServiceRestlet;
import org.iucn.sis.server.api.utils.RegionConflictException;
import org.iucn.sis.shared.api.assessments.AssessmentFetchRequest;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.AssessmentFilter;
import org.iucn.sis.shared.api.models.AssessmentType;
import org.iucn.sis.shared.api.models.Region;
import org.iucn.sis.shared.api.models.Taxon;
import org.iucn.sis.shared.api.models.User;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;

import com.solertium.lwxml.factory.NativeDocumentFactory;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.NativeNodeList;

public class AssessmentRestlet extends ServiceRestlet {


	public AssessmentRestlet(String vfsroot, Context context) {
		super(vfsroot, context);
	}

	@Override
	public void definePaths() {
		paths.add("/assessments");
		paths.add("/assessments/{type}");
		paths.add("/assessments/{type}/{id}");
	}

	private void deleteAssessment(Request request, Response response, final Integer assessmentID, final User user) {

		Assessment assessment = SIS.get().getAssessmentIO().getNonCachedAssessment(assessmentID);
		if (assessment != null) {
			AssessmentIOWriteResult deleted = SIS.get().getAssessmentIO().trashAssessment(assessment, user);
			if (deleted.status.isSuccess()) {
				response.setStatus(Status.SUCCESS_OK);
			} else
				response.setStatus(Status.CLIENT_ERROR_LOCKED);
		} else {
			response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			response.setEntity("Assessment with id " + assessmentID + " was not found.", MediaType.TEXT_PLAIN);
		}

	}


	@Override
	public void performService(Request request, Response response) {
		try {
			String username = SIS.get().getUsername(request);
			String id = (String) request.getAttributes().get("id");
			String type = (String) request.getAttributes().get("type");

			if (username == null || username.equals("")) {
				response.setStatus(Status.CLIENT_ERROR_FORBIDDEN);
			} else if (request.getMethod().equals(Method.GET)) {
				getAssessment(Integer.valueOf(id), request, response, username);
			} else if (request.getMethod().equals(Method.PUT)) {
				putAssessment(request, response, SIS.get().getUser(request));
			} else if (request.getMethod().equals(Method.POST)) {
				String action = request.getResourceRef().getQueryAsForm().getFirstValue("action");
				if (action == null) {
					postAssessment(request, response, SIS.get().getUser(request));
				} else if (action.equalsIgnoreCase("fetch")) {
					getAssessments(request, response, username, type);
				} else if (action.equalsIgnoreCase("batch")) {
					batchCreate(request, response, SIS.get().getUser(request));
				}
			} else if (request.getMethod().equals(Method.DELETE)) {
				deleteAssessment(request, response, Integer.valueOf(id), SIS.get().getUser(request));
			} else {
				response.setStatus(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
			}
		} catch (Exception e) {
			e.printStackTrace();
			response.setStatus(Status.SERVER_ERROR_INTERNAL);
		}
	}

	/**
	 * puts the xml representaiton of the assessment into the response
	 * 
	 * @param assID
	 * @param request
	 * @param response
	 * @param user
	 */
	private void getAssessment(Integer assID, Request request, Response response, String user) {
		Assessment assessment = SIS.get().getAssessmentIO().getAssessment(assID);
		if (assessment != null) {
			response.setStatus(Status.SUCCESS_OK);
			response.setEntity(assessment.toXML(), MediaType.TEXT_XML);
		} else {
			response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			response.setEntity("No Assessment found with id " + assID, MediaType.TEXT_PLAIN);
		}

	}

	/**
	 * Format of a proper get request: &lt;uid&gt;(assessmentUID)&lt;/uid&gt;
	 * 
	 * or
	 * 
	 * &lt;taxon&gt;(taxonID)&lt;/taxon&gt;
	 * 
	 * If the type parameter is null for the latter format, all .
	 * 
	 * @param getEntity
	 * @param type
	 * @return
	 */
	private void getAssessments(Request request, Response response, String user, String type) throws IOException {
		String entity = request.getEntityAsText();
		NativeDocument ndoc = NativeDocumentFactory.newNativeDocument();
		ndoc.parse(entity);
		AssessmentFetchRequest afq = AssessmentFetchRequest.fromXML(ndoc.getDocumentElement());
		StringBuilder ret = new StringBuilder("<assessments>");
		Set<Assessment> fetched = new HashSet<Assessment>();
		
		for (Integer assID : afq.getAssessmentUIDs()) {
			Assessment assessment = SIS.get().getAssessmentIO().getAssessment(assID);
			if (assessment != null) {
				fetched.addAll(SIS.get().getAssessmentIO().readAssessmentsForTaxon(
						assessment.getSpeciesID()));
			}
		}

		for (Integer taxonID : afq.getTaxonIDs()) {
			if (type == null) {
				fetched.addAll(SIS.get().getAssessmentIO().readAssessmentsForTaxon(taxonID));
			} else if (type.equalsIgnoreCase(AssessmentType.DRAFT_ASSESSMENT_TYPE)) {
				fetched.addAll(SIS.get().getAssessmentIO().readDraftAssessmentsForTaxon(taxonID));
			} else if (type.equalsIgnoreCase(AssessmentType.PUBLISHED_ASSESSMENT_TYPE)) {
				fetched.addAll(SIS.get().getAssessmentIO().readPublishedAssessmentsForTaxon(taxonID));
			}
		}

		for (Assessment asm : fetched) {
			ret.append(SIS.get().getAssessmentIO().getAssessmentXML(asm.getId()));
		}
		ret.append("</assessments>");

		response.setStatus(Status.SUCCESS_OK);
		response.setEntity(ret.toString(), MediaType.TEXT_XML);

	}

	private void postAssessment(Request request, Response response, User username) {
		try {
			NativeDocument doc = NativeDocumentFactory.newNativeDocument();
			doc.parse(request.getEntityAsText());
			Assessment assessment = Assessment.fromXML(doc);

			// ONLY ALLOW POSTING OF ASSESSMENTS THAT ALREADY EXIST;
			if (assessment.getId() != 0) {
				AssessmentIOWriteResult result = saveAssessment(assessment, username);
				if (result.status.isSuccess()) {
					response.setEntity(assessment.toXML(), MediaType.TEXT_XML);
					response.setStatus(result.status);
				} else {
					response.setStatus(Status.CLIENT_ERROR_EXPECTATION_FAILED);
				}
			} else {
				response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			}

		} catch (RegionConflictException e) {
			response.setStatus(Status.CLIENT_ERROR_CONFLICT);
		} catch (Exception e) {
			e.printStackTrace();
			response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
		}
	}

	private void batchCreate(Request request, Response response, User user) {
		NativeDocument doc = NativeDocumentFactory.newNativeDocument();
		StringBuffer successfulIDs = new StringBuffer();
		StringBuffer extantIDs = new StringBuffer();
		StringBuffer unsuccessfulIDs = new StringBuffer();

		try {
			String text = request.getEntity().getText();
			doc.parse(text);

			AssessmentFilter filter = AssessmentFilter.fromXML(doc.getDocumentElement().getElementsByTagName(
					AssessmentFilter.ROOT_TAG).elementAt(0));

			NativeNodeList nodes = doc.getDocumentElement().getElementsByTagName("taxon");
			boolean useTemplate = Boolean.parseBoolean(doc.getDocumentElement().getElementsByTagName("useTemplate")
					.elementAt(0).getTextContent());
			System.out.println("Using template? " + useTemplate);

			for (int i = 0; i < nodes.getLength(); i++) {
				Taxon taxon = SIS.get().getTaxonIO().getTaxon(Integer.valueOf(nodes.elementAt(i).getTextContent()));
				Assessment curAss = doCreateAssessmentForBatch(user, filter, useTemplate, taxon);
				try {
					AssessmentIOWriteResult result = saveAssessment(curAss, user);
					if (result.status.isSuccess())
						successfulIDs.append(curAss.getSpeciesName() + (i == nodes.getLength() - 1 ? "" : ", "));
					else
						unsuccessfulIDs.append(curAss.getSpeciesName() + (i == nodes.getLength() - 1 ? "" : ", "));
				} catch (RegionConflictException e) {
					extantIDs.append(curAss.getSpeciesName() + (i == nodes.getLength() - 1 ? "" : ", "));
				}
			}

			StringBuilder ret = new StringBuilder();
			if (unsuccessfulIDs.length() > 0)
				ret.append("<div>Unable to create an assessment for the following species: " + unsuccessfulIDs
						+ "</div>\r\n");
			if (extantIDs.length() > 0)
				ret.append("<div>The following species already have draft assessments with the specific locality: "
						+ extantIDs + "</div>\r\n");
			if (successfulIDs.length() > 0)
				ret.append("<div>Successfully created an assessment for the following species: " + successfulIDs
						+ "</div>\r\n");
			response.setStatus(Status.SUCCESS_OK);
			response.setEntity(ret.toString(), MediaType.TEXT_HTML);
		} catch (IOException e) {
			e.printStackTrace();
			response.setStatus(Status.SERVER_ERROR_INTERNAL);
			response.setEntity(e.toString(), MediaType.TEXT_PLAIN);
		} catch (NullPointerException e) {
			e.printStackTrace();
			response.setStatus(Status.SERVER_ERROR_INTERNAL);
			response.setEntity(e.toString(), MediaType.TEXT_PLAIN);
		} catch (Exception e) {
			e.printStackTrace();
			response.setStatus(Status.SERVER_ERROR_INTERNAL);
			response.setEntity(e.toString(), MediaType.TEXT_PLAIN);
		}
	}

	private Assessment doCreateAssessmentForBatch(User user, AssessmentFilter filter, boolean useTemplate, Taxon taxon) {
		AssessmentFilter draftFilter = filter.deepCopy();
		draftFilter.setDraft(false);
		draftFilter.setRecentPublished(true);
		draftFilter.setAllPublished(false);

		AssessmentFilterHelper helper = new AssessmentFilterHelper(draftFilter);

		Assessment curAss = null;

		if (useTemplate) {
			List<Assessment> assessments = helper.getAssessments(taxon.getId());
			if (assessments.size() == 0) {
				draftFilter.getRegions().clear();
				draftFilter.getRegions().add(Region.getGlobalRegion());
				assessments = helper.getAssessments(taxon.getId());
			}

			if (assessments.size() == 0) {
				System.out.println("No template exists for species " + taxon.getFullName());
				curAss = new Assessment(); // No template exists...
			} else {
				curAss = assessments.get(0).deepCopy();
			}
		} else
			curAss = new Assessment();

//		ArrayList<Integer> regionsIDS = new ArrayList<Integer>();
//		for (Region region : filter.getRegions())
//			regionsIDS.add(region.getId());
//		curAss.setRegionIDs(regionsIDS);
		curAss.setRegions(filter.getRegions(), filter.getRegions().contains("-1"));
		curAss.setType(AssessmentType.DRAFT_ASSESSMENT_TYPE);
		curAss.setTaxon(taxon);
		return curAss;
	}

	private void putAssessment(Request request, Response response, User user) {
		try {
			NativeDocument doc = NativeDocumentFactory.newNativeDocument();
			doc.parse(request.getEntity().getText());
			Assessment assessment = Assessment.fromXML(doc);
			AssessmentIOWriteResult result = saveAssessment(assessment, user);
			if (result.status.isSuccess()) {
				response.setEntity(assessment.getId()+"", MediaType.TEXT_PLAIN);
				response.setStatus(Status.SUCCESS_OK);
			} else {
				response.setStatus(Status.CLIENT_ERROR_EXPECTATION_FAILED);
			}
		} catch (RegionConflictException e) {
			response.setStatus(Status.CLIENT_ERROR_CONFLICT);
		} catch (Exception e) {
			response.setStatus(Status.SERVER_ERROR_INTERNAL);
		}
	}

	/**
	 * checks to make sure regionailty is unique for draft assessemnts saves via
	 * ASSESSMENTIO if no conflicts
	 * 
	 * @param assessment
	 * @param username
	 * @return
	 * @throws RegionConflictException
	 */
	private AssessmentIOWriteResult saveAssessment(Assessment assessment, User username) throws RegionConflictException {
		return SIS.get().getAssessmentIO().saveNewAssessment(assessment, username);
	}

	
}
