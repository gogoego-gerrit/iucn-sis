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

package com.solertium.vfs.provider.zip;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.Date;

import net.jcip.annotations.ThreadSafe;

import com.solertium.util.MD5Hash;
import com.solertium.util.Replacer;
import com.solertium.util.TrivialExceptionHandler;
import com.solertium.vfs.BaseVFS;
import com.solertium.vfs.BoundsException;
import com.solertium.vfs.ConflictException;
import com.solertium.vfs.NotFoundException;
import com.solertium.vfs.VFS;
import com.solertium.vfs.VFSMetadata;
import com.solertium.vfs.VFSPath;
import com.solertium.vfs.VFSPathToken;
import com.solertium.vfs.events.VFSCreateEvent;
import com.solertium.vfs.events.VFSDeleteEvent;
import com.solertium.vfs.events.VFSMoveEvent;
import com.solertium.vfs.events.VFSUpdateEvent;

import de.schlichtherle.io.File;
import de.schlichtherle.io.FileInputStream;
import de.schlichtherle.io.FileOutputStream;

/**
 * Implementation of the VFS interface using simple unversioned files in the
 * local filesystem.
 * 
 * @author <a href="mailto:rob.heittman@solertium.com">Rob Heittman</a>, <a
 *         href="http://www.solertium.com">Solertium Corporation</a>
 */
@ThreadSafe
public class ZipFileVFS extends BaseVFS implements VFS {

	private class WrappedFileOutputStream extends FileOutputStream {
		final private VFSPath uri;

		public WrappedFileOutputStream(final VFSPath uri, final File f)
				throws FileNotFoundException {
			super(f);
			this.uri = uri;
		}

		@Override
		public void close() throws IOException {
			super.close();
			notifyListeners(new VFSUpdateEvent(uri));
		}
	}

	private static final int BUFFER_SIZE = 65536;

	public final static void copyFile(final File in, final File out)
			throws IOException, FileNotFoundException {
		out.getParentFile().mkdirs();
		final FileInputStream fis = new FileInputStream(in);
		final FileOutputStream fos = new FileOutputStream(out);
		ZipFileVFS.copyStream(fis, fos);
		fis.close();
		fos.close();
		out.setLastModified(in.lastModified());
	}

	public final static void copyStream(final InputStream is,
			final OutputStream os) throws IOException {
		final byte[] buf = new byte[BUFFER_SIZE];
		int i = 0;
		while ((i = is.read(buf)) != -1)
			os.write(buf, 0, i);
	}

	public static VFS create(final File repositoryRoot)
			throws ConflictException {
		if (repositoryRoot.exists())
			throw new ConflictException();
		repositoryRoot.mkdirs();
		return new ZipFileVFS(repositoryRoot);
	}

	private final File repositoryRoot;

	public ZipFileVFS(final java.io.File jroot) {
		repositoryRoot = new File(jroot);
	}

	private void _delete(final File f) throws NotFoundException,
			ConflictException {
		if (f.isDirectory()) {
			final File[] children = f.listFiles(f.getArchiveDetector());
			for (final File child : children)
				_delete(child);
		}
		// TODO: check return value of f.delete() and do something
		// useful ... not sure what the right thing is
		f.delete();
	}

	protected File _getFileForURI(final VFSPath uri) {
		return new File(getRepositoryHead(), uri.toString());
	}

	public void copy(final VFSPath from, final VFSPath to)
			throws NotFoundException, ConflictException {
		final File ffrom = _getFileForURI(from);
		if (!ffrom.exists())
			throw new NotFoundException();
		final File fto = _getFileForURI(to);
		if (fto.exists())
			throw new ConflictException();
		try {
			recursiveCopy(ffrom, fto);
		} catch (final Exception ex) {
			throw new ConflictException();
		}
	}

	public void delete(final VFSPath uri) throws NotFoundException,
			ConflictException {
		final File f = _getFileForURI(uri);
		if (!f.exists())
			throw new NotFoundException();
		_delete(f);
		notifyListeners(new VFSDeleteEvent(uri));
	}

	public boolean exists(final VFSPath uri) {
		if (uri == null)
			return false;
		return _getFileForURI(uri).exists();
	}

