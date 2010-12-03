package org.iucn.sis.server.api.schema;

import org.gogoego.api.plugins.GoGoEgo;
import org.gogoego.api.utils.PluginBroker;
import org.iucn.sis.server.api.schema.redlist.RedListAssessmentSchema;

public class AssessmentSchemaBroker extends PluginBroker<AssessmentSchemaFactory> {
	
	public AssessmentSchemaBroker() {
		super(GoGoEgo.get().getBundleContext(), AssessmentSchemaFactory.class.getName());
		addHeaderKey("Bundle-Name");
		
		addLocalReference("org.iucn.sis.server.schemas.redlist", new AssessmentSchemaFactory() {
			public AssessmentSchema newInstance() {
				return new RedListAssessmentSchema();
			}
		});
	}

}
