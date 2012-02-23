package org.iucn.sis.server.extensions.updates.restlets;

import java.io.FileInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.iucn.sis.server.api.restlets.SimpleRestlet;
import org.iucn.sis.server.extensions.updates.lib.SoftwareIO;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ResourceException;

public class SoftwareVersionRestlet extends SimpleRestlet {
	
	private static final SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
	
	public SoftwareVersionRestlet(Context context) {
		super(context);
	}
	
	@Override
	public void definePaths() {
		paths.add("/updates/{version}/{mode}");
	}
	
	@Override
	public Representation handleGet(Request request, Response response) throws ResourceException {
		String mode = getParameter(request, "mode");
		Date version;
		try {
			version = fmt.parse(getParameter(request, "version"));
		} catch (ParseException e) {
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Format must be yyyy-MM-dd", e);
		}
		
		Representation representation;
		if ("list".equals(mode)) {
			return listUpdates(version);
		}
		else if ("zip".equals(mode)) {
			return zipUpdates(version);
		}
		else
			representation = super.handleGet(request, response);
		
		return representation;
	}
	
	private Representation listUpdates(Date date) throws ResourceException {
		SoftwareIO lib = new SoftwareIO();
		try {
			return new StringRepresentation(lib.listUpdates(date).toXML(), MediaType.TEXT_XML);
		} catch (IOException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
	}
	
	private Representation zipUpdates(Date date) throws ResourceException {
		SoftwareIO lib = new SoftwareIO();
		try {
			Representation representation = new InputRepresentation(new FileInputStream(lib.zip(date)));
			representation.setDownloadable(true);
			representation.setDownloadName("updates.zip");
			
			return representation;
		} catch (IOException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e.getMessage(), e);
		}
	}
	
	private String getParameter(Request request, String key) throws ResourceException {
		String value;
		try {
			value = (String)request.getAttributes().get(key);
		} catch (Exception e) {
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, e);
		}
		if (value == null)
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "No parameter specified for " + key);
		
		return value;
	}
	
}
