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
package org.gogoego.api.plugins;

import java.io.File;
import java.util.Map;

import org.gogoego.api.applications.GoGoEgoApplication;
import org.gogoego.api.applications.ServerApplicationAPI;
import org.gogoego.api.applications.TemplateDataAPI;
import org.gogoego.api.images.ImageManipulator;
import org.gogoego.api.images.ImageManipulatorHelper;
import org.gogoego.api.images.ImageManipulatorPreferences;
import org.gogoego.api.representations.GoGoEgoBaseRepresentation;
import org.osgi.framework.BundleContext;
import org.restlet.Context;
import org.restlet.data.CookieSetting;

import com.solertium.vfs.NotFoundException;

public interface GoGoEgoAPI {
	
	/**
	 * Given a key of a registered template and a base, find the template and 
	 * use it to template the given base representation. 
	 * @param templateKey the template key 
	 * @param baseRepresentation the base representation
	 * @return the new templated representation
	 * @throws NotFoundException thrown if the template is not found
	 */
	public GoGoEgoBaseRepresentation applyTemplating(final String templateKey, final GoGoEgoBaseRepresentation baseRepresentation) throws NotFoundException;
	
	/**
	 * Given the template data and the base representation, template the content
	 * @param template the template data
	 * @param baseRepresentation the base representation
	 * @return the new templated content
	 */
	public GoGoEgoBaseRepresentation applyTemplating(final TemplateDataAPI template, final GoGoEgoBaseRepresentation baseRepresentation);
	
	/**
	 * Given two representations, template the first with the second.  A registered 
	 * template need not be used here.
	 * @param templateRepresentation the template
	 * @param baseRepresentation the base
	 * @return the new templated content
	 */
	public GoGoEgoBaseRepresentation applyTemplating(final GoGoEgoBaseRepresentation templateRepresentation, final GoGoEgoBaseRepresentation baseRepresentation);
	
	/**
	 * Retrieve a class loader for a ClassLoader plugin installed 
	 * on this server.  If there are multiple versions, it will 
	 * pull the most recently updated version.
	 * @param bundleName the name of the OSGi bundle
	 * @return the class loader
	 */
	public ClassLoader getClassLoaderPlugin(String bundleName);
	
	/**
	 * Retrieve a class loader for the ClassLoader plugin installed 
	 * on this server that fuilfills the specified minimum version 
	 * at the lowest level possible.
	 * @param bundleName the name of the OSGi bundle
	 * @param minimumVersion the minimum version of this plugin
	 * @return the class loader
	 */
	public ClassLoader getClassLoaderPlugin(String bundleName, String minimumVersion);
	
	public Map<String, Map<String, String>> getClassLoaderPluginMetadata(String bundleName);
	
	/**
	 * Retrieve an image manipulator helper, which provides access to 
	 * focused, mundance operations for resizing images.  For more 
	 * complex image manipulation operations, access the ImageManipulator 
	 * itself. 
	 * @param context the context
	 * @return the image manipulator helper
	 */
	public ImageManipulatorHelper getImageManipulatorHelper(Context context);
	
	/**
	 * Retrieve an image manipulator, used mainly for resizing images.  
	 * If there are multiple versions, it will pull the most 
	 * recently updated version.
	 * @param context the context
	 * @return the image manipulator
	 */	
	public ImageManipulator getImageManipulatorPlugin(Context context);
	
	/**
	 * Retrieve an image manipulator, used mainly for resizing images.  
	 * If there are multiple versions, it will pull the most 
	 * recently updated version OR the best matching version given 
	 * your preferences
	 * @param context the context
	 * @param properties your image manipulator preferences
	 * @return the image manipulator
	 */
	public ImageManipulator getImageManipulatorPlugin(Context context, ImageManipulatorPreferences properties);
	
	/**
	 * Retrieve an image manipulator, used mainly for resizing images.  
	 * If there are multiple versions, it will pull your preferred 
	 * version if available, OR the most recently updated version OR 
	 * the best matching version.
	 * @param context the context
	 * @param properties your image manipulator preferences
	 * @param preferred preferred image manipulator bundle name
	 * @return the image manipulator
	 */
	public ImageManipulator getImageManipulatorPlugin(Context context, ImageManipulatorPreferences properties, String preferred);
	
	/**
	 * Get the OSGi bundle context.
	 * @return the bundle context
	 */
	public BundleContext getBundleContext();
	
	/**
	 * Get a GoGoEgo Application as is installed on a given site 
	 * based on the registration key
	 * @param context the context
	 * @param registrationKey the registration key (bundle symbolic name) 
	 * @return the GoGoEgo Application
	 */
	public GoGoEgoApplication getApplication(Context context, String registrationKey);
	
	/**
	 * Adds a security cookie that can be verified against later.
	 * @param cookieSetting
	 */
	public void addCookie(CookieSetting cookieSetting);
	
	/**
	 * Get the server application.  GoGoEgo Application have 
	 * instant access to this via the "app" variable, but it 
	 * may be useful in other contexts as well.
	 * @param context the context
	 * @return the api to the server application
	 */
	public ServerApplicationAPI getFromContext(Context context);
	
	/**
	 * This mechanism acquires storage appropriate to the site servicing the
	 * current thread.  This is useful for plugins that need persistent disk
	 * storage to support libraries that produce indices, databases, etc.
	 * 
	 * By default, this space is allocated adjacent to the vfs root for the site,
	 * in "storage/[key]" where the key should be specific to the requesting
	 * bundle to minimize collisions.
	 * 
	 * However, this layout is not guaranteed; a more complex installation may
	 * provide physically separate storage from the VFS.
	 * 
	 * @deprecated Ideally, you don't want to use filesystem access, but File
	 *   centric existing libraries may force it upon you.
	 * @param key A bundle specific key, e.g. "org.apache.lucene.index"
	 * @return A guaranteed site-specific Directory for arbitrary storage.
	 */
	public File getCurrentFileStorage(String key);

}
