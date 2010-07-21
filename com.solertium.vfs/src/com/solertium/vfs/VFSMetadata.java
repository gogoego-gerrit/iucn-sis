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
import java.util.HashMap;

import net.jcip.annotations.ThreadSafe;

/**
 * Structure capturing metadata for VFS file objects. Metadata is persisted
 * using XStream.
 * 
 * @author <a href="mailto:rob.heittman@solertium.com">Rob Heittman</a>, <a
 *         href="http://www.solertium.com">Solertium Corporation</a>
 */

@ThreadSafe
public class VFSMetadata {

	public static final String ALWAYS_ALLOW = "alwaysAllow";
	public static final String PASSWORD_PROTECTED = "passwordProtected";
	public static final String SECURE_REJECT_ALL = "rejectAll";
	public static final String SECURE_REJECT_PUBLIC = "rejectPublic";

	private static final long serialVersionUID = 2L;

	private HashMap<String, String> arbitraryData = new HashMap<String, String>();

	private String description = null;

	private long expires = 0;

	private boolean generated = false;

	private boolean hidden = false;

	private long lastModified = 0;

	private transient long length = 0;

	private HashMap<String, String> securityProperties = new HashMap<String, String>();

	private transient boolean sync = false;

	private boolean versioned = true;

	public synchronized void addArbitraryData(final String key,
			final String value) {
		arbitraryData.put(key, value);
	}

	public synchronized void addSecurityProperty(final String key,
			final String value) {
		securityProperties.put(key, value);
	}

	public synchronized VFSMetadata copy() {
		final VFSMetadata copy = new VFSMetadata();
		copy.setArbitraryData(arbitraryData);
		copy.setGenerated(generated);
		copy.setVersioned(versioned);
		copy.setHidden(hidden);
		copy.setExpires(expires);
		copy.setLastModified(lastModified);
		copy.setLength(length);
		copy.setSecurityProperties(securityProperties);
		copy.setSync(sync);
		return copy;
	}

	public synchronized HashMap<String, String> getArbitraryData() {
		return arbitraryData;
	}

	public synchronized String getDescription() {
		return description;
	}

	public synchronized long getExpires() {
		return expires;
	}

	public synchronized long getLastModified() {
		return lastModified;
	}

	public synchronized long getLength() {
		return length;
	}

	public synchronized HashMap<String, String> getSecurityProperties() {
		return securityProperties;
	}

	public synchronized boolean isGenerated() {
		return generated;
	}

	public synchronized boolean isHidden() {
		return hidden;
	}

	public synchronized boolean isSync() {
		return sync;
	}

	public synchronized boolean isVersioned() {
		return versioned;
	}

	public synchronized void setArbitraryData(
			final HashMap<String, String> arbitraryData) {
		this.arbitraryData = arbitraryData;
	}

	public synchronized void setDescription(final String description) {
		this.description = description;
	}

	public synchronized void setExpires(final long expires) {
		this.expires = expires;
	}

	public synchronized void setGenerated(final boolean generated) {
		this.generated = generated;
	}

	public synchronized void setHidden(final boolean hidden) {
		this.hidden = hidden;
	}

	private synchronized void setLastModified(final long lastModified) {
		this.lastModified = lastModified;
	}

	private synchronized void setLength(final long length) {
		this.length = length;
	}

	public synchronized void setSecurityProperties(
			final HashMap<String, String> securityProperties) {
		this.securityProperties = securityProperties;
	}

	private synchronized void setSync(final boolean sync) {
		this.sync = sync;
	}

	public synchronized void setVersioned(final boolean versioned) {
		this.versioned = versioned;
	}

	public synchronized void sync(final File f) {
		if ((f == null) || !f.exists())
			return; // nothing to sync to
		setLastModified(f.lastModified());
		setLength(f.length());
		setSync(true);
	}

}
