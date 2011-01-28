/**
 *
 */
package org.iucn.sis.server.extensions.user.application;

import org.gogoego.api.plugins.GoGoEgo;
import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.server.api.application.SimpleSISApplication;
import org.iucn.sis.server.extensions.user.resources.BatchUpdateRestlet;
import org.iucn.sis.server.extensions.user.resources.CSVImportRestlet;
import org.iucn.sis.server.extensions.user.resources.CustomFieldManager;
import org.iucn.sis.server.extensions.user.resources.DumpResource;
import org.iucn.sis.server.extensions.user.resources.ProfileSearchResource;
import org.iucn.sis.server.extensions.user.resources.UserRestlet;
import org.restlet.Context;

import com.solertium.db.ExecutionContext;

/**
 * UserManagementApplication.java
 * 
 * User Management application. Controls the database of SIS users. Created for
 * SIS-216.  Available online and offline
 * 
 * @author carl.scott <carl.scott@solertium.com>
 * 
 */
public final class UserManagementApplication extends SimpleSISApplication {

	@Override
	public void init() {		
		
//		addResource(UserResource.class, "/list", true, true, true);
		
		final BatchUpdateRestlet restlet =  new BatchUpdateRestlet(app.getContext());
		
		addResource(restlet, restlet.getPaths(), false);
		addResource(DumpResource.class, "/dump", false);
		addResource(CustomFieldManager.class, "/manager/custom", false);
		addResource(CustomFieldManager.class, "/manager/custom/{id}", false);
		addResource(ProfileSearchResource.class, "/browse/profile", false);
		
		addServiceToRouter(new UserRestlet(app.getContext()));
		addServiceToRouter(new CSVImportRestlet(app.getContext()));
	}


	public static UserManagementApplication getFromContext(Context context) {
		return (UserManagementApplication) GoGoEgo.get().getApplication(context, "org.iucn.sis.server.extensions.user");
	}

	public ExecutionContext getExecutionContext() {
		return SIS.get().getExecutionContext();
	}


}
