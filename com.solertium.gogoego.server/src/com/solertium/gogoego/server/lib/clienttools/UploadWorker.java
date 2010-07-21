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
package com.solertium.gogoego.server.lib.clienttools;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.fileupload.FileItem;
import org.gogoego.api.plugins.GoGoEgo;

import com.solertium.vfs.VFS;
import com.solertium.vfs.VFSPath;
import com.solertium.vfs.VFSPathToken;

/**
 * UploadWorker.java
 * 
 * 
 * 
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a
 *  href="http://www.solertium.com">Solertium Corporation</a>
 */
public class UploadWorker {
	
	public static boolean upload(FileItem item, final VFSPath uri, VFS vfs) {
		final VFSPathToken filename = cleanFilename(item.getName());
		GoGoEgo.debug("debug").println("Name is: {0} (was {1})", filename, item.getName());

		final VFSPath fileUri = uri.child(filename);
		GoGoEgo.debug("debug").println("File URI is {0}", fileUri);

		if (item.getFieldName().equals(""))
			return false;				

		try {
			final OutputStream out = vfs.getOutputStream(fileUri);
			out.write(item.get());
			out.close();
		} catch(IOException e){
			return false;
		}
		
		return true;		
	}
	
	/**
	 * Removes characters from a filename that aren't web-friendly.
	 * 
	 * @param name
	 *            the original file name.
	 * @return the cleaned filename
	 */
	public static VFSPathToken cleanFilename(String name) {
		String clean = name.replaceAll(" ", "_");
		clean = clean.replaceAll("\\+", "_");
		clean = clean.replaceAll(",", "_");
		clean = clean.replaceAll("&", "_");
		clean = clean.replaceAll("\\?", "_");
		
		return convertWindowsPath(clean);
	}
	
	public static VFSPathToken convertWindowsPath(String name) {
		String clean = name;
		VFSPathToken token;
		try {
			token = new VFSPathToken(clean);
		} catch (IllegalArgumentException e) {
			clean = clean.replace('\\', '/');
			if (clean.endsWith("/"))
				clean = clean.substring(0, clean.length() - 1);
			final String[] pathParts = clean.split("/");
			try {
				token = new VFSPathToken(pathParts[pathParts.length - 1]);
			} catch (IllegalArgumentException f) {
				token = new VFSPathToken("new_unresolved_file.tmp");
			}
		}
		return token;
	}
	

}
