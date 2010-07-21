package org.iucn.sis.server.ref;

import com.solertium.db.DBException;
import com.solertium.util.restlet.DesktopIntegration;
import com.solertium.util.restlet.StandardServerComponent;

public class MiniReferenceServer extends StandardServerComponent {

	public static void main(final String[] args) {
		try {
			DesktopIntegration.launch("SIS Mini Reference Server", "/reference/0079C44FA705C6314B6073E6766011E",
					new MiniReferenceServer(41141, 41142));
		} catch (DBException dbx) {
			dbx.printStackTrace();
		}
	}

	public MiniReferenceServer(int http, int https) throws DBException {
		super(http, https);
	}

	@Override
	protected void setupDefaultVirtualHost() {
		try {
			getDefaultHost().attach(new ReferenceApplication(getContext()));
		} catch (DBException dbx) {
			throw new RuntimeException("Failure to initialize databases at startup", dbx);
		}
	}

}
