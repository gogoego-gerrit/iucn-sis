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

/**
 * A VFSAccessListener can be used to listen for ALL accesses to
 * a VFS.  This may have performance implications, and so a
 * VFSAccessListener should be used sparingly compared to a
 * VFSListener, which will only listen for change events.
 * 
 * VFSAccessListeners are fired synchronously instead of
 * asynchronously, PRIOR to access (read or write) as opposed
 * to AFTER access as ordinary VFSListeners.  They may
 * throw a RuntimeException to forcibly abort access. 
 * This allows them to be used as a brute form
 * of security backstop: e.g. interrogate the stack for
 * untrusted code and block access to sensitive data.
 * 
 * @author rob.heittman@solertium.com
 *
 */
public interface VFSAccessListener extends VFSListener {

}
