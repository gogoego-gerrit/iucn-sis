package org.iucn.sis.server.restlets.assessments;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.server.api.filters.AssessmentFilterHelper;
import org.iucn.sis.server.api.io.AssessmentIO;
import org.iucn.sis.server.api.io.TaxonIO;
import org.iucn.sis.server.api.io.AssessmentIO.AssessmentIOWriteResult;
import org.iucn.sis.server.api.persistance.FieldDAO;
import org.iucn.sis.server.api.persistance.PrimitiveFieldDAO;
import org.iucn.sis.server.api.persistance.SISPersistentManager;
import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
import org.iucn.sis.server.api.restlets.BaseServiceRestlet;
import org.iucn.sis.server.api.utils.RegionConflictException;
import org.iucn.sis.server.utils.AssessmentPersistence;
import org.iucn.sis.shared.api.assessments.AssessmentDeepCopyFilter;
import org.iucn.sis.shared.api.assessments.AssessmentFetchRequest;
import org.iucn.sis.shared.api.assessments.PublishedAssessmentsComparator;
import org.iucn.sis.shared.api.debug.Debug;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.AssessmentFilter;
import org.iucn.sis.shared.api.models.AssessmentType;
import org.iucn.sis.shared.api.models.Edit;
import org.iucn.sis.shared.api.models.Field;
import org.iucn.sis.shared.api.models.PrimitiveField;
import org.iucn.sis.shared.api.models.Reference;
import org.iucn.sis.shared.api.models.Region;
import org.iucn.sis.shared.api.models.Taxon;
import org.iucn.sis.shared.api.models.User;
import org.iucn.sis.shared.api.utils.CanonicalNames;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ResourceException;

import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.NativeNodeList;
import com.solertium.util.AlphanumericComparator;
import com.solertium.util.events.ComplexListener;

public class AssessmentRestlet extends BaseServiceRestlet {

	public AssessmentRestlet(Context context) {
		super(context);
	}

	@Override
	public void definePaths() {
		paths.add("/assessments");
		paths.add("/assessments/{type}");
		paths.add("/assessments/{type}/{id}");
	}
	
	@Override
	protected boolean shouldOpenTransation(Request request, Response response) {
		return super.shouldOpenTransation(request, response) && !(
			Method.POST.equals(request.getMethod()) && 
			"fetch".equals(request.getResourceRef().getQueryAsForm().getFirstValue("action"))
		);
	}
	
	@Override
	public void handlePost(Representation entity, Request request, Response response, Session session) throws ResourceException {
		String action = request.getResourceRef().getQueryAsForm().getFirstValue("action");
		if (action == null) {
			postAssessment(entity, request, response, getUser(request, session), session);
		} else if (action.equalsIgnoreCase("fetch")) {
			String mode = request.getResourceRef().getQueryAsForm().getFirstValue("mode", "FULL");
			getAssessments(entity, request, response, getIdentifier(request), (String)request.getAttributes().get("type"), mode, session);
		} else if (action.equalsIgnoreCase("batch")) {
			batchCreate(entity, request, response, getUser(request, session), session);
		}
	}

