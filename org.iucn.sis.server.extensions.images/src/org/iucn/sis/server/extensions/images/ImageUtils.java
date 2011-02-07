/*
 * Copyright (C) 2007-2009 Solertium Corporation
 *
 * This file is part of the open source GoGoEgo project.
 *
 * GoGoEgo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *  
 * GoGoEgo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GoGoEgo.  If not, see http://www.gnu.org/licenses/.
 * 
 * Unless you have been granted a different license in writing by the
 * copyright holders for GoGoEgo, only the GNU General Public License
 * grants you rights to modify or redistribute this code.
 */
package org.iucn.sis.server.extensions.images;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.gogoego.api.collections.Constants;
import org.gogoego.api.images.ImageManipulator;
import org.gogoego.api.images.ImageManipulatorHelper;
import org.gogoego.api.images.ImageProperties;
import org.gogoego.api.plugins.GoGoEgo;
import org.iucn.sis.server.api.application.SIS;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;

import com.solertium.vfs.NotFoundException;
import com.solertium.vfs.VFS;
import com.solertium.vfs.VFSMetadata;
import com.solertium.vfs.VFSPath;
import com.solertium.vfs.VFSPathToken;

/**
 * ImageUtils.java
 * 
 * Used in the dynamic content bindings, allow for image utilities 
 * such as on-the-fly resizing.
 * 
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a href="http://www.solertium.com">Solertium Corporation</a>
 *
 */
public class ImageUtils implements ImageManipulatorHelper {

	private final VFS vfs;
	private final ImageManipulator manipulator;
	
	public ImageUtils(Context context) {
		vfs = SIS.get().getVFS();
		manipulator = GoGoEgo.get().getImageManipulatorPlugin(context);
	}
	
	public ImageManipulator getImageManipulator() {
		return manipulator;
	}
	
	public String getResizedURI(final String path, final int size) {
		return _getResizedURI(vfs, manipulator, path, size, MAX, null);
	}

	/**
	 * Like getResizedURI, except that if it is given "width" or "height" will
	 * do the size specified by the width or height.
	 * 
	 * @param path
	 * @param size
	 * @param mode
	 * @return
	 */
	public String getResizedURI(final String path, final int size, final String mode) {
		if ("width".equalsIgnoreCase(mode))
			return _getResizedURI(vfs, manipulator, path, size, WIDTH, null);
		else if ("height".equalsIgnoreCase(mode))
			return _getResizedURI(vfs, manipulator, path, size, HEIGHT, null);
		else
			return getResizedURI(path, size);
	}
	
	public interface ExtraTransform{
		public String getResult();
		public String getToken();
		public void transform(VFS vfs, VFSPath source, VFSPath target, String format);
	}
	
	public static void copyStream(final InputStream is, final OutputStream os) throws IOException {
		final byte[] buf = new byte[65535];
		int i = 0;
		while ((i = is.read(buf)) != -1)
			os.write(buf, 0, i);
	}
	
	public static String _getResizedURI(final VFS vfs, final ImageManipulator manipulator, String path, final int size, final int mode, final ExtraTransform transform) {
		if (path.contains("://")){
			Reference r = new Reference(path);
			VFSPath local = new VFSPath("/rzRemote/"+r.getAuthority()+r.getPath());
			if(!vfs.exists(local)){
				try{
					vfs.generateCollections(local.getCollection());
				} catch (IOException ignored) {}
				try{
					VFSMetadata md = vfs.getMetadata(local);
					md.setGenerated(true);
					vfs.setMetadata(local, md);
					if(!vfs.exists(local.getCollection())){
						vfs.makeCollections(local.getCollection());
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				System.out.println(">>>> Doing fetch of source bits from "+path);
				Client c = new Client(r.getSchemeProtocol());
				Request req = new Request(Method.GET,r);
				req.setChallengeResponse(new ChallengeResponse(ChallengeScheme.HTTP_BASIC,"",""));
				Response resp = c.handle(req);
				try{
					InputStream is = resp.getEntity().getStream();
					OutputStream os = vfs.getOutputStream(local);
					copyStream(is,os);
					is.close();
					os.close();
					System.out.println(">>>> Copied locally");
				} catch (IOException iox) {
					System.out.println(">>>> Failed to copy locally");
					iox.printStackTrace();
				}
			}
			path = local.toString();
			System.out.println(">>>> Using equivalent local path "+path);
		}
		
		if (manipulator == null) {
			System.out.println(">>>> Image manipulator could not be created!");
			return Constants.NO_IMAGE;
		}
		
		String token = "rzA";
		if(transform!=null) token = transform.getToken();
		
		VFSPath source = new VFSPath(path);
		// calculate name based on call
		VFSPath sourceParent = source.getCollection();
		String sourceName = source.getName();
		final int dot = sourceName.lastIndexOf(".");
		if(dot==-1){
			System.out.println(">>>> format could not be detected");
			return Constants.NO_IMAGE;
		}
		String format = sourceName.toLowerCase().substring(dot+1);
		String root = sourceName.toLowerCase().substring(0,dot);
		final int pos = root.indexOf("_"+token+"_");
		if(pos>-1) root = root.substring(0,pos);

		VFSPath target = sourceParent.child(new VFSPathToken(root+"_"+token+"_"+mode+"_"+size+"."+format));
		System.out.println(">>>> Target file: "+target);

		if(vfs.exists(target)){
			// System.out.println(">>>> exists");
			try{
				if(vfs.getLastModified(target)>vfs.getLastModified(source)){
					// System.out.println(">>>> valid");
					return target.toString();
				}
			} catch (NotFoundException nf) {
				System.out.println(">>>> timestamps could not be compared");
				return Constants.NO_IMAGE;
			}
		}
		
		// System.out.println(">>>> doesn't exist");
		
		final ImageProperties img;
		try {
			img = manipulator.getImageProperties(vfs.getInputStream(source));
		} catch (IOException e) {
			System.out.println(">>>> source properties could not be read");
			return Constants.NO_IMAGE;
		}
		
		final int myMode;
		if (mode == MAX)
			if (img.getWidth() >= img.getHeight())
				myMode = WIDTH;
			else
				myMode = HEIGHT;
		else
			myMode = mode;
		
		int x = 0;
		int y = 0;
		
		float scale;
		if (myMode == WIDTH) {
			scale = (float) size / (float) img.getWidth();
			x = size;
			y = (int) (img.getHeight()*scale);
		} 
		else {
			scale = (float) size / (float) img.getHeight();
			x = (int) (img.getWidth()*scale);
			y = size;
		}
		
		try{
			vfs.generateCollections(target.getCollection());
		} catch (IOException ignored) {}

		try {
			System.out.println(">>>> doing "+format+" resize to "+x+" x "+y);
			VFSMetadata md = vfs.getMetadata(target);
			md.setGenerated(true);
			vfs.setMetadata(target, md);
			if(!vfs.exists(target.getCollection())){
				try{
					vfs.makeCollections(target.getCollection());
				} catch (NotFoundException e) {
					System.out.println(">>>> NFE in makeCollections on "+target.getCollection());
				}
			}
			manipulator.resize(vfs.getInputStream(source),
					vfs.getOutputStream(target),
					format,
					x, y);
			if(transform!=null){
				transform.transform(vfs, target, target, format);
			}
			return target.toString();
		} catch (UnsupportedOperationException e) {
			System.out.println(">>>> unsupported operation exception");
			return Constants.NO_IMAGE;
		} catch (IOException e) {
			System.out.println(">>>> io exception");
			e.printStackTrace();
			return Constants.NO_IMAGE;
		}
	}

}

