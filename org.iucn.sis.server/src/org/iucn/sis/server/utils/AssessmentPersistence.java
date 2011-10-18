package org.iucn.sis.server.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.Session;
import org.iucn.sis.server.api.persistance.SISPersistentManager;
import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
import org.iucn.sis.shared.api.debug.Debug;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.AssessmentChange;
import org.iucn.sis.shared.api.models.Edit;
import org.iucn.sis.shared.api.models.Field;
import org.iucn.sis.shared.api.models.Notes;
import org.iucn.sis.shared.api.models.PrimitiveField;
import org.iucn.sis.shared.api.models.Reference;

import com.solertium.util.events.ComplexListener;

public class AssessmentPersistence {
	
	private final List<AssessmentChange> changeSet;
	private final Session session;
	private final Assessment target;
	
	private ComplexListener<Field> deleteFieldListener;
	private ComplexListener<PrimitiveField> deletePrimitiveFieldListener;
	
	private boolean allowAdd = true;
	private boolean allowDelete = true;
	private boolean allowManageReferences = true;
	private boolean allowManageNotes = true;
	
	public AssessmentPersistence(Session session, Assessment target) {
		this.session = session;
		this.target = target;
		this.changeSet = new ArrayList<AssessmentChange>();
	}
	
	public void addChange(AssessmentChange change) {
		changeSet.add(change);
	}
	
	public List<AssessmentChange> getChangeSet() {
		return changeSet;
	}
	
	public void setDeleteFieldListener(ComplexListener<Field> deleteFieldListener) {
		this.deleteFieldListener = deleteFieldListener;
	}
	
	public void setDeletePrimitiveFieldListener(ComplexListener<PrimitiveField> deletePrimitiveFieldListener) {
		this.deletePrimitiveFieldListener = deletePrimitiveFieldListener;
	}
	
	public void setAllowAdd(boolean allowAdd) {
		this.allowAdd = allowAdd;
	}
	
	public void setAllowDelete(boolean allowDelete) {
		this.allowDelete = allowDelete;
	}
	
	public void setAllowManageReferences(boolean allowManageReferences) {
		this.allowManageReferences = allowManageReferences;
	}
	
	public void setAllowManageNotes(boolean allowManageNotes) {
		this.allowManageNotes = allowManageNotes;
	}
	
	public void sink(Assessment source) throws PersistentException {
		sink(source.getField());
	}
	
