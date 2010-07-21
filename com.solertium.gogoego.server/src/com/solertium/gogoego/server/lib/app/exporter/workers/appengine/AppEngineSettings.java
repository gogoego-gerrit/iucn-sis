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
package com.solertium.gogoego.server.lib.app.exporter.workers.appengine;

import java.util.ArrayList;

import com.solertium.gogoego.server.lib.app.exporter.utils.SimpleExporterSettings;

/**
 * AppEngineSettings.java
 * 
 * A take-no-bs HashMap that does not allow for null values. Has helper
 * functions to fetch known properties
 * 
 * 
 */
public class AppEngineSettings {

	private static final long serialVersionUID = 1L;
	
	private final SimpleExporterSettings settings;
	
	public AppEngineSettings(SimpleExporterSettings settings) {
		this.settings = settings;
	}
	
	public boolean setProperty(String key, String value) {
		return settings.setProperty(key, value);
	}

	public String getGoGoKey() {
		return settings.get("gogoKey");
	}

	public String getWebAddress() {
		return (getApplicationID() == null) ? null : 
			"http://" + getApplicationID() + ".appspot.com/gogoUpdate";
	}

	public String getInstallAddress() {
		return "https://" + getApplicationID() + ".appspot.com/.ggeinstall";
	}

	public String getUninstallAddress() {
		return "https://" + getApplicationID() + ".appspot.com/.ggeuninstall";
	}

	public String getTagAddress() {
		return "http://" + getApplicationID() + ".appspot.com/apps/tags";
	}

	public String getLinkAddress() {
		return settings.get("linkaddress");
	}

	public String getApplicationID() {
		return settings.get("applicationID");
	}

	public String toPublicXML() {
		final ArrayList<String> exclude = new ArrayList<String>();
		exclude.add("gogoKey");
		
		return settings.toXML(exclude);
	}

	public String toInstallXML() {
		final ArrayList<String> exclude = new ArrayList<String>(settings.keySet());
		exclude.remove("gogoKey");
		
		return settings.toXML(exclude);
	}

	/**
	 * Returns the appengine webspot
	 * 
	 * @return
	 */
	public String getSiteAddress() {
		return "http://" + getApplicationID() + ".appspot.com";
	}

}