	protected VFSPath fileToUrl(final File file) {
		return new VFSPath(file.getPath().replace(
				getRepositoryHead().getPath(), ""));
	}

	public String getETag(final VFSPath uri) throws NotFoundException {
		if (!exists(uri))
			throw new NotFoundException();
		final MD5Hash md5 = new MD5Hash();
		md5.update(uri.toString());
		md5.update("" + getLastModified(uri));
		md5.update("" + getLength(uri));
		return md5.toString();
	}

	protected FileInputStream getFileInputStream(final VFSPath uri)
			throws NotFoundException {
		try {
			return new FileInputStream(_getFileForURI(uri));
		} catch (final FileNotFoundException fnf) {
			throw new NotFoundException();
		}
	}

	protected FileOutputStream getFileOutputStream(final VFSPath uri)
			throws NotFoundException, ConflictException {
		final File f = _getFileForURI(uri);
		if (f.exists())
			_delete(f); // should throw exception if in use
		try {
			return new WrappedFileOutputStream(uri, _getFileForURI(uri));
		} catch (final FileNotFoundException fnf) {
			throw new NotFoundException();
		}
	}

	/**
	 * @deprecated Use this as a last resort; direct File operations will not go
	 *             through the VFS API and will almost certainly break things.
	 */
	@Deprecated
	public File getHead() {
		return repositoryRoot;
	}

	public InputStream getInputStream(final VFSPath uri)
			throws NotFoundException {
		return getFileInputStream(uri);
	}

	public long getLastModified(final VFSPath uri) throws NotFoundException {
		return getMetadata(uri).getLastModified();
	}

	public long getLength(final VFSPath uri) throws NotFoundException {
		return getMetadata(uri).getLength();
	}

	public VFSMetadata getMetadata(final VFSPath uri) {
		final VFSMetadata ret = new VFSMetadata();
		ret.sync(_getFileForURI(uri));
		return ret;
	}

	public OutputStream getOutputStream(final VFSPath uri)
			throws NotFoundException, ConflictException {
		return getFileOutputStream(uri);
	}

	public ReadableByteChannel getReadableByteChannel(final VFSPath uri)
			throws NotFoundException {
		return Channels.newChannel(getFileInputStream(uri));
	}

	public Reader getReader(final VFSPath uri) throws NotFoundException {
		Reader reader = null;
		try {
			reader = new InputStreamReader(getInputStream(uri), "UTF-8");
		} catch (final UnsupportedEncodingException unpossible) {
			throw new RuntimeException("UTF-8 support expected, but missing");
		}
		return reader;
	}

	protected File getRepositoryHead() {
		return repositoryRoot;
	}

	public File getRepositoryRoot() {
		return repositoryRoot;
	}

	public String getString(final VFSPath uri) throws IOException,
			NotFoundException, BoundsException {
		if (exists(uri) && !isCollection(uri)) {
			final long l = getLength(uri);
			if (l > 1024 * 1024)
				throw new BoundsException(
						"File is >1MB, too large to process as a String");
			final StringWriter writer = new StringWriter((int) l);
			Replacer.copy(getReader(uri), writer);
			writer.close();
			return writer.toString();
		}
		throw new NotFoundException();
	}

	public WritableByteChannel getWritableByteChannel(final VFSPath uri)
			throws IOException, NotFoundException {
		return Channels.newChannel(getFileOutputStream(uri));
	}

	public Writer getWriter(final VFSPath uri) throws NotFoundException,
			ConflictException {
		Writer w = null;
		try {
			w = new OutputStreamWriter(getOutputStream(uri), "UTF-8");
		} catch (final UnsupportedEncodingException unpossible) {
			throw new RuntimeException("UTF-8 support expected, but missing");
		}
		return w;
	}

	public boolean isCollection(final VFSPath uri) throws NotFoundException {
		if (!exists(uri))
			throw new NotFoundException();
		return _getFileForURI(uri).isDirectory();
	}

