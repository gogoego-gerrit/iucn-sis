package org.iucn.sis.server.api.schema.redlist;

import org.iucn.sis.server.api.fields.definitions.FieldDefinitionLoader;
import org.iucn.sis.server.api.schema.AssessmentSchema;
import org.w3c.dom.Document;

public class RedListAssessmentSchema implements AssessmentSchema {

	@Override
	public String getDescription() {
		return "Red List Assessments";
	}
	
	@Override
	public String getTablePrefix() {
		return "";
	}

	@Override
	public Document getField(String fieldName) {
		return FieldDefinitionLoader.get(fieldName);
	}

	@Override
	public String getName() {
		return "Red List Assessments";
	}

	@Override
	public Document getViews() {
		return DocumentLoader.getView();
	}

}
