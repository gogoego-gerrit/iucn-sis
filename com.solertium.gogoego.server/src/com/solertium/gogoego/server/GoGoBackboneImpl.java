/*
 * Copyright (C) 2007-2009 Solertium Corporation
 *
 * This file is part of the open source GoGoEgo project.
 *
 * GoGoEgo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *  
 * GoGoEgo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GoGoEgo.  If not, see http://www.gnu.org/licenses/.
 * 
 * Unless you have been granted a different license in writing by the
 * copyright holders for GoGoEgo, only the GNU General Public License
 * grants you rights to modify or redistribute this code.
 */
package com.solertium.gogoego.server;

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
import org.gogoego.api.plugins.GoGoBackboneAPI;
import org.gogoego.api.representations.GoGoEgoBaseRepresentation;
import org.gogoego.api.scripting.ScriptableObjectFactory;
import org.gogoego.api.utils.BestMatchPluginBroker;
import org.gogoego.api.utils.PluginBroker;
import org.osgi.framework.BundleContext;
import org.restlet.Context;
import org.restlet.data.CookieSetting;

import com.solertium.gogoego.server.applications.ServerApplicationImpl;
import com.solertium.gogoego.server.cm.PluginAgent;
import com.solertium.gogoego.server.lib.caching.CacheHandlerImpl;
import com.solertium.gogoego.server.lib.representations.ImageUtils;
import com.solertium.vfs.NotFoundException;

public class GoGoBackboneImpl implements GoGoBackboneAPI {

	public PluginBroker<ClassLoader> getClassLoaderBroker() {
		return PluginAgent.getClassLoaderBroker();
	}
	
	public BestMatchPluginBroker<ImageManipulatorFactory, ImageManipulatorPreferences> getImageManipulatorBroker() {
		return PluginAgent.getImageManipulatorBroker();
	}

	public PluginBroker<GoGoEgoApplicationFactory> getGoGoEgoApplicationBroker() {
		return PluginAgent.getGoGoEgoApplicationBroker();
	}

	public PluginBroker<ScriptableObjectFactory> getScriptableObjectBroker() {
		return PluginAgent.getScriptableObjectBroker();
	}
	
	public PluginBroker<AuthenticatorFactory> getAuthenticatorBroker() {
		return PluginAgent.getAuthenticatorBroker();
	}
	
	public BundleContext getBundleContext() {
		return PluginAgent.getGoGoEgoApplicationBroker().getBundleContext();
	}
		
	public GoGoDebugger getDebugger(String name) {
		return GoGoDebug.get(name);
	}
	
	public GoGoEgoBaseRepresentation applyTemplating(String templateKey, GoGoEgoBaseRepresentation baseRepresentation)
			throws NotFoundException {
		return GoGoMagicFilter.applyTemplating(templateKey, baseRepresentation);
	}
	
	public GoGoEgoBaseRepresentation applyTemplating(TemplateDataAPI template,
			GoGoEgoBaseRepresentation baseRepresentation) {
		return GoGoMagicFilter.applyTemplating(template, baseRepresentation);
	}
	
	public GoGoEgoBaseRepresentation applyTemplating(GoGoEgoBaseRepresentation templateRepresentation,
			GoGoEgoBaseRepresentation baseRepresentation) {
		return GoGoMagicFilter.applyTemplating(templateRepresentation, baseRepresentation);
	}
	
	public GoGoEgoApplication getApplication(Context context, String registrationKey) {
		return ServerApplication.getFromContext(context, registrationKey);		
	}
	
	public ServerApplicationAPI getFromContext(Context context) {
		return new ServerApplicationImpl(ServerApplication.getFromContext(context));
	}
	
	public void addCookie(CookieSetting cookieSetting) {
		GoGoMagicFilter.addCookie(cookieSetting);
	}
	
	@SuppressWarnings("deprecation")
	public File getCurrentFileStorage(String key) {
		return ServerApplication.getCurrentFileStorage(key);
	}
	
	public ImageManipulatorHelper getImageManipulatorHelper(Context context) {
		return new ImageUtils(context);
	}
	
	public CacheHandler getCacheHandler() {
		return new CacheHandlerImpl();
	}

}
