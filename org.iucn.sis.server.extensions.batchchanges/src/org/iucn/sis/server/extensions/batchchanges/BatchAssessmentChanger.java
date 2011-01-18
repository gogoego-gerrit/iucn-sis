package org.iucn.sis.server.extensions.batchchanges;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.Field;
import org.iucn.sis.shared.api.models.PrimitiveField;

public class BatchAssessmentChanger {
	
	public static boolean changeAssessment(Assessment target, Assessment template, 
			boolean overwrite, boolean append, List<String> fieldNames) {
		
		boolean changed = false;
		for (String fieldName : fieldNames) {
			Field sourceField = template.getField(fieldName);
			if (sourceField == null)
				continue;
			
			Field targetField = target.getField(fieldName);
			if (targetField == null) {
				targetField = new Field(fieldName, target);
				target.getField().add(targetField);
			}
			changed |= copyInto(sourceField, targetField, append, overwrite);
		}
		
		/*for (Field field : newData.getField()) {
			Field oldField = assessment.getField(field.getName());
			if (oldField == null) {
				oldField = new Field(field.getName(), assessment);
			}
			
			changed |= field.copyInto(oldField, append, overwrite);
		}*/

		return changed;
	}
	
	@SuppressWarnings("unchecked")
	private static boolean copyInto(Field source, Field target, boolean append, boolean overwrite) {
		if (source.equals(target))
			return false;
		
		//If data exists...
		if (target.hasData()) {
			//Must specify overwrite
			if (!overwrite)
				if (!target.isNarrativeField())
					return false;
		
			//Must also specify append for narratives
			if (target.isNarrativeField() && !append)   
				return false;
		}
		
		System.out.println("Working...");
		
		if (overwrite) {
			target.setFields(new HashSet<Field>());
			target.setPrimitiveField(new HashSet<PrimitiveField>());
		}
		
		Map<String,PrimitiveField> keyToPF = new HashMap<String, PrimitiveField>(target.getKeyToPrimitiveFields());
		for (PrimitiveField pf : source.getPrimitiveField()) {
			if (keyToPF.containsKey(pf.getName())) {
				if (overwrite) {
					pf.copyInto(keyToPF.get(pf.getName()));
				} else if (append && target.isNarrativeField()) {
					keyToPF.get(pf.getName()).appendValue(pf.getValue());
				}
			} else {
				PrimitiveField newPrim = pf.deepCopy();
				newPrim.setField(target);
				target.getPrimitiveField().add(newPrim);
			}
		}
		
		
		Map<String, Field> keyToField = target.getKeyToFields();
		for (Field f : source.getFields()) {
			if (!keyToField.containsKey(f.getName())) {
				target.getFields().add(f.deepCopy(false));
			} 
		}
		return true;
	}


}
