package com.solertium.gogoego.server.lib.app.publishing.resources;

import org.gogoego.api.collections.CollectionCache;
import org.gogoego.api.plugins.GoGoEgo;
import org.gogoego.api.representations.GoGoEgoDomRepresentation;
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

import com.solertium.gogoego.server.GoGoMagicFilter;
import com.solertium.gogoego.server.lib.app.publishing.container.PublishingApplication;
import com.solertium.gogoego.server.lib.caching.MemoryCache;
import com.solertium.util.BaseDocumentUtils;

public class CollectionPublishingNotificationResource extends Resource {
	
	private final PublishingApplication application;

	public CollectionPublishingNotificationResource(Context context,
			Request request, Response response) {
		super(context, request, response);

		this.application = (PublishingApplication)GoGoEgo.
		get().getApplication(context, PublishingApplication.REGISTRATION);

		getVariants().add(new Variant(MediaType.TEXT_XML));
	}
	
	@Override
	public Representation represent(Variant variant) throws ResourceException {
		final String key = 
			getRequest().getResourceRef().getQueryAsForm().getFirstValue("key");
		if (!application.getSettings().getSetting("key", "changeme").equals(key))
			throw new ResourceException(Status.CLIENT_ERROR_FORBIDDEN, "Invalid publishing key specified.");
		
		//FIXME: if this ever moves standalone, this will break, expose public API
		CollectionCache.getInstance().invalidateInstance(getContext());
		MemoryCache.getInstance().clear(getContext());
		MemoryCache.getInstance().getLandlord(getContext()).invalidateAll(getContext());
		
		return new GoGoEgoDomRepresentation(BaseDocumentUtils.impl.createConfirmDocument("Notification accepted, cache cleared."));
	}

}
