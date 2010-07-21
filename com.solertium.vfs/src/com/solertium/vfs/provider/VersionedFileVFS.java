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

package com.solertium.vfs.provider;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.jcip.annotations.ThreadSafe;

import com.solertium.util.AlphanumericComparator;
import com.solertium.vfs.ConflictException;
import com.solertium.vfs.NotFoundException;
import com.solertium.vfs.PartException;
import com.solertium.vfs.VFS;
import com.solertium.vfs.VFSMetadata;
import com.solertium.vfs.VFSPath;
import com.solertium.vfs.VersionedVFS;
import com.solertium.vfs.events.VFSCreateEvent;
import com.solertium.vfs.events.VFSDeleteEvent;
import com.solertium.vfs.events.VFSUpdateEvent;
import com.thoughtworks.xstream.XStream;

/**
 * Implementation of the VFS interface using files in a local filesystem, with
 * different top level folders handling metadata and versioning.
 * 
 * @author <a href="mailto:rob.heittman@solertium.com">Rob Heittman</a>, <a
 *         href="http://www.solertium.com">Solertium Corporation</a>
 */
@ThreadSafe
public class VersionedFileVFS extends FileVFS implements VersionedVFS {

	private class VersionedWrappedFileOutputStream extends FileOutputStream {
		private boolean closed = false;
		private final File f;
		private final VFSPath uri;

		public VersionedWrappedFileOutputStream(final VFSPath uri, final File f)
				throws FileNotFoundException {
			super(f);
			this.uri = uri;
			this.f = f;
		}

		@Override
		public synchronized void close() throws IOException, PartException {
			if (closed)
				return; // deal with multiple close operations
			super.close();
			doVersioning();
			notifyListeners(new VFSUpdateEvent(uri));
			closed = true;
		}

		public void doVersioning() throws PartException {
			final VFSMetadata metadata = getMetadata(uri);
			final File target = _getFileForURI(uri);
			if (target.exists())
				// System.out.println("VFS ))) exists: "+target.getPath());
				if (metadata.isVersioned()) {
					// System.out.println("VFS ))) is versioned:
					// "+target.getPath());
					int inc = 1;
					final File revisions = new File(repositoryUndo, uri
							+ "/revisions");
					if (revisions.exists())
						try {
							final BufferedReader br = new BufferedReader(
									new FileReader(revisions));
							inc = Integer.parseInt(br.readLine());
							br.close();
						} catch (final Exception triviallyLogged) {
							triviallyLogged.printStackTrace();
						}
					File backup = new File(repositoryUndo, uri + "/" + inc
							+ ".UNDO");
					while (backup.exists()) {
						inc++;
						backup = new File(repositoryUndo, uri + "/" + inc
								+ ".UNDO");
					}
					backup.getParentFile().mkdirs();
					try {
						safeRename(target, backup);
						final FileWriter fw = new FileWriter(revisions);
						fw.write("" + inc + "\n");
						fw.close();
						// System.out.println("VFS ))) rev = "+inc+" for
						// "+revisions.getPath());
					} catch (final Exception logged) {
						logged.printStackTrace();
					}
				} else // System.out.println("VFS ))) NOT versioned:
				// "+target.getPath());
				if (!target.delete())
					System.out
							.println("VFS: Unable to delete target!! This is bad!!");
			try {
//				 System.out.println("VFS: File path: " + f.getPath());
//				 System.out.println("VFS: Target path: " + target.getPath());
				if (!f.getPath().equals(target.getPath()))
					// System.out.println("VFS ))) renaming "+f.getName()+" to
					// "+target.getName());
					safeRename(f, target);
			} catch (final IOException logged) {
				logged.printStackTrace();
				throw new PartException("File exists and could not be moved.",
						logged);
			}
			metadata.sync(target);
		}
	}

	private static int sn = 1;

	public static VFS create(final File repositoryRoot)
			throws ConflictException {
		if (repositoryRoot.exists())
			throw new ConflictException();
		repositoryRoot.mkdirs();
		new File(repositoryRoot, "HEAD").mkdirs();
		return new VersionedFileVFS(repositoryRoot);
	}

	private static synchronized int getSerial() {
		sn++;
		if (sn > 999999)
			sn = 1;
		return sn;
	}

	private final Map<VFSPath, VFSMetadata> metadataCache = new HashMap<VFSPath, VFSMetadata>();

