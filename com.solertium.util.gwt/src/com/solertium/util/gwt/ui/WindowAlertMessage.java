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

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * WindowAlertMessage.java
 * 
 * A simple message alert, with support for auto close 
 *
 * @author carl.scott
 *
 */
public class WindowAlertMessage extends CenteredPopupPanel {
	
	public static final String IMAGE_DIALOG_INFORMATION = "images/dialog-information.png";
	public static final String IMAGE_DIALOG_WARNING = "images/dialog-warning.png";
	public static final String IMAGE_DIALOG_ERROR = "images/dialog-error.png";
	public static final String IMAGE_USER_LOGOUT = "images/system-lock-screen.png";

	private Button button;
	private StyledHTML message;

	private Timer timer;
	private int timeInMillis = -1;

	/**
	 * Creates a window alert message
	 * @param image the image url, as defined above or you can supply your own
	 * @param messageText the message text
	 * @param buttonText the button text ("OK")
	 */
	public WindowAlertMessage(final String image, final String messageText, final String buttonText) {
		super(false, true);

		final FlexTable upperTable = new FlexTable();
		upperTable.setWidget(0, 0, new Image(image));
		upperTable.setWidget(0, 1, message = new StyledHTML(messageText, "fontSize80"));

		final FlexTable bottomTable = new FlexTable();
		bottomTable.setWidth("100%");
		bottomTable.setWidget(0, 0, button = new Button(buttonText));
		bottomTable.getCellFormatter().setAlignment(0, 0, HasHorizontalAlignment.ALIGN_CENTER,
			HasVerticalAlignment.ALIGN_MIDDLE);

		final VerticalPanel bodyPanel = new VerticalPanel();
		bodyPanel.setSpacing(10);
		bodyPanel.add(upperTable);
		bodyPanel.add(bottomTable);

		// button.setFocus(true);
		button.addStyleName("CIPD_WindowAlertButton");
		button.addStyleName("fontSize80");
		button.addStyleName("clickable");
		button.addClickListener(new ClickListener() {
			public void onClick(final Widget sender) {
				try {
					timer.cancel();
				} catch (final Exception e) {
				}
				onClose();
			}
		});

		setWidth("300px");

		addStyleName("CIPD_PopupPanel");
		setWidget(bodyPanel);
	}

	/**
	 * Hit these buttons to close the window...
	 */
	public boolean onKeyUpPreview(char key, int modifiers) {
		if (key == KeyboardListener.KEY_ESCAPE || key == KeyboardListener.KEY_ENTER)
			onClose();
		return true;
	}

	/**
	 * Its important to call hide() so the window listeners can be 
	 * disposed properly
	 *
	 */
	public void onClose() {
		if (this.isAttached() && this.isVisible()) {
			hide();
		}
	}

	public void setMessageStyle(final String style) {
		message.setStyleName(style);
	}

	public void setTimer(final int timeInMillis) {
		this.timeInMillis = timeInMillis;
	}

	public void show() {
		super.show();
		if (timeInMillis > 0) {
			timer = new Timer() {
				public void run() {
					onClose();
				}
			};
			timer.schedule(timeInMillis);
		}
	}

}