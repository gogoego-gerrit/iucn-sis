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
package org.gogoego.api.images;

import org.gogoego.api.utils.BestMatchPluginBroker;

public class ImageManipulatorScorer extends BestMatchPluginBroker.Scorer<ImageManipulatorFactory, ImageManipulatorPreferences> {
	
	public int score(ImageManipulatorFactory plugin, String key, Object value,
			ImageManipulatorPreferences properties) {
		final ImageManipulatorDiagnostics diagnostics = plugin.getDiagnostics();
		if (diagnostics == null || properties == null)
			return 0;
		
		if (ImageManipulatorPreferences.PREFERENCE_SPEED.equals(key)) {
			try {
				return diagnostics.getSpeed() >= ((Integer)value).intValue() ? 
						1 : 0;
			} catch (ClassCastException e) {
				return 0;
			}
		}
		else if (ImageManipulatorPreferences.PREFERENCE_IMAGE_TYPE.equals(key)) {
			try {
				return diagnostics.getSupportedImageTypes().contains((String)value) ? 
						1 : 0;
			} catch (ClassCastException e) {
				return 0;
			}
		}
		else
			return 0;
	}
	
}
