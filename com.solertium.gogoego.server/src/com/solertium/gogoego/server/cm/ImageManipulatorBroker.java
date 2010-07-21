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
package com.solertium.gogoego.server.cm;

import org.gogoego.api.applications.ServerApplicationAPI;
import org.gogoego.api.images.ImageManipulator;
import org.gogoego.api.images.ImageManipulatorFactory;
import org.gogoego.api.images.ImageManipulatorPreferences;
import org.gogoego.api.images.ImageManipulatorScorer;
import org.gogoego.api.utils.BestMatchPluginBroker;
import org.osgi.framework.BundleContext;

public class ImageManipulatorBroker extends BestMatchPluginBroker<ImageManipulatorFactory, ImageManipulatorPreferences> {

	public ImageManipulatorBroker(BundleContext bundleContext) {
		super(bundleContext, ImageManipulatorFactory.class.getName(), new ImageManipulatorScorer());
		
		addHeaderKey("Bundle-Name");
		
		/*
		addLocalReference("com.solertium.gogoego.server", new ImageManipulatorFactory() {
			public ImageManipulatorDiagnostics getDiagnostics() {
				return new LegacyImageManipulatorDiagnostics();
			}
			public ImageManipulator newInstance(ServerApplicationAPI api) {
				return new LegacyImageManipulator(api);
			}
		});
		*/
	}

	public ImageManipulator getImageManipulator(ServerApplicationAPI api) {
		return getImageManipulator(api, null);
	}
	
	public ImageManipulator getImageManipulator(ServerApplicationAPI api, ImageManipulatorPreferences preferences) {
		return getPlugin(preferences).newInstance(api);
	}
	
}
