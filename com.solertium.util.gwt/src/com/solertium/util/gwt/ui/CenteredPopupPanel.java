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
package com.solertium.util.gwt.ui;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.WindowResizeListener;
import com.google.gwt.user.client.ui.PopupPanel;

/**
 * PopupPanel that stays centered when user resizes
 * 
 * @author carl.scott
 * 
 */
public class CenteredPopupPanel extends PopupPanel implements WindowResizeListener {
	
	/**
	 * Constructor.
	 * @param autoHide true if this popup autohides, false otherwise
	 * @param modal true if this popup should be modal
	 */
	public CenteredPopupPanel(final boolean autoHide, final boolean modal) {
		super(autoHide, modal);
	}

	/**
	 * Keeps the popup centered when window resizes
	 */
	public void onWindowResized(final int width, final int height) {
		center();
	}
	
	/**
	 * Shows and centers the panel, then adds a window resize listener
	 */
	public void show() {
		super.show();
		center();
		Window.addWindowResizeListener(this);
	}
	
	/**
	 * Hides the popup and removes the window listener
	 */
	public void hide() {
		super.hide();
		Window.removeWindowResizeListener(this);
		removeFromParent();
	}
}