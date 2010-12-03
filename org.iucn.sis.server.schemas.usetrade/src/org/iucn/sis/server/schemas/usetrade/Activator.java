package org.iucn.sis.server.schemas.usetrade;

import org.iucn.sis.server.api.schema.AssessmentSchema;
import org.iucn.sis.server.api.schema.AssessmentSchemaActivator;
import org.iucn.sis.server.api.schema.AssessmentSchemaFactory;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AssessmentSchemaActivator {

	@Override
	public AssessmentSchemaFactory getService() {
		return new AssessmentSchemaFactory() {
			public AssessmentSchema newInstance() {
				return new UseTradeAssessmentSchema();
			}
		};
	}

}
