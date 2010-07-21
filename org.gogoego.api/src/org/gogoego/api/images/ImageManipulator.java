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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

public interface ImageManipulator {

	public static enum RotationStyle {
		NEG_90DEG_ROT, POS_90DEG_ROT, IMAGE_FLIP
	}
	
    /**
     * Internals of the resize operation; operates on streams.  This
     * is exposed so that specialized services can consume it.  In
     * most high level circumstances, getResizedURL is preferable.
     * 
     * @param source InputStream for image source
     * @param target OutputStream for resized image
     * @param format jpeg, png, gif are supported under AWT
     * @param newWidth new width in pixels
     * @param newHeight new height in pixels
     * @throws IOException most underlying media problems will
     * produce an IOException regardless of technique
     */
	public void resize(InputStream source, OutputStream target, String format, 
			int newWidth, int newHeight) throws IOException;
	
	/**
	 * This URL based method is the preferred resize operation
	 * for high level operations.  It allows for delegation to
	 * external services and for resized image URLs to be returned
	 * on other hosts.
	 * 
	 * @param source the URL whose content is to be resized
	 * @param newWidth new width in pixels
	 * @param newHeight new height in pixels
	 * @return the URL of the successfully resized content, or null if
	 *   the resize operation does not successfully result in an image.
	 * @throws UnsupportedOperationException immediately if the source
	 *   URL is not eligible to be resized.
	 */
	public URL getResizedURL(URL source, int newWidth, int newHeight) throws 
		UnsupportedOperationException;
	

	/**
	 * Internals of the rotate operation; operates on streams.  This
     * is exposed so that specialized services can consume it.  In
     * most high level circumstances, getRotatedURL is preferable.
     * 
     * @param source InputStream for image source
     * @param target OutputStream for resized image
     * @param direction the rotation direction constant
     * @throws IOException most underlying media problems will
     * produce an IOException regardless of technique
	 */
	public void rotate(InputStream source, OutputStream target, String format, 
			RotationStyle direction) throws IOException;
	
	/**
	 * 
	 * @param source the URL whose content is to be resized
	 * @param direction rotation direction constant
	 * @return the URL of the successfully rotated content, or null if
	 *   the rotate operation does not successfully result in an image.
	 * @throws UnsupportedOperationException immediately if the source
	 *   URL is not eligible to be rotated.
	 */
	public URL getRotatedURL(URL source, RotationStyle direction) throws UnsupportedOperationException;
	
	/**
	 * Internals of the property test; operates on streams.  This
	 * is exposed so that specialized services can consume it.  In
	 * most high level circumstances, getImageProperties(URL source)
	 * is preferable.
	 *
	 * @param source InputStream for image source
	 * @throws IOException most underlying media problems will
	 *   produce an IOException regardless of technique
	 */
	public ImageProperties getImageProperties(InputStream source) throws IOException;
	 
	/**
	 * This URL based method is the preferred image property getter
	 * for high level operations.  It allows for delegation to
	 * external services.
	 * 
	 * @param source the URL whose content is to be resized
	 * @return the properties (height and width especially) of the source
	 * @throws UnsupportedOperationException immediately if the source
	 *   URL is not eligible for analysis.
	 */
	public ImageProperties getImageProperties(URL source) throws UnsupportedOperationException;

}
