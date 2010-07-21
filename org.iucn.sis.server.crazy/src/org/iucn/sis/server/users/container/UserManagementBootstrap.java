package org.iucn.sis.server.users.container;

import org.restlet.Context;

import com.solertium.db.DBException;
import com.solertium.util.restlet.StandardServerComponent;

/**
 * UserManagementBootstrap.java
 * 
 * @author carl.scott <carl.scott@solertium.com>
 * 
 */
public class UserManagementBootstrap extends StandardServerComponent {

	public static void main(final String args[]) {
		UserManagementBootstrap component;
		try {
			component = new UserManagementBootstrap();
		} catch (DBException e) {
			System.err.println("Could not start DBSession: " + e.getMessage());
			return;
		}

		try {
			component.start();
		} catch (final Exception startupException) {
			startupException.printStackTrace();
		}
	}

	public UserManagementBootstrap() throws DBException {
		super(8888, 8443);
	}

	@Override
	protected void setupDefaultVirtualHost() {
		final Context childContext = getContext().createChildContext();

		String vfsroot = getInitProperties().getProperty("VFSROOT");
		if (vfsroot == null)
			vfsroot = "/var/sis/vfs";

		getDefaultHost().attach(new UserManagementApplication(childContext, vfsroot));
	}

}
