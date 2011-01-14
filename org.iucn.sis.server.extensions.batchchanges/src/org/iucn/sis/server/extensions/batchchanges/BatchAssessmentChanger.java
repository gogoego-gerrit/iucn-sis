package org.iucn.sis.server.extensions.batchchanges;

import java.util.List;

import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.Field;
import org.iucn.sis.shared.api.models.User;

public class BatchAssessmentChanger {
	
	public static boolean changeAssessment(Assessment assessment, Assessment template, 
			boolean overwrite, boolean append, User username, List<String> fieldNames) {
		
		boolean changed = false;
		for (String fieldName : fieldNames) {
			Field templateField = template.getField(fieldName);
			
			Field currentFieldData = assessment.getField(fieldName);
			if  (currentFieldData == null) {
				currentFieldData = new Field(templateField.getName(), assessment);
				assessment.getField().add(currentFieldData);
			}
			changed |= templateField.copyInto(currentFieldData, append, overwrite);
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


}
