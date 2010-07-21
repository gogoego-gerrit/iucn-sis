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
package com.solertium.util.extjs.client;

import com.extjs.gxt.ui.client.widget.button.IconButton;
import com.extjs.gxt.ui.client.widget.tips.ToolTipConfig;

/**
 * TooltipIcon.java
 * 
 * Wrapper for an IconButton that takes an iconStyle and a tooltip, so you can
 * create an instance with just a constructor call.
 * 
 * @author carl.scott
 * 
 */
public class TooltipIcon extends IconButton {

	public TooltipIcon(String iconStyle, String tooltipBody) {
		this(iconStyle, null, tooltipBody);
	}

	public TooltipIcon(String iconStyle, String tooltipTitle, String tooltipBody) {
		super(iconStyle);
		setToolTip(new ToolTipConfig(tooltipTitle, tooltipBody));
	}

}
