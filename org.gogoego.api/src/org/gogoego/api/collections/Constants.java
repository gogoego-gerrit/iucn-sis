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

package org.gogoego.api.collections;

/**
 * Constants.java
 * 
 * Simple constants
 * 
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a href="http://www.solertium.com">Solertium Corporation</a>
 *
 */
public class Constants {

	/**
	 * The root filename for all collection-based items
	 */
	public static final String COLLECTION_ROOT_FILENAME = "index.xml";
	
	/**
	 * The standard no-image URI returned when image resizing fails
	 */
	public static final String NO_IMAGE = "/images/no-image.gif";
	
	/**
	 * Base reference, as inserted via Dynamic Context during the 
	 * templating process.
	 */
	public static final String BASE_REFERENCE = "baseReference";
	
	public static final String FORCE_MAGIC = "forceMagic";
	
	public static final String USES_DYNAMIC_TEMPLATING = "usesDynamicTemplating";
	
	public final static String EDITMODE = "com.solertium.gogoego.server.editMode";
	
	public static final String VFS_URI = "vfsUri";
	
	public static final String BASE_REPRESENTATION = "baseRepresentation";
	
	public static final String DISABLE_EXPIRATION = "disableExpiration";
	
	public static final String ALLOW_GZIP = "allowGZIP";
	
	public static final String COLLECTION_CACHE_CONTENT = "com.solertium.gogoego.server.collections.cache.content";
	
	public static final String TOP_LEVEL_REQUEST = "com.solertium.gogoego.server.magic.topLevelRequest";
	
	/**
	 * This request comes from a shortcutted URL
	 */
	public static final String SHORTCUT = "com.solertium.gogoego.server.magic.isShortcut";
	
	/**
	 * Enable the memory cache
	 */
	public static final String PROPERTY_MEMORY_CACHE_ENABLED = "com.solertium.gogoego.server.cache.enabled";
	
	/**
	 * Set the memory cache size
	 */
	public static final String PROPERTY_MEMORY_CACHE_SIZE = "com.solertium.gogoego.server.cache.size";
	
	/**
	 * Use eviction = full | normal .  Full will evict entire cache on change, normal will evict only what's 
	 * deemed necessary.  If you are having caching problems with normal mode, use full.
	 */
	public static final String PROPERTY_MEMORY_CACHE_EVICTION = "com.solertium.gogoego.server.cache.eviction";
	
	/**
	 * Enable the collection cache
	 */
	public static final String PROPERTY_COLLECTION_CACHE_ENABLED = "com.solertium.gogoego.server.collections.cache.enabled";
	
	/**
	 * Set the collection cache size
	 */
	public static final String PROPERTY_COLLECTION_CACHE_SIZE = "com.solertium.gogoego.server.collections.cache.size";
	
	/**
	 * Use eviction = full | normal .  Full will evict entire cache on change, normal will evict only what's 
	 * deemed necessary.  If you are having caching problems with normal mode, use full.
	 */
	public static final String PROPERTY_COLLECTION_CACHE_EVICTION = "com.solertium.gogoego.server.collections.cache.eviction";
	
	/**
	 * Logging property
	 */
	public static final String PROPERTY_CONSOLE_LOGGING = "com.solertium.gogoego.server.logging";

}
