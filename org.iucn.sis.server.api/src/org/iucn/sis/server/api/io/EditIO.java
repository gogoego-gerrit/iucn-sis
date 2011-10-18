package org.iucn.sis.server.api.io;

import java.util.Date;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.server.api.persistance.EditDAO;
import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
import org.iucn.sis.shared.api.debug.Debug;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.Edit;
import org.iucn.sis.shared.api.models.User;

public class EditIO {
	
	private final Session session;
	
	public EditIO(Session session) {
		this.session = session;
	}

	public Edit get(int id) throws PersistentException {
		return EditDAO.getEditByORMID(session, id);
	}
	
	/**
	 * Called when saving fields individually, and not saving entire assessment
	 * 
	 * @param user
	 * @param assessmentID
	 * @param date
	 *            -- null if you want to use current date
	 * @return
	 * @throws PersistentException 
	 */
	Edit createAndSaveEditForAssessment(User user, Integer assessmentID, Date date, String reason) throws PersistentException {

		Edit edit = new Edit(reason);
		edit.setCreatedDate(date == null ? new Date() : date);
		edit.setUser(user);
		Assessment assessment = (Assessment) SIS.get().getManager().getObject(session, Assessment.class, assessmentID);
		assessment.getEdit().add(edit);
		edit.getAssessment().add(assessment);
		
		try {
			SIS.get().getManager().saveObject(session, edit);
			return edit;
		} catch (HibernateException e) {
			Debug.println(e);
			throw new PersistentException("Unable to create edit for assessment " + assessmentID, e);
		} catch (PersistentException e) {
			Debug.println(e);
			throw new PersistentException("Unable to create edit for assessment " + assessmentID, e);
		}
		

	}

}
