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
package com.solertium.gogoego.server.lib.app.exporter.workers.appengine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Random;

import org.gogoego.api.applications.GoGoEgoApplicationException;
import org.gogoego.api.debugging.GoGoDebugger;
import org.gogoego.api.utils.DocumentUtils;
import org.restlet.Client;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.representation.StringRepresentation;
import org.w3c.dom.Document;

import com.solertium.gogoego.server.GoGoDebug;
import com.solertium.gogoego.server.ServerApplication;
import com.solertium.gogoego.server.lib.app.exporter.utils.ExporterConstants;
import com.solertium.gogoego.server.lib.app.exporter.utils.SimpleExporterSettings;
import com.solertium.gogoego.server.lib.app.exporter.workers.ExporterWorkerManagement;
import com.solertium.gogoego.server.lib.app.exporter.workers.ExporterWorkerMetadata;
import com.solertium.util.BaseDocumentUtils;
import com.solertium.util.TrivialExceptionHandler;
import com.solertium.vfs.ConflictException;
import com.solertium.vfs.NotFoundException;
import com.solertium.vfs.VFS;
import com.solertium.vfs.VFSPath;
import com.solertium.vfs.VFSPathToken;

/**
 * AppEngineExporterManager.java
 * 
 * Manage AppEngine.
 * 
 * @author liz.schwartz
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a
 *  href="http://www.solertium.com">Solertium Corporation</a>
 */
public class AppEngineExporterManager implements ExporterWorkerManagement, ExporterWorkerMetadata {
		
	public Document getSettingsAuthorityUI() {
		return BaseDocumentUtils.impl.getInputStreamFile(getClass().getResourceAsStream("appengine.xml"));
	}
	
	public Collection<String> getRequiredSettings() {
		final ArrayList<String> requiredEntities = new ArrayList<String>();
		requiredEntities.add("linkaddress");
		requiredEntities.add("applicationID");	
		return requiredEntities;
	}
	
	public static VFSPath getVFSInstallPath() {
		return getVFSExporterDirectoryPath().child(new VFSPathToken("install.xml"));
	}
	
	public static VFSPath getVFSConfigXMLPath() {
		return getVFSExporterDirectoryPath().child(new VFSPathToken("config.xml"));
	}
	
	public static VFSPath getVFSTagXMLPath() {
		return getVFSExporterDirectoryPath().child(new VFSPathToken(".tags.xml"));
	}

	public static VFSPath getVFSExporterDirectoryPath() {
		final VFSPath configPath = new VFSPath(ExporterConstants.CONFIG_DIR);
		return configPath.child(new VFSPathToken("com.solertium.gogoego.server.lib.app.exporter.workers.appengine"));
	}
	
	private GoGoDebugger log() {
		return GoGoDebug.get("debug");
	}
	
	public void install(VFS vfs, VFSPath homeFolder, SimpleExporterSettings initSettings) throws GoGoEgoApplicationException {
		final AppEngineSettings settings = new AppEngineSettings(initSettings);
		log().println("This is webaddress {0}", settings.getWebAddress());
		
		/*
		 * TODO: if the applicationID changes, then we need to attempt to 
		 * install, and change our gogoKey.  We should check for this here.
		 */
		
		/*
		 * First attempt to locate a gogoKey.
		 */
		final SimpleExporterSettings privateSettings = new SimpleExporterSettings();
		try {
			privateSettings.loadConfig(vfs.getDocument(getVFSInstallPath()));
		} catch (IOException e) {
			TrivialExceptionHandler.ignore(this, e);
		}
		
		String gogoKey = privateSettings.get("gogoKey");
		
		/*
		 * If the gogoKey is null, we need to install...
		 */
		if (gogoKey == null) {
			log().println("Do need to do install");	

			gogoKey = new Random(new Date().getTime()).nextLong()+"";

			final Request request = new Request(Method.PUT, settings.getInstallAddress(), 
					new StringRepresentation("<xml><gogoKey>" + gogoKey + "</gogoKey><baseLink>" + settings.getLinkAddress()
					+ "</baseLink><applicationID>" + settings.getApplicationID() + "</applicationID></xml>", MediaType.TEXT_XML));
			
			final Client client = new Client(Protocol.HTTPS);
			
			final Response response = client.handle(request);
			if (((ServerApplication) ServerApplication.getCurrent()).isHostedMode() || response.getStatus().isSuccess()) {
				privateSettings.setProperty("gogoKey", gogoKey);
				try {
					if (!vfs.exists(getVFSExporterDirectoryPath())) {
						log().println("Trying to write directory " + getVFSExporterDirectoryPath());
						vfs.makeCollections(getVFSExporterDirectoryPath());
					}					
					VFSPath installPath = getVFSInstallPath();
					log().println("tring to save file at {0}", installPath.toString());
					DocumentUtils.writeVFSFile(installPath, vfs, privateSettings.toXML());
				} catch (NotFoundException e) {
					throw new GoGoEgoApplicationException(e);
				} catch (ConflictException e) {
					throw new GoGoEgoApplicationException(e);
				}
			}
			else
				throw new GoGoEgoApplicationException("Could not install.");
		}
		
		/*
		 * Now write the other settings as normal.
		 */
		
		final VFSPath configPath = getVFSConfigXMLPath();
		
		log().println("trying to write file at {0}" + 
				" where the document says \r\n{1}", configPath, settings.toPublicXML());

		if (!DocumentUtils.writeVFSFile(configPath, vfs, settings.toPublicXML()))
			throw new GoGoEgoApplicationException("Could not save settings");
	}

	public void uninstall(VFS vfs, VFSPath homeFolder) throws GoGoEgoApplicationException {
		final Document document;
		try {
			document = vfs.getDocument(getVFSInstallPath());
		} catch (Exception e) {
			throw new GoGoEgoApplicationException("The config document does not exist!!", e);
		}
		
		final SimpleExporterSettings simpleSettings = new SimpleExporterSettings();
		simpleSettings.loadConfig(document);
		
		final AppEngineSettings settings = new AppEngineSettings(simpleSettings);
		
		Request request = new Request(Method.PUT, settings.getUninstallAddress());
		request.setEntity("<xml><gogoKey>" + settings.getGoGoKey() + "</gogoKey></xml>", MediaType.TEXT_XML);
		Client client = new Client(Protocol.HTTPS);
		Response response = client.handle(request);

		if (((ServerApplication) ServerApplication.getCurrent()).isHostedMode() || response.getStatus().isSuccess()) {
			try {
				vfs.delete(getVFSExporterDirectoryPath());
			} catch (NotFoundException e) {
				TrivialExceptionHandler.ignore(this, e);
			} catch (ConflictException e) {
				throw new GoGoEgoApplicationException(e);				
			}
		} else
			throw new GoGoEgoApplicationException("Could not uninstall.");
	}

	public String getDescription() {
		return "Exports a site to AppEngine";
	}

	public String getName() {
		return "AppEngine";
	}

}
