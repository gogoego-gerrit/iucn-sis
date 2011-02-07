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

import java.util.ArrayList;
import java.util.List;

import org.gogoego.api.images.ImageManipulatorDiagnostics;
import org.gogoego.api.images.ImageManipulatorPreferences;

public class ConvertImageManipulatorDiagnostics implements
		ImageManipulatorDiagnostics {
	
	private final List<String> supportedImageTypes;
	
	public ConvertImageManipulatorDiagnostics() {
		super();
		
		supportedImageTypes = new ArrayList<String>();
		supportedImageTypes.add("png");
		supportedImageTypes.add("jpg");
		supportedImageTypes.add("jpeg");
		supportedImageTypes.add("gif");
		supportedImageTypes.add("bmp");
	}

	/**
	 * This may be pushing the truth a bit but whatever
	 */
	public int getSpeed() {
		return ImageManipulatorPreferences.SPEED_FAST;
	}

	public List<String> getSupportedImageTypes() {
		return supportedImageTypes;
	}

}
