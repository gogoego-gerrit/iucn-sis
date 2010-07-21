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
package com.solertium.vfs;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.List;

import com.solertium.util.Replacer;

public class VFSRevisionUtils {
	/**
	 * Reverts to the most recently created UNDO version of the file located at
	 * uri. Returns true on success, false if the file had no UNDO versions.
	 * 
	 * @param vfs
	 * @param uri
	 * @return returns false if the file had no version to revert it to
	 * @throws NotFoundException
	 * @throws ConflictException
	 */
	public static boolean revertToLastUndo(final VersionedVFS vfs,
			final VFSPath uri) throws NotFoundException, ConflictException {
		final List<String> revisions = vfs.getRevisionIDsBefore(uri, null, 1);

		if (revisions.isEmpty())
			return false;
		else {
			VFSRevisionUtils.revertToVersion(vfs, uri, revisions.get(0));
			return true;
		}
	}

	/**
	 * Reverts to the version of the file as specified.
	 * 
	 * @param vfs
	 * @param uri
	 * @param revision
	 * @throws NotFoundException
	 * @throws ConflictException
	 */
	public static void revertToVersion(final VersionedVFS vfs,
			final VFSPath uri, final String revision) throws NotFoundException,
			ConflictException {
		final OutputStream out = vfs.getOutputStream(uri);
		final InputStream in = vfs.getInputStream(uri, revision);

		int len;
		final byte[] temp = new byte[65536];

		try {
			len = in.read(temp);

			while (len != -1) {
				out.write(temp, 0, len);
				len = in.read(temp);
			}
		} catch (final IOException e) {
			throw new ConflictException(e);
		}
	}
	
	public static String getLastUndoString(final VersionedVFS vfs, final VFSPath uri) throws IOException 
	{
		final List<String> revisions = vfs.getRevisionIDsBefore(uri, null, 1);
		for (String revision: revisions)
		{
			System.out.println("This is revision " + revision);
		}

		if (revisions.isEmpty())
			return null;
		else {
			String revisionID = revisions.get(0);
			final InputStreamReader in = new InputStreamReader(vfs.getInputStream(uri, revisionID));
			StringWriter writer = new StringWriter();
			Replacer.copy(in, writer );
			return writer.toString();
		}
	}
}
