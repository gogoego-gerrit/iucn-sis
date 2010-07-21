package org.iucn.sis.server.api.io;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.hibernate.HibernateException;
import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.server.api.persistance.SISPersistentManager;
import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.Edit;
import org.iucn.sis.shared.api.models.User;
import org.iucn.sis.shared.api.models.primitivefields.TextPrimitiveField;

public class PrimitiveFieldIO {

	protected Map<Integer, Assessment> updatedPFIDToAssessment;

	public PrimitiveFieldIO() {
		updatedPFIDToAssessment = new HashMap<Integer, Assessment>();
	}

	/**
	 * 
	 * Given a textPrimitiveFieldID, the new value of the textPrimitive field,
	 * the user that is going to save, and the detached assessment (whose
	 * textPrimitiveField value must already be set in), will save it to the
	 * database, and save the assessment to the vfs.
	 * 
	 * @param textPrimitiveFieldID
	 * @param newValue
	 * @param user
	 * @param assessment
	 * @return
	 */
	public boolean updateTextPrimitveFieldValueInDatabase(Integer textPrimitiveFieldID, String newValue, User user,
			Assessment assessment) {
		Map<Integer, String> idsToNewValue = new HashMap<Integer, String>();
		idsToNewValue.put(textPrimitiveFieldID, newValue);
		return updateTextPrimitiveFieldValuesInDatabase(idsToNewValue, user, assessment);
	}

	/**
	 * 
	 * Given a map of textPrimitiveField ids to new values, the user that is
	 * going to save, and the detached assessment (whose textPrimitiveField
	 * values must already be set in), will save it to the database, and save
	 * the assessment to the vfs.
	 * 
	 * adds an edit to the assessment
	 * 
	 * 
	 * @param idToNewValues
	 * @param user
	 * @param assessment
	 * @return -- if return false, then you need to rollback transaction
	 */
	public boolean updateTextPrimitiveFieldValuesInDatabase(Map<Integer, String> idToNewValues, User user,
			Assessment assessment) {
		try {
			//ONLY ADD ONE EDIT FOR ALL THESE CHANGES
			Edit edit = SIS.get().getEditIO().createAndSaveEditForAssessment(user, assessment.getId(), null);
			assessment.getEdit().add(edit);
			
			for (Entry<Integer, String> entry : idToNewValues.entrySet()) {
				TextPrimitiveField field;

				field = (TextPrimitiveField) SIS.get().getManager().getSession().get(TextPrimitiveField.class,
						entry.getKey());
				field.setValue(entry.getValue());
				addUpdatedAssessment(entry.getKey(), assessment);
				SISPersistentManager.instance().saveObject(field);
			}
		} catch (HibernateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (PersistentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (Throwable e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	/**
	 * Should only be called when trying to save to vfs
	 * 
	 * @param primitiveFieldID
	 * @return
	 */
	public Assessment getUpdatedAssessment(Integer primitiveFieldID) {
		return updatedPFIDToAssessment.remove(primitiveFieldID);
	}

	/**
	 * Used to save on vfs after database update, only call on successful save
	 * when everything is in the assessment object including a new PF value and
	 * a new edit
	 * 
	 * @param primitiveFieldID
	 * @param assessment
	 */
	public void addUpdatedAssessment(Integer primitiveFieldID, Assessment assessment) {
		updatedPFIDToAssessment.put(primitiveFieldID, assessment);
	}

}
