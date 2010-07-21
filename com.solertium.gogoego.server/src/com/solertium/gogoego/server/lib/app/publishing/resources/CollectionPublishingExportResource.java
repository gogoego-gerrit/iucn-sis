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
package com.solertium.gogoego.server.lib.app.publishing.resources;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Properties;

import org.gogoego.api.plugins.GoGoEgo;
import org.gogoego.api.representations.GoGoEgoDomRepresentation;
import org.gogoego.api.representations.GoGoEgoInputRepresentation;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.Resource;
import org.restlet.resource.ResourceException;

import com.solertium.gogoego.server.lib.app.publishing.container.CollectionPublishingSettings;
import com.solertium.gogoego.server.lib.app.publishing.container.PublishingApplication;
import com.solertium.gogoego.server.lib.app.publishing.worker.CollectionPublisher;
import com.solertium.gogoego.server.lib.app.publishing.worker.OnPublishNotifier;
import com.solertium.util.BaseDocumentUtils;
import com.solertium.util.TrivialExceptionHandler;
import com.solertium.util.restlet.InternalRequest;
import com.solertium.vfs.VFSPath;
import com.solertium.vfs.restlet.VFSResource;

public class CollectionPublishingExportResource extends Resource {
	
	private final VFSPath uri;
	private final CollectionPublishingSettings settings;

	public CollectionPublishingExportResource(Context context, Request request,
			Response response) {
		super(context, request, response);
		setModifiable(true);
		
		VFSPath uri = null;
		try { 
			uri = VFSResource.decodeVFSPath(request.getResourceRef().getRemainingPart());
		} catch (Exception e) {
			TrivialExceptionHandler.ignore(this, e);
		}
		
		this.uri = uri;
		this.settings = ((PublishingApplication)GoGoEgo.
				get().getApplication(context, PublishingApplication.REGISTRATION)).getSettings();
		
		getVariants().add(new Variant(MediaType.TEXT_XML));
	}
	
	@Override
	public void handlePost() {
		try {
			acceptRepresentation(getRequest().getEntity());
		} catch (ResourceException e) {
			e.printStackTrace();
			getResponse().setStatus(e.getStatus());
		}
	}
	
	public Representation represent(Variant variant) throws ResourceException {
		acceptRepresentation(null);
		
		return getResponse().getEntity();
	}
	
	@Override
	public void acceptRepresentation(Representation entity) throws ResourceException {
		if (uri == null)
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Invalid URI supplied.");
		
		final CollectionPublisher publisher = new CollectionPublisher(getContext());
		final Representation out = publisher.publish(uri);
		final Properties instructions = publisher.getInstructions();
		
		final String key = settings.getSetting("key");
		final String server = "http://" + settings.getSetting("target");
		
		final ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			instructions.store(os, null);
			os.close();
		} catch (Exception e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
		
		final String uri = server + "/apps/"+PublishingApplication.REGISTRATION+"/import?key=" + key;
		final Request request = new InternalRequest(getRequest(), Method.POST, uri, new GoGoEgoInputRepresentation(new ByteArrayInputStream(os.toByteArray()), MediaType.TEXT_PLAIN));
		
		final Client client = new Client(request.getProtocol());
		
		final Response response = client.handle(request);
		
		//Could not reach the server or the app
		if (Status.CLIENT_ERROR_NOT_FOUND.equals(response.getStatus())) {
			getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			getResponse().setEntity(new GoGoEgoDomRepresentation(
				BaseDocumentUtils.impl.createErrorDocument("Publishing Application not " +
					"installed on target server, or target server could not be reached.")
			));
		}
		//Found server, key is not correct
		else if (Status.CLIENT_ERROR_FORBIDDEN.equals(response.getStatus())) {
			getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN);
			getResponse().setEntity(new GoGoEgoDomRepresentation(
				BaseDocumentUtils.impl.createErrorDocument("Publishing Application not " +
					"installed on target server, or target server could not be reached.")
			));
		}
		//This is on a different server, must send the whole package
		else if (Status.CLIENT_ERROR_GONE.equals(response.getStatus())) {
			final String full_uri = server + "/apps/"+PublishingApplication.REGISTRATION+
				"/import/full?key=" + key;
			final Request fullRequest = new InternalRequest(getRequest(), Method.POST, full_uri, out);
			final Response fullResponse = client.handle(fullRequest);
			
			getResponse().setStatus(fullResponse.getStatus());
			if (fullResponse.getStatus().isSuccess()) {
				runNotify();
				getResponse().setEntity(new GoGoEgoDomRepresentation(
					BaseDocumentUtils.impl.createErrorDocument("Import successful")	
				));
			}
			else
				getResponse().setEntity(fullResponse.getEntity());	
		}
		else if (response.getStatus().isSuccess()) {
			runNotify();
			getResponse().setStatus(Status.SUCCESS_NO_CONTENT);
		}
		else {
			getResponse().setStatus(response.getStatus());
			if (response.isEntityAvailable())
				getResponse().setEntity(response.getEntity());
			else
				getResponse().setEntity(new GoGoEgoDomRepresentation(
					BaseDocumentUtils.impl.createErrorDocument(response.getStatus().getDescription())	
				));
		}
	}
	
	private void runNotify() {
		final OnPublishNotifier notify = new OnPublishNotifier(settings, getContext());
		
		new Thread(notify).start();
	}

}
