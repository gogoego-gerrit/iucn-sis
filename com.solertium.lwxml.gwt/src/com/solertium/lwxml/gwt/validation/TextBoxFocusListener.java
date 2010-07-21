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
package com.solertium.lwxml.gwt.validation;

import com.google.gwt.user.client.ui.FocusListener;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * TextBoxFocusListener.java
 * 
 * Listens for when a 
 *
 * @author carl.scott
 *
 */
public abstract class TextBoxFocusListener implements FocusListener {
		
	private final boolean revertToText;
	private String curText;
	
	public TextBoxFocusListener() { this(false); }
	
	public TextBoxFocusListener(boolean revertToText) {
		this.revertToText = revertToText;
	}
	
	public void onFocus(Widget sender) {
		curText = ((TextBox)sender).getText();
	}
	
	public void onLostFocus(Widget sender) {
		if (!isValid(((TextBox)sender).getText()))
			((TextBox)sender).setText(revertToText ? curText : "");
	}
	
	public abstract boolean isValid(String text);

}
