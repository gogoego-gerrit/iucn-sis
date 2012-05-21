package org.iucn.sis.server.api.io;

import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.iucn.sis.server.api.persistance.SISPersistentManager;
import org.iucn.sis.server.api.persistance.UserCriteria;
import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
import org.iucn.sis.shared.api.debug.Debug;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.AssessmentType;
import org.iucn.sis.shared.api.models.Field;
import org.iucn.sis.shared.api.models.PrimitiveField;
import org.iucn.sis.shared.api.models.PublicationData;
import org.iucn.sis.shared.api.models.PublicationTarget;
import org.iucn.sis.shared.api.models.Reference;
import org.iucn.sis.shared.api.models.User;
import org.iucn.sis.shared.api.models.fields.ProxyField;
import org.iucn.sis.shared.api.models.fields.RedListCreditedUserField;
import org.iucn.sis.shared.api.models.primitivefields.TextPrimitiveField;
import org.iucn.sis.shared.api.utils.CanonicalNames;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;

public class PublicationIO {
	
	private final Session session;
	
	public PublicationIO(Session session) {
		this.session = session;
	}
	
	public void submit(int assessmentID, String group, User user) throws PersistentException, PublicationException {
		AssessmentIO io = new AssessmentIO(session);
		Assessment assessment = io.getAssessment(assessmentID);
		if (assessment == null)
			throw new PersistentException("Assessment " + assessmentID + " not found.");
		
		submit(assessment, group, user);
	}
	
	public void submit(Assessment assessment, String group, User user) throws PublicationException {
		if (!assessment.isDraft())
			throw new PublicationException(Status.CLIENT_ERROR_LOCKED, "This assessment can not be submitted because it is not in Draft status.");
		
		PublicationData data = assessment.getPublicationData();
		if (data == null) {
			data = new PublicationData();
			data.setAssessment(assessment);
			data.setGroup(group);
			
			assessment.setPublicationData(data);
		}
		data.setSubmitter(user);
		data.setSubmissionDate(Calendar.getInstance().getTime());
		
		if (data.getId() == 0)
			session.save(data);
		else
			session.update(data);
		
		assessment.setType(AssessmentType.SUBMITTED_ASSESSMENT_TYPE);
		
		session.update(assessment);
	}
	
	public void update(PublicationData source, List<Integer> dataList) throws PersistentException, PublicationException {
		int batchSize = 100;
		int count = 0;
		
		if (dataList.isEmpty())
			return;
		
		for (Integer id : dataList) {
			update(source, id);
			if (++count % batchSize == 0) {
				session.getTransaction().commit();
				session.beginTransaction();
			}
		}
	}
	
	public void update(PublicationData source, int targetID) throws PersistentException, PublicationException {
		PublicationData target = (PublicationData)session.get(PublicationData.class, targetID);
		if (target == null)
			throw new PersistentException("Object " + source.getId() + " not found");
		
		Hibernate.initialize(target.getAssessment());
		
		update(source, target);
	}
	
	private void update(PublicationData source, PublicationData target) throws PersistentException, PublicationException {
		if (source.getAssessment() != null)
			target.getAssessment().setType(source.getAssessment().getType());
		if (source.getNotes() != null)
			target.setNotes(source.getNotes());
		if (source.getTargetApproved() != null)
			target.setTargetApproved(getPublicationTarget(source.getTargetApproved().getId()));
		if (source.getTargetGoal() != null)
			target.setTargetGoal(getPublicationTarget(source.getTargetGoal().getId()));
		if (source.getPriority() != null)
			target.setPriority(source.getPriority());
		
		if (source.getAssessment() != null && source.getAssessment().isPublished()) {
			if (target.getTargetApproved() != null)
				publish(target.getAssessment(), target.getTargetApproved());
			else
				throw new PublicationException(Status.CLIENT_ERROR_CONFLICT, "No publication target specified.");
		}
		
		session.update(target);
		session.update(target.getAssessment());
	}
	
