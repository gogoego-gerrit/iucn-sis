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
package com.solertium.gogoego.resizer.imagemagick;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import javax.imageio.ImageIO;

import org.gogoego.api.applications.ServerApplicationAPI;
import org.gogoego.api.images.ImageManipulator;
import org.gogoego.api.images.ImageProperties;

import com.solertium.gogoego.server.lib.representations.ImageUtils;
import com.solertium.util.MD5Hash;
import com.solertium.vfs.VFS;
import com.solertium.vfs.VFSPath;

@SuppressWarnings("deprecation")
public class ConvertImageManipulator implements ImageManipulator {

	private final ServerApplicationAPI api;

	public ConvertImageManipulator(ServerApplicationAPI api) {
		super();
		this.api = api;
	}

	public static void copyStream(final InputStream is, final OutputStream os) throws IOException {
		final byte[] buf = new byte[65535];
		int i = 0;
		while ((i = is.read(buf)) != -1)
			os.write(buf, 0, i);
	}

	public void resize(InputStream source, OutputStream target, String format,
			int newWidth, int newHeight) throws IOException {
		ProcessBuilder pb = new ProcessBuilder("/usr/bin/convert","-resize",newWidth+"x"+newHeight,format+":-",format+":-");
		Process p = pb.start();
		InputStream convertIn = p.getInputStream();
		OutputStream convertOut = p.getOutputStream();
		copyStream(source,convertOut);
		convertOut.flush();
		convertOut.close();
		copyStream(convertIn,target);
		target.flush();
		target.close();
	}

	public String getResizedWatermarkedURI(final String path, final String watermark, final int size){
		System.out.println("getResizedWatermarkedURI");
		ImageUtils.ExtraTransform transform = new ImageUtils.ExtraTransform(){
			private String result;
			public String getResult() {
				return result;
			}
			public String getToken() {
				return "rzW_"+(new MD5Hash(watermark).toString());
			}
			public void transform(VFS vfs, VFSPath source, VFSPath target, String format) {
				System.out.println("extratransform invoked on "+source+" -> "+target);
				try{
					watermark(vfs.getInputStream(source), vfs.getOutputStream(target), format, watermark);
				} catch (IOException iox) {
					iox.printStackTrace();
				}
			}
		};
		return ImageUtils._getResizedURI(api.getVFS(), this, path, size, ImageUtils.MAX, transform);
	}

	public void watermark(InputStream source, OutputStream target, String format, String watermark) throws IOException {
		System.out.println("Doing watermark operation");
		ProcessBuilder pb = new ProcessBuilder("/usr/bin/convert",format+":-","-gravity","south","-stroke","#000C","-strokewidth","2","-annotate","0",watermark,
				"-stroke","none","-fill","white","-annotate","0",watermark,format+":-");
		Process p = pb.start();
		InputStream convertIn = p.getInputStream();
		OutputStream convertOut = p.getOutputStream();
		copyStream(source,convertOut);
		convertOut.flush();
		convertOut.close();
		copyStream(convertIn,target);
		target.flush();
		target.close();
	}

	public ImageProperties getImageProperties(InputStream source)
			throws IOException {
		BufferedImage srcImage = ImageIO.read(source);
	   	
		ImageProperties properties = new ImageProperties();
		properties.setWidth(srcImage.getWidth());
		properties.setHeight(srcImage.getHeight());

		return properties;
	}

	public ImageProperties getImageProperties(URL source)
			throws UnsupportedOperationException {
		throw new UnsupportedOperationException("Use InputStreams instead.");
	}

	public URL getResizedURL(URL source, int newWidth, int newHeight)
			throws UnsupportedOperationException {
		throw new UnsupportedOperationException("Use InputStreams instead.");
	}

	public URL getRotatedURL(URL source, RotationStyle direction)
			throws UnsupportedOperationException {
		throw new UnsupportedOperationException("Rotation not supported.");
	}

	public void rotate(InputStream source, OutputStream target, String format,
			RotationStyle direction) throws IOException {
		throw new UnsupportedOperationException("Rotation not supported.");
	}

}
