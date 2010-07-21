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

/**
 * DrawsLazily.java
 * 
 * Interface that suggests that a widget draw itself lazily.
 * 
 * @author carl.scott
 * 
 */
public interface DrawsLazily {

	/**
	 * Render UI components and attach them to the DOM
	 * 
	 */
	public void draw(final DrawsLazily.DoneDrawingCallback callback);

	public static interface DoneDrawingCallback {

		public void isDrawn();

	}

	public static interface DoneDrawingCallbackWithParam<T> {

		public void isDrawn(T parameter);

	}

	public static class DoneDrawingWithNothingToDoCallback implements DoneDrawingCallback {

		public void isDrawn() {
		}

	}

}
