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

import java.util.Iterator;

import org.gogoego.api.utils.DocumentUtils;
import org.restlet.Context;
import org.restlet.data.Form;
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
import org.w3c.dom.Element;

import com.solertium.gogoego.server.ServerApplication;
import com.solertium.gogoego.server.lib.app.exporter.container.ExporterApplication;
import com.solertium.gogoego.server.lib.app.exporter.utils.ExportException;
import com.solertium.gogoego.server.lib.app.exporter.workers.ExporterWorker;

/**
 * ExportResource.java
 * 
 * Finds the appropriate ExporterWorker and sends commands to it to perform
 * exporting jobs.
 * 
 * @author carl.scott
 * 
 */
public class ExportResource extends Resource {

	private final ExporterWorker worker;
	private final String command;

	/**
	 * @param context
	 * @param request
	 * @param response
	 */
	public ExportResource(Context context, Request request, Response response) {
		super(context, request, response);
		setModifiable(true);
		this.worker = ((ExporterApplication)ServerApplication.getFromContext(getContext(), ExporterApplication.REGISTRATION)).getExportInstance().getWorker((String) request.getAttributes().get("exporter"));
		this.command = (String) request.getAttributes().get("command");

		getVariants().add(new Variant(MediaType.TEXT_XML));
	}

	public Representation represent(Variant variant) throws ResourceException {
		if (worker == null)
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);

		final Representation representation;
		try {
			final Document document = DocumentUtils.impl.newDocument();
			final Element root = document.createElement("root");
			final Form form = getRequest().getResourceRef().getQueryAsForm();
			final Iterator<String> iterator = form.getNames().iterator();
			while (iterator.hasNext()) {
				String key = iterator.next();
				root.appendChild(DocumentUtils.impl.createElementWithText(document, key, form.getFirstValue(key)));
			}
			document.appendChild(root);

			ServerApplication.getFromContext(getContext()).getScratchResourceBin().showContents();

			representation = worker.doCommand(document, getContext(), command);
		} catch (ExportException e) {
			if (e.getDescription() != null)
				return new DomRepresentation(MediaType.TEXT_XML, DocumentUtils.impl.createErrorDocument(e
						.getDescription()));
			throw new ResourceException(e.getStatus());
		}
		return representation;
	}

	/**
	 * Performs a job, then returns a status document.
	 */
	public void acceptRepresentation(Representation entity) throws ResourceException {
		if (worker == null)
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);

		Document document;
		try {
			document = new DomRepresentation(entity).getDocument();
		} catch (Exception e) {
			document = null;
		}

		final Representation representation;
		try {
			if (command.equals("update"))
				representation = new DomRepresentation(MediaType.TEXT_XML, worker.exportAll(document, getContext()));
			else if (command.equals("file"))
				representation = new DomRepresentation(MediaType.TEXT_XML, worker.exportFile(document, getContext()));
			else if (command.equals("refresh"))
				representation = new DomRepresentation(MediaType.TEXT_XML, worker.refresh(document, getContext()));
			else
				representation = worker.doCommand(document, getContext(), command);
		} catch (ExportException e) {
			if (e.getDescription() != null)
				getResponse().setEntity(
						new DomRepresentation(MediaType.TEXT_XML, DocumentUtils.impl.createErrorDocument(e
								.getDescription())));
			throw new ResourceException(e.getStatus());
		}

		getResponse().setStatus(Status.SUCCESS_OK);
		getResponse().setEntity(representation);
	}

}
