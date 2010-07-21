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
package org.gogoego.api.applications;

import org.restlet.Application;
import org.restlet.Context;

/**
 * BasicGoGoEgoApplicationFactory.java
 * 
 * Use this to quickly implement a factory for your GoGoEgoApplicationActivator
 * 
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a
 *         href="http://www.solertium.com">Solertium Corporation</a>
 *
 */
public abstract class BasicGoGoEgoApplicationFactory implements GoGoEgoApplicationFactory {
	
	public abstract Application getService(Context context);

	public GoGoEgoApplicationManagement getManagement() {
		return null;
	}

	public GoGoEgoApplicationMetaData getMetaData() {
		return new GoGoEgoApplicationMetaData() {
			public String getDescription() {
				return "No Description Provided";	
			}
			public String getName() {
				return "No Name Provided";				
			}
		};
	}

	public GoGoEgoApplication newInstance() {
		return new BasicGoGoEgoApplication() {
			public Application getApplication(Context context) {
				return getService(context);
			}
		};
	}

}
