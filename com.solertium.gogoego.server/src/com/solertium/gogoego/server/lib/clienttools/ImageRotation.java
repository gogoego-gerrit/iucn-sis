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
package com.solertium.gogoego.server.lib.clienttools;

import org.gogoego.api.collections.Constants;
import org.gogoego.api.debugging.GoGoDebugger;
import org.gogoego.api.images.ImageManipulator;
import org.gogoego.api.images.ImageManipulator.RotationStyle;

import com.solertium.gogoego.server.GoGoDebug;
import com.solertium.vfs.NotFoundException;
import com.solertium.vfs.VFS;
import com.solertium.vfs.VFSPath;

/**
 * ImageRotation.java
 * 
 * Handles image rotation
 * 
 * @author carl.scott
 * 
 */
public class ImageRotation {

	public VFSPath getRotatedURI(ImageManipulator manipulator, String format, VFS vhc, VFSPath uri, RotationStyle direction) throws NotFoundException {
		String s = uri.toString();
		String unmodifiedFileURI = s;
		if (s.contains("rot90"))
			unmodifiedFileURI = s.substring(0, s.lastIndexOf("rot90"));
		if (s.contains("rot180"))
			unmodifiedFileURI = s.substring(0, s.lastIndexOf("rot180"));
		if (s.contains("rot270"))
			unmodifiedFileURI = s.substring(0, s.lastIndexOf("rot270"));

		if (vhc.exists(uri)) {
			log().println("Original image file exists");
			VFSPath origname = uri;
			log().println("Original image file name is {0}", origname);
			log().println("Unmodified image file name is {0}", unmodifiedFileURI);
			VFSPath newname = getNewURI(direction, uri);
			log().println("Rotated image name is {0}", newname);
			boolean refreshURI = false;
			if (vhc.exists(newname)) {
				log().println("Rotated image file already exists.");
				if (vhc.getLastModified(newname) < vhc.getLastModified(new VFSPath(unmodifiedFileURI))) {
					log().println("but is stale.");
					refreshURI = true;
				}
			} else {
				log().println("Rotated image file does not exist.");
				refreshURI = true;
			}
			if (refreshURI) {
				try {
					manipulator.rotate(vhc.getInputStream(uri), vhc.getOutputStream(newname), format, direction);
				} catch (Exception e) {
					log().println("Image rotation failed: {0}", e.getMessage());
					return new VFSPath(Constants.NO_IMAGE);
				}
				
			}
			return newname;
		} else {
			log().println("Source image missing for {0}", uri);
			return new VFSPath(Constants.NO_IMAGE);
		}
	}

	/**
	 * Creates an appropriate URI for an image to be rotated
	 * 
	 * @param direction
	 *            the direction
	 * @param uri
	 *            the uri
	 * @return the uri appropriate for a resized image.
	 */
	public static VFSPath getNewURI(RotationStyle direction, VFSPath uri) {
		String s = uri.toString();

		String origext = s.substring(s.lastIndexOf('.'));
		String origtrunc = s.substring(0, s.lastIndexOf('.'));
		String newname = "";

		if (direction.equals(RotationStyle.POS_90DEG_ROT)) {
			if ((!s.contains("rot90")) && (!s.contains("rot180") && (!s.contains("rot270")))) {
				newname = origtrunc + "rot90" + origext;
			} else if (s.contains("rot90")) {
				newname = origtrunc.substring(0, origtrunc.lastIndexOf("rot90")) + "rot180" + origext;
			} else if (s.contains("rot180")) {
				newname = origtrunc.substring(0, origtrunc.lastIndexOf("rot180")) + "rot270" + origext;
			} else if (s.contains("rot270")) {
				newname = origtrunc.substring(0, origtrunc.lastIndexOf("rot270")) + origext;
			}
		} else if (direction.equals(RotationStyle.NEG_90DEG_ROT)) {
			if ((!s.contains("rot90")) && (!s.contains("rot180") && (!s.contains("rot270")))) {
				newname = origtrunc + "rot270" + origext;
			} else if (s.contains("rot90")) {
				newname = origtrunc.substring(0, origtrunc.lastIndexOf("rot90")) + origext;
			} else if (s.contains("rot180")) {
				newname = origtrunc.substring(0, origtrunc.lastIndexOf("rot180")) + "rot90" + origext;
			} else if (s.contains("rot270")) {
				newname = origtrunc.substring(0, origtrunc.lastIndexOf("rot270")) + "rot180" + origext;
			}
		}

		return new VFSPath(newname);
	}
	
	public GoGoDebugger log() {
		return GoGoDebug.get("fine");
	}

}
