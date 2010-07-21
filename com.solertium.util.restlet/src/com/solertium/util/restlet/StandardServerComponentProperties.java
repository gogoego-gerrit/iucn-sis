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
package com.solertium.util.restlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import com.solertium.util.CurrentBinary;

/**
 * StandardServerComponentProperties.java
 * 
 * Standard properties from component_config.properties, or something 
 * like it.
 * 
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a
 *         href="http://www.solertium.com">Solertium Corporation</a>
 *
 */
public class StandardServerComponentProperties {
	
	public static final String CONFIG_FILE_PROPERTY = "COMPONENT_CONFIG";

	protected final String config;
	
	private Properties initProperties;
	private boolean usesSystemProperties = false;	
	
	public StandardServerComponentProperties() {
		this(System.getProperties().getProperty(CONFIG_FILE_PROPERTY));
	}
	
	public StandardServerComponentProperties(String config) {
		this.config = config;
	}
	
	public File getWorkingDirectory() {
		File file = CurrentBinary.getDirectory(this);
		if (file.getAbsolutePath().endsWith(File.separatorChar + "bin"))
			file = file.getParentFile();
		return file;
	}
	
	public void refresh() {
		Properties properties = System.getProperties();
		
		final File cf = loadFile();
		if (cf.exists()) {
			try {
				properties = new Properties();
				properties.load(new FileInputStream(cf));
			} catch (final FileNotFoundException fileNotFound) {
				throw new RuntimeException(
						"Config file was not found although exists() returned true",
						fileNotFound);
			} catch (final IOException ioException) {
				throw new RuntimeException(
						"Config file could not be fully parsed due to an I/O exception",
						ioException);
			} finally {
				if (properties==null) {
					properties = System.getProperties();
					usesSystemProperties = true;
				}
				setProperties(properties);
			}
		}
		else {
			setProperties(System.getProperties());
			usesSystemProperties = true;
		}
	}
	
	protected void setProperties(Properties properties) {
		initProperties = properties; 
	}
	
	protected File loadFile() {
		/*
		 * If config null, try to find one.  Otherwise, 
		 * if a full path is given, 
		 */
		final File wd = getWorkingDirectory();
		return (config == null ? getConfigFile(wd) : 
			config.indexOf(File.separatorChar) == -1 ? 
				new File(wd, config) : new File(config));
	}

	protected File getConfigFile(File workingDirectory){
		System.out.println("Working directory is " + workingDirectory.getAbsolutePath());
		File localProps = new File(workingDirectory,"local_config.properties");
		if(localProps.exists()) return localProps;
		return new File(workingDirectory,"component_config.properties");
	}
	
	public Properties getInitProperties() {
		return initProperties;
	}
	
	public boolean usesSystemProperties() {
		return usesSystemProperties;
	}
}
