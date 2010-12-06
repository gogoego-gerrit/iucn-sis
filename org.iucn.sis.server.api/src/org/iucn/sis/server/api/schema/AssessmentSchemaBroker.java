package org.iucn.sis.server.api.schema;

import org.gogoego.api.plugins.GoGoEgo;
import org.gogoego.api.utils.PluginBroker;
import org.iucn.sis.server.api.schema.redlist.RedListAssessmentSchema;
import org.iucn.sis.shared.api.debug.Debug;

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
	
	public AssessmentSchema getAssessmentSchema(String id) {
		AssessmentSchemaFactory factory;
		try {
			factory = getPlugin(id);
		} catch (Throwable osgiError) {
			Debug.println("OSGi Error occurred: {0}", osgiError);
			return null;
		}
		
		try {
			return factory.newInstance();
		} catch (Throwable osgiError) {
			Debug.println("OSGi Error occurred: {0}", osgiError);
			return null;
		}
	}

}
