/*
 * Copyright (C) 2007-2009 Solertium Corporation
 *
 * This file is part of the open source GoGoEgo project.
 *
 * Unless you have been granted a different license in writing by the
 * copyright holders for GoGoEgo, you may only modify or redistribute
 * this code under the terms of one of the following licenses:
 * 
 * 1) The Eclipse Public License, v.1.0
 *    http://www.eclipse.org/legal/epl-v10.html
 *
 * 2) The GNU General Public License, version 2 or later
 *    http://www.gnu.org/licenses
 */
package com.solertium.util.restlet.usermodel.test;

import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.routing.Router;

import com.solertium.util.restlet.HasInstanceId;
import com.solertium.util.restlet.usermodel.core.ProfileManagerFactory;
import com.solertium.util.restlet.usermodel.core.ProfileResource;
import com.solertium.util.restlet.usermodel.managers.H2ProfileManager;

/**
 * TestApplication.java
 * 
 * Registers a profile manager and attaches the resource for testing.
 * 
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a
 *         href="http://www.solertium.com">Solertium Corporation</a>
 *
 */
public class TestApplication extends Application implements HasInstanceId {
	
	public TestApplication(final Context context) {
		super(context);
	}
	
	@Override
	public Restlet createRoot() {
		final Router publicRouter = new Router(getContext());
		H2ProfileManager profileManager = new H2ProfileManager(getInstanceId());
		ProfileManagerFactory.impl.register(getInstanceId(), profileManager);
		publicRouter.attach("/profile/{check-username}", ProfileResource.class);
		return publicRouter;
	}
	
	public String getInstanceId() {
		return "test";
	}

}
