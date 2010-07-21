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
package com.solertium.gogoego.server.lib.app.exporter.resources;

import org.gogoego.api.applications.GoGoEgoApplicationException;
import org.gogoego.api.utils.DocumentUtils;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.resource.ResourceException;
import org.w3c.dom.Document;

import com.solertium.gogoego.server.ServerApplication;
import com.solertium.gogoego.server.lib.app.exporter.container.ExporterApplication;
import com.solertium.gogoego.server.lib.app.exporter.container.ExporterWorkerFactory;
import com.solertium.gogoego.server.lib.app.exporter.utils.ExportInstance;
import com.solertium.gogoego.server.lib.app.exporter.utils.ExporterConstants;
import com.solertium.gogoego.server.lib.app.exporter.utils.SimpleExporterSettings;
import com.solertium.gogoego.server.lib.app.exporter.workers.ExporterWorker;
import com.solertium.vfs.VFS;
import com.solertium.vfs.VFSPath;
import com.solertium.vfs.VFSPathToken;

/**
 * ExporterInstallerRestlet.java
 * 
 * Install exporters.
 * 
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a href="http://www.solertium.com">Solertium Corporation</a>
 *
 */
public class ExporterInstallerRestlet extends Restlet {

	protected VFS vfs;

	public ExporterInstallerRestlet(Context context, VFS vfs) {
		super(context);
		this.vfs = vfs;
	}

	public void handle(Request request, Response response) {
		final String protocol = (String) request.getAttributes().get("protocol");
		final String exporter = (String) request.getAttributes().get("exporter");

		final Method method = request.getMethod();
		if (!method.equals(Method.PUT)) {
			response.setStatus(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
			response.setEntity(new DomRepresentation(MediaType.TEXT_XML, DocumentUtils.impl
					.createErrorDocument("Method not allowed: " + method)));
		} else {
			final ExportInstance instance = ((ExporterApplication)ServerApplication.getFromContext(getContext(), ExporterApplication.REGISTRATION)).getExportInstance();
			final ExporterWorker worker;

			if ("install".equals(protocol)) {
				try {
					worker = install(vfs, exporter, request);
				} catch (ResourceException e) {
					e.printStackTrace();
					response.setStatus(e.getStatus());
					return;
				}

				if (instance.addWorker(exporter, worker))
					response.setStatus(Status.SUCCESS_CREATED);
				else {
					response.setStatus(Status.SERVER_ERROR_INTERNAL);
					response.setEntity(new DomRepresentation(MediaType.TEXT_XML, DocumentUtils.impl
							.createErrorDocument(exporter + " was installed but could not be registered.")));
				}
			} else if ("uninstall".equals(protocol)) {
				if (instance.getWorker(exporter) == null) {
					response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
					return;
				}
				try {
					uninstall(exporter);
				} catch (ResourceException e) {
					response.setStatus(e.getStatus());
				}

				if (instance.removeWorker(exporter))
					response.setStatus(Status.SUCCESS_OK);
				else {
					response.setStatus(Status.SERVER_ERROR_INTERNAL);
					response.setEntity(new DomRepresentation(MediaType.TEXT_XML, DocumentUtils.impl
							.createErrorDocument(exporter + " was uninstalled but could not be un-registered.")));
				}
			} else {
				response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
				response.setEntity(new DomRepresentation(MediaType.TEXT_XML, DocumentUtils.impl
						.createErrorDocument("Invalid protocol: " + protocol)));
			}
		}
	}

	/**
	 * Calls install. If there are any errors, resource exceptions are thrown.
	 * Upon successful install, return the newly created ExporterWorker.
	 * 
	 * @param workerID
	 *            the id
	 * @param request
	 *            the request
	 * @return the new worker
	 * @throws ResourceException
	 */
	public static ExporterWorker install(VFS vfs, String workerID, Request request) throws ResourceException {
		final ExporterWorkerFactory factory = ExporterApplication.broker.getPlugin(workerID);
		if (factory == null)
			throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "Worker " + workerID + " is not registered.");

		final Document document;
		try {
			document = new DomRepresentation(request.getEntity()).getDocument();
		} catch (Exception e) {
			throw new ResourceException(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY);
		}

		/*
		 * The worker should now validate the file, and, if correct, write all
		 * necessary files to the file system in order for a loadConfig call to
		 * succeed...
		 */
		final VFSPath homeDirectory = new VFSPath(ExporterConstants.CONFIG_DIR).child(new VFSPathToken(workerID));
		final SimpleExporterSettings initSettings = new SimpleExporterSettings();
		if (!initSettings.loadConfig(document, factory.getManagement().getRequiredSettings()))
			throw new ResourceException(Status.CLIENT_ERROR_EXPECTATION_FAILED, "Not all required fields were supplied.");
		
		try {
			factory.getManagement().install(vfs, homeDirectory, initSettings);
		} catch (GoGoEgoApplicationException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Could not install exporter: " + e.getMessage());
		}

		final ExporterWorker installedWorker = factory.newInstance(vfs);
		try {
			installedWorker.init(homeDirectory, initSettings);
		} catch (GoGoEgoApplicationException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Installed but could not initialize worker: " + e.getMessage());
		}
		
		return installedWorker;
	}

	/**
	 * Calls uninstall. if there are any errors, resource exceptions are thrown.
	 * Otherwise, it's assumed that all went according to plan
	 * 
	 * @param worker
	 *            the worker to uninstall, may be null.
	 * @throws ResourceException
	 */
	private void uninstall(String workerID) throws ResourceException {
		final ExporterWorkerFactory factory = ExporterApplication.broker.getPlugin(workerID);
		if (factory == null)
			throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "Worker " + workerID + " is not registered.");
		
		final VFSPath homeDirectory = new VFSPath(ExporterConstants.CONFIG_DIR).child(new VFSPathToken(workerID));
		try {
			factory.getManagement().uninstall(vfs, homeDirectory);
		} catch (GoGoEgoApplicationException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
		}
	}

}
