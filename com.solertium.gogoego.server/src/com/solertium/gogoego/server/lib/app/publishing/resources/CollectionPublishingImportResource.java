package com.solertium.gogoego.server.lib.app.publishing.resources;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.gogoego.api.plugins.GoGoEgo;
import org.gogoego.api.representations.GoGoEgoDomRepresentation;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.Resource;
import org.restlet.resource.ResourceException;
import org.w3c.dom.Document;

import com.solertium.gogoego.server.lib.app.publishing.container.CollectionPublishingSettings;
import com.solertium.gogoego.server.lib.app.publishing.container.PublishingApplication;
import com.solertium.gogoego.server.lib.app.publishing.worker.CollectionPublisher;

public class CollectionPublishingImportResource extends Resource {
	
	private final PublishingApplication application;

	public CollectionPublishingImportResource(Context context, Request request,
			Response response) {
		super(context, request, response);
		setModifiable(true);
		
		this.application = (PublishingApplication)GoGoEgo.
				get().getApplication(context, PublishingApplication.REGISTRATION);
		
		getVariants().add(new Variant(MediaType.TEXT_XML));
	}
	
	public void acceptRepresentation(Representation entity) throws ResourceException {
		final InputStream stream;
		try {
			stream = entity.getStream();
		} catch (Exception e) {
			throw new ResourceException(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY, e);
		}
		
		final String key = 
			getRequest().getResourceRef().getQueryAsForm().getFirstValue("key");
		if (!application.getSettings().getSetting("key", "changeme").equals(key))
			throw new ResourceException(Status.CLIENT_ERROR_FORBIDDEN, "Invalid publishing key specified.");
		
		if (getRequest().getResourceRef().getPath().endsWith("full"))
			handleFullPublish(stream);
		else
			handleLocalPublish(stream);
	}
	
	private void handleLocalPublish(final InputStream stream) throws ResourceException {
		final Properties instructions = new Properties();
		try {
			instructions.load(stream);
		} catch (IOException e) {
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Could not load publishing data.");
		}
		
		instructions.list(System.out);
		
		String fileLocation = instructions.getProperty("file");
		if (fileLocation == null)
			throw new ResourceException(Status.CLIENT_ERROR_GONE, "Invalid properties specified");
		
		final File file = new File(fileLocation);
		//This is a "valid" failure
		if (!file.exists())
			throw new ResourceException(Status.CLIENT_ERROR_GONE);
		else {
			try {
				handleFullPublish(new FileInputStream(file));
			} catch (FileNotFoundException impossible) {
				throw new ResourceException(Status.CLIENT_ERROR_GONE);
			}
		}
	}
	
	private void handleFullPublish(final InputStream stream) throws ResourceException {
		if (application.getPublishingMarker().get())
			throw new ResourceException(Status.CLIENT_ERROR_CONFLICT, "Currently publishing, please try again later.");
		
		application.getPublishingMarker().set(true);
		
		final CollectionPublisher publisher = 
			new CollectionPublisher(getContext());
		
		final Document results;
		try {
			results = publisher.importCollection(stream);
		} catch (ResourceException e) {
			throw e;
		} finally  {
			application.getPublishingMarker().set(false);
		}
		
		getResponse().setStatus(Status.SUCCESS_OK);
		getResponse().setEntity(new GoGoEgoDomRepresentation(results));
	}

}
