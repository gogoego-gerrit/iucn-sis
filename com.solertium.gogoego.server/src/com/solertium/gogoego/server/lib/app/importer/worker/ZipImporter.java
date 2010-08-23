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
package com.solertium.gogoego.server.lib.app.importer.worker;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.solertium.util.BaseDocumentUtils;
import com.solertium.util.TrivialExceptionHandler;
import com.solertium.vfs.VFS;
import com.solertium.vfs.VFSPath;
import com.solertium.vfs.utils.VFSUtils;

public class ZipImporter {
	
	private final VFS vfs;
	private final Set<String> restrictedPaths;
	
	private InputStreamListener restrictionListener;
	
	public ZipImporter(VFS vfs) {
		this.vfs = vfs;
		this.restrictedPaths = new HashSet<String>();
	}
	
	public Document doImport(final ZipInputStream zis, final ImportMode mode) throws ResourceException {
		final Document document = BaseDocumentUtils.impl.newDocument();
		final Element root = document.createElement("results");
		root.setAttribute("mode", mode.toString());

		ZipEntry ze;
		try {
			ze = zis.getNextEntry();
		} catch (IOException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
		byte[] buf = new byte[1024];
		while (ze != null) {
			final VFSPath path;
			try {
				path = VFSUtils.parseVFSPath(ze.getName());
			} catch (VFSUtils.VFSPathParseException e) {
				e.printStackTrace();
				try {
					ze = zis.getNextEntry();
					continue;
				} catch (IOException f) {
					throw new ResourceException(Status.SERVER_ERROR_INTERNAL, f);
				}
			}
			
			if (isRestricted(path)) {
				if (restrictionListener != null && !ze.isDirectory()) {
					try {
						final VFSPath restrictedUri = VFSUtils.parseVFSPath(ze.getName());
						final ByteArrayOutputStream baos = new ByteArrayOutputStream();
						byte[] in_bytes = buf;
						int n;
						while ((n = zis.read(in_bytes, 0, 1024)) > -1)
							baos.write(in_bytes, 0, n);
						ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
						
						restrictionListener.handle(restrictedUri, bais);
					} catch (Exception e) {
						e.printStackTrace();
						TrivialExceptionHandler.ignore(this, e);
					} 
				}
				try {
					ze = zis.getNextEntry();
					continue;
				} catch (IOException f) {
					throw new ResourceException(Status.SERVER_ERROR_INTERNAL, f);
				}
			}
			
			final Element el;
			
			if (ze.isDirectory()) {
				el = document.createElement("folder");
				el.setAttribute("path", path.toString());
				
				if (!vfs.exists(path)) {
					try {
						vfs.makeCollections(path);
					} catch (IOException e) {
						//Let it fail below for proper iteration behavior
						TrivialExceptionHandler.ignore(this, e);
					}
				}
				else {
					try {
						el.setAttribute("status", "skipped");
						root.appendChild(el);
						ze = zis.getNextEntry();
						continue;
					} catch (IOException e) {
						throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
					}
				}
			}
			else {
				el = document.createElement("file");
				el.setAttribute("path", path.toString());
				
				if (ImportMode.FRESHEN.equals(mode) && vfs.exists(path)) {
					try {
						long localTime = vfs.getLastModified(path);
						long remoteTime = ze.getTime();
						
						if (localTime >= remoteTime) {
							el.setAttribute("status", "skipped");
							root.appendChild(el);
							ze = zis.getNextEntry();
							continue;
						}
					} catch (Exception e) {
						TrivialExceptionHandler.ignore(this, e);
					}
				}
				
				//Create parent dir if it does not already exist 
				if (!vfs.exists(path.getCollection())) {
					try {
						vfs.makeCollections(path.getCollection());
					} catch (IOException e) {
						//Let it fail below for proper iteration behavior
						TrivialExceptionHandler.ignore(this, e);
					}
				}
			
				final OutputStream os;
				try {
					os = vfs.getOutputStream(path);
				} catch (IOException e) {
					try {
						ze = zis.getNextEntry();
						el.setAttribute("status", "error");
						root.appendChild(el);
						continue;
					} catch (IOException f) {
						throw new ResourceException(Status.SERVER_ERROR_INTERNAL, f);
					}
				}
	
				int n;
				try {
					while ((n = zis.read(buf, 0, 1024)) > -1)
						os.write(buf, 0, n);
				} catch (IOException e) {
					el.setAttribute("status", "error");
					root.appendChild(el);
					continue;
				} finally {
					try {
						os.close();
					} catch (IOException e) {
						TrivialExceptionHandler.ignore(this, e);
					}
				}
			}
			
			el.setAttribute("status", "success");
			root.appendChild(el);

			try {
				ze = zis.getNextEntry();
			} catch (IOException e) {
				throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
			}
		}

		try {
			zis.close();
		} catch (IOException e) {
			TrivialExceptionHandler.ignore(this, e);
		}

		document.appendChild(root);
		
		return document;
	}
	
	private boolean isRestricted(final VFSPath path) {
		for (String folder : restrictedPaths)
			if (path.toString().startsWith(folder))
				return true;
		return false;
	}
	
	public void restrict(VFSPath restrictedPath) {
		restrictedPaths.add(restrictedPath.toString());
	}
	
	public void setRestrictionListener(InputStreamListener restrictionListener) {
		this.restrictionListener = restrictionListener;
	}
	
	public static interface InputStreamListener {
		
		public void handle(final VFSPath uri, InputStream is);
		
	}

}