	private void publish(Assessment assessment, PublicationTarget target) {
		if (target.getReference() != null) {
			Field rlSource = assessment.getField(CanonicalNames.RedListPublication);
			if (rlSource == null) {
				rlSource = new Field(CanonicalNames.RedListPublication, assessment);
				assessment.getField().add(rlSource);
				session.save(rlSource);
			}
			rlSource.setReference(new HashSet<Reference>());
			rlSource.getReference().add(target.getReference());
		}
		
		Field rlAsmAuthorsField = assessment.getField(CanonicalNames.RedListAssessmentAuthors);
		ProxyField rlAsmAuthors = new ProxyField(rlAsmAuthorsField);
		if ("".equals(rlAsmAuthors.getStringPrimitiveField("value"))) {
			if (rlAsmAuthorsField == null) {
				rlAsmAuthorsField = new Field(CanonicalNames.RedListAssessmentAuthors, assessment);
				rlAsmAuthors = new ProxyField(rlAsmAuthorsField);
				assessment.getField().add(rlAsmAuthorsField);
				session.save(rlAsmAuthorsField);
			}
			RedListCreditedUserField assessors = new RedListCreditedUserField(assessment.getField(CanonicalNames.RedListAssessors));
			if (!assessors.getUsers().isEmpty()) {
				UserCriteria criteria = new UserCriteria(session);
				criteria.id.in(assessors.getUsers().toArray(new Integer[assessors.getUsers().size()]));
				List<User> users = Arrays.asList(criteria.listUser());
				rlAsmAuthors.setStringPrimitiveField("value", RedListCreditedUserField.generateText(users, assessors.getOrder()));
				session.saveOrUpdate(rlAsmAuthorsField);
			}
		}
		
		for (String fieldName : CanonicalNames.credits) {
			Field field = assessment.getField(fieldName);
			if (field == null)
				continue;
			
			RedListCreditedUserField proxy = new RedListCreditedUserField(field);
			if (!proxy.getText().equals("")) {
				proxy.setTextPrimitiveField("publication", proxy.getText());
			}
			else if (!proxy.getUsers().isEmpty()) {
				UserCriteria criteria = new UserCriteria(session);
				criteria.id.in(proxy.getUsers().toArray(new Integer[proxy.getUsers().size()]));
				List<User> users = Arrays.asList(criteria.listUser());
				
				proxy.setTextPrimitiveField("publication",
					RedListCreditedUserField.generateText(users, proxy.getOrder())
				);
			}
			session.update(field);
		}
		
		Field taxonomicNotes = assessment.getTaxon().getTaxonomicNotes();
		if (taxonomicNotes != null) {
			Set<Reference> refs = new HashSet<Reference>();
			for (Reference reference : taxonomicNotes.getReference())
				refs.add((Reference)session.get(Reference.class, reference.getId()));
			
			taxonomicNotes = taxonomicNotes.deepCopy(false);
			taxonomicNotes.setAssessment(assessment);
			taxonomicNotes.setReference(refs);
			
			Field existing = assessment.getField(CanonicalNames.TaxonomicNotes);
			if (existing != null)
				assessment.getField().remove(existing);
			assessment.getField().add(taxonomicNotes);
			
			session.save(taxonomicNotes);
		}
		
		for (String fieldName : CanonicalNames.narrativeFields) {
			Field field = assessment.getField(fieldName);
			if (field == null)
				continue;
			
			//Narrative fields have only one primitive field...
			for (PrimitiveField<?> prim : field.getPrimitiveField()) {
				if (prim instanceof TextPrimitiveField) {
					String value = prim.getRawValue();
					if (value == null || "".equals(value)) //Should be impossible
						continue;
					
					prim.setRawValue(value.replaceAll("background-color: [a-z|0-9]*;[\\s]*", ""));
					session.saveOrUpdate(prim);
				}
			}
		}
	}
	
	public void createPublicationTarget(PublicationTarget target) {
		session.save(target);
	}
	
	public PublicationTarget getPublicationTarget(int id) {
		return (PublicationTarget)session.get(PublicationTarget.class, id);
	}
	
	public List<PublicationTarget> listPublicationTargets() throws PersistentException {
		return SISPersistentManager.instance().listObjects(PublicationTarget.class, session);
	}
	
	@SuppressWarnings("unchecked")
	public List<PublicationData> listPublicationData() throws PersistentException {
		Criteria criteria = session.createCriteria(PublicationData.class)
			.createAlias("assessment", "Assessment")
			.createAlias("Assessment.AssessmentType", "AssessmentType")
			.add(Restrictions.ne("AssessmentType.id", AssessmentType.PUBLISHED_ASSESSMENT_STATUS_ID));
		
		return criteria.list();
	}
	
	public void deleteTarget(int id) throws PublicationException {
		PublicationTarget target;
		try {
			target = (PublicationTarget)session.load(PublicationTarget.class, id);
		} catch (HibernateException e) {
			throw new PublicationException(Status.CLIENT_ERROR_BAD_REQUEST, "Target not found: " + id);
		}
		
		Hibernate.initialize(target.getApproved());
		Hibernate.initialize(target.getGoals());
		
		if (target.getApproved().isEmpty() && target.getGoals().isEmpty())
			session.delete(target);
		else
			throw new PublicationException(Status.CLIENT_ERROR_CONFLICT, "Target in use.");
	}
	
	public void updateTarget(PublicationTarget source) throws PublicationException {
		PublicationTarget target;
		try {
			target = (PublicationTarget)session.load(PublicationTarget.class, source.getId());
		} catch (HibernateException e) {
			throw new PublicationException(Status.CLIENT_ERROR_NOT_FOUND, "Did not find a target with identifier " + source.getId());
		}
		
		target.setName(source.getName());
		target.setDate(source.getDate());
		if (source.getReference() == null)
			target.setReference(null);
		else
			target.setReference((Reference)session.get(Reference.class, source.getReference().getId()));
		
		try {
			session.update(target);
		} catch (HibernateException e) {
			Debug.println(e);
			throw new PublicationException(Status.SERVER_ERROR_INTERNAL, "Could not save due to server error.");
		}
	}
	
	public static class PublicationException extends ResourceException {
		
		private static final long serialVersionUID = 1L;
		
		public PublicationException(Status status, String description) {
			super(status, description);
		}
		
	}

}
