package org.iucn.sis.server.api.schema;

import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;

public abstract class AssessmentSchemaActivator implements BundleActivator {
	
	public abstract AssessmentSchemaFactory getService();

	public final void start(BundleContext context) throws Exception {
		final Hashtable<String, String> props = new Hashtable<String, String>();
		props.put(Constants.SERVICE_PID, getClass().getName());
		
		final AssessmentSchemaFactory service = getService();
		context.registerService(AssessmentSchemaFactory.class.getName(), service, props);
	}

	public final void stop(BundleContext context) throws Exception {
		// TODO Auto-generated method stub

	}

}
