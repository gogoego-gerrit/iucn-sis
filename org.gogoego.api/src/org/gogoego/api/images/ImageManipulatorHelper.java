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

/**
 * ImageManipulatorHelper.java
 * 
 * Quick helper class to perform commonly used image resizing.  
 * For use of advanced image manipulation, specific image 
 * manipulators, or any other custom processing, access the 
 * ImageManipulator directly via the GoGoEgo object.
 * 
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, 
 * <a href="http://www.solertium.com">Solertium Corporation</a>
 *
 */
public interface ImageManipulatorHelper {
	
	public final static int HEIGHT = 1;
	public final static int MAX = 2;
	public final static int WIDTH = 0;
	
	/**
	 * Resizes the image at the given path to the specified size.  
	 * It will attempt to determine the best way to resize the 
	 * image based on its dimensions.
	 * 
	 * @param path the image uri
	 * @param size the desired size of the image
	 * @return the uri of the resized image
	 */
	public String getResizedURI(final String path, final int size);

	/**
	 * Like getResizedURI, except that if it is given "width" or "height" will
	 * do the size specified by the width or height.
	 * 
	 * @param path the image uri
	 * @param size the desired size of the image
	 * @param mode sizing mode, 0 for width, 1 for height, 2 for max (default)
	 * @return the uri of the resized image
	 */
	public String getResizedURI(final String path, final int size, final String mode);

}
