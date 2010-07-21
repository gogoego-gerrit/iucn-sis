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
package com.solertium.gogoego.server.lib.settings.base;

import org.restlet.Context;

import com.solertium.gogoego.server.ServerApplication;
import com.solertium.gogoego.server.lib.settings.addons.AddOnsSettings;
import com.solertium.gogoego.server.lib.settings.personal.PersonalSettings;
import com.solertium.gogoego.server.lib.settings.site.SiteSettings;

/**
 * SimpleSettingsWorkerFactory.java
 * 
 * @author carl.scott
 * 
 */
public class SimpleSettingsWorkerFactory {

	public static SimpleSettingsWorker getWorker(String key, Context context) {
		SimpleSettingsWorker worker = null;
		if (key.equals("personal"))
			worker = new PersonalSettings(ServerApplication.getFromContext(context).getVFS());
		else if (key.equals("site"))
			worker = new SiteSettings(ServerApplication.getFromContext(context).getVFS());
		else if (key.equals("addons"))
			worker = new AddOnsSettings(ServerApplication.getFromContext(context).getVFS());

		return worker;
	}

}