	public void sink(Set<Field> sourceFields) throws PersistentException {
		Map<Integer, Field> existingFields = mapFields(target.getField());
		
		for (Field sourceField : sourceFields) {
			if (sourceField.getId() == 0) {
				if (allowAdd) {
					sourceField.setAssessment(target);
					//FieldDAO.save(sourceField);
					target.getField().add(sourceField);
					changeSet.add(createAddChange(sourceField));
				}
			}
			else {
				Field targetField = existingFields.remove(sourceField.getId());
				if (targetField != null) {
					AssessmentChange pendingEdit = createEditChange(targetField, sourceField);
					AssessmentChange pendingDelete = createDeleteChange(targetField);
					sink(sourceField, targetField);
					if (isBlank(targetField)) {
						if (allowDelete) {
							changeSet.add(pendingDelete);
							deleteField(targetField);
						}
					}
					else
						changeSet.add(pendingEdit);
				}
			}
		}
		
		/*
		 * Only delete top-level fields
		 */
		if (allowDelete) {
			for (Field field : existingFields.values()) {
				if (field.getParent() == null) {
					changeSet.add(createDeleteChange(field));
					deleteField(field);
				}
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private void sink(Field source, Field target) throws PersistentException {
		{
			if (allowManageReferences) {
				Map<Integer, Reference> existingReferences = mapFields(target.getReference());
			
				for (Reference sourceReference : source.getReference()) {
					//Should never be the case...
					if (sourceReference.getId() == 0) {
						sourceReference.getField().add(target);
						target.getReference().add(sourceReference);
					}
					else {
						Reference targetReference = existingReferences.remove(sourceReference.getId());
						if (targetReference == null)
							target.getReference().add(SISPersistentManager.instance().loadObject(session, Reference.class, sourceReference.getId()));
					}
				}
				
				target.getReference().removeAll(existingReferences.values());
			}
			
			if (allowManageNotes) {
				Map<Integer, Notes> existingNotes = mapFields(target.getNotes());
				
				for (Notes sourceNotes : source.getNotes()) {
					//Should never be the case...
					if (sourceNotes.getId() == 0) {
						sourceNotes.setField(target);
						target.getNotes().add(sourceNotes);
					}
					else {
						Notes targetNotes = existingNotes.remove(sourceNotes.getId());
						if (targetNotes == null)
							target.getNotes().add(SISPersistentManager.instance().loadObject(session, Notes.class, sourceNotes.getId()));
					}
				}
				
				target.getNotes().removeAll(existingNotes.values());
			}
		}
		{
			Map<Integer, PrimitiveField> existingFields = mapFields(target.getPrimitiveField());
		
			for (PrimitiveField sourceField : source.getPrimitiveField()) {
				if (sourceField.getId() == 0) {
					sourceField.setField(target);
					//PrimitiveFieldDAO.save(sourceField);
					target.getPrimitiveField().add(sourceField);
				}
				else {
					PrimitiveField targetField = existingFields.remove(sourceField.getId());
					if (targetField != null)
						targetField.setRawValue(sourceField.getRawValue());
				}
			}
			
			for (PrimitiveField field : existingFields.values())
				deletePrimitiveField(field);
		}
		{
			Map<Integer, Field> existingFields = mapFields(target.getFields());
			
			for (Field sourceField : source.getFields()) {
				if (sourceField.getId() == 0) {
					sourceField.setParent(target);
					//FieldDAO.save(sourceField);
					target.getFields().add(sourceField);
				}
				else {
					Field targetField = existingFields.remove(sourceField.getId());
					if (targetField != null)
						sink(sourceField, targetField);
				}
			}
			
			for (Field field : existingFields.values()) {
				Debug.println("Deleting existing field {0}: {1}", field.getId(), field.getName());
				deleteField(field);
			}
		}
		
		//FieldDAO.save(target);
	}
	
	private boolean isBlank(Field field) {
		return field.getReference().isEmpty() && field.getNotes().isEmpty() && !field.hasData();
	}
	
	private void deleteField(Field field) {
		if (deleteFieldListener != null)
			deleteFieldListener.handleEvent(field);
	}
	
	private void deletePrimitiveField(PrimitiveField field) {
		if (deletePrimitiveFieldListener != null)
			deletePrimitiveFieldListener.handleEvent(field);
	}
	
	public AssessmentChange createAddChange(Field newField) {
		AssessmentChange change = new AssessmentChange();
		change.setAssessment(target);
		change.setFieldName(newField.getName());
		change.setOldField(null);
		change.setNewField(deepCopy(newField));
		change.setType(AssessmentChange.ADD);
		
		return change;
	}
	
	public AssessmentChange createDeleteChange(Field removedField) {
		AssessmentChange change = new AssessmentChange();
		change.setAssessment(target);
		change.setFieldName(removedField.getName());
		change.setOldField(deepCopy(removedField));
		change.setNewField(null);
		change.setType(AssessmentChange.DELETE);
		
		return change;
	}
	
	public AssessmentChange createEditChange(Field oldField, Field newField) {
		AssessmentChange change = new AssessmentChange();
		change.setAssessment(target);
		change.setFieldName(oldField.getName());
		change.setOldField(deepCopy(oldField));
		change.setNewField(deepCopy(newField));
		change.setType(AssessmentChange.EDIT);
		
		return change;
	}
	
	private Field deepCopy(Field source) {
		Field target = new Field(source.getName(), null);
		for (PrimitiveField prim : source.getPrimitiveField()) {
			PrimitiveField copy = prim.deepCopy(false);
			copy.setField(target);
			target.getPrimitiveField().add(copy);
		}
		for (Field child : source.getFields()) {
			Field copy = deepCopy(child);
			copy.setParent(target);
			target.getFields().add(copy);
		}
		return target;
	}
	
	@SuppressWarnings("unchecked")
	private <X> Map<Integer, X> mapFields(Collection<X> fields) {
		Map<Integer, X> map = new HashMap<Integer, X>();
		for (X field : fields) {
			if (field instanceof Field) 
				map.put(((Field)field).getId(), field);
			else if (field instanceof PrimitiveField)
				map.put(((PrimitiveField)field).getId(), field);
			else if (field instanceof Reference)
				map.put(((Reference)field).getId(), field);
			else if (field instanceof Notes)
				map.put(((Notes)field).getId(), field);
		}
		return map;
	}
	
	public void saveChanges(Assessment assessment, Edit edit) {
		ChangeTracker tracker = new ChangeTracker(assessment.getId(), edit.getId(), getChangeSet());
		new Thread(tracker).start();
	}
	
	public static class ChangeTracker implements Runnable {
	
		private Integer assessmentID, editID;
		private List<AssessmentChange> changes;
		
		public ChangeTracker(Integer assessmentID, Integer editID, List<AssessmentChange> changes) {
			this.assessmentID = assessmentID;
			this.editID = editID;
			this.changes = changes;
		}
		
		@Override
		public void run() {
			try {
				execute();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		public void execute() throws Exception {
			Session session = SISPersistentManager.instance().openSession();
			session.beginTransaction();
			
			Debug.println("Saving change for assessment {0} with edit {1}", assessmentID, editID);
			
			Assessment assessment = SISPersistentManager.instance().getObject(session, Assessment.class, assessmentID);
			Edit edit = getEdit(session, editID);
			
			for (AssessmentChange change : changes) {
				if (AssessmentChange.EDIT == change.getType()) {
					String oldXML = change.getOldField().toXML();
					String newXML = change.getNewField().toXML();
					
					if (oldXML.equals(newXML))
						continue;
				}
				change.setAssessment(assessment);
				change.setEdit(edit);
				
				if (change.getOldField() != null)
					session.save(change.getOldField());
				if (change.getNewField() != null)
					session.save(change.getNewField());
				
				session.save(change);
			}
			
			session.getTransaction().commit();
		}
		
		private Edit getEdit(Session session, Integer id) throws Exception {
			Edit edit = null;
			int tries = 0, max = 10;
			while (edit == null) {
				Thread.sleep(2000);
				edit = SISPersistentManager.instance().getObject(session, Edit.class, editID);
				if (++tries > max)
					break;
			}
			if (edit == null)
				throw new Exception("Taking too long to get edit " + id);
			return edit;
		}
		
	}

}
