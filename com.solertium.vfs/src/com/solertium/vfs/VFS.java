/*
 * Copyright (C) 2004-2005 Cluestream Ventures, LLC
 * Copyright (C) 2006-2009 Solertium Corporation
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

package com.solertium.vfs;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Date;
import java.util.List;

import org.w3c.dom.Document;

/**
 * The VFS interface describes a general versioned filesystem that maps to
 * HTTP/REST/WebDAV semantics. Implementation of the methods herein is
 * sufficient to allow a component to interact with file-type resources without
 * recourse to File semantics. Checked exceptions mapping closely to HTTP Status
 * codes are used.
 *
 * There is also a facility for notifications to be fired when a change is made
 * to the VFS. These are currently limited to a single VFS object; two VFS
 * instances observing the same physical storage will not be notified of each
 * other's changes.
 *
 * @author <a href="mailto:rob.heittman@solertium.com">Rob Heittman</a>, <a
 *         href="http://www.solertium.com">Solertium Corporation</a>
 */
public interface VFS {

	/**
	 * @deprecated Pass VFSPath instead of String for uri
	 */
	@Deprecated
	public void addListener(String uri, VFSListener listener);

	public void addListener(VFSListener listener);

	public void addListener(VFSPath uri, VFSListener listener);

	/**
	 * @deprecated Pass VFSPath instead of String for uri
	 */
	@Deprecated
	public void copy(String from, String to) throws NotFoundException,
			ConflictException;

	public void copy(VFSPath from, VFSPath to) throws NotFoundException,
			ConflictException;

	/**
	 * Returns a classloader which loads classes from one or more stated VFSPaths
	 * in the current VFS.  The returned ClassLoader will behave according to the standard
	 * Java ClassLoader contract: first delegate, then search locally.  This is NOT the
	 * JEE Web Application behavior, which searches local classes first.<p/>
	 * 
	 * The VFSPaths may point to an expanded directory of class files directly, or may
	 * point to individual JAR files.  To include all the code in a "lib" directory of
	 * the VFS, iterate it yourself and submit the list of contained JARs in their
	 * entirety.
	 * 
	 * @param uris List of VFSPaths to be searched in this classloader
	 * @return
	 */
	public ClassLoader createClassLoader(List<VFSPath> uris, ClassLoader parent);
	
	/**
	 * @deprecated Pass VFSPath instead of String for uri
	 */
	@Deprecated
	public void delete(String uri) throws NotFoundException, ConflictException;

	public void delete(VFSPath uri) throws NotFoundException, ConflictException;

	/**
	 * @deprecated Pass VFSPath instead of String for uri
	 */
	@Deprecated
	public boolean exists(String uri);

	public boolean exists(VFSPath uri);
	
	/**
	 * Creates a directory in the generated space (GEN)
	 * @param uri
	 * @throws IOException
	 * @throws NotFoundException
	 * @deprecated this will go away in the future.
	 */
	public void generateCollection(VFSPath uri) throws NotFoundException, ConflictException;
	
	/**
	 * Creates directories in the generated space (GEN)
	 * @param uri
	 * @throws IOException
	 * @throws NotFoundException
	 * @deprecated this will go away in the future.
	 */
	public void generateCollections(VFSPath uri) throws NotFoundException, ConflictException;

	public Document getDocument(VFSPath uri) throws IOException,
			NotFoundException;

	/**
	 * @deprecated Pass VFSPath instead of String for uri
	 */
	@Deprecated
	public String getETag(String uri) throws NotFoundException;

	public String getETag(VFSPath uri) throws NotFoundException;

	/**
	 * @deprecated Pass VFSPath instead of String for uri
	 */
	@Deprecated
	public InputStream getInputStream(String uri) throws NotFoundException;

	public InputStream getInputStream(VFSPath uri) throws NotFoundException;

	/**
	 * @deprecated Pass VFSPath instead of String for uri
	 */
	@Deprecated
	public long getLastModified(String uri) throws NotFoundException;

	public long getLastModified(VFSPath uri) throws NotFoundException;

	/**
	 * @deprecated Pass VFSPath instead of String for uri
	 */
	@Deprecated
	public long getLength(String uri) throws NotFoundException;

	public long getLength(VFSPath uri) throws NotFoundException;

	/**
	 * @deprecated Pass VFSPath instead of String for uri
	 */
	@Deprecated
	public VFSMetadata getMetadata(String uri);