	public VFSPathToken[] list(final VFSPath uri) throws NotFoundException {
		if (!exists(uri))
			throw new NotFoundException();
		if (!isCollection(uri))
			throw new NotFoundException();
		final File f = _getFileForURI(uri);
		final File[] files = f.listFiles(f.getArchiveDetector());

		final ArrayList<VFSPathToken> names = new ArrayList<VFSPathToken>();
		for (final File element : files)
			try {
				names.add(new VFSPathToken(element.getName()));
			} catch (final Exception e) {
				TrivialExceptionHandler.ignore(this, e);
			}

		final VFSPathToken[] uris = new VFSPathToken[names.size()];
		for (int i = 0; i < names.size(); i++)
			uris[i] = names.get(i);

		/*
		 * for (int i = 0; i < files.length; i++) { try { uris[i] = new
		 * VFSPathToken(files[i].getName()); } catch (IllegalArgumentException
		 * e) { uris[i] = new VFSPathToken(""); } }
		 */

		if (files.length != uris.length)
			System.err.print("Warning: " + (files.length - uris.length)
					+ " files have "
					+ "bad names and will not appear in this list.");

		return uris;
	}

	public void makeCollection(final VFSPath uri) throws NotFoundException,
			ConflictException {
		final File f = _getFileForURI(uri);
		if (f.exists())
			throw new NotFoundException();
		if (!f.getParentFile().exists())
			throw new ConflictException();
		f.mkdir();
		notifyListeners(new VFSCreateEvent(uri));
		return;
	}

	public void makeCollections(final VFSPath uri) throws NotFoundException,
			ConflictException {
		final File f = _getFileForURI(uri);
		if (f.exists())
			throw new NotFoundException();
		f.mkdirs();
		// TODO: include created parents in create event uri list
		notifyListeners(new VFSCreateEvent(uri));
		return;
	}

	public void move(final VFSPath from, final VFSPath to)
			throws NotFoundException, ConflictException {
		final File ffrom = _getFileForURI(from);
		if (!ffrom.exists())
			throw new NotFoundException();
		final File fto = _getFileForURI(to);
		if (fto.exists())
			throw new ConflictException();
		try {
			safeMove(ffrom, fto);
		} catch (final Exception ex) {
			throw new ConflictException();
		}
	}

	protected void recursiveCopy(final File from, final File to)
			throws NotFoundException, IOException, ConflictException {
		if (from.isDirectory()) {
			to.mkdir();

			final File[] children = from.listFiles(from.getArchiveDetector());
			for (final File element : children)
				recursiveCopy(element, new File(to, element.getName()));
		} else {
			ZipFileVFS.copyFile(from, to);
			setMetadata(fileToUrl(to), getMetadata(fileToUrl(from)).copy());
		}
	}

	protected void safeMove(final File src, final File tgt)
			throws ConflictException, IOException {
		final File parent = new File(tgt.getParent());
		if (!parent.exists())
			synchronized (ZipFileVFS.class) {
				parent.mkdirs();
			}
		if (!parent.exists())
			throw new IOException("Failed to create directory " + tgt.getPath());
		safeRename(src, tgt);
		notifyListeners(new VFSMoveEvent(fileToUrl(src), fileToUrl(tgt)));
	}

	protected void safeRename(final File src, final File tgt)
			throws ConflictException, IOException {
		if (tgt.exists())
			throw new ConflictException("Target exists");
		final boolean renameResult = src.renameTo(tgt);
		if (renameResult)
			return;
		// rename operation failed if we get to here ...
		// attempt copy instead
		final FileInputStream is = new FileInputStream(src);
		final FileOutputStream os = new FileOutputStream(tgt);
		ZipFileVFS.copyStream(is, os);
		is.close();
		os.close();
		notifyListeners(new VFSDeleteEvent(fileToUrl(src)));
		notifyListeners(new VFSCreateEvent(fileToUrl(tgt)));
	}

	public void setLastModified(final VFSPath uri, final Date modified) {
		final File fmod = _getFileForURI(uri);
		fmod.setLastModified(modified.getTime());
	}

	public void setMetadata(final VFSPath uri, final VFSMetadata metadata)
			throws ConflictException {
		// doesn't do anything useful in FileVFS
	}

}
