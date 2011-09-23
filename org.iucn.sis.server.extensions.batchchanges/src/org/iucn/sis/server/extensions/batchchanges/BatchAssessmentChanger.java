package org.iucn.sis.server.extensions.batchchanges;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.Session;
import org.iucn.sis.server.api.persistance.SISPersistentManager;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.Field;
import org.iucn.sis.shared.api.models.PrimitiveField;
import org.iucn.sis.shared.api.models.Reference;

public class BatchAssessmentChanger {
	
	public enum BatchChangeMode {
		APPEND(false), OVERWRITE(true), OVERWRITE_IF_BLANK(true);
		
		private boolean canOverwrite;
		
		private BatchChangeMode(boolean canOverwrite) {
			this.canOverwrite = canOverwrite;
		}
		
		public boolean canOverwrite() {
			return canOverwrite;
		}
	}
	
	public static boolean changeAssessment(Session session, Assessment target, Assessment template, 
			BatchChangeMode mode, List<String> fieldNames) {
		
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
			changed |= copyInto(session, sourceField, targetField, mode);
		}

		return changed;
	}
	
	@SuppressWarnings("unchecked")
	private static boolean copyInto(final Session session, Field source, Field target, BatchChangeMode mode) {
		if (source.equals(target))
			return false;
		
		boolean hasData = target.hasData();
		
		if (hasData) {
			if (BatchChangeMode.OVERWRITE_IF_BLANK.equals(mode)) {
				/*
				 * Only overwrite if blank, and there is data.
				 */
				return false;
			}
			else if (BatchChangeMode.APPEND.equals(mode)) {
				/*
				 * Bail if this is not a narrative field, which is 
				 * the only type of field that you can append data to.
				 */
				if (!target.isNarrativeField() && !target.isClassificationScheme())
					return false;
			}
			else if (BatchChangeMode.OVERWRITE.equals(mode)) {
				/*
				 * Overwrite means overwrite, so this is OK.
				 */
			}
		}
		
		if (BatchChangeMode.OVERWRITE.equals(mode) || (!hasData && BatchChangeMode.OVERWRITE_IF_BLANK.equals(mode))) {
			target.setFields(new HashSet<Field>());
			target.setPrimitiveField(new HashSet<PrimitiveField>());
			
			Map<String, Field> keyToField = target.getKeyToFields();
			for (Field f : source.getFields()) {
				if (!keyToField.containsKey(f.getName())) {
					target.getFields().add(f.deepCopy(false));
				} 
			}
		}
		
		Map<String,PrimitiveField> keyToPF = new HashMap<String, PrimitiveField>(target.getKeyToPrimitiveFields());
		for (PrimitiveField pf : source.getPrimitiveField()) {
			if (keyToPF.containsKey(pf.getName())) {
				if (mode.canOverwrite()) {
					pf.copyInto(keyToPF.get(pf.getName()));
				} else if (target.isNarrativeField()) {
					String currentValue = keyToPF.get(pf.getName()).getRawValue();
					if (currentValue == null)
						currentValue = pf.getRawValue();
					else
						currentValue += "<br/><br/>" + pf.getRawValue();
					keyToPF.get(pf.getName()).setRawValue(currentValue);
				}
			} else {
				PrimitiveField newPrim = pf.deepCopy(false);
				newPrim.setField(target);
				target.getPrimitiveField().add(newPrim);
			}
		}
		
		if (BatchChangeMode.APPEND.equals(mode)) {
			String lookupFieldName = source.getName() + "Lookup";
			
			/*
			 * Going through subfields here...
			 */
			Set<String> selectedLookups = new HashSet<String>();
			for (Field field : target.getFields()) {
				PrimitiveField prim = field.getPrimitiveField(lookupFieldName);
				if (prim != null) {
					String value = prim.getRawValue();
					if (value != null && !"0".equals(value))
						selectedLookups.add(value);
				}
			}
			
			for (Field f : source.getFields()) {
				PrimitiveField prim = f.getPrimitiveField(lookupFieldName);
				if (prim == null)
					continue;
					
				//The lookup value.
				String value = prim.getRawValue();
				if (value != null && !"0".equals(value) && selectedLookups.add(value)) {
					Field newField = f.deepCopy(false, new Field.ReferenceCopyHandler() {
						public Reference copyReference(Reference source) {
							try {
								return SISPersistentManager.instance().getObject(session, Reference.class, source.getId());
							} catch (Exception e) {
								return null;
							}
						}
					});
					for (Field child : newField.getFields())
						child.setParent(newField);
					for (PrimitiveField child : newField.getPrimitiveField())
						child.setField(newField);
					newField.setParent(target);
					newField.setAssessment(target.getAssessment());
					
					target.getFields().add(newField);
				}
			}
		}
		
		return true;
	}

}