	/**
	 * This method always returns a metadata object, regardless of whether or
	 * not there is a corresponding VFS file. This is so that metadata can be
	 * mutated prior to the original creation of a file.
	 */
	public VFSMetadata getMetadata(VFSPath uri);

	/**
	 * This method returns a freshly parsed, unique, read-write Document object.
	 * It is much slower under load than getDocument(...) and should only be used
	 * when the caller explicitly needs to modify the underlying Document.
	 */
	public Document getMutableDocument(VFSPath uri) throws IOException;

	/**
	 * @deprecated Pass VFSPath instead of String for uri
	 */
	@Deprecated
	public String getName(String uri);

	public String getName(VFSPath uri);

	/**
	 * @deprecated Pass VFSPath instead of String for uri
	 */
	@Deprecated
	public OutputStream getOutputStream(String uri) throws NotFoundException,
			ConflictException;

	public OutputStream getOutputStream(VFSPath uri) throws NotFoundException,
			ConflictException;

	public ReadableByteChannel getReadableByteChannel(VFSPath uri)
			throws NotFoundException;

	/**
	 * @deprecated Pass VFSPath instead of String for uri
	 */
	@Deprecated
	public Reader getReader(String uri) throws NotFoundException;

	public Reader getReader(VFSPath uri) throws NotFoundException;

	/**
	 * @deprecated Pass VFSPath instead of String for uri
	 */
	@Deprecated
	public String getString(String uri) throws IOException, NotFoundException,
			BoundsException;

	public String getString(VFSPath uri) throws IOException, NotFoundException,
			BoundsException;
	
	/**
	 * Returns a temporary file for the given uri.  It is 
	 * obviously not connected to the VFS at this point.
	 * @param uri
	 * @return
	 * @throws IOException
	 * @throws NotFoundException
	 */
	public File getTempFile(VFSPath uri) throws IOException, NotFoundException;

	public WritableByteChannel getWritableByteChannel(VFSPath uri)
			throws IOException, NotFoundException;

	/**
	 * @deprecated Pass VFSPath instead of String for uri
	 */
	@Deprecated
	public Writer getWriter(String uri) throws NotFoundException,
			ConflictException;

	public Writer getWriter(VFSPath uri) throws NotFoundException,
			ConflictException;

	/**
	 * @deprecated Pass VFSPath instead of String for uri
	 */
	@Deprecated
	public boolean isCollection(String uri) throws NotFoundException;

	public boolean isCollection(VFSPath uri) throws NotFoundException;

	/**
	 * @deprecated Pass VFSPath instead of String for uri
	 */
	@Deprecated
	public String[] list(String uri) throws NotFoundException;

	/**
	 * This will return the file or folder names as tokens
	 *
	 * @param uri
	 *            the directory to fetch
	 * @return a list of path token objects
	 * @throws NotFoundException
	 */
	public VFSPathToken[] list(VFSPath uri) throws NotFoundException;

	/**
	 * @deprecated Pass VFSPath instead of String for uri
	 */
	@Deprecated
	public void makeCollection(String uri) throws NotFoundException,
			ConflictException;

	public void makeCollection(VFSPath uri) throws NotFoundException,
			ConflictException;

	/**
	 * @deprecated Pass VFSPath instead of String for uri
	 */
	@Deprecated
	public void makeCollections(String uri) throws NotFoundException,
			ConflictException;

	public void makeCollections(VFSPath uri) throws NotFoundException,
			ConflictException;

	/**
	 * @deprecated Pass VFSPath instead of String for from, to
	 */
	@Deprecated
	public void move(String from, String to) throws NotFoundException,
			ConflictException;

	public void move(VFSPath from, VFSPath to) throws NotFoundException,
			ConflictException;

	public void removeListener(VFSListener listener);

	/**
	 * @deprecated Pass VFSPath instead of String for uri
	 */
	@Deprecated
	public void setLastModified(String uri, Date modified)
			throws NotFoundException;

	public void setLastModified(VFSPath uri, Date modified)
			throws NotFoundException;

	/**
	 * @deprecated Pass VFSPath instead of String for uri
	 */
	@Deprecated
	public void setMetadata(String uri, VFSMetadata metadata)
			throws ConflictException;

	public void setMetadata(VFSPath uri, VFSMetadata metadata)
			throws ConflictException;
}