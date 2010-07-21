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
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import com.solertium.vfs.provider.FileVFS;
import com.solertium.vfs.provider.VersionedFileVFS;

/**
 * This factory is a stub for future expansion.
 * 
 * Note: From the pre-OSGi version, the ability to create Zip and Subversion
 * working copy VFS instances has been lost.  This factory needs to be able to
 * recognize the presence of other providers somehow (e.g. via a properties
 * file, registry, or discovery) and detect them when appropriate.
 * 
 * @author <a href="mailto:rob.heittman@solertium.com">Rob Heittman</a>, <a
 *         href="http://www.solertium.com">Solertium Corporation</a>
 */
public class VFSFactory {

	private final static ConcurrentHashMap<String, VFS> vfsObjectsInUse = new ConcurrentHashMap<String, VFS>();

	/**
	 * This will return a VersionedVFS. If the target cannot be associated with
	 * a VFS provider that supports the VersionedVFS interface, a
	 * NotFoundException will be thrown.
	 * 
	 * @param target
	 *            Path to the root of the VFS structure
	 * @return the VersionedVFS mounted at the named root
	 */
	public static VersionedVFS getVersionedVFS(final java.io.File target)
			throws NotFoundException {
		final VFS vfs = VFSFactory.getVFS(target);
		if (vfs instanceof VersionedVFS)
			return (VersionedVFS) vfs;
		else
			throw new NotFoundException("No versioned VFS detected at target "
					+ target.getPath());
	}

	/**
	 * This method is a stub that returns an appropriate type of VFS associated
	 * with a working copy in the local filesystem.
	 * <p>
	 * If HEAD and META folders are detected in the target, the method will
	 * return a VersionedFileVFS, which does trivial full-revision versioning in
	 * the local filesystem.
	 * <p>
	 * If .svn folders are detected in the target, the method will return a
	 * SubversionWorkingCopyVFS, which attempts to commit revisions to a backing
	 * Subversion repository.
	 * <p>
	 * Otherwise, the returned VFS will be a FileVFS (unversioned)
	 * <p>
	 * The target of a VersionedFileVFS or FileVFS may be a ZIP or other archive
	 * file supported by TrueZIP. This will involve different performance and
	 * space tradeoffs than a target stored in ordinary files and directories.
	 * 
	 * @param target
	 *            Path to the root of the VFS structure
	 * @return the VFS mounted at the named root
	 */
	public static VFS getVFS(final File target) throws NotFoundException {
		String specifier = null;
		try {
			specifier = "file://" + target.getCanonicalPath();
			final VFS existing = vfsObjectsInUse.get(specifier);
			if (existing != null)
				return existing;
		} catch (final IOException exception) {
			throw new NotFoundException(
					"Could not look up canonical name for VFS root", exception);
		}

		VFS result = null;

		if (!target.exists())
			throw new NotFoundException();

		if (target.isFile()) {
			if (target.getName().endsWith(".properties")) {
				final Properties properties = new Properties();
				try {
					final FileInputStream is = new FileInputStream(target);
					properties.load(is);
					is.close();
					result = VFSConfiguration.configure(properties, target
							.getParentFile());
				} catch (final IOException exception) {
					throw new NotFoundException(
							"Specified properties file could not be read.",
							exception);
				}
			} else
				throw new NotFoundException(
						"Cannot determine VFS type for file "
								+ target.getPath());
		} else if (new File(target, "HEAD").exists()
				&& new File(target, "META").exists())
			result = new VersionedFileVFS(target);
		else
			result = new FileVFS(target);

		vfsObjectsInUse.put(specifier, result);
		return result;
	}

	/**
	 * @deprecated Please supply a File to get behavior like the old String
	 *             constructor
	 * @param target
	 *            Path to the root of the VFS
	 * @return An appropriate type of VFS
	 * @throws NotFoundException
	 */
	@Deprecated
	public static VFS getVFS(final String target) throws NotFoundException {
		return VFSFactory.getVFS(new File(target));
	}
	
	/**
	 * Sets a VFS that was not created via the VFSFactory, mapping it to the 
	 * supplied target and returning true, assuming the supplied target is not
	 * already mapped to a VFS. The VFS can be subsequently fetched via getVFS(target).
	 * If a VFS is already set for that target, this method will NOT allow you to
	 * override that VFS, and will return false.
	 * 
	 * @param target
	 *    Path to the root of the VFS
	 * @param a VFS to use with the associated target
	 * @throws IOException
	 * @return true if VFS was set, false if a vfs was already set to that target
	 */
	public static boolean setVFS(final File target, final VFS vfs) throws IOException {
		if( !vfsObjectsInUse.containsKey(target) ) {
			String path = "file://" + target.getCanonicalPath();
			vfsObjectsInUse.put(path, vfs);
			return true;
		} else
			return false;
	}
}
