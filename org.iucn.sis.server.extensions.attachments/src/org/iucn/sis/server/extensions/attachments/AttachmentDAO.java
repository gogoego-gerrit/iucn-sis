package org.iucn.sis.server.extensions.attachments;

import org.hibernate.Session;
import org.iucn.sis.server.api.persistance.SISPersistentManager;
import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
import org.iucn.sis.shared.api.models.Edit;
import org.iucn.sis.shared.api.models.Field;
import org.iucn.sis.shared.api.models.FieldAttachment;

public class AttachmentDAO {
	
	public static FieldAttachment getAttachment(Session session, int id) throws PersistentException {
		return SISPersistentManager.instance().getObject(session, FieldAttachment.class, id);
	}
	
	public static void updateAttachment(Session session, FieldAttachment attachment) throws PersistentException {
		SISPersistentManager.instance().saveObject(session, attachment);
	}
	
	public static boolean deleteAndDissociate(FieldAttachment attachment, Session session)throws PersistentException {
		try {
			Edit[] lEdits = (Edit[])attachment.getEdits().toArray(new Edit[attachment.getEdits().size()]);
			for(int i = 0; i < lEdits.length; i++) {
				lEdits[i].getAttachments().remove(attachment);
			}
			Field[] lFields = (Field[])attachment.getFields().toArray(new Field[attachment.getFields().size()]);
			for(int i = 0; i < lFields.length; i++) {
				lFields[i].getFieldAttachment().remove(attachment);
			}
			try {
				session.delete(attachment);
				return true;
			} catch (Exception e) {
				return false;
			}
		}
		catch(Exception e) {
			;
			throw new PersistentException(e);
		}
	}
}
