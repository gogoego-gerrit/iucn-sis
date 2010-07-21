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

package com.solertium.util.extjs.client;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.button.Button;

/**
 * QuickButton.java
 * 
 * Allows you to make one call do to all of the MyGWT button creation fun.
 * 
 * @author carl.scott
 * 
 */
public class QuickButton extends Button {

	public QuickButton(final String text, final SelectionListener<ButtonEvent> listener) {
		super(text);
		addSelectionListener(listener);
	}

	public QuickButton(final String text, final SelectionListener<ButtonEvent> listener, final String tooltip) {
		super(text);
		addSelectionListener(listener);
		setToolTip(tooltip);
	}

	public QuickButton(final String text, final String iconStyle, final SelectionListener<ButtonEvent> listener) {
		super(text);
		setIconStyle(iconStyle);
		addSelectionListener(listener);
	}

	public QuickButton(final String text, final String iconStyle, final SelectionListener<ButtonEvent> listener,
			final String tooltip) {
		super(text);
		setIconStyle(iconStyle);
		addSelectionListener(listener);
		setToolTip(tooltip);
	}

}
