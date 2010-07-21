package org.iucn.sis.server.api.io;

import java.util.Date;

import org.hibernate.HibernateException;
import org.hibernate.SQLQuery;
import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.server.api.persistance.EditDAO;
import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.Edit;
import org.iucn.sis.shared.api.models.User;

import com.solertium.db.CInteger;
import com.solertium.db.Column;
import com.solertium.db.Row;
import com.solertium.db.query.InsertQuery;

public class EditIO {

	public Edit get(int id) throws PersistentException {
		return EditDAO.getEditByORMID(id);
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
	Edit createAndSaveEditForAssessment(User user, Integer assessmentID, Date date) throws PersistentException {

		Edit edit = new Edit();
		edit.setCreatedDate(date == null ? new Date() : date);
		edit.setUser(user);
		Assessment assessment = (Assessment) SIS.get().getManager().getSession().get(Assessment.class, assessmentID);
		assessment.getEdit().add(edit);
		edit.getAssessment().add(assessment);
		
		try {
			if (EditDAO.save(edit)) {
				return edit;
			}
		} catch (HibernateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (PersistentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		throw new PersistentException("Unable to create edit for assessment " + assessmentID);

	}

}
