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

package com.solertium.vfs.events;

import com.solertium.vfs.VFSPath;

/**
 * Base class of all VFS events.
 * 
 * @author <a href="mailto:rob.heittman@solertium.com">Rob Heittman</a>, <a
 *         href="http://www.solertium.com">Solertium Corporation</a>
 */
public abstract class VFSEvent {

	final VFSPath[] uris;

	public VFSEvent(final VFSPath... uris) {
		this.uris = uris.clone();
	}

	/**
	 * An event affects one or more URIs in the VFS. The VFS implementation may
	 * deliver any number of the affected URIs with one event notification. For
	 * performance reasons, this list may be "chunked," i.e. a recursive delete
	 * of 100,000 files might generate 200 VFSDeleteEvents each containing 500
	 * URIs. It should, however, be guaranteed that each VFS API call resulting
	 * in an event generates a single event. If vfs.delete() were called 100,000
	 * discrete times, this would generate 100,000 discrete events. Thus, there
	 * may be more events than API calls, but never less.
	 * 
	 * @return The URIs to which this event pertains
	 */
	public VFSPath[] getURIs() {
		return uris.clone();
	}

}
