package org.iucn.sis.server.extensions.attachments;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;
import org.iucn.sis.server.api.persistance.AssessmentCriteria;
import org.iucn.sis.server.api.persistance.FieldCriteria;
import org.iucn.sis.server.api.persistance.FieldDAO;
import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.Edit;
import org.iucn.sis.shared.api.models.Field;
import org.iucn.sis.shared.api.models.FieldAttachment;
import org.iucn.sis.shared.api.models.User;

public class AttachmentIO {
	
	private final Session session;
	
	public AttachmentIO(Session session) {
		this.session = session;
	}
	
	public FieldAttachment getAttachment(Integer attachmentID) {
		try {
			return AttachmentDAO.getAttachment(session, attachmentID);
		} catch (PersistentException e) {
			return null;
		}
	}
	
	public FieldAttachment[] getAttachments(Field field) {
		AttachmentCriteria criteria = new AttachmentCriteria(session);
		FieldCriteria fieldCriteria = criteria.createFieldCriteria();
		fieldCriteria.id.eq(field.getId());
		
		return criteria.listAttachment();
	}
	
	public FieldAttachment[] getAttachments(Assessment assessment) {
		AttachmentCriteria criteria = new AttachmentCriteria(session);
		AssessmentCriteria assessmentCriteria = 
			criteria.createFieldCriteria().createAssessmentCriteria();
		assessmentCriteria.id.eq(assessment.getId());
		assessmentCriteria.state.eq(Assessment.ACTIVE);
		
		return criteria.listAttachment();
	}
	
	public FieldAttachment createAttachment(String name, String key, boolean publish, User user) {
		Edit edit = new Edit("Attachment created.");
		edit.setUser(user);
		
		FieldAttachment attachment = new FieldAttachment();
		attachment.setName(name);
		attachment.setKey(key);
		attachment.setPublish(publish);
		attachment.getEdits().add(edit);
		
		edit.getAttachments().add(attachment);
		
		session.save(attachment);
		
		return attachment;
	}
	
	public void attach(Integer attachmentID, List<Integer> fields) throws PersistentException {
		FieldAttachment attachment = AttachmentDAO.getAttachment(session, attachmentID);
		if (attachment == null)
			throw new PersistentException("Attachment " + attachmentID + " not found.");
		
		List<Integer> attached = new ArrayList<Integer>();
		for (Field field : attachment.getFields())
			attached.add(field.getId());
		
		for (Integer fieldID : fields) {
			if (!attached.contains(fieldID)) {
				Field field = FieldDAO.getFieldByORMID(session, fieldID);
				if (field != null)
					attachment.getFields().add(field);
			}
		}
		
		session.save(attachment);
	}
	
	public void detach(Integer attachmentID, Integer fieldID) throws PersistentException {
		FieldAttachment attachment = AttachmentDAO.getAttachment(session, attachmentID);
		if (attachment == null)
			throw new PersistentException("Attachment " + attachmentID + " not found.");
		
		Field found = null;
		
		for (Field field : attachment.getFields()) {
			if (field.getId() == fieldID) {
				found = field;
				break;
			}
		}
		
		if (found != null) {
			if (attachment.getFields().size() == 1)
				delete(attachment);
			else {
				attachment.getFields().remove(found);
				
				session.save(attachment);
			}
		}
	}
	
	public void delete(Integer attachmentID) throws PersistentException {
		FieldAttachment attachment = AttachmentDAO.getAttachment(session, attachmentID);
		if (attachment == null)
			throw new PersistentException("Attachment " + attachmentID + " not found.");
		
		delete(attachment);
	}
	
	public void delete(FieldAttachment attachment) throws PersistentException {
		AttachmentDAO.deleteAndDissociate(attachment, session);
	}
	
	public void saveMetadata(FieldAttachment attachment) throws PersistentException {
		AttachmentDAO.updateAttachment(session, attachment);
	}

}