	private final File repositoryGen;

	private final File repositoryMeta;

	private final File repositoryUndo;

	private final File repositoryXHead;

	public VersionedFileVFS(final File root) {
		super(root);
		repositoryXHead = new File(getRepositoryRoot(), "HEAD");
		repositoryUndo = new File(getRepositoryRoot(), "UNDO");
		repositoryMeta = new File(getRepositoryRoot(), "META");
		repositoryGen = new File(getRepositoryRoot(), "GEN");
		if (getRepositoryHead().exists()) { // recreate the rest if needed
			repositoryMeta.mkdirs();
			repositoryUndo.mkdirs();
			repositoryGen.mkdirs();
		}
	}
	
	public boolean deleteVersioning(final VFSPath uri) {
		final VFSMetadata metadata = getMetadata(uri);
		final File target = _getFileForURI(uri);
		if (target.exists())
			// System.out.println("VFS ))) exists: "+target.getPath());
			if (metadata.isVersioned()) {
				// System.out.println("VFS ))) is versioned:
				// "+target.getPath());
				int inc = 1;
				final File revisions = new File(repositoryUndo, uri
						+ "/revisions");
				if (revisions.exists())
					try {
						final BufferedReader br = new BufferedReader(
								new FileReader(revisions));
						inc = Integer.parseInt(br.readLine());
						br.close();
					} catch (final Exception triviallyLogged) {
						triviallyLogged.printStackTrace();
					}
				File backup = new File(repositoryUndo, uri + "/" + inc
						+ ".UNDO");
				while (backup.exists()) {
					inc++;
					backup = new File(repositoryUndo, uri + "/" + inc
							+ ".UNDO");
				}
				backup.getParentFile().mkdirs();
				try {
					safeRename(target, backup);
					final FileWriter fw = new FileWriter(revisions);
					fw.write("" + inc + "\n");
					fw.close();
					// System.out.println("VFS ))) rev = "+inc+" for
					// "+revisions.getPath());
				} catch (final Exception logged) {
					logged.printStackTrace();
					return false;
				}
			} else // System.out.println("VFS ))) NOT versioned:
			// "+target.getPath());
			if (!target.delete()) {
				System.out
						.println("VFS: Unable to delete target!! This is bad!!");
				return false;
			}

		metadata.sync(target);
		return true;
	}

	private File _getFileForURI(final File root, final VFSPath uri) {
		return new File(root, uri.toString());
	}

	@Override
	protected File _getFileForURI(final VFSPath uri) {
		if (getRepositoryHead() == null)
			throw new RuntimeException("This VFS " + this
					+ " is not initialized, do not use it");
		File ret;
		final VFSMetadata meta = getMetadata(uri);
		if (meta.isGenerated())
			ret = _getFileForURI(repositoryGen, uri);
		else
			ret = _getFileForURI(getRepositoryHead(), uri);
		return ret;
	}

	@Override
	public void delete(final VFSPath uri) throws NotFoundException,
			ConflictException {
		
		if( !deleteVersioning(uri) )
			super.delete(uri);
		
		metadataCache.remove(uri);
		final File mf = _getFileForURI(repositoryMeta, uri);
		mf.delete();
		return;
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
		final File file = new File(repositoryUndo, uri + "/" + revision
				+ ".UNDO");
		if (!file.exists())
			throw new NotFoundException();
		file.delete();
		return;
	}

	/**
	 * @deprecated Pass VFSPath instead of String for uri
	 */
	@Deprecated
	public boolean exists(final String uri, final String revision) {
		return exists(new VFSPath(uri), revision);
	}

	public boolean exists(final VFSPath uri, final String revision) {
		return new File(repositoryUndo, uri + "/" + revision + ".UNDO")
				.exists();
	}

	@Override
	protected VFSPath fileToUrl(final File file) {
		if (file.getPath().startsWith(repositoryGen.getPath()))
			return new VFSPath(file.getPath().replace(repositoryGen.getPath(),
					""));
		else if (file.getPath().startsWith(getRepositoryHead().getPath()))
			return new VFSPath(file.getPath().replace(
					getRepositoryHead().getPath(), ""));
		else if (file.getPath().startsWith(repositoryUndo.getPath()))
			return new VFSPath(file.getPath().replace(repositoryUndo.getPath(),
					""));
		else if (file.getPath().startsWith(repositoryMeta.getPath()))
			return new VFSPath(file.getPath().replace(repositoryMeta.getPath(),
					""));
		else
			return null;
	}
	
