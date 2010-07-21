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
package com.solertium.vfs.provider.layered;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import com.solertium.vfs.BaseVFS;
import com.solertium.vfs.BoundsException;
import com.solertium.vfs.ConfigurationException;
import com.solertium.vfs.ConflictException;
import com.solertium.vfs.NotFoundException;
import com.solertium.vfs.PropertyDrivenVFS;
import com.solertium.vfs.VFS;
import com.solertium.vfs.VFSFactory;
import com.solertium.vfs.VFSMetadata;
import com.solertium.vfs.VFSPath;
import com.solertium.vfs.VFSPathToken;
import com.solertium.vfs.VersionedVFS;

/**
 * This implements layering of VFS abstractions to allow hierarchical storage
 * management. It applies a contractual relationship between a top and bottom
 * layer. The top layer VFS is the primary active read/write storage. The bottom
 * layer VFS is read-only, obscured by the top layer, and only receives delete
 * operations. If it is versioned, the versions are not visible.
 * 
 * Metadata operations apply to the top layer. If a file is not present in the
 * top layer and its metadata is modified, the file will be moved to the top
 * layer and its metadata copied.
 * 
 * @author robheittman
 * 
 */
public class LayeredVFS extends BaseVFS implements PropertyDrivenVFS,
		VersionedVFS {

	private VFS bottom = null;
	private VFS top = null;

	public void configure(final Properties properties, final File location)
			throws ConfigurationException {
		try {
			top = VFSFactory.getVFS(new File(location, properties
					.getProperty("vfs.layered.top")));
			bottom = VFSFactory.getVFS(new File(location, properties
					.getProperty("vfs.layered.bottom")));
		} catch (final NotFoundException nf) {
			throw new ConfigurationException(nf);
		}
	}

	public void copy(final VFSPath from, final VFSPath to)
			throws NotFoundException, ConflictException {
		if (top.exists(from))
			top.copy(from, to);
		else {
			final InputStream is = bottom.getInputStream(from);
			final OutputStream os = top.getOutputStream(to);
			try {
				BaseVFS.copyStream(is, os);
				os.close();
				is.close();
				top.setMetadata(to, bottom.getMetadata(from));
			} catch (final IOException io) {
				throw new ConflictException(io);
			}
		}
	}

	public void delete(final VFSPath uri) throws NotFoundException,
			ConflictException {
		if (top.exists(uri))
			top.delete(uri);
		if (bottom.exists(uri))
			bottom.delete(uri);
	}

	/**
	 * @deprecated Pass VFSPath instead of String for uri
	 */
	@Deprecated
	public void deleteVersion(final String uri, final String revision)
			throws NotFoundException, ConflictException {
		deleteVersion(new VFSPath(uri), revision);
	}

	public void deleteVersion(final VFSPath uri, final String revision)
			throws NotFoundException, ConflictException {
		// Version operations only work on the top
		if (top instanceof VersionedVFS)
			((VersionedVFS) top).deleteVersion(uri, revision);
	}

	/**
	 * @deprecated Pass VFSPath instead of String for uri
	 */
	@Deprecated
	public boolean exists(final String uri, final String revision) {
		return exists(new VFSPath(uri), revision);
	}

	public boolean exists(final VFSPath uri) {
		return top.exists(uri) || bottom.exists(uri);
	}

	public boolean exists(final VFSPath uri, final String revision) {
		return ((VersionedVFS) top).exists(uri, revision);
	}

	public String getETag(final VFSPath uri) throws NotFoundException {
		if (top.exists(uri))
			return top.getETag(uri);
		return bottom.getETag(uri);
	}

	/**
	 * @deprecated Pass VFSPath instead of String for uri
	 */
	@Deprecated
	public InputStream getInputStream(final String uri, final String revision)
			throws NotFoundException {
		return getInputStream(new VFSPath(uri), revision);
	}

	public InputStream getInputStream(final VFSPath uri)
			throws NotFoundException {
		if (top.exists(uri))
			return top.getInputStream(uri);
		return bottom.getInputStream(uri);
	}

	public InputStream getInputStream(final VFSPath uri, final String revision)
			throws NotFoundException {
		return ((VersionedVFS) top).getInputStream(uri, revision);
	}

	/**
	 * @deprecated Pass VFSPath instead of String for uri
	 */
	@Deprecated
	public long getLastModified(final String uri, final String revision)
			throws NotFoundException {
		return getLastModified(new VFSPath(uri), revision);
	}

	public long getLastModified(final VFSPath uri) throws NotFoundException {
		if (top.exists(uri))
			return top.getLastModified(uri);
		return bottom.getLastModified(uri);
	}

	public long getLastModified(final VFSPath uri, final String revision)
			throws NotFoundException {
		return ((VersionedVFS) top).getLastModified(uri, revision);
	}

	/**
	 * @deprecated Pass VFSPath instead of String for uri
	 */
	@Deprecated
	public long getLength(final String uri, final String revision)
			throws NotFoundException {
		return getLength(new VFSPath(uri), revision);
	}

	public long getLength(final VFSPath uri) throws NotFoundException {
		if (top.exists(uri))
			return top.getLength(uri);
		return bottom.getLength(uri);
	}

	public long getLength(final VFSPath uri, final String revision)
			throws NotFoundException {
		return ((VersionedVFS) top).getLength(uri, revision);
	}

	public VFSMetadata getMetadata(final VFSPath uri) {
		if (top.exists(uri))
			return top.getMetadata(uri);
		return bottom.getMetadata(uri);
	}

	public OutputStream getOutputStream(final VFSPath uri)
			throws NotFoundException, ConflictException {
		return top.getOutputStream(uri);
	}

	public ReadableByteChannel getReadableByteChannel(final VFSPath uri)
			throws NotFoundException {
		final InputStream is = getInputStream(uri);
		if (is instanceof FileInputStream)
			return ((FileInputStream) is).getChannel();
		return Channels.newChannel(is);
	}

	public Reader getReader(final VFSPath uri) throws NotFoundException {
		if (top.exists(uri))
			return top.getReader(uri);
		return bottom.getReader(uri);
	}

	/**
	 * @deprecated Pass VFSPath instead of String for uri
	 */
	@Deprecated
	public List<String> getRevisionIDsBefore(final String uri,
			final String revision, final int max) {
		return getRevisionIDsBefore(new VFSPath(uri), revision, max);
	}

	public List<String> getRevisionIDsBefore(final VFSPath uri,
			final String revision, final int max) {
		return ((VersionedVFS) top).getRevisionIDsBefore(uri, revision, max);
	}

	public String getString(final VFSPath uri) throws IOException,
			NotFoundException, BoundsException {
		if (top.exists(uri))
			return top.getString(uri);
		return bottom.getString(uri);
	}

	public WritableByteChannel getWritableByteChannel(final VFSPath uri)
			throws IOException, NotFoundException {
		final OutputStream os = getOutputStream(uri);
		if (os instanceof FileOutputStream)
			return ((FileOutputStream) os).getChannel();
		return Channels.newChannel(os);
	}

	public Writer getWriter(final VFSPath uri) throws NotFoundException,
			ConflictException {
		return top.getWriter(uri);
	}

	public boolean isCollection(final VFSPath uri) throws NotFoundException {
		if (top.exists(uri))
			return top.isCollection(uri);
		return bottom.isCollection(uri);
	}

	public VFSPathToken[] list(final VFSPath uri) throws NotFoundException {
		// combine lists from both layers
		final HashMap<VFSPathToken, Boolean> tokenMap = new HashMap<VFSPathToken, Boolean>();
		final Boolean FALSE = Boolean.valueOf(false);
		if (top.exists(uri))
			for (final VFSPathToken t : top.list(uri))
				tokenMap.put(t, FALSE);
		if (bottom.exists(uri))
			for (final VFSPathToken t : bottom.list(uri))
				tokenMap.put(t, FALSE);
		return tokenMap.keySet().toArray(new VFSPathToken[tokenMap.size()]);
	}

	public void makeCollection(final VFSPath uri) throws NotFoundException,
			ConflictException {
		top.makeCollection(uri);
	}

	public void makeCollections(final VFSPath uri) throws NotFoundException,
			ConflictException {
		top.makeCollections(uri);
	}

	public void move(final VFSPath from, final VFSPath to)
			throws NotFoundException, ConflictException {
		if (top.exists(from))
			top.move(from, to);
		else {
			final InputStream is = bottom.getInputStream(from);
			final OutputStream os = top.getOutputStream(to);
			try {
				BaseVFS.copyStream(is, os);
				os.close();
				is.close();
				top.setMetadata(to, bottom.getMetadata(from));
			} catch (final IOException io) {
				throw new ConflictException(io);
			}
		}
		if (bottom.exists(from))
			bottom.delete(from);
	}

	public void setLastModified(final VFSPath uri, final Date modified)
			throws NotFoundException {
		if (top.exists(uri))
			top.setLastModified(uri, modified);
		else
			try {
				final InputStream is = bottom.getInputStream(uri);
				final OutputStream os = top.getOutputStream(uri);
				BaseVFS.copyStream(is, os);
				os.close();
				is.close();
				top.setMetadata(uri, bottom.getMetadata(uri));
				top.setLastModified(uri, modified);
			} catch (final IOException io) {
				throw new NotFoundException(io);
			}
	}

	public void setMetadata(final VFSPath uri, final VFSMetadata metadata)
			throws ConflictException {
		if (top.exists(uri))
			top.setMetadata(uri, metadata);
		else
			try {
				final InputStream is = bottom.getInputStream(uri);
				final OutputStream os = top.getOutputStream(uri);
				BaseVFS.copyStream(is, os);
				os.close();
				is.close();
				top.setMetadata(uri, metadata);
			} catch (final IOException io) {
				throw new ConflictException(io);
			}
	}

}
