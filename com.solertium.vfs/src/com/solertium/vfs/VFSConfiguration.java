/*******************************************************************************
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
 *     http://www.gnu.org/licenses
 ******************************************************************************/
package com.solertium.vfs;

import java.io.File;
import java.util.Properties;

public class VFSConfiguration {

	public static VFS configure(final Properties properties, final File location)
			throws NotFoundException {
		try {
			final Class<?> driver = Class.forName(properties
					.getProperty("vfs.driver"));
			final PropertyDrivenVFS vfs = (PropertyDrivenVFS) driver
					.newInstance();
			vfs.configure(properties, location);
			return vfs;
		} catch (final ClassNotFoundException cnfe) {
			throw new NotFoundException(cnfe);
		} catch (final IllegalAccessException iae) {
			throw new NotFoundException(iae);
		} catch (final InstantiationException ie) {
			throw new NotFoundException(ie);
		} catch (final ConfigurationException ce) {
			throw new NotFoundException(ce);
		}
	}

}
