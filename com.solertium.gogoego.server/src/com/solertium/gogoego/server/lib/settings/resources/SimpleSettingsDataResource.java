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
package com.solertium.gogoego.server.lib.settings.resources;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

import javax.xml.transform.stream.StreamSource;

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

import com.solertium.gogoego.server.lib.settings.base.SimpleSettingsWorker;
import com.solertium.gogoego.server.lib.settings.base.SimpleSettingsWorkerFactory;
import com.solertium.gogoego.server.lib.settings.base.SimpleSettingsWorker.SimpleSettingsWorkerException;
import com.solertium.gogoego.server.lib.settings.schemas.SimpleFetcherClass;
import com.solertium.util.SchemaValidator;
import com.solertium.util.TrivialExceptionHandler;

/**
 * SimpleSettingsDataResource.java
 * 
 * Manages serving and saving for simple settings. It will pass the document PUT
 * to the server and will only involve the worker if the document conforms to
 * schema.
 * 
 * @author carl.scott
 * 
 */
public class SimpleSettingsDataResource extends Resource {

	private final String remaining;
	private final SimpleSettingsWorker worker;

	public SimpleSettingsDataResource(Context context, Request request, Response response) {
		super(context, request, response);
		setModifiable(true);

		this.remaining = (String) request.getAttributes().get("remaining");
		this.worker = SimpleSettingsWorkerFactory.getWorker((String) request.getAttributes().get("worker"), context);

		getVariants().add(new Variant(MediaType.TEXT_XML));
	}

	public Representation represent(Variant variant) throws ResourceException {
		if (worker == null)
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);

		try {
			Representation representation = worker.getData(remaining);
			representation.setMediaType(variant.getMediaType());
			return representation;
		} catch (SimpleSettingsWorker.SimpleSettingsWorkerException e) {
			throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, e);
		}
	}
	
	public void handlePut() {
		try {
			storeRepresentation(getRequest().getEntity());
		} catch (ResourceException e) {
			getResponse().setStatus(e.getStatus());
		}
	}

	public void storeRepresentation(Representation entity) throws ResourceException {
		if (worker == null)
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);

		final Document document = getDocument(entity);

		InputStream source = null;
		boolean couldBeValidated = false, isValid = false;
		try {
			source = SimpleFetcherClass.getResource("simpleSettingsData.xsd");
			isValid = SchemaValidator.isValid(new StreamSource(source), new StringReader(DocumentUtils.impl
					.serializeDocumentToString(document)));
			couldBeValidated = true;
		} catch (Exception e) {
			couldBeValidated = false;
		} finally {
			try {
				source.close();
			} catch (IOException e) {
				TrivialExceptionHandler.ignore(this, e);
			}
		}

		if (couldBeValidated && !isValid)
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Invalid document");

		try {
			worker.setData(remaining, document);
			getResponse().setStatus(Status.SUCCESS_CREATED);
		} catch (SimpleSettingsWorkerException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
	}

	private Document getDocument(Representation entity) throws ResourceException {
		try {
			return new DomRepresentation(entity).getDocument();
		} catch (Exception e) {
			throw new ResourceException(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY);
		}
	}

}
