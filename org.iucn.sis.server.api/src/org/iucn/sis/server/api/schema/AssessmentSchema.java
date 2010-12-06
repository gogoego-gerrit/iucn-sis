package org.iucn.sis.server.api.schema;

import org.w3c.dom.Document;

public interface AssessmentSchema {
	
	public String getName();
	
	public String getDescription();
	
	public String getTablePrefix();
	
	public Document getViews();
	
	public Document getField(String fieldName);

}
