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
package org.gogoego.api.utils;

import java.io.File;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import com.solertium.util.ImmutableProperties;
import com.solertium.util.restlet.StandardServerComponentProperties;

public class ProductProperties extends StandardServerComponentProperties {
	
	public static ProductProperties impl = new ProductProperties();
	
	/**
	 * use <code>GoGoEgo.getInitProperties()</code> instead
	 */
	public static Properties getProperties() {
		return impl._load();
	}
	
	private File workingDirectory;
	private AtomicBoolean loaded = new AtomicBoolean(false);
	
	private Properties _load() {
		if (!loaded.get()) {
			loaded.set(true);
			System.out.println("Initializing GoGoEgo Properties");
			refresh();
			System.out.println("Using system properties? " + usesSystemProperties());
		}		
		return getInitProperties();
	}

	public void setWorkingDirectory(File workingDirectory) {
		this.workingDirectory = workingDirectory;
	}
	
	public File getWorkingDirectory() {
		return workingDirectory;
	}
	
	protected File loadFile() {
		final File wd = getWorkingDirectory();
		System.out.println("Working directory is" + wd.getPath());
		System.out.println("Config is " + config);
		
		if (config == null)
			return getConfigFile(wd);
		
		if (config.indexOf(File.separatorChar) == -1) {
			//Check the working directory, for eclipse...
			File f = new File(wd, config);
			if (!f.exists() && wd.getParentFile() != null) {
				//Check the root directory, for build
				f = new File(wd.getParentFile(), config);
				if (!f.exists()) {
					//Check the configuration directory for it!
					f = new File(new File(f, "configuration"), config);
				}
			}
			return f;
		}
		else
			return new File(config);
	}
	
	/**
	 * Overidden to use ImmutableProperties, set any default properties 
	 * here.
	 */
	protected void setProperties(Properties properties) {
		final String ivmroot = properties.getProperty("GOGOEGO_VMROOT");
		if (ivmroot == null)
			properties.setProperty("GOGOEGO_VMROOT","workspace");
		super.setProperties(new ImmutableProperties(properties));
	}
}
