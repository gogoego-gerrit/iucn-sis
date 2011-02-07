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
package com.solertium.gogoego.resizer.imagemagick;

import org.gogoego.api.applications.ServerApplicationAPI;
import org.gogoego.api.images.ImageManipulator;
import org.gogoego.api.images.ImageManipulatorActivator;
import org.gogoego.api.images.ImageManipulatorDiagnostics;
import org.gogoego.api.images.ImageManipulatorFactory;

/**
 * This is a placeholder for a pluggable AWT version of the resizer.
 */
public class ConvertActivator extends ImageManipulatorActivator {

	public ImageManipulatorFactory getService() {
		return new ImageManipulatorFactory() {
			public ImageManipulatorDiagnostics getDiagnostics() {
				return new ConvertImageManipulatorDiagnostics();
			}
			public ImageManipulator newInstance(ServerApplicationAPI api) {
				return new ConvertImageManipulator(api);
			}
		};
	}

}
