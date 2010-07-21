/*
 * Copyright (C) 2009 Solertium Corporation
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

import java.io.InputStream;
import java.util.List;

public interface VersionedVFS extends VFS {

	/**
	 * @deprecated Pass VFSPath instead of String for uri
	 */
	@Deprecated
	public void deleteVersion(final String uri, final String revision)
			throws NotFoundException, ConflictException;

	/**
	 * Removes a particular version
	 * 
	 * @param uri
	 *            VFS uri to be fetched
	 * @param revision
	 *            Revision ID to be fetched
	 * @throws NotFoundException
	 *             if either the URI or named revision ID cannot be found
	 * @throws ConflictException,
	 *             possibliy unnecessarily
	 */
	public void deleteVersion(final VFSPath uri, final String revision)
			throws NotFoundException, ConflictException;

	/**
	 * @deprecated Pass VFSPath instead of String for uri
	 */
	@Deprecated
	public boolean exists(final String uri, final String revision);

	/**
	 * Test to see if a particular revision ID exists for a given VFS URI.
	 * 
	 * @param uri
	 *            VFS URI to be fetched
	 * @param revision
	 *            Revision ID to be fetched
	 * @throws NotFoundException
	 *             if either the URI or the named revision ID cannot be found
	 */
	public boolean exists(final VFSPath uri, final String revision);

	/**
	 * @deprecated Pass VFSPath instead of String for uri
	 */
	@Deprecated
	public InputStream getInputStream(final String uri, final String revision)
			throws NotFoundException;

	/**
	 * Obtain an InputStream for a particular revision.
	 * 
	 * @param uri
	 *            VFS URI to be fetched
	 * @param revision
	 *            Revision ID to be fetched
	 * @return InputStream providing access to the named revision contents
	 * @throws NotFoundException
	 *             if either the URI or the named revision ID cannot be found
	 */
	public InputStream getInputStream(final VFSPath uri, final String revision)
			throws NotFoundException;

	/**
	 * @deprecated Pass VFSPath instead of String for uri
	 */
	@Deprecated
	public long getLastModified(final String uri, final String revision)
			throws NotFoundException;

	/**
	 * Obtain the date of a particular revision.
	 * 
	 * @param uri
	 *            VFS URI to be fetched
	 * @param revision
	 *            Revision ID to be fetched
	 * @throws NotFoundException
	 *             if either the URI or the named revision ID cannot be found
	 */
	public long getLastModified(final VFSPath uri, final String revision)
			throws NotFoundException;

	/**
	 * @deprecated Pass VFSPath instead of String for uri
	 */
	@Deprecated
	public long getLength(final String uri, final String revision)
			throws NotFoundException;

	/**
	 * Obtain the length of a particular revision.
	 * 
	 * @param uri
	 *            VFS URI to be fetched
	 * @param revision
	 *            Revision ID to be fetched
	 * @throws NotFoundException
	 *             if either the URI or the named revision ID cannot be found
	 */
	public long getLength(final VFSPath uri, final String revision)
			throws NotFoundException;

	/**
	 * @deprecated Pass VFSPath instead of String for uri
	 */
	@Deprecated
	public List<String> getRevisionIDsBefore(String uri, String revision,
			int max);

	/**
	 * Obtain a bounded list of revision IDs prior to a given revision.
	 * 
	 * @param uri
	 *            VFS URI to interrogate for revision history
	 * @param revision
	 *            Revision ID. Null to begin at the revision following "HEAD"
	 * @param max
	 *            Maximum number of revision IDs to return. -1 for no maximum
	 *            (may be expensive)
	 * @return List of revision IDs expressed as Strings. List will be empty if
	 *         the URI is invalid or not versioned.
	 */
	public List<String> getRevisionIDsBefore(VFSPath uri, String revision,
			int max);
}
