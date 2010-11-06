/**
 *
 */
package org.iucn.sis.server.extensions.user.application;

import org.gogoego.api.plugins.GoGoEgo;
import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.server.api.application.SISApplication;
import org.iucn.sis.server.extensions.user.resources.BatchUpdateRestlet;
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
 * SIS-216
 * 
 * @author carl.scott <carl.scott@solertium.com>
 * 
 */
public final class UserManagementApplication extends SISApplication {

	@Override
	public void init() {		
		
		addServerResource(UserRestlet.class, UserRestlet.getPaths(), true, true, false);
//		addResource(UserResource.class, "/list", true, true, true);
		
		final BatchUpdateRestlet restlet =  new BatchUpdateRestlet(app.getContext());
		
		addResource(restlet, restlet.getPaths(), true, true, false);
		addResource(DumpResource.class, "/dump", true, true, false);
		addResource(CustomFieldManager.class, "/manager/custom", true, true, false);
		addResource(CustomFieldManager.class, "/manager/custom/{id}", true, true, false);
		addResource(ProfileSearchResource.class, "/browse/profile", true, true, false);
		
	}


	public static UserManagementApplication getFromContext(Context context) {
		return (UserManagementApplication) GoGoEgo.get().getApplication(context, "org.iucn.sis.server.extensions.user");
	}

	public ExecutionContext getExecutionContext() {
		return SIS.get().getExecutionContext();
	}


}