	public void generateCollection(final VFSPath uri) throws NotFoundException,
		ConflictException {
		final File f = _getFileForURI(repositoryGen, uri);
		if (f.exists())
			throw new NotFoundException();
		if (!f.getParentFile().exists())
			throw new ConflictException();
		f.mkdir();
		notifyListeners(new VFSCreateEvent(uri));
		return;
	}
		
	public void generateCollections(final VFSPath uri) throws NotFoundException,
		ConflictException {
		final File f = _getFileForURI(repositoryGen, uri);
		if (f.exists())
			throw new NotFoundException();
		f.mkdirs();
		// TODO: include created parents in create event uri list
		notifyListeners(new VFSCreateEvent(uri));
		return;
	}

	@Override
	protected FileOutputStream getFileOutputStream(final VFSPath uri)
			throws NotFoundException, ConflictException {
		final VFSPath partUri;
		partUri = new VFSPath(uri.getCollection() + "/." + getName(uri) + "."
				+ VersionedFileVFS.getSerial() + ".part");

		final File part = _getFileForURI(partUri);
		if (part.exists())
			part.delete();
		// System.out.println("VFS ))) writing to "+partUri);
		// System.out.println("Path: " + part.getPath() + "\r\nWas: " + uri);
		try {
			return new VersionedWrappedFileOutputStream(uri, part);
		} catch (final FileNotFoundException fnf) {
			try {
				// attempt to make parent directory
				part.getParentFile().mkdirs();
				return new VersionedWrappedFileOutputStream(uri, part);
			} catch (final FileNotFoundException reallyNotFound) {
				throw new NotFoundException();
			}
		}
	}

	/**
	 * @deprecated Use this as a last resort; direct File operations will not go
	 *             through the VFS API and will almost certainly break things.
	 */
	@Deprecated
	@Override
	public File getHead() {
		return repositoryXHead;
	}

	/**
	 * @deprecated Pass VFSPath instead of String for uri
	 */
	@Deprecated
	public InputStream getInputStream(final String uri, final String revision)
			throws NotFoundException {
		return getInputStream(new VFSPath(uri), revision);
	}

	/**
	 * {@inheritDoc}
	 */
	public InputStream getInputStream(final VFSPath uri, final String revision)
			throws NotFoundException {
		try {
			return new FileInputStream(new File(repositoryUndo, uri + "/"
					+ revision + ".UNDO"));
		} catch (final FileNotFoundException fnf) {
			throw new NotFoundException();
		}
	}

	/**
	 * @deprecated Pass VFSPath instead of String for uri
	 */
	@Deprecated
	public long getLastModified(final String uri, final String revision)
			throws NotFoundException {
		return getLastModified(new VFSPath(uri), revision);
	}

	/**
	 * {@inheritDoc}
	 */
	public long getLastModified(final VFSPath uri, final String revision)
			throws NotFoundException {
		final File f = new File(repositoryUndo, uri + "/" + revision + ".UNDO");
		if (!f.exists())
			throw new NotFoundException();
		return f.lastModified();
	}

	/**
	 * @deprecated Pass VFSPath instead of String for uri
	 */
	@Deprecated
	public long getLength(final String uri, final String revision)
			throws NotFoundException {
		return getLength(new VFSPath(uri), revision);
	}

	/**
	 * {@inheritDoc}
	 */
	public long getLength(final VFSPath uri, final String revision)
			throws NotFoundException {
		final File f = new File(repositoryUndo, uri + "/" + revision + ".UNDO");
		if (!f.exists())
			throw new NotFoundException();
		return f.length();
	}

	@Override
	public VFSMetadata getMetadata(final VFSPath uri) {
		// look in metadata cache
		VFSMetadata ret = metadataCache.get(uri);
		if (ret == null) {
			final File f = _getFileForURI(repositoryMeta, new VFSPath(uri
					+ ".meta"));
			if (f.exists())
				try {
					ret = loadMetadata(f);
				} catch (final IOException failureToLoad) {
					ret = null;
				} catch (Exception e) {
					System.err.println("Unexpected failure loading metadata for " + uri + ": " + e.getMessage());
					ret = null;
				}
			if (ret == null)
				ret = new VFSMetadata();
			if (ret.isGenerated())
				ret.sync(_getFileForURI(repositoryGen, uri));
			else
				ret.sync(_getFileForURI(getRepositoryHead(), uri));
		}
		return ret;
	}

