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

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;

import org.gogoego.api.utils.DocumentUtils;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.Resource;
import org.restlet.resource.ResourceException;
import org.w3c.dom.Document;

import com.solertium.gogoego.server.GoGoDebug;
import com.solertium.gogoego.server.ServerApplication;
import com.solertium.gogoego.server.lib.app.exporter.container.ExporterApplication;
import com.solertium.gogoego.server.lib.app.exporter.schemas.ExporterSimpleFetcherClass;
import com.solertium.gogoego.server.lib.app.exporter.utils.ExportInstance;
import com.solertium.gogoego.server.lib.app.exporter.utils.ExporterConstants;
import com.solertium.gogoego.server.lib.app.exporter.workers.ExporterWorker;
import com.solertium.util.SchemaValidator;
import com.solertium.util.TrivialExceptionHandler;
import com.solertium.vfs.VFS;
import com.solertium.vfs.VFSPath;
import com.solertium.vfs.VFSPathToken;

/**
 * ExporterSettings.java
 * 
 * @author carl.scott
 * 
 */
public class ExporterSettings extends Resource {

	private final ExportInstance exportInstance;
	private final VFS vfs;
	private final String worker;

	public ExporterSettings(Context context, Request request, Response response) {
		super(context, request, response);
		setModifiable(true);

		exportInstance = ((ExporterApplication)ServerApplication.getFromContext(getContext(), ExporterApplication.REGISTRATION)).getExportInstance();
		vfs = ((ExporterApplication)ServerApplication.getFromContext(getContext(), ExporterApplication.REGISTRATION)).getVFS();

		worker = (String) request.getAttributes().get("worker");

		getVariants().add(new Variant(MediaType.TEXT_XML));
	}

	private VFSPath getWorkerConfigPath() {
		return new VFSPath(ExporterConstants.CONFIG_DIR).child(new VFSPathToken(worker)).child(
				new VFSPathToken("config.xml"));
	}

	public Representation represent(Variant variant) throws ResourceException {
		Document document;
		if (worker == null) {
			try {
				document = DocumentUtils.getReadOnlyDocument(new VFSPath(ExporterConstants.CONFIG_FILE), vfs);
			} catch (IOException e) {
				e.printStackTrace();
				document = DocumentUtils.impl.newDocument();
				document.appendChild(document.createElement("root"));
			}
		} else {
			try {
				document = DocumentUtils.getReadOnlyDocument(getWorkerConfigPath(), vfs);
			} catch (IOException e) {
				e.printStackTrace();
				throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);
			}
		}

		return new DomRepresentation(variant.getMediaType(), document);
	}

	/**
	 * This will overwrite an existing settings configuration, or it will
	 * overwrite the listing of ExportInstance's ExporterWorkers.
	 */
	public void storeRepresentation(Representation entity) throws ResourceException {
		// FIXME: NEEDS TO CALL THE WORKERS UPDATESETTINGS FUNCTION

		if (!getRequest().isEntityAvailable())
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "No entity supplied.");

		if (worker == null) {
			saveMainConfig(entity);
		} else {
			ExporterWorker exporterWorker = exportInstance.getWorker(worker);
			if (exporterWorker == null)
				throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Worker " + worker + " does not exist.");

			/*
			 * This is the new document that will overwrite the old one. If it
			 * can not be read or the document can not be loaded because it's
			 * invalid, the ExporterWorker should reject the document, causing
			 * an error to be returned to the client. Otherwise, the document
			 * gets saved.
			 */
			ExporterWorker installedWorker = ExporterInstallerRestlet.install(vfs, worker, getRequest());
			if (installedWorker != null) {
				exportInstance.updateWorker(worker, installedWorker);				
				getResponse().setStatus(Status.SUCCESS_OK);
				getResponse().setEntity(
						new DomRepresentation(MediaType.TEXT_XML, DocumentUtils.impl.createConfirmDocument("Exporter "
								+ "configuration saved for " + worker + ".")));
			} else {
				getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
				getResponse().setEntity(
						new DomRepresentation(MediaType.TEXT_XML, DocumentUtils.impl.createErrorDocument("Exporter "
								+ "configuration could not be saved for " + worker + ".")));
			}
		}
	}

	private void saveMainConfig(Representation entity) throws ResourceException {
		Reader dataReader;
		String xml;
		try {
			// Should be OK for the amount of data being read.
			xml = entity.getText();
			dataReader = new StringReader(xml);
		} catch (Exception e) {
			throw new ResourceException(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY);
		}

		InputStream schemaReader;
		try {
			schemaReader = ExporterSimpleFetcherClass.getResource("config.xsd");
		} catch (IOException e) {
			schemaReader = null;
			GoGoDebug.get("debug").println("Schema not found, continuing...");
		}

		if (schemaReader != null) {
			if (!SchemaValidator.isValid(schemaReader, dataReader))
				throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,
						"Document does not meet schema requirements");

			try {
				schemaReader.close();
			} catch (IOException e) {
				TrivialExceptionHandler.ignore(this, e);
			}
		}

		try {
			dataReader.close();
		} catch (IOException e) {
			TrivialExceptionHandler.ignore(this, e);
		}

		// It's valid, write it to file:
		try {
			Document document = DocumentUtils.impl.createDocumentFromString(xml);
			DocumentUtils.writeVFSFile(ExporterConstants.CONFIG_FILE, vfs, document);
		} catch (Exception impossible) {
			impossible.printStackTrace();
			throw new ResourceException(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY, impossible);
		}

		if (exportInstance.configure(true)) {
			getResponse().setStatus(Status.SUCCESS_OK);
			getResponse().setEntity(
					new DomRepresentation(MediaType.TEXT_XML, DocumentUtils.impl
							.createConfirmDocument("Exporter configuration saved.")));
		} else {
			getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
			getResponse().setEntity(
					new DomRepresentation(MediaType.TEXT_XML, DocumentUtils.impl
							.createErrorDocument("Exporter configuration could not be saved.")));
		}
	}

}
