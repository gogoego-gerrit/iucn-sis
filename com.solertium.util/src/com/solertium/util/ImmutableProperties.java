/*
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
 *    http://www.gnu.org/licenses
 */
package com.solertium.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.InvalidPropertiesFormatException;
import java.util.Map;
import java.util.Properties;

/**
 * ImmutableProperties.java
 * 
 * A set of properties that can not be mutated.
 * 
 * UnsupportedOperationException is thrown for write methods.
 * 
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a
 *         href="http://www.solertium.com">Solertium Corporation</a>
 *
 */
public class ImmutableProperties extends Properties {
	
	private static final long serialVersionUID = 1L;
	
	public ImmutableProperties(final Properties original) {
		for (Map.Entry<Object, Object> entry : original.entrySet())
			super.put(entry.getKey(), entry.getValue());
	}
	
	public synchronized void load(InputStream inStream) throws IOException {
		throw new UnsupportedOperationException("Write operations are not allowed.");
	}
	
	public synchronized void loadFromXML(InputStream in) throws IOException, InvalidPropertiesFormatException {
		throw new UnsupportedOperationException("Write operations are not allowed.");
	}
	
	public synchronized Object put(Object key, Object value) {
		throw new UnsupportedOperationException("Write operations are not allowed.");
	}
	
	public synchronized void putAll(Map<? extends Object, ? extends Object> t) {
		throw new UnsupportedOperationException("Write operations are not allowed.");
	}
	
	public synchronized Object remove(Object key) {
		throw new UnsupportedOperationException("Write operations are not allowed.");
	}
	
	public synchronized void save(OutputStream out, String comments) {
		throw new UnsupportedOperationException("Write operations are not allowed.");
	}
	
	public synchronized Object setProperty(String key, String value) {
		throw new UnsupportedOperationException("Write operations are not allowed.");
	}
	
	public synchronized void store(OutputStream out, String comments) throws IOException {
		throw new UnsupportedOperationException("Write operations are not allowed.");
	}
	
	public synchronized void storeToXML(OutputStream os, String comment) throws IOException {
		throw new UnsupportedOperationException("Write operations are not allowed.");
	}

	public synchronized void storeToXML(OutputStream os, String comment, String encoding) throws IOException {
		throw new UnsupportedOperationException("Write operations are not allowed.");
	}
	
}
