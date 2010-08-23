package com.solertium.gogoego.server.extensions.testing;

import org.gogoego.api.applications.GoGoEgoApplication;
import org.gogoego.api.applications.GoGoEgoApplicationActivator;
import org.gogoego.api.applications.GoGoEgoApplicationFactory;
import org.gogoego.api.applications.GoGoEgoApplicationManagement;
import org.gogoego.api.applications.GoGoEgoApplicationMetaData;

/**
 * The activator class controls the plug-in life cycle
 */
public class TestAppActivator extends GoGoEgoApplicationActivator {

	public GoGoEgoApplicationFactory getApplicationFactory() {
		return new GoGoEgoApplicationFactory() {
			public GoGoEgoApplicationManagement getManagement() {
				return null;
			}
			public GoGoEgoApplication newInstance() {
				return new TestApp();
			}
			public GoGoEgoApplicationMetaData getMetaData() {
				return new GoGoEgoApplicationMetaData() {
					public String getName() {
						return "Test App";
					}
					public String getDescription() {
						return "This is just a test application, it doesn't do much.";
					}
				};
			}
		};
	}
	
}