	@Override
	public void handleDelete(Request request, Response response, Session session) throws ResourceException {
		final Integer assessmentID = getAssessmentID(request);
		final AssessmentIO assessmentIO = new AssessmentIO(session);
		Assessment assessment = assessmentIO.getNonCachedAssessment(assessmentID);
		if (assessment != null) {
			AssessmentIOWriteResult deleted = assessmentIO.trashAssessment(assessment, getUser(request, session));
			if (deleted.status.isSuccess()) {
				response.setStatus(Status.SUCCESS_OK);
			} else
				throw new ResourceException(Status.CLIENT_ERROR_LOCKED);
		} else {
			throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "Could not find assessment #" + assessmentID);
			//response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			//response.setEntity("Assessment with id " + assessmentID + " was not found.", MediaType.TEXT_PLAIN);
		}
	}
	
	private Integer getAssessmentID(Request request) throws ResourceException {
		try {
			return Integer.valueOf((String) request.getAttributes().get("id"));
		} catch (NumberFormatException e) {
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Please supply a valid ID");
		}
	}

	public Representation handleGet(Request request, Response response, Session session) throws ResourceException {
		Integer id = getAssessmentID(request);
		final AssessmentIO assessmentIO = new AssessmentIO(session);
		Assessment assessment = assessmentIO.getAssessment(id);
		if (assessment != null) {
			response.setStatus(Status.SUCCESS_OK);
			return new StringRepresentation(assessment.toXML(), MediaType.TEXT_XML);
		} else {
			throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "Could not find assessment #" + id);
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
	 * @param type
	 * @param session TODO
	 * @param getEntity
	 * 
	 * @return
	 */
	
	private void getAssessments(Representation entity, Request request, Response response, String user, String type, String mode, Session session) throws ResourceException {
		NativeDocument ndoc = getEntityAsNativeDocument(entity);
		AssessmentFetchRequest afq = AssessmentFetchRequest.fromXML(ndoc.getDocumentElement());
		
		Set<Assessment> fetched = new HashSet<Assessment>();
		
		final AssessmentIO assessmentIO = new AssessmentIO(session);
		
		for (Integer id : afq.getAssessmentUIDs()) {
			Assessment assessment = assessmentIO.getAssessment(id);
			if (assessment != null) 
				fetched.add(assessment);
		}

		for (Integer taxonID : afq.getTaxonIDs()) {
			if (type == null) {
				fetched.addAll(assessmentIO.readAssessmentsForTaxon(taxonID));
			} else if (type.equalsIgnoreCase(AssessmentType.DRAFT_ASSESSMENT_TYPE)) {
				fetched.addAll(assessmentIO.readUnpublishedAssessmentsForTaxon(taxonID));
			} else if (type.equalsIgnoreCase(AssessmentType.PUBLISHED_ASSESSMENT_TYPE)) {
				fetched.addAll(assessmentIO.readPublishedAssessmentsForTaxon(taxonID));
			}
		}
		
		StringBuilder ret = new StringBuilder("<assessments>");
		for (Assessment asm : fetched) {
			filterFields(asm, mode);
			ret.append(asm.toXML());
		}
		ret.append("</assessments>");

		response.setStatus(Status.SUCCESS_OK);
		response.setEntity(ret.toString(), MediaType.TEXT_XML);
	}
	
	private void filterFields(Assessment assessment, String mode) {
		Edit lastEdit = assessment.getLastEdit();
		if (lastEdit != null) {
			HashSet<Edit> editSet = new HashSet<Edit>();
			editSet.add(lastEdit);
			assessment.setEdit(editSet);
		}
		
		if ("PARTIAL".equalsIgnoreCase(mode)) {
			List<String> allowed = new ArrayList<String>();
			allowed.add(CanonicalNames.RedListAssessmentDate);
			allowed.add(CanonicalNames.RedListCriteria);
			allowed.add(CanonicalNames.RegionInformation);
			
			Set<Field> fields = new HashSet<Field>();
			for (String name : new String[] {CanonicalNames.RedListAssessmentDate, CanonicalNames.RedListCriteria, CanonicalNames.RegionInformation}) {
				Field field = assessment.getField(name);
				if (field != null)
					fields.add(field);
			}
			
			assessment.setField(fields);
		}
	}
	
	private void postAssessment(Representation entity, Request request, Response response, User username, final Session session) throws ResourceException  {
		session.clear();
		
		final NativeDocument doc;
		try {
			doc = getEntityAsNativeDocument(entity);
		} catch (ResourceException e) {
			throw e;
		}
		
		final AssessmentIO assessmentIO = new AssessmentIO(session);
		try {
			Assessment source = Assessment.fromXML(doc);
			
			// ONLY ALLOW POSTING OF ASSESSMENTS THAT ALREADY EXIST;
			if (source.getId() != 0) {
				final Assessment target = assessmentIO.getAssessment(source.getId());
				
				final Map<Integer, Reference> targetRefs = new HashMap<Integer, Reference>();
				for (Reference reference : target.getReference())
					targetRefs.put(reference.getId(), reference);
				
				for (Reference sourceRef : source.getReference()) {
					if (sourceRef.getId() == 0)
						continue;
					
					Reference targetRef = targetRefs.remove(sourceRef.getId());
					if (targetRef == null) {
						Reference ref = SISPersistentManager.instance().getObject(session, Reference.class, sourceRef.getId());
						if (ref != null)
							target.getReference().add(ref);
					}
				}
				
				target.getReference().removeAll(targetRefs.values());
				
				//Required...
				source.toXML();
				target.toXML();
				
				final AssessmentPersistence saver = new AssessmentPersistence(session, target);
				saver.setDeleteFieldListener(new ComplexListener<Field>() {
					public void handleEvent(Field field) {
						try {
							FieldDAO.deleteAndDissociate(field, session);
						} catch (PersistentException e) {
							Debug.println(e);
						}
					}
				});
				saver.setDeletePrimitiveFieldListener(new ComplexListener<PrimitiveField<?>>() {
					public void handleEvent(PrimitiveField<?> field) {
						try {
							PrimitiveFieldDAO.deleteAndDissociate(field, session);
						} catch (PersistentException e) {
							Debug.println(e);
						}
					}
				});
				saver.sink(source);
				
				Hibernate.initialize(target.getEdit());
				
				//This may or may not need to happen for hibernate reasons...
				target.toXML();
				
				/*
				 * If this happens, then some field that should not have been 
				 * removed got removed, and I'd rather fail here than continue 
				 * processing; lest we risk losing data, notes, or references.
				 * 
				 * TODO: add this back; removing the constraint for now until 
				 * this is further tested with the client 
				 */
				/*if (source.getField().size() != target.getField().size())
					throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Server error: fields not persisted correctly.");*/
							
				if (!assessmentIO.allowedToCreateNewAssessment(target))
					throw new RegionConflictException();
				
				AssessmentIOWriteResult result = 
					assessmentIO.writeAssessment(target, getUser(request, session), "Changes made to assessment.", true);
				
				if (!result.status.isSuccess())
					throw new ResourceException(result.status, "AssessmentIOWrite threw exception when saving.");
				
				session.flush();
				
				response.setStatus(result.status);
				response.setEntity(target.toXML(), MediaType.TEXT_XML);
				
				if (result.edit == null)
					Debug.println("Error: No edit associated with this change. Not backing up changes.");
				else
					saver.saveChanges(target, result.edit);

			} else {
				throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
			}

		} catch (RegionConflictException e) {
			throw new ResourceException(Status.CLIENT_ERROR_CONFLICT, e);
		} catch (ResourceException e) {
			throw e;
		} catch (Exception e){
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}	
	}

	private void batchCreate(Representation entity, Request request, Response response, User user, Session session) throws ResourceException {
		final NativeDocument doc = getEntityAsNativeDocument(entity);
		
		final List<String> successfulIDs = new ArrayList<String>();
		final List<String> extantIDs = new ArrayList<String>();
		final List<String> unsuccessfulIDs = new ArrayList<String>();
		
		final AssessmentFilter filter = AssessmentFilter.fromXML(doc.getDocumentElement().getElementsByTagName(
				AssessmentFilter.ROOT_TAG).elementAt(0));

		NativeNodeList nodes = doc.getDocumentElement().getElementsByTagName("taxon");
		boolean useTemplate = Boolean.parseBoolean(doc.getDocumentElement().getElementsByTagName("useTemplate")
				.elementAt(0).getTextContent());
		
		TaxonIO taxonIO = new TaxonIO(session);
		AssessmentIO io = new AssessmentIO(session);

		final int count = nodes.getLength();
		for (int i = 0; i < count; i++) {
			Taxon taxon = taxonIO.getTaxon(Integer.valueOf(nodes.elementAt(i).getTextContent()));
			Assessment curAssessment = doCreateAssessmentForBatch(user, filter, useTemplate, taxon, session);
			try {
				AssessmentIOWriteResult result = io.saveNewAssessment(curAssessment, user);
				if (result.status.isSuccess())
					successfulIDs.add(taxon.getFriendlyName());
				else
					unsuccessfulIDs.add(taxon.getFriendlyName());
			} catch (RegionConflictException e) {
				extantIDs.add(taxon.getFriendlyName());
			}
		}

		StringBuilder ret = new StringBuilder();
		ret.append("<div>");
		listBatchResultBlock(ret, "Unable to create an assessment for the following species", unsuccessfulIDs, count);
		listBatchResultBlock(ret, "The following species already have draft assessments with the specific locality", extantIDs, count);
		listBatchResultBlock(ret, "Successfully created an assessment for the following species", successfulIDs, count);
		ret.append("</div>");
		
		response.setStatus(Status.SUCCESS_OK);
		response.setEntity(ret.toString(), MediaType.TEXT_HTML);
	}
	
	private void listBatchResultBlock(StringBuilder ret, String heading, List<String> list, int count) {
		if (!list.isEmpty()) {
			Collections.sort(list, new AlphanumericComparator());
			
			ret.append("<div>\r\n");
			ret.append(String.format("<h3>%s: (%s/%s)</h3>\r\n", heading, list.size(), count));
			for (String name : list)
				ret.append("<div> - " + name + "</div>");
			ret.append("</div>\r\n");
		}
	}

	private Assessment doCreateAssessmentForBatch(User user, AssessmentFilter filter, boolean useTemplate, Taxon taxon, final Session session) {
		Assessment assessment = null;

		if (useTemplate) {
			//Find most recent published first...
			AssessmentFilter draftFilter = filter.deepCopy();
			draftFilter.setDraft(false);
			draftFilter.setRecentPublished(true);
			draftFilter.setAllPublished(false);
			
			AssessmentFilterHelper helper = new AssessmentFilterHelper(session, draftFilter);
			List<Assessment> assessments = helper.getAssessments(taxon.getId());
			if (assessments.isEmpty()) {
				draftFilter.getRegions().clear();
				draftFilter.getRegions().add(Region.getGlobalRegion());
				assessments = helper.getAssessments(taxon.getId());
			}
			if (assessments.isEmpty()) {
				Debug.println("No template exists for species {0}", taxon.getFullName());
				assessment = new Assessment(); // No template exists...
			} else {
				Collections.sort(assessments, new PublishedAssessmentsComparator(false));
				assessment = assessments.get(0).deepCopy(new AssessmentDeepCopyFilter() {
					public Reference copyReference(Reference source) {
						try {
							return (Reference)session.get(Reference.class, source.getId());
						} catch (Exception e) {
							return null;
						}
					}
				});
			}
		} else
			assessment = new Assessment();

		assessment.setTaxon(taxon);
		assessment.setType(AssessmentType.DRAFT_ASSESSMENT_TYPE);
		assessment.setSchema(SIS.get().getDefaultSchema());
		assessment.setRegions(filter.getRegions(), filter.listRegionIDs().contains(Region.GLOBAL_ID));
		
		return assessment;
	}
	
	@Override
	public void handlePut(Representation entity, Request request, Response response, Session session) throws ResourceException {
		NativeDocument doc = getEntityAsNativeDocument(entity);
		try {
			Assessment assessment = Assessment.fromXML(doc);
			
			final List<Integer> globalReferences = new ArrayList<Integer>();
			for (Reference ref : assessment.getReference())
				globalReferences.add(ref.getId());
			assessment.getReference().clear();
		
			final Map<String, List<Integer>> references = new HashMap<String, List<Integer>>();
			for (Field field : assessment.getField()) {
				if (field.getReference() != null && !field.getReference().isEmpty()) {
					List<Integer> refs = new ArrayList<Integer>();
					for (Reference reference : field.getReference())
						refs.add(reference.getId());
					references.put(field.getName(), refs);
					field.setReference(new HashSet<Reference>());
				}
			}
			
			// Set Offline status to true if Reference created Offline
			if(assessment.getId() == 0)
				assessment.setOfflineStatus(!SIS.amIOnline());
			
			AssessmentIO io = new AssessmentIO(session);
			AssessmentIOWriteResult result = io.saveNewAssessment(assessment, getUser(request, session));
			if (result.status.isSuccess()) {
				for (Map.Entry<String, List<Integer>> entry : references.entrySet()) {
					Field field = assessment.getField(entry.getKey());
					if (field != null && field.getId() > 0) {
						HashSet<Reference> refs = new HashSet<Reference>();
						for (Integer id : entry.getValue()) 
							refs.add((Reference)session.get(Reference.class, id));
						field.setReference(refs);
						
						session.update(field);
					}
				}
				for (Integer refID : globalReferences) {
					assessment.getReference().add((Reference)session.get(Reference.class, refID));
				}
				
				response.setEntity(assessment.getId()+"", MediaType.TEXT_PLAIN);
				response.setStatus(Status.SUCCESS_OK);
			} else {
				throw new ResourceException(Status.CLIENT_ERROR_EXPECTATION_FAILED);
			}
		} catch (RegionConflictException e) {
			throw new ResourceException(Status.CLIENT_ERROR_CONFLICT, e);
		} catch (ResourceException e) {
			throw e;
		} catch (Exception e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
	}
	
}
