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

public class ImageManipulatorPreferences extends BestMatchPluginBroker.ScoreCard {
	
	public static final String PREFERENCE_SPEED = "speed";
	public static final String PREFERENCE_IMAGE_TYPE = "type";
	
	public static final Integer SPEED_SLOW = 0;
	public static final Integer SPEED_NORMAL = 1;
	public static final Integer SPEED_FAST = 2;

	public ImageManipulatorPreferences() {
		super();
	}
	
	public void setSpeed(Integer speed) {
		setSpeed(speed, false);
	}
	
	public void setSpeed(Integer speed, boolean isRequired) {
		if (isRequired)
			setRequiredProperty(PREFERENCE_SPEED, speed);
		else
			setOptionalProperty(PREFERENCE_SPEED, speed);
	}
	
	public void setImageType(String imageType) {
		setImageType(imageType, false);
	}
	
	public void setImageType(String imageType, boolean isRequired) {
		if (isRequired)
			setRequiredProperty(PREFERENCE_IMAGE_TYPE, imageType);
		else
			setOptionalProperty(PREFERENCE_IMAGE_TYPE, imageType);
	}
	
}
