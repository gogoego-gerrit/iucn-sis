package org.iucn.sis.server.api.io;

import java.util.Calendar;
import java.util.List;
import java.util.Properties;

import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.iucn.sis.server.api.persistance.SISPersistentManager;
import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.AssessmentType;
import org.iucn.sis.shared.api.models.PublicationData;
import org.iucn.sis.shared.api.models.PublicationTarget;
import org.iucn.sis.shared.api.models.User;
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
	
	public void update(PublicationData source, List<Integer> dataList) throws PersistentException {
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
	
	public void update(PublicationData source, int targetID) throws PersistentException {
		PublicationData target = (PublicationData)session.get(PublicationData.class, targetID);
		if (target == null)
			throw new PersistentException("Object " + source.getId() + " not found");
		
		Hibernate.initialize(target.getAssessment());
		
		update(source, target);
	}
	
	private void update(PublicationData source, PublicationData target) throws PersistentException {
		if (source.getAssessment() != null)
			target.getAssessment().setType(source.getAssessment().getType());
		if (source.getNotes() != null)
			target.setNotes(source.getNotes());
		if (source.getTargetApproved() != null)
			target.setTargetApproved(getPublicationTarget(source.getTargetApproved().getId()));
		if (source.getTargetGoal() != null)
			target.setTargetGoal(getPublicationTarget(source.getTargetGoal().getId()));
		
		session.update(target);
		session.update(target.getAssessment());
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
			.add(Restrictions.ne("AssessmentType.id", AssessmentType.DRAFT_ASSESSMENT_STATUS_ID));
		
		return criteria.list();
	}
	
	public static class PublicationException extends ResourceException {
		
		private static final long serialVersionUID = 1L;
		
		public PublicationException(Status status, String description) {
			super(status, description);
		}
		
	}

}
