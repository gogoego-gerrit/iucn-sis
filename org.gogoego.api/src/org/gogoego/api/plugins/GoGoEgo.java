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
import java.util.Properties;

import org.gogoego.api.applications.GoGoEgoApplication;
import org.gogoego.api.applications.ServerApplicationAPI;
import org.gogoego.api.applications.TemplateDataAPI;
import org.gogoego.api.caching.CacheHandler;
import org.gogoego.api.debugging.GoGoDebugger;
import org.gogoego.api.images.ImageManipulator;
import org.gogoego.api.images.ImageManipulatorHelper;
import org.gogoego.api.images.ImageManipulatorPreferences;
import org.gogoego.api.representations.GoGoEgoBaseRepresentation;
import org.gogoego.api.utils.ProductProperties;
import org.osgi.framework.BundleContext;
import org.restlet.Context;
import org.restlet.data.CookieSetting;

import com.solertium.vfs.NotFoundException;

public final class GoGoEgo implements GoGoEgoAPI {
	
	private static GoGoEgo impl;
	
	public static void build(GoGoBackboneAPI backbone) {
		if (impl == null)
			impl = new GoGoEgo(backbone);
	}
	
	public static GoGoEgoAPI get() {
		return impl;
	}
	
	
	public static CacheHandler getCacheHandler() {
		return impl.backbone.getCacheHandler();
	}
	
	/**
	 * Returns a default GoGoDebugger object that will 
	 * log output to the site's console.
	 * @return
	 */
	public static GoGoDebugger debug() {
		return debug("debug");
	}
	
	/**
	 * Returns the specified GoGoDebugger object that will 
	 * log output to the site's console.
	 * @return
	 */
	public static GoGoDebugger debug(String name) {
		return impl.backbone.getDebugger(name);
	}
	
	/**
	 * Returns an immutable set of configuration properties 
	 * for GoGoEgo.
	 * @return
	 */
	public static Properties getInitProperties() {
		return ProductProperties.getProperties();
	}
	
	private final GoGoBackboneAPI backbone;
	
	private GoGoEgo(GoGoBackboneAPI backbone) {
		this.backbone = backbone;
	}
	
	public ClassLoader getClassLoaderPlugin(String bundleName) {
		return backbone.getClassLoaderBroker().getPlugin(bundleName);
	}
	
	public ClassLoader getClassLoaderPlugin(String bundleName, String minimumVersion) {
		return backbone.getClassLoaderBroker().getPlugin(bundleName, minimumVersion);
	}
	
	public ImageManipulatorHelper getImageManipulatorHelper(Context context) {
		return backbone.getImageManipulatorHelper(context);
	}
	
	public ImageManipulator getImageManipulatorPlugin(Context context) {
		return getImageManipulatorPlugin(context, null);
	}
	
	public ImageManipulator getImageManipulatorPlugin(Context context, ImageManipulatorPreferences properties) {
		return getImageManipulatorPlugin(context, properties, null);
	}
	
	public ImageManipulator getImageManipulatorPlugin(Context context, ImageManipulatorPreferences properties, String preferred) {
		return backbone.getImageManipulatorBroker().getPlugin(properties, preferred).newInstance(backbone.getFromContext(context));
	}
	
	public BundleContext getBundleContext() {
		return backbone.getBundleContext();
	}
	
	public GoGoEgoBaseRepresentation applyTemplating(final String templateKey, final GoGoEgoBaseRepresentation baseRepresentation) throws NotFoundException {
		return backbone.applyTemplating(templateKey, baseRepresentation);
	}
	
	/**
	 * Given the template data and the base representation, template the content
	 * @param template the template data
	 * @param baseRepresentation the base representation
	 * @return the new templated content
	 */
	public GoGoEgoBaseRepresentation applyTemplating(final TemplateDataAPI template, final GoGoEgoBaseRepresentation baseRepresentation) {
		return backbone.applyTemplating(template, baseRepresentation);		
	}
	
	/**
	 * Given two representations, template the first with the second.  A registered 
	 * template need not be used here.
	 * @param templateRepresentation the template
	 * @param baseRepresentation the base
	 * @return the new templated content
	 */
	public GoGoEgoBaseRepresentation applyTemplating(final GoGoEgoBaseRepresentation templateRepresentation, final GoGoEgoBaseRepresentation baseRepresentation) {
		return backbone.applyTemplating(templateRepresentation, baseRepresentation);
	}
	
	public ServerApplicationAPI getFromContext(Context context) {
		return backbone.getFromContext(context);
	}
	
	public GoGoEgoApplication getApplication(Context context, String registrationKey) {
		return backbone.getApplication(context, registrationKey);
	}
	
	public void addCookie(CookieSetting cookieSetting) {
		backbone.addCookie(cookieSetting);
	}
	
	public File getCurrentFileStorage(String key) {
		return backbone.getCurrentFileStorage(key);
	}

}
