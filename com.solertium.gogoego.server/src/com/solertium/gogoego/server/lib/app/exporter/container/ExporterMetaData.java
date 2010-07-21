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
package com.solertium.gogoego.server.lib.app.exporter.container;

import org.gogoego.api.applications.GoGoEgoApplicationMetaData;

/**
 * ExporterMetaData.java
 * 
 * Meta-information for the Exporter Application
 * 
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a
 *  href="http://www.solertium.com">Solertium Corporation</a>
 */
public class ExporterMetaData implements GoGoEgoApplicationMetaData {
	
	public String getName() {
		return "Exporter";
	}

	public String getDescription() {
		return "Allows users to export their GoGoEgo in a number of ways, " +
			"such as to flat portable sites that can be run offline or " +
			"to another existing GoGoEgo site.  There are also integrations " +
			"with third-party platforms such as Google AppEngine, with a " +
			"means of having other pluggable export points.";
	}

}
