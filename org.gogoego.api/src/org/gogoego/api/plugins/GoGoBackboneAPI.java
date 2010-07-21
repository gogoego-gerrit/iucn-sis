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

import org.gogoego.api.applications.GoGoEgoApplication;
import org.gogoego.api.applications.GoGoEgoApplicationFactory;
import org.gogoego.api.applications.ServerApplicationAPI;
import org.gogoego.api.applications.TemplateDataAPI;
import org.gogoego.api.authentication.AuthenticatorFactory;
import org.gogoego.api.caching.CacheHandler;
import org.gogoego.api.debugging.GoGoDebugger;
import org.gogoego.api.images.ImageManipulatorFactory;
import org.gogoego.api.images.ImageManipulatorHelper;
import org.gogoego.api.images.ImageManipulatorPreferences;
import org.gogoego.api.representations.GoGoEgoBaseRepresentation;
import org.gogoego.api.scripting.ScriptableObjectFactory;
import org.gogoego.api.utils.BestMatchPluginBroker;
import org.gogoego.api.utils.PluginBroker;
import org.osgi.framework.BundleContext;
import org.restlet.Context;
import org.restlet.data.CookieSetting;

import com.solertium.vfs.NotFoundException;

public interface GoGoBackboneAPI {
	
	public void addCookie(CookieSetting cookieSetting);
	
	public BestMatchPluginBroker<ImageManipulatorFactory, ImageManipulatorPreferences> getImageManipulatorBroker();
	
	public PluginBroker<ScriptableObjectFactory> getScriptableObjectBroker();

	public PluginBroker<GoGoEgoApplicationFactory> getGoGoEgoApplicationBroker();
	
	public PluginBroker<ClassLoader> getClassLoaderBroker();
	
	public PluginBroker<AuthenticatorFactory> getAuthenticatorBroker();
	
	public BundleContext getBundleContext();
	
	public GoGoDebugger getDebugger(String name);
	
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
	
	public GoGoEgoApplication getApplication(Context context, String registrationKey);
	
	public ServerApplicationAPI getFromContext(Context context);
	
	public File getCurrentFileStorage(String key);
	
	public ImageManipulatorHelper getImageManipulatorHelper(Context context);
	
	public CacheHandler getCacheHandler();

}