	@Override
	protected File getRepositoryHead() {
		return repositoryXHead;
	}

	/**
	 * @deprecated Pass VFSPath instead of String for uri
	 */
	@Deprecated
	public List<String> getRevisionIDsBefore(final String uri,
			final String revision, final int max) {
		return getRevisionIDsBefore(new VFSPath(uri), revision, max);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<String> getRevisionIDsBefore(final VFSPath uri,
			final String revision, final int max) {
		final ArrayList<String> revisionIDs = new ArrayList<String>();
		final String irevision = (revision == null) ? "" + Integer.MAX_VALUE
				+ ".UNDO" : revision + ".UNDO";
		final File revisionContainer = new File(repositoryUndo, uri.toString());
		final String[] vers = revisionContainer.list();
		if (vers == null)
			return revisionIDs;
		final Comparator<CharSequence> ac = Collections
				.reverseOrder(new AlphanumericComparator());
		Arrays.sort(vers, ac);
		int returned = 0;
		for (final String s : vers)
			if (ac.compare(irevision, s) < 0) {
				returned++;
				revisionIDs.add(s.replace(".UNDO", ""));
				if ((max != -1) && (returned == max))
					break;
			}
		return revisionIDs;
	}

	@Override
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

	private VFSMetadata loadMetadata(final File f) throws IOException {
		final XStream xstream = new XStream();
		xstream.alias("VFSMetadata", VFSMetadata.class);
		FileInputStream fis = new FileInputStream(f);
		VFSMetadata md = (VFSMetadata) xstream.fromXML(fis);
		fis.close();
		return md;
	}

	@Override
	public void move(final VFSPath from, final VFSPath to)
			throws NotFoundException, ConflictException {
		super.move(from, to);
		try {
			setMetadata(to, removeMetadata(from));
		} catch (final Exception ex) {
			System.out.println("Failed to set metadata: " + ex.getMessage());
			// throw new ConflictException();
		}
	}

	public VFSMetadata removeMetadata(final VFSPath uri)
			throws ConflictException {
		if (!metadataCache.containsKey(uri))
			throw new ConflictException();

		return metadataCache.remove(uri);
	}

	@Override
	protected void safeRename(final File src, final File tgt)
			throws IOException {
		if (tgt.exists()) {
			final String collision_fn = tgt.getName() + ".COLLISION."
					+ System.currentTimeMillis() + "."
					+ VersionedFileVFS.getSerial();
			boolean collideResult = tgt.renameTo(new File(tgt.getParentFile(),
					collision_fn));
			if (!collideResult)
				throw new IOException("Target file exists and cannot be moved.");
		}
		final boolean renameResult = src.renameTo(tgt);
		if (renameResult)
			return;
		// rename operation failed if we get to here ...
		// attempt copy instead
		final FileInputStream is = new FileInputStream(src);
		final FileOutputStream os = new FileOutputStream(tgt);
		FileVFS.copyStream(is, os);
		is.close();
		os.close();
		notifyListeners(new VFSDeleteEvent(fileToUrl(src)));
		notifyListeners(new VFSCreateEvent(fileToUrl(tgt)));
	}

	private void saveMetadata(final VFSMetadata m, final File f)
			throws IOException {
		f.getParentFile().mkdirs();
		final XStream xstream = new XStream();
		xstream.alias("VFSMetadata", m.getClass());
		FileOutputStream fos = new FileOutputStream(f);
		xstream.toXML(m, fos);
		fos.flush();
		fos.close();
	}

	@Override
	public void setMetadata(final VFSPath uri, final VFSMetadata metadata)
			throws ConflictException {
		if (exists(uri)) {
			final VFSMetadata old = getMetadata(uri);
			if (old.isGenerated() != metadata.isGenerated())
				throw new ConflictException();
		}
		metadataCache.put(uri, metadata);
		try {
			saveMetadata(metadata, _getFileForURI(repositoryMeta, new VFSPath(
					uri + ".meta")));
		} catch (final IOException x) {
			throw new ConflictException(x);
		} // couldn't save
	}

}
