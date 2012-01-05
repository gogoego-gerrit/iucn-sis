package org.iucn.sis.server.extensions.reports;

import java.util.ArrayList;
import java.util.List;

import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.Resource;
import org.restlet.resource.ResourceException;

import com.solertium.util.restlet.MediaTypeManager;

@SuppressWarnings("deprecation")
public class LocalFileResource extends Resource {
	
	public static List<String> getPaths() {
		List<String> paths = new ArrayList<String>();
		paths.add("/resources/{file}");
		return paths;
	}
	
	private final String file;
	
	public LocalFileResource(Context context, Request request, Response response) {
		super(context, request, response);
		setModifiable(false);
		setReadable(true);
		
		file = (String)request.getAttributes().get("file");
		
		getVariants().add(new Variant(MediaType.IMAGE_ALL));
		getVariants().add(new Variant(MediaType.TEXT_CSS));
	}
	
	@Override
	public Representation represent(Variant variant) throws ResourceException {
		if (file == null)
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
		
		MediaType mt = MediaTypeManager.getMediaType(file);
		
		try {
			return new InputRepresentation(getClass().getResourceAsStream(file), mt);
		} catch (Exception e) {
			throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, e);
		}
	}

}
