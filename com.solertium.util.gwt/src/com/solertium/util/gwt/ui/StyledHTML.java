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

import com.google.gwt.user.client.ui.HTML;

/**
 * StyledHTML.java
 * 
 * Wrapper for the GWT HTML class that takes a style 
 * name as an argument in the constructor.
 * 
 * @author carl.scott@solertium.com
 *
 */
public class StyledHTML extends HTML {

	/**
	 * Constructs a new HTML instance, and immediately adds a style
	 * to it
	 * @param text the text/html you want to construct with
	 * @param styleName the style for this HTML
	 */
	public StyledHTML(final String text, final String styleName) {
		super(text);
		String[] split = styleName.split(";");
		for (int i = 0; i < split.length; i++)
			addStyleName(split[i]);
	}

}
