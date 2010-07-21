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

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;

/**
 * ButtonIcon.java
 * 
 * Clickable text with an icon image on its side.  It knows 
 * its positions as well when you click it. Must implement 
 * onClick
 *
 * @author carl.scott
 *
 */
public abstract class ButtonIcon extends HorizontalPanel {
	protected Button button;
	protected Image icon;
	protected boolean isEnabled = true;

	protected int xPos = 0, yPos = 0;

	/**
	 * Create a new ButtonIcon
	 * @param text the button text
	 * @param image the image
	 * @param styleName the style
	 */
	public ButtonIcon(final String text, final Image image,
			final String styleName) {
		this(text, image, styleName, true);
	}

	/**
	 * Creates a new ButtonIcon, allowing you to specify where 
	 * the icon should go (right or left of text)
	 * @param text the button text
	 * @param image the image
	 * @param styleName the style 
	 * @param isIconOnLeft true if icon goes to left of text, false otherwise
	 */
	public ButtonIcon(final String text, final Image image,
			String styleName, final boolean isIconOnLeft) {
		super();

		if (styleName == null)
			styleName = "ButtonIcon";

		button = new Button(text);
		icon = image;

		button.addStyleName(styleName + "_Button");

		if (isIconOnLeft) {
			add(icon);
			add(button);
		} else {
			add(button);
			add(icon);
		}

		addStyleName(styleName);

		sinkEvents(Event.ONCLICK);
	}

	/**
	 * Creates a new ButtonIcon from text and a url of an image
	 * @param text the button text
	 * @param url the url of the image
	 * @param styleName the style
	 */
	public ButtonIcon(final String text, final String url,
			final String styleName) {
		this(text, new Image(url), styleName);
	}

	public String getText() {
		return button.getText();
	}

	public String getUrl() {
		return icon.getUrl();
	}

	public void onBrowserEvent(final Event event) {
		switch (DOM.eventGetType(event)) {
		case Event.ONCLICK:
			if (isEnabled) {
				xPos = DOM.eventGetClientX(event);
				yPos = DOM.eventGetClientY(event);
				onClick(this);
			}
		}
	}

	public abstract void onClick(Widget sender);

	public void setEnabled(final boolean isEnabled) {
		button.setEnabled(this.isEnabled = isEnabled);
	}

	public void setText(final String text) {
		button.setText(text);
	}

	public void setUrl(final String url) {
		icon.setUrl(url);
	}

}