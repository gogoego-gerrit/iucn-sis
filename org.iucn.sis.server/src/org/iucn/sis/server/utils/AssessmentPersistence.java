package org.iucn.sis.server.utils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.Field;
import org.iucn.sis.shared.api.models.PrimitiveField;

import com.solertium.util.events.ComplexListener;

public class AssessmentPersistence {
	
	private ComplexListener<Field> deleteFieldListener;
	private ComplexListener<PrimitiveField> deletePrimitiveFieldListener;
	
	public void setDeleteFieldListener(ComplexListener<Field> deleteFieldListener) {
		this.deleteFieldListener = deleteFieldListener;
	}
	
	public void setDeletePrimitiveFieldListener(ComplexListener<PrimitiveField> deletePrimitiveFieldListener) {
		this.deletePrimitiveFieldListener = deletePrimitiveFieldListener;
	}
	
	public void sink(Assessment source, Assessment target) {
		Map<Integer, Field> existingFields = mapFields(target.getField());
		
		for (Field sourceField : source.getField()) {
			if (sourceField.getId() == 0) {
				sourceField.setAssessment(target);
				//FieldDAO.save(sourceField);
				target.getField().add(sourceField);
			}
			else {
				Field targetField = existingFields.remove(sourceField.getId());
				if (targetField != null) {
					sink(sourceField, targetField);
					if (!targetField.hasData())
						deleteField(targetField);
				}
			}
		}
		
		/*
		 * Only delete top-level fields
		 */
		for (Field field : existingFields.values())
			if (field.getParent() == null)
				deleteField(field);
	}
	
	@SuppressWarnings("unchecked")
	private void sink(Field source, Field target) {
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
			
			for (Field field : existingFields.values())
				deleteField(field);
		}
		
		//FieldDAO.save(target);
	}
	
	private void deleteField(Field field) {
		if (deleteFieldListener != null)
			deleteFieldListener.handleEvent(field);
	}
	
	private void deletePrimitiveField(PrimitiveField field) {
		if (deletePrimitiveFieldListener != null)
			deletePrimitiveFieldListener.handleEvent(field);
	}
	
	@SuppressWarnings("unchecked")
	private <X> Map<Integer, X> mapFields(Collection<X> fields) {
		Map<Integer, X> map = new HashMap<Integer, X>();
		for (X field : fields) {
			if (field instanceof Field) 
				map.put(((Field)field).getId(), field);
			else if (field instanceof PrimitiveField)
				map.put(((PrimitiveField)field).getId(), field);
		}
		return map;
	}

}
